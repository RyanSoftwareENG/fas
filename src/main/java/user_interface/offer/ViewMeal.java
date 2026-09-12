package user_interface.offer;

import entities.FoodItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ViewMeal {

    @FXML private Label lblMealName;
    @FXML private Label lblCalories;
    @FXML private Label lblCarbs;
    @FXML private Label lblProtein;
    @FXML private Label lblFat;
    @FXML private Label lblCostLevel;
    @FXML private Button btnClose;

    private FoodItem currentFood;

    // دالة لتمرير كائن الوجبة وعرض تفاصيله داخل الـ Labels
    public void setMealDetails(FoodItem foodItem) {
        this.currentFood = foodItem;

        if (currentFood != null) {
            lblMealName.setText(currentFood.getFoodName());
            lblCalories.setText(currentFood.getCalories() + " سعرة حرارية");

            // تم توحيد المسافات لضمان مظهر متناسق للكلمات العربية
            lblCarbs.setText(currentFood.getCarbohydrates() + " جرام");
            lblProtein.setText(currentFood.getProtein() + " جرام");
            lblFat.setText(currentFood.getFat() + " جرام");

            // تحقق من وجود قيم لمستوى التكلفة وعرضها
            if (currentFood.getCostLevel() != null && !currentFood.getCostLevel().isEmpty()) {
                lblCostLevel.setText(currentFood.getCostLevel());
            } else {
                lblCostLevel.setText("غير محدد");
            }
        }
    }

    // حدث النقر على زر إغلاق النافذة
    @FXML
    void handleClose(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}