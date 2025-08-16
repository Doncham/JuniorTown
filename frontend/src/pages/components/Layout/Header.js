import React from 'react';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../auth/AuthContext';
export default function Header() {
  const navigate = useNavigate();
  const { user, isAuthenticated, ready, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Navbar bg="primary" variant="dark" expand="lg" className="shadow-sm">
      <Container>
        <Navbar.Brand as={Link} to="/" className="fw-bold">
          JuniorTown
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="main-nav" />
        <Navbar.Collapse id="main-nav">
          <Nav className="ms-auto align-items-center">

            {/* 로그인 안 된 경우 */}
            {!isAuthenticated && (
              <>
                <Nav.Link as={Link} to="/signup">회원가입</Nav.Link>
                <Nav.Link as={Link} to="/login">로그인</Nav.Link>
              </>
            )}

            {/* 로그인 된 경우 */}
            {isAuthenticated && (
              <>
                <span className="text-light me-2">
                  {user?.username || '회원'}님 환영합니다!
                </span>
                <Button variant="outline-light" className="ms-2" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            )}

            <Button variant="outline-light" className="ms-2">
              도움말
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}