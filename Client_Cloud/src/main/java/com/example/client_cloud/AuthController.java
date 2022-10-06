package com.example.client_cloud;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.AuthMessage;
import org.example.model.RegistrationMessage;

import java.io.IOException;

public class AuthController {
    private FXMLLoader loader;

    private final Client client;
    public TextField login_in;
    public TextField password_in;
    public TextField signUP_field_login;
    public TextField signUP_field_password;

    private Stage stage;
    private Scene scene;
    private Parent root;

    private ActionEvent lastEvent;

    public AuthController() {
        client = new Client(this);
    }


    public void sign_in(ActionEvent event) throws IOException {
        lastEvent = event;
        switchScene("authView.fxml");
    }

    public void sign_up(ActionEvent event) throws IOException {
        lastEvent = event;
        switchScene("regView.fxml");
    }

    public void log_in(ActionEvent event) throws IOException {
        client.getNetwork().getOutputStream().writeObject(new AuthMessage(login_in.getText(), password_in.getText()));
        lastEvent = event;
    }

    public void signUP(ActionEvent actionEvent) throws IOException {
        client.getNetwork().getOutputStream().writeObject(new RegistrationMessage(signUP_field_login.getText(), signUP_field_password.getText()));
        lastEvent = actionEvent;
    }

    public void switchScene(String viewName) {
        loader = new FXMLLoader(getClass().getResource(viewName));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (loader.getController() instanceof CloudMainController) {
            client.setController(loader.getController());
            ((CloudMainController) loader.getController()).setClient(client);
        }
        stage = (Stage)((Node)lastEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void showError(String error) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, error, new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Ошибка!");
        alert.showAndWait();
    }

    public void backToStartView(ActionEvent event) {
        lastEvent = event;
        switchScene("hello-view.fxml");
    }
}
