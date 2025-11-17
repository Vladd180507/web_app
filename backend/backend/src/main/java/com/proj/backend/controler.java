package com.proj.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class controler {
    private final repository repository;

    @Autowired
    public controler(repository repository) {
        this.repository = repository;
    }

    @GetMapping("/zalupa")
    public List<User> zalupa(){
        return repository.findAll();
    }
    @GetMapping("/zalupa/{username}")
    public User zalupa(@PathVariable String username){
        return repository.findByName(username).orElse(null);
    }
}
