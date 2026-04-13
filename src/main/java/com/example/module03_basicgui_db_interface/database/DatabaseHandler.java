package com.example.module03_basicgui_db_interface.database;

import com.example.module03_basicgui_db_interface.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for the Person Database application.
 *
 * Connects to an Azure MySQL database and performs full CRUD operations
 * on the 'person' table. Each method opens and closes its own connection
 * to keep usage simple for a single-user desktop application.
 *
 * Challenge encountered: Azure MySQL requires SSL — solved by appending
 * "?useSSL=true" to the JDBC URL.
 */
public class DatabaseHandler {

    // Azure MySQL connection details
    private static final String DB_URL  = "jdbc:mysql://csc311charlie.mysql.database.azure.com:3306/mydb?useSSL=true";
    private static final String USERNAME = "deleteme";
    private static final String PASSWORD = "Pa55w0rd";

    /**
     * Connects to the database and creates the 'person' table if it does not exist.
     *
     * Challenge encountered: the original code queried a non-existent 'users' table
     * and made a redundant CREATE DATABASE call — both have been removed/fixed here.
     *
     * @return true if at least one user record already exists in the table; false otherwise
     */
    public boolean connectToDatabase() {
        boolean hasRegisteredUsers = false;

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             Statement statement = conn.createStatement()) {

            // Create the 'person' table only if it doesn't already exist
            String createTable = "CREATE TABLE IF NOT EXISTS person ("
                    + "id          INT(10)      NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "first_name  VARCHAR(200) NOT NULL,"
                    + "last_name   VARCHAR(200) NOT NULL UNIQUE,"
                    + "department  VARCHAR(200),"
                    + "major       VARCHAR(200))";
            statement.executeUpdate(createTable);

            // Check whether any users are already stored
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM person");
            if (rs.next()) {
                hasRegisteredUsers = rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }

        return hasRegisteredUsers;
    }

    /**
     * Searches for a person by their first name.
     *
     * Note: returns the first match only. If multiple people share the same
     * first name the result is non-deterministic — use the ID-based methods
     * for precise lookups.
     *
     * @param name The first name to search for
     * @return The matching Person, or null if none is found
     */
    public Person queryUserByName(String name) {
        String sql = "SELECT * FROM person WHERE first_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Person(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("department"),
                        rs.getString("major")
                );
            }

        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves every person record from the database.
     *
     * @return A list of all Person objects; empty list if the table is empty or an error occurs
     */
    public List<Person> listAllUsers() {
        List<Person> results = new ArrayList<>();
        String sql = "SELECT * FROM person";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(new Person(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("department"),
                        rs.getString("major")
                ));
                System.out.println(results.get(results.size()-1));
            }

        } catch (SQLException e) {
            System.err.println("List users error: " + e.getMessage());
            e.printStackTrace();
        };
        return results;
    }

    /**
     * Inserts a new person record into the database.
     *
     * Note: last_name has a UNIQUE constraint — inserting a duplicate last name
     * will return false and print the SQL error.
     *
     * @param firstName  Person's first name (required)
     * @param lastName   Person's last name  (required, must be unique)
     * @param department Person's department
     * @param major      Person's major
     * @return true if the row was inserted successfully; false otherwise
     */
    public boolean insertUser(String firstName, String lastName, String department, String major) {
        String sql = "INSERT INTO person (first_name, last_name, department, major) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, department);
            ps.setString(4, major);

            return ps.executeUpdate() > 0;  // true when a row was written

        } catch (SQLException e) {
            System.err.println("Insert error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing person record identified by its database ID.
     *
     * Challenge encountered: the original editRecord() method in the controller
     * passed the old field values instead of the updated form values to this method.
     * The fix is in DB_GUI_Controller — this method itself is correct.
     *
     * @param userId     The primary-key ID of the record to update
     * @param firstName  New first name
     * @param lastName   New last name (must still be unique)
     * @param department New department
     * @param major      New major
     * @return true if the row was updated successfully; false otherwise
     */
    public boolean updateUser(int userId, String firstName, String lastName,
                               String department, String major) {
        String sql = "UPDATE person SET first_name=?, last_name=?, department=?, major=? WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, department);
            ps.setString(4, major);
            ps.setInt(5, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a person record from the database by primary-key ID.
     *
     * @param userId The ID of the person to delete
     * @return true if the row was deleted; false otherwise
     */
    public boolean removeUser(int userId) {
        String sql = "DELETE FROM person WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
