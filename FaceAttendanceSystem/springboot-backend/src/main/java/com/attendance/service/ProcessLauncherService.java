package com.attendance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * Launches python-face-recognition/recognize.py as a subprocess when a
 * teacher clicks "Start Attendance", and can kill it again on "Stop".
 *
 * Only works because the backend and the camera/Python script run on the
 * SAME machine (true for your local setup). If you ever move to a real
 * classroom IP camera on a separate device, this whole approach needs
 * rethinking - the backend can't launch a process on a different computer.
 */
@Service
public class ProcessLauncherService {

    @Value("${python.recognition.script-path}")
    private String scriptPath;

    @Value("${python.recognition.venv-python}")
    private String pythonExecutable;

    private Process runningProcess;

    public synchronized boolean isRunning() {
        return runningProcess != null && runningProcess.isAlive();
    }

    public synchronized void startRecognition(Long teacherId, String subject) throws Exception {
        if (isRunning()) {
            throw new IllegalStateException("A recognition session is already running. Stop it first.");
        }

        List<String> command = List.of(
                pythonExecutable, "recognize.py",
                "--teacher-id", String.valueOf(teacherId),
                "--subject", subject
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(scriptPath));
        // recognize.py's own output isn't visible in any terminal once launched
        // this way, so it's redirected to a log file instead.
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(scriptPath, "recognize.log")));
        pb.redirectErrorStream(true);

        runningProcess = pb.start();
    }

    /** Called by the "Stop Attendance" button - kills the running scan session immediately. */
    public synchronized boolean stopRecognition() {
        if (!isRunning()) {
            return false;
        }
        runningProcess.destroy();
        // Give it a moment to shut down cleanly; force-kill if it doesn't.
        try {
            if (!runningProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                runningProcess.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
}
