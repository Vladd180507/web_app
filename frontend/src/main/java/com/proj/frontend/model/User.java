package com.proj.frontend.model;

import com.google.gson.annotations.SerializedName; // <--- ВАЖЛИВО!

public class User {

    @SerializedName("userId") // <--- Кажемо: "бери значення з поля 'userId' у JSON"
    private Long id;

    private String name;
    private String email;

    public User() {}

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return name; // Щоб у списках показувало ім'я, а не хеш
    }
}