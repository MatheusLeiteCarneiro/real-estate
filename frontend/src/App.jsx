import Footer from './components/Footer/Footer'
import Header from './components/Header/Header'
import { useSession } from './hooks/useSession'
import HomePage from './pages/HomePage'

function App() {
  const { user } = useSession()

  return (
    <>
      <Header user={user} />
      <HomePage />
      <Footer />
    </>
  )
}

export default App
