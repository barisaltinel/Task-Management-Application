package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.ProjectService;
import io.github.barisaltinel.taskmanagement.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAllByDeletedFalseOrderByIdAsc();
    }

    @Override
    public Project findById(Long id) {
        Long requiredId = requireId(id, "Project id");

        return projectRepository.findByIdAndDeletedFalse(requiredId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public Project create(Project project, List<Long> teamMemberIds) {
        if (project == null) {
            throw new IllegalArgumentException("Project details are required");
        }

        project.setDeleted(false);
        project.setTeamMembers(resolveTeamMembers(teamMemberIds));
        return projectRepository.save(project);
    }

    @Override
    public Project update(Long id, Project projectDetails, List<Long> teamMemberIds) {
        if (projectDetails == null) {
            throw new IllegalArgumentException("Project details are required");
        }

        Project existingProject = findById(id);
        existingProject.setTitle(projectDetails.getTitle());
        existingProject.setDescription(projectDetails.getDescription());
        existingProject.setStatus(projectDetails.getStatus());
        existingProject.setDepartmentName(projectDetails.getDepartmentName());
        existingProject.setTeamMembers(resolveTeamMembers(teamMemberIds));
        return projectRepository.save(existingProject);
    }

    @Override
    public void softDelete(Long id) {
        Project project = findById(id);
        project.setDeleted(true);
        projectRepository.save(project);
    }

    private Long requireId(Long id, String fieldName) {
        return Objects.requireNonNull(id, fieldName + " is required");
    }

    private List<User> resolveTeamMembers(List<Long> teamMemberIds) {
        if (teamMemberIds == null || teamMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userRepository.findAllByIdInAndDeletedFalse(teamMemberIds);
        if (users.size() != teamMemberIds.size()) {
            throw new IllegalArgumentException("One or more team members could not be found");
        }
        return users;
    }
}



