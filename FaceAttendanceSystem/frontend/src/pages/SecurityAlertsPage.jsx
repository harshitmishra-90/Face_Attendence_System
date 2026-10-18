import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";

export default function SecurityAlertsPage() {
  const [detections, setDetections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    Api.getUnknownDetections()
      .then(setDetections)
      .catch((err) => setError(err.message || "Could not load security alerts."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <h2 className="page-title">Security Alerts — Unknown Person Detections</h2>
      <p className="hint" style={{ marginBottom: 16 }}>
        Faces detected during scans that didn't match any registered student.
      </p>

      <div className="card">
        {loading && <p className="loading-text">Loading...</p>}
        {error && <p className="error-text">{error}</p>}
        {!loading && !error && (
          <table>
            <thead>
              <tr><th>Detected At</th><th>Classroom</th><th>Subject</th><th>Location</th></tr>
            </thead>
            <tbody>
              {detections.length === 0 && (
                <tr><td colSpan={4} className="hint">No unknown-person events recorded.</td></tr>
              )}
              {detections.map((d) => (
                <tr key={d.id}>
                  <td>{new Date(d.detectedAt).toLocaleString()}</td>
                  <td>{d.classroom}</td>
                  <td>{d.subject}</td>
                  <td>{d.latitude != null && d.longitude != null ? `${d.latitude.toFixed(4)}, ${d.longitude.toFixed(4)}` : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
