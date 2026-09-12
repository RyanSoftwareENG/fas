package user_interface.offer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import api.ClientApiManager;
import entities.FoodItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import runner.BackgroundRunner;
import api.FoodAPI;

import static user_interface.forminput.Desgin.showAlert;

public class MealsCard {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label mealsName;

    @FXML
    private Button btnView;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private FoodItem currentFood;
    private MealsList parentController;

    public void setFoodtData(FoodItem foodItem, MealsList parentController) {
        this.currentFood = foodItem;
        this.parentController = parentController;

        if (currentFood != null && mealsName != null) {
            mealsName.setText(currentFood.getFoodName());
        }
    }

    @FXML
    void handleView(ActionEvent event) {
        if (currentFood == null) return;

        try {
            // 1. تحميل واجهة العرض
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ViewMeal.fxml"));
            Parent root = loader.load();

            // 2. جلب الكنترولر وتمرير بيانات الوجبة الحالية له
            ViewMeal viewLoader = loader.getController();
            viewLoader.setMealDetails(currentFood);

            // 3. إنشاء نافذة منبثقة وعرضها
            Stage stage = new Stage();
            stage.setTitle("تفاصيل: " + currentFood.getFoodName());
            stage.initModality(Modality.APPLICATION_MODAL); // منع التفاعل مع النافذة الخلفية لحين الإغلاق
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("خطأ أثناء فتح واجهة عرض تفاصيل الوجبة: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        if (currentFood == null) return;

        try {
            // 1. تحميل ملف الـ FXML الخاص بواجهة التعديل
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MealsEditCard.fxml"));
            Parent root = loader.load();

            // 2. الحصول على متحكم واجهة التعديل (EditMeal Controller)
            EditMeal editController = loader.getController();

            // 3. تمرير الوجبة الحالية ومرجع من MealsList لتحديث القائمة بعد الحفظ
            editController.setMealData(currentFood, parentController);

            // 4. إنشاء نافذة جديدة (Stage) لعرض واجهة التعديل فوق النافذة الحالية
            Stage stage = new Stage();
            stage.setTitle("تعديل الوجبة: " + currentFood.getFoodName());

            // جعل النافذة منبثقة تمنع التفاعل مع النافذة الخلفية حتى تُغلق
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("خطأ أثناء فتح واجهة تعديل الوجبة: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأكيد الحذف ⚠️");
        confirmAlert.setHeaderText("هل تريد حذف الوجبة (" + currentFood.getFoodName() + ")؟");
        // تم تصحيح رسالة التنبيه لتناسب الوجبات بدلاً من العملاء
        confirmAlert.setContentText("تنبيه: سيقوم النظام بحذف هذه الوجبة نهائياً ولا يمكن التراجع عن هذا الإجراء.");

        confirmAlert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {

            // مصفوفة مؤقتة لتخزين حالة الحذف
            final boolean[] isDeletedHolder = new boolean[1];

            // إرسال عملية الحذف إلى الخلفية لمنع التجميد
            BackgroundRunner.run("جاري حذف الوجبة... ⏳", () -> {
                try {
                    final FoodAPI foodAPI =
                            ClientApiManager
                                    .getInstance()
                                    .getFoodAPI();
                    isDeletedHolder[0] = foodAPI.deleteFood(currentFood.getFoodItemId());
                } catch (Exception e) {
                    System.err.println("❌ خطأ أثناء الاتصال بقاعدة البيانات لحذف الوجبة: " + e.getMessage());
                }
                return true;
            }, () -> {
                // تحديث الواجهة بناءً على النتيجة التي تمت في الخلفية
                if (isDeletedHolder[0]) {
                    System.out.println("حذف الوجبة: " + currentFood.getFoodName());
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "تم الحذف ✔",
                            "تم حذف الوجبة (" + currentFood.getFoodName() + ") بنجاح."
                    );

                    if (parentController != null) {
                        parentController.refreshList();
                    }
                } else {
                    showAlert(
                            Alert.AlertType.ERROR,
                            "فشل الحذف ❌",
                            "حدث خطأ ولم يتم حذف الوجبة (" + currentFood.getFoodName() + ")."
                    );
                }
            });
        }
    }

}