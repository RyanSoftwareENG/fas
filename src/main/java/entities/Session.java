package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Session {

    private Long id;
    @JsonIgnore
    private Client client;
    private LocalDateTime uploadTime;
    private String duration;
    private BigDecimal price;
    private String notes;
    private NutritionPlan nutritionPlan;
    private BodyData bodyData;
    private List<Examination> examinations = new ArrayList<>();

    public Session() {}

    public Session(
            Client client,
            String duration,
            BigDecimal price,
            String notes
    ){
        this.client = client;
        this.duration = duration;
        this.price = price;
        this.notes = notes;
    }



    public Session(Long sessionId, Client client, String duration, BigDecimal price, String notes) {
        this.id = sessionId;
        this.client = client;
        this.duration = duration;
        this.price = price;
        this.notes = notes;
    }

    public Session(Long i, Client currentClient, String duration, BigDecimal sPrice, BodyData bodyData, NutritionPlan plan, ArrayList<Examination> examsList, String notes) {
        this.id = i;


        this.client = currentClient;

        this.duration = duration;
        this.price = sPrice;
        this.notes = notes;
        this.bodyData = bodyData;
        this.nutritionPlan = plan;
        this.examinations = examsList;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }


    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public NutritionPlan getNutritionPlan() { return nutritionPlan; }
    public void setNutritionPlan(NutritionPlan nutritionPlan) { this.nutritionPlan = nutritionPlan; }

    public BodyData getBodyData() { return bodyData; }
    public void setBodyData(BodyData bodyData) { this.bodyData = bodyData; }

    public List<Examination> getExaminations() { return examinations; }
    public void setExaminations(List<Examination> examinations) {
        if (examinations != null) {
            this.examinations = examinations;
        }
    }

    public void addExamination(Examination examination) {
        if (this.examinations == null) {
            this.examinations = new ArrayList<>();
        }
        if (examination != null) {
            this.examinations.add(examination);
        }
    }
}
