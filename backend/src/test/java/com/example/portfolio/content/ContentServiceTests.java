package com.example.portfolio.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.content.dto.NoteRequest;
import com.example.portfolio.content.dto.ProjectRequest;
import com.example.portfolio.note.TechnicalNote;
import com.example.portfolio.note.TechnicalNoteRepository;
import com.example.portfolio.project.Project;
import com.example.portfolio.project.ProjectLifecycleStatus;
import com.example.portfolio.project.ProjectRepository;
import com.example.portfolio.project.ProjectType;
import com.example.portfolio.taxonomy.Category;
import com.example.portfolio.taxonomy.CategoryRepository;
import com.example.portfolio.taxonomy.TagRepository;
import com.example.portfolio.taxonomy.TaxonomyStatus;
import com.example.portfolio.taxonomy.Technology;
import com.example.portfolio.taxonomy.TechnologyRepository;
import com.example.portfolio.taxonomy.TechnologyType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ContentServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TechnicalNoteRepository noteRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private AuditService auditService;

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentService(
                projectRepository,
                noteRepository,
                categoryRepository,
                tagRepository,
                technologyRepository,
                auditService,
                CLOCK);
    }

    @Test
    void publishProjectRequiresAtLeastOneActiveTechnology() {
        when(projectRepository.findBySlugAndLanguageAndDeletedAtIsNull("portfolio-api", ContentLanguage.EN))
                .thenReturn(Optional.empty());

        ProjectRequest request = projectRequest(ContentStatus.PUBLISHED, List.of(), "SEO title", "SEO description");

        assertThatThrownBy(() -> contentService.saveProject(null, request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void savePublishedProjectSetsPublishedAt() {
        Technology technology = technology(10L, "Spring Boot", "spring-boot");
        when(projectRepository.findBySlugAndLanguageAndDeletedAtIsNull("portfolio-api", ContentLanguage.EN))
                .thenReturn(Optional.empty());
        when(technologyRepository.findByIdInAndStatusAndDeletedAtIsNull(List.of(10L), TaxonomyStatus.ACTIVE))
                .thenReturn(List.of(technology));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.id = 99L;
            return project;
        });

        var response = contentService.saveProject(
                null,
                projectRequest(ContentStatus.PUBLISHED, List.of(10L), "SEO title", "SEO description"));

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.contentStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(response.publishedAt()).isEqualTo(CLOCK.instant());
        assertThat(response.technologies()).extracting("slug").containsExactly("spring-boot");
    }

    @Test
    void publishNoteRequiresActiveCategoryAndSeo() {
        when(noteRepository.findBySlugAndLanguageAndDeletedAtIsNull("jwt-sessions", ContentLanguage.EN))
                .thenReturn(Optional.empty());

        NoteRequest request = noteRequest(ContentStatus.PUBLISHED, null, "SEO title", "SEO description");

        assertThatThrownBy(() -> contentService.saveNote(null, request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void publicProjectsReturnOnlyPublishedProjectsFromRepository() {
        Project project = new Project();
        project.id = 1L;
        project.title = "Portfolio API";
        project.slug = "portfolio-api";
        project.language = ContentLanguage.EN;
        project.summary = "CMS backend";
        project.role = "Backend";
        project.contentStatus = ContentStatus.PUBLISHED;
        when(projectRepository.findByContentStatusAndDeletedAtIsNullOrderByDisplayOrderAscPublishedAtDescIdDesc(
                ContentStatus.PUBLISHED))
                .thenReturn(List.of(project));

        var response = contentService.listPublicProjects();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().slug()).isEqualTo("portfolio-api");
        assertThat(response.getFirst().contentStatus()).isEqualTo(ContentStatus.PUBLISHED);
    }

    private ProjectRequest projectRequest(
            ContentStatus status,
            List<Long> technologyIds,
            String seoTitle,
            String seoDescription) {
        return new ProjectRequest(
                "Portfolio API",
                "portfolio-api",
                ContentLanguage.EN,
                "CMS backend",
                "Detailed write-up",
                "Backend Engineer",
                ProjectType.BACKEND,
                ProjectLifecycleStatus.COMPLETED,
                status,
                "Problem",
                "Solution",
                "Spring security",
                "React admin",
                "Layered architecture",
                "https://github.com/Nhatdev26/portfolio",
                "https://example.com",
                seoTitle,
                seoDescription,
                1,
                technologyIds,
                List.of(),
                List.of());
    }

    private NoteRequest noteRequest(ContentStatus status, Long categoryId, String seoTitle, String seoDescription) {
        return new NoteRequest(
                "JWT sessions",
                "jwt-sessions",
                ContentLanguage.EN,
                "Token rotation notes",
                "## Token rotation",
                categoryId,
                status,
                seoTitle,
                seoDescription,
                4,
                1,
                List.of(),
                List.of());
    }

    private Technology technology(Long id, String name, String slug) {
        Technology technology = new Technology();
        technology.id = id;
        technology.name = name;
        technology.slug = slug;
        technology.type = TechnologyType.FRAMEWORK;
        technology.status = TaxonomyStatus.ACTIVE;
        return technology;
    }

    @SuppressWarnings("unused")
    private Category category(Long id, String name, String slug) {
        Category category = new Category();
        category.id = id;
        category.name = name;
        category.slug = slug;
        category.status = TaxonomyStatus.ACTIVE;
        return category;
    }
}
