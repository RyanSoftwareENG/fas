package user_interface.offer;

import api.ApiClient;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner;

import java.io.IOException;

public class HomeView extends BorderPane {

    // =====================================================
    // API CLIENT
    // =====================================================

    private ApiClient apiClient;

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private final StackPane contentArea =
            new StackPane();

    private AnchorPane centerContentHolder;

    // =====================================================
    // HOME CONTROLLER
    // =====================================================

    private Home homeController;

    // =====================================================
    // CONSTRUCTORS
    // =====================================================

    public HomeView() {
        this(null);
    }

    public HomeView(
            ApiClient apiClient
    ) {
        this.apiClient = apiClient;
        initialize();
    }

    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initialize() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        getStyleClass().add(
                "fas-root"
        );

        setCenter(
                contentArea
        );

        contentArea.setMaxWidth(
                Double.MAX_VALUE
        );

        contentArea.setMaxHeight(
                Double.MAX_VALUE
        );

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/Home.fxml"
                            )
                    );

            Parent homeNode =
                    loader.load();

            homeController =
                    loader.getController();

            if (homeController != null) {

                homeController.setMainHomeView(
                        this
                );

                centerContentHolder =
                        homeController.getMainContentArea();

            } else {

                System.err.println(
                        "تحذير: لم يتم العثور على Controller لملف Home.fxml"
                );
            }

            if (homeNode instanceof Region region) {

                region.setMaxWidth(
                        Double.MAX_VALUE
                );

                region.setMaxHeight(
                        Double.MAX_VALUE
                );
            }

            contentArea
                    .getChildren()
                    .add(
                            homeNode
                    );

        } catch (IOException e) {

            System.err.println(
                    "خطأ: تعذر تحميل Home.fxml"
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // API CLIENT
    // =====================================================

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(
            ApiClient apiClient
    ) {
        this.apiClient = apiClient;
    }

    // =====================================================
    // HOME WELCOME
    // =====================================================

    public void showHomeWelcome() {

        if (centerContentHolder == null) {
            return;
        }

        centerContentHolder
                .getChildren()
                .clear();

        VBox welcome =
                createWelcomeView();

        AnchorPane.setTopAnchor(
                welcome,
                0.0
        );

        AnchorPane.setBottomAnchor(
                welcome,
                0.0
        );

        AnchorPane.setLeftAnchor(
                welcome,
                0.0
        );

        AnchorPane.setRightAnchor(
                welcome,
                0.0
        );

        centerContentHolder
                .getChildren()
                .add(
                        welcome
                );
    }

    // =====================================================
    // CREATE HOME WELCOME
    // =====================================================

    private VBox createWelcomeView() {

        VBox root =
                new VBox(24);

        root.setAlignment(
                Pos.CENTER
        );

        root.setPadding(
                new Insets(
                        45,
                        50,
                        45,
                        50
                )
        );

        root.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        root.getStyleClass().add(
                "fas-home-welcome"
        );

        // =================================================
        // TEXT LOGO
        // =================================================

        Label logo =
                new Label(
                        "FAS"
                );

        logo.getStyleClass().add(
                "fas-home-logo-text"
        );

        // =================================================
        // LOGO SUBTITLE
        // =================================================

        Label logoSubtitle =
                new Label(
                        "Food Assistant System"
                );

        logoSubtitle.getStyleClass().add(
                "fas-home-logo-subtitle"
        );

        // =================================================
        // DIVIDER
        // =================================================

        Region divider =
                new Region();

        divider.setPrefHeight(
                2
        );

        divider.setMaxWidth(
                110
        );

        divider.getStyleClass().add(
                "fas-home-divider"
        );

        // =================================================
        // WELCOME SECTION
        // =================================================

        VBox welcomeSection =
                new VBox(10);

        welcomeSection.setAlignment(
                Pos.CENTER
        );

        welcomeSection.setMaxWidth(
                760
        );

        Label welcomeTitle =
                new Label(
                        "مرحباً بك في نظام FAS"
                );

        welcomeTitle.getStyleClass().add(
                "fas-home-main-title"
        );

        Label welcomeDescription =
                new Label(
                        "نظام متكامل يساعد عيادات التغذية على تنظيم أعمالها وإدارة بيانات العملاء والجلسات والخطط الغذائية والتقارير من خلال واجهة سهلة وواضحة."
                );

        welcomeDescription.setWrapText(
                true
        );

        welcomeDescription.setMaxWidth(
                680
        );

        welcomeDescription.setAlignment(
                Pos.CENTER
        );

        welcomeDescription.setTextAlignment(
                javafx.scene.text.TextAlignment.CENTER
        );

        welcomeDescription.getStyleClass().add(
                "fas-home-main-description"
        );

        welcomeSection
                .getChildren()
                .addAll(
                        welcomeTitle,
                        welcomeDescription
                );

        // =================================================
        // ABOUT PROJECT
        // =================================================

        VBox aboutSection =
                new VBox(9);

        aboutSection.setAlignment(
                Pos.CENTER
        );

        aboutSection.setMaxWidth(
                760
        );

        Label aboutTitle =
                new Label(
                        "عن المشروع"
                );

        aboutTitle.getStyleClass().add(
                "fas-home-section-heading"
        );

        Label aboutDescription =
                new Label(
                        "تم تطوير FAS ليكون حلاً عملياً لإدارة عيادات التغذية، مع التركيز على سهولة الاستخدام وتنظيم المعلومات وربط مختلف العمليات في نظام واحد."
                );

        aboutDescription.setWrapText(
                true
        );

        aboutDescription.setMaxWidth(
                680
        );

        aboutDescription.setAlignment(
                Pos.CENTER
        );

        aboutDescription.setTextAlignment(
                javafx.scene.text.TextAlignment.CENTER
        );

        aboutDescription.getStyleClass().add(
                "fas-home-section-description"
        );

        aboutSection
                .getChildren()
                .addAll(
                        aboutTitle,
                        aboutDescription
                );

        // =================================================
        // DEVELOPMENT TEAM
        // =================================================

        VBox teamSection =
                new VBox(9);

        teamSection.setAlignment(
                Pos.CENTER
        );

        teamSection.setMaxWidth(
                760
        );

        Label teamTitle =
                new Label(
                        "فريق التطوير"
                );

        teamTitle.getStyleClass().add(
                "fas-home-team-heading"
        );

        Label teamDescription =
                new Label(
                        "تم تصميم وتحليل وتطوير المشروع بواسطة فريق من طلاب هندسة البرمجيات، بهدف تحويل المفاهيم البرمجية والأكاديمية إلى نظام حقيقي قابل للاستخدام والتطوير."
                );

        teamDescription.setWrapText(
                true
        );

        teamDescription.setMaxWidth(
                680
        );

        teamDescription.setAlignment(
                Pos.CENTER
        );

        teamDescription.setTextAlignment(
                javafx.scene.text.TextAlignment.CENTER
        );

        teamDescription.getStyleClass().add(
                "fas-home-section-description"
        );

        teamSection
                .getChildren()
                .addAll(
                        teamTitle,
                        teamDescription
                );

        // =================================================
        // CLOSING MESSAGE
        // =================================================

        Label closingMessage =
                new Label(
                        "نسعى إلى بناء برمجيات بسيطة، عملية، وقابلة للتطور."
                );

        closingMessage.getStyleClass().add(
                "fas-home-closing-text"
        );

        // =================================================
        // ADD ALL
        // =================================================

        root.getChildren().addAll(

                logo,

                logoSubtitle,

                divider,

                welcomeSection,

                aboutSection,

                teamSection,

                closingMessage
        );

        return root;
    }

    // =====================================================
    // CLIENT LIST
    // =====================================================

    public void setViewToClientList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري فتح إدارة العملاء...",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/ClientList.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () ->
                        displayView(
                                rootHolder[0]
                        )
        );
    }

    // =====================================================
    // MEALS
    // =====================================================

    public void setViewToMealsList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري تحميل قائمة الوجبات...",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/Meals.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () ->
                        displayView(
                                rootHolder[0]
                        )
        );
    }

    // =====================================================
    // SESSIONS
    // =====================================================

    public void setViewToSessionList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري تحميل إدارة الجلسات...",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/Session.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () ->
                        displayView(
                                rootHolder[0]
                        )
        );
    }

    // =====================================================
    // HEALTH
    // =====================================================

    public void setViewToHealthList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري فتح واجهة الأمراض والحساسية...",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/HealthList.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () ->
                        displayView(
                                rootHolder[0]
                        )
        );
    }

    // =====================================================
    // REPORTS
    // =====================================================

    public void setViewToReport() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري فتح لوحة التقارير...",

                () -> {

                    rootHolder[0] =
                            new ReportsDashboardView();

                    return true;
                },

                () ->
                        displayView(
                                rootHolder[0]
                        )
        );
    }

    // =====================================================
    // DISPLAY
    // =====================================================

    private void displayView(
            Parent root
    ) {

        if (root == null
                || centerContentHolder == null) {

            return;
        }

        centerContentHolder
                .getChildren()
                .clear();

        centerContentHolder
                .getChildren()
                .add(
                        root
                );

        AnchorPane.setTopAnchor(
                root,
                0.0
        );

        AnchorPane.setBottomAnchor(
                root,
                0.0
        );

        AnchorPane.setLeftAnchor(
                root,
                0.0
        );

        AnchorPane.setRightAnchor(
                root,
                0.0
        );

        if (root instanceof Region region) {

            region.setMaxWidth(
                    Double.MAX_VALUE
            );

            region.setMaxHeight(
                    Double.MAX_VALUE
            );
        }
    }
}