package user_interface.subscription;

import api.ClientApiManager;
import api.SubscriptionAPI;
import api.SubscriptionPlanAPI;
import api.SubscriptionRequestAPI;

import dto.SubscriptionPlanResponse;
import dto.SubscriptionRequestCreateRequest;
import dto.SubscriptionRequestResponse;
import dto.SubscriptionResponse;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import runner.BackgroundRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SubscriptionView extends BorderPane {

    // =====================================================
    // APIs
    // =====================================================

    private final SubscriptionAPI subscriptionAPI;
    private final SubscriptionRequestAPI requestAPI;
    private final SubscriptionPlanAPI planAPI;

    // =====================================================
    // DATA
    // =====================================================

    private SubscriptionResponse currentSubscription;

    private final ObservableList<SubscriptionRequestResponse>
            requests =
            FXCollections.observableArrayList();

    private final ObservableList<SubscriptionPlanResponse>
            availablePlans =
            FXCollections.observableArrayList();

    // =====================================================
    // UI - HEADER
    // =====================================================

    private final Label pageTitle =
            new Label("إدارة الاشتراك");

    private final Label pageSubtitle =
            new Label(
                    "إدارة اشتراك العيادة وطلبات الاشتراك والتجديد"
            );

    private final Label subscriptionStatusBadge =
            new Label("غير مشترك");

    // =====================================================
    // UI - SUBSCRIPTION
    // =====================================================

    private final Label planValue =
            new Label("-");

    private final Label priceValue =
            new Label("-");

    private final Label startValue =
            new Label("-");

    private final Label endValue =
            new Label("-");

    private final Label statusValue =
            new Label("-");

    private final Label remainingValue =
            new Label("-");

    private final Label subscriptionMessage =
            new Label();

    private final VBox subscriptionStatusCard =
            new VBox();

    // =====================================================
    // UI - REQUESTS
    // =====================================================

    private final TableView<SubscriptionRequestResponse>
            requestTable =
            new TableView<>();

    // =====================================================
    // BUTTONS
    // =====================================================

    private final Button newRequestButton =
            new Button("➕ طلب اشتراك");

    private final Button renewButton =
            new Button("🔄 طلب تجديد");

    private final Button refreshButton =
            new Button("🔃 تحديث");

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SubscriptionView() {

        ClientApiManager apiManager =
                ClientApiManager.getInstance();

        subscriptionAPI =
                apiManager.getSubscriptionAPI();

        requestAPI =
                apiManager.getSubscriptionRequestAPI();

        planAPI =
                apiManager.getSubscriptionPlanAPI();

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        getStyleClass().add(
                "fas-root"
        );

        setPadding(
                new Insets(8)
        );

        configureGeneralUI();
        configureEvents();

        updateNoSubscriptionState();

        loadData();
    }

    // =====================================================
    // GENERAL UI
    // =====================================================

    private void configureGeneralUI() {

        VBox content =
                new VBox(18);

        content.setFillWidth(
                true
        );

        content.setPadding(
                new Insets(
                        8,
                        10,
                        18,
                        10
                )
        );

        Node header =
                createPageHeader();

        Node subscription =
                createSubscriptionCard();

        Node requests =
                createRequestsSection();

        content.getChildren().addAll(
                header,
                subscription,
                requests
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        content
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.getStyleClass().add(
                "fas-scroll"
        );

        setCenter(
                scrollPane
        );
    }

    // =====================================================
    // PAGE HEADER
    // =====================================================

    private Node createPageHeader() {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        header.getStyleClass().add(
                "fas-header"
        );

        VBox titleBox =
                new VBox(4);

        titleBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        pageTitle.getStyleClass().add(
                "fas-page-title"
        );

        pageSubtitle.getStyleClass().add(
                "fas-page-subtitle"
        );

        titleBox.getChildren().addAll(
                pageTitle,
                pageSubtitle
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        subscriptionStatusBadge
                .getStyleClass()
                .add(
                        "fas-status-warning"
                );

        subscriptionStatusBadge
                .setPadding(
                        new Insets(
                                8,
                                12,
                                8,
                                12
                        )
                );

        header.getChildren().addAll(
                titleBox,
                spacer,
                subscriptionStatusBadge
        );

        return header;
    }

    // =====================================================
    // SUBSCRIPTION CARD
    // =====================================================

    private Node createSubscriptionCard() {

        VBox card =
                new VBox(16);

        card.getStyleClass().add(
                "fas-card"
        );

        HBox header =
                createSectionHeader(
                        "الاشتراك الحالي",
                        "تفاصيل الخطة وحالة الاشتراك الحالية"
                );

        GridPane grid =
                createSubscriptionGrid();

        subscriptionMessage.setWrapText(
                true
        );

        subscriptionMessage
                .getStyleClass()
                .add(
                        "fas-card-subtitle"
                );

        HBox actions =
                createActionsBar();

        VBox messageBox =
                new VBox(
                        subscriptionMessage
                );

        messageBox.getStyleClass().add(
                "fas-inner-info"
        );

        card.getChildren().addAll(
                header,
                createSeparator(),
                grid,
                messageBox,
                actions
        );

        subscriptionStatusCard
                .getChildren()
                .setAll(
                        card
                );

        return card;
    }

    // =====================================================
    // SUBSCRIPTION GRID
    // =====================================================

    private GridPane createSubscriptionGrid() {

        GridPane grid =
                new GridPane();

        grid.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        grid.setHgap(
                14
        );

        grid.setVgap(
                14
        );

        grid.setMaxWidth(
                Double.MAX_VALUE
        );

        configureSubscriptionColumns(
                grid
        );

        addInfoCard(
                grid,
                0,
                0,
                "الخطة",
                planValue,
                "fas-section-blue"
        );

        addInfoCard(
                grid,
                0,
                1,
                "السعر",
                priceValue,
                "fas-section-cyan"
        );

        addInfoCard(
                grid,
                1,
                0,
                "تاريخ البداية",
                startValue,
                "fas-section-green"
        );

        addInfoCard(
                grid,
                1,
                1,
                "تاريخ النهاية",
                endValue,
                "fas-section-orange"
        );

        addInfoCard(
                grid,
                2,
                0,
                "الحالة",
                statusValue,
                "fas-section-purple"
        );

        addInfoCard(
                grid,
                2,
                1,
                "المدة المتبقية",
                remainingValue,
                "fas-section-teal"
        );

        return grid;
    }

    private void configureSubscriptionColumns(
            GridPane grid
    ) {

        ColumnConstraints first =
                new ColumnConstraints();

        first.setHgrow(
                Priority.ALWAYS
        );

        first.setFillWidth(
                true
        );

        ColumnConstraints second =
                new ColumnConstraints();

        second.setHgrow(
                Priority.ALWAYS
        );

        second.setFillWidth(
                true
        );

        grid.getColumnConstraints()
                .addAll(
                        first,
                        second
                );
    }

    // =====================================================
    // INFO CARD
    // =====================================================

    private void addInfoCard(
            GridPane grid,
            int column,
            int row,
            String title,
            Label value,
            String styleClass
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(
                        13
                )
        );

        card.getStyleClass().add(
                styleClass
        );

        card.getStyleClass().add(
                "fas-status-card"
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.getStyleClass().add(
                "fas-card-subtitle"
        );

        value.getStyleClass().add(
                "fas-card-title"
        );

        card.getChildren().addAll(
                titleLabel,
                value
        );

        grid.add(
                card,
                column,
                row
        );

        GridPane.setHgrow(
                card,
                Priority.ALWAYS
        );
    }

    // =====================================================
    // ACTIONS
    // =====================================================

    private HBox createActionsBar() {

        HBox actions =
                new HBox(10);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        newRequestButton
                .getStyleClass()
                .add(
                        "fas-primary-button"
                );

        renewButton
                .getStyleClass()
                .add(
                        "fas-section-purple"
                );

        refreshButton
                .getStyleClass()
                .add(
                        "fas-secondary-button"
                );

        newRequestButton.setMinHeight(
                42
        );

        renewButton.setMinHeight(
                42
        );

        refreshButton.setMinHeight(
                42
        );

        actions.getChildren().addAll(
                newRequestButton,
                renewButton,
                refreshButton
        );

        return actions;
    }

    // =====================================================
    // SECTION HEADER
    // =====================================================

    private HBox createSectionHeader(
            String title,
            String subtitle
    ) {

        HBox header =
                new HBox(12);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox texts =
                new VBox(3);

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitleLabel =
                new Label(
                        subtitle
                );

        subtitleLabel.getStyleClass().add(
                "fas-card-subtitle"
        );

        texts.getChildren().addAll(
                titleLabel,
                subtitleLabel
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        header.getChildren().addAll(
                texts,
                spacer
        );

        return header;
    }

    // =====================================================
    // REQUESTS SECTION
    // =====================================================

    private Node createRequestsSection() {

        VBox card =
                new VBox(14);

        card.getStyleClass().add(
                "fas-card"
        );

        HBox header =
                createSectionHeader(
                        "طلبات الاشتراك السابقة",
                        "سجل جميع طلبات الاشتراك والتجديد"
                );

        createRequestTable();

        card.getChildren().addAll(
                header,
                createSeparator(),
                requestTable
        );

        VBox.setVgrow(
                requestTable,
                Priority.ALWAYS
        );

        return card;
    }

    // =====================================================
    // REQUEST TABLE
    // =====================================================

    private void createRequestTable() {

        TableColumn<
                SubscriptionRequestResponse,
                String
                > typeColumn =
                new TableColumn<>(
                        "نوع الطلب"
                );

        TableColumn<
                SubscriptionRequestResponse,
                String
                > dateColumn =
                new TableColumn<>(
                        "تاريخ الطلب"
                );

        TableColumn<
                SubscriptionRequestResponse,
                String
                > statusColumn =
                new TableColumn<>(
                        "الحالة"
                );

        TableColumn<
                SubscriptionRequestResponse,
                String
                > ownerNotesColumn =
                new TableColumn<>(
                        "ملاحظات المالك"
                );

        TableColumn<
                SubscriptionRequestResponse,
                String
                > adminNotesColumn =
                new TableColumn<>(
                        "رد الإدارة"
                );

        typeColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                translateRequestType(
                                        data.getValue()
                                                .getRequestType()
                                )
                        )
        );

        dateColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                formatDateTime(
                                        data.getValue()
                                                .getRequestedAt()
                                )
                        )
        );

        statusColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                translateRequestStatus(
                                        data.getValue()
                                                .getStatus()
                                )
                        )
        );

        ownerNotesColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                safe(
                                        data.getValue()
                                                .getOwnerNotes()
                                )
                        )
        );

        adminNotesColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                safe(
                                        data.getValue()
                                                .getAdminNotes()
                                )
                        )
        );

        requestTable
                .getColumns()
                .setAll(
                        typeColumn,
                        dateColumn,
                        statusColumn,
                        ownerNotesColumn,
                        adminNotesColumn
                );

        requestTable.setItems(
                requests
        );

        requestTable.setPrefHeight(
                300
        );

        requestTable.setMinHeight(
                220
        );

        requestTable.setColumnResizePolicy(
                TableView
                        .CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        requestTable.setPlaceholder(
                new Label(
                        "لا توجد طلبات اشتراك سابقة."
                )
        );

        requestTable.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        requestTable.getStyleClass().add(
                "fas-subscription-table"
        );
    }

    // =====================================================
    // EVENTS
    // =====================================================

    private void configureEvents() {

        refreshButton.setOnAction(
                event ->
                        loadData()
        );

        newRequestButton.setOnAction(
                event ->
                        showNewRequestDialog()
        );

        renewButton.setOnAction(
                event ->
                        showRenewRequestDialog()
        );
    }

    // =====================================================
    // LOAD DATA
    // =====================================================

    private void loadData() {

        setLoadingState();

        BackgroundRunner.runAuthenticated(
                "جاري تحميل بيانات الاشتراك...",

                () -> {

                    SubscriptionResponse
                            loadedSubscription =
                            subscriptionAPI.getCurrent();

                    List<SubscriptionRequestResponse>
                            loadedRequests =
                            requestAPI.getMyRequests();

                    List<SubscriptionPlanResponse>
                            loadedPlans =
                            planAPI.getActive();

                    Platform.runLater(
                            () -> {

                                currentSubscription =
                                        loadedSubscription;

                                availablePlans.clear();

                                if (loadedPlans != null) {

                                    availablePlans.addAll(
                                            loadedPlans
                                    );
                                }

                                requests.setAll(
                                        loadedRequests == null
                                                ? List.of()
                                                : loadedRequests
                                );

                                applySubscriptionData();
                            }
                    );

                    return true;
                }
        );
    }

    // =====================================================
    // LOADING STATE
    // =====================================================

    private void setLoadingState() {

        planValue.setText(
                "جاري التحميل..."
        );

        priceValue.setText(
                "..."
        );

        startValue.setText(
                "..."
        );

        endValue.setText(
                "..."
        );

        statusValue.setText(
                "..."
        );

        remainingValue.setText(
                "..."
        );

        subscriptionMessage.setText(
                "جاري تحميل بيانات الاشتراك..."
        );

        refreshButton.setDisable(
                true
        );
    }

    // =====================================================
    // APPLY SUBSCRIPTION
    // =====================================================

    private void applySubscriptionData() {

        refreshButton.setDisable(
                false
        );

        if (currentSubscription == null) {

            updateNoSubscriptionState();

            return;
        }

        planValue.setText(
                safe(
                        currentSubscription
                                .getPlanName()
                )
        );

        priceValue.setText(
                formatPrice(
                        currentSubscription.getPrice(),
                        currentSubscription
                                .getCurrencyCode()
                )
        );

        startValue.setText(
                formatDate(
                        currentSubscription
                                .getStartDate()
                )
        );

        endValue.setText(
                formatDate(
                        currentSubscription
                                .getEndDate()
                )
        );

        String translatedStatus =
                translateSubscriptionStatus(
                        currentSubscription
                                .getStatus()
                );

        statusValue.setText(
                translatedStatus
        );

        remainingValue.setText(
                calculateRemainingDays(
                        currentSubscription.getStartDate(),
                        currentSubscription.getEndDate(),
                        currentSubscription.getStatus()
                )
        );

        subscriptionMessage.setText(
                getSubscriptionMessage(
                        currentSubscription
                )
        );

        updateSubscriptionStatusBadge(
                currentSubscription.getStatus()
        );

        boolean active =
                "ACTIVE".equalsIgnoreCase(
                        currentSubscription
                                .getStatus()
                );

        newRequestButton.setDisable(
                active
        );

        renewButton.setDisable(
                false
        );
    }

    // =====================================================
    // STATUS BADGE
    // =====================================================

    private void updateSubscriptionStatusBadge(
            String status
    ) {

        subscriptionStatusBadge
                .getStyleClass()
                .removeAll(
                        "fas-status-success",
                        "fas-status-warning",
                        "fas-status-danger",
                        "fas-status-info",
                        "fas-status-purple"
                );

        if (status == null) {

            subscriptionStatusBadge
                    .setText(
                            "غير معروف"
                    );

            subscriptionStatusBadge
                    .getStyleClass()
                    .add(
                            "fas-status-warning"
                    );

            return;
        }

        switch (
                status.toUpperCase()
        ) {

            case "ACTIVE" -> {

                subscriptionStatusBadge
                        .setText(
                                "● نشط"
                        );

                subscriptionStatusBadge
                        .getStyleClass()
                        .add(
                                "fas-status-success"
                        );
            }

            case "PENDING" -> {

                subscriptionStatusBadge
                        .setText(
                                "● قيد الانتظار"
                        );

                subscriptionStatusBadge
                        .getStyleClass()
                        .add(
                                "fas-status-warning"
                        );
            }

            case "EXPIRED" -> {

                subscriptionStatusBadge
                        .setText(
                                "● منتهي"
                        );

                subscriptionStatusBadge
                        .getStyleClass()
                        .add(
                                "fas-status-danger"
                        );
            }

            case "SUSPENDED" -> {

                subscriptionStatusBadge
                        .setText(
                                "● موقوف"
                        );

                subscriptionStatusBadge
                        .getStyleClass()
                        .add(
                                "fas-status-purple"
                        );
            }

            case "CANCELLED" -> {

                subscriptionStatusBadge
                        .setText(
                                "● ملغى"
                        );

                subscriptionStatusBadge
                        .getStyleClass()
                        .add(
                                "fas-status-danger"
                        );
            }

            default -> {

                subscriptionStatusBadge
                        .setText(
                                "● "
                                        + translateSubscriptionStatus(
                                        status
                                )
                        );

                subscriptionStatusBadge
                        .getStyleClass()
                        .add(
                                "fas-status-info"
                        );
            }
        }
    }

    // =====================================================
    // NO SUBSCRIPTION
    // =====================================================

    private void updateNoSubscriptionState() {

        refreshButton.setDisable(
                false
        );

        planValue.setText(
                "لا يوجد اشتراك"
        );

        priceValue.setText(
                "-"
        );

        startValue.setText(
                "-"
        );

        endValue.setText(
                "-"
        );

        statusValue.setText(
                "غير مشترك"
        );

        remainingValue.setText(
                "-"
        );

        subscriptionMessage.setText(
                "لا يوجد اشتراك حالي. اختر إحدى الخطط المتاحة وأرسل طلب اشتراك."
        );

        updateSubscriptionStatusBadge(
                null
        );

        subscriptionStatusBadge
                .setText(
                        "غير مشترك"
                );

        subscriptionStatusBadge
                .getStyleClass()
                .removeAll(
                        "fas-status-success",
                        "fas-status-warning",
                        "fas-status-danger",
                        "fas-status-info",
                        "fas-status-purple"
                );

        subscriptionStatusBadge
                .getStyleClass()
                .add(
                        "fas-status-info"
                );

        newRequestButton.setDisable(
                false
        );

        renewButton.setDisable(
                true
        );
    }

    // =====================================================
    // NEW REQUEST
    // =====================================================

    private void showNewRequestDialog() {

        if (availablePlans.isEmpty()) {

            showInfo(
                    "لا توجد خطط",
                    "لا توجد خطط اشتراك نشطة متاحة حاليًا."
            );

            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "طلب اشتراك جديد"
        );

        dialog.setHeaderText(
                "اختر خطة الاشتراك المناسبة"
        );

        VBox content =
                new VBox(14);

        content.setPadding(
                new Insets(20)
        );

        content.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        content.getStyleClass().add(
                "fas-root"
        );

        ListView<SubscriptionPlanResponse>
                planList =
                new ListView<>();

        planList.setItems(
                availablePlans
        );

        planList.setPrefHeight(
                330
        );

        planList.getStyleClass().add(
                "fas-subscription-plan-list"
        );

        planList.setCellFactory(
                list ->
                        new ListCell<>() {

                            private final VBox box =
                                    new VBox(5);

                            private final Label name =
                                    new Label();

                            private final Label description =
                                    new Label();

                            private final Label price =
                                    new Label();

                            private final Label duration =
                                    new Label();

                            {
                                box.setPadding(
                                        new Insets(12)
                                );

                                name.getStyleClass().add(
                                        "fas-card-title"
                                );

                                description.getStyleClass().add(
                                        "fas-card-subtitle"
                                );

                                description.setWrapText(
                                        true
                                );

                                price.getStyleClass().add(
                                        "fas-field-label-required"
                                );

                                duration.getStyleClass().add(
                                        "fas-card-subtitle"
                                );

                                box.getChildren().addAll(
                                        name,
                                        description,
                                        price,
                                        duration
                                );
                            }

                            @Override
                            protected void updateItem(
                                    SubscriptionPlanResponse plan,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        plan,
                                        empty
                                );

                                if (empty ||
                                        plan == null) {

                                    setGraphic(
                                            null
                                    );

                                    return;
                                }

                                name.setText(
                                        safe(
                                                plan.getPlanName()
                                        )
                                );

                                description.setText(
                                        safe(
                                                plan.getDescription()
                                        )
                                );

                                price.setText(
                                        "السعر: "
                                                + formatPrice(
                                                plan.getPrice(),
                                                plan.getCurrencyCode()
                                        )
                                );

                                duration.setText(
                                        plan.getDurationDays() == null
                                                ? "المدة: غير محددة"
                                                : "المدة: "
                                                + plan.getDurationDays()
                                                + " يوم"
                                );

                                setGraphic(
                                        box
                                );
                            }
                        }
        );

        Label plansLabel =
                new Label(
                        "الخطط المتاحة"
                );

        plansLabel.getStyleClass().add(
                "fas-field-label"
        );

        Label hint =
                new Label(
                        "سيتم إرسال الخطة التي تختارها إلى مدير النظام للمراجعة."
                );

        hint.getStyleClass().add(
                "fas-card-subtitle"
        );

        hint.setWrapText(
                true
        );

        TextArea notes =
                new TextArea();

        notes.setPromptText(
                "ملاحظات للمدير (اختياري)"
        );

        notes.setWrapText(
                true
        );

        notes.setPrefRowCount(
                4
        );

        notes.getStyleClass().add(
                "fas-session-input"
        );

        Label notesLabel =
                new Label(
                        "الملاحظات"
                );

        notesLabel.getStyleClass().add(
                "fas-field-label"
        );

        content.getChildren().addAll(
                plansLabel,
                planList,
                hint,
                notesLabel,
                notes
        );

        dialog.getDialogPane()
                .setContent(
                        content
                );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        dialog.getDialogPane()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        "/css/fas.css"
                                )
                                .toExternalForm()
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );

        dialog.showAndWait()
                .ifPresent(
                        result -> {

                            if (result != ButtonType.OK) {
                                return;
                            }

                            SubscriptionPlanResponse
                                    selectedPlan =
                                    planList
                                            .getSelectionModel()
                                            .getSelectedItem();

                            if (selectedPlan == null) {

                                showError(
                                        "اختيار الخطة",
                                        "يرجى اختيار خطة اشتراك."
                                );

                                return;
                            }

                            sendRequest(
                                    selectedPlan.getPlanId(),
                                    "NEW",
                                    notes.getText()
                            );
                        }
                );
    }

    // =====================================================
    // RENEW REQUEST
    // =====================================================

    private void showRenewRequestDialog() {

        if (currentSubscription == null) {

            showInfo(
                    "لا يوجد اشتراك",
                    "لا يوجد اشتراك حالي. أرسل طلب اشتراك جديد."
            );

            return;
        }

        Long planId =
                currentSubscription.getPlanId();

        if (planId == null) {

            showError(
                    "بيانات الاشتراك",
                    "تعذر تحديد الخطة الحالية."
            );

            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "طلب تجديد الاشتراك"
        );

        dialog.setHeaderText(
                "تجديد الاشتراك الحالي"
        );

        VBox content =
                new VBox(14);

        content.setPadding(
                new Insets(20)
        );

        content.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        content.getStyleClass().add(
                "fas-root"
        );

        Label plan =
                new Label(
                        "الخطة الحالية: "
                                + safe(
                                currentSubscription
                                        .getPlanName()
                        )
                );

        plan.getStyleClass().add(
                "fas-card-title"
        );

        Label price =
                new Label(
                        "السعر: "
                                + formatPrice(
                                currentSubscription.getPrice(),
                                currentSubscription.getCurrencyCode()
                        )
                );

        price.getStyleClass().add(
                "fas-field-label-required"
        );

        Label duration =
                new Label(
                        currentSubscription
                                .getDurationDays()
                                == null
                                ? "المدة: غير محددة"
                                : "المدة: "
                                + currentSubscription
                                .getDurationDays()
                                + " يوم"
                );

        duration.getStyleClass().add(
                "fas-card-subtitle"
        );

        Label notesLabel =
                new Label(
                        "الملاحظات"
                );

        notesLabel.getStyleClass().add(
                "fas-field-label"
        );

        TextArea notes =
                new TextArea();

        notes.setPromptText(
                "ملاحظات للمدير (اختياري)"
        );

        notes.setWrapText(
                true
        );

        notes.setPrefRowCount(
                4
        );

        notes.getStyleClass().add(
                "fas-session-input"
        );

        content.getChildren().addAll(
                plan,
                price,
                duration,
                notesLabel,
                notes
        );

        dialog.getDialogPane()
                .setContent(
                        content
                );

        dialog.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        dialog.getDialogPane()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        "/css/fas.css"
                                )
                                .toExternalForm()
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );

        dialog.showAndWait()
                .ifPresent(
                        result -> {

                            if (result != ButtonType.OK) {
                                return;
                            }

                            sendRequest(
                                    planId,
                                    "RENEW",
                                    notes.getText()
                            );
                        }
                );
    }

    // =====================================================
    // SEND REQUEST
    // =====================================================

    private void sendRequest(
            Long planId,
            String requestType,
            String ownerNotes
    ) {

        if (planId == null ||
                planId <= 0) {

            showError(
                    "الخطة",
                    "الخطة المحددة غير صالحة."
            );

            return;
        }

        SubscriptionRequestCreateRequest request =
                new SubscriptionRequestCreateRequest();

        request.setPlanId(
                planId
        );

        request.setRequestType(
                requestType
        );

        request.setOwnerNotes(
                clean(
                        ownerNotes
                )
        );

        BackgroundRunner.runAuthenticated(
                "جاري إرسال طلب الاشتراك...",

                () -> {

                    requestAPI.createRequest(
                            request
                    );

                    return true;
                },

                () -> {

                    showInfo(
                            "تم إرسال الطلب",
                            "تم إرسال طلب الاشتراك إلى مدير النظام بنجاح."
                    );

                    loadData();
                }
        );
    }

    // =====================================================
    // REMAINING DAYS
    // =====================================================

    private String calculateRemainingDays(
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {

        if (endDate == null) {

            return "-";
        }

        if ("EXPIRED".equalsIgnoreCase(
                status
        )) {

            return "منتهي";
        }

        LocalDate today =
                LocalDate.now();

        if (startDate != null &&
                today.isBefore(
                        startDate
                )) {

            return "لم يبدأ بعد";
        }

        if (today.isAfter(
                endDate
        )) {

            return "منتهي";
        }

        long days =
                ChronoUnit.DAYS.between(
                        today,
                        endDate
                );

        return days + " يوم";
    }

    // =====================================================
    // SUBSCRIPTION MESSAGE
    // =====================================================

    private String getSubscriptionMessage(
            SubscriptionResponse subscription
    ) {

        if (subscription == null) {

            return "لا يوجد اشتراك.";
        }

        String status =
                subscription.getStatus();

        if ("ACTIVE".equalsIgnoreCase(
                status
        )) {

            return "الاشتراك فعال ويمكنك استخدام النظام.";
        }

        if ("SUSPENDED".equalsIgnoreCase(
                status
        )) {

            return "تم تعليق الاشتراك. يرجى التواصل مع مدير النظام.";
        }

        if ("EXPIRED".equalsIgnoreCase(
                status
        )) {

            return "انتهى الاشتراك. يمكنك إرسال طلب تجديد.";
        }

        if ("PENDING".equalsIgnoreCase(
                status
        )) {

            return "طلب الاشتراك قيد المراجعة من مدير النظام.";
        }

        return "حالة الاشتراك: "
                + translateSubscriptionStatus(
                status
        );
    }

    // =====================================================
    // SUBSCRIPTION STATUS
    // =====================================================

    private String translateSubscriptionStatus(
            String status
    ) {

        if (status == null) {
            return "-";
        }

        return switch (
                status.toUpperCase()
                ) {

            case "ACTIVE" ->
                    "نشط";

            case "PENDING" ->
                    "قيد الانتظار";

            case "EXPIRED" ->
                    "منتهي";

            case "SUSPENDED" ->
                    "موقوف";

            case "CANCELLED" ->
                    "ملغى";

            default ->
                    status;
        };
    }

    // =====================================================
    // REQUEST TYPE
    // =====================================================

    private String translateRequestType(
            String type
    ) {

        if (type == null) {
            return "-";
        }

        return switch (
                type.toUpperCase()
                ) {

            case "NEW" ->
                    "اشتراك جديد";

            case "RENEW" ->
                    "تجديد";

            default ->
                    type;
        };
    }

    // =====================================================
    // REQUEST STATUS
    // =====================================================

    private String translateRequestStatus(
            String status
    ) {

        if (status == null) {
            return "-";
        }

        return switch (
                status.toUpperCase()
                ) {

            case "PENDING" ->
                    "قيد المراجعة";

            case "APPROVED" ->
                    "مقبول";

            case "REJECTED" ->
                    "مرفوض";

            case "CANCELLED" ->
                    "ملغى";

            default ->
                    status;
        };
    }

    // =====================================================
    // PRICE
    // =====================================================

    private String formatPrice(
            BigDecimal price,
            String currency
    ) {

        if (price == null) {

            return "-";
        }

        String value =
                price.stripTrailingZeros()
                        .toPlainString();

        if (currency == null ||
                currency.isBlank()) {

            return value;
        }

        return value
                + " "
                + currency;
    }

    // =====================================================
    // DATE
    // =====================================================

    private String formatDate(
            LocalDate date
    ) {

        if (date == null) {

            return "-";
        }

        return String.format(
                "%02d/%02d/%04d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear()
        );
    }

    // =====================================================
    // DATE TIME
    // =====================================================

    private String formatDateTime(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {

            return "-";
        }

        return String.format(
                "%02d/%02d/%04d %02d:%02d",
                dateTime.getDayOfMonth(),
                dateTime.getMonthValue(),
                dateTime.getYear(),
                dateTime.getHour(),
                dateTime.getMinute()
        );
    }

    // =====================================================
    // CLEAN
    // =====================================================

    private String clean(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String result =
                value.trim();

        return result.isBlank()
                ? null
                : result;
    }

    // =====================================================
    // SAFE
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    // =====================================================
    // SEPARATOR
    // =====================================================

    private Separator createSeparator() {

        Separator separator =
                new Separator();

        separator.getStyleClass().add(
                "fas-inner-separator"
        );

        return separator;
    }

    // =====================================================
    // INFORMATION ALERT
    // =====================================================

    private void showInfo(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        addAlertStyle(
                alert
        );

        alert.showAndWait();
    }

    // =====================================================
    // ERROR ALERT
    // =====================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message == null ||
                        message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        addAlertStyle(
                alert
        );

        alert.showAndWait();
    }

    private void addAlertStyle(
            Alert alert
    ) {

        alert.getDialogPane()
                .getStyleClass()
                .add(
                        "fas-alert"
                );

        String css =
                getClass()
                        .getResource(
                                "/css/fas.css"
                        )
                        .toExternalForm();

        if (!alert.getDialogPane()
                .getStylesheets()
                .contains(css)) {

            alert.getDialogPane()
                    .getStylesheets()
                    .add(css);
        }
    }
}