package com.example.portfolio.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.portfolio.audit.AuditAdminController;
import com.example.portfolio.audit.AuditService;
import com.example.portfolio.auth.AuthController;
import com.example.portfolio.auth.AuthService;
import com.example.portfolio.auth.JwtAuthenticationFilter;
import com.example.portfolio.auth.TokenService;
import com.example.portfolio.auth.config.SecurityConfig;
import com.example.portfolio.content.ContentService;
import com.example.portfolio.content.PublicContentController;
import com.example.portfolio.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        AuditAdminController.class,
        AuthController.class,
        PublicContentController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityBoundaryTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ContentService contentService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void adminAuditRouteRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."))
                .andExpect(jsonPath("$.path").value("/api/admin/audit-logs"));
    }

    @Test
    void invalidBearerTokenDoesNotAuthenticateAdminRoute() throws Exception {
        when(tokenService.parseAccessToken("bad-token")).thenThrow(new BadJwtException("bad token"));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void authMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void publicProjectListAllowsAnonymousAccess() throws Exception {
        when(contentService.listPublicProjects()).thenReturn(List.of());

        mockMvc.perform(get("/public/projects"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void configuredCorsOriginCanPreflightAdminApi() throws Exception {
        mockMvc.perform(options("/api/admin/audit-logs")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void loginValidationErrorDoesNotEchoPasswordValue() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"super-secret-password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", not(containsString("super-secret-password"))));
    }
}
