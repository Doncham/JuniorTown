import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'react-bootstrap';
import axios from 'axios'; // axios를 import
import { useAuth } from '../../auth/AuthContext.jsx'; // 인증 컨텍스트 임포트

const PostCreatePage = () => {
  const navigate = useNavigate();
  const { token } = useAuth(); // 인증 토큰 가져오기

  // ① 제목과 내용을 담을 상태 변수 선언
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

  // ② 폼 제출(버튼 클릭) 핸들러
  const handleSubmit = async () => {
    const postData = { title, content };

    try {
      if (!token) {
        alert('로그인이 필요합니다.');
        navigate('/login', { replace: true });
        return;
      }
      const response = await axios.post('/api/posts', postData, {
        headers: {
          'Authorization': `${token}`,
        },
      });

      // const result = response.data;

      navigate('/posts', { replace: true });
    } catch (error) {
      console.error('게시물 작성 중 오류 발생:', error);
      alert('게시물 등록에 실패했습니다. 다시 시도해 주세요.');
    }
  };

  return (
    <div className="container mt-4">
      <h1>게시물을 작성해보자!</h1>

      {/* 제목 입력란 */}
      <div className="mt-2">
        <input
          type="text"
          placeholder="제목을 입력하세요"
          className="form-control"
          value={title}                                 // ③ value를 state와 바인딩
          onChange={(e) => setTitle(e.target.value)}   // ③ 입력 시 state 업데이트
        />
      </div>

      {/* 내용 입력란 */}
      <div className="mt-2">
        <textarea
          placeholder="내용을 입력하세요"
          rows="15"
          className="form-control"
          value={content}                                 // ③ value를 state와 바인딩
          onChange={(e) => setContent(e.target.value)}   // ③ 입력 시 state 업데이트
        />
      </div>

      {/* 게시물 작성 버튼 */}
      <div className="mt-3">
        <Button variant="primary" onClick={handleSubmit}>
          게시물 작성
        </Button>
      </div>
    </div>
  );
};

export default PostCreatePage;
