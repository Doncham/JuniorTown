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
      <Route path="/post/add" element={<PostCreatePage />} />
      <Route path="/post/edit/:id" element={<PostEditPage />} />
      <Route path="/post/:id" element={<PostDetailsPage />} />
      <Route path="/posts" element={<PostListPage />} />
      <Route path="*" element={<Error />} />
    </Routes>

  )
}
export default App