// src/pages/PostDetailPage.jsx

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Card, Spinner, Alert, Button } from 'react-bootstrap';

const PostDetailPage = () => {
  const { id } = useParams();      // URL에서 id 파라미터 추출
  const navigate = useNavigate();  // 뒤로가기 등 내비게이션용

  // 게시물 단건 상태
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 컴포넌트 마운트 시(또는 id가 바뀔 때) 해당 게시물 조회
  useEffect(() => {
    const fetchPost = async () => {
      setLoading(true);
      setError(null);

      try {
        // GET /api/posts/{id} 엔드포인트 호출
        const response = await axios.get(`/api/posts/${id}`);
        setPost(response.data);
      } catch (err) {
        console.error('게시물 상세 조회 중 오류 발생:', err);
        setError('해당 게시물을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [id]);

  // 뒤로 가기 버튼
  const handleBack = () => {
    navigate(-1);
  };

  // 수정 페이지로 이동 (예: /posts/edit/:id)
  const handleEdit = () => {
    navigate(`/posts/edit/${id}`);
  };

  return (
    <Container className="mt-4">
      <Button variant="outline-secondary" onClick={handleBack} className="mb-3">
        &larr; 뒤로 가기
      </Button>

      {/* 로딩 중일 때 */}
      {loading && (
        <div className="d-flex justify-content-center my-5">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      )}

      {/* 에러 발생 시 */}
      {error && (
        <Alert variant="danger" className="my-3">
          {error}
        </Alert>
      )}

      {/* 데이터 로드 후 */}
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
                <Button variant="outline-primary" onClick={handleEdit}>
                  수정하기
                </Button>
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
