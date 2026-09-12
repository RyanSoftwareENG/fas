package user_interface.offer;

import entities.AdminFinancialSummary;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner;
import api.AdminFinancialSummaryAPI;
import pdfmanagement.PDFGenerator;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

public class AdminFinancialReportController {

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button btnCalculate;

    @FXML
    private Button btnExportPdf;

    @FXML
    private Label placeholderLabel;

    @FXML
    private GridPane summaryGrid;

    @FXML
    private Label totalSessionsLabel;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private Label newClientsLabel;

    @FXML
    private Label commitmentTitleLabel;

    @FXML
    private VBox commitmentContainer;

    private AdminFinancialSummary currentSummary;

    @FXML
    private void handleCalculate() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "الرجاء تحديد تاريخ البداية والنهاية.");
            return;
        }

        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "تاريخ البداية يجب أن يكون قبل تاريخ النهاية.");
            return;
        }

        btnCalculate.setDisable(true);

        BackgroundRunner.run("جاري حساب التقرير... ⏳", () -> {
            currentSummary = new AdminFinancialSummaryAPI().calculate(start, end);
            return true;
        }, () -> {
            btnCalculate.setDisable(false);
            displaySummary(currentSummary);
        });
    }

    private void displaySummary(AdminFinancialSummary summary) {
        placeholderLabel.setVisible(false);
        placeholderLabel.setManaged(false);

        summaryGrid.setVisible(true);
        summaryGrid.setManaged(true);

        totalSessionsLabel.setText(String.valueOf(summary.getTotalSessions()));
        totalRevenueLabel.setText(String.format("%.2f", summary.getTotalRevenue()));
        newClientsLabel.setText(String.valueOf(summary.getNewClients()));

        commitmentContainer.getChildren().clear();
        Map<String, Integer> distribution = summary.getCommitmentDistribution();

        if (distribution.isEmpty()) {
            commitmentTitleLabel.setVisible(false);
            commitmentTitleLabel.setManaged(false);
        } else {
            commitmentTitleLabel.setVisible(true);
            commitmentTitleLabel.setManaged(true);

            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                Label row = new Label("• " + entry.getKey() + ": " + entry.getValue() + " جلسة");
                row.setStyle("-fx-font-family: 'Dubai Regular'; -fx-font-size: 13px; -fx-text-fill: #334155;");
                commitmentContainer.getChildren().add(row);
            }
        }
    }

    @FXML
    private void handleExportPdf() {
        if (currentSummary == null) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "الرجاء عرض التقرير أولاً قبل التصدير.");
            return;
        }

        File pdfDir = new File(System.getProperty("user.home"), "FASInterface_Reports");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        String fileName = "تقرير إداري ومالي " + currentSummary.getStartDate() + " إلى " + currentSummary.getEndDate() + ".pdf";
        String path = new File(pdfDir, fileName).getAbsolutePath();

        try {
            PDFGenerator.createAdminFinancialReport(currentSummary, path);
            showAlert(Alert.AlertType.INFORMATION, "تم التصدير بنجاح", "🎉 تم حفظ التقرير في:\n" + path);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "فشل التصدير", "حدثت مشكلة أثناء إنشاء ملف PDF:\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}