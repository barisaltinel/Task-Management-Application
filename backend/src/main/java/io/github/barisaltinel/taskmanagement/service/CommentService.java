package io.github.barisaltinel.taskmanagement.service;

import io.github.barisaltinel.taskmanagement.exception.CommentNotFoundException;
import io.github.barisaltinel.taskmanagement.model.Comment;
import java.util.List;

public interface CommentService {
  List<Comment> getAllComments();

  Comment findById(Long id) throws CommentNotFoundException;

  Comment create(String text, Long taskId);
}
