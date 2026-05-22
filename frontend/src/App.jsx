import { useState } from 'react'
import axios from 'axios'
import './App.css'

const API_BASE = '/api/pdf'
const AUTH_BASE = '/api/auth'

function App() {
  const [token, setToken] = useState(localStorage.getItem('jwt') || '')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [mergeFiles, setMergeFiles] = useState([])
  const [splitFile, setSplitFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [splitPages, setSplitPages] = useState({})

  const authHeader = {
    headers: { 'Authorization': `Bearer ${token}` }
  }

  async function handleLogin() {
    try {
      const response = await axios.post(`${AUTH_BASE}/login`, { username, password })
      const jwt = response.data.token
      setToken(jwt)
      localStorage.setItem('jwt', jwt)
      setError('')
    } catch {
      setError('Felaktigt användarnamn eller lösenord')
    }
  }

  async function handleRegister() {
    try {
      const response = await axios.post(`${AUTH_BASE}/register`, { username, password })
      const jwt = response.data.token
      setToken(jwt)
      localStorage.setItem('jwt', jwt)
      setError('')
    } catch {
      setError('Registrering misslyckades')
    }
  }

  function handleLogout() {
    setToken('')
    localStorage.removeItem('jwt')
  }

  async function handleMerge() {
    if (mergeFiles.length < 2) { setError('Välj minst 2 PDF-filer'); return }
    setLoading(true); setError('')
    try {
      const formData = new FormData()
      mergeFiles.forEach(f => formData.append('files', f))
      const response = await axios.post(`${API_BASE}/merge`, formData, authHeader)
      window.open(response.data.downloadUrl, '_blank')
    } catch { setError('Fel vid sammanslagning') }
    finally { setLoading(false) }
  }

  async function handleSplit() {
    if (!splitFile) { setError('Välj en PDF-fil'); return }
    setLoading(true); setError('')
    try {
      const formData = new FormData()
      formData.append('file', splitFile)
      const response = await axios.post(`${API_BASE}/split`, formData, authHeader)
      setSplitPages(response.data)
    } catch { setError('Fel vid uppdelning') }
    finally { setLoading(false) }
  }

  if (!token) {
    return (
        <div className='container'>
          <h1>PDF-tjänsten</h1>
          <section className='card'>
            <h2>Logga in</h2>
            <input placeholder='Användarnamn' value={username}
                   onChange={e => setUsername(e.target.value)} />
            <input placeholder='Lösenord' type='password' value={password}
                   onChange={e => setPassword(e.target.value)} />
            <button onClick={handleLogin}>Logga in</button>
            <button onClick={handleRegister}>Registrera</button>
          </section>
          {error && <p className='error'>{error}</p>}
        </div>
    )
  }

  return (
      <div className='container'>
        <h1>PDF-tjänsten</h1>
        <button onClick={handleLogout}>Logga ut</button>

        <section className='card'>
          <h2>Slå ihop PDF:er</h2>
          <input type='file' accept='.pdf' multiple
                 onChange={e => setMergeFiles([...e.target.files])} />
          <p>{mergeFiles.length} filer valda</p>
          <button onClick={handleMerge} disabled={loading}>
            {loading ? 'Bearbetar...' : 'Slå ihop'}
          </button>
        </section>

        <section className='card'>
          <h2>Dela upp PDF</h2>
          <input type='file' accept='.pdf'
                 onChange={e => setSplitFile(e.target.files[0])} />
          <button onClick={handleSplit} disabled={loading}>
            {loading ? 'Bearbetar...' : 'Dela upp'}
          </button>
          {Object.entries(splitPages).map(([num, url]) => (
              <div key={num}>
                <span>Sida {num} </span>
                <a href={url} target='_blank' rel='noreferrer'>Ladda ned</a>
              </div>
          ))}
        </section>

        {error && <p className='error'>{error}</p>}
      </div>
  )
}

export default App