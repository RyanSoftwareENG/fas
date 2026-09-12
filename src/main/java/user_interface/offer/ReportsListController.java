package user_interface.offer;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import entities.GeneratedReport;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import runner.BackgroundRunner;
import pdfmanagement.GetGeneratedReports;

public class ReportsListController implements Initializable {

    @FXML
    private ScrollPane reportsScroll;

    @FXML
    private VBox reportsList;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> comboSort;

    @FXML
    private ToggleButton btnReverse;

    private List<GeneratedReport> allReports;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSortComboBox();
        setupListeners();
        loadAllReports();
    }

    private void initSortComboBox() {
        comboSort.setItems(FXCollections.observableArrayList("الأحدث أولاً", "الأقدم أولاً", "اسم العميل"));
        comboSort.setValue("الأحدث أولاً");
    }

    private void loadAllReports() {
        Label loadingLabel = new Label("جاري تحميل سجل التقارير... ⏳");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 30px;");
        loadingLabel.setAlignment(Pos.CENTER);
        reportsList.getChildren().setAll(loadingLabel);

        BackgroundRunner.run("جاري تحديث التقارير... ⏳", () -> {
            try {
                allReports = new GetGeneratedReports().getAllReports();
            } catch (Exception e) {
                System.err.println("خطأ أثناء جلب التقارير: " + e.getMessage());
            }
            return true;
        }, () -> {
            applyFiltersAndSort();
        });
    }

    public void refreshList() {
        if (searchField != null) searchField.clear();
        comboSort.setValue("الأحدث أولاً");
        btnReverse.setSelected(false);
        loadAllReports();
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        comboSort.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        btnReverse.selectedProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    private void applyFiltersAndSort() {
        if (allReports == null) return;

        String searchText = searchField.getText();
        String sortBy = comboSort.getValue();
        boolean isReverse = btnReverse.isSelected();

        List<GeneratedReport> processedList = allReports.stream()
                .filter(reportObj -> {
                    if (searchText == null || searchText.trim().isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = searchText.toLowerCase();
                    String clientName = reportObj.getClientName() != null ? reportObj.getClientName().toLowerCase() : "";

                    return clientName.contains(lowerCaseFilter)
                            || String.valueOf(reportObj.getClientId()).contains(lowerCaseFilter)
                            || String.valueOf(reportObj.getReportId()).contains(lowerCaseFilter);
                })
                .collect(Collectors.toList());

        if (sortBy != null) {
            Comparator<GeneratedReport> comparator = null;

            switch (sortBy) {
                case "الأحدث أولاً":
                    comparator = Comparator.comparing(GeneratedReport::getGeneratedDate,
                            Comparator.nullsLast(Comparator.reverseOrder()));
                    break;
                case "الأقدم أولاً":
                    comparator = Comparator.comparing(GeneratedReport::getGeneratedDate,
                            Comparator.nullsLast(Comparator.naturalOrder()));
                    break;
                case "اسم العميل":
                    comparator = Comparator.comparing(GeneratedReport::getClientName,
                            Comparator.nullsLast(Comparator.naturalOrder()));
                    break;
            }

            if (comparator != null) {
                if (isReverse) {
                    comparator = comparator.reversed();
                }
                processedList.sort(comparator);
            }
        }

        renderReports(processedList);
    }

    private void renderReports(List<GeneratedReport> reportsToRender) {
        reportsList.getChildren().clear();

        if (reportsToRender != null && !reportsToRender.isEmpty()) {
            for (GeneratedReport reportObj : reportsToRender) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportCard.fxml"));
                    Parent card = loader.load();

                    ReportCardController controller = loader.getController();
                    controller.setReportData(reportObj, this);

                    reportsList.getChildren().add(card);
                } catch (IOException e) {
                    System.err.println("فشل تحميل كرت التقرير: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Label emptyLabel = new Label("لا توجد تقارير مطابقة للبحث.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 30px;");
            reportsList.getChildren().add(emptyLabel);
        }
    }

    @FXML
    private void handleOpenAdminReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminFinancialReport.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("التقارير الإدارية والمالية");
            stage.setScene(new Scene(root, 650, 650));
            stage.show();

        } catch (IOException e) {
            System.err.println("فشل فتح واجهة التقارير الإدارية والمالية: " + e.getMessage());
            e.printStackTrace();
        }
    }
}