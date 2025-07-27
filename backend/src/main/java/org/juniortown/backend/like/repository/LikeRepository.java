package org.juniortown.backend.like.repository;

import java.util.Optional;

import org.juniortown.backend.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
	Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
	Long countByPostId(Long postId);
}
