USE face_attendance;

-- Only student profile data is seeded here. Login accounts (admin, teacher,
-- student) are NOT seeded via SQL because passwords need to go through
-- BCrypt, which only the running application can do correctly - inserting
-- a hand-typed hash here risks a hash that doesn't actually match "the
-- password you think you set." See README "First-time setup" for the
-- curl commands that create real accounts through the API instead.

INSERT INTO student (name, roll_no, branch, year) VALUES
('Aditi Sharma', 'CS2023001', 'Computer Science', '3rd Year'),
('Karan Patel', 'CS2023002', 'Computer Science', '3rd Year'),
('Meera Nair', 'CS2023003', 'Computer Science', '3rd Year');

-- face_encoding left NULL here; populated by train.py once real photos
-- are captured and processed. Don't hand-fill this column.
