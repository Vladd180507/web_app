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
            // 1. Знаходимо активне вікно, щоб знати, де показувати
            Window activeWindow = Window.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(w -> w instanceof Stage) // Беремо тільки Stage
                    .findFirst()
                    .orElse(null);

            if (activeWindow == null) return;

            // 2. Створюємо контейнер
            VBox root = new VBox(5); // 5px відступ між заголовком і текстом
            root.getStylesheets().add(App.class.getResource("/css/notification.css").toExternalForm());
            root.getStyleClass().add("notification-box");
            root.setAlignment(Pos.CENTER_LEFT);

            // 3. Заголовок
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("notification-title");

            // 4. Текст
            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true); // Переносити довгий текст
            messageLabel.setMaxWidth(250);  // Макс ширина

            root.getChildren().addAll(titleLabel, messageLabel);

            // 5. Створюємо Popup
            Popup popup = new Popup();
            popup.getContent().add(root);
            popup.setAutoFix(true);
            popup.setAutoHide(true); // Закривати при кліку повз

            // 6. Показуємо в правому нижньому куті вікна
            popup.show(activeWindow);

            // Вираховуємо координати (правий нижній кут активного вікна)
            double x = activeWindow.getX() + activeWindow.getWidth() - 300;
            double y = activeWindow.getY() + activeWindow.getHeight() - 100;
            popup.setX(x);
            popup.setY(y);

            // 7. Автоматично ховаємо через 4 секунди
            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(e -> popup.hide());
            delay.play();
        });
    }
}