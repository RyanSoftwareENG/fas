package user_interface.offer;

import api.ReportsAPI;
import dto.FullDashboardResponse;
import dto.FullDashboardResponse.*;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;

import javafx.scene.Node;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.util.StringConverter;

import java.text.DecimalFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * =========================================================
 * FAS - Reports Dashboard
 * =========================================================
 *
 * واجهة لوحة التقارير الرئيسية للنظام.
 *
 * التصميم يعتمد على:
 *
 * 1. KPI Cards
 * 2. Sessions Bar Chart
 * 3. Revenue Line Chart
 * 4. Nutrition Plans Pie Chart
 * 5. Recent Sessions Table
 * 6. Alerts / Monitoring
 *
 * تم تصميم الواجهة للعمل بشكل جيد مع:
 *
 * 7 أيام
 * 30 يومًا
 * 90 يومًا
 * 180 يومًا
 * سنة
 *
 * مع تجميع البيانات تلقائيًا عند الفترات الطويلة.
 *
 * =========================================================
 */
public class ReportsDashboardView
        extends BorderPane {

    // =====================================================
    // API
    // =====================================================

    private final ReportsAPI reportsAPI =
            new ReportsAPI();

    // =====================================================
    // Colors
    // =====================================================

    private static final String BG =
            "#F4F7FB";

    private static final String SURFACE =
            "#FFFFFF";

    private static final String BORDER =
            "#E6EAF0";

    private static final String TEXT_PRIMARY =
            "#172033";

    private static final String TEXT_SECONDARY =
            "#667085";

    private static final String TEXT_MUTED =
            "#98A2B3";

    private static final String PRIMARY =
            "#2563EB";

    private static final String PRIMARY_LIGHT =
            "#EFF6FF";

    private static final String SUCCESS =
            "#059669";

    private static final String SUCCESS_LIGHT =
            "#ECFDF5";

    private static final String WARNING =
            "#D97706";

    private static final String WARNING_LIGHT =
            "#FFFBEB";

    private static final String DANGER =
            "#DC2626";

    private static final String DANGER_LIGHT =
            "#FEF2F2";

    private static final String PURPLE =
            "#7C3AED";

    private static final String PURPLE_LIGHT =
            "#F5F3FF";

    private static final String TEAL =
            "#0F766E";

    private static final String TEAL_LIGHT =
            "#F0FDFA";

    // =====================================================
    // State
    // =====================================================

    private volatile boolean loading =
            false;

    // =====================================================
    // Header
    // =====================================================

    private final ComboBox<String> periodCombo =
            new ComboBox<>();

    private final Button refreshButton =
            new Button("⟳  تحديث");

    // =====================================================
    // KPI Values
    // =====================================================

    private final Label totalClientsValue =
            new Label("—");

    private final Label newClientsValue =
            new Label("—");

    private final Label sessionsValue =
            new Label("—");

    private final Label activePlansValue =
            new Label("—");

    private final Label revenueValue =
            new Label("—");

    private final Label averageDurationValue =
            new Label("—");

    // =====================================================
    // KPI Growth
    // =====================================================

    private final Label clientsGrowth =
            new Label("—");

    private final Label sessionsGrowth =
            new Label("—");

    private final Label plansGrowth =
            new Label("—");

    // =====================================================
    // Charts
    // =====================================================

    private BarChart<String, Number>
            sessionsChart;

    private LineChart<String, Number>
            revenueChart;

    private final PieChart plansChart =
            new PieChart();

    // =====================================================
    // Chart Holders
    // =====================================================

    private final StackPane
            sessionsChartHolder =
            new StackPane();

    private final StackPane
            revenueChartHolder =
            new StackPane();

    private final StackPane
            plansChartHolder =
            new StackPane();

    private final Label
            sessionsEmptyLabel =
            createChartEmptyLabel();

    private final Label
            revenueEmptyLabel =
            createChartEmptyLabel();

    private final Label
            plansEmptyLabel =
            createChartEmptyLabel();

    // =====================================================
    // Recent Sessions
    // =====================================================

    private final TableView<RecentSession>
            recentSessionsTable =
            new TableView<>();

    // =====================================================
    // Alerts
    // =====================================================

    private final VBox alertsContainer =
            new VBox(10);

    // =====================================================
    // Main Content
    // =====================================================

    private final StackPane contentContainer =
            new StackPane();

    private final VBox dashboardContent =
            new VBox(22);

    // =====================================================
    // Loading Overlay
    // =====================================================

    private final StackPane loadingOverlay =
            new StackPane();

    private final ProgressIndicator progress =
            new ProgressIndicator();

    // =====================================================
    // Formatting
    // =====================================================

    private final DecimalFormat numberFormat =
            new DecimalFormat("#,##0.##");

    private final DecimalFormat integerFormat =
            new DecimalFormat("#,##0");

    private final DateTimeFormatter
            dailyFormatter =
            DateTimeFormatter.ofPattern("dd/MM");

    private final DateTimeFormatter
            monthlyFormatter =
            DateTimeFormatter.ofPattern("MM/yyyy");

    // =====================================================
    // Constructor
    // =====================================================

    public ReportsDashboardView() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setStyle(
                "-fx-background-color: " + BG + ";"
        );

        createCharts();

        createHeader();

        createContent();

        loadDashboard();
    }

    // =====================================================
    // Charts Initialization
    // =====================================================

    private void createCharts() {

        // =================================================
        // Sessions Bar Chart
        // =================================================

        CategoryAxis sessionsXAxis =
                new CategoryAxis();

        NumberAxis sessionsYAxis =
                new NumberAxis();

        sessionsXAxis.setLabel(
                "الفترة"
        );

        sessionsYAxis.setLabel(
                "عدد الجلسات"
        );

        sessionsYAxis.setForceZeroInRange(
                true
        );

        sessionsYAxis.setAutoRanging(
                true
        );

        sessionsChart =
                new BarChart<>(
                        sessionsXAxis,
                        sessionsYAxis
                );

        sessionsChart.setLegendVisible(
                false
        );

        sessionsChart.setAnimated(
                false
        );

        sessionsChart.setAlternativeColumnFillVisible(
                false
        );

        sessionsChart.setAlternativeRowFillVisible(
                false
        );

        sessionsChart.setHorizontalGridLinesVisible(
                true
        );

        sessionsChart.setVerticalGridLinesVisible(
                false
        );

        sessionsChart.setBarGap(
                3
        );

        sessionsChart.setCategoryGap(
                10
        );

        sessionsChart.setHorizontalZeroLineVisible(
                true
        );

        sessionsXAxis.setTickLabelRotation(
                35
        );

        // =================================================
        // Revenue Line Chart
        // =================================================

        CategoryAxis revenueXAxis =
                new CategoryAxis();

        NumberAxis revenueYAxis =
                new NumberAxis();

        revenueXAxis.setLabel(
                "الفترة"
        );

        revenueYAxis.setLabel(
                "الإيرادات"
        );

        revenueYAxis.setForceZeroInRange(
                true
        );

        revenueChart =
                new LineChart<>(
                        revenueXAxis,
                        revenueYAxis
                );

        revenueChart.setLegendVisible(
                false
        );

        revenueChart.setAnimated(
                false
        );

        revenueChart.setCreateSymbols(
                true
        );

        revenueChart.setHorizontalGridLinesVisible(
                true
        );

        revenueChart.setVerticalGridLinesVisible(
                false
        );

        revenueChart.setHorizontalZeroLineVisible(
                true
        );

        revenueXAxis.setTickLabelRotation(
                35
        );

        // =================================================
        // Plans Pie Chart
        // =================================================

        plansChart.setLegendVisible(
                true
        );

        plansChart.setLabelsVisible(
                true
        );

        plansChart.setAnimated(
                false
        );

        plansChart.setStartAngle(
                90
        );

        plansChart.setClockwise(
                false
        );

        // =================================================
        // Chart Holders
        // =================================================

        sessionsChartHolder
                .getChildren()
                .setAll(
                        sessionsChart,
                        sessionsEmptyLabel
                );

        revenueChartHolder
                .getChildren()
                .setAll(
                        revenueChart,
                        revenueEmptyLabel
                );

        plansChartHolder
                .getChildren()
                .setAll(
                        plansChart,
                        plansEmptyLabel
                );

        sessionsChartHolder.setMinHeight(
                350
        );

        revenueChartHolder.setMinHeight(
                350
        );

        plansChartHolder.setMinHeight(
                350
        );

        StackPane.setAlignment(
                sessionsEmptyLabel,
                Pos.CENTER
        );

        StackPane.setAlignment(
                revenueEmptyLabel,
                Pos.CENTER
        );

        StackPane.setAlignment(
                plansEmptyLabel,
                Pos.CENTER
        );
    }

    // =====================================================
    // Header
    // =====================================================

    private void createHeader() {

        Label title =
                new Label(
                        "لوحة التقارير والمراقبة"
                );

        title.setStyle("""
            -fx-font-size: 30px;
            -fx-font-weight: 800;
            -fx-text-fill: #172033;
        """);

        Label subtitle =
                new Label(
                        "نظرة شاملة على أداء العيادة، الجلسات، الإيرادات، والخطط الغذائية"
                );

        subtitle.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #667085;
        """);

        VBox titleBox =
                new VBox(
                        6,
                        title,
                        subtitle
                );

        titleBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        // =================================================
        // Period
        // =================================================

        periodCombo
                .getItems()
                .setAll(
                        "آخر 7 أيام",
                        "آخر 30 يومًا",
                        "آخر 90 يومًا",
                        "آخر 180 يومًا",
                        "آخر سنة"
                );

        periodCombo.setValue(
                "آخر 30 يومًا"
        );

        periodCombo.setPrefWidth(
                155
        );

        periodCombo.setMinHeight(
                42
        );

        periodCombo.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 11px;
            -fx-border-color: #D9E1EA;
            -fx-border-radius: 11px;
            -fx-border-width: 1px;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #344054;
        """);

        periodCombo.setTooltip(
                new Tooltip(
                        "حدد الفترة الزمنية للتقارير"
                )
        );

        periodCombo.setOnAction(
                event -> loadDashboard()
        );

        // =================================================
        // Refresh Button
        // =================================================

        refreshButton.setMinHeight(
                42
        );

        refreshButton.setPrefHeight(
                42
        );

        refreshButton.setPadding(
                new Insets(
                        0,
                        18,
                        0,
                        18
                )
        );

        refreshButton.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 11px;
            -fx-border-color: #D9E1EA;
            -fx-border-radius: 11px;
            -fx-border-width: 1px;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #344054;
            -fx-cursor: hand;
        """);

        refreshButton.setTooltip(
                new Tooltip(
                        "تحديث بيانات لوحة التقارير"
                )
        );

        refreshButton.setOnMouseEntered(
                event ->
                        refreshButton.setStyle("""
                            -fx-background-color: #F8FAFC;
                            -fx-background-radius: 11px;
                            -fx-border-color: #C7D2E0;
                            -fx-border-radius: 11px;
                            -fx-border-width: 1px;
                            -fx-font-size: 13px;
                            -fx-font-weight: bold;
                            -fx-text-fill: #1D4ED8;
                            -fx-cursor: hand;
                        """)
        );

        refreshButton.setOnMouseExited(
                event ->
                        refreshButton.setStyle("""
                            -fx-background-color: #FFFFFF;
                            -fx-background-radius: 11px;
                            -fx-border-color: #D9E1EA;
                            -fx-border-radius: 11px;
                            -fx-border-width: 1px;
                            -fx-font-size: 13px;
                            -fx-font-weight: bold;
                            -fx-text-fill: #344054;
                            -fx-cursor: hand;
                        """)
        );

        refreshButton.setOnAction(
                event -> loadDashboard()
        );

        HBox actions =
                new HBox(
                        10,
                        periodCombo,
                        refreshButton
                );

        actions.setAlignment(
                Pos.CENTER_LEFT
        );

        // =================================================
        // Header
        // =================================================

        BorderPane header =
                new BorderPane();

        header.setRight(
                titleBox
        );

        header.setLeft(
                actions
        );

        header.setPadding(
                new Insets(
                        26,
                        34,
                        18,
                        34
                )
        );

        setTop(
                header
        );
    }

    // =====================================================
    // Content
    // =====================================================

    private void createContent() {

        dashboardContent.setPadding(
                new Insets(
                        4,
                        34,
                        34,
                        34
                )
        );

        dashboardContent
                .getChildren()
                .addAll(
                        createKpiSection(),
                        createChartsSection(),
                        createBottomSection()
                );

        contentContainer
                .getChildren()
                .add(
                        dashboardContent
                );

        createLoadingOverlay();

        StackPane root =
                new StackPane(
                        contentContainer,
                        loadingOverlay
                );

        ScrollPane scrollPane =
                new ScrollPane(
                        root
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setFitToHeight(
                false
        );

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setPannable(
                true
        );

        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
        """);

        setCenter(
                scrollPane
        );
    }

    // =====================================================
    // Loading Overlay
    // =====================================================

    private void createLoadingOverlay() {

        loadingOverlay.setVisible(
                false
        );

        loadingOverlay.setPickOnBounds(
                true
        );

        loadingOverlay.setStyle("""
            -fx-background-color: rgba(244,247,251,0.88);
        """);

        VBox loadingBox =
                new VBox(
                        12
                );

        loadingBox.setAlignment(
                Pos.CENTER
        );

        loadingBox.setPadding(
                new Insets(22)
        );

        loadingBox.setMaxWidth(
                250
        );

        loadingBox.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
            -fx-border-color: #E3E8EF;
            -fx-border-radius: 16px;
        """);

        progress.setMaxSize(
                42,
                42
        );

        progress.setStyle(
                "-fx-progress-color: " + PRIMARY + ";"
        );

        Label loadingLabel =
                new Label(
                        "جارٍ تحديث التقارير..."
                );

        loadingLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #344054;
        """);

        Label loadingSubLabel =
                new Label(
                        "يتم تحميل أحدث البيانات"
                );

        loadingSubLabel.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #98A2B3;
        """);

        loadingBox
                .getChildren()
                .addAll(
                        progress,
                        loadingLabel,
                        loadingSubLabel
                );

        loadingOverlay
                .getChildren()
                .add(
                        loadingBox
                );

        StackPane.setAlignment(
                loadingBox,
                Pos.CENTER
        );
    }

    // =====================================================
    // KPI Section
    // =====================================================

    private VBox createKpiSection() {

        VBox section =
                new VBox(
                        13
                );

        Label title =
                sectionTitle(
                        "نظرة عامة"
                );

        Label subtitle =
                sectionSubtitle(
                        "أهم المؤشرات الحالية للعيادة"
                );

        VBox heading =
                new VBox(
                        3,
                        title,
                        subtitle
                );

        GridPane grid =
                new GridPane();

        grid.setHgap(
                14
        );

        grid.setVgap(
                14
        );

        for (int i = 0; i < 6; i++) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(
                    16.6666667
            );

            column.setHgrow(
                    Priority.ALWAYS
            );

            column.setFillWidth(
                    true
            );

            grid.getColumnConstraints()
                    .add(
                            column
                    );
        }

        grid.add(
                createKpiCard(
                        "إجمالي المرضى",
                        totalClientsValue,
                        clientsGrowth,
                        "👥",
                        PRIMARY,
                        PRIMARY_LIGHT,
                        "جميع المرضى المسجلين"
                ),
                0,
                0
        );

        grid.add(
                createKpiCard(
                        "المرضى الجدد",
                        newClientsValue,
                        new Label("خلال الفترة المحددة"),
                        "+",
                        SUCCESS,
                        SUCCESS_LIGHT,
                        "مرضى تمت إضافتهم حديثًا"
                ),
                1,
                0
        );

        grid.add(
                createKpiCard(
                        "الجلسات",
                        sessionsValue,
                        sessionsGrowth,
                        "◷",
                        TEAL,
                        TEAL_LIGHT,
                        "إجمالي الجلسات خلال الفترة"
                ),
                2,
                0
        );

        grid.add(
                createKpiCard(
                        "الخطط النشطة",
                        activePlansValue,
                        plansGrowth,
                        "≡",
                        PURPLE,
                        PURPLE_LIGHT,
                        "خطط غذائية نشطة حاليًا"
                ),
                3,
                0
        );

        grid.add(
                createKpiCard(
                        "الإيرادات",
                        revenueValue,
                        new Label("خلال الفترة المحددة"),
                        "ر.ي",
                        WARNING,
                        WARNING_LIGHT,
                        "إجمالي إيرادات الجلسات"
                ),
                4,
                0
        );

        grid.add(
                createKpiCard(
                        "متوسط الجلسة",
                        averageDurationValue,
                        new Label("متوسط المدة"),
                        "◴",
                        PRIMARY,
                        PRIMARY_LIGHT,
                        "متوسط مدة الجلسات بالدقائق"
                ),
                5,
                0
        );

        section.getChildren().addAll(
                heading,
                grid
        );

        return section;
    }

    // =====================================================
    // KPI Card
    // =====================================================

    private VBox createKpiCard(
            String title,
            Label value,
            Label footer,
            String icon,
            String accent,
            String accentLight,
            String tooltipText
    ) {

        VBox card =
                new VBox(
                        12
                );

        card.setPadding(
                new Insets(
                        18
                )
        );

        card.setMinHeight(
                158
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
            -fx-border-color: #E5EAF1;
            -fx-border-radius: 16px;
            -fx-border-width: 1px;
        """);

        // =================================================
        // Accent line
        // =================================================

        Region accentLine =
                new Region();

        accentLine.setPrefHeight(
                3
        );

        accentLine.setMaxWidth(
                Double.MAX_VALUE
        );

        accentLine.setStyle(
                "-fx-background-color: "
                        + accent
                        + ";"
                        + "-fx-background-radius: 3px;"
        );

        // =================================================
        // Header
        // =================================================

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #667085;
        """);

        titleLabel.setTooltip(
                new Tooltip(
                        tooltipText
                )
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label iconLabel =
                new Label(
                        icon
                );

        iconLabel.setMinSize(
                44,
                44
        );

        iconLabel.setPrefSize(
                44,
                44
        );

        iconLabel.setMaxSize(
                44,
                44
        );

        iconLabel.setAlignment(
                Pos.CENTER
        );

        iconLabel.setStyle(
                "-fx-background-color: "
                        + accentLight
                        + ";"
                        + "-fx-background-radius: 13px;"
                        + "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + accent
                        + ";"
        );

        header.getChildren().addAll(
                titleLabel,
                spacer,
                iconLabel
        );

        // =================================================
        // Value
        // =================================================

        value.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: 800;
            -fx-text-fill: #101828;
        """);

        value.setMaxWidth(
                Double.MAX_VALUE
        );

        // =================================================
        // Footer
        // =================================================

        footer.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #98A2B3;
        """);

        footer.setWrapText(
                true
        );

        // =================================================
        // Card
        // =================================================

        card.getChildren().addAll(
                accentLine,
                header,
                value,
                footer
        );

        return card;
    }

    // =====================================================
    // Charts Section
    // =====================================================

    private VBox createChartsSection() {

        VBox section =
                new VBox(
                        14
                );

        VBox sessionsPanel =
                createChartPanel(
                        "الجلسات",
                        "توزيع الجلسات خلال الفترة المحددة",
                        sessionsChartHolder
                );

        VBox revenuePanel =
                createChartPanel(
                        "الإيرادات",
                        "تطور الإيرادات خلال الفترة المحددة",
                        revenueChartHolder
                );

        HBox topRow =
                new HBox(
                        14,
                        sessionsPanel,
                        revenuePanel
                );

        HBox.setHgrow(
                sessionsPanel,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                revenuePanel,
                Priority.ALWAYS
        );

        VBox plansPanel =
                createChartPanel(
                        "حالة الخطط الغذائية",
                        "التوزيع الحالي للخطط حسب الحالة",
                        plansChartHolder
                );

        section.getChildren().addAll(
                topRow,
                plansPanel
        );

        return section;
    }

    // =====================================================
    // Chart Panel
    // =====================================================

    private VBox createChartPanel(
            String title,
            String subtitle,
            Node chart
    ) {

        VBox panel =
                new VBox(
                        10
                );

        panel.setPadding(
                new Insets(
                        20
                )
        );

        panel.setMinHeight(
                425
        );

        panel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
            -fx-border-color: #E5EAF1;
            -fx-border-radius: 16px;
            -fx-border-width: 1px;
        """);

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle("""
            -fx-font-size: 17px;
            -fx-font-weight: 800;
            -fx-text-fill: #172033;
        """);

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #98A2B3;
        """);

        VBox header =
                new VBox(
                        3,
                        titleLabel,
                        subtitleLabel
                );

        panel.getChildren().addAll(
                header,
                chart
        );

        VBox.setVgrow(
                chart,
                Priority.ALWAYS
        );

        return panel;
    }

    // =====================================================
    // Bottom Section
    // =====================================================

    private HBox createBottomSection() {

        VBox recentSessions =
                createRecentSessionsPanel();

        VBox alerts =
                createAlertsPanel();

        HBox result =
                new HBox(
                        14,
                        recentSessions,
                        alerts
                );

        HBox.setHgrow(
                recentSessions,
                Priority.ALWAYS
        );

        alerts.setPrefWidth(
                380
        );

        alerts.setMinWidth(
                340
        );

        return result;
    }

    // =====================================================
    // Recent Sessions Panel
    // =====================================================

    private VBox createRecentSessionsPanel() {

        VBox panel =
                new VBox(
                        12
                );

        panel.setPadding(
                new Insets(
                        18
                )
        );

        panel.setMinHeight(
                360
        );

        panel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
            -fx-border-color: #E5EAF1;
            -fx-border-radius: 16px;
            -fx-border-width: 1px;
        """);

        Label title =
                new Label(
                        "آخر الجلسات"
                );

        title.setStyle("""
            -fx-font-size: 17px;
            -fx-font-weight: 800;
            -fx-text-fill: #172033;
        """);

        Label subtitle =
                new Label(
                        "أحدث الزيارات المسجلة في العيادة"
                );

        subtitle.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #98A2B3;
        """);

        VBox heading =
                new VBox(
                        3,
                        title,
                        subtitle
                );

        // =================================================
        // Client
        // =================================================

        TableColumn<RecentSession, String>
                clientColumn =
                new TableColumn<>(
                        "المريض"
                );

        clientColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                safe(
                                        data.getValue()
                                                .getClientName()
                                )
                        )
        );

        // =================================================
        // Date
        // =================================================

        TableColumn<RecentSession, String>
                dateColumn =
                new TableColumn<>(
                        "التاريخ"
                );

        dateColumn.setCellValueFactory(
                data -> {

                    if (data.getValue()
                            .getSessionDate()
                            == null) {

                        return new SimpleStringProperty(
                                "—"
                        );
                    }

                    return new SimpleStringProperty(
                            data.getValue()
                                    .getSessionDate()
                                    .format(
                                            DateTimeFormatter
                                                    .ofPattern(
                                                            "dd/MM/yyyy HH:mm"
                                                    )
                                    )
                    );
                }
        );

        // =================================================
        // Duration
        // =================================================

        TableColumn<RecentSession, String>
                durationColumn =
                new TableColumn<>(
                        "المدة"
                );

        durationColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                formatDuration(
                                        data.getValue()
                                                .getDuration()
                                )
                        )
        );

        // =================================================
        // Price
        // =================================================

        TableColumn<RecentSession, String>
                priceColumn =
                new TableColumn<>(
                        "السعر"
                );

        priceColumn.setCellValueFactory(
                data -> {

                    Double price =
                            data.getValue()
                                    .getPrice();

                    return new SimpleStringProperty(
                            price != null
                                    ? numberFormat.format(
                                    price
                            )
                                    + " ر.ي"
                                    : "—"
                    );
                }
        );

        // =================================================
        // Column Width
        // =================================================

        clientColumn.setPrefWidth(
                220
        );

        dateColumn.setPrefWidth(
                170
        );

        durationColumn.setPrefWidth(
                120
        );

        priceColumn.setPrefWidth(
                125
        );

        recentSessionsTable
                .getColumns()
                .setAll(
                        clientColumn,
                        dateColumn,
                        durationColumn,
                        priceColumn
                );

        recentSessionsTable
                .setColumnResizePolicy(
                        TableView.CONSTRAINED_RESIZE_POLICY
                );

        recentSessionsTable
                .setPlaceholder(
                        createTableEmptyLabel(
                                "لا توجد جلسات خلال الفترة الحالية"
                        )
                );

        recentSessionsTable.setFixedCellSize(
                52
        );

        recentSessionsTable.setMinHeight(
                260
        );

        recentSessionsTable.setStyle("""
            -fx-background-color: transparent;
            -fx-border-color: transparent;
            -fx-table-cell-border-color: transparent;
            -fx-font-size: 12px;
        """);

        recentSessionsTable.setRowFactory(
                tableView -> {

                    TableRow<RecentSession>
                            row =
                            new TableRow<>();

                    row.setOnMouseEntered(
                            event -> {

                                if (!row.isEmpty()) {

                                    row.setStyle("""
                                        -fx-background-color: #F8FAFC;
                                    """);
                                }
                            }
                    );

                    row.setOnMouseExited(
                            event -> {

                                if (!row.isEmpty()) {

                                    row.setStyle(
                                            ""
                                    );
                                }
                            }
                    );

                    return row;
                }
        );

        // =================================================
        // Custom Cell Styling
        // =================================================

        clientColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }

                                setText(
                                        item
                                );

                                setStyle("""
                                    -fx-font-size: 12px;
                                    -fx-font-weight: bold;
                                    -fx-text-fill: #344054;
                                """);
                            }
                        }
        );

        dateColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }

                                setText(
                                        item
                                );

                                setStyle("""
                                    -fx-font-size: 11px;
                                    -fx-text-fill: #667085;
                                """);
                            }
                        }
        );

        durationColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }

                                setText(
                                        item
                                );

                                setStyle("""
                                    -fx-font-size: 11px;
                                    -fx-font-weight: bold;
                                    -fx-text-fill: #0F766E;
                                """);
                            }
                        }
        );

        priceColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }

                                setText(
                                        item
                                );

                                setStyle("""
                                    -fx-font-size: 11px;
                                    -fx-font-weight: bold;
                                    -fx-text-fill: #B45309;
                                """);
                            }
                        }
        );

        panel.getChildren().addAll(
                heading,
                recentSessionsTable
        );

        VBox.setVgrow(
                recentSessionsTable,
                Priority.ALWAYS
        );

        return panel;
    }

    // =====================================================
    // Alerts Panel
    // =====================================================

    private VBox createAlertsPanel() {

        VBox panel =
                new VBox(
                        12
                );

        panel.setPadding(
                new Insets(
                        18
                )
        );

        panel.setMinHeight(
                360
        );

        panel.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
            -fx-border-color: #E5EAF1;
            -fx-border-radius: 16px;
            -fx-border-width: 1px;
        """);

        Label title =
                new Label(
                        "المراقبة والتنبيهات"
                );

        title.setStyle("""
            -fx-font-size: 17px;
            -fx-font-weight: 800;
            -fx-text-fill: #172033;
        """);

        Label subtitle =
                new Label(
                        "تنبيهات تحتاج إلى الانتباه أو المتابعة"
                );

        subtitle.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #98A2B3;
        """);

        VBox heading =
                new VBox(
                        3,
                        title,
                        subtitle
                );

        ScrollPane scroll =
                new ScrollPane(
                        alertsContainer
                );

        scroll.setFitToWidth(
                true
        );

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scroll.setPannable(
                true
        );

        scroll.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-border-color: transparent;
        """);

        alertsContainer.setPadding(
                new Insets(
                        2,
                        3,
                        3,
                        3
                )
        );

        panel.getChildren().addAll(
                heading,
                scroll
        );

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        return panel;
    }

    // =====================================================
    // Alert Card
    // =====================================================

    private HBox createAlertCard(
            ReportAlert alert
    ) {

        String severity =
                alert.getSeverity() != null
                        ? alert.getSeverity()
                        .toUpperCase()
                        : "INFO";

        HBox box =
                new HBox(
                        11
                );

        box.setPadding(
                new Insets(
                        13
                )
        );

        box.setAlignment(
                Pos.CENTER_RIGHT
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                getAlertBackground(
                        severity
                )
        );

        // =================================================
        // Icon
        // =================================================

        Label icon =
                new Label(
                        alertIcon(
                                severity
                        )
                );

        icon.setMinSize(
                34,
                34
        );

        icon.setPrefSize(
                34,
                34
        );

        icon.setMaxSize(
                34,
                34
        );

        icon.setAlignment(
                Pos.CENTER
        );

        icon.setStyle(
                getAlertIconStyle(
                        severity
                )
        );

        // =================================================
        // Text
        // =================================================

        VBox text =
                new VBox(
                        4
                );

        String countText =
                alert.getCount() > 0
                        ? "  (" +
                        integerFormat.format(
                                alert.getCount()
                        )
                        + ")"
                        : "";

        Label title =
                new Label(
                        safe(
                                alert.getTitle()
                        )
                                + countText
                );

        title.setWrapText(
                true
        );

        title.setStyle("""
            -fx-font-size: 12px;
            -fx-font-weight: 800;
            -fx-text-fill: #344054;
        """);

        Label message =
                new Label(
                        safe(
                                alert.getMessage()
                        )
                );

        message.setWrapText(
                true
        );

        message.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #667085;
            -fx-line-spacing: 1px;
        """);

        text.getChildren().addAll(
                title,
                message
        );

        HBox.setHgrow(
                text,
                Priority.ALWAYS
        );

        box.getChildren().addAll(
                text,
                icon
        );

        return box;
    }

    // =====================================================
    // Loading
    // =====================================================

    private void showLoading() {

        loadingOverlay.setOpacity(
                0
        );

        loadingOverlay.setVisible(
                true
        );

        FadeTransition fade =
                new FadeTransition(
                        javafx.util.Duration.millis(
                                150
                        ),
                        loadingOverlay
                );

        fade.setFromValue(
                0
        );

        fade.setToValue(
                1
        );

        fade.play();

        refreshButton.setDisable(
                true
        );

        periodCombo.setDisable(
                true
        );
    }

    private void hideLoading() {

        FadeTransition fade =
                new FadeTransition(
                        javafx.util.Duration.millis(
                                160
                        ),
                        loadingOverlay
                );

        fade.setFromValue(
                1
        );

        fade.setToValue(
                0
        );

        fade.setOnFinished(
                event ->
                        loadingOverlay.setVisible(
                                false
                        )
        );

        fade.play();

        refreshButton.setDisable(
                false
        );

        periodCombo.setDisable(
                false
        );
    }

    // =====================================================
    // Load Dashboard
    // =====================================================

    private void loadDashboard() {

        if (loading) {
            return;
        }

        loading = true;

        int days =
                getSelectedDays();

        showLoading();

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                FullDashboardResponse data =
                                        reportsAPI
                                                .getFullDashboard(
                                                        days,
                                                        10
                                                );

                                Platform.runLater(
                                        () -> {

                                            try {

                                                updateDashboard(
                                                        data
                                                );

                                            } finally {

                                                loading =
                                                        false;

                                                hideLoading();
                                            }
                                        }
                                );

                            } catch (
                                    Exception e
                            ) {

                                e.printStackTrace();

                                Platform.runLater(
                                        () -> {

                                            try {

                                                showError(
                                                        e.getMessage()
                                                );

                                            } finally {

                                                loading =
                                                        false;

                                                hideLoading();
                                            }
                                        }
                                );
                            }
                        }
                );

        thread.setDaemon(
                true
        );

        thread.setName(
                "FAS-Reports-Dashboard"
        );

        thread.start();
    }

    // =====================================================
    // Period
    // =====================================================

    private int getSelectedDays() {

        String value =
                periodCombo.getValue();

        if (value == null) {
            return 30;
        }

        return switch (value) {

            case "آخر 7 أيام" ->
                    7;

            case "آخر 90 يومًا" ->
                    90;

            case "آخر 180 يومًا" ->
                    180;

            case "آخر سنة" ->
                    365;

            default ->
                    30;
        };
    }

    // =====================================================
    // Update Dashboard
    // =====================================================

    private void updateDashboard(
            FullDashboardResponse data
    ) {

        if (data == null) {

            showError(
                    "السيرفر أعاد بيانات فارغة."
            );

            return;
        }

        Dashboard overview =
                data.getOverview();

        if (overview == null) {

            showError(
                    "بيانات الإحصائيات الرئيسية غير موجودة."
            );

            return;
        }

        // =================================================
        // KPI
        // =================================================

        totalClientsValue.setText(
                integerFormat.format(
                        overview.getTotalClients()
                )
        );

        newClientsValue.setText(
                integerFormat.format(
                        overview.getNewClients()
                )
        );

        sessionsValue.setText(
                integerFormat.format(
                        overview.getTotalSessions()
                )
        );

        activePlansValue.setText(
                integerFormat.format(
                        overview.getActivePlans()
                )
        );

        revenueValue.setText(
                numberFormat.format(
                        overview.getPeriodRevenue()
                )
                        + " ر.ي"
        );

        averageDurationValue.setText(
                numberFormat.format(
                        overview
                                .getAverageSessionDuration()
                )
        );

        // =================================================
        // Growth
        // =================================================

        clientsGrowth.setText(
                formatGrowth(
                        overview.getClientsGrowth()
                )
        );

        sessionsGrowth.setText(
                formatGrowth(
                        overview.getSessionsGrowth()
                )
        );

        plansGrowth.setText(
                formatGrowth(
                        overview.getPlansGrowth()
                )
        );

        styleGrowthLabel(
                clientsGrowth,
                overview.getClientsGrowth()
        );

        styleGrowthLabel(
                sessionsGrowth,
                overview.getSessionsGrowth()
        );

        styleGrowthLabel(
                plansGrowth,
                overview.getPlansGrowth()
        );

        // =================================================
        // Charts
        // =================================================

        updateSessionsChart(
                data.getSessionsTrend()
        );

        updateRevenueChart(
                data.getRevenueTrend()
        );

        updatePlansChart(
                data.getPlanStatuses()
        );

        // =================================================
        // Table
        // =================================================

        updateRecentSessions(
                data.getRecentSessions()
        );

        // =================================================
        // Alerts
        // =================================================

        updateAlerts(
                data.getAlerts()
        );

        // =================================================
        // Smooth Update
        // =================================================

        dashboardContent.setOpacity(
                0.88
        );

        FadeTransition fade =
                new FadeTransition(
                        javafx.util.Duration.millis(
                                220
                        ),
                        dashboardContent
                );

        fade.setFromValue(
                0.88
        );

        fade.setToValue(
                1
        );

        fade.play();
    }

    // =====================================================
    // Sessions Chart
    // =====================================================

    private void updateSessionsChart(
            List<SessionTrend> trend
    ) {

        LinkedHashMap<String, Number>
                values =
                buildSessionsChartData(
                        trend
                );

        XYChart.Series<String, Number>
                series =
                new XYChart.Series<>();

        series.setName(
                "الجلسات"
        );

        for (
                Map.Entry<String, Number> entry :
                values.entrySet()
        ) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        sessionsChart
                .getData()
                .setAll(
                        series
                );

        boolean empty =
                values.isEmpty();

        sessionsEmptyLabel.setVisible(
                empty
        );

        sessionsEmptyLabel.setManaged(
                empty
        );

        sessionsChart.setVisible(
                !empty
        );

        sessionsChart.setManaged(
                !empty
        );

        if (!empty) {

            Platform.runLater(
                    () -> {

                        styleCharts();

                        installSessionTooltips(
                                series
                        );
                    }
            );
        }
    }

    // =====================================================
    // Revenue Chart
    // =====================================================

    private void updateRevenueChart(
            List<RevenueTrend> trend
    ) {

        LinkedHashMap<String, Number>
                values =
                buildRevenueChartData(
                        trend
                );

        XYChart.Series<String, Number>
                series =
                new XYChart.Series<>();

        series.setName(
                "الإيرادات"
        );

        for (
                Map.Entry<String, Number> entry :
                values.entrySet()
        ) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        revenueChart
                .getData()
                .setAll(
                        series
                );

        boolean empty =
                values.isEmpty();

        revenueEmptyLabel.setVisible(
                empty
        );

        revenueEmptyLabel.setManaged(
                empty
        );

        revenueChart.setVisible(
                !empty
        );

        revenueChart.setManaged(
                !empty
        );

        if (!empty) {

            Platform.runLater(
                    () -> {

                        styleCharts();

                        installRevenueTooltips(
                                series
                        );
                    }
            );
        }
    }

    // =====================================================
    // Plans Chart
    // =====================================================

    private void updatePlansChart(
            List<PlanStatus> statuses
    ) {

        List<PieChart.Data>
                chartData =
                new ArrayList<>();

        if (statuses != null) {

            for (
                    PlanStatus item :
                    statuses
            ) {

                if (item == null
                        || item.getCount() <= 0) {

                    continue;
                }

                String translated =
                        translateStatus(
                                item.getStatus()
                        );

                String label =
                        translated
                                + "  ("
                                + integerFormat.format(
                                item.getCount()
                        )
                                + ")";

                chartData.add(
                        new PieChart.Data(
                                label,
                                item.getCount()
                        )
                );
            }
        }

        plansChart.setData(
                FXCollections.observableArrayList(
                        chartData
                )
        );

        boolean empty =
                chartData.isEmpty();

        plansEmptyLabel.setVisible(
                empty
        );

        plansEmptyLabel.setManaged(
                empty
        );

        plansChart.setVisible(
                !empty
        );

        plansChart.setManaged(
                !empty
        );

        if (!empty) {

            Platform.runLater(
                    this::stylePlansChart
            );
        }
    }

    // =====================================================
    // Recent Sessions
    // =====================================================

    private void updateRecentSessions(
            List<RecentSession> sessions
    ) {

        recentSessionsTable.setItems(
                FXCollections.observableArrayList(
                        sessions != null
                                ? sessions
                                : List.of()
                )
        );
    }

    // =====================================================
    // Alerts
    // =====================================================

    private void updateAlerts(
            List<ReportAlert> alerts
    ) {

        alertsContainer
                .getChildren()
                .clear();

        if (alerts == null
                || alerts.isEmpty()) {

            HBox emptyBox =
                    new HBox(
                            10
                    );

            emptyBox.setPadding(
                    new Insets(
                            16
                    )
            );

            emptyBox.setAlignment(
                    Pos.CENTER_RIGHT
            );

            emptyBox.setStyle("""
                -fx-background-color: #F8FAFC;
                -fx-background-radius: 12px;
                -fx-border-color: #E2E8F0;
                -fx-border-radius: 12px;
            """);

            Label icon =
                    new Label(
                            "✓"
                    );

            icon.setStyle("""
                -fx-font-size: 17px;
                -fx-font-weight: bold;
                -fx-text-fill: #059669;
            """);

            Label text =
                    new Label(
                            "لا توجد تنبيهات تحتاج إلى متابعة حاليًا"
                    );

            text.setWrapText(
                    true
            );

            text.setStyle("""
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-text-fill: #475467;
            """);

            emptyBox
                    .getChildren()
                    .addAll(
                            text,
                            icon
                    );

            alertsContainer
                    .getChildren()
                    .add(
                            emptyBox
                    );

            return;
        }

        for (
                ReportAlert alert :
                alerts
        ) {

            if (alert == null) {
                continue;
            }

            alertsContainer
                    .getChildren()
                    .add(
                            createAlertCard(
                                    alert
                            )
                    );
        }
    }

    // =====================================================
    // Build Sessions Data
    // =====================================================

    private LinkedHashMap<String, Number>
    buildSessionsChartData(
            List<SessionTrend> trend
    ) {

        LinkedHashMap<String, Number>
                result =
                new LinkedHashMap<>();

        if (trend == null
                || trend.isEmpty()) {

            return result;
        }

        int days =
                getSelectedDays();

        if (days <= 30) {

            Map<LocalDate, Long>
                    raw =
                    new HashMap<>();

            for (
                    SessionTrend item :
                    trend
            ) {

                if (item == null
                        || item.getDate() == null) {

                    continue;
                }

                raw.put(
                        item.getDate(),
                        item.getCount()
                );
            }

            LocalDate end =
                    LocalDate.now();

            LocalDate start =
                    end.minusDays(
                            days - 1L
                    );

            LocalDate current =
                    start;

            while (!current.isAfter(end)) {

                result.put(
                        current.format(
                                dailyFormatter
                        ),
                        raw.getOrDefault(
                                current,
                                0L
                        )
                );

                current =
                        current.plusDays(1);
            }

            return result;
        }

        if (days <= 90) {

            Map<LocalDate, Long>
                    weekly =
                    new HashMap<>();

            for (
                    SessionTrend item :
                    trend
            ) {

                if (item == null
                        || item.getDate() == null) {

                    continue;
                }

                LocalDate week =
                        item.getDate()
                                .with(
                                        TemporalAdjusters
                                                .previousOrSame(
                                                        DayOfWeek.MONDAY
                                                )
                                );

                weekly.merge(
                        week,
                        item.getCount(),
                        Long::sum
                );
            }

            LocalDate end =
                    LocalDate.now();

            LocalDate start =
                    end.minusDays(
                                    days - 1L
                            )
                            .with(
                                    TemporalAdjusters
                                            .previousOrSame(
                                                    DayOfWeek.MONDAY
                                            )
                            );

            LocalDate current =
                    start;

            while (!current.isAfter(end)) {

                result.put(
                        current.format(
                                dailyFormatter
                        ),
                        weekly.getOrDefault(
                                current,
                                0L
                        )
                );

                current =
                        current.plusWeeks(1);
            }

            return result;
        }

        Map<YearMonth, Long>
                monthly =
                new HashMap<>();

        for (
                SessionTrend item :
                trend
        ) {

            if (item == null
                    || item.getDate() == null) {

                continue;
            }

            YearMonth month =
                    YearMonth.from(
                            item.getDate()
                    );

            monthly.merge(
                    month,
                    item.getCount(),
                    Long::sum
            );
        }

        YearMonth end =
                YearMonth.now();

        YearMonth start =
                end.minusMonths(
                        estimateMonthCount(
                                days
                        ) - 1L
                );

        YearMonth current =
                start;

        while (!current.isAfter(end)) {

            result.put(
                    current.format(
                            monthlyFormatter
                    ),
                    monthly.getOrDefault(
                            current,
                            0L
                    )
            );

            current =
                    current.plusMonths(1);
        }

        return result;
    }

    // =====================================================
    // Build Revenue Data
    // =====================================================

    private LinkedHashMap<String, Number>
    buildRevenueChartData(
            List<RevenueTrend> trend
    ) {

        LinkedHashMap<String, Number>
                result =
                new LinkedHashMap<>();

        if (trend == null
                || trend.isEmpty()) {

            return result;
        }

        int days =
                getSelectedDays();

        if (days <= 30) {

            Map<LocalDate, Double>
                    raw =
                    new HashMap<>();

            for (
                    RevenueTrend item :
                    trend
            ) {

                if (item == null
                        || item.getDate() == null) {

                    continue;
                }

                raw.put(
                        item.getDate(),
                        item.getRevenue()
                );
            }

            LocalDate end =
                    LocalDate.now();

            LocalDate start =
                    end.minusDays(
                            days - 1L
                    );

            LocalDate current =
                    start;

            while (!current.isAfter(end)) {

                result.put(
                        current.format(
                                dailyFormatter
                        ),
                        raw.getOrDefault(
                                current,
                                0.0
                        )
                );

                current =
                        current.plusDays(1);
            }

            return result;
        }

        if (days <= 90) {

            Map<LocalDate, Double>
                    weekly =
                    new HashMap<>();

            for (
                    RevenueTrend item :
                    trend
            ) {

                if (item == null
                        || item.getDate() == null) {

                    continue;
                }

                LocalDate week =
                        item.getDate()
                                .with(
                                        TemporalAdjusters
                                                .previousOrSame(
                                                        DayOfWeek.MONDAY
                                                )
                                );

                weekly.merge(
                        week,
                        item.getRevenue(),
                        Double::sum
                );
            }

            LocalDate end =
                    LocalDate.now();

            LocalDate start =
                    end.minusDays(
                                    days - 1L
                            )
                            .with(
                                    TemporalAdjusters
                                            .previousOrSame(
                                                    DayOfWeek.MONDAY
                                            )
                            );

            LocalDate current =
                    start;

            while (!current.isAfter(end)) {

                result.put(
                        current.format(
                                dailyFormatter
                        ),
                        weekly.getOrDefault(
                                current,
                                0.0
                        )
                );

                current =
                        current.plusWeeks(1);
            }

            return result;
        }

        Map<YearMonth, Double>
                monthly =
                new HashMap<>();

        for (
                RevenueTrend item :
                trend
        ) {

            if (item == null
                    || item.getDate() == null) {

                continue;
            }

            YearMonth month =
                    YearMonth.from(
                            item.getDate()
                    );

            monthly.merge(
                    month,
                    item.getRevenue(),
                    Double::sum
            );
        }

        YearMonth end =
                YearMonth.now();

        YearMonth start =
                end.minusMonths(
                        estimateMonthCount(
                                days
                        ) - 1L
                );

        YearMonth current =
                start;

        while (!current.isAfter(end)) {

            result.put(
                    current.format(
                            monthlyFormatter
                    ),
                    monthly.getOrDefault(
                            current,
                            0.0
                    )
            );

            current =
                    current.plusMonths(1);
        }

        return result;
    }

    // =====================================================
    // Month Count
    // =====================================================

    private int estimateMonthCount(
            int days
    ) {

        if (days <= 180) {
            return 6;
        }

        return 12;
    }

    // =====================================================
    // Chart Styling
    // =====================================================

    private void styleCharts() {

        // =================================================
        // Sessions
        // =================================================

        Node sessionsPlot =
                sessionsChart.lookup(
                        ".chart-plot-background"
                );

        if (sessionsPlot != null) {

            sessionsPlot.setStyle("""
                -fx-background-color: transparent;
                """
            );
        }

        Node sessionBar =
                sessionsChart.lookup(
                        ".default-color0.chart-bar"
                );

        if (sessionBar != null) {

            sessionBar.setStyle(
                    "-fx-bar-fill: "
                            + PRIMARY
                            + ";"
            );
        }

        Node sessionXAxis =
                sessionsChart.lookup(
                        ".axis"
                );

        if (sessionXAxis != null) {

            sessionXAxis.setStyle("""
                -fx-tick-label-fill: #667085;
                -fx-font-size: 10px;
            """);
        }

        Node sessionYAxis =
                sessionsChart
                        .getYAxis();

        if (sessionYAxis != null) {

            sessionYAxis.setStyle("""
                -fx-tick-label-fill: #667085;
                -fx-font-size: 10px;
            """);
        }

        // =================================================
        // Revenue
        // =================================================

        Node revenuePlot =
                revenueChart.lookup(
                        ".chart-plot-background"
                );

        if (revenuePlot != null) {

            revenuePlot.setStyle("""
                -fx-background-color: transparent;
                """
            );
        }

        Node revenueLine =
                revenueChart.lookup(
                        ".default-color0.chart-series-line"
                );

        if (revenueLine != null) {

            revenueLine.setStyle(
                    "-fx-stroke: "
                            + SUCCESS
                            + ";"
                            + "-fx-stroke-width: 3px;"
            );
        }

        Node revenueSymbols =
                revenueChart.lookup(
                        ".default-color0.chart-line-symbol"
                );

        if (revenueSymbols != null) {

            revenueSymbols.setStyle(
                    "-fx-background-color: "
                            + SUCCESS
                            + ", white;"
                            + "-fx-background-radius: 7px;"
                            + "-fx-padding: 5px;"
            );
        }

        Node revenueYAxis =
                revenueChart
                        .getYAxis();

        if (revenueYAxis != null) {

            revenueYAxis.setStyle("""
                -fx-tick-label-fill: #667085;
                -fx-font-size: 10px;
            """);
        }

        // =================================================
        // Common
        // =================================================

        Node revenueXAxis =
                revenueChart.lookup(
                        ".axis"
                );

        if (revenueXAxis != null) {

            revenueXAxis.setStyle("""
                -fx-tick-label-fill: #667085;
                -fx-font-size: 10px;
            """);
        }

        // =================================================
        // Grid Lines
        // =================================================

        Node sessionsGrid =
                sessionsChart.lookup(
                        ".chart-horizontal-grid-lines"
                );

        if (sessionsGrid != null) {

            sessionsGrid.setStyle("""
                -fx-stroke: #EEF2F6;
                """
            );
        }

        Node revenueGrid =
                revenueChart.lookup(
                        ".chart-horizontal-grid-lines"
                );

        if (revenueGrid != null) {

            revenueGrid.setStyle("""
                -fx-stroke: #EEF2F6;
                """
            );
        }
    }

    // =====================================================
    // Plans Chart Styling
    // =====================================================

    private void stylePlansChart() {

        Node chartBackground =
                plansChart.lookup(
                        ".chart-plot-background"
                );

        if (chartBackground != null) {

            chartBackground.setStyle("""
                -fx-background-color: transparent;
                """
            );
        }

        for (
                PieChart.Data data :
                plansChart.getData()
        ) {

            Tooltip tooltip =
                    new Tooltip(
                            data.getName()
                                    + "\n"
                                    + "عدد الخطط: "
                                    + integerFormat.format(
                                    data.getPieValue()
                            )
                    );

            Tooltip.install(
                    data.getNode(),
                    tooltip
            );
        }
    }

    // =====================================================
    // Session Tooltips
    // =====================================================

    private void installSessionTooltips(
            XYChart.Series<String, Number> series
    ) {

        for (
                XYChart.Data<String, Number> data :
                series.getData()
        ) {

            if (data.getNode() == null) {
                continue;
            }

            Tooltip tooltip =
                    new Tooltip(
                            data.getXValue()
                                    + "\n"
                                    + "عدد الجلسات: "
                                    + integerFormat.format(
                                    data.getYValue()
                            )
                    );

            tooltip.setStyle("""
                -fx-font-size: 11px;
                -fx-background-color: #172033;
                -fx-text-fill: white;
                -fx-background-radius: 8px;
                -fx-padding: 8px 10px;
            """);

            Tooltip.install(
                    data.getNode(),
                    tooltip
            );
        }
    }

    // =====================================================
    // Revenue Tooltips
    // =====================================================

    private void installRevenueTooltips(
            XYChart.Series<String, Number> series
    ) {

        for (
                XYChart.Data<String, Number> data :
                series.getData()
        ) {

            if (data.getNode() == null) {
                continue;
            }

            String value =
                    numberFormat.format(
                            data.getYValue()
                    );

            Tooltip tooltip =
                    new Tooltip(
                            data.getXValue()
                                    + "\n"
                                    + "الإيرادات: "
                                    + value
                                    + " ر.ي"
                    );

            tooltip.setStyle("""
                -fx-font-size: 11px;
                -fx-background-color: #172033;
                -fx-text-fill: white;
                -fx-background-radius: 8px;
                -fx-padding: 8px 10px;
            """);

            Tooltip.install(
                    data.getNode(),
                    tooltip
            );
        }
    }

    // =====================================================
    // Empty Chart Label
    // =====================================================

    private static Label createChartEmptyLabel() {

        Label label =
                new Label(
                        "لا توجد بيانات خلال الفترة المحددة"
                );

        label.setStyle("""
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #98A2B3;
            -fx-padding: 20px;
        """);

        label.setVisible(
                false
        );

        label.setManaged(
                false
        );

        return label;
    }

    // =====================================================
    // Empty Table Label
    // =====================================================

    private Label createTableEmptyLabel(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #98A2B3;
        """);

        return label;
    }

    // =====================================================
    // Section Title
    // =====================================================

    private Label sectionTitle(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setStyle("""
            -fx-font-size: 19px;
            -fx-font-weight: 800;
            -fx-text-fill: #172033;
        """);

        return label;
    }

    // =====================================================
    // Section Subtitle
    // =====================================================

    private Label sectionSubtitle(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #98A2B3;
        """);

        return label;
    }

    // =====================================================
    // Growth Formatting
    // =====================================================

    private String formatGrowth(
            double value
    ) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            return "—";
        }

        if (value == 0) {

            return "0%";
        }

        String sign =
                value > 0
                        ? "↑ "
                        : "↓ ";

        return sign
                + numberFormat.format(
                Math.abs(value)
        )
                + "%";
    }

    // =====================================================
    // Growth Styling
    // =====================================================

    private void styleGrowthLabel(
            Label label,
            double value
    ) {

        String background;
        String textColor;

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            background =
                    "#F2F4F7";

            textColor =
                    "#667085";

        } else if (value > 0) {

            background =
                    SUCCESS_LIGHT;

            textColor =
                    SUCCESS;

        } else if (value < 0) {

            background =
                    DANGER_LIGHT;

            textColor =
                    DANGER;

        } else {

            background =
                    "#F2F4F7";

            textColor =
                    "#667085";
        }

        label.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + textColor
                        + ";"
                        + "-fx-background-color: "
                        + background
                        + ";"
                        + "-fx-background-radius: 7px;"
                        + "-fx-padding: 4px 7px;"
        );
    }

    // =====================================================
    // Status Translation
    // =====================================================

    private String translateStatus(
            String status
    ) {

        if (status == null
                || status.isBlank()) {

            return "غير معروف";
        }

        return switch (
                status.toLowerCase()
                ) {

            case "active" ->
                    "نشطة";

            case "completed" ->
                    "مكتملة";

            case "cancelled" ->
                    "ملغاة";

            default ->
                    status;
        };
    }

    // =====================================================
    // Alert Icon
    // =====================================================

    private String alertIcon(
            String severity
    ) {

        if (severity == null) {

            return "•";
        }

        return switch (
                severity.toUpperCase()
                ) {

            case "CRITICAL" ->
                    "×";

            case "WARNING" ->
                    "⚠";

            case "INFO" ->
                    "i";

            default ->
                    "•";
        };
    }

    // =====================================================
    // Alert Background
    // =====================================================

    private String getAlertBackground(
            String severity
    ) {

        return switch (
                severity
                ) {

            case "CRITICAL" -> """
                -fx-background-color: #FEF2F2;
                -fx-background-radius: 12px;
                -fx-border-color: #FECACA;
                -fx-border-radius: 12px;
                -fx-border-width: 1px;
                """;

            case "WARNING" -> """
                -fx-background-color: #FFFBEB;
                -fx-background-radius: 12px;
                -fx-border-color: #FDE68A;
                -fx-border-radius: 12px;
                -fx-border-width: 1px;
                """;

            default -> """
                -fx-background-color: #F8FAFC;
                -fx-background-radius: 12px;
                -fx-border-color: #E2E8F0;
                -fx-border-radius: 12px;
                -fx-border-width: 1px;
                """;
        };
    }

    // =====================================================
    // Alert Icon Style
    // =====================================================

    private String getAlertIconStyle(
            String severity
    ) {

        return switch (
                severity
                ) {

            case "CRITICAL" ->
                    """
                    -fx-background-color: #FEE2E2;
                    -fx-background-radius: 10px;
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #DC2626;
                    """;

            case "WARNING" ->
                    """
                    -fx-background-color: #FEF3C7;
                    -fx-background-radius: 10px;
                    -fx-font-size: 15px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #D97706;
                    """;

            default ->
                    """
                    -fx-background-color: #E0F2FE;
                    -fx-background-radius: 10px;
                    -fx-font-size: 15px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #0369A1;
                    """;
        };
    }

    // =====================================================
    // Duration Formatting
    // =====================================================

    private String formatDuration(
            String duration
    ) {

        if (duration == null
                || duration.isBlank()) {

            return "—";
        }

        String value =
                duration.trim();

        // -------------------------------------------------
        // HH:MM
        // -------------------------------------------------

        if (value.matches(
                "\\d{1,2}:\\d{1,2}"
        )) {

            String[] parts =
                    value.split(":");

            int hours =
                    Integer.parseInt(
                            parts[0]
                    );

            int minutes =
                    Integer.parseInt(
                            parts[1]
                    );

            return buildDurationText(
                    hours,
                    minutes
            );
        }

        // -------------------------------------------------
        // D H:M:S
        // مثال:
        // 0 1:0:0
        // -------------------------------------------------

        if (value.matches(
                "\\d+\\s+\\d+:\\d+:\\d+"
        )) {

            String[] dayAndTime =
                    value.split(
                            "\\s+"
                    );

            String[] time =
                    dayAndTime[1]
                            .split(":");

            int days =
                    Integer.parseInt(
                            dayAndTime[0]
                    );

            int hours =
                    Integer.parseInt(
                            time[0]
                    );

            int minutes =
                    Integer.parseInt(
                            time[1]
                    );

            hours +=
                    days * 24;

            return buildDurationText(
                    hours,
                    minutes
            );
        }

        // -------------------------------------------------
        // رقم = دقائق
        // -------------------------------------------------

        if (value.matches(
                "\\d+(\\.\\d+)?"
        )) {

            double minutes =
                    Double.parseDouble(
                            value
                    );

            if (minutes < 60) {

                return numberFormat.format(
                        minutes
                )
                        + " دقيقة";
            }

            int hours =
                    (int) (
                            minutes / 60
                    );

            int remaining =
                    (int) (
                            minutes % 60
                    );

            return buildDurationText(
                    hours,
                    remaining
            );
        }

        return value;
    }

    // =====================================================
    // Duration Text
    // =====================================================

    private String buildDurationText(
            int hours,
            int minutes
    ) {

        if (hours <= 0
                && minutes <= 0) {

            return "0 دقيقة";
        }

        if (hours <= 0) {

            return minutes
                    + " دقيقة";
        }

        if (minutes <= 0) {

            return hours
                    + " ساعة";
        }

        return hours
                + " ساعة "
                + minutes
                + " دقيقة";
    }

    // =====================================================
    // Safe
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                || value.isBlank()
                ? "—"
                : value;
    }

    // =====================================================
    // Error
    // =====================================================

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "خطأ في التقارير"
        );

        alert.setHeaderText(
                "تعذر تحميل لوحة التقارير"
        );

        alert.setContentText(
                message == null
                        || message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.getDialogPane()
                .setStyle("""
                    -fx-font-family: "Arial";
                    -fx-background-color: #FFFFFF;
                """);

        alert.show();
    }
}