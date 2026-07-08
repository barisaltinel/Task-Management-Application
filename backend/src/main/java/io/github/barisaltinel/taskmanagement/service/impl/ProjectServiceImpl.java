package io.github.barisaltinel.taskmanagement.service.impl;

import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheCoordinator;
import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheNames;
import io.github.barisaltinel.taskmanagement.exception.ProjectNotFoundException;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEntityType;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventAction;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventPublisher;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEvents;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private TaskManagementEventPublisher eventPublisher = TaskManagementEventPublisher.noOp();
    private TaskManagementCacheCoordinator cacheCoordinator = TaskManagementCacheCoordinator.noOp();

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable(
            cacheNames = TaskManagementCacheNames.PROJECT_LIST,
            key = "T(io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheKeys).currentAccessScope()"
    )
    public List<Project> getAllProjects() {
        return projectRepository.findAllByDeletedFalseOrderByIdAsc();
    }

    @Override
    @Cacheable(
            cacheNames = TaskManagementCacheNames.PROJECT_DETAILS,
            key = "T(io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheKeys).scopedId(#id)"
    )
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
        Project savedProject = projectRepository.save(project);
        Project persistedProject = savedProject != null ? savedProject : project;
        cacheCoordinator.evictWorkspaceCaches();
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.PROJECT,
                persistedProject.getId(),
                TaskManagementEventAction.CREATED,
                "Created project " + persistedProject.getTitle()
        ));
        return persistedProject;
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
        Project savedProject = projectRepository.save(existingProject);
        Project persistedProject = savedProject != null ? savedProject : existingProject;
        cacheCoordinator.evictWorkspaceCaches();
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.PROJECT,
                persistedProject.getId(),
                TaskManagementEventAction.UPDATED,
                "Updated project " + persistedProject.getTitle()
        ));
        return persistedProject;
    }

    @Override
    public void softDelete(Long id) {
        Project project = findById(id);
        project.setDeleted(true);
        Project savedProject = projectRepository.save(project);
        Project persistedProject = savedProject != null ? savedProject : project;
        cacheCoordinator.evictWorkspaceCaches();
        eventPublisher.publish(TaskManagementEvents.create(
                TaskManagementEntityType.PROJECT,
                persistedProject.getId(),
                TaskManagementEventAction.DELETED,
                "Archived project " + persistedProject.getTitle()
        ));
    }

    @Autowired(required = false)
    public void setEventPublisher(TaskManagementEventPublisher eventPublisher) {
        if (eventPublisher != null) {
            this.eventPublisher = eventPublisher;
        }
    }

    @Autowired
    public void setCacheCoordinator(TaskManagementCacheCoordinator cacheCoordinator) {
        if (cacheCoordinator != null) {
            this.cacheCoordinator = cacheCoordinator;
        }
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



