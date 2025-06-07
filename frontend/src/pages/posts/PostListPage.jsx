// src/pages/PostListPage.jsx

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Table, Spinner, Alert, Button } from 'react-bootstrap';

const PostListPage = () => {
  const navigate = useNavigate();

  // 1. 상태 정의
  const [posts, setPosts] = useState([]);        // 게시물 배열
  const [loading, setLoading] = useState(true);  // 로딩 중 표시용
  const [error, setError] = useState(null);      // 에러 메시지 저장

  // 2. 마운트 시점에 게시물 목록 조회
  useEffect(() => {
    const fetchPosts = async () => {
      try {
        // axios로 GET 요청
        const response = await axios.get('/api/posts?page=1&size=5');
        setPosts(response.data);
      } catch (err) {
        console.error('게시물 목록 조회 중 오류 발생:', err);
        setError('게시물 목록을 가져오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, []);

  // 3. 각 게시물을 클릭하면 상세 페이지로 이동
  const handleRowClick = (id) => {
    navigate(`/posts/${id}`);
  };

  return (
    <Container className="mt-4">
      <h2>게시물 목록</h2>

      {/* 4. 로딩 중일 때 스피너 */}
      {loading && (
        <div className="d-flex justify-content-center my-5">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      )}

      {/* 5. 에러 발생 시 경고창 */}
      {error && (
        <Alert variant="danger" className="my-3">
          {error}
        </Alert>
      )}

      {/* 6. 로딩이 끝나고, 에러 없으면 테이블로 게시물 표시 */}
      {!loading && !error && (
        <>
          {posts.length === 0 ? (
            <p>등록된 게시물이 없습니다.</p>
          ) : (
            <Table striped bordered hover responsive className="mt-3">
              <thead>
                <tr>
                  <th>#</th>
                  <th>제목</th>
                  <th>작성일</th>
                  <th>액션</th>
                </tr>
              </thead>
              <tbody>
                {posts.map((post, index) => (
                  <tr key={post.id}>
                    <td>{index + 1}</td>
                    <td style={{ cursor: 'pointer' }} onClick={() => handleRowClick(post.id)}>
                      {post.title}
                    </td>
                    <td>{new Date(post.createdAt).toLocaleString('ko-KR')}</td>
                    <td>
                      <Button size="sm" variant="outline-primary" onClick={() => handleRowClick(post.id)}>
                        상세 보기
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </>
      )}

      {/* 7. 새 게시물 작성 버튼 */}
      <div className="text-end mt-4">
        <Button variant="success" onClick={() => navigate('/posts/add')}>
          새 게시물 작성
        </Button>
      </div>
    </Container>
  );
};

export default PostListPage;
