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
		Map<Long, CommentsInPost> map = new HashMap<>();
		List<CommentsInPost> roots = new ArrayList<>();
		for (Comment c : comments) {
			CommentsInPost comment = CommentsInPost.from(c);
			map.put(comment.getCommentId(), comment);
			if (c.getParent() == null) {
				roots.add(comment);
			} else{
				CommentsInPost parent = map.get(c.getParent().getId());
				if(parent != null) {
					parent.getChildren().add(comment);
				}
			}
		}
		return roots;
	}
}
