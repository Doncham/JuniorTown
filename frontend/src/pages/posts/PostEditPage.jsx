// src/pages/PostEditPage.jsx

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Form, Button, Spinner, Alert } from 'react-bootstrap';
import base64 from 'base-64';

const PostEditPage = () => {
  // 1. URL에서 id 파라미터를 추출
  const { id } = useParams();
  const navigate = useNavigate();

  // 2. 컴포넌트 상태 정의
  const [title, setTitle] = useState('');       // 수정할 게시물의 제목
  const [content, setContent] = useState('');   // 수정할 게시물의 내용
  const [loading, setLoading] = useState(true); // 기존 게시물을 불러오는 중인지 여부
  const [error, setError] = useState(null);     // 에러 메시지 저장용
  const [saving, setSaving] = useState(false);  // 저장(수정) 요청 중인지 여부

  // 3. 마운트 시(또는 id가 바뀔 때) 기존 게시물 조회
  useEffect(() => {
    const fetchPost = async () => {
      try {
        const token = localStorage.getItem('jwt');
      
        const response = await axios.get(`/api/posts/details/${id}`, {
          headers: {
            'Authorization': `${token}`,
          },
        });
        // 성공적으로 데이터를 받아오면 title과 content를 초기화
        setTitle(response.data.title);
        setContent(response.data.content);
      } catch (err) {
        console.error('게시물 로드 중 오류 발생:', err);
        setError('게시물을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [id]);

  // 4. 폼 제출 시(수정 요청) 처리 함수
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    const token = localStorage.getItem('jwt');

    if (!token) {
      alert('로그인이 필요합니다.');
      navigate('/login', { replace: true });
      return;
    }
    try {
      // PUT /api/posts/{id}로 수정 요청
  
      await axios.patch(`/api/posts/${id}`, {
        title,
        content,
      }, {
        headers: {
          'Authorization': `${token}`,
        },
      });
      // 수정이 완료되면 상세 페이지로 이동
      navigate(`/posts/${id}`, { replace: true });
    } catch (err) {
      console.error('게시물 수정 중 오류 발생:', err);
      setError('게시물을 수정하는 데 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  // 5. 취소 버튼 클릭 시 이전 화면으로 이동
  const handleCancel = () => {
    navigate(-1);
  };

  return (
    <Container className="mt-4">
      <h4 className="mb-3">게시물 수정</h4>

      {/* 로딩 중일 때 스피너 표시 */}
      {loading && (
        <div className="d-flex justify-content-center my-5">
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Loading...</span>
          </Spinner>
        </div>
      )}

      {/* 에러 발생 시 경고창 표시 */}
      {error && (
        <Alert variant="danger" className="my-3">
          {error}
        </Alert>
      )}

      {/* 로딩 완료 후, 에러가 없으면 수정 폼 표시 */}
      {!loading && !error && (
        <Form onSubmit={handleSubmit}>
          {/* 제목 입력 필드 */}
          <Form.Group className="mb-3" controlId="postTitle">
            <Form.Label>제목</Form.Label>
            <Form.Control
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목을 입력하세요"
            />
          </Form.Group>

          {/* 내용 입력 필드 */}
          <Form.Group className="mb-3" controlId="postContent">
            <Form.Label>내용</Form.Label>
            <Form.Control
              as="textarea"
              rows={10}
              style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="내용을 입력하세요"
            />
          </Form.Group>

          {/* 버튼 영역: 취소, 저장하기 */}
          <div className="d-flex justify-content-end">
            <Button
              variant="secondary"
              onClick={handleCancel}
              className="me-2"
              disabled={saving}
            >
              취소
            </Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? '저장 중...' : '저장하기'}
            </Button>
          </div>
        </Form>
      )}
    </Container>
  );
};

export default PostEditPage;
