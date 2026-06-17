package com.example.portfolio.content;

import com.example.portfolio.content.dto.NoteResponse;
import com.example.portfolio.content.dto.ProjectResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicContentController {

    private final ContentService contentService;

    public PublicContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/projects")
    public List<ProjectResponse> listProjects() {
        return contentService.listPublicProjects();
    }

    @GetMapping("/projects/{slug}")
    public ProjectResponse getProject(
            @PathVariable String slug,
            @RequestParam(defaultValue = "EN") ContentLanguage language) {
        return contentService.getPublicProject(language, slug);
    }

    @GetMapping("/notes")
    public List<NoteResponse> listNotes() {
        return contentService.listPublicNotes();
    }

    @GetMapping("/notes/{slug}")
    public NoteResponse getNote(
            @PathVariable String slug,
            @RequestParam(defaultValue = "EN") ContentLanguage language) {
        return contentService.getPublicNote(language, slug);
    }
}
