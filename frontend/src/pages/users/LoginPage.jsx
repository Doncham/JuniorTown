import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../../auth/AuthContext.jsx'

export function LoginPage() {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: 백엔드 API 호출
    axios.post('/api/auth/login', credentials)
      .then(res => {
        const token = res.headers['authorization'];
        if (!token) {
          throw new Error('토큰이 없습니다.');
        }
        login(token);
        alert('로그인에 성공했습니다.');
        // 로그인 성공 후 홈 페이지로 리다이렉트
        navigate('/', { replace: true });
      })
      .catch(error => {
        console.error('로그인 실패:', error);
        alert('로그인에 실패했습니다. 이메일과 비밀번호를 확인해 주세요.');
      });
    console.log('로그인 데이터:', credentials);
  };

  return (
    <div className="container mt-5" style={{ maxWidth: '400px' }}>
      <h2 className="mb-4">로그인</h2>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="loginEmail" className="form-label">이메일</label>
          <input
            type="email"
            className="form-control"
            id="loginEmail"
            name="email"
            value={credentials.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="mb-4">
          <label htmlFor="loginPassword" className="form-label">비밀번호</label>
          <input
            type="password"
            className="form-control"
            id="loginPassword"
            name="password"
            value={credentials.password}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit" className="btn btn-success w-100">로그인</button>
      </form>
    </div>
  );
}