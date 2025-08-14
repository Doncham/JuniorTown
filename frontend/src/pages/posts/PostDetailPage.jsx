// src/pages/posts/PostDetailPage.jsx
import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Card, Spinner, Alert, Button } from 'react-bootstrap';
import CommentSection from './CommentPage';
import { useAuth } from '../../auth/AuthContext';

// --------- 미니 아이콘 컴포넌트 (가벼운 라인 스타일) ----------
const EyeIcon = ({ size = 18, className }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    className={className}
    style={{ verticalAlign: '-2px' }}
  >
    <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" strokeWidth="1.8" />
    <circle cx="12" cy="12" r="3" strokeWidth="1.8" />
  </svg>
);

const LikeButton = ({ isLiked, count, busy, onClick }) => (
  <Button
    variant={isLiked ? 'danger' : 'outline-danger'}
    disabled={busy}
    onClick={onClick}
    className="d-inline-flex align-items-center"
    style={{
      borderRadius: 999,
      padding: '6px 12px',
      fontWeight: 600,
      transition: 'transform 120ms ease',
      boxShadow: isLiked ? '0 2px 6px rgba(220,53,69,.25)' : 'none'
    }}
    aria-pressed={isLiked}
    aria-label={isLiked ? '좋아요 취소' : '좋아요'}
  >
    <span
      style={{
        fontSize: 16,
        marginRight: 8,
        transform: isLiked ? 'scale(1.05)' : 'none',
        transition: 'transform 120ms ease'
      }}
    >
      {isLiked ? '❤️' : '🤍'}
    </span>
    <span style={{ minWidth: 28, textAlign: 'right' }}>
      {Number(count ?? 0).toLocaleString()}
    </span>
  </Button>
);

const PostDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, token } = useAuth();

  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);      // 초기 로딩
  const [error, setError] = useState(null);
  const [likeBusy, setLikeBusy] = useState(false);   // 좋아요 처리 중 잠금

  const myUserId = useMemo(() => user?.id ?? null, [user]);

  // 최신 요청만 반영하기 위한 ID
  const lastLikeReqIdRef = useRef(0);

  // 게시글 조회
  const fetchPost = useCallback(async () => {
    if (!post) setLoading(true);
    setError(null);
    try {
      const res = await axios.get(`/api/posts/details/${id}`, {
        headers: token ? { Authorization: token } : undefined,
      });
      setPost(res.data);
    } catch (err) {
      setError('해당 게시물을 불러오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [id, token]);

  useEffect(() => {
    fetchPost();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchPost]);

  const handleBack = () => navigate(-1);
  const handleEdit = () => navigate(`/posts/edit/${id}`);

  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    try {
      await axios.delete(`/api/posts/${id}`, {
        headers: token ? { Authorization: token } : undefined,
      });
      alert('게시글이 삭제되었습니다.');
      navigate('/posts');
    } catch {
      alert('게시물 삭제에 실패했습니다.');
    }
  };

  // 좋아요 토글 (낙관적 업데이트 + 중복 클릭 방지 + 최신 응답만 반영)
  const handleLike = useCallback(async () => {
    if (!token) {
      alert('로그인이 필요합니다.');
      navigate('/login', { replace: true });
      return;
    }
    if (!post || likeBusy) return;

    setLikeBusy(true);
    const reqId = ++lastLikeReqIdRef.current;

    const prevIsLiked = !!post.isLiked;
    const prevCount = Number(post.likeCount ?? 0);

    // 낙관적 반영
    const nextIsLiked = !prevIsLiked;
    const nextCount = nextIsLiked ? prevCount + 1 : Math.max(0, prevCount - 1);
    setPost(p => (p ? { ...p, isLiked: nextIsLiked, likeCount: nextCount } : p));

    try {
      const res = await axios.post(`/api/posts/likes/${post.id}`, null, {
        headers: { Authorization: token },
      });
      // 최신 요청만 반영
      if (reqId === lastLikeReqIdRef.current) {
        const serverIsLiked = !!res.data?.isLiked;
        const fixedCount = serverIsLiked
          ? (prevIsLiked ? prevCount : prevCount + 1)
          : (prevIsLiked ? Math.max(0, prevCount - 1) : prevCount);

        setPost(p => (p ? { ...p, isLiked: serverIsLiked, likeCount: fixedCount } : p));
      }
    } catch {
      if (reqId === lastLikeReqIdRef.current) {
        // 실패 롤백
        setPost(p => (p ? { ...p, isLiked: prevIsLiked, likeCount: prevCount } : p));
        alert('좋아요 처리에 실패했습니다.');
      }
    } finally {
      if (reqId === lastLikeReqIdRef.current) setLikeBusy(false);
    }
  }, [post, token, navigate, likeBusy]);

  const isOwner = post && myUserId && String(post.userId) === String(myUserId);

  return (
    <Container className="mt-4">
      <Button variant="outline-secondary" onClick={handleBack} className="mb-3">
        &larr; 뒤로 가기
      </Button>

      {loading && (
        <div className="d-flex justify-content-center my-5">
          <Spinner animation="border" role="status" />
        </div>
      )}

      {error && !loading && (
        <Alert variant="danger" className="my-3">
          {error}
        </Alert>
      )}

      {!loading && !error && (
        <>
          {post ? (
            <Card className="shadow-sm">
              {/* ===== 헤더: 제목 / 메타 / 좋아요 버튼 ===== */}
              <Card.Header className="bg-white border-0">
                <div className="d-flex align-items-start justify-content-between gap-3">
                  <div className="flex-grow-1">
                    <h4 className="mb-2" style={{ wordBreak: 'keep-all' }}>
                      {post.title}
                    </h4>

                    {/* 메타 라인: 작성일 / 조회수 */}
                    <div className="d-flex flex-wrap align-items-center gap-1">
                      <span
                        className="badge rounded-pill text-bg-light"
                        style={{ fontWeight: 500 }}
                        title="작성일"
                      >
                        {new Date(post.createdAt).toLocaleString('ko-KR', {
                          year: 'numeric',
                          month: '2-digit',
                          day: '2-digit',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>

                      <span className="text-muted">•</span>

                      <span
                        className="badge rounded-pill text-bg-light d-inline-flex align-items-center"
                        style={{ gap: 6, fontWeight: 500 }}
                        title="조회수"
                      >
                        <EyeIcon />
                        {post.readCount?.toLocaleString()}회
                      </span>
                    </div>
                  </div>

                  {/* 좋아요 버튼 */}
                  <LikeButton
                    isLiked={!!post.isLiked}
                    count={post.likeCount}
                    busy={likeBusy}
                    onClick={handleLike}
                  />
                </div>
              </Card.Header>

              {/* ===== 본문 ===== */}
              <Card.Body>
                <Card.Text style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
                  {post.content}
                </Card.Text>

                {/* 댓글 섹션 */}
                <div className="mt-5">
                  <CommentSection
                    postId={post.id}
                    comments={post.comments}
                    myUserId={myUserId}
                    refreshPost={fetchPost}
                  />
                </div>
              </Card.Body>

              {/* ===== 푸터: 소유자 액션 ===== */}
              <Card.Footer className="bg-white border-0 text-end">
                {isOwner ? (
                  <>
                    <Button variant="outline-primary" onClick={handleEdit} className="me-2">
                      수정하기
                    </Button>
                    <Button variant="outline-danger" onClick={handleDelete}>
                      삭제하기
                    </Button>
                  </>
                ) : (
                  <span className="text-muted">본인 게시글만 수정/삭제할 수 있습니다.</span>
                )}
              </Card.Footer>
            </Card>
          ) : (
            <p className="text-center text-muted">해당 게시물이 존재하지 않습니다.</p>
          )}
        </>
      )}
    </Container>
  );
};

export default PostDetailPage;
