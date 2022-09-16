module com.example.client_cloud {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.client_cloud to javafx.fxml;
    exports com.example.client_cloud;
}