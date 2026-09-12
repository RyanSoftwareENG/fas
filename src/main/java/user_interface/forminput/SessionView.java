package user_interface.forminput;

import api.ClientAPI;
import api.ClientApiManager;
import api.FoodAPI;
import api.SessionAPI;
import app.Main;
import entities.*;

import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import runner.BackgroundRunner;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static user_interface.forminput.Desgin.*;

/**
 * واجهة إدارة الجلسة.
 *
 * الأقسام:
 * - بيانات الجلسة
 * - الفحوصات
 * - الخطة الغذائية
 * - توزيع الوجبات
 * - القياسات
 *
 * الاختصارات:
 * - Alt + 1 : بيانات الجلسة
 * - Alt + 2 : الفحوصات
 * - Alt + 3 : الخطة الغذائية
 * - Alt + 4 : القياسات
 * - Ctrl + S : حفظ
 * - Ctrl + Shift + R : إعادة ضبط
 * - F2 : مسح بيانات الجلسة
 */
public class SessionView extends VBox {

    // =========================================================
    // MAIN UI
    // =========================================================

    private final TabPane tabPane =
            new TabPane();

    private final HBox sectionNavigation =
            new HBox(8);

    private final VBox contentWrapper =
            new VBox();

    private final Label pageTitle =
            new Label("بيانات الجلسة");

    private final Label pageSubtitle =
            new Label(
                    "إنشاء جلسة جديدة وإدخال القياسات والخطة الغذائية"
            );

    private final Label statusLabel =
            new Label("جاهز");

    private final Button saveButton =
            new Button("حفظ بيانات الجلسة");

    private final Map<String, Button> sectionButtons =
            new HashMap<>();

    // =========================================================
    // SESSION
    // =========================================================

    private final TextField clientName =
            new TextField();

    private final TextField price =
            new TextField();

    /**
     * إدخال وقت الجلسة.
     *
     * يقبل:
     * 60   -> 60 دقيقة
     * 90   -> 90 دقيقة
     * 1:30 -> ساعة ونصف
     */
    private final TextField sessionDurationField =
            new TextField();

    private final TextArea sessionNotes =
            new TextArea();

    private Client currentClient;

    // =========================================================
    // NUTRITION PLAN
    // =========================================================

    private final TextField targetGoal =
            new TextField();

    private final DatePicker startDate =
            new DatePicker();

    private final DatePicker endDate =
            new DatePicker();

    private final ComboBox<String> statusComboBox =
            new ComboBox<>();

    private final TextField proteinField =
            new TextField();

    private final TextField fatField =
            new TextField();

    private final TextField carbsField =
            new TextField();

    private final TextField totalCaloriesField =
            new TextField();

    /**
     * عدد الوجبات.
     *
     * أصبح TextField بدل Spinner.
     */
    private final TextField mealsCountField =
            new TextField();

    private final TextField waterIntakeField =
            new TextField();

    private final TextArea mealDistribution =
            new TextArea();

    private final TextArea planNotesArea =
            new TextArea();

    private NutritionPlan plan;

    // =========================================================
    // EXAMS
    // =========================================================

    private final VBox examsContainer =
            new VBox(12);

    // =========================================================
    // BODY DATA
    // =========================================================

    private final TextField height =
            new TextField();

    private final TextField weight =
            new TextField();

    private final TextField activityFactor =
            new TextField();

    private final TextField bodyFatPercentage =
            new TextField();

    private final TextField SMM =
            new TextField();

    private final TextField muscleMass =
            new TextField();

    private final TextField physicalActivity =
            new TextField();

    private final TextField armC =
            new TextField();

    private final TextField chestC =
            new TextField();

    private final TextField waistC =
            new TextField();

    private final TextField abdominalC =
            new TextField();

    private final TextField hipC =
            new TextField();

    private final TextField midThigh =
            new TextField();

    private final TextField calfC =
            new TextField();

    // =========================================================
    // MEALS
    // =========================================================

    private final VBox mealsContainer =
            new VBox(12);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SessionView()
            throws IOException, InterruptedException {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setFillWidth(true);

        getStyleClass().add(
                "fas-root"
        );

        configureGeneralUI();
        configureFields();

        // مهم: كان ناقصًا في النسخة السابقة
        configureSectionNavigation();

        configureTabs();
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
                "بيانات الجلسة"
        );
    }

    // =========================================================
    // GENERAL UI
    // =========================================================

    private void configureGeneralUI() {

        tabPane.setTabClosingPolicy(
                TabPane.TabClosingPolicy.UNAVAILABLE
        );

        tabPane.getStyleClass().add(
                "fas-tab-pane"
        );

        tabPane.setFocusTraversable(false);
    }

    private void configureFields() {

        clientName.setPromptText(
                "ابحث عن اسم العميل..."
        );

        price.setPromptText(
                "سعر الجلسة"
        );

        sessionDurationField.setPromptText(
                "مثال: 60 أو 1:30"
        );

        sessionNotes.setPromptText(
                "ملاحظات الجلسة..."
        );

        targetGoal.setPromptText(
                "مثال: زيادة الوزن"
        );

        totalCaloriesField.setPromptText(
                "تُحسب آليًا"
        );

        mealsCountField.setPromptText(
                "مثال: 3"
        );

        waterIntakeField.setPromptText(
                "مثال: 3 لتر"
        );

        mealDistribution.setPromptText(
                "توزيع الوجبات والملاحظات..."
        );

        planNotesArea.setPromptText(
                "ملاحظات إضافية للخطة..."
        );

        physicalActivity.setPromptText(
                "مثال: موظف مكتبي خامل"
        );

        configureTextArea(
                sessionNotes,
                3
        );

        configureTextArea(
                mealDistribution,
                3
        );

        configureTextArea(
                planNotesArea,
                2
        );

        height.setPromptText("0.0");
        weight.setPromptText("0.0");
        activityFactor.setPromptText("0.0");
        bodyFatPercentage.setPromptText("0.0");
        SMM.setPromptText("0.0");
        muscleMass.setPromptText("0.0");
        armC.setPromptText("0.0");
        chestC.setPromptText("0.0");
        waistC.setPromptText("0.0");
        abdominalC.setPromptText("0.0");
        hipC.setPromptText("0.0");
        midThigh.setPromptText("0.0");
        calfC.setPromptText("0.0");

        // ============================
        // DECIMAL FIELDS
        // ============================

        makeDecimalOnly(price);
        makeDecimalOnly(proteinField);
        makeDecimalOnly(fatField);
        makeDecimalOnly(carbsField);
        makeDecimalOnly(totalCaloriesField);

        makeDecimalOnly(height);
        makeDecimalOnly(weight);
        makeDecimalOnly(activityFactor);
        makeDecimalOnly(bodyFatPercentage);
        makeDecimalOnly(SMM);
        makeDecimalOnly(muscleMass);
        makeDecimalOnly(armC);
        makeDecimalOnly(chestC);
        makeDecimalOnly(waistC);
        makeDecimalOnly(abdominalC);
        makeDecimalOnly(hipC);
        makeDecimalOnly(midThigh);
        makeDecimalOnly(calfC);

        // ============================
        // INTEGER FIELDS
        // ============================

        makeIntegerOnly(
                mealsCountField
        );

        // وقت الجلسة يسمح بالأرقام و :
        configureDurationInput();

        // ============================
        // DATES
        // ============================

        startDate.setEditable(false);
        endDate.setEditable(false);

        configureDateRules();

        // ============================
        // PLAN STATUS
        // ============================

        statusComboBox.getItems().setAll(
                "نشط",
                "مكتمل",
                "ملغي"
        );

        statusComboBox.setValue(
                "نشط"
        );

        statusComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        mealsCountField.setMaxWidth(
                Double.MAX_VALUE
        );

        sessionDurationField.setMaxWidth(
                Double.MAX_VALUE
        );

        // ============================
        // INPUT STYLES
        // ============================

        addInputStyle(
                clientName,
                price,
                sessionDurationField,
                targetGoal,
                totalCaloriesField,
                mealsCountField,
                waterIntakeField,
                proteinField,
                fatField,
                carbsField,
                height,
                weight,
                activityFactor,
                bodyFatPercentage,
                SMM,
                muscleMass,
                physicalActivity,
                armC,
                chestC,
                waistC,
                abdominalC,
                hipC,
                midThigh,
                calfC
        );
    }

    /**
     * تقييد حقل وقت الجلسة.
     *
     * يسمح:
     * 0-9
     * :
     */
    private void configureDurationInput() {

        sessionDurationField.setTextFormatter(
                new TextFormatter<String>(
                        change -> {

                            String newText =
                                    change.getControlNewText();

                            if (newText.isEmpty()) {
                                return change;
                            }

                            if (!newText.matches(
                                    "\\d{0,2}(:\\d{0,2})?"
                            )) {
                                return null;
                            }

                            return change;
                        }
                )
        );
    }

    /**
     * تقييد الحقول الصحيحة.
     */
    private void makeIntegerOnly(
            TextField field
    ) {

        field.setTextFormatter(
                new TextFormatter<String>(
                        change -> {

                            String newText =
                                    change.getControlNewText();

                            if (newText.isEmpty()) {
                                return change;
                            }

                            if (!newText.matches(
                                    "\\d{0,2}"
                            )) {
                                return null;
                            }

                            return change;
                        }
                )
        );
    }

    private void configureTextArea(
            TextArea area,
            int rows
    ) {

        area.setWrapText(true);

        area.setPrefRowCount(
                rows
        );

        area.setMinHeight(
                70
        );

        area.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );
    }

    private void addInputStyle(
            Control... controls
    ) {

        for (Control control : controls) {

            control.getStyleClass().add(
                    "fas-session-input"
            );
        }
    }

    // =========================================================
    // TABS
    // =========================================================

    private void configureTabs()
            throws IOException, InterruptedException {

        tabPane.getTabs().clear();

        tabPane.getTabs().add(
                sessionTab()
        );
    }

    // =========================================================
    // HEADER
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

        header.getChildren().addAll(
                titleBox,
                spacer
        );

        return header;
    }

    // =========================================================
    // SECTION NAVIGATION
    // =========================================================

    private void configureSectionNavigation() {

        sectionNavigation.setAlignment(
                Pos.CENTER_RIGHT
        );

        sectionNavigation.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        sectionNavigation.setMaxWidth(
                Double.MAX_VALUE
        );

        sectionNavigation.getStyleClass().add(
                "fas-section-navigation"
        );

        sectionNavigation.getChildren().clear();

        sectionNavigation.getChildren().addAll(

                createSectionButton(
                        "بيانات الجلسة",
                        "⏱️",
                        "Alt + 1",
                        () ->
                                showTab(
                                        "بيانات الجلسة"
                                )
                ),

                createSectionButton(
                        "الفحوصات",
                        "🔬",
                        "Alt + 2",
                        () ->
                                switchOrAddTab(
                                        "الفحوصات",
                                        this::examinationTab
                                )
                ),

                createSectionButton(
                        "الخطة الغذائية",
                        "📋",
                        "Alt + 3",
                        () ->
                                switchOrAddTab(
                                        "الخطة الغذائية",
                                        this::nutritionPlanTab
                                )
                ),

                createSectionButton(
                        "القياسات",
                        "📏",
                        "Alt + 4",
                        () ->
                                switchOrAddTab(
                                        "القياسات",
                                        this::bodyDataTab
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

        Button button =
                new Button();

        HBox content =
                new HBox(8);

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

        button.setMinHeight(40);
        button.setPrefHeight(40);
        button.setMinWidth(130);
        button.setPrefWidth(145);

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
    // SESSION TAB
    // =========================================================

    public Tab sessionTab()
            throws IOException, InterruptedException {

        Tab tab =
                new Tab(
                        "بيانات الجلسة"
                );

        VBox root =
                createScrollContent();

        VBox card =
                createCard();

        Label title =
                new Label(
                        "بيانات الجلسة الأساسية"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "حدد العميل وأدخل بيانات الجلسة الأساسية"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        GridPane grid =
                createFormGrid(
                        2
                );

        ClientAPI clientAPI =
                ClientApiManager
                        .getInstance()
                        .getClientAPI();

        List<Client> clients =
                clientAPI.allClients();

        configureClientSearch(
                clients
        );

        grid.add(
                fieldLabel(
                        "العميل",
                        true
                ),
                0,
                0
        );

        grid.add(
                clientName,
                1,
                0
        );

        grid.add(
                fieldLabel(
                        "سعر الجلسة",
                        true
                ),
                0,
                1
        );

        grid.add(
                price,
                1,
                1
        );

        // وقت الجلسة
        grid.add(
                fieldLabel(
                        "مدة الجلسة",
                        true
                ),
                0,
                2
        );

        HBox durationBox =
                createDurationBox();

        grid.add(
                durationBox,
                1,
                2
        );

        grid.add(
                fieldLabel(
                        "ملاحظات",
                        false
                ),
                0,
                3
        );

        grid.add(
                sessionNotes,
                1,
                3
        );

        Button healthButton =
                createQuickAction(
                        "🏥",
                        "الخطة والوجبات",
                        "Alt + 3",
                        "الانتقال إلى الخطة الغذائية",
                        () ->
                                switchOrAddTab(
                                        "الخطة الغذائية",
                                        this::nutritionPlanTab
                                )
                );

        Button examButton =
                createQuickAction(
                        "🔬",
                        "الفحوصات",
                        "Alt + 2",
                        "إضافة فحوصات العميل",
                        () ->
                                switchOrAddTab(
                                        "الفحوصات",
                                        this::examinationTab
                                )
                );

        Button bodyButton =
                createQuickAction(
                        "📏",
                        "القياسات",
                        "Alt + 4",
                        "إدخال القياسات الجسدية",
                        () ->
                                switchOrAddTab(
                                        "القياسات",
                                        this::bodyDataTab
                                )
                );

        HBox quickActions =
                new HBox(12);

        quickActions.setAlignment(
                Pos.CENTER_RIGHT
        );

        quickActions.getChildren().addAll(
                examButton,
                healthButton,
                bodyButton
        );

        card.getChildren().addAll(
                title,
                subtitle,
                createSeparator(),
                grid,
                createSeparator(),
                quickActions
        );

        root.getChildren().add(
                card
        );

        tab.setContent(
                createScrollPane(root)
        );

        return tab;
    }

    // =========================================================
    // CLIENT SEARCH
    // =========================================================

    private void configureClientSearch(
            List<Client> clients
    ) {

        if (clients == null) {
            clients = new ArrayList<>();
        }

        List<Client> finalClients =
                clients;

        ContextMenu suggestionsPopup =
                new ContextMenu();

        suggestionsPopup.getStyleClass().add(
                "fas-suggestions-menu"
        );

        PauseTransition debounce =
                new PauseTransition(
                        Duration.millis(250)
                );

        clientName.textProperty().addListener(
                (obs, oldValue, newValue) -> {

                    debounce.setOnFinished(
                            event -> {

                                suggestionsPopup
                                        .getItems()
                                        .clear();

                                String query =
                                        newValue == null
                                                ? ""
                                                : newValue
                                                .trim()
                                                .toLowerCase();

                                if (query.isEmpty()) {

                                    suggestionsPopup.hide();

                                    currentClient = null;

                                    return;
                                }

                                boolean found =
                                        false;

                                for (Client client :
                                        finalClients) {

                                    if (client == null
                                            || client
                                            .getFullName()
                                            == null) {

                                        continue;
                                    }

                                    String name =
                                            client
                                                    .getFullName()
                                                    .trim();

                                    if (name.toLowerCase()
                                            .contains(
                                                    query
                                            )) {

                                        MenuItem item =
                                                new MenuItem(
                                                        name
                                                );

                                        item.getStyleClass().add(
                                                "fas-suggestion-item"
                                        );

                                        item.setOnAction(
                                                action -> {

                                                    clientName.setText(
                                                            name
                                                    );

                                                    currentClient =
                                                            client;

                                                    suggestionsPopup
                                                            .hide();
                                                }
                                        );

                                        suggestionsPopup
                                                .getItems()
                                                .add(item);

                                        found = true;
                                    }
                                }

                                if (!found) {

                                    MenuItem addItem =
                                            new MenuItem(
                                                    "⚠ العميل غير موجود — إضافة عميل جديد"
                                            );

                                    addItem.getStyleClass().add(
                                            "fas-suggestion-add"
                                    );

                                    addItem.setOnAction(
                                            action -> {

                                                suggestionsPopup
                                                        .hide();

                                                try {

                                                    Main.setView(
                                                            new ClientView(),
                                                            "Client"
                                                    );

                                                } catch (Exception ex) {

                                                    showAlert(
                                                            Alert.AlertType.ERROR,
                                                            "العميل",
                                                            "تعذر فتح واجهة إضافة العميل."
                                                    );
                                                }
                                            }
                                    );

                                    suggestionsPopup
                                            .getItems()
                                            .add(addItem);
                                }

                                if (!suggestionsPopup
                                        .getItems()
                                        .isEmpty()) {

                                    if (!suggestionsPopup
                                            .isShowing()) {

                                        suggestionsPopup.show(
                                                clientName,
                                                Side.BOTTOM,
                                                0,
                                                0
                                        );
                                    }

                                } else {

                                    suggestionsPopup.hide();
                                }
                            }
                    );

                    debounce.playFromStart();
                }
        );
    }

    // =========================================================
    // DURATION
    // =========================================================

    private HBox createDurationBox() {

        Label hint =
                new Label(
                        "مثال: 60 أو 1:30"
                );

        hint.getStyleClass().add(
                "fas-field-hint"
        );

        HBox box =
                new HBox(
                        10,
                        sessionDurationField,
                        hint
                );

        box.setAlignment(
                Pos.CENTER_RIGHT
        );

        HBox.setHgrow(
                sessionDurationField,
                Priority.ALWAYS
        );

        return box;
    }

    /**
     * يحول إدخال المستخدم إلى LocalTime.
     *
     * أمثلة:
     * 60   -> 01:00
     * 90   -> 01:30
     * 1:30 -> 01:30
     */
    private LocalTime getSessionDuration() {

        String input =
                sessionDurationField
                        .getText()
                        .trim();

        if (input.isEmpty()) {

            throw new IllegalArgumentException(
                    "يرجى إدخال مدة الجلسة."
            );
        }

        try {

            if (input.contains(":")) {

                String[] parts =
                        input.split(":");

                if (parts.length != 2) {

                    throw new IllegalArgumentException(
                            "صيغة وقت الجلسة غير صحيحة. استخدم مثلًا 1:30."
                    );
                }

                int hours =
                        Integer.parseInt(
                                parts[0]
                        );

                int minutes =
                        Integer.parseInt(
                                parts[1]
                        );

                if (hours < 0
                        || hours > 23
                        || minutes < 0
                        || minutes > 59) {

                    throw new IllegalArgumentException(
                            "مدة الجلسة غير صحيحة."
                    );
                }

                LocalTime result =
                        LocalTime.of(
                                hours,
                                minutes
                        );

                if (result.equals(
                        LocalTime.MIDNIGHT
                )) {

                    throw new IllegalArgumentException(
                            "مدة الجلسة يجب أن تكون أكبر من صفر."
                    );
                }

                return result;
            }

            int totalMinutes =
                    Integer.parseInt(
                            input
                    );

            if (totalMinutes <= 0) {

                throw new IllegalArgumentException(
                        "مدة الجلسة يجب أن تكون أكبر من صفر."
                );
            }

            if (totalMinutes >= 24 * 60) {

                throw new IllegalArgumentException(
                        "مدة الجلسة لا يمكن أن تتجاوز 23 ساعة و59 دقيقة."
                );
            }

            int hours =
                    totalMinutes / 60;

            int minutes =
                    totalMinutes % 60;

            return LocalTime.of(
                    hours,
                    minutes
            );

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "صيغة مدة الجلسة غير صحيحة. استخدم مثلًا 60 أو 1:30."
            );
        }
    }

    // =========================================================
    // EXAMINATION TAB
    // =========================================================

    public Tab examinationTab() {

        Tab tab =
                new Tab(
                        "الفحوصات"
                );

        VBox root =
                createScrollContent();

        VBox card =
                createCard();

        HBox header =
                createSectionHeader(
                        "الفحوصات",
                        "إضافة صور الفحوصات والملاحظات"
                );

        Button addButton =
                createPrimaryAction(
                        "+",
                        "إضافة فحص",
                        "إضافة فحص جديد"
                );

        addButton.setOnAction(
                e ->
                        addExaminationCard()
        );

        header.getChildren().add(
                addButton
        );

        card.getChildren().addAll(
                header,
                createSeparator(),
                examsContainer
        );

        root.getChildren().add(
                card
        );

        tab.setContent(
                createScrollPane(root)
        );

        return tab;
    }

    private void addExaminationCard() {

        VBox pane =
                new VBox(12);

        pane.getStyleClass().add(
                "fas-exam-card"
        );

        pane.setPadding(
                new Insets(16)
        );

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        TextField name =
                new TextField();

        name.setPromptText(
                "اسم الفحص"
        );

        HBox.setHgrow(
                name,
                Priority.ALWAYS
        );

        addInputStyle(
                name
        );

        Button delete =
                createDeleteButton(
                        "حذف الفحص"
                );

        delete.setOnAction(
                event ->
                        examsContainer
                                .getChildren()
                                .remove(pane)
        );

        header.getChildren().addAll(
                name,
                delete
        );

        ImageView preview =
                new ImageView();

        preview.setFitWidth(
                180
        );

        preview.setFitHeight(
                140
        );

        preview.setPreserveRatio(
                true
        );

        preview.getStyleClass().add(
                "fas-exam-preview"
        );

        Button upload =
                createSecondaryAction(
                        "📷",
                        "اختيار صورة",
                        "رفع صورة الفحص"
                );

        upload.setOnAction(
                event -> {

                    if (getScene() == null
                            || getScene().getWindow() == null) {

                        return;
                    }

                    FileChooser chooser =
                            new FileChooser();

                    chooser.setTitle(
                            "اختيار صورة الفحص"
                    );

                    chooser.getExtensionFilters()
                            .add(
                                    new FileChooser.ExtensionFilter(
                                            "صور",
                                            "*.png",
                                            "*.jpg",
                                            "*.jpeg"
                                    )
                            );

                    File selected =
                            chooser.showOpenDialog(
                                    getScene().getWindow()
                            );

                    if (selected == null) {
                        return;
                    }

                    try {

                        preview.setImage(
                                new Image(
                                        selected
                                                .toURI()
                                                .toString()
                                )
                        );

                        String savedPath =
                                saveImageToProjectFolder(
                                        selected
                                );

                        pane.setUserData(
                                savedPath
                        );

                    } catch (Exception ex) {

                        showAlert(
                                Alert.AlertType.ERROR,
                                "الصورة",
                                "تعذر حفظ صورة الفحص."
                        );
                    }
                }
        );

        TextArea examNotes =
                new TextArea();

        examNotes.setPromptText(
                "ملاحظات الفحص..."
        );

        configureTextArea(
                examNotes,
                3
        );

        VBox imageBox =
                new VBox(
                        8,
                        upload,
                        preview
                );

        imageBox.setAlignment(
                Pos.CENTER
        );

        HBox content =
                new HBox(
                        15,
                        imageBox,
                        examNotes
                );

        HBox.setHgrow(
                examNotes,
                Priority.ALWAYS
        );

        pane.getChildren().addAll(
                header,
                content
        );

        examsContainer.getChildren().add(
                pane
        );
    }

    // =========================================================
    // NUTRITION PLAN
    // =========================================================

    public Tab nutritionPlanTab() {

        Tab tab =
                new Tab(
                        "الخطة الغذائية"
                );

        VBox root =
                createScrollContent();

        VBox card =
                createCard();

        Label title =
                new Label(
                        "الخطة الغذائية"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "حدد المدة والحالة والاحتياجات الغذائية الأساسية"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        GridPane grid =
                createFormGrid(
                        4
                );

        addField(
                grid,
                0,
                "هدف الخطة",
                targetGoal,
                true
        );

        addField(
                grid,
                1,
                "البداية",
                startDate,
                true
        );

        addField(
                grid,
                2,
                "النهاية",
                endDate,
                true
        );

        addField(
                grid,
                3,
                "حالة الخطة",
                statusComboBox,
                false
        );

        addField(
                grid,
                4,
                "عدد الوجبات",
                mealsCountField,
                false
        );

        addField(
                grid,
                0,
                2,
                "البروتين (g)",
                proteinField,
                false
        );

        addField(
                grid,
                1,
                2,
                "الدهون (g)",
                fatField,
                false
        );

        addField(
                grid,
                2,
                2,
                "الكارب (g)",
                carbsField,
                false
        );

        addField(
                grid,
                3,
                2,
                "السعرات",
                totalCaloriesField,
                false
        );

        addField(
                grid,
                4,
                2,
                "الاحتياج المائي",
                waterIntakeField,
                false
        );

        addTextAreaField(
                grid,
                5,
                "توزيع الوجبات",
                mealDistribution
        );

        addTextAreaField(
                grid,
                6,
                "ملاحظات الخطة",
                planNotesArea
        );

        /*
         * زر فتح توزيع الوجبات.
         */
        Button mealsButton =
                createPrimaryAction(
                        "🍎",
                        "فتح توزيع الوجبات",
                        "فتح قسم توزيع وإضافة الأطعمة"
                );

        mealsButton.setMaxWidth(
                Double.MAX_VALUE
        );

        mealsButton.setMinHeight(
                46
        );

        mealsButton.setOnAction(
                e ->
                        switchOrAddTab(
                                "توزيع الوجبات",
                                this::planFoodItemsTab
                        )
        );

        grid.add(
                mealsButton,
                0,
                7,
                4,
                1
        );

        card.getChildren().addAll(
                title,
                subtitle,
                createSeparator(),
                grid
        );

        root.getChildren().add(
                card
        );

        tab.setContent(
                createScrollPane(root)
        );

        return tab;
    }

    // =========================================================
// BODY DATA
// =========================================================

    public Tab bodyDataTab() {

        Tab tab =
                new Tab(
                        "القياسات"
                );

        VBox root =
                createScrollContent();

        VBox card =
                createCard();

        Label title =
                new Label(
                        "القياسات الجسدية"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "أدخل القياسات اللازمة لمتابعة حالة العميل"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        /*
         * 6 أعمدة:
         *
         * 0 = Label
         * 1 = Field
         *
         * 2 = Label
         * 3 = Field
         *
         * 4 = Label
         * 5 = Field
         */
        GridPane grid =
                createFormGrid(
                        6
                );

        // =====================================================
        // ROW 0
        // =====================================================

        addField(
                grid,
                0,
                0,
                "الطول",
                height
        );

        addField(
                grid,
                0,
                2,
                "الوزن",
                weight
        );

        addField(
                grid,
                0,
                4,
                "الدهون %",
                bodyFatPercentage
        );

        // =====================================================
        // ROW 1
        // =====================================================

        addField(
                grid,
                1,
                0,
                "الخصر",
                waistC
        );

        addField(
                grid,
                1,
                2,
                "الصدر",
                chestC
        );

        addField(
                grid,
                1,
                4,
                "الأرداف",
                hipC
        );

        // =====================================================
        // ROW 2
        // =====================================================

        addField(
                grid,
                2,
                0,
                "الذراع",
                armC
        );

        addField(
                grid,
                2,
                2,
                "البطن",
                abdominalC
        );

        addField(
                grid,
                2,
                4,
                "الفخذ",
                midThigh
        );

        // =====================================================
        // ROW 3
        // =====================================================

        addField(
                grid,
                3,
                0,
                "الساق",
                calfC
        );

        addField(
                grid,
                3,
                2,
                "كتلة العضل",
                muscleMass
        );

        addField(
                grid,
                3,
                4,
                "SMM",
                SMM
        );

        // =====================================================
        // ROW 4
        // =====================================================

        addField(
                grid,
                4,
                0,
                "معامل النشاط",
                activityFactor
        );

        addField(
                grid,
                4,
                2,
                "النشاط الحركي",
                physicalActivity
        );

        card.getChildren().addAll(
                title,
                subtitle,
                createSeparator(),
                grid
        );

        root.getChildren().add(
                card
        );

        tab.setContent(
                createScrollPane(root)
        );

        return tab;
    }
    // =========================================================
    // PLAN FOOD ITEMS
    // =========================================================

    public Tab planFoodItemsTab() {

        Tab tab =
                new Tab(
                        "توزيع الوجبات"
                );

        VBox root =
                createScrollContent();

        VBox card =
                createCard();

        /*
         * =====================================================
         * شريط علوي واضح جدًا
         * =====================================================
         */
        HBox mealTopBar =
                createMealTopBar();

        List<FoodItem> allFoods;

        try {

            FoodAPI foodAPI =
                    ClientApiManager
                            .getInstance()
                            .getFoodAPI();

            allFoods =
                    foodAPI.getAllFood();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            showAlert(
                    Alert.AlertType.ERROR,
                    "الاتصال",
                    "تمت مقاطعة الاتصال بالسيرفر."
            );

            return tab;

        } catch (
                IOException |
                RuntimeException e
        ) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "الأطعمة",
                    e.getMessage() == null
                            ? "تعذر تحميل قائمة الأطعمة."
                            : e.getMessage()
            );

            return tab;
        }

        List<FoodItem> finalAllFoods =
                allFoods;

        /*
         * إضافة أول وجبة مباشرة إذا أراد المستخدم
         * من الزر.
         */
        Button addMealButton =
                (Button)
                        mealTopBar
                                .getProperties()
                                .get("addMealButton");

        if (addMealButton != null) {

            addMealButton.setOnAction(
                    event ->
                            addMealCard(
                                    finalAllFoods
                            )
            );
        }

        card.getChildren().addAll(
                mealTopBar,
                createSeparator(),
                mealsContainer
        );

        root.getChildren().add(
                card
        );

        tab.setContent(
                createScrollPane(root)
        );

        return tab;
    }

    /**
     * شريط علوي لقسم توزيع الوجبات.
     *
     * يحتوي:
     * - العنوان
     * - وصف مختصر
     * - عدد الأصناف الحالية
     * - زر إضافة وجبة واضح
     */
    private HBox createMealTopBar() {

        HBox outer =
                new HBox(15);

        outer.setAlignment(
                Pos.CENTER_RIGHT
        );

        outer.setPadding(
                new Insets(
                        4,
                        0,
                        4,
                        0
                )
        );

        VBox titleBox =
                new VBox(4);

        titleBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label title =
                new Label(
                        "توزيع الوجبات"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "أضف الأطعمة وحدد نوع الوجبة والكمية"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label countLabel =
                new Label();

        countLabel.getStyleClass().add(
                "fas-status-label"
        );

        updateMealCountLabel(
                countLabel
        );

        mealsContainer
                .getChildren()
                .addListener(
                        (javafx.collections.ListChangeListener<Node>)
                                change ->
                                        updateMealCountLabel(
                                                countLabel
                                        )
                );

        Button addMealButton =
                createPrimaryAction(
                        "＋",
                        "إضافة وجبة",
                        "إضافة صنف غذائي جديد"
                );

        addMealButton.setMinHeight(
                46
        );

        addMealButton.setMinWidth(
                165
        );

        /*
         * نخزن الزر مؤقتًا لكي نربطه بعد تحميل الأطعمة.
         */
        outer.getProperties().put(
                "addMealButton",
                addMealButton
        );

        HBox counterBox =
                new HBox(
                        7,
                        new Label("الأصناف:"),
                        countLabel
                );

        counterBox.setAlignment(
                Pos.CENTER
        );

        outer.getChildren().addAll(
                titleBox,
                spacer,
                counterBox,
                addMealButton
        );

        return outer;
    }

    private void updateMealCountLabel(
            Label label
    ) {

        int count =
                mealsContainer
                        .getChildren()
                        .size();

        label.setText(
                String.valueOf(count)
        );
    }

    // =========================================================
    // ADD MEAL
    // =========================================================

    private void addMealCard(
            List<FoodItem> allFoods
    ) {

        if (allFoods == null
                || allFoods.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "الأطعمة",
                    "لا توجد أصناف غذائية متاحة حاليًا."
            );

            return;
        }

        VBox card =
                new VBox(12);

        card.getStyleClass().add(
                "fas-meal-card"
        );

        card.setPadding(
                new Insets(16)
        );

        ObjectProperty<FoodItem> selectedFood =
                new SimpleObjectProperty<>();

        HBox firstRow =
                new HBox(10);

        firstRow.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button selectFoodButton =
                new Button(
                        "🔍 اختر الصنف الغذائي"
                );

        selectFoodButton.getStyleClass().add(
                "fas-secondary-button"
        );

        selectFoodButton.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                selectFoodButton,
                Priority.ALWAYS
        );

        selectFoodButton.setOnAction(
                event -> {

                    FoodSelectionDialog dialog =
                            new FoodSelectionDialog(
                                    allFoods
                            );

                    Optional<FoodItem> result =
                            dialog.showAndWait();

                    result.ifPresent(
                            food -> {

                                selectedFood.set(
                                        food
                                );

                                selectFoodButton
                                        .setText(
                                                "✓ "
                                                        + food
                                                        .getFoodName()
                                        );

                                selectFoodButton
                                        .getStyleClass()
                                        .remove(
                                                "fas-secondary-button"
                                        );

                                selectFoodButton
                                        .getStyleClass()
                                        .add(
                                                "fas-selected-food-button"
                                        );
                            }
                    );
                }
        );

        ComboBox<String> mealType =
                new ComboBox<>();

        mealType.getItems().addAll(
                "Breakfast",
                "Lunch",
                "Dinner",
                "Snack"
        );

        mealType.setValue(
                "Lunch"
        );

        mealType.setPrefWidth(
                140
        );

        firstRow.getChildren().addAll(
                new Label("الصنف"),
                selectFoodButton,
                new Label("نوع الوجبة"),
                mealType
        );

        HBox secondRow =
                new HBox(10);

        secondRow.setAlignment(
                Pos.CENTER_RIGHT
        );

        TextField quantity =
                new TextField();

        quantity.setPromptText(
                "الكمية (جرام)"
        );

        quantity.setPrefWidth(
                130
        );

        makeDecimalOnly(
                quantity
        );

        quantity.getStyleClass().add(
                "fas-session-input"
        );

        Label unit =
                new Label(
                        "جرام (g)"
                );

        secondRow.getChildren().addAll(
                new Label("الكمية"),
                quantity,
                unit
        );

        Button delete =
                createDeleteButton(
                        "حذف الصنف"
                );

        delete.setOnAction(
                event ->
                        mealsContainer
                                .getChildren()
                                .remove(card)
        );

        card.getProperties().put(
                "foodProperty",
                selectedFood
        );

        card.getProperties().put(
                "type",
                mealType
        );

        card.getProperties().put(
                "qty",
                quantity
        );

        card.getChildren().addAll(
                firstRow,
                secondRow,
                delete
        );

        mealsContainer.getChildren().add(
                card
        );
    }

    // =========================================================
    // SAVE
    // =========================================================

    public Session saveSession() {

        if (currentClient == null
                || currentClient.getClientID() == null
                || currentClient.getClientID() <= 0) {

            throw new IllegalStateException(
                    "لم يتم تحديد العميل. يرجى اختيار العميل من قائمة البحث."
            );
        }

        LocalTime duration =
                getSessionDuration();

        BigDecimal sessionPrice =
                getDecimal(
                        price,
                        "سعر الجلسة"
                );

        plan = null;

        if (!targetGoal.getText()
                .trim()
                .isEmpty()) {

            if (startDate.getValue() == null
                    || endDate.getValue() == null) {

                throw new IllegalArgumentException(
                        "يرجى تحديد تاريخ بداية ونهاية الخطة."
                );
            }

            if (endDate.getValue()
                    .isBefore(
                            startDate.getValue()
                    )) {

                throw new IllegalArgumentException(
                        "تاريخ نهاية الخطة لا يمكن أن يسبق تاريخ البداية."
                );
            }

            String status =
                    switch (
                            statusComboBox.getValue()
                            ) {

                        case "مكتمل" ->
                                "Completed";

                        case "ملغي" ->
                                "Cancelled";

                        default ->
                                "Active";
                    };

            int mealsCount =
                    getMealsCount();

            plan =
                    new NutritionPlan(
                            targetGoal.getText()
                                    .trim(),
                            startDate.getValue(),
                            endDate.getValue(),
                            status,
                            mealDistribution
                                    .getText()
                                    .trim(),
                            getDecimal(
                                    proteinField,
                                    "البروتين"
                            ),
                            getDecimal(
                                    fatField,
                                    "الدهون"
                            ),
                            getDecimal(
                                    carbsField,
                                    "الكارب"
                            )
                    );

            String caloriesText =
                    totalCaloriesField
                            .getText()
                            .trim();

            if (caloriesText.isEmpty()) {

                plan.setTotalCalories(
                        BigDecimal.ZERO
                );

            } else {

                plan.setTotalCalories(
                        getDecimal(
                                totalCaloriesField,
                                "إجمالي السعرات"
                        )
                );
            }

            plan.setMealsCount(
                    mealsCount
            );

            plan.setWaterIntake(
                    waterIntakeField
                            .getText()
                            .trim()
            );

            plan.setNotes(
                    planNotesArea
                            .getText()
                            .trim()
            );

            ArrayList<PlanFoodItem> items =
                    new ArrayList<>();

            for (Node node :
                    mealsContainer.getChildren()) {

                if (!(node instanceof VBox card)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                ObjectProperty<FoodItem>
                        foodProperty =
                        (ObjectProperty<FoodItem>)
                                card.getProperties()
                                        .get(
                                                "foodProperty"
                                        );

                ComboBox<String> typeCombo =
                        (ComboBox<String>)
                                card.getProperties()
                                        .get(
                                                "type"
                                        );

                TextField quantityField =
                        (TextField)
                                card.getProperties()
                                        .get(
                                                "qty"
                                        );

                FoodItem selectedFood =
                        foodProperty == null
                                ? null
                                : foodProperty.get();

                /*
                 * إذا أضاف المستخدم بطاقة ولم يختر
                 * صنفًا، نتجاوزها.
                 */
                if (selectedFood == null) {
                    continue;
                }

                String type =
                        typeCombo == null
                                || typeCombo.getValue() == null
                                ? "Lunch"
                                : typeCombo.getValue();

                if (quantityField == null
                        || quantityField
                        .getText()
                        .trim()
                        .isEmpty()) {

                    throw new IllegalArgumentException(
                            "يرجى إدخال كمية الصنف: "
                                    + selectedFood
                                    .getFoodName()
                    );
                }

                BigDecimal quantity =
                        getDecimal(
                                quantityField,
                                "كمية "
                                        + selectedFood
                                        .getFoodName()
                        );

                if (quantity.signum() <= 0) {

                    throw new IllegalArgumentException(
                            "كمية الصنف يجب أن تكون أكبر من صفر: "
                                    + selectedFood
                                    .getFoodName()
                    );
                }

                items.add(
                        new PlanFoodItem(
                                0L,
                                selectedFood
                                        .getFoodName(),
                                selectedFood
                                        .getFoodItemId(),
                                type,
                                quantity
                        )
                );
            }

            plan.setSelectedFoods(
                    items
            );
        }

        ArrayList<Examination> exams =
                new ArrayList<>();

        for (Node node :
                examsContainer.getChildren()) {

            if (!(node instanceof VBox pane)) {
                continue;
            }

            if (pane.getChildren().size() < 2) {
                continue;
            }

            Node first =
                    pane.getChildren().get(0);

            Node second =
                    pane.getChildren().get(1);

            if (!(first instanceof HBox header)) {
                continue;
            }

            if (!(second instanceof HBox body)) {
                continue;
            }

            if (header.getChildren().isEmpty()) {
                continue;
            }

            if (!(header.getChildren().get(0)
                    instanceof TextField nameField)) {

                continue;
            }

            if (body.getChildren().size() < 2) {
                continue;
            }

            if (!(body.getChildren().get(1)
                    instanceof TextArea noteArea)) {

                continue;
            }

            String name =
                    nameField.getText()
                            .trim();

            if (name.isEmpty()) {
                continue;
            }

            String imagePath =
                    pane.getUserData()
                            instanceof String path
                            ? path
                            : null;

            exams.add(
                    new Examination(
                            name,
                            imagePath,
                            noteArea
                                    .getText()
                                    .trim()
                    )
            );
        }

        BodyData bodyData =
                saveBodyData();

        String notes =
                sessionNotes.getText()
                        .trim();

        return new Session(
                0L,
                currentClient,
                duration.toString(),
                sessionPrice,
                bodyData,
                plan,
                exams,
                notes
        );
    }

    // =========================================================
    // MEALS COUNT
    // =========================================================

    private int getMealsCount() {

        String text =
                mealsCountField
                        .getText()
                        .trim();

        if (text.isEmpty()) {

            return 0;
        }

        try {

            int count =
                    Integer.parseInt(
                            text
                    );

            if (count < 0
                    || count > 20) {

                throw new IllegalArgumentException(
                        "عدد الوجبات يجب أن يكون بين 0 و20."
                );
            }

            return count;

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "عدد الوجبات يجب أن يكون رقمًا صحيحًا."
            );
        }
    }

    // =========================================================
    // BODY DATA SAVE
    // =========================================================

    public BodyData saveBodyData() {

        return new BodyData.Builder()
                .height(
                        getDecimal(
                                height,
                                "الطول"
                        )
                )
                .weight(
                        getDecimal(
                                weight,
                                "الوزن"
                        )
                )
                .bodyFatPercentage(
                        getDecimal(
                                bodyFatPercentage,
                                "نسبة الدهون"
                        )
                )
                .activityFactor(
                        getDecimal(
                                activityFactor,
                                "معامل النشاط"
                        )
                )
                .smm(
                        getDecimal(
                                SMM,
                                "SMM"
                        )
                )
                .muscleMass(
                        getDecimal(
                                muscleMass,
                                "كتلة العضل"
                        )
                )
                .physicalActivity(
                        physicalActivity
                                .getText()
                                .trim()
                )
                .arm_C(
                        getDecimal(
                                armC,
                                "محيط الذراع"
                        )
                )
                .chest_C(
                        getDecimal(
                                chestC,
                                "محيط الصدر"
                        )
                )
                .waist_C(
                        getDecimal(
                                waistC,
                                "محيط الخصر"
                        )
                )
                .abdominal_C(
                        getDecimal(
                                abdominalC,
                                "محيط البطن"
                        )
                )
                .hip_C(
                        getDecimal(
                                hipC,
                                "محيط الأرداف"
                        )
                )
                .midThigh_C(
                        getDecimal(
                                midThigh,
                                "محيط الفخذ"
                        )
                )
                .calf_C(
                        getDecimal(
                                calfC,
                                "محيط الساق"
                        )
                )
                .build();
    }

    // =========================================================
    // HANDLE SAVE
    // =========================================================

    public void handelSave() {

        Session session;

        try {

            setStatus(
                    "جاري التحقق من البيانات..."
            );

            session =
                    saveSession();

        } catch (NumberFormatException ex) {

            setStatus(
                    "خطأ في البيانات الرقمية"
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "خطأ في التنسيق الرقمي",
                    ex.getMessage()
            );

            return;

        } catch (
                IllegalStateException |
                IllegalArgumentException ex
        ) {

            setStatus(
                    "بيانات غير صالحة"
            );

            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه في البيانات",
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

        final Session finalSession =
                session;

        setProcessingState(
                true
        );

        BackgroundRunner.run(

                "جاري حفظ بيانات الجلسة...",

                () -> {

                    SessionAPI sessionAPI =
                            ClientApiManager
                                    .getInstance()
                                    .getSessionAPI();

                    return sessionAPI.save(
                            finalSession
                    );
                },

                () -> {

                    setProcessingState(
                            false
                    );

                    setStatus(
                            "تم الحفظ بنجاح"
                    );

                    String selectedName =
                            currentClient != null
                                    ? currentClient
                                    .getFullName()
                                    : "العميل";

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "تم الحفظ بنجاح",
                            "تم حفظ وتوثيق بيانات الجلسة "
                                    + "والقياسات للعميل ["
                                    + selectedName
                                    + "] بنجاح."
                    );

                    clearAllFields();
                }
        );
    }

    // =========================================================
    // PROCESSING
    // =========================================================

    private void setProcessingState(
            boolean processing
    ) {

        saveButton.setDisable(
                processing
        );

        if (processing) {

            saveButton.setText(
                    "جاري الحفظ...");

        } else {

            saveButton.setText(
                    "حفظ بيانات الجلسة"
            );
        }
    }

    // =========================================================
    // TAB NAVIGATION
    // =========================================================

    public void switchOrAddTab(
            String title,
            Supplier<Tab> tabCreator
    ) {

        Tab existing =
                findTab(title);

        if (existing != null) {

            tabPane.getSelectionModel()
                    .select(
                            existing
                    );

            updateNavigationState(
                    title
            );

            updatePageHeader(
                    title
            );

            return;
        }

        BackgroundRunner.run(
                "جاري تجهيز الواجهة...",

                () -> {

                    Thread.sleep(120);

                    return true;
                },

                () -> {

                    Tab newTab;

                    try {

                        newTab =
                                tabCreator.get();

                    } catch (Exception ex) {

                        showAlert(
                                Alert.AlertType.ERROR,
                                "الواجهة",
                                ex.getMessage() == null
                                        ? "تعذر تجهيز القسم المطلوب."
                                        : ex.getMessage()
                        );

                        return;
                    }

                    tabPane.getTabs().add(
                            newTab
                    );

                    showTab(
                            title
                    );
                }
        );
    }

    private Tab findTab(
            String title
    ) {

        return tabPane
                .getTabs()
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

                    case "بيانات الجلسة" ->
                            "بيانات الجلسة";

                    case "الفحوصات" ->
                            "الفحوصات";

                    case "الخطة الغذائية" ->
                            "الخطة الغذائية";

                    case "القياسات" ->
                            "القياسات";

                    default ->
                            null;
                };

        if (key == null) {
            return;
        }

        Button activeButton =
                sectionButtons.get(key);

        if (activeButton != null) {

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

            case "بيانات الجلسة" -> {

                pageTitle.setText(
                        "بيانات الجلسة"
                );

                pageSubtitle.setText(
                        "إنشاء جلسة جديدة وإدخال البيانات الأساسية"
                );
            }

            case "الفحوصات" -> {

                pageTitle.setText(
                        "الفحوصات"
                );

                pageSubtitle.setText(
                        "إدارة صور الفحوصات والملاحظات"
                );
            }

            case "الخطة الغذائية" -> {

                pageTitle.setText(
                        "الخطة الغذائية"
                );

                pageSubtitle.setText(
                        "إعداد خطة التغذية والاحتياجات اليومية"
                );
            }

            case "توزيع الوجبات" -> {

                pageTitle.setText(
                        "توزيع الوجبات"
                );

                pageSubtitle.setText(
                        "إضافة الأطعمة وتحديد الكمية ونوع الوجبة"
                );
            }

            case "القياسات" -> {

                pageTitle.setText(
                        "القياسات الجسدية"
                );

                pageSubtitle.setText(
                        "متابعة القياسات والمؤشرات الجسدية"
                );
            }

            default -> {
            }
        }
    }

    // =========================================================
    // SHORTCUTS
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

        if (event.isControlDown()
                && event.getCode()
                == KeyCode.S) {

            handelSave();

            event.consume();

            return;
        }

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT1) {

            showTab(
                    "بيانات الجلسة"
            );

            event.consume();

            return;
        }

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT2) {

            switchOrAddTab(
                    "الفحوصات",
                    this::examinationTab
            );

            event.consume();

            return;
        }

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT3) {

            switchOrAddTab(
                    "الخطة الغذائية",
                    this::nutritionPlanTab
            );

            event.consume();

            return;
        }

        if (event.isAltDown()
                && event.getCode()
                == KeyCode.DIGIT4) {

            switchOrAddTab(
                    "القياسات",
                    this::bodyDataTab
            );

            event.consume();

            return;
        }

        if (event.isControlDown()
                && event.isShiftDown()
                && event.getCode()
                == KeyCode.R) {

            clearAllFields();

            event.consume();

            return;
        }

        if (event.getCode()
                == KeyCode.F2) {

            clearSessionData();

            event.consume();

            return;
        }

        if (event.getCode()
                == KeyCode.ESCAPE) {

            setStatus(
                    "جاهز"
            );

            event.consume();
        }
    }

    // =========================================================
    // CONTEXT MENU
    // =========================================================

    private void configureContextMenu() {

        ContextMenu contextMenu =
                new ContextMenu();

        contextMenu.getStyleClass().add(
                "fas-context-menu"
        );

        MenuItem refreshAll =
                new MenuItem(
                        "↻ إعادة ضبط الجلسة بالكامل"
                );

        MenuItem refreshSession =
                new MenuItem(
                        "⏱️ مسح بيانات الجلسة"
                );

        MenuItem refreshExams =
                new MenuItem(
                        "🔬 مسح الفحوصات"
                );

        MenuItem refreshPlan =
                new MenuItem(
                        "📋 مسح الخطة الغذائية"
                );

        MenuItem refreshBody =
                new MenuItem(
                        "📏 مسح القياسات"
                );

        refreshAll.setOnAction(
                e ->
                        clearAllFields()
        );

        refreshSession.setOnAction(
                e ->
                        clearSessionData()
        );

        refreshExams.setOnAction(
                e ->
                        clearExamsData()
        );

        refreshPlan.setOnAction(
                e ->
                        clearPlanData()
        );

        refreshBody.setOnAction(
                e ->
                        clearBodyDataFields()
        );

        contextMenu.getItems().addAll(
                refreshAll,
                new SeparatorMenuItem(),
                refreshSession,
                refreshExams,
                refreshPlan,
                refreshBody
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
    // CLEAR
    // =========================================================

    private void clearSessionData() {

        clientName.clear();

        price.clear();

        sessionDurationField.clear();

        sessionNotes.clear();

        currentClient = null;

        setStatus(
                "تم مسح بيانات الجلسة"
        );
    }

    private void clearExamsData() {

        examsContainer
                .getChildren()
                .clear();

        removeTab(
                "الفحوصات"
        );

        setStatus(
                "تم مسح الفحوصات"
        );

        showTab(
                "بيانات الجلسة"
        );
    }

    private void clearPlanData() {

        targetGoal.clear();

        startDate.setValue(
                null
        );

        endDate.setValue(
                null
        );

        statusComboBox.setValue(
                "نشط"
        );

        mealDistribution.clear();

        proteinField.clear();

        fatField.clear();

        carbsField.clear();

        totalCaloriesField.clear();

        mealsCountField.clear();

        waterIntakeField.clear();

        planNotesArea.clear();

        mealsContainer
                .getChildren()
                .clear();

        plan = null;

        removeTab(
                "الخطة الغذائية"
        );

        removeTab(
                "توزيع الوجبات"
        );

        setStatus(
                "تم مسح الخطة الغذائية"
        );

        showTab(
                "بيانات الجلسة"
        );
    }

    private void clearBodyDataFields() {

        height.clear();
        weight.clear();
        activityFactor.clear();
        bodyFatPercentage.clear();
        SMM.clear();
        muscleMass.clear();
        physicalActivity.clear();
        armC.clear();
        chestC.clear();
        waistC.clear();
        abdominalC.clear();
        hipC.clear();
        midThigh.clear();
        calfC.clear();

        removeTab(
                "القياسات"
        );

        setStatus(
                "تم مسح القياسات"
        );

        showTab(
                "بيانات الجلسة"
        );
    }

    private void clearAllFields() {

        clearSessionData();

        examsContainer
                .getChildren()
                .clear();

        mealsContainer
                .getChildren()
                .clear();

        targetGoal.clear();

        startDate.setValue(
                null
        );

        endDate.setValue(
                null
        );

        statusComboBox.setValue(
                "نشط"
        );

        mealDistribution.clear();

        proteinField.clear();

        fatField.clear();

        carbsField.clear();

        totalCaloriesField.clear();

        mealsCountField.clear();

        waterIntakeField.clear();

        planNotesArea.clear();

        height.clear();
        weight.clear();
        activityFactor.clear();
        bodyFatPercentage.clear();
        SMM.clear();
        muscleMass.clear();
        physicalActivity.clear();
        armC.clear();
        chestC.clear();
        waistC.clear();
        abdominalC.clear();
        hipC.clear();
        midThigh.clear();
        calfC.clear();

        plan = null;

        tabPane.getTabs()
                .removeIf(
                        tab ->
                                !tab.getText()
                                        .equals(
                                                "بيانات الجلسة"
                                        )
                );

        showTab(
                "بيانات الجلسة"
        );

        setStatus(
                "تمت إعادة ضبط الجلسة بالكامل"
        );
    }

    private void removeTab(
            String title
    ) {

        tabPane.getTabs()
                .removeIf(
                        tab ->
                                tab.getText()
                                        .equals(title)
                );
    }

    // =========================================================
    // DATE RULES
    // =========================================================

    private void configureDateRules() {

        startDate.setDayCellFactory(
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

                        /*
                         * لا نمنع التاريخ الماضي.
                         */
                    }
                }
        );

        endDate.valueProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue != null
                                    && startDate.getValue()
                                    != null
                                    && newValue.isBefore(
                                    startDate.getValue()
                            )) {

                                endDate.setValue(
                                        null
                                );

                                showAlert(
                                        Alert.AlertType.WARNING,
                                        "تاريخ الخطة",
                                        "تاريخ النهاية لا يمكن أن يسبق تاريخ البداية."
                                );
                            }
                        }
                );

        endDate.setDayCellFactory(
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
                                && startDate.getValue()
                                != null
                                && date.isBefore(
                                startDate.getValue()
                        )) {

                            setDisable(true);
                        }
                    }
                }
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private VBox createScrollContent() {

        VBox content =
                new VBox(15);

        content.setPadding(
                new Insets(4)
        );

        content.setFillWidth(
                true
        );

        return content;
    }

    private ScrollPane createScrollPane(
            VBox content
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

        scroll.setMaxSize(
                Double.MAX_VALUE,
                Double.MAX_VALUE
        );

        scroll.getStyleClass().add(
                "fas-scroll"
        );

        return scroll;
    }

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

    /**
     * إنشاء GridPane مع ColumnConstraints مستقلة
     * حتى لا نعيد استخدام نفس ColumnConstraints object.
     */
    private GridPane createFormGrid(
            int columns
    ) {

        GridPane grid =
                new GridPane();

        grid.setHgap(16);

        grid.setVgap(14);

        grid.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        for (int i = 0; i < columns; i++) {

            ColumnConstraints constraint =
                    new ColumnConstraints();

            if (i % 2 == 0) {

                constraint.setMinWidth(
                        105
                );

                constraint.setPrefWidth(
                        125
                );

                constraint.setHalignment(
                        HPos.RIGHT
                );

            } else {

                constraint.setHgrow(
                        Priority.ALWAYS
                );

                constraint.setFillWidth(
                        true
                );
            }

            grid.getColumnConstraints()
                    .add(
                            constraint
                    );
        }

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
            String label,
            Node node,
            boolean required
    ) {

        addField(
                grid,
                row,
                0,
                label,
                node,
                required
        );
    }

    private void addField(
            GridPane grid,
            int row,
            int startColumn,
            String label,
            Node node,
            boolean required
    ) {

        grid.add(
                fieldLabel(
                        label,
                        required
                ),
                startColumn,
                row
        );

        grid.add(
                node,
                startColumn + 1,
                row
        );

        if (node instanceof Region region) {

            region.setMaxWidth(
                    Double.MAX_VALUE
            );
        }
    }

    private void addField(
            GridPane grid,
            int row,
            String label,
            Node node
    ) {

        addField(
                grid,
                row,
                0,
                label,
                node,
                false
        );
    }

    private void addField(
            GridPane grid,
            int row,
            int startColumn,
            String label,
            Node node
    ) {

        addField(
                grid,
                row,
                startColumn,
                label,
                node,
                false
        );
    }

    private void addTextAreaField(
            GridPane grid,
            int row,
            String label,
            TextArea area
    ) {

        grid.add(
                fieldLabel(
                        label,
                        false
                ),
                0,
                row
        );

        grid.add(
                area,
                1,
                row,
                3,
                1
        );

        GridPane.setFillWidth(
                area,
                true
        );

        GridPane.setHgrow(
                area,
                Priority.ALWAYS
        );
    }

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
                new Label(title);

        titleLabel.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitleLabel =
                new Label(subtitle);

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

    private Button createQuickAction(
            String icon,
            String title,
            String shortcut,
            String tooltip,
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
                150
        );

        button.setPrefHeight(
                95
        );

        button.getStyleClass().add(
                "fas-quick-button"
        );

        applyTooltip(
                button,
                tooltip
        );

        button.setOnAction(
                e ->
                        action.run()
        );

        return button;
    }

    private Button createPrimaryAction(
            String icon,
            String text,
            String tooltip
    ) {

        Button button =
                new Button(
                        icon + "  " + text
                );

        button.getStyleClass().add(
                "fas-primary-button"
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

    private Button createSecondaryAction(
            String icon,
            String text,
            String tooltip
    ) {

        Button button =
                new Button(
                        icon + "  " + text
                );

        button.getStyleClass().add(
                "fas-secondary-button"
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

    private Button createDeleteButton(
            String tooltip
    ) {

        Button button =
                new Button(
                        "حذف"
                );

        button.getStyleClass().add(
                "fas-delete-button"
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

    private void applyTooltip(
            Control control,
            String text
    ) {

        Tooltip tooltip =
                new Tooltip(text);

        tooltip.setShowDelay(
                Duration.millis(300)
        );

        tooltip.setHideDelay(
                Duration.millis(100)
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
    }

    // =========================================================
    // FOOTER
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

        Region statusDot =
                new Region();

        statusDot.getStyleClass().add(
                "fas-status-dot"
        );

        HBox status =
                new HBox(
                        7,
                        statusLabel,
                        statusDot
                );

        status.setAlignment(
                Pos.CENTER_RIGHT
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button clear =
                new Button(
                        "إعادة ضبط"
                );

        clear.getStyleClass().add(
                "fas-secondary-button"
        );

        applyTooltip(
                clear,
                "إعادة ضبط الجلسة بالكامل\n"
                        + "Ctrl + Shift + R"
        );

        clear.setOnAction(
                e ->
                        clearAllFields()
        );

        saveButton.getStyleClass().add(
                "fas-primary-button"
        );

        saveButton.setMinWidth(
                180
        );

        saveButton.setPrefHeight(
                42
        );

        saveButton.setOnAction(
                e ->
                        handelSave()
        );

        applyTooltip(
                saveButton,
                "حفظ بيانات الجلسة بالكامل\nCtrl + S"
        );

        Label shortcut =
                new Label(
                        "Ctrl + S"
                );

        shortcut.getStyleClass().add(
                "fas-footer-shortcut"
        );

        footer.getChildren().addAll(
                status,
                spacer,
                clear,
                shortcut,
                saveButton
        );

        return footer;
    }

    // =========================================================
    // FILE
    // =========================================================

    private String saveImageToProjectFolder(
            File file
    ) throws IOException {

        File destDir =
                new File(
                        "data\\exminatoinphoto"
                );

        if (!destDir.exists()
                && !destDir.mkdirs()) {

            throw new IOException(
                    "تعذر إنشاء مجلد صور الفحوصات."
            );
        }

        String extension =
                "";

        String fileName =
                file.getName();

        int dotIndex =
                fileName.lastIndexOf('.');

        if (dotIndex >= 0) {

            extension =
                    fileName.substring(
                            dotIndex
                    );
        }

        String uniqueName =
                UUID.randomUUID()
                        + extension;

        File destination =
                new File(
                        destDir,
                        uniqueName
                );

        Files.copy(
                file.toPath(),
                destination.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );

        return destination.getAbsolutePath();
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

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