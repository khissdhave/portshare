package org.portshare.portshare.screens;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class History {

    private static final String HISTORY_FILE = "history.csv";

    // Method to log history into the CSV file
    public static void logHistory(String fileName, String ipAddress, long fileSize) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String historyEntry = String.format("%s,%s,%d,%s", fileName, ipAddress, fileSize, timeStamp);
            writer.write(historyEntry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load the history from the CSV file as a list of strings
    public static List<String> loadHistory() {
        List<String> historyList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                historyList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    // Method to clear history (optional)
    public static void clearHistory() {
        try {
            new FileWriter(HISTORY_FILE, false).close();  // Overwrite the file with an empty one
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
