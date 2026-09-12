package user_interface.offer;

import entities.BodyData;
import entities.Client;
import entities.NutritionPlan;
import entities.Session;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import runner.BackgroundRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller class for the Comprehensive Client Report View.
 * Handles extensive data processing, responsive UI updates, medical metrics generation,
 * and asynchronous operations with strict timeout protocols.
 */
public class ClientReportViewController {

    // =================================================================================
    // FXML INJECTIONS
    // =================================================================================

    // --- Header ---
    @FXML private ImageView imgClient;
    @FXML private Label lblClientName, lblClientId, lblClientAge, lblClientGender;
    @FXML private Label lblClientPhone, lblRegDate, lblSessionCount, lblLastSessionDate, lblHealthStatus;
    @FXML private Button btnExportPDF;

    // --- Sidebar ---
    @FXML private DatePicker dpStartDate, dpEndDate;
    @FXML private ListView<Session> lvSessions;

    // --- Tab 1: Dashboard ---
    @FXML private FlowPane cardsContainer;

    // --- Tab 2: Charts ---
    @FXML private FlowPane chartTogglesContainer;
    @FXML private LineChart<String, Number> mainChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    private final Map<String, XYChart.Series<String, Number>> chartSeriesMap = new HashMap<>();

    // --- Tab 3: Detailed Session Data ---
    @FXML private GridPane measurementsGrid, nutritionGrid;
    @FXML private TableView<Object> tvExaminations;
    @FXML private TableColumn<Object, String> colExamName, colExamValue, colExamRef, colExamStatus;

    // --- Tab 4: Smart Report & Comparison ---
    @FXML private GridPane comparisonGrid;
    @FXML private TextArea txtSmartReport;

    // =================================================================================
    // STATE VARIABLES
    // =================================================================================

    private Client client;
    private final ObservableList<Session> masterSessionList = FXCollections.observableArrayList();
    private FilteredList<Session> filteredSessions;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // =================================================================================
    // INITIALIZATION & DATA BINDING
    // =================================================================================

    /**
     * Entry point to inject the client data into the controller.
     * Initializes all UI components and begins data processing.
     *
     * @param client The active Client object.
     */
    public void setClientData(Client client) {
        if (client == null) {
            showAlert("خطأ في البيانات", "لم يتم تمرير بيانات العميل بشكل صحيح.", Alert.AlertType.ERROR);
            return;
        }
        this.client = client;

        masterSessionList.clear();
        // استخراج وترتيب الجلسات بأمان
        if (client.getSessions() != null) {
            masterSessionList.addAll(client.getSessions());
            masterSessionList.sort(Comparator.comparing(s ->
                    s.getUploadTime() != null ? s.getUploadTime() : LocalDateTime.MIN
            ));
        }

        filteredSessions = new FilteredList<>(masterSessionList, p -> true);

        initHeaderData();
        setupSessionList();
        setupDateFilters();
        setupTableView(); // إعداد جدول الفحوصات

        // اختيار الجلسة الأخيرة افتراضياً إن وجدت
        if (!filteredSessions.isEmpty()) {
            lvSessions.getSelectionModel().selectLast();
        } else {
            txtSmartReport.setText("لا توجد جلسات مسجلة لهذا العميل بعد.");
        }
    }

    private void initHeaderData() {
        lblClientName.setText(client.getFullName() != null ? String.join(" ", client.getFullName()) : "غير معروف");
        lblClientId.setText("رقم العميل: #" + client.getClientID());
        lblClientAge.setText("العمر: " + client.getAge() + " سنة");
        lblClientGender.setText("الجنس: " + (String.valueOf(client.getGender()) != null ? client.getGender() : "-"));
        lblClientPhone.setText("الهاتف: " + (client.getContactNumber() != null ? client.getContactNumber() : "-"));
        lblSessionCount.setText("عدد الجلسات: " + masterSessionList.size());

        if (!masterSessionList.isEmpty()) {
            Session first = masterSessionList.get(0);
            Session last = masterSessionList.get(masterSessionList.size() - 1);
            lblRegDate.setText("التسجيل: " + formatDateTime(first.getUploadTime()));
            lblLastSessionDate.setText("آخر جلسة: " + formatDateTime(last.getUploadTime()));
            updateHealthStatus(last.getBodyData());
        } else {
            lblRegDate.setText("التسجيل: -");
            lblLastSessionDate.setText("آخر جلسة: -");
            lblHealthStatus.setText("الحالة: غير متوفرة");
        }
    }

    private void setupSessionList() {
        lvSessions.setItems(filteredSessions);
        lvSessions.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createSessionCard(item));
                    if (isSelected()) {
                        setStyle("-fx-background-color: #e0e7ff; -fx-border-color: #1e3a8a; -fx-border-width: 0 0 0 4;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
                    }
                }
            }
        });

        // الاستماع لتغير التحديد وتحديث كافة الواجهات
        lvSessions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Platform.runLater(() -> {
                    refreshDashboard(newSelection);
                    refreshDetailsGrids(newSelection);
                    generateSmartReport(newSelection);
                    updateComparisonReport(newSelection);
                    lvSessions.refresh(); // لتحديث ألوان التحديد
                });
            }
        });

        setupCharts();
    }

    private void setupDateFilters() {
        dpStartDate.valueProperty().addListener((obs, oldV, newV) -> filterSessions());
        dpEndDate.valueProperty().addListener((obs, oldV, newV) -> filterSessions());
    }

    private void filterSessions() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        filteredSessions.setPredicate(session -> {
            if (session.getUploadTime() == null) return false;
            LocalDate sessionDate = session.getUploadTime().toLocalDate();
            boolean afterStart = (start == null) || !sessionDate.isBefore(start);
            boolean beforeEnd = (end == null) || !sessionDate.isAfter(end);
            return afterStart && beforeEnd;
        });

        setupCharts(); // إعادة رسم المنحنيات بعد الفلترة
        if (!filteredSessions.isEmpty()) {
            lvSessions.getSelectionModel().selectLast();
        }
    }

    private void setupTableView() {
        tvExaminations.setPlaceholder(new Label("لا توجد فحوصات طبية مرفقة لهذه الجلسة."));
    }

    // =================================================================================
    // UI GENERATORS (Cards, Grids, Reports)
    // =================================================================================

    private VBox createSessionCard(Session s) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("جلسة #" + s.getId());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-font-size: 14px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(formatDateOnly(s.getUploadTime()));
        date.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        header.getChildren().addAll(title, spacer, date);

        BodyData bd = s.getBodyData();
        HBox details = new HBox(15);
        if (bd != null) {
            Label weight = new Label("الوزن: " + formatNum(bd.getWeight().doubleValue()) + "kg");
            Label fat = new Label("دهون: " + formatNum(bd.getBodyFatPercentage().doubleValue()) + "%");
            weight.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
            fat.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
            details.getChildren().addAll(weight, fat);
        } else {
            Label noData = new Label("بيانات القياس غير مكتملة");
            noData.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            details.getChildren().add(noData);
        }

        card.getChildren().addAll(header, details);
        return card;
    }

    private void refreshDashboard(Session session) {
        cardsContainer.getChildren().clear();
        BodyData bd = session.getBodyData();
        if (bd == null) return;

        char gender = client.getGender();
        int age = client.getAge();

        addDashboardCard("الوزن الإجمالي", bd.getWeight().doubleValue(), "kg", evaluateWeight(bd.getWeight().doubleValue()), "⚖️", "الوزن الكلي للجسم.");
        addDashboardCard("مؤشر كتلة الجسم", bd.getBMI().doubleValue(), "BMI", evaluateBMI(bd.getBMI()), "📊", "يحدد فئة الوزن (طبيعي، سمنة، الخ).");
        addDashboardCard("نسبة الدهون", bd.getBodyFatPercentage().doubleValue(), "%", evaluateFat(bd.getBodyFatPercentage().doubleValue(), String.valueOf(gender)), "🥓", "نسبة الدهون الصافية من وزن الجسم.");
        addDashboardCard("الكتلة العضلية (SMM)", bd.getSmm().doubleValue(), "kg", "🟢", "💪", "الكتلة العضلية الهيكلية المسؤولة عن الحركة.");

        double tdee = bd.getTDEE(gender, age).doubleValue();
        double lbm = calculateLBM(bd.getWeight(), bd.getBodyFatPercentage());
        double water = calculateIdealWater(bd.getWeight(), String.valueOf(gender));

        addDashboardCard("الحرق الأساسي", bd.getBMR(gender, age).doubleValue(), "kcal", "🟡", "🔥", "السعرات المطلوبة لعمل الأجهزة الحيوية وقت الراحة.");
        addDashboardCard("إجمالي الحرق (TDEE)", tdee, "kcal", "🔵", "⚡", "إجمالي السعرات المحروقة يومياً مع النشاط.");
        addDashboardCard("كتلة لادهنية (LBM)", lbm, "kg", "🟢", "🦴", "وزن الجسم باستثناء الدهون (عضلات، عظام، أعضاء).");
        addDashboardCard("الاحتياج المائي", water, "Ltr", "🔵", "💧", "الاحتياج اليومي المقدر من الماء.");
        addDashboardCard("الدهون الحشوية", calculateVisceralFat(bd), "درجة", evaluateVisceral(calculateVisceralFat(bd)), "🫀", "الدهون المحيطة بالأعضاء الداخلية.");
    }

    private void addDashboardCard(String title, double value, String unit, String colorCode, String icon, String tooltipText) {
        VBox card = new VBox(8);
        String bgColor = getColorBackground(colorCode);
        String textColor = getColorText(colorCode);

        card.setPrefSize(185, 110);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + textColor + "40;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);"
        );
        card.setAlignment(Pos.CENTER);

        Label lblTitle = new Label(icon + " " + title);
        lblTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");

        Label lblValue = new Label(formatNum(value) + " " + unit);
        lblValue.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + textColor + ";");

        card.getChildren().addAll(lblTitle, lblValue);

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: #1e293b;");
        Tooltip.install(card, tooltip);

        cardsContainer.getChildren().add(card);
    }

    private void refreshDetailsGrids(Session session) {
        measurementsGrid.getChildren().clear();
        nutritionGrid.getChildren().clear();
        BodyData bd = session.getBodyData();
        NutritionPlan np = session.getNutritionPlan();

        if (bd != null) {
            String[][] bodyMetrics = {
                    {"الوزن الكلي", formatNum(bd.getWeight().doubleValue()) + " كج"},
                    {"مؤشر كتلة الجسم (BMI)", formatNum(bd.getBMI().doubleValue())},
                    {"نسبة الدهون (%)", formatNum(bd.getBodyFatPercentage().doubleValue()) + "%"},
                    {"الكتلة العضلية الهيكلية", formatNum(bd.getSmm().doubleValue()) + " كج"},
                    {"معدل الحرق الأساسي (BMR)", formatNum(bd.getBMR(client.getGender(), client.getAge()).doubleValue()) + " سعرة"},
                    {"معدل الأيض النشط (TDEE)", formatNum(bd.getTDEE(client.getGender(), client.getAge()).doubleValue()) + " سعرة"}
            };
            populateGrid(measurementsGrid, bodyMetrics, 3);
        } else {
            measurementsGrid.add(new Label("لا توجد قياسات جسدية مسجلة لهذه الجلسة."), 0, 0);
        }

        if (np != null) {
            String[][] nutritionMetrics = {
                    {"السعرات المستهدفة", formatNum(np.getTotalCalories().doubleValue()) + " سعرة"},
                    {"البروتين", formatNum(np.getProteinAmount().doubleValue()) + " جم"},
                    {"الكربوهيدرات", formatNum(np.getCarbohydratesAmount().doubleValue()) + " جم"},
                    {"الدهون الصحية", formatNum(np.getFatAmount().doubleValue()) + " جم"},
                    {"عدد الوجبات", np.getMealsCount() + " وجبات"},
                    {"الماء الموصى به", np.getWaterIntake() != null ? np.getWaterIntake() : "-"}
            };
            populateGrid(nutritionGrid, nutritionMetrics, 3);

            if (np.getNotes() != null && !np.getNotes().isEmpty()) {
                Label lblNoteTitle = new Label("ملاحظات الخطة:");
                lblNoteTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-padding: 10 0 0 0;");
                Label lblNoteContent = new Label(np.getNotes());
                lblNoteContent.setWrapText(true);
                lblNoteContent.setStyle("-fx-text-fill: #334155;");
                nutritionGrid.add(lblNoteTitle, 0, 10, 2, 1);
                nutritionGrid.add(lblNoteContent, 0, 11, 6, 1);
            }
        } else {
            nutritionGrid.add(new Label("لم يتم إرفاق خطة غذائية في هذه الجلسة."), 0, 0);
        }
    }

    private void populateGrid(GridPane grid, String[][] data, int columns) {
        int col = 0;
        int row = 0;
        for (String[] pair : data) {
            VBox cell = new VBox(3);
            cell.setPadding(new Insets(10));
            cell.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 5; -fx-background-radius: 5;");
            cell.setPrefWidth(200);

            Label title = new Label(pair[0]);
            title.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

            Label value = new Label(pair[1]);
            value.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

            cell.getChildren().addAll(title, value);
            grid.add(cell, col, row);

            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
    }

    // =================================================================================
    // INTERACTIVE CHARTS
    // =================================================================================

    private void setupCharts() {
        mainChart.getData().clear();
        chartSeriesMap.clear();
        chartTogglesContainer.getChildren().clear();

        if (filteredSessions == null || filteredSessions.isEmpty()) return;

        String[] metrics = {"الوزن (kg)", "الدهون (%)", "العضلات (kg)"};
        String[] colors = {"#1e3a8a", "#dc2626", "#16a34a"};

        for (int i = 0; i < metrics.length; i++) {
            String metric = metrics[i];
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(metric);
            chartSeriesMap.put(metric, series);

            CheckBox toggle = new CheckBox(metric);
            toggle.setSelected(true);
            toggle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colors[i] + "; -fx-cursor: hand;");
            toggle.selectedProperty().addListener((obs, oldV, newV) -> {
                if (series.getNode() != null) {
                    series.getNode().setVisible(newV);
                }
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) data.getNode().setVisible(newV);
                }
            });
            chartTogglesContainer.getChildren().add(toggle);
        }

        for (Session s : filteredSessions) {
            BodyData bd = s.getBodyData();
            if (bd == null) continue;

            String dateLabel = formatDateOnly(s.getUploadTime());

            addDataPoint(chartSeriesMap.get("الوزن (kg)"), dateLabel, bd.getWeight().doubleValue());
            addDataPoint(chartSeriesMap.get("الدهون (%)"), dateLabel, bd.getBodyFatPercentage().doubleValue());
            addDataPoint(chartSeriesMap.get("العضلات (kg)"), dateLabel, bd.getSmm().doubleValue());
        }

        mainChart.getData().addAll(chartSeriesMap.values());

        Platform.runLater(() -> {
            for (int i = 0; i < metrics.length; i++) {
                XYChart.Series<String, Number> s = chartSeriesMap.get(metrics[i]);
                if (s != null && s.getNode() != null) {
                    s.getNode().setStyle("-fx-stroke: " + colors[i] + "; -fx-stroke-width: 3px;");
                }
                if (s != null) {
                    for (XYChart.Data<String, Number> d : s.getData()) {
                        if (d.getNode() != null) {
                            d.getNode().setStyle("-fx-background-color: " + colors[i] + ", white; -fx-background-insets: 0, 2;");
                        }
                    }
                }
            }
        });
    }

    private void addDataPoint(XYChart.Series<String, Number> series, String label, double value) {
        if (series != null) {
            series.getData().add(new XYChart.Data<>(label, value));
        }
    }

    // =================================================================================
    // SMART REPORT & COMPARISON LOGIC
    // =================================================================================

    private void updateComparisonReport(Session current) {
        comparisonGrid.getChildren().clear();
        comparisonGrid.setPadding(new Insets(10));

        if (filteredSessions == null || filteredSessions.isEmpty()) return;

        Session first = filteredSessions.get(0);
        Session previous = null;
        int currentIndex = filteredSessions.indexOf(current);
        if (currentIndex > 0) {
            previous = filteredSessions.get(currentIndex - 1);
        }

        addCompCell("المؤشر", 0, 0, true, "#f1f5f9");
        addCompCell("الحالية", 1, 0, true, "#f1f5f9");
        addCompCell("السابقة", 2, 0, true, "#f1f5f9");
        addCompCell("الأولى", 3, 0, true, "#f1f5f9");
        addCompCell("التحسن الكلي", 4, 0, true, "#e0e7ff");

        if (current.getBodyData() == null) return;

        addCompRow("الوزن (kg)", 1,
                current.getBodyData().getWeight().doubleValue(),
                previous != null && previous.getBodyData() != null ? previous.getBodyData().getWeight().doubleValue() : 0,
                first.getBodyData() != null ? first.getBodyData().getWeight().doubleValue() : 0,
                false);

        addCompRow("الدهون (%)", 2,
                current.getBodyData().getBodyFatPercentage().doubleValue(),
                previous != null && previous.getBodyData() != null ? previous.getBodyData().getBodyFatPercentage().doubleValue() : 0,
                first.getBodyData() != null ? first.getBodyData().getBodyFatPercentage().doubleValue(): 0,
                false);

        addCompRow("العضلات (kg)", 3,
                current.getBodyData().getSmm().doubleValue(),
                previous != null && previous.getBodyData() != null ? previous.getBodyData().getSmm().doubleValue() : 0,
                first.getBodyData() != null ? first.getBodyData().getSmm().doubleValue() : 0,
                true);
    }

    private void addCompCell(String text, int col, int row, boolean isHeader, String bgColor) {
        Label lbl = new Label(text);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setPadding(new Insets(10, 5, 10, 5));

        String style = "-fx-border-color: #cbd5e1; -fx-border-width: 0 0 1 0; -fx-background-color: " + bgColor + ";";
        if (isHeader) {
            style += "-fx-font-weight: bold; -fx-text-fill: #1e3a8a;";
        } else {
            style += "-fx-text-fill: #334155;";
        }

        lbl.setStyle(style);
        comparisonGrid.add(lbl, col, row);
        GridPane.setFillWidth(lbl, true);
        GridPane.setHalignment(lbl, HPos.CENTER);
    }

    private void addCompRow(String metric, int row, double current, double prev, double first, boolean higherIsBetter) {
        addCompCell(metric, 0, row, true, "white");
        addCompCell(formatNum(current), 1, row, false, "white");
        addCompCell(prev > 0 ? formatNum(prev) : "-", 2, row, false, "white");
        addCompCell(first > 0 ? formatNum(first) : "-", 3, row, false, "white");

        if (first > 0) {
            double diff = current - first;
            boolean improved = higherIsBetter ? (diff > 0) : (diff < 0);
            String arrow = diff == 0 ? "➖" : (improved ? "📈" : "📉");
            String color = diff == 0 ? "#64748b" : (improved ? "#16a34a" : "#dc2626");

            Label diffLabel = new Label(String.format("%s %s", formatNum(Math.abs(diff)), arrow));
            diffLabel.setMaxWidth(Double.MAX_VALUE);
            diffLabel.setAlignment(Pos.CENTER);
            diffLabel.setPadding(new Insets(10, 5, 10, 5));
            diffLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 1 0; -fx-background-color: #f8fafc;");

            comparisonGrid.add(diffLabel, 4, row);
        } else {
            addCompCell("-", 4, row, false, "#f8fafc");
        }
    }

    private void generateSmartReport(Session session) {
        BodyData bd = session.getBodyData();
        if (bd == null) {
            txtSmartReport.setText("لا توجد بيانات جسدية كافية لتوليد التقرير الذكي.");
            return;
        }

        StringBuilder report = new StringBuilder();
        char gender = client.getGender();

        report.append("📊 التقرير التحليلي الموجز لجلسة: ").append(formatDateOnly(session.getUploadTime())).append("\n");
        report.append("ـــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــ\n\n");

        double bmi = bd.getBMI().doubleValue();
        report.append("🔹 تقييم مؤشر الكتلة (BMI): ").append(formatNum(bmi)).append(".\n");
        if(bmi >= 30) {
            report.append("   - التصنيف: سمنة. الهدف الأساسي هو تحقيق عجز في السعرات الحرارية مع التركيز على النشاط البدني وتناول كميات كافية من البروتين.\n");
        } else if(bmi >= 25) {
            report.append("   - التصنيف: وزن زائد. ينصح بزيادة المجهود القلبي مع تعديل توزيع الماكروز للحد من الكربوهيدرات.\n");
        } else if(bmi >= 18.5) {
            report.append("   - التصنيف: وزن طبيعي. الأولوية الحالية هي التركيز على إعادة التشكيل (Body Recomposition) عبر تدريبات المقاومة.\n");
        } else {
            report.append("   - التصنيف: نقص في الوزن. يتطلب زيادة في السعرات الحرارية مدعوماً بتدريبات المقاومة.\n");
        }
        report.append("\n");

        report.append("🔹 تقييم الكتلة الدهنية: ").append(formatNum(bd.getBodyFatPercentage().doubleValue())).append("%\n");
        String fatStatus = evaluateFat(bd.getBodyFatPercentage().doubleValue(), String.valueOf(gender));
        if (fatStatus.equals("🔴")) {
            report.append("   - نسبة الدهون مرتفعة وتشكل خطراً على المدى الطويل. يجب استهداف خفض الدهون الحشوية.\n");
        } else if (fatStatus.equals("🟡")) {
            report.append("   - النسبة مقبولة ولكن قابلة للتحسين للحصول على مظهر رياضي وصحة أفضل.\n");
        } else {
            report.append("   - نسبة الدهون ممتازة وضمن النطاق الصحي والرياضي.\n");
        }
        report.append("\n");

        NutritionPlan plan = session.getNutritionPlan();
        if (plan != null) {
            double tdee = bd.getTDEE(gender, client.getAge()).doubleValue();
            double planCalories = plan.getTotalCalories().doubleValue();
            double diff = planCalories - tdee;

            report.append("🔹 مطابقة الخطة الغذائية مع المؤشرات:\n");
            report.append("   - الخطة توفر ").append(formatNum(planCalories)).append(" سعرة حرارية.\n");

            if (diff < -800) {
                report.append("   - ⚠️ تحذير: العجز في السعرات كبير جداً، مما قد يؤدي إلى هدم عضلي.\n");
            } else if (diff < 0) {
                report.append("   - استراتيجية ممتازة لنزول الوزن المعتدل بطريقة مستدامة.\n");
            } else if (diff > 0) {
                report.append("   - الخطة الحالية تدعم زيادة الوزن بناءً على فائض السعرات.\n");
            }

            double requiredProtein = bd.getWeight().doubleValue() * 1.6;
            if (plan.getProteinAmount().doubleValue() >= requiredProtein) {
                report.append("   - كمية البروتين بالخطة (").append(plan.getProteinAmount()).append("g) كافية وممتازة.\n");
            } else {
                report.append("   - 💡 توصية: يُنصح برفع كمية البروتين إلى حوالي ").append(formatNum(requiredProtein)).append("g لدعم الألياف العضلية.\n");
            }
        }

        txtSmartReport.setText(report.toString());
    }

    // =================================================================================
    // MEDICAL ALGORITHMS & UTILITIES
    // =================================================================================

    private double calculateLBM(BigDecimal weight, BigDecimal fatPercentage) {
        return weight.doubleValue() - (weight.doubleValue() * (fatPercentage.doubleValue() / 100.0));
    }

    private double calculateIdealWater(BigDecimal weight, String gender) {
        boolean isMale = "Male".equalsIgnoreCase(gender) || "ذكر".equals(gender) || "M".equalsIgnoreCase(gender);
        return (isMale ? weight.doubleValue() * 0.04 : weight.doubleValue()* 0.035);
    }

    private double calculateVisceralFat(BodyData bd) {
        return  (bd.getBodyFatPercentage().doubleValue() * 0.35);
    }

    private String evaluateBMI(BigDecimal bmi) {
        if (bmi.doubleValue() < 18.5) return "🟡";
        if (bmi.doubleValue() < 25.0) return "🟢";
        if (bmi.doubleValue() < 30.0) return "🟠";
        return "🔴";
    }

    private String evaluateFat(double fat, String gender) {
        boolean isMale = "Male".equalsIgnoreCase(gender) || "ذكر".equals(gender) || "M".equalsIgnoreCase(gender);
        if (isMale) {
            return fat <= 15 ? "🟢" : (fat <= 24 ? "🟡" : "🔴");
        } else {
            return fat <= 24 ? "🟢" : (fat <= 31 ? "🟡" : "🔴");
        }
    }

    private String evaluateWeight(double weight) { return "🔵"; }

    private String evaluateVisceral(double vf) {
        return vf < 10 ? "🟢" : (vf < 14 ? "🟠" : "🔴");
    }

    private void updateHealthStatus(BodyData bd) {
        if (bd == null) return;
        String statusColor = evaluateBMI(bd.getBMI());
        String txt = switch (statusColor) {
            case "🟢" -> "طبيعية";
            case "🟡", "🟠" -> "تتطلب متابعة";
            case "🔴" -> "تدخل ضروري";
            default -> "مستقرة";
        };
        lblHealthStatus.setText("الحالة الصحية: " + txt);
        lblHealthStatus.setTextFill(Color.web(getColorText(statusColor)));
    }

    private String getColorBackground(String code) {
        return switch (code) {
            case "🟢" -> "#dcfce7";
            case "🟡" -> "#fef08a";
            case "🟠" -> "#ffedd5";
            case "🔴" -> "#fee2e2";
            case "🔵" -> "#e0e7ff";
            default -> "#f8fafc";
        };
    }

    private String getColorText(String code) {
        return switch (code) {
            case "🟢" -> "#15803d";
            case "🟡" -> "#a16207";
            case "🟠" -> "#c2410c";
            case "🔴" -> "#b91c1c";
            case "🔵" -> "#1e3a8a";
            default -> "#334155";
        };
    }

    private String formatNum(double val) {
        return String.format(Locale.US, "%.1f", val);
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.toLocalDate().toString() : "-";
    }

    private String formatDateOnly(LocalDateTime dt) {
        return dt != null ? dt.format(dateFormatter) : "غير محدد";
    }

    // =================================================================================
    // EXPORT & INTERACTION HANDLERS
    // =================================================================================

    /**
     * الدالة المربوطة بملف FXML لمعالجة ضغط زر تصدير الـ PDF بشكل آمن وبدون تجميد النظام.
     */
    @FXML
    void exportToPDF() {
        // 1. جلب الجلسة المحددة حالياً من القائمة الجانبية
        Session currentSession = lvSessions.getSelectionModel().getSelectedItem();

        if (currentSession == null) {
            showAlert("تنبيه", "يرجى اختيار جلسة من القائمة الجانبية أولاً لمراجعتها وطباعتها.", Alert.AlertType.WARNING);
            return;
        }

        // المصفوفات تستخدم كحاويات مؤقتة لنقل الكائنات المستخرجة من خيط الخلفية إلى خيط الواجهة
        final Parent[] rootHolder = new Parent[1];
        final SessionReportController[] controllerHolder = new SessionReportController[1];

        // 2. تشغيل عملية التحميل في الخلفية عبر الـ BackgroundRunner
        BackgroundRunner.run(
                "جاري تحميل واجهة مراجعة التقرير... ⏳",
                () -> {
                    // كود التحميل والتجهيز البرمجي (يتم في الخلفية لضمان سلاسة حركة الأنيميشن والدائرة)
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SessionReport.fxml"));
                    rootHolder[0] = loader.load();
                    controllerHolder[0] = loader.getController();

                    // تمرير المعطيات والسياق للكنترولر الجديد بداخل خيط الخلفية
                    controllerHolder[0].setSessionContext(client, currentSession);

                    return true; // إعلام الكلاس بنجاح عملية التحميل والتجهيز
                },
                () -> {
                    // 3. هذا الجزء (onSuccess) يعمل تلقائياً في خيط الواجهة (UI Thread) فور انتهاء التحميل
                    if (rootHolder[0] != null) {
                        String clientName = (client != null && client.getFullName() != null) ? String.join(" ", client.getFullName()) : "غير معروف";

                        // بناء النافذة وعرضها كشاشة منبثقة مراجعة حصرية (Modal) بأمان تام
                        Stage stage = new Stage();
                        stage.setTitle("📝 مراجعة التقرير الطبي قبل الطباعة - " + clientName);
                        stage.setScene(new Scene(rootHolder[0]));

                        // منع التفاعل مع الشاشة الخلفية لحين إنهاء المراجعة
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.show();
                    }
                }
        );
    }
    /**
     * يفتح واجهة كتابة ومراجعة التقرير المنسقة (SessionReport)
     * ويمرر لها بيانات الجلسة النشطة المحددة حالياً من القائمة.
     */
    private void resetExportButton() {
        Platform.runLater(() -> {
            btnExportPDF.setDisable(false);
            btnExportPDF.setText("📄 تصدير PDF");
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-font-family: 'System'; -fx-font-size: 14px;");

            alert.showAndWait();
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        showAlert(title, content, type);
    }

    @FXML
    void handleClose() {
        if (btnExportPDF != null && btnExportPDF.getScene() != null) {
            Stage stage = (Stage) btnExportPDF.getScene().getWindow();
            stage.close();
        }
    }
}