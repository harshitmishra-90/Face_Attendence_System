import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ROLE_HOME = { ADMIN: "/admin", TEACHER: "/teacher", STUDENT: "/student" };

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    if (!email || !password) {
      setError("Enter both email and password.");
      return;
    }
    setLoading(true);
    try {
      const res = await login(email.trim(), password);
      navigate(ROLE_HOME[res.role] || "/login");
    } catch (err) {
      setError(err.message || "Login failed. Check your credentials and that the backend is running.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-wrap">
      <div className="card login-card">
        <h2>Sign in</h2>
        <form onSubmit={handleSubmit}>
          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="username" />

          <label>Password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />

          <div className="error-text">{error}</div>

          <button className="primary" type="submit" style={{ width: "100%" }} disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
        <p className="hint" style={{ marginTop: 16 }}>
          Admin, Teacher, and Student accounts all log in here — you'll land on the right dashboard automatically.
        </p>
      </div>
    </div>
  );
}
