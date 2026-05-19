import { useState } from 'react'
import axios from 'axios'
import './App.css'

const API_BASE = '/api/pdf'

function App() {
  const [mergeFiles, setMergeFiles] = useState([])
  const [splitFile, setSplitFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [splitPages, setSplitPages] = useState({})

  const authHeader = {
    headers: {
      'Authorization': 'Basic ' + btoa('admin:hemligt123')
    }
  }

  async function handleMerge() {
    if (mergeFiles.length < 2) {
      setError('Välj minst 2 PDF-filer')
      return
    }
    setLoading(true)
    setError('')
    try {
      const formData = new FormData()
      mergeFiles.forEach(f => formData.append('files', f))

      const response = await axios.post(
          `${API_BASE}/merge`,
          formData,
          { ...authHeader, responseType: 'blob' }
      )

      const url = URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.download = 'merged.pdf'
      link.click()
    } catch (err) {
      setError('Fel vid sammanslagning: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleSplit() {
    if (!splitFile) {
      setError('Välj en PDF-fil')
      return
    }
    setLoading(true)
    setError('')
    try {
      const formData = new FormData()
      formData.append('file', splitFile)

      const response = await axios.post(
          `${API_BASE}/split`,
          formData,
          authHeader
      )
      setSplitPages(response.data)
    } catch (err) {
      setError('Fel vid uppdelning: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  function downloadPage(pageNum, base64Data) {
    const bytes = atob(base64Data)
    const arr = new Uint8Array(bytes.length)
    for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i)
    const url = URL.createObjectURL(new Blob([arr], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `sida_${pageNum}.pdf`
    link.click()
  }

  return (
      <div className='container'>
        <h1>PDF-tjänsten</h1>

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
          {Object.entries(splitPages).map(([num, data]) => (
              <div key={num}>
                <span>Sida {num}</span>
                <button onClick={() => downloadPage(num, data)}>
                  Ladda ned
                </button>
              </div>
          ))}
        </section>

        {error && <p className='error'>{error}</p>}
      </div>
  )
}

export default App