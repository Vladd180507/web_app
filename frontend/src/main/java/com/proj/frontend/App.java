package com.proj.frontend;

import com.proj.frontend.controller.LoginController;
import com.proj.frontend.service.BackendService;
import com.proj.frontend.service.MockBackendService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private BackendService backendService = new MockBackendService();

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());

        // передаємо контролеру сервіс
        LoginController controller = loader.getController();
        controller.setBackendService(backendService);
        controller.setStage(stage);

        stage.setTitle("Collaborative Study Platform - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}