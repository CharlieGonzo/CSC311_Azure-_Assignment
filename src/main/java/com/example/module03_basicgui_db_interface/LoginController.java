package com.example.module03_basicgui_db_interface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Controller for the Login screen (login.fxml).
 *
 * Note (per lab instructions): Login functionality does NOT need to be
 * operational for this lab — the UI pages simply need to be present.
 * Clicking "Login" navigates directly to the main database interface.
 * Clicking "Register" navigates to the registration screen.
 */
public class LoginController {

    /** Username input field */
    @FXML private TextField usernameField;

    /** Password input field (masked) */
    @FXML private PasswordField passwordField;

    /**
     * Button reference used to obtain the current Scene for navigation.
     * (Any FXML-injected node belonging to the scene would work equally well.)
     */
    @FXML private Button loginBtn;

    /**
     * Handles the "Login" button click.
     *
     * Per lab instructions, authentication is not enforced — any input (or no input)
     * proceeds to the main database GUI. In a real application, credentials would be
     * validated against the database before granting access.
     */
    @FXML
    protected void login() {
        navigateTo("db_interface_gui.fxml");
    }

    /**
     * Handles the "Register" link/button click.
     * Navigates to the registration screen so a new user can create an account.
     */
    @FXML
    protected void goToRegister() {
        navigateTo("register.fxml");
    }

    /**
     * Replaces the current scene's root with the specified FXML layout.
     * Using scene.setRoot() keeps the same Stage/window so no new window appears.
     *
     * @param fxmlFile The file name (relative to this class's package resource path)
     */
    private void navigateTo(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            loginBtn.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("LoginController: could not load " + fxmlFile + " — " + e.getMessage());
            e.printStackTrace();
        }
    }
}
