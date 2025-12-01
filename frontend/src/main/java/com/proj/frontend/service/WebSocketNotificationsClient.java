package com.proj.frontend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Примітивний STOMP-клієнт для підписки на /topic/notifications.
 * Працює поверх Java 11+ HttpClient WebSocket API.
 */
public class WebSocketNotificationsClient implements WebSocket.Listener {

    // ⚠️ Якщо бекенд буде слухати на іншому порту / шляху – міняєте тут
    private static final String WS_URL = "ws://localhost:8080/ws/websocket";
    private static final String DESTINATION = "/topic/notifications";

    private final Consumer<String> onNotification;
    private WebSocket webSocket;
    private final StringBuilder buffer = new StringBuilder();

    public WebSocketNotificationsClient(Consumer<String> onNotification) {
        this.onNotification = onNotification;
    }

    public void connect() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            client.newWebSocketBuilder()
                    .buildAsync(URI.create(WS_URL), this)
                    .whenComplete((ws, error) -> {
                        if (error != null) {
                            System.err.println("WebSocket connection failed: " + error.getMessage());
                        } else {
                            this.webSocket = ws;
                            // як тільки підключилися, відправляємо STOMP CONNECT + SUBSCRIBE
                            sendConnectFrame();
                            sendSubscribeFrame();
                        }
                    });

        } catch (Exception e) {
            System.err.println("Cannot connect to WebSocket: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
        }
    }

    // ---- STOMP helper methods ----

    private void sendConnectFrame() {
        String frame =
                "CONNECT\n" +
                        "accept-version:1.1,1.2\n" +
                        "host:localhost\n" +
                        "heart-beat:0,0\n\n" +
                        "\0";
        sendFrame(frame);
    }

    private void sendSubscribeFrame() {
        String frame =
                "SUBSCRIBE\n" +
                        "id:sub-0\n" +
                        "destination:" + DESTINATION + "\n\n" +
                        "\0";
        sendFrame(frame);
    }

    private void sendFrame(String frame) {
        if (webSocket != null) {
            webSocket.sendText(frame, true);
        }
    }

    // ---- WebSocket.Listener implementation ----

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket opened");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket,
                                     CharSequence data,
                                     boolean last) {
        buffer.append(data);
        if (last) {
            String text = buffer.toString();
            buffer.setLength(0);
            processFrames(text);
        }
        webSocket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    private void processFrames(String text) {
        // STOMP фрейми розділяються символом \0
        String[] frames = text.split("\0");
        for (String frame : frames) {
            frame = frame.trim();
            if (frame.isEmpty()) continue;

            // цікавлять тільки MESSAGE-фрейми
            if (frame.startsWith("MESSAGE")) {
                int idx = frame.indexOf("\n\n");
                String body = (idx >= 0) ? frame.substring(idx + 2) : frame;
                String msg = body.trim();

                if (!msg.isEmpty() && onNotification != null) {
                    onNotification.accept(msg);
                }
            } else if (frame.startsWith("CONNECTED")) {
                System.out.println("STOMP connected");
            } else if (frame.startsWith("ERROR")) {
                System.err.println("STOMP error frame: " + frame);
            } else {
                // інші фрейми можна просто залогувати
                System.out.println("STOMP frame: " + frame);
            }
        }
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket,
                                       java.nio.ByteBuffer data,
                                       boolean last) {
        // нам не треба, працюємо тільки з текстом
        webSocket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket,
                                      int statusCode,
                                      String reason) {
        System.out.println("WebSocket closed: " + statusCode + " " + reason);
        return CompletableFuture.completedFuture(null);
    }
}