package com.proj.frontend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketNotificationsClient implements WebSocket.Listener {

    // ⚠️ Якщо ти запускаєш не на localhost, зміни IP тут
    private static final String WS_URL = "ws://localhost:8080/ws/websocket";
    private static final String TOPIC = "/topic/notifications";

    private final Consumer<String> onNotification;
    private WebSocket webSocket;

    public WebSocketNotificationsClient(Consumer<String> onNotification) {
        this.onNotification = onNotification;
    }

    public void connect() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                    .buildAsync(URI.create(WS_URL), this)
                    .join(); // Чекаємо підключення
        } catch (Exception e) {
            System.err.println("WebSocket connect failed: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        System.out.println(">>> WS Connected. Sending STOMP CONNECT...");

        // 1. Відправляємо STOMP CONNECT фрейм при відкритті
        String connectFrame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000";
        webSocket.sendText(connectFrame, true);
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();

        if (message.startsWith("CONNECTED")) {
            System.out.println(">>> STOMP Handshake success. Subscribing to " + TOPIC);

            // 2. ТІЛЬКИ ТУТ, коли сервер відповів "ОК", ми підписуємось
            String id = "sub-0";
            String subscribeFrame = "SUBSCRIBE\nid:" + id + "\ndestination:" + TOPIC + "\n\n\u0000";
            webSocket.sendText(subscribeFrame, true);

        } else if (message.startsWith("MESSAGE")) {
            // 3. Це повідомлення!
            int bodyStart = message.indexOf("\n\n");
            if (bodyStart != -1) {
                String body = message.substring(bodyStart + 2).replace("\u0000", "").trim();
                if (!body.isEmpty() && onNotification != null) {
                    onNotification.accept(body);
                }
            }
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket Error: " + error.getMessage());
    }
}