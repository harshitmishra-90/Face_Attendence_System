import { useState, useEffect, useRef } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";
import { useAuth } from "../context/AuthContext";

const POLL_INTERVAL_MS = 5000;

export default function TeacherDashboard() {
  const { user } = useAuth();
  const [subject, setSubject] = useState("");
  const [hint, setHint] = useState("");
  const [hintIsError, setHintIsError] = useState(false);
  const [sessionActive, setSessionActive] = useState(false);
  const [busy, setBusy] = useState(false);
  const [liveRecords, setLiveRecords] = useState([]);
  const pollRef = useRef(null);

  // While a session is active, re-fetch this subject's attendance every
  // few seconds so newly-recognized students show up without a page
  // refresh. This is what was actually missing before - the backend and
  // Python were both working; nothing in React ever asked for updates.
  useEffect(() => {
    if (!sessionActive) {
      clearInterval(pollRef.current);
      return;
    }

    const poll = () => {
      Api.getAttendanceReport(subject)
        .then(setLiveRecords)
        .catch(() => {}); // a transient poll failure shouldn't show as an error
    };

    poll(); // fetch immediately, don't wait for the first interval tick
    pollRef.current = setInterval(poll, POLL_INTERVAL_MS);

    return () => clearInterval(pollRef.current);
  }, [sessionActive, subject]);

  async function handleStart() {
    setHint("");
    setHintIsError(false);
    if (!subject.trim()) {
      setHint("Enter a subject first.");
      setHintIsError(true);
      return;
    }
    if (!user?.teacherId) {
      setHint("This account isn't linked to a teacher profile.");
      setHintIsError(true);
      return;
    }
    setBusy(true);
    try {
      await Api.startSession(user.teacherId, subject);
      setSessionActive(true);
      setHint(`Camera started for "${subject}". Scans will run in the background.`);
    } catch (err) {
      setHint(err.message || "Could not start the camera session.");
      setHintIsError(true);
    } finally {
      setBusy(false);
    }
  }

  async function handleStop() {
    setBusy(true);
    try {
      await Api.stopSession();
      setSessionActive(false);
      setLiveRecords([]);
      setHint("Session stopped.");
      setHintIsError(false);
    } catch (err) {
      setHint(err.message || "Could not stop the session.");
      setHintIsError(true);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Layout>
      <h2 className="page-title">Welcome, {user?.name || user?.email}</h2>

      <div className="card">
        <h3>Start Attendance</h3>
        <div className="grid-2">
          <div>
            <label>Subject / Class</label>
            <input
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              placeholder="e.g. Data Structures"
              disabled={sessionActive}
            />
          </div>
        </div>
        <div className="actions-row" style={{ marginTop: 20 }}>
          <button className="primary" style={{ marginTop: 0 }} onClick={handleStart} disabled={busy || sessionActive}>
            {busy && !sessionActive ? "Starting..." : "Start Attendance"}
          </button>
          <button className="secondary" onClick={handleStop} disabled={busy || !sessionActive}>
            {busy && sessionActive ? "Stopping..." : "Stop Attendance"}
          </button>
        </div>
        {hint && <p className={hintIsError ? "error-text" : "success-text"}>{hint}</p>}
        <p className="hint" style={{ marginTop: 12 }}>
          Requires the backend's <code>python.recognition.*</code> config to point at your local
          <code> python-face-recognition</code> folder and venv. Scan output is written to
          <code> python-face-recognition/recognize.log</code>.
        </p>
      </div>

      {sessionActive && (
        <div className="card">
          <h3>Recognized so far ({liveRecords.length})</h3>
          <p className="hint" style={{ marginBottom: 12 }}>
            Refreshes automatically every {POLL_INTERVAL_MS / 1000}s while the session is running.
          </p>
          <table>
            <thead>
              <tr><th>Student ID</th><th>Time</th><th>Scan #</th><th>Liveness</th></tr>
            </thead>
            <tbody>
              {liveRecords.length === 0 && (
                <tr><td colSpan={4} className="hint">No matches recognized yet.</td></tr>
              )}
              {liveRecords.map((r) => (
                <tr key={r.attendanceId}>
                  <td>{r.studentId}</td>
                  <td>{r.time}</td>
                  <td>{r.scanNumber ?? "—"}</td>
                  <td>{r.livenessVerified ? "✅" : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
