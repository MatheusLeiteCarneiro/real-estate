import { BrowserRouter as Router, Routes, Route } from "react-router"
import Footer from './components/Footer/Footer'
import Header from './components/Header/Header'
import { useSession } from './hooks/useSession'
import HomePage from './pages/HomePage'
import PropertyPage from "./pages/PropertyPage"
import NotFoundPage from "./pages/NotFoundPage"

function App() {
  const { user } = useSession()

  return (
    <Router>
      <Header user={user} />
        <Routes>
          <Route path="/" element={<HomePage/>}/>
          <Route path="/properties/:id" element={<PropertyPage/>}/>
          <Route path="*" element={<NotFoundPage/>}/>
        </Routes>
      <Footer />
    </Router>
  )
}

export default App
