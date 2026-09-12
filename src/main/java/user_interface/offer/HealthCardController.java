package user_interface.offer;

import api.ClientApiManager;
import entities.Allergy;
import entities.ChronicDisease;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import api.PatientClientAPI;

import java.sql.SQLException;

public class HealthCardController {

    @FXML private Button editHeath;
    @FXML private Label idHealth;
    @FXML private Label nameHealth;

    private ChronicDisease currentDisease;
    private Allergy currentAllergy;
    private TextField editField;
    private boolean isEditMode = false;

    // دالة لاستقبال البيانات من HealthList
    public void setDiseaseData(ChronicDisease disease) {
        this.currentDisease = disease;
        idHealth.setText(String.valueOf(disease.getId()));
        nameHealth.setText(disease.getDiseaseName());
    }
    public void setAllergyData(Allergy allergy) {
        this.currentAllergy = allergy;
        idHealth.setText(String.valueOf(allergy.getId()));
        nameHealth.setText(allergy.getAllergyName());
    }

    @FXML
    void handleEditClient(ActionEvent event) {
        if (!isEditMode) {
            // -- الدخول في وضع التعديل --
            editField = new TextField(nameHealth.getText());
            editField.getStyleClass().add("edit-text-field"); // يمكنك إضافة ستايل CSS هنا

            // استبدال الـ Label بـ TextField في نفس المكان
            Pane parent = (Pane) nameHealth.getParent();
            int index = parent.getChildren().indexOf(nameHealth);
            parent.getChildren().set(index, editField);

            editHeath.setText("حفظ"); // تغيير نص الزر
            isEditMode = true;

        } else {
            // -- الدخول في وضع الحفظ --
            String newName = editField.getText();

            try {
                // 1. استدعاء طبقة الخدمة (Service Layer) لإجراء التعديل بأمان وحماية زمنية
                PatientClientAPI service =  ClientApiManager.getInstance().getPatientClientAPI();

                // تنفيذ عملية التحديث وفحص النتيجة
                boolean isUpdated = service.updateDisease(currentDisease.getId(), newName);

                if (isUpdated) {
                    // تحديث الكائن المحلي بعد نجاح الحفظ في قاعدة البيانات
                    currentDisease.setDiseaseName(newName);

                    // 2. تحديث الواجهة والعودة لوضع القراءة
                    nameHealth.setText(newName);
                    Pane parent = (Pane) editField.getParent();
                    int index = parent.getChildren().indexOf(editField);
                    parent.getChildren().set(index, nameHealth);

                    editHeath.setText("تعديل");
                    isEditMode = false;
                    System.out.println("✅ تم تحديث الاسم بنجاح.");
                } else {
                    // في حال فشل التعديل (بسبب انتهاء المهلة 10 ثوانٍ أو أي خطأ آخر)
                    // يمكنك هنا إظهار رسالة Alert للمستخدم تنبهه بالخطأ المسترجع من السيرفس
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}