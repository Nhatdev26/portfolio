package com.example.portfolio.cv;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.cv.dto.CvFileResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/cv-files")
public class CvAdminController {

    private final CvFileService cvFileService;

    public CvAdminController(CvFileService cvFileService) {
        this.cvFileService = cvFileService;
    }

    @GetMapping
    public List<CvFileResponse> list() {
        return cvFileService.listAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CvFileResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "EN") ContentLanguage language,
            @RequestParam String targetRole,
            @RequestParam String version) {
        return cvFileService.upload(file, language, targetRole, version);
    }

    @PatchMapping("/{id}/activate")
    public CvFileResponse activate(@PathVariable Long id) {
        return cvFileService.activate(id);
    }

    @PatchMapping("/{id}/archive")
    public CvFileResponse archive(@PathVariable Long id) {
        return cvFileService.archive(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cvFileService.delete(id);
    }
}
