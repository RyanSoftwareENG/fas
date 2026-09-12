package user_interface.offer;

import api.ClientApiManager;
import entities.Allergy;
import entities.Client;
import entities.PatientAllergy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import runner.BackgroundRunner;
import api.PatientClientAPI;
import static user_interface.forminput.Desgin.showAlert;

import java.util.ArrayList;
import java.util.Optional;

public class ClientAllergyEditController {

    @FXML private VBox allergyCardsContainer;
    @FXML private Button closeBtn;

    private Client patient;

    // تعديل بيانات الحساسية
    public void setPatientData(Client patient) {
        this.patient = patient;

        // تأمين القائمة لتكون قابلة للإضافة والحذف والتعديل (Mutable)
        if (this.patient != null) {
            if (this.patient.getAllergies() == null) {
                this.patient.setAllergies(new ArrayList<>());
            } else {
                this.patient.setAllergies(new ArrayList<>(this.patient.getAllergies()));
            }
        }
        refreshAllergyCards();
    }

    // تحديث الواجهة
    private void refreshAllergyCards() {
        // تفريغ الحاوية دائماً (إذا كان العميل عادياً وبدون حساسية، ستظل الواجهة فارغة من البطاقات بسلاسة)
        allergyCardsContainer.getChildren().clear();
        if (patient == null || patient.getAllergies() == null) return;

        for (PatientAllergy allergy : patient.getAllergies()) {
            VBox card = createAllergyCard(allergy);
            allergyCardsContainer.getChildren().add(card);
        }
    }

    // انشاء حاوية الحساسية
    private VBox createAllergyCard(PatientAllergy allergy) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-radius: 8px; "
                + "-fx-background-radius: 8px; -fx-padding: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        Label titleLabel = new Label("الحساسية: " + allergy.getAllergy().getAllergyName());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2f3640;");

        TextField severityField = new TextField(allergy.getSeverity());
        severityField.setPromptText("الشدة (Severity)");

        TextField statusField = new TextField(allergy.getStatus());
        statusField.setPromptText("الحالة (Status)");

        TextField foodField = new TextField(allergy.getContraindicated());
        foodField.setPromptText("الأطعمة المسببة للتهيج");

        TextArea notesArea = new TextArea(allergy.getNotes());
        notesArea.setPromptText("ملاحظات إضافية...");
        notesArea.setPrefHeight(50);

        HBox fieldsRow = new HBox(10, new VBox(5, new Label("الشدة:"), severityField), new VBox(5, new Label("الحالة:"), statusField));
        fieldsRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("حفظ");
        saveBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

        Button deleteBtn = new Button("حذف");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        saveBtn.setOnAction(e -> {
            try {
                // نقل البيانات من عناصر الواجهة إلى الكائن قبل إرساله للداتابيز
                allergy.setSeverity(severityField.getText().trim());
                allergy.setStatus(statusField.getText().trim());
                allergy.setContraindicated(foodField.getText().trim()); // التقاط الوجبات الممنوعة
                allergy.setNotes(notesArea.getText().trim());           // التقاط الملاحظات

                PatientClientAPI saveService = ClientApiManager.getInstance().getPatientClientAPI();
                boolean success = saveService.saveNewpatientClient(patient);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "تم الحفظ ✔", "تم تحديث الملاحظات والأطعمة المسببة للحساسية بنجاح.");
                    refreshAllergyCards();
                } else {
                    showAlert(Alert.AlertType.ERROR, "خطأ في الحفظ ❌", "تعذر ترحيل البيانات.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "هل أنت متأكد من حذف هذه الحساسية من سجل المريض؟", ButtonType.YES, ButtonType.NO);
            confirm.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    patient.getAllergies().remove(allergy);
                    // allergyDAO.deletePatientAllergy(patient.getClientID(), allergy.getIdAllergy());
                    refreshAllergyCards();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "خطأ في الحذف ❌", "تعذر إزالة الحساسية من قاعدة البيانات.");
                }
            }
        });

        HBox controls = new HBox(10, saveBtn, deleteBtn);
        controls.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(titleLabel, fieldsRow, new Label("الأطعمة المسببة للحساسية:"), foodField, new Label("ملاحظات:"), notesArea, controls);
        return card;
    };

    @FXML
    void handleAddNewAllergy(ActionEvent event) {

        if (patient == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("إضافة حساسية جديدة ➕");
        dialog.setHeaderText(
                "يرجى إدخال اسم الحساسية المراد إضافتها للمريض:"
        );
        dialog.setContentText("اسم الحساسية:");

        // دعم اتجاه اللغة العربية
        dialog.getDialogPane().setNodeOrientation(
                javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
        );

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(allergyName -> {

            // ============================================
            // التحقق من الاسم
            // ============================================

            String name = allergyName.trim();

            if (name.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "تنبيه ⚠",
                        "لا يمكن ترك اسم الحساسية فارغاً."
                );

                return;
            }

            // ============================================
            // إنشاء Allergy
            // ============================================

            Allergy allergy = new Allergy();

            allergy.setAllergyName(name);

            // ============================================
            // إنشاء PatientAllergy
            // ============================================

            PatientAllergy newAllergy =
                    new PatientAllergy();

            newAllergy.setAllergy(allergy);

            // ============================================
            // البيانات الإضافية
            // ============================================

            newAllergy.setSeverity("متوسطة");
            newAllergy.setStatus("نشط");

            // ============================================
            // إضافتها إلى قائمة المريض
            // ============================================

            patient.getAllergies().add(newAllergy);

            // ============================================
            // الحفظ في الخلفية
            // ============================================

            try {

                BackgroundRunner.run(
                        "جاري حفظ الحساسية ... ⏳",

                        () -> {

                            PatientClientAPI saveClient = ClientApiManager.getInstance().getPatientClientAPI();

                            boolean isSaved =
                                    saveClient.saveNewpatientClient(
                                            patient
                                    );

                            if (!isSaved) {

                                throw new Exception(
                                        "فشل تسجيل الحساسية الجديدة في قاعدة البيانات. ❌"
                                );
                            }

                            return true;
                        },

                        () -> {

                            refreshAllergyCards();
                        }
                );

            } catch (Exception ex) {

                ex.printStackTrace();

                showAlert(
                        Alert.AlertType.ERROR,
                        "خطأ",
                        "حدث خطأ أثناء حفظ الحساسية."
                );
            }
        });
    }

    @FXML void handleClose(ActionEvent event) { ((Stage) closeBtn.getScene().getWindow()).close(); }
}