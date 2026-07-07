package io.github.barisaltinel.taskmanagement.repository_tests;

import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.ProjectRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        Project project = new Project();
        project.setTitle("Project 1");
        project.setDescription("Description");
        project.setDepartmentName("IT");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project = projectRepository.save(project);

        User user = new User();
        user.setName("Repository User");
        user.setEmail("repo@example.com");
        user.setPassword("encoded-password");
        user.setRole("TEAM_MEMBER");
        user.setDeleted(false);
        user = userRepository.save(user);

        task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Valid description");
        task1.setPriority(TaskPriority.HIGH);
        task1.setState(TaskState.BACKLOG);
        task1.setProject(project);
        task1.setAssignee(user);

        task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Valid description");
        task2.setPriority(TaskPriority.MEDIUM);
        task2.setState(TaskState.CANCELLED);
        task2.setProject(project);
        task2.setAssignee(user);

        taskRepository.save(requireTask(task1, "task1 is required"));
        taskRepository.save(requireTask(task2, "task2 is required"));
    }

    @Test
    void shouldFindAllActiveTasks() {
        List<Task> tasks = taskRepository.findAllByDeletedFalseAndProjectDeletedFalseOrderByIdAsc();

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getTitle).containsExactly("Task 1", "Task 2");
    }

    private @NonNull Task requireTask(@Nullable Task task, String message) {
        return Objects.requireNonNull(task, message);
    }
}


