package com.proj.frontend.service;
import com.proj.frontend.model.*;
import com.proj.frontend.model.Member;

import java.util.List;

public interface BackendService {
    User login(String email, String password) throws Exception;
    User register(String name, String email, String password) throws Exception;
    User updateUserProfile(String newName, String newEmail) throws Exception;

    List<Group> getGroups() throws Exception;
    Group createGroup(String name, String description) throws Exception;

    List<Task> getTasksByGroup(long groupId) throws Exception;
    Task createTask(long groupId, String title, String description, String deadline) throws Exception;
    Task updateTaskStatus(long taskId, String status) throws Exception;
    Task updateTask(long taskId, String title, String description, String deadline) throws Exception;
    boolean deleteTask(long taskId) throws Exception;

    List<Resource> getResourcesByGroup(long groupId) throws Exception;
    Resource createResource(long groupId, String title, String url) throws Exception;
    Group updateGroup(long groupId, String name, String description) throws Exception;
    boolean deleteGroup(long groupId) throws Exception;

    List<ActivityLog> getActivityLogs() throws Exception;

    List<Member> getMembersByGroup(long groupId) throws Exception;
    Member addMemberToGroup(long groupId, String name, String email, String role) throws Exception;
    boolean removeMemberFromGroup(long groupId, long userId) throws Exception;

    List<Resource> getAllResources() throws Exception;

    void connectWebSocket();
    void disconnectWebSocket();
}