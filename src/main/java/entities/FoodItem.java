package entities;

import java.math.BigDecimal;

public class FoodItem {

    private Long foodItemId;
    private String foodName;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrates;
    private BigDecimal fat;
    private boolean available = true; // القيمة الافتراضية true تعادل 'Y'
    private String costLevel;

    // Constructors
    public FoodItem(){}


    // منشئ للجلب من قاعدة البيانات
    public FoodItem(Long id, String foodName, BigDecimal calories, BigDecimal protein, BigDecimal carbohydrates, BigDecimal fat,
                    boolean available, String costLevel) {
        this.foodItemId = id;
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.available = available;
        this.costLevel = costLevel;
    }
    public FoodItem(
            String foodName,
            BigDecimal calories,
            BigDecimal protein,
            BigDecimal carbohydrates,
            BigDecimal fat,
            boolean available,
            String costLevel
    ){
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.available = available;
        this.costLevel = costLevel;
    }

    // Getters and Setters

    public Long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public void setCalories(BigDecimal calories) {
        this.calories = calories;
    }

    public BigDecimal getProtein() {
        return protein;
    }

    public void setProtein(BigDecimal protein) {
        this.protein = protein;
    }

    public BigDecimal getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(BigDecimal carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public BigDecimal getFat() {
        return fat;
    }

    public void setFat(BigDecimal fat) {
        this.fat = fat;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCostLevel() {
        return costLevel;
    }

    public void setCostLevel(String costLevel) {
        this.costLevel = costLevel;
    }
}