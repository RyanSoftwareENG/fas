package entities;



import java.io.Serializable;
import java.util.Objects;

public class PatientChronicDiseaseId implements Serializable {

    private Long clientId;
    private Long diseaseId;

    public PatientChronicDiseaseId() {
    }

    public PatientChronicDiseaseId(Long clientId,
                                   Long diseaseId) {
        this.clientId = clientId;
        this.diseaseId = diseaseId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getDiseaseId() {
        return diseaseId;
    }

    public void setDiseaseId(Long diseaseId) {
        this.diseaseId = diseaseId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (!(o instanceof PatientChronicDiseaseId that))
            return false;

        return Objects.equals(clientId, that.clientId)
                && Objects.equals(diseaseId, that.diseaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, diseaseId);
    }
}