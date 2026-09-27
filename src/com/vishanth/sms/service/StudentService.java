package com.vishanth.sms.service;

import com.vishanth.sms.model.Student;
import com.vishanth.sms.util.FileStorageManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer coordinating CRUD operations, business logic, analytics,
 * and persistence for Student entities.
 */
public class StudentService {

    private final Map<String, Student> studentMap; // Key: rollNumber (upper-case)
    private final FileStorageManager storageManager;

    public StudentService(FileStorageManager storageManager) {
        this.storageManager = storageManager;
        this.studentMap = new LinkedHashMap<>();
        loadData();
    }

    /**
     * Loads records from file storage into memory.
     */
    public void loadData() {
        try {
            List<Student> loaded = storageManager.loadStudents();
            studentMap.clear();
            for (Student s : loaded) {
                studentMap.put(s.getRollNumber().toUpperCase(), s);
            }
        } catch (IOException e) {
            System.err.println("Failed to load records from storage: " + e.getMessage());
        }
    }

    /**
     * Saves current in-memory records to file storage.
     */
    public boolean saveData() {
        try {
            storageManager.saveStudents(new ArrayList<>(studentMap.values()));
            return true;
        } catch (IOException e) {
            System.err.println("Failed to persist records: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new student if the roll number is unique.
     */
    public boolean addStudent(Student student) {
        if (student == null || student.getRollNumber() == null) {
            return false;
        }
        String key = student.getRollNumber().trim().toUpperCase();
        if (studentMap.containsKey(key)) {
            return false; // Duplicate
        }
        studentMap.put(key, student);
        saveData();
        return true;
    }

    /**
     * Finds a student by roll number (case-insensitive).
     */
    public Optional<Student> findByRollNumber(String rollNumber) {
        if (rollNumber == null) return Optional.empty();
        return Optional.ofNullable(studentMap.get(rollNumber.trim().toUpperCase()));
    }

    /**
     * Retrieves all student records.
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    /**
     * Updates an existing student's personal and academic attributes.
     */
    public boolean updateStudentDetails(String rollNumber, String name, String email,
                                         String contact, String course, int semester,
                                         double attendance) {
        Optional<Student> opt = findByRollNumber(rollNumber);
        if (opt.isEmpty()) return false;

        Student student = opt.get();
        if (name != null && !name.trim().isEmpty()) student.setName(name);
        if (email != null && !email.trim().isEmpty()) student.setEmail(email);
        if (contact != null && !contact.trim().isEmpty()) student.setContactNumber(contact);
        if (course != null && !course.trim().isEmpty()) student.setCourse(course);
        student.setSemester(semester);
        student.setAttendancePercentage(attendance);

        saveData();
        return true;
    }

    /**
     * Deletes a student by roll number.
     */
    public boolean deleteStudent(String rollNumber) {
        if (rollNumber == null) return false;
        Student removed = studentMap.remove(rollNumber.trim().toUpperCase());
        if (removed != null) {
            saveData();
            return true;
        }
        return false;
    }

    /**
     * Records or updates a subject mark for a student.
     */
    public boolean setSubjectMark(String rollNumber, String subject, double mark) {
        Optional<Student> opt = findByRollNumber(rollNumber);
        if (opt.isEmpty()) return false;

        opt.get().addSubjectMark(subject, mark);
        saveData();
        return true;
    }

    /**
     * Searches students by keyword matching roll number, name, or course.
     */
    public List<Student> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) return getAllStudents();
        String q = query.trim().toLowerCase();
        return studentMap.values().stream()
                .filter(s -> s.getRollNumber().toLowerCase().contains(q)
                          || s.getName().toLowerCase().contains(q)
                          || s.getCourse().toLowerCase().contains(q)
                          || s.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Filters students by course name.
     */
    public List<Student> filterByCourse(String course) {
        if (course == null || course.trim().isEmpty()) return getAllStudents();
        String target = course.trim().toLowerCase();
        return studentMap.values().stream()
                .filter(s -> s.getCourse().toLowerCase().contains(target))
                .collect(Collectors.toList());
    }

    /**
     * Filters students having attendance below a specific threshold (e.g. 75%).
     */
    public List<Student> filterByAttendanceBelow(double threshold) {
        return studentMap.values().stream()
                .filter(s -> s.getAttendancePercentage() < threshold)
                .sorted(Comparator.comparingDouble(Student::getAttendancePercentage))
                .collect(Collectors.toList());
    }

    /**
     * Returns top N students ranked by GPA.
     */
    public List<Student> getTopPerformers(int limit) {
        return studentMap.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.calculateGPA(), s1.calculateGPA()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Calculates comprehensive batch analytics.
     */
    public Map<String, Object> calculateBatchStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        int count = studentMap.size();
        stats.put("totalStudents", count);

        if (count == 0) {
            stats.put("averageGPA", 0.0);
            stats.put("highestGPA", 0.0);
            stats.put("lowestGPA", 0.0);
            stats.put("averageAttendance", 0.0);
            stats.put("gradeCounts", Collections.emptyMap());
            return stats;
        }

        double totalGpa = 0.0;
        double maxGpa = -1.0;
        double minGpa = 11.0;
        double totalAttendance = 0.0;
        Map<String, Integer> gradeDist = new TreeMap<>();

        for (Student s : studentMap.values()) {
            double gpa = s.calculateGPA();
            totalGpa += gpa;
            if (gpa > maxGpa) maxGpa = gpa;
            if (gpa < minGpa) minGpa = gpa;
            totalAttendance += s.getAttendancePercentage();

            String grade = s.calculateGrade().split(" ")[0]; // e.g. "O", "A+", "A"
            gradeDist.put(grade, gradeDist.getOrDefault(grade, 0) + 1);
        }

        stats.put("averageGPA", Math.round((totalGpa / count) * 100.0) / 100.0);
        stats.put("highestGPA", maxGpa);
        stats.put("lowestGPA", minGpa);
        stats.put("averageAttendance", Math.round((totalAttendance / count) * 100.0) / 100.0);
        stats.put("gradeCounts", gradeDist);

        return stats;
    }

    /**
     * Seeds initial demo records aligned with Vishanth's resume profile and classmates.
     */
    public void seedInitialDemoData() {
        if (!studentMap.isEmpty()) return;

        Student s1 = new Student("25JUCS001", "Vishanth R", "vishantra@gmail.com", "+919677730223", "B.Tech CSE (AI & DE)", 2, 88.5);
        s1.addSubjectMark("Data Structures & Algorithms", 89.0);
        s1.addSubjectMark("Object-Oriented Programming (Java)", 94.0);
        s1.addSubjectMark("Database Management Systems", 86.0);
        s1.addSubjectMark("Discrete Mathematics", 82.0);
        s1.addSubjectMark("Computer System Architecture", 85.0);

        Student s2 = new Student("25JUCS002", "Aarav Sharma", "aarav.sharma@example.com", "+919811223344", "B.Tech CSE (AI & DE)", 2, 91.0);
        s2.addSubjectMark("Data Structures & Algorithms", 92.0);
        s2.addSubjectMark("Object-Oriented Programming (Java)", 88.0);
        s2.addSubjectMark("Database Management Systems", 90.0);
        s2.addSubjectMark("Discrete Mathematics", 87.0);
        s2.addSubjectMark("Computer System Architecture", 91.0);

        Student s3 = new Student("25JUCS003", "Priya Nair", "priya.nair@example.com", "+919744556677", "B.Tech CSE (AI & DE)", 2, 72.0);
        s3.addSubjectMark("Data Structures & Algorithms", 74.0);
        s3.addSubjectMark("Object-Oriented Programming (Java)", 78.0);
        s3.addSubjectMark("Database Management Systems", 71.0);
        s3.addSubjectMark("Discrete Mathematics", 68.0);
        s3.addSubjectMark("Computer System Architecture", 70.0);

        Student s4 = new Student("25JUCS004", "Rohan Verma", "rohan.v@example.com", "+919633221100", "B.Tech CSE (Data Science)", 2, 65.5);
        s4.addSubjectMark("Data Structures & Algorithms", 62.0);
        s4.addSubjectMark("Object-Oriented Programming (Java)", 65.0);
        s4.addSubjectMark("Database Management Systems", 58.0);
        s4.addSubjectMark("Discrete Mathematics", 55.0);
        s4.addSubjectMark("Computer System Architecture", 60.0);

        Student s5 = new Student("25JUCS005", "Ananya Iyer", "ananya.iyer@example.com", "+919877889900", "B.Tech CSE (AI & DE)", 2, 95.0);
        s5.addSubjectMark("Data Structures & Algorithms", 96.0);
        s5.addSubjectMark("Object-Oriented Programming (Java)", 98.0);
        s5.addSubjectMark("Database Management Systems", 94.0);
        s5.addSubjectMark("Discrete Mathematics", 92.0);
        s5.addSubjectMark("Computer System Architecture", 95.0);

        addStudent(s1);
        addStudent(s2);
        addStudent(s3);
        addStudent(s4);
        addStudent(s5);
    }
}
