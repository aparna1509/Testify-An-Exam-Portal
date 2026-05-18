package com.examportal.model;

public class User {
    private String userId;
    private String password;
    private String name;
    private String role; // ADMIN or USER
    private boolean examAttempted;
    private String email;

    public User() {}

    public User(String userId, String password, String name, String role, boolean examAttempted) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.role = role;
        this.examAttempted = examAttempted;
        this.email = "";
    }

    public User(String userId, String password, String name, String role, boolean examAttempted, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.role = role;
        this.examAttempted = examAttempted;
        this.email = email != null ? email : "";
    }

    public String getUserId()           { return userId; }
    public void setUserId(String id)    { this.userId = id; }

    public String getPassword()              { return password; }
    public void setPassword(String pwd)      { this.password = pwd; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public String getRole()             { return role; }
    public void setRole(String role)    { this.role = role; }

    public boolean isExamAttempted()                 { return examAttempted; }
    public void setExamAttempted(boolean attempted)  { this.examAttempted = attempted; }

    public String getEmail()            { return email != null ? email : ""; }
    public void setEmail(String email)  { this.email = email != null ? email : ""; }

    public boolean checkPassword(String entered) {
        return this.password.equals(entered);
    }
}
