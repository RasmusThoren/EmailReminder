package com.example.emailsender;

public class ReminderRequest {
    private String recipient;
    private String projectname; // as requested
    private String date;        // ISO-8601 with timezone, e.g. 2025-09-08T10:00:00+02:00

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getProjectname() { return projectname; }
    public void setProjectname(String projectname) { this.projectname = projectname; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
