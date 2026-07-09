package io.github.barisaltinel.taskmanagement.service_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.barisaltinel.taskmanagement.cache.TaskManagementCacheCoordinator;
import io.github.barisaltinel.taskmanagement.exception.AccessDeniedException;
import io.github.barisaltinel.taskmanagement.messaging.TaskManagementEventPublisher;
import io.github.barisaltinel.taskmanagement.model.Comment;
import io.github.barisaltinel.taskmanagement.model.Project;
import io.github.barisaltinel.taskmanagement.model.ProjectStatus;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.TaskPriority;
import io.github.barisaltinel.taskmanagement.model.TaskState;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.repository.CommentRepository;
import io.github.barisaltinel.taskmanagement.repository.TaskRepository;
import io.github.barisaltinel.taskmanagement.repository.UserRepository;
import io.github.barisaltinel.taskmanagement.service.impl.CommentServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskManagementEventPublisher eventPublisher;

    @Mock
    private TaskManagementCacheCoordinator cacheCoordinator;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User assignee;
    private Task task;
    private Comment comment;

    @BeforeEach
    void setUp() {
        assignee = new User();
        assignee.setId(10L);
        assignee.setName("Assignee");
        assignee.setEmail("assignee@example.com");
        assignee.setRole("TEAM_MEMBER");
        assignee.setDeleted(false);

        Project project = new Project();
        project.setId(5L);
        project.setTitle("Delivery Project");
        project.setDescription("Project description");
        project.setDepartmentName("Engineering");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDeleted(false);

        task = new Task();
        task.setId(1L);
        task.setTitle("Review API spec");
        task.setDescription("Review the current API spec");
        task.setPriority(TaskPriority.HIGH);
        task.setState(TaskState.IN_PROGRESS);
        task.setProject(project);
        task.setAssignee(assignee);
        task.setDeleted(false);

        comment = new Comment();
        comment.setId(3L);
        comment.setText("Looks ready");
        comment.setAuthor(assignee);
        comment.setTask(task);
        comment.setCreatedAt(LocalDateTime.now());

        commentService.setEventPublisher(eventPublisher);
        commentService.setCacheCoordinator(cacheCoordinator);
        setAuthentication("admin@example.com", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAllCommentsForAdmin() {
        when(commentRepository.findAllByTaskDeletedFalseAndTaskProjectDeletedFalseOrderByIdAsc())
                .thenReturn(List.of(comment));

        List<Comment> comments = commentService.getAllComments();

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getText()).isEqualTo("Looks ready");
    }

    @Test
    void shouldReturnCommentByIdForAdmin() {
        when(commentRepository.findByIdAndTaskDeletedFalseAndTaskProjectDeletedFalse(3L))
                .thenReturn(Optional.of(comment));

        Comment foundComment = commentService.findById(3L);

        assertThat(foundComment).isEqualTo(comment);
    }

    @Test
    void shouldCreateCommentForAssignedUser() {
        setAuthentication("assignee@example.com", "TEAM_MEMBER");
        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("assignee@example.com"))
                .thenReturn(Optional.of(assignee));
        when(taskRepository.findByIdAndDeletedFalseAndProjectDeletedFalse(1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setId(33L);
            return savedComment;
        });

        Comment createdComment = commentService.create("  Looks ready  ", 1L);

        assertThat(createdComment.getId()).isEqualTo(33L);
        assertThat(createdComment.getText()).isEqualTo("Looks ready");
        verify(cacheCoordinator).evictWorkspaceCaches();
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectEmptyCommentText() {
        assertThatThrownBy(() -> commentService.create("   ", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment text cannot be empty");
    }

    @Test
    void shouldRejectCommentWhenTaskIsNotAccessible() {
        User currentUser = new User();
        currentUser.setId(11L);
        currentUser.setEmail("other@example.com");
        currentUser.setRole("TEAM_MEMBER");
        setAuthentication("other@example.com", "TEAM_MEMBER");

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("other@example.com"))
                .thenReturn(Optional.of(currentUser));
        when(taskRepository.findByIdAndDeletedFalseAndProjectDeletedFalse(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> commentService.create("Looks blocked", 1L)).isInstanceOf(AccessDeniedException.class);
    }

    private void setAuthentication(String username, String role) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        username, "N/A", Set.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
