package com.example.portfolio.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {
        "app.admin.seed.enabled=true",
        "app.admin.seed.email=integration-admin@example.com",
        "app.admin.seed.password=integration-password",
        "app.auth.jwt.secret=integration-test-secret-that-is-long-enough-for-hmac",
        "app.auth.jwt.access-token-minutes=30"
})
@AutoConfigureMockMvc
class CriticalWorkflowIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authLoginRefreshMeAndAuditFlowWorks() throws Exception {
        JsonNode login = login();
        String accessToken = login.get("accessToken").asText();
        String refreshToken = login.get("refreshToken").asText();

        mockMvc.perform(get("/auth/me").header(AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration-admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        JsonNode refreshed = readJson(mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn());

        mockMvc.perform(post("/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object("refreshToken", refreshed.get("refreshToken").asText()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revoked").value(true));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());

        String auditToken = login().get("accessToken").asText();
        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("action", "LOGIN_SUCCESS")
                        .header(AUTHORIZATION, bearer(auditToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$[0].result").value("SUCCESS"));
    }

    @Test
    void projectAndNotePublishWorkflowsControlPublicVisibility() throws Exception {
        String token = login().get("accessToken").asText();
        long technologyId = createTechnology(token, unique("spring-boot"));
        long categoryId = createCategory(token, unique("backend"));

        String projectSlug = unique("portfolio-api");
        long projectId = createDraftProject(token, projectSlug);
        mockMvc.perform(get("/public/projects/{slug}", projectSlug))
                .andExpect(status().isNotFound());

        publishProject(token, projectId, projectSlug, technologyId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.technologies[0].id").value((int) technologyId));

        mockMvc.perform(get("/public/projects/{slug}", projectSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(projectSlug))
                .andExpect(jsonPath("$.contentStatus").value("PUBLISHED"));

        String noteSlug = unique("jwt-sessions");
        long noteId = createDraftNote(token, noteSlug);
        mockMvc.perform(get("/public/notes/{slug}", noteSlug))
                .andExpect(status().isNotFound());

        publishNote(token, noteId, noteSlug, categoryId, technologyId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.category.id").value((int) categoryId));

        mockMvc.perform(get("/public/notes/{slug}", noteSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(noteSlug))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void cvActiveRulePublishesOnlyLatestActiveCvForRole() throws Exception {
        String token = login().get("accessToken").asText();
        String targetRole = unique("backend-developer");

        long firstId = uploadCv(token, targetRole, "v1", "cv-v1.pdf");
        mockMvc.perform(patch("/api/admin/cv-files/{id}/activate", firstId)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        long secondId = uploadCv(token, targetRole, "v2", "cv-v2.pdf");
        mockMvc.perform(patch("/api/admin/cv-files/{id}/activate", secondId)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/public/cv/download")
                        .param("targetRole", targetRole))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"cv-v2.pdf\""))
                .andExpect(content().bytes(pdfBytes("v2")));

        mockMvc.perform(get("/api/admin/cv-files").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + firstId + ")].status").value("ARCHIVED"))
                .andExpect(jsonPath("$[?(@.id == " + secondId + ")].status").value("ACTIVE"));
    }

    @Test
    void mediaDeleteProtectionBlocksUsedMedia() throws Exception {
        String token = login().get("accessToken").asText();
        long technologyId = createTechnology(token, unique("java"));
        String projectSlug = unique("media-project");
        long projectId = createDraftProject(token, projectSlug);
        publishProject(token, projectId, projectSlug, technologyId).andExpect(status().isOk());

        long mediaAssetId = uploadMedia(token);
        mockMvc.perform(post("/api/admin/media-assets/{id}/usages", mediaAssetId)
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object(
                                "entityType", "PROJECT",
                                "entityId", projectId,
                                "usageType", "COVER_IMAGE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityType").value("PROJECT"));

        mockMvc.perform(delete("/api/admin/media-assets/{id}", mediaAssetId)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Media asset is used and cannot be deleted. Usages: 1"));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("action", "MEDIA_USAGE_ATTACH")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("MEDIA_USAGE_ATTACH"))
                .andExpect(jsonPath("$[0].result").value("SUCCESS"));
    }

    private JsonNode login() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object(
                                "email", "integration-admin@example.com",
                                "password", "integration-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();
        return readJson(result);
    }

    private long createTechnology(String token, String slug) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/technologies")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object(
                                "name", "Spring Boot " + slug,
                                "slug", slug,
                                "type", "FRAMEWORK",
                                "status", "ACTIVE",
                                "description", "Backend framework",
                                "howIUseIt", "Build REST APIs",
                                "core", true,
                                "displayOrder", 0))))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private long createCategory(String token, String slug) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/categories")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(object(
                                "name", "Backend " + slug,
                                "slug", slug,
                                "description", "Backend notes",
                                "status", "ACTIVE",
                                "displayOrder", 0))))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private long createDraftProject(String token, String slug) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/projects")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(projectRequest(slug, "DRAFT", null, null, new long[0])))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions publishProject(
            String token,
            long id,
            String slug,
            long technologyId) throws Exception {
        return mockMvc.perform(put("/api/admin/projects/{id}", id)
                .header(AUTHORIZATION, bearer(token))
                .contentType(APPLICATION_JSON)
                .content(projectRequest(slug, "PUBLISHED", "Project SEO", "Project SEO description", new long[] {technologyId})));
    }

    private long createDraftNote(String token, String slug) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/notes")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(noteRequest(slug, "DRAFT", null, null, null, new long[0])))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions publishNote(
            String token,
            long id,
            String slug,
            long categoryId,
            long technologyId) throws Exception {
        return mockMvc.perform(put("/api/admin/notes/{id}", id)
                .header(AUTHORIZATION, bearer(token))
                .contentType(APPLICATION_JSON)
                .content(noteRequest(slug, "PUBLISHED", categoryId, "Note SEO", "Note SEO description", new long[] {technologyId})));
    }

    private String projectRequest(
            String slug,
            String status,
            String seoTitle,
            String seoDescription,
            long[] technologyIds) throws Exception {
        ObjectNode node = object(
                "title", "Portfolio API " + slug,
                "slug", slug,
                "language", "EN",
                "summary", "A portfolio API summary.",
                "description", "A portfolio API description.",
                "role", "Backend developer",
                "projectType", "FULL_STACK",
                "projectStatus", "COMPLETED",
                "contentStatus", status,
                "problemStatement", "Need a CMS.",
                "solutionOverview", "Build a Spring Boot API.",
                "backendHighlights", "JWT and audit logs.",
                "frontendHighlights", "React admin.",
                "architectureNotes", "Layered architecture.",
                "sourceUrl", "https://github.com/example/project",
                "demoUrl", "https://example.com",
                "seoTitle", seoTitle,
                "seoDescription", seoDescription,
                "displayOrder", 0,
                "tagIds", objectMapper.createArrayNode(),
                "noteIds", objectMapper.createArrayNode());
        node.set("technologyIds", ids(technologyIds));
        return objectMapper.writeValueAsString(node);
    }

    private String noteRequest(
            String slug,
            String status,
            Long categoryId,
            String seoTitle,
            String seoDescription,
            long[] technologyIds) throws Exception {
        ObjectNode node = object(
                "title", "JWT Sessions " + slug,
                "slug", slug,
                "language", "EN",
                "excerpt", "JWT session notes.",
                "content", "# JWT Sessions\nA practical note.",
                "categoryId", categoryId,
                "status", status,
                "seoTitle", seoTitle,
                "seoDescription", seoDescription,
                "readingMinutes", 3,
                "displayOrder", 0,
                "tagIds", objectMapper.createArrayNode());
        node.set("technologyIds", ids(technologyIds));
        return objectMapper.writeValueAsString(node);
    }

    private long uploadCv(String token, String targetRole, String version, String filename) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, "application/pdf", pdfBytes(version));
        MvcResult result = mockMvc.perform(multipart("/api/admin/cv-files")
                        .file(file)
                        .param("language", "EN")
                        .param("targetRole", targetRole)
                        .param("version", version)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private long uploadMedia(String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", pngBytes());
        MvcResult result = mockMvc.perform(multipart("/api/admin/media-assets")
                        .file(file)
                        .param("title", "Cover")
                        .param("altText", "Cover alt")
                        .param("caption", "Cover caption")
                        .param("visibility", "PUBLIC")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private ObjectNode object(Object... pairs) {
        assertThat(pairs.length % 2).isZero();
        ObjectNode node = objectMapper.createObjectNode();
        for (int index = 0; index < pairs.length; index += 2) {
            String key = (String) pairs[index];
            Object value = pairs[index + 1];
            node.set(key, objectMapper.valueToTree(value));
        }
        return node;
    }

    private com.fasterxml.jackson.databind.node.ArrayNode ids(long[] ids) {
        var array = objectMapper.createArrayNode();
        for (long id : ids) {
            array.add(id);
        }
        return array;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String unique(String prefix) {
        return prefix + "-" + System.nanoTime();
    }

    private byte[] pdfBytes(String marker) {
        return ("%PDF-1.4\n" + marker + "\n%%EOF").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] pngBytes() {
        return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};
    }
}
