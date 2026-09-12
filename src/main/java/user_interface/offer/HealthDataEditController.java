package user_interface.offer;

import api.ClientApiManager;
import entities.Client;
import entities.HealthData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import runner.BackgroundRunner;
import api.ClientAPI;

import static user_interface.forminput.Desgin.showAlert;

public class HealthDataEditController {

    @FXML private Button backHealthData;
    @FXML private TextArea currentMadcein;
    @FXML private TextArea familyHistory;
    @FXML private TextArea healthHistory;
    @FXML private TextArea noteHealthDate;
    @FXML private Button saveHealthData;

    private Client client;

    public void setHealthData(Client client) {
        this.client = client;

        if (client != null && client.getHealthData() != null) {
            HealthData health = client.getHealthData();

            currentMadcein.setText(health.getCurrentMedication() != null ? health.getCurrentMedication() : "");
            healthHistory.setText(health.getMedicalHistory() != null ? health.getMedicalHistory() : "");
            familyHistory.setText(health.getFamilyMedicalHistory() != null ? health.getFamilyMedicalHistory() : "");
            noteHealthDate.setText(health.getNotes() != null ? health.getNotes() : "");
        }
    }

    @FXML
    void handleSaveHealthData(ActionEvent event) {
        HealthData.HealthDataBuilder builder = new HealthData.HealthDataBuilder();

        if (currentMadcein.getText() != null) {
            builder.currentMedication(currentMadcein.getText().trim());
        }
        if (healthHistory.getText() != null) {
            builder.medicalHistory(healthHistory.getText().trim());
        }
        if (familyHistory.getText() != null) {
            builder.familyMedicalHistory(familyHistory.getText().trim());
        }
        if (noteHealthDate.getText() != null) {
            builder.notes(noteHealthDate.getText().trim());
        }

        HealthData updatedHealth = builder.build();

        // مصفوفة مؤقتة لتخزين حالة نجاح عملية الحفظ
        final boolean[] isSuccessHolder = new boolean[1];

        // تشغيل عملية الحفظ الثقيلة في الخلفية لتجنب تجميد الواجهة
        BackgroundRunner.run("جاري حفظ البيانات الصحية... ⏳", () -> {
            ClientAPI service = ClientApiManager.getInstance().getClientAPI();
            isSuccessHolder[0] = service.updateHealthData(updatedHealth, client.getClientID());
            return true;
        }, () -> {
            // تحديث الواجهة بناءً على النتيجة التي تم جلبها من خيط الخلفية
            if (isSuccessHolder[0]) {
                client.setHealthData(updatedHealth);
                handleBackHealthData(event); // إغلاق النافذة
                showAlert(Alert.AlertType.INFORMATION, "تم التحديث بنجاح", "تم حفظ البيانات الصحية الجديدة بنجاح.");
            } else {
                showAlert(Alert.AlertType.WARNING, "فشل التحديث", "هناك خطأ في عملية حفظ البيانات المحدثة.");
            }
        });
    }

    @FXML
    void handleBackHealthData(ActionEvent event) {
        Stage stage = (Stage) backHealthData.getScene().getWindow();
        stage.close();
    }
}