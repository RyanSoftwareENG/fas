package entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


public class NutritionPlan {


    private Long planId;
    private Session session;
    private String targetGoal;
    private LocalDate startDate;
    private LocalDate endDate;
    private int planDuration;
    private LocalDateTime uploadDate;
    private LocalDateTime modificationDate;
    private String planStatus;
    private String mealDistribution;
    private BigDecimal proteinAmount;
    private BigDecimal fatAmount;
    private BigDecimal carbohydratesAmount;
    private BigDecimal totalCalories;
    private List<PlanFoodItem> selectedFoods = new ArrayList<>();
    private Integer mealsCount;
    private String waterIntake;
    private String notes;

    public NutritionPlan() {}

    public NutritionPlan(
            String targetGoal,
            LocalDate startDate,
            LocalDate endDate,
            String planStatus,
            String mealDistribution,
            BigDecimal proteinAmount,
            BigDecimal fatAmount,
            BigDecimal carbohydratesAmount
    ) {
        this.targetGoal = targetGoal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.planStatus = planStatus;
        this.mealDistribution = mealDistribution;
        this.proteinAmount = proteinAmount;
        this.fatAmount = fatAmount;
        this.carbohydratesAmount = carbohydratesAmount;
    }

    // ==========================================
    // Hibernate Lifecycle Callbacks (دوال تلقائية)
    // ==========================================

    // هذه الدالة ستعمل تلقائياً قبل أي عملية (Save) أو (Update) في قاعدة البيانات

    public void calculateBeforeSave() {
        // 1. حساب مدة الخطة بالأيام لملء عمود Plan_Duration
        if (startDate != null && endDate != null) {
            this.planDuration = (int) ChronoUnit.DAYS.between(startDate, endDate);
        } else {
            this.planDuration = 0;
        }

        // 2. حساب إجمالي السعرات لملء عمود Total_Calories
        BigDecimal protein = proteinAmount != null ? proteinAmount.multiply(BigDecimal.valueOf(4)) : BigDecimal.ZERO;
        BigDecimal carbs = carbohydratesAmount != null ? carbohydratesAmount.multiply(BigDecimal.valueOf(4)) : BigDecimal.ZERO;
        BigDecimal fat = fatAmount != null ? fatAmount.multiply(BigDecimal.valueOf(9)) : BigDecimal.ZERO;
        this.totalCalories = protein.add(carbs).add(fat);
    }

    // ==========================================
    // الدوال الحسابية الخارجية
    // ==========================================

    public BigDecimal calculateIdealWater(BigDecimal weight, String gender) {
        boolean male = "Male".equalsIgnoreCase(gender) || "ذكر".equals(gender) || "M".equalsIgnoreCase(gender);
        BigDecimal factor = male ? BigDecimal.valueOf(0.04) : BigDecimal.valueOf(0.035);
        return weight.multiply(factor);
    }

    // ==========================================
    // Getters and Setters
    // ==========================================

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public String getTargetGoal() { return targetGoal; }
    public void setTargetGoal(String targetGoal) { this.targetGoal = targetGoal; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getPlanDuration() { return planDuration; }
    public void setPlanDuration(int planDuration) { this.planDuration = planDuration; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getModificationDate() { return modificationDate; }
    public void setModificationDate(LocalDateTime modificationDate) { this.modificationDate = modificationDate; }

    public String getPlanStatus() { return planStatus; }
    public void setPlanStatus(String planStatus) { this.planStatus = planStatus; }

    public String getMealDistribution() { return mealDistribution; }
    public void setMealDistribution(String mealDistribution) { this.mealDistribution = mealDistribution; }

    public BigDecimal getProteinAmount() { return proteinAmount; }
    public void setProteinAmount(BigDecimal proteinAmount) { this.proteinAmount = proteinAmount; }

    public BigDecimal getFatAmount() { return fatAmount; }
    public void setFatAmount(BigDecimal fatAmount) { this.fatAmount = fatAmount; }

    public BigDecimal getCarbohydratesAmount() { return carbohydratesAmount; }
    public void setCarbohydratesAmount(BigDecimal carbohydratesAmount) { this.carbohydratesAmount = carbohydratesAmount; }

    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }

    public List<PlanFoodItem> getSelectedFoods() { return selectedFoods; }
    public void setSelectedFoods(List<PlanFoodItem> selectedFoods) { this.selectedFoods = selectedFoods; }

    public Integer getMealsCount() { return mealsCount; }
    public void setMealsCount(Integer mealsCount) { this.mealsCount = mealsCount; }

    public String getWaterIntake() { return waterIntake; }
    public void setWaterIntake(String waterIntake) { this.waterIntake = waterIntake; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

}