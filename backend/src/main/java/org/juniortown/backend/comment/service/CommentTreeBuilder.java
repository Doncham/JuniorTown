package org.juniortown.backend.comment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.juniortown.backend.comment.dto.response.CommentsInPost;
import org.juniortown.backend.comment.entity.Comment;

public class CommentTreeBuilder {
	public static List<CommentsInPost> build(List<Comment> comments) {
		if(comments.isEmpty()) return List.of();
		List<CommentsInPost> roots = new ArrayList<>();
		Map<Long, CommentsInPost> map = new HashMap<>();
		// 1. 모든 댓글을 Map에 넣는다
		for (Comment c : comments) {
			CommentsInPost comment = CommentsInPost.from(c);
			map.put(comment.getCommentId(), comment);
		}
		// 2. 부모-자식 관계 설정
		for (Comment c : comments) {
			CommentsInPost comment = map.get(c.getId());
			if (c.getParent() == null) {
				roots.add(comment);
			} else {
				CommentsInPost parent = map.get(c.getParent().getId());
				if (parent != null) {
					parent.getChildren().add(comment);
				}
			}
		}
		return roots;
	}
}
