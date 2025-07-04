package org.juniortown.backend.like.service;

import java.util.Optional;

import org.juniortown.backend.like.dto.response.LikeResponse;
import org.juniortown.backend.like.entity.Like;
import org.juniortown.backend.like.exception.LikeFailureException;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	@Transactional
	public LikeResponse likePost(Long userId, Long postId) {
		User user = userRepository.getReferenceById(userId);
		Post post = postRepository.getReferenceById(postId);
		Optional<Like> like = likeRepository.findByUserIdAndPostId(userId, postId);

		if(like.isEmpty()){
			try{
				likeRepository.save(Like.builder()
					.user(user)
					// 저장하는 시점에 post가 삭제될 수 있으니까 try-catch로 잡음.
					.post(post)
					.build()
				);
				return LikeResponse.builder()
					.userId(userId)
					.postId(postId)
					.isLiked(true)
					.build();
			}catch (Exception e) {
				throw new LikeFailureException(e);
			}
		}
		else{
			likeRepository.deleteById(like.get().getId());

			return LikeResponse.builder()
				.userId(userId)
				.postId(postId)
				.isLiked(false)
				.build();
		}


	}

}
