package com.example.module03_basicgui_db_interface;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

/**
 * Controller for the Splash Screen (splash_screen.fxml).
 *
 * The splash screen is displayed briefly on application startup while the
 * background thread can warm up (e.g., DB driver loading). After 2 seconds
 * DB_Application automatically transitions to the Login screen via a fade
 * animation.
 *
 * Clicking the logo skips the auto-transition and jumps to the Login screen
 * immediately — useful during development or for impatient users.
 */
public class SplashScreenController {

    /**
     * Handles a click on the splash screen image (SUNY Farmingdale logo).
     * Replaces the current scene root with the Login screen, bypassing the
     * automatic 2-second countdown.
     *
     * @param event The mouse click event triggered by clicking the ImageView
     */
    @FXML
    void clickme(MouseEvent event) {
        // Navigate to the login page instead of the old shortcut to main GUI
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("login.fxml"));
            // setRoot() reuses the existing Stage so no new window is opened
            ((ImageView) event.getSource()).getParent().getScene().setRoot(loginRoot);
        } catch (IOException e) {
            System.err.println("SplashScreenController: could not load login.fxml — " + e.getMessage());
            e.printStackTrace();
        }
    }
}
