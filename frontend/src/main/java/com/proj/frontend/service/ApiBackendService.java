package com.proj.frontend.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.proj.frontend.model.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiBackendService implements BackendService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client;
    private final Gson gson;

    private String jwtToken;
    private User currentUser;

    public ApiBackendService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // ================= AUTH =================

    @Override
    public User login(String email, String password) throws Exception {
        System.out.println(">>> START LOGIN for: " + email);

        Map<String, String> creds = new HashMap<>();
        creds.put("email", email.trim());
        creds.put("password", password.trim());

        String jsonBody = gson.toJson(creds);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(">>> LOGIN RESPONSE CODE: " + response.statusCode());
        System.out.println(">>> LOGIN TOKEN: " + response.body());

        if (response.statusCode() == 200) {
            this.jwtToken = response.body();
            return fetchUserByEmail(email);
        } else {
            throw new Exception("Login failed: " + response.statusCode());
        }
    }

    private User fetchUserByEmail(String email) throws Exception {
        System.out.println(">>> FETCHING USER DETAILS for: " + email);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/users/email/" + email)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(">>> USER JSON FROM BACKEND: " + response.body());

        if (response.statusCode() == 200) {
            // 🔥 РУЧНИЙ ПАРСИНГ (Щоб обійти будь-які помилки GSON)
            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                Long id = json.has("userId") ? json.get("userId").getAsLong() : null;
                if (id == null && json.has("id")) id = json.get("id").getAsLong();

                String name = json.has("name") ? json.get("name").getAsString() : "Unknown";
                String mail = json.has("email") ? json.get("email").getAsString() : email;

                this.currentUser = new User(id, name, mail);
                System.out.println(">>> SUCCESSFULLY CREATED USER OBJ: " + this.currentUser.getName() + " (ID: " + this.currentUser.getId() + ")");

                return this.currentUser;
            } catch (Exception e) {
                System.err.println("!!! JSON PARSING ERROR: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        return null;
    }

    @Override
    public User register(String name, String email, String password) throws Exception {
        System.out.println(">>> REGISTERING: " + email);

        Map<String, String> body = new HashMap<>();
        body.put("name", name.trim());          // 🔥 Trim тут
        body.put("email", email.trim());        // 🔥 Trim тут
        body.put("password", password.trim());  // 🔥 Trim тут!

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(">>> REGISTER RESPONSE: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            // МИ НЕ РОБИМО ТУТ АВТОМАТИЧНИЙ ЛОГІН, ЩОБ ПОБАЧИТИ УСПІХ
            System.out.println(">>> REGISTRATION SUCCESSFUL! Trying to fetch user info manually if possible...");

            // Повертаємо тимчасового юзера, щоб UI переключився
            return new User(null, name, email);
        } else {
            throw new Exception("Registration failed: " + response.body());
        }
    }

    @Override
    public User updateUserProfile(String newName, String newEmail) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", newName);
        body.put("email", newEmail);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/users/" + currentUser.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            // Теж ручний парсинг для надійності
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            this.currentUser.setName(json.get("name").getAsString());
            this.currentUser.setEmail(json.get("email").getAsString());
            return this.currentUser;
        }
        return currentUser;
    }

    // ================= GROUPS =================

    @Override
    public List<Group> getGroups() throws Exception {
        System.out.println(">>> GETTING GROUPS...");

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/groups")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println(">>> GROUPS JSON: " + response.body());
            Type listType = new TypeToken<List<Group>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        }
        return Collections.emptyList();
    }

    @Override
    public Group createGroup(String name, String description) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/groups")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Group.class);
        }
        throw new Exception("Error creating group: " + response.body());
    }

    @Override
    public Group updateGroup(long groupId, String name, String description) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/groups/" + groupId)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Group.class);
        }
        return null;
    }

    @Override
    public boolean deleteGroup(long groupId) throws Exception {
        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/groups/" + groupId)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 200 OK або 204 No Content вважаються успіхом
        return response.statusCode() == 204 || response.statusCode() == 200;
    }

    // ================= TASKS =================

    // ================= TASKS =================

    @Override
    public List<Task> getTasksByGroup(long groupId) throws Exception {
        // GET /api/tasks/group/{groupId}
        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/tasks/group/" + groupId)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<Task>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        }
        return Collections.emptyList();
    }

    @Override
    public Task createTask(long groupId, String title, String description, String deadline) throws Exception {
        // POST /api/tasks/group/{groupId}

        // Бекенд чекає формат ISO (2023-12-31T23:59:00).
        // Якщо фронт дає просто дату (2023-12-31), доклеїмо час.
        if (deadline != null && !deadline.contains("T")) {
            deadline = deadline + "T23:59:00";
        }

        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("deadline", deadline);
        // creatorName бекенд візьме з токена, але DTO може вимагати поле, тому пусте:
        body.put("creatorName", "");

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/tasks/group/" + groupId)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Task.class);
        }
        throw new Exception("Failed to create task: " + response.body());
    }

    @Override
    public Task updateTaskStatus(long taskId, String status) throws Exception {
        // PATCH /api/tasks/{taskId}/status
        // Body: { "status": "DONE" }

        Map<String, String> body = new HashMap<>();
        body.put("status", status);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/tasks/" + taskId + "/status")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Task.class);
        }
        return null;
    }

    @Override
    public Task updateTask(long taskId, String title, String description, String deadline) throws Exception {
        // PUT /api/tasks/{taskId}

        if (deadline != null && !deadline.contains("T")) {
            deadline = deadline + "T23:59:00";
        }

        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("deadline", deadline);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/tasks/" + taskId)
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Task.class);
        }
        return null;
    }

    @Override
    public boolean deleteTask(long taskId) throws Exception {
        // DELETE /api/tasks/{taskId}
        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/tasks/" + taskId)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 || response.statusCode() == 204;
    }

    // ================= RESOURCES =================

    @Override
    public List<Resource> getResourcesByGroup(long groupId) throws Exception {
        // GET /api/resources/group/{groupId}
        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/resources/group/" + groupId)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<Resource>>(){}.getType());
        }
        return Collections.emptyList();
    }

    @Override
    public Resource createResource(long groupId, String title, String url) throws Exception {
        // POST /api/resources
        // Backend ResourceDTO: { groupId, title, type="LINK", pathOrUrl=..., uploadedBy=... }

        if (currentUser == null) throw new Exception("User not logged in");

        Map<String, Object> body = new HashMap<>();
        body.put("groupId", groupId);
        body.put("title", title);
        body.put("type", "LINK"); // Поки що працюємо тільки з посиланнями
        body.put("pathOrUrl", url);
        body.put("uploadedBy", currentUser.getId()); // ID поточного юзера

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/resources")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), Resource.class);
        }
        throw new Exception("Create resource failed: " + response.body());
    }

    @Override
    public List<Resource> getAllResources() throws Exception {
        // Цей метод можна залишити пустим або реалізувати пошук по всіх групах, якщо треба
        return Collections.emptyList();
    }

    // ================= MEMBERS & LOGS =================

    @Override
    public List<Member> getMembersByGroup(long groupId) throws Exception {
        // GET /api/memberships/groups/{groupId}/members
        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/memberships/groups/" + groupId + "/members")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<Member>>(){}.getType());
        }
        return Collections.emptyList();
    }

    @Override
    public Member addMemberToGroup(long groupId, String name, String email, String role) throws Exception {
        // Логіка:
        // 1. Знайти user_id за email (бо ми знаємо тільки email)
        // 2. Відправити запит на додавання (POST /api/memberships)

        User userToAdd = fetchUserByEmail(email);
        if (userToAdd == null) {
            throw new Exception("User with email " + email + " not found");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userId", userToAdd.getId());
        body.put("groupId", groupId);
        body.put("role", role); // "MEMBER" або "ADMIN"

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/memberships")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Member.class);
        }
        throw new Exception("Failed to add member: " + response.body());
    }

    @Override
    public boolean removeMemberFromGroup(long groupId, long userId) throws Exception {
        // DELETE /api/memberships/leave
        // Body: { groupId, userId }

        Map<String, Object> body = new HashMap<>();
        body.put("groupId", groupId);
        body.put("userId", userId);

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/memberships/leave")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    @Override
    public List<ActivityLog> getActivityLogs() throws Exception {
        // GET /api/activity-logs/user/{myId}
        if (currentUser == null) return Collections.emptyList();

        HttpRequest request = authenticatedRequestBuilder(BASE_URL + "/activity-logs/user/" + currentUser.getId())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<ActivityLog>>(){}.getType());
        }
        return Collections.emptyList();
    }

    // ================= HELPER =================

    private HttpRequest.Builder authenticatedRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        return builder;
    }
}