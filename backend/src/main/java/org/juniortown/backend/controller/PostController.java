package org.juniortown.backend.controller;

// @Slf4j
// @RestController
// @RequiredArgsConstructor
// public class PostController {
// 	private final PostService postService;
//
// 	/*@PostMapping("/posts")
// 	public void post(@RequestBody @Valid PostCreate request) {
// 		// Case1. 저장한 데이터 Entity -> response로 응답하기
// 		// Case2. 저장한 데이터의 primary_id -> response로 응답하기
// 		request.validate();
// 		postService.write(request);
// 	}*/
//
// 	// 단건 조회
// 	@GetMapping("/posts/{postId}")
// 	public PostResponse get(@PathVariable(name = "postId") Long id) {
// 		PostResponse response = postService.get(id);
// 		return response;
// 	}
//
// 	// 여러건 조회
// 	@GetMapping("/posts")
// 	public List<PostResponse> getList(@ModelAttribute PostSearch postSearch) {
// 		return postService.getList(postSearch);
// 	}
//
// 	// 수정
// 	@PatchMapping("/posts/{postId}")
// 	public void edit(@PathVariable Long postId, @RequestBody @Valid PostEdit request) {
// 		postService.edit(postId, request);
// 	}
//
// 	@DeleteMapping("/posts/{postId}")
// 	public void delete(@PathVariable Long postId) {
// 		postService.delete(postId);
// 	}
// }
