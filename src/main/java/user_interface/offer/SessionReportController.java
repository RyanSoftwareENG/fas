package user_interface.offer;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import api.ClientApiManager;
import entities.Client;
import entities.Session;
import entities.SessionReport;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // 👈 إضافة الاستيراد
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import runner.BackgroundRunner;
import api.SessionReportAPI;
import pdfmanagement.PDFGenerator;
public class SessionReportController implements Initializable {

    @FXML private TextField txtClientName;
    @FXML private TextField txtClientID;
    @FXML private DatePicker dpSessionDate;
    @FXML private TextField txtNutritionist;

    @FXML private TextArea txtDiagnosis;
    @FXML private TextArea txtAssessment;
    @FXML private TextArea txtResults;
    @FXML private TextArea txtRecommendations;
    @FXML private TextArea txtGoals;
    @FXML private TextArea txtNotes;

    @FXML private DatePicker dpNextAppointment;
    @FXML private ComboBox<String> cmbCommitment;

    @FXML private Button btnClear;
    @FXML private Button btnPreview;
    @FXML private Button btnSave;
    @FXML private Button btnExport;
    private Client currentClient;
    private List<Session> clientSessions;

    // كائن تخزين لتحديد ما إذا كنا نعدل أو نضيف تقرير جديد[cite: 12]
    private SessionReport currentReport;
    private Long activeSessionId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // إدخال خيارات مستوى التزام العميل[cite: 12]
        cmbCommitment.setItems(FXCollections.observableArrayList(
                "High Commitment", "Moderate", "Low Commitment", "Uncommitted"
        ));

        dpSessionDate.setValue(LocalDate.now()); // التاريخ الحالي كافتراضي[cite: 12]
    }

    /**
     * دالة يتم استدعاؤها من الشاشة السابقة لتمرير بيانات الجلسة النشطة وملء بيانات العميل.[cite: 12]
     */
    public void setSessionContext(Client client,Session session) {
        this.activeSessionId = session.getId();
        this.txtClientID.setText(String.valueOf(client.getClientID()));
        this.txtClientName.setText(client.getFullName());
        this.currentClient = client;
        this.clientSessions = client.getSessions();

        // محاولة جلب التقرير إن كان مخزناً مسبقاً لعرضه (تعديل) بدلاً من إنشاء جديد[cite: 12]
        loadExistingReport(session.getId());
    }

    private void loadExistingReport(Long sessionId) {
        final SessionReport[] reportHolder = new SessionReport[1];

        // استخدام BackgroundRunner بدلاً من Thread لتجنب تجميد الواجهة[cite: 12]
        BackgroundRunner.run("جاري تحميل بيانات التقرير... ⏳", () -> {
            SessionReportAPI getService =  ClientApiManager.getInstance().getSessionReportAPI();
            reportHolder[0] = getService.getReportBySessionId(sessionId);
            return true;
        }, () -> {
            // العودة لخيط الواجهة لتحديث العناصر[cite: 12]
            SessionReport report = reportHolder[0];
            if (report != null) {
                this.currentReport = report;
                txtDiagnosis.setText(report.getDiagnosis());
                txtAssessment.setText(report.getAssessment());
                txtResults.setText(report.getSessionResults());
                txtRecommendations.setText(report.getRecommendations());
                txtGoals.setText(report.getNextGoals());
                txtNotes.setText(report.getNotes());
                txtNutritionist.setText(report.getNutritionistName());
                dpNextAppointment.setValue(report.getNextAppointment());
                cmbCommitment.setValue(report.getCommitmentLevel());
                btnSave.setText("Update Report"); // تحويل الزر لوضع التعديل[cite: 12]
            }
        });
    }

    @FXML
    private void handleSaveReport() {
        if (activeSessionId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "No active session context selected.");
            return;
        }

        // بناء أو تحديث الكائن من مدخلات الشاشة[cite: 12]
        if (currentReport == null) {
            currentReport = new SessionReport();
            currentReport.getSession().setId(activeSessionId);
        }

        // قراءة البيانات من الواجهة على الخيط الرئيسي (Main Thread) لمنع الأخطاء[cite: 12]
        currentReport.setDiagnosis(txtDiagnosis.getText());
        currentReport.setAssessment(txtAssessment.getText());
        currentReport.setSessionResults(txtResults.getText());
        currentReport.setRecommendations(txtRecommendations.getText());
        currentReport.setNextGoals(txtGoals.getText());
        currentReport.setNotes(txtNotes.getText());
        currentReport.setNutritionistName(txtNutritionist.getText());
        currentReport.setNextAppointment(dpNextAppointment.getValue());
        currentReport.setCommitmentLevel(cmbCommitment.getValue());

        // تفعيل شريط تحميل وتعطيل الأزرار مؤقتاً لحين انتهاء المعالجة في الخلفية[cite: 12]
        btnSave.setDisable(true);

        final boolean[] successHolder = new boolean[1];
        final String[] messageHolder = new String[1];

        // تنفيذ عملية الحفظ في الخلفية لتجنب التجميد[cite: 12]
        BackgroundRunner.run("جاري حفظ التقرير... ⏳", () -> {
            try {
                if (currentReport.getReportId() > 0) {
                    // عملية تحديث[cite: 12]
                    SessionReportAPI updateService = ClientApiManager.getInstance().getSessionReportAPI();
                    successHolder[0] = updateService.updateReport(currentReport);
                    messageHolder[0] = successHolder[0] ? "Report updated successfully!" :" updateService.getExceptionMessage()";
                } else {
                    // عملية حفظ جديدة[cite: 12]
                    SessionReportAPI saveService = ClientApiManager.getInstance().getSessionReportAPI();
                    successHolder[0] = saveService.saveReport(currentReport);
                    messageHolder[0] = successHolder[0] ? "Report saved successfully!" : saveService.getExceptionMessage();
                }
            } catch (Exception e) {
                successHolder[0] = false;
                messageHolder[0] = e.getMessage();
            }
            return true;
        }, () -> {
            // إعادة تفعيل الزر وعرض رسالة النتيجة على واجهة المستخدم[cite: 12]
            btnSave.setDisable(false);
            if (successHolder[0]) {
                showAlert(Alert.AlertType.INFORMATION, "Success", messageHolder[0]);
                if (currentReport.getReportId() > 0) btnSave.setText("Update Report");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", messageHolder[0] != null ? messageHolder[0] : "Unexpected Error occurred.");
            }
        });
    }

    @FXML
    private void handleClearForm() {
        txtDiagnosis.clear();
        txtAssessment.clear();
        txtResults.clear();
        txtRecommendations.clear();
        txtGoals.clear();
        txtNotes.clear();
        txtNutritionist.clear();
        dpNextAppointment.setValue(null);
        cmbCommitment.getSelectionModel().clearSelection();
    }

    @FXML
    private void handlePreviewReport() {
        if (currentClient == null || clientSessions == null || clientSessions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "لا توجد بيانات كافية لإنشاء معاينة حالياً!");
            return;
        }

        try {
            // 1. إنشاء ملف مؤقت ينتهي بـ .pdf ويحذف تلقائياً عند إغلاق التطبيق[cite: 12]
            File tempPreviewFile = File.createTempFile("تقرير_معاينة_", ".pdf");
            tempPreviewFile.deleteOnExit();

            // 2. توليد التقرير داخل الملف المؤقت[cite: 12]
            PDFGenerator.createComprehensiveReport(currentClient, clientSessions, currentReport,tempPreviewFile.getAbsolutePath());

            // 3. فتح الملف باستخدام تطبيق قراءة الـ PDF الافتراضي في الجهاز (مثل المتصفح أو Adobe Reader)[cite: 12]
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempPreviewFile);
            } else {
                showAlert(Alert.AlertType.ERROR, "خطأ في النظام", "نظام التشغيل لا يدعم فتح الملفات تلقائياً.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "خطأ أثناء المعاينة", "حدث خطأ أثناء إعداد المعاينة السريعة: " + e.getMessage());
        }
    }

    /**
     * تصدير التقرير النهائي بصيغة PDF[cite: 12]
     * يتيح للمستخدم اختيار مكان الحفظ واسم الملف بكل مرونة
     */
    @FXML
    private void handleExportPDF() {
        if (currentClient == null || clientSessions == null || clientSessions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "تنبيه",
                    "لا توجد بيانات كافية لتصدير التقرير!");
            return;
        }

        // بناء اسم نظيف للعميل
        String cleanClientName = buildCleanName(currentClient);

        // التعديل هنا: مجلد الحفظ أصبح مساراً نسبياً داخل مجلد المشروع
        File pdfDir = new File("data/pdf");

        // إنشاء المجلد إذا لم يكن موجوداً
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        // إضافة رقم الجلسة أو العميل لاسم الملف (اختياري) لتجنب الكتابة فوق الملفات القديمة
        String fileName = cleanClientName + "_Session_" + activeSessionId + ".pdf";
        String path = new File(pdfDir, fileName).getAbsolutePath();

        try {
            PDFGenerator.createComprehensiveReport(
                    currentClient,
                    clientSessions,
                    currentReport,
                    path);

            showAlert(Alert.AlertType.INFORMATION,
                    "تم التصدير بنجاح",
                    "🎉 تم حفظ التقرير بنجاح في المسار:\n" );

        } catch (Exception e) {
            e.printStackTrace();

            showAlert(Alert.AlertType.ERROR,
                    "فشل التصدير",
                    "حدثت مشكلة أثناء إنشاء ملف PDF:\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // بناء اسم نظيف للعميل يصلح كاسم ملف: يتجاهل الأجزاء الفارغة، ويحذف أي حرف غير مسموح في أسماء الملفات
    private String buildCleanName(Client client) {
        if (client.getFullName() == null) return "تقرير_غير_معروف";

        StringBuilder sb = new StringBuilder();
        for (String part : client.getFullName().split(" ")) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part.trim());
            }
        }

        String name = sb.length() > 0 ? sb.toString() : "تقرير_غير_معروف";
        // إزالة أي رموز قد تسبب مشاكل في أسماء الملفات على Windows أو Linux
        return name.replaceAll("[\\\\/:*?\"<>|]", "");
    }
}