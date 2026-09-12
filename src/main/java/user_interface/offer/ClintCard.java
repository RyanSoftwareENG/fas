package user_interface.offer;

import api.ClientApiManager;
import entities.Client;
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
import api.ClientAPI;

public class ClintCard {

    @FXML private Label gender;
    @FXML private Label age;
    @FXML private Label nameCustomer;

    @FXML private Button offerClient;
    @FXML private Button editClient;
    @FXML private Button deleteClient;

    // حقل الإدخال لزر التقرير البياني
    @FXML private Button btnReport;

    private Client currentClient;
    private ClintList parentController;

    // دالة لاستقبال كائن العميل الفعلي وتعبئة البيانات في الواجهة
    public void setClientData(Client client, ClintList parentController) {
        this.currentClient = client;
        this.parentController = parentController;

        if (client != null) {
            // 1. تجميع الاسم من مصفوفة الأسماء وتأمين الـ Null
            if (client.getFullName() != null ) {
                nameCustomer.setText(client.getFirstName() + " " + client.getLastName());
            } else {
                nameCustomer.setText("عميل غير معروف");
            }

            // 2. حساب العمر
            age.setText(client.getAge() + " سنة");

            // 3. تحديد نص الجنس
            if (client.getGender() == 'M' || client.getGender() == 'ذ') {
                gender.setText("ذكر");
            } else if (client.getGender() == 'F' || client.getGender() == 'ن' || client.getGender() == 'أ') {
                gender.setText("أنثى");
            } else {
                gender.setText(String.valueOf(client.getGender()));
            }

            System.out.println("✅ تم إرسال البيانات للواجهة بنجاح - الاسم: " + client.getModificationDate());
        }
    }
    // =================================================================================
    // 1. فتح لوحة تحكم العميل الشاملة (جلب بيانات ثقيلة + تحميل FXML في الخلفية)
    // =================================================================================
    @FXML
    void handleOfferClient(ActionEvent event) {
        if (currentClient == null) return;

        // مصفوفات لنقل كائنات الواجهة من خيط الخلفية إلى خيط الواجهة الرئيسي بأمان
        final Client[] loadedClientHolder = new Client[1];
        final Parent[] rootHolder = new Parent[1];
        final ClientDashboardViewController[] controllerHolder = new ClientDashboardViewController[1];


        BackgroundRunner.run("جاري تحميل لوحة تحكم العميل... ⏳", () -> {
            // أ) جلب البيانات الإضافية من قاعدة البيانات (عملية ثقيلة)
            ClientAPI getClientService = ClientApiManager.getInstance().getClientAPI();
            loadedClientHolder[0] =
                    getClientService.populateAdditionalData(currentClient);


            if (loadedClientHolder[0] == null) {
                throw new Exception("عذراً، فشل جلب بيانات العميل من قاعدة البيانات.");
            }

            // ب) تحميل ملف الـ FXML في الخلفية لتفادي أي تجميد لحظي
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientProfileView.fxml"));
            rootHolder[0] = loader.load();
            controllerHolder[0] = loader.getController();

            // ج) تمرير البيانات للكنترولر الجديد
            controllerHolder[0].setClientFullDashboardData(loadedClientHolder[0]);

            return true;
        }, () -> {
            // د) فتح النافذة وعرضها (يجب أن يتم فقط في خيط الواجهة الرئيسي)
            if (rootHolder[0] != null && loadedClientHolder[0] != null) {
                Stage stage = new Stage();
                stage.setTitle("لوحة التحكم والمؤشرات السريرية للعميل: " + loadedClientHolder[0].getClientID());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(rootHolder[0]));
                stage.show();
            }
        });
    }

    // =================================================================================
    // 2. تعديل بيانات العميل (تحميل ملف الـ FXML فقط في الخلفية)
    // =================================================================================
    @FXML
    void handleEditClient(ActionEvent event) {
        if (currentClient == null) return;

        final Parent[] rootHolder = new Parent[1];
        final ClintEdit[] editControllerHolder = new ClintEdit[1];

        BackgroundRunner.run("جاري تحميل واجهة التعديل... ⏳", () -> {
            // تحميل ملف الـ FXML وتجهيز الكنترولر في الخلفية
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientEdit.fxml"));
            rootHolder[0] = loader.load();
            editControllerHolder[0] = loader.getController();

            editControllerHolder[0].setClientData(currentClient, parentController);
            return true;
        }, () -> {
            // إظهار نافذة التعديل فور اكتمال التجهيز في خيط الواجهة
            if (rootHolder[0] != null) {
                Stage stage = new Stage();
                stage.setTitle("تعديل بيانات العميل");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(rootHolder[0]));
                stage.show();
            }
        });
    }

    // =================================================================================
    // 3. حذف العميل نهائياً (الحذف من قاعدة البيانات في الخلفية بعد تأكيد المستخدم)
    // =================================================================================
    @FXML
    void handleDeleteClient(ActionEvent event) {
        if (currentClient == null) return;

        String clientName = nameCustomer.getText();

        // أ) إظهار نافذة تأكيد الحذف (يجب أن تظهر في خيط الواجهة أولاً)
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأكيد الحذف ⚠️");
        confirmAlert.setHeaderText("هل تريد حذف العميل (" + clientName + ")؟");
        confirmAlert.setContentText("تنبيه: سيقوم النظام بحذف هذا العميل نهائياً مع كافة سجلاته الطبية والبدنية والجلسات التابعة له.");
        confirmAlert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();

        // ب) إذا وافق المستخدم، نقوم بالحذف في الخلفية عبر الـ BackgroundRunner
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {

            BackgroundRunner.run("جاري حذف العميل وسجلاته الطبية نهائياً... ⏳", () -> {
                ClientAPI delete = ClientApiManager.getInstance().getClientAPI();
                boolean isDeleted = delete.deleteClient(currentClient.getClientID());

                // رمي الاستثناء ليقوم الـ BackgroundRunner بالتقاطه وعرض التنبيه تلقائياً للمستخدم

                if (!isDeleted) {
                    throw new Exception("عذراً، فشل إتمام الحذف.");
                }

                return true;
            }, () -> {
                // ج) تحديث قائمة العملاء في الشاشة الرئيسية (تلقائياً في خيط الواجهة بعد النجاح)
                if (parentController != null) {
                    parentController.refreshList();
                }
            });
        }
    }

    // =================================================================================
    // 4. فتح واجهة التقرير البياني (التحقق والتحميل وجلب الجلسات في الخلفية)
    // =================================================================================
    @FXML
    void handleClientReport(ActionEvent event) {
        if (currentClient == null) return;

        final Client[] loadedClientHolder = new Client[1];
        final Parent[] rootHolder = new Parent[1];
        final ClientReportViewController[] controllerHolder = new ClientReportViewController[1];

        BackgroundRunner.run("جاري تحليل الجلسات وتجهيز التقرير البياني... ⏳", () -> {
            // أ) جلب الزيارات والتحاليل من قاعدة البيانات في الخلفية
            ClientAPI getClientService = ClientApiManager.getInstance().getClientAPI();
            loadedClientHolder[0] = getClientService.populateAdditionalData(currentClient);

            if (loadedClientHolder[0] == null) {
                throw new Exception("تعذر جلب بيانات العميل التخطيطية من قاعدة البيانات.");
            }

            // ب) التحقق من وجود جلسات (إذا لم توجد يرمي تنبيهاً آمناً للمستخدم)
            if (loadedClientHolder[0].getSessions() == null || loadedClientHolder[0].getSessions().isEmpty()) {
                throw new Exception("العميل الحالي لا يملك أي جلسات مسجلة لإنشاء تقرير بياني لها. ⚠️");
            }

            // ج) تحميل ملف الـ FXML للتقرير في الخلفية
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientReportView.fxml"));
            rootHolder[0] = loader.load();
            controllerHolder[0] = loader.getController();

            controllerHolder[0].setClientData(loadedClientHolder[0]);
            return true;
        }, () -> {
            // د) إظهار شاشة التقرير البياني الذكية للمستخدم فور انتهاء التحضير
            if (rootHolder[0] != null && loadedClientHolder[0] != null) {
                Stage stage = new Stage();
                stage.setTitle("التقرير التحليلي المتقدم لتطور العميل: " + loadedClientHolder[0].getFullName());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(rootHolder[0]));
                stage.setResizable(true);
                stage.show();
            }
        });
    }
}