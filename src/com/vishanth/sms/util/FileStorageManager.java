package com.vishanth.sms.util;

import com.vishanth.sms.model.Student;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent file storage operations for Student records.
 * Supports CSV persistence, atomic writing, and automated fallback initialization.
 */
public class FileStorageManager {

    private static final String CSV_HEADER = "RollNumber,Name,Email,ContactNumber,Course,Semester,AttendancePercentage,SubjectMarks";
    private final Path filePath;

    public FileStorageManager(String relativeOrAbsolutePath) {
        this.filePath = Paths.get(relativeOrAbsolutePath);
    }

    /**
     * Ensures parent directory exists.
     */
    public void initStorage() throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(filePath)) {
            // Write initial header
            Files.write(filePath, (CSV_HEADER + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Loads student records from the CSV file.
     */
    public List<Student> loadStudents() throws IOException {
        List<Student> students = new ArrayList<>();

        if (!Files.exists(filePath)) {
            initStorage();
            return students;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isFirstLine) {
                    isFirstLine = false;
                    // Check if it's the header row
                    if (line.toLowerCase().startsWith("rollnumber") || line.toLowerCase().startsWith("roll")) {
                        continue;
                    }
                }

                try {
                    Student student = Student.fromCsvRow(line);
                    students.add(student);
                } catch (Exception ex) {
                    System.err.println("[Storage Warning] Skipping corrupted record at line " + lineNumber + ": " + ex.getMessage());
                }
            }
        }

        return students;
    }

    /**
     * Saves all students to the CSV file safely using a temporary file to prevent data corruption.
     */
    public synchronized void saveStudents(List<Student> students) throws IOException {
        initStorage();
        Path tempPath = Paths.get(filePath.toString() + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (Student student : students) {
                writer.write(student.toCsvRow());
                writer.newLine();
            }
        }

        // Atomically replace original file with temp file
        Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public String getStoragePath() {
        return filePath.toAbsolutePath().toString();
    }
}
