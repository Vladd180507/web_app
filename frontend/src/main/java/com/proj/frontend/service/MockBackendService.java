package com.proj.frontend.service;

import com.proj.frontend.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockBackendService implements BackendService {

    private List<Group> groups = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Resource> resources = new ArrayList<>();
    private List<ActivityLog> logs = new ArrayList<>();

    private User fakeUser = new User(1L, "Test User", "test@example.com");

    public MockBackendService() {
        // Початкові групи
        groups.add(new Group(1L, "OOP Study", "Exam preparation"));
        groups.add(new Group(2L, "Database Project", "Semestral work"));

        // Початкові задачі
        tasks.add(new Task(1L, 1L, "Read chapter 1", "Basic OOP principles", "OPEN"));
        tasks.add(new Task(2L, 1L, "Do homework", "Polymorphism tasks", "IN_PROGRESS"));
        tasks.add(new Task(3L, 2L, "Design ER diagram", "Database schema", "DONE"));

        // Початкові ресурси
        resources.add(new Resource(1L, 1L, "Google Drive", "LINK", "https://drive.google.com"));
        resources.add(new Resource(2L, 1L, "Lecture slides", "LINK", "https://example.com/slides"));
        resources.add(new Resource(3L, 2L, "Project repo", "LINK", "https://github.com/example/repo"));

        // Початкові логи
        addLog(fakeUser.getId(), "LOGIN", "User logged in (mock)");
        addLog(fakeUser.getId(), "CREATE_GROUP", "Initial groups created");
        addLog(fakeUser.getId(), "CREATE_TASK", "Initial tasks created");
    }

    private void addLog(Long userId, String action, String details) {
        long newId = logs.size() + 1;
        String ts = LocalDateTime.now().toString();
        logs.add(new ActivityLog(newId, userId, action, details, ts));
    }

    @Override
    public User login(String email, String password) {
        addLog(fakeUser.getId(), "LOGIN", "Login with email: " + email);
        return fakeUser;
    }

    @Override
    public User register(String name, String email, String password) {
        User u = new User(2L, name, email);
        addLog(u.getId(), "REGISTER", "New user registered: " + email);
        return u;
    }

    @Override
    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public Group createGroup(String name, String description) {
        long newId = groups.size() + 1;
        Group g = new Group(newId, name, description);
        groups.add(g);
        addLog(fakeUser.getId(), "CREATE_GROUP", "Created group: " + name);
        return g;
    }

    @Override
    public List<Task> getTasksByGroup(long groupId) {
        return tasks.stream()
                .filter(t -> t.getGroupId() == groupId)
                .collect(Collectors.toList());
    }

    @Override
    public Task createTask(long groupId, String title, String description) {
        long newId = tasks.size() + 1;
        Task t = new Task(newId, groupId, title, description, "OPEN");
        tasks.add(t);
        addLog(fakeUser.getId(), "CREATE_TASK", "Created task '" + title + "' in group " + groupId);
        return t;
    }

    @Override
    public Task updateTaskStatus(long taskId, String status) {
        for (Task t : tasks) {
            if (t.getId() == taskId) {
                t.setStatus(status);
                addLog(fakeUser.getId(), "UPDATE_TASK_STATUS",
                        "Task " + taskId + " status changed to " + status);
                return t;
            }
        }
        return null;
    }

    @Override
    public List<Resource> getResourcesByGroup(long groupId) {
        return resources.stream()
                .filter(r -> r.getGroupId() == groupId)
                .collect(Collectors.toList());
    }

    @Override
    public Resource createResource(long groupId, String title, String url) {
        long newId = resources.size() + 1;
        Resource r = new Resource(newId, groupId, title, "LINK", url);
        resources.add(r);
        addLog(fakeUser.getId(), "CREATE_RESOURCE",
                "Resource '" + title + "' added to group " + groupId);
        return r;
    }

    @Override
    public List<ActivityLog> getActivityLogs() {
        return logs;
    }
}