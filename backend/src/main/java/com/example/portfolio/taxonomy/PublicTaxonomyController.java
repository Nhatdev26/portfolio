package com.example.portfolio.taxonomy;

import com.example.portfolio.taxonomy.dto.CategoryResponse;
import com.example.portfolio.taxonomy.dto.SkillGroupResponse;
import com.example.portfolio.taxonomy.dto.TagResponse;
import com.example.portfolio.taxonomy.dto.TechnologyResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicTaxonomyController {

    private final TaxonomyService taxonomyService;

    public PublicTaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return taxonomyService.getPublicCategories();
    }

    @GetMapping("/tags")
    public List<TagResponse> getTags() {
        return taxonomyService.getPublicTags();
    }

    @GetMapping("/technologies")
    public List<TechnologyResponse> getTechnologies() {
        return taxonomyService.getPublicTechnologies();
    }

    @GetMapping("/technologies/{slug}")
    public TechnologyResponse getTechnology(@PathVariable String slug) {
        return taxonomyService.getPublicTechnology(slug);
    }

    @GetMapping("/skill-groups")
    public List<SkillGroupResponse> getSkillGroups() {
        return taxonomyService.getPublicSkillGroups();
    }
}
