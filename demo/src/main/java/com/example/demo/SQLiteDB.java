package com.example.demo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDB {
    static String url = "jdbc:sqlite:database.db";
    public void addUser(String username, String password) {
        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(url);


        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e);
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e);
            e.printStackTrace();
        }
    }
}