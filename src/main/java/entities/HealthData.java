package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HealthData {

    private Long id;
    @JsonIgnore
    private Client client;
    private String medicalHistory;
    private String familyMedicalHistory;
    private String notes;
    private String currentMedication;




    public HealthData(){}



    private HealthData(HealthData.HealthDataBuilder builder){

        this.currentMedication = builder.currentMedication;
        this.medicalHistory = builder.medicalHistory;
        this.familyMedicalHistory = builder.familyMedicalHistory;
        this.notes = builder.notes;

    }



    public static class HealthDataBuilder {

        private String currentMedication;
        private String medicalHistory;
        private String familyMedicalHistory;
        private String notes;


        public HealthData.HealthDataBuilder currentMedication(String value){
            this.currentMedication = value;
            return this;
        }


        public HealthData.HealthDataBuilder medicalHistory(String value){
            this.medicalHistory = value;
            return this;
        }


        public HealthData.HealthDataBuilder familyMedicalHistory(String value){
            this.familyMedicalHistory = value;
            return this;
        }


        public HealthData.HealthDataBuilder notes(String value){
            this.notes = value;
            return this;
        }


        public HealthData build(){
            return new HealthData(this);
        }
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId(){
        return id;
    }


    public Client getClient(){
        return client;
    }


    public void setClient(Client client){
        this.client = client;
    }


    public String getCurrentMedication(){
        return currentMedication;
    }


    public void setCurrentMedication(String currentMedication){
        this.currentMedication = currentMedication;
    }


    public String getMedicalHistory(){
        return medicalHistory;
    }


    public void setMedicalHistory(String medicalHistory){
        this.medicalHistory = medicalHistory;
    }


    public String getFamilyMedicalHistory(){
        return familyMedicalHistory;
    }


    public void setFamilyMedicalHistory(String familyMedicalHistory){
        this.familyMedicalHistory = familyMedicalHistory;
    }


    public String getNotes(){
        return notes;
    }


    public void setNotes(String notes){
        this.notes = notes;
    }
}