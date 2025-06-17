import React from 'react';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';

/**
 * Renders the main navigation bar for the application with brand, signup, login, and help options.
 *
 * The navigation bar is responsive, styled with a primary background and dark variant, and includes links to the home, signup, and login pages, as well as a help button.
 *
 * @returns {JSX.Element} The rendered header navigation bar component.
 */
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