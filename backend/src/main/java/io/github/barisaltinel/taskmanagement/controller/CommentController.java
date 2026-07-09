package io.github.barisaltinel.taskmanagement.controller;

import io.github.barisaltinel.taskmanagement.dto.ApiDtos;
import io.github.barisaltinel.taskmanagement.dto.ApiMapper;
import io.github.barisaltinel.taskmanagement.exception.CommentNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Comment;
import io.github.barisaltinel.taskmanagement.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
  public ResponseEntity<List<ApiDtos.CommentResponse>> getAllComments() {
    return ResponseEntity.ok(
        commentService.getAllComments().stream().map(ApiMapper::toCommentResponse).toList());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
  public ResponseEntity<ApiDtos.CommentResponse> getCommentById(@PathVariable Long id)
      throws CommentNotFoundException {
    return ResponseEntity.ok(ApiMapper.toCommentResponse(commentService.findById(id)));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER', 'PROJECT_MANAGER', 'ADMIN')")
  public ResponseEntity<ApiDtos.CommentResponse> createComment(
      @Valid @RequestBody ApiDtos.CommentCreateRequest request) {
    Comment comment = commentService.create(request.text(), request.taskId());
    return new ResponseEntity<>(ApiMapper.toCommentResponse(comment), HttpStatus.CREATED);
  }
}
