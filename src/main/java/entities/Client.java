package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Client {
    private Long clientID;
    private String firstName;
    private String lastName;
    private Character gender;
    private LocalDate birthDate;
    private String contactNumber;
    private LocalDateTime uploadDate;
    private LocalDateTime modificationDate;
    private HealthData healthData;
    private List<PatientAllergy> allergies = new ArrayList<>();
    private List<PatientChronicDisease> chronicDiseases = new ArrayList<>();
    private LifeStyleInformation lifeStyleInformation;
    private List<Session> sessions = new ArrayList<>();

    public Client() {
    }
public Client(String name,Long clientID) {
        this.clientID = clientID;
        this.firstName = name;
}
    public Client(String firstName, String lastName, Character gender, LocalDate birthDate) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }
    public Client(Long clientID, String firstName, String lastName, char gender,
                  LocalDate birthDate, String contactNumber, LocalDateTime uploadDate, LocalDateTime modificationDate) {
        this.clientID = clientID;
        this.gender = gender;
        this.birthDate = birthDate;
        this.contactNumber = contactNumber;
        this.uploadDate = uploadDate;
        this.modificationDate = modificationDate;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Client(String[] name, Character gender, LocalDate birthDate, String contactNumber) {
        this.firstName = getFirstName(name);
        this.lastName = getLastName(name);
        this.gender = gender;
        this.birthDate = birthDate;
        this.contactNumber = contactNumber;
        this.uploadDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
    }

    public Client(Long clientID, String[] name, Character gender, LocalDate birthDate, String contactNumber, LocalDateTime uploadDate, LocalDateTime modificationDate) {
        this.clientID = clientID;
        this.firstName = getFirstName(name);
        this.lastName = getLastName(name);
        this.gender = gender;
        this.birthDate = birthDate;
        this.contactNumber = contactNumber;
        this.uploadDate = uploadDate;
        this.modificationDate = modificationDate;
    }

    public Client(String[] name, Character gender, LocalDate birthDate, String contactNumber, LifeStyleInformation lifeStyleInformation) {
        this(name, gender, birthDate, contactNumber);
        this.lifeStyleInformation = lifeStyleInformation;
    }

    public Client(String[] name, Character gender, LocalDate birthDate, String contactNumber, HealthData healthData) {
        this(name, gender, birthDate, contactNumber);
        this.healthData = healthData;
    }

    public Client(String[] name, Character gender, LocalDate birthDate, String contactNumber, HealthData healthData, LifeStyleInformation lifeStyleInformation) {
        this(name, gender, birthDate, contactNumber, healthData);
        this.lifeStyleInformation = lifeStyleInformation;
    }

    // =========================
    // Getters & Setters
    // =========================


    public Long getClientID() {
        return clientID;
    }


    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }


    public String getFirstName() {
        return firstName;
    }
    public String getFirstName(String[] name) {
        if (name == null || name.length == 0) {
            return "";
        }
        String firstName = name[0];
        for (int i = 1; i < name.length - 1; i++) {
            firstName += " "+name[i];
        }
        return firstName;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }



    public String getLastName() {
        return lastName;
    }

    public String getLastName(String[] name) {
        if (name.length <= 1) {
            return "";
        }
        return name[name.length - 1];
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public Character getGender() {
        return gender;
    }


    public void setGender(Character gender) {
        this.gender = gender;
    }


    public LocalDate getBirthDate() {
        return birthDate;
    }


    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }


    public String getContactNumber() {
        return contactNumber;
    }


    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }


    public LocalDateTime getUploadDate() {
        return uploadDate;
    }


    public LocalDateTime getModificationDate() {
        return modificationDate;
    }


    public HealthData getHealthData() {
        return healthData;
    }


    public void setHealthData(HealthData healthData) {

        this.healthData = healthData;

        if (healthData != null) {
            healthData.setClient(this);
        }
    }


    public LifeStyleInformation getLifeStyleInformation() {
        return lifeStyleInformation;
    }


    public void setLifeStyleInformation(
            LifeStyleInformation lifeStyleInformation) {

        this.lifeStyleInformation = lifeStyleInformation;

        if (lifeStyleInformation != null) {
            lifeStyleInformation.setClient(this);
        }
    }



    public List<Session> getSessions() {
        return sessions;
    }



    public void setSessions(List<Session> sessions) {

        this.sessions.clear();

        if(sessions != null) {

            for(Session session : sessions) {
                addSession(session);
            }
        }
    }



    public void addSession(Session session) {

        if(session != null){

            sessions.add(session);
            session.setClient(this);

        }
    }

    public void removeSession(Session session) {

        if(session != null){

            sessions.remove(session);
            session.setClient(null);

        }
    }

    public List<PatientChronicDisease> getChronicDiseases() {
        return chronicDiseases;
    }

    public void setChronicDiseases(List<PatientChronicDisease> chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }

    public List<PatientAllergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<PatientAllergy> allergies) {
        this.allergies = allergies;
    }

// =========================
    // Calculated Fields
    // =========================


    public int getAge() {

        if (birthDate == null) {
            return 0;
        }

        return Period
                .between(birthDate, LocalDate.now())
                .getYears();
    }



    public String getFullName() {

        StringBuilder name = new StringBuilder();

        if(firstName != null)
            name.append(firstName);

        if(lastName != null)
            name.append(" ").append(lastName);

        return name.toString().trim();
    }



    public void setFullName(String fullName) {

        if(fullName == null || fullName.trim().isEmpty()) {

            this.firstName = "Unknown";
            this.lastName = "Unknown";

            return;
        }


        String[] parts = fullName.trim().split("\\s+");


        if(parts.length == 1) {

            this.firstName = parts[0];
            this.lastName = "Unknown";

        }else {

            this.firstName = parts[0];

            StringBuilder last = new StringBuilder();

            for(int i = 1; i < parts.length; i++) {

                last.append(parts[i]);

                if(i < parts.length - 1)
                    last.append(" ");
            }

            this.lastName = last.toString();
        }
    }
}