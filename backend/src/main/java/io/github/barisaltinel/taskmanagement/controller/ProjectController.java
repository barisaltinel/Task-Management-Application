package io.github.barisaltinel.taskmanagement.controller;

import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.dto.ApiMapper;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<List<ApiDtos.ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects().stream().map(ApiMapper::toProjectResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.ProjectResponse> createProject(@Valid @RequestBody ApiDtos.ProjectUpsertRequest request) {
        Project createdProject = projectService.create(ApiMapper.toProject(request), request.teamMemberIds());
        return new ResponseEntity<>(ApiMapper.toProjectResponse(createdProject), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ApiDtos.ProjectUpsertRequest request
    ) {
        Project updatedProject = projectService.update(id, ApiMapper.toProject(request), request.teamMemberIds());
        return ResponseEntity.ok(ApiMapper.toProjectResponse(updatedProject));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiDtos.ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiMapper.toProjectResponse(projectService.findById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteProject(@PathVariable Long id) {
        projectService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}


