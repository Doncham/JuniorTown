package org.juniortown.backend.util;

import java.time.LocalDateTime;

import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.post.entity.Category;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public class TestBuilders {
	public static class UserBuilder {
		private Long id = 1L;
		private String name = "user";
		private String email = "user@example.com";
		private String password = "password";

		public UserBuilder id(Long id) { this.id = id; return this; }
		public UserBuilder name(String name) { this.name = name; return this; }
		public UserBuilder email(String email) { this.email = email; return this; }


		public User build() {
			User u = User.builder()
				.name(name)
				.email(email)
				.password(password)
				.build();
			ReflectionTestUtils.setField(u, "id", id);
			return u;
		}
	}
	public static UserBuilder user() { return new UserBuilder(); }

	public static class PostBuilder {
		private Long id = 100L;
		private String title = "title";
		private String content = "content";
		private User user = TestBuilders.user().build();
		private Category category = Category.QUESTION;
		private long readCount = 0L;
		private LocalDateTime createdAt = LocalDateTime.now();

		public PostBuilder id(Long id) { this.id = id; return this; }
		public PostBuilder title(String title) { this.title = title; return this; }
		public PostBuilder content(String content) { this.content = content; return this; }
		public PostBuilder user(User user) { this.user = user; return this; }
		public PostBuilder category(Category category) { this.category = category; return this; }
		public PostBuilder readCount(long readCount) { this.readCount = readCount; return this; }
		public PostBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

		public Post build() {
			Post p = Post.builder()
				.title(title)
				.content(content)
				.user(user)
				.category(category)
				.build();
			ReflectionTestUtils.setField(p, "id", id);
			ReflectionTestUtils.setField(p, "readCount", readCount);
			ReflectionTestUtils.setField(p, "createdAt", createdAt);
			return p;
		}
	}
	public static PostBuilder post() { return new PostBuilder(); }


	public static class CommentBuilder {
		private Long id = 1000L;
		private Post post = TestBuilders.post().build();
		private User user = TestBuilders.user().build();
		private String username = "user";
		private String content = "comment";
		private Comment parent = null;
		private LocalDateTime createdAt = LocalDateTime.now();

		public CommentBuilder id(Long id) { this.id = id; return this; }
		public CommentBuilder post(Post post) { this.post = post; return this; }
		public CommentBuilder user(User user) { this.user = user; this.username = user.getName(); return this; }
		public CommentBuilder username(String username) { this.username = username; return this; }
		public CommentBuilder content(String content) { this.content = content; return this; }
		public CommentBuilder parent(Comment parent) { this.parent = parent; return this; }
		public CommentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

		public Comment build() {
			Comment c = Comment.builder()
				.post(post)
				.user(user)
				.username(username)
				.content(content)
				.parent(parent)
				.build();
			ReflectionTestUtils.setField(c, "id", id);
			ReflectionTestUtils.setField(c, "createdAt", createdAt);
			return c;
		}
	}
	public static CommentBuilder comment() { return new CommentBuilder(); }
}
