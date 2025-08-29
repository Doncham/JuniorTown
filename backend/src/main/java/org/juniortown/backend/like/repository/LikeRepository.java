package org.juniortown.backend.like.repository;

import java.util.List;
import java.util.Optional;

import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.post.dto.response.IdCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
	Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
	Long countByPostId(Long postId);

	@Query("SELECT l.post.id AS postId, COUNT(l) AS cnt "
		+ "FROM Like l "
		+ "WHERE l.post.id IN :postIds "
		+ "GROUP BY l.post.id")
	List<IdCount> countByPostIds(@Param("postIds")List<Long>postIds);
}
