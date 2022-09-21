module com.example.client_cloud {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.example.common;


    opens com.example.client_cloud to javafx.fxml;
    exports com.example.client_cloud;
}