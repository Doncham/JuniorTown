package org.juniortown.backend.comment.repository;

import java.util.List;

import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.post.dto.response.IdCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

	@Query("SELECT c.post.id AS postId, COUNT(c) AS count "
		+ "FROM Comment c "
		+ "WHERE c.post.id IN :postIds "
		+ "GROUP BY c.post.id")
	List<IdCount> countByPostIds(@Param("postIds") List<Long> postsIds);
}
