package com.proj.frontend.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.proj.frontend.model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import com.proj.frontend.model.ActivityLog;

public class ApiBackendService implements BackendService {

    // ⚠️ якщо бекенд слухає на іншому порту / префіксі – змінюй тут
    private static final String BASE_URL = "http://localhost:8080/api";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // JWT токен, який отримаємо після логіну
    private String authToken;

    // ---- допоміжні методи ----

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path));
        if (authToken != null && !authToken.isBlank()) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }

    private String doGet(String path) throws IOException, InterruptedException {
        HttpRequest req = requestBuilder(path)
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new RuntimeException("GET " + path + " failed: " + resp.statusCode() + " " + resp.body());
    }

    private String doPost(String path, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest req = requestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new RuntimeException("POST " + path + " failed: " + resp.statusCode() + " " + resp.body());
    }

    private String doPut(String path, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest req = requestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new RuntimeException("PUT " + path + " failed: " + resp.statusCode() + " " + resp.body());
    }

    private String doPatch(String path, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest req = requestBuilder(path)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new RuntimeException("PATCH " + path + " failed: " + resp.statusCode() + " " + resp.body());
    }

    // ---- Реалізація BackendService ----

    @Override
    public User login(String email, String password) throws Exception {
        // бекенд: POST /api/users/login → повертає JWT-строку
        Map<String, String> body = Map.of(
                "email", email,
                "password", password
        );

        String respBody = doPost("/users/login", body);

        // може прийти просто текст або "token" в лапках – прибираємо лапки
        authToken = respBody.replace("\"", "").trim();

        // імені користувача бекенд тут не повертає – ставимо email як name
        return new User(null, email, email);
    }

    @Override
    public User register(String name, String email, String password) throws Exception {
        Map<String, String> body = Map.of(
                "name", name,
                "email", email,
                "password", password
        );

        String respBody = doPost("/users/register", body);
        // UserDto на бекенді сумісний з нашим User: id, name, email – решту полів Gson проігнорує
        return gson.fromJson(respBody, User.class);
    }

    @Override
    public List<Group> getGroups() throws Exception {
        String respBody = doGet("/groups");
        Type listType = new TypeToken<List<Group>>(){}.getType();
        return gson.fromJson(respBody, listType);
    }

    @Override
    public Group createGroup(String name, String description) throws Exception {
        // GroupController.create очікує { name, description, createdBy }
        // Для простоти поки що createdBy = 1L
        Map<String, Object> body = Map.of(
                "name", name,
                "description", description,
                "createdBy", 1L
        );

        String respBody = doPost("/groups", body);
        return gson.fromJson(respBody, Group.class);
    }

    @Override
    public List<Task> getTasksByGroup(long groupId) throws Exception {
        String respBody = doGet("/tasks/group/" + groupId);
        Type listType = new TypeToken<List<Task>>(){}.getType();
        return gson.fromJson(respBody, listType);
    }

    @Override
    public Task createTask(long groupId, String title, String description) throws Exception {
        // TaskController.CreateTaskRequest: title, description, deadline, creatorName
        Map<String, Object> body = Map.of(
                "title", title,
                "description", description,
                "deadline", null,          // поки без дедлайну
                "creatorName", "FrontendUser"
        );

        String respBody = doPost("/tasks/group/" + groupId, body);
        return gson.fromJson(respBody, Task.class);
    }

    @Override
    public Task updateTaskStatus(long taskId, String status) throws Exception {
        Map<String, Object> body = Map.of(
                "status", status
        );
        String respBody = doPatch("/tasks/" + taskId + "/status", body);
        return gson.fromJson(respBody, Task.class);
    }

    @Override
    public List<Resource> getResourcesByGroup(long groupId) throws Exception {
        String respBody = doGet("/resources/group/" + groupId);
        Type listType = new TypeToken<List<Resource>>(){}.getType();
        return gson.fromJson(respBody, listType);
    }

    @Override
    public Resource createResource(long groupId, String title, String url) throws Exception {
        // ResourceDTO на бекенді має приблизно ті ж поля, що й наш Resource
        Resource dto = new Resource(null, groupId, title, "LINK", url);
        String respBody = doPost("/resources", dto);
        return gson.fromJson(respBody, Resource.class);
    }

    @Override
    public List<ActivityLog> getActivityLogs() throws Exception {
        return List.of();
    }
}