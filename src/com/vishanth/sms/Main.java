package com.vishanth.sms;

import com.vishanth.sms.model.Student;
import com.vishanth.sms.service.StudentService;
import com.vishanth.sms.util.FileStorageManager;
import com.vishanth.sms.util.InputValidator;

import java.nio.file.Paths;
import java.util.*;

/**
 * Main application console user interface for Student Management System.
 * Architected adhering to OOP principles with persistent file handling.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static StudentService studentService;

    public static void main(String[] args) {
        String dataPath = Paths.get("data", "students.csv").toString();
        FileStorageManager storageManager = new FileStorageManager(dataPath);
        studentService = new StudentService(storageManager);

        // If storage is empty, initialize with realistic sample data
        if (studentService.getAllStudents().isEmpty()) {
            studentService.seedInitialDemoData();
        }

        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter your choice (1-9): ").trim();

            switch (choice) {
                case "1":
                    handleDisplayAllStudents();
                    break;
                case "2":
                    handleSearchByRollNumber();
                    break;
                case "3":
                    handleAddNewStudent();
                    break;
                case "4":
                    handleUpdateStudent();
                    break;
                case "5":
                    handleManageSubjectMarks();
                    break;
                case "6":
                    handleDeleteStudent();
                    break;
                case "7":
                    handleFilterAndQueries();
                    break;
                case "8":
                    handleBatchAnalytics();
                    break;
                case "9":
                    studentService.saveData();
                    System.out.println("\n[✓] Student records saved to " + storageManager.getStoragePath());
                    System.out.println("[✓] Exiting Student Management System. Goodbye!\n");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid option. Please select between 1 and 9.\n");
            }
        }
    }

    private static void printWelcomeBanner() {
        System.out.println("================================================================================");
        System.out.println("                    ACADEMIC STUDENT MANAGEMENT SYSTEM                          ");
        System.out.println("               Built with Java, OOP Principles & File I/O                       ");
        System.out.println("                         Author: Vishanth R                                     ");
        System.out.println("================================================================================");
    }

    private static void printMainMenu() {
        System.out.println("\n----------------------------- MAIN MENU ----------------------------------------");
        System.out.println(" 1. View All Students (Tabular Summary)");
        System.out.println(" 2. Search Student by Roll Number (Detailed Academic Card)");
        System.out.println(" 3. Add New Student Record");
        System.out.println(" 4. Modify Student Details");
        System.out.println(" 5. Manage Subject Marks");
        System.out.println(" 6. Delete Student Record");
        System.out.println(" 7. Filter & Query Records (Course / Low Attendance < 75%)");
        System.out.println(" 8. View Batch Performance & Academic Analytics");
        System.out.println(" 9. Save & Exit Application");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void handleDisplayAllStudents() {
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("\n[i] No student records currently on file.");
            return;
        }

        printStudentTable(students);
    }

    private static void printStudentTable(List<Student> list) {
        System.out.println("\n==========================================================================================================");
        System.out.printf("%-12s | %-20s | %-24s | %-4s | %-6s | %-16s | %-10s%n",
                "Roll No", "Student Name", "Course", "Sem", "GPA", "Grade", "Attendance");
        System.out.println("-------------+----------------------+--------------------------+------+--------+------------------+-----------");

        for (Student s : list) {
            System.out.printf("%-12s | %-20s | %-24s | %-4d | %-6.2f | %-16s | %-9.1f%%%n",
                    s.getRollNumber(),
                    truncate(s.getName(), 20),
                    truncate(s.getCourse(), 24),
                    s.getSemester(),
                    s.calculateGPA(),
                    truncate(s.calculateGrade(), 16),
                    s.getAttendancePercentage()
            );
        }
        System.out.println("==========================================================================================================");
        System.out.println("Total Count: " + list.size() + " student(s)\n");
    }

    private static void handleSearchByRollNumber() {
        String roll = prompt("Enter Roll Number to search: ").trim();
        Optional<Student> opt = studentService.findByRollNumber(roll);

        if (opt.isEmpty()) {
            System.out.println("\n[✗] Student with Roll Number '" + roll + "' was not found.");
            return;
        }

        Student s = opt.get();
        printStudentCard(s);
    }

    private static void printStudentCard(Student s) {
        System.out.println("\n==================================================================");
        System.out.println("                   STUDENT ACADEMIC PROFILE                       ");
        System.out.println("==================================================================");
        System.out.printf("  Roll Number   : %s%n", s.getRollNumber());
        System.out.printf("  Full Name     : %s%n", s.getName());
        System.out.printf("  Email         : %s%n", s.getEmail());
        System.out.printf("  Contact       : %s%n", s.getContactNumber());
        System.out.printf("  Course        : %s%n", s.getCourse());
        System.out.printf("  Semester      : %d%n", s.getSemester());
        System.out.printf("  Attendance    : %.2f%%%n", s.getAttendancePercentage());
        System.out.printf("  Computed GPA  : %.2f / 10.0%n", s.calculateGPA());
        System.out.printf("  Overall Grade : %s%n", s.calculateGrade());
        System.out.println("------------------------------------------------------------------");
        System.out.println("  SUBJECT BREAKDOWN & MARKS:");
        Map<String, Double> marks = s.getSubjectMarks();
        if (marks.isEmpty()) {
            System.out.println("    (No subject marks entered yet)");
        } else {
            for (Map.Entry<String, Double> entry : marks.entrySet()) {
                System.out.printf("    - %-32s : %6.2f / 100%n", entry.getKey(), entry.getValue());
            }
        }
        System.out.println("==================================================================\n");
    }

    private static void handleAddNewStudent() {
        System.out.println("\n>>> ADD NEW STUDENT");

        String roll;
        while (true) {
            roll = prompt("Enter Roll Number (e.g. 25JUCS010): ").trim();
            if (!InputValidator.isValidRollNumber(roll)) {
                System.out.println("[!] Invalid roll number format (alphanumeric, 2-20 characters).");
                continue;
            }
            if (studentService.findByRollNumber(roll).isPresent()) {
                System.out.println("[!] Roll number '" + roll + "' already exists! Must be unique.");
                continue;
            }
            break;
        }

        String name;
        while (true) {
            name = prompt("Enter Student Full Name: ").trim();
            if (!InputValidator.isValidName(name)) {
                System.out.println("[!] Name must be between 2 and 50 characters.");
                continue;
            }
            break;
        }

        String email;
        while (true) {
            email = prompt("Enter Email Address: ").trim();
            if (!InputValidator.isValidEmail(email)) {
                System.out.println("[!] Invalid email format (e.g. student@example.com).");
                continue;
            }
            break;
        }

        String contact;
        while (true) {
            contact = prompt("Enter Contact Number: ").trim();
            if (!InputValidator.isValidContactNumber(contact)) {
                System.out.println("[!] Invalid contact number (7-15 digits).");
                continue;
            }
            break;
        }

        String course;
        while (true) {
            course = prompt("Enter Course Name (e.g. B.Tech CSE (AI & DE)): ").trim();
            if (!InputValidator.isValidCourse(course)) {
                System.out.println("[!] Course name cannot be empty.");
                continue;
            }
            break;
        }

        int semester;
        while (true) {
            String semStr = prompt("Enter Current Semester (1-12): ").trim();
            try {
                semester = Integer.parseInt(semStr);
                if (InputValidator.isValidSemester(semester)) break;
            } catch (NumberFormatException ignored) {}
            System.out.println("[!] Please enter a valid semester number between 1 and 12.");
        }

        double attendance;
        while (true) {
            String attStr = prompt("Enter Attendance Percentage (0-100): ").trim();
            try {
                attendance = Double.parseDouble(attStr);
                if (InputValidator.isValidAttendance(attendance)) break;
            } catch (NumberFormatException ignored) {}
            System.out.println("[!] Please enter a valid percentage between 0.0 and 100.0.");
        }

        Student newStudent = new Student(roll, name, email, contact, course, semester, attendance);

        // Optional prompt for initial marks
        String addMarks = prompt("Would you like to enter subject marks now? (y/n): ").trim();
        if (addMarks.equalsIgnoreCase("y") || addMarks.equalsIgnoreCase("yes")) {
            enterMarksLoop(newStudent);
        }

        boolean added = studentService.addStudent(newStudent);
        if (added) {
            System.out.println("\n[✓] Student successfully added and saved to storage!");
        } else {
            System.out.println("\n[✗] Failed to add student record.");
        }
    }

    private static void handleUpdateStudent() {
        System.out.println("\n>>> MODIFY STUDENT RECORD");
        String roll = prompt("Enter Roll Number to modify: ").trim();
        Optional<Student> opt = studentService.findByRollNumber(roll);

        if (opt.isEmpty()) {
            System.out.println("[✗] Student not found with Roll Number: " + roll);
            return;
        }

        Student s = opt.get();
        System.out.println("[i] Current details for: " + s.getName() + " (" + s.getRollNumber() + ")");
        System.out.println("    Press ENTER to keep existing values.\n");

        String name = prompt("Name [" + s.getName() + "]: ").trim();
        if (name.isEmpty()) name = s.getName();

        String email = prompt("Email [" + s.getEmail() + "]: ").trim();
        if (email.isEmpty()) email = s.getEmail();

        String contact = prompt("Contact [" + s.getContactNumber() + "]: ").trim();
        if (contact.isEmpty()) contact = s.getContactNumber();

        String course = prompt("Course [" + s.getCourse() + "]: ").trim();
        if (course.isEmpty()) course = s.getCourse();

        String semStr = prompt("Semester [" + s.getSemester() + "]: ").trim();
        int semester = s.getSemester();
        if (!semStr.isEmpty()) {
            try {
                int parsed = Integer.parseInt(semStr);
                if (InputValidator.isValidSemester(parsed)) semester = parsed;
            } catch (NumberFormatException ignored) {}
        }

        String attStr = prompt("Attendance % [" + s.getAttendancePercentage() + "]: ").trim();
        double attendance = s.getAttendancePercentage();
        if (!attStr.isEmpty()) {
            try {
                double parsed = Double.parseDouble(attStr);
                if (InputValidator.isValidAttendance(parsed)) attendance = parsed;
            } catch (NumberFormatException ignored) {}
        }

        boolean updated = studentService.updateStudentDetails(roll, name, email, contact, course, semester, attendance);
        if (updated) {
            System.out.println("\n[✓] Student record updated successfully!");
        } else {
            System.out.println("\n[✗] Failed to update student record.");
        }
    }

    private static void handleManageSubjectMarks() {
        System.out.println("\n>>> MANAGE SUBJECT MARKS");
        String roll = prompt("Enter Student Roll Number: ").trim();
        Optional<Student> opt = studentService.findByRollNumber(roll);

        if (opt.isEmpty()) {
            System.out.println("[✗] Student not found with Roll Number: " + roll);
            return;
        }

        Student s = opt.get();
        System.out.println("[i] Editing marks for: " + s.getName());
        enterMarksLoop(s);
        studentService.saveData();
        System.out.println("\n[✓] Subject marks updated successfully. Current GPA: " + s.calculateGPA());
    }

    private static void enterMarksLoop(Student student) {
        while (true) {
            String subject = prompt("  Enter Subject Name (or 'done' to stop): ").trim();
            if (subject.equalsIgnoreCase("done") || subject.isEmpty()) {
                break;
            }

            double mark;
            while (true) {
                String markStr = prompt("  Enter Mark for '" + subject + "' (0 - 100): ").trim();
                try {
                    mark = Double.parseDouble(markStr);
                    if (InputValidator.isValidMark(mark)) break;
                } catch (NumberFormatException ignored) {}
                System.out.println("  [!] Please enter a numeric mark between 0 and 100.");
            }

            student.addSubjectMark(subject, mark);
            System.out.printf("  [+] Recorded %s: %.2f%n", subject, mark);
        }
    }

    private static void handleDeleteStudent() {
        System.out.println("\n>>> DELETE STUDENT RECORD");
        String roll = prompt("Enter Roll Number to delete: ").trim();
        Optional<Student> opt = studentService.findByRollNumber(roll);

        if (opt.isEmpty()) {
            System.out.println("[✗] Student not found.");
            return;
        }

        Student s = opt.get();
        String confirm = prompt("Are you sure you want to permanently delete '" + s.getName() + "' (" + roll + ")? (y/n): ").trim();
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            boolean deleted = studentService.deleteStudent(roll);
            if (deleted) {
                System.out.println("[✓] Record deleted successfully.");
            } else {
                System.out.println("[✗] Failed to delete record.");
            }
        } else {
            System.out.println("[i] Deletion aborted.");
        }
    }

    private static void handleFilterAndQueries() {
        System.out.println("\n--- FILTER & SEARCH OPTIONS ---");
        System.out.println(" 1. Filter by Course");
        System.out.println(" 2. Filter Students with Low Attendance (< 75%)");
        System.out.println(" 3. View Top 3 Academic Performers (Ranked by GPA)");
        System.out.println(" 4. Keyword Search (Across Roll No, Name, Course, Email)");
        System.out.println(" 5. Back to Main Menu");

        String choice = prompt("Select filter (1-5): ").trim();
        switch (choice) {
            case "1": {
                String course = prompt("Enter course name substring: ").trim();
                List<Student> results = studentService.filterByCourse(course);
                printStudentTable(results);
                break;
            }
            case "2": {
                List<Student> lowAtt = studentService.filterByAttendanceBelow(75.0);
                if (lowAtt.isEmpty()) {
                    System.out.println("\n[✓] Excellent! No students are below 75% attendance threshold.");
                } else {
                    System.out.println("\n[!] WARNING: The following students have attendance below 75%:");
                    printStudentTable(lowAtt);
                }
                break;
            }
            case "3": {
                List<Student> top = studentService.getTopPerformers(3);
                System.out.println("\n[RANKING] TOP 3 ACADEMIC PERFORMERS:");
                printStudentTable(top);
                break;
            }
            case "4": {
                String q = prompt("Enter search keyword: ").trim();
                List<Student> match = studentService.searchStudents(q);
                printStudentTable(match);
                break;
            }
            default:
                break;
        }
    }

    private static void handleBatchAnalytics() {
        Map<String, Object> stats = studentService.calculateBatchStatistics();
        System.out.println("\n==================================================================");
        System.out.println("               BATCH ACADEMIC ANALYTICS REPORT                    ");
        System.out.println("==================================================================");
        System.out.printf("  Total Enrolled Students : %s%n", stats.get("totalStudents"));
        System.out.printf("  Batch Average GPA       : %.2f / 10.0%n", stats.get("averageGPA"));
        System.out.printf("  Highest Recorded GPA    : %.2f%n", stats.get("highestGPA"));
        System.out.printf("  Lowest Recorded GPA     : %.2f%n", stats.get("lowestGPA"));
        System.out.printf("  Average Attendance      : %.2f%%%n", stats.get("averageAttendance"));
        System.out.println("------------------------------------------------------------------");
        System.out.println("  GRADE DISTRIBUTION:");
        @SuppressWarnings("unchecked")
        Map<String, Integer> grades = (Map<String, Integer>) stats.get("gradeCounts");
        if (grades != null && !grades.isEmpty()) {
            for (Map.Entry<String, Integer> e : grades.entrySet()) {
                System.out.printf("    Grade %-4s : %d student(s)%n", e.getKey(), e.getValue());
            }
        } else {
            System.out.println("    (No grade data available)");
        }
        System.out.println("==================================================================\n");
    }

    private static String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
