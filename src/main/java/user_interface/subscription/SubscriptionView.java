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
    // Data
    // =====================================================

    private SubscriptionResponse currentSubscription;

    private final ObservableList<SubscriptionRequestResponse>
            requests =
            FXCollections.observableArrayList();

    private final ObservableList<SubscriptionPlanResponse>
            availablePlans =
            FXCollections.observableArrayList();

    // =====================================================
    // UI
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

    private final TableView<SubscriptionRequestResponse>
            requestTable =
            new TableView<>();

    private final Button newRequestButton =
            new Button("➕ طلب اشتراك");

    private final Button renewButton =
            new Button("🔄 طلب تجديد");

    private final Button refreshButton =
            new Button("🔃 تحديث");

    // =====================================================
    // Constructor
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

        setPadding(
                new Insets(25)
        );

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        // =================================================
        // Header
        // =================================================

        Label title =
                new Label(
                        "إدارة الاشتراك"
                );

        title.setStyle("""
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            -fx-text-fill: #1f2937;
        """);

        Label subtitle =
                new Label(
                        "عرض اشتراك العيادة وطلب الاشتراك أو التجديد."
                );

        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6b7280;
        """);

        VBox header =
                new VBox(
                        5,
                        title,
                        subtitle
                );

        // =================================================
        // Subscription Card
        // =================================================

        VBox subscriptionCard =
                createSubscriptionCard();

        // =================================================
        // Requests
        // =================================================

        VBox requestsSection =
                createRequestsSection();

        // =================================================
        // Main Content
        // =================================================

        VBox content =
                new VBox(
                        25,
                        header,
                        subscriptionCard,
                        requestsSection
                );

        content.setFillWidth(
                true
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

        scrollPane.setStyle(
                "-fx-background-color: transparent;"
        );

        setCenter(
                scrollPane
        );

        // =================================================
        // Events
        // =================================================

        refreshButton.setOnAction(
                e -> loadData()
        );

        newRequestButton.setOnAction(
                e -> showNewRequestDialog()
        );

        renewButton.setOnAction(
                e -> showRenewRequestDialog()
        );

        // =================================================
        // Initial State
        // =================================================

        updateNoSubscriptionState();

        loadData();
    }

    // =====================================================
    // Subscription Card
    // =====================================================

    private VBox createSubscriptionCard() {

        Label title =
                new Label(
                        "الاشتراك الحالي"
                );

        title.setStyle("""
            -fx-font-size: 19px;
            -fx-font-weight: bold;
            -fx-text-fill: #1f2937;
        """);

        GridPane grid =
                new GridPane();

        grid.setHgap(
                35
        );

        grid.setVgap(
                15
        );

        grid.setPadding(
                new Insets(
                        10,
                        0,
                        10,
                        0
                )
        );

        addInfoRow(
                grid,
                0,
                0,
                "الخطة",
                planValue
        );

        addInfoRow(
                grid,
                0,
                1,
                "السعر",
                priceValue
        );

        addInfoRow(
                grid,
                0,
                2,
                "تاريخ البداية",
                startValue
        );

        addInfoRow(
                grid,
                0,
                3,
                "تاريخ النهاية",
                endValue
        );

        addInfoRow(
                grid,
                1,
                0,
                "الحالة",
                statusValue
        );

        addInfoRow(
                grid,
                1,
                1,
                "المدة المتبقية",
                remainingValue
        );

        subscriptionMessage.setWrapText(
                true
        );

        subscriptionMessage.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #6b7280;
        """);

        newRequestButton.setPrefHeight(
                38
        );

        renewButton.setPrefHeight(
                38
        );

        refreshButton.setPrefHeight(
                38
        );

        HBox actions =
                new HBox(
                        10,
                        newRequestButton,
                        renewButton,
                        refreshButton
                );

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox card =
                new VBox(
                        15,
                        title,
                        grid,
                        subscriptionMessage,
                        actions
                );

        card.setPadding(
                new Insets(20)
        );

        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 14px;
            -fx-border-color: #e5e7eb;
            -fx-border-radius: 14px;
        """);

        return card;
    }

    // =====================================================
    // Info Row
    // =====================================================

    private void addInfoRow(
            GridPane grid,
            int column,
            int row,
            String title,
            Label value
    ) {

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #6b7280;
            -fx-font-weight: bold;
        """);

        value.setStyle("""
            -fx-font-size: 15px;
            -fx-text-fill: #111827;
            -fx-font-weight: bold;
        """);

        VBox box =
                new VBox(
                        3,
                        titleLabel,
                        value
                );

        box.setMinWidth(
                180
        );

        grid.add(
                box,
                column,
                row
        );
    }

    // =====================================================
    // Requests Section
    // =====================================================

    private VBox createRequestsSection() {

        Label title =
                new Label(
                        "طلبات الاشتراك السابقة"
                );

        title.setStyle("""
            -fx-font-size: 19px;
            -fx-font-weight: bold;
            -fx-text-fill: #1f2937;
        """);

        createRequestTable();

        VBox box =
                new VBox(
                        12,
                        title,
                        requestTable
                );

        box.setFillWidth(
                true
        );

        return box;
    }

    // =====================================================
    // Request Table
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
                280
        );

        requestTable.setColumnResizePolicy(
                TableView
                        .CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        requestTable.setPlaceholder(
                new Label(
                        "لا توجد طلبات اشتراك."
                )
        );
    }

    // =====================================================
    // تحميل البيانات
    // =====================================================

    private void loadData() {

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
    // تطبيق بيانات الاشتراك
    // =====================================================

    private void applySubscriptionData() {

        if (currentSubscription == null) {

            updateNoSubscriptionState();

            return;
        }

        planValue.setText(
                safe(
                        currentSubscription.getPlanName()
                )
        );

        priceValue.setText(
                formatPrice(
                        currentSubscription.getPrice(),
                        currentSubscription.getCurrencyCode()
                )
        );

        startValue.setText(
                formatDate(
                        currentSubscription.getStartDate()
                )
        );

        endValue.setText(
                formatDate(
                        currentSubscription.getEndDate()
                )
        );

        statusValue.setText(
                translateSubscriptionStatus(
                        currentSubscription.getStatus()
                )
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

        boolean active =
                "ACTIVE".equalsIgnoreCase(
                        currentSubscription.getStatus()
                );

        newRequestButton.setDisable(
                active
        );

        renewButton.setDisable(
                false
        );
    }

    // =====================================================
    // لا يوجد اشتراك
    // =====================================================

    private void updateNoSubscriptionState() {

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

        newRequestButton.setDisable(
                false
        );

        renewButton.setDisable(
                true
        );
    }

    // =====================================================
    // طلب اشتراك جديد
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
                new VBox(
                        15
                );

        content.setPadding(
                new Insets(20)
        );

        content.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
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

        planList.setCellFactory(
                list ->
                        new ListCell<>() {

                            private final VBox box =
                                    new VBox(
                                            6
                                    );

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
                                        new Insets(10)
                                );

                                name.setStyle("""
                                    -fx-font-size: 16px;
                                    -fx-font-weight: bold;
                                    -fx-text-fill: #1f2937;
                                """);

                                description.setWrapText(
                                        true
                                );

                                description.setStyle("""
                                    -fx-font-size: 12px;
                                    -fx-text-fill: #6b7280;
                                """);

                                price.setStyle("""
                                    -fx-font-size: 14px;
                                    -fx-font-weight: bold;
                                """);

                                duration.setStyle("""
                                    -fx-font-size: 12px;
                                    -fx-text-fill: #6b7280;
                                """);

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

        Label hint =
                new Label(
                        "سيتم إرسال الخطة التي تختارها إلى مدير النظام للمراجعة."
                );

        hint.setWrapText(
                true
        );

        hint.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #6b7280;
        """);

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

        content.getChildren().addAll(
                new Label(
                        "الخطط المتاحة"
                ),
                planList,
                hint,
                new Label(
                        "الملاحظات"
                ),
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
    // طلب التجديد
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
                new VBox(
                        15
                );

        content.setPadding(
                new Insets(20)
        );

        content.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        Label plan =
                new Label(
                        "الخطة الحالية: "
                                + safe(
                                currentSubscription
                                        .getPlanName()
                        )
                );

        plan.setStyle("""
            -fx-font-size: 15px;
            -fx-font-weight: bold;
        """);

        Label price =
                new Label(
                        "السعر: "
                                + formatPrice(
                                currentSubscription.getPrice(),
                                currentSubscription.getCurrencyCode()
                        )
                );

        Label duration =
                new Label(
                        currentSubscription.getDurationDays() == null
                                ? "المدة: غير محددة"
                                : "المدة: "
                                + currentSubscription.getDurationDays()
                                + " يوم"
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

        content.getChildren().addAll(
                plan,
                price,
                duration,
                new Label(
                        "الملاحظات"
                ),
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
    // إرسال الطلب
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
    // حساب الأيام المتبقية
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
    // رسالة الاشتراك
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

        return "حالة الاشتراك: "
                + translateSubscriptionStatus(
                status
        );
    }

    // =====================================================
    // ترجمة حالة الاشتراك
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
    // ترجمة نوع الطلب
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
    // ترجمة حالة الطلب
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
    // السعر
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
    // التاريخ
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
    // التاريخ والوقت
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
    // Clean
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
    // Safe
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    // =====================================================
    // Information Dialog
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

        alert.showAndWait();
    }

    // =====================================================
    // Error Dialog
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

        alert.showAndWait();
    }
}