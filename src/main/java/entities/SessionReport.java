package entities;
import java.time.LocalDate;

// 1. تعديل اسم الجدول
public class SessionReport {

    private Long reportId;
    private Session session;
    private String diagnosis;
    private String assessment;
    private String sessionResults;
    private String recommendations;
    private String nextGoals;
    private String notes;
    private LocalDate nextAppointment;
    private String commitmentLevel;
    private String nutritionistName;

    // Constructors
    public SessionReport(){}

    // Getters and Setters

    public Session getSession(){
        return session;
    }

    public void setSession(Session session){
        this.session = session;
    }

    public Long getReportId(){
        return reportId;
    }

    public void setReportId(Long reportId){
        this.reportId = reportId;
    }

    public String getDiagnosis(){
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis){
        this.diagnosis = diagnosis;
    }

    public String getAssessment(){
        return assessment;
    }

    public void setAssessment(String assessment){
        this.assessment = assessment;
    }

    public String getSessionResults(){
        return sessionResults;
    }

    public void setSessionResults(String sessionResults){
        this.sessionResults = sessionResults;
    }

    public String getRecommendations(){
        return recommendations;
    }

    public void setRecommendations(String recommendations){
        this.recommendations = recommendations;
    }

    public String getNextGoals(){
        return nextGoals;
    }

    public void setNextGoals(String nextGoals){
        this.nextGoals = nextGoals;
    }

    public String getNotes(){
        return notes;
    }

    public void setNotes(String notes){
        this.notes = notes;
    }

    public LocalDate getNextAppointment(){
        return nextAppointment;
    }

    public void setNextAppointment(LocalDate nextAppointment){
        this.nextAppointment = nextAppointment;
    }

    public String getCommitmentLevel(){
        return commitmentLevel;
    }

    public void setCommitmentLevel(String commitmentLevel){
        this.commitmentLevel = commitmentLevel;
    }

    public String getNutritionistName(){
        return nutritionistName;
    }

    public void setNutritionistName(String nutritionistName){
        this.nutritionistName = nutritionistName;
    }
}