import axios from 'axios';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Renders a user registration form and handles sign-up logic.
 *
 * Displays input fields for username, email, and password, manages form state, and submits the data to the server. On successful registration, notifies the user and redirects to the login page.
 *
 * @returns {JSX.Element} The sign-up page component.
 */
export function SignUpPage() {
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const navigate = useNavigate();
  
  // 실시간 유효성 검사를 위해 handleChange 함수를 사용합니다.
  // 지금 하지는 않을듯
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post('/api/auth/signup', formData)
      .then(response => {
        console.log('회원가입 성공:', response.data);
        alert('회원가입이 완료되었습니다.');
        // 회원가입 성공 후 로그인 페이지로 리다이렉트
        navigate('/login', { replace: true });
      })
      .catch(error => {
        console.error('회원가입 실패:', error);
        alert('회원가입에 실패했습니다. 다시 시도해 주세요.');
      });
  };

  return (
    <div className="container mt-5" style={{ maxWidth: '400px' }}>
      <h2 className="mb-4">회원가입</h2>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="username" className="form-label">이름</label>
          <input
            type="text"
            className="form-control"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            required
          />
        </div>
        <div className="mb-3">
          <label htmlFor="email" className="form-label">이메일</label>
          <input
            type="email"
            className="form-control"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="mb-4">
          <label htmlFor="password" className="form-label">비밀번호</label>
          <input
            type="password"
            className="form-control"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary w-100">가입하기</button>
      </form>
    </div>
  );
}