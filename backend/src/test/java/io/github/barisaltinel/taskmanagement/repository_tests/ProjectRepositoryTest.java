package io.github.barisaltinel.taskmanagement.repository_tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setTitle("Test Project");
        testProject.setDescription("Repository Test");
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setDepartmentName("IT");
        projectRepository.save(testProject);
    }

    @Test
    void shouldReturnAllProjects() {
        List<Project> projects = projectRepository.findAll();
        assertThat(projects).isNotEmpty();
    }

    @Test
    void shouldUpdateProjectSuccessfully() {
        testProject.setTitle("Updated Title");
        projectRepository.save(testProject);

        Optional<Project> updatedProject =
                projectRepository.findById(requireId(testProject.getId(), "Project id is required"));
        assertThat(updatedProject).isPresent();
        assertThat(updatedProject.get().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void shouldSoftDeleteProject() {
        testProject.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(testProject);

        Optional<Project> deletedProject =
                projectRepository.findById(requireId(testProject.getId(), "Project id is required"));
        assertThat(deletedProject).isPresent();
        assertThat(deletedProject.get().getStatus()).isEqualTo(ProjectStatus.CANCELLED);
    }

    private @NonNull Long requireId(@Nullable Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}
