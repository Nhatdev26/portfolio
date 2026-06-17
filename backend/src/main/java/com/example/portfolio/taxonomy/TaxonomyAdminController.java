package com.example.portfolio.taxonomy;

import com.example.portfolio.taxonomy.dto.CategoryRequest;
import com.example.portfolio.taxonomy.dto.CategoryResponse;
import com.example.portfolio.taxonomy.dto.SkillGroupRequest;
import com.example.portfolio.taxonomy.dto.SkillGroupResponse;
import com.example.portfolio.taxonomy.dto.TagRequest;
import com.example.portfolio.taxonomy.dto.TagResponse;
import com.example.portfolio.taxonomy.dto.TaxonomyAdminResponse;
import com.example.portfolio.taxonomy.dto.TechnologyRequest;
import com.example.portfolio.taxonomy.dto.TechnologyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class TaxonomyAdminController {

    private final TaxonomyService taxonomyService;

    public TaxonomyAdminController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    @GetMapping("/taxonomy")
    public TaxonomyAdminResponse getTaxonomy() {
        return taxonomyService.getAdminTaxonomy();
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return taxonomyService.saveCategory(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return taxonomyService.saveCategory(new CategoryRequest(
                id,
                request.name(),
                request.slug(),
                request.description(),
                request.status(),
                request.displayOrder()));
    }

    @PatchMapping("/categories/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveCategory(@PathVariable Long id) {
        taxonomyService.archiveCategory(id);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        taxonomyService.deleteCategory(id);
    }

    @PostMapping("/tags")
    public TagResponse createTag(@Valid @RequestBody TagRequest request) {
        return taxonomyService.saveTag(request);
    }

    @PutMapping("/tags/{id}")
    public TagResponse updateTag(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return taxonomyService.saveTag(new TagRequest(
                id,
                request.name(),
                request.slug(),
                request.status(),
                request.displayOrder()));
    }

    @PatchMapping("/tags/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveTag(@PathVariable Long id) {
        taxonomyService.archiveTag(id);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        taxonomyService.deleteTag(id);
    }

    @PostMapping("/technologies")
    public TechnologyResponse createTechnology(@Valid @RequestBody TechnologyRequest request) {
        return taxonomyService.saveTechnology(request);
    }

    @PutMapping("/technologies/{id}")
    public TechnologyResponse updateTechnology(
            @PathVariable Long id,
            @Valid @RequestBody TechnologyRequest request) {
        return taxonomyService.saveTechnology(new TechnologyRequest(
                id,
                request.name(),
                request.slug(),
                request.type(),
                request.status(),
                request.description(),
                request.howIUseIt(),
                request.core(),
                request.displayOrder()));
    }

    @PatchMapping("/technologies/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveTechnology(@PathVariable Long id) {
        taxonomyService.archiveTechnology(id);
    }

    @DeleteMapping("/technologies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTechnology(@PathVariable Long id) {
        taxonomyService.deleteTechnology(id);
    }

    @PostMapping("/skill-groups")
    public SkillGroupResponse createSkillGroup(@Valid @RequestBody SkillGroupRequest request) {
        return taxonomyService.saveSkillGroup(request);
    }

    @PutMapping("/skill-groups/{id}")
    public SkillGroupResponse updateSkillGroup(
            @PathVariable Long id,
            @Valid @RequestBody SkillGroupRequest request) {
        return taxonomyService.saveSkillGroup(new SkillGroupRequest(
                id,
                request.name(),
                request.slug(),
                request.description(),
                request.status(),
                request.displayOrder(),
                request.technologyIds()));
    }

    @PatchMapping("/skill-groups/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveSkillGroup(@PathVariable Long id) {
        taxonomyService.archiveSkillGroup(id);
    }

    @DeleteMapping("/skill-groups/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkillGroup(@PathVariable Long id) {
        taxonomyService.deleteSkillGroup(id);
    }
}
