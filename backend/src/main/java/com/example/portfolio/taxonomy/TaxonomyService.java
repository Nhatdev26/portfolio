package com.example.portfolio.taxonomy;

import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.taxonomy.dto.CategoryRequest;
import com.example.portfolio.taxonomy.dto.CategoryResponse;
import com.example.portfolio.taxonomy.dto.SkillGroupRequest;
import com.example.portfolio.taxonomy.dto.SkillGroupResponse;
import com.example.portfolio.taxonomy.dto.TagRequest;
import com.example.portfolio.taxonomy.dto.TagResponse;
import com.example.portfolio.taxonomy.dto.TaxonomyAdminResponse;
import com.example.portfolio.taxonomy.dto.TechnologyRequest;
import com.example.portfolio.taxonomy.dto.TechnologyResponse;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxonomyService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TechnologyRepository technologyRepository;
    private final SkillGroupRepository skillGroupRepository;
    private final Clock clock;

    public TaxonomyService(
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            TechnologyRepository technologyRepository,
            SkillGroupRepository skillGroupRepository,
            Clock clock) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.technologyRepository = technologyRepository;
        this.skillGroupRepository = skillGroupRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TaxonomyAdminResponse getAdminTaxonomy() {
        return new TaxonomyAdminResponse(
                categoryRepository.findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc().stream()
                        .map(this::toCategoryResponse)
                        .toList(),
                tagRepository.findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc().stream()
                        .map(this::toTagResponse)
                        .toList(),
                technologyRepository.findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc().stream()
                        .map(this::toTechnologyResponse)
                        .toList(),
                skillGroupRepository.findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc().stream()
                        .map(this::toSkillGroupResponse)
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getPublicCategories() {
        return categoryRepository.findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus.ACTIVE)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getPublicTags() {
        return tagRepository.findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus.ACTIVE)
                .stream()
                .map(this::toTagResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnologyResponse> getPublicTechnologies() {
        return technologyRepository.findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus.ACTIVE)
                .stream()
                .map(this::toTechnologyResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TechnologyResponse getPublicTechnology(String slug) {
        return technologyRepository.findBySlugAndStatusAndDeletedAtIsNull(slug, TaxonomyStatus.ACTIVE)
                .map(this::toTechnologyResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
    }

    @Transactional(readOnly = true)
    public List<SkillGroupResponse> getPublicSkillGroups() {
        return skillGroupRepository.findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus.ACTIVE)
                .stream()
                .map(group -> new SkillGroupResponse(
                        group.id,
                        group.name,
                        group.slug,
                        group.description,
                        group.status,
                        group.displayOrder,
                        group.technologies.stream()
                                .filter(technology -> technology.deletedAt == null && technology.status == TaxonomyStatus.ACTIVE)
                                .map(this::toTechnologyResponse)
                                .toList()))
                .toList();
    }

    @Transactional
    public CategoryResponse saveCategory(CategoryRequest request) {
        Category category = request.id() == null
                ? new Category()
                : categoryRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category was not found."));
        ensureCategorySlugAvailable(request.slug(), category.id);
        category.name = request.name().trim();
        category.slug = normalizeSlug(request.slug());
        category.description = blankToNull(request.description());
        category.status = request.status();
        category.displayOrder = request.displayOrder();
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public TagResponse saveTag(TagRequest request) {
        Tag tag = request.id() == null
                ? new Tag()
                : tagRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag was not found."));
        ensureTagSlugAvailable(request.slug(), tag.id);
        tag.name = request.name().trim();
        tag.slug = normalizeSlug(request.slug());
        tag.status = request.status();
        tag.displayOrder = request.displayOrder();
        return toTagResponse(tagRepository.save(tag));
    }

    @Transactional
    public TechnologyResponse saveTechnology(TechnologyRequest request) {
        Technology technology = request.id() == null
                ? new Technology()
                : technologyRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
        ensureTechnologySlugAvailable(request.slug(), technology.id);
        technology.name = request.name().trim();
        technology.slug = normalizeSlug(request.slug());
        technology.type = request.type();
        technology.status = request.status();
        technology.description = blankToNull(request.description());
        technology.howIUseIt = blankToNull(request.howIUseIt());
        technology.core = request.core();
        technology.displayOrder = request.displayOrder();
        return toTechnologyResponse(technologyRepository.save(technology));
    }

    @Transactional
    public SkillGroupResponse saveSkillGroup(SkillGroupRequest request) {
        SkillGroup group = request.id() == null
                ? new SkillGroup()
                : skillGroupRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill group was not found."));
        ensureSkillGroupSlugAvailable(request.slug(), group.id);
        group.name = request.name().trim();
        group.slug = normalizeSlug(request.slug());
        group.description = blankToNull(request.description());
        group.status = request.status();
        group.displayOrder = request.displayOrder();
        List<Long> ids = request.technologyIds() == null ? List.of() : request.technologyIds();
        group.technologies = new LinkedHashSet<>(
                technologyRepository.findByIdInAndStatusAndDeletedAtIsNull(ids, TaxonomyStatus.ACTIVE));
        if (group.technologies.size() != new LinkedHashSet<>(ids).size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Skill group can attach only active technologies.");
        }
        return toSkillGroupResponse(skillGroupRepository.save(group));
    }

    @Transactional
    public void archiveCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category was not found."));
        category.status = TaxonomyStatus.ARCHIVED;
    }

    @Transactional
    public void archiveTag(Long id) {
        Tag tag = tagRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag was not found."));
        tag.status = TaxonomyStatus.ARCHIVED;
    }

    @Transactional
    public void archiveTechnology(Long id) {
        Technology technology = technologyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
        technology.status = TaxonomyStatus.ARCHIVED;
    }

    @Transactional
    public void archiveSkillGroup(Long id) {
        SkillGroup group = skillGroupRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill group was not found."));
        group.status = TaxonomyStatus.ARCHIVED;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category was not found."));
        category.deletedAt = clock.instant();
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag was not found."));
        tag.deletedAt = clock.instant();
    }

    @Transactional
    public void deleteTechnology(Long id) {
        Technology technology = technologyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
        technology.deletedAt = clock.instant();
    }

    @Transactional
    public void deleteSkillGroup(Long id) {
        SkillGroup group = skillGroupRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill group was not found."));
        group.deletedAt = clock.instant();
    }

    private void ensureCategorySlugAvailable(String slug, Long currentId) {
        categoryRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .filter(existing -> !existing.id.equals(currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Category slug already exists.");
                });
    }

    private void ensureTagSlugAvailable(String slug, Long currentId) {
        tagRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .filter(existing -> !existing.id.equals(currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Tag slug already exists.");
                });
    }

    private void ensureTechnologySlugAvailable(String slug, Long currentId) {
        technologyRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .filter(existing -> !existing.id.equals(currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Technology slug already exists.");
                });
    }

    private void ensureSkillGroupSlugAvailable(String slug, Long currentId) {
        skillGroupRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .filter(existing -> !existing.id.equals(currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Skill group slug already exists.");
                });
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.id,
                category.name,
                category.slug,
                category.description,
                category.status,
                category.displayOrder);
    }

    private TagResponse toTagResponse(Tag tag) {
        return new TagResponse(tag.id, tag.name, tag.slug, tag.status, tag.displayOrder);
    }

    private TechnologyResponse toTechnologyResponse(Technology technology) {
        return new TechnologyResponse(
                technology.id,
                technology.name,
                technology.slug,
                technology.type,
                technology.status,
                technology.description,
                technology.howIUseIt,
                technology.core,
                technology.displayOrder);
    }

    private SkillGroupResponse toSkillGroupResponse(SkillGroup group) {
        return new SkillGroupResponse(
                group.id,
                group.name,
                group.slug,
                group.description,
                group.status,
                group.displayOrder,
                group.technologies.stream().map(this::toTechnologyResponse).toList());
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
