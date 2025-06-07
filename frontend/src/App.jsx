import { Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import Error from './pages/components/Error'
import PostCreatePage from './pages/posts/PostCreatePage'
import PostDetailsPage from './pages/posts/PostDetailPage'
import PostEditPage from './pages/posts/PostEditPage'
import PostListPage from './pages/posts/PostListPage'

const App = () => {
  return (

    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/posts/add" element={<PostCreatePage />} />
      <Route path="/posts/edit/:id" element={<PostEditPage />} />
      <Route path="/posts/:id" element={<PostDetailsPage />} />
      <Route path="/posts" element={<PostListPage />} />
      <Route path="*" element={<Error />} />
    </Routes>

  )
}
export default App