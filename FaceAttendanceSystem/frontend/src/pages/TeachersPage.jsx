import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";

export default function TeachersPage() {
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    Api.getTeachers()
      .then(setTeachers)
      .catch((err) => setError(err.message || "Could not load teachers."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <h2 className="page-title">Teachers</h2>
      <p className="hint" style={{ marginBottom: 16 }}>
        Create new teacher logins from the Dashboard page's "Create Teacher/Student Login" form.
      </p>
      <div className="card">
        {loading && <p className="loading-text">Loading...</p>}
        {error && <p className="error-text">{error}</p>}
        {!loading && !error && (
          <table>
            <thead>
              <tr><th>ID</th><th>Name</th><th>Email</th><th>Department</th></tr>
            </thead>
            <tbody>
              {teachers.length === 0 && (
                <tr><td colSpan={4} className="hint">No teachers yet.</td></tr>
              )}
              {teachers.map((t) => (
                <tr key={t.teacherId}>
                  <td>{t.teacherId}</td>
                  <td>{t.name}</td>
                  <td>{t.email}</td>
                  <td>{t.department}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
