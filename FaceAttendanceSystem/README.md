# Face Recognition Attendance System — v2

React + Spring Boot + MySQL + Python attendance system with role-based
access, JWT auth, geofencing, basic liveness detection, analytics, exports,
and unknown-person alerts.

## Structure

```
FaceAttendanceSystem/
├── springboot-backend/      Java/Spring Boot REST API + MySQL via JPA
├── python-face-recognition/ Training + webcam recognition + liveness checks
├── frontend/                 React app (Admin, Teacher, Student dashboards)
└── database/                 schema.sql + sample_data.sql
```

## Feature status — what's real vs. what's a known gap

Being upfront about this matters more before a viva than after, so here's
an honest table rather than a marketing one:

| Feature | Status |
|---|---|
| React web app | Built (Vite + React Router) |
| MySQL database | Done |
| Admin / Teacher / Student dashboards | Role-based routing and views |
| JWT-based secure login | Real BCrypt password hashing + JWT, not plaintext |
| Analytics dashboard (%, defaulters) | Percentage calc, defaulter list, bar chart |
| PDF and Excel report generation | Via OpenPDF and Apache POI |
| Encrypted face embeddings | AES encryption at rest — see caveat below |
| Geofencing | Haversine distance check — see caveat below |
| Liveness detection | Basic blink detection — see caveat below |
| Unknown-person alerts | Logged + emailed |
| Email alerts (confirmation, low attendance) | Wired up, OFF by default — needs real SMTP creds |
| Multi-classroom support | Partial — see below |
| "Start Attendance" button actually launching the camera | Not wired — see below |

### Caveats worth knowing for a viva

- **Liveness detection** is eye-blink detection (Eye Aspect Ratio dipping
  and recovering across a few frames). This defeats a static photo held up
  to the camera. It does NOT defeat a video of someone blinking played on
  a phone/tablet. Real anti-spoofing systems use depth sensors, IR, or
  trained CNN spoof classifiers — worth saying this plainly if asked.
- **Geofencing** uses the Haversine formula against a single fixed
  campus lat/long + radius, set in `application.properties`. It's a real,
  working check — but GPS accuracy on a fixed classroom camera really
  just means "hardcode the room's coordinates," not live GPS tracking.
- **Encrypted face embeddings** use a single static AES key from a config
  file. This demonstrates the concept correctly but isn't real key
  management (rotation, KMS/HSM) — don't claim this is production-grade
  security for real biometric data.
- **Multi-classroom support** — the `classroom` field exists and multiple
  `recognize.py` instances can point at one backend, but there's no
  central scheduling/coordination across rooms.
- **"Start Attendance" button** on the Teacher dashboard still doesn't
  trigger `recognize.py` — that hookup (how does a web click launch a
  Python process and track its status?) is a real design decision left
  open. For demos, run `recognize.py` manually from a terminal.

## Setup

### 1. Database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```
This creates all 5 tables (teacher, student, app_user, attendance,
unknown_detection) and seeds 3 student profiles. It does NOT seed any
login accounts — passwords need to go through the app's BCrypt encoder,
so accounts are created via API (step 3).

### 2. Backend

Edit `springboot-backend/src/main/resources/application.properties`:
- Set your real MySQL password
- Set `jwt.secret` to a real random 32+ byte string
- Set `encryption.secret-key` to a random 16-character string
- Set `geofence.campus-latitude` / `-longitude` to your actual campus
- Leave `attendance.alerts-enabled=false` unless you've configured real
  SMTP credentials in `spring.mail.*`

```bash
cd springboot-backend
mvn spring-boot:run
```

### 3. Create your first Admin account

There's no signup form (by design — admin accounts shouldn't be
self-service). Create the first one directly, before any auth is in
place to protect `/api/auth/register` itself:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@example.edu","password":"adminpass123","role":"ADMIN"}'
```

This endpoint is protected by `hasRole("ADMIN")` in SecurityConfig, which
means after the first admin exists, only a logged-in admin can create
more accounts through it — including more teachers and students. The
command above works for the very first account because there's no admin
yet to enforce it against; that's a bootstrapping gap worth knowing about,
not a bug you introduced.

Once you have an admin, log into the frontend and use Admin Dashboard →
Create Teacher/Student Login for everyone else.

### 4. Python recognition service

```bash
cd python-face-recognition
python3 -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env
```
Edit `.env`:
- `INTERNAL_API_KEY` must match `internal.api.key` in application.properties
- `CAMERA_LATITUDE` / `CAMERA_LONGITUDE` should match where the camera
  actually is, or every scan will be rejected by the geofence

Heads up: `face_recognition` depends on `dlib`, which needs `cmake` and a
C++ compiler installed BEFORE `pip install` will succeed. Budget time for
this — it's the most common setup failure in this stack.

```bash
python3 train.py       # builds encodings, syncs (encrypted) to backend
python3 recognize.py   # opens webcam, scans + liveness-checks periodically
```

### 5. Frontend

```bash
cd frontend
npm install
npm run dev
```
Opens on `http://localhost:5500`.

## Typical first-run order

1. Database → 2. Backend running → 3. Create admin account (curl) →
4. Log into frontend as admin → 5. Register student profiles (Students
page) → 6. Note each student's ID → 7. Create photo folders in
dataset/&lt;id&gt;/ → 8. train.py → 9. Create teacher login (Admin
dashboard) → 10. Log in as teacher → 11. Run recognize.py manually →
12. Check Reports/Analytics.

## What's genuinely not built

- The "Start Attendance" → camera trigger (documented above)
- Central multi-classroom scheduling
- Any UI to edit/delete students or teachers after creation
- Password reset flow
