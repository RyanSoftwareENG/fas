package user_interface.offer;

import api.ClientApiManager;
import api.FoodAPI;
import entities.FoodItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import runner.BackgroundRunner;

import java.math.BigDecimal;

public class EditMeal {

    @FXML private TextField carbMeals;
    @FXML private TextField nameMeals;
    @FXML private TextField cal;
    @FXML private TextField fat;
    @FXML private TextField protin;
    @FXML private ComboBox<String> costLevel;
    @FXML private CheckBox isActive;
    @FXML private Button backMeal;
    @FXML private Button saveEditzMeals;

    private FoodItem currentFood;
    private MealsList parentController;

// =====================================================
// API المركزي
// =====================================================

    private final FoodAPI foodAPI =
            ClientApiManager
                    .getInstance()
                    .getFoodAPI();

// =====================================================
// Initialize
// =====================================================

    @FXML
    public void initialize() {

        costLevel.getItems().clear();

        costLevel.getItems().addAll(
                "Low",
                "Medium",
                "High"
        );
    }

// =====================================================
// تحميل بيانات الوجبة
// =====================================================

    public void setMealData(
            FoodItem foodItem,
            MealsList parentController
    ) {

        this.currentFood =
                foodItem;

        this.parentController =
                parentController;

        if (currentFood == null) {
            return;
        }

        nameMeals.setText(
                safe(
                        currentFood.getFoodName()
                )
        );

        cal.setText(
                currentFood.getCalories() == null
                        ? ""
                        : currentFood
                        .getCalories()
                        .toString()
        );

        carbMeals.setText(
                currentFood.getCarbohydrates() == null
                        ? ""
                        : currentFood
                        .getCarbohydrates()
                        .toString()
        );

        protin.setText(
                currentFood.getProtein() == null
                        ? ""
                        : currentFood
                        .getProtein()
                        .toString()
        );

        fat.setText(
                currentFood.getFat() == null
                        ? ""
                        : currentFood
                        .getFat()
                        .toString()
        );

        isActive.setSelected(
                currentFood.isAvailable()
        );

        if (currentFood.getCostLevel() != null) {

            costLevel.setValue(
                    currentFood.getCostLevel()
            );
        }
    }

// =====================================================
// حفظ التعديل
// =====================================================

    @FXML
    void handleSaveEditMeals(
            ActionEvent event
    ) {

        if (currentFood == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "خطأ",
                    "لا توجد وجبة محددة للتعديل."
            );

            return;
        }

        // -------------------------------------------------
        // 1. التحقق من اسم الوجبة
        // -------------------------------------------------

        String foodName =
                nameMeals
                        .getText()
                        .trim();

        if (foodName.isEmpty() ||
                foodName.length() < 2) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات غير مكتملة",
                    "يرجى إدخال اسم الوجبة بشكل صحيح."
            );

            return;
        }

        // -------------------------------------------------
        // 2. التحقق من مستوى التكلفة
        // -------------------------------------------------

        if (costLevel.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات غير مكتملة",
                    "يرجى اختيار مستوى تكلفة الوجبة."
            );

            return;
        }

        // -------------------------------------------------
        // 3. قراءة القيم الرقمية
        // -------------------------------------------------

        BigDecimal calories;
        BigDecimal carbohydrates;
        BigDecimal protein;
        BigDecimal fatValue;

        try {

            calories =
                    parseDecimal(
                            cal.getText(),
                            "السعرات"
                    );

            carbohydrates =
                    parseDecimal(
                            carbMeals.getText(),
                            "الكربوهيدرات"
                    );

            protein =
                    parseDecimal(
                            protin.getText(),
                            "البروتين"
                    );

            fatValue =
                    parseDecimal(
                            fat.getText(),
                            "الدهون"
                    );

        } catch (IllegalArgumentException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "إدخال غير صحيح",
                    e.getMessage()
            );

            return;
        }

        // -------------------------------------------------
        // 4. تحديث الكائن
        // -------------------------------------------------

        currentFood.setFoodName(
                foodName
        );

        currentFood.setCalories(
                calories
        );

        currentFood.setCarbohydrates(
                carbohydrates
        );

        currentFood.setProtein(
                protein
        );

        currentFood.setFat(
                fatValue
        );

        currentFood.setAvailable(
                isActive.isSelected()
        );

        currentFood.setCostLevel(
                costLevel.getValue()
        );

        // -------------------------------------------------
        // 5. الحفظ في الخلفية
        // -------------------------------------------------

        BackgroundRunner.run(
                "جاري حفظ التعديلات... ⏳",

                () -> {

                    foodAPI.updateFood(
                            currentFood
                    );

                    return true;
                },

                () -> {

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "نجاح العملية",
                            "تم تحديث بيانات الوجبة بنجاح."
                    );

                    if (parentController != null) {

                        parentController.refreshList();
                    }

                    closeWindow();
                }
        );
    }

// =====================================================
// تحويل النص إلى BigDecimal
// =====================================================

    private BigDecimal parseDecimal(
            String value,
            String fieldName
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "يرجى إدخال قيمة "
                            + fieldName
                            + "."
            );
        }

        try {

            BigDecimal number =
                    new BigDecimal(
                            value.trim()
                    );

            if (number.signum() < 0) {

                throw new IllegalArgumentException(
                        "لا يمكن أن تكون قيمة "
                                + fieldName
                                + " سالبة."
                );
            }

            return number;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "قيمة "
                            + fieldName
                            + " يجب أن تكون رقمًا صحيحًا."
            );
        }
    }

// =====================================================
// Alert
// =====================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

// =====================================================
// العودة
// =====================================================

    @FXML
    void handleBackMeal(
            ActionEvent event
    ) {

        closeWindow();
    }

// =====================================================
// إغلاق النافذة
// =====================================================

    private void closeWindow() {

        if (backMeal == null ||
                backMeal.getScene() == null) {

            return;
        }

        Stage stage =
                (Stage)
                        backMeal
                                .getScene()
                                .getWindow();

        stage.close();
    }

// =====================================================
// Safe String
// =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

// =====================================================
// FXML Handlers
// =====================================================

    @FXML
    void handleNameMeals(
            ActionEvent event
    ) {
    }

    @FXML
    void handleCarbMeals(
            ActionEvent event
    ) {
    }

    @FXML
    void handleCal(
            ActionEvent event
    ) {
    }

    @FXML
    void handleFat(
            ActionEvent event
    ) {
    }

    @FXML
    void handleProtin(
            ActionEvent event
    ) {
    }

    @FXML
    void handleCostLevel(
            ActionEvent event
    ) {
    }

    @FXML
    void handleIsActive(
            ActionEvent event
    ) {
    }
}
