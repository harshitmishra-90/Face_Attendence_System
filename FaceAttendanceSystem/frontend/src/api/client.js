const BASE_URL = "http://localhost:8080/api";

async function request(path, options = {}) {
  const token = localStorage.getItem("token");
  const headers = { "Content-Type": "application/json", ...options.headers };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  } catch (networkErr) {
    throw new Error("Could not reach the backend. Is it running on port 8080?");
  }

  if (!res.ok) {
    let message = res.statusText;
    try {
      const text = await res.text();
      if (text) message = text;
    } catch {}
    throw new Error(message || `Request failed: ${res.status}`);
  }

  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) return res.json();
  return res;
}

export const Api = {
  // --- auth ---
  login: (email, password) =>
    request("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  registerUser: (dto) =>
    request("/auth/register", { method: "POST", body: JSON.stringify(dto) }),

  // --- students ---
  getStudents: () => request("/students"),
  getStudent: (id) => request(`/students/${id}`),
  registerStudent: (student) =>
    request("/students", { method: "POST", body: JSON.stringify(student) }),

  // --- teachers ---
  getTeachers: () => request("/teachers"),

  // --- attendance ---
  getAttendanceReport: (subject, date) =>
    request(`/attendance/report?subject=${encodeURIComponent(subject)}${date ? `&date=${date}` : ""}`),
  getStudentAttendance: (studentId) => request(`/attendance/student/${studentId}`),
  startSession: (teacherId, subject) =>
    request("/attendance/start-session", {
      method: "POST",
      body: JSON.stringify({ teacherId, subject }),
    }),
  stopSession: () => request("/attendance/stop-session", { method: "POST" }),

  // --- analytics ---
  getSubjects: () => request("/analytics/subjects"),
  getSubjectStats: (subject) =>
      request(`/analytics/subject?subject=${encodeURIComponent(subject)}`),
  getStudentStats: (studentId, subject) =>
    request(`/analytics/student/${studentId}?subject=${encodeURIComponent(subject)}`),
  getDefaulters: (subject) => request(`/analytics/defaulters?subject=${encodeURIComponent(subject)}`),

  // --- security ---
  getUnknownDetections: () => request("/unknown-detections"),

  // --- reports/export ---
  downloadReport: async (format, subject, date) => {
    const token = localStorage.getItem("token");
    const url = `${BASE_URL}/reports/${format}?subject=${encodeURIComponent(subject)}${date ? `&date=${date}` : ""}`;
    const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
    if (!res.ok) throw new Error("Export failed");
    const blob = await res.blob();
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    link.download = `attendance_${subject}.${format === "pdf" ? "pdf" : "xlsx"}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
  },
};
