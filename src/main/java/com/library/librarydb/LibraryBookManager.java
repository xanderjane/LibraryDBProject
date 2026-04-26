package com.library.librarydb;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//* SO THE BIGGEST CHANGE IS THE LIBARY CLASS SINCE IT HOSTS ALL THE JAVAFX STUFF YOU NEED TO CREATE
// THEM AS PROPERITES FOR THEM TO BE LIKE DYNAMIC ON THE ACTUAL JAVAFX BUTTONS TEXT BOXES ETC
// THIS SHOULD ALL WORK THOUGH I HAVE 0 IDEA SINCE I CANT RUN THE PROGRAM LOL */
class Book {

    private IntegerProperty bookID;
    private StringProperty title;
    private StringProperty authorName;
    private IntegerProperty yearPublished;

    public Book(int bookID, String title, String authorName, int yearPublished) {
        this.bookID = new SimpleIntegerProperty(bookID);
        this.title = new SimpleStringProperty(title);
        this.authorName = new SimpleStringProperty(authorName);
        this.yearPublished = new SimpleIntegerProperty(yearPublished);
    }

    public int getBookID() {
        return bookID.get();
    }

    public IntegerProperty bookIDProperty() {
        return bookID;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getAuthorName() {
        return authorName.get();
    }

    public void setAuthorName(String authorName) {
        this.authorName.set(authorName);
    }

    public StringProperty authorNameProperty() {
        return authorName;
    }

    public int getYearPublished() {
        return yearPublished.get();
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished.set(yearPublished);
    }

    public IntegerProperty yearPublishedProperty() {
        return yearPublished;
    }
}

class Author {
    private IntegerProperty authorID;
    private StringProperty name;

    public Author(int authorID, String name) {
        this.authorID = new SimpleIntegerProperty(authorID);
        this.name = new SimpleStringProperty(name);
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getAuthorID() {
        return authorID.get();
    }

    public IntegerProperty authorIDProperty() {
        return authorID;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }
}

public class LibraryBookManager extends Application {

    TableView<Book> tableView = new TableView<>();

    TextField titleField = new TextField();
    TextField yearField = new TextField();
    ComboBox<Author> authorBox = new ComboBox<>();

    ObservableList<Book> bookList = FXCollections.observableArrayList();
    ObservableList<Author> authorList = FXCollections.observableArrayList();

    DatabaseManager db = new DatabaseManager();

    @Override
    public void start(Stage stage) {

        try {
            db.connect();

            TableColumn<Book, String> titleCol = new TableColumn<>("Title");
            titleCol.setCellValueFactory(data -> data.getValue().titleProperty());

            TableColumn<Book, String> authorCol = new TableColumn<>("Author");
            authorCol.setCellValueFactory(data -> data.getValue().authorNameProperty());

            TableColumn<Book, Integer> yearCol = new TableColumn<>("Year");
            yearCol.setCellValueFactory(data -> data.getValue().yearPublishedProperty().asObject());

            tableView.getColumns().addAll(titleCol, authorCol, yearCol);

            Button add = new Button("Add");
            Button update = new Button("Update");
            Button delete = new Button("Delete");
            Button refresh = new Button("Refresh");

            add.setOnAction(e -> {
                try {
                    Author a = authorBox.getValue();
                    db.addBook(titleField.getText(), a.getAuthorID(), Integer.parseInt(yearField.getText()));
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            update.setOnAction(e -> {
                try {
                    Book b = tableView.getSelectionModel().getSelectedItem();
                    Author a = authorBox.getValue();
                    db.updateBook(b.getBookID(), titleField.getText(), a.getAuthorID(), Integer.parseInt(yearField.getText()));
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            delete.setOnAction(e -> {
                try {
                    Book b = tableView.getSelectionModel().getSelectedItem();
                    db.deleteBook(b.getBookID());
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            refresh.setOnAction(e -> loadData());

            tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    titleField.setText(val.getTitle());
                    yearField.setText(String.valueOf(val.getYearPublished()));

                    for (Author a : authorList) {
                        if (a.getName().equals(val.getAuthorName())) {
                            authorBox.setValue(a);
                            break;
                        }
                    }
                }
            });

            HBox inputs = new HBox(10, titleField, authorBox, yearField, add, update, delete, refresh);
            VBox root = new VBox(10, tableView, inputs);

            stage.setScene(new Scene(root, 800, 500));
            stage.show();

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            bookList.setAll(db.getAllBooks());
            tableView.setItems(bookList);

            authorList.setAll(db.getAllAuthors());
            authorBox.setItems(authorList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

class DatabaseManager {
    // change db user name and pass to match prof requirements
    private static final String URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USER = "scott";
    private static final String PASSWORD = "tiger";

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Connection getConnection() {
        return connection;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> list = new ArrayList<>();

        String query = """
                SELECT Books.BookID, Books.Title, Authors.Name AS AuthorName, Books.YearPublished
                FROM Books
                JOIN Authors ON Books.AuthorID = Authors.AuthorID
                """;

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            list.add(new Book(
                    rs.getInt("BookID"),
                    rs.getString("Title"),
                    rs.getString("AuthorName"),
                    rs.getInt("YearPublished")
            ));
        }

        return list;
    }

    public List<Author> getAllAuthors() throws SQLException {
        List<Author> list = new ArrayList<>();

        String query = "SELECT * FROM Authors";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            list.add(new Author(
                    rs.getInt("AuthorID"),
                    rs.getString("Name")
            ));
        }

        return list;
    }

    public void close() throws SQLException {
        if (connection != null  && !connection.isClosed()) {
            connection.close();
        }
    }


    public void addBook(String title, int authorID, int year) throws SQLException {
        String sql = "INSERT INTO Books (Title, AuthorID, YearPublished) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, title);
        ps.setInt(2, authorID);
        ps.setInt(3, year);
        ps.executeUpdate();
    }

    public void updateBook(int id, String title, int authorID, int year) throws SQLException {
        String sql = "UPDATE Books SET Title=?, AuthorID=?, YearPublished=? WHERE BookID=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, title);
        ps.setInt(2, authorID);
        ps.setInt(3, year);
        ps.setInt(4, id);
        ps.executeUpdate();
    }

    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM Books WHERE BookID=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}