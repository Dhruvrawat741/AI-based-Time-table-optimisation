import React,{ useState } from 'react'

const DAYS_OPTIONS = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday']

const TIME_SLOTS = [
  '8:00–8:55','9:00–9:55','9:55–10:50','BREAK','11:10–12:05','LUNCH','1:00–1:55','1:55–2:50','3:10–4:05'
]

const COLORS = [
  '#4f46e5','#0891b2','#059669','#d97706','#dc2626','#7c3aed','#db2777','#0284c7'
]

function App() {
  // ── state ──────────────────────────────────────────────
  const [step, setStep] = useState('setup') // setup | result
  const [numSections, setNumSections] = useState(2)
  const [days, setDays] = useState(['Monday','Tuesday','Wednesday','Thursday','Friday'])
  const [rooms, setRooms] = useState([
    { roomNumber:'CR-1', capacity:60, isLab:false },
    { roomNumber:'CR-2', capacity:60, isLab:false },
    { roomNumber:'LAB-1', capacity:40, isLab:true },
  ])
  const [subjects, setSubjects] = useState([
    { code:'TCS-401', name:'Theory of Computation', hoursPerWeek:3, isLab:false, teachers:'Dr. Sharma, Dr. Verma' },
    { code:'TCS-402', name:'Operating Systems',     hoursPerWeek:3, isLab:false, teachers:'Dr. Gupta, Dr. Mehta'  },
    { code:'TCS-403', name:'Computer Networks',     hoursPerWeek:3, isLab:false, teachers:'Dr. Singh, Dr. Rao'   },
    { code:'TCS-409', name:'OS Lab',                hoursPerWeek:2, isLab:true,  teachers:'Dr. Gupta, Dr. Mehta' },
  ])
  const [timetable, setTimetable]   = useState(null)
  const [activeSection, setActiveSection] = useState(0)
  const [loading, setLoading]       = useState(false)
  const [error, setError]           = useState('')

  // ── helpers ────────────────────────────────────────────
  const toggleDay = d => setDays(prev => prev.includes(d) ? prev.filter(x=>x!==d) : [...prev,d])

  const addRoom = () => setRooms(r=>[...r,{roomNumber:'',capacity:60,isLab:false}])
  const updateRoom = (i,k,v) => setRooms(r=>r.map((x,j)=>j===i?{...x,[k]:v}:x))
  const removeRoom = i => setRooms(r=>r.filter((_,j)=>j!==i))

  const addSubject = () => setSubjects(s=>[...s,{code:'',name:'',hoursPerWeek:3,isLab:false,teachers:''}])
  const updateSubject = (i,k,v) => setSubjects(s=>s.map((x,j)=>j===i?{...x,[k]:v}:x))
  const removeSubject = i => setSubjects(s=>s.filter((_,j)=>j!==i))

  const teacherWarning = subjects.some(s => {
    const t = s.teachers.split(',').map(x=>x.trim()).filter(Boolean)
    return t.length > 0 && t.length < numSections
  })

  // ── generate ───────────────────────────────────────────
  const generate = async () => {
    setLoading(true); setError('')
    const payload = {
      numSections,
      days,
      subjects: subjects.map(s=>({
        code: s.code, name: s.name, isLab: s.isLab,
        hoursPerWeek: Number(s.hoursPerWeek),
        teachers: s.teachers.split(',').map(x=>x.trim()).filter(Boolean)
      })),
      rooms: rooms.map(r=>({
        roomNumber: r.roomNumber, isLab: r.isLab, capacity: Number(r.capacity)
      }))
    }
    try {
      const res  = await fetch('http://localhost:8080/api/generate', {
        method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(payload)
      })
      const data = await res.json()
      if (!res.ok || !data.success) throw new Error(data.message || 'Generation failed')
      setTimetable(data)
      setActiveSection(0)
      setStep('result')
    } catch(e) {
      setError(e.message || 'Could not reach backend. Is Spring Boot running on port 8080?')
    } finally { setLoading(false) }
  }

  // ── cell colour by subject code ────────────────────────
  const subjectColor = (code) => {
    if (!code) return '#f3f4f6'
    const idx = subjects.findIndex(s=>s.code===code)
    return idx>=0 ? COLORS[idx % COLORS.length] : '#6b7280'
  }

  // ── render ─────────────────────────────────────────────
  return (
    <div style={styles.root}>
      {/* ── HEADER ── */}
      <header style={styles.header}>
        <div style={styles.headerInner}>
          <div>
            <div style={styles.logo}>⏱ SmartTable</div>
            <div style={styles.tagline}>AI-Based Timetable Optimizer</div>
          </div>
          {step==='result' && (
            <button style={styles.btnSecondary} onClick={()=>setStep('setup')}>
              ← Back to Setup
            </button>
          )}
        </div>
      </header>

      <main style={styles.main}>
        {step==='setup' ? (
          <SetupPanel
            numSections={numSections} setNumSections={setNumSections}
            days={days} toggleDay={toggleDay}
            rooms={rooms} addRoom={addRoom} updateRoom={updateRoom} removeRoom={removeRoom}
            subjects={subjects} addSubject={addSubject} updateSubject={updateSubject} removeSubject={removeSubject}
            teacherWarning={teacherWarning}
            generate={generate} loading={loading} error={error}
          />
        ) : (
          <ResultPanel
            timetable={timetable} days={days}
            activeSection={activeSection} setActiveSection={setActiveSection}
            subjectColor={subjectColor} subjects={subjects}
          />
        )}
      </main>
    </div>
  )
}

// ══════════════════════════════════════════════════════════
//  SETUP PANEL
// ══════════════════════════════════════════════════════════
function SetupPanel({ numSections,setNumSections,days,toggleDay,rooms,addRoom,updateRoom,removeRoom,
                      subjects,addSubject,updateSubject,removeSubject,teacherWarning,generate,loading,error }) {
  return (
    <div style={styles.grid2}>
      {/* LEFT COLUMN */}
      <div style={{display:'flex',flexDirection:'column',gap:24}}>

        {/* Sections */}
        <Card title="📚 Sections & Days">
          <label style={styles.label}>Number of Sections</label>
          <div style={{display:'flex',alignItems:'center',gap:12,marginBottom:20}}>
            <button style={styles.btnRound} onClick={()=>setNumSections(n=>Math.max(1,n-1))}>−</button>
            <span style={{fontSize:28,fontWeight:700,color:'#1e293b',minWidth:40,textAlign:'center'}}>{numSections}</span>
            <button style={styles.btnRound} onClick={()=>setNumSections(n=>Math.min(26,n+1))}>+</button>
            <span style={{color:'#64748b',fontSize:13}}>
              → Sections: {Array.from({length:numSections},(_,i)=>String.fromCharCode(65+i)).join(', ')}
            </span>
          </div>

          <label style={styles.label}>Active Days</label>
          <div style={{display:'flex',flexWrap:'wrap',gap:8}}>
            {['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'].map(d=>(
              <button key={d}
                style={days.includes(d) ? styles.dayBtnOn : styles.dayBtnOff}
                onClick={()=>toggleDay(d)}>{d.slice(0,3)}</button>
            ))}
          </div>
        </Card>

        {/* Rooms */}
        <Card title="🏫 Rooms">
          {rooms.map((r,i)=>(
            <div key={i} style={styles.row}>
              <input style={{...styles.input,flex:1}} placeholder="Room No." value={r.roomNumber}
                onChange={e=>updateRoom(i,'roomNumber',e.target.value)}/>
              <input style={{...styles.input,width:70}} type="number" placeholder="Cap" value={r.capacity}
                onChange={e=>updateRoom(i,'capacity',e.target.value)}/>
              <label style={styles.checkLabel}>
                <input type="checkbox" checked={r.isLab} onChange={e=>updateRoom(i,'isLab',e.target.checked)}/>
                Lab
              </label>
              <button style={styles.btnDelete} onClick={()=>removeRoom(i)}>✕</button>
            </div>
          ))}
          <button style={styles.btnAdd} onClick={addRoom}>+ Add Room</button>
        </Card>
      </div>

      {/* RIGHT COLUMN */}
      <div style={{display:'flex',flexDirection:'column',gap:24}}>

        {/* Subjects */}
        <Card title="📖 Subjects & Teachers">
          {teacherWarning && (
            <div style={styles.warning}>
              ⚠ Some subjects have fewer teachers than sections — teachers will be shared (round-robin).
            </div>
          )}
          {subjects.map((s,i)=>(
            <div key={i} style={{...styles.subjectCard, borderLeft:`4px solid ${COLORS[i%COLORS.length]}`}}>
              <div style={styles.row}>
                <input style={{...styles.input,width:100}} placeholder="Code" value={s.code}
                  onChange={e=>updateSubject(i,'code',e.target.value)}/>
                <input style={{...styles.input,flex:1}} placeholder="Subject Name" value={s.name}
                  onChange={e=>updateSubject(i,'name',e.target.value)}/>
                <button style={styles.btnDelete} onClick={()=>removeSubject(i)}>✕</button>
              </div>
              <div style={styles.row}>
                <label style={styles.label2}>Sessions/week:</label>
                <input style={{...styles.input,width:60}} type="number" min={1} max={7} value={s.hoursPerWeek}
                  onChange={e=>updateSubject(i,'hoursPerWeek',e.target.value)}/>
                <label style={styles.checkLabel}>
                  <input type="checkbox" checked={s.isLab} onChange={e=>updateSubject(i,'isLab',e.target.checked)}/>
                  Lab
                </label>
              </div>
              <input style={{...styles.input,width:'100%'}} placeholder="Teachers (comma-separated): Dr. Sharma, Dr. Verma"
                value={s.teachers} onChange={e=>updateSubject(i,'teachers',e.target.value)}/>
            </div>
          ))}
          <button style={styles.btnAdd} onClick={addSubject}>+ Add Subject</button>
        </Card>

        {/* Generate */}
        {error && <div style={styles.errorBox}>{error}</div>}
        <button style={styles.btnGenerate} onClick={generate} disabled={loading}>
          {loading ? '⚙ Generating...' : '🚀 Generate Timetable'}
        </button>
      </div>
    </div>
  )
}

// ══════════════════════════════════════════════════════════
//  RESULT PANEL
// ══════════════════════════════════════════════════════════
function ResultPanel({ timetable, days, activeSection, setActiveSection, subjectColor, subjects }) {
  if (!timetable?.sections) return null
  const sec = timetable.sections[activeSection]
  if (!sec) return null

  // count sessions placed per subject
  const counts = {}
  subjects.forEach(s=>{ counts[s.code]=0 })
  days.forEach(day=>{
    const cells = sec.schedule?.[day] || []
    cells.forEach(c=>{ if(c?.subjectCode) counts[c.subjectCode]=(counts[c.subjectCode]||0)+1 })
  })

  return (
    <div>
      {/* Section tabs */}
      <div style={styles.tabs}>
        {timetable.sections.map((s,i)=>(
          <button key={i} style={i===activeSection ? styles.tabOn : styles.tabOff}
            onClick={()=>setActiveSection(i)}>{s.sectionName}</button>
        ))}
      </div>

      {/* Grid */}
      <div style={{overflowX:'auto',borderRadius:12,boxShadow:'0 4px 24px rgba(0,0,0,0.08)'}}>
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={{...styles.th,width:80}}>Day</th>
              {TIME_SLOTS.map((t,i)=>(
                <th key={i} style={{
                  ...styles.th,
                  background: t==='LUNCH' ? '#fef3c7' : t==='BREAK' ? '#f0fdf4' : '#f8fafc',
                  color: t==='LUNCH'||t==='BREAK' ? '#92400e' : '#475569',
                  minWidth: t==='LUNCH'||t==='BREAK' ? 70 : 110,
                  fontSize: 11
                }}>{t}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {days.map((day,di)=>{
              const cells = sec.schedule?.[day] || []
              let cellIdx = 0
              return (
                <tr key={di}>
                  <td style={styles.dayCell}>{day.slice(0,3)}</td>
                  {TIME_SLOTS.map((slot,si)=>{
                    if (slot==='LUNCH') return <td key={si} style={styles.lunchCell}>🍽 Lunch</td>
                    if (slot==='BREAK') return <td key={si} style={styles.breakCell}>☕ Break</td>
                    const cell = cells[cellIdx++]
                    if (!cell || !cell.subjectCode) return <td key={si} style={styles.freeCell}>—</td>
                    return (
                      <td key={si} style={{...styles.classCell, background: subjectColor(cell.subjectCode)+'18',
                        borderTop:`3px solid ${subjectColor(cell.subjectCode)}`}}>
                        <div style={{fontWeight:700,color:subjectColor(cell.subjectCode),fontSize:12}}>{cell.subjectCode}</div>
                        <div style={{fontSize:11,color:'#334155',marginTop:1}}>{cell.teacherName}</div>
                        <div style={{fontSize:10,color:'#64748b',marginTop:1}}>📍 {cell.roomNumber}</div>
                      </td>
                    )
                  })}
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      {/* Summary */}
      <div style={{marginTop:24}}>
        <div style={{fontWeight:700,fontSize:15,color:'#1e293b',marginBottom:12}}>Session Summary — {sec.sectionName}</div>
        <div style={{display:'flex',flexWrap:'wrap',gap:10}}>
          {subjects.map((s,i)=>(
            <div key={i} style={{...styles.badge, borderColor: COLORS[i%COLORS.length], color: COLORS[i%COLORS.length]}}>
              <span style={{fontWeight:700}}>{s.code}</span>
              <span style={{margin:'0 6px',color:'#94a3b8'}}>·</span>
              {counts[s.code] || 0}/{s.hoursPerWeek} sessions
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

// ══════════════════════════════════════════════════════════
//  CARD wrapper
// ══════════════════════════════════════════════════════════
function Card({ title, children }) {
  return (
    <div style={styles.card}>
      <div style={styles.cardTitle}>{title}</div>
      {children}
    </div>
  )
}

// ══════════════════════════════════════════════════════════
//  STYLES
// ══════════════════════════════════════════════════════════
const styles = {
  root:{ minHeight:'100vh', background:'#f1f5f9', fontFamily:"'Segoe UI',system-ui,sans-serif" },
  header:{ background:'linear-gradient(135deg,#1e293b 0%,#334155 100%)', color:'#fff', padding:'0 32px' },
  headerInner:{ maxWidth:1200,margin:'0 auto',display:'flex',alignItems:'center',justifyContent:'space-between',height:64 },
  logo:{ fontSize:22,fontWeight:800,letterSpacing:'-0.5px' },
  tagline:{ fontSize:12,color:'#94a3b8',marginTop:2 },
  main:{ maxWidth:1200,margin:'32px auto',padding:'0 24px' },

  grid2:{ display:'grid',gridTemplateColumns:'1fr 1fr',gap:24 },

  card:{ background:'#fff',borderRadius:14,padding:24,boxShadow:'0 2px 12px rgba(0,0,0,0.06)' },
  cardTitle:{ fontWeight:700,fontSize:15,color:'#1e293b',marginBottom:18,paddingBottom:12,borderBottom:'1px solid #e2e8f0' },

  label:{ display:'block',fontSize:12,fontWeight:600,color:'#64748b',marginBottom:8,textTransform:'uppercase',letterSpacing:'0.05em' },
  label2:{ fontSize:12,color:'#64748b',whiteSpace:'nowrap' },
  checkLabel:{ display:'flex',alignItems:'center',gap:6,fontSize:13,color:'#475569',cursor:'pointer',whiteSpace:'nowrap' },

  input:{ border:'1.5px solid #e2e8f0',borderRadius:8,padding:'8px 12px',fontSize:13,outline:'none',background:'#f8fafc',
    color:'#1e293b',transition:'border 0.2s' },

  row:{ display:'flex',alignItems:'center',gap:10,marginBottom:10,flexWrap:'wrap' },

  dayBtnOn:{ padding:'6px 14px',borderRadius:20,border:'none',background:'#4f46e5',color:'#fff',
    fontWeight:600,fontSize:13,cursor:'pointer' },
  dayBtnOff:{ padding:'6px 14px',borderRadius:20,border:'1.5px solid #cbd5e1',background:'#fff',
    color:'#64748b',fontWeight:500,fontSize:13,cursor:'pointer' },

  btnRound:{ width:36,height:36,borderRadius:'50%',border:'2px solid #4f46e5',background:'#fff',
    color:'#4f46e5',fontSize:20,cursor:'pointer',display:'flex',alignItems:'center',justifyContent:'center' },

  btnAdd:{ marginTop:8,padding:'8px 16px',borderRadius:8,border:'1.5px dashed #cbd5e1',background:'transparent',
    color:'#4f46e5',fontWeight:600,fontSize:13,cursor:'pointer',width:'100%' },
  btnDelete:{ padding:'6px 10px',borderRadius:6,border:'none',background:'#fee2e2',color:'#dc2626',
    cursor:'pointer',fontWeight:600,fontSize:12 },
  btnSecondary:{ padding:'8px 18px',borderRadius:8,border:'1.5px solid rgba(255,255,255,0.3)',background:'rgba(255,255,255,0.1)',
    color:'#fff',cursor:'pointer',fontSize:13,fontWeight:600 },
  btnGenerate:{ padding:'16px',borderRadius:12,border:'none',
    background:'linear-gradient(135deg,#4f46e5,#7c3aed)',color:'#fff',
    fontSize:16,fontWeight:700,cursor:'pointer',width:'100%',
    boxShadow:'0 4px 16px rgba(79,70,229,0.4)',letterSpacing:'0.02em' },

  subjectCard:{ marginBottom:14,padding:14,borderRadius:10,background:'#f8fafc',border:'1px solid #e2e8f0' },

  warning:{ background:'#fef9c3',border:'1px solid #fde047',borderRadius:8,padding:'10px 14px',
    fontSize:13,color:'#713f12',marginBottom:14 },
  errorBox:{ background:'#fef2f2',border:'1px solid #fca5a5',borderRadius:10,padding:'12px 16px',
    fontSize:13,color:'#991b1b' },

  tabs:{ display:'flex',gap:8,marginBottom:20,flexWrap:'wrap' },
  tabOn:{ padding:'8px 20px',borderRadius:20,border:'none',background:'#4f46e5',color:'#fff',fontWeight:700,fontSize:13,cursor:'pointer' },
  tabOff:{ padding:'8px 20px',borderRadius:20,border:'1.5px solid #e2e8f0',background:'#fff',color:'#64748b',fontWeight:500,fontSize:13,cursor:'pointer' },

  table:{ width:'100%',borderCollapse:'collapse',background:'#fff',minWidth:900 },
  th:{ padding:'10px 8px',fontWeight:600,fontSize:12,borderBottom:'2px solid #e2e8f0',textAlign:'center',color:'#475569' },
  dayCell:{ padding:'12px 8px',fontWeight:700,fontSize:13,color:'#1e293b',background:'#f8fafc',
    borderRight:'2px solid #e2e8f0',textAlign:'center',letterSpacing:'0.05em' },
  lunchCell:{ textAlign:'center',background:'#fef3c7',color:'#92400e',fontSize:12,fontWeight:600,padding:8 },
  breakCell:{ textAlign:'center',background:'#f0fdf4',color:'#166534',fontSize:12,fontWeight:600,padding:8 },
  freeCell:{ textAlign:'center',color:'#cbd5e1',fontSize:18,padding:8 },
  classCell:{ padding:'8px 6px',verticalAlign:'top',borderBottom:'1px solid #f1f5f9' },

  badge:{ padding:'6px 14px',borderRadius:20,border:'1.5px solid',fontSize:12,background:'#fff',
    display:'flex',alignItems:'center',gap:2 },
}

export default App
