package com.proj.frontend.service;

import com.proj.frontend.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockBackendService implements BackendService {

    private List<Group> groups = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private User fakeUser = new User(1L, "Test User", "test@example.com");

    public MockBackendService() {
        groups.add(new Group(1L, "OOP Study", "Exam preparation"));
        groups.add(new Group(2L, "Database Project", "Semestral work"));

        tasks.add(new Task(1L, 1L, "Read chapter 1", "Basic OOP principles", "OPEN"));
        tasks.add(new Task(2L, 1L, "Do homework", "Polymorphism tasks", "IN_PROGRESS"));
    }

    @Override
    public User login(String email, String password) {
        // ніякої перевірки, просто повертаємо фейкового користувача
        return fakeUser;
    }

    @Override
    public User register(String name, String email, String password) {
        return new User(2L, name, email);
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
        return t;
    }

    @Override
    public Task updateTaskStatus(long taskId, String status) {
        for (Task t : tasks) {
            if (t.getId() == taskId) {
                t.setStatus(status);
                return t;
            }
        }
        return null;
    }

    @Override
    public List<Resource> getResourcesByGroup(long groupId) {
        return List.of(
                new Resource(1L, groupId, "Google Drive", "LINK", "https://drive.google.com")
        );
    }

    @Override
    public Resource createResource(long groupId, String title, String url) {
        return new Resource(10L, groupId, title, "LINK", url);
    }
}