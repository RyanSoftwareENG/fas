package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import entities.Client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionListDTO {

    // =========================
    // Session Data
    // =========================

    private Long id;
    private LocalDateTime uploadTime;
    private String duration;
    private BigDecimal price;
    private String notes;

    // =========================
    // Client Basic Data
    // =========================

    private Long clientID;
    private String firstName;
    private String lastName;
    private Character gender;
    private LocalDate birthDate;
    private String contactNumber;
    private LocalDateTime uploadDate;
    private LocalDateTime modificationDate;

    public SessionListDTO() {
    }

    // =========================
    // Session Getters
    // =========================

    public Long getId() {
        return id;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public String getDuration() {
        return duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getNotes() {
        return notes;
    }

    // =========================
    // Client Getters
    // =========================

    public Long getClientID() {
        return clientID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Character getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }
    public Client getClient() {
        long safeClientID = this.clientID != null ? this.clientID : 0L;
        char safeGender = this.gender != null ? this.gender : 'M';

        return new Client(
                safeClientID,
                this.firstName,
                this.lastName,
                safeGender,
                this.birthDate,
                this.contactNumber,
                this.uploadDate,
                this.modificationDate
        );
    }

    // =========================
    // Client Full Name
    // =========================

    public String getClientName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";

        return (first + " " + last).trim();
    }
}