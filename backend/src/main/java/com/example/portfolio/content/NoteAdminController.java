package com.example.portfolio.content;

import com.example.portfolio.content.dto.NoteRequest;
import com.example.portfolio.content.dto.NoteResponse;
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
@RequestMapping("/api/admin/notes")
public class NoteAdminController {

    private final ContentService contentService;

    public NoteAdminController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public List<NoteResponse> list() {
        return contentService.listAdminNotes();
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id) {
        return contentService.getAdminNote(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(@RequestBody NoteRequest request) {
        return contentService.saveNote(null, request);
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @RequestBody NoteRequest request) {
        return contentService.saveNote(id, request);
    }

    @PatchMapping("/{id}/archive")
    public NoteResponse archive(@PathVariable Long id) {
        return contentService.archiveNote(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteNote(id);
    }
}
