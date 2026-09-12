package user_interface.offer;

import api.ClientAPI;
import api.PatientClientAPI;
import api.SessionAPI;
import runner.BackgroundRunner;
import pdfmanagement.PDFGenerator;
import dto.SessionListDTO;
import entities.*;
import api.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsController {

    // فلاتر التاريخ
    @FXML private ComboBox<String> cmbDateRange;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    // الحاويات (Views)
    @FXML private VBox viewFinancial, viewClients, viewHealth, viewOperations, viewAdmin;

    // --- مكونات التقرير المالي ---
    @FXML private Label lblTotalRevenue, lblTotalSessions;
    @FXML private BarChart<String, Number> revenueChart;

    // --- مكونات تقرير العملاء ---
    @FXML private PieChart genderPieChart, agePieChart;
    @FXML private TableView<Client> tblClients;
    @FXML private TableColumn<Client, Long> colClientId;
    @FXML private TableColumn<Client, String> colClientName;
    @FXML private TableColumn<Client, String> colClientGender;
    @FXML private TableColumn<Client, Integer> colClientAge;
    @FXML private TableColumn<Client, String> colClientPhone;
    @FXML private TableColumn<Client, LocalDateTime> colClientRegDate;

    // --- مكونات التقرير الصحي ---
    @FXML private TableView<ChronicDisease> tblDiseases;
    @FXML private TableColumn<ChronicDisease, String> colDiseaseName, colDiseaseStatus, colDiseaseSeverity;

    @FXML private TableView<Allergy> tblAllergies;
    @FXML private TableColumn<Allergy, String> colAllergyName, colAllergySeverity;

    // --- مكونات تقرير العمليات (Session Reports) ---
    @FXML private TableView<SessionReport> tblSessionReports;
    @FXML private TableColumn<SessionReport, Integer> colRepSessionId;
    @FXML private TableColumn<SessionReport, String> colRepNutritionist, colRepDiagnosis, colRepCommitment;
    @FXML private TableColumn<SessionReport, LocalDate> colRepNextAppt;

    // --- مكونات التقرير الإداري (Sessions Logs) ---
    @FXML private TableView<SessionListDTO> tblSessionsAdmin;
    @FXML private TableColumn<Session, Integer> colAdminSessionId;
    @FXML private TableColumn<Session, Long> colAdminClientId;
    @FXML private TableColumn<Session, LocalDateTime> colAdminUploadTime, colAdminModTime;
    @FXML private TableColumn<Session, String> colAdminDuration;

    // --- Services ---
    private ClientAPI clientService;
    private SessionAPI sessionService;
    private SessionReportAPI sessionReportService;
    private PatientClientAPI healthService;

    @FXML
    public void initialize() {
        clientService = ClientApiManager.getInstance().getClientAPI();
        sessionService = ClientApiManager.getInstance().getSessionAPI();
        sessionReportService = ClientApiManager.getInstance().getSessionReportAPI();
        try {
            healthService = ClientApiManager.getInstance().getPatientClientAPI();
        } catch (Exception e) {
            showAlert("خطأ في الاتصال", "تعذر الاتصال بقاعدة البيانات لتهيئة الخدمات الصحية.");
        }

        setupDateFilters();
        setupTablesColumns();
        showFinancialReport(null);
    }

    private void setupDateFilters() {
        cmbDateRange.setItems(FXCollections.observableArrayList(
                "يومي", "شهري", "سنوي", "تاريخ مخصص"
        ));
        cmbDateRange.setValue("شهري");
        handleDateRangeChange(null);
    }

    private void setupTablesColumns() {
        colClientId.setCellValueFactory(new PropertyValueFactory<>("clientID"));
        colClientName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        colClientGender.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGender())));
        colClientAge.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        colClientPhone.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colClientRegDate.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        colDiseaseName.setCellValueFactory(new PropertyValueFactory<>("chronicDiseaseName"));
        colDiseaseStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDiseaseSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));

        colAllergyName.setCellValueFactory(new PropertyValueFactory<>("allergyName"));
        colAllergySeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));

        colRepSessionId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colRepNutritionist.setCellValueFactory(new PropertyValueFactory<>("nutritionistName"));
        colRepDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colRepCommitment.setCellValueFactory(new PropertyValueFactory<>("commitmentLevel"));
        colRepNextAppt.setCellValueFactory(new PropertyValueFactory<>("nextAppointment"));

        colAdminSessionId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAdminClientId.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        colAdminUploadTime.setCellValueFactory(new PropertyValueFactory<>("uploadTime"));
        colAdminModTime.setCellValueFactory(new PropertyValueFactory<>("modificationDate"));
        colAdminDuration.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDuration() != null ? cellData.getValue().getDuration().toString() : ""
        ));
    }

    @FXML
    void handleDateRangeChange(ActionEvent event) {
        String selection = cmbDateRange.getValue();
        if (selection == null) return;

        LocalDate now = LocalDate.now();
        boolean isCustom = selection.equals("تاريخ مخصص");
        dpStartDate.setDisable(!isCustom);
        dpEndDate.setDisable(!isCustom);

        if (!isCustom) {
            switch (selection) {
                case "يومي":
                    dpStartDate.setValue(now);
                    dpEndDate.setValue(now);
                    break;
                case "شهري":
                    dpStartDate.setValue(now.withDayOfMonth(1));
                    dpEndDate.setValue(now.withDayOfMonth(now.lengthOfMonth()));
                    break;
                case "سنوي":
                    dpStartDate.setValue(now.withDayOfYear(1));
                    dpEndDate.setValue(now.withDayOfYear(now.lengthOfYear()));
                    break;
            }
        }
    }

    @FXML
    void applyDateFilter(ActionEvent event) {
        if (viewFinancial.isVisible()) loadFinancialData();
        else if (viewClients.isVisible()) loadClientsData();
        else if (viewHealth.isVisible()) loadHealthData();
        else if (viewOperations.isVisible()) loadOperationsData();
        else if (viewAdmin.isVisible()) loadAdminData();
    }

    private void hideAllViews() {
        viewFinancial.setVisible(false);
        viewClients.setVisible(false);
        viewHealth.setVisible(false);
        viewOperations.setVisible(false);
        viewAdmin.setVisible(false);
    }

    @FXML void showFinancialReport(ActionEvent event) { hideAllViews(); viewFinancial.setVisible(true); loadFinancialData(); }
    @FXML void showClientsReport(ActionEvent event) { hideAllViews(); viewClients.setVisible(true); loadClientsData(); }
    @FXML void showHealthReport(ActionEvent event) { hideAllViews(); viewHealth.setVisible(true); loadHealthData(); }
    @FXML void showOperationsReport(ActionEvent event) { hideAllViews(); viewOperations.setVisible(true); loadOperationsData(); }
    @FXML void showAdministrativeReport(ActionEvent event) { hideAllViews(); viewAdmin.setVisible(true); loadAdminData(); }

    // 🛠️ تم تعديلها لاستقبال التاريخ كمتغيرات محلية لتجنب خطأ خيط الواجهة
    private boolean isWithinDateRange(LocalDateTime dateTime, LocalDate start, LocalDate end) {
        if (dateTime == null || start == null || end == null) return false;
        LocalDate date = dateTime.toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private void loadFinancialData() {
        // التقاط التواريخ في خيط الواجهة
        final LocalDate start = dpStartDate.getValue();
        final LocalDate end = dpEndDate.getValue();
        final List<SessionListDTO>[] sessionsHolder = new List[1];

        BackgroundRunner.run("جاري تحميل التقرير المالي... ⏳", () -> {
            sessionsHolder[0] = sessionService.getAllSessions().stream()
                    .filter(s -> s.getUploadTime() != null && isWithinDateRange(s.getUploadTime(), start, end))
                    .collect(Collectors.toList());
            return true;
        }, () -> {
            if (sessionsHolder[0] != null) {
                float totalIncome = 0;
                for(SessionListDTO s : sessionsHolder[0]) {
                    totalIncome += 50.0f; // قيمة افتراضية للتجربة
                }

                lblTotalRevenue.setText(String.format("%.2f $", totalIncome));
                lblTotalSessions.setText(String.valueOf(sessionsHolder[0].size()));

                revenueChart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("إيرادات الجلسات");
                series.getData().add(new XYChart.Data<>("الإجمالي", totalIncome));
                revenueChart.getData().add(series);
            }
        });
    }

    private void loadClientsData() {
        final LocalDate start = dpStartDate.getValue();
        final LocalDate end = dpEndDate.getValue();
        final List<Client>[] clientsHolder = new List[1];

        BackgroundRunner.run("جاري تحميل بيانات العملاء... ⏳", () -> {
            clientsHolder[0] = clientService.allClients().stream()
                    .filter(c -> c.getUploadDate() != null && isWithinDateRange(c.getUploadDate(), start, end))
                    .collect(Collectors.toList());
            return true;
        }, () -> {
            if (clientsHolder[0] != null) {
                tblClients.setItems(FXCollections.observableArrayList(clientsHolder[0]));

                int maleCount = 0, femaleCount = 0;
                int ageUnder18 = 0, age18To35 = 0, age36To50 = 0, ageOver50 = 0;

                for (Client c : clientsHolder[0]) {
                    if (String.valueOf(c.getGender()).equalsIgnoreCase("M")) maleCount++;
                    else femaleCount++;

                    int age = c.getAge();
                    if (age < 18) ageUnder18++;
                    else if (age <= 35) age18To35++;
                    else if (age <= 50) age36To50++;
                    else ageOver50++;
                }

                genderPieChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("ذكور", maleCount), new PieChart.Data("إناث", femaleCount)));
                agePieChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("أقل من 18", ageUnder18), new PieChart.Data("18 - 35", age18To35),
                        new PieChart.Data("36 - 50", age36To50), new PieChart.Data("أكثر من 50", ageOver50)));
            }
        });
    }

    private void loadHealthData() {
        final List<ChronicDisease>[] diseasesHolder = new List[1];
        final List<Allergy>[] allergiesHolder = new List[1];

        BackgroundRunner.run("جاري تحميل البيانات الصحية... ⏳", () -> {
            if (healthService != null) {
                diseasesHolder[0] = healthService.getAllChronicDiseases();
                allergiesHolder[0] = healthService.getAllAllergies();
            }
            return true;
        }, () -> {
            if (diseasesHolder[0] != null) tblDiseases.setItems(FXCollections.observableArrayList(diseasesHolder[0]));
            if (allergiesHolder[0] != null) tblAllergies.setItems(FXCollections.observableArrayList(allergiesHolder[0]));
        });
    }

    private void loadOperationsData() {
        final LocalDate start = dpStartDate.getValue();
        final LocalDate end = dpEndDate.getValue();
        final List<SessionReport>[] reportsHolder = new List[1];

        BackgroundRunner.run("جاري تحميل تقارير الجلسات... ⏳", () -> {
            reportsHolder[0] = sessionReportService.getAllReport();
            return true;
        }, () -> {
            if (reportsHolder[0] != null) {
                tblSessionReports.setItems(FXCollections.observableArrayList(reportsHolder[0]));
            }
        });
    }

    private void loadAdminData() {
        final LocalDate start = dpStartDate.getValue();
        final LocalDate end = dpEndDate.getValue();
        final List<SessionListDTO>[] adminHolder = new List[1];

        BackgroundRunner.run("جاري تحميل التقارير الإدارية... ⏳", () -> {
            adminHolder[0] = sessionService.getAllSessions().stream()
                    .filter(s -> s.getUploadTime() != null && isWithinDateRange(s.getUploadTime(), start, end))
                    .collect(Collectors.toList());
            return true;
        }, () -> {
            if (adminHolder[0] != null) {
                tblSessionsAdmin.setItems(FXCollections.observableArrayList(adminHolder[0]));
            }
        });
    }

    @FXML
    void handleExportPDF(ActionEvent event) {
        Client selectedClient = tblClients.getSelectionModel().getSelectedItem();

        if (selectedClient == null && viewClients.isVisible()) {
            showAlert("تنبيه", "يرجى تحديد عميل من الجدول أولاً لاستخراج تقريره الشامل.");
            return;
        }

        // فتح واجهة حفظ الملف في خيط الواجهة السريع
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("حفظ التقرير كـ PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File defaultDir = new File("D:\\Downloads\\FASInterface\\data\\pdf");
        if (!defaultDir.exists()) defaultDir.mkdirs();
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            fileChooser.setInitialDirectory(defaultDir);
        }

        String defaultFileName = selectedClient != null ? "Report_" + selectedClient.getClientID() + ".pdf" : "System_Report.pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            if (selectedClient != null) {
                // مصفوفات لحفظ النتيجة وعرضها في خيط الواجهة
                final boolean[] isSuccessHolder = new boolean[1];
                final String[] errorMessageHolder = new String[1];
                final Client currentClient = selectedClient;

                // تشغيل إنشاء ملف PDF وبناء البيانات في الخلفية لتفادي تجميد الواجهة أثناء الكتابة والحفظ
                BackgroundRunner.run("جاري بناء التقرير واستخراج ملف PDF... ⏳", () -> {
                    try {
                        Client fullClient = clientService.populateAdditionalData(currentClient);
                        SessionReport report = sessionReportService.getReportBySessionId(
                                fullClient.getSessions().get(fullClient.getSessions().size()-1).getId()
                        );
                        PDFGenerator.createComprehensiveReport(fullClient, fullClient.getSessions(), report, file.getAbsolutePath());
                        isSuccessHolder[0] = true;
                    } catch (Exception e) {
                        errorMessageHolder[0] = e.getMessage();
                        e.printStackTrace();
                    }
                    return true;
                }, () -> {
                    // عرض رسالة التأكيد أو الفشل للمستخدم
                    if (isSuccessHolder[0]) {
                        showAlert("نجاح", "تم تصدير التقرير بنجاح للمريض: " + currentClient.getFullName());
                    } else {
                        showAlert("خطأ", "حدث خطأ أثناء تصدير ملف الـ PDF: " + errorMessageHolder[0]);
                    }
                });
            } else {
                showAlert("معلومة", "تم تخصيص كلاس PDFGenerator لتقارير العملاء الشاملة. يمكنك اختيار عميل من قسم (العملاء) وطباعة تقريره.");
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}