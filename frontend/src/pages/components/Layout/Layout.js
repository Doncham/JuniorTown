import Footer from "./Footer";
import Header from "./Header";
/**
 * Provides a page layout with a header, footer, and a flexible main content area.
 *
 * Wraps the given {@link children} between a persistent header and footer, ensuring the main content expands to fill the available vertical space.
 *
 * @param {object} props
 * @param {React.ReactNode} props.children - Content to display between the header and footer.
 * @returns {JSX.Element} The composed layout structure.
 */
export default function Layout({ children }) {
  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="flex-grow-1">
        {children}
      </main>
      <Footer />
    </div>
  );
}