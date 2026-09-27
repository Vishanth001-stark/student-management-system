package com.vishanth.sms.test;

import com.vishanth.sms.model.Student;
import com.vishanth.sms.service.StudentService;
import com.vishanth.sms.util.FileStorageManager;
import com.vishanth.sms.util.InputValidator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Self-contained Automated Test Suite for Student Management System.
 * Tests OOP principles, CRUD operations, CSV persistence, GPA calculation, and input validation.
 */
public class StudentSystemTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("        RUNNING STUDENT MANAGEMENT SYSTEM AUTOMATED TESTS         ");
        System.out.println("==================================================================");

        testInputValidator();
        testModelAndCalculations();
        testServiceCrudOperations();
        testPersistenceCycle();
        testBatchAnalytics();

        System.out.println("------------------------------------------------------------------");
        System.out.printf("Test Results: %d Passed | %d Failed%n", testsPassed, testsFailed);
        System.out.println("==================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (match) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.printf("  [FAIL] %s - Expected: %s, Actual: %s%n", testName, expected, actual);
            testsFailed++;
        }
    }

    private static void testInputValidator() {
        System.out.println("\n[Testing InputValidator]");
        assertTrue("Valid roll number", InputValidator.isValidRollNumber("25JUCS001"));
        assertTrue("Invalid roll number with symbols", !InputValidator.isValidRollNumber("25@JU!#"));
        assertTrue("Valid email", InputValidator.isValidEmail("vishantra@gmail.com"));
        assertTrue("Invalid email without domain", !InputValidator.isValidEmail("vishantra@"));
        assertTrue("Valid contact number", InputValidator.isValidContactNumber("+919677730223"));
        assertTrue("Valid semester 2", InputValidator.isValidSemester(2));
        assertTrue("Invalid semester 15", !InputValidator.isValidSemester(15));
        assertTrue("Valid mark 95.0", InputValidator.isValidMark(95.0));
        assertTrue("Invalid mark 105.0", !InputValidator.isValidMark(105.0));
    }

    private static void testModelAndCalculations() {
        System.out.println("\n[Testing Student Model & GPA Calculations]");
        Student student = new Student("TEST001", "Test Student", "test@example.com", "9876543210", "B.Tech CSE", 2, 85.0);
        student.addSubjectMark("DSA", 90.0);
        student.addSubjectMark("DBMS", 80.0);

        // Average percentage = 85.0 -> GPA = 8.5
        assertEquals("GPA Calculation (90 + 80)/2/10", 8.5, student.calculateGPA());
        assertEquals("Grade determination", "A+ (Excellent)", student.calculateGrade());
        assertEquals("Role inheritance", "Student", student.getRole());

        // CSV Serialization & Deserialization
        String csvRow = student.toCsvRow();
        Student parsed = Student.fromCsvRow(csvRow);
        assertEquals("CSV Roundtrip Roll", student.getRollNumber(), parsed.getRollNumber());
        assertEquals("CSV Roundtrip Name", student.getName(), parsed.getName());
        assertEquals("CSV Roundtrip GPA", student.calculateGPA(), parsed.calculateGPA());
    }

    private static void testServiceCrudOperations() {
        System.out.println("\n[Testing StudentService CRUD]");
        String tempCsv = "data/test_students.csv";
        new File(tempCsv).delete();

        FileStorageManager sm = new FileStorageManager(tempCsv);
        StudentService service = new StudentService(sm);

        Student s1 = new Student("T01", "Alice", "alice@example.com", "9000000001", "CSE", 2, 90.0);
        Student s2 = new Student("T02", "Bob", "bob@example.com", "9000000002", "ECE", 2, 70.0);

        assertTrue("Add Student 1", service.addStudent(s1));
        assertTrue("Add Student 2", service.addStudent(s2));
        assertTrue("Prevent duplicate roll", !service.addStudent(s1));
        assertEquals("Total students count", 2, service.getAllStudents().size());

        Optional<Student> found = service.findByRollNumber("t01");
        assertTrue("Case-insensitive search found", found.isPresent());
        assertEquals("Found student name", "Alice", found.get().getName());

        service.updateStudentDetails("T01", "Alice Smith", "alicesmith@example.com", "9000000001", "CSE", 3, 92.0);
        Optional<Student> updated = service.findByRollNumber("T01");
        assertEquals("Updated name", "Alice Smith", updated.get().getName());
        assertEquals("Updated semester", 3, updated.get().getSemester());

        assertTrue("Delete Student T02", service.deleteStudent("T02"));
        assertEquals("Count after deletion", 1, service.getAllStudents().size());

        // Clean up test file
        new File(tempCsv).delete();
    }

    private static void testPersistenceCycle() {
        System.out.println("\n[Testing File Persistence Across Service Re-initialization]");
        String persistCsv = "data/test_persist.csv";
        new File(persistCsv).delete();

        FileStorageManager sm1 = new FileStorageManager(persistCsv);
        StudentService service1 = new StudentService(sm1);
        Student student = new Student("P01", "Persistent User", "user@persist.org", "9888877777", "AI & ML", 1, 99.0);
        student.addSubjectMark("Python", 98.0);
        service1.addStudent(student);

        // Re-initialize a second service reading from the exact same file
        FileStorageManager sm2 = new FileStorageManager(persistCsv);
        StudentService service2 = new StudentService(sm2);

        Optional<Student> reloaded = service2.findByRollNumber("P01");
        assertTrue("Reloaded student exists", reloaded.isPresent());
        assertEquals("Reloaded student name", "Persistent User", reloaded.get().getName());
        assertEquals("Reloaded subject mark", 98.0, reloaded.get().getSubjectMarks().get("Python"));

        // Clean up test file
        new File(persistCsv).delete();
    }

    private static void testBatchAnalytics() {
        System.out.println("\n[Testing Batch Analytics]");
        String tempCsv = "data/test_analytics.csv";
        new File(tempCsv).delete();

        FileStorageManager sm = new FileStorageManager(tempCsv);
        StudentService service = new StudentService(sm);

        Student s1 = new Student("A1", "One", "one@test.com", "1111111111", "CSE", 1, 80.0);
        s1.addSubjectMark("Subject1", 90.0); // GPA = 9.0 (O)
        Student s2 = new Student("A2", "Two", "two@test.com", "2222222222", "CSE", 1, 70.0);
        s2.addSubjectMark("Subject1", 70.0); // GPA = 7.0 (A)

        service.addStudent(s1);
        service.addStudent(s2);

        Map<String, Object> stats = service.calculateBatchStatistics();
        assertEquals("Batch count", 2, stats.get("totalStudents"));
        assertEquals("Average GPA", 8.0, stats.get("averageGPA"));
        assertEquals("Highest GPA", 9.0, stats.get("highestGPA"));
        assertEquals("Lowest GPA", 7.0, stats.get("lowestGPA"));
        assertEquals("Average Attendance", 75.0, stats.get("averageAttendance"));

        // Clean up
        new File(tempCsv).delete();
    }
}
