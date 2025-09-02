package org.juniortown.backend.post.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.juniortown.backend.comment.repository.CommentRepository;
import org.juniortown.backend.like.repository.LikeRepository;
import org.juniortown.backend.post.dto.response.HomePageResponse;
import org.juniortown.backend.post.dto.response.HomePageSection;
import org.juniortown.backend.post.dto.response.IdCount;
import org.juniortown.backend.post.dto.response.PostForHomePage;
import org.juniortown.backend.post.entity.Category;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;

import org.juniortown.backend.util.TestBuilders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

// {
// 	"sections": [
// 	{
// 	"category": "question",
// 	"items": [
// 	{
// 	"postId": 21,
// 	"title": "title20",
// 	"userId": 1,
// 	"nickname": "testUser",
// 	"likeCount": 0,
// 	"commentCount": 0,
// 	"readCount": 0,
// 	"createdAt": "2025-08-31T07:11:39.535671"
// 	},
//
// 	]
// 	},
// 	{
// 	"category": "study",
// 	"items": [
// 	{
// 	"postId": 22,
// 	"title": "title21",
// 	"userId": 1,
// 	"nickname": "testUser",
// 	"likeCount": 0,
// 	"commentCount": 0,
// 	"readCount": 0,
// 	"createdAt": "2025-08-31T07:11:39.539753"
// 	}
// 	]
// 	},
// 	{
// 	"category": "notice",
// 	"items": [
// 	{
// 	"postId": 23,
// 	"title": "title22",
// 	"userId": 1,
// 	"nickname": "testUser",
// 	"likeCount": 0,
// 	"commentCount": 0,
// 	"readCount": 0,
// 	"createdAt": "2025-08-31T07:11:39.544494"
// 	}
// 	"createdAt": "2025-08-31T16:11:54.634256"
// 	}
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class MainPageServiceTest {
	@InjectMocks
	private PostService postService;
	@Mock
	private PostRepository postRepository;
	@Mock
	private LikeRepository likeRepository;
	@Mock
	private CommentRepository commentRepository;


	private IdCount idCount(long postId, long cnt) {
		return new IdCount() {
			@Override
			public Long getPostId() { return postId; }
			@Override
			public Long getCnt() { return cnt; }
		};
	}

	private Post post(long id, long userId, String username, String title,
		Category category, long readCount, LocalDateTime createdAt) {
		User user = User.builder()
			.name(username)
			.id(userId)
			.build();

		Post post = Post.builder()
			.title(title)
			.user(user)
			.category(category)
			.build();
		ReflectionTestUtils.setField(post, "id", id);
		ReflectionTestUtils.setField(post, "readCount", readCount);
		ReflectionTestUtils.setField(post, "createdAt", createdAt);
		return post;
	}

	@Test
	@DisplayName("메인 페이지 조회 성공")
	void get_main_page_success() {
		int perCategory = 2;
		long userId = 1L;
		LocalDateTime now = LocalDateTime.now();

		Category[] cats = Category.values();
		// Enum 순서에 의존한 부분 리팩토링 필요
		Category C1 = cats[0];
		Category C2 = cats[1];

		List<Post> c1Posts = List.of(
			TestBuilders.post().id(101L).user(TestBuilders.user().id(1L).name("user1").build())
				.title("Q1").category(C1).readCount(5).createdAt(now.minusMinutes(1)).build(),
			TestBuilders.post().id(102L).user(TestBuilders.user().id(2L).name("user2").build())
				.title("Q2").category(C1).readCount(5).createdAt(now.minusMinutes(2)).build()
		);
		List<Post> c2Posts = List.of(
			TestBuilders.post().id(201L).user(TestBuilders.user().id(3L).name("user3").build())
				.title("S1").category(C2).readCount(10).createdAt(now.minusMinutes(3)).build(),
			TestBuilders.post().id(202L).user(TestBuilders.user().id(4L).name("user4").build())
				.title("S2").category(C2).readCount(11).createdAt(now.minusMinutes(4)).build()
		);
		// 1) 기본 스텁: 모든 카테고리는 빈 페이지 반환
		when(postRepository.findByCategoryAndDeletedAtIsNull(any(Category.class), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of()));

		// Page 목킹 (count 쿼리까지는 신경 안 써도 됨), 기본 스텁을 덮어씌움.
		when(postRepository.findByCategoryAndDeletedAtIsNull(eq(C1), any(Pageable.class)))
			.thenReturn(new PageImpl<>(c1Posts));
		when(postRepository.findByCategoryAndDeletedAtIsNull(eq(C2), any(Pageable.class)))
			.thenReturn(new PageImpl<>(c2Posts));

		List<Long> allIds = List.of(101L, 102L, 201L, 202L);
		when(commentRepository.countByPostIds(allIds))
			.thenReturn(List.of(idCount(101, 7), idCount(201, 3)));
		when(likeRepository.countByPostIds(allIds))
			.thenReturn(List.of(idCount(101, 2), idCount(102, 4), idCount(201, 1), idCount(202, 0)));

		// 실행
		HomePageResponse resp = postService.homePagePosts(perCategory);

		// 검증
		assertNotNull(resp);
		assertNotNull(resp.getSections());
		assertTrue(resp.getSections().size() >= 2);

		// 왜 cat[0]에 있는 값이랑 반환값의 첫번째 첫번째 category가 같은건지 이해하기
		HomePageSection s0 = resp.getSections().get(0);
		assertEquals(cats[0].name().toLowerCase(), s0.getCategory());
		assertEquals(2, s0.getItems().size());
		PostForHomePage p0 = s0.getItems().get(0);
		assertEquals(101L, p0.getPostId());
		assertEquals(2L, p0.getLikeCount());
		assertEquals(7L, p0.getCommentCount());   // hit
		assertEquals(5L, p0.getReadCount());

		PostForHomePage p1 = s0.getItems().get(1);
		assertEquals(102L, p1.getPostId());
		assertEquals(4L, p1.getLikeCount());
		assertEquals(0L, p1.getCommentCount());   // 누락 → 0 폴백

		// Pageable 전달 검증
		ArgumentCaptor<PageRequest> pr = ArgumentCaptor.forClass(PageRequest.class);
		verify(postRepository).findByCategoryAndDeletedAtIsNull(eq(C1), pr.capture());
		PageRequest used = pr.getValue();
		assertEquals(perCategory, used.getPageSize());
		List<Sort.Order> orderList = used.getSort().toList();
		assertEquals("createdAt", orderList.get(0).getProperty());
		assertTrue(orderList.get(0).isDescending());
		assertEquals("id", orderList.get(1).getProperty());
		assertTrue(orderList.get(1).isDescending());
	}

	@Test
	void when_empty_category_returns_empty_array_and_no_repository_call() {
		// 모든 카테고리에서 빈 Page 반환
		when(postRepository.findByCategoryAndDeletedAtIsNull(any(), any(PageRequest.class)))
			.thenReturn(new PageImpl<Post>(List.of()));

		HomePageResponse resp = postService.homePagePosts(5);

		assertNotNull(resp);
		assertEquals(0, resp.getSections().size());
		verify(commentRepository, never()).countByPostIds(anyList());
		verify(likeRepository, never()).countByPostIds(anyList());
	}
}
