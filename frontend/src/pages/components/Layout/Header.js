import React from 'react';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';

export default function Header() {
  return (
    <Navbar bg="primary" variant="dark" expand="lg" className="shadow-sm">
      <Container>
        <Navbar.Brand href="/" className="fw-bold">
          JuniorTown
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="main-nav" />
        <Navbar.Collapse id="main-nav">
          <Nav className="ms-auto">
            <Nav.Link href="/signup">회원가입</Nav.Link>
            <Nav.Link href="/login">로그인</Nav.Link>
            <Button variant="outline-light" className="ms-2">
              도움말
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}