package io.github.barisaltinel.taskmanagement.controller_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.barisaltinel.taskmanagement.controller.CommentController;
import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.exception.CommentNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Comment;
import io.github.barisaltinel.taskmanagement.model.Task;
import io.github.barisaltinel.taskmanagement.model.User;
import io.github.barisaltinel.taskmanagement.service.CommentService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private Comment mockComment;

    @BeforeEach
    void setUp() {
        User author = new User();
        author.setId(3L);
        author.setName("Team Member");
        author.setEmail("member@example.com");
        author.setRole("TEAM_MEMBER");
        author.setCreatedAt(LocalDateTime.now());

        Task task = new Task();
        task.setId(7L);
        task.setTitle("Document API flow");

        mockComment = new Comment();
        mockComment.setId(1L);
        mockComment.setText("Looks good to me");
        mockComment.setAuthor(author);
        mockComment.setTask(task);
        mockComment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void shouldReturnAllComments() {
        when(commentService.getAllComments()).thenReturn(List.of(mockComment));

        ResponseEntity<List<ApiDtos.CommentResponse>> response = commentController.getAllComments();

        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).text()).isEqualTo("Looks good to me");
    }

    @Test
    @WithMockUser
    void shouldReturnCommentById() {
        when(commentService.findById(1L)).thenReturn(mockComment);

        ResponseEntity<ApiDtos.CommentResponse> response = commentController.getCommentById(1L);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().taskId()).isEqualTo(7L);
    }

    @Test
    @WithMockUser
    void shouldThrowWhenCommentNotFound() {
        when(commentService.findById(99L)).thenThrow(new CommentNotFoundException());

        assertThatThrownBy(() -> commentController.getCommentById(99L))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    @WithMockUser
    void shouldCreateComment() {
        ApiDtos.CommentCreateRequest request = new ApiDtos.CommentCreateRequest(7L, "Looks good to me");
        when(commentService.create("Looks good to me", 7L)).thenReturn(mockComment);

        ResponseEntity<ApiDtos.CommentResponse> response = commentController.createComment(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().author().email()).isEqualTo("member@example.com");
    }
}
