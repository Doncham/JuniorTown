package org.juniortown.backend.dummyData;

import org.juniortown.backend.post.entity.Category;
import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DummyDataInit implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final AuthService authService;

	@Override
	public void run(String... args) throws Exception {
		// 회원 가입
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.email("test@email.com")
			.username("testUser")
			.password("1234")
			.build();

		authService.signUp(signUpDTO);

		User user = userRepository.findByEmail(signUpDTO.getEmail())
			.orElseThrow(() -> new RuntimeException("User not found"));

		// 게시글 생성
		Category[] categories = Category.values();
		for (int i = 0; i < 25; i++) {
			createPost("title" + i,"content" + i, user, categories[i % categories.length]);
		}
	}

	private void createPost(String Dummy_Post_Title, String content, User user, Category category) {
		Post testPost1 = Post.builder()
			.title(Dummy_Post_Title)
			.content(content)
			.user(user)
			.category(category)
			.build();
		postRepository.save(testPost1);
	}
}
