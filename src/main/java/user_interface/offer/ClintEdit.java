package user_interface.offer;

import api.ClientApiManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import entities.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import runner.BackgroundRunner;
import api.ClientAPI;
import java.io.IOException;
import java.time.LocalDate;

import static user_interface.forminput.Desgin.showAlert;

public class ClintEdit {

    @FXML private TextField nameClientEdit;
    @FXML private ToggleGroup genderGroup;
    @FXML private RadioButton male;
    @FXML private RadioButton female;
    @FXML private DatePicker brithDate;
    @FXML private TextField phoneNumber;

    @FXML private Button lifeStyleBtn;
    @FXML private Button healthBtn;
    @FXML private Button saveCLientEditBtn;
    @FXML private Button backClientEdit;

    // عناصر المريض الجديدة المرتبطة بالـ FXML
    @FXML private HBox patientButtonsBox;
    @FXML private Button diseaseBtn;
    @FXML private Button allergyBtn;

    private Client client;
    private ClintList parentController;

    /**
     * تهيئة بيانات العميل بداخل الـ BackgroundRunner لمنع حدوث Lag عند فتح الشاشة
     */
    public void setClientData(Client incomingClient, ClintList parentController) {
        this.parentController = parentController;

        System.out.println("DEBUG: Incoming Client ID = " + incomingClient.getClientID());

        // جلب البيانات الإضافية في الخلفية قبل تعبئتها بالواجهة
        BackgroundRunner.run("جاري تحميل تفاصيل العميل... ⏳", () -> {
            ClientAPI getClient = ClientApiManager.getInstance().getClientAPI();
            this.client = getClient.populateAdditionalData(incomingClient);

            if (this.client != null) {
                this.client.setClientID(incomingClient.getClientID());
            }
            return true;
        }, () -> {
            // تعبئة البيانات في عناصر التحكم داخل خيط الواجهة بعد الانتهاء
            if (this.client != null) {
                System.out.println("DEBUG: Populated Patient ID = " + this.client.getClientID());

                nameClientEdit.setText(this.client.getFullName());

                if (this.client.getBirthDate() != null) {
                    brithDate.setValue(this.client.getBirthDate());
                }
                if (this.client.getContactNumber() != null) {
                    phoneNumber.setText(this.client.getContactNumber());
                }
                if (this.client.getGender() == 'M') {
                    male.setSelected(true);
                } else if (this.client.getGender() == 'F') {
                    female.setSelected(true);
                }
            }
        });
    }

    /**
     * معالجة وحفظ البيانات المعدلة في قاعدة البيانات في الخلفية
     */
    @FXML
    void handleSaveClientEditBtn(ActionEvent event) {
        if (client == null) return;

        String updatedName = nameClientEdit.getText().trim();
        LocalDate updatedBirth = brithDate.getValue();
        String updatedPhone = phoneNumber.getText().trim();

        char updatedGender =
                male.isSelected()
                        ? 'M'
                        : (female.isSelected() ? 'F' : 'M');

        if (updatedName.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه ⚠️",
                    "يرجى كتابة اسم العميل."
            );
            return;
        }

        // تحديث الكائن محلياً
        client.setFullName(updatedName);
        client.setBirthDate(updatedBirth);
        client.setContactNumber(updatedPhone);
        client.setGender(updatedGender);

        BackgroundRunner.run(
                "جاري حفظ التعديلات... 💾",
                () -> {

                    ClientAPI updateService = ClientApiManager.getInstance().getClientAPI();

                    boolean isUpdated =
                            updateService.updateClient(client);

                    if (!isUpdated) {
                        throw new Exception(
                                "فشل تحديث بيانات العميل في قاعدة البيانات."
                        );
                    }

                    return true;

                },
                () -> {

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "تم التعديل ✔",
                            "تم حفظ التعديلات بنجاح."
                    );

                    if (parentController != null) {
                        parentController.refreshList();
                    }

                    closeWindow();
                }
        );
    }
    /**
     * العودة وإغلاق النافذة الحالية
     */
    @FXML
    void handleBackClint(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        if (backClientEdit != null && backClientEdit.getScene() != null) {
            Stage stage = (Stage) backClientEdit.getScene().getWindow();
            stage.close();
        }
    }

    // =================================================================================
    // 1. فتح لوحة تحكم العميل الشاملة
    // =================================================================================
    @FXML
    void handleOfferClient(ActionEvent event) {
        if (client == null) return;

        BackgroundRunner.run("جاري تحميل لوحة تحكم العميل... ⏳", () -> {
           ClientAPI getClientService = ClientApiManager.getInstance().getClientAPI();
            client = getClientService.populateAdditionalData(client);

            if (client == null) {
                throw new Exception("عذراً، فشل جلب بيانات العميل من قاعدة البيانات.");
            }
            return true;
        }, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientProfileView.fxml"));
                Parent root = loader.load();

                ClientDashboardViewController controller = loader.getController();
                controller.setClientFullDashboardData(client);

                Stage stage = new Stage();
                stage.setTitle("لوحة التحكم والمؤشرات السريرية للعميل: " + client.getFullName());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.err.println("❌ فشل بناء واجهة لوحة التحكم: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "خطأ في النظام ❌", "تعذر تحميل شاشة لوحة تحكم العميل.");
            }
        });
    }

    // =================================================================================
    // 2. تعديل بيانات العميل
    // =================================================================================
    @FXML
    void handleEditClient(ActionEvent event) {
        if (client == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientEdit.fxml"));
            Parent root = loader.load();

            ClintEdit editController = loader.getController();
            editController.setClientData(client, parentController);

            Stage stage = new Stage();
            stage.setTitle("تعديل بيانات العميل");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ فشل فتح شاشة التعديل: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "خطأ في النظام ❌", "تعذر فتح شاشة تعديل البيانات.");
        }
    }

    // =================================================================================
    // 3. حذف العميل نهائياً
    // =================================================================================
    @FXML
    void handleDeleteClient(ActionEvent event) {
        if (client == null) return;

        String clientName = client.getFirstName();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأكيد الحذف ⚠️");
        confirmAlert.setHeaderText("هل تريد حذف العميل (" + clientName + ")؟");
        confirmAlert.setContentText("تنبيه: سيقوم النظام بحذف هذا العميل نهائياً مع كافة سجلاته الطبية والبدنية والجلسات التابعة له.");
        confirmAlert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            BackgroundRunner.run("جاري حذف العميل وسجلاته الطبية نهائياً... ⏳", () -> {
                ClientAPI delete = ClientApiManager.getInstance().getClientAPI();
                boolean isDeleted = delete.deleteClient(client.getClientID());

                if (!isDeleted) {
                    throw new Exception("عذراً، فشل إتمام الحذف.");
                }

                return true;
            }, () -> {
                if (parentController != null) {
                    parentController.refreshList();
                }
                closeWindow(); // إغلاق نافذة التعديل فور حذف العميل
            });
        }
    }

    // =================================================================================
    // 4. فتح واجهة التقرير البياني
    // =================================================================================
    @FXML
    void handleClientReport(ActionEvent event) {
        if (client == null) return;

        BackgroundRunner.run("جاري تحليل الجلسات وتجهيز التقرير البياني... ⏳", () -> {
            ClientAPI getClientService = ClientApiManager.getInstance().getClientAPI();
            client = getClientService.populateAdditionalData(client);

            if (client == null) {
                throw new Exception("تعذر جلب بيانات العميل التخطيطية من قاعدة البيانات.");
            }

            if (client.getSessions() == null || client.getSessions().isEmpty()) {
                throw new Exception("العميل الحالي لا يملك أي جلسات مسجلة لإنشاء تقرير بياني لها. ⚠️");
            }

            return true;
        }, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientReportView.fxml"));
                Parent root = loader.load();

                ClientReportViewController controller = loader.getController();
                controller.setClientData(client);

                Stage stage = new Stage();
                stage.setTitle("التقرير التحليلي المتقدم لتطور العميل: " + client.getFullName());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.show();
            } catch (IOException e) {
                System.err.println("❌ فشل في فتح واجهة التقارير البيانية للعميل: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "خطأ في فتح الواجهة ❌", "تعذر تحميل التقرير البياني للعميل.");
            }
        });
    }

    // =========================================================
// دوال FXML
// =========================================================

    @FXML
    void handleNameClientEdit(ActionEvent event) {
    }

    @FXML
    void handleMale(ActionEvent event) {
    }

    @FXML
    void handleFemale(ActionEvent event) {
    }

    @FXML
    void handleBrithDate(ActionEvent event) {
    }

    @FXML
    void handlePhoneNumber(ActionEvent event) {
    }


// =========================================================
// أزرار البيانات الإضافية
// =========================================================

    @FXML
    void handleLifeStyleBtn(ActionEvent event) {
        if (client == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LifeStyleEdit.fxml"));
            Parent root = loader.load();

            // ربط الكنترولر وتمرير البيانات
            LifeStyleEditController controller = loader.getController();
            controller.setClientData(client);

            Stage stage = new Stage();
            stage.setTitle("تعديل بيانات نمط الحياة");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ فشل فتح شاشة نمط الحياة: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "خطأ ❌", "تعذر فتح شاشة نمط الحياة.");
        }
    }

    @FXML
    void handleHealthBtn(ActionEvent event) {
        if (client == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HealthDataEdit.fxml"));
            Parent root = loader.load();

            // ربط الكنترولر وتمرير البيانات
            HealthDataEditController controller = loader.getController();
            controller.setHealthData(client);

            Stage stage = new Stage();
            stage.setTitle("تعديل البيانات الصحية");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ فشل فتح شاشة البيانات الصحية: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "خطأ ❌", "تعذر فتح شاشة البيانات الصحية.");
        }
    }

    @FXML
    void handleDiseaseBtn(ActionEvent event) {
        if (client == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChronicDiseaseEdit.fxml"));
            Parent root = loader.load();

            // ربط الكنترولر وتمرير البيانات
            ChronicDiseaseEditController controller = loader.getController();
            controller.setPatientData(client);

            Stage stage = new Stage();
            stage.setTitle("تعديل الأمراض المزمنة");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ فشل فتح شاشة الأمراض المزمنة: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "خطأ ❌", "تعذر فتح شاشة الأمراض المزمنة.");
        }
    }

    @FXML
    void handleAllergyBtn(ActionEvent event) {
        if (client == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientAllergyEdit.fxml"));
            Parent root = loader.load();

            // ربط الكنترولر وتمرير البيانات
            ClientAllergyEditController controller = loader.getController();
            controller.setPatientData(client);

            Stage stage = new Stage();
            stage.setTitle("تعديل الحساسية");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ فشل فتح شاشة الحساسية: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "خطأ ❌", "تعذر فتح شاشة الحساسية.");
        }
    }


}