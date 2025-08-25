// src/pages/Home.jsx
import { useEffect, useState, useMemo } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, ListGroup, Button, Placeholder, Alert } from 'react-bootstrap';

// ─────────────────────────────────────────────────────────────────────────────
// 기대하는 백엔드 응답 예시 (GET /api/home?limit=5)
// {
//   "sections": {
//     "QUESTION": [{id, title, createdAt, readCount, commentCount, authorName}, ...],
//     "STUDY": [...],
//     "NOTICE": [...],
//     "SECOND_HAND_MARKET": [...],
//     "MENTOR": [...]
//   }
// }
// ─────────────────────────────────────────────────────────────────────────────

// Enum → 라벨/경로 매핑
const CATEGORY_META = {
  QUESTION: { label: '질문', path: '/posts?category=QUESTION' },
  STUDY: { label: '스터디', path: '/posts?category=STUDY' },
  NOTICE: { label: '공지', path: '/posts?category=NOTICE' },
  SECOND_HAND_MARKET: { label: '중고장터', path: '/posts?category=SECOND_HAND_MARKET' },
  MENTOR: { label: '멘토', path: '/posts?category=MENTOR' },
};

// 표시 순서 고정 (원하는 대로 변경 가능)
const CATEGORY_ORDER = ['QUESTION', 'STUDY', 'NOTICE', 'SECOND_HAND_MARKET', 'MENTOR'];

// 날짜 포맷터
const fmtDate = (iso) =>
  new Date(iso).toLocaleString('ko-KR', {
    year: '2-digit',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });

// 개별 섹션 컴포넌트
function CategorySection({ category, items, loading }) {
  const meta = CATEGORY_META[category];
  return (
    <Card className="mb-4 shadow-sm h-100">
      <Card.Header className="bg-white d-flex align-items-center justify-content-between">
        <strong>{meta.label}</strong>
        <Button as={Link} to={meta.path} variant="outline-secondary" size="sm">
          더보기
        </Button>
      </Card.Header>

      {loading ? (
        <ListGroup variant="flush">
          {Array.from({ length: 5 }).map((_, i) => (
            <ListGroup.Item key={i}>
              <Placeholder as="div" animation="glow">
                <Placeholder xs={8} /> <Placeholder xs={2} /> <br />
                <Placeholder xs={4} />
              </Placeholder>
            </ListGroup.Item>
          ))}
        </ListGroup>
      ) : (
        <ListGroup variant="flush">
          {(items ?? []).length === 0 && (
            <ListGroup.Item className="text-muted">게시글이 없습니다.</ListGroup.Item>
          )}
          {(items ?? []).map((post) => (
            <ListGroup.Item
              key={post.id}
              as={Link}
              to={`/posts/${post.id}`}
              className="d-block text-decoration-none"
              style={{ color: 'inherit' }}
            >
              <div className="d-flex align-items-start justify-content-between gap-3">
                <div className="flex-grow-1">
                  <div className="fw-semibold text-truncate">{post.title}</div>
                  <div className="small text-muted mt-1">
                    {post.authorName ?? '익명'} · {fmtDate(post.createdAt)}
                  </div>
                </div>
                <div className="text-nowrap small text-muted">
                  👁️ {post.readCount?.toLocaleString?.() ?? 0}
                  <span className="ms-2">💬 {post.commentCount ?? 0}</span>
                </div>
              </div>
            </ListGroup.Item>
          ))}
        </ListGroup>
      )}
    </Card>
  );
}

export default function Home() {
  const [sections, setSections] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState(null);

  const ordered = useMemo(() => CATEGORY_ORDER, []);

  useEffect(() => {
    const ctrl = new AbortController();
    setLoading(true);
    setErr(null);

    axios
      .get('/api/home', { params: { limit: 5 }, signal: ctrl.signal })
      .then((res) => {
        // 방어적 파싱
        const data = res.data?.sections ?? {};
        setSections(data);
      })
      .catch((e) => {
        if (axios.isCancel(e)) return;
        setErr('홈 데이터를 불러오는 중 문제가 발생했습니다.');
      })
      .finally(() => setLoading(false));

    return () => ctrl.abort();
  }, []);

  return (
    <Container className="py-4">
      {err && <Alert variant="danger" className="mb-4">{err}</Alert>}

      <Row xs={1} md={2} lg={2} xl={3} className="g-4">
        {ordered.map((cat) => (
          <Col key={cat}>
            <CategorySection
              category={cat}
              items={sections?.[cat] ?? []}
              loading={loading}
            />
          </Col>
        ))}
      </Row>
    </Container>
  );
}
