package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class LifeStyleInformation {

    private Long clientId;
    @JsonIgnore
    private Client client;
    private Integer mealsPerDay;
    private BigDecimal budget;
    private String breakfast;
    private String lunch;
    private String dinner;
    private String snacks;
    private String drinks;
    private String badHabits;
    private String sleepHours;
    private String foodDislike;

    public LifeStyleInformation() {
    }

    // =========================
    // Getters & Setters
    // =========================

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Integer getMealsPerDay() {
        return mealsPerDay;
    }

    public void setMealsPerDay(Integer mealsPerDay) {
        this.mealsPerDay = mealsPerDay;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }

    public String getLunch() {
        return lunch;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public String getDinner() {
        return dinner;
    }

    public void setDinner(String dinner) {
        this.dinner = dinner;
    }

    public String getSnacks() {
        return snacks;
    }

    public void setSnacks(String snacks) {
        this.snacks = snacks;
    }

    public String getDrinks() {
        return drinks;
    }

    public void setDrinks(String drinks) {
        this.drinks = drinks;
    }

    public String getBadHabits() {
        return badHabits;
    }

    public void setBadHabits(String badHabits) {
        this.badHabits = badHabits;
    }

    public String getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(String sleepHours) {
        this.sleepHours = sleepHours;
    }

    public String getFoodDislike() {
        return foodDislike;
    }

    public void setFoodDislike(String foodDislike) {
        this.foodDislike = foodDislike;
    }

    // =========================
    // Builder
    // =========================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long clientId;
        private Client client;
        private Integer mealsPerDay = 3;
        private BigDecimal budget;
        private String breakfast;
        private String lunch;
        private String dinner;
        private String snacks;
        private String drinks;
        private String badHabits;
        private String sleepHours;
        private String foodDislike;

        public Builder clientId(Long clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder client(Client client) {
            this.client = client;
            return this;
        }

        public Builder mealsPerDay(Integer mealsPerDay) {
            this.mealsPerDay = mealsPerDay;
            return this;
        }

        public Builder budget(BigDecimal budget) {
            this.budget = budget;
            return this;
        }

        public Builder breakfast(String breakfast) {
            this.breakfast = breakfast;
            return this;
        }

        public Builder lunch(String lunch) {
            this.lunch = lunch;
            return this;
        }

        public Builder dinner(String dinner) {
            this.dinner = dinner;
            return this;
        }

        public Builder snacks(String snacks) {
            this.snacks = snacks;
            return this;
        }

        public Builder drinks(String drinks) {
            this.drinks = drinks;
            return this;
        }

        public Builder badHabits(String badHabits) {
            this.badHabits = badHabits;
            return this;
        }

        public Builder sleepHours(String sleepHours) {
            this.sleepHours = sleepHours;
            return this;
        }

        public Builder foodDislike(String foodDislike) {
            this.foodDislike = foodDislike;
            return this;
        }

        public LifeStyleInformation build() {

            LifeStyleInformation information = new LifeStyleInformation();

            information.clientId = this.clientId;
            information.client = this.client;
            information.mealsPerDay = this.mealsPerDay;
            information.budget = this.budget;
            information.breakfast = this.breakfast;
            information.lunch = this.lunch;
            information.dinner = this.dinner;
            information.snacks = this.snacks;
            information.drinks = this.drinks;
            information.badHabits = this.badHabits;
            information.sleepHours = this.sleepHours;
            information.foodDislike = this.foodDislike;

            return information;
        }
    }
}