package com.example.portfolio.content;

import com.example.portfolio.content.dto.ProjectRequest;
import com.example.portfolio.content.dto.ProjectResponse;
import java.util.List;
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
@RequestMapping("/api/admin/projects")
public class ProjectAdminController {

    private final ContentService contentService;

    public ProjectAdminController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return contentService.listAdminProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        return contentService.getAdminProject(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@RequestBody ProjectRequest request) {
        return contentService.saveProject(null, request);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectRequest request) {
        return contentService.saveProject(id, request);
    }

    @PatchMapping("/{id}/archive")
    public ProjectResponse archive(@PathVariable Long id) {
        return contentService.archiveProject(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteProject(id);
    }
}
