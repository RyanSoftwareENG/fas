package user_interface.offer;

import api.ClientApiManager;
import dto.SessionListDTO;
import entities.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import runner.BackgroundRunner; // 👈 إضافة الاستيراد
import api.ClientAPI;
import api.SessionAPI;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SessionDataController {

    // --- ScrollPanes للتحكم بالصعود للأعلى ---
    @FXML private ScrollPane scrollSession;
    @FXML private ScrollPane scrollBody;
    @FXML private ScrollPane scrollPlan;

    // --- Header ---
    @FXML private Label lblSessionId;
    @FXML private Label lblClientId;

    // --- Tab 1: Session Info ---
    @FXML private Label lblPrice;
    @FXML private Label lblDuration;
    @FXML private Label lblUploadDate;
    @FXML private Label lblModificationDate;
    @FXML private Label lblNotes;

    // --- Tab 2: Body Data ---
    @FXML private Label lblBodyId;
    @FXML private Label lblHeight;
    @FXML private Label lblWeight;
    @FXML private Label lblFat;
    @FXML private Label lblSMM;
    @FXML private Label lblMuscle;
    @FXML private Label lblPhysicalActivity;
    @FXML private Label lblActivityFactor;

    @FXML private Label lblBmi;
    @FXML private Label lblStatus;
    @FXML private Label lblBMR;
    @FXML private Label lblTDEE;

    @FXML private Label lblArm;
    @FXML private Label lblChest;
    @FXML private Label lblWaist;
    @FXML private Label lblAbdominal;
    @FXML private Label lblHip;
    @FXML private Label lblThigh;
    @FXML private Label lblCalf;

    @FXML private Label lblBodyUploadDate;
    @FXML private Label lblBodyModDate;

    // --- Tab 3: Nutrition Plan ---
    @FXML private Label lblPlanId;
    @FXML private Label lblPlanStatus;
    @FXML private Label lblPlanGoal;
    @FXML private Label lblPlanStart;
    @FXML private Label lblPlanEnd;
    @FXML private Label lblPlanDuration;
    @FXML private Label lblPlanMeals;
    @FXML private Label lblProtein;
    @FXML private Label lblCarb;
    @FXML private Label lblFatMacro;
    @FXML private Label lblTotalCalories, lblMealsCount, lblWaterIntake, lblNote;

    // Table Foods
    @FXML private TableView<PlanFoodItem> tableFoods;
    @FXML private TableColumn<PlanFoodItem, String> colMealType;
    @FXML private TableColumn<PlanFoodItem, String> colFoodName;
    @FXML private TableColumn<PlanFoodItem, Number> colQuantity;
    @FXML private TableColumn<PlanFoodItem, String> colUnit;
    @FXML private TableColumn<PlanFoodItem, Number> colCalories;
    @FXML private TableColumn<PlanFoodItem, Number> colFoodProtein;
    @FXML private TableColumn<PlanFoodItem, Number> colFoodCarb;
    @FXML private TableColumn<PlanFoodItem, Number> colFoodFat;
    @FXML private TableColumn<PlanFoodItem, String> colCostLevel;

    // --- Tab 4: Examinations ---
    @FXML private TableView<Examination> tableExaminations;
    @FXML private TableColumn<Examination, Number> colExamId;
    @FXML private TableColumn<Examination, String> colExamName;
    @FXML private TableColumn<Examination, String> colExamDate;
    @FXML private TableColumn<Examination, String> colExamMod;
    @FXML private TableColumn<Examination, String> colExamNotes;

    // تم تغيير النوع البرمجي هنا ليدعم بناء الحاوية الصورية المعززة بصرياً
    @FXML private TableColumn<Examination, HBox> colExamImage;

    private Session currentSession;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // لتسهيل رؤية الصور، نرفع الارتفاع الافتراضي لأسطر جدول الفحوصات الطبية تلقائياً[cite: 9]
        tableExaminations.setStyle("-fx-cell-size: 90px;");

        // --- تهيئة أعمدة جدول الأطعمة ---[cite: 9]
        colMealType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMealType()));
        colQuantity.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantity().doubleValue()));
        colUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        colFoodName.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue().getFoodItem();
            return new SimpleStringProperty(item != null ? item.getFoodName() : "غير متوفر");
        });
        colCalories.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue().getFoodItem();
            return new SimpleDoubleProperty(item != null ? item.getCalories().doubleValue() : 0);
        });
        colFoodProtein.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue().getFoodItem();
            return new SimpleDoubleProperty(item != null ? item.getProtein().doubleValue() : 0);
        });
        colFoodCarb.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue().getFoodItem();
            return new SimpleDoubleProperty(item != null ? item.getCarbohydrates().doubleValue() : 0);
        });
        colFoodFat.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue().getFoodItem();
            return new SimpleDoubleProperty(item != null ? item.getFat().doubleValue() : 0);
        });
        colCostLevel.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue().getFoodItem();
            return new SimpleStringProperty(item != null && item.getCostLevel() != null ? item.getCostLevel() : "-");
        });

        // --- تهيئة أعمدة جدول الفحوصات ---[cite: 9]
        colExamId.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getExaminationId().longValue()));
        colExamName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExaminationName()));
        colExamNotes.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));
        colExamDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUploadDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getUploadDate().format(formatter));
            }
            return new SimpleStringProperty("-");
        });
        colExamMod.setCellValueFactory(cellData -> {
            if (cellData.getValue().getModificationDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getModificationDate().format(formatter));
            }
            return new SimpleStringProperty("-");
        });

        // --- بناء مصنع الخلية الصورية الذكي لعرض الفحص الفعلي ممركزاً ---[cite: 9]
        colExamImage.setCellValueFactory(cellData -> {
            String path = cellData.getValue().getExaminationImage();
            HBox cellContainer = new HBox();
            cellContainer.setAlignment(Pos.CENTER);

            if (path != null && !path.trim().isEmpty()) {
                try {
                    String resolvedUrl = path;
                    if (!resolvedUrl.startsWith("http://") && !resolvedUrl.startsWith("https://") && !resolvedUrl.startsWith("file:")) {
                        resolvedUrl = new File(path).toURI().toString();
                    }

                    Image img = new Image(resolvedUrl, 80, 80, true, true, true);
                    ImageView imgView = new ImageView(img);

                    imgView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 4, 0, 0, 1); -fx-cursor: hand;");

                    cellContainer.getChildren().add(imgView);
                } catch (Exception e) {
                    Label errLbl = new Label("⚠️ خطأ في الملف");
                    errLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
                    cellContainer.getChildren().add(errLbl);
                }
            } else {
                Label noImgLbl = new Label("📷 بلا صورة مرفقة");
                noImgLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                cellContainer.getChildren().add(noImgLbl);
            }

            return new SimpleObjectProperty<>(cellContainer);
        });
    }

    public void setSessionData(SessionListDTO session) {
        // --- رسالة خطأ: لم يتم تمرير جلسة أساساً ---[cite: 9]
        if (session == null) {
            showErrorMessage("خطأ في عرض البيانات",
                    "عذراً، لا توجد جلسة محددة للعرض. يرجى اختيار جلسة من القائمة أولاً.");
            return;
        }

        // مصفوفات لحمل البيانات بين خيوط المعالجة بأمان[cite: 9]
        final Session[] fullSessionHolder = new Session[1];
        final Client[] clientHolder = new Client[1];
        final String[] clientErrorHolder = new String[1];
        final Exception[] exceptionHolder = new Exception[1];

        // عزل الاتصال بقاعدة البيانات في الخلفية لتجنب تجميد الواجهة (UI Freeze)
        BackgroundRunner.run("جاري تحميل بيانات الجلسة الشاملة... ⏳", () -> {
            try {
                // --- جلب كامل بيانات الجلسة من قاعدة البيانات ---[cite: 9]
                SessionAPI getSession = ClientApiManager.getInstance().getSessionAPI();
                fullSessionHolder[0] = getSession.getAllDataSession(session.getId());
                System.out.println("numbre session of database"+fullSessionHolder[0].getId());

                // --- جلب بيانات العميل المرتبط بالجلسة ---[cite: 9]

                clientHolder[0] = session.getClient();

                if (clientHolder[0] == null && session.getClientID() <= 0) {
                    clientErrorHolder[0] = "getClient.getExceptionMessage()";
                }
            } catch (Exception e) {
                exceptionHolder[0] = e;
            }
            return true;
        }, () -> {
            // --- معالجة الأخطاء المحتملة على خيط الواجهة ---[cite: 9]
            if (exceptionHolder[0] != null) {
                showErrorMessage("خطأ غير متوقع",
                        "حدث خطأ غير متوقع أثناء تحميل بيانات الجلسة:\n" + exceptionHolder[0].getMessage());
                return;
            }

            this.currentSession = fullSessionHolder[0];

            if (this.currentSession == null) {
                showErrorMessage("فشل جلب البيانات",
                        "تعذر تحميل بيانات الجلسة رقم (" + session.getId() + ") من قاعدة البيانات.\n" +
                                "يرجى التحقق من الاتصال والمحاولة مرة أخرى.");
                return;
            }

            // 1. تعبئة الرأس وبيانات الجلسة (Tab 1)[cite: 9]
            lblSessionId.setText(String.valueOf(session.getId()));
            Client client = clientHolder[0];

            if (client != null) {
                lblClientId.setText(client.getFullName());
            } else if(session.getClientID() > 0) {
                lblClientId.setText("عميل غير معروف (ID: " + session.getClientID() + ")");
                System.err.println("تحذير: لم يتم العثور على العميل رقم " + session.getClientID()
                        + " — " + clientErrorHolder[0]);
            } else {
                lblClientId.setText("عميل غير معروف ");
            }

            lblPrice.setText(currentSession.getPrice() + " ريال");
            lblDuration.setText(currentSession.getDuration() != null ? currentSession.getDuration().toString() : "-");
            lblNotes.setText(currentSession.getNotes() != null && !currentSession.getNotes().isEmpty() ? currentSession.getNotes() : "لا توجد ملاحظات مسجلة لهذه الجلسة.");

            if (currentSession.getUploadTime() != null) lblUploadDate.setText(currentSession.getUploadTime().format(formatter));
           // if (currentSession.getModificationDate() != null) lblModificationDate.setText(currentSession.getModificationDate().format(formatter));

            // 2. تعبئة القياسات البدنية (Tab 2)[cite: 9]
            BodyData bodyData = currentSession.getBodyData();
            if (bodyData != null) {
                lblBodyId.setText(String.valueOf(bodyData.getSessionId()));
                lblHeight.setText(bodyData.getHeight() + " سم");
                lblWeight.setText(bodyData.getWeight() + " كجم");
                lblFat.setText(bodyData.getBodyFatPercentage() + " %");
                lblSMM.setText(String.valueOf(bodyData.getSmm()));
                lblMuscle.setText(bodyData.getMuscleMass() + " كجم");
                lblPhysicalActivity.setText(bodyData.getPhysicalActivity() != null ? bodyData.getPhysicalActivity() : "-");
                lblActivityFactor.setText(String.valueOf(bodyData.getActivityFactor()));

                // التحليلات[cite: 9]
                lblBmi.setText(String.format("%.1f", bodyData.getBMI()));
                lblStatus.setText(bodyData.getStatus());

                // حسابات افتراضية للـ BMR و TDEE محمية من الـ NullPointerException[cite: 9]
                char gender = client != null ? client.getGender() : 'M'; // افتراضي إذا لم يوجد عميل
                int age = client != null ? client.getAge() : 25; // افتراضي إذا لم يوجد عميل
                lblBMR.setText(String.format("%.1f سعرة", bodyData.getBMR(gender, age)));
                lblTDEE.setText(String.format("%.1f سعرة", bodyData.getTDEE(gender, age)));

                // المحيطات السبعة بالكامل دون إهمال[cite: 9]
                lblArm.setText(bodyData.getArm_C() + " سم");
                lblChest.setText(bodyData.getChest_C() + " سم");
                lblWaist.setText(bodyData.getWaist_C() + " سم");
                lblAbdominal.setText(bodyData.getAbdominal_C() + " سم");
                lblHip.setText(bodyData.getHip_C() + " سم");
                lblThigh.setText(bodyData.getMidThigh_C() + " سم");
                lblCalf.setText(bodyData.getCalf_C() + " سم");

                // التواريخ الزمنية للقياسات[cite: 9]
// منسّق مخصص للتواريخ فقط (بدون HH:mm)
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                if (bodyData.getMeasurementDate() != null) {
                    lblBodyUploadDate.setText(bodyData.getMeasurementDate().format(dateFormatter));
                }               // if(bodyData.getModificationDate() != null) lblBodyModDate.setText(bodyData.getModificationDate().format(formatter));
            } else {
                // --- رسالة تحذير: لا توجد قياسات بدنية مسجلة ---[cite: 9]
                showWarningMessage("بيانات ناقصة", "لا توجد قياسات بدنية مسجلة لهذه الجلسة.");
            }

            // 3. تعبئة الخطة الغذائية (Tab 3)[cite: 9]
            NutritionPlan plan = currentSession.getNutritionPlan();
            if (plan != null) {
                lblPlanId.setText(String.valueOf(plan.getPlanId()));
                lblPlanStatus.setText(plan.getPlanStatus() != null ? plan.getPlanStatus() : "-");
                lblPlanGoal.setText(plan.getTargetGoal() != null ? plan.getTargetGoal() : "-");

                if (plan.getStartDate() != null) lblPlanStart.setText(plan.getStartDate().format(dateFormatter));
                if (plan.getEndDate() != null) lblPlanEnd.setText(plan.getEndDate().format(dateFormatter));
                lblPlanDuration.setText(String.valueOf(plan.getPlanDuration()));

                lblPlanMeals.setText(plan.getMealDistribution() != null ? plan.getMealDistribution() : "-");
                lblProtein.setText(plan.getProteinAmount() + " جم");
                lblCarb.setText(plan.getCarbohydratesAmount() + " جم");
                lblFatMacro.setText(plan.getFatAmount() + " جم");

                if (plan.getWaterIntake() == null || plan.getWaterIntake().trim().isEmpty()) {
                    String genderStr = client != null ? String.valueOf(client.getGender()) : "M";
                    double idealWater = plan.calculateIdealWater(bodyData != null ? bodyData.getWeight() : BigDecimal.valueOf(70.0), genderStr).doubleValue();
                    lblWaterIntake.setText(String.format(Locale.US, "%.1f Ltr", idealWater));
                } else {
                    lblWaterIntake.setText(plan.getWaterIntake());
                }

                lblNote.setText(plan.getNotes());
                lblMealsCount.setText(String.valueOf(plan.getMealsCount()));
                lblTotalCalories.setText(String.valueOf(plan.getTotalCalories()));

                // تعبئة جدول الأطعمة بالتفاصيل الكاملة والماكروز الخاصة بكل عنصر[cite: 9]
                if (plan.getSelectedFoods() != null) {
                    tableFoods.setItems(FXCollections.observableArrayList(plan.getSelectedFoods()));
                }
            } else {
                // --- رسالة تحذير: لا توجد خطة غذائية مرتبطة ---[cite: 9]
                showWarningMessage("بيانات ناقصة", "لا توجد خطة غذائية مرتبطة بهذه الجلسة.");
            }

            // 4. تعبئة الفحوصات الطبية بما فيها الصور المحدثة (Tab 4)[cite: 9]
            if (currentSession.getExaminations() != null) {
                tableExaminations.setItems(FXCollections.observableArrayList(currentSession.getExaminations()));
            }

            // --- إصلاح انزلاق شريط التمرير الافتراضي في بيئات الـ RTL البرمجية لـ JavaFX ---[cite: 9]
            Platform.runLater(() -> {
                if (scrollSession != null) scrollSession.setVvalue(0.0);
                if (scrollBody != null) scrollBody.setVvalue(0.0);
                if (scrollPlan != null) scrollPlan.setVvalue(0.0);
            });
        });
    }

    // --- دالة مساعدة لعرض رسالة خطأ ---[cite: 9]
    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- دالة مساعدة لعرض رسالة تحذير ---[cite: 9]
    private void showWarningMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}