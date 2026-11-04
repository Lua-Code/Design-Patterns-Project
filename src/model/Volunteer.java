package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Volunteer implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String phone;
    private ArrayList<String> skills;
    private String availability;
    private double volunteerHours;

    public Volunteer(int id, String name, String email,
                     String phone, ArrayList<String> skills,
                     String availability) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.skills = new ArrayList<>(skills);
        this.availability = availability;
        this.volunteerHours = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public ArrayList<String> getSkills() {
        return new ArrayList<>(skills);
    }

    public String getAvailability() {
        return availability;
    }

    public double getVolunteerHours() {
        return volunteerHours;
    }

    public void addSkill(String skill) {
        if (skill != null && !skill.isBlank()
                && !skills.contains(skill)) {
            skills.add(skill);
        }
    }

    public void addHours(double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException(
                    "Hours must be greater than zero."
            );
        }

        volunteerHours += hours;
    }

    public void setVolunteerHours(double hours) {
        if (!Double.isFinite(hours) || hours < 0) {
            throw new IllegalArgumentException(
                    "Invalid volunteer hours."
            );
        }

        volunteerHours = hours;
    }

    public void displayInfo() {
        System.out.println("Volunteer ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Skills: " + skills);
        System.out.println("Availability: " + availability);
        System.out.println("Volunteer Hours: " + volunteerHours);
    }
}
