module org.portshare.portshare {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires jdk.httpserver;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    opens org.portshare.portshare to javafx.fxml;
    exports org.portshare.portshare;

    opens org.portshare.portshare.screens to javafx.fxml; // Add this line
    exports org.portshare.portshare.screens to javafx.fxml;
}