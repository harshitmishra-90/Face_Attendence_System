import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { Api } from "../api/client";

export default function AdminDashboard() {
  const [studentCount, setStudentCount] = useState(null);
  const [teacherCount, setTeacherCount] = useState(null);
  const [loadError, setLoadError] = useState("");
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "TEACHER", department: "", linkedStudentId: "" });
  const [message, setMessage] = useState("");
  const [formError, setFormError] = useState("");

  useEffect(() => {
    Api.getStudents()
      .then((s) => setStudentCount(s.length))
      .catch(() => { setStudentCount("—"); setLoadError("Could not reach backend for stats."); });
    Api.getTeachers()
      .then((t) => setTeacherCount(t.length))
      .catch(() => setTeacherCount("—"));
  }, []);

  async function handleCreateUser(e) {
    e.preventDefault();
    setMessage("");
    setFormError("");

    if (!form.name || !form.email || !form.password) {
      setFormError("Name, email, and password are required.");
      return;
    }
    if (form.role === "STUDENT" && !form.linkedStudentId) {
      setFormError("Enter the linked Student ID for a student login.");
      return;
    }

    try {
      const dto = {
        name: form.name,
        email: form.email,
        password: form.password,
        role: form.role,
        department: form.role === "TEACHER" ? form.department : undefined,
        linkedStudentId: form.role === "STUDENT" ? Number(form.linkedStudentId) : undefined,
      };
      await Api.registerUser(dto);
      setMessage(`${form.role} account created for ${form.email}.`);
      setForm({ name: "", email: "", password: "", role: "TEACHER", department: "", linkedStudentId: "" });
    } catch (err) {
      setFormError(err.message || "Could not create account.");
    }
  }

  return (
    <Layout>
      <h2 className="page-title">Admin Dashboard</h2>

      <div className="grid-3" style={{ marginBottom: 20 }}>
        <div className="stat-card">
          <div className="label">Students</div>
          <div className="value">{studentCount ?? "…"}</div>
        </div>
        <div className="stat-card">
          <div className="label">Teachers</div>
          <div className="value">{teacherCount ?? "…"}</div>
        </div>
        <div className="stat-card">
          <div className="label">Role</div>
          <div className="value">Admin</div>
        </div>
      </div>
      {loadError && <p className="error-text">{loadError}</p>}

      <div className="card">
        <h3>Create Teacher / Student Login</h3>
        <p className="hint">
          For a Student login, the student profile must already exist (see Students page) — enter their Student ID below.
        </p>
        <form onSubmit={handleCreateUser}>
          <div className="grid-2">
            <div>
              <label>Full name</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div>
              <label>Role</label>
              <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                <option value="TEACHER">Teacher</option>
                <option value="STUDENT">Student</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>
          <div className="grid-2">
            <div>
              <label>Email</label>
              <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div>
              <label>Password</label>
              <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
          </div>

          {form.role === "TEACHER" && (
            <div>
              <label>Department</label>
              <input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} />
            </div>
          )}
          {form.role === "STUDENT" && (
            <div>
              <label>Linked Student ID</label>
              <input value={form.linkedStudentId} onChange={(e) => setForm({ ...form, linkedStudentId: e.target.value })} />
            </div>
          )}

          {message && <p className="success-text">{message}</p>}
          <div className="error-text">{formError}</div>

          <button className="primary" type="submit">Create Account</button>
        </form>
      </div>
    </Layout>
  );
}
