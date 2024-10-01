package org.portshare.portshare;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class BaseAppController {

    public Pane footer;
    @FXML
    private VBox sideNavbar; // Side navbar for navigation

    @FXML
    private Pane mainSection; // Main section where scenes will switch

    private Button activeButton; // Track the active button

    private void loadScene(String fxmlFile, Button selectedButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Pane newPane = loader.load(); // load the new pane
            mainSection.getChildren().clear(); // Clear the current scene
            mainSection.getChildren().add(newPane); // Add the new scene

            // Update the active button's style
            updateActiveButton(selectedButton);  // Make sure this is called!
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateActiveButton(Button clickedButton) {
        if (activeButton != null) {
            // Reset the style for the previous active button
            activeButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        }
        // Set the style for the clicked button
        clickedButton.setStyle("-fx-background-color: #DD8F00; -fx-border-color: white; -fx-border-width: 2px; -fx-border-radius: 10px;"); // Example border color and width
        activeButton = clickedButton; // Set the clicked button as the current active button
    }



    @FXML
    public void initialize() {
        // Load the home scene by default on app start
        loadScene("/org/portshare/portshare/screens/home.fxml", (Button) sideNavbar.lookup("#homeButton"));
    }

    // Handler for history button
    @FXML
    public void handleHomeButton() {
        loadScene("/org/portshare/portshare/screens/home.fxml", (Button) sideNavbar.lookup("#homeButton"));
    }

    // Handler for history button
    @FXML
    public void handleHistoryButton() {
        loadScene("/org/portshare/portshare/screens/history.fxml", (Button) sideNavbar.lookup("#historyButton"));
    }

    // Handler for settings button
    @FXML
    public void handleSettingsButton() {
        loadScene("/org/portshare/portshare/screens/settings.fxml", (Button) sideNavbar.lookup("#settingsButton"));
    }

    // Handler for help button
    @FXML
    public void handleHelpButton() {
        loadScene("/org/portshare/portshare/screens/help.fxml", (Button) sideNavbar.lookup("#helpButton"));
    }

    // Handler for info button
    @FXML
    public void handleInfoButton() {
        loadScene("/org/portshare/portshare/screens/info.fxml", (Button) sideNavbar.lookup("#infoButton"));
    }
}
