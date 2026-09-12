package user_interface.forminput;

import api.ClientApiManager;
import api.FoodAPI;
import api.SessionAPI;
import app.Main;
import entities.*;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import runner.BackgroundRunner;
import api.ClientAPI;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Supplier;

import static user_interface.forminput.Desgin.*;

public class SessionView extends VBox  {

    private TabPane tabPane = new TabPane();
    private Button saveButton = new Button("حفظ بيانات الجلسة والقياسات");

    // بيانات الجلسة
    private TextField clientName = new TextField();
    private TextField price = new TextField();
    private Spinner<Integer> hourSpin = new Spinner<>(0, 11, 0);
    private Spinner<Integer> minSpin = new Spinner<>(0, 59, 0);
    private TextArea sessionNotes = new TextArea();


    // بيانات الخطة الغذائية (تم تحديثها)
    private TextField targetGoal = new TextField();
    private DatePicker startDate = new DatePicker();
    private DatePicker endDate = new DatePicker();
    private ComboBox<String> statusComboBox = new ComboBox<>();

    private TextField proteinField = new TextField();
    private TextField fatField = new TextField();
    private TextField carbsField = new TextField();

    // -- الحقول الجديدة المضافة للخطة الغذائية --
    private TextField totalCaloriesField = new TextField();
    private Spinner<Integer> mealsCountSpin = new Spinner<>(0, 10, 0); // من 0 إلى 10 وجبات
    private TextField waterIntakeField = new TextField();
    private TextArea mealDistribution = new TextArea();
    private TextArea planNotesArea = new TextArea(); // ملاحظات الخطة

    // حاوية الفحوصات
    private VBox examsContainer = new VBox(10);

    // بيانات القياسات (BodyData)
    private TextField height = new TextField();
    private TextField weight = new TextField();
    private TextField activityFactor = new TextField();
    private TextField bodyFatPercentage = new TextField();
    private TextField SMM = new TextField();
    private TextField muscleMass = new TextField();
    private TextField physicalActivity = new TextField();
    private TextField armC = new TextField();
    private TextField chestC = new TextField();
    private TextField waistC = new TextField();
    private TextField abdominalC = new TextField();
    private TextField hipC = new TextField();
    private TextField midThigh = new TextField();
    private TextField calfC = new TextField();

    private Client currentClient = null;

    private VBox mealsContainer = new VBox(15);
    NutritionPlan plan;

    // التنسيقات الاساسية للواجهة
    public SessionView() throws IOException, InterruptedException {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(sessionTab());

        saveButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25;");
        saveButton.prefWidthProperty().bind(this.widthProperty().multiply(0.3));
        saveButton.setOnAction(e -> {
            handelSave();
        });

        // بناء قائمة النقر بزر الماوس الأيمن (ContextMenu)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem refreshAllItem = new MenuItem("✨ إعادة تعيين الجلسة بالكامل");
        MenuItem refreshSessionItem = new MenuItem("⏱️ مسح بيانات الجلسة الأساسية");
        MenuItem refreshExamsItem = new MenuItem("🔬 مسح قائمة الفحوصات");
        MenuItem refreshPlanItem = new MenuItem("📋 مسح الخطة الغذائية والوجبات");
        MenuItem refreshBodyItem = new MenuItem("📏 مسح القياسات الجسدية");

        refreshAllItem.setOnAction(e -> clearAllFields());
        refreshSessionItem.setOnAction(e -> clearSessionData());
        refreshExamsItem.setOnAction(e -> clearExamsData());
        refreshPlanItem.setOnAction(e -> clearPlanData());
        refreshBodyItem.setOnAction(e -> clearBodyDataFields());

        contextMenu.getItems().addAll(
                refreshAllItem,
                new SeparatorMenuItem(),
                refreshSessionItem,
                refreshExamsItem,
                refreshPlanItem,
                refreshBodyItem
        );

        // ربط القائمة عبر الـ EventFilter لالتقاط النقر الأيمن ومنع ابتلاعه من العناصر الأبناء
        this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
                event.consume();
            } else {
                contextMenu.hide();
            }
        });

        HBox buttonWrapper = new HBox(saveButton);
        buttonWrapper.setAlignment(Pos.CENTER_LEFT);
        buttonWrapper.setPadding(new Insets(10));

        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.getChildren().setAll(tabPane, buttonWrapper);
    }

    // واجهة اضافة بيانات الجلسة
    public Tab sessionTab() throws IOException, InterruptedException {
        Tab tab = new Tab("بيانات الجلسة");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15); grid.setHgap(15);

        ClientAPI getAllClient =
                ClientApiManager
                        .getInstance()
                        .getClientAPI();

        List<Client> clients =
                getAllClient.allClients();

        Map<String, Long> nameToIdMap = new HashMap<>();
        for (Client c : clients) {
            nameToIdMap.put(c.getFullName().trim(),  c.getClientID().longValue());
        }
        List<String> allFullNames = new ArrayList<>(nameToIdMap.keySet());

        ContextMenu suggestionsPopup = new ContextMenu();
        suggestionsPopup.getStyleClass().add("suggestions-menu");

        PauseTransition debounce = new PauseTransition(Duration.millis(300));

        clientName.setPromptText("ابحث عن اسم العميل...");

        clientName.textProperty().addListener((obs, oldVal, newVal) -> {

            debounce.setOnFinished(event -> {

                suggestionsPopup.getItems().clear();

                if (newVal == null || newVal.trim().isEmpty()) {
                    suggestionsPopup.hide();
                    return;
                }

                String searchKey = newVal.trim().toLowerCase();
                boolean found = false;

                for (Client client : clients) {

                    String name = client.getFullName().trim();

                    if (name.toLowerCase().contains(searchKey)) {

                        MenuItem item = new MenuItem(name);
                        item.setStyle(
                                "-fx-font-size: 14px; -fx-padding: 5px 10px;"
                        );

                        item.setOnAction(a -> {

                            // وضع اسم العميل في الحقل
                            clientName.setText(name);

                            // حفظ كائن Client نفسه
                            currentClient = client;

                            System.out.println(
                                    "تم اختيار العميل: " +
                                            currentClient.getFullName()
                            );

                            System.out.println(
                                    "Client ID = " +
                                            currentClient.getClientID()
                            );

                            suggestionsPopup.hide();
                        });

                        suggestionsPopup.getItems().add(item);
                        found = true;
                    }
                }

                if (!found) {

                    MenuItem addItem = new MenuItem(
                            "⚠️ العميل غير موجود. إضافة عميل جديد؟"
                    );

                    addItem.setStyle(
                            "-fx-text-fill: #1976D2;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-cursor: hand;"
                    );

                    addItem.setOnAction(a -> {

                        suggestionsPopup.hide();

                        System.out.println(
                                "تم الانتقال إلى واجهة إضافة العميل"
                        );

                        Main.setView(new ClientView(), "Client");
                    });

                    suggestionsPopup.getItems().add(addItem);
                }

                if (!suggestionsPopup.getItems().isEmpty()) {

                    if (!suggestionsPopup.isShowing()) {
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
            });

            debounce.playFromStart();
        });
        grid.add(new Label("اسم المريض :"), 0, 0);
        grid.add(clientName, 1, 0);

        grid.add(new Label("سعر الجلسة:"), 0, 1);
        makeDecimalOnly(price);
        grid.add(price, 1, 1);

        grid.add(new Label("مدة الجلسة:"), 0, 2);
        hourSpin.setPrefWidth(80); minSpin.setPrefWidth(80);
        grid.add(new HBox(5, hourSpin, new Label("ساعة"), minSpin, new Label("دقيقة")), 1, 2);

        grid.add(new Label("ملاحظات:"), 0, 3);
        sessionNotes.setPrefRowCount(3);
        grid.add(sessionNotes, 1, 3);

        HBox nav = new HBox(10,
                createNavBtn("الفحوصات", this::examinationTab),
                createNavBtn("الخطة الغذائية", this::nutritionPlanTab),
                createNavBtn("القياسات", this::bodyDataTab)
        );
        grid.add(nav, 1, 4);

        tab.setContent(grid);
        return tab;
    }

    private Button createNavBtn(String title, Supplier<Tab> tabSupplier) {
        Button btn = new Button(title);
        btn.setOnAction(e -> switchOrAddTab(title,() -> tabSupplier.get()));
        return btn;
    }

    public Tab examinationTab() {
        Tab tab = new Tab("الفحوصات");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        Button addBtn = new Button("+ إضافة فحص");

        ScrollPane scroll = new ScrollPane(examsContainer);
        scroll.setFitToWidth(true);

        addBtn.setOnAction(e -> {
            VBox pane = new VBox(10);
            pane.setPadding(new Insets(10));
            pane.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #4CAF50; -fx-border-radius: 5;");

            TextField name = new TextField(); name.setPromptText("اسم الفحص");
            Button del = new Button("X"); del.setOnAction(ev -> examsContainer.getChildren().remove(pane));
            HBox header = new HBox(10, name, del);

            ImageView preview = new ImageView(); preview.setFitWidth(100); preview.setPreserveRatio(true);
            Button upload = new Button("صورة 📷");
            upload.setOnAction(ev -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

                File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());

                if (selectedFile != null) {
                    try {
                        preview.setImage(new Image(selectedFile.toURI().toString()));
                        String savedPath = saveImageToProjectFolder(selectedFile);
                        pane.setUserData(savedPath);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            TextArea examNotes = new TextArea(); examNotes.setPrefHeight(50);examNotes.setPromptText("ملاحظات ...");
            pane.getChildren().addAll(header, new HBox(10, new VBox(5, upload, preview), examNotes));
            examsContainer.getChildren().add(pane);
        });

        root.getChildren().addAll(addBtn, scroll);
        tab.setContent(root);
        return tab;
    }

    // --- تم إعادة تصميم وبناء تبويب الخطة الغذائية ليشمل الحقول الجديدة ---
    public Tab nutritionPlanTab() {
        Tab tab = new Tab("الخطة الغذائية");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15); grid.setHgap(15);

        // --- تعريف قيود الأعمدة لضمان التناسق ---
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(100); // عرض ثابت للعناوين

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setMinWidth(150);

        grid.getColumnConstraints().addAll(labelCol, fieldCol, labelCol, fieldCol);
        // ----------------------------------------

        statusComboBox.getItems().addAll("نشط", "مكتمل", "ملغي");
        statusComboBox.setValue("نشط");
        statusComboBox.setMaxWidth(Double.MAX_VALUE);

        mealDistribution.setPrefRowCount(3);
        planNotesArea.setPrefRowCount(2);
        planNotesArea.setPromptText("ملاحظات إضافية للخطة...");

        totalCaloriesField.setPromptText("يُحسب آلياً");
        waterIntakeField.setPromptText("مثال: 3 لتر");

        // إضافة العناصر مع التأكد من خاصية MaxWidth لملء الخلية
        grid.add(new Label("هدف الخطة:"), 0, 0); grid.add(targetGoal, 1, 0);
        targetGoal.setMaxWidth(Double.MAX_VALUE);

        setupEndDateConstraints();
        grid.add(new Label("بداية:"), 0, 1); grid.add(startDate, 1, 1);
        startDate.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("نهاية:"), 0, 2); grid.add(endDate, 1, 2);
        endDate.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("حالة الخطة:"), 0, 3); grid.add(statusComboBox, 1, 3);

        grid.add(new Label("عدد الوجبات:"), 0, 4); grid.add(mealsCountSpin, 1, 4);
        mealsCountSpin.setMaxWidth(Double.MAX_VALUE);

        // أعمدة الماكروز
        grid.add(new Label("بروتين (g):"), 2, 0); grid.add(makeDecimalOnly(proteinField), 3, 0);
        grid.add(new Label("دهون (g):"), 2, 1); grid.add(makeDecimalOnly(fatField), 3, 1);
        grid.add(new Label("كارب (g):"), 2, 2); grid.add(makeDecimalOnly(carbsField), 3, 2);

        // إجمالي السعرات والماء
        grid.add(new Label("إجمالي السعرات:"), 2, 3); grid.add(makeDecimalOnly(totalCaloriesField), 3, 3);
        grid.add(new Label("الاحتياج المائي:"), 2, 4); grid.add(waterIntakeField, 3, 4);

        // النصوص الكبيرة
        grid.add(new Label("توزيع الوجبات:"), 0, 5); grid.add(mealDistribution, 1, 5, 3, 1);
        grid.add(new Label("ملاحظات الخطة:"), 0, 6); grid.add(planNotesArea, 1, 6, 3, 1);

        // الزر (بما أننا اعتمدنا اللون الأزرق الغامق كـ هوية للمشروع)
        Button addMealsBtn = new Button("اضافة وتوزيع الأطعمة");
        addMealsBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-padding: 8 20;");
        addMealsBtn.setMaxWidth(Double.MAX_VALUE);
        addMealsBtn.setOnAction(e -> switchOrAddTab("توزيع الوجبات", this::planFoodItemsTab));

        // وضع الزر في منتصف الأعمدة أو توسيعه
        grid.add(addMealsBtn, 0, 7, 4, 1);

        tab.setContent(grid);
        return tab;
    }
    public Tab bodyDataTab() {
        Tab tab = new Tab("القياسات");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(12); grid.setHgap(12);

        grid.add(new Label("الطول:"), 0, 0);       grid.add(makeDecimalOnly(height), 1, 0);
        grid.add(new Label("الوزن:"), 2, 0);       grid.add(makeDecimalOnly(weight), 3, 0);
        grid.add(new Label("الدهون%:"), 4, 0);     grid.add(makeDecimalOnly(bodyFatPercentage), 5, 0);

        grid.add(new Label("الخصر:"), 0, 1);       grid.add(makeDecimalOnly(waistC), 1, 1);
        grid.add(new Label("الصدر:"), 2, 1);       grid.add(makeDecimalOnly(chestC), 3, 1);
        grid.add(new Label("الأرداف:"), 4, 1);     grid.add(makeDecimalOnly(hipC), 5, 1);

        grid.add(new Label("الذراع:"), 0, 2);      grid.add(makeDecimalOnly(armC), 1, 2);
        grid.add(new Label("البطن:"), 2, 2);       grid.add(makeDecimalOnly(abdominalC), 3, 2);
        grid.add(new Label("الفخذ:"), 4, 2);       grid.add(makeDecimalOnly(midThigh), 5, 2);

        grid.add(new Label("الساق:"), 0, 3);       grid.add(makeDecimalOnly(calfC), 1, 3);
        grid.add(new Label("كتلة العضل:"), 2, 3);  grid.add(makeDecimalOnly(muscleMass), 3, 3);
        grid.add(new Label("SMM:"), 4, 3);         grid.add(makeDecimalOnly(SMM), 5, 3);

        grid.add(new Label("معامل النشاط:"), 0, 4); grid.add(makeDecimalOnly(activityFactor), 1, 4);
        grid.add(new Label("النشاط الحركي:"), 2, 4); grid.add(physicalActivity, 3, 4);

        for (Node node : grid.getChildren()) {
            if (node instanceof TextField tf) {
                tf.setPrefWidth(110);
                if (tf == physicalActivity) {
                    tf.setPrefWidth(180);
                    tf.setPromptText("مثال: موظف مكتبي خامل");
                } else {
                    tf.setPromptText("0.0");
                }
                tf.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #bdc3c7; -fx-padding: 5;");
            } else if (node instanceof Label lbl) {
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13px;");
            }
        }

        tab.setContent(grid);
        return tab;
    }

    public Tab planFoodItemsTab() {
        Tab tab = new Tab("توزيع الوجبات");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Button addMealBtn = new Button("+ إضافة صنف غذائي للخطة");
        addMealBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        ScrollPane scroll = new ScrollPane(mealsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

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

        } catch (IOException | RuntimeException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "الأطعمة",
                    e.getMessage() == null
                            ? "تعذر تحميل قائمة الأطعمة."
                            : e.getMessage()
            );

            return tab;
        }

        addMealBtn.setOnAction(e -> {
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #8bc34a; -fx-border-radius: 8; -fx-background-radius: 8;");

            // استبدال الـ ComboBox بخصائص اختيار جديدة
            ObjectProperty<FoodItem> selectedFood = new SimpleObjectProperty<>();

            Button selectFoodBtn = new Button("🔍 اختر الصنف الغذائي...");
            selectFoodBtn.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");

            // حدث النقر لفتح نافذة البحث والاختيار الاحترافية
            selectFoodBtn.setOnAction(ev -> {
                FoodSelectionDialog dialog = new FoodSelectionDialog(allFoods);
                Optional<FoodItem> result = dialog.showAndWait();
                result.ifPresent(food -> {
                    selectedFood.set(food);
                    selectFoodBtn.setText(food.getFoodName());
                    selectFoodBtn.setStyle("-fx-background-color: #c8e6c9; -fx-font-weight: bold;");
                });
            });


            ComboBox<String> mealTypeCombo = new ComboBox<>();
            mealTypeCombo.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
            mealTypeCombo.setValue("Lunch");

            TextField quantityField = new TextField();
            quantityField.setPromptText("الكمية (جرام)");
            quantityField.setPrefWidth(80);
            Label unitLabel = new Label("جرام (g)");

            HBox row1 = new HBox(10);
            row1.setAlignment(Pos.CENTER_LEFT);
            row1.getChildren().addAll(new Label("الصنف:"), selectFoodBtn, new Label("نوع الوجبة:"), mealTypeCombo);

            HBox row2 = new HBox(10);
            row2.setAlignment(Pos.CENTER_LEFT);
            row2.getChildren().addAll(new Label("الكمية:"), quantityField, unitLabel);

            Button deleteBtn = new Button("حذف X");
            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteBtn.setOnAction(ev -> mealsContainer.getChildren().remove(card));

            // تخزين المتغيرات في خصائص الكارت لاسترجاعها عند الحفظ
            card.getProperties().put("foodProperty", selectedFood); // استخدمنا Property بدلا من ComboBox
            card.getProperties().put("type", mealTypeCombo);
            card.getProperties().put("qty", quantityField);

            card.getChildren().addAll(row1, row2, deleteBtn);
            mealsContainer.getChildren().add(card);
        });

        root.getChildren().addAll(addMealBtn, scroll);
        tab.setContent(root);
        return tab;
    }

    public Session saveSession() {
        if (currentClient.getClientID() <= 0) {
            throw new IllegalStateException("لم يتم تحديد العميل، يرجى اختياره من القائمة المنسدلة أولاً.");
        }

        LocalTime duration = LocalTime.of(hourSpin.getValue(), minSpin.getValue());
        if (duration.getHour() == 0 && duration.getMinute() == 0) {
            throw new IllegalArgumentException("يرجى تحديد مدة زمنية صالحة للجلسة (ساعات / دقائق).");
        }

        BigDecimal sPrice = getDecimal(price, "سعر الجلسة");

        if (!targetGoal.getText().trim().isEmpty()) {

            if (startDate.getValue() == null || endDate.getValue() == null) {
                throw new IllegalArgumentException("خطة التغذية: يرجى تحديد تاريخ البداية وتاريخ النهاية للخطة المكتوبة.");
            }
            String statusInArabic = statusComboBox.getValue();
            String status = "Active";

            if ("مكتمل".equals(statusInArabic)) {
                status = "Completed";
            } else if ("ملغي".equals(statusInArabic)) {
                status = "Cancelled";
            }

            // إنشاء كائن الخطة
            plan = new NutritionPlan(
                    targetGoal.getText().trim(), startDate.getValue(), endDate.getValue(),
                    status, mealDistribution.getText().trim(),
                    getDecimal(proteinField, "البروتين"),
                    getDecimal(fatField, "الدهون"),
                    getDecimal(carbsField, "الكارب")
            );

            // إضافة الحقول الجديدة
            String calsInput = totalCaloriesField.getText().trim();
            if (!calsInput.isEmpty()) {
                plan.setTotalCalories(BigDecimal.valueOf(Float.parseFloat(calsInput)));
            } else {
                plan.setTotalCalories(BigDecimal.ZERO); // سيرسل 0 لتقوم الدالة getTotalCalories بحسابه آلياً
            }

            plan.setMealsCount(mealsCountSpin.getValue());
            plan.setWaterIntake(waterIntakeField.getText() != null ? waterIntakeField.getText().trim() : "");
            plan.setNotes(planNotesArea.getText() != null ? planNotesArea.getText().trim() : "");


            ArrayList<PlanFoodItem> tempPlanItems = new ArrayList<>();
            for (Node node : mealsContainer.getChildren()) {
                if (node instanceof VBox card) {
                    @SuppressWarnings("unchecked")
                    ObjectProperty<FoodItem> foodProperty = (ObjectProperty<FoodItem>) card.getProperties().get("foodProperty");
                    ComboBox<String> typeCombo = (ComboBox<String>) card.getProperties().get("type");
                    TextField qtyField = (TextField) card.getProperties().get("qty");

                    FoodItem selectedFood = foodProperty != null ? foodProperty.get() : null;
                    String mealType = typeCombo.getValue();
                    String qtyStr = qtyField.getText().trim();

                    if (selectedFood != null && !qtyStr.isEmpty()) {
                        try {
                            BigDecimal quantity = BigDecimal.valueOf(Float.parseFloat(qtyStr));
                            tempPlanItems.add(new PlanFoodItem(0L, selectedFood.getFoodName(), selectedFood.getFoodItemId(), mealType, quantity));
                        } catch (NumberFormatException e) {
                            throw new NumberFormatException("خطأ في توزيع الوجبات: القيمة '" + qtyStr + "' غير صالحة لكمية الصنف '" + selectedFood.getFoodName() + "'.");
                        }
                    }
                }
            }
            plan.setSelectedFoods(tempPlanItems);
        }

        ArrayList<Examination> examsList = new ArrayList<>();
        for (Node node : examsContainer.getChildren()) {
            if (node instanceof VBox pane) {
                HBox header = (HBox) pane.getChildren().get(0);
                TextField nameF = (TextField) header.getChildren().get(0);
                HBox content = (HBox) pane.getChildren().get(1);
                TextArea noteF = (TextArea) content.getChildren().get(1);

                String img = (String) pane.getUserData();
                if (!nameF.getText().trim().isEmpty()) {
                    examsList.add(new Examination(nameF.getText().trim(), img, noteF.getText().trim()));
                }
            }
        }

        String notes = sessionNotes.getText() == null ? "" : sessionNotes.getText().trim();
        BodyData bodyData = saveBodyData();

        return new Session(0L, currentClient, duration.toString(), sPrice, bodyData, plan, examsList, notes);
    }

    public BodyData saveBodyData() {
        BodyData bodyData = new BodyData.Builder()
                .height(getDecimal(height, "الطول"))
                .weight(getDecimal(weight, "الوزن"))
                .bodyFatPercentage(getDecimal(bodyFatPercentage, "نسبة الدهون"))
                .activityFactor(getDecimal(activityFactor, "معامل النشاط"))
                .smm(getDecimal(SMM, "SMM"))
                .muscleMass(getDecimal(muscleMass, "كتلة العضل"))
                .physicalActivity(physicalActivity.getText() == null ? "" : physicalActivity.getText().trim())
                .arm_C(getDecimal(armC, "محيط الذراع"))
                .chest_C(getDecimal(chestC, "محيط الصدر"))
                .waist_C(getDecimal(waistC, "محيط الخصر"))
                .abdominal_C(getDecimal(abdominalC, "محيط البطن"))
                .hip_C(getDecimal(hipC, "محيط الأرداف"))
                .midThigh_C(getDecimal(midThigh, "محيط الفخذ"))
                .calf_C(getDecimal(calfC, "محيط الساق"))
                .build();

        System.out.println("ActivityFactor in view = "+ bodyData.getActivityFactor());
        return bodyData;
    }

    public void handelSave() {
        // 1. تجميع البيانات والتحقق منها في مجرى الواجهة الرئيسي
        Session session;
        try {
            session = saveSession();
        } catch (NumberFormatException ex){
            showAlert(Alert.AlertType.ERROR, "خطأ في التنسيق الرقمي ❌", ex.getMessage());
            return; // إيقاف العملية إذا كان هناك خطأ
        } catch (IllegalStateException | IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "تنبيه في البيانات ⚠", ex.getMessage());
            return; // إيقاف العملية
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "خطأ ❌", ex.getMessage());
            return;
        }

        // 2. استدعاء نافذة التحميل وتنفيذ الحفظ في مجرى خلفي
        BackgroundRunner.run("جاري حفظ بيانات الجلسة...", () -> {

            // يتم تنفيذ هذا الجزء في Background Thread لمنع تجميد الواجهة
            SessionAPI addSession =
                    ClientApiManager
                            .getInstance()
                            .getSessionAPI();

            return addSession.save(session);

        }, () -> {

            // 3. ما بعد النجاح (يعود التنفيذ إلى مجرى JavaFX الرئيسي)
            String clientNameText = (clientName != null && !clientName.getText().trim().isEmpty()) ? clientName.getText().trim() : "العميل";
            showAlert(Alert.AlertType.INFORMATION, "تم الحفظ بنجاح ✔", "تم حفظ وتوثيق بيانات الجلسة والقياسات للعميل [" + clientNameText + "] بنجاح في قاعدة البيانات.");
            clearAllFields();

        });
    }
    public void switchOrAddTab(String title, Supplier<Tab> tabCreator) {
        tabPane.getTabs().stream().filter(t -> t.getText().equals(title)).findFirst()
                .ifPresentOrElse(
                        t -> tabPane.getSelectionModel().select(t),
                        () -> {
                            // إظهار نافذة الانتظار عند بناء تاب جديد
                            BackgroundRunner.run("جاري تجهيز الواجهة...", () -> {

                                // تأخير زمني بسيط في الخلفية لضمان ظهور الرسالة بسلاسة
                                Thread.sleep(150);
                                return true;

                            }, () -> {

                                // بناء التاب وإضافته في مجرى JavaFX الرئيسي
                                Tab newTab = tabCreator.get();
                                tabPane.getTabs().add(newTab);
                                tabPane.getSelectionModel().select(newTab);

                            });
                        }
                );
    }

    private String saveImageToProjectFolder(File file) throws IOException {
        File destDir = new File("data\\exminatoinphoto");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        String uniqueName = UUID.randomUUID().toString() + extension;

        File destination = new File(destDir, uniqueName);
        Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return destination.getAbsolutePath();
    }

    private void setupEndDateConstraints() {
        endDate.setEditable(false);
        endDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && startDate.getValue() != null) {
                if (newValue.isBefore(startDate.getValue())) {
                    endDate.setValue(null);
                }
            }
        });

        endDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (startDate.getValue() != null && date != null) {
                    if (date.isBefore(startDate.getValue())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #ababab;");
                    }
                }
            }
        });
    }

    // --- دالات إعادة التعيين والمسح المنفصلة ---

    // 1. مسح بيانات الجلسة الأساسية
    private void clearSessionData() {
        clientName.clear();
        price.clear();
        hourSpin.getValueFactory().setValue(0);
        minSpin.getValueFactory().setValue(0);
        sessionNotes.clear();
        currentClient = null;
        System.out.println("تم مسح بيانات الجلسة الأساسية.");
    }

    // 2. مسح الفحوصات المضافة
    private void clearExamsData() {
        examsContainer.getChildren().clear();
        tabPane.getTabs().removeIf(tab -> tab.getText().equals("الفحوصات"));
        System.out.println("تم تصفير قائمة الفحوصات.");
    }

    // 3. مسح بيانات الخطة الغذائية والوجبات التابعة لها (تم تحديثها لتشمل الحقول الجديدة)
    private void clearPlanData() {
        targetGoal.clear();
        startDate.setValue(null);
        endDate.setValue(null);
        statusComboBox.setValue("نشط");
        mealDistribution.clear();
        proteinField.clear();
        fatField.clear();
        carbsField.clear();

        // تفريغ الحقول الجديدة
        totalCaloriesField.clear();
        mealsCountSpin.getValueFactory().setValue(0);
        waterIntakeField.clear();
        planNotesArea.clear();

        mealsContainer.getChildren().clear();

        tabPane.getTabs().removeIf(tab -> tab.getText().equals("الخطة الغذائية") || tab.getText().equals("توزيع الوجبات"));
        System.out.println("تم مسح الخطة الغذائية والأصناف التابعة لها.");
    }

    // 4. مسح حقول القياسات الجسدية بالكامل
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

        tabPane.getTabs().removeIf(tab -> tab.getText().equals("القياسات"));
        System.out.println("تم مسح كافة حقول القياسات الجسدية.");
    }

    // 5. إعادة تعيين الواجهة بالكامل
    private void clearAllFields() {
        clearSessionData();
        clearExamsData();
        clearPlanData();
        clearBodyDataFields();

        tabPane.getSelectionModel().select(0); // العودة لتبويب الجلسة الأساسي
        System.out.println("تم تصفير واجهة الجلسة بالكامل.");
    }
}