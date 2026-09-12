package entities;
import java.util.ArrayList;
import java.util.List;


public class Allergy {

    private Long id;
    private List<PatientAllergy> patientAllergies = new ArrayList<>();
    private String allergyName;

    public Allergy() {
    }

    public Allergy(String allergyName) {
        this.allergyName = allergyName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAllergyName() {
        return allergyName;
    }

    public void setAllergyName(String allergyName) {
        this.allergyName = allergyName;
    }
}
