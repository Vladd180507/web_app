package com.proj.frontend;

import com.proj.frontend.controller.LoginController;
import com.proj.frontend.service.ApiBackendService;
import com.proj.frontend.service.BackendService;
import com.proj.frontend.service.MockBackendService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.proj.frontend.App;
import com.proj.frontend.controller.LoginController;


public class App extends Application {

    // можна швидко переключати тип бекенду
    private static final boolean USE_REAL_API = false;

    private BackendService backendService =
            USE_REAL_API ? new ApiBackendService() : new MockBackendService();

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 1150, 700);


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