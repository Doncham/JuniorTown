package org.juniortown.backend.repository;

import java.util.List;

import org.juniortown.backend.domain.Post;
import org.juniortown.backend.request.PostSearch;

public interface PostRepositoryCustom {
	List<Post> getList(PostSearch postSearch);
}
