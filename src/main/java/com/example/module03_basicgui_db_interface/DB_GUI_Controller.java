package com.example.module03_basicgui_db_interface;

import com.example.module03_basicgui_db_interface.database.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the main database interface (db_interface_gui.fxml).
 *
 * Responsibilities:
 *  - Initialize the TableView with Person data from the database
 *  - Handle Add, Edit, Delete, Clear CRUD operations
 *  - Validate user input before writing to the database
 *  - Toggle between dark and light themes at runtime (no restart needed)
 *  - Open a profile picture via FileChooser
 *  - Display status messages and error alerts to the user
 */
public class DB_GUI_Controller implements Initializable {

    // ── Observable list backing the TableView ──────────────────────────────────
    // JavaFX automatically refreshes the table whenever this list changes.
    private final ObservableList<Person> data = FXCollections.observableArrayList();

    // ── Database access layer ──────────────────────────────────────────────────
    private DatabaseHandler db;

    // ── Theme state ───────────────────────────────────────────────────────────
    // Tracks whether the dark theme is currently applied so toggleTheme() knows
    // which direction to switch.
    private boolean isDarkTheme = false;

    // ── FXML-injected fields ───────────────────────────────────────────────────

    /** Root pane — used to add/remove the 'dark-theme' CSS class for live theming */
    @FXML private AnchorPane rootPane;

    /** Status bar label shown at the bottom of the window */
    @FXML private Label statusLabel;

    /** Form input fields */
    @FXML TextField first_name, last_name, department, major;

    /** The main data table */
    @FXML private TableView<Person> tv;

    /** Table columns — mapped to Person properties via PropertyValueFactory */
    @FXML private TableColumn<Person, Integer> tv_id;
    @FXML private TableColumn<Person, String>  tv_fn, tv_ln, tv_dept, tv_major;

    /** Profile picture viewer; clicking it opens a FileChooser */
    @FXML private ImageView img_view;

    // ──────────────────────────────────────────────────────────────────────────
    // Initialization
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Called automatically by JavaFX after the FXML file has been loaded.
     * Wires table columns to Person model properties, connects to the database,
     * and populates the table with existing records.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Bind each column to the corresponding Person getter via property name
        tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tv_dept.setCellValueFactory(new PropertyValueFactory<>("dept"));
        tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));

        // Connect to the Azure MySQL database and create the table if needed
        db = new DatabaseHandler();
        boolean hasExistingUsers = db.connectToDatabase();

        // Load all records and display them in the table
        data.setAll(db.listAllUsers());
        tv.setItems(data);

        // Update the status bar with a connection summary
        setStatus(hasExistingUsers
                ? "Connected. " + data.size() + " record(s) loaded."
                : "Connected. No records found — add the first one!");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CRUD Operations
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Reads the form fields and inserts a new Person record into the database.
     * Validates that First Name and Last Name are not blank before inserting.
     * After a successful insert, re-queries the database to obtain the
     * auto-generated ID and adds the record to the observable list.
     */
    @FXML
    protected void addNewRecord() {
        if (!validateInput()) return;   // Stop early if validation fails

        String fn   = first_name.getText().trim();
        String ln   = last_name.getText().trim();
        String dept = department.getText().trim();
        String maj  = major.getText().trim();

        if (db.insertUser(fn, ln, dept, maj)) {
            // Re-query to get the auto-generated primary key
            Person inserted = db.queryUserByName(fn);
            if (inserted != null) {
                data.add(inserted);
                clearForm();
                setStatus("Added: " + fn + " " + ln);
            }
        } else {
            // Most likely cause: duplicate last name (UNIQUE constraint on last_name)
            showAlert(Alert.AlertType.ERROR, "Insert Failed",
                    "Could not add the record.\n"
                    + "A person with that last name may already exist in the database.");
        }
    }

    /**
     * Updates the selected table row with the current form field values.
     *
     * Bug fixed here: the original code passed the OLD Person values (before the
     * user's edits) to db.updateUser(). It now correctly passes the form values.
     *
     * A row must be selected; shows a warning if none is selected.
     */
    @FXML
    protected void editRecord() {
        Person selected = tv.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a row to edit.");
            return;
        }
        if (!validateInput()) return;

        // Build an updated Person from the form, keeping the original ID
        Person updated = new Person();
        updated.setId(selected.getId());
        updated.setFirstName(first_name.getText().trim());
        updated.setLastName(last_name.getText().trim());
        updated.setDept(department.getText().trim());
        updated.setMajor(major.getText().trim());

        // Persist the change — pass the form values, NOT the stale 'selected' values
        if (db.updateUser(selected.getId(),
                updated.getFirstName(), updated.getLastName(),
                updated.getDept(),     updated.getMajor())) {

            // Replace the item in the list so the TableView refreshes immediately
            // (editing an object in-place does NOT trigger a refresh)
            int index = data.indexOf(selected);
            data.set(index, updated);
            tv.getSelectionModel().select(index);
            setStatus("Updated: " + updated.getFirstName() + " " + updated.getLastName());

        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed",
                    "Could not update the record. The new last name may already be in use.");
        }
    }

    /**
     * Deletes the selected person record from the database and removes it from the table.
     * Shows a confirmation dialog before performing the irreversible delete.
     */
    @FXML
    protected void deleteRecord() {
        Person selected = tv.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a row to delete.");
            return;
        }

        // Ask the user to confirm before deleting
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Record");
        confirm.setContentText("Delete " + selected.getFirstName() + " " + selected.getLastName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (db.removeUser(selected.getId())) {
                data.remove(selected);
                clearForm();
                setStatus("Deleted: " + selected.getFirstName() + " " + selected.getLastName());
            } else {
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete the record.");
            }
        }
    }

    /**
     * Clears all form input fields and deselects any table row.
     */
    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        major.clear();
        tv.getSelectionModel().clearSelection();
        setStatus("Form cleared.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Profile Image
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Opens a FileChooser so the user can select a profile picture.
     * Supports PNG, JPG/JPEG, and GIF formats.
     * Bound to both the profile ImageView click and File → Open Image menu item.
     */
    @FXML
    protected void showImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = chooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            setStatus("Profile picture updated.");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Table Selection
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Populates the form fields from the row that was clicked in the TableView.
     * Guard clause prevents a NullPointerException if the click lands on an
     * empty row (which can happen when the table has fewer rows than its height).
     *
     * @param mouseEvent The mouse click event from the TableView
     */
    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person selected = tv.getSelectionModel().getSelectedItem();
        if (selected == null) return;   // Empty-row click — nothing to do

        first_name.setText(selected.getFirstName());
        last_name.setText(selected.getLastName());
        department.setText(selected.getDept());
        major.setText(selected.getMajor());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Theme Toggle
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Switches between the light and dark UI themes without restarting.
     *
     * Implementation: the CSS file defines a '.dark-theme' selector that overrides
     * colors for all child elements. Adding that class to the root AnchorPane
     * causes JavaFX to re-apply CSS immediately — no scene reload needed.
     *
     * Bound to: Settings → Toggle Dark/Light Theme (Ctrl+T)
     */
    @FXML
    protected void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            rootPane.getStyleClass().add("dark-theme");
            setStatus("Dark theme enabled.");
        } else {
            rootPane.getStyleClass().remove("dark-theme");
            setStatus("Light theme enabled.");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Menu Handlers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Exits the application cleanly.
     * Bound to: File → Close (Ctrl+Q)
     */
    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    /**
     * Shows an About dialog with application version and course information.
     * Bound to: Help → About
     */
    @FXML
    protected void showAboutDialog() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Person Database Manager  v1.0");
        about.setContentText(
                "CSC 311 — Module 3 Lab\n"
                + "JavaFX + Azure MySQL (csc311charlie)\n\n"
                + "Manage person records with full CRUD support,\n"
                + "live theme switching, and profile picture upload.");
        about.showAndWait();
    }

    /**
     * Shows the Help dialog documenting integration challenges and solutions.
     * Bound to: Help → Challenges & Solutions
     */
    @FXML
    protected void showHelpDialog() {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle("Help — Challenges & Solutions");
        help.setHeaderText("Development Challenges Encountered");
        help.setContentText(
                "1. Azure MySQL SSL Requirement\n"
                + "   Problem:  Plain JDBC URL refused connection.\n"
                + "   Solution: Appended '?useSSL=true' to DB_URL.\n\n"

                + "2. Java Module System (module-info.java)\n"
                + "   Problem:  'requires java.sql' was missing; JDBC classes inaccessible.\n"
                + "   Solution: Added the directive to module-info.java.\n\n"

                + "3. editRecord() Used Stale Values\n"
                + "   Problem:  The original code passed the OLD Person object's fields\n"
                + "             to db.updateUser() instead of the form field values.\n"
                + "   Solution: Extract values from the TextFields before calling updateUser().\n\n"

                + "4. TableView Doesn't Auto-Refresh on Edit\n"
                + "   Problem:  Modifying an object in the list does not trigger a visual update.\n"
                + "   Solution: Use data.set(index, updatedPerson) to replace the item in place.\n\n"

                + "5. NullPointerException on Empty-Row Click\n"
                + "   Problem:  Clicking an empty table row returns null from getSelectedItem().\n"
                + "   Solution: Added null guard at the top of selectedItemTV().\n\n"

                + "6. Live Theme Switching Without Restart\n"
                + "   Problem:  Changing CSS usually requires reloading the scene.\n"
                + "   Solution: Toggle the 'dark-theme' style class on the root AnchorPane;\n"
                + "             JavaFX re-applies CSS immediately.\n\n"

                + "7. Duplicate MySQL Connector Dependency\n"
                + "   Problem:  pom.xml listed mysql-connector-j twice, causing build warnings.\n"
                + "   Solution: Removed the duplicate <dependency> entry."
        );
        help.setResizable(true);
        help.getDialogPane().setPrefWidth(560);
        help.showAndWait();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Validates the required form fields (First Name and Last Name).
     * Focuses the offending field and shows a warning alert if validation fails.
     *
     * @return true if all required fields are filled; false otherwise
     */
    private boolean validateInput() {
        if (first_name.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "First Name cannot be empty.");
            first_name.requestFocus();
            return false;
        }
        if (last_name.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Last Name cannot be empty.");
            last_name.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Displays a modal alert dialog to the user.
     *
     * @param type    The severity/type of the alert (INFORMATION, WARNING, ERROR, etc.)
     * @param title   Window title of the dialog
     * @param message The body text shown in the dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Updates the status bar label at the bottom of the window.
     * Safely does nothing if the label was not injected (e.g., during unit tests).
     *
     * @param message The message to display
     */
    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
