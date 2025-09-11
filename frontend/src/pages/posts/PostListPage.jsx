// src/pages/PostListPage.jsx
import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import axios from 'axios';
import {
  Container,
  Table,
  Spinner,
  Alert,
  Button,
  Pagination,
  Nav,
  Badge,
  Card,
  Dropdown,
  ButtonGroup,
} from 'react-bootstrap';

const GROUP_SIZE = 10;
const DEFAULT_PAGE_SIZE = 10;

// 카테고리 메타 (라벨/아이콘/컬러)
const CATEGORY_META = {
  QUESTION: { label: '질문', icon: '❓', tint: '#0d6efd' },
  STUDY: { label: '스터디', icon: '📚', tint: '#20c997' },
  NOTICE: { label: '공지', icon: '📢', tint: '#6f42c1' },
  SECOND_HAND_MARKET: { label: '중고장터', icon: '💱', tint: '#fd7e14' },
  MENTOR: { label: '멘토', icon: '🧭', tint: '#198754' },
};
const CATEGORY_ORDER = ['QUESTION', 'STUDY', 'NOTICE', 'SECOND_HAND_MARKET', 'MENTOR'];

// URL의 :category → 내부 Enum 정규화
function normalizeCategoryFromParam(raw) {
  if (!raw) return 'QUESTION';
  const up = String(raw).toUpperCase();
  if (CATEGORY_META[up]) return up;
  const lowerMap = {
    question: 'QUESTION',
    study: 'STUDY',
    notice: 'NOTICE',
    second_hand_market: 'SECOND_HAND_MARKET',
    mentor: 'MENTOR',
  };
  return lowerMap[String(raw).toLowerCase()] ?? 'QUESTION';
}

// 날짜/숫자 포맷
const fmtDate = (iso) =>
  iso
    ? new Date(iso).toLocaleString('ko-KR', {
      year: '2-digit',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'Asia/Seoul',
    })
    : '';
const fmtNumber = (n) =>
  typeof n === 'number' ? n.toLocaleString('ko-KR') : '0';

export default function PostListPage() {
  const navigate = useNavigate();
  const { category: categoryParam } = useParams();

  const category = useMemo(
    () => normalizeCategoryFromParam(categoryParam),
    [categoryParam]
  );

  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 0-based 페이지
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);

  // 정렬: latest, views, likes, comments
  const [sort, setSort] = useState('latest');

  // 카테고리/정렬 바뀌면 1페이지로 리셋
  useEffect(() => {
    setPage(0);
  }, [category, sort]);

  // 게시물 조회
  useEffect(() => {
    const controller = new AbortController();
    const fetchPosts = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await axios.get('/api/posts', {
          params: {
            page: page,           // 서버가 0-based 페이지 받는다고 가정
            size: pageSize,      // 페이지 크기
            category: category,   // QUESTION 등 Enum 문자열
            sort: sort,           // 정렬 키 (백엔드와 합의)
          },
          signal: controller.signal,
        });

        setPosts(response.data.content ?? []);
        setTotalPages(response.data.totalPages ?? 1);
        if (typeof response.data.size === 'number' && response.data.size > 0) {
          setPageSize(response.data.size);
        }
      } catch (err) {
        if (axios.isCancel(err)) return;
        console.error('게시물 목록 조회 실패:', err);
        setError('게시물 목록을 가져오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
    return () => controller.abort();
  }, [page, category, sort]);

  // 페이지네이션 그룹 계산 (0-based)
  const currentGroup = Math.floor(page / GROUP_SIZE);
  const startPage = currentGroup * GROUP_SIZE;
  const endPage = Math.min(startPage + GROUP_SIZE - 1, Math.max(0, totalPages - 1));

  const heroStyle = useMemo(() => {
    const tint = CATEGORY_META[category].tint;
    // 카테고리별 배너 그라데이션
    return {
      background: `linear-gradient(180deg, ${tint}18 0%, #ffffff 100%)`,
      borderColor: `${tint}33`,
    };
  }, [category]);

  return (
    <Container className="mt-4">
      {/* 헤더 배너 */}
      <Card className="border rounded-4 shadow-sm mb-3" style={heroStyle}>
        <Card.Body className="d-flex justify-content-between align-items-center flex-wrap gap-3">
          <div>
            <div className="d-flex align-items-center gap-2">
              <span style={{ fontSize: 22 }} aria-hidden="true">
                {CATEGORY_META[category].icon}
              </span>
              <h2 className="h4 m-0">{CATEGORY_META[category].label}</h2>
              <Badge bg="light" text="dark" className="border">
                {category}
              </Badge>
            </div>
            <div className="text-muted small mt-1">
              {CATEGORY_META[category].label} 카테고리의 최신 글을 확인하세요.
            </div>
          </div>

          <div className="d-flex align-items-center gap-2">
            <Dropdown as={ButtonGroup}>
              <Button variant="outline-secondary" className="rounded-pill">
                정렬: {
                  {
                    latest: '최신순',
                    views: '조회순',
                    likes: '좋아요순',
                    comments: '댓글순',
                  }[sort]
                }
              </Button>
              <Dropdown.Toggle split variant="outline-secondary" id="sort-split" className="rounded-pill" />
              <Dropdown.Menu align="end">
                <Dropdown.Item active={sort === 'latest'} onClick={() => setSort('latest')}>
                  최신순
                </Dropdown.Item>
                <Dropdown.Item active={sort === 'views'} onClick={() => setSort('views')}>
                  조회순
                </Dropdown.Item>
                <Dropdown.Item active={sort === 'likes'} onClick={() => setSort('likes')}>
                  좋아요순
                </Dropdown.Item>
                <Dropdown.Item active={sort === 'comments'} onClick={() => setSort('comments')}>
                  댓글순
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>

            <Button
              as={Link}
              to={`/posts/add?category=${encodeURIComponent(category)}`}
              variant="primary"
              className="rounded-pill"
            >
              글쓰기
            </Button>
          </div>
        </Card.Body>
      </Card>

      {/* 카테고리 탭 */}
      <Nav variant="pills" className="gap-2 flex-wrap mb-3" activeKey={category}>
        {CATEGORY_ORDER.map((key) => (
          <Nav.Item key={key}>
            <Nav.Link
              as={Link}
              to={`/posts/category/${key}`}
              eventKey={key}
              className="rounded-pill px-3 py-1"
              style={{
                border: '1px solid rgba(0,0,0,0.08)',
              }}
              aria-label={`${CATEGORY_META[key].label} 목록으로 이동`}
            >
              <span className="me-1" aria-hidden="true">{CATEGORY_META[key].icon}</span>
              {CATEGORY_META[key].label}
            </Nav.Link>
          </Nav.Item>
        ))}
      </Nav>

      {/* 페이지네이션 */}
      <Pagination className="justify-content-center mt-3">
        <Pagination.First onClick={() => setPage(0)} disabled={page === 0} />
        <Pagination.Prev onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0} />
        {Array.from({ length: Math.max(0, endPage - startPage + 1) }).map((_, idx) => {
          const number = startPage + idx;
          return (
            <Pagination.Item key={number} active={number === page} onClick={() => setPage(number)}>
              {number + 1}
            </Pagination.Item>
          );
        })}
        <Pagination.Next
          onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
          disabled={page === totalPages - 1}
        />
        <Pagination.Last onClick={() => setPage(totalPages - 1)} disabled={page === totalPages - 1} />
      </Pagination>

      {/* 본문 */}
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
            <Card className="border-0 shadow-sm rounded-4 mt-3">
              <Card.Body className="text-center text-muted py-5">
                <div className="mb-2" style={{ fontSize: 28 }}>🗒️</div>
                <div className="fw-semibold">등록된 게시물이 없습니다.</div>
                <div className="small mt-1">첫 글을 작성해보세요.</div>
                <Button
                  as={Link}
                  to={`/posts/add?category=${encodeURIComponent(category)}`}
                  variant="primary"
                  size="sm"
                  className="mt-3 rounded-pill"
                >
                  {CATEGORY_META[category].label}에 글쓰기
                </Button>
              </Card.Body>
            </Card>
          ) : (
            <Table striped hover responsive className="mt-3 align-middle">
              <thead>
                <tr style={{ background: 'rgba(0,0,0,0.02)' }}>
                  <th style={{ width: 72 }}>#</th>
                  <th>제목</th>
                  <th style={{ width: 160 }}>작성일</th>
                  <th style={{ width: 120 }}>작성자</th>
                  <th style={{ width: 120, textAlign: 'center' }}>좋아요</th>
                  <th style={{ width: 100 }}>조회수</th>
                  <th style={{ width: 120 }}>액션</th>
                </tr>
              </thead>
              <tbody>
                {posts.map((post, index) => (
                  <tr key={post.id}>
                    <td>{index + 1 + page * pageSize}</td>
                    <td
                      style={{ cursor: 'pointer', maxWidth: 560 }}
                      onClick={() => navigate(`/posts/${post.id}`)}
                    >
                      <div className="fw-semibold text-truncate">{post.title}</div>
                      <div className="small text-muted">
                        {/* 보조 메타: 댓글/조회 간단히 */}
                        💬 {fmtNumber(post.commentCount)} · 👁️ {fmtNumber(post.readCount)}
                      </div>
                    </td>
                    <td>{fmtDate(post.createdAt)}</td>
                    <td>{post.nickname ?? post.username ?? '익명'}</td>
                    <td style={{ textAlign: 'center', fontSize: '1.1rem' }}>
                      <span
                        role="img"
                        aria-label={post.isLiked ? '좋아요 취소' : '좋아요'}
                        style={{ color: post.isLiked ? '#dc3545' : '#adb5bd' }}
                      >
                        {post.isLiked ? '❤️' : '🤍'}
                      </span>
                      <span style={{ fontWeight: 'bold', marginLeft: 6 }}>{fmtNumber(post.likeCount)}</span>
                    </td>
                    <td>{fmtNumber(post.readCount)}</td>
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
    </Container>
  );
}
