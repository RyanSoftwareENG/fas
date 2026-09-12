package user_interface.forminput;

import api.ClientAPI;
import api.ClientApiManager;
import entities.*;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import runner.BackgroundRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static user_interface.forminput.Desgin.*;

public class ClientView extends VBox {

    // =========================================================
    // Main UI
    // =========================================================

    private final TabPane tabPane =
            new TabPane();

    private final VBox contentWrapper =
            new VBox();

    private final HBox sectionNavigation =
            new HBox(8);

    private final Label pageTitle =
            new Label("بيانات العميل");

    private final Label pageSubtitle =
            new Label("المعلومات الأساسية للعميل");

    private final Label statusLabel =
            new Label("جاهز");

    private final ProgressBar completionBar =
            new ProgressBar(0);

    private final Label completionLabel =
            new Label("0%");

    private final Button saveButton =
            new Button("حفظ العميل");

    private final Map<String, Button> sectionButtons =
            new LinkedHashMap<>();

    // =========================================================
    // Basic client data
    // =========================================================

    private final TextField nameField =
            new TextField();

    private final ToggleGroup genderGroup =
            new ToggleGroup();

    private final DatePicker datePicker =
            new DatePicker();

    private final TextField phoneField =
            new TextField();

    // =========================================================
    // Health data
    // =========================================================

    private final TextArea currentMedication =
            new TextArea();

    private final TextArea medicalHistory =
            new TextArea();

    private final TextArea familyMedicalHistory =
            new TextArea();

    private final TextArea healthNotes =
            new TextArea();

    // =========================================================
    // Dynamic collections
    // =========================================================

    private final VBox diseasesContainer =
            new VBox(12);

    private final VBox allergyContainer =
            new VBox(12);

    private final ArrayList<Allergy> allergies =
            new ArrayList<>();

    private final ArrayList<ChronicDisease> diseases =
            new ArrayList<>();

    // =========================================================
    // Lifestyle
    // =========================================================

    private final TextField mealsNumber =
            new TextField();

    private final TextField budgetField =
            new TextField();

    private final TextArea breakfastArea =
            new TextArea();

    private final TextArea lunchArea =
            new TextArea();

    private final TextArea dinnerArea =
            new TextArea();

    private final TextArea snacksArea =
            new TextArea();

    private final TextArea drinksArea =
            new TextArea();

    private final TextArea sleepArea =
            new TextArea();

    private final TextArea foodDislikeArea =
            new TextArea();

    private final TextArea badHabitsArea =
            new TextArea();

    // =========================================================
    // Internal state
    // =========================================================

    private boolean completionListenersAttached = false;

    // =========================================================
    // Constructor
    // =========================================================

    public ClientView() {

        getStyleClass().add(
                "fas-root"
        );

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setFillWidth(true);

        configureFields();
        configureTabs();
        configureHeader();
        configureSectionNavigation();
        configureFooter();
        configureContextMenu();
        configureShortcuts();

        contentWrapper.setFillWidth(true);

        VBox.setVgrow(
                contentWrapper,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                tabPane,
                Priority.ALWAYS
        );

        contentWrapper.getChildren().addAll(
                createPageHeader(),
                sectionNavigation,
                tabPane
        );

        getChildren().addAll(
                contentWrapper,
                createFooter()
        );

        showTab(
                "بيانات العميل"
        );

        updateCompletion();
    }

    // =========================================================
    // Configuration
    // =========================================================

    private void configureFields() {

        // -----------------------------------------------------
        // Prompts
        // -----------------------------------------------------

        nameField.setPromptText(
                "مثال: محمد علي أحمد"
        );

        phoneField.setPromptText(
                "رقم هاتف العميل"
        );

        mealsNumber.setPromptText(
                "مثال: 3"
        );

        budgetField.setPromptText(
                "الميزانية اليومية"
        );

        currentMedication.setPromptText(
                "الأدوية الحالية..."
        );

        medicalHistory.setPromptText(
                "التاريخ الصحي..."
        );

        familyMedicalHistory.setPromptText(
                "التاريخ الصحي للعائلة..."
        );

        healthNotes.setPromptText(
                "ملاحظات عامة..."
        );

        // -----------------------------------------------------
        // Text areas
        // -----------------------------------------------------

        configureTextArea(
                currentMedication
        );

        configureTextArea(
                medicalHistory
        );

        configureTextArea(
                familyMedicalHistory
        );

        configureTextArea(
                healthNotes
        );

        configureTextArea(
                breakfastArea
        );

        configureTextArea(
                lunchArea
        );

        configureTextArea(
                dinnerArea
        );

        configureTextArea(
                snacksArea
        );

        configureTextArea(
                drinksArea
        );

        configureTextArea(
                sleepArea
        );

        configureTextArea(
                foodDislikeArea
        );

        configureTextArea(
                badHabitsArea
        );

        // -----------------------------------------------------
        // Input protection
        // -----------------------------------------------------

        protectTextInput(
                nameField,
                120
        );

        protectDigits(
                phoneField,
                15
        );

        protectDigits(
                mealsNumber,
                2
        );

        protectDecimal(
                budgetField,
                10,
                2
        );

        protectTextInput(
                currentMedication,
                2000
        );

        protectTextInput(
                medicalHistory,
                4000
        );

        protectTextInput(
                familyMedicalHistory,
                4000
        );

        protectTextInput(
                healthNotes,
                4000
        );

        protectTextInput(
                breakfastArea,
                3000
        );

        protectTextInput(
                lunchArea,
                3000
        );

        protectTextInput(
                dinnerArea,
                3000
        );

        protectTextInput(
                snacksArea,
                3000
        );

        protectTextInput(
                drinksArea,
                3000
        );

        protectTextInput(
                sleepArea,
                1000
        );

        protectTextInput(
                foodDislikeArea,
                3000
        );

        protectTextInput(
                badHabitsArea,
                3000
        );

        // -----------------------------------------------------
        // Date
        // -----------------------------------------------------

        datePicker.setEditable(false);

        datePicker.setValue(
                LocalDate.of(
                        2000,
                        1,
                        1
                )
        );

        datePicker.setDayCellFactory(
                picker -> new DateCell() {

                    @Override
                    public void updateItem(
                            LocalDate date,
                            boolean empty
                    ) {

                        super.updateItem(
                                date,
                                empty
                        );

                        if (date != null
                                && date.isAfter(
                                LocalDate.now()
                        )) {

                            setDisable(true);
                        }
                    }
                }
        );

        // -----------------------------------------------------
        // Tooltips
        // -----------------------------------------------------

        applyTooltip(
                nameField,
                "أدخل اسم العميل الكامل"
        );

        applyTooltip(
                phoneField,
                "أدخل رقم الهاتف بدون مسافات أو رموز"
        );

        applyTooltip(
                datePicker,
                "حدد تاريخ ميلاد العميل"
        );

        applyTooltip(
                mealsNumber,
                "عدد الوجبات اليومية"
        );

        applyTooltip(
                budgetField,
                "الميزانية المتاحة للغذاء"
        );
    }

    private void configureTextArea(
            TextArea area
    ) {

        area.setWrapText(true);

        area.setPrefRowCount(3);

        area.setMinHeight(85);
    }

    private void configureTabs() {

        tabPane.getStyleClass().add(
                "fas-tab-pane"
        );

        tabPane.setTabClosingPolicy(
                TabPane.TabClosingPolicy.UNAVAILABLE
        );

        tabPane.getTabs().add(
                createClientDataTab()
        );

        /*
         * يمنع التنقل الطبيعي بالـTab نفسه،
         * لأن التنقل يتم من خلال شريط الأقسام.
         */
        tabPane.setFocusTraversable(false);
    }

    private void configureHeader() {

        pageTitle.getStyleClass().add(
                "fas-page-title"
        );

        pageSubtitle.getStyleClass().add(
                "fas-page-subtitle"
        );

        completionBar.setPrefWidth(160);

        completionBar.setMaxWidth(
                Double.MAX_VALUE
        );

        completionBar.getStyleClass().add(
                "fas-progress"
        );

        completionLabel.getStyleClass().add(
                "fas-progress-label"
        );
    }

    // =========================================================
    // Section navigation
    // =========================================================

    private void configureSectionNavigation() {

        sectionNavigation.setAlignment(
                Pos.CENTER_RIGHT
        );

        sectionNavigation.setPadding(
                new Insets(
                        6,
                        0,
                        12,
                        0
                )
        );

        sectionNavigation.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        sectionNavigation.getStyleClass().add(
                "fas-section-navigation"
        );

        sectionNavigation.getChildren().addAll(

                createSectionButton(
                        "بيانات العميل",
                        "👤",
                        "Alt + 1",
                        () ->
                                showTab(
                                        "بيانات العميل"
                                )
                ),

                createSectionButton(
                        "البيانات الصحية",
                        "🏥",
                        "Alt + 2",
                        () ->
                                switchTab(
                                        "بيانات العميل الصحية",
                                        this::createHealthDataTab
                                )
                ),

                createSectionButton(
                        "نمط الحياة",
                        "🍎",
                        "Alt + 3",
                        () ->
                                switchTab(
                                        "معلومات شخصية",
                                        this::createLifeInformationTab
                                )
                ),

                createSectionButton(
                        "الأمراض",
                        "🩺",
                        "Alt + 4",
                        () ->
                                switchTab(
                                        "الأمراض",
                                        this::diseas
                                )
                ),

                createSectionButton(
                        "الحساسية",
                        "⚠",
                        "Alt + 5",
                        () ->
                                switchTab(
                                        "الحساسية",
                                        this::Allergy
                                )
                )
        );
    }

    private Button createSectionButton(
            String title,
            String icon,
            String shortcut,
            Runnable action
    ) {

        Button button = new Button();

        HBox content = new HBox(8);

        content.setAlignment(
                Pos.CENTER
        );

        Label iconLabel =
                new Label(icon);

        iconLabel.getStyleClass().add(
                "fas-nav-icon"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass().add(
                "fas-nav-title"
        );

        Label shortcutLabel =
                new Label(shortcut);

        shortcutLabel.getStyleClass().add(
                "fas-nav-shortcut"
        );

        content.getChildren().addAll(
                iconLabel,
                titleLabel,
                shortcutLabel
        );

        button.setGraphic(
                content
        );

        button.setMinHeight(42);

        button.setPrefHeight(42);

        button.setPrefWidth(155);

        button.setMinWidth(135);

        button.setFocusTraversable(false);

        button.getStyleClass().add(
                "fas-nav-button"
        );

        applyTooltip(
                button,
                title + "\n" + shortcut
        );

        button.setOnAction(
                e -> action.run()
        );

        sectionButtons.put(
                title,
                button
        );

        return button;
    }

    // =========================================================
    // Header
    // =========================================================

    private Node createPageHeader() {

        HBox header =
                new HBox(20);

        header.setPadding(
                new Insets(
                        8,
                        2,
                        8,
                        2
                )
        );

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

        VBox progressBox =
                new VBox(5);

        progressBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        HBox progressHeader =
                new HBox(8);

        progressHeader.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label progressTitle =
                new Label(
                        "اكتمال الملف"
                );

        progressTitle.getStyleClass().add(
                "fas-progress-title"
        );

        progressHeader.getChildren().addAll(
                completionLabel,
                progressTitle
        );

        progressBox.getChildren().addAll(
                progressHeader,
                completionBar
        );

        header.getChildren().addAll(
                titleBox,
                spacer,
                progressBox
        );

        return header;
    }

    // =========================================================
    // Footer
    // =========================================================

    private Node createFooter() {

        HBox footer =
                new HBox(12);

        footer.setAlignment(
                Pos.CENTER_RIGHT
        );

        footer.setPadding(
                new Insets(
                        10,
                        14,
                        10,
                        14
                )
        );

        footer.getStyleClass().add(
                "fas-footer"
        );

        statusLabel.getStyleClass().add(
                "fas-status-label"
        );

        Label shortcut =
                new Label(
                        "Ctrl + S"
                );

        shortcut.getStyleClass().add(
                "fas-footer-shortcut"
        );

        Button clearButton =
                new Button(
                        "إعادة ضبط"
                );

        clearButton.getStyleClass().add(
                "fas-secondary-button"
        );

        applyTooltip(
                clearButton,
                "مسح جميع البيانات وإعادة النموذج للحالة الافتراضية\n" +
                        "Ctrl + Shift + R"
        );

        clearButton.setOnAction(
                e -> clearAllFields()
        );

        HBox statusBox =
                new HBox(7);

        statusBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Region statusDot =
                new Region();

        statusDot.getStyleClass().add(
                "fas-status-dot"
        );

        statusBox.getChildren().addAll(
                statusLabel,
                statusDot
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        saveButton.setMinWidth(
                155
        );

        saveButton.setPrefHeight(
                42
        );

        saveButton.getStyleClass().add(
                "fas-primary-button"
        );

        saveButton.setDefaultButton(
                true
        );

        applyTooltip(
                saveButton,
                "حفظ بيانات العميل بالكامل في النظام\n" +
                        "Ctrl + S"
        );

        saveButton.setOnAction(
                e -> handleSave()
        );

        footer.getChildren().addAll(
                statusBox,
                spacer,
                clearButton,
                shortcut,
                saveButton
        );

        return footer;
    }

    private void configureFooter() {
        // Reserved for future use.
    }

    // =========================================================
    // Client data tab
    // =========================================================

    public Tab createClientDataTab() {

        Tab tab =
                new Tab(
                        "بيانات العميل"
                );

        tab.setClosable(false);

        VBox root =
                new VBox(18);

        root.setPadding(
                new Insets(4)
        );

        root.setFillWidth(true);

        VBox card =
                createCard();

        Label title =
                new Label(
                        "البيانات الأساسية"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "أدخل المعلومات الأساسية للعميل قبل الانتقال إلى الأقسام الأخرى"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        GridPane grid =
                createFormGrid();

        Label nameLabel =
                fieldLabel(
                        "الاسم الكامل",
                        true
                );

        grid.add(
                nameLabel,
                0,
                0
        );

        grid.add(
                nameField,
                1,
                0
        );

        Label phoneLabel =
                fieldLabel(
                        "رقم الهاتف",
                        true
                );

        grid.add(
                phoneLabel,
                0,
                1
        );

        grid.add(
                phoneField,
                1,
                1
        );

        Label genderLabel =
                fieldLabel(
                        "الجنس",
                        true
                );

        RadioButton male =
                new RadioButton(
                        "ذكر"
                );

        RadioButton female =
                new RadioButton(
                        "أنثى"
                );

        male.setToggleGroup(
                genderGroup
        );

        female.setToggleGroup(
                genderGroup
        );

        male.setSelected(true);

        HBox genderBox =
                new HBox(
                        18,
                        male,
                        female
                );

        genderBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        grid.add(
                genderLabel,
                0,
                2
        );

        grid.add(
                genderBox,
                1,
                2
        );

        Label dateLabel =
                fieldLabel(
                        "تاريخ الميلاد",
                        true
                );

        grid.add(
                dateLabel,
                0,
                3
        );

        grid.add(
                datePicker,
                1,
                3
        );

        card.getChildren().addAll(
                title,
                subtitle,
                createSeparator(),
                grid
        );

        VBox quickActions =
                createCard();

        Label quickTitle =
                new Label(
                        "الوصول السريع"
                );

        quickTitle.getStyleClass().add(
                "fas-card-title"
        );

        Label quickSubtitle =
                new Label(
                        "انتقل مباشرة إلى القسم الذي تريد تعبئته"
                );

        quickSubtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        HBox actions =
                new HBox(12);

        actions.getChildren().addAll(

                createQuickButton(
                        "🏥",
                        "البيانات الصحية",
                        "Alt + 2",
                        () ->
                                switchTab(
                                        "بيانات العميل الصحية",
                                        this::createHealthDataTab
                                )
                ),

                createQuickButton(
                        "🍎",
                        "نمط الحياة",
                        "Alt + 3",
                        () ->
                                switchTab(
                                        "معلومات شخصية",
                                        this::createLifeInformationTab
                                )
                ),

                createQuickButton(
                        "🩺",
                        "الأمراض",
                        "Alt + 4",
                        () ->
                                switchTab(
                                        "الأمراض",
                                        this::diseas
                                )
                ),

                createQuickButton(
                        "⚠",
                        "الحساسية",
                        "Alt + 5",
                        () ->
                                switchTab(
                                        "الحساسية",
                                        this::Allergy
                                )
                )
        );

        quickActions.getChildren().addAll(
                quickTitle,
                quickSubtitle,
                createSeparator(),
                actions
        );

        root.getChildren().addAll(
                card,
                quickActions
        );

        ScrollPane scroll =
                createScroll(
                        root
                );

        tab.setContent(
                scroll
        );

        attachCompletionListeners();

        return tab;
    }

    // =========================================================
    // Health tab
    // =========================================================

    public Tab createHealthDataTab() {

        Tab tab =
                new Tab(
                        "بيانات العميل الصحية"
                );

        tab.setClosable(false);

        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(4)
        );

        VBox healthCard =
                createCard();

        Label title =
                new Label(
                        "البيانات الصحية"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "أدخل المعلومات الصحية المهمة التي يحتاجها أخصائي التغذية"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        GridPane grid =
                createFormGrid();

        addTextAreaRow(
                grid,
                0,
                "الأدوية الحالية",
                currentMedication
        );

        addTextAreaRow(
                grid,
                1,
                "التاريخ الصحي",
                medicalHistory
        );

        addTextAreaRow(
                grid,
                2,
                "تاريخ العائلة",
                familyMedicalHistory
        );

        addTextAreaRow(
                grid,
                3,
                "ملاحظات عامة",
                healthNotes
        );

        healthCard.getChildren().addAll(
                title,
                subtitle,
                createSeparator(),
                grid
        );

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button allergyButton =
                createActionButton(
                        "⚠",
                        "إضافة حساسية",
                        "fas-warning-button",
                        "إضافة حساسية جديدة إلى ملف العميل"
                );

        allergyButton.setOnAction(
                e ->
                        switchTab(
                                "الحساسية",
                                this::Allergy
                        )
        );

        Button diseaseButton =
                createActionButton(
                        "🩺",
                        "إضافة مرض",
                        "fas-danger-button",
                        "إضافة مرض مزمن إلى ملف العميل"
                );

        diseaseButton.setOnAction(
                e ->
                        switchTab(
                                "الأمراض",
                                this::diseas
                        )
        );

        actions.getChildren().addAll(
                diseaseButton,
                allergyButton
        );

        root.getChildren().addAll(
                healthCard,
                actions
        );

        tab.setContent(
                createScroll(root)
        );

        attachCompletionListeners();

        return tab;
    }

    // =========================================================
    // Diseases
    // =========================================================

    public Tab diseas() {

        Tab tab =
                new Tab(
                        "الأمراض"
                );

        tab.setClosable(false);

        VBox root =
                new VBox(14);

        root.setPadding(
                new Insets(4)
        );

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox titles =
                new VBox(3);

        Label title =
                new Label(
                        "الأمراض المزمنة"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "أضف كل مرض مع حالته ودرجة الخطورة والممنوعات"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        titles.getChildren().addAll(
                title,
                subtitle
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button addButton =
                createActionButton(
                        "+",
                        "إضافة مرض",
                        "fas-primary-button",
                        "إضافة مرض جديد"
                );

        addButton.setOnAction(
                e -> addDiseaseCard()
        );

        header.getChildren().addAll(
                titles,
                spacer,
                addButton
        );

        VBox card =
                createCard();

        card.getChildren().addAll(
                header,
                createSeparator(),
                diseasesContainer
        );

        ScrollPane scroll =
                createScroll(
                        card
                );

        root.getChildren().add(
                scroll
        );

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        tab.setContent(
                root
        );

        return tab;
    }

    private void addDiseaseCard() {

        VBox pane =
                new VBox(12);

        pane.getStyleClass().add(
                "fas-dynamic-card"
        );

        pane.setPadding(
                new Insets(16)
        );

        Label cardTitle =
                new Label(
                        "مرض جديد"
                );

        cardTitle.getStyleClass().add(
                "fas-dynamic-title"
        );

        GridPane grid =
                createFormGrid();

        TextField dName =
                new TextField();

        dName.setPromptText(
                "اسم المرض"
        );

        TextField dStatus =
                new TextField();

        dStatus.setPromptText(
                "الحالة الحالية"
        );

        ComboBox<String> dSeverity =
                new ComboBox<>();

        dSeverity.getItems().addAll(
                "بسيطة",
                "متوسطة",
                "خطيرة"
        );

        dSeverity.setValue(
                "بسيطة"
        );

        dSeverity.setMaxWidth(
                Double.MAX_VALUE
        );

        TextArea dFood =
                new TextArea();

        dFood.setPromptText(
                "الممنوعات الغذائية..."
        );

        configureTextArea(
                dFood
        );

        TextArea dNotes =
                new TextArea();

        dNotes.setPromptText(
                "ملاحظات إضافية..."
        );

        configureTextArea(
                dNotes
        );

        // -----------------------------------------------------
        // Protection
        // -----------------------------------------------------

        protectTextInput(
                dName,
                200
        );

        protectTextInput(
                dStatus,
                500
        );

        protectTextInput(
                dFood,
                3000
        );

        protectTextInput(
                dNotes,
                3000
        );

        // -----------------------------------------------------
        // Grid
        // -----------------------------------------------------

        addField(
                grid,
                0,
                "المرض",
                dName
        );

        addField(
                grid,
                1,
                "الحالة",
                dStatus
        );

        addField(
                grid,
                2,
                "الخطورة",
                dSeverity
        );

        addField(
                grid,
                3,
                "الممنوعات",
                dFood
        );

        addField(
                grid,
                4,
                "ملاحظات",
                dNotes
        );

        Button deleteButton =
                new Button(
                        "حذف"
                );

        deleteButton.getStyleClass().add(
                "fas-delete-button"
        );

        applyTooltip(
                deleteButton,
                "حذف هذا المرض من النموذج"
        );

        deleteButton.setOnAction(
                event -> {

                    diseasesContainer
                            .getChildren()
                            .remove(pane);

                    updateCompletion();
                }
        );

        HBox bottom =
                new HBox(
                        deleteButton
                );

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        pane.getChildren().addAll(
                cardTitle,
                grid,
                bottom
        );

        diseasesContainer.getChildren().add(
                pane
        );

        updateCompletion();
    }

    // =========================================================
    // Allergy
    // =========================================================

    public Tab Allergy() {

        Tab tab =
                new Tab(
                        "الحساسية"
                );

        tab.setClosable(false);

        VBox root =
                new VBox(14);

        root.setPadding(
                new Insets(4)
        );

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox titles =
                new VBox(3);

        Label title =
                new Label(
                        "الحساسية"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "سجل الحساسية والمسبب ودرجة الخطورة والملاحظات"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        titles.getChildren().addAll(
                title,
                subtitle
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button addButton =
                createActionButton(
                        "+",
                        "إضافة حساسية",
                        "fas-danger-button",
                        "إضافة حساسية جديدة"
                );

        addButton.setOnAction(
                e -> addAllergyCard()
        );

        header.getChildren().addAll(
                titles,
                spacer,
                addButton
        );

        VBox card =
                createCard();

        card.getChildren().addAll(
                header,
                createSeparator(),
                allergyContainer
        );

        ScrollPane scroll =
                createScroll(card);

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        root.getChildren().add(
                scroll
        );

        tab.setContent(
                root
        );

        return tab;
    }

    private void addAllergyCard() {

        VBox pane =
                new VBox(12);

        pane.getStyleClass().add(
                "fas-allergy-card"
        );

        pane.setPadding(
                new Insets(16)
        );

        Label title =
                new Label(
                        "حساسية جديدة"
                );

        title.getStyleClass().add(
                "fas-dynamic-title"
        );

        GridPane grid =
                createFormGrid();

        TextField aName =
                new TextField();

        aName.setPromptText(
                "اسم الحساسية"
        );

        TextField aStatus =
                new TextField();

        aStatus.setPromptText(
                "الحالة الحالية"
        );

        ComboBox<String> aSeverity =
                new ComboBox<>();

        aSeverity.getItems().addAll(
                "بسيطة",
                "متوسطة",
                "خطيرة"
        );

        aSeverity.setValue(
                "بسيطة"
        );

        aSeverity.setMaxWidth(
                Double.MAX_VALUE
        );

        TextArea aFood =
                new TextArea();

        aFood.setPromptText(
                "المسبب / المادة المسببة..."
        );

        configureTextArea(
                aFood
        );

        TextArea aNotes =
                new TextArea();

        aNotes.setPromptText(
                "ملاحظات..."
        );

        configureTextArea(
                aNotes
        );

        // -----------------------------------------------------
        // Protection
        // -----------------------------------------------------

        protectTextInput(
                aName,
                200
        );

        protectTextInput(
                aStatus,
                500
        );

        protectTextInput(
                aFood,
                3000
        );

        protectTextInput(
                aNotes,
                3000
        );

        // -----------------------------------------------------
        // Grid
        // -----------------------------------------------------

        addField(
                grid,
                0,
                "الحساسية",
                aName
        );

        addField(
                grid,
                1,
                "الحالة",
                aStatus
        );

        addField(
                grid,
                2,
                "الخطورة",
                aSeverity
        );

        addField(
                grid,
                3,
                "المسبب",
                aFood
        );

        addField(
                grid,
                4,
                "ملاحظات",
                aNotes
        );

        Button deleteButton =
                new Button(
                        "حذف"
                );

        deleteButton.getStyleClass().add(
                "fas-delete-button"
        );

        applyTooltip(
                deleteButton,
                "حذف هذه الحساسية من النموذج"
        );

        deleteButton.setOnAction(
                event -> {

                    allergyContainer
                            .getChildren()
                            .remove(pane);

                    updateCompletion();
                }
        );

        HBox bottom =
                new HBox(
                        deleteButton
                );

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        pane.getChildren().addAll(
                title,
                grid,
                bottom
        );

        allergyContainer.getChildren().add(
                pane
        );

        updateCompletion();
    }

    // =========================================================
    // Lifestyle
    // =========================================================

    public Tab createLifeInformationTab() {

        Tab tab =
                new Tab(
                        "معلومات شخصية"
                );

        tab.setClosable(false);

        VBox root =
                new VBox(14);

        root.setPadding(
                new Insets(4)
        );

        VBox basicCard =
                createCard();

        Label title =
                new Label(
                        "نمط الحياة"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "معلومات الوجبات والميزانية والعادات اليومية"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        GridPane grid =
                createFormGrid();

        addField(
                grid,
                0,
                "الوجبات / يوم",
                mealsNumber
        );

        addField(
                grid,
                1,
                "الميزانية",
                budgetField
        );

        addField(
                grid,
                2,
                "الإفطار",
                breakfastArea
        );

        addField(
                grid,
                3,
                "الغداء",
                lunchArea
        );

        addField(
                grid,
                4,
                "العشاء",
                dinnerArea
        );

        addField(
                grid,
                5,
                "الحلويات",
                snacksArea
        );

        addField(
                grid,
                6,
                "المشروبات",
                drinksArea
        );

        addField(
                grid,
                7,
                "النوم",
                sleepArea
        );

        addField(
                grid,
                8,
                "الطعام غير المرغوب",
                foodDislikeArea
        );

        addField(
                grid,
                9,
                "العادات السيئة",
                badHabitsArea
        );

        basicCard.getChildren().addAll(
                title,
                subtitle,
                createSeparator(),
                grid
        );

        root.getChildren().add(
                basicCard
        );

        tab.setContent(
                createScroll(root)
        );

        return tab;
    }

    // =========================================================
    // Navigation
    // =========================================================

    private void switchTab(
            String title,
            Supplier<Tab> tabCreator
    ) {

        if (findTab(title) != null) {

            showTab(title);

            return;
        }

        BackgroundRunner.run(
                "جاري تجهيز الواجهة...",

                () -> {

                    Thread.sleep(120);

                    return true;
                },

                () -> {

                    Tab newTab =
                            tabCreator.get();

                    tabPane.getTabs().add(
                            newTab
                    );

                    showTab(title);
                }
        );
    }

    private Tab findTab(
            String title
    ) {

        return tabPane.getTabs()
                .stream()
                .filter(
                        tab ->
                                tab.getText()
                                        .equals(title)
                )
                .findFirst()
                .orElse(null);
    }

    private void showTab(
            String title
    ) {

        Tab tab =
                findTab(title);

        if (tab == null) {
            return;
        }

        tabPane.getSelectionModel()
                .select(tab);

        updateNavigationState(
                title
        );

        updatePageHeader(
                title
        );
    }

    private void updateNavigationState(
            String title
    ) {

        sectionButtons.values()
                .forEach(
                        button ->
                                button
                                        .getStyleClass()
                                        .remove(
                                                "fas-nav-active"
                                        )
                );

        String key =
                switch (title) {

                    case "بيانات العميل" ->
                            "بيانات العميل";

                    case "بيانات العميل الصحية" ->
                            "البيانات الصحية";

                    case "معلومات شخصية" ->
                            "نمط الحياة";

                    case "الأمراض" ->
                            "الأمراض";

                    case "الحساسية" ->
                            "الحساسية";

                    default ->
                            null;
                };

        if (key == null) {
            return;
        }

        Button activeButton =
                sectionButtons.get(key);

        if (activeButton != null
                && !activeButton
                .getStyleClass()
                .contains(
                        "fas-nav-active"
                )) {

            activeButton
                    .getStyleClass()
                    .add(
                            "fas-nav-active"
                    );
        }
    }

    private void updatePageHeader(
            String title
    ) {

        switch (title) {

            case "بيانات العميل" -> {

                pageTitle.setText(
                        "بيانات العميل"
                );

                pageSubtitle.setText(
                        "المعلومات الأساسية للعميل"
                );
            }

            case "بيانات العميل الصحية" -> {

                pageTitle.setText(
                        "البيانات الصحية"
                );

                pageSubtitle.setText(
                        "الأدوية والتاريخ الصحي والملاحظات"
                );
            }

            case "معلومات شخصية" -> {

                pageTitle.setText(
                        "نمط الحياة"
                );

                pageSubtitle.setText(
                        "الوجبات والميزانية والعادات اليومية"
                );
            }

            case "الأمراض" -> {

                pageTitle.setText(
                        "الأمراض المزمنة"
                );

                pageSubtitle.setText(
                        "إدارة الأمراض والحالات الصحية"
                );
            }

            case "الحساسية" -> {

                pageTitle.setText(
                        "الحساسية"
                );

                pageSubtitle.setText(
                        "إدارة الحساسية والمسببات"
                );
            }

            default -> {
            }
        }
    }

    // =========================================================
    // Save client
    // =========================================================

    public Client saveClient() {

        if (genderGroup.getSelectedToggle() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات ناقصة",
                    "الرجاء اختيار جنس العميل."
            );

            return null;
        }

        char gender =
                ((RadioButton)
                        genderGroup
                                .getSelectedToggle())
                        .getText()
                        .equals("ذكر")
                        ? 'M'
                        : 'F';

        String fullName =
                nameField.getText()
                        .trim();

        if (fullName.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات ناقصة",
                    "لا يمكن حفظ العميل! الرجاء إدخال اسم العميل."
            );

            return null;
        }

        String[] names =
                fullName.split(
                        "\\s+"
                );

        if (names.length < 2) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات غير مكتملة",
                    "يرجى إدخال الاسم كاملاً."
            );

            return null;
        }

        if (phoneField.getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات ناقصة",
                    "الرجاء إدخال رقم هاتف العميل."
            );

            return null;
        }

        if (datePicker.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات ناقصة",
                    "الرجاء تحديد تاريخ ميلاد العميل."
            );

            return null;
        }

        return new Client(
                names,
                gender,
                datePicker.getValue(),
                phoneField.getText()
        );
    }

    // =========================================================
    // Save health
    // =========================================================

    public HealthData saveHealthData() {

        return new HealthData.HealthDataBuilder()
                .currentMedication(
                        currentMedication.getText()
                )
                .familyMedicalHistory(
                        familyMedicalHistory.getText()
                )
                .medicalHistory(
                        medicalHistory.getText()
                )
                .notes(
                        healthNotes.getText()
                )
                .build();
    }

    // =========================================================
    // Save lifestyle
    // =========================================================

    public LifeStyleInformation saveLifeStyleInformation() {

        String mealsText =
                mealsNumber.getText()
                        .trim();

        String budgetText =
                budgetField.getText()
                        .trim();

        int mealsPerDay =
                mealsText.isEmpty()
                        ? 1
                        : Integer.parseInt(
                        mealsText
                );

        BigDecimal budget =
                budgetText.isEmpty()
                        ? BigDecimal.ZERO
                        : new BigDecimal(
                        budgetText
                );

        return new LifeStyleInformation.Builder()
                .mealsPerDay(
                        mealsPerDay
                )
                .budget(
                        budget
                )
                .breakfast(
                        breakfastArea.getText()
                )
                .lunch(
                        lunchArea.getText()
                )
                .dinner(
                        dinnerArea.getText()
                )
                .snacks(
                        snacksArea.getText()
                )
                .drinks(
                        drinksArea.getText()
                )
                .sleepHours(
                        sleepArea.getText()
                )
                .foodDislike(
                        foodDislikeArea.getText()
                )
                .badHabits(
                        badHabitsArea.getText()
                )
                .build();
    }

    // =========================================================
    // Save diseases
    // =========================================================

    public List<PatientChronicDisease>
    savePatientChronicDisease(
            Client client
    ) {

        ArrayList<PatientChronicDisease>
                diseasesList =
                new ArrayList<>();

        for (Node node :
                diseasesContainer.getChildren()) {

            if (!(node instanceof VBox pane)) {
                continue;
            }

            GridPane grid =
                    findGridPane(pane);

            if (grid == null) {
                continue;
            }

            TextField nameField =
                    (TextField)
                            findNodeByRow(
                                    grid,
                                    0
                            );

            TextField statusField =
                    (TextField)
                            findNodeByRow(
                                    grid,
                                    1
                            );

            @SuppressWarnings("unchecked")
            ComboBox<String> severityBox =
                    (ComboBox<String>)
                            findNodeByRow(
                                    grid,
                                    2
                            );

            TextArea foodArea =
                    (TextArea)
                            findNodeByRow(
                                    grid,
                                    3
                            );

            TextArea notesArea =
                    (TextArea)
                            findNodeByRow(
                                    grid,
                                    4
                            );

            String name =
                    nameField.getText()
                            .trim();

            String status =
                    statusField.getText()
                            .trim();

            String severity =
                    severityBox.getValue() != null
                            ? severityBox.getValue()
                            : "";

            String contraindicated =
                    foodArea.getText();

            if (!name.isEmpty()) {

                ChronicDisease disease =
                        new ChronicDisease(
                                name
                        );

                diseasesList.add(
                        new PatientChronicDisease(
                                client,
                                disease,
                                status,
                                severity,
                                notesArea.getText(),
                                contraindicated
                        )
                );
            }
        }

        return diseasesList;
    }

    // =========================================================
    // Save allergies
    // =========================================================

    public List<PatientAllergy>
    savePatientAllergy(
            Client client
    ) {

        ArrayList<PatientAllergy>
                allergiesList =
                new ArrayList<>();

        for (Node node :
                allergyContainer.getChildren()) {

            if (!(node instanceof VBox pane)) {
                continue;
            }

            GridPane grid =
                    findGridPane(pane);

            if (grid == null) {
                continue;
            }

            TextField nameField =
                    (TextField)
                            findNodeByRow(
                                    grid,
                                    0
                            );

            TextField statusField =
                    (TextField)
                            findNodeByRow(
                                    grid,
                                    1
                            );

            @SuppressWarnings("unchecked")
            ComboBox<String> severityBox =
                    (ComboBox<String>)
                            findNodeByRow(
                                    grid,
                                    2
                            );

            TextArea foodArea =
                    (TextArea)
                            findNodeByRow(
                                    grid,
                                    3
                            );

            TextArea notesArea =
                    (TextArea)
                            findNodeByRow(
                                    grid,
                                    4
                            );

            String name =
                    nameField.getText()
                            .trim();

            String status =
                    statusField.getText()
                            .trim();

            String severity =
                    severityBox.getValue() != null
                            ? severityBox.getValue()
                            : "";

            String contraindicated =
                    foodArea.getText();

            if (!name.isEmpty()) {

                Allergy allergy =
                        new Allergy(
                                name
                        );

                allergiesList.add(
                        new PatientAllergy(
                                client,
                                allergy,
                                status,
                                severity,
                                notesArea.getText(),
                                contraindicated
                        )
                );
            }
        }

        return allergiesList;
    }

    // =========================================================
    // Handle save
    // =========================================================

    public void handleSave() {

        Client client;
        HealthData healthData;
        LifeStyleInformation lifeStyleInformation;

        try {

            setStatus(
                    "جاري التحقق من البيانات..."
            );

            if (!validateInputData()) {

                setStatus(
                        "يرجى مراجعة البيانات"
                );

                return;
            }

            client =
                    saveClient();

            if (client == null) {

                setStatus(
                        "يرجى مراجعة البيانات"
                );

                return;
            }

            healthData =
                    saveHealthData();

            lifeStyleInformation =
                    saveLifeStyleInformation();

            client.setHealthData(
                    healthData
            );

            client.setLifeStyleInformation(
                    lifeStyleInformation
            );

            client.setChronicDiseases(
                    savePatientChronicDisease(
                            client
                    )
            );

            client.setAllergies(
                    savePatientAllergy(
                            client
                    )
            );

        } catch (NumberFormatException ex) {

            setStatus(
                    "خطأ في البيانات الرقمية"
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "خطأ في التنسيق الرقمي",
                    "تأكد من أن القيم الرقمية صحيحة."
            );

            return;

        } catch (
                IllegalArgumentException |
                IllegalStateException ex
        ) {

            setStatus(
                    "بيانات غير صالحة"
            );

            showAlert(
                    Alert.AlertType.WARNING,
                    "بيانات غير صالحة",
                    ex.getMessage()
            );

            return;

        } catch (Exception ex) {

            setStatus(
                    "حدث خطأ"
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "خطأ",
                    ex.getMessage() == null
                            ? "حدث خطأ غير متوقع."
                            : ex.getMessage()
            );

            return;
        }

        final Client finalClient =
                client;

        setProcessingState(
                true
        );

        BackgroundRunner.run(

                "جاري حفظ بيانات العميل...",

                () -> {

                    ClientAPI service =
                            ClientApiManager
                                    .getInstance()
                                    .getClientAPI();

                    return service.saveFullClientData(
                            finalClient
                    );
                },

                () -> {

                    setProcessingState(
                            false
                    );

                    setStatus(
                            "تم الحفظ بنجاح"
                    );

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "تم الحفظ بنجاح",
                            "تم حفظ وتوثيق بيانات العميل '"
                                    + finalClient
                                    .getFirstName()
                                    + "' في قاعدة البيانات بنجاح."
                    );

                    clearAllFields();
                }
        );
    }

    // =========================================================
    // Processing state
    // =========================================================

    private void setProcessingState(
            boolean processing
    ) {

        saveButton.setDisable(
                processing
        );

        if (processing) {

            saveButton.setText(
                    "جاري الحفظ..."
            );

        } else {

            saveButton.setText(
                    "حفظ العميل"
            );
        }
    }

    // =========================================================
    // Validation
    // =========================================================

    private boolean validateInputData() {

        String name =
                nameField.getText()
                        .trim();

        if (name.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "الاسم",
                    "الرجاء إدخال اسم العميل."
            );

            return false;
        }

        if (name.length() > 120) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "الاسم",
                    "اسم العميل طويل جدًا."
            );

            return false;
        }

        String phone =
                phoneField.getText()
                        .trim();

        if (!phone.matches("\\d{7,15}")) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "رقم الهاتف",
                    "رقم الهاتف يجب أن يحتوي على أرقام فقط " +
                            "وبطول من 7 إلى 15 رقمًا."
            );

            return false;
        }

        String meals =
                mealsNumber.getText()
                        .trim();

        if (!meals.isEmpty()) {

            try {

                int value =
                        Integer.parseInt(
                                meals
                        );

                if (value < 1
                        || value > 20) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "عدد الوجبات",
                            "عدد الوجبات يجب أن يكون بين 1 و20."
                    );

                    return false;
                }

            } catch (NumberFormatException ex) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "عدد الوجبات",
                        "عدد الوجبات يجب أن يكون رقمًا صحيحًا."
                );

                return false;
            }
        }

        String budget =
                budgetField.getText()
                        .trim();

        if (!budget.isEmpty()) {

            try {

                BigDecimal value =
                        new BigDecimal(
                                budget
                        );

                if (value.signum() < 0) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "الميزانية",
                            "الميزانية لا يمكن أن تكون سالبة."
                    );

                    return false;
                }

            } catch (NumberFormatException ex) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "الميزانية",
                        "الميزانية يجب أن تكون رقمًا صالحًا."
                );

                return false;
            }
        }

        return true;
    }

    // =========================================================
    // Clear
    // =========================================================

    private void clearBasicData() {

        nameField.clear();

        phoneField.clear();

        datePicker.setValue(
                LocalDate.of(
                        2000,
                        1,
                        1
                )
        );

        if (!genderGroup
                .getToggles()
                .isEmpty()) {

            genderGroup
                    .getToggles()
                    .get(0)
                    .setSelected(true);
        }

        updateCompletion();

        setStatus(
                "تم مسح البيانات الأساسية"
        );
    }

    private void clearHealthData() {

        currentMedication.clear();

        medicalHistory.clear();

        familyMedicalHistory.clear();

        healthNotes.clear();

        diseasesContainer
                .getChildren()
                .clear();

        allergyContainer
                .getChildren()
                .clear();

        diseases.clear();

        allergies.clear();

        tabPane
                .getTabs()
                .removeIf(
                        tab ->
                                tab.getText()
                                        .equals(
                                                "بيانات العميل الصحية"
                                        )
                                        ||
                                        tab.getText()
                                                .equals(
                                                        "الأمراض"
                                                )
                                        ||
                                        tab.getText()
                                                .equals(
                                                        "الحساسية"
                                                )
                );

        updateCompletion();

        showTab(
                "بيانات العميل"
        );

        setStatus(
                "تم مسح البيانات الصحية"
        );
    }

    private void clearLifeData() {

        mealsNumber.clear();

        budgetField.clear();

        breakfastArea.clear();

        lunchArea.clear();

        dinnerArea.clear();

        snacksArea.clear();

        drinksArea.clear();

        sleepArea.clear();

        foodDislikeArea.clear();

        badHabitsArea.clear();

        tabPane
                .getTabs()
                .removeIf(
                        tab ->
                                tab.getText()
                                        .equals(
                                                "معلومات شخصية"
                                        )
                );

        updateCompletion();

        showTab(
                "بيانات العميل"
        );

        setStatus(
                "تم مسح بيانات نمط الحياة"
        );
    }

    private void clearAllFields() {

        clearBasicData();

        clearHealthData();

        clearLifeData();

        diseasesContainer
                .getChildren()
                .clear();

        allergyContainer
                .getChildren()
                .clear();

        tabPane
                .getSelectionModel()
                .select(0);

        showTab(
                "بيانات العميل"
        );

        setStatus(
                "تمت إعادة ضبط النموذج"
        );

        updateCompletion();
    }

    // =========================================================
    // Context menu
    // =========================================================

    private void configureContextMenu() {

        ContextMenu contextMenu =
                new ContextMenu();

        MenuItem refreshAllItem =
                new MenuItem(
                        "↻ إعادة ضبط النموذج"
                );

        MenuItem refreshBasicItem =
                new MenuItem(
                        "👤 مسح البيانات الأساسية"
                );

        MenuItem refreshHealthItem =
                new MenuItem(
                        "🏥 مسح البيانات الصحية"
                );

        MenuItem refreshLifeItem =
                new MenuItem(
                        "🍎 مسح نمط الحياة"
                );

        refreshAllItem.setOnAction(
                e -> clearAllFields()
        );

        refreshBasicItem.setOnAction(
                e -> clearBasicData()
        );

        refreshHealthItem.setOnAction(
                e -> clearHealthData()
        );

        refreshLifeItem.setOnAction(
                e -> clearLifeData()
        );

        contextMenu.getItems().addAll(
                refreshAllItem,
                new SeparatorMenuItem(),
                refreshBasicItem,
                refreshHealthItem,
                refreshLifeItem
        );

        addEventFilter(
                javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                event -> {

                    if (event.getButton()
                            == MouseButton.SECONDARY) {

                        contextMenu.show(
                                this,
                                event.getScreenX(),
                                event.getScreenY()
                        );

                        event.consume();

                    } else {

                        contextMenu.hide();
                    }
                }
        );
    }

    // =========================================================
    // Keyboard shortcuts
    // =========================================================

    private void configureShortcuts() {

        addEventFilter(
                KeyEvent.KEY_PRESSED,
                this::handleShortcut
        );
    }

    private void handleShortcut(
            KeyEvent event
    ) {

        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        if (event.isControlDown()
                && event.getCode()
                == KeyCode.S) {

            handleSave();

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // Alt + 1
        // -----------------------------------------------------

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT1) {

            showTab(
                    "بيانات العميل"
            );

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // Alt + 2
        // -----------------------------------------------------

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT2) {

            switchTab(
                    "بيانات العميل الصحية",
                    this::createHealthDataTab
            );

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // Alt + 3
        // -----------------------------------------------------

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT3) {

            switchTab(
                    "معلومات شخصية",
                    this::createLifeInformationTab
            );

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // Alt + 4
        // -----------------------------------------------------

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT4) {

            switchTab(
                    "الأمراض",
                    this::diseas
            );

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // Alt + 5
        // -----------------------------------------------------

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT5) {

            switchTab(
                    "الحساسية",
                    this::Allergy
            );

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // F2
        // -----------------------------------------------------

        if (event.getCode()
                == KeyCode.F2) {

            clearBasicData();

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // Ctrl + Shift + R
        // -----------------------------------------------------

        if (event.isControlDown()
                && event.isShiftDown()
                && event.getCode()
                == KeyCode.R) {

            clearAllFields();

            event.consume();

            return;
        }

        // -----------------------------------------------------
        // ESC
        // -----------------------------------------------------

        if (event.getCode()
                == KeyCode.ESCAPE) {

            setStatus(
                    "جاهز"
            );

            event.consume();
        }
    }

    // =========================================================
    // Input protection
    // =========================================================

    private void protectTextInput(
            TextInputControl control,
            int maxLength
    ) {

        UnaryOperator<TextFormatter.Change> filter =
                change -> {

                    String controlText =
                            change.getControlNewText();

                    if (controlText.length()
                            > maxLength) {

                        return null;
                    }

                    String insertedText =
                            change.getText();

                    if (insertedText == null
                            || insertedText.isEmpty()) {

                        return change;
                    }

                    StringBuilder cleaned =
                            new StringBuilder();

                    boolean allowNewLines =
                            control instanceof TextArea;

                    for (char character :
                            insertedText.toCharArray()) {

                        if (character == '\n'
                                || character == '\r') {

                            if (allowNewLines) {
                                cleaned.append(
                                        character
                                );
                            }

                            continue;
                        }

                        if (Character.isISOControl(
                                character
                        )) {

                            continue;
                        }

                        cleaned.append(
                                character
                        );
                    }

                    change.setText(
                            cleaned.toString()
                    );

                    return change;
                };

        control.setTextFormatter(
                new TextFormatter<>(
                        filter
                )
        );
    }

    private void protectDigits(
            TextField field,
            int maxLength
    ) {

        UnaryOperator<TextFormatter.Change> filter =
                change -> {

                    String text =
                            change.getControlNewText();

                    if (text.isEmpty()) {
                        return change;
                    }

                    if (!text.matches(
                            "\\d{0,"
                                    + maxLength
                                    + "}"
                    )) {

                        return null;
                    }

                    return change;
                };

        field.setTextFormatter(
                new TextFormatter<>(
                        filter
                )
        );
    }

    private void protectDecimal(
            TextField field,
            int integerDigits,
            int decimalDigits
    ) {

        String regex =
                "\\d{0,"
                        + integerDigits
                        + "}(\\.\\d{0,"
                        + decimalDigits
                        + "})?";

        UnaryOperator<TextFormatter.Change> filter =
                change -> {

                    String text =
                            change.getControlNewText();

                    if (text.isEmpty()) {
                        return change;
                    }

                    if (!text.matches(regex)) {
                        return null;
                    }

                    return change;
                };

        field.setTextFormatter(
                new TextFormatter<>(
                        filter
                )
        );
    }

    // =========================================================
    // UI helpers
    // =========================================================

    private VBox createCard() {

        VBox card =
                new VBox(12);

        card.getStyleClass().add(
                "fas-card"
        );

        card.setFillWidth(
                true
        );

        return card;
    }

    private GridPane createFormGrid() {

        GridPane grid =
                new GridPane();

        grid.setHgap(18);

        grid.setVgap(14);

        ColumnConstraints labelColumn =
                new ColumnConstraints();

        labelColumn.setMinWidth(
                130
        );

        labelColumn.setPrefWidth(
                150
        );

        ColumnConstraints fieldColumn =
                new ColumnConstraints();

        fieldColumn.setHgrow(
                Priority.ALWAYS
        );

        grid.getColumnConstraints()
                .addAll(
                        labelColumn,
                        fieldColumn
                );

        return grid;
    }

    private Label fieldLabel(
            String text,
            boolean required
    ) {

        Label label =
                new Label(
                        required
                                ? text + " *"
                                : text
                );

        label.getStyleClass().add(
                required
                        ? "fas-field-label-required"
                        : "fas-field-label"
        );

        return label;
    }

    private void addField(
            GridPane grid,
            int row,
            String labelText,
            Node node
    ) {

        grid.add(
                fieldLabel(
                        labelText,
                        false
                ),
                0,
                row
        );

        grid.add(
                node,
                1,
                row
        );

        if (node instanceof Region region) {

            region.setMaxWidth(
                    Double.MAX_VALUE
            );
        }
    }

    private void addTextAreaRow(
            GridPane grid,
            int row,
            String label,
            TextArea area
    ) {

        addField(
                grid,
                row,
                label,
                area
        );
    }

    private Button createQuickButton(
            String icon,
            String title,
            String shortcut,
            Runnable action
    ) {

        Button button =
                new Button();

        VBox box =
                new VBox(4);

        box.setAlignment(
                Pos.CENTER
        );

        Label iconLabel =
                new Label(icon);

        iconLabel.getStyleClass().add(
                "fas-quick-icon"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass().add(
                "fas-quick-title"
        );

        Label shortcutLabel =
                new Label(shortcut);

        shortcutLabel.getStyleClass().add(
                "fas-quick-shortcut"
        );

        box.getChildren().addAll(
                iconLabel,
                titleLabel,
                shortcutLabel
        );

        button.setGraphic(
                box
        );

        button.setPrefWidth(
                155
        );

        button.setPrefHeight(
                105
        );

        button.getStyleClass().add(
                "fas-quick-button"
        );

        applyTooltip(
                button,
                title + "\n" + shortcut
        );

        button.setOnAction(
                e -> action.run()
        );

        return button;
    }

    private Button createActionButton(
            String icon,
            String text,
            String styleClass,
            String tooltip
    ) {

        Button button =
                new Button(
                        icon + "  " + text
                );

        button.getStyleClass().add(
                styleClass
        );

        button.setMinHeight(
                40
        );

        applyTooltip(
                button,
                tooltip
        );

        return button;
    }

    private Separator createSeparator() {

        Separator separator =
                new Separator();

        separator.getStyleClass().add(
                "fas-inner-separator"
        );

        return separator;
    }

    private ScrollPane createScroll(
            Node content
    ) {

        ScrollPane scroll =
                new ScrollPane(
                        content
                );

        scroll.setFitToWidth(
                true
        );

        scroll.setFitToHeight(
                false
        );

        scroll.getStyleClass().add(
                "fas-scroll"
        );

        return scroll;
    }

    private void applyTooltip(
            Control control,
            String text
    ) {

        Tooltip tooltip =
                new Tooltip(
                        text
                );

        tooltip.setShowDelay(
                javafx.util.Duration.millis(
                        350
                )
        );

        tooltip.setHideDelay(
                javafx.util.Duration.millis(
                        100
                )
        );

        control.setTooltip(
                tooltip
        );
    }

    private void setStatus(
            String text
    ) {

        statusLabel.setText(
                text
        );

        if (text.contains(
                "نجاح"
        )) {

            statusLabel.setTextFill(
                    Color.web(
                            "#16A34A"
                    )
            );

        } else if (text.contains(
                "خطأ"
        )) {

            statusLabel.setTextFill(
                    Color.web(
                            "#DC2626"
                    )
            );

        } else {

            statusLabel.setTextFill(
                    Color.web(
                            "#64748B"
                    )
            );
        }
    }

    // =========================================================
    // Completion
    // =========================================================

    private void updateCompletion() {

        int total = 0;

        int completed = 0;

        // -----------------------------------------------------
        // Basic
        // -----------------------------------------------------

        total += 4;

        if (!nameField.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (!phoneField.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (genderGroup.getSelectedToggle()
                != null) {

            completed++;
        }

        if (datePicker.getValue()
                != null) {

            completed++;
        }

        // -----------------------------------------------------
        // Health
        // -----------------------------------------------------

        total += 4;

        if (!currentMedication.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (!medicalHistory.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (!familyMedicalHistory.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (!healthNotes.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        // -----------------------------------------------------
        // Lifestyle
        // -----------------------------------------------------

        total += 3;

        if (!mealsNumber.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (!budgetField.getText()
                .trim()
                .isEmpty()) {

            completed++;
        }

        if (!breakfastArea.getText()
                .trim()
                .isEmpty()
                ||
                !lunchArea.getText()
                        .trim()
                        .isEmpty()
                ||
                !dinnerArea.getText()
                        .trim()
                        .isEmpty()) {

            completed++;
        }

        // -----------------------------------------------------
        // Dynamic
        // -----------------------------------------------------

        total += 2;

        if (!diseasesContainer
                .getChildren()
                .isEmpty()) {

            completed++;
        }

        if (!allergyContainer
                .getChildren()
                .isEmpty()) {

            completed++;
        }

        double progress =
                total == 0
                        ? 0
                        : (double) completed / total;

        completionBar.setProgress(
                progress
        );

        int percent =
                (int) Math.round(
                        progress * 100
                );

        completionLabel.setText(
                percent + "%"
        );
    }

    private void attachCompletionListeners() {

        if (completionListenersAttached) {
            return;
        }

        completionListenersAttached = true;

        nameField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        phoneField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        currentMedication.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        medicalHistory.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        familyMedicalHistory.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        healthNotes.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        mealsNumber.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        budgetField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        breakfastArea.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        lunchArea.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );

        dinnerArea.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateCompletion()
                );
    }

    // =========================================================
    // Dynamic grid extraction
    // =========================================================

    private GridPane findGridPane(
            VBox pane
    ) {

        for (Node node :
                pane.getChildren()) {

            if (node instanceof GridPane grid) {

                return grid;
            }
        }

        return null;
    }

    private Node findNodeByRow(
            GridPane grid,
            int row
    ) {

        for (Node node :
                grid.getChildren()) {

            Integer nodeRow =
                    GridPane.getRowIndex(
                            node
                    );

            Integer nodeColumn =
                    GridPane.getColumnIndex(
                            node
                    );

            if (nodeRow == null) {
                nodeRow = 0;
            }

            if (nodeColumn == null) {
                nodeColumn = 0;
            }

            if (nodeRow == row
                    && nodeColumn == 1) {

                return node;
            }
        }

        throw new IllegalStateException(
                "تعذر العثور على حقل الصف: "
                        + row
        );
    }

    // =========================================================
    // Alert
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

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
                .getStyleClass()
                .add(
                        "fas-alert"
                );

        if (getScene() != null
                && getScene().getWindow() != null) {

            alert.initOwner(
                    getScene().getWindow()
            );
        }

        alert.showAndWait();
    }
}