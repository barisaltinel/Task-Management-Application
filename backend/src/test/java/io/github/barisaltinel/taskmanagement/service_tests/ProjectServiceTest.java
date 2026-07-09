package io.github.barisaltinel.taskmanagement.service_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.barisaltinel.taskmanagement.exception.ProjectNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.impl.ProjectServiceImpl;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock private ProjectRepository projectRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private ProjectServiceImpl projectService;

  private Project mockProject;

  @BeforeEach
  void setUp() {
    mockProject = new Project();
    mockProject.setId(1L);
    mockProject.setTitle("Test Project");
    mockProject.setDescription("Project for testing");
    mockProject.setStatus(ProjectStatus.IN_PROGRESS);
    mockProject.setDepartmentName("IT");
    mockProject.setDeleted(false);
  }

  @Test
  void shouldReturnAllProjects() {
    when(projectRepository.findAllByDeletedFalseOrderByIdAsc()).thenReturn(List.of(mockProject));
    List<Project> projects = projectService.getAllProjects();
    assertThat(projects).hasSize(1);
    assertThat(projects.get(0)).isEqualTo(mockProject);
  }

  @Test
  void shouldReturnProjectById() {
    when(projectRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(mockProject));
    Project project = projectService.findById(1L);
    assertThat(project).isEqualTo(mockProject);
  }

  @Test
  void shouldThrowExceptionWhenProjectNotFound() {
    when(projectRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> projectService.findById(99L))
        .isInstanceOf(ProjectNotFoundException.class)
        .hasMessageContaining("Project not found");
  }

  @Test
  void shouldCreateProject() {
    when(projectRepository.save(anyProject()))
        .thenReturn(requireProject(mockProject, "mockProject must not be null"));
    Project project = projectService.create(mockProject, List.of());
    assertThat(project).isEqualTo(mockProject);
  }

  @Test
  void shouldUpdateProject() {
    when(projectRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(mockProject));
    when(projectRepository.save(anyProject()))
        .thenReturn(requireProject(mockProject, "mockProject must not be null"));
    Project updatedProject = projectService.update(1L, mockProject, List.of());
    assertThat(updatedProject).isEqualTo(mockProject);
  }

  @Test
  void shouldSoftDeleteProject() {
    when(projectRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(mockProject));
    projectService.softDelete(1L);
    assertThat(mockProject.isDeleted()).isTrue();
    verify(projectRepository, times(1))
        .save(requireProject(mockProject, "mockProject must not be null"));
  }

  private @NonNull Project requireProject(@Nullable Project project, String message) {
    return Objects.requireNonNull(project, message);
  }

  private @NonNull Project anyProject() {
    return any(Project.class);
  }
}
