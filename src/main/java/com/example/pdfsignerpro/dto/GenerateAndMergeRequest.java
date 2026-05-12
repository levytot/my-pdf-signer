package com.example.pdfsignerpro.dto;

import java.util.List;

public class GenerateAndMergeRequest {

    private List<GenerationTask> tasks;
    private String password; // Optional

    // Getters and Setters
    public List<GenerationTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<GenerationTask> tasks) {
        this.tasks = tasks;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}