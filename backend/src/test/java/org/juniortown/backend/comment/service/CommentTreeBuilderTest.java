package org.juniortown.backend.comment.service;

import static org.juniortown.backend.util.TestDataUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.juniortown.backend.comment.dto.response.CommentsInPost;
import org.juniortown.backend.comment.entity.Comment;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentTreeBuilderTest {
	private User userA;
	private User userB;
	private Post post;
	private Comment parentComment1;
	private Comment parentComment2;
	private Comment childComment1;
	private Comment childComment2;

	@BeforeEach
	void setUp() {
		userA = createUser(1L, "testA", "testA@gmail.com");
		userB = createUser(2L, "testB", "testB@gmail.com");
		post = createPost(1L, "테스트 게시글", "테스트 게시글 내용", userA);
		parentComment1 = createComment(1L, post, userA, "부모 댓글1", null);
		parentComment2 = createComment(2L, post, userB, "부모 댓글2", null);
		childComment1 = createComment(3L, post, userA, "자식 댓글1", parentComment1);
		childComment2 = createComment(4L, post, userB, "자식 댓글2", parentComment2);
	}
	@Test
	@DisplayName("빈 리스트 주입 시 빈 리스트 반환")
	void build_emptyList_returnsEmptyList() {
		assertTrue(CommentTreeBuilder.build(List.of()).isEmpty());
	}

	@Test
	@DisplayName("부모가 없는 댓글들만 주입 시 부모가 없는 댓글들만 반환")
	void build_no_parentComments_returns_only_root_comments() {
		List<CommentsInPost> comments = CommentTreeBuilder.build(List.of(
			parentComment1, parentComment2
		));
		Assertions.assertThat(comments.size()).isEqualTo(2);
		Assertions.assertThat(comments.get(0).getChildren()).isEmpty();
		Assertions.assertThat(comments.get(1).getChildren()).isEmpty();
	}

	@Test
	@DisplayName("부모가 있는 댓글들만 주입 시 반환 안함.")
	void build_only_childComments_returns_nothing() {
		List<CommentsInPost> comments = CommentTreeBuilder.build(List.of(
			childComment1, childComment2
		));
		Assertions.assertThat(comments).isEmpty();
	}

	@Test
	@DisplayName("부모 댓글과 자식 댓글을 주입 시 트리 구조로 반환")
	void build_parentAndChildComments_returns_tree_structure() {
		List<CommentsInPost> comments = CommentTreeBuilder.build(List.of(
			parentComment1, parentComment2, childComment1, childComment2
		));
		Assertions.assertThat(comments.size()).isEqualTo(2);
		Assertions.assertThat(comments.get(0).getChildren().size()).isEqualTo(1);
		Assertions.assertThat(comments.get(1).getChildren().size()).isEqualTo(1);
		Assertions.assertThat(comments.get(0).getChildren().get(0).getContent()).isEqualTo("자식 댓글1");
		Assertions.assertThat(comments.get(1).getChildren().get(0).getContent()).isEqualTo("자식 댓글2");
	}


}