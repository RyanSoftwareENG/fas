package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class PatientChronicDisease extends HealthInfo {

    private PatientChronicDiseaseId id;
    @JsonIgnore
    private Client client;
    private ChronicDisease chronicDisease;
    private List<ChronicDisease> chronicDiseases;

    public PatientChronicDisease() {
    }

    public PatientChronicDisease(Client client,
                                 ChronicDisease chronicDisease,
                                 String status,
                                 String severity,
                                 String notes,
                                 String contraindicated) {

        super(status, severity, notes, contraindicated);

        this.client = client;
        this.chronicDisease = chronicDisease;

        this.id = new PatientChronicDiseaseId(
                client.getClientID(),
                chronicDisease.getId()
        );
    }

    public PatientChronicDiseaseId getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public ChronicDisease getChronicDisease() {
        return chronicDisease;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setChronicDisease(
            ChronicDisease chronicDisease) {
        this.chronicDisease = chronicDisease;
    }

    public void setId(PatientChronicDiseaseId id) {
        this.id = id;
    }

    public List<ChronicDisease> getChronicDiseases() {
        return chronicDiseases;
    }

    public void setChronicDiseases(List<ChronicDisease> chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }
}