"""
recognize.py

Opens the webcam, periodically scans for faces, matches them against
encodings/encodings.pkl, runs a basic liveness (blink) check, and reports
matches to the backend via api_sender.py. Unmatched faces are reported to
the unknown-person alert endpoint.

Can be run manually from a terminal (as before), OR launched automatically
by the backend when a teacher clicks "Start Attendance" - in that case
--teacher-id and --subject are passed in as command-line arguments and
this file's own print() output goes to recognize.log instead of a
terminal you're watching.

IMPORTANT - liveness detection honesty note:
The blink-detection check here is a BASIC anti-spoofing measure suitable
for a student project demonstrating the concept. It is not a substitute
for production liveness detection (which typically uses depth sensors,
IR cameras, or trained CNN-based spoof classifiers). A sufficiently
determined person could still defeat this with e.g. a video of someone
blinking. Be upfront about this limitation if asked in a viva.
"""

import os
import pickle
import time
from collections import deque

import cv2
import face_recognition
import numpy as np
from scipy.spatial import distance as dist
from dotenv import load_dotenv

from api_sender import send_recognition_event, send_unknown_detection

load_dotenv()

ENCODINGS_PATH = "encodings/encodings.pkl"
CAMERA_INDEX = int(os.getenv("CAMERA_INDEX", "0"))
SCAN_TIMES = [30, 300]   # 30 sec, 5 min, 10 min
MATCH_TOLERANCE = float(os.getenv("FACE_MATCH_TOLERANCE", "0.6"))
CLASSROOM_NAME = os.getenv("CLASSROOM_NAME", "Room-101")
CAMERA_LATITUDE = float(os.getenv("CAMERA_LATITUDE", "28.684273"))
CAMERA_LONGITUDE = float(os.getenv("CAMERA_LONGITUDE", "77.505357"))
LIVENESS_CHECK_FRAMES = int(os.getenv("LIVENESS_CHECK_FRAMES", "60"))
LIVENESS_EAR_THRESHOLD = float(os.getenv("LIVENESS_EAR_THRESHOLD", "0.23"))
SAVE_UNKNOWN_SNAPSHOTS = os.getenv("SAVE_UNKNOWN_SNAPSHOTS", "true").lower() == "true"


def load_known_encodings():
    with open(ENCODINGS_PATH, "rb") as f:
        raw = pickle.load(f)

    student_ids = []
    vectors = []
    for student_id, encs in raw.items():
        for enc in encs:
            student_ids.append(student_id)
            vectors.append(enc)
    return student_ids, vectors


def eye_aspect_ratio(eye_points):
    a = dist.euclidean(eye_points[1], eye_points[5])
    b = dist.euclidean(eye_points[2], eye_points[4])
    c = dist.euclidean(eye_points[0], eye_points[3])
    return (a + b) / (2.0 * c)


def check_liveness(cap,
                   timeout_seconds=10,
                   ear_threshold=0.23,
                   consecutive_closed_frames=3):

    print(f"Watching for blink for {timeout_seconds} seconds...", flush=True)

    closed_counter = 0
    start_time = time.time()

    while time.time() - start_time < timeout_seconds:

        ret, frame = cap.read()
        if not ret:
            continue

        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        landmarks_list = face_recognition.face_landmarks(rgb_frame)

        if not landmarks_list:
            continue

        landmarks = landmarks_list[0]

        if "left_eye" not in landmarks or "right_eye" not in landmarks:
            continue

        left_ear = eye_aspect_ratio(landmarks["left_eye"])
        right_ear = eye_aspect_ratio(landmarks["right_eye"])
        avg_ear = (left_ear + right_ear) / 2.0

        print(f"EAR = {avg_ear:.3f}", flush=True)

        if avg_ear < ear_threshold:
            closed_counter += 1
        else:
            if closed_counter >= consecutive_closed_frames:
                print("Blink detected!", flush=True)
                return True

            closed_counter = 0

    return False


def recognize_frame(frame, known_ids, known_vectors):
    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    face_locations = face_recognition.face_locations(rgb_frame)
    face_encodings = face_recognition.face_encodings(rgb_frame, face_locations)

    matched_ids = []
    unmatched_count = 0

    for encoding in face_encodings:
        distances = face_recognition.face_distance(known_vectors, encoding)
        if len(distances) == 0:
            unmatched_count += 1
            continue
        best_match_index = np.argmin(distances)
        if distances[best_match_index] <= MATCH_TOLERANCE:
            matched_ids.append(known_ids[best_match_index])
        else:
            unmatched_count += 1

    return matched_ids, unmatched_count


def save_snapshot(frame):
    if not SAVE_UNKNOWN_SNAPSHOTS:
        return None
    os.makedirs("unknown_snapshots", exist_ok=True)
    path = f"unknown_snapshots/unknown_{int(time.time())}.jpg"
    cv2.imwrite(path, frame)
    return path

def run_scan_session(teacher_id: int, subject: str, num_scans: int = 3):
    known_ids, known_vectors = load_known_encodings()

    cap = cv2.VideoCapture(CAMERA_INDEX)
    if not cap.isOpened():
        raise RuntimeError(f"Could not open camera at index {CAMERA_INDEX}")

    # Warm-up camera
    for _ in range(5):
        cap.read()
        time.sleep(0.1)

    try:
        for scan_number in range(1, num_scans + 1):

            print(f"\n--- Scan {scan_number}/{num_scans} ({CLASSROOM_NAME}) ---", flush=True)

            ret, frame = cap.read()
            if not ret:
                print("  [warn] Failed to read frame.", flush=True)
                continue

            matched_ids, unmatched_count = recognize_frame(
                frame,
                known_ids,
                known_vectors
            )

            if not matched_ids and unmatched_count == 0:
                print("  No face detected.", flush=True)

            # ---------- Recognized Students ----------
            for student_id in matched_ids:

                print(f"Matched student {student_id}", flush=True)

                is_live = check_liveness(
                    cap,
                    timeout_seconds=10
                )

                if not is_live:
                    print(f"[LIVENESS FAILED] Student {student_id}", flush=True)
                    continue

                print(f"[LIVENESS OK] Marking attendance for {student_id}", flush=True)

                send_recognition_event(
                    student_id=int(student_id),
                    teacher_id=teacher_id,
                    subject=subject,
                    classroom=CLASSROOM_NAME,
                    scan_number=scan_number,
                    latitude=CAMERA_LATITUDE,
                    longitude=CAMERA_LONGITUDE,
                    liveness_verified=True,
                )

            # ---------- Unknown Faces ----------
            if unmatched_count > 0:

                print(
                    f"{unmatched_count} unknown face(s) detected.",
                    flush=True
                )

                snapshot_path = save_snapshot(frame)

                send_unknown_detection(
                    classroom=CLASSROOM_NAME,
                    subject=subject,
                    latitude=CAMERA_LATITUDE,
                    longitude=CAMERA_LONGITUDE,
                    snapshot_path=snapshot_path,
                )

            # ---------- Wait before next scan ----------
            if scan_number < num_scans:

                wait_time = SCAN_TIMES[scan_number - 1]

                print(
                    f"\nWaiting {wait_time} seconds for next scan...",
                    flush=True
                )

                time.sleep(wait_time)

    finally:
        cap.release()
        print("\nSession ended. Camera released.", flush=True)



if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--teacher-id", type=int, default=1)
    parser.add_argument("--subject", type=str, default="Data Structures")
    parser.add_argument("--num-scans", type=int, default=3)
    args = parser.parse_args()

    run_scan_session(teacher_id=args.teacher_id, subject=args.subject, num_scans=args.num_scans)
