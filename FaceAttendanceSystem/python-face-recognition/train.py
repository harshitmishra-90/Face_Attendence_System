"""
train.py

Builds face encodings from images in dataset/ and saves them to
encodings/encodings.pkl.

Expected dataset layout (one folder per student, named by student_id):

    dataset/
        1/          <- student_id = 1
            img1.jpg
            img2.jpg
        2/
            img1.jpg
        ...

Run this whenever new students are added or re-photographed.
After generating an encoding for a student, this also pushes the encoding
up to the backend via PUT /api/students/{id}/face-encoding (protected by
the shared internal API key, not a user login), so the two data stores
don't drift out of sync. The backend encrypts it before storing - see
EncryptionService.java. If you'd rather keep training fully offline,
comment out the sync_to_backend() call.
"""

import os
import pickle
import json

import face_recognition
import requests
from dotenv import load_dotenv

load_dotenv()

DATASET_DIR = "dataset"
ENCODINGS_PATH = "encodings/encodings.pkl"
BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")
INTERNAL_API_KEY = os.getenv("INTERNAL_API_KEY", "change-this-shared-secret")


def build_encodings():
    known_encodings = {}  # student_id (str) -> list of encoding vectors

    if not os.path.isdir(DATASET_DIR):
        raise FileNotFoundError(f"Dataset directory not found: {DATASET_DIR}")

    for student_id in os.listdir(DATASET_DIR):
        student_dir = os.path.join(DATASET_DIR, student_id)
        if not os.path.isdir(student_dir):
            continue

        encodings_for_student = []
        for filename in os.listdir(student_dir):
            filepath = os.path.join(student_dir, filename)
            image = face_recognition.load_image_file(filepath)
            faces = face_recognition.face_encodings(image)

            if not faces:
                print(f"  [warn] no face found in {filepath}, skipping")
                continue

            encodings_for_student.append(faces[0])

        if encodings_for_student:
            known_encodings[student_id] = encodings_for_student
            print(f"  [ok] {len(encodings_for_student)} encoding(s) for student {student_id}")
        else:
            print(f"  [warn] no usable faces found for student {student_id}")

    return known_encodings


def save_encodings(encodings):
    os.makedirs(os.path.dirname(ENCODINGS_PATH), exist_ok=True)
    with open(ENCODINGS_PATH, "wb") as f:
        pickle.dump(encodings, f)
    print(f"Saved encodings for {len(encodings)} student(s) to {ENCODINGS_PATH}")


def sync_to_backend(encodings):
    """Push each student's average encoding to the backend so Student.faceEncoding
    stays populated (encrypted at rest). Optional - safe to skip if you only
    need local recognize.py."""
    headers = {
        "Content-Type": "application/json",
        "X-Internal-Api-Key": INTERNAL_API_KEY,
    }

    for student_id, vectors in encodings.items():
        avg_vector = [sum(dim) / len(vectors) for dim in zip(*vectors)]
        payload = json.dumps(avg_vector)

        url = f"{BACKEND_URL}/api/students/{student_id}/face-encoding"
        try:
            resp = requests.put(url, data=payload, headers=headers, timeout=5)
            resp.raise_for_status()
            print(f"  [synced] student {student_id} -> backend")
        except requests.RequestException as e:
            print(f"  [error] could not sync student {student_id} to backend: {e}")


if __name__ == "__main__":
    print("Building face encodings from dataset/ ...")
    encodings = build_encodings()
    save_encodings(encodings)

    print("\nSyncing encodings to backend (optional step)...")
    sync_to_backend(encodings)
