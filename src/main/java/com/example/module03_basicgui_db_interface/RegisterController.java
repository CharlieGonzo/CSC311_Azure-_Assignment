package com.example.module03_basicgui_db_interface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterController {

    // ── Form input fields ─────────────────────────────────────────────────────

    /** First and last name for display purposes */
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;

    /** Contact and login details */
    @FXML private TextField emailField;
    @FXML private TextField usernameField;

    /** Password (masked) and its confirmation */
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    /**
     * Button reference used to obtain the current Scene for navigation.
     * Any FXML-injected node belonging to the scene would work equally well.
     */
    @FXML private Button registerBtn;

    /**
     * Handles the "Create Account" button click.
     *
     * Performs a basic password-match check. If passwords do not match, a warning
     * is shown and the form stays open. Otherwise, a success dialog is displayed
     * and the user is redirected to the Login screen.
     *
     * No data is written to the database in this lab iteration.
     */
    @FXML
    protected void register() {
        // Ensure the two password fields match before "creating" the account
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            Alert mismatch = new Alert(Alert.AlertType.WARNING);
            mismatch.setTitle("Password Mismatch");
            mismatch.setHeaderText(null);
            mismatch.setContentText("Passwords do not match. Please try again.");
            mismatch.showAndWait();
            confirmPasswordField.clear();
            return;
        }

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Registration Successful");
        success.setHeaderText(null);
        success.setContentText("Account created for " + firstNameField.getText() + "!\n"
                + "You can now log in with your credentials.");
        success.showAndWait();

        // Return to the login screen
        goToLogin();
    }

    /**
     * Handles the "Back to Login" button/link click.
     * Navigates back to the login screen without creating an account.
     */
    @FXML
    protected void goToLogin() {
        navigateTo("login.fxml");
    }

    /**
     * Replaces the current scene's root with the specified FXML layout.
     *
     * @param fxmlFile The file name (relative to this class's package resource path)
     */
    private void navigateTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            registerBtn.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("RegisterController: could not load " + fxmlFile + " — " + e.getMessage());
            e.printStackTrace();
        }
    }
}
