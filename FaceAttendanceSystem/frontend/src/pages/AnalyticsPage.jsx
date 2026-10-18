import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";

export default function AnalyticsPage() {
  const [subjects, setSubjects] = useState([]);
  const [subject, setSubject] = useState("");
  const [stats, setStats] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    Api.getSubjects()
        .then((subs) => {
          setSubjects(subs);
          if (subs.length > 0) setSubject(subs[0]);
        })
        .catch((err) => setError(err.message || "Could not load subject list."));
  }, []);

  useEffect(() => {
    if (!subject) return;
    setLoading(true);
    setError("");
    Api.getSubjectStats(subject)
        .then(setStats)
        .catch((err) => setError(err.message || "Could not load analytics for this subject."))
        .finally(() => setLoading(false));
  }, [subject]);

  const defaulterCount = stats.filter((s) => s.isDefaulter).length;

  return (
      <Layout>
        <h2 className="page-title">Analytics</h2>

        <div className="card">
          <label>Subject</label>
          {subjects.length === 0 ? (
              <p className="hint">No subjects with recorded attendance yet.</p>
          ) : (
              <select value={subject} onChange={(e) => setSubject(e.target.value)}>
                {subjects.map((s) => (
                    <option key={s} value={s}>{s}</option>
                ))}
              </select>
          )}
          <div className="error-text">{error}</div>
        </div>

        {loading && <p className="loading-text">Loading analytics...</p>}

        {!loading && stats.length > 0 && (
            <>
              <div className="grid-3" style={{ marginBottom: 20 }}>
                <div className="stat-card">
                  <div className="label">Students Tracked</div>
                  <div className="value">{stats.length}</div>
                </div>
                <div className="stat-card">
                  <div className="label">Below Threshold</div>
                  <div className="value" style={{ color: defaulterCount > 0 ? "var(--danger)" : "var(--success)" }}>
                    {defaulterCount}
                  </div>
                </div>
                <div className="stat-card">
                  <div className="label">Avg. Attendance</div>
                  <div className="value">
                    {(stats.reduce((sum, s) => sum + s.percentage, 0) / stats.length).toFixed(1)}%
                  </div>
                </div>
              </div>

              <div className="card">
                <h3>Attendance % by Student</h3>
                <ul className="attendance-list">
                  {[...stats]
                      .sort((a, b) => b.percentage - a.percentage)
                      .map((s) => (
                          <li key={s.studentId} className="attendance-list-row">
                    <span className="attendance-list-name">
                      {s.studentName} <span className="hint">({s.rollNo})</span>
                    </span>
                            <div className="attendance-list-bar-track">
                              <div
                                  className="attendance-list-bar-fill"
                                  style={{
                                    width: `${s.percentage}%`,
                                    background: s.isDefaulter ? "var(--danger)" : "var(--success)",
                                  }}
                              />
                            </div>
                            <span className="attendance-list-pct">{s.percentage}%</span>
                          </li>
                      ))}
                </ul>
              </div>

              <div className="card">
                <h3>Defaulter List (below threshold)</h3>
                <table>
                  <thead>
                  <tr><th>Name</th><th>Roll No</th><th>Present / Total</th><th>%</th></tr>
                  </thead>
                  <tbody>
                  {stats.filter((s) => s.isDefaulter).length === 0 && (
                      <tr><td colSpan={4} className="hint">No defaulters for this subject.</td></tr>
                  )}
                  {stats.filter((s) => s.isDefaulter).map((s) => (
                      <tr key={s.studentId}>
                        <td>{s.studentName}</td>
                        <td>{s.rollNo}</td>
                        <td>{s.presentDays} / {s.totalDays}</td>
                        <td><span className="badge warn">{s.percentage}%</span></td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
            </>
        )}

        {!loading && subjects.length > 0 && stats.length === 0 && !error && (
            <p className="hint">No attendance data recorded for this subject yet.</p>
        )}
      </Layout>
  );
}