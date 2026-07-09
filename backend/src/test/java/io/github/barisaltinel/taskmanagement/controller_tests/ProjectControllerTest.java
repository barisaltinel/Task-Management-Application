package io.github.barisaltinel.taskmanagement.controller_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import io.github.barisaltinel.taskmanagement.controller.ProjectController;
import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.service.ProjectService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setTitle("Test Project");
        mockProject.setDescription("Project for testing");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnAllProjects() {
        when(projectService.getAllProjects()).thenReturn(List.of(mockProject));
        ResponseEntity<List<ApiDtos.ProjectResponse>> response = projectController.getAllProjects();
        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).title()).isEqualTo(mockProject.getTitle());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnProjectById() {
        when(projectService.findById(1L)).thenReturn(mockProject);
        ResponseEntity<ApiDtos.ProjectResponse> response = projectController.getProjectById(1L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo(mockProject.getTitle());
    }

    @Test
    @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
    void shouldCreateProject() {
        ApiDtos.ProjectUpsertRequest request = new ApiDtos.ProjectUpsertRequest(
                "Test Project", "Project for testing", "IT", ProjectStatus.IN_PROGRESS, List.of());
        when(projectService.create(any(Project.class), anyList())).thenReturn(mockProject);
        ResponseEntity<ApiDtos.ProjectResponse> response = projectController.createProject(request);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo(mockProject.getTitle());
    }

    @Test
    @WithMockUser(username = "project_manager", roles = "PROJECT_MANAGER")
    void shouldUpdateProject() {
        ApiDtos.ProjectUpsertRequest request = new ApiDtos.ProjectUpsertRequest(
                "Test Project", "Project for testing", "IT", ProjectStatus.IN_PROGRESS, List.of());
        when(projectService.update(anyLong(), any(Project.class), anyList())).thenReturn(mockProject);
        ResponseEntity<ApiDtos.ProjectResponse> response = projectController.updateProject(1L, request);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo(mockProject.getTitle());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldSoftDeleteProject() {
        doNothing().when(projectService).softDelete(1L);
        ResponseEntity<Void> response = projectController.softDeleteProject(1L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
}
