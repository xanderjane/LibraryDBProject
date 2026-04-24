package com.library.librarydb;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.*;

public class LibraryBookManager extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("App started");

        try {
            DatabaseManager db = new DatabaseManager();
            db.connect();

            if (db.getConnection() != null && !db.getConnection().isClosed()) {
                System.out.println("Connected to database SUCCESS");
            } else {
                System.out.println("Connection FAILED");
            }

            System.out.println("Current books:");
            db.testQuery();

            // TEST ADD BOOK - only run this once, then comment it out
            // db.addBook("Test Book", 1, 2022);
            // System.out.println("After adding:");
            // db.testQuery();

            // TEST UPDATE BOOK - change bookID if needed
            // db.updateBook(4, "Updated Test Book", 2, 2023);
            // System.out.println("After updating:");
            // db.testQuery();

            // TEST DELETE BOOK - change bookID if needed
            // db.deleteBook(4);
            // System.out.println("After deleting:");
            // db.testQuery();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Connection getConnection() {
        return connection;
    }

    public void testQuery() throws SQLException {
        String query = """
                SELECT
                    Books.BookID,
                    Books.Title,
                    Authors.Name AS AuthorName,
                    Books.YearPublished
                FROM Books
                JOIN Authors ON Books.AuthorID = Authors.AuthorID
                """;

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            System.out.println(
                    rs.getInt("BookID") + " - " +
                            rs.getString("Title") + " - " +
                            rs.getString("AuthorName") + " - " +
                            rs.getInt("YearPublished")
            );
        }
    }

    public void addBook(String title, int authorID, int yearPublished) throws SQLException {
        String query = "INSERT INTO Books (Title, AuthorID, YearPublished) VALUES (?, ?, ?)";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, title);
        stmt.setInt(2, authorID);
        stmt.setInt(3, yearPublished);

        stmt.executeUpdate();
    }

    public void updateBook(int bookID, String title, int authorID, int yearPublished) throws SQLException {
        String query = "UPDATE Books SET Title = ?, AuthorID = ?, YearPublished = ? WHERE BookID = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, title);
        stmt.setInt(2, authorID);
        stmt.setInt(3, yearPublished);
        stmt.setInt(4, bookID);

        stmt.executeUpdate();
    }

    public void deleteBook(int bookID) throws SQLException {
        String query = "DELETE FROM Books WHERE BookID = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, bookID);

        stmt.executeUpdate();
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}