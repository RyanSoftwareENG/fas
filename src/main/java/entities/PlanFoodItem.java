package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class PlanFoodItem {

    @JsonIgnore
    private NutritionPlan nutritionPlan;
    private FoodItem foodItem;
    private String mealType;
    private BigDecimal quantity;
    private String unit = "غرام";

    // Constructors
    public PlanFoodItem(){}

    public PlanFoodItem(
            NutritionPlan nutritionPlan,
            FoodItem foodItem,
            String mealType,
            BigDecimal quantity
    ){
        this.nutritionPlan = nutritionPlan;
        this.foodItem = foodItem;
        this.mealType = mealType;
        this.quantity = quantity;
    }
    public PlanFoodItem(Long nutritionPlanId, String foodName, Long foodItemId ,String mealType, BigDecimal quantity) {
        this.nutritionPlan = new NutritionPlan();
        this.foodItem = new FoodItem();
        this.nutritionPlan.setPlanId(nutritionPlanId);
        this.foodItem.setFoodItemId(foodItemId);
        this.foodItem.setFoodName(foodName);
        this.mealType = mealType;
        this.quantity = quantity;
        this.unit = "غرام";
    }


    // Getters and Setters (بدون getId)

    public NutritionPlan getNutritionPlan(){
        return nutritionPlan;
    }

    public void setNutritionPlan(NutritionPlan nutritionPlan){
        this.nutritionPlan = nutritionPlan;
    }

    public FoodItem getFoodItem(){
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem){
        this.foodItem = foodItem;
    }

    public String getMealType(){
        return mealType;
    }

    public void setMealType(String mealType){
        this.mealType = mealType;
    }

    public BigDecimal getQuantity(){
        return quantity;
    }

    public void setQuantity(BigDecimal quantity){
        this.quantity = quantity;
    }

    public String getUnit(){
        return unit;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }
}