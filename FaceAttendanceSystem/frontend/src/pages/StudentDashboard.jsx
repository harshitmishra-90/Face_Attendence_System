import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function StudentDashboard() {
  const { user } = useAuth();
  const [history, setHistory] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!user?.studentId) {
      setError("This account isn't linked to a student profile.");
      setLoading(false);
      return;
    }
    Api.getStudentAttendance(user.studentId)
      .then((data) => {
        setHistory(data);
        const uniqueSubjects = [...new Set(data.map((r) => r.subject))];
        setSubjects(uniqueSubjects);
      })
      .catch((err) => setError(err.message || "Could not load your attendance history."))
      .finally(() => setLoading(false));
  }, [user]);

  useEffect(() => {
    subjects.forEach((subject) => {
      Api.getStudentStats(user.studentId, subject)
        .then((s) => setStats((prev) => ({ ...prev, [subject]: s })))
        .catch(() => {});
    });
  }, [subjects]);

  return (
    <Layout>
      <h2 className="page-title">My Attendance</h2>
      {error && <p className="error-text">{error}</p>}
      {loading && <p className="loading-text">Loading...</p>}

      {subjects.length > 0 && (
        <div className="grid-3" style={{ marginBottom: 20 }}>
          {subjects.map((subject) => {
            const s = stats[subject];
            return (
              <div className="stat-card" key={subject}>
                <div className="label">{subject}</div>
                <div className="value">{s ? `${s.percentage}%` : "…"}</div>
                {s && (
                  <span className={`badge ${s.isDefaulter ? "warn" : "ok"}`}>
                    {s.presentDays} / {s.totalDays} classes
                  </span>
                )}
              </div>
            );
          })}
        </div>
      )}

      {!loading && (
        <div className="card">
          <h3>Recent Scans</h3>
          <table>
            <thead>
              <tr><th>Date</th><th>Subject</th><th>Time</th><th>Status</th></tr>
            </thead>
            <tbody>
              {history.length === 0 && (
                <tr><td colSpan={4} className="hint">No attendance recorded yet.</td></tr>
              )}
              {history.map((r) => (
                <tr key={r.attendanceId}>
                  <td>{r.date}</td>
                  <td>{r.subject}</td>
                  <td>{r.time}</td>
                  <td>
                    <span className={`badge ${r.status === "P" ? "ok" : "warn"}`}>
                      {r.status === "P" ? "Present" : "Absent"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
