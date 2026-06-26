module org.example.music {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires jbcrypt;

    opens org.example.music to
            javafx.fxml,
            javafx.graphics,
            javafx.base,
            org.junit.platform.commons,
            org.junit.jupiter.api;

    exports org.example.music;
}