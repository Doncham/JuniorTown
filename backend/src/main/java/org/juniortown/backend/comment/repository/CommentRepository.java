package org.juniortown.backend.comment.repository;

import java.util.List;

import org.juniortown.backend.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);
}
