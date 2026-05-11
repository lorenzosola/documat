package com.documat.client.controller;

import com.documat.client.model.AuthResponse;
import com.documat.client.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password", Alert.AlertType.ERROR);
            return;
        }

        try {
            AuthResponse response = ApiService.login(username, password);
            showAlert("Success", "Welcome " + response.getUsername() + "!", Alert.AlertType.INFORMATION);
            
            // Close login window
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
            
            // Here you would typically open the main application window
            // For now, we'll just close the login window
            
        } catch (Exception e) {
            showAlert("Error", "Login failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
