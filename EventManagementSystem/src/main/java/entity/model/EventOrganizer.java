package entity.model;

import jakarta.persistence.*;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
public class EventOrganizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private String userCode;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;

    @OneToMany(mappedBy = "eventOrganizer")
    private List<Event> events;

    // Lifecycle callback to generate userCode
    @PrePersist
    protected void onCreate() {
        if (this.userCode == null) {
            String year = Year.now().toString();
            String suffix = String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
            this.userCode = year + suffix;
        }
    }

    // Getters & setters (omit setter for userCode)
    public Long getId() {
        return id;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
