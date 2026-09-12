package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PatientAllergy extends HealthInfo {


    private PatientAllergyId id;
    @JsonIgnore
    private Client client;
    private Allergy allergy;

    public PatientAllergy() {
    }

    public PatientAllergy(Client client,
                          Allergy allergy,
                          String status,
                          String severity,
                          String notes,
                          String contraindicated) {

        super(status, severity, notes, contraindicated);

        this.client = client;
        this.allergy = allergy;

        this.id = new PatientAllergyId(
                client.getClientID(),
                allergy.getId()
        );
    }

    public PatientAllergyId getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Allergy getAllergy() {
        return allergy;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setAllergy(Allergy allergy) {
        this.allergy = allergy;
    }
}