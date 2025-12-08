package com.proj.frontend.model;

import com.google.gson.annotations.SerializedName;

public class Group {
    @SerializedName("groupId") // Мапимо поле groupId з JSON у id
    private Long id;

    private String name;
    private String description;

    @SerializedName("createdByName") // Додаткове поле для краси
    private String createdBy;

    public Group() {}

    public Group(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return name; } // Щоб гарно виглядало в списку
}