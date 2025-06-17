import React from 'react';

export default function Footer() {
  return (
    <footer className="bg-dark text-white text-center py-4 mt-auto">
      <div className="container">
        <p className="mb-1">
          &copy; {new Date().getFullYear()} JuniorTown. All rights reserved.
        </p>
        <small>
          <a href="/privacy" className="text-white text-decoration-none me-3">
            개인정보 처리방침
          </a>
          <a href="/terms" className="text-white text-decoration-none">
            이용약관
          </a>
        </small>
      </div>
    </footer>
  );
}