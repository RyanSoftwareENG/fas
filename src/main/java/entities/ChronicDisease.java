package entities;

import java.util.ArrayList;
import java.util.List;

public class ChronicDisease {

    // تم التعديل ليطابق SQL بإضافة حرف s
    private Long id;

    private List<PatientChronicDisease> patientDiseases = new ArrayList<>();

    // تم التعديل ليطابق الاسم بالكامل في SQL
    private String diseaseName;

    public ChronicDisease() {
    }

    public ChronicDisease(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }
}