package org.juniortown.backend.post.repository;



import org.juniortown.backend.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
	Page<Post> findAllByDeletedAtIsNull(Pageable pageable);
}
