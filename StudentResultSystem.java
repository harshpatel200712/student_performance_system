import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


class InvalidMarksException extends Exception {
    public InvalidMarksException(String message) {
        super(message);
    }
}


class Student {
    private int id;
    private String name;
    private int[] marks;

    public Student(int id, String name, int[] marks)
            throws InvalidMarksException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                "Student ID must be positive.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Student name cannot be empty.");
        }

        setMarks(marks);
        this.id = id;
        this.name = name.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int[] getMarks() {
        return marks.clone();
    }

    public void setMarks(int[] marks)
            throws InvalidMarksException {

        if (marks == null || marks.length != 5) {
            throw new InvalidMarksException(
                "Enter marks for all 5 subjects.");
        }

        for (int mark : marks) {
            if (mark < 0 || mark > 100) {
                throw new InvalidMarksException(
                    "Marks must be between 0 and 100.");
            }
        }

        this.marks = marks.clone();
    }
}


interface ResultCalculator {
    StudentResult calculate(Student student);
}


class StudentResult {
    int total;
    double percentage;
    String grade;
    String status;

    public StudentResult(int total, double percentage,
                         String grade, String status) {
        this.total = total;
        this.percentage = percentage;
        this.grade = grade;
        this.status = status;
    }
}


class StandardResultCalculator implements ResultCalculator {

    @Override
    public StudentResult calculate(Student student) {
        int total = 0;
        boolean passed = true;

        for (int mark : student.getMarks()) {
            total += mark;

            if (mark < 35) {
                passed = false;
            }
        }

        double percentage = total / 5.0;
        String grade = getGrade(percentage);

        String status = passed ? "PASS" : "FAIL";

        return new StudentResult(
            total, percentage, grade, status);
    }

    protected String getGrade(double percentage) {
        if (percentage >= 90) {
            return "A+";
        } else if (percentage >= 80) {
            return "A";
        } else if (percentage >= 70) {
            return "B";
        } else if (percentage >= 60) {
            return "C";
        } else if (percentage >= 50) {
            return "D";
        } else {
            return "F";
        }
    }
}


class BestFourResultCalculator extends StandardResultCalculator {

    @Override
    public StudentResult calculate(Student student) {
        int[] marks = student.getMarks();
        int total = 0;

        
        int lowest = marks[0];

        for (int mark : marks) {
            if (mark < lowest) {
                lowest = mark;
            }
        }

        
        for (int mark : marks) {
            total += mark;
        }

        total = total - lowest;

        
        double percentage = total / 4.0;
        String grade = getGrade(percentage);

        
        boolean passed = true;

        for (int mark : marks) {
            if (mark < 35) {
                passed = false;
            }
        }

        String status = passed ? "PASS" : "FAIL";

        return new StudentResult(
            total, percentage, grade, status);
    }
}


public class StudentResultSystem extends JFrame {

    private JTextField idField;
    private JTextField nameField;
    private JTextField[] markFields;

    private JComboBox<String> methodBox;
    private JTable table;
    private DefaultTableModel tableModel;

    private ArrayList<Student> students;
    private HashMap<Integer, StudentResult> results;

    public StudentResultSystem() {
        students = new ArrayList<>();
        results = new HashMap<>();

        setTitle("Student Examination and Result System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 8, 8));

        idField = new JTextField();
        nameField = new JTextField();
        markFields = new JTextField[5];

        inputPanel.add(new JLabel("Student ID:"));
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Student Name:"));
        inputPanel.add(nameField);

        String[] subjects = {
            "Java", "Python", "DBMS", "Maths", "Computer Networks"
        };

        for (int i = 0; i < 5; i++) {
            inputPanel.add(new JLabel(subjects[i] + " Marks:"));
            markFields[i] = new JTextField();
            inputPanel.add(markFields[i]);
        }

        inputPanel.add(new JLabel("Calculation Method:"));

        methodBox = new JComboBox<>(new String[] {
            "All 5 Subjects", "Best 4 Subjects"
        });

        inputPanel.add(methodBox);

        
        String[] columns = {
            "ID", "Name", "Total", "Percentage", "Grade", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        
        JButton addButton = new JButton("Add Student");
        JButton sortNameButton = new JButton("Sort by Name");
        JButton sortPercentageButton =
            new JButton("Sort by Percentage");
        JButton summaryButton = new JButton("Show Summary");

        addButton.addActionListener(e -> addStudent());

        sortNameButton.addActionListener(e -> sortByName());

        sortPercentageButton.addActionListener(
            e -> sortByPercentage());

        summaryButton.addActionListener(e -> showSummary());

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(addButton);
        buttonPanel.add(sortNameButton);
        buttonPanel.add(sortPercentageButton);
        buttonPanel.add(summaryButton);

        
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    
    private void addStudent() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();

            int[] marks = new int[5];

            for (int i = 0; i < 5; i++) {
                marks[i] = Integer.parseInt(
                    markFields[i].getText().trim());
            }

           
            Student student = new Student(id, name, marks);

            
            for (Student s : students) {
                if (s.getId() == id) {
                    JOptionPane.showMessageDialog(this,
                        "Student ID already exists!");
                    return;
                }
            }

           
            ResultCalculator calculator;

            if (methodBox.getSelectedIndex() == 0) {
                calculator = new StandardResultCalculator();
            } else {
                calculator = new BestFourResultCalculator();
            }

            StudentResult result = calculator.calculate(student);

            
            students.add(student);
            results.put(id, result);

            
            tableModel.addRow(new Object[] {
                student.getId(),
                student.getName(),
                result.total,
                String.format("%.2f%%", result.percentage),
                result.grade,
                result.status
            });

            JOptionPane.showMessageDialog(this,
                "Student: " + student.getName()
                + "\nTotal Marks: " + result.total
                + "\nPercentage: "
                + String.format("%.2f%%", result.percentage)
                + "\nGrade: " + result.grade
                + "\nStatus: " + result.status);

           
            idField.setText("");
            nameField.setText("");

            for (JTextField field : markFields) {
                field.setText("");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Enter a valid numeric ID and marks.");
        } catch (InvalidMarksException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    
    private void sortByName() {
        Collections.sort(students,
            Comparator.comparing(Student::getName,
                String.CASE_INSENSITIVE_ORDER));

        refreshTable();
    }

    
    private void sortByPercentage() {
        Collections.sort(students, (s1, s2) ->
            Double.compare(
                results.get(s2.getId()).percentage,
                results.get(s1.getId()).percentage));

        refreshTable();
    }

    
    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Student student : students) {
            StudentResult result = results.get(student.getId());

            tableModel.addRow(new Object[] {
                student.getId(),
                student.getName(),
                result.total,
                String.format("%.2f%%", result.percentage),
                result.grade,
                result.status
            });
        }
    }

    
    private void showSummary() {
        int passed = 0;
        int failed = 0;

        for (StudentResult result : results.values()) {
            if (result.status.equals("PASS")) {
                passed++;
            } else {
                failed++;
            }
        }

        JOptionPane.showMessageDialog(this,
            "Total Students: " + students.size()
            + "\nPassed: " + passed
            + "\nFailed: " + failed);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentResultSystem app = new StudentResultSystem();
            app.setVisible(true);
        });
    }
}
