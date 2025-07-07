package com.example.chamberly;

public class AppUser {
    public String username;
    public String role;

    public AppUser() {
        // Default constructor required for Firebase
    }

    public AppUser(String username, String role) {
        this.username = username;
        this.role = role;
    }
}
