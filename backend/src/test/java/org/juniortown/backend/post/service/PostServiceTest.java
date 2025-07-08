package org.juniortown.backend.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.juniortown.backend.post.dto.request.PostCreateRequest;
import org.juniortown.backend.post.dto.response.PostResponse;
import org.juniortown.backend.post.dto.response.PostWithLikeCountProjection;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.exception.PostNotFoundException;
import org.juniortown.backend.post.exception.PostUpdatePermissionDeniedException;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.exception.UserNotFoundException;
import org.juniortown.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PostServiceTest {
	private static final int PAGE_SIZE = 10;
	@InjectMocks
	private PostService postService;
	@Mock
	private PostRepository postRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private Clock clock;

	@Mock
	private Post post;
	@Mock
	private User user;

	@BeforeEach
	void clear() {
		postRepository.deleteAll();
	}

	@Test
	@DisplayName("게시글 생성 성공")
	void createPost_success() {
		// given
		PostCreateRequest postCreateRequest = PostCreateRequest.builder()
			.title("테스트 게시물")
			.content("테스트 내용입니다.")
			.build();
		long userId = 1L; // 예시로 사용될 userId

		// when
		when(postRepository.save(any(Post.class))).thenReturn(
			Post.builder()
				.title(postCreateRequest.getTitle())
				.content(postCreateRequest.getContent())
				.user(User.builder().id(userId).build())
				.build()
		);

		when(userRepository.findById(userId)).thenReturn(
			Optional.of(User.builder().id(userId).build())
		);
		postService.create(userId, postCreateRequest);

		// then
		ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
		verify(postRepository).save(captor.capture());
		Post saved = captor.getValue();
		assertThat(saved.getTitle()).isEqualTo("테스트 게시물");
		assertThat(saved.getContent()).isEqualTo("테스트 내용입니다.");
		assertThat(saved.getUser().getId()).isEqualTo(userId);
	}

	@Test
	@DisplayName("게시글 생성 실패 - 사용자 존재하지 않음")
	void createPost_userNotFound_throws() {
		// given
		PostCreateRequest postCreateRequest = PostCreateRequest.builder()
			.title("테스트 게시물")
			.content("테스트 내용입니다.")
			.build();

		// when
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		// then
		assertThatThrownBy(
				() -> postService.create(1L, postCreateRequest))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("해당 사용자를 찾을 수 없습니다.");

	}

	@Test
	@DisplayName("게시글 삭제 성공")
	void deletePost_success() {
		// given
		Long userId = 1L;
		Long postId = 1L;

		when(user.getId()).thenReturn(userId);
		when(post.getUser()).thenReturn(user);

		when(postRepository.findById(postId)).thenReturn(Optional.of(post));

		// when
		postService.delete(postId,userId);

		// then
		verify(post).softDelete(clock);
	}

	@Test
	@DisplayName("게시글 수정 성공")
	void update_post_success() {
		// given
		Long postId = 1L;
		Long userId = 1L;

		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		// when
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(post.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId);
		postService.update(postId, userId, editRequest);

		// then
		verify(post).update(editRequest, clock);
	}

	@Test
	@DisplayName("게시글 수정 실패 - 게시글이 존재하지 않음")
	void update_post_not_found_failure() {
		// given
		Long postId = 1L;
		Long userId = 1L;

		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.update(postId, userId, editRequest))
			.isInstanceOf(PostNotFoundException.class)
			.hasMessage("해당 게시글을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("게시글 수정 실패 - 권한 없음")
	void update_post_no_permission_failure() {
		// given
		Long postId = 1L;
		Long userId = 1L;

		PostCreateRequest editRequest = PostCreateRequest.builder()
			.title("Changed Title")
			.content("Changed Content")
			.build();

		// when
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(post.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(userId + 1);

		// then
		assertThatThrownBy(() -> postService.update(postId, userId, editRequest))
			.isInstanceOf(PostUpdatePermissionDeniedException.class)
			.hasMessage("해당 게시글을 수정할 권한이 없습니다.");
	}

	@Test
	@DisplayName("getPosts: 페이지 조회 시, Repository 호출 -> PostResponse로 매핑된 Page 반환")
	void getPosts_returnsMappedPage() {
		// given
		int page = 0;
		Sort sort = Sort.by("createdAt").descending();
		PageRequest expectedPageable = PageRequest.of(page, PAGE_SIZE, sort);

		PostWithLikeCountProjection post1 = mock(PostWithLikeCountProjection.class);
		when(post1.getTitle()).thenReturn("TA");
		PostWithLikeCountProjection post2 = mock(PostWithLikeCountProjection.class);
		when(post2.getTitle()).thenReturn("TB");

		int totalElements = 2;

		List<PostWithLikeCountProjection> projections = List.of(post1, post2);
		PageImpl<PostWithLikeCountProjection> mockPage = new PageImpl<>(projections, expectedPageable, totalElements);
		when(user.getId()).thenReturn(1L);
		when(postRepository.findAllWithLikeCount(user.getId(), expectedPageable)).thenReturn(mockPage);
		// when
		Page<PostWithLikeCountProjection> result = postService.getPosts(user.getId(), page);

		// then
		verify(postRepository).findAllWithLikeCount(user.getId(), expectedPageable);

		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getSize()).isEqualTo(PAGE_SIZE);
		assertThat(result.getTotalPages()).isEqualTo(1);
		// 현재 페이지 번호
		assertThat(result.getNumber()).isEqualTo(0);
		// 현재 페이지에 들어있는 요소의 개수
		assertThat(result.getNumberOfElements()).isEqualTo(2);

		List<PostWithLikeCountProjection> content = result.getContent();
		assertThat(content).hasSize(2);
		assertThat(content.get(0).getTitle()).isEqualTo("TA");
		assertThat(content.get(1).getTitle()).isEqualTo("TB");
	}

	@Test
	@DisplayName("getPosts: 페이지 조회 시, 빈 페이지 반환")
	void getPosts_emptyPage() {
		//given
		int page = 0;
		PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
		Page<PostWithLikeCountProjection> emptyPage = Page.empty(pageable);
		when(user.getId()).thenReturn(1L);
		when(postRepository.findAllWithLikeCount(user.getId(),pageable)).thenReturn(emptyPage);

		// when
		Page<PostWithLikeCountProjection> result = postService.getPosts(user.getId(), page);

		// then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isEqualTo(0);
		assertThat(result.getSize()).isEqualTo(PAGE_SIZE);
		assertThat(result.getTotalPages()).isEqualTo(0);
		assertThat(result.getNumber()).isEqualTo(0);
		assertThat(result.getNumberOfElements()).isEqualTo(0);
	}

	@Test
	@DisplayName("getPost: 페이지 상세 조회 성공")
	void getPost_return_pageDetail() {
		// given
		long postId = 1L;
		long userId = 2L;
		String title = "testTitle";
		String content = "testContent";
		String name = "testName";
		Post post = Post.builder()
			.user(user)
			.content(content)
			.title(title)
			.build();

		// when
		//when(user.getId()).thenReturn(userId);
		when(user.getName()).thenReturn(name);
		when(postRepository.findById(postId)).thenReturn(Optional.ofNullable(post));

		PostResponse result = postService.getPost(postId);

		// then
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getTitle()).isEqualTo(title);
		assertThat(result.getUserName()).isEqualTo(name);
		verify(postRepository).findById(postId);
	}

	@Test
	@DisplayName("getPost: 삭제된 페이지 조회 실패")
	void getPost_not_return_deleted_page() {
		// given
		Long postId = 999L;

		// when
		when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

		// then
		assertThatThrownBy(() -> postService.getPost(postId))
			.isInstanceOf(PostNotFoundException.class)
			.hasMessage("해당 게시글을 찾을 수 없습니다.");
	}

}