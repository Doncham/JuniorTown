// src/pages/Home.jsx
import { useEffect, useState, useMemo } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import {
  Container, Row, Col, Card, ListGroup, Button, Placeholder, Alert, Badge, OverlayTrigger, Tooltip,
} from 'react-bootstrap';

// ─────────────────────────────────────────────────────────────────────────────
// 서버 응답 (예시)
// { "sections": [ { "category": "question", "items": [ { "postId": 2, ... } ] }, ... ] }
// ─────────────────────────────────────────────────────────────────────────────

// 카테고리 메타: 라벨/경로/아이콘
const CATEGORY_META = {
  QUESTION: { label: '질문', path: '/posts/category/QUESTION', icon: '❓' },
  STUDY: { label: '스터디', path: '/posts/category/STUDY', icon: '📚' },
  NOTICE: { label: '공지', path: '/posts/category/NOTICE', icon: '📢' },
  SECOND_HAND_MARKET: { label: '중고장터', path: '/posts/category/SECOND_HAND_MARKET', icon: '💱' },
  MENTOR: { label: '멘토', path: '/posts/category/MENTOR', icon: '🧭' },
};

// 서버 카테고리 문자열 → 내부 Enum 키로 정규화
const CATEGORY_NORMALIZE = {
  question: 'QUESTION',
  study: 'STUDY',
  notice: 'NOTICE',
  second_hand_market: 'SECOND_HAND_MARKET',
  mentor: 'MENTOR',
};

// 표시 순서 고정
const CATEGORY_ORDER = ['QUESTION', 'STUDY', 'NOTICE', 'SECOND_HAND_MARKET', 'MENTOR'];

// 날짜 포맷터
const fmtDate = (iso) =>
  iso
    ? new Date(iso).toLocaleString('ko-KR', {
      year: '2-digit',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
    : '';

// 서버 payload → 화면에서 쓰기 좋은 형태로 1회 변환
function normalizeSections(serverSections = []) {
  const base = CATEGORY_ORDER.reduce((acc, k) => {
    acc[k] = [];
    return acc;
  }, {});
  for (const sec of serverSections) {
    const enumKey = CATEGORY_NORMALIZE[sec?.category] ?? null;
    if (!enumKey) continue;
    const mappedItems = (sec?.items ?? []).map((p) => ({
      id: p.postId,
      title: p.title,
      authorName: p.nickname,
      createdAt: p.createdAt,
      readCount: p.readCount ?? 0,
      commentCount: p.commentCount ?? 0,
      likeCount: p.likeCount ?? 0,
      userId: p.userId,
    }));
    base[enumKey] = mappedItems;
  }
  return base;
}

// 공통: 숫자 포맷
const fmtNumber = (n) =>
  typeof n === 'number' ? n.toLocaleString?.('ko-KR') ?? `${n}` : '0';

// 빈 상태(Empty State) 컴포넌트
function EmptyState({ title, description, ctaText, to }) {
  return (
    <div className="text-center text-muted py-4">
      <div className="mb-2" style={{ fontSize: 24 }}>🗒️</div>
      <div className="fw-semibold">{title}</div>
      <div className="small mt-1">{description}</div>
      {to && (
        <Button as={Link} to={to} variant="primary" size="sm" className="mt-3">
          {ctaText}
        </Button>
      )}
    </div>
  );
}

// 메타 뱃지 묶음 (좋아요/조회/댓글)
function MetaBadges({ likeCount, readCount, commentCount }) {
  return (
    <div className="d-flex align-items-center gap-2">
      <OverlayTrigger placement="top" overlay={<Tooltip>좋아요</Tooltip>}>
        <Badge bg="light" text="dark" className="border">{`👍 ${fmtNumber(likeCount)}`}</Badge>
      </OverlayTrigger>
      <OverlayTrigger placement="top" overlay={<Tooltip>조회수</Tooltip>}>
        <Badge bg="light" text="dark" className="border">{`👁️ ${fmtNumber(readCount)}`}</Badge>
      </OverlayTrigger>
      <OverlayTrigger placement="top" overlay={<Tooltip>댓글</Tooltip>}>
        <Badge bg="light" text="dark" className="border">{`💬 ${fmtNumber(commentCount)}`}</Badge>
      </OverlayTrigger>
    </div>
  );
}

// 개별 섹션
function CategorySection({ category, items, loading }) {
  const meta = CATEGORY_META[category];

  // 헤더 스타일(부드러운 그라데이션 + 얇은 구분선)
  const headerStyle = {
    background:
      'linear-gradient(180deg, rgba(250,250,252,1) 0%, rgba(245,246,248,1) 100%)',
  };

  return (
    <Card className="mb-4 shadow-sm h-100 border-0 rounded-3">
      <Card.Header
        style={headerStyle}
        className="d-flex align-items-center justify-content-between rounded-top-3"
      >
        <div className="d-flex align-items-center gap-2">
          <span aria-hidden="true" style={{ fontSize: 18 }}>{meta.icon}</span>
          <strong className="ms-1">{meta.label}</strong>
        </div>
        <Button
          as={Link}
          to={meta.path}
          variant="outline-secondary"
          size="sm"
          className="rounded-pill"
          aria-label={`${meta.label} 더보기`}
        >
          더보기
        </Button>
      </Card.Header>

      {loading ? (
        <ListGroup variant="flush">
          {Array.from({ length: 5 }).map((_, i) => (
            <ListGroup.Item key={i} className="py-3">
              <Placeholder as="div" animation="glow">
                <Placeholder xs={7} className="me-2" /> <Placeholder xs={2} /> <br />
                <Placeholder xs={3} className="mt-2" /> <Placeholder xs={2} /> <Placeholder xs={2} />
              </Placeholder>
            </ListGroup.Item>
          ))}
        </ListGroup>
      ) : (items?.length ?? 0) === 0 ? (
        // to를 수정해야 함. 글 작성 페이지는 이 URL이 아님.
        <EmptyState
          title="게시글이 없습니다."
          description="첫 글을 작성해보세요."
          ctaText="글쓰기"
          to={`/posts/new?category=${category}`}
        />
      ) : (
        <ListGroup variant="flush">
          {items.map((post) => (
            <ListGroup.Item
              key={post.id}
              className="py-3"
              style={{
                transition: 'background-color .15s ease, transform .06s ease',
              }}
              // hover/active 피드백
              onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'rgba(33,37,41,0.025)')}
              onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
              onMouseDown={(e) => (e.currentTarget.style.transform = 'scale(0.995)')}
              onMouseUp={(e) => (e.currentTarget.style.transform = 'scale(1)')}
            >
              {/* 제목만 링크가 되도록 title 영역만 Link로 감쌈 */}
              <div className="d-flex align-items-start justify-content-between gap-3">
                <div className="flex-grow-1">
                  <Link
                    to={`/posts/${post.id}`}
                    className="text-decoration-none"
                    style={{ color: 'inherit' }}
                    aria-label={`${post.title}로 이동`}
                  >
                    <div className="fw-semibold text-truncate">{post.title}</div>
                  </Link>
                  <div className="small text-muted mt-1">
                    {post.authorName ?? '익명'} · {fmtDate(post.createdAt)}
                  </div>
                </div>
                <div className="text-nowrap small">
                  <MetaBadges
                    likeCount={post.likeCount}
                    readCount={post.readCount}
                    commentCount={post.commentCount}
                  />
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
  const [sections, setSections] = useState(null); // { QUESTION: [], STUDY: [], ... }
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState(null);

  const ordered = useMemo(() => CATEGORY_ORDER, []);

  useEffect(() => {
    const ctrl = new AbortController();
    setLoading(true);
    setErr(null);

    axios
      .get('/api/posts/home', { params: { limit: 5 }, signal: ctrl.signal })
      .then((res) => {
        const serverSections = res.data?.sections ?? [];
        const normalized = normalizeSections(serverSections);
        setSections(normalized);
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

      {/* 상단 히어로/서브헤더 영역(선택) */}
      <Card className="border-0 rounded-4 mb-4 shadow-sm">
        <Card.Body className="d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <div className="fw-bold" style={{ fontSize: 20 }}>JuniorTown</div>
            <div className="text-muted small">최신 글을 한눈에 모아보세요.</div>
          </div>
          <div className="d-flex align-items-center gap-2">
            <Button as={Link} to="/posts/add" variant="primary" className="rounded-pill">
              글쓰기
            </Button>
            <Button as={Link} to="/posts/all" variant="outline-secondary" className="rounded-pill">
              전체 글 보기
            </Button>
          </div>
        </Card.Body>
      </Card>

      {/* 그리드 */}
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
