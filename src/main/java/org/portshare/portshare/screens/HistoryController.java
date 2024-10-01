package org.portshare.portshare.screens;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.util.List;

public class HistoryController {

    @FXML
    private ListView<String> historyListView; // ListView to display the history

    @FXML
    private Button loadHistoryButton;  // Button to load history

    @FXML
    public void initialize() {
        // Load the history when the button is clicked
        loadHistory();
        loadHistoryButton.setOnAction(event -> loadHistory());
    }

    // Method to load history and display it in the ListView
    private void loadHistory() {
        // Load history from the History class
        List<String> history = History.loadHistory();

        // Display history in the ListView
        historyListView.getItems().setAll(history);
    }
}
