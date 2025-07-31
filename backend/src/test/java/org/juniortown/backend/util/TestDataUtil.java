package org.juniortown.backend.util;

import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public class TestDataUtil {
	public static Comment commentCreate(Long id, Post post, User user, String content, Comment parentComment) {
		Comment comment = Comment.builder()
			.post(post)
			.user(user)
			.username(user.getName())
			.content(content)
			.parent(parentComment)
			.build();
		ReflectionTestUtils.setField(comment, "id", id);
		return comment;
	}
	public static Post createPost(Long id,String title,String content, User user) {
		Post post = Post.builder()
			.title(title)
			.content(content)
			.user(user)
			.build();
		ReflectionTestUtils.setField(post, "id", id);
		return post;
	}
	public static User createUser(Long id, String name,String email) {
		User user = User.builder()
			.name(name)
			.password("password")
			.email(email)
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}
}
