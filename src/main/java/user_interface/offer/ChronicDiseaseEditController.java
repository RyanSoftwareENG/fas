package user_interface.offer;

import api.ClientApiManager;
import entities.ChronicDisease;
import entities.Client;
import entities.PatientChronicDisease;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import runner.BackgroundRunner;
import api.PatientClientAPI;

import java.util.ArrayList;
import java.util.Optional;

import static user_interface.forminput.Desgin.showAlert;

public class ChronicDiseaseEditController {

    @FXML private VBox cardsContainer;
    @FXML private Button closeBtn;

    private Client client;
    private PatientChronicDisease patient;


    // تعديل بيانات العميل المريض
    public void setPatientData(Client client) {
        this.client = client;
        if (client != null && client.getChronicDiseases() != null) {
            this.client.setChronicDiseases(new ArrayList<>(client.getChronicDiseases()));
        }
        refreshCards();
    }

    // تحديث الواجهة
    private void refreshCards() {
        cardsContainer.getChildren().clear();
        if (client == null || client.getChronicDiseases() == null) return;

        for (PatientChronicDisease disease : client.getChronicDiseases()) {
            VBox card = createDiseaseCard(disease);
            cardsContainer.getChildren().add(card);
        }
    }

    // حاوية المرض
    private VBox createDiseaseCard(PatientChronicDisease disease) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-radius: 8px; "
                + "-fx-background-radius: 8px; -fx-padding: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        Label titleLabel = new Label("المرض: " + disease.getChronicDisease().getDiseaseName());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2f3640;");

        TextField severityField = new TextField(disease.getSeverity());
        severityField.setPromptText("الخطورة (Severity)");

        TextField statusField = new TextField(disease.getStatus());
        statusField.setPromptText("الحالة (Status)");

        TextField foodField = new TextField(disease.getContraindicated());
        foodField.setPromptText("الأطعمة الممنوعة");

        TextArea notesArea = new TextArea(disease.getNotes());
        notesArea.setPromptText("ملاحظات...");
        notesArea.setPrefHeight(50);

        HBox row1 = new HBox(10, new VBox(5, new Label("الخطورة:"), severityField), new VBox(5, new Label("الحالة:"), statusField));
        row1.setAlignment(Pos.CENTER_LEFT);

        Button saveCardBtn = new Button("حفظ هذا المرض");
        saveCardBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

        Button deleteCardBtn = new Button("حذف");
        deleteCardBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        saveCardBtn.setOnAction(e -> {
            try {
                // 1. تحديث الكائن بالقيم المكتوبة في الواجهة أولاً
                disease.setSeverity(severityField.getText().trim());
                disease.setStatus(statusField.getText().trim());
                disease.setContraindicated(foodField.getText().trim()); // ربط حقل الأطعمة
                disease.setNotes(notesArea.getText().trim());           // ربط حقل الملاحظات

                // 2. استدعاء السيرفيس للحفظ في الداتابيز
                PatientClientAPI saveService = ClientApiManager.getInstance().getPatientClientAPI();
                boolean success = saveService.saveNewpatientClient(client);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "تم التحديث ✔", "تم حفظ الملاحظات والأطعمة الممنوعة بنجاح.");
                    refreshCards(); // إعادة بناء البطاقات لعرض البيانات المحدثة
                } else {
                    showAlert(Alert.AlertType.ERROR, "خطأ في الحفظ ❌", "فشل تحديث قاعدة البيانات.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        deleteCardBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "هل تريد إزالة هذا المرض من الملف الطبي للمريض؟", ButtonType.YES, ButtonType.NO);
            confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    client.getChronicDiseases().remove(disease);
                    refreshCards();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "خطأ في الحذف ❌", "تعذر حذف السجل من قاعدة البيانات.");
                }
            }
        });

        HBox actionsBox = new HBox(10, saveCardBtn, deleteCardBtn);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(titleLabel, row1, new Label("الأطعمة الممنوعة:"), foodField, new Label("ملاحظات:"), notesArea, actionsBox);
        return card;
    }

    @FXML
    void handleAddNewCard(ActionEvent event) {

        // =====================================================
        // 1. التحقق من العميل
        // =====================================================

        if (client == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه",
                    "لا يوجد عميل محدد."
            );
            return;
        }

        // =====================================================
        // 2. فتح نافذة إدخال اسم المرض
        // =====================================================

        TextInputDialog dialog =
                new TextInputDialog();

        dialog.setTitle(
                "إضافة مرض مزمن جديد ➕"
        );

        dialog.setHeaderText(
                "أدخل اسم المرض لتضمينه في ملف العميل الطبي:"
        );

        dialog.setContentText(
                "اسم المرض:"
        );

        dialog.getDialogPane().setNodeOrientation(
                javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
        );

        Optional<String> result =
                dialog.showAndWait();

        // =====================================================
        // 3. معالجة الاسم المدخل
        // =====================================================

        result.ifPresent(diseaseName -> {

            String name =
                    diseaseName.trim();

            // التحقق من الاسم
            if (name.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "تنبيه ⚠",
                        "لا يمكن ترك اسم المرض فارغاً."
                );

                return;
            }

            // =================================================
            // 4. إنشاء ChronicDisease أولاً
            // =================================================

            ChronicDisease disease =
                    new ChronicDisease();

            disease.setDiseaseName(name);

            // =================================================
            // 5. إنشاء PatientChronicDisease
            // =================================================

            PatientChronicDisease newDisease =
                    new PatientChronicDisease();

            // وضع المرض داخل العلاقة
            newDisease.setChronicDisease(
                    disease
            );

            // =================================================
            // 6. البيانات الإضافية
            // =================================================

            newDisease.setSeverity(
                    "مستقر"
            );

            newDisease.setStatus(
                    "تحت المراقبة"
            );

            // =================================================
            // 7. إضافة المرض إلى العميل الحالي
            // =================================================

            if (client.getChronicDiseases() == null) {

                client.setChronicDiseases(
                        new ArrayList<>()
                );
            }

            client.getChronicDiseases()
                    .add(newDisease);

            // =================================================
            // 8. الحفظ في الخلفية
            // =================================================

            BackgroundRunner.run(
                    "جاري حفظ المرض ... ⏳",

                    () -> {

                        PatientClientAPI saveService =
                                ClientApiManager.getInstance().getPatientClientAPI();

                        boolean success =
                                saveService.saveNewpatientClient(
                                        client
                                );

                        if (!success) {

                            throw new Exception(
                                    "عذراً، فشل ترحيل البيانات إلى قاعدة البيانات. ❌"
                            );
                        }

                        System.out.println(
                                "✅ تم ترحيل المرض الجديد للداتابيز."
                        );

                        return true;
                    },

                    () -> {

                        // =================================================
                        // 9. تحديث الكروت بعد نجاح الحفظ
                        // =================================================

                        refreshCards();
                    }
            );
        });
    }
    @FXML void handleClose(ActionEvent event) { ((Stage) closeBtn.getScene().getWindow()).close(); }
}