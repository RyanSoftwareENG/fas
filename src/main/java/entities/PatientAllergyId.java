package entities;

import java.io.Serializable;
import java.util.Objects;

public class PatientAllergyId implements Serializable {

    private Long clientId;
    private Long allergyId;

    public PatientAllergyId() {
    }

    public PatientAllergyId(Long clientId, Long allergyId) {
        this.clientId = clientId;
        this.allergyId = allergyId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getAllergyId() {
        return allergyId;
    }

    public void setAllergyId(Long allergyId) {
        this.allergyId = allergyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientAllergyId that)) return false;
        return Objects.equals(clientId, that.clientId)
                && Objects.equals(allergyId, that.allergyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, allergyId);
    }
}