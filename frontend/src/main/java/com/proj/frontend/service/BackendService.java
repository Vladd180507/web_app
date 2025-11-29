package com.proj.frontend.service;

import com.proj.frontend.model.*;

import java.util.List;

public interface BackendService {
    User login(String email, String password) throws Exception;
    User register(String name, String email, String password) throws Exception;

    List<Group> getGroups() throws Exception;
    Group createGroup(String name, String description) throws Exception;

    List<Task> getTasksByGroup(long groupId) throws Exception;
    Task createTask(long groupId, String title, String description) throws Exception;
    Task updateTaskStatus(long taskId, String status) throws Exception;

    List<Resource> getResourcesByGroup(long groupId) throws Exception;
    Resource createResource(long groupId, String title, String url) throws Exception;
}