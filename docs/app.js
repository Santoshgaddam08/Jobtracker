
const { useEffect, useMemo, useState } = React;

const API_BASE = localStorage.getItem("API_BASE") || "http://localhost:8080";
const WS_URL = localStorage.getItem("WS_URL") || "ws://localhost:8080/ws";

const http = axios.create({ baseURL: API_BASE });
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

function decodeJwt(token) {
  try {
    const payload = token.split(".")[1];
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}

function App() {
  const [route, setRoute] = useState("login"); // login | register | dashboard | detail | analytics | settings
  const [selectedId, setSelectedId] = useState(null);
  const [toast, setToast] = useState(null);

  const token = localStorage.getItem("token");
  const me = useMemo(() => (token ? decodeJwt(token) : null), [token]);
  const userId = me?.userId || me?.sub || me?.id || null;

  function showToast(msg, type = "success") {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 2500);
  }

  useEffect(() => {
    if (token) setRoute("dashboard");
    else setRoute("login");
  }, []);

  // Realtime events
  useEffect(() => {
    if (!token || !userId) return;

    const client = new StompJs.Client({
      brokerURL: WS_URL,
      reconnectDelay: 2000,
      // Some backends ignore headers; safe either way.
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        client.subscribe(`/topic/users/${userId}`, (message) => {
          try {
            const evt = JSON.parse(message.body);
            showToast(`Realtime: ${evt.type}`, "success");
          } catch {
            showToast("Realtime event received", "success");
          }
        });
      },
      onStompError: () => {},
    });

    client.activate();
    return () => {
      try { client.deactivate(); } catch {}
    };
  }, [token, userId]);

  function logout() {
    localStorage.removeItem("token");
    setRoute("login");
    showToast("Logged out", "success");
  }

  return (
    <>
      <Header route={route} setRoute={setRoute} onLogout={logout} token={token} />
      <div className="wrap">
        {toast && (
          <div className={`card ${toast.type === "error" ? "error" : "success"}`}>
            {toast.msg}
          </div>
        )}

        {route === "login" && <Login onOk={() => { setRoute("dashboard"); showToast("Welcome!", "success"); }} />}
        {route === "register" && <Register onOk={() => { setRoute("dashboard"); showToast("Account created!", "success"); }} />}

        {route === "dashboard" && (
          <Dashboard
            onOpen={(id) => { setSelectedId(id); setRoute("detail"); }}
            onAnalytics={() => setRoute("analytics")}
            onSettings={() => setRoute("settings")}
            toast={showToast}
          />
        )}

        {route === "detail" && (
          <Detail
            id={selectedId}
            onBack={() => setRoute("dashboard")}
            toast={showToast}
          />
        )}

        {route === "analytics" && <Analytics onBack={() => setRoute("dashboard")} />}

        {route === "settings" && <Settings onBack={() => setRoute("dashboard")} toast={showToast} />}
      </div>
    </>
  );
}

function Header({ route, setRoute, onLogout, token }) {
  return (
    <header>
      <div><b>JobTracker</b> <span className="muted">– Java Full Stack Portfolio</span></div>
      <div>
        {token ? (
          <>
            <a className="link" onClick={() => setRoute("dashboard")}>Dashboard</a>
            <a className="link" onClick={() => setRoute("analytics")}>Analytics</a>
            <a className="link" onClick={() => setRoute("settings")}>Settings</a>
            <a className="link" onClick={onLogout}>Logout</a>
          </>
        ) : (
          <>
            <a className="link" onClick={() => setRoute("login")}>Login</a>
            <a className="link" onClick={() => setRoute("register")}>Register</a>
          </>
        )}
      </div>
    </header>
  );
}

function Login({ onOk }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");

  async function submit(e) {
    e.preventDefault();
    setErr("");
    try {
      const res = await http.post("/api/auth/login", { email, password });
      localStorage.setItem("token", res.data.token);
      onOk();
    } catch (e2) {
      setErr(e2?.response?.data?.message || "Login failed");
    }
  }

  return (
    <div className="card">
      <h2>Login</h2>
      <form onSubmit={submit}>
        <div className="row">
          <input placeholder="Email" value={email} onChange={(e)=>setEmail(e.target.value)} />
          <input placeholder="Password" type="password" value={password} onChange={(e)=>setPassword(e.target.value)} />
          <button>Sign in</button>
        </div>
      </form>
      {err && <div className="error">{err}</div>}
      <div className="muted" style={{ marginTop: 10 }}>
        Tip: go to <b>Settings</b> after login to set API/WS URLs for Codespaces later.
      </div>
    </div>
  );
}

function Register({ onOk }) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");

  async function submit(e) {
    e.preventDefault();
    setErr("");
    try {
      const res = await http.post("/api/auth/register", { fullName, email, password });
      localStorage.setItem("token", res.data.token);
      onOk();
    } catch (e2) {
      setErr(e2?.response?.data?.message || "Register failed");
    }
  }

  return (
    <div className="card">
      <h2>Register</h2>
      <form onSubmit={submit}>
        <div className="grid">
          <input placeholder="Full Name" value={fullName} onChange={(e)=>setFullName(e.target.value)} />
          <input placeholder="Email" value={email} onChange={(e)=>setEmail(e.target.value)} />
        </div>
        <div className="row" style={{ marginTop: 10 }}>
          <input placeholder="Password" type="password" value={password} onChange={(e)=>setPassword(e.target.value)} />
          <button>Create account</button>
        </div>
      </form>
      {err && <div className="error">{err}</div>}
    </div>
  );
}

function Dashboard({ onOpen, onAnalytics, onSettings, toast }) {
  const [apps, setApps] = useState([]);
  const [company, setCompany] = useState("");
  const [role, setRole] = useState("");
  const [status, setStatus] = useState("APPLIED");
  const [q, setQ] = useState("");
  const [err, setErr] = useState("");

  async function load() {
    setErr("");
    try {
      const res = await http.get("/api/applications");
      setApps(res.data || []);
    } catch (e) {
      setErr("Could not load applications. Check Settings → API Base URL.");
    }
  }

  useEffect(() => { load(); }, []);

  async function create(e) {
    e.preventDefault();
    setErr("");
    try {
      await http.post("/api/applications", { company, role, status });
      setCompany(""); setRole(""); setStatus("APPLIED");
      toast("Application created");
      await load();
    } catch {
      setErr("Create failed");
    }
  }

  async function del(id) {
    if (!confirm("Delete this application?")) return;
    await http.delete(`/api/applications/${id}`);
    toast("Deleted");
    await load();
  }

  const filtered = apps.filter(a => {
    const t = `${a.company||""} ${a.role||""} ${a.status||""}`.toLowerCase();
    return t.includes(q.toLowerCase());
  });

  return (
    <>
      <div className="card">
        <div className="row">
          <div>
            <h2 style={{ margin: 0 }}>Dashboard</h2>
            <div className="muted">Track applications, status, notes, reminders, and analytics.</div>
          </div>
          <button className="secondary" onClick={onAnalytics}>Analytics</button>
          <button className="secondary" onClick={onSettings}>Settings</button>
          <button className="secondary" onClick={load}>Refresh</button>
        </div>
      </div>

      <div className="card">
        <h3>Add Application</h3>
        <form onSubmit={create}>
          <div className="row">
            <input placeholder="Company" value={company} onChange={(e)=>setCompany(e.target.value)} />
            <input placeholder="Role" value={role} onChange={(e)=>setRole(e.target.value)} />
            <select value={status} onChange={(e)=>setStatus(e.target.value)}>
              {["APPLIED","INTERVIEW","OFFER","REJECTED"].map(s => <option key={s} value={s}>{s}</option>)}
            </select>
            <button>Add</button>
          </div>
        </form>
        {err && <div className="error">{err}</div>}
      </div>

      <div className="card">
        <div className="row">
          <h3 style={{ margin: 0 }}>Applications</h3>
          <input placeholder="Search company/role/status..." value={q} onChange={(e)=>setQ(e.target.value)} />
        </div>

        <table>
          <thead>
            <tr>
              <th>Company</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(a => (
              <tr key={a.id}>
                <td>{a.company}</td>
                <td>{a.role}</td>
                <td><span className="tag">{a.status}</span></td>
                <td>
                  <span className="link" onClick={() => onOpen(a.id)}>Open</span>
                  {"  |  "}
                  <span className="link" onClick={() => del(a.id)}>Delete</span>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan="4" style={{ textAlign:"center" }}>No results</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}

function Detail({ id, onBack, toast }) {
  const [data, setData] = useState(null);
  const [noteBody, setNoteBody] = useState("");
  const [remTitle, setRemTitle] = useState("");
  const [remDueAt, setRemDueAt] = useState("");

  async function load() {
    const res = await http.get(`/api/applications/${id}`);
    setData(res.data);
  }

  useEffect(() => { load(); }, [id]);

  async function addNote(e) {
    e.preventDefault();
    await http.post(`/api/applications/${id}/notes`, { body: noteBody });
    setNoteBody("");
    toast("Note added");
    await load();
  }

  async function addReminder(e) {
    e.preventDefault();
    await http.post(`/api/applications/${id}/reminders`, { title: remTitle, dueAt: remDueAt });
    setRemTitle(""); setRemDueAt("");
    toast("Reminder added");
    await load();
  }

  if (!data) return <div className="card">Loading...</div>;

  const { application, history, notes, reminders } = data;

  return (
    <>
      <div className="card">
        <div className="row">
          <div>
            <h2 style={{ margin: 0 }}>{application.company} – {application.role}</h2>
            <div className="muted">Status: <span className="tag">{application.status}</span></div>
          </div>
          <button className="secondary" onClick={onBack}>Back</button>
          <button className="secondary" onClick={load}>Refresh</button>
        </div>
      </div>

      <div className="grid">
        <div className="card">
          <h3>Notes</h3>
          <form onSubmit={addNote}>
            <textarea rows="4" placeholder="Add note..." value={noteBody} onChange={(e)=>setNoteBody(e.target.value)}></textarea>
            <div style={{ marginTop: 8 }}><button>Add Note</button></div>
          </form>
          <ul>
            {(notes || []).map(n => <li key={n.id}>{n.body}</li>)}
          </ul>
        </div>

        <div className="card">
          <h3>Reminders</h3>
          <form onSubmit={addReminder}>
            <input placeholder="Title" value={remTitle} onChange={(e)=>setRemTitle(e.target.value)} />
            <div style={{ height: 10 }}></div>
            <input placeholder="DueAt ISO (e.g. 2026-01-16T10:00:00Z)" value={remDueAt} onChange={(e)=>setRemDueAt(e.target.value)} />
            <div style={{ marginTop: 8 }}><button>Add Reminder</button></div>
          </form>
          <ul>
            {(reminders || []).map(r => <li key={r.id}>{r.title} – {String(r.dueAt)}</li>)}
          </ul>
        </div>
      </div>

      <div className="card">
        <h3>Status History</h3>
        <ul>
          {(history || []).map(h => (
            <li key={h.id}>{h.fromStatus} → {h.toStatus} ({h.note || ""})</li>
          ))}
        </ul>
      </div>
    </>
  );
}

function Analytics({ onBack }) {
  const [summary, setSummary] = useState(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    http.get("/api/analytics/summary")
      .then(res => setSummary(res.data))
      .catch(() => setErr("Analytics not available. Check backend + Settings → API Base URL."));
  }, []);

  return (
    <div className="card">
      <div className="row">
        <h2 style={{ margin: 0 }}>Analytics</h2>
        <button className="secondary" onClick={onBack}>Back</button>
      </div>

      {err && <div className="error">{err}</div>}
      {!summary && !err && <div>Loading...</div>}

      {summary && (
        <>
          <p><b>Total applications:</b> {summary.totalApplications}</p>
          <p><b>Created last 7 days:</b> {summary.createdLast7Days}</p>
          <p><b>Status changes last 7 days:</b> {summary.statusChangesLast7Days}</p>
          <p><b>Avg days since applied:</b> {summary.avgDaysSinceApplied}</p>

          <h3>By Status</h3>
          <pre>{JSON.stringify(summary.byStatus, null, 2)}</pre>

          <h3>Top Companies</h3>
          <ul>
            {(summary.topCompanies || []).map(c => (
              <li key={c.company}>{c.company} — {c.count}</li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}

function Settings({ onBack, toast }) {
  const [api, setApi] = useState(API_BASE);
  const [ws, setWs] = useState(WS_URL);

  function save() {
    localStorage.setItem("API_BASE", api);
    localStorage.setItem("WS_URL", ws);
    toast("Saved settings. Refresh page to apply.");
  }

  return (
    <div className="card">
      <div className="row">
        <h2 style={{ margin: 0 }}>Settings</h2>
        <button className="secondary" onClick={onBack}>Back</button>
      </div>

      <p className="muted">
        This frontend is static (GitHub Pages friendly). When you run backend in Codespaces,
        set these URLs to your forwarded ports.
      </p>

      <div style={{ marginTop: 10 }}>
        <label className="muted">API Base URL</label>
        <input value={api} onChange={(e)=>setApi(e.target.value)} placeholder="http://localhost:8080" />
      </div>

      <div style={{ marginTop: 10 }}>
        <label className="muted">WebSocket URL</label>
        <input value={ws} onChange={(e)=>setWs(e.target.value)} placeholder="ws://localhost:8080/ws" />
      </div>

      <div style={{ marginTop: 12 }}>
        <button onClick={save}>Save</button>
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
