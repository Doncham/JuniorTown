package org.juniortown.backend.post.repository;

import org.juniortown.backend.post.dto.response.PostWithLikeCountProjection;
import org.juniortown.backend.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
	Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

	@Query(
		"SELECT p.id AS id, p.title AS title, u.name AS username, u.id AS userId, COUNT(l.id) AS likeCount, " +
			"p.createdAt AS createdAt, p.updatedAt AS updatedAt, " +
			"CASE WHEN EXISTS (" +
			"   SELECT 1 FROM Like l2 WHERE l2.post.id = p.id AND l2.user.id = :userId" +
			") THEN true ELSE false END AS isLiked " +
			"FROM Post p " +
			"JOIN p.user u " +
			"LEFT JOIN Like l ON l.post.id = p.id " +
			"WHERE p.deletedAt IS NULL " +
			"GROUP BY p.id, p.title, u.name, u.id, p.createdAt, p.updatedAt, p.deletedAt"
	)
	Page<PostWithLikeCountProjection> findAllWithLikeCount(@Param("userId") Long userId, Pageable pageable);


}
