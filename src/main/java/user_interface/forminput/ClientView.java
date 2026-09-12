package user_interface.forminput;

import api.ClientApiManager;
import entities.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner;
import api.ClientAPI;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static user_interface.forminput.Desgin.*;

public class ClientView extends VBox {

    private TabPane tabPane = new TabPane();
    private Button saveButton = new Button("حفظ بيانات العميل");

    // بيانات العميل الأساسية
    private TextField nameField = new TextField();
    private ToggleGroup genderGroup = new ToggleGroup();
    private DatePicker datePicker = new DatePicker();
    private TextField phoneField = new TextField();

    // بيانات صحية عامة
    private TextArea currentMedication = new TextArea();
    private TextArea medicalHistory = new TextArea();
    private TextArea familyMedicalHistory = new TextArea();
    private TextArea healthNotes = new TextArea();

    // حاويات البيانات الديناميكية
    private VBox diseasesContainer = new VBox(10);
    private VBox allergyContainer = new VBox(10);

    private ArrayList<Allergy> allergies = new ArrayList<>();
    private ArrayList<ChronicDisease> diseases = new ArrayList<>();

    // بيانات نمط الحياة
    private TextField mealsNumber = new TextField();
    private TextField budgetField = new TextField();
    private TextArea breakfastArea = new TextArea();
    private TextArea lunchArea = new TextArea();
    private TextArea dinnerArea = new TextArea();
    private TextArea snacksArea = new TextArea();
    private TextArea drinksArea = new TextArea();
    private TextArea sleepArea = new TextArea();
    private TextArea foodDislikeArea = new TextArea();
    private TextArea badHabitsArea = new TextArea();

    public ClientView() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getTabs().add(createClientDataTab());

        // تنسيق زر الحفظ
        saveButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25;");
        saveButton.prefWidthProperty().bind(this.widthProperty().multiply(0.25));
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> handleSave());

        // بناء قائمة النقر بزر الماوس الأيمن (ContextMenu)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem refreshAllItem = new MenuItem("✨ إعادة تعيين الواجهة بالكامل");
        MenuItem refreshBasicItem = new MenuItem("👤 مسح البيانات الأساسية فقط");
        MenuItem refreshHealthItem = new MenuItem("🏥 مسح البيانات الصحية والأمراض");
        MenuItem refreshLifeItem = new MenuItem("🍏 مسح بيانات نمط الحياة");

        refreshAllItem.setOnAction(e -> clearAllFields());
        refreshBasicItem.setOnAction(e -> clearBasicData());
        refreshHealthItem.setOnAction(e -> clearHealthData());
        refreshLifeItem.setOnAction(e -> clearLifeData());

        contextMenu.getItems().addAll(
                refreshAllItem,
                new SeparatorMenuItem(),
                refreshBasicItem,
                refreshHealthItem,
                refreshLifeItem
        );

        // الحل: استخدام Event Filter على الواجهة بالكامل ليلتقط النقرة الأيمن أينما حدثت
        this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
                event.consume(); // إيقاف الحدث حتى لا يحدث تعارض مع عناصر أخرى
            } else {
                contextMenu.hide();
            }
        });

        // ترتيب الأزرار بجانب بعضها في الأسفل
        HBox buttonWrapper = new HBox(15, saveButton);
        buttonWrapper.setAlignment(Pos.CENTER_LEFT);
        buttonWrapper.setPadding(new Insets(10));

        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.getChildren().setAll(tabPane, buttonWrapper);
    }

    public Tab createClientDataTab() {
        Tab tab = new Tab("بيانات العميل");
        GridPane grid = new GridPane();

        grid.setPadding(new Insets(20));
        grid.setVgap(15); grid.setHgap(15);
        grid.add(new Label("الاسم الرباعي:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("الجنس:"), 0, 1);

        RadioButton male = new RadioButton("M"); male.setToggleGroup(genderGroup); male.setSelected(true);
        RadioButton female = new RadioButton("F"); female.setToggleGroup(genderGroup);
        grid.add(new HBox(15, male, female), 1, 1);

        datePicker.setEditable(false);
        datePicker.setValue(LocalDate.of(2000, 1, 1));
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #bbbbbb;");
                }
            }
        });
        grid.add(new Label("تاريخ الميلاد:"), 0, 2);
        grid.add(datePicker, 1, 2);

        phoneField = Desgin.makeDecimalOnly(phoneField);
        grid.add(new Label("رقم الهاتف:"), 0, 3);
        grid.add(phoneField, 1, 3);

        Button healthBtn = new Button("البيانات الصحية ➕");
        healthBtn.setOnAction(e -> switchTab("بيانات العميل الصحية", () -> createHealthDataTab()));

        Button lifeBtn = new Button("نمط الحياة ➕");
        lifeBtn.setOnAction(e -> switchTab("معلومات شخصية",() -> createLifeInformationTab()));

        grid.add(new HBox(10, healthBtn, lifeBtn), 1, 5);
        tab.setContent(grid);
        return tab;
    }

    public Tab createHealthDataTab() {
        Tab tab = new Tab("بيانات العميل الصحية");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15); grid.setHgap(15);

        setupTextArea(currentMedication, "الأدوية الحالية", grid, 0);
        setupTextArea(medicalHistory, "التاريخ الصحي", grid, 1);
        setupTextArea(familyMedicalHistory, "تاريخ العائلة", grid, 2);
        setupTextArea(healthNotes, "ملاحظات عامة", grid, 3);

        Button allergyBtn = new Button("إضافة حساسية");
        allergyBtn.setOnAction(e -> switchTab("الحساسية",() -> Allergy()));
        Button diseaseBtn = new Button("إضافة أمراض");
        diseaseBtn.setOnAction(e -> switchTab("الأمراض",() -> diseas()));
        grid.add(new HBox(10, allergyBtn, diseaseBtn), 1, 4);
        tab.setContent(grid);
        return tab;
    }

    public Tab diseas() {
        Tab tab = new Tab("الأمراض");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        Button addBtn = new Button("+ إضافة مرض جديد");
        addBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        ScrollPane scroll = new ScrollPane(diseasesContainer);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        addBtn.setOnAction(e -> {
            VBox pane = new VBox(10);
            pane.setPadding(new Insets(10));
            pane.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-radius: 5;");

            TextField dName = new TextField(); dName.setPromptText("اسم المرض");
            TextField dStatus = new TextField(); dStatus.setPromptText("الحالة");
            ComboBox<String> dSeverity = new ComboBox<>(); dSeverity.getItems().addAll("بسيطة","متوسطة","خطيرة"); dSeverity.setValue("بسيطة");
            TextArea dFood = new TextArea(); dFood.setPromptText("الممنوعات..."); dFood.setPrefHeight(50);
            TextArea dNotes = new TextArea(); dNotes.setPromptText("ملاحظات..."); dNotes.setPrefHeight(50);

            HBox r1 = new HBox(10, new Label("المرض:"), dName, new Label("الحالة:"), dStatus);
            HBox r2 = new HBox(10, new Label("الخطورة:"), dSeverity, new Label("ممنوعات:"), dFood);
            Button del = new Button("حذف X"); del.setOnAction(ev -> diseasesContainer.getChildren().remove(pane));
            pane.getChildren().addAll(r1, r2, dNotes, del);
            diseasesContainer.getChildren().add(pane);
        });
        root.getChildren().addAll(addBtn, scroll);
        tab.setContent(root);
        return tab;
    }

    public Tab Allergy() {
        Tab tab = new Tab("الحساسية");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Button addBtn = new Button("+ إضافة حساسية جديدة");
        addBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

        ScrollPane scroll = new ScrollPane(allergyContainer);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        addBtn.setOnAction(e -> {
            VBox pane = new VBox(10);
            pane.setPadding(new Insets(10));
            pane.setStyle("-fx-background-color: #ffebee; -fx-border-color: #F44336; -fx-border-radius: 5;");
            TextField aName = new TextField();
            TextField aStatus = new TextField();
            ComboBox<String> aSeverity = new ComboBox<>(); aSeverity.getItems().addAll("بسيطة","متوسطة","خطيرة"); aSeverity.setValue("بسيطة");
            TextArea aFood = new TextArea(); aFood.setPrefHeight(50);
            TextArea aNotes = new TextArea(); aNotes.setPrefHeight(50);

            HBox r1 = new HBox(10, new Label("الحساسية:"), aName, new Label("الحالة:"), aStatus);
            HBox r2 = new HBox(10, new Label("الخطورة:"), aSeverity, new Label("المسبب:"), aFood);

            Button del = new Button("حذف X"); del.setOnAction(ev -> allergyContainer.getChildren().remove(pane));
            pane.getChildren().addAll(r1, r2, aNotes, del);
            allergyContainer.getChildren().add(pane);
        });
        root.getChildren().addAll(addBtn, scroll);
        tab.setContent(root);
        return tab;
    }

    public Tab createLifeInformationTab() {
        Tab tab = new Tab("معلومات شخصية");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10); grid.setHgap(15);
        grid.add(new Label("الوجبات/يوم:"), 0, 0); grid.add(makeDecimalOnly(mealsNumber), 1, 0);
        grid.add(new Label("الميزانية:"), 0, 1); grid.add(makeDecimalOnly(budgetField), 1, 1);
        setupTextArea(breakfastArea, "الصبوح", grid, 2);
        setupTextArea(lunchArea, "الغداء", grid, 3);
        setupTextArea(dinnerArea, "العشاء", grid, 4);
        setupTextArea(drinksArea, "المشروبات", grid, 0, 2);
        setupTextArea(snacksArea,"الحلويات", grid, 1, 2);
        setupTextArea(sleepArea, "النوم", grid, 2, 2);
        setupTextArea(badHabitsArea, "عادات سيئة", grid, 4, 2);
        setupTextArea(foodDislikeArea,"الطعام الغير مرغوب به",grid, 3, 2);
        tab.setContent(grid);
        return tab;
    }

    private void setupTextArea(TextArea area, String label, GridPane grid, int row, int col) {
        grid.add(new Label(label + ":"), col, row);
        area.setPrefRowCount(2); area.setWrapText(true);
        grid.add(area, col + 1, row);
    }

    private void setupTextArea(TextArea area, String label, GridPane grid, int row) {
        setupTextArea(area, label, grid, row, 0);
    }

    private void switchTab(String title, Supplier<Tab> tabCreator) {
        tabPane.getTabs().stream().filter(t -> t.getText().equals(title)).findFirst()
                .ifPresentOrElse(
                        t -> tabPane.getSelectionModel().select(t),
                        () -> {
                            // استدعاء نافذة التحميل عند طلب فتح قسم جديد
                            BackgroundRunner.run("جاري تجهيز الواجهة...", () -> {

                                // محاكاة تأخير بسيط جداً في الخلفية لضمان نعومة ظهور الدائرة
                                // (يمكن إزالته، لكنه مفيد بصرياً إذا كان بناء الواجهة سريعاً)
                                Thread.sleep(150);
                                return true;

                            }, () -> {

                                // بناء عناصر الواجهة حصراً داخل الـ JavaFX Thread
                                Tab newTab = tabCreator.get();
                                tabPane.getTabs().add(newTab);
                                tabPane.getSelectionModel().select(newTab);

                            });
                        }
                );
    }
    public Client saveClient(){
        if (genderGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "بيانات ناقصة", "الرجاء اختيار جنس العميل.");
            return null;
        }
        char gender = ((RadioButton) genderGroup.getSelectedToggle()).getText().charAt(0);

        String fullName = nameField.getText().trim();

        if (fullName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "بيانات ناقصة", "لا يمكن حفظ العميل! الرجاء إدخال اسم العميل.");
            return null;
        }

        String[] names = fullName.split("\\s+");

        if (names.length < 2) {
            showAlert(Alert.AlertType.WARNING, "بيانات غير مكتملة", "يرجى إدخال الاسم كاملاً (الاسم الأول واللقب أو اسم الأب على الأقل).");
            return null;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "بيانات ناقصة", "الرجاء إدخال رقم هاتف العميل.");
            return null;
        }
        if (datePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "بيانات ناقصة", "الرجاء تحديد تاريخ ميلاد العميل من التقويم.");
            return null;
        }

        return new Client(names, gender, datePicker.getValue(), phoneField.getText());
    }

    public HealthData saveHealthData(){
        return new HealthData.HealthDataBuilder()
                .currentMedication(currentMedication.getText())
                .familyMedicalHistory(familyMedicalHistory.getText())
                .medicalHistory(medicalHistory.getText())
                .notes(healthNotes.getText())
                .build();
    }

    public LifeStyleInformation saveLifeStyleInformation() {
        return new LifeStyleInformation.Builder()
                .mealsPerDay(
                        mealsNumber.getText().trim().isEmpty()
                                ? 1
                                : Integer.parseInt(mealsNumber.getText().trim())
                )
                .budget(
                        budgetField.getText().trim().isEmpty()
                                ? BigDecimal.ZERO
                                : new BigDecimal(budgetField.getText().trim())
                )
                .breakfast(breakfastArea.getText())
                .lunch(lunchArea.getText())
                .dinner(dinnerArea.getText())
                .snacks(snacksArea.getText())
                .drinks(drinksArea.getText())
                .sleepHours(sleepArea.getText())
                .foodDislike(foodDislikeArea.getText())
                .badHabits(badHabitsArea.getText())
                .build();
    }


    public List<PatientChronicDisease> savePatientChronicDisease(Client client){
        ArrayList<PatientChronicDisease> diseasesList = new ArrayList<>();

        for (Node node : diseasesContainer.getChildren()) {
            if (node instanceof VBox pane) {
                HBox r1 = (HBox) pane.getChildren().get(0);
                HBox r2 = (HBox) pane.getChildren().get(1);

                TextArea notesArea = (TextArea) pane.getChildren().get(2);
                String name = ((TextField) r1.getChildren().get(1)).getText();
                String status = ((TextField) r1.getChildren().get(3)).getText();

                // استخدام قيمة ComboBox مع التحقق من عدم وجود null
                ComboBox<String> sevBox = (ComboBox<String>) r2.getChildren().get(1);
                String sev = sevBox.getValue() != null ? sevBox.getValue() : "";

                String contraindicated = ((TextArea) r2.getChildren().get(3)).getText();

                if (!name.isEmpty()) {
                    ChronicDisease disease = new ChronicDisease(name);

                    // 2. إضافة الأوبجكت مباشرة إلى القائمة المعدلة
                    diseasesList.add(new PatientChronicDisease(client, disease, status, sev, notesArea.getText(), contraindicated));
                }
            }
        }
        return diseasesList;
    }

    public List<PatientAllergy> savePatientAllergy(Client client){
        ArrayList<PatientAllergy> allergiesList = new ArrayList<>();

        for (Node node : allergyContainer.getChildren()) {
            if (node instanceof VBox pane) {
                HBox r1 = (HBox) pane.getChildren().get(0);
                HBox r2 = (HBox) pane.getChildren().get(1);

                TextArea notesArea = (TextArea) pane.getChildren().get(2);
                String name = ((TextField) r1.getChildren().get(1)).getText();
                String status = ((TextField) r1.getChildren().get(3)).getText();
                String sev = ((ComboBox<String>) r2.getChildren().get(1)).getValue();
                String contraindicated = ((TextArea) r2.getChildren().get(3)).getText();
                if (!name.isEmpty()){
                    Allergy allergy = new Allergy(name);
                    allergiesList.add(new PatientAllergy(client,allergy ,status, sev, notesArea.getText(), contraindicated));
                }
            }
        }
        return allergiesList;
    }

    public void handleSave() {
        // 1. تجميع البيانات والتحقق منها (يجب أن يتم في مجرى الواجهة الرئيسي)
        Client client;
        HealthData healthData;
        LifeStyleInformation lifeStyleInformation;


        try {
            client = saveClient();
            if (client == null) return; // تم إيقاف العملية (يوجد نقص في البيانات وتم إظهار تنبيه)

            client.setHealthData(saveHealthData());
            client.setLifeStyleInformation(saveLifeStyleInformation());
            client.setChronicDiseases(savePatientChronicDisease(client));
            client.setAllergies(savePatientAllergy(client));

        } catch (NumberFormatException ex){
            showAlert(Alert.AlertType.ERROR, "خطأ في التنسيق الرقمي ❌", ex.getMessage());
            return;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showAlert(Alert.AlertType.WARNING, "تنبيه - بيانات غير صالحة ⚠", ex.getMessage());
            return;
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "خطأ ❌", ex.getMessage());
            return;
        }

        // 2. استدعاء نافذة التحميل وتنفيذ الحفظ في مجرى خلفي
        BackgroundRunner.run("جاري حفظ بيانات العميل...", () -> {

            // هذا الكود يعمل في الـ Background Thread
            ClientAPI service = ClientApiManager.getInstance().getClientAPI();

            // أي استثناءات مثل SQLException أو TimeoutException سيتم التقاطها
            // وعرضها تلقائياً بواسطة BackgroundRunner

            return service.saveFullClientData(client);

        }, () -> {

            // 3. ما بعد النجاح (يتم تنفيذه في مجرى الواجهة الرئيسي JavaFX Thread)
            showAlert(Alert.AlertType.INFORMATION, "تم الحفظ بنجاح ✔", "تم حفظ وتوثيق بيانات العميل'" + client.getFirstName() + "' في قاعدة البيانات بنجاح.");
            clearAllFields();

        });
    }
    private void clearBasicData() {
        nameField.clear();
        phoneField.clear();
        datePicker.setValue(LocalDate.of(2000, 1, 1));
        if (!genderGroup.getToggles().isEmpty()) {
            genderGroup.getToggles().get(0).setSelected(true);
        }
        System.out.println("تم مسح البيانات الأساسية للعميل.");
    }

    private void clearHealthData() {
        currentMedication.clear();
        medicalHistory.clear();
        familyMedicalHistory.clear();
        healthNotes.clear();

        diseasesContainer.getChildren().clear();
        allergyContainer.getChildren().clear();
        diseases.clear();
        allergies.clear();

        tabPane.getTabs().removeIf(tab -> tab.getText().equals("بيانات العميل الصحية") ||
                tab.getText().equals("الأمراض") ||
                tab.getText().equals("الحساسية"));
        System.out.println("تم مسح كافة البيانات الصحية والأمراض المضافة.");
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

        tabPane.getTabs().removeIf(tab -> tab.getText().equals("معلومات شخصية"));
        System.out.println("تم مسح بيانات نمط الحياة.");
    }

    private void clearAllFields() {
        clearBasicData();
        clearHealthData();
        clearLifeData();

        tabPane.getSelectionModel().select(0);
        System.out.println("تم تصفير الواجهة بالكامل.");
    }
}