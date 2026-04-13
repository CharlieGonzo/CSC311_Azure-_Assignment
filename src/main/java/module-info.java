/**
 * Module descriptor for Person Database Manager.
 *
 * Declares dependencies on:
 *   javafx.controls — UI controls (Button, TextField, TableView, Alert, etc.)
 *   javafx.fxml     — FXML loader (FXMLLoader, @FXML annotation)
 *   java.sql        — JDBC API (DriverManager, Connection, PreparedStatement, etc.)
 *
 * Opens the main package to javafx.fxml so the FXMLLoader can reflectively
 * instantiate controllers and inject @FXML-annotated fields at runtime.
 */
module com.example.module03_basicgui_db_interface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Allow JavaFX's FXML loader to access controller classes and @FXML fields
    opens com.example.module03_basicgui_db_interface to javafx.fxml;
    exports com.example.module03_basicgui_db_interface;
}
