# Library DB Project

## Setup

1. Install MySQL
2. Create database:
   CREATE DATABASE librarydb;

3. Use database:
   USE librarydb;

4. Create tables:

CREATE TABLE Authors (
    AuthorID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(50) NOT NULL
);

CREATE TABLE Books (
    BookID INT PRIMARY KEY AUTO_INCREMENT,
    Title VARCHAR(50) NOT NULL,
    AuthorID INT NOT NULL,
    YearPublished YEAR NOT NULL,
    FOREIGN KEY (AuthorID) REFERENCES Authors(AuthorID)
);

5. Create user:
   username: scott
   password: tiger

## Run Project

Run using Maven:

javafx:run
