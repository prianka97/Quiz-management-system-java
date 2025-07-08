import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;


public class QuizManagementSystem {

    private static final String USERS_FILE = "src/main/resources/users.json";
    private static final String QUIZ_FILE = "src/main/resources/quiz.json";

    private static Scanner scanner = new Scanner(System.in);
    private static JSONParser parser = new JSONParser();

    public static void main(String[] args) {
        System.out.println("=== Welcome to Quiz Management System ===\n");

        initializeFiles();

        startLogin();
    }


    private static void initializeFiles() {
        File usersFile = new File(USERS_FILE);
        File quizFile = new File(QUIZ_FILE);

        try {
            usersFile.getParentFile().mkdirs();
            if (!usersFile.exists()) {
                createDefaultUsers();
                System.out.println("Created users.json with default users");
            }

            if (!quizFile.exists()) {
                createDefaultQuestions();
                System.out.println("Created quiz.json with sample questions");
            }

        } catch (IOException e) {
            System.err.println("Error initializing files: " + e.getMessage());
            System.exit(1);
        }
    }


    private static void createDefaultUsers() throws IOException {
        JSONArray usersArray = new JSONArray();

        // admin user
        JSONObject admin = new JSONObject();
        admin.put("username", "admin");
        admin.put("password", "1234");
        admin.put("role", "admin");
        usersArray.add(admin);

        // student
        JSONObject student = new JSONObject();
        student.put("username", "prianka");
        student.put("password", "1234");
        student.put("role", "student");
        usersArray.add(student);

        FileWriter writer = new FileWriter(USERS_FILE);
        writer.write(usersArray.toJSONString());
        writer.flush();
        writer.close();
    }

    private static void createDefaultQuestions() throws IOException {
        JSONArray quizArray = new JSONArray();

        String[][] sampleQuestions = {
                {"Which is not part of system testing?", "Regression Testing", "Sanity Testing", "Load Testing", "Unit Testing", "4"}
        };

        for (String[] q : sampleQuestions) {
            JSONObject question = new JSONObject();
            question.put("question", q[0]);
            question.put("option 1", q[1]);
            question.put("option 2", q[2]);
            question.put("option 3", q[3]);
            question.put("option 4", q[4]);
            question.put("answerkey", Integer.parseInt(q[5]));
            quizArray.add(question);
        }

        FileWriter writer = new FileWriter(QUIZ_FILE);
        writer.write(quizArray.toJSONString());
        writer.flush();
        writer.close();
    }


    private static void startLogin() {
        while (true) {
            System.out.print("System:> Enter your username\nUser:> ");
            String username = scanner.nextLine().trim();

            System.out.print("System:> Enter password\nUser:> ");
            String password = scanner.nextLine().trim();

            String userRole = authenticateUser(username, password);

            if (userRole != null) {
                if (userRole.equals("admin")) {
                    System.out.println("System:> Welcome " + username + "! Please create new questions in the question bank.");
                    handleAdminFlow();
                } else if (userRole.equals("student")) {
                    System.out.println("System:> Welcome " + username + " to the quiz! We will throw you 10 questions. Each MCQ mark is 1 and no negative marking. Are you ready? Press 's' to start.");
                    handleStudentFlow();
                } else {
                    System.out.println("System:> Invalid role. Please contact administrator.");
                }
            } else {
                System.out.println("System:> Invalid username or password. Please try again.\n");
            }
        }
    }


    private static String authenticateUser(String username, String password) {
        try {
            FileReader reader = new FileReader(USERS_FILE);
            JSONArray users = (JSONArray) parser.parse(reader);
            reader.close();

            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                String storedUsername = (String) user.get("username");
                String storedPassword = (String) user.get("password");

                if (storedUsername.equals(username) && storedPassword.equals(password)) {
                    return (String) user.get("role");
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }

        return null;
    }


    private static void handleAdminFlow() {
        while (true) {
            System.out.println("\n=== Add New Question ===");

            System.out.print("System:> Input your question\nAdmin:> ");
            String question = scanner.nextLine().trim();

            if (question.isEmpty()) {
                System.out.println("System:> Question cannot be empty. Please try again.");
                continue;
            }

            System.out.print("System:> Input option 1:\nAdmin:> ");
            String option1 = scanner.nextLine().trim();

            System.out.print("System:> Input option 2:\nAdmin:> ");
            String option2 = scanner.nextLine().trim();

            System.out.print("System:> Input option 3:\nAdmin:> ");
            String option3 = scanner.nextLine().trim();

            System.out.print("System:> Input option 4:\nAdmin:> ");
            String option4 = scanner.nextLine().trim();

            int answerKey = 0;
            while (answerKey < 1 || answerKey > 4) {
                System.out.print("System:> What is the answer key? (1-4)\nAdmin:> ");
                try {
                    answerKey = Integer.parseInt(scanner.nextLine().trim());
                    if (answerKey < 1 || answerKey > 4) {
                        System.out.println("System:> Please enter a valid option number (1-4).");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("System:> Please enter a valid number (1-4).");
                }
            }

            if (saveQuestion(question, option1, option2, option3, option4, answerKey)) {
                System.out.println("System:> Saved successfully!");

                System.out.print("System:> Do you want to add more questions? (press 's' to start, 'q' to quit)\nAdmin:> ");
                String choice = scanner.nextLine().trim().toLowerCase();

                if (choice.equals("q")) {
                    System.out.println("System:> Thank you for adding questions. Goodbye!");
                    System.exit(0);
                } else if (!choice.equals("s")) {
                    System.out.println("System:> Invalid choice. Continuing to add questions...");
                }
            } else {
                System.out.println("System:> Error saving question. Please try again.");
            }
        }
    }


    private static boolean saveQuestion(String question, String option1, String option2, String option3, String option4, int answerKey) {
        try {

            JSONArray questions = readQuestions();

            JSONObject newQuestion = new JSONObject();
            newQuestion.put("question", question);
            newQuestion.put("option 1", option1);
            newQuestion.put("option 2", option2);
            newQuestion.put("option 3", option3);
            newQuestion.put("option 4", option4);
            newQuestion.put("answerkey", answerKey);

            questions.add(newQuestion);

            FileWriter writer = new FileWriter(QUIZ_FILE);
            writer.write(questions.toJSONString());
            writer.flush();
            writer.close();

            return true;

        } catch (IOException e) {
            System.err.println("Error saving question: " + e.getMessage());
            return false;
        }
    }

    private static JSONArray readQuestions() {
        try {
            FileReader reader = new FileReader(QUIZ_FILE);
            JSONArray questions = (JSONArray) parser.parse(reader);
            reader.close();
            return questions;

        } catch (IOException | ParseException e) {
            System.err.println("Error reading quiz file: " + e.getMessage());
            return new JSONArray(); // Return empty array if error
        }
    }

    private static void handleStudentFlow() {
        while (true) {
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("s")) {

                JSONArray questions = readQuestions();

                if (questions.size() < 1) {
                    System.out.println("System:> No questions available in the question bank. Please contact admin.");
                    return;
                }

                int score = conductQuiz(questions);

                showResults(score);

                System.out.print("System:> Would you like to start again? Press 's' for start or 'q' for quit\nStudent:> ");

            } else if (choice.equals("q")) {
                System.out.println("System:> Thank you for using the quiz system. Goodbye!");
                System.exit(0);
            } else {
                System.out.print("System:> Invalid choice. Press 's' to start or 'q' to quit\nStudent:> ");
            }
        }
    }

    /**
     * Conduct the quiz with 10 random questions
     */
    private static int conductQuiz(JSONArray questions) {
        System.out.println("\n=== Starting Quiz ===");
        System.out.println("Instructions: Answer each question by entering the option number (1-4)");
        System.out.println("Total Questions: 10 | Marks per question: 1 | No negative marking\n");

        Random random = new Random();
        int score = 0;

        for (int i = 1; i <= 10; i++) {
            int randomIndex = random.nextInt(questions.size());
            JSONObject question = (JSONObject) questions.get(randomIndex);

            System.out.println("System:> [Question " + i + "] " + question.get("question"));
            System.out.println("1. " + question.get("option 1"));
            System.out.println("2. " + question.get("option 2"));
            System.out.println("3. " + question.get("option 3"));
            System.out.println("4. " + question.get("option 4"));

            int studentAnswer = 0;
            boolean validAnswer = false;

            while (!validAnswer) {
                System.out.print("Student:> ");
                try {
                    studentAnswer = Integer.parseInt(scanner.nextLine().trim());
                    if (studentAnswer >= 1 && studentAnswer <= 4) {
                        validAnswer = true;
                    } else {
                        System.out.println("System:> Please enter a valid option number (1-4).");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("System:> Please enter a valid number (1-4).");
                }
            }

            int correctAnswer = ((Long) question.get("answerkey")).intValue();
            if (studentAnswer == correctAnswer) {
                score++;
            }

            System.out.println();
        }

        return score;
    }


    private static void showResults(int score) {
        System.out.println("\n=== Quiz Results ===");
        System.out.println("Your Score: " + score + " out of 10");

        String message;
        if (score >= 8 && score <= 10) {
            message = "Excellent! You have got " + score + " out of 10";
        } else if (score >= 5 && score < 8) {
            message = "Good. You have got " + score + " out of 10";
        } else if (score >= 3 && score < 5) {
            message = "Very poor! You have got " + score + " out of 10";
        } else {
            message = "Very sorry you are failed. You have got " + score + " out of 10";
        }

        System.out.println("System:> " + message);
        System.out.println("Percentage: " + (score * 10) + "%");
        System.out.println("====================\n");
    }
}