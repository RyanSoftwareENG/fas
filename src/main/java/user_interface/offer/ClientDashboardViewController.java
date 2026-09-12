package user_interface.offer;

import entities.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientDashboardViewController {

    @FXML private Label lblHeaderTitle;
    @FXML private Label lblClientID, lblFullName, lblGender, lblBirthDate, phoneNumber;
    @FXML private Label uploadTime;
    @FXML private Label modificationDate;

    // عناصر الـ Lifestyle المعيشية (تمت إضافة الحقول الثلاثة الناقصة هنا)
    @FXML private Label lblMealsPerDay, lblBudget, lblBreakfast, lblLunch, lblDinner, lblSleepHours;
    @FXML private Label lblSnacks, lblDrinks, lblBadHabits;

    @FXML private Label lblCurrentMeds, lblDislikedFoods, lblMedicalHistory;

    @FXML private ListView<String> listViewDiseases;
    @FXML private ListView<String> listViewAllergies;

    // عناصر جدول المراقبة لجلسات الكلاس Session المعتمد
    @FXML private TableView<Session> tblSessions;
    @FXML private TableColumn<Session, Long> colSessionID;
    @FXML private TableColumn<Session, String> colSessionUploadTime;
    @FXML private TableColumn<Session, String> colSessionDuration;
    @FXML private TableColumn<Session, BigDecimal> colSessionPrice;


    @FXML private Label lblSessionNotes;

    // عناصر الـ BodyData الأساسية
    @FXML private Label lblHeight, lblWeight, lblSMM, lblMuscleMass, lblBodyFat, lblPhysicalActivity;
    @FXML private Label lblArmC, lblChestC, lblWaistC, lblAbdominalC, lblHipC, lblMidThighC, lblCalfC;

    // عناصر الـ BodyData الجديدة (المحسوبة والتواريخ)
    @FXML private Label lblActivityFactor, lblBMI, lblBMIStatus, lblBMR, lblTDEE;
    @FXML private Label lblBodyDataUpload, lblBodyDataMod;

    // عناصر تبويب الفحوصات والخطط الغذائية
    @FXML private ScrollPane examintionList;

    // عناصر الخطة
    @FXML private Label lblPlanGoal, lblPlanDatesAndStatus, lblPlanProtein, lblPlanFat, lblPlanCarbs, lblMeal, lblMealDistribution ,lblTotalCalories, lblMealsCount, lblWaterIntake, lblNote;

    @FXML
    private Button btnBack;

    private Client currentClient;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // عرض كل بيانات العميل
    public void setClientFullDashboardData(Client client) {
        if (client == null) return;
        this.currentClient = client;

        // 1. فك وقراءة البيانات الشخصية والهوية
        lblHeaderTitle.setText("الملف الطبي الموحد للشخص الحاضر: " + client.getFullName());
        lblClientID.setText(String.valueOf(client.getClientID()));
        lblFullName.setText(client.getFullName());
        lblGender.setText(client.getGender() == 'M' ? "ذكر (Male)" : "أنثى (Female)");
        lblBirthDate.setText(client.getBirthDate() != null ? client.getBirthDate().toString() + " (" + client.getAge() + " سنة)" : "-");
        phoneNumber.setText(client.getContactNumber() !=null ? client.getContactNumber() : " لا يوجد رقم");

        if (client.getModificationDate() != null) {
            modificationDate.setText(client.getModificationDate().format(dateTimeFormatter));
        } else {
            modificationDate.setText("لم يتم التعديل"); // حتى لا تتركها فارغة أو تأخذ قيمة افتراضية
        }

        if (client.getUploadDate() != null) {
            uploadTime.setText(client.getUploadDate().format(dateTimeFormatter));
        } else {
            uploadTime.setText("لم يحدد");
        }

        // 2. فك وقراءة بيانات النمط الحياتي (تحديث شامل ليعرض كل بيانات الكلاس)
        if (client.getLifeStyleInformation() != null) {
            LifeStyleInformation life = client.getLifeStyleInformation();
            lblMealsPerDay.setText(life.getMealsPerDay() + " وجبات");
            lblBudget.setText(life.getBudget() + " ريال");
            lblBreakfast.setText(life.getBreakfast());
            lblLunch.setText(life.getLunch());
            lblDinner.setText(life.getDinner());
            lblSleepHours.setText(life.getSleepHours() + " ساعات");

            // تعيين قيم المتغيرات الجديدة المتواجدة في كلاس الـ Lifestyle لشاشات العرض
            lblSnacks.setText(life.getSnacks() != null ? life.getSnacks() : "غير محدد");
            lblDrinks.setText(life.getDrinks() != null ? life.getDrinks() : "غير محدد");
            lblBadHabits.setText(life.getBadHabits() != null ? life.getBadHabits() : "لا يوجد");

            lblDislikedFoods.setText(life.getFoodDislike() != null ? life.getFoodDislike() : "لا يوجد");
        }

        // 3. فك وقراءة بيانات التاريخ الطبي (HealthData)
        if (client.getHealthData() != null) {
            HealthData health = client.getHealthData();
            lblCurrentMeds.setText(health.getCurrentMedication());
            lblMedicalHistory.setText("العائلي: " + health.getFamilyMedicalHistory() + " | الشخصي: " + health.getMedicalHistory() + " | ملاحظات: " + health.getNotes());
        }

        // التعامل مع كلاس الوراثة للمريض
        if (client.getChronicDiseases() != null) {
            List<String> diseaseStrings = new ArrayList<>();
                for (PatientChronicDisease cd : client.getChronicDiseases()) {
                    diseaseStrings.add("📌 " + cd.getChronicDisease().getDiseaseName() + " [الشدة: " + cd.getSeverity() + " | الحالة: " + cd.getStatus() + "]" +
                            (cd.getContraindicated() != null ? " 🚫 ممنوعات: " + cd.getContraindicated() : "" )+ "    ");

            }
            listViewDiseases.setItems(FXCollections.observableArrayList(diseaseStrings));

            List<String> allergyStrings = new ArrayList<>();
            if (client.getAllergies() != null) {
                for (PatientAllergy al : client.getAllergies()) {
                    allergyStrings.add("⚠️ " + al.getAllergy().getAllergyName() + " [الخطورة: " + al.getSeverity() + "]" +
                            (al.getContraindicated() != null ? " 🥦 يمنع تناول: " + al.getContraindicated() : "") + "  ");
                }
            }
            listViewAllergies.setItems(FXCollections.observableArrayList(allergyStrings));
        }

        setupSessionsArchitecture();
    }

    // عرض بيانات الجلسات الخاصة بالعميل
    private void setupSessionsArchitecture() {
        colSessionID.setCellValueFactory(cellData -> {
            try { return new SimpleObjectProperty<>(cellData.getValue().getId()); }
            catch (Exception e) { return new SimpleObjectProperty<>(0L); }
        });

        colSessionUploadTime.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue().getUploadTime() != null) {
                    Object timeObj = cellData.getValue().getUploadTime();
                    if (timeObj instanceof java.time.LocalDateTime) {
                        return new SimpleStringProperty(((java.time.LocalDateTime) timeObj).format(dateTimeFormatter));
                    } else {
                        return new SimpleStringProperty(timeObj.toString());
                    }
                }
            } catch (Exception e) {}
            return new SimpleStringProperty("-");
        });

        colSessionDuration.setCellValueFactory(cellData -> {
            try { return new SimpleObjectProperty<>(cellData.getValue().getDuration()); }
            catch (Exception e) { return null; }
        });

        colSessionPrice.setCellValueFactory(cellData -> {
            try { return new SimpleObjectProperty<>(cellData.getValue().getPrice()); }
            catch (Exception e) { return null; }
        });

        ObservableList<Session> observableSessions = FXCollections.observableArrayList();
        if (currentClient.getSessions() != null) {
            observableSessions.addAll(currentClient.getSessions());
        }

        tblSessions.setItems(observableSessions);
        tblSessions.getSelectionModel().selectedItemProperty().addListener((observable, oldSession, selectedSession) -> {
            if (selectedSession != null) {
                populateSelectedSessionEmbeddedData(selectedSession);
            }
        });

        if (!observableSessions.isEmpty()) {
            tblSessions.getSelectionModel().select(0);
        }
    }

    // تعبئة وعرض كافة تفاصيل الجلسة المحددة
    private void populateSelectedSessionEmbeddedData(Session session) {
        lblSessionNotes.setText(session.getNotes() != null ? session.getNotes() : "لم يتم تسجيل ملاحظات نصية لهذه الجلسة.");

        BodyData body = session.getBodyData();
        if (body != null) {
            lblHeight.setText(body.getHeight() + " سم");
            lblWeight.setText(body.getWeight() + " كجم");
            lblSMM.setText(body.getSmm()+ " كجم");
            lblMuscleMass.setText(body.getMuscleMass() + " كجم");
            lblBodyFat.setText(body.getBodyFatPercentage() + " %");
            lblPhysicalActivity.setText(body.getPhysicalActivity());

            // جلب البيانات بشكل مباشر بدون تحويلات نصية معقدة
            int clientAge = currentClient.getAge();
            char clientGender = currentClient.getGender();


            lblActivityFactor.setText(String.valueOf(body.getActivityFactor()));
            lblBMI.setText(String.format("%.1f", body.getBMI()));
            lblBMIStatus.setText(body.getStatus());

            if(body.getWeight().doubleValue() > 0 && body.getHeight().doubleValue() > 0) {
                lblBMR.setText(String.format("%.1f سعرة حرارية", body.getBMR(clientGender, clientAge)));
                lblTDEE.setText(String.format("%.1f سعرة حرارية", body.getTDEE(clientGender, clientAge)));
            } else {
                lblBMR.setText("-");
                lblTDEE.setText("-");
            }

            // تواريخ السجل
            lblBodyDataUpload.setText(
                    body.getMeasurementDate() != null
                            ? body.getMeasurementDate().toString()
                            : "-"
            );
            // المحيطات
            lblArmC.setText(body.getArm_C().doubleValue() != 0 ? body.getArm_C() + " سم" : "-");
            lblChestC.setText(body.getChest_C().doubleValue() != 0 ? body.getChest_C() + " سم" : "-");
            lblWaistC.setText(body.getWaist_C().doubleValue() != 0 ? body.getWaist_C() + " سم" : "-");
            lblAbdominalC.setText(body.getAbdominal_C().doubleValue() != 0 ? body.getAbdominal_C() + " سم" : "-");
            lblHipC.setText(body.getHip_C().doubleValue() != 0 ? body.getHip_C() + " سم" : "-");
            lblMidThighC.setText(body.getMidThigh_C().doubleValue() != 0 ? body.getMidThigh_C() + " سم" : "-");
            lblCalfC.setText(body.getCalf_C().doubleValue() != 0 ? body.getCalf_C() + " سم" : "-");
        } else {
            clearBodyDataLabels();
        }

        NutritionPlan plan = session.getNutritionPlan();
        if (plan != null) {
            lblPlanGoal.setText(plan.getTargetGoal());
            lblPlanDatesAndStatus.setText("من: " + plan.getStartDate() + " إلى: " + plan.getEndDate() + " [" + plan.getPlanStatus() + "]");
            lblPlanProtein.setText(plan.getProteinAmount() + " جرام");
            lblPlanFat.setText(plan.getFatAmount() + " جرام");
            lblPlanCarbs.setText(plan.getCarbohydratesAmount() + " جرام");
            lblMealDistribution.setText(plan.getMealDistribution());
            if (plan.getWaterIntake() == null || plan.getWaterIntake().trim().isEmpty()) {
                BigDecimal idealWater = plan.calculateIdealWater(body.getWeight(), String.valueOf(currentClient.getGender()));
                lblWaterIntake.setText(String.format(Locale.US, "%.1f Ltr", idealWater));
            } else {
                lblWaterIntake.setText(plan.getWaterIntake());
            }
            lblNote.setText(plan.getNotes());
            lblMealsCount.setText(String.valueOf(plan.getMealsCount()));
            lblTotalCalories.setText(String.valueOf(plan.getTotalCalories()));

            if (plan.getSelectedFoods() != null && !plan.getSelectedFoods().isEmpty()) {
                StringBuilder mealsText = new StringBuilder();

                for (PlanFoodItem item : plan.getSelectedFoods()) {

                    String mealTypeArabic = "أخرى";
                    if (item.getMealType() != null) {
                        switch (item.getMealType().toLowerCase()) {
                            case "breakfast":
                                mealTypeArabic = "إفطار";
                                break;
                            case "lunch":
                                mealTypeArabic = "غداء";
                                break;
                            case "dinner":
                                mealTypeArabic = "عشاء";
                                break;
                            case "snack":
                                mealTypeArabic = "سناك";
                                break;
                            default:
                                mealTypeArabic = item.getMealType();
                        }
                    }
                    mealsText.append(String.format("الوجبة: %s  [%s] -> الكمية: %.1f %s\n",
                            item.getFoodItem().getFoodName(),
                            mealTypeArabic,
                            item.getQuantity(),
                            item.getUnit() != null ? item.getUnit() : "غرام"));
                }

                lblMeal.setText(mealsText.toString().trim());
                }} else {
            lblPlanGoal.setText("لا توجد خطة غذائية مخصصة.");
            lblPlanDatesAndStatus.setText("-");
            lblPlanProtein.setText("-");
            lblPlanFat.setText("-");
            lblPlanCarbs.setText("-");
            lblMeal.setText("-");
            lblMealDistribution.setText("-");
            lblWaterIntake.setText("-");
            lblNote.setText("-");
            lblMealsCount.setText("-");
            lblTotalCalories.setText("-");
        }

        VBox mainContainer = new VBox(12);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setAlignment(Pos.TOP_RIGHT);
        mainContainer.setStyle("-fx-background-color: transparent;");

        if (session.getExaminations() != null && !session.getExaminations().isEmpty()) {
            for (Examination exam : session.getExaminations()) {
                // 1. إنشاء وتنسيق الكرت الرئيسي
                VBox examCard = new VBox(10);
                examCard.setStyle("-fx-background-color: #ffffff; "
                        + "-fx-border-color: #e2e8f0; "
                        + "-fx-border-radius: 8px; "
                        + "-fx-background-radius: 8px; "
                        + "-fx-padding: 16px; "
                        + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 5, 0, 0, 1);");
                examCard.setAlignment(Pos.TOP_RIGHT);
                examCard.setMaxWidth(460);

                // 2. إعداد وتأمين ظهور نص العنوان (اسم الفحص فقط)
                Label lblTitle = new Label("🔬 فحص: " + exam.getExaminationName());
                lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
                lblTitle.setWrapText(true);
                lblTitle.setMaxWidth(Double.MAX_VALUE);
                lblTitle.setTextAlignment(TextAlignment.RIGHT);

                // 3. إعداد التاريخ في سطر جديد كلياً وبتنسيق سهل القراءة
                String dateStr = exam.getUploadDate() != null ? exam.getUploadDate().toString() : "غير محدد";
                Label lblDate = new Label("📅 تاريخ الرفع: " + dateStr);
                // قمنا باختيار لون رمادي/أزرق هادئ وحجم خط أصغر قليلاً لتمييزه كمعلومة ثانوية منسقة
                lblDate.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: normal;");
                lblDate.setWrapText(true);
                lblDate.setMaxWidth(Double.MAX_VALUE);
                lblDate.setTextAlignment(TextAlignment.RIGHT);

                // 4. إعداد وتأمين ظهور نص الملاحظات
                String noteText = exam.getNotes() != null ? exam.getNotes() : "لا يوجد";
                Label lblNotes = new Label("📝 ملاحظة الفحص: " + noteText);
                lblNotes.setWrapText(true);
                lblNotes.setMaxWidth(Double.MAX_VALUE);
                lblNotes.setTextAlignment(TextAlignment.RIGHT);
                lblNotes.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-line-spacing: 3px;");

                // إضافة العناصر النصية بالترتيب المرتب (العنوان ثم التاريخ في سطر منفصل ثم الملاحظات)
                examCard.getChildren().addAll(lblTitle, lblDate, lblNotes);

                // 5. معالجة وتغليف الصورة (Image Wrapper)
                String imgPath = exam.getExaminationImage();
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    try {
                        Image image = null;
                        if (imgPath.startsWith("http://") || imgPath.startsWith("https://") || imgPath.startsWith("file:")) {
                            image = new Image(imgPath, true);
                        } else {
                            File imgFile = new File(imgPath);
                            if (imgFile.exists()) {
                                image = new Image(imgFile.toURI().toString(), true);
                            }
                        }

                        if (image != null) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(410);
                            imageView.setPreserveRatio(true);

                            // الربر (Wrapper) للحفاظ على أبعاد وإطار الصورة
                            HBox imageWrapper = new HBox(imageView);
                            imageWrapper.setAlignment(Pos.CENTER);
                            imageWrapper.setStyle("-fx-border-color: #cbd5e1; "
                                    + "-fx-border-radius: 6px; "
                                    + "-fx-background-radius: 6px; "
                                    + "-fx-padding: 4px; "
                                    + "-fx-background-color: #f8fafc;");
                            imageWrapper.setMaxWidth(420);

                            Label lblImgHeader = new Label("📸 صورة ومرفق الفحص المخبري:");
                            lblImgHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed; -fx-font-size: 11px; -fx-padding: 6 0 0 0;");
                            lblImgHeader.setWrapText(true);
                            lblImgHeader.setMaxWidth(Double.MAX_VALUE);
                            lblImgHeader.setTextAlignment(TextAlignment.RIGHT);

                            examCard.getChildren().addAll(lblImgHeader, imageWrapper);
                        }
                    } catch (Exception e) {
                        System.err.println("خطأ أثناء تحميل صورة الفحص: " + e.getMessage());
                    }
                }

                // إدخال الكرت المنسق إلى الحاوية الرئيسية
                mainContainer.getChildren().add(examCard);
            }
        } else {
            Label noExamLabel = new Label("لم يتم رفع أو إلحاق مستندات أو صور فحوصات سريرية خلال هذه الجلسة.");
            noExamLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            mainContainer.getChildren().add(noExamLabel);
        }

        examintionList.setContent(mainContainer);
        examintionList.setFitToWidth(true);
    }

    // تنظيف حقول البيانات الجسدية في حال عدم تسجيل بيانات
    private void clearBodyDataLabels() {
        lblHeight.setText("-"); lblWeight.setText("-"); lblSMM.setText("-");
        lblMuscleMass.setText("-"); lblBodyFat.setText("-"); lblPhysicalActivity.setText("-");
        lblArmC.setText("-"); lblChestC.setText("-"); lblWaistC.setText("-");
        lblAbdominalC.setText("-"); lblHipC.setText("-"); lblMidThighC.setText("-"); lblCalfC.setText("-");
        lblActivityFactor.setText("-"); lblBMI.setText("-"); lblBMIStatus.setText("-");
        lblBMR.setText("-"); lblTDEE.setText("-");
        lblBodyDataUpload.setText("-"); lblBodyDataMod.setText("-");
    }

    // تنسيق اسم العميل


    // زر اغلاق الواجهة
    @FXML
    void handleCloseDashboard(ActionEvent event) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}