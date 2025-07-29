package org.juniortown.backend.dummyData;

import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.repository.PostRepository;
import org.juniortown.backend.user.entity.User;
import org.juniortown.backend.user.repository.UserRepository;
import org.juniortown.backend.user.request.SignUpDTO;
import org.juniortown.backend.user.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
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
			.password("password")
			.build();
		// 와 반환값 살벌하네
		authService.signUp(signUpDTO);

		User user = userRepository.findByEmail(signUpDTO.getEmail())
			.orElseThrow(() -> new RuntimeException("User not found"));

		// 게시글 생성
		Post testPost1 = Post.builder()
			.title("Dummy Post Title")
			.content("This is a dummy post content.")
			.user(user)
			.build();
		postRepository.save(testPost1);

		Post testPost2 = Post.builder()
			.title("Another Dummy Post Title")
			.content("This is another dummy post content.")
			.user(user)
			.build();
		postRepository.save(testPost2);
	}
}
