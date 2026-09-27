package com.vishanth.sms.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete Student class inheriting from Person.
 * Demonstrates Inheritance, Polymorphism, and Encapsulation.
 */
public class Student extends Person {
    private static final long serialVersionUID = 1L;

    private String rollNumber;
    private String course;
    private int semester;
    private double attendancePercentage;
    private Map<String, Double> subjectMarks;

    public Student(String rollNumber, String name, String email, String contactNumber,
                   String course, int semester, double attendancePercentage) {
        super(name, email, contactNumber);
        this.rollNumber = rollNumber;
        this.course = course;
        this.semester = semester;
        this.attendancePercentage = attendancePercentage;
        this.subjectMarks = new HashMap<>();
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        if (semester > 0 && semester <= 12) {
            this.semester = semester;
        }
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        if (attendancePercentage >= 0.0 && attendancePercentage <= 100.0) {
            this.attendancePercentage = attendancePercentage;
        }
    }

    public Map<String, Double> getSubjectMarks() {
        return Collections.unmodifiableMap(subjectMarks);
    }

    public void addSubjectMark(String subject, double mark) {
        if (mark >= 0.0 && mark <= 100.0) {
            subjectMarks.put(subject.trim(), mark);
        }
    }

    public void setSubjectMarks(Map<String, Double> marks) {
        this.subjectMarks = new HashMap<>(marks);
    }

    /**
     * Computes the Grade Point Average on a 10.0 scale.
     */
    public double calculateGPA() {
        if (subjectMarks == null || subjectMarks.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (double mark : subjectMarks.values()) {
            total += mark;
        }
        double averagePercentage = total / subjectMarks.size();
        // Standard 10.0 conversion (percentage / 10)
        return Math.round((averagePercentage / 10.0) * 100.0) / 100.0;
    }

    /**
     * Determines letter grade based on calculated GPA.
     */
    public String calculateGrade() {
        double gpa = calculateGPA();
        if (gpa >= 9.0) return "O (Outstanding)";
        if (gpa >= 8.0) return "A+ (Excellent)";
        if (gpa >= 7.0) return "A (Very Good)";
        if (gpa >= 6.0) return "B+ (Good)";
        if (gpa >= 5.0) return "B (Above Average)";
        if (gpa >= 4.0) return "C (Pass)";
        return "F (Fail)";
    }

    // Polymorphism: Implementation of abstract methods from Person
    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String getFormattedSummary() {
        return String.format(
            "Roll No: %s | %s | %s (Sem %d) | GPA: %.2f | Attendance: %.1f%%",
            rollNumber, getName(), course, semester, calculateGPA(), attendancePercentage
        );
    }

    /**
     * Serializes student record to CSV row format:
     * rollNumber,name,email,contactNumber,course,semester,attendance,marksData(sub1:m1;sub2:m2)
     */
    public String toCsvRow() {
        StringBuilder marksStr = new StringBuilder();
        int idx = 0;
        for (Map.Entry<String, Double> entry : subjectMarks.entrySet()) {
            if (idx++ > 0) marksStr.append(";");
            marksStr.append(entry.getKey()).append(":").append(entry.getValue());
        }

        return String.format("%s,%s,%s,%s,%s,%d,%.1f,%s",
            escapeCsv(rollNumber),
            escapeCsv(getName()),
            escapeCsv(getEmail()),
            escapeCsv(getContactNumber()),
            escapeCsv(course),
            semester,
            attendancePercentage,
            escapeCsv(marksStr.toString())
        );
    }

    /**
     * Parses student record from CSV row.
     */
    public static Student fromCsvRow(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid CSV record format: insufficient fields");
        }

        String rollNo = unescapeCsv(parts[0]);
        String name = unescapeCsv(parts[1]);
        String email = unescapeCsv(parts[2]);
        String contact = unescapeCsv(parts[3]);
        String course = unescapeCsv(parts[4]);
        int semester = Integer.parseInt(parts[5].trim());
        double attendance = Double.parseDouble(parts[6].trim());

        Student student = new Student(rollNo, name, email, contact, course, semester, attendance);

        if (parts.length >= 8 && !parts[7].trim().isEmpty()) {
            String marksData = unescapeCsv(parts[7].trim());
            String[] subjects = marksData.split(";");
            for (String sub : subjects) {
                String[] pair = sub.split(":");
                if (pair.length == 2) {
                    try {
                        student.addSubjectMark(pair[0].trim(), Double.parseDouble(pair[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return student;
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    private static String unescapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"\"", "\"");
    }

    @Override
    public String toString() {
        return String.format(
            "Student [Roll: %s, Name: %s, Course: %s, Sem: %d, GPA: %.2f, Attendance: %.1f%%]",
            rollNumber, getName(), course, semester, calculateGPA(), attendancePercentage
        );
    }
}
