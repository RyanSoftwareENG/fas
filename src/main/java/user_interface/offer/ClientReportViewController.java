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
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
import java.util.stream.Collectors;

/**
 * ========================================================================
 * ClientReportViewController
 * ========================================================================
 *
 * الشاشة الشاملة لملف العميل.
 *
 * الوظائف:
 * 1) عرض بيانات العميل.
 * 2) إدارة الجلسات والفلترة.
 * 3) Dashboard للمؤشرات.
 * 4) الرسوم البيانية.
 * 5) التفاصيل الجسدية والغذائية.
 * 6) المقارنة بين الجلسات.
 * 7) محرك التحليل الذكي المحلي.
 * 8) تجهيز تقرير الجلسة للتصدير.
 * 9) اختصارات لوحة المفاتيح.
 *
 * ملاحظة مهمة:
 * التحليل الذكي ليس تشخيصاً طبياً ولا بديلاً عن قرار المختص.
 * لا يتم افتراض البيانات غير الموجودة.
 */
public class ClientReportViewController {

    // ========================================================================
    // FXML - HEADER
    // ========================================================================

    @FXML
    private ImageView imgClient;

    @FXML
    private Label lblClientName;

    @FXML
    private Label lblClientId;

    @FXML
    private Label lblClientAge;

    @FXML
    private Label lblClientGender;

    @FXML
    private Label lblClientPhone;

    @FXML
    private Label lblRegDate;

    @FXML
    private Label lblSessionCount;

    @FXML
    private Label lblLastSessionDate;

    @FXML
    private Label lblHealthStatus;

    @FXML
    private Button btnExportPDF;


    // ========================================================================
    // FXML - SIDEBAR
    // ========================================================================

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private ListView<Session> lvSessions;


    // ========================================================================
    // FXML - DASHBOARD
    // ========================================================================

    @FXML
    private FlowPane cardsContainer;


    // ========================================================================
    // FXML - CHARTS
    // ========================================================================

    @FXML
    private FlowPane chartTogglesContainer;

    @FXML
    private LineChart<String, Number> mainChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;


    // ========================================================================
    // FXML - DETAILS
    // ========================================================================

    @FXML
    private GridPane measurementsGrid;

    @FXML
    private GridPane nutritionGrid;

    @FXML
    private TableView<Object> tvExaminations;

    @FXML
    private TableColumn<Object, String> colExamName;

    @FXML
    private TableColumn<Object, String> colExamValue;

    @FXML
    private TableColumn<Object, String> colExamRef;

    @FXML
    private TableColumn<Object, String> colExamStatus;


    // ========================================================================
    // FXML - COMPARISON / AI
    // ========================================================================

    @FXML
    private GridPane comparisonGrid;

    @FXML
    private TextArea txtSmartReport;


    // ========================================================================
    // STATE
    // ========================================================================

    private Client client;

    /**
     * القائمة الأصلية لجميع الجلسات.
     */
    private final ObservableList<Session> masterSessionList =
            FXCollections.observableArrayList();

    /**
     * القائمة المفلترة المعروضة في ListView.
     */
    private FilteredList<Session> filteredSessions;

    /**
     * تنسيق التاريخ.
     */
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * منع تكرار الـListeners.
     */
    private boolean sessionSelectionListenerInstalled = false;

    private boolean dateFilterListenersInstalled = false;

    /**
     * منع إعادة تسجيل الاختصارات.
     */
    private boolean keyboardShortcutsInstalled = false;

    /**
     * Series الرسوم.
     */
    private final Map<String, XYChart.Series<String, Number>>
            chartSeriesMap = new LinkedHashMap<>();


    // ========================================================================
    // INITIALIZE
    // ========================================================================

    @FXML
    private void initialize() {

        configureCharts();

        configureExaminationsTable();

        if (txtSmartReport != null) {

            txtSmartReport.setText(
                    "حدد جلسة من القائمة لعرض التحليل الذكي الكامل."
            );
        }
    }


    // ========================================================================
    // CLIENT DATA
    // ========================================================================

    /**
     * استقبال بيانات العميل من الشاشة السابقة.
     */
    public void setClientData(Client client) {

        if (client == null) {

            showAlert(
                    "خطأ في البيانات",
                    "تعذر فتح ملف العميل لأن بيانات العميل غير متوفرة.",
                    Alert.AlertType.ERROR
            );

            return;
        }


        this.client = client;


        // ================================================================
        // 1. تحميل الجلسات
        // ================================================================

        loadSessions();


        // ================================================================
        // 2. بيانات الرأس
        // ================================================================

        initializeHeader();


        // ================================================================
        // 3. قائمة الجلسات
        // ================================================================

        initializeSessionList();


        // ================================================================
        // 4. الفلاتر
        // ================================================================

        initializeDateFilters();


        // ================================================================
        // 5. الاختصارات
        // ================================================================

        installKeyboardShortcuts();


        // ================================================================
        // 6. الرسم البياني
        //
        // مهم جداً:
        // هذا الاستدعاء كان مفقوداً في النسخة السابقة.
        // ================================================================

        setupCharts();


        // ================================================================
        // 7. عرض أحدث جلسة
        // ================================================================

        if (filteredSessions != null &&
                !filteredSessions.isEmpty()) {

            Session latest =
                    filteredSessions.get(
                            filteredSessions.size() - 1
                    );


            lvSessions
                    .getSelectionModel()
                    .select(latest);


            /*
             * لا نعتمد فقط على Listener.
             * نقوم بالتحديث بشكل صريح حتى تظهر:
             *
             * Dashboard
             * Details
             * Comparison
             * Smart Report
             */
            refreshSelectedSession(latest);

        } else {

            clearClientSessionViews();

            txtSmartReport.setText(
                    buildNoSessionMessage()
            );
        }
    }


    // ========================================================================
    // LOAD SESSIONS
    // ========================================================================

    private void loadSessions() {

        masterSessionList.clear();


        if (client == null) {

            filteredSessions =
                    new FilteredList<>(
                            masterSessionList,
                            session -> true
                    );

            return;
        }


        if (client.getSessions() != null) {

            for (Session session :
                    client.getSessions()) {

                if (session != null) {

                    masterSessionList.add(
                            session
                    );
                }
            }
        }


        /*
         * ترتيب زمني تصاعدي:
         *
         * الأولى -> الأخيرة
         */
        masterSessionList.sort(
                Comparator.comparing(
                        Session::getUploadTime,
                        Comparator.nullsFirst(
                                Comparator.naturalOrder()
                        )
                )
        );


        filteredSessions =
                new FilteredList<>(
                        masterSessionList,
                        session -> true
                );
    }


    // ========================================================================
    // HEADER
    // ========================================================================

    private void initializeHeader() {

        if (client == null) {
            return;
        }


        lblClientName.setText(
                getClientName()
        );


        lblClientId.setText(
                "رقم العميل: #" +
                        safeObject(
                                client.getClientID()
                        )
        );


        lblClientAge.setText(
                "العمر: " +
                        client.getAge() +
                        " سنة"
        );


        lblClientGender.setText(
                "الجنس: " +
                        formatGender(
                                client.getGender()
                        )
        );


        lblClientPhone.setText(
                "الهاتف: " +
                        safeText(
                                client.getContactNumber()
                        )
        );


        lblSessionCount.setText(
                "عدد الجلسات: " +
                        masterSessionList.size()
        );


        if (masterSessionList.isEmpty()) {

            lblRegDate.setText(
                    "التسجيل: -"
            );

            lblLastSessionDate.setText(
                    "آخر متابعة: -"
            );

            setHealthStatus(
                    "لا توجد بيانات",
                    "#cbd5e1"
            );

            return;
        }


        Session first =
                masterSessionList.get(0);

        Session last =
                masterSessionList.get(
                        masterSessionList.size() - 1
                );


        lblRegDate.setText(
                "التسجيل: " +
                        formatDateTime(
                                first.getUploadTime()
                        )
        );


        lblLastSessionDate.setText(
                "آخر متابعة: " +
                        formatDateTime(
                                last.getUploadTime()
                        )
        );


        updateHealthStatus(
                last.getBodyData()
        );
    }


    private String getClientName() {

        if (client == null) {
            return "عميل غير معروف";
        }


        String fullName =
                client.getFullName();


        if (fullName == null ||
                fullName.isBlank()) {

            return "عميل غير معروف";
        }


        /*
         * إزالة الفراغات الزائدة.
         */
        return Arrays.stream(
                        fullName.trim().split("\\s+")
                )
                .filter(
                        part ->
                                part != null &&
                                        !part.isBlank()
                )
                .collect(
                        Collectors.joining(" ")
                );
    }


    private String formatGender(
            char gender
    ) {

        return switch (gender) {

            case 'M', 'm' ->
                    "ذكر";

            case 'F', 'f' ->
                    "أنثى";

            default ->
                    "غير محدد";
        };
    }


    // ========================================================================
    // SESSION LIST
    // ========================================================================

    private void initializeSessionList() {

        if (lvSessions == null ||
                filteredSessions == null) {

            return;
        }


        lvSessions.setItems(
                filteredSessions
        );


        lvSessions.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    Session session,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        session,
                                        empty
                                );


                                if (empty ||
                                        session == null) {

                                    setText(null);

                                    setGraphic(null);

                                    setStyle(
                                            "-fx-background-color: transparent;"
                                    );

                                    return;
                                }


                                setText(null);


                                setGraphic(
                                        createSessionCard(
                                                session
                                        )
                                );


                                updateSelectionStyle();
                            }


                            private void updateSelectionStyle() {

                                if (isSelected()) {

                                    setStyle(
                                            "-fx-background-color:#eff6ff;" +
                                                    "-fx-background-radius:10;" +
                                                    "-fx-border-color:#2563eb;" +
                                                    "-fx-border-width:0 0 0 3;"
                                    );

                                } else {

                                    setStyle(
                                            "-fx-background-color:transparent;" +
                                                    "-fx-border-width:0;"
                                    );
                                }
                            }
                        }
        );


        if (!sessionSelectionListenerInstalled) {

            lvSessions
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener(
                            (observable,
                             oldSession,
                             newSession) -> {

                                if (newSession != null) {

                                    /*
                                     * Listener يعمل على JavaFX Thread،
                                     * فلا حاجة إلى Platform.runLater هنا.
                                     */
                                    refreshSelectedSession(
                                            newSession
                                    );
                                }
                            }
                    );


            sessionSelectionListenerInstalled = true;
        }
    }


    private VBox createSessionCard(
            Session session
    ) {

        VBox card =
                new VBox(6);


        card.setPadding(
                new Insets(10)
        );


        card.setMinHeight(
                78
        );


        card.setMaxWidth(
                Double.MAX_VALUE
        );


        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:10;" +
                        "-fx-effect:dropshadow(" +
                        "three-pass-box," +
                        "rgba(15,23,42,0.05)," +
                        "5,0,0,1" +
                        ");"
        );


        // ================================================================
        // HEADER
        // ================================================================

        HBox header =
                new HBox(7);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );


        Label title =
                new Label(
                        "جلسة #" +
                                safeObject(
                                        session.getId()
                                )
                );


        title.setStyle(
                "-fx-text-fill:#1e3a8a;" +
                        "-fx-font-size:12px;" +
                        "-fx-font-weight:bold;"
        );


        Region spacer =
                new Region();


        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        Label date =
                new Label(
                        formatDateOnly(
                                session.getUploadTime()
                        )
                );


        date.setStyle(
                "-fx-text-fill:#94a3b8;" +
                        "-fx-font-size:10px;"
        );


        header.getChildren().addAll(
                title,
                spacer,
                date
        );


        // ================================================================
        // DATA
        // ================================================================

        VBox bodyBox =
                new VBox(4);


        BodyData body =
                session.getBodyData();


        if (body == null) {

            Label noData =
                    new Label(
                            "⚠ لا توجد قياسات"
                    );


            noData.setStyle(
                    "-fx-text-fill:#b45309;" +
                            "-fx-font-size:10px;"
            );


            bodyBox.getChildren().add(
                    noData
            );

        } else {

            HBox metrics =
                    new HBox(12);


            if (body.getWeight() != null) {

                metrics.getChildren().add(
                        createMiniMetric(
                                "الوزن",
                                formatNum(
                                        body.getWeight()
                                ) + " kg"
                        )
                );
            }


            if (body.getBodyFatPercentage() != null) {

                metrics.getChildren().add(
                        createMiniMetric(
                                "الدهون",
                                formatNum(
                                        body.getBodyFatPercentage()
                                ) + "%"
                        )
                );
            }


            if (body.getSmm() != null) {

                metrics.getChildren().add(
                        createMiniMetric(
                                "العضلات",
                                formatNum(
                                        body.getSmm()
                                ) + " kg"
                        )
                );
            }


            if (metrics.getChildren().isEmpty()) {

                bodyBox.getChildren().add(
                        new Label(
                                "⚠ القياسات غير مكتملة"
                        )
                );

            } else {

                bodyBox.getChildren().add(
                        metrics
                );
            }
        }


        card.getChildren().addAll(
                header,
                bodyBox
        );


        return card;
    }


    private VBox createMiniMetric(
            String title,
            String value
    ) {

        VBox box =
                new VBox(1);


        Label titleLabel =
                new Label(
                        title
                );


        titleLabel.setStyle(
                "-fx-text-fill:#94a3b8;" +
                        "-fx-font-size:9px;"
        );


        Label valueLabel =
                new Label(
                        value
                );


        valueLabel.setStyle(
                "-fx-text-fill:#334155;" +
                        "-fx-font-size:10px;" +
                        "-fx-font-weight:bold;"
        );


        box.getChildren().addAll(
                titleLabel,
                valueLabel
        );


        return box;
    }


    // ========================================================================
    // SELECTED SESSION
    // ========================================================================

    private void refreshSelectedSession(
            Session session
    ) {

        if (session == null) {
            return;
        }


        refreshDashboard(
                session
        );


        refreshDetailsGrids(
                session
        );


        updateComparisonReport(
                session
        );


        generateSmartReport(
                session
        );


        updateHealthStatus(
                session.getBodyData()
        );


        if (lvSessions != null) {

            lvSessions.refresh();
        }
    }


    // ========================================================================
    // DATE FILTERS
    // ========================================================================

    private void initializeDateFilters() {

        if (dpStartDate == null ||
                dpEndDate == null) {

            return;
        }


        if (dateFilterListenersInstalled) {
            return;
        }


        dpStartDate
                .valueProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                filterSessions()
                );


        dpEndDate
                .valueProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                filterSessions()
                );


        dateFilterListenersInstalled = true;
    }


    private void filterSessions() {

        if (filteredSessions == null) {
            return;
        }


        LocalDate start =
                dpStartDate.getValue();


        LocalDate end =
                dpEndDate.getValue();


        // ================================================================
        // INVALID RANGE
        // ================================================================

        if (start != null &&
                end != null &&
                start.isAfter(end)) {

            filteredSessions.setPredicate(
                    session -> false
            );


            clearClientSessionViews();


            txtSmartReport.setText(
                    "الفترة الزمنية غير صحيحة.\n\n" +
                            "يجب أن يكون تاريخ البداية قبل أو مساوياً لتاريخ النهاية."
            );


            return;
        }


        // ================================================================
        // FILTER
        // ================================================================

        filteredSessions.setPredicate(
                session -> {

                    if (session == null ||
                            session.getUploadTime() == null) {

                        return false;
                    }


                    LocalDate date =
                            session.getUploadTime()
                                    .toLocalDate();


                    boolean validStart =
                            start == null ||
                                    !date.isBefore(start);


                    boolean validEnd =
                            end == null ||
                                    !date.isAfter(end);


                    return validStart &&
                            validEnd;
                }
        );


        // ================================================================
        // REBUILD CHARTS
        // ================================================================

        setupCharts();


        // ================================================================
        // EMPTY
        // ================================================================

        if (filteredSessions.isEmpty()) {

            lvSessions
                    .getSelectionModel()
                    .clearSelection();


            clearClientSessionViews();


            txtSmartReport.setText(
                    "لا توجد جلسات ضمن الفترة الزمنية المحددة."
            );


            return;
        }


        // ================================================================
        // SELECT LATEST
        // ================================================================

        Session latest =
                filteredSessions.get(
                        filteredSessions.size() - 1
                );


        lvSessions
                .getSelectionModel()
                .select(latest);


        /*
         * تحديث مباشر.
         */
        refreshSelectedSession(
                latest
        );
    }


    // ========================================================================
    // DASHBOARD
    // ========================================================================

    private void refreshDashboard(
            Session session
    ) {

        cardsContainer
                .getChildren()
                .clear();


        if (session == null) {

            return;
        }


        BodyData body =
                session.getBodyData();


        if (body == null) {

            cardsContainer
                    .getChildren()
                    .add(
                            createEmptyStateCard(
                                    "لا توجد قياسات",
                                    "لا توجد بيانات جسدية مسجلة لهذه الجلسة."
                            )
                    );

            return;
        }


        char gender =
                client != null
                        ? client.getGender()
                        : '\0';


        int age =
                client != null
                        ? client.getAge()
                        : 0;


        // ================================================================
        // WEIGHT
        // ================================================================

        if (body.getWeight() != null) {

            addDashboardCard(
                    "الوزن",
                    body.getWeight().doubleValue(),
                    "kg",
                    "🔵",
                    "⚖",
                    "الوزن المسجل في الجلسة الحالية."
            );
        }


        // ================================================================
        // BMI
        // ================================================================

        if (body.getBMI() != null) {

            String state =
                    evaluateBMI(
                            body.getBMI()
                    );


            addDashboardCard(
                    "مؤشر كتلة الجسم",
                    body.getBMI().doubleValue(),
                    "BMI",
                    state,
                    "◉",
                    "قيمة BMI المسجلة أو المحسوبة داخل BodyData."
            );
        }


        // ================================================================
        // FAT
        // ================================================================

        if (body.getBodyFatPercentage() != null) {

            String state =
                    evaluateFat(
                            body.getBodyFatPercentage()
                                    .doubleValue(),
                            String.valueOf(gender)
                    );


            addDashboardCard(
                    "نسبة الدهون",
                    body.getBodyFatPercentage().doubleValue(),
                    "%",
                    state,
                    "◌",
                    "نسبة الدهون المسجلة في بيانات الجلسة."
            );
        }


        // ================================================================
        // MUSCLE
        // ================================================================

        if (body.getSmm() != null) {

            addDashboardCard(
                    "الكتلة العضلية",
                    body.getSmm().doubleValue(),
                    "kg",
                    "🟢",
                    "💪",
                    "الكتلة العضلية الهيكلية المسجلة."
            );
        }


        // ================================================================
        // BMR
        // ================================================================

        BigDecimal bmr =
                calculateBMR(
                        body,
                        gender,
                        age
                );


        if (bmr != null) {

            addDashboardCard(
                    "معدل الأيض الأساسي BMR",
                    bmr.doubleValue(),
                    "kcal",
                    "🟡",
                    "🔥",
                    "قيمة حسابية ناتجة عن دالة BMR في BodyData."
            );
        }


        // ================================================================
        // TDEE
        // ================================================================

        BigDecimal tdee =
                calculateTDEE(
                        body,
                        gender,
                        age
                );


        if (tdee != null) {

            addDashboardCard(
                    "الاستهلاك اليومي TDEE",
                    tdee.doubleValue(),
                    "kcal",
                    "🔵",
                    "⚡",
                    "قيمة حسابية ناتجة عن دالة TDEE في BodyData."
            );
        }


        // ================================================================
        // LBM
        // ================================================================

        if (body.getWeight() != null &&
                body.getBodyFatPercentage() != null) {

            double lbm =
                    calculateLBM(
                            body.getWeight(),
                            body.getBodyFatPercentage()
                    );


            if (lbm >= 0) {

                addDashboardCard(
                        "الكتلة الخالية من الدهون",
                        lbm,
                        "kg",
                        "🟢",
                        "◈",
                        "تقدير حسابي اعتماداً على الوزن ونسبة الدهون."
                );
            }
        }


        // ================================================================
        // WATER ESTIMATE
        // ================================================================

        if (body.getWeight() != null) {

            double water =
                    calculateWaterEstimate(
                            body.getWeight(),
                            String.valueOf(gender)
                    );


            addDashboardCard(
                    "تقدير الماء",
                    water,
                    "L",
                    "🔵",
                    "💧",
                    "تقدير حسابي فقط، وليس وصفاً علاجياً."
            );
        }


        // ================================================================
        // FALLBACK
        // ================================================================

        if (cardsContainer.getChildren().isEmpty()) {

            cardsContainer
                    .getChildren()
                    .add(
                            createEmptyStateCard(
                                    "بيانات غير كافية",
                                    "البيانات الحالية لا تحتوي على مؤشرات رقمية كافية."
                            )
                    );
        }
    }


    private VBox createEmptyStateCard(
            String title,
            String description
    ) {

        VBox card =
                new VBox(8);


        card.setAlignment(
                Pos.CENTER
        );


        card.setPrefSize(
                240,
                115
        );


        card.setPadding(
                new Insets(15)
        );


        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:12;"
        );


        Label titleLabel =
                new Label(
                        title
                );


        titleLabel.setStyle(
                "-fx-text-fill:#334155;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;"
        );


        Label descriptionLabel =
                new Label(
                        description
                );


        descriptionLabel.setWrapText(true);

        descriptionLabel.setAlignment(
                Pos.CENTER
        );


        descriptionLabel.setStyle(
                "-fx-text-fill:#94a3b8;" +
                        "-fx-font-size:10px;"
        );


        card.getChildren().addAll(
                titleLabel,
                descriptionLabel
        );


        return card;
    }


    private void addDashboardCard(
            String title,
            double value,
            String unit,
            String state,
            String icon,
            String tooltipText
    ) {

        VBox card =
                new VBox(8);


        String bg =
                getColorBackground(
                        state
                );


        String text =
                getColorText(
                        state
                );


        card.setPrefSize(
                190,
                112
        );


        card.setMinSize(
                170,
                105
        );


        card.setAlignment(
                Pos.CENTER
        );


        card.setPadding(
                new Insets(13)
        );


        card.setStyle(
                "-fx-background-color:" +
                        bg +
                        ";" +
                        "-fx-background-radius:13;" +
                        "-fx-border-color:" +
                        text +
                        "35;" +
                        "-fx-border-radius:13;" +
                        "-fx-effect:dropshadow(" +
                        "three-pass-box," +
                        "rgba(15,23,42,0.06)," +
                        "7,0,0,2" +
                        ");"
        );


        Label titleLabel =
                new Label(
                        icon +
                                "  " +
                                title
                );


        titleLabel.setWrapText(
                true
        );


        titleLabel.setAlignment(
                Pos.CENTER
        );


        titleLabel.setStyle(
                "-fx-font-size:11px;" +
                        "-fx-text-fill:#475569;" +
                        "-fx-font-weight:bold;"
        );


        Label valueLabel =
                new Label(
                        formatNum(
                                value
                        ) +
                                " " +
                                unit
                );


        valueLabel.setStyle(
                "-fx-font-size:21px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:" +
                        text +
                        ";"
        );


        Label stateLabel =
                new Label(
                        getStateText(
                                state
                        )
                );


        stateLabel.setStyle(
                "-fx-font-size:9px;" +
                        "-fx-text-fill:" +
                        text +
                        ";"
        );


        card.getChildren().addAll(
                titleLabel,
                valueLabel,
                stateLabel
        );


        Tooltip tooltip =
                new Tooltip(
                        tooltipText
                );


        tooltip.setWrapText(
                true
        );


        tooltip.setMaxWidth(
                320
        );


        Tooltip.install(
                card,
                tooltip
        );


        cardsContainer
                .getChildren()
                .add(card);
    }


    // ========================================================================
    // DETAILS
    // ========================================================================

    private void refreshDetailsGrids(
            Session session
    ) {

        measurementsGrid
                .getChildren()
                .clear();


        nutritionGrid
                .getChildren()
                .clear();


        if (session == null) {
            return;
        }


        BodyData body =
                session.getBodyData();


        NutritionPlan plan =
                session.getNutritionPlan();


        // ================================================================
        // BODY
        // ================================================================

        if (body == null) {

            addEmptyGridMessage(
                    measurementsGrid,
                    "لا توجد قياسات جسدية مسجلة لهذه الجلسة."
            );

        } else {

            List<String[]> metrics =
                    new ArrayList<>();


            if (body.getWeight() != null) {

                metrics.add(
                        metric(
                                "الوزن الكلي",
                                formatNum(
                                        body.getWeight()
                                ) + " كج"
                        )
                );
            }


            if (body.getBMI() != null) {

                metrics.add(
                        metric(
                                "مؤشر كتلة الجسم BMI",
                                formatNum(
                                        body.getBMI()
                                )
                        )
                );
            }


            if (body.getBodyFatPercentage() != null) {

                metrics.add(
                        metric(
                                "نسبة الدهون",
                                formatNum(
                                        body.getBodyFatPercentage()
                                ) + "%"
                        )
                );
            }


            if (body.getSmm() != null) {

                metrics.add(
                        metric(
                                "الكتلة العضلية الهيكلية",
                                formatNum(
                                        body.getSmm()
                                ) + " كج"
                        )
                );
            }


            BigDecimal bmr =
                    calculateBMR(
                            body,
                            client.getGender(),
                            client.getAge()
                    );


            if (bmr != null) {

                metrics.add(
                        metric(
                                "معدل الأيض الأساسي BMR",
                                formatNum(
                                        bmr
                                ) + " سعرة"
                        )
                );
            }


            BigDecimal tdee =
                    calculateTDEE(
                            body,
                            client.getGender(),
                            client.getAge()
                    );


            if (tdee != null) {

                metrics.add(
                        metric(
                                "الاستهلاك اليومي TDEE",
                                formatNum(
                                        tdee
                                ) + " سعرة"
                        )
                );
            }


            if (body.getWeight() != null &&
                    body.getBodyFatPercentage() != null) {

                double lbm =
                        calculateLBM(
                                body.getWeight(),
                                body.getBodyFatPercentage()
                        );


                if (lbm >= 0) {

                    metrics.add(
                            metric(
                                    "الكتلة الخالية من الدهون",
                                    formatNum(lbm) +
                                            " كج"
                            )
                    );
                }
            }


            if (metrics.isEmpty()) {

                addEmptyGridMessage(
                        measurementsGrid,
                        "القياسات موجودة ولكن القيم الرقمية غير متوفرة."
                );

            } else {

                populateGrid(
                        measurementsGrid,
                        metrics,
                        3
                );
            }
        }


        // ================================================================
        // NUTRITION
        // ================================================================

        if (plan == null) {

            addEmptyGridMessage(
                    nutritionGrid,
                    "لم يتم إرفاق خطة غذائية بهذه الجلسة."
            );

            return;
        }


        List<String[]> nutrition =
                new ArrayList<>();


        if (plan.getTotalCalories() != null) {

            nutrition.add(
                    metric(
                            "السعرات المستهدفة",
                            formatNum(
                                    plan.getTotalCalories()
                            ) + " سعرة"
                    )
            );
        }


        if (plan.getProteinAmount() != null) {

            nutrition.add(
                    metric(
                            "البروتين",
                            formatNum(
                                    plan.getProteinAmount()
                            ) + " جم"
                    )
            );
        }


        if (plan.getCarbohydratesAmount() != null) {

            nutrition.add(
                    metric(
                            "الكربوهيدرات",
                            formatNum(
                                    plan.getCarbohydratesAmount()
                            ) + " جم"
                    )
            );
        }


        if (plan.getFatAmount() != null) {

            nutrition.add(
                    metric(
                            "الدهون",
                            formatNum(
                                    plan.getFatAmount()
                            ) + " جم"
                    )
            );
        }


        nutrition.add(
                metric(
                        "عدد الوجبات",
                        String.valueOf(
                                plan.getMealsCount()
                        )
                )
        );


        if (plan.getWaterIntake() != null) {

            nutrition.add(
                    metric(
                            "الماء المسجل",
                            safeObject(
                                    plan.getWaterIntake()
                            )
                    )
            );
        }


        if (!nutrition.isEmpty()) {

            populateGrid(
                    nutritionGrid,
                    nutrition,
                    3
            );
        }


        if (plan.getNotes() != null &&
                !plan.getNotes().isBlank()) {

            int noteRow =
                    (nutrition.size() + 2) / 3 + 1;


            Label title =
                    new Label(
                            "ملاحظات الخطة"
                    );


            title.setStyle(
                    "-fx-text-fill:#1e3a8a;" +
                            "-fx-font-size:12px;" +
                            "-fx-font-weight:bold;"
            );


            Label content =
                    new Label(
                            plan.getNotes()
                    );


            content.setWrapText(
                    true
            );


            content.setMaxWidth(
                    Double.MAX_VALUE
            );


            content.setStyle(
                    "-fx-text-fill:#475569;" +
                            "-fx-font-size:11px;" +
                            "-fx-background-color:#f8fafc;" +
                            "-fx-background-radius:9;" +
                            "-fx-padding:12;"
            );


            nutritionGrid.add(
                    title,
                    0,
                    noteRow,
                    3,
                    1
            );


            nutritionGrid.add(
                    content,
                    0,
                    noteRow + 1,
                    3,
                    1
            );
        }
    }


    private String[] metric(
            String title,
            String value
    ) {

        return new String[]{
                title,
                value
        };
    }


    private void populateGrid(
            GridPane grid,
            List<String[]> data,
            int columns
    ) {

        grid.setMaxWidth(
                Double.MAX_VALUE
        );


        int column = 0;
        int row = 0;


        for (String[] pair : data) {

            VBox cell =
                    createDataCell(
                            pair[0],
                            pair[1]
                    );


            grid.add(
                    cell,
                    column,
                    row
            );


            GridPane.setFillWidth(
                    cell,
                    true
            );


            GridPane.setHgrow(
                    cell,
                    Priority.ALWAYS
            );


            column++;


            if (column >= columns) {

                column = 0;

                row++;
            }
        }
    }


    private VBox createDataCell(
            String title,
            String value
    ) {

        VBox cell =
                new VBox(4);


        cell.setPadding(
                new Insets(12)
        );


        cell.setMinHeight(
                66
        );


        cell.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:10;" +
                        "-fx-background-radius:10;"
        );


        Label titleLabel =
                new Label(
                        title
                );


        titleLabel.setWrapText(
                true
        );


        titleLabel.setStyle(
                "-fx-text-fill:#94a3b8;" +
                        "-fx-font-size:10px;"
        );


        Label valueLabel =
                new Label(
                        value
                );


        valueLabel.setWrapText(
                true
        );


        valueLabel.setStyle(
                "-fx-text-fill:#0f172a;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;"
        );


        cell.getChildren().addAll(
                titleLabel,
                valueLabel
        );


        return cell;
    }


    private void addEmptyGridMessage(
            GridPane grid,
            String text
    ) {

        Label label =
                new Label(
                        text
                );


        label.setWrapText(
                true
        );


        label.setStyle(
                "-fx-text-fill:#94a3b8;" +
                        "-fx-font-size:11px;" +
                        "-fx-padding:10;"
        );


        grid.add(
                label,
                0,
                0,
                3,
                1
        );
    }


    // ========================================================================
    // CHARTS
    // ========================================================================

    private void configureCharts() {

        if (mainChart == null) {
            return;
        }


        mainChart.setAnimated(
                false
        );


        mainChart.setCreateSymbols(
                true
        );


        mainChart.setLegendVisible(
                true
        );


        mainChart.setTitle(
                "تطور القياسات عبر جلسات المتابعة"
        );


        if (xAxis != null) {

            xAxis.setLabel(
                    "تاريخ الجلسة"
            );
        }


        if (yAxis != null) {

            yAxis.setLabel(
                    "القيمة"
            );


            yAxis.setForceZeroInRange(
                    false
            );
        }
    }


    /**
     * يبني الرسوم من filteredSessions الحالية.
     */
    private void setupCharts() {

        if (mainChart == null ||
                chartTogglesContainer == null) {

            return;
        }


        mainChart.getData()
                .clear();


        chartSeriesMap.clear();


        chartTogglesContainer
                .getChildren()
                .clear();


        if (filteredSessions == null ||
                filteredSessions.isEmpty()) {

            return;
        }


        createChartSeries(
                "الوزن (kg)",
                "⚖",
                "#2563eb"
        );


        createChartSeries(
                "الدهون (%)",
                "◌",
                "#dc2626"
        );


        createChartSeries(
                "العضلات (kg)",
                "💪",
                "#16a34a"
        );


        for (Session session :
                filteredSessions) {

            if (session == null ||
                    session.getBodyData() == null) {

                continue;
            }


            BodyData body =
                    session.getBodyData();


            String date =
                    formatDateOnly(
                            session.getUploadTime()
                    );


            if (body.getWeight() != null) {

                addDataPoint(
                        chartSeriesMap.get(
                                "الوزن (kg)"
                        ),
                        date,
                        body.getWeight()
                                .doubleValue()
                );
            }


            if (body.getBodyFatPercentage() != null) {

                addDataPoint(
                        chartSeriesMap.get(
                                "الدهون (%)"
                        ),
                        date,
                        body.getBodyFatPercentage()
                                .doubleValue()
                );
            }


            if (body.getSmm() != null) {

                addDataPoint(
                        chartSeriesMap.get(
                                "العضلات (kg)"
                        ),
                        date,
                        body.getSmm()
                                .doubleValue()
                );
            }
        }


        /*
         * لا نضيف Series الفارغة.
         */
        for (XYChart.Series<String, Number> series :
                chartSeriesMap.values()) {

            if (!series.getData().isEmpty()) {

                mainChart.getData()
                        .add(series);
            }
        }


        Platform.runLater(
                this::applyChartStyles
        );
    }


    private void createChartSeries(
            String name,
            String icon,
            String color
    ) {

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();


        series.setName(
                icon +
                        " " +
                        name
        );


        chartSeriesMap.put(
                name,
                series
        );


        CheckBox toggle =
                new CheckBox(
                        icon +
                                " " +
                                name
                );


        toggle.setSelected(
                true
        );


        toggle.setStyle(
                "-fx-font-weight:bold;" +
                        "-fx-font-size:11px;" +
                        "-fx-text-fill:" +
                        color +
                        ";" +
                        "-fx-cursor:hand;"
        );


        toggle.selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         selected) ->
                                setSeriesVisible(
                                        series,
                                        selected
                                )
                );


        chartTogglesContainer
                .getChildren()
                .add(toggle);
    }


    private void setSeriesVisible(
            XYChart.Series<String, Number> series,
            boolean visible
    ) {

        if (series == null) {
            return;
        }


        if (series.getNode() != null) {

            series.getNode()
                    .setVisible(visible);
        }


        for (XYChart.Data<String, Number> data :
                series.getData()) {

            if (data.getNode() != null) {

                data.getNode()
                        .setVisible(visible);
            }
        }
    }


    private void applyChartStyles() {

        styleSeries(
                "الوزن (kg)",
                "#2563eb"
        );


        styleSeries(
                "الدهون (%)",
                "#dc2626"
        );


        styleSeries(
                "العضلات (kg)",
                "#16a34a"
        );
    }


    private void styleSeries(
            String name,
            String color
    ) {

        XYChart.Series<String, Number> series =
                chartSeriesMap.get(name);


        if (series == null) {
            return;
        }


        if (series.getNode() != null) {

            series.getNode()
                    .setStyle(
                            "-fx-stroke:" +
                                    color +
                                    ";" +
                                    "-fx-stroke-width:3px;"
                    );
        }


        for (XYChart.Data<String, Number> data :
                series.getData()) {

            if (data.getNode() != null) {

                data.getNode()
                        .setStyle(
                                "-fx-background-color:" +
                                        color +
                                        ", white;" +
                                        "-fx-background-insets:0,2;"
                        );
            }
        }
    }


    private void addDataPoint(
            XYChart.Series<String, Number> series,
            String label,
            double value
    ) {

        if (series == null) {
            return;
        }


        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {

            return;
        }


        series.getData()
                .add(
                        new XYChart.Data<>(
                                label,
                                value
                        )
                );
    }


    // ========================================================================
    // COMPARISON
    // ========================================================================

    private void updateComparisonReport(
            Session current
    ) {

        comparisonGrid
                .getChildren()
                .clear();


        if (filteredSessions == null ||
                filteredSessions.isEmpty() ||
                current == null) {

            return;
        }


        comparisonGrid.setPadding(
                new Insets(5)
        );


        int index =
                filteredSessions.indexOf(
                        current
                );


        Session first =
                filteredSessions.get(0);


        Session previous =
                index > 0
                        ? filteredSessions.get(
                        index - 1
                )
                        : null;


        createComparisonHeader();


        int row = 1;


        addComparisonRow(
                "الوزن",
                "kg",
                getWeight(current),
                getWeight(previous),
                getWeight(first),
                row++
        );


        addComparisonRow(
                "BMI",
                "",
                getBMI(current),
                getBMI(previous),
                getBMI(first),
                row++
        );


        addComparisonRow(
                "نسبة الدهون",
                "%",
                getFat(current),
                getFat(previous),
                getFat(first),
                row++
        );


        addComparisonRow(
                "الكتلة العضلية",
                "kg",
                getMuscle(current),
                getMuscle(previous),
                getMuscle(first),
                row++
        );


        addComparisonRow(
                "الكتلة الخالية من الدهون",
                "kg",
                getLBM(current),
                getLBM(previous),
                getLBM(first),
                row++
        );


        addComparisonRow(
                "BMR",
                "kcal",
                getBMR(current),
                getBMR(previous),
                getBMR(first),
                row++
        );


        addComparisonRow(
                "TDEE",
                "kcal",
                getTDEE(current),
                getTDEE(previous),
                getTDEE(first),
                row++
        );


        addComparisonRow(
                "سعرات الخطة",
                "kcal",
                getPlanCalories(current),
                getPlanCalories(previous),
                getPlanCalories(first),
                row++
        );


        addComparisonRow(
                "البروتين",
                "g",
                getPlanProtein(current),
                getPlanProtein(previous),
                getPlanProtein(first),
                row++
        );


        addComparisonRow(
                "الكربوهيدرات",
                "g",
                getPlanCarbs(current),
                getPlanCarbs(previous),
                getPlanCarbs(first),
                row++
        );


        addComparisonRow(
                "الدهون",
                "g",
                getPlanFat(current),
                getPlanFat(previous),
                getPlanFat(first),
                row
        );
    }


    private void createComparisonHeader() {

        addCompCell(
                "المؤشر",
                0,
                0,
                true,
                "#f1f5f9"
        );


        addCompCell(
                "الحالية",
                1,
                0,
                true,
                "#eef2ff"
        );


        addCompCell(
                "السابقة",
                2,
                0,
                true,
                "#f8fafc"
        );


        addCompCell(
                "الأولى",
                3,
                0,
                true,
                "#f8fafc"
        );


        addCompCell(
                "التغير عن الأولى",
                4,
                0,
                true,
                "#eef2ff"
        );
    }


    private void addComparisonRow(
            String metric,
            String unit,
            Double current,
            Double previous,
            Double first,
            int row
    ) {

        addCompCell(
                metric,
                0,
                row,
                true,
                "white"
        );


        addCompCell(
                formatNullable(
                        current,
                        unit
                ),
                1,
                row,
                false,
                "white"
        );


        addCompCell(
                formatNullable(
                        previous,
                        unit
                ),
                2,
                row,
                false,
                "white"
        );


        addCompCell(
                formatNullable(
                        first,
                        unit
                ),
                3,
                row,
                false,
                "white"
        );


        if (current == null ||
                first == null) {

            addCompCell(
                    "—",
                    4,
                    row,
                    false,
                    "#f8fafc"
            );

            return;
        }


        double diff =
                current - first;


        double percent =
                Math.abs(first) > 0.000001
                        ? diff / first * 100.0
                        : 0;


        String arrow;


        if (Math.abs(diff) < 0.0001) {

            arrow = "→";

        } else if (diff > 0) {

            arrow = "↑";

        } else {

            arrow = "↓";
        }


        String text =
                arrow +
                        " " +
                        formatNum(
                                Math.abs(diff)
                        ) +
                        (
                                unit.isBlank()
                                        ? ""
                                        : " " + unit
                        ) +
                        " (" +
                        formatNum(
                                Math.abs(percent)
                        ) +
                        "%)";


        String color =
                Math.abs(diff) < 0.0001
                        ? "#64748b"
                        : "#2563eb";


        Label label =
                new Label(
                        text
                );


        label.setMaxWidth(
                Double.MAX_VALUE
        );


        label.setAlignment(
                Pos.CENTER
        );


        label.setPadding(
                new Insets(10, 5, 10, 5)
        );


        label.setStyle(
                "-fx-text-fill:" +
                        color +
                        ";" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-color:#f8fafc;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-width:0 0 1 0;"
        );


        comparisonGrid.add(
                label,
                4,
                row
        );


        GridPane.setFillWidth(
                label,
                true
        );


        GridPane.setHalignment(
                label,
                HPos.CENTER
        );
    }


    private void addCompCell(
            String text,
            int column,
            int row,
            boolean header,
            String background
    ) {

        Label label =
                new Label(
                        text
                );


        label.setMaxWidth(
                Double.MAX_VALUE
        );


        label.setWrapText(
                true
        );


        label.setAlignment(
                Pos.CENTER
        );


        label.setPadding(
                new Insets(
                        11,
                        7,
                        11,
                        7
                )
        );


        label.setStyle(
                "-fx-background-color:" +
                        background +
                        ";" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-width:0 0 1 1;" +
                        (
                                header
                                        ? "-fx-text-fill:#1e3a8a;" +
                                        "-fx-font-weight:bold;"
                                        : "-fx-text-fill:#334155;"
                        )
        );


        comparisonGrid.add(
                label,
                column,
                row
        );


        GridPane.setFillWidth(
                label,
                true
        );


        GridPane.setHgrow(
                label,
                Priority.ALWAYS
        );


        GridPane.setHalignment(
                label,
                HPos.CENTER
        );
    }


    // ========================================================================
    // SMART AI REPORT
    // ========================================================================

    /**
     * واجهة المحرك الذكي.
     */
    private void generateSmartReport(
            Session session
    ) {

        if (txtSmartReport == null) {
            return;
        }


        if (session == null) {

            txtSmartReport.setText(
                    "لا توجد جلسة محددة."
            );

            return;
        }


        String report =
                SmartReportEngine.generate(
                        client,
                        masterSessionList,
                        filteredSessions,
                        session
                );


        txtSmartReport.setText(
                report
        );
    }


    // ========================================================================
    // SMART REPORT ENGINE
    // ========================================================================

    /**
     * محرك التحليل الذكي المحلي.
     *
     * الفكرة:
     *
     * Client
     *   ↓
     * Session History
     *   ↓
     * Data Quality
     *   ↓
     * Current Metrics
     *   ↓
     * Trend Analysis
     *   ↓
     * Nutrition Analysis
     *   ↓
     * Review Points
     *   ↓
     * Structured Professional Report
     */
    private static final class SmartReportEngine {

        private SmartReportEngine() {
        }


        public static String generate(
                Client client,
                ObservableList<Session> allSessions,
                FilteredList<Session> filteredSessions,
                Session current
        ) {

            StringBuilder report =
                    new StringBuilder();


            BodyData body =
                    current.getBodyData();


            NutritionPlan plan =
                    current.getNutritionPlan();


            // =========================================================
            // TITLE
            // =========================================================

            appendHeader(
                    report,
                    client,
                    current
            );


            // =========================================================
            // SAFETY
            // =========================================================

            appendSafety(
                    report
            );


            // =========================================================
            // CLIENT PROFILE
            // =========================================================

            appendClientProfile(
                    report,
                    client,
                    allSessions
            );


            // =========================================================
            // DATA QUALITY
            // =========================================================

            appendDataQuality(
                    report,
                    current
            );


            // =========================================================
            // CURRENT SESSION
            // =========================================================

            appendCurrentSession(
                    report,
                    client,
                    current,
                    body
            );


            // =========================================================
            // HISTORY / TREND
            // =========================================================

            appendHistoryAndTrends(
                    report,
                    filteredSessions,
                    current
            );


            // =========================================================
            // NUTRITION
            // =========================================================

            appendNutrition(
                    report,
                    client,
                    current,
                    body,
                    plan
            );


            // =========================================================
            // ATTENTION POINTS
            // =========================================================

            appendAttentionPoints(
                    report,
                    client,
                    filteredSessions,
                    current,
                    body,
                    plan
            );


            // =========================================================
            // QUESTIONS
            // =========================================================

            appendQuestions(
                    report,
                    body,
                    plan,
                    filteredSessions,
                    current
            );


            // =========================================================
            // CONCLUSION
            // =========================================================

            appendConclusion(
                    report,
                    body,
                    plan,
                    filteredSessions,
                    current
            );


            return report.toString();
        }


        // ====================================================================
        // HEADER
        // ====================================================================

        private static void appendHeader(
                StringBuilder report,
                Client client,
                Session current
        ) {

            report.append(
                    "✨ التقرير التحليلي الذكي\n"
            );


            report.append(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
            );


            report.append(
                    "العميل: "
            ).append(
                    safeClientName(client)
            ).append(
                    "\n"
            );


            report.append(
                    "الجلسة: #"
            ).append(
                    safeObject(
                            current.getId()
                    )
            ).append(
                    "\n"
            );


            report.append(
                    "التاريخ: "
            ).append(
                    formatDate(
                            current.getUploadTime()
                    )
            ).append(
                    "\n\n"
            );
        }


        // ====================================================================
        // SAFETY
        // ====================================================================

        private static void appendSafety(
                StringBuilder report
        ) {

            report.append(
                    "🛡️ نطاق التحليل\n"
            );


            report.append(
                    "هذا التقرير عبارة عن تحليل مساعد للبيانات "
                            + "المسجلة في ملف العميل. "
                            + "لا يمثل تشخيصاً طبياً، ولا يصف دواءً، "
                            + "ولا يستبدل تقييم المختص.\n"
            );


            report.append(
                    "القيم المفقودة لا يتم استنتاجها أو اختراعها، "
                            + "وأي نقطة تحتاج مراجعة يتم عرضها بشكل مستقل "
                            + "عن البيانات الرقمية.\n\n"
            );
        }


        // ====================================================================
        // CLIENT PROFILE
        // ====================================================================

        private static void appendClientProfile(
                StringBuilder report,
                Client client,
                ObservableList<Session> allSessions
        ) {

            report.append(
                    "👤 ملف العميل\n"
            );


            report.append(
                    "• الاسم: "
            ).append(
                    safeClientName(client)
            ).append(
                    "\n"
            );


            report.append(
                    "• رقم العميل: #"
            ).append(
                    safeObject(
                            client.getClientID()
                    )
            ).append(
                    "\n"
            );


            report.append(
                    "• العمر: "
            ).append(
                    client.getAge()
            ).append(
                    " سنة\n"
            );


            report.append(
                    "• الجنس: "
            ).append(
                    formatGender(
                            client.getGender()
                    )
            ).append(
                    "\n"
            );


            report.append(
                    "• الهاتف: "
            ).append(
                    safeText(
                            client.getContactNumber()
                    )
            ).append(
                    "\n"
            );


            report.append(
                    "• إجمالي الجلسات المسجلة: "
            ).append(
                    allSessions == null
                            ? 0
                            : allSessions.size()
            ).append(
                    "\n\n"
            );
        }


        // ====================================================================
        // DATA QUALITY
        // ====================================================================

        private static void appendDataQuality(
                StringBuilder report,
                Session session
        ) {

            BodyData body =
                    session.getBodyData();


            NutritionPlan plan =
                    session.getNutritionPlan();


            List<String> missing =
                    new ArrayList<>();


            if (body == null) {

                missing.add(
                        "القياسات الجسدية"
                );

            } else {

                if (body.getWeight() == null) {
                    missing.add("الوزن");
                }

                if (body.getBMI() == null) {
                    missing.add("BMI");
                }

                if (body.getBodyFatPercentage() == null) {
                    missing.add("نسبة الدهون");
                }

                if (body.getSmm() == null) {
                    missing.add("الكتلة العضلية");
                }
            }


            if (plan == null) {

                missing.add(
                        "الخطة الغذائية"
                );

            } else {

                if (plan.getTotalCalories() == null) {
                    missing.add("السعرات");
                }

                if (plan.getProteinAmount() == null) {
                    missing.add("البروتين");
                }

                if (plan.getCarbohydratesAmount() == null) {
                    missing.add("الكربوهيدرات");
                }

                if (plan.getFatAmount() == null) {
                    missing.add("الدهون");
                }
            }


            report.append(
                    "🔎 جودة البيانات\n"
            );


            if (missing.isEmpty()) {

                report.append(
                        "✓ البيانات الأساسية متوفرة لهذه الجلسة.\n\n"
                );

            } else {

                report.append(
                        "⚠ توجد بيانات غير مكتملة:\n"
                );


                for (String value :
                        missing) {

                    report.append(
                            "   • "
                    ).append(
                            value
                    ).append(
                            "\n"
                    );
                }


                report.append(
                        "لا يتم افتراض قيم بديلة لهذه العناصر.\n\n"
                );
            }
        }


        // ====================================================================
        // CURRENT SESSION
        // ====================================================================

        private static void appendCurrentSession(
                StringBuilder report,
                Client client,
                Session current,
                BodyData body
        ) {

            report.append(
                    "📊 قراءة الجلسة الحالية\n"
            );


            if (body == null) {

                report.append(
                        "لا تحتوي الجلسة الحالية على BodyData "
                                + "كافية لبناء تحليل رقمي.\n\n"
                );

                return;
            }


            if (body.getWeight() != null) {

                report.append(
                        "• الوزن: "
                ).append(
                        formatNum(
                                body.getWeight()
                        )
                ).append(
                        " كج\n"
                );
            }


            if (body.getBMI() != null) {

                double bmi =
                        body.getBMI()
                                .doubleValue();


                report.append(
                        "• BMI: "
                ).append(
                        formatNum(
                                bmi
                        )
                ).append(
                        " — "
                ).append(
                        getBMIClassification(
                                bmi
                        )
                ).append(
                        "\n"
                );
            }


            if (body.getBodyFatPercentage() != null) {

                double fat =
                        body.getBodyFatPercentage()
                                .doubleValue();


                report.append(
                        "• نسبة الدهون: "
                ).append(
                        formatNum(
                                fat
                        )
                ).append(
                        "%"
                ).append(
                        " — "
                ).append(
                        getFatClassification(
                                fat,
                                client.getGender()
                        )
                ).append(
                        "\n"
                );
            }


            if (body.getSmm() != null) {

                report.append(
                        "• الكتلة العضلية: "
                ).append(
                        formatNum(
                                body.getSmm()
                        )
                ).append(
                        " كج\n"
                );
            }


            if (body.getWeight() != null &&
                    body.getBodyFatPercentage() != null) {

                double lbm =
                        calculateLBM(
                                body.getWeight(),
                                body.getBodyFatPercentage()
                        );


                if (lbm >= 0) {

                    report.append(
                            "• الكتلة الخالية من الدهون: "
                    ).append(
                            formatNum(
                                    lbm
                            )
                    ).append(
                            " كج — تقدير حسابي\n"
                    );
                }
            }


            try {

                BigDecimal bmr =
                        body.getBMR(
                                client.getGender(),
                                client.getAge()
                        );


                if (bmr != null) {

                    report.append(
                            "• BMR: "
                    ).append(
                            formatNum(
                                    bmr
                            )
                    ).append(
                            " سعرة/يوم\n"
                    );
                }

            } catch (Exception ignored) {
            }


            try {

                BigDecimal tdee =
                        body.getTDEE(
                                client.getGender(),
                                client.getAge()
                        );


                if (tdee != null) {

                    report.append(
                            "• TDEE: "
                    ).append(
                            formatNum(
                                    tdee
                            )
                    ).append(
                            " سعرة/يوم\n"
                    );
                }

            } catch (Exception ignored) {
            }


            report.append(
                    "\n"
            );
        }


        // ====================================================================
        // TRENDS
        // ====================================================================

        private static void appendHistoryAndTrends(
                StringBuilder report,
                FilteredList<Session> sessions,
                Session current
        ) {

            report.append(
                    "📈 تحليل التطور\n"
            );


            if (sessions == null ||
                    sessions.isEmpty()) {

                report.append(
                        "لا توجد جلسات متاحة للمقارنة.\n\n"
                );

                return;
            }


            int currentIndex =
                    sessions.indexOf(
                            current
                    );


            Session first =
                    sessions.get(0);


            Session previous =
                    currentIndex > 0
                            ? sessions.get(
                            currentIndex - 1
                    )
                            : null;


            report.append(
                    "• عدد الجلسات المستخدمة في الفترة الحالية: "
            ).append(
                    sessions.size()
            ).append(
                    "\n"
            );


            if (currentIndex <= 0) {

                report.append(
                        "• الجلسة الحالية هي أول جلسة ضمن الفترة المختارة.\n"
                );

            } else {

                report.append(
                        "• الجلسة السابقة: #"
                ).append(
                        safeObject(
                                previous.getId()
                        )
                ).append(
                        " — "
                ).append(
                        formatDate(
                                previous.getUploadTime()
                        )
                ).append(
                        "\n"
                );
            }


            appendTrend(
                    report,
                    "الوزن",
                    getWeight(previous),
                    getWeight(current),
                    "كج"
            );


            appendTrend(
                    report,
                    "BMI",
                    getBMI(previous),
                    getBMI(current),
                    ""
            );


            appendTrend(
                    report,
                    "نسبة الدهون",
                    getFat(previous),
                    getFat(current),
                    "%"
            );


            appendTrend(
                    report,
                    "الكتلة العضلية",
                    getMuscle(previous),
                    getMuscle(current),
                    "كج"
            );


            /*
             * مقارنة الحالية بالأولى.
             */
            if (first != null &&
                    first != current) {

                report.append(
                        "\n"
                );


                report.append(
                        "مقارنة مع أول جلسة:\n"
                );


                appendTrend(
                        report,
                        "الوزن",
                        getWeight(first),
                        getWeight(current),
                        "كج"
                );


                appendTrend(
                        report,
                        "الدهون",
                        getFat(first),
                        getFat(current),
                        "%"
                );


                appendTrend(
                        report,
                        "العضلات",
                        getMuscle(first),
                        getMuscle(current),
                        "كج"
                );
            }


            report.append(
                    "\n"
            );


            report.append(
                    "ملاحظة: التغير الرقمي لا يمثل بمفرده حكماً "
                            + "على جودة أو سوء الحالة، ويجب تفسيره في سياقه.\n\n"
            );
        }


        // ====================================================================
        // TREND LINE
        // ====================================================================

        private static void appendTrend(
                StringBuilder report,
                String name,
                Double oldValue,
                Double newValue,
                String unit
        ) {

            if (oldValue == null ||
                    newValue == null) {

                report.append(
                        "• "
                ).append(
                        name
                ).append(
                        ": لا توجد بيانات مقارنة كافية.\n"
                );

                return;
            }


            double diff =
                    newValue - oldValue;


            double percentage =
                    Math.abs(oldValue) > 0.000001
                            ? diff / oldValue * 100
                            : 0;


            String arrow;


            if (Math.abs(diff) < 0.0001) {

                arrow = "→";

            } else if (diff > 0) {

                arrow = "↑";

            } else {

                arrow = "↓";
            }


            report.append(
                    "• "
            ).append(
                    name
            ).append(
                    ": "
            ).append(
                    formatNum(
                            oldValue
                    )
            ).append(
                    unit.isBlank()
                            ? ""
                            : " " + unit
            ).append(
                    " → "
            ).append(
                    formatNum(
                            newValue
                    )
            ).append(
                    unit.isBlank()
                            ? ""
                            : " " + unit
            ).append(
                    "  "
            ).append(
                    arrow
            ).append(
                    " "
            ).append(
                    formatNum(
                            Math.abs(diff)
                    )
            ).append(
                    unit.isBlank()
                            ? ""
                            : " " + unit
            ).append(
                    " ("
            ).append(
                    formatNum(
                            Math.abs(percentage)
                    )
            ).append(
                    "%)\n"
            );
        }


        // ====================================================================
        // NUTRITION
        // ====================================================================

        private static void appendNutrition(
                StringBuilder report,
                Client client,
                Session current,
                BodyData body,
                NutritionPlan plan
        ) {

            report.append(
                    "🥗 تحليل الخطة الغذائية\n"
            );


            if (plan == null) {

                report.append(
                        "لا توجد خطة غذائية مرتبطة بهذه الجلسة.\n\n"
                );

                return;
            }


            if (plan.getTotalCalories() != null) {

                report.append(
                        "• السعرات المستهدفة: "
                ).append(
                        formatNum(
                                plan.getTotalCalories()
                        )
                ).append(
                        " سعرة\n"
                );
            }


            if (plan.getProteinAmount() != null) {

                report.append(
                        "• البروتين: "
                ).append(
                        formatNum(
                                plan.getProteinAmount()
                        )
                ).append(
                        " جم\n"
                );
            }


            if (plan.getCarbohydratesAmount() != null) {

                report.append(
                        "• الكربوهيدرات: "
                ).append(
                        formatNum(
                                plan.getCarbohydratesAmount()
                        )
                ).append(
                        " جم\n"
                );
            }


            if (plan.getFatAmount() != null) {

                report.append(
                        "• الدهون: "
                ).append(
                        formatNum(
                                plan.getFatAmount()
                        )
                ).append(
                        " جم\n"
                );
            }


            report.append(
                    "• عدد الوجبات: "
            ).append(
                    plan.getMealsCount()
            ).append(
                    "\n"
            );


            if (plan.getWaterIntake() != null) {

                report.append(
                        "• الماء المسجل: "
                ).append(
                        safeObject(
                                plan.getWaterIntake()
                        )
                ).append(
                        "\n"
                );
            }


            /*
             * مقارنة رقمية بين سعرات الخطة وTDEE.
             */
            if (body != null &&
                    plan.getTotalCalories() != null) {

                try {

                    BigDecimal tdee =
                            body.getTDEE(
                                    client.getGender(),
                                    client.getAge()
                            );


                    if (tdee != null) {

                        double difference =
                                plan.getTotalCalories()
                                        .doubleValue()
                                        - tdee.doubleValue();


                        report.append(
                                "\n"
                        );


                        report.append(
                                "تحليل الطاقة:\n"
                        );


                        if (difference > 0) {

                            report.append(
                                    "• سعرات الخطة أعلى من TDEE "
                                            + "التقديري بمقدار "
                            ).append(
                                    formatNum(
                                            difference
                                    )
                            ).append(
                                    " سعرة تقريباً.\n"
                            );

                        } else if (difference < 0) {

                            report.append(
                                    "• سعرات الخطة أقل من TDEE "
                                            + "التقديري بمقدار "
                            ).append(
                                    formatNum(
                                            Math.abs(
                                                    difference
                                            )
                                    )
                            ).append(
                                    " سعرة تقريباً.\n"
                            );

                        } else {

                            report.append(
                                    "• سعرات الخطة قريبة حسابياً من TDEE.\n"
                            );
                        }


                        report.append(
                                "• هذه مقارنة رقمية وليست توصية "
                                        + "بتعديل الخطة تلقائياً.\n"
                        );
                    }

                } catch (Exception ignored) {
                }
            }


            if (plan.getNotes() != null &&
                    !plan.getNotes().isBlank()) {

                report.append(
                        "• ملاحظات المختص: "
                ).append(
                        plan.getNotes()
                ).append(
                        "\n"
                );
            }


            report.append(
                    "\n"
            );
        }


        // ====================================================================
        // ATTENTION POINTS
        // ====================================================================

        private static void appendAttentionPoints(
                StringBuilder report,
                Client client,
                FilteredList<Session> sessions,
                Session current,
                BodyData body,
                NutritionPlan plan
        ) {

            report.append(
                    "⚠ نقاط تستحق المراجعة\n"
            );


            List<String> points =
                    new ArrayList<>();


            // -------------------------------------------------------------
            // Missing body
            // -------------------------------------------------------------

            if (body == null) {

                points.add(
                        "القياسات الجسدية غير متوفرة للجلسة الحالية."
                );

            } else {

                if (body.getWeight() == null) {

                    points.add(
                            "الوزن غير مسجل."
                    );
                }


                if (body.getBMI() == null) {

                    points.add(
                            "BMI غير متوفر."
                    );
                }


                if (body.getBodyFatPercentage() == null) {

                    points.add(
                            "نسبة الدهون غير متوفرة."
                    );
                }


                if (body.getSmm() == null) {

                    points.add(
                            "الكتلة العضلية غير متوفرة."
                    );
                }
            }


            // -------------------------------------------------------------
            // Nutrition
            // -------------------------------------------------------------

            if (plan == null) {

                points.add(
                        "لا توجد خطة غذائية مرتبطة بالجلسة."
                );

            } else {

                if (plan.getTotalCalories() == null) {

                    points.add(
                            "السعرات المستهدفة غير مسجلة."
                    );
                }


                if (plan.getProteinAmount() == null) {

                    points.add(
                            "كمية البروتين غير مسجلة."
                    );
                }
            }


            // -------------------------------------------------------------
            // Large movement between sessions
            // -------------------------------------------------------------

            if (sessions != null &&
                    sessions.size() >= 2) {

                int index =
                        sessions.indexOf(
                                current
                        );


                if (index > 0) {

                    Session previous =
                            sessions.get(
                                    index - 1
                            );


                    Double currentWeight =
                            getWeight(current);


                    Double previousWeight =
                            getWeight(previous);


                    if (currentWeight != null &&
                            previousWeight != null) {

                        double change =
                                Math.abs(
                                        currentWeight -
                                                previousWeight
                                );


                        /*
                         * هذه ليست قاعدة طبية.
                         * مجرد Flag برمجي للتغير الملحوظ.
                         */
                        if (change >= 5.0) {

                            points.add(
                                    "يوجد تغير ملحوظ في الوزن "
                                            + "مقارنة بالجسلة السابقة؛ "
                                            + "يفضل مراجعة ظروف القياس والسياق."
                            );
                        }
                    }
                }
            }


            // -------------------------------------------------------------
            // BMI
            // -------------------------------------------------------------

            if (body != null &&
                    body.getBMI() != null) {

                double bmi =
                        body.getBMI()
                                .doubleValue();


                if (bmi < 18.5) {

                    points.add(
                            "قيمة BMI الحالية تقع تحت "
                                    + "النطاق المستخدم في النظام."
                    );

                } else if (bmi >= 30) {

                    points.add(
                            "قيمة BMI الحالية تقع في الفئة الأعلى "
                                    + "وفق قواعد التصنيف المستخدمة."
                    );
                }
            }


            // -------------------------------------------------------------
            // Result
            // -------------------------------------------------------------

            if (points.isEmpty()) {

                report.append(
                        "✓ لا توجد نقاط بيانات ناقصة أو "
                                + "Flags واضحة ضمن نطاق التحليل الحالي.\n"
                );

            } else {

                for (String point :
                        points) {

                    report.append(
                            "• "
                    ).append(
                            point
                    ).append(
                            "\n"
                    );
                }
            }


            report.append(
                    "\n"
            );
        }


        // ====================================================================
        // QUESTIONS
        // ====================================================================

        private static void appendQuestions(
                StringBuilder report,
                BodyData body,
                NutritionPlan plan,
                FilteredList<Session> sessions,
                Session current
        ) {

            report.append(
                    "💬 أسئلة مقترحة للمختص\n"
            );


            report.append(
                    "• هل ظروف القياس الحالية مماثلة للجلسات السابقة؟\n"
            );


            if (body == null) {

                report.append(
                        "• ما القياسات التي يجب استكمالها في الزيارة التالية؟\n"
                );
            }


            if (body != null &&
                    body.getBMI() != null) {

                report.append(
                        "• كيف تتوافق قراءة BMI الحالية "
                                + "مع بقية مؤشرات العميل؟\n"
                );
            }


            if (body != null &&
                    body.getBodyFatPercentage() != null) {

                report.append(
                        "• هل طريقة قياس نسبة الدهون ثابتة "
                                + "بين الجلسات؟\n"
                );
            }


            if (sessions != null &&
                    sessions.size() >= 2) {

                report.append(
                        "• ما السياق المصاحب للتغيرات الظاهرة "
                                + "بين الجلسات؟\n"
                );
            }


            if (plan == null) {

                report.append(
                        "• هل تحتاج هذه الجلسة إلى خطة غذائية مرتبطة بها؟\n"
                );

            } else {

                report.append(
                        "• هل الخطة الحالية متوافقة مع الهدف "
                                + "الذي حدده المختص؟\n"
                );


                if (plan.getNotes() == null ||
                        plan.getNotes().isBlank()) {

                    report.append(
                            "• هل توجد ملاحظات إضافية يجب تسجيلها "
                                    + "على الخطة؟\n"
                    );
                }
            }


            report.append(
                    "\n"
            );
        }


        // ====================================================================
        // CONCLUSION
        // ====================================================================

        private static void appendConclusion(
                StringBuilder report,
                BodyData body,
                NutritionPlan plan,
                FilteredList<Session> sessions,
                Session current
        ) {

            report.append(
                    "📌 الخلاصة التحليلية\n"
            );


            if (body == null) {

                report.append(
                        "الجلسة الحالية تحتوي على بيانات غير كافية "
                                + "لبناء ملخص رقمي شامل.\n"
                );

            } else {

                int availableMetrics = 0;


                if (body.getWeight() != null) {
                    availableMetrics++;
                }

                if (body.getBMI() != null) {
                    availableMetrics++;
                }

                if (body.getBodyFatPercentage() != null) {
                    availableMetrics++;
                }

                if (body.getSmm() != null) {
                    availableMetrics++;
                }


                report.append(
                        "تم العثور على "
                ).append(
                        availableMetrics
                ).append(
                        " مؤشرات جسدية أساسية متاحة "
                                + "في الجلسة الحالية.\n"
                );


                if (sessions != null &&
                        sessions.size() > 1) {

                    report.append(
                            "توجد إمكانية لمتابعة الاتجاهات "
                                    + "ومقارنة التغيرات عبر الجلسات.\n"
                    );

                } else {

                    report.append(
                            "لا توجد جلسات سابقة كافية لبناء "
                                    + "مقارنة زمنية واسعة.\n"
                    );
                }


                if (plan != null) {

                    report.append(
                            "الخطة الغذائية مرتبطة بالجلسة ويمكن "
                                    + "مقارنتها مع المؤشرات الحسابية المتاحة.\n"
                    );

                } else {

                    report.append(
                            "لا توجد خطة غذائية مرتبطة بالجلسة الحالية.\n"
                    );
                }
            }


            report.append(
                    "\n"
            );


            report.append(
                    "تم إنشاء هذا التقرير آلياً اعتماداً "
                            + "على البيانات المتاحة فقط. "
                            + "لا يتم استخدام قيم افتراضية للبيانات المفقودة.\n"
            );


            report.append(
                    "القرار النهائي وتفسير المؤشرات يعودان للمختص.\n"
            );
        }


        // ====================================================================
        // ENGINE HELPERS
        // ====================================================================

        private static String safeClientName(
                Client client
        ) {

            if (client == null) {
                return "عميل غير معروف";
            }


            String name =
                    client.getFullName();


            if (name == null ||
                    name.isBlank()) {

                return "عميل غير معروف";
            }


            return name.trim();
        }


        private static String formatGender(
                char gender
        ) {

            return switch (gender) {

                case 'M', 'm' ->
                        "ذكر";

                case 'F', 'f' ->
                        "أنثى";

                default ->
                        "غير محدد";
            };
        }


        private static String getBMIClassification(
                double bmi
        ) {

            if (bmi < 18.5) {

                return "أقل من النطاق المستخدم";

            } else if (bmi < 25) {

                return "ضمن النطاق الطبيعي المستخدم";

            } else if (bmi < 30) {

                return "ضمن فئة الوزن الزائد المستخدمة";

            } else {

                return "ضمن الفئة الأعلى المستخدمة";
            }
        }


        private static String getFatClassification(
                double fat,
                char gender
        ) {

            boolean male =
                    gender == 'M' ||
                            gender == 'm';


            if (male) {

                if (fat <= 15) {

                    return "ضمن النطاق المستخدم";

                } else if (fat <= 24) {

                    return "ضمن نطاق يحتاج متابعة";

                } else {

                    return "خارج النطاق المستخدم";
                }
            }


            if (fat <= 24) {

                return "ضمن النطاق المستخدم";

            } else if (fat <= 31) {

                return "ضمن نطاق يحتاج متابعة";

            } else {

                return "خارج النطاق المستخدم";
            }
        }


        private static Double getWeight(
                Session session
        ) {

            if (session == null ||
                    session.getBodyData() == null ||
                    session.getBodyData().getWeight() == null) {

                return null;
            }


            return session.getBodyData()
                    .getWeight()
                    .doubleValue();
        }


        private static Double getBMI(
                Session session
        ) {

            if (session == null ||
                    session.getBodyData() == null ||
                    session.getBodyData().getBMI() == null) {

                return null;
            }


            return session.getBodyData()
                    .getBMI()
                    .doubleValue();
        }


        private static Double getFat(
                Session session
        ) {

            if (session == null ||
                    session.getBodyData() == null ||
                    session.getBodyData()
                            .getBodyFatPercentage() == null) {

                return null;
            }


            return session.getBodyData()
                    .getBodyFatPercentage()
                    .doubleValue();
        }


        private static Double getMuscle(
                Session session
        ) {

            if (session == null ||
                    session.getBodyData() == null ||
                    session.getBodyData()
                            .getSmm() == null) {

                return null;
            }


            return session.getBodyData()
                    .getSmm()
                    .doubleValue();
        }


        private static double calculateLBM(
                BigDecimal weight,
                BigDecimal fat
        ) {

            if (weight == null ||
                    fat == null) {

                return -1;
            }


            double w =
                    weight.doubleValue();


            double f =
                    fat.doubleValue();


            double result =
                    w -
                            (
                                    w *
                                            f /
                                            100.0
                            );


            return result >= 0
                    ? result
                    : -1;
        }


        private static String formatNum(
                BigDecimal value
        ) {

            if (value == null) {
                return "-";
            }


            return formatNum(
                    value.doubleValue()
            );
        }


        private static String formatNum(
                double value
        ) {

            if (Double.isNaN(value) ||
                    Double.isInfinite(value)) {

                return "-";
            }


            return String.format(
                    Locale.US,
                    "%.1f",
                    value
            );
        }


        private static String formatDate(
                LocalDateTime dateTime
        ) {

            if (dateTime == null) {
                return "-";
            }


            return dateTime.format(
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm"
                    )
            );
        }


        private static String safeObject(
                Object value
        ) {

            return value == null
                    ? "-"
                    : String.valueOf(value);
        }


        private static String safeText(
                Object value
        ) {

            if (value == null) {
                return "-";
            }


            String text =
                    String.valueOf(
                            value
                    ).trim();


            return text.isEmpty()
                    ? "-"
                    : text;
        }
    }


    // ========================================================================
    // CALCULATION HELPERS
    // ========================================================================

    private BigDecimal calculateBMR(
            BodyData body,
            char gender,
            int age
    ) {

        if (body == null) {
            return null;
        }


        try {

            return body.getBMR(
                    gender,
                    age
            );

        } catch (Exception e) {

            return null;
        }
    }


    private BigDecimal calculateTDEE(
            BodyData body,
            char gender,
            int age
    ) {

        if (body == null) {
            return null;
        }


        try {

            return body.getTDEE(
                    gender,
                    age
            );

        } catch (Exception e) {

            return null;
        }
    }


    private double calculateLBM(
            BigDecimal weight,
            BigDecimal fatPercentage
    ) {

        if (weight == null ||
                fatPercentage == null) {

            return -1;
        }


        double w =
                weight.doubleValue();


        double fat =
                fatPercentage.doubleValue();


        double result =
                w -
                        (
                                w *
                                        fat /
                                        100.0
                        );


        return result >= 0
                ? result
                : -1;
    }


    private double calculateWaterEstimate(
            BigDecimal weight,
            String gender
    ) {

        if (weight == null) {
            return 0;
        }


        boolean male =
                "Male".equalsIgnoreCase(gender) ||
                        "M".equalsIgnoreCase(gender) ||
                        "ذكر".equals(gender);


        return male
                ? weight.doubleValue() * 0.04
                : weight.doubleValue() * 0.035;
    }


    // ========================================================================
    // STATUS
    // ========================================================================

    private String evaluateBMI(
            BigDecimal bmi
    ) {

        if (bmi == null) {
            return "⚪";
        }


        double value =
                bmi.doubleValue();


        if (value < 18.5) {
            return "🟡";
        }


        if (value < 25) {
            return "🟢";
        }


        if (value < 30) {
            return "🟠";
        }


        return "🔴";
    }


    private String evaluateFat(
            double fat,
            String gender
    ) {

        boolean male =
                "Male".equalsIgnoreCase(
                        gender
                ) ||
                        "M".equalsIgnoreCase(
                                gender
                        ) ||
                        "ذكر".equals(
                                gender
                        );


        if (male) {

            if (fat <= 15) {
                return "🟢";
            }

            if (fat <= 24) {
                return "🟡";
            }

            return "🔴";
        }


        if (fat <= 24) {
            return "🟢";
        }


        if (fat <= 31) {
            return "🟡";
        }


        return "🔴";
    }


    private void updateHealthStatus(
            BodyData body
    ) {

        if (lblHealthStatus == null) {
            return;
        }


        if (body == null ||
                body.getBMI() == null) {

            setHealthStatus(
                    "لا توجد بيانات كافية",
                    "#cbd5e1"
            );

            return;
        }


        String state =
                evaluateBMI(
                        body.getBMI()
                );


        String text =
                switch (state) {

                    case "🟢" ->
                            "ضمن النطاق المستخدم";

                    case "🟡" ->
                            "يحتاج متابعة";

                    case "🟠" ->
                            "يحتاج مراجعة";

                    case "🔴" ->
                            "يحتاج اهتماماً";

                    default ->
                            "غير متوفر";
                };


        setHealthStatus(
                text,
                getColorText(
                        state
                )
        );
    }


    private void setHealthStatus(
            String text,
            String color
    ) {

        lblHealthStatus.setText(
                "الحالة: " +
                        text
        );


        try {

            lblHealthStatus.setTextFill(
                    Color.web(
                            color
                    )
            );

        } catch (Exception ignored) {
        }
    }


    // ========================================================================
    // COLORS
    // ========================================================================

    private String getColorBackground(
            String state
    ) {

        return switch (state) {

            case "🟢" ->
                    "#ecfdf5";

            case "🟡" ->
                    "#fffbeb";

            case "🟠" ->
                    "#fff7ed";

            case "🔴" ->
                    "#fef2f2";

            case "🔵" ->
                    "#eff6ff";

            default ->
                    "#f8fafc";
        };
    }


    private String getColorText(
            String state
    ) {

        return switch (state) {

            case "🟢" ->
                    "#15803d";

            case "🟡" ->
                    "#a16207";

            case "🟠" ->
                    "#c2410c";

            case "🔴" ->
                    "#b91c1c";

            case "🔵" ->
                    "#1d4ed8";

            default ->
                    "#64748b";
        };
    }


    private String getStateText(
            String state
    ) {

        return switch (state) {

            case "🟢" ->
                    "ضمن النطاق المستخدم";

            case "🟡" ->
                    "يحتاج متابعة";

            case "🟠" ->
                    "يحتاج مراجعة";

            case "🔴" ->
                    "يحتاج اهتماماً";

            case "🔵" ->
                    "مؤشر معلوماتي";

            default ->
                    "غير متوفر";
        };
    }


    // ========================================================================
    // VALUE HELPERS
    // ========================================================================

    private Double getWeight(
            Session session
    ) {

        if (session == null ||
                session.getBodyData() == null ||
                session.getBodyData()
                        .getWeight() == null) {

            return null;
        }


        return session.getBodyData()
                .getWeight()
                .doubleValue();
    }


    private Double getBMI(
            Session session
    ) {

        if (session == null ||
                session.getBodyData() == null ||
                session.getBodyData()
                        .getBMI() == null) {

            return null;
        }


        return session.getBodyData()
                .getBMI()
                .doubleValue();
    }


    private Double getFat(
            Session session
    ) {

        if (session == null ||
                session.getBodyData() == null ||
                session.getBodyData()
                        .getBodyFatPercentage() == null) {

            return null;
        }


        return session.getBodyData()
                .getBodyFatPercentage()
                .doubleValue();
    }


    private Double getMuscle(
            Session session
    ) {

        if (session == null ||
                session.getBodyData() == null ||
                session.getBodyData()
                        .getSmm() == null) {

            return null;
        }


        return session.getBodyData()
                .getSmm()
                .doubleValue();
    }


    private Double getLBM(
            Session session
    ) {

        Double weight =
                getWeight(session);


        Double fat =
                getFat(session);


        if (weight == null ||
                fat == null) {

            return null;
        }


        double result =
                weight -
                        (
                                weight *
                                        fat /
                                        100.0
                        );


        return result >= 0
                ? result
                : null;
    }


    private Double getBMR(
            Session session
    ) {

        if (session == null ||
                session.getBodyData() == null ||
                client == null) {

            return null;
        }


        BigDecimal value =
                calculateBMR(
                        session.getBodyData(),
                        client.getGender(),
                        client.getAge()
                );


        return value == null
                ? null
                : value.doubleValue();
    }


    private Double getTDEE(
            Session session
    ) {

        if (session == null ||
                session.getBodyData() == null ||
                client == null) {

            return null;
        }


        BigDecimal value =
                calculateTDEE(
                        session.getBodyData(),
                        client.getGender(),
                        client.getAge()
                );


        return value == null
                ? null
                : value.doubleValue();
    }


    private Double getPlanCalories(
            Session session
    ) {

        if (session == null ||
                session.getNutritionPlan() == null ||
                session.getNutritionPlan()
                        .getTotalCalories() == null) {

            return null;
        }


        return session.getNutritionPlan()
                .getTotalCalories()
                .doubleValue();
    }


    private Double getPlanProtein(
            Session session
    ) {

        if (session == null ||
                session.getNutritionPlan() == null ||
                session.getNutritionPlan()
                        .getProteinAmount() == null) {

            return null;
        }


        return session.getNutritionPlan()
                .getProteinAmount()
                .doubleValue();
    }


    private Double getPlanCarbs(
            Session session
    ) {

        if (session == null ||
                session.getNutritionPlan() == null ||
                session.getNutritionPlan()
                        .getCarbohydratesAmount() == null) {

            return null;
        }


        return session.getNutritionPlan()
                .getCarbohydratesAmount()
                .doubleValue();
    }


    private Double getPlanFat(
            Session session
    ) {

        if (session == null ||
                session.getNutritionPlan() == null ||
                session.getNutritionPlan()
                        .getFatAmount() == null) {

            return null;
        }


        return session.getNutritionPlan()
                .getFatAmount()
                .doubleValue();
    }


    // ========================================================================
    // FORMATTING
    // ========================================================================

    private String formatNullable(
            Double value,
            String unit
    ) {

        if (value == null) {
            return "—";
        }


        return formatNum(
                value
        ) +
                (
                        unit == null ||
                                unit.isBlank()
                                ? ""
                                : " " + unit
                );
    }


    private String formatNum(
            double value
    ) {

        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {

            return "-";
        }


        return String.format(
                Locale.US,
                "%.1f",
                value
        );
    }


    private String formatNum(
            BigDecimal value
    ) {

        if (value == null) {
            return "-";
        }


        return formatNum(
                value.doubleValue()
        );
    }


    private String formatDateOnly(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {
            return "غير محدد";
        }


        return dateTime.format(
                dateFormatter
        );
    }


    private String formatDateTime(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {
            return "-";
        }


        return dateTime.format(
                dateFormatter
        );
    }


    private String safeText(
            Object value
    ) {

        if (value == null) {
            return "-";
        }


        String text =
                String.valueOf(value)
                        .trim();


        return text.isEmpty()
                ? "-"
                : text;
    }


    private String safeObject(
            Object value
    ) {

        return value == null
                ? "-"
                : String.valueOf(value);
    }


    private String buildNoSessionMessage() {

        return """
                لا توجد جلسات مسجلة لهذا العميل.

                عند إنشاء جلسة جديدة ستظهر هنا:
                • المؤشرات الجسدية
                • الخطة الغذائية
                • التطور عبر الزمن
                • المقارنة
                • التحليل الذكي

                لا يقوم النظام بافتراض بيانات غير مسجلة.
                """;
    }


    // ========================================================================
    // EXAMINATIONS
    // ========================================================================

    private void configureExaminationsTable() {

        if (tvExaminations == null) {
            return;
        }


        tvExaminations.setPlaceholder(
                new Label(
                        "لا توجد فحوصات طبية مسجلة أو مرفقة بهذه الجلسة."
                )
        );


        /*
         * لم يتم ربط CellValueFactory لأن كلاس الفحص
         * غير موجود ضمن الكود المرسل.
         *
         * لا نقوم بافتراض أسماء getters غير موجودة.
         */
    }


    // ========================================================================
    // KEYBOARD SHORTCUTS
    // ========================================================================

    private void installKeyboardShortcuts() {

        if (keyboardShortcutsInstalled ||
                btnExportPDF == null) {

            return;
        }


        btnExportPDF
                .sceneProperty()
                .addListener(
                        (observable,
                         oldScene,
                         newScene) -> {

                            if (newScene != null) {

                                registerShortcuts(
                                        newScene
                                );
                            }
                        }
                );


        if (btnExportPDF.getScene() != null) {

            registerShortcuts(
                    btnExportPDF.getScene()
            );
        }


        keyboardShortcutsInstalled = true;
    }


    private void registerShortcuts(
            Scene scene
    ) {

        if (scene == null) {
            return;
        }


        TabPane tabPane =
                findTabPane(
                        scene.getRoot()
                );


        if (tabPane == null) {
            return;
        }


        registerTabShortcut(
                scene,
                KeyCode.DIGIT1,
                tabPane,
                0
        );


        registerTabShortcut(
                scene,
                KeyCode.DIGIT2,
                tabPane,
                1
        );


        registerTabShortcut(
                scene,
                KeyCode.DIGIT3,
                tabPane,
                2
        );


        registerTabShortcut(
                scene,
                KeyCode.DIGIT4,
                tabPane,
                3
        );


        registerTabShortcut(
                scene,
                KeyCode.DIGIT5,
                tabPane,
                4
        );


        scene.getAccelerators()
                .put(
                        new KeyCodeCombination(
                                KeyCode.P,
                                KeyCombination.CONTROL_DOWN
                        ),
                        this::exportToPDF
                );
    }


    private void registerTabShortcut(
            Scene scene,
            KeyCode keyCode,
            TabPane tabPane,
            int index
    ) {

        scene.getAccelerators()
                .put(
                        new KeyCodeCombination(
                                keyCode,
                                KeyCombination.CONTROL_DOWN
                        ),
                        () -> {

                            if (index <
                                    tabPane.getTabs().size()) {

                                tabPane
                                        .getSelectionModel()
                                        .select(index);
                            }
                        }
                );
    }


    private TabPane findTabPane(
            Parent root
    ) {

        if (root instanceof TabPane) {

            return (TabPane) root;
        }


        for (Node node :
                root.getChildrenUnmodifiable()) {

            if (node instanceof Parent parent) {

                TabPane result =
                        findTabPane(
                                parent
                        );


                if (result != null) {
                    return result;
                }
            }
        }


        return null;
    }


    // ========================================================================
    // CLEAR
    // ========================================================================

    private void clearClientSessionViews() {

        if (cardsContainer != null) {

            cardsContainer
                    .getChildren()
                    .clear();
        }


        if (measurementsGrid != null) {

            measurementsGrid
                    .getChildren()
                    .clear();
        }


        if (nutritionGrid != null) {

            nutritionGrid
                    .getChildren()
                    .clear();
        }


        if (comparisonGrid != null) {

            comparisonGrid
                    .getChildren()
                    .clear();
        }


        if (mainChart != null) {

            mainChart
                    .getData()
                    .clear();
        }


        if (chartTogglesContainer != null) {

            chartTogglesContainer
                    .getChildren()
                    .clear();
        }
    }


    // ========================================================================
    // PDF EXPORT
    // ========================================================================

    @FXML
    void exportToPDF() {

        if (client == null) {

            showAlert(
                    "خطأ",
                    "بيانات العميل غير متوفرة.",
                    Alert.AlertType.ERROR
            );

            return;
        }


        Session currentSession =
                lvSessions
                        .getSelectionModel()
                        .getSelectedItem();


        if (currentSession == null) {

            showAlert(
                    "اختيار الجلسة",
                    "يرجى اختيار جلسة من القائمة أولاً.",
                    Alert.AlertType.WARNING
            );

            return;
        }


        if (btnExportPDF != null) {

            btnExportPDF.setDisable(
                    true
            );


            btnExportPDF.setText(
                    "جاري التحضير..."
            );
        }


        final Parent[] rootHolder =
                new Parent[1];


        final SessionReportController[] controllerHolder =
                new SessionReportController[1];


        BackgroundRunner.run(

                "جاري تحميل واجهة التقرير...",

                () -> {

                    try {

                        FXMLLoader loader =
                                new FXMLLoader(
                                        getClass()
                                                .getResource(
                                                        "/fxml/SessionReport.fxml"
                                                )
                                );


                        rootHolder[0] =
                                loader.load();


                        controllerHolder[0] =
                                loader.getController();


                        controllerHolder[0]
                                .setSessionContext(
                                        client,
                                        currentSession
                                );


                        return true;

                    } catch (Exception e) {

                        Platform.runLater(
                                () ->
                                        showAlert(
                                                "خطأ في التقرير",
                                                "تعذر تحميل واجهة التقرير:\n" +
                                                        e.getMessage(),
                                                Alert.AlertType.ERROR
                                        )
                        );


                        return false;
                    }
                },


                () -> {

                    try {

                        if (rootHolder[0] == null) {

                            resetExportButton();

                            return;
                        }


                        Stage stage =
                                new Stage();


                        stage.setTitle(
                                "مراجعة التقرير - " +
                                        getClientName()
                        );


                        Scene scene =
                                new Scene(
                                        rootHolder[0]
                                );


                        scene.setNodeOrientation(
                                NodeOrientation.RIGHT_TO_LEFT
                        );


                        stage.setScene(
                                scene
                        );


                        stage.initModality(
                                Modality.APPLICATION_MODAL
                        );


                        stage.setMinWidth(
                                900
                        );


                        stage.setMinHeight(
                                650
                        );


                        stage.setOnHidden(
                                event ->
                                        resetExportButton()
                        );


                        stage.show();

                    } catch (Exception e) {

                        showAlert(
                                "خطأ",
                                "حدث خطأ أثناء فتح التقرير:\n" +
                                        e.getMessage(),
                                Alert.AlertType.ERROR
                        );


                        resetExportButton();
                    }
                }
        );
    }


    private void resetExportButton() {

        Platform.runLater(
                () -> {

                    if (btnExportPDF == null) {
                        return;
                    }


                    btnExportPDF.setDisable(
                            false
                    );


                    btnExportPDF.setText(
                            "📄  تصدير التقرير"
                    );
                }
        );
    }


    // ========================================================================
    // ALERT
    // ========================================================================

    private void showAlert(
            String title,
            String content,
            Alert.AlertType type
    ) {

        Runnable action =
                () -> {

                    Alert alert =
                            new Alert(
                                    type
                            );


                    alert.setTitle(
                            title
                    );


                    alert.setHeaderText(
                            null
                    );


                    alert.setContentText(
                            content
                    );


                    DialogPane pane =
                            alert.getDialogPane();


                    pane.setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );


                    pane.setStyle(
                            "-fx-font-family:'System';" +
                                    "-fx-font-size:13px;"
                    );


                    alert.showAndWait();
                };


        if (Platform.isFxApplicationThread()) {

            action.run();

        } else {

            Platform.runLater(
                    action
            );
        }
    }


    // ========================================================================
    // CLOSE
    // ========================================================================

    @FXML
    void handleClose() {

        if (btnExportPDF == null ||
                btnExportPDF.getScene() == null) {

            return;
        }


        Stage stage =
                (Stage)
                        btnExportPDF
                                .getScene()
                                .getWindow();


        stage.close();
    }
}