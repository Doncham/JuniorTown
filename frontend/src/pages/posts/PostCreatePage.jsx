// src/pages/PostCreatePage.jsx
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Button, Nav, Card, Badge } from 'react-bootstrap';
import axios from 'axios';
import { useAuth } from '../../auth/AuthContext.jsx';

// 1) 카테고리 메타 & 순서
const CATEGORY_META = {
  QUESTION: { label: '질문', icon: '❓' },
  STUDY: { label: '스터디', icon: '📚' },
  NOTICE: { label: '공지', icon: '📢' },
  SECOND_HAND_MARKET: { label: '중고장터', icon: '💱' },
  MENTOR: { label: '멘토', icon: '🧭' },
};
const CATEGORY_ORDER = ['QUESTION', 'STUDY', 'NOTICE', 'SECOND_HAND_MARKET', 'MENTOR'];

// 2) 쿼리스트링(category) → 내부 Enum 키로 정규화
function normalizeCategoryFromQuery(raw) {
  if (!raw) return null;
  const upper = String(raw).toUpperCase();
  if (CATEGORY_META[upper]) return upper;
  const lowerMap = {
    question: 'QUESTION',
    study: 'STUDY',
    notice: 'NOTICE',
    second_hand_market: 'SECOND_HAND_MARKET',
    mentor: 'MENTOR',
  };
  return lowerMap[String(raw).toLowerCase()] ?? null;
}

const PostCreatePage = () => {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [searchParams] = useSearchParams();

  // 3) 폼 상태
  const [category, setCategory] = useState('QUESTION'); // 기본값
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // 4) URL 쿼리(category)로 초기 선택 반영 (예: /posts/new?category=STUDY)
  useEffect(() => {
    const fromQuery = normalizeCategoryFromQuery(searchParams.get('category'));
    if (fromQuery) setCategory(fromQuery);
  }, [searchParams]);

  const canSubmit = useMemo(() => {
    return Boolean(title.trim()) && Boolean(content.trim()) && Boolean(category) && !submitting;
  }, [title, content, category, submitting]);

  // 5) 제출 핸들러
  const handleSubmit = async () => {
    if (!token) {
      alert('로그인이 필요합니다.');
      navigate('/login', { replace: true });
      return;
    }
    if (!canSubmit) return;

    const postData = { title: title.trim(), content: content.trim(), category };

    try {
      setSubmitting(true);

      const response = await axios.post('/api/posts', postData);

      // 생성 후: 해당 카테고리 목록으로 이동
      // 서버가 생성된 게시글 id를 반환한다면 다음처럼 상세로 보내도 됨:
      // const createdId = response?.data?.id;
      // if (createdId) return navigate(`/posts/${createdId}`, { replace: true });

      navigate(`/`, { replace: true });
    } catch (error) {
      console.error('게시물 작성 중 오류 발생:', error);
      alert('게시물 등록에 실패했습니다. 다시 시도해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container mt-4">
      <Card className="border-0 shadow-sm rounded-4">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
            <h1 className="h4 m-0">게시물을 작성해보자!</h1>
            <Button as={Link} to="/posts" variant="outline-secondary" size="sm" className="rounded-pill">
              목록으로
            </Button>
          </div>

          {/* 6) 카테고리 선택 탭 */}
          <div className="mb-3">
            <Nav
              variant="pills"
              activeKey={category}
              onSelect={(k) => k && setCategory(k)}
              className="gap-2 flex-wrap"
            >
              {CATEGORY_ORDER.map((key) => (
                <Nav.Item key={key}>
                  <Nav.Link
                    eventKey={key}
                    className="rounded-pill px-3 py-1"
                    aria-label={`${CATEGORY_META[key].label} 카테고리 선택`}
                  >
                    <span className="me-1" aria-hidden="true">{CATEGORY_META[key].icon}</span>
                    {CATEGORY_META[key].label}
                  </Nav.Link>
                </Nav.Item>
              ))}
            </Nav>
            <div className="mt-2 small text-muted">
              선택된 카테고리:{' '}
              <Badge bg="light" text="dark" className="border">
                {CATEGORY_META[category].label}
              </Badge>
            </div>
          </div>

          {/* 제목 입력 */}
          <div className="mt-3">
            <input
              type="text"
              placeholder="제목을 입력하세요"
              className="form-control"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              maxLength={120}
            />
            <div className="form-text">최대 120자</div>
          </div>

          {/* 내용 입력 */}
          <div className="mt-3">
            <textarea
              placeholder="내용을 입력하세요"
              rows={15}
              className="form-control"
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
          </div>

          {/* 제출 */}
          <div className="mt-3 d-flex gap-2">
            <Button
              variant="primary"
              onClick={handleSubmit}
              disabled={!canSubmit}
              className="rounded-pill px-4"
            >
              {submitting ? '작성 중…' : '게시물 작성'}
            </Button>
            <Button
              variant="outline-secondary"
              onClick={() => navigate(-1)}
              className="rounded-pill"
            >
              취소
            </Button>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default PostCreatePage;
