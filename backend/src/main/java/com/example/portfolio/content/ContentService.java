package com.example.portfolio.content;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.content.dto.NoteRequest;
import com.example.portfolio.content.dto.NoteResponse;
import com.example.portfolio.content.dto.NoteSummaryResponse;
import com.example.portfolio.content.dto.ProjectRequest;
import com.example.portfolio.content.dto.ProjectResponse;
import com.example.portfolio.media.MediaEntityType;
import com.example.portfolio.media.MediaService;
import com.example.portfolio.note.TechnicalNote;
import com.example.portfolio.note.TechnicalNoteRepository;
import com.example.portfolio.project.Project;
import com.example.portfolio.project.ProjectLifecycleStatus;
import com.example.portfolio.project.ProjectRepository;
import com.example.portfolio.project.ProjectType;
import com.example.portfolio.taxonomy.Category;
import com.example.portfolio.taxonomy.CategoryRepository;
import com.example.portfolio.taxonomy.Tag;
import com.example.portfolio.taxonomy.TagRepository;
import com.example.portfolio.taxonomy.TaxonomyStatus;
import com.example.portfolio.taxonomy.Technology;
import com.example.portfolio.taxonomy.TechnologyRepository;
import com.example.portfolio.taxonomy.dto.CategoryResponse;
import com.example.portfolio.taxonomy.dto.TagResponse;
import com.example.portfolio.taxonomy.dto.TechnologyResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentService {

    private final ProjectRepository projectRepository;
    private final TechnicalNoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TechnologyRepository technologyRepository;
    private final MediaService mediaService;
    private final AuditService auditService;
    private final Clock clock;

    public ContentService(
            ProjectRepository projectRepository,
            TechnicalNoteRepository noteRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            TechnologyRepository technologyRepository,
            MediaService mediaService,
            AuditService auditService,
            Clock clock) {
        this.projectRepository = projectRepository;
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.technologyRepository = technologyRepository;
        this.mediaService = mediaService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listAdminProjects() {
        return projectRepository.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc().stream()
                .map(this::toProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getAdminProject(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::toProjectResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project was not found."));
    }

    @Transactional
    public ProjectResponse saveProject(Long id, ProjectRequest request) {
        Project project = id == null
                ? new Project()
                : projectRepository.findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project was not found."));
        ProjectResponse oldValue = project.id == null ? null : toProjectResponse(project);
        ContentStatus oldStatus = project.contentStatus;
        ContentLanguage language = defaultLanguage(request.language());
        ContentStatus status = defaultStatus(request.contentStatus());
        String slug = normalizeSlug(request.slug());
        ensureProjectSlugAvailable(slug, language, id);

        project.title = required(request.title(), "Project title is required.");
        project.slug = slug;
        project.language = language;
        project.summary = required(request.summary(), "Project summary is required.");
        project.description = blankToNull(request.description());
        project.role = required(request.role(), "Project role is required.");
        project.projectType = request.projectType() == null ? ProjectType.FULL_STACK : request.projectType();
        project.projectStatus = request.projectStatus() == null
                ? ProjectLifecycleStatus.COMPLETED
                : request.projectStatus();
        project.contentStatus = status;
        project.problemStatement = blankToNull(request.problemStatement());
        project.solutionOverview = blankToNull(request.solutionOverview());
        project.backendHighlights = blankToNull(request.backendHighlights());
        project.frontendHighlights = blankToNull(request.frontendHighlights());
        project.architectureNotes = blankToNull(request.architectureNotes());
        project.sourceUrl = blankToNull(request.sourceUrl());
        project.demoUrl = blankToNull(request.demoUrl());
        project.seoTitle = blankToNull(request.seoTitle());
        project.seoDescription = blankToNull(request.seoDescription());
        project.displayOrder = request.displayOrder() == null ? 0 : request.displayOrder();
        project.technologies = new LinkedHashSet<>(loadActiveTechnologies(request.technologyIds()));
        project.tags = new LinkedHashSet<>(loadActiveTags(request.tagIds()));
        project.notes = new LinkedHashSet<>(loadNotes(request.noteIds()));

        if (status == ContentStatus.PUBLISHED) {
            validateProjectPublish(project);
            if (project.publishedAt == null) {
                project.publishedAt = Instant.now(clock);
            }
        }
        ProjectResponse response = toProjectResponse(projectRepository.save(project));
        String action = status == ContentStatus.PUBLISHED && oldStatus != ContentStatus.PUBLISHED
                ? "PROJECT_PUBLISH"
                : oldValue == null ? "PROJECT_CREATE" : "PROJECT_UPDATE";
        auditService.success(action, "PROJECT", response.id(), response.title(), oldValue, response);
        return response;
    }

    @Transactional
    public ProjectResponse archiveProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project was not found."));
        ProjectResponse oldValue = toProjectResponse(project);
        project.contentStatus = ContentStatus.ARCHIVED;
        ProjectResponse response = toProjectResponse(projectRepository.save(project));
        auditService.success("PROJECT_ARCHIVE", "PROJECT", response.id(), response.title(), oldValue, response);
        return response;
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project was not found."));
        ProjectResponse oldValue = toProjectResponse(project);
        project.deletedAt = Instant.now(clock);
        projectRepository.save(project);
        auditService.success("PROJECT_DELETE", "PROJECT", project.id, project.title, oldValue, Map.of("deletedAt", project.deletedAt));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listPublicProjects() {
        return projectRepository
                .findByContentStatusAndDeletedAtIsNullOrderByDisplayOrderAscPublishedAtDescIdDesc(
                        ContentStatus.PUBLISHED)
                .stream()
                .map(this::toPublicProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getPublicProject(ContentLanguage language, String slug) {
        return projectRepository
                .findBySlugAndLanguageAndContentStatusAndDeletedAtIsNull(
                        normalizeSlug(slug),
                        defaultLanguage(language),
                        ContentStatus.PUBLISHED)
                .map(this::toPublicProjectResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Published project was not found."));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> listAdminNotes() {
        return noteRepository.findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc().stream()
                .map(this::toNoteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse getAdminNote(Long id) {
        return noteRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::toNoteResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technical note was not found."));
    }

    @Transactional
    public NoteResponse saveNote(Long id, NoteRequest request) {
        TechnicalNote note = id == null
                ? new TechnicalNote()
                : noteRepository.findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technical note was not found."));
        NoteResponse oldValue = note.id == null ? null : toNoteResponse(note);
        ContentStatus oldStatus = note.status;
        ContentLanguage language = defaultLanguage(request.language());
        ContentStatus status = defaultStatus(request.status());
        String slug = normalizeSlug(request.slug());
        ensureNoteSlugAvailable(slug, language, id);

        note.title = required(request.title(), "Technical note title is required.");
        note.slug = slug;
        note.language = language;
        note.excerpt = required(request.excerpt(), "Technical note excerpt is required.");
        note.content = required(request.content(), "Technical note content is required.");
        note.category = loadCategory(request.categoryId());
        note.status = status;
        note.seoTitle = blankToNull(request.seoTitle());
        note.seoDescription = blankToNull(request.seoDescription());
        note.readingMinutes = request.readingMinutes() == null ? 1 : Math.max(1, request.readingMinutes());
        note.displayOrder = request.displayOrder() == null ? 0 : request.displayOrder();
        note.technologies = new LinkedHashSet<>(loadActiveTechnologies(request.technologyIds()));
        note.tags = new LinkedHashSet<>(loadActiveTags(request.tagIds()));

        if (status == ContentStatus.PUBLISHED) {
            validateNotePublish(note);
            if (note.publishedAt == null) {
                note.publishedAt = Instant.now(clock);
            }
        }
        NoteResponse response = toNoteResponse(noteRepository.save(note));
        String action = status == ContentStatus.PUBLISHED && oldStatus != ContentStatus.PUBLISHED
                ? "NOTE_PUBLISH"
                : oldValue == null ? "NOTE_CREATE" : "NOTE_UPDATE";
        auditService.success(action, "TECHNICAL_NOTE", response.id(), response.title(), oldValue, response);
        return response;
    }

    @Transactional
    public NoteResponse archiveNote(Long id) {
        TechnicalNote note = noteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technical note was not found."));
        NoteResponse oldValue = toNoteResponse(note);
        note.status = ContentStatus.ARCHIVED;
        NoteResponse response = toNoteResponse(noteRepository.save(note));
        auditService.success("NOTE_ARCHIVE", "TECHNICAL_NOTE", response.id(), response.title(), oldValue, response);
        return response;
    }

    @Transactional
    public void deleteNote(Long id) {
        TechnicalNote note = noteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technical note was not found."));
        NoteResponse oldValue = toNoteResponse(note);
        note.deletedAt = Instant.now(clock);
        noteRepository.save(note);
        auditService.success("NOTE_DELETE", "TECHNICAL_NOTE", note.id, note.title, oldValue, Map.of("deletedAt", note.deletedAt));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> listPublicNotes() {
        return noteRepository.findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscPublishedAtDescIdDesc(
                        ContentStatus.PUBLISHED)
                .stream()
                .map(this::toPublicNoteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse getPublicNote(ContentLanguage language, String slug) {
        return noteRepository
                .findBySlugAndLanguageAndStatusAndDeletedAtIsNull(
                        normalizeSlug(slug),
                        defaultLanguage(language),
                        ContentStatus.PUBLISHED)
                .map(this::toPublicNoteResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Published note was not found."));
    }

    private void validateProjectPublish(Project project) {
        if (project.technologies.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A project needs at least one active technology to publish.");
        }
        required(project.seoTitle, "SEO title is required before publishing a project.");
        required(project.seoDescription, "SEO description is required before publishing a project.");
    }

    private void validateNotePublish(TechnicalNote note) {
        if (note.category == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A technical note needs an active category to publish.");
        }
        required(note.seoTitle, "SEO title is required before publishing a technical note.");
        required(note.seoDescription, "SEO description is required before publishing a technical note.");
    }

    private List<Technology> loadActiveTechnologies(List<Long> ids) {
        List<Long> normalized = distinctIds(ids);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<Technology> technologies = technologyRepository.findByIdInAndStatusAndDeletedAtIsNull(
                normalized,
                TaxonomyStatus.ACTIVE);
        if (technologies.size() != normalized.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more technologies are inactive or missing.");
        }
        return technologies.stream()
                .sorted(Comparator.comparingInt((Technology technology) -> technology.displayOrder)
                        .thenComparing(technology -> technology.name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<Tag> loadActiveTags(List<Long> ids) {
        List<Long> normalized = distinctIds(ids);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<Tag> tags = tagRepository.findByIdInAndStatusAndDeletedAtIsNull(normalized, TaxonomyStatus.ACTIVE);
        if (tags.size() != normalized.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more tags are inactive or missing.");
        }
        return tags.stream()
                .sorted(Comparator.comparingInt((Tag tag) -> tag.displayOrder)
                        .thenComparing(tag -> tag.name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<TechnicalNote> loadNotes(List<Long> ids) {
        List<Long> normalized = distinctIds(ids);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<TechnicalNote> notes = noteRepository.findByIdInAndDeletedAtIsNull(normalized);
        if (notes.size() != normalized.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more related technical notes are missing.");
        }
        return notes;
    }

    private Category loadCategory(Long id) {
        if (id == null) {
            return null;
        }
        return categoryRepository.findByIdAndStatusAndDeletedAtIsNull(id, TaxonomyStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Category is inactive or missing."));
    }

    private void ensureProjectSlugAvailable(String slug, ContentLanguage language, Long currentId) {
        projectRepository.findBySlugAndLanguageAndDeletedAtIsNull(slug, language)
                .filter(existing -> !Objects.equals(existing.id, currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Project slug already exists for this language.");
                });
    }

    private void ensureNoteSlugAvailable(String slug, ContentLanguage language, Long currentId) {
        noteRepository.findBySlugAndLanguageAndDeletedAtIsNull(slug, language)
                .filter(existing -> !Objects.equals(existing.id, currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Technical note slug already exists for this language.");
                });
    }

    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.id,
                project.title,
                project.slug,
                project.language,
                project.summary,
                project.description,
                project.role,
                project.projectType,
                project.projectStatus,
                project.contentStatus,
                project.problemStatement,
                project.solutionOverview,
                project.backendHighlights,
                project.frontendHighlights,
                project.architectureNotes,
                project.sourceUrl,
                project.demoUrl,
                project.seoTitle,
                project.seoDescription,
                project.publishedAt,
                project.displayOrder,
                project.technologies.stream().map(this::toTechnologyResponse).toList(),
                project.tags.stream().map(this::toTagResponse).toList(),
                project.notes.stream().map(this::toNoteSummaryResponse).toList(),
                mediaService.listEntityMedia(MediaEntityType.PROJECT, project.id, false));
    }

    private ProjectResponse toPublicProjectResponse(Project project) {
        return new ProjectResponse(
                project.id,
                project.title,
                project.slug,
                project.language,
                project.summary,
                project.description,
                project.role,
                project.projectType,
                project.projectStatus,
                project.contentStatus,
                project.problemStatement,
                project.solutionOverview,
                project.backendHighlights,
                project.frontendHighlights,
                project.architectureNotes,
                project.sourceUrl,
                project.demoUrl,
                project.seoTitle,
                project.seoDescription,
                project.publishedAt,
                project.displayOrder,
                project.technologies.stream()
                        .filter(technology -> technology.status == TaxonomyStatus.ACTIVE && technology.deletedAt == null)
                        .map(this::toTechnologyResponse)
                        .toList(),
                project.tags.stream()
                        .filter(tag -> tag.status == TaxonomyStatus.ACTIVE && tag.deletedAt == null)
                        .map(this::toTagResponse)
                        .toList(),
                project.notes.stream()
                        .filter(note -> note.status == ContentStatus.PUBLISHED && note.deletedAt == null)
                        .map(this::toNoteSummaryResponse)
                        .toList(),
                mediaService.listEntityMedia(MediaEntityType.PROJECT, project.id, true));
    }

    private NoteResponse toNoteResponse(TechnicalNote note) {
        return new NoteResponse(
                note.id,
                note.title,
                note.slug,
                note.language,
                note.excerpt,
                note.content,
                note.category == null ? null : toCategoryResponse(note.category),
                note.status,
                note.seoTitle,
                note.seoDescription,
                note.readingMinutes,
                note.publishedAt,
                note.displayOrder,
                note.technologies.stream().map(this::toTechnologyResponse).toList(),
                note.tags.stream().map(this::toTagResponse).toList(),
                mediaService.listEntityMedia(MediaEntityType.TECHNICAL_NOTE, note.id, false));
    }

    private NoteResponse toPublicNoteResponse(TechnicalNote note) {
        return new NoteResponse(
                note.id,
                note.title,
                note.slug,
                note.language,
                note.excerpt,
                note.content,
                note.category == null ? null : toCategoryResponse(note.category),
                note.status,
                note.seoTitle,
                note.seoDescription,
                note.readingMinutes,
                note.publishedAt,
                note.displayOrder,
                note.technologies.stream()
                        .filter(technology -> technology.status == TaxonomyStatus.ACTIVE && technology.deletedAt == null)
                        .map(this::toTechnologyResponse)
                        .toList(),
                note.tags.stream()
                        .filter(tag -> tag.status == TaxonomyStatus.ACTIVE && tag.deletedAt == null)
                        .map(this::toTagResponse)
                        .toList(),
                mediaService.listEntityMedia(MediaEntityType.TECHNICAL_NOTE, note.id, true));
    }

    private NoteSummaryResponse toNoteSummaryResponse(TechnicalNote note) {
        return new NoteSummaryResponse(note.id, note.title, note.slug, note.language, note.excerpt, note.publishedAt);
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

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private ContentLanguage defaultLanguage(ContentLanguage language) {
        return language == null ? ContentLanguage.EN : language;
    }

    private ContentStatus defaultStatus(ContentStatus status) {
        return status == null ? ContentStatus.DRAFT : status;
    }

    private String normalizeSlug(String slug) {
        String value = required(slug, "Slug is required.").toLowerCase(Locale.ROOT);
        if (!value.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Slug must use lowercase letters, numbers, and hyphens.");
        }
        return value;
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
