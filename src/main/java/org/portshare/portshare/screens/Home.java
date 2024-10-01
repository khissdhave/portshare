package org.portshare.portshare.screens;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;

public class Home {

    private static final int PORT = 8000;
    private HttpServer server; // Store server instance to stop it later

    private File selectedFile;

    @FXML
    private Button fileChooserButton;

    @FXML
    private Label selectedFileNameAndPath;

    @FXML
    private Button shareButton;

    @FXML
    private Label wlanAddressLabel;

    @FXML
    private Label addressSuccess;

    @FXML
    private Label errorMessage1;

    @FXML
    private Label errorMessage2;

    @FXML
    private Button refreshButton;

    @FXML
    public void initialize() throws SocketException {
        addressSuccess.setVisible(false);
        errorMessage1.setVisible(false);
        errorMessage2.setVisible(false);
        shareButton.setVisible(false); // Hide the share button until a file is selected

        fileChooserButton.setOnAction(event -> openFileChooser());
        refreshButton.setOnAction(event -> {
            try {
                detectWlanAddress();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        });

        // Initial WLAN detection when the application starts
        detectWlanAddress();
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            selectedFileNameAndPath.setText(selectedFile.getAbsolutePath());
            shareButton.setVisible(true); // Make the share button visible after file is selected
        }
    }

    @FXML
    public void onShareButtonClick() {
        if (selectedFile != null) {
            try {
                String wlanIp = wlanAddressLabel.getText(); // Get WLAN IP address from label
                if (!wlanIp.isEmpty()) {
                    startSharing(selectedFile, wlanIp);
                } else {
                    errorMessage1.setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void detectWlanAddress() throws SocketException {
        String wlanAddress = getWlanAddress();
        wlanAddressLabel.setText(wlanAddress);

        if (wlanAddress != null && !wlanAddress.isEmpty()) {
            addressSuccess.setVisible(true);
            errorMessage1.setVisible(false);
            errorMessage2.setVisible(false);
        } else {
            addressSuccess.setVisible(false);
            errorMessage1.setVisible(true);
            errorMessage2.setVisible(true);
        }
    }

    private static String getWlanAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
            if (networkInterface.isUp() && !networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                    if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().contains(".")) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        return null;
    }

    // Start the file sharing service
    private void startSharing(File file, String wlanIp) throws Exception {
        // Generate QR code for file sharing
        generateQRCode(wlanIp, PORT);

        // Start HTTP server
        server = HttpServer.create(new InetSocketAddress(PORT), 0); // Assign to the class-level variable
        server.createContext("/", new FileHandler(file));
        server.setExecutor(null); // Use default executor
        System.out.println("Serving at port " + PORT);
        server.start();

        // Log the sharing history
        History.logHistory(file.getName(), wlanIp, file.length());

        // Show QR code pop-up window
        showQRCodePopup();
    }

    // QR Code generation
    private void generateQRCode(String ip, int port) throws WriterException, IOException {
        String url = "http://" + ip + ":" + port + "/" + selectedFile.getName();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200);

        Path path = FileSystems.getDefault().getPath("qrcode.png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        System.out.println("QR Code generated at: qrcode.png");
    }

    // Pop-up window to display QR code and handle the "Done" button
    private void showQRCodePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with other windows
        popupStage.setTitle("QR Code");

        // Create an ImageView to display the generated QR code
        ImageView qrCodeView = new ImageView();
        File qrCodeFile = new File("qrcode.png");
        if (qrCodeFile.exists()) {
            Image qrCodeImage = new Image(qrCodeFile.toURI().toString());
            qrCodeView.setImage(qrCodeImage);
            qrCodeView.setFitWidth(200); // Set the size of the QR code image
            qrCodeView.setFitHeight(200);
        }



        // Create a "Done" button
        Button doneButton = new Button("Done");
        doneButton.setOnAction(event -> {
            stopServer();   // Stop the server when the "Done" button is clicked
            deleteQRCodeFile();  // Delete the QR code image
            popupStage.close();  // Close the pop-up window
        });

        // Set up what happens when the pop-up window is closed
        popupStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to prevent default close behavior
            stopServer();    // Stop the server
            deleteQRCodeFile(); // Delete the QR code image
            popupStage.close(); // Close the pop-up window
        });


        // Create a VBox layout for the QR code and button
        VBox layout = new VBox(10, qrCodeView, doneButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Set the scene for the pop-up window
        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.show();
    }

    // Method to stop the HTTP server
    private void stopServer() {
        if (server != null) {
            server.stop(0); // Stop the server immediately
            System.out.println("Server stopped.");
        }
    }

    // Method to delete the generated QR code file
    private void deleteQRCodeFile() {
        File qrCodeFile = new File("qrcode.png");
        if (qrCodeFile.exists()) {
            boolean deleted = qrCodeFile.delete();
            if (deleted) {
                System.out.println("QR Code file deleted.");
            } else {
                System.out.println("Failed to delete QR Code file.");
            }
        }
    }


    // Handler for serving the file over HTTP
    static class FileHandler implements HttpHandler {
        private final File file;

        public FileHandler(File file) {
            this.file = file;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            exchange.sendResponseHeaders(200, file.length());

            OutputStream os = exchange.getResponseBody();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            os.close();
        }
    }
}
