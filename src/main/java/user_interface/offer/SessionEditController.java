package user_interface.offer;

import api.ClientAPI;
import api.ClientApiManager;
import api.FoodAPI;
import api.SessionAPI;
import dto.SessionListDTO;
import entities.BodyData;
import entities.Client;
import entities.Examination;
import entities.FoodItem;
import entities.NutritionPlan;
import entities.PlanFoodItem;
import entities.Session;
import runner.BackgroundRunner;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import user_interface.forminput.FoodSelectionDialog;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SessionEditController {

    // =========================================================
    // Main TabPane
    // =========================================================

    @FXML
    private TabPane mainTabPane;


    // =========================================================
    // Header
    // =========================================================

    @FXML
    private Label lblSessionId;

    @FXML
    private Label lblClientId;


    // =========================================================
    // Session
    // =========================================================

    @FXML
    private TextField txtPrice;

    @FXML
    private TextField txtDuration;

    @FXML
    private TextArea txtNotes;


    // =========================================================
    // Body Data
    // =========================================================

    @FXML
    private TextField txtHeight;

    @FXML
    private TextField txtWeight;

    @FXML
    private TextField txtFatPercentage;

    @FXML
    private TextField txtSMM;

    @FXML
    private TextField txtMuscleMass;

    @FXML
    private TextField txtActivityFactor;

    @FXML
    private ComboBox<String> comboPhysicalActivity;

    @FXML
    private TextField txtArm;

    @FXML
    private TextField txtChest;

    @FXML
    private TextField txtWaist;

    @FXML
    private TextField txtAbdominal;

    @FXML
    private TextField txtHip;

    @FXML
    private TextField txtThigh;

    @FXML
    private TextField txtCalf;


    // =========================================================
    // Nutrition Plan
    // =========================================================

    @FXML
    private TextField txtPlanGoal;

    @FXML
    private ComboBox<String> comboPlanStatus;

    @FXML
    private TextField txtMealDistribution;

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private TextField txtProteinAmount;

    @FXML
    private TextField txtCarbAmount;

    @FXML
    private TextField txtFatAmount;

    @FXML
    private TextField txtTotalCalories;

    @FXML
    private TextField txtMealsCount;

    @FXML
    private TextField txtWaterIntake;

    @FXML
    private TextArea txtNote;


    // =========================================================
    // Examinations
    // =========================================================

    @FXML
    private TableView<Examination> tableExaminations;

    @FXML
    private TableColumn<Examination, Number> colExamId;

    @FXML
    private TableColumn<Examination, String> colExamName;

    @FXML
    private TableColumn<Examination, String> colExamNotes;

    @FXML
    private TableColumn<Examination, HBox> colExamImagePreview;


    // =========================================================
    // Examination Editing
    // =========================================================

    @FXML
    private TextField txtSelectedExamName;

    @FXML
    private TextField txtSelectedExamNotes;

    @FXML
    private Label lblImagePath;


    // =========================================================
    // Current Session / Client
    // =========================================================

    private Session activeSession;

    private Client activeClient;


    // =========================================================
    // Examination List
    // =========================================================

    private final ObservableList<Examination> examList =
            FXCollections.observableArrayList();


    // =========================================================
    // Meal Distribution
    // =========================================================

    private VBox mealsContainer;

    private Tab mealDistributionTab;

    /*
     * كل عنصر هنا يمثل Card واحدة في واجهة توزيع الوجبات.
     *
     * food       = الطعام المحدد
     * mealType   = نوع الوجبة
     * quantity   = الكمية
     * card       = بطاقة الواجهة نفسها
     */
    private final List<MealEntry> mealEntries =
            new ArrayList<>();


    // =========================================================
    // Selected Image
    // =========================================================

    private String newlySelectedImagePath = "";


    // =========================================================
    // Initialize
    // =========================================================

    @FXML
    public void initialize() {

        setupPhysicalActivity();

        setupPlanStatus();

        setupExaminationTable();

        setupExaminationSelection();
    }


    // =========================================================
    // Physical Activity
    // =========================================================

    private void setupPhysicalActivity() {

        comboPhysicalActivity.setItems(
                FXCollections.observableArrayList(
                        "خامل جداً",
                        "نشاط خفيف",
                        "نشاط متوسط",
                        "نشط جداً",
                        "نشط للغاية"
                )
        );
    }


    // =========================================================
    // Plan Status
    // =========================================================

    private void setupPlanStatus() {

        comboPlanStatus.setItems(
                FXCollections.observableArrayList(
                        "Active",
                        "Pending",
                        "Completed",
                        "Cancelled"
                )
        );
    }


    // =========================================================
    // Examination Table
    // =========================================================

    private void setupExaminationTable() {

        tableExaminations.setStyle(
                "-fx-cell-size: 85px;"
        );


        colExamId.setCellValueFactory(
                cellData -> {

                    Long id =
                            cellData.getValue()
                                    .getExaminationId();

                    return new SimpleLongProperty(
                            id != null ? id : 0L
                    );
                }
        );


        colExamName.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                safeString(
                                        cellData.getValue()
                                                .getExaminationName()
                                )
                        )
        );


        colExamNotes.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                safeString(
                                        cellData.getValue()
                                                .getNotes()
                                )
                        )
        );


        colExamImagePreview.setCellValueFactory(
                cellData ->
                        createImagePreview(
                                cellData.getValue()
                                        .getExaminationImage()
                        )
        );
    }


    // =========================================================
    // Examination Selection
    // =========================================================

    private void setupExaminationSelection() {

        tableExaminations
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldSelection, newSelection) -> {

                            if (newSelection == null) {
                                return;
                            }

                            txtSelectedExamName.setText(
                                    safeString(
                                            newSelection
                                                    .getExaminationName()
                                    )
                            );

                            txtSelectedExamNotes.setText(
                                    safeString(
                                            newSelection.getNotes()
                                    )
                            );

                            newlySelectedImagePath =
                                    safeString(
                                            newSelection
                                                    .getExaminationImage()
                                    );

                            lblImagePath.setText(
                                    newlySelectedImagePath.isEmpty()
                                            ? "لا توجد صورة"
                                            : newlySelectedImagePath
                            );
                        }
                );
    }


    // =========================================================
    // Image Preview
    // =========================================================

    private SimpleObjectProperty<HBox> createImagePreview(
            String path) {

        HBox box =
                new HBox();

        box.setAlignment(
                Pos.CENTER
        );

        if (path == null ||
                path.trim().isEmpty()) {

            box.getChildren().add(
                    new Label("📷 لا توجد صورة")
            );

            return new SimpleObjectProperty<>(
                    box
            );
        }


        try {

            String cleanUrl =
                    path.trim();

            if (!cleanUrl.startsWith("http://") &&
                    !cleanUrl.startsWith("https://") &&
                    !cleanUrl.startsWith("file:")) {

                cleanUrl =
                        new File(cleanUrl)
                                .toURI()
                                .toString();
            }


            Image image =
                    new Image(
                            cleanUrl,
                            75,
                            75,
                            true,
                            true,
                            true
                    );


            ImageView imageView =
                    new ImageView(image);

            imageView.setFitWidth(75);
            imageView.setFitHeight(75);
            imageView.setPreserveRatio(true);

            box.getChildren().add(
                    imageView
            );


        } catch (Exception e) {

            box.getChildren().add(
                    new Label("⚠️ تعذر تحميل الصورة")
            );
        }


        return new SimpleObjectProperty<>(
                box
        );
    }


    // =========================================================
    // Load Session
    // =========================================================

    public void setSessionData(
            SessionListDTO session) {

        if (session == null) {

            showErrorAlert(
                    "خطأ",
                    "لا توجد جلسة محددة."
            );

            return;
        }


        final Session[] sessionHolder =
                new Session[1];

        final Client[] clientHolder =
                new Client[1];

        final Exception[] exceptionHolder =
                new Exception[1];


        BackgroundRunner.run(
                "جاري تحميل بيانات الجلسة... ⏳",

                () -> {

                    try {

                        SessionAPI sessionAPI =
                                ClientApiManager
                                        .getInstance()
                                        .getSessionAPI();

                        sessionHolder[0] =
                                sessionAPI.getAllDataSession(
                                        session.getId()
                                );
                        clientHolder[0] = session.getClient();


                    } catch (Exception e) {

                        exceptionHolder[0] = e;
                    }

                    return true;
                },

                () -> {

                    if (exceptionHolder[0] != null) {

                        showErrorAlert(
                                "خطأ أثناء التحميل",
                                exceptionHolder[0]
                                        .getMessage() != null
                                        ? exceptionHolder[0]
                                        .getMessage()
                                        : "حدث خطأ غير معروف."
                        );

                        return;
                    }


                    activeSession =
                            sessionHolder[0];

                    activeClient =
                            clientHolder[0];


                    if (activeSession == null) {

                        showErrorAlert(
                                "فشل التحميل",
                                "تعذر تحميل الجلسة رقم "
                                        + session.getId()
                        );

                        return;
                    }


                    // =================================================
                    // Session ID
                    // =================================================

                    lblSessionId.setText(
                            String.valueOf(
                                    activeSession.getId()
                            )
                    );


                    // =================================================
                    // Client Name
                    // =================================================

                    String clientName =
                            getClientDisplayName(
                                    activeClient
                            );


                    lblClientId.setText(
                            clientName.isEmpty()
                                    ? (
                                    session.getClientID() != null
                                            ? "عميل غير معروف (ID: "
                                            + session.getClientID()
                                            + ")"
                                            : "عميل غير معروف"
                            )
                                    : clientName
                    );


                    // =================================================
                    // Basic Session
                    // =================================================

                    txtPrice.setText(
                            activeSession.getPrice() != null
                                    ? activeSession.getPrice()
                                    .toPlainString()
                                    : ""
                    );


                    txtDuration.setText(
                            safeString(
                                    activeSession.getDuration()
                            )
                    );


                    txtNotes.setText(
                            safeString(
                                    activeSession.getNotes()
                            )
                    );


                    // =================================================
                    // BodyData
                    // =================================================

                    fillBodyData(
                            activeSession.getBodyData()
                    );


                    // =================================================
                    // NutritionPlan
                    // =================================================

                    fillNutritionPlan(
                            activeSession.getNutritionPlan(),
                            activeSession.getBodyData(),
                            activeClient
                    );


                    // =================================================
                    // Examinations
                    // =================================================

                    examList.clear();

                    if (activeSession.getExaminations() != null) {

                        examList.addAll(
                                activeSession.getExaminations()
                        );
                    }


                    tableExaminations.setItems(
                            examList
                    );

                    tableExaminations.refresh();


                    // =================================================
                    // Reset Meal Tab
                    // =================================================

                    mealEntries.clear();

                    if (mealsContainer != null) {

                        mealsContainer
                                .getChildren()
                                .clear();
                    }

                    mealDistributionTab = null;
                }
        );
    }


    // =========================================================
    // Body Data
    // =========================================================

    private void fillBodyData(
            BodyData bd) {

        if (bd == null) {

            clearBodyFields();

            return;
        }


        txtHeight.setText(
                decimalString(bd.getHeight())
        );

        txtWeight.setText(
                decimalString(bd.getWeight())
        );

        txtFatPercentage.setText(
                decimalString(
                        bd.getBodyFatPercentage()
                )
        );

        txtSMM.setText(
                decimalString(
                        bd.getSmm()
                )
        );

        txtMuscleMass.setText(
                decimalString(
                        bd.getMuscleMass()
                )
        );

        txtActivityFactor.setText(
                decimalString(
                        bd.getActivityFactor()
                )
        );

        comboPhysicalActivity.setValue(
                bd.getPhysicalActivity()
        );

        txtArm.setText(
                decimalString(
                        bd.getArm_C()
                )
        );

        txtChest.setText(
                decimalString(
                        bd.getChest_C()
                )
        );

        txtWaist.setText(
                decimalString(
                        bd.getWaist_C()
                )
        );

        txtAbdominal.setText(
                decimalString(
                        bd.getAbdominal_C()
                )
        );

        txtHip.setText(
                decimalString(
                        bd.getHip_C()
                )
        );

        txtThigh.setText(
                decimalString(
                        bd.getMidThigh_C()
                )
        );

        txtCalf.setText(
                decimalString(
                        bd.getCalf_C()
                )
        );
    }


    // =========================================================
    // Nutrition Plan
    // =========================================================

    private void fillNutritionPlan(
            NutritionPlan plan,
            BodyData bodyData,
            Client client) {

        if (plan == null) {

            clearNutritionFields();

            return;
        }


        txtPlanGoal.setText(
                safeString(
                        plan.getTargetGoal()
                )
        );


        comboPlanStatus.setValue(
                plan.getPlanStatus()
        );


        txtMealDistribution.setText(
                safeString(
                        plan.getMealDistribution()
                )
        );


        dpStartDate.setValue(
                plan.getStartDate()
        );


        dpEndDate.setValue(
                plan.getEndDate()
        );


        txtProteinAmount.setText(
                decimalString(
                        plan.getProteinAmount()
                )
        );


        txtCarbAmount.setText(
                decimalString(
                        plan.getCarbohydratesAmount()
                )
        );


        txtFatAmount.setText(
                decimalString(
                        plan.getFatAmount()
                )
        );


        txtMealsCount.setText(
                plan.getMealsCount() != null
                        ? String.valueOf(
                        plan.getMealsCount()
                )
                        : ""
        );


        txtTotalCalories.setText(
                decimalString(
                        plan.getTotalCalories()
                )
        );


        if (plan.getWaterIntake() != null &&
                !plan.getWaterIntake()
                        .trim()
                        .isEmpty()) {

            txtWaterIntake.setText(
                    plan.getWaterIntake()
            );

        } else {

            try {

                String gender =
                        client != null
                                ? String.valueOf(
                                client.getGender()
                        )
                                : "M";


                BigDecimal weight =
                        bodyData != null &&
                                bodyData.getWeight() != null
                                ? bodyData.getWeight()
                                : BigDecimal.valueOf(
                                70
                        );


                BigDecimal idealWater =
                        plan.calculateIdealWater(
                                weight,
                                gender
                        );


                txtWaterIntake.setText(
                        String.format(
                                Locale.US,
                                "%.1f Ltr",
                                idealWater.doubleValue()
                        )
                );

            } catch (Exception e) {

                txtWaterIntake.clear();
            }
        }


        txtNote.setText(
                safeString(
                        plan.getNotes()
                )
        );
    }


    // =========================================================
    // Open Meal Distribution
    // =========================================================

    @FXML
    private void handleOpenMealDistribution() {

        if (mainTabPane == null) {

            showErrorAlert(
                    "خطأ",
                    "تعذر الوصول إلى TabPane."
            );

            return;
        }


        if (mealDistributionTab != null &&
                mainTabPane.getTabs()
                        .contains(
                                mealDistributionTab
                        )) {

            mainTabPane
                    .getSelectionModel()
                    .select(
                            mealDistributionTab
                    );

            return;
        }


        mealDistributionTab =
                createMealDistributionTab();


        mainTabPane
                .getTabs()
                .add(
                        mealDistributionTab
                );


        mainTabPane
                .getSelectionModel()
                .select(
                        mealDistributionTab
                );
    }


    // =========================================================
    // Create Meal Distribution Tab
    // =========================================================

    private Tab createMealDistributionTab() {

        Tab tab =
                new Tab(
                        "🍽️ توزيع الوجبات"
                );


        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(20)
        );


        mealsContainer =
                new VBox(10);

        mealsContainer.setFillWidth(
                true
        );


        Button addMealBtn =
                new Button(
                        "+ إضافة صنف غذائي للخطة"
                );

        addMealBtn.setMaxWidth(
                Double.MAX_VALUE
        );

        addMealBtn.setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );


        ScrollPane scroll =
                new ScrollPane(
                        mealsContainer
                );

        scroll.setFitToWidth(
                true
        );

        scroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;"
        );


        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );


        // =====================================================
        // Load Foods
        // =====================================================

        List<FoodItem> allFoods;


        try {


            FoodAPI foodAPI =
                    ClientApiManager
                            .getInstance()
                            .getFoodAPI();

            allFoods =
                    foodAPI.getAllFood();

        } catch (Exception e) {

            allFoods =
                    new ArrayList<>();

            showErrorAlert(
                    "خطأ في الأطعمة",
                    "تعذر جلب قائمة الأطعمة."
            );
        }


        // =====================================================
        // Load Existing Foods
        // =====================================================

        if (activeSession != null &&
                activeSession.getNutritionPlan() != null) {

            loadExistingMeals(
                    activeSession
                            .getNutritionPlan(),
                    allFoods
            );
        }


        // =====================================================
        // Add New Meal
        // =====================================================

        List<FoodItem> finalAllFoods =
                allFoods;


        addMealBtn.setOnAction(
                e ->
                        addMealCard(
                                finalAllFoods
                        )
        );


        root.getChildren().addAll(
                addMealBtn,
                scroll
        );


        tab.setContent(
                root
        );


        return tab;
    }


    // =========================================================
    // Load Existing Meals
    // =========================================================

    private void loadExistingMeals(
            NutritionPlan plan,
            List<FoodItem> allFoods) {

        if (plan == null ||
                plan.getSelectedFoods() == null ||
                plan.getSelectedFoods().isEmpty()) {

            return;
        }


        mealEntries.clear();


        for (PlanFoodItem savedItem :
                plan.getSelectedFoods()) {

            if (savedItem == null ||
                    savedItem.getFoodItem() == null) {

                continue;
            }


            FoodItem savedFood =
                    savedItem.getFoodItem();


            ObjectProperty<FoodItem> selectedFood =
                    new SimpleObjectProperty<>(
                            savedFood
                    );


            ComboBox<String> mealTypeCombo =
                    new ComboBox<>();


            mealTypeCombo.getItems().addAll(
                    "Breakfast",
                    "Lunch",
                    "Dinner",
                    "Snack"
            );


            mealTypeCombo.setValue(
                    safeString(
                            savedItem.getMealType()
                    ).isEmpty()
                            ? "Lunch"
                            : savedItem.getMealType()
            );


            TextField quantityField =
                    new TextField();


            quantityField.setPromptText(
                    "الكمية (جرام)"
            );


            quantityField.setPrefWidth(
                    100
            );


            if (savedItem.getQuantity() != null) {

                quantityField.setText(
                        savedItem
                                .getQuantity()
                                .toPlainString()
                );
            }


            Button selectFoodBtn =
                    new Button(
                            safeString(
                                    savedFood.getFoodName()
                            )
                    );


            selectFoodBtn.setStyle(
                    "-fx-background-color: #c8e6c9;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );


            selectFoodBtn.setOnAction(
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

                                    selectFoodBtn.setText(
                                            food.getFoodName()
                                    );
                                }
                        );
                    }
            );


            HBox row1 =
                    new HBox(10);

            row1.setAlignment(
                    Pos.CENTER_LEFT
            );


            row1.getChildren().addAll(
                    new Label("الصنف:"),
                    selectFoodBtn,
                    new Label("نوع الوجبة:"),
                    mealTypeCombo
            );


            HBox row2 =
                    new HBox(10);

            row2.setAlignment(
                    Pos.CENTER_LEFT
            );


            row2.getChildren().addAll(
                    new Label("الكمية:"),
                    quantityField,
                    new Label("جرام (g)")
            );


            Button deleteBtn =
                    new Button(
                            "حذف X"
                    );


            deleteBtn.setStyle(
                    "-fx-background-color: #f44336;" +
                            "-fx-text-fill: white;"
            );


            VBox card =
                    new VBox(10);

            card.setPadding(
                    new Insets(15)
            );


            card.setStyle(
                    "-fx-background-color: #f1f8e9;" +
                            "-fx-border-color: #8bc34a;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;"
            );


            MealEntry entry =
                    new MealEntry();


            entry.food =
                    selectedFood;

            entry.mealType =
                    mealTypeCombo;

            entry.quantity =
                    quantityField;

            entry.card =
                    card;


            mealEntries.add(
                    entry
            );


            deleteBtn.setOnAction(
                    event -> {

                        mealsContainer
                                .getChildren()
                                .remove(
                                        card
                                );

                        mealEntries.remove(
                                entry
                        );
                    }
            );


            card.getChildren().addAll(
                    row1,
                    row2,
                    deleteBtn
            );


            mealsContainer
                    .getChildren()
                    .add(
                            card
                    );
        }
    }


    // =========================================================
    // Add New Meal Card
    // =========================================================

    private void addMealCard(
            List<FoodItem> allFoods) {

        if (mealsContainer == null) {
            return;
        }


        VBox card =
                new VBox(10);

        card.setPadding(
                new Insets(15)
        );


        card.setStyle(
                "-fx-background-color: #f1f8e9;" +
                        "-fx-border-color: #8bc34a;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );


        // =====================================================
        // Food
        // =====================================================

        ObjectProperty<FoodItem> selectedFood =
                new SimpleObjectProperty<>();


        Button selectFoodBtn =
                new Button(
                        "🔍 اختر الصنف الغذائي..."
                );


        selectFoodBtn.setStyle(
                "-fx-background-color: #e0e0e0;" +
                        "-fx-cursor: hand;"
        );


        selectFoodBtn.setOnAction(
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

                                selectFoodBtn.setText(
                                        food.getFoodName()
                                );

                                selectFoodBtn.setStyle(
                                        "-fx-background-color: #c8e6c9;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-cursor: hand;"
                                );
                            }
                    );
                }
        );


        // =====================================================
        // Meal Type
        // =====================================================

        ComboBox<String> mealTypeCombo =
                new ComboBox<>();


        mealTypeCombo.getItems().addAll(
                "Breakfast",
                "Lunch",
                "Dinner",
                "Snack"
        );


        mealTypeCombo.setValue(
                "Lunch"
        );


        // =====================================================
        // Quantity
        // =====================================================

        TextField quantityField =
                new TextField();


        quantityField.setPromptText(
                "الكمية (جرام)"
        );


        quantityField.setPrefWidth(
                100
        );


        Label unitLabel =
                new Label(
                        "جرام (g)"
                );


        // =====================================================
        // Rows
        // =====================================================

        HBox row1 =
                new HBox(10);


        row1.setAlignment(
                Pos.CENTER_LEFT
        );


        row1.getChildren().addAll(
                new Label("الصنف:"),
                selectFoodBtn,
                new Label("نوع الوجبة:"),
                mealTypeCombo
        );


        HBox row2 =
                new HBox(10);


        row2.setAlignment(
                Pos.CENTER_LEFT
        );


        row2.getChildren().addAll(
                new Label("الكمية:"),
                quantityField,
                unitLabel
        );


        // =====================================================
        // Delete
        // =====================================================

        Button deleteBtn =
                new Button(
                        "حذف X"
                );


        deleteBtn.setStyle(
                "-fx-background-color: #f44336;" +
                        "-fx-text-fill: white;"
        );


        // =====================================================
        // Meal Entry
        // =====================================================

        MealEntry entry =
                new MealEntry();


        entry.food =
                selectedFood;

        entry.mealType =
                mealTypeCombo;

        entry.quantity =
                quantityField;

        entry.card =
                card;


        mealEntries.add(
                entry
        );


        // =====================================================
        // Delete
        // =====================================================

        deleteBtn.setOnAction(
                event -> {

                    mealsContainer
                            .getChildren()
                            .remove(
                                    card
                            );

                    mealEntries.remove(
                            entry
                    );
                }
        );


        // =====================================================
        // Card Properties
        // =====================================================

        card.getProperties().put(
                "foodProperty",
                selectedFood
        );


        card.getProperties().put(
                "type",
                mealTypeCombo
        );


        card.getProperties().put(
                "qty",
                quantityField
        );


        card.getChildren().addAll(
                row1,
                row2,
                deleteBtn
        );


        mealsContainer
                .getChildren()
                .add(
                        card
                );
    }


    // =========================================================
    // Build Selected Foods
    // =========================================================

    private List<PlanFoodItem> buildSelectedFoods(
            NutritionPlan plan) {

        List<PlanFoodItem> foods =
                new ArrayList<>();


        if (mealEntries == null) {
            return foods;
        }


        for (MealEntry entry :
                mealEntries) {

            if (entry == null ||
                    entry.food == null) {

                continue;
            }


            FoodItem food =
                    entry.food.get();


            if (food == null) {

                throw new IllegalArgumentException(
                        "يوجد عنصر غذائي لم يتم اختياره."
                );
            }


            String quantityText =
                    entry.quantity != null
                            ? entry.quantity.getText()
                            .trim()
                            : "";


            if (quantityText.isEmpty()) {

                throw new IllegalArgumentException(
                        "الكمية غير محددة للصنف: "
                                + food.getFoodName()
                );
            }


            BigDecimal quantity;


            try {

                quantity =
                        new BigDecimal(
                                quantityText
                        );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "الكمية غير صحيحة للصنف: "
                                + food.getFoodName()
                );
            }


            if (quantity.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                throw new IllegalArgumentException(
                        "الكمية يجب أن تكون أكبر من صفر للصنف: "
                                + food.getFoodName()
                );
            }


            String mealType =
                    entry.mealType != null &&
                            entry.mealType.getValue() != null
                            ? entry.mealType.getValue()
                            : "Lunch";


            PlanFoodItem planFoodItem =
                    new PlanFoodItem(
                            plan,
                            food,
                            mealType,
                            quantity
                    );


            planFoodItem.setUnit(
                    "غرام"
            );


            foods.add(
                    planFoodItem
            );
        }


        return foods;
    }


    // =========================================================
    // Choose Examination Image
    // =========================================================

    @FXML
    private void handleChooseExamImage() {

        if (lblImagePath.getScene() == null) {
            return;
        }


        FileChooser chooser =
                new FileChooser();


        chooser.setTitle(
                "اختر صورة الفحص"
        );


        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif",
                        "*.bmp"
                )
        );


        Stage stage =
                (Stage)
                        lblImagePath
                                .getScene()
                                .getWindow();


        File selectedFile =
                chooser.showOpenDialog(
                        stage
                );


        if (selectedFile != null) {

            newlySelectedImagePath =
                    selectedFile
                            .getAbsolutePath();


            lblImagePath.setText(
                    newlySelectedImagePath
            );
        }
    }


    // =========================================================
    // Update Examination
    // =========================================================

    @FXML
    private void handleUpdateExamRow() {

        Examination selectedExam =
                tableExaminations
                        .getSelectionModel()
                        .getSelectedItem();


        if (selectedExam == null) {

            showAlert(
                    "تنبيه",
                    "الرجاء تحديد الفحص أولاً."
            );

            return;
        }


        String name =
                txtSelectedExamName
                        .getText()
                        .trim();


        if (name.isEmpty()) {

            showAlert(
                    "خطأ",
                    "اسم الفحص لا يمكن أن يكون فارغًا."
            );

            return;
        }


        selectedExam.setExaminationName(
                name
        );


        selectedExam.setNotes(
                txtSelectedExamNotes
                        .getText()
                        .trim()
        );


        if (newlySelectedImagePath != null &&
                !newlySelectedImagePath
                        .trim()
                        .isEmpty()) {

            selectedExam.setExaminationImage(
                    newlySelectedImagePath
            );
        }


        selectedExam.setModificationDate(
                LocalDateTime.now()
        );


        tableExaminations.refresh();


        showInfoAlert(
                "تم التحديث",
                "تم تحديث بيانات الفحص في القائمة."
        );
    }


    // =========================================================
    // Add Examination
    // =========================================================

    @FXML
    private void handleAddNewExamRow() {

        if (activeSession == null ||
                activeSession.getId() == null) {

            showErrorAlert(
                    "خطأ",
                    "لا توجد جلسة نشطة."
            );

            return;
        }


        String name =
                txtSelectedExamName
                        .getText()
                        .trim();


        if (name.isEmpty()) {

            showAlert(
                    "خطأ",
                    "يجب إدخال اسم الفحص."
            );

            return;
        }


        Examination newExam =
                new Examination();


        newExam.setExaminationId(
                null
        );


        newExam.setExaminationName(
                name
        );


        newExam.setNotes(
                txtSelectedExamNotes
                        .getText()
                        .trim()
        );


        newExam.setExaminationImage(
                newlySelectedImagePath != null &&
                        !newlySelectedImagePath
                                .trim()
                                .isEmpty()
                        ? newlySelectedImagePath
                        : null
        );


        newExam.setModificationDate(
                LocalDateTime.now()
        );


        Session sessionReference =
                new Session();


        sessionReference.setId(
                activeSession.getId()
        );


        newExam.setSession(
                sessionReference
        );


        examList.add(
                newExam
        );


        tableExaminations.refresh();


        txtSelectedExamName.clear();

        txtSelectedExamNotes.clear();

        newlySelectedImagePath = "";

        lblImagePath.setText(
                "لم يتم اختيار صورة"
        );
    }


    // =========================================================
    // Save All Changes
    // =========================================================

    @FXML
    private void handleSaveChanges() {

        if (activeSession == null ||
                activeSession.getId() == null) {

            showErrorAlert(
                    "خطأ في الحفظ",
                    "لا توجد جلسة نشطة."
            );

            return;
        }


        if (!showConfirmationAlert(
                "تأكيد الحفظ",
                "هل أنت متأكد من حفظ جميع تعديلات الجلسة رقم ("
                        + activeSession.getId()
                        + ")?"
        )) {

            return;
        }


        try {

            // =================================================
            // 1. Session
            // =================================================

            activeSession.setPrice(
                    parseDecimal(
                            txtPrice.getText(),
                            "السعر"
                    )
            );


            activeSession.setDuration(
                    txtDuration
                            .getText()
                            .trim()
            );


            activeSession.setNotes(
                    txtNotes
                            .getText()
                            .trim()
            );


            // =================================================
            // 2. BodyData
            // =================================================

            BodyData bd =
                    activeSession.getBodyData();


            if (bd == null) {

                bd =
                        new BodyData.Builder()
                                .build();


                activeSession.setBodyData(
                        bd
                );
            }


            bd.setHeight(
                    parseDecimal(
                            txtHeight.getText(),
                            "الطول"
                    )
            );


            bd.setWeight(
                    parseDecimal(
                            txtWeight.getText(),
                            "الوزن"
                    )
            );


            bd.setBodyFatPercentage(
                    parseDecimal(
                            txtFatPercentage.getText(),
                            "نسبة الدهون"
                    )
            );


            bd.setSmm(
                    parseDecimal(
                            txtSMM.getText(),
                            "SMM"
                    )
            );


            bd.setMuscleMass(
                    parseDecimal(
                            txtMuscleMass.getText(),
                            "كتلة العضلات"
                    )
            );


            bd.setActivityFactor(
                    parseDecimal(
                            txtActivityFactor.getText(),
                            "معامل النشاط"
                    )
            );


            bd.setPhysicalActivity(
                    comboPhysicalActivity.getValue()
            );


            bd.setArm_C(
                    parseDecimal(
                            txtArm.getText(),
                            "محيط الذراع"
                    )
            );


            bd.setChest_C(
                    parseDecimal(
                            txtChest.getText(),
                            "محيط الصدر"
                    )
            );


            bd.setWaist_C(
                    parseDecimal(
                            txtWaist.getText(),
                            "محيط الخصر"
                    )
            );


            bd.setAbdominal_C(
                    parseDecimal(
                            txtAbdominal.getText(),
                            "محيط البطن"
                    )
            );


            bd.setHip_C(
                    parseDecimal(
                            txtHip.getText(),
                            "محيط الورك"
                    )
            );


            bd.setMidThigh_C(
                    parseDecimal(
                            txtThigh.getText(),
                            "محيط الفخذ"
                    )
            );


            bd.setCalf_C(
                    parseDecimal(
                            txtCalf.getText(),
                            "محيط الساق"
                    )
            );


            activeSession.setBodyData(
                    bd
            );


            // =================================================
            // 3. Nutrition Plan
            // =================================================

            NutritionPlan plan =
                    activeSession.getNutritionPlan();


            if (plan == null) {

                plan =
                        new NutritionPlan();


                activeSession.setNutritionPlan(
                        plan
                );
            }


            plan.setTargetGoal(
                    txtPlanGoal
                            .getText()
                            .trim()
            );


            plan.setPlanStatus(
                    comboPlanStatus.getValue()
            );


            plan.setMealDistribution(
                    txtMealDistribution
                            .getText()
                            .trim()
            );


            plan.setStartDate(
                    dpStartDate.getValue()
            );


            plan.setEndDate(
                    dpEndDate.getValue()
            );


            plan.setProteinAmount(
                    parseDecimal(
                            txtProteinAmount.getText(),
                            "البروتين"
                    )
            );


            plan.setCarbohydratesAmount(
                    parseDecimal(
                            txtCarbAmount.getText(),
                            "الكربوهيدرات"
                    )
            );


            plan.setFatAmount(
                    parseDecimal(
                            txtFatAmount.getText(),
                            "الدهون"
                    )
            );


            plan.setWaterIntake(
                    txtWaterIntake
                            .getText()
                            .trim()
            );


            plan.setNotes(
                    txtNote
                            .getText()
                            .trim()
            );


            plan.setTotalCalories(
                    parseDecimal(
                            txtTotalCalories.getText(),
                            "السعرات"
                    )
            );


            String mealsCount =
                    txtMealsCount
                            .getText()
                            .trim();


            if (!mealsCount.isEmpty()) {

                plan.setMealsCount(
                        Integer.parseInt(
                                mealsCount
                        )
                );

            } else {

                plan.setMealsCount(
                        null
                );
            }


            // =================================================
            // 4. حفظ الوجبات داخل NutritionPlan
            // =================================================

            List<PlanFoodItem> selectedFoods =
                    buildSelectedFoods(
                            plan
                    );


            plan.setSelectedFoods(
                    selectedFoods
            );


            activeSession.setNutritionPlan(
                    plan
            );


            // =================================================
            // 5. Examinations
            // =================================================

            activeSession.setExaminations(
                    new ArrayList<>(
                            examList
                    )
            );


            // =================================================
            // 6. Background Save
            // =================================================

            final boolean[] success =
                    new boolean[1];


            final String[] error =
                    new String[1];


            SessionAPI sessionAPI =
                    ClientApiManager
                            .getInstance()
                            .getSessionAPI();


            BackgroundRunner.run(
                    "جاري حفظ جميع التعديلات... ⏳",

                    () -> {

                        try {

                            success[0] =
                                    sessionAPI.updateSession(
                                            activeSession
                                    );


                            if (!success[0]) {

                                error[0] =
                                        "السيرفر لم يؤكد نجاح التحديث.";
                            }

                        } catch (Exception e) {

                            success[0] = false;

                            error[0] =
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "حدث خطأ أثناء الحفظ.";

                            e.printStackTrace();
                        }


                        return true;
                    },

                    () -> {

                        if (success[0]) {

                            showInfoAlert(
                                    "تم الحفظ بنجاح",
                                    "تم حفظ جميع تعديلات الجلسة رقم ("
                                            + activeSession.getId()
                                            + ") بنجاح."
                            );


                            closeCurrentWindow();

                        } else {

                            showErrorAlert(
                                    "فشل الحفظ",
                                    "لم يتم حفظ التعديلات.\n\n"
                                            + "السبب:\n"
                                            + (
                                            error[0] != null
                                                    ? error[0]
                                                    : "سبب غير معروف."
                                    )
                            );
                        }
                    }
            );


        } catch (NumberFormatException e) {

            showAlert(
                    "خطأ في الأرقام",
                    "تأكد من إدخال جميع القيم الرقمية بصورة صحيحة."
            );


        } catch (Exception e) {

            e.printStackTrace();


            showErrorAlert(
                    "خطأ",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "حدث خطأ غير متوقع."
            );
        }
    }


    // =========================================================
    // Cancel
    // =========================================================

    @FXML
    private void handleCancel() {

        if (showConfirmationAlert(
                "تأكيد الإلغاء",
                "هل أنت متأكد من الإلغاء؟"
        )) {

            closeCurrentWindow();
        }
    }


    // =========================================================
    // Client Name
    // =========================================================

    private String getClientDisplayName(
            Client client) {

        if (client == null) {
            return "";
        }


        String firstName =
                safeString(
                        client.getFirstName()
                );


        String lastName =
                safeString(
                        client.getLastName()
                );


        String fullName =
                (
                        firstName
                                + " "
                                + lastName
                ).trim();


        if (!fullName.isEmpty()) {
            return fullName;
        }


        try {

            return safeString(
                    client.getFullName()
            );

        } catch (Exception ignored) {

            return "";
        }
    }


    // =========================================================
    // Clear Body
    // =========================================================

    private void clearBodyFields() {

        txtHeight.clear();
        txtWeight.clear();
        txtFatPercentage.clear();
        txtSMM.clear();
        txtMuscleMass.clear();
        txtActivityFactor.clear();

        comboPhysicalActivity.setValue(
                null
        );

        txtArm.clear();
        txtChest.clear();
        txtWaist.clear();
        txtAbdominal.clear();
        txtHip.clear();
        txtThigh.clear();
        txtCalf.clear();
    }


    // =========================================================
    // Clear Nutrition
    // =========================================================

    private void clearNutritionFields() {

        txtPlanGoal.clear();

        comboPlanStatus.setValue(
                null
        );

        txtMealDistribution.clear();

        dpStartDate.setValue(
                null
        );

        dpEndDate.setValue(
                null
        );

        txtProteinAmount.clear();
        txtCarbAmount.clear();
        txtFatAmount.clear();
        txtTotalCalories.clear();
        txtMealsCount.clear();
        txtWaterIntake.clear();
        txtNote.clear();
    }


    // =========================================================
    // Safe String
    // =========================================================

    private String safeString(
            String value) {

        return value != null
                ? value
                : "";
    }


    // =========================================================
    // Decimal String
    // =========================================================

    private String decimalString(
            BigDecimal value) {

        return value != null
                ? value.toPlainString()
                : "";
    }


    // =========================================================
    // Parse Decimal
    // =========================================================

    private BigDecimal parseDecimal(
            String value,
            String fieldName) {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new NumberFormatException(
                    "الحقل فارغ: "
                            + fieldName
            );
        }


        return new BigDecimal(
                value.trim()
        );
    }


    // =========================================================
    // Close
    // =========================================================

    private void closeCurrentWindow() {

        if (lblSessionId == null ||
                lblSessionId.getScene() == null) {

            return;
        }


        Stage stage =
                (Stage)
                        lblSessionId
                                .getScene()
                                .getWindow();


        if (stage != null) {
            stage.close();
        }
    }


    // =========================================================
    // Warning
    // =========================================================

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );


        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );


        alert.showAndWait();
    }


    // =========================================================
    // Error
    // =========================================================

    private void showErrorAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );


        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );


        alert.showAndWait();
    }


    // =========================================================
    // Information
    // =========================================================

    private void showInfoAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );


        alert.showAndWait();
    }


    // =========================================================
    // Confirmation
    // =========================================================

    private boolean showConfirmationAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );


        Optional<ButtonType> result =
                alert.showAndWait();


        return result.isPresent() &&
                result.get() ==
                        ButtonType.OK;
    }


    // =========================================================
    // Meal Entry
    // =========================================================

    private static class MealEntry {

        private ObjectProperty<FoodItem> food;

        private ComboBox<String> mealType;

        private TextField quantity;

        private VBox card;
    }
}