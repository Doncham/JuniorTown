import React from 'react';

/**
 * Renders the site footer with copyright information and links to the privacy policy and terms of service.
 *
 * The current year is displayed dynamically.
 */
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