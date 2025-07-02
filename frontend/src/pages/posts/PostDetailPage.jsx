import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Card, Spinner, Alert, Button } from 'react-bootstrap';
import base64 from 'base-64';

const PostDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // 로그인 사용자 id 저장
  const [myUserId, setMyUserId] = useState(null);

  // 게시글 조회 및 내 userId 파싱
  useEffect(() => {
    const fetchPost = async () => {
      setLoading(true);
      setError(null);
      try {
        const token = localStorage.getItem('jwt');
        // JWT 파싱 (try-catch로 에러 방어 추천)
        let loginUserId = null;
        if (token) {
          const payload = JSON.parse(base64.decode(token.split('.')[1]));
          loginUserId = payload.userId;
          setMyUserId(loginUserId);
        }
        // GET /api/posts/details/{id}
        const response = await axios.get(`/api/posts/details/${id}`, {
          headers: {
            'Authorization': `${token}`,
          },
        });
        setPost(response.data);
      } catch (err) {
        setError('해당 게시물을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchPost();
  }, [id]);

  const handleBack = () => navigate(-1);

  // 게시글 수정 페이지로 이동
  const handleEdit = () => {
    navigate(`/posts/edit/${id}`);
  };

  // 게시글 삭제 (선택)
  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    try {
      const token = localStorage.getItem('jwt');
      await axios.delete(`/api/posts/${id}`, {
        headers: { 'Authorization': `${token}` },
      });
      alert('게시글이 삭제되었습니다.');
      navigate('/posts');
    } catch (err) {
      alert('게시물 삭제에 실패했습니다.');
    }
  };

  // 내 userId와 게시글 userId 비교
  const isOwner = post && myUserId && String(post.userId) === String(myUserId);

  return (
    <Container className="mt-4">
      <Button variant="outline-secondary" onClick={handleBack} className="mb-3">
        &larr; 뒤로 가기
      </Button>

      {loading && (
        <div className="d-flex justify-content-center my-5">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      )}

      {error && (
        <Alert variant="danger" className="my-3">
          {error}
        </Alert>
      )}

      {!loading && !error && (
        <>
          {post ? (
            <Card className="shadow-sm">
              <Card.Header className="bg-white border-0">
                <h4 className="mb-0">{post.title}</h4>
                <small className="text-muted">
                  작성일:{" "}
                  {new Date(post.createdAt).toLocaleString("ko-KR", {
                    year: "numeric",
                    month: "2-digit",
                    day: "2-digit",
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </small>
              </Card.Header>
              <Card.Body>
                <Card.Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>
                  {post.content}
                </Card.Text>
              </Card.Body>
              <Card.Footer className="bg-white border-0 text-end">
                {/* 소유자만 수정/삭제 버튼 노출 */}
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
