package com.proj.frontend.util;

import com.proj.frontend.App;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationUtil {

    private static final Set<String> recentNotifications = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void showNotification(String title, String message) {
        String key = title + ":" + message;

        if (!recentNotifications.add(key)) {
            return;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                recentNotifications.remove(key);
            }
        }, 3000);

        Platform.runLater(() -> {
            Window activeWindow = Window.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(w -> w instanceof Stage)
                    .findFirst()
                    .orElse(null);

            if (activeWindow == null) return;

            VBox root = new VBox(5);
            root.getStylesheets().add(App.class.getResource("/css/notification.css").toExternalForm());
            root.getStyleClass().add("notification-box");
            root.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("notification-title");

            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(250);

            root.getChildren().addAll(titleLabel, messageLabel);

            Popup popup = new Popup();
            popup.getContent().add(root);
            popup.setAutoFix(true);
            popup.setAutoHide(true);

            popup.show(activeWindow);

            // ✅ ЗМІНЕНО КООРДИНАТИ: Лівий нижній кут

            // X: Координата X вікна + 30 пікселів відступу зліва
            double x = activeWindow.getX() + 30;

            // Y: Координата Y вікна + Висота вікна - 100 пікселів (відступ знизу)
            double y = activeWindow.getY() + activeWindow.getHeight() - 100;

            popup.setX(x);
            popup.setY(y);

            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }
}