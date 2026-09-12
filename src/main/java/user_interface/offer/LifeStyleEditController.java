package user_interface.offer;

import api.ClientApiManager;
import entities.Client;
import entities.LifeStyleInformation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import runner.BackgroundRunner; // إضافة الاستيراد الخاص بالتشغيل في الخلفية
import api.ClientAPI;

import java.math.BigDecimal;

import static user_interface.forminput.Desgin.showAlert;

public class LifeStyleEditController {

    @FXML private Button backLifeStyle;
    @FXML private TextArea badHabits;
    @FXML private TextArea breakfast;
    @FXML private TextField budget;
    @FXML private TextField dialyMeals;
    @FXML private TextArea dinner;
    @FXML private TextArea drinks;
    @FXML private TextArea foodDislike;
    @FXML private TextArea lunch;
    @FXML private Button saveLifeStyle;
    @FXML private TextArea sleepHours;
    @FXML private TextArea snacks;

    private Client client; // الاحتفاظ بكائن العميل بالكامل

    // دالة معدلة لتمرير كائن العميل وعرض بياناته السابقة فوراً
    @FXML
    public void setClientData(Client client) {
        this.client = client;
        if (client != null && client.getLifeStyleInformation() != null) {
            LifeStyleInformation info = client.getLifeStyleInformation();

            // عرض الأرقام بأمان
            dialyMeals.setText(String.valueOf(info.getMealsPerDay()));
            budget.setText(String.valueOf(info.getBudget()));

            // عرض النصوص مع حماية ضد الـ null لكي لا يظهر النص "null" داخل حقول الواجهة
            breakfast.setText(info.getBreakfast() != null ? info.getBreakfast() : "");
            lunch.setText(info.getLunch() != null ? info.getLunch() : "");
            dinner.setText(info.getDinner() != null ? info.getDinner() : "");
            snacks.setText(info.getSnacks() != null ? info.getSnacks() : "");
            drinks.setText(info.getDrinks() != null ? info.getDrinks() : "");
            badHabits.setText(info.getBadHabits() != null ? info.getBadHabits() : "");
            sleepHours.setText(info.getSleepHours() != null ? info.getSleepHours() : "");
            foodDislike.setText(info.getFoodDislike() != null ? info.getFoodDislike() : "");
        }
    }

    @FXML
    void handleSaveLifeStyle(ActionEvent event) {
        // 1. تجميع البيانات وبناء الكائن على خيط الواجهة
        int meals = 0;
        try {
            meals = (dialyMeals.getText() == null || dialyMeals.getText().trim().isEmpty())
                    ? 0 : Integer.parseInt(dialyMeals.getText().trim());
        } catch (NumberFormatException ignored) {}

        double bgt = 0.0;
        try {
            bgt = (budget.getText() == null || budget.getText().trim().isEmpty())
                    ? 0.0 : Double.parseDouble(budget.getText().trim());
        } catch (NumberFormatException ignored) {}

        LifeStyleInformation.Builder builder = new LifeStyleInformation.Builder()
                .mealsPerDay(meals)
                .budget(BigDecimal.valueOf(bgt));

        if (breakfast.getText() != null) builder.breakfast(breakfast.getText().trim());
        if (lunch.getText() != null) builder.lunch(lunch.getText().trim());
        if (dinner.getText() != null) builder.dinner(dinner.getText().trim());
        if (snacks.getText() != null) builder.snacks(snacks.getText().trim());
        if (drinks.getText() != null) builder.drinks(drinks.getText().trim());
        if (badHabits.getText() != null) builder.badHabits(badHabits.getText().trim());
        if (sleepHours.getText() != null) builder.sleepHours(sleepHours.getText().trim());
        if (foodDislike.getText() != null) builder.foodDislike(foodDislike.getText().trim());

        LifeStyleInformation updatedInfo = builder.build();

        // 2. استخدام المصفوفة المؤقتة لحفظ حالة نجاح العملية
        final boolean[] isSuccessHolder = new boolean[1];

        // 3. تنفيذ الاستعلام وتحديث قاعدة البيانات في الخلفية
        BackgroundRunner.run("جاري حفظ بيانات نمط الحياة... ⏳", () -> {
            ClientAPI service = ClientApiManager.getInstance().getClientAPI();
            isSuccessHolder[0] = service.updateLifeStyle(updatedInfo, client.getClientID());
            return true;
        }, () -> {
            // 4. تحديث حالة الواجهة بأمان (دالة بدون معاملات)
            if (isSuccessHolder[0]) {
                System.out.println("🎉 تم تحديث بيانات نمط الحياة بنجاح!");

                client.setLifeStyleInformation(updatedInfo);
                handleBackLifeStyle(event); // إغلاق النافذة
                showAlert(Alert.AlertType.INFORMATION, "تم التحديث بنجاح", "تم حفظ بيانات نمط الحياة الجديدة بنجاح.");
            } else {
                showAlert(Alert.AlertType.WARNING, "فشل التحديث", "هناك خطأ في عملية حفظ البيانات المحدثة.");
            }
        });
    }

    @FXML
    void handleBackLifeStyle(ActionEvent event) {
        Stage stage = (Stage) backLifeStyle.getScene().getWindow();
        stage.close();
    }
}