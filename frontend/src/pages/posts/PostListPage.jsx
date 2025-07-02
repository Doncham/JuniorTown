import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Table, Spinner, Alert, Button } from 'react-bootstrap';
import Pagination from 'react-bootstrap/Pagination';

const GROUP_SIZE = 10;

const PostListPage = () => {
  const navigate = useNavigate();

  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [page, setPage] = useState(0); // 0-based
  const [totalPages, setTotalPages] = useState(1);

  // 2. 페이지 변경마다 게시물 목록 조회
  useEffect(() => {
    const fetchPosts = async () => {
      setLoading(true);
      setError(null);
      try {
        const token = localStorage.getItem('jwt');
        // 서버가 쿼리 파라미터로 page를 받는 걸 권장
        const response = await axios.get(`/api/posts/${page}`, {
          headers: {
            'Authorization': `${token}`,
          },
        });

        setPosts(response.data.content);
        setTotalPages(response.data.totalPages);
      } catch (err) {
        setError('게시물 목록을 가져오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [page]);

  // 페이지네이션 그룹 계산 (0-based)
  const currentGroup = Math.floor(page / GROUP_SIZE);
  const startPage = currentGroup * GROUP_SIZE;
  const endPage = Math.min(startPage + GROUP_SIZE - 1, totalPages - 1);

  let items = [];
  for (let number = startPage; number <= endPage; number++) {
    items.push(
      <Pagination.Item
        key={number}
        active={number === page}
        onClick={() => setPage(number)}
      >
        {number + 1} {/* 사용자에겐 1-based로 보여줌 */}
      </Pagination.Item>
    );
  }

  return (
    <Container className="mt-4">
      <h2>게시물 목록</h2>
      <Pagination className="justify-content-center mt-4">
        <Pagination.First onClick={() => setPage(0)} disabled={page === 0} />
        <Pagination.Prev
          onClick={() => setPage(Math.max(0, page - 1))}
          disabled={page === 0}
        />
        {items}
        <Pagination.Next
          onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
          disabled={page === totalPages - 1}
        />
        <Pagination.Last
          onClick={() => setPage(totalPages - 1)}
          disabled={page === totalPages - 1}
        />
      </Pagination>

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
          {posts.length === 0 ? (
            <p>등록된 게시물이 없습니다.</p>
          ) : (
            <Table striped bordered hover responsive className="mt-3">
              <thead>
                <tr>
                  <th>#</th>
                  <th>제목</th>
                  <th>작성일</th>
                  <th>작성자</th>
                  <th>액션</th>
                </tr>
              </thead>
              <tbody>
                {posts.map((post, index) => (
                  <tr key={post.id}>
                    <td>{index + 1 + page * 10}</td>
                    <td style={{ cursor: 'pointer' }} onClick={() => navigate(`/posts/${post.id}`)}>
                      {post.title}
                    </td>
                    <td>{new Date(post.createdAt).toLocaleString('ko-KR')}</td>
                    <td>{post.username}</td>
                    <td>
                      <Button size="sm" variant="outline-primary" onClick={() => navigate(`/posts/${post.id}`)}>
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

      <div className="text-end mt-4">
        <Button variant="success" onClick={() => navigate('/posts/add')}>
          새 게시물 작성
        </Button>
      </div>
    </Container>
  );
};

export default PostListPage;
