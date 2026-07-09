package io.github.barisaltinel.taskmanagement.service;

import io.github.barisaltinel.taskmanagement.exception.ProjectNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Project;
import java.util.List;

public interface ProjectService {
    List<Project> getAllProjects();

    Project create(Project project, List<Long> teamMemberIds);

    Project findById(Long id) throws ProjectNotFoundException;

    Project update(Long id, Project projectDetails, List<Long> teamMemberIds);

    void softDelete(Long id);
}
