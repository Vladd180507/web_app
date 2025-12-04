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
    private List<Member> members = new ArrayList<>();

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

        members.add(new Member(1L, 1L, "Alice", "alice@example.com", "ADMIN",
                LocalDateTime.now().minusDays(5).toString()));
        members.add(new Member(2L, 1L, "Bob", "bob@example.com", "MEMBER",
                LocalDateTime.now().minusDays(2).toString()));
        members.add(new Member(3L, 2L, "Charlie", "charlie@example.com", "MEMBER",
                LocalDateTime.now().minusDays(1).toString()));
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
    public Task createTask(long groupId, String title, String description, String deadline) {
        long newId = tasks.size() + 1;
        Task t = new Task(newId, groupId, title, description, "OPEN", deadline);
        tasks.add(t);

        String detail = "Created task '" + title + "' in group " + groupId;
        if (deadline != null && !deadline.isBlank()) {
            detail += " with deadline " + deadline;
        }

        addLog(fakeUser.getId(), "CREATE_TASK", detail);
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

    @Override
    public User updateUserProfile(String newName, String newEmail) {
        fakeUser.setName(newName);
        fakeUser.setEmail(newEmail);

        addLog(fakeUser.getId(), "PROFILE_UPDATE", "User updated profile");

        return new User(fakeUser.getId(), fakeUser.getName(), fakeUser.getEmail());
    }

    @Override
    public Task updateTask(long taskId, String title, String description, String deadline) {
        for (Task t : tasks) {
            if (t.getId() == taskId) {
                t.setTitle(title);
                t.setDescription(description);
                t.setDeadline(deadline);

                addLog(fakeUser.getId(), "UPDATE_TASK", "Updated task " + taskId);
                return t;
            }
        }
        return null;
    }

    @Override
    public boolean deleteTask(long taskId) {
        Task toRemove = null;

        for (Task t : tasks) {
            if (t.getId() == taskId) {
                toRemove = t;
                break;
            }
        }

        if (toRemove != null) {
            tasks.remove(toRemove);
            addLog(fakeUser.getId(), "DELETE_TASK", "Deleted task " + taskId);
            return true;
        }

        return false;
    }

    @Override
    public Group updateGroup(long groupId, String name, String description) {
        for (Group g : groups) {
            if (g.getId() == groupId) {
                g.setName(name);
                g.setDescription(description);

                addLog(fakeUser.getId(), "UPDATE_GROUP",
                        "Updated group " + groupId + " (" + name + ")");
                return g;
            }
        }
        return null;
    }

    @Override
    public boolean deleteGroup(long groupId) {
        Group toRemove = null;
        for (Group g : groups) {
            if (g.getId() == groupId) {
                toRemove = g;
                break;
            }
        }

        if (toRemove != null) {
            groups.remove(toRemove);

            // Також приберемо задачі та ресурси цієї групи (щоб не висіли сиротами)
            tasks.removeIf(t -> t.getGroupId() == groupId);
            resources.removeIf(r -> r.getGroupId() == groupId);

            addLog(fakeUser.getId(), "DELETE_GROUP",
                    "Deleted group " + groupId + " (" + toRemove.getName() + ")");
            return true;
        }

        return false;   // ← ОЦЕГО РАНІШЕ НЕ ВИСТАЧАЛО
    }


    @Override
    public List<Member> getMembersByGroup(long groupId) {
        return members.stream()
                .filter(m -> m.getGroupId() == groupId)
                .collect(Collectors.toList());
    }

    @Override
    public Member addMemberToGroup(long groupId, String name, String email, String role) {
        long newUserId = members.size() + 1;
        String joinedAt = LocalDateTime.now().toString();

        Member m = new Member(newUserId, groupId, name, email, role, joinedAt);
        members.add(m);

        addLog(fakeUser.getId(), "ADD_MEMBER",
                "Added member " + name + " to group " + groupId);

        return m;
    }

    @Override
    public boolean removeMemberFromGroup(long groupId, long userId) {
        Member toRemove = null;
        for (Member m : members) {
            if (m.getGroupId() == groupId && m.getUserId() == userId) {
                toRemove = m;
                break;
            }
        }

        if (toRemove != null) {
            members.remove(toRemove);
            addLog(fakeUser.getId(), "REMOVE_MEMBER",
                    "Removed member " + toRemove.getName() + " from group " + groupId);
            return true;
        }
        return false;
    }

    @Override
    public List<Resource> getAllResources() {
        return resources;
    }
}