import React, { useState } from 'react';
import { Button, Form, Card } from 'react-bootstrap';
import axios from 'axios';
export default CommentSection;

function CommentSection({ postId, comments, myUserId, refreshPost }) {
  // 댓글 입력/수정 상태
  const [commentContent, setCommentContent] = useState('');
  const [replyContent, setReplyContent] = useState('');
  const [editingId, setEditingId] = useState(null); // 수정 중인 댓글 id
  const [editingContent, setEditingContent] = useState('');
  const [replyParentId, setReplyParentId] = useState(null); // 대댓글 입력창 표시용 parent id

  // 댓글 등록
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!commentContent.trim()) return;
    try {
      const token = localStorage.getItem('jwt');
      await axios.post('/api/comments', {
        postId,
        content: commentContent,
        parentId: null,
      }, { headers: { Authorization: token } });
      setCommentContent('');
      refreshPost(); // 게시글 및 댓글 데이터 새로고침
    } catch (err) {
      alert('댓글 등록 실패');
    }
  };

  // 대댓글 등록
  const handleReplySubmit = async (e, parentId) => {
    e.preventDefault();
    if (!replyContent.trim()) return;
    try {
      const token = localStorage.getItem('jwt');
      await axios.post('/api/comments', {
        postId,
        content: replyContent,
        parentId,
      }, { headers: { Authorization: token } });
      setReplyContent('');
      setReplyParentId(null);
      refreshPost();
    } catch (err) {
      alert('대댓글 등록 실패');
    }
  };

  // 댓글 삭제
  const handleDelete = async (commentId) => {
    if (!window.confirm('댓글을 삭제할까요?')) return;
    try {
      const token = localStorage.getItem('jwt');
      await axios.delete(`/api/comments/${commentId}`, {
        headers: { Authorization: token },
      });
      refreshPost();
    } catch (err) {
      alert('댓글 삭제 실패');
    }
  };

  // 댓글 수정
  const handleEditSubmit = async (e, commentId) => {
    e.preventDefault();
    if (!editingContent.trim()) return;
    try {
      const token = localStorage.getItem('jwt');
      await axios.patch(`/api/comments/${commentId}`, {
        content: editingContent,
      }, { headers: { Authorization: token } });
      setEditingId(null);
      setEditingContent('');
      refreshPost();
    } catch (err) {
      alert('댓글 수정 실패');
    }
  };

  // 트리형 댓글 렌더링
  const renderComments = (commentList, depth = 0) =>
    commentList.map((comment) => (
      <div key={comment.commentId} style={{ marginLeft: depth * 24, marginTop: 12 }}>
        <Card className="mb-1">
          <Card.Body className="py-2 px-3">
            <div className="d-flex justify-content-between align-items-center">
              <span>
                <b>{comment.username}</b>{" "}
                <small className="text-muted">
                  {new Date(comment.createdAt).toLocaleString("ko-KR")}
                </small>
                <small className="text-muted ms-2">
                  {comment.createdAt !== comment.updatedAt && comment.deletedAt === null && " (수정됨)"}
                </small>
              </span>
              {/* 내 댓글만 수정/삭제 */}
              {!comment.deletedAt && myUserId && myUserId === comment.userId && (
                <span>
                  {editingId === comment.commentId ? null : (
                    <>
                      <Button
                        variant="link"
                        size="sm"
                        onClick={(e) => {
                          setEditingId(comment.commentId);
                          setEditingContent(comment.content);
                          setReplyParentId(null); // 대댓글 입력창 닫기
                        }}
                        style={{ textDecoration: 'underline', color: '#0d6efd' }}
                      >
                        수정
                      </Button>
                      <Button
                        variant="link"
                        size="sm"
                        onClick={() => handleDelete(comment.commentId)}
                        style={{ textDecoration: 'underline', color: '#dc3545' }}
                      >
                        삭제
                      </Button>
                    </>
                  )}
                </span>
              )}
            </div>
            {/* 댓글 내용 or 수정폼 */}
            {editingId === comment.commentId ? (
              <Form
                className="mt-2"
                onSubmit={(e) => handleEditSubmit(e, comment.commentId)}
              >
                <Form.Control
                  as="textarea"
                  size="sm"
                  value={editingContent}
                  onChange={(e) => setEditingContent(e.target.value)}
                  rows={2}
                  className="mb-2"
                />
                <div className="d-flex">
                  <Button
                    type="submit"
                    variant="primary"
                    size="sm"
                    className="me-2"
                  >
                    저장
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => setEditingId(null)}
                  >
                    취소
                  </Button>
                </div>
              </Form>
            ) : (
              <div style={{ whiteSpace: 'pre-wrap', color: comment.deletedAt ? '#adb5bd' : undefined }}>
                {comment.deletedAt ? '삭제된 댓글입니다.' : comment.content}
              </div>
            )}

            {/* 대댓글 입력창 */}
            {!comment.deletedAt && comment.parentId === null && (
              <>
                {replyParentId === comment.commentId ? (
                  <Form
                    className="mt-2"
                    onSubmit={(e) => handleReplySubmit(e, comment.commentId)}
                  >
                    <Form.Control
                      as="textarea"
                      size="sm"
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      rows={2}
                      className="mb-2"
                      placeholder="대댓글을 입력하세요"
                    />
                    <div className="d-flex">
                      <Button
                        type="submit"
                        variant="success"
                        size="sm"
                        className="me-2"
                      >
                        등록
                      </Button>
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setReplyParentId(null)}
                      >
                        취소
                      </Button>
                    </div>
                  </Form>
                ) : editingId !== comment.commentId ? (
                  <Button
                    variant="outline-secondary"
                    size="sm"
                    className="mt-2"
                    onClick={() => setReplyParentId(comment.commentId)}
                  >
                    답글
                  </Button>
                ) : null}

              </>
            )}
          </Card.Body>
        </Card>
        {/* 대댓글 트리 재귀 */}
        {comment.children && comment.children.length > 0 &&
          renderComments(comment.children, depth + 1)}
      </div>
    ));

  return (
    <div className="mt-4">
      {/* 댓글 입력 폼 */}
      <Form onSubmit={handleCommentSubmit} className="mb-3">
        <Form.Group controlId="commentContent" className="d-flex">
          <Form.Control
            as="textarea"
            rows={2}
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            placeholder="댓글을 입력하세요"
          />
          <Button type="submit" variant="primary" className="ms-2" style={{ minWidth: 80 }}>
            등록
          </Button>
        </Form.Group>
      </Form>
      {/* 댓글 목록 */}
      {comments && comments.length > 0 ? (
        renderComments(comments)
      ) : (
        <div className="text-muted">아직 댓글이 없습니다.</div>
      )}
    </div>
  );
};


