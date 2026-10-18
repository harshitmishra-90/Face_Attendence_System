import { useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";

export default function ReportsPage() {
  const [subject, setSubject] = useState("");
  const [date, setDate] = useState("");
  const [records, setRecords] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function loadReport() {
    setError("");
    if (!subject.trim()) {
      setError("Enter a subject.");
      return;
    }
    setLoading(true);
    try {
      const data = await Api.getAttendanceReport(subject, date);
      setRecords(data);
    } catch (err) {
      setError(err.message || "Could not load report.");
    } finally {
      setLoading(false);
    }
  }

  async function handleExport(format) {
    setError("");
    if (!subject.trim()) {
      setError("Enter a subject first.");
      return;
    }
    try {
      await Api.downloadReport(format, subject, date);
    } catch (err) {
      setError(err.message || `Could not export ${format.toUpperCase()}.`);
    }
  }

  return (
    <Layout>
      <h2 className="page-title">Attendance Reports</h2>

      <div className="card">
        <div className="grid-2">
          <div>
            <label>Subject</label>
            <input value={subject} onChange={(e) => setSubject(e.target.value)} placeholder="e.g. Data Structures" />
          </div>
          <div>
            <label>Date</label>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
        </div>
        <div className="actions-row" style={{ marginTop: 20 }}>
          <button className="primary" onClick={loadReport} style={{ marginTop: 0 }} disabled={loading}>
            {loading ? "Loading..." : "Load Report"}
          </button>
          <button className="secondary" onClick={() => handleExport("pdf")}>Export PDF</button>
          <button className="secondary" onClick={() => handleExport("excel")}>Export Excel</button>
        </div>
        <div className="error-text">{error}</div>
      </div>

      {records && (
        <div className="card">
          <table>
            <thead>
              <tr><th>Student ID</th><th>Time</th><th>Scan #</th><th>Liveness</th><th>Status</th></tr>
            </thead>
            <tbody>
              {records.length === 0 && (
                <tr><td colSpan={5} className="hint">No records for this subject/date yet.</td></tr>
              )}
              {records.map((r) => (
                <tr key={r.attendanceId}>
                  <td>{r.studentId}</td>
                  <td>{r.time}</td>
                  <td>{r.scanNumber ?? "—"}</td>
                  <td>{r.livenessVerified ? "✅" : "—"}</td>
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
