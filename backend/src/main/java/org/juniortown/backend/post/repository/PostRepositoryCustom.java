package org.juniortown.backend.post.repository;

import java.util.List;

import org.juniortown.backend.post.entity.Post;
import org.juniortown.backend.post.dto.PostSearch;

public interface PostRepositoryCustom {
	List<Post> getList(PostSearch postSearch);
}
