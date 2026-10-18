"""
api_sender.py

Sends recognition events and unknown-face reports from recognize.py to the
Spring Boot backend.

This is the one place the Python <-> Java API contract lives on this side.
If you change a payload shape, update the matching DTO in
springboot-backend/.../dto/ to match.
"""

import os
import requests
from dotenv import load_dotenv

load_dotenv()

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")
INTERNAL_API_KEY = os.getenv("INTERNAL_API_KEY", "change-this-shared-secret")

HEADERS = {
    "Content-Type": "application/json",
    "X-Internal-Api-Key": INTERNAL_API_KEY,
}


def send_recognition_event(student_id: int, teacher_id: int, subject: str, classroom: str,
                            scan_number: int, latitude: float, longitude: float,
                            liveness_verified: bool) -> bool:
    """POSTs a single recognition event. Returns True on success, False
    otherwise (including a 422 geofence rejection, logged but not raised -
    a dropped attendance ping shouldn't crash the scan session)."""
    url = f"{BACKEND_URL}/api/attendance/recognize"
    payload = {
        "studentId": student_id,
        "teacherId": teacher_id,
        "subject": subject,
        "classroom": classroom,
        "scanNumber": scan_number,
        "latitude": latitude,
        "longitude": longitude,
        "livenessVerified": liveness_verified,
    }

    try:
        print("Sending request to:", url)
        print("Payload:", payload)

        resp = requests.post(url, json=payload, headers=HEADERS, timeout=5)

        print("Status Code:", resp.status_code)
        print("Response:", resp.text)

        if resp.status_code == 422:
            print(f"[rejected] student {student_id} outside campus geofence")
            return False

        resp.raise_for_status()

        print("Attendance sent successfully!")
        return True

    except requests.RequestException as e:
        print("Request failed:", e)
        return False


def send_unknown_detection(classroom: str, subject: str, latitude: float, longitude: float,
                            snapshot_path: str = None) -> bool:
    """Reports an unrecognized face for security review."""
    url = f"{BACKEND_URL}/api/attendance/unknown-detection"
    payload = {
        "classroom": classroom,
        "subject": subject,
        "latitude": latitude,
        "longitude": longitude,
        "snapshotPath": snapshot_path,
    }

    try:
        resp = requests.post(url, json=payload, headers=HEADERS, timeout=5)
        resp.raise_for_status()
        return True
    except requests.RequestException as e:
        print(f"  [error] failed to report unknown detection: {e}")
        return False
