import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";

export default function StudentsPage() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [form, setForm] = useState({ name: "", rollNo: "", branch: "", year: "" });
  const [formError, setFormError] = useState("");

  function loadStudents() {
    setLoading(true);
    setLoadError("");
    Api.getStudents()
      .then(setStudents)
      .catch((err) => setLoadError(err.message || "Could not load students."))
      .finally(() => setLoading(false));
  }

  useEffect(loadStudents, []);

  async function handleRegister(e) {
    e.preventDefault();
    setFormError("");
    if (!form.name || !form.rollNo) {
      setFormError("Name and Roll No are required.");
      return;
    }
    try {
      await Api.registerStudent(form);
      setForm({ name: "", rollNo: "", branch: "", year: "" });
      loadStudents();
    } catch (err) {
      setFormError(err.message || "Could not register student. Roll No may already be taken.");
    }
  }

  return (
    <Layout>
      <h2 className="page-title">Students</h2>

      <div className="card">
        <h3>Register Student</h3>
        <form onSubmit={handleRegister}>
          <div className="grid-2">
            <div>
              <label>Name</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div>
              <label>Roll No</label>
              <input value={form.rollNo} onChange={(e) => setForm({ ...form, rollNo: e.target.value })} />
            </div>
          </div>
          <div className="grid-2">
            <div>
              <label>Branch</label>
              <input value={form.branch} onChange={(e) => setForm({ ...form, branch: e.target.value })} />
            </div>
            <div>
              <label>Year</label>
              <input value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })} />
            </div>
          </div>
          <div className="error-text">{formError}</div>
          <button className="primary" type="submit">Register Student</button>
        </form>
        <p className="hint" style={{ marginTop: 12 }}>
          After registering, note the ID shown below — it's the folder name to use under{" "}
          <code>python-face-recognition/dataset/</code> for that student's photos.
        </p>
      </div>

      <div className="card">
        <h3>All Students</h3>
        {loading && <p className="loading-text">Loading...</p>}
        {loadError && <p className="error-text">{loadError}</p>}
        {!loading && !loadError && (
          <table>
            <thead>
              <tr><th>ID</th><th>Name</th><th>Roll No</th><th>Branch</th><th>Year</th><th>Face Data</th></tr>
            </thead>
            <tbody>
              {students.length === 0 && (
                <tr><td colSpan={6} className="hint">No students yet.</td></tr>
              )}
              {students.map((s) => (
                <tr key={s.studentId}>
                  <td>{s.studentId}</td>
                  <td>{s.name}</td>
                  <td>{s.rollNo}</td>
                  <td>{s.branch}</td>
                  <td>{s.year}</td>
                  <td>
                    {s.faceEncoding ? (
                      <span className="badge ok">Trained</span>
                    ) : (
                      <span className="badge warn">Not trained</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
