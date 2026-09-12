package user_interface.offer;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import api.ClientApiManager;
import entities.GeneratedReport;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import api.SessionReportAPI;

public class ReportCardController {

    @FXML
    private Label clientNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label reportIdLabel;

    private GeneratedReport report;
    private ReportsListController parentController;

    public void setReportData(GeneratedReport report, ReportsListController parentController) {
        this.report = report;
        this.parentController = parentController;

        clientNameLabel.setText(report.getClientName());
        reportIdLabel.setText("#" + report.getReportId());

        if (report.getGeneratedDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            dateLabel.setText(report.getGeneratedDate().format(formatter));
        } else {
            dateLabel.setText("غير معروف");
        }
    }

    @FXML
    private void handleOpen() {
        File pdfFile = new File(report.getFilePath());

        if (!pdfFile.exists()) {
            showAlert("الملف غير موجود",
                    "لم يتم العثور على الملف في المسار المسجل:\n");
            return;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Windows: استخدام cmd للفتح بالبرنامج الافتراضي
                pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", pdfFile.getAbsolutePath());
            } else if (os.contains("mac")) {
                // macOS
                pb = new ProcessBuilder("open", pdfFile.getAbsolutePath());
            } else {
                // Linux وما شابهه: xdg-open هو المعيار القياسي على جميع بيئات سطح المكتب
                pb = new ProcessBuilder("xdg-open", pdfFile.getAbsolutePath());
            }

            pb.start();

        } catch (Exception e) {
            // في حال فشل فتح الملف تلقائياً لأي سبب، نعرض المسار للمستخدم ليفتحه يدوياً
            showPathFallback(pdfFile.getAbsolutePath());
        }
    }

    private void showPathFallback(String path) {
        TextInputDialog dialog = new TextInputDialog(path);
        dialog.setTitle("تعذر فتح الملف تلقائياً");
        dialog.setHeaderText("لم نتمكن من فتح الملف تلقائياً على هذا النظام.");
        dialog.setContentText("يمكنك نسخ المسار التالي وفتحه يدوياً من مدير الملفات:");
        dialog.showAndWait();
    }

    @FXML
    private void handleDelete() {

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle(
                "تأكيد الحذف"
        );

        confirm.setHeaderText(null);

        confirm.setContentText(
                "هل أنت متأكد من حذف سجل هذا التقرير؟\n"
                        + "ملاحظة: سيتم حذف السجل من الأرشيف "
                        + "وPDF الفعلي من الجهاز."
        );

        Optional<ButtonType> result =
                confirm.showAndWait();

        if (result.isEmpty() ||
                result.get() != ButtonType.OK) {

            return;
        }

        try {

            boolean deleted =
                    ClientApiManager
                            .getInstance()
                            .getSessionReportAPI()
                            .deleteReport(
                                    report.getReportId()
                            );

            if (deleted) {

                if (parentController != null) {
                    parentController.refreshList();
                }

            } else {

                showAlert(
                        "فشل الحذف",
                        "تعذر حذف السجل."
                );
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            showAlert(
                    "تمت مقاطعة العملية",
                    "تمت مقاطعة الاتصال بالسيرفر أثناء حذف التقرير."
            );

        } catch (IOException | RuntimeException e) {

            showAlert(
                    "فشل الحذف",
                    e.getMessage() == null
                            ? "تعذر حذف التقرير."
                            : e.getMessage()
            );
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}