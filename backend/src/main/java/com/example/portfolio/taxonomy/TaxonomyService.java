package com.example.portfolio.taxonomy;

import com.example.portfolio.audit.AuditService;
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
import java.util.Map;
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
    private final AuditService auditService;
    private final Clock clock;

    public TaxonomyService(
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            TechnologyRepository technologyRepository,
            SkillGroupRepository skillGroupRepository,
            AuditService auditService,
            Clock clock) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.technologyRepository = technologyRepository;
        this.skillGroupRepository = skillGroupRepository;
        this.auditService = auditService;
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
        CategoryResponse oldValue = category.id == null ? null : toCategoryResponse(category);
        ensureCategorySlugAvailable(request.slug(), category.id);
        category.name = request.name().trim();
        category.slug = normalizeSlug(request.slug());
        category.description = blankToNull(request.description());
        category.status = request.status();
        category.displayOrder = request.displayOrder();
        CategoryResponse response = toCategoryResponse(categoryRepository.save(category));
        auditService.success(oldValue == null ? "CATEGORY_CREATE" : "CATEGORY_UPDATE",
                "CATEGORY", response.id(), response.name(), oldValue, response);
        return response;
    }

    @Transactional
    public TagResponse saveTag(TagRequest request) {
        Tag tag = request.id() == null
                ? new Tag()
                : tagRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag was not found."));
        TagResponse oldValue = tag.id == null ? null : toTagResponse(tag);
        ensureTagSlugAvailable(request.slug(), tag.id);
        tag.name = request.name().trim();
        tag.slug = normalizeSlug(request.slug());
        tag.status = request.status();
        tag.displayOrder = request.displayOrder();
        TagResponse response = toTagResponse(tagRepository.save(tag));
        auditService.success(oldValue == null ? "TAG_CREATE" : "TAG_UPDATE",
                "TAG", response.id(), response.name(), oldValue, response);
        return response;
    }

    @Transactional
    public TechnologyResponse saveTechnology(TechnologyRequest request) {
        Technology technology = request.id() == null
                ? new Technology()
                : technologyRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
        TechnologyResponse oldValue = technology.id == null ? null : toTechnologyResponse(technology);
        ensureTechnologySlugAvailable(request.slug(), technology.id);
        technology.name = request.name().trim();
        technology.slug = normalizeSlug(request.slug());
        technology.type = request.type();
        technology.status = request.status();
        technology.description = blankToNull(request.description());
        technology.howIUseIt = blankToNull(request.howIUseIt());
        technology.core = request.core();
        technology.displayOrder = request.displayOrder();
        TechnologyResponse response = toTechnologyResponse(technologyRepository.save(technology));
        auditService.success(oldValue == null ? "TECHNOLOGY_CREATE" : "TECHNOLOGY_UPDATE",
                "TECHNOLOGY", response.id(), response.name(), oldValue, response);
        return response;
    }

    @Transactional
    public SkillGroupResponse saveSkillGroup(SkillGroupRequest request) {
        SkillGroup group = request.id() == null
                ? new SkillGroup()
                : skillGroupRepository.findByIdAndDeletedAtIsNull(request.id())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill group was not found."));
        SkillGroupResponse oldValue = group.id == null ? null : toSkillGroupResponse(group);
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
        SkillGroupResponse response = toSkillGroupResponse(skillGroupRepository.save(group));
        auditService.success(oldValue == null ? "SKILL_GROUP_CREATE" : "SKILL_GROUP_UPDATE",
                "SKILL_GROUP", response.id(), response.name(), oldValue, response);
        return response;
    }

    @Transactional
    public void archiveCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category was not found."));
        CategoryResponse oldValue = toCategoryResponse(category);
        category.status = TaxonomyStatus.ARCHIVED;
        auditService.success("CATEGORY_ARCHIVE", "CATEGORY", category.id, category.name, oldValue, toCategoryResponse(category));
    }

    @Transactional
    public void archiveTag(Long id) {
        Tag tag = tagRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag was not found."));
        TagResponse oldValue = toTagResponse(tag);
        tag.status = TaxonomyStatus.ARCHIVED;
        auditService.success("TAG_ARCHIVE", "TAG", tag.id, tag.name, oldValue, toTagResponse(tag));
    }

    @Transactional
    public void archiveTechnology(Long id) {
        Technology technology = technologyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
        TechnologyResponse oldValue = toTechnologyResponse(technology);
        technology.status = TaxonomyStatus.ARCHIVED;
        auditService.success("TECHNOLOGY_ARCHIVE", "TECHNOLOGY", technology.id, technology.name, oldValue, toTechnologyResponse(technology));
    }

    @Transactional
    public void archiveSkillGroup(Long id) {
        SkillGroup group = skillGroupRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill group was not found."));
        SkillGroupResponse oldValue = toSkillGroupResponse(group);
        group.status = TaxonomyStatus.ARCHIVED;
        auditService.success("SKILL_GROUP_ARCHIVE", "SKILL_GROUP", group.id, group.name, oldValue, toSkillGroupResponse(group));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category was not found."));
        CategoryResponse oldValue = toCategoryResponse(category);
        category.deletedAt = clock.instant();
        auditService.success("CATEGORY_DELETE", "CATEGORY", category.id, category.name, oldValue, Map.of("deletedAt", category.deletedAt));
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag was not found."));
        TagResponse oldValue = toTagResponse(tag);
        tag.deletedAt = clock.instant();
        auditService.success("TAG_DELETE", "TAG", tag.id, tag.name, oldValue, Map.of("deletedAt", tag.deletedAt));
    }

    @Transactional
    public void deleteTechnology(Long id) {
        Technology technology = technologyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology was not found."));
        TechnologyResponse oldValue = toTechnologyResponse(technology);
        technology.deletedAt = clock.instant();
        auditService.success("TECHNOLOGY_DELETE", "TECHNOLOGY", technology.id, technology.name, oldValue, Map.of("deletedAt", technology.deletedAt));
    }

    @Transactional
    public void deleteSkillGroup(Long id) {
        SkillGroup group = skillGroupRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill group was not found."));
        SkillGroupResponse oldValue = toSkillGroupResponse(group);
        group.deletedAt = clock.instant();
        auditService.success("SKILL_GROUP_DELETE", "SKILL_GROUP", group.id, group.name, oldValue, Map.of("deletedAt", group.deletedAt));
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
