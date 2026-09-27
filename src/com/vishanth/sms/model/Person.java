package com.vishanth.sms.model;

import java.io.Serializable;

/**
 * Abstract class representing a general Person.
 * Demonstrates Abstraction and Encapsulation in OOP.
 */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
    private String contactNumber;

    public Person(String name, String email, String contactNumber) {
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    // Encapsulation: Controlled Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email.trim();
        }
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            this.contactNumber = contactNumber.trim();
        }
    }

    // Abstraction: Abstract methods to be implemented by concrete subclasses
    public abstract String getRole();
    public abstract String getFormattedSummary();

    @Override
    public String toString() {
        return "Name: " + name + " | Email: " + email + " | Contact: " + contactNumber;
    }
}
