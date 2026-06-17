package com.example.portfolio.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.taxonomy.dto.CategoryRequest;
import com.example.portfolio.taxonomy.dto.SkillGroupRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private SkillGroupRepository skillGroupRepository;

    private TaxonomyService taxonomyService;

    @BeforeEach
    void setUp() {
        taxonomyService = new TaxonomyService(
                categoryRepository,
                tagRepository,
                technologyRepository,
                skillGroupRepository,
                CLOCK);
    }

    @Test
    void saveCategoryRejectsDuplicateSlug() {
        Category existing = category(10L, "Backend", "backend", TaxonomyStatus.ACTIVE);
        when(categoryRepository.findBySlugAndDeletedAtIsNull("backend")).thenReturn(Optional.of(existing));

        CategoryRequest request = new CategoryRequest(
                null,
                "Backend",
                "backend",
                "Server side topics",
                TaxonomyStatus.ACTIVE,
                1);

        assertThatThrownBy(() -> taxonomyService.saveCategory(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void publicSkillGroupsExposeOnlyActiveTechnologies() {
        Technology active = technology(20L, "Spring Boot", "spring-boot", TaxonomyStatus.ACTIVE);
        Technology archived = technology(21L, "Legacy", "legacy", TaxonomyStatus.ARCHIVED);
        SkillGroup group = skillGroup(30L, "Backend", "backend", TaxonomyStatus.ACTIVE, active, archived);
        when(skillGroupRepository.findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus.ACTIVE))
                .thenReturn(List.of(group));

        var response = taxonomyService.getPublicSkillGroups();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().technologies()).extracting("slug").containsExactly("spring-boot");
    }

    @Test
    void saveSkillGroupRejectsInactiveTechnologyIds() {
        Technology active = technology(20L, "Spring Boot", "spring-boot", TaxonomyStatus.ACTIVE);
        when(skillGroupRepository.findBySlugAndDeletedAtIsNull("backend")).thenReturn(Optional.empty());
        when(technologyRepository.findByIdInAndStatusAndDeletedAtIsNull(List.of(20L, 21L), TaxonomyStatus.ACTIVE))
                .thenReturn(List.of(active));

        SkillGroupRequest request = new SkillGroupRequest(
                null,
                "Backend",
                "backend",
                "Backend stack",
                TaxonomyStatus.ACTIVE,
                1,
                List.of(20L, 21L));

        assertThatThrownBy(() -> taxonomyService.saveSkillGroup(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void saveCategoryPersistsTrimmedFields() {
        when(categoryRepository.findBySlugAndDeletedAtIsNull("backend")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.id = 10L;
            return category;
        });

        CategoryRequest request = new CategoryRequest(
                null,
                " Backend ",
                "backend",
                " APIs ",
                TaxonomyStatus.ACTIVE,
                1);

        var response = taxonomyService.saveCategory(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Backend");
        assertThat(response.description()).isEqualTo("APIs");
    }

    private Category category(Long id, String name, String slug, TaxonomyStatus status) {
        Category category = new Category();
        category.id = id;
        category.name = name;
        category.slug = slug;
        category.status = status;
        return category;
    }

    private Technology technology(Long id, String name, String slug, TaxonomyStatus status) {
        Technology technology = new Technology();
        technology.id = id;
        technology.name = name;
        technology.slug = slug;
        technology.type = TechnologyType.FRAMEWORK;
        technology.status = status;
        technology.displayOrder = id.intValue();
        return technology;
    }

    private SkillGroup skillGroup(
            Long id,
            String name,
            String slug,
            TaxonomyStatus status,
            Technology... technologies) {
        SkillGroup group = new SkillGroup();
        group.id = id;
        group.name = name;
        group.slug = slug;
        group.status = status;
        group.technologies = new LinkedHashSet<>(List.of(technologies));
        return group;
    }
}
