import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const links = {
    ADMIN: [
      { to: "/admin", label: "Dashboard" },
      { to: "/admin/students", label: "Students" },
      { to: "/admin/teachers", label: "Teachers" },
      { to: "/admin/analytics", label: "Analytics" },
      { to: "/admin/reports", label: "Reports" },
      { to: "/admin/security", label: "Security Alerts" },
    ],
    TEACHER: [
      { to: "/teacher", label: "Dashboard" },
      { to: "/teacher/students", label: "Students" },
      { to: "/teacher/reports", label: "Reports" },
      { to: "/teacher/analytics", label: "Analytics" },
    ],
    STUDENT: [
      { to: "/student", label: "My Attendance" },
    ],
  };

  const roleLinks = user ? links[user.role] || [] : [];

  return (
    <div className="app-shell">
      <div className="sidebar">
        <h1>Face Attendance</h1>
        {user && (
          <div className="user-info">
            {user.name || user.email}
            <br />
            <strong>{user.role}</strong>
          </div>
        )}
        <nav>
          {roleLinks.map((link) => (
            <NavLink key={link.to} to={link.to} end>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button onClick={handleLogout}>Log out</button>
      </div>
      <div className="main-content">{children}</div>
    </div>
  );
}
