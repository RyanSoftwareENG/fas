package user_interface.offer;

import api.ClientApiManager;
import dto.SessionListDTO;
import javafx.concurrent.Task;
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
import api.SessionAPI;

import java.io.IOException;

import static user_interface.forminput.Desgin.showAlert;

public class SessionsCardController {

    @FXML private Label date;
    @FXML private Label sessionId;
    @FXML private Label nameCustomerSession;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private SessionListDTO currentSession;
    private SessionListController parentController;

    public void setSessionData(SessionListDTO session, SessionListController parentController) {
        this.currentSession = session;
        this.parentController = parentController;

        if (session != null) {
            System.out.println("Sessions Id: " + session.getId()); // تم نقله لداخل التحقق لتجنب الـ NullPointerException
            sessionId.setText("#" + session.getId());

            nameCustomerSession.setText(String.valueOf(currentSession.getClientName()));

            String dateText = (session.getUploadTime() != null)
                    ? session.getUploadTime().toLocalDate().toString()
                    : "غير محدد";
            date.setText(dateText);
            System.out.println(sessionId.getText()+'\n'+nameCustomerSession.getText()+'\n'+date.getText());
        }
    }

    @FXML
    void handleView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SessionData.fxml"));
        Parent root = loader.load();

        SessionDataController controller = loader.getController();
        controller.setSessionData(currentSession);

        Stage stage = new Stage();
        stage.setTitle("الجلسات");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void handleEdit(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SessionEdit.fxml"));
        Parent root = loader.load();

        SessionEditController controller = loader.getController();
        controller.setSessionData(currentSession);

        Stage stage = new Stage();
        stage.setTitle("تعديل الجلسات");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void handleDelete(ActionEvent event) {
        try {
            Long sessionId = currentSession.getId();

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("تأكيد الحذف ⚠️");
            confirmAlert.setHeaderText("هل تريد حذف الجلسة رقم (" + sessionId + ")؟");
            confirmAlert.setContentText("تنبيه: سيقوم النظام بحذف هذه الجلسة نهائياً مع كافة بياناتها البدنية والخطة الغذائية والفحوصات التابعة لها.");

            confirmAlert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

            java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                System.out.println("تأكيد الحذف للجلسة رقم: " + sessionId);

                // 🚀 إنشاء الـ Background Runner (Task) للتعامل مع قاعدة البيانات في خيط منفصل
                Task<Boolean> deleteWorker = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        SessionAPI delete = ClientApiManager.getInstance().getSessionAPI();
                        return delete.deleteSession(sessionId);
                    }
                };

                // 🟢 في حال نجاح العملية في الخلفية دون أخطاء
                deleteWorker.setOnSucceeded(workerStateEvent -> {
                    boolean isDeleted = deleteWorker.getValue();
                    if (isDeleted) {
                        showAlert(Alert.AlertType.INFORMATION, "تم الحذف ✔", "تم حذف الجلسة رقم (" + sessionId + ") بنجاح.");
                        if (parentController != null) {
                            parentController.refreshList();
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "فشلت العملية ❌", "تعذر حذف الجلسة من قاعدة البيانات.");
                    }
                });

                // 🔴 في حال حدوث خطأ أو استثناء أثناء معالجة قاعدة البيانات في الخلفية
                deleteWorker.setOnFailed(workerStateEvent -> {
                    Throwable exception = deleteWorker.getException();
                    if (exception instanceof java.sql.SQLException) {
                        showAlert(Alert.AlertType.ERROR, "خطأ اتصال 🔌", "تعذر فتح اتصال آمن مع قاعدة البيانات لإتمام الحذف.");
                    } else {
                        System.err.println("⚠️ استثناء أثناء محاولة الحذف في الخلفية:");
                        exception.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "خطأ غير متوقع ❌", "حدث خطأ داخلي في النظام.");
                    }
                });

                // ⚙️ تشغيل الـ Thread الخلفي
                Thread thread = new Thread(deleteWorker);
                thread.setDaemon(true); // لضمان إغلاق الخيط عند إغلاق التطبيق
                thread.start();
            }
        } catch (Exception e) {
            System.err.println("⚠️ استثناء أثناء معالجة طلب الحذف الرئيسي:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "خطأ غير متوقع ❌", "حدث خطأ غير متوقع أثناء معالجة الطلب.");
        }
    }
}