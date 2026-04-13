package com.example.module03_basicgui_db_interface;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main JavaFX application entry point for Person Database Manager.
 *
 * Application flow:
 *   1. Splash screen (splash_screen.fxml) is shown immediately on startup.
 *   2. After a 2-second fade-out, the Login screen (login.fxml) fades in.
 *   3. From the Login screen, users can navigate to the Registration screen
 *      or proceed directly to the main database interface (db_interface_gui.fxml).
 *
 * Note: clicking the splash logo skips the timer (handled by SplashScreenController).
 */
public class DB_Application extends Application {

    // The primary application window — kept as a field so changeScene() can access it.
    private Stage primaryStage;

    // The fade-in transition for the Login screen.
    // Declared as a field so the lambda inside transitionToLogin() can capture it
    // after it has been assigned (assigning before starting fadeOut ensures no race).
    private FadeTransition fadeIn;

    /**
     * Application entry point — delegates to JavaFX's launch() mechanism.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Called by JavaFX after the application thread is initialised.
     * Sets up the primary Stage and displays the splash screen.
     *
     * @param primaryStage The root window provided by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setResizable(false);
        this.primaryStage.setTitle("Person Database Manager");
        showSplashScreen();
    }

    /**
     * Loads and displays the splash screen scene (splash_screen.fxml).
     * Immediately queues the automatic transition to the Login screen.
     */
    private void showSplashScreen() {
        try {
            Parent splashRoot = FXMLLoader.load(getClass().getResource("splash_screen.fxml"));
            Scene scene = new Scene(splashRoot, 850, 560);
            // Apply global stylesheet so the splash shares the same CSS
            scene.getStylesheets().add(getClass().getResource("styling/style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();

            // Begin the timed transition to the Login screen
            transitionToLogin();

        } catch (Exception e) {
            System.err.println("Error loading splash screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Animates the splash screen fading out, then fades in the Login screen.
     *
     * Timeline:
     *   0 s  — splash begins fading out
     *   2 s  — splash fully transparent; login scene is set on the Stage
     *   2 s  — login begins fading in
     *   4 s  — login fully visible
     *
     * Challenge encountered: assigning fadeIn AFTER calling fadeOut.play()
     * caused a NullPointerException inside the onFinished lambda on slow machines.
     * Fixed by creating fadeIn first, then starting fadeOut.
     */
    private void transitionToLogin() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("login.fxml"));
            loginRoot.setOpacity(0);  // Start invisible — fadeIn will bring it to 1

            // Prepare the fade-in BEFORE starting the fade-out so the lambda is safe
            fadeIn = new FadeTransition(Duration.seconds(2), loginRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Fade out the splash screen, then swap scenes and fade in login
            Parent splashRoot = primaryStage.getScene().getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), splashRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                // Replace the scene with the Login scene (same window, no flicker)
                Scene loginScene = new Scene(loginRoot, 850, 560);
                loginScene.getStylesheets().add(
                        getClass().getResource("styling/style.css").toExternalForm());
                primaryStage.setScene(loginScene);
                fadeIn.play();
            });

            fadeOut.play();

        } catch (Exception e) {
            System.err.println("Error transitioning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
