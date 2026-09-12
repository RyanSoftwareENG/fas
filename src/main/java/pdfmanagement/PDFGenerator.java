package pdfmanagement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;


import entities.AdminFinancialSummary;
import entities.BodyData;
import entities.Client;
import entities.NutritionPlan;
import entities.PlanFoodItem;
import entities.Session;
import entities.SessionReport;

public class PDFGenerator {

    Client client;
    // =================================================================================
    // 1. مدير التنسيقات (Style Manager) - لإدارة الألوان والخطوط مركزياً
    // =================================================================================
    public static class ReportStyles {
        public static final BaseColor COLOR_PRIMARY = new BaseColor(124, 179, 66);   // أخضر الواجهة
        public static final BaseColor COLOR_SECONDARY = new BaseColor(198, 93, 30);  // برتقالي الواجهة
        public static final BaseColor COLOR_TEXT_DARK = new BaseColor(15, 23, 42);
        public static final BaseColor COLOR_BG_LIGHT = new BaseColor(248, 250, 252);
        public static final BaseColor COLOR_BORDER = new BaseColor(226, 232, 240);

        public Font fontCoverTitle, fontSection, fontSubSection, fontBold, fontNormal, fontNumber;

        public ReportStyles() throws Exception {

            java.net.URL fontUrl = getClass().getClassLoader().getResource("fonts/arial.ttf");
            if (fontUrl == null) {
                throw new java.io.FileNotFoundException("fonts/arial.ttf not found on classpath (expected under src/main/resources/fonts/)");
            }
            String fontPath = fontUrl.toExternalForm();
            BaseFont arabicBaseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            fontCoverTitle = new Font(arabicBaseFont, 24, Font.BOLD, COLOR_PRIMARY);
            fontSection = new Font(arabicBaseFont, 16, Font.BOLD, COLOR_PRIMARY);
            fontSubSection = new Font(arabicBaseFont, 14, Font.BOLD, COLOR_SECONDARY);
            fontBold = new Font(arabicBaseFont, 11, Font.BOLD, COLOR_TEXT_DARK);
            fontNormal = new Font(arabicBaseFont, 11, Font.NORMAL, COLOR_TEXT_DARK);
            fontNumber = new Font(arabicBaseFont, 11, Font.BOLD, COLOR_PRIMARY);
        }
    }

    // =================================================================================
    // 2. بناء صفحة الغلاف (Cover Page)
    // =================================================================================
    private static void createCoverPage(Document document, Client client, ReportStyles styles) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addEmptyRows(table, 6);

        table.addCell(createCell("عيادة التغذية العلاجية - Food Assistant System", styles.fontSubSection, Element.ALIGN_CENTER, Rectangle.NO_BORDER, null));
        table.addCell(createCell("التقرير الطبي والقياسات الحيوية", styles.fontCoverTitle, Element.ALIGN_CENTER, Rectangle.NO_BORDER, null));

        addEmptyRows(table, 8);

        PdfPTable clientBox = new PdfPTable(1);
        clientBox.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        String clientName = buildCleanFullName(client);
        clientBox.addCell(createCell("أُعد هذا التقرير خصيصاً للمراجع:", styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        clientBox.addCell(createCell(clientName, styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        clientBox.addCell(createCell("رقم الملف الطبي: " + client.getClientID(), styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        clientBox.addCell(createCell("تاريخ التقرير: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));

        PdfPCell boxCell = new PdfPCell(clientBox);
        boxCell.setBorderColor(styles.COLOR_PRIMARY);
        boxCell.setBorderWidthLeft(4f);
        boxCell.setBorderWidthTop(0);
        boxCell.setBorderWidthRight(0);
        boxCell.setBorderWidthBottom(0);
        boxCell.setPaddingLeft(15f);
        boxCell.setBackgroundColor(styles.COLOR_BG_LIGHT);
        boxCell.setPadding(15f);

        table.addCell(boxCell);
        document.add(table);
    }

    // =================================================================================
    // 3. صفحة البيانات الشخصية (Client Profile)
    // =================================================================================
    private static void createClientProfilePage(Document document, Client client, Session lastSession, ReportStyles styles) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        table.addCell(createCell("📋 أولاً: البيانات الشخصية والتعريفية", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        addEmptyRows(table, 1);

        PdfPTable infoGrid = new PdfPTable(4);
        infoGrid.setWidthPercentage(100);
        infoGrid.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        infoGrid.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});

        String clientName = buildCleanFullName(client);
        String gender = String.valueOf(client.getGender()).equalsIgnoreCase("M") ? "ذكر" : "أنثى";
        String dateStr = (lastSession.getUploadTime() != null) ? lastSession.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "-";

        infoGrid.addCell(createCell("الاسم الرباعي:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        infoGrid.addCell(createCell(clientName, styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        infoGrid.addCell(createCell("رقم الملف:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        infoGrid.addCell(createCell("#" + client.getClientID(), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        infoGrid.addCell(createCell("العمر:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        infoGrid.addCell(createCell(client.getAge() + " سنة", styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        infoGrid.addCell(createCell("الجنس:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        infoGrid.addCell(createCell(gender, styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));

        infoGrid.addCell(createCell("رقم التواصل:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        infoGrid.addCell(createCell((client.getContactNumber() != null ? client.getContactNumber() : "-"), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));
        infoGrid.addCell(createCell("تاريخ آخر جلسة:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        infoGrid.addCell(createCell(dateStr, styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        PdfPCell gridContainer = new PdfPCell(infoGrid);
        gridContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(gridContainer);

        document.add(table);
    }

    // =================================================================================
    // 4. صفحة القياسات الحيوية (Health Assessment Page)
    // =================================================================================
    private static void createHealthAssessmentPage(Document document, BodyData bodyData, Client client, ReportStyles styles) throws DocumentException {
        if (bodyData == null) return;

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        table.addCell(createCell("🔬 ثانياً: القياسات الحيوية للجسم", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        addEmptyRows(table, 1);

        double heightM = bodyData.getHeight().doubleValue() / 100.0;
        double bmi = (heightM > 0) ? bodyData.getWeight().doubleValue() / (heightM * heightM) : 0;
        double fat = bodyData.getBodyFatPercentage().doubleValue();
        double muscle = bodyData.getSmm().doubleValue();
        double bmr = bodyData.getBMR(client.getGender(), client.getAge()).doubleValue();
        double tdee = bodyData.getTDEE(client.getGender(), client.getAge()).doubleValue();
        double water = (String.valueOf(client.getGender()).equalsIgnoreCase("M")) ? bodyData.getWeight().doubleValue() * 0.04 : bodyData.getWeight().doubleValue() * 0.035;

        PdfPTable diagTable = new PdfPTable(2);
        diagTable.setWidthPercentage(100);
        diagTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        diagTable.setWidths(new float[]{1f, 1f});

        diagTable.addCell(createCell("المؤشر الحيوي", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        diagTable.addCell(createCell("القيمة", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_BG_LIGHT));

        diagTable.addCell(createCell("مؤشر كتلة الجسم (BMI)", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        diagTable.addCell(createCell(String.format(Locale.US, "%.1f", bmi), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        diagTable.addCell(createCell("نسبة الدهون (Body Fat)", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        diagTable.addCell(createCell(String.format(Locale.US, "%.1f %%", fat), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        diagTable.addCell(createCell("الكتلة العضلية (SMM)", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        diagTable.addCell(createCell(String.format(Locale.US, "%.1f كج", muscle), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        diagTable.addCell(createCell("الاحتياج المائي المقدر", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        diagTable.addCell(createCell(String.format(Locale.US, "%.1f لتر", water), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        diagTable.addCell(createCell("معدل الحرق (BMR/TDEE)", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        diagTable.addCell(createCell(String.format(Locale.US, "%.0f / %.0f", bmr, tdee), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        PdfPCell diagContainer = new PdfPCell(diagTable);
        diagContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(diagContainer);

        document.add(table);
    }

    // =================================================================================
    // 5. صفحة تحليل التطور الزمني (Progress Analysis Page)
    // =================================================================================
    private static void createProgressAnalysisPage(Document document, List<Session> sessions, ReportStyles styles) throws DocumentException {
        if (sessions == null || sessions.size() < 1) return;

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        table.addCell(createCell("📈 ثالثاً: سجل المتابعة الزمني", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        addEmptyRows(table, 1);

        PdfPTable historyTable = new PdfPTable(4);
        historyTable.setWidthPercentage(100);
        historyTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        historyTable.setWidths(new float[]{1.5f, 1.2f, 1.2f, 1.2f});

        historyTable.addCell(createCell("تاريخ الزيارة", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_PRIMARY));
        historyTable.addCell(createCell("الوزن (كج)", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_PRIMARY));
        historyTable.addCell(createCell("الدهون (%)", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_PRIMARY));
        historyTable.addCell(createCell("العضلات (كج)", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_PRIMARY));

        for (PdfPCell cell : historyTable.getRow(0).getCells()) {
            cell.setPhrase(new Phrase(cell.getPhrase().getContent(), new Font(styles.fontBold.getBaseFont(), 11, Font.BOLD, BaseColor.WHITE)));
        }

        boolean alternate = false;
        for (Session s : sessions) {
            BaseColor rowColor = alternate ? styles.COLOR_BG_LIGHT : BaseColor.WHITE;
            BodyData bd = s.getBodyData();
            String dateStr = (s.getUploadTime() != null) ? s.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "-";

            historyTable.addCell(createCell(dateStr, styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));

            if (bd != null) {
                historyTable.addCell(createCell(String.format(Locale.US, "%.1f", bd.getWeight()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));
                historyTable.addCell(createCell(String.format(Locale.US, "%.1f", bd.getBodyFatPercentage()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));
                historyTable.addCell(createCell(String.format(Locale.US, "%.1f", bd.getSmm()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));
            } else {
                historyTable.addCell(createCell("-", styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));
                historyTable.addCell(createCell("-", styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));
                historyTable.addCell(createCell("-", styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, rowColor));
            }
            alternate = !alternate;
        }

        PdfPCell histContainer = new PdfPCell(historyTable);
        histContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(histContainer);

        document.add(table);
    }

    // =================================================================================
    // 6. صفحة الخطة الغذائية (Nutrition Plan)
    // =================================================================================
    private static void createNutritionPlanPage(Document document, NutritionPlan plan, ReportStyles styles , BigDecimal weight, char gendar) throws DocumentException, IOException {
        if (plan == null) return;

        document.newPage();

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        table.addCell(createCell("🍏 رابعاً: الخطة الغذائية", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        addEmptyRows(table, 1);

        table.addCell(createCell("توزيع المغذيات الكبرى (Macros):", styles.fontSubSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));

        PdfPTable macroGrid = new PdfPTable(4);
        macroGrid.setWidthPercentage(100);
        macroGrid.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        macroGrid.addCell(createCell("السعرات المستهدفة", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        macroGrid.addCell(createCell("البروتين (Protein)", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        macroGrid.addCell(createCell("الكربوهيدرات (Carbs)", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        macroGrid.addCell(createCell("الدهون الصحية (Fats)", styles.fontBold, Element.ALIGN_CENTER, Rectangle.BOX, styles.COLOR_BG_LIGHT));

        macroGrid.addCell(createCell(String.format(Locale.US, "%.0f kcal", plan.getTotalCalories()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));
        macroGrid.addCell(createCell(String.format(Locale.US, "%.0f g", plan.getProteinAmount()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));
        macroGrid.addCell(createCell(String.format(Locale.US, "%.0f g", plan.getCarbohydratesAmount()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));
        macroGrid.addCell(createCell(String.format(Locale.US, "%.0f g", plan.getFatAmount()), styles.fontNumber, Element.ALIGN_CENTER, Rectangle.BOX, null));

        PdfPCell macroContainer = new PdfPCell(macroGrid);
        macroContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(macroContainer);

        if (plan.getNotes() != null && !plan.getNotes().trim().isEmpty()) {
            addEmptyRows(table, 2);
            table.addCell(createCell("📝 ملاحظات وتوجيهات الخطة:", styles.fontSubSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
            table.addCell(createCell(plan.getNotes(), styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        }
        addEmptyRows(table,2);


        table.addCell(
                createCell(
                        "📌 تفاصيل الخطة الغذائية",
                        styles.fontSubSection,
                        Element.ALIGN_RIGHT,
                        Rectangle.NO_BORDER,
                        null)
        );



        PdfPTable details = new PdfPTable(4);

        details.setWidthPercentage(100);
        details.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);



        details.addCell(createCell(
                "الهدف",
                styles.fontBold,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                styles.COLOR_BG_LIGHT));


        details.addCell(createCell(
                plan.getTargetGoal(),
                styles.fontNormal,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                null));



        details.addCell(createCell(
                "حالة الخطة",
                styles.fontBold,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                styles.COLOR_BG_LIGHT));


        details.addCell(createCell(
                plan.getPlanStatus(),
                styles.fontNormal,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                null));



        details.addCell(createCell(
                "مدة الخطة",
                styles.fontBold,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                styles.COLOR_BG_LIGHT));


        details.addCell(createCell(
                plan.getPlanDuration()+" يوم",
                styles.fontNumber,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                null));


        details.addCell(createCell(
                "عدد الوجبات",
                styles.fontBold,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                styles.COLOR_BG_LIGHT));


        details.addCell(createCell(
                String.valueOf(plan.getMealsCount()),
                styles.fontNumber,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                null));



        details.addCell(createCell(
                "الماء اليومي",
                styles.fontBold,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                styles.COLOR_BG_LIGHT));


        details.addCell(createCell(
                plan.getWaterIntake()==null?
                        String.format("%.1f", plan.calculateIdealWater(weight, String.valueOf(gendar))) :                        plan.getWaterIntake(),
                styles.fontNormal,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                null));


        details.addCell(createCell(
                "توزيع الوجبات",
                styles.fontBold,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                styles.COLOR_BG_LIGHT));


        details.addCell(createCell(
                plan.getMealDistribution(),
                styles.fontNormal,
                Element.ALIGN_CENTER,
                Rectangle.BOX,
                null));



        PdfPCell detailContainer=new PdfPCell(details);
        detailContainer.setBorder(Rectangle.NO_BORDER);

        table.addCell(detailContainer);

        if(plan.getSelectedFoods()!=null)
        {

            table.addCell(
                    createCell(
                            "🍽 الأغذية المعتمدة في الخطة",
                            styles.fontSubSection,
                            Element.ALIGN_RIGHT,
                            Rectangle.NO_BORDER,
                            null)
            );



            // 1. تعريف الخط (نستخدم نفس الخط المضمّن في classpath، متوافق مع جميع أنظمة التشغيل)
            java.net.URL bfUrl = PDFGenerator.class.getClassLoader().getResource("fonts/arial.ttf");
            if (bfUrl == null) {
                throw new IOException("fonts/arial.ttf not found on classpath (expected under src/main/resources/fonts/)");
            }
            BaseFont bf = BaseFont.createFont(bfUrl.toExternalForm(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 12);
            Font headerFont = new Font(bf, 12, Font.BOLD);

            PdfPTable foodTable = new PdfPTable(4);
            foodTable.setWidthPercentage(100);
            foodTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

// 2. ضبط عرض الأعمدة (اختياري: لتوزيع المساحة بشكل أفضل)
            float[] columnWidths = {2f, 1f, 1f, 1f};
            foodTable.setWidths(columnWidths);

// 3. إضافة الرؤوس مع تنسيق
            String[] headers = {"الغذاء", "الوجبة", "الكمية", "الوحدة"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER); // توسيط أفقي
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);   // توسيط عمودي
                cell.setPadding(8);                                // إضافة مساحة داخل الخلية
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);    // لون خلفية للرأس
                foodTable.addCell(cell);
            }

// 4. إضافة البيانات
            for (PlanFoodItem food : plan.getSelectedFoods()) {
                // دالة مساعدة لإنشاء خلية منسقة
                foodTable.addCell(createCell(food.getFoodItem().getFoodName(), font));
                foodTable.addCell(createCell(food.getMealType(), font));
                foodTable.addCell(createCell(String.valueOf(food.getQuantity()), font));
                foodTable.addCell(createCell(food.getUnit(), font));
            }

            PdfPCell foodContainer=new PdfPCell(foodTable);

            foodContainer.setBorder(Rectangle.NO_BORDER);

            table.addCell(foodContainer);


        }
        document.add(table);
    }
    private static PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }
    // =================================================================================
    // 7. صفحة تفاصيل وتوصيات الجلسة (Session Report Page) - [الجديدة]
    // =================================================================================
    private static void createSessionReportDetailsPage(Document document, SessionReport report, ReportStyles styles) throws DocumentException {
        if (report == null) return;

        document.newPage();

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        table.addCell(createCell("📑 خامساً: تفاصيل ومخرجات الجلسة", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        addEmptyRows(table, 1);

        // 1. الشبكة الثنائية للصناديق النصية
        PdfPTable gridTable = new PdfPTable(2);
        gridTable.setWidthPercentage(100);
        gridTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        gridTable.setWidths(new float[]{1f, 1f});

        // الصف الأول: التشخيص والتقييم
        gridTable.addCell(createTitledBox("🩺 التشخيص الطبي", report.getDiagnosis(), styles));
        gridTable.addCell(createTitledBox("🔬 التقييم الإكلينيكي", report.getAssessment(), styles));

        // الصف الثاني: النتائج والتوصيات
        gridTable.addCell(createTitledBox("📊 نتائج الجلسة", report.getSessionResults(), styles));
        gridTable.addCell(createTitledBox("💡 التوصيات والإرشادات", report.getRecommendations(), styles));

        // الصف الثالث: الأهداف والملاحظات
        gridTable.addCell(createTitledBox("🎯 الأهداف القادمة", report.getNextGoals(), styles));
        gridTable.addCell(createTitledBox("📝 ملاحظات إضافية", report.getNotes(), styles));

        PdfPCell gridContainer = new PdfPCell(gridTable);
        gridContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(gridContainer);
        addEmptyRows(table, 2);

        // 2. معلومات المتابعة
        PdfPTable followUpGrid = new PdfPTable(4);
        followUpGrid.setWidthPercentage(100);
        followUpGrid.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        followUpGrid.setWidths(new float[]{1.2f, 2f, 1.2f, 2f});

        String nextApp = (report.getNextAppointment() != null) ? report.getNextAppointment().toString() : "غير محدد";
        String commitment = (report.getCommitmentLevel() != null) ? report.getCommitmentLevel() : "غير محدد";

        followUpGrid.addCell(createCell("الموعد القادم:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        followUpGrid.addCell(createCell(nextApp, styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));
        followUpGrid.addCell(createCell("مستوى الالتزام:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        followUpGrid.addCell(createCell(commitment, styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));

        PdfPCell followContainer = new PdfPCell(followUpGrid);
        followContainer.setBorder(Rectangle.NO_BORDER);
        table.addCell(followContainer);

        document.add(table);
    }

    // =================================================================================
    // 8. صفحة الاعتماد (Final Summary & Signature)
    // =================================================================================
    private static void createFinalSummaryPage(Document document, SessionReport report, ReportStyles styles) throws DocumentException {
        document.newPage();

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        table.addCell(createCell("🏁 سادساً: الاعتماد والموافقة", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        addEmptyRows(table, 2);

        String nutritionistName = (report != null && report.getNutritionistName() != null && !report.getNutritionistName().isEmpty())
                ? report.getNutritionistName() : "__________________________";

        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        signatureTable.setWidths(new float[]{1f, 1f});

        signatureTable.addCell(createCell("توقيع أخصائي التغذية:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        signatureTable.addCell(createCell("توقيع العميل / المراجع:", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));

        addEmptyRows(signatureTable, 4);

        signatureTable.addCell(createCell(nutritionistName, styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
        signatureTable.addCell(createCell("__________________________", styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));

        PdfPCell sigContainer = new PdfPCell(signatureTable);
        sigContainer.setBorder(Rectangle.NO_BORDER);
        sigContainer.setPaddingTop(20f);
        table.addCell(sigContainer);

        addEmptyRows(table, 5);
        table.addCell(createCell("هذا التقرير هو وثيقة مخصصة للاستخدام المهني.", new Font(styles.fontNormal.getBaseFont(), 8, Font.ITALIC, styles.COLOR_TEXT_DARK), Element.ALIGN_CENTER, Rectangle.NO_BORDER, null));

        document.add(table);
    }

    // =================================================================================
    // 9. أدوات مساعدة للتنسيق والـ RTL
    // =================================================================================
    public static PdfPCell createCell(String text, Font font, int alignment, int borderType, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell.setPhrase(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(borderType);

        if (borderType == Rectangle.BOX) {
            cell.setBorderColor(ReportStyles.COLOR_BORDER);
            cell.setPadding(8f);
        } else {
            cell.setPaddingBottom(6f);
        }

        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        return cell;
    }

    private static void addEmptyRows(PdfPTable table, int count) {
        for (int i = 0; i < count; i++) {
            table.addCell(createCell(" ", new Font(), Element.ALIGN_CENTER, Rectangle.NO_BORDER, null));
        }
    }

    // دالة مساعدة لإنشاء صندوق أنيق للنصوص الطويلة (مثل التشخيص والتوصيات)
    private static PdfPCell createTitledBox(String title, String content, ReportStyles styles) {
        PdfPTable box = new PdfPTable(1);
        box.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        box.setWidthPercentage(100);

        box.addCell(createCell(title, styles.fontSubSection, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));

        String safeContent = (content == null || content.trim().isEmpty()) ? "لا يوجد" : content;
        PdfPCell contentCell = createCell(safeContent, styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null);
        contentCell.setPadding(10f);
        box.addCell(contentCell);

        PdfPCell containerCell = new PdfPCell(box);
        containerCell.setBorder(Rectangle.NO_BORDER);
        containerCell.setPadding(5f);
        return containerCell;
    }

    // =================================================================================
    // 10. تذييل الصفحة الأوتوماتيكي (Page Footer)
    // =================================================================================
    private static class PageFooterEventHandler extends PdfPageEventHelper {
        private final Font footerFont;

        public PageFooterEventHandler(ReportStyles styles) {
            this.footerFont = new Font(styles.fontNormal.getBaseFont(), 9, Font.NORMAL, ReportStyles.COLOR_TEXT_DARK);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            if (writer.getPageNumber() == 1) return;

            PdfPTable footer = new PdfPTable(2);
            footer.setTotalWidth(document.right() - document.left());
            footer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            try {
                footer.addCell(createCell("نظام المساعد الغذائي - Food Assistant System", footerFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
                footer.addCell(createCell("صفحة " + writer.getPageNumber(), footerFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER, null));
                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
            } catch (Exception ignored) {}
        }
    }

    public static void createDetailedHealthAssessmentPage(
            Document document,
            BodyData bodyData,
            Client client,
            ReportStyles styles) throws DocumentException {

        if(bodyData == null) return;

        PdfPTable main = new PdfPTable(1);
        main.setWidthPercentage(100);
        main.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);


        main.addCell(createCell(
                "🔬 ثانياً: التحليل الكامل للقياسات الحيوية",
                styles.fontSection,
                Element.ALIGN_RIGHT,
                Rectangle.NO_BORDER,
                null
        ));


        addEmptyRows(main,1);


        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);


        table.setWidths(new float[]{
                1.5f,1.5f,1.5f,1.5f
        });


        addHealthCell(table,"الطول",bodyData.getHeight()+" cm",styles);
        addHealthCell(table,"الوزن",bodyData.getWeight()+" kg",styles);

        addHealthCell(table,
                "BMI",
                String.format("%.1f",bodyData.getBMI()),
                styles);

        addHealthCell(table,
                "الحالة",
                bodyData.getStatus(),
                styles);



        addHealthCell(table,
                "نسبة الدهون",
                bodyData.getBodyFatPercentage()+" %",
                styles);


        addHealthCell(table,
                "الكتلة العضلية",
                bodyData.getMuscleMass()+" kg",
                styles);



        addHealthCell(table,
                "SMM",
                bodyData.getSmm()+" kg",
                styles);


        addHealthCell(table,
                "النشاط",
                bodyData.getPhysicalActivity(),
                styles);



        addHealthCell(table,
                "BMR",
                String.format("%.0f kcal",
                        bodyData.getBMR(
                                client.getGender(),
                                client.getAge())),
                styles);


        addHealthCell(table,
                "TDEE",
                String.format("%.0f kcal",
                        bodyData.getTDEE(
                                client.getGender(),
                                client.getAge())),
                styles);



        PdfPCell container=new PdfPCell(table);
        container.setBorder(Rectangle.NO_BORDER);

        main.addCell(container);



        addEmptyRows(main,2);



        main.addCell(createCell(
                "📏 قياسات محيط الجسم",
                styles.fontSubSection,
                Element.ALIGN_RIGHT,
                Rectangle.NO_BORDER,
                null));


        PdfPTable circumference=new PdfPTable(4);
        circumference.setWidthPercentage(100);
        circumference.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);



        addHealthCell(circumference,
                "الذراع",
                bodyData.getArm_C()+" cm",
                styles);

        addHealthCell(circumference,
                "الصدر",
                bodyData.getChest_C()+" cm",
                styles);


        addHealthCell(circumference,
                "الخصر",
                bodyData.getWaist_C()+" cm",
                styles);


        addHealthCell(circumference,
                "البطن",
                bodyData.getAbdominal_C()+" cm",
                styles);


        addHealthCell(circumference,
                "الورك",
                bodyData.getHip_C()+" cm",
                styles);


        addHealthCell(circumference,
                "الفخذ",
                bodyData.getMidThigh_C()+" cm",
                styles);


        addHealthCell(circumference,
                "الساق",
                bodyData.getCalf_C()+" cm",
                styles);



        PdfPCell c=new PdfPCell(circumference);
        c.setBorder(Rectangle.NO_BORDER);

        main.addCell(c);


        document.add(main);
    }
    private static void addHealthCell(
            PdfPTable table,
            String title,
            String value,
            ReportStyles styles
    ){

        table.addCell(
                createCell(
                        title,
                        styles.fontBold,
                        Element.ALIGN_RIGHT,
                        Rectangle.BOX,
                        styles.COLOR_BG_LIGHT
                )
        );


        table.addCell(
                createCell(
                        value,
                        styles.fontNumber,
                        Element.ALIGN_CENTER,
                        Rectangle.BOX,
                        null
                )
        );
    }
    // =================================================================================
    // 11. دالة التجميع الرئيسية (تم التحديث لتستقبل SessionReport)
    // =================================================================================
    public static void createComprehensiveReport(Client client, List<Session> sessions, SessionReport sessionReport, String filePath) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        ReportStyles styles = new ReportStyles();
        writer.setPageEvent(new PageFooterEventHandler(styles));

        document.open();

        if (sessions == null || sessions.isEmpty()) {
            document.add(new Paragraph("لا توجد بيانات كافية لتوليد التقرير.", styles.fontNormal));
            document.close();
            return;
        }

        Session lastSession = sessions.get(sessions.size() - 1);

        // بناء الصفحات بالترتيب
        createCoverPage(document, client, styles);
        createClientProfilePage(document, client, lastSession, styles);
        createHealthAssessmentPage(document, lastSession.getBodyData(), client, styles);
        createDetailedHealthAssessmentPage(
                document,
                lastSession.getBodyData(),
                client,
                styles);
        createProgressAnalysisPage(document, sessions, styles);
        createNutritionPlanPage(document, lastSession.getNutritionPlan(), styles,lastSession.getBodyData().getWeight(),client.getGender());

        // طباعة صفحة التقرير الجديدة التي تم تمريرها
        createSessionReportDetailsPage(document, sessionReport, styles);

        createFinalSummaryPage(document, sessionReport, styles);

        document.close();
        System.out.println("✅ تم إنشاء التقرير الطبي بنجاح: " );


    }


    private static String buildCleanFullName(Client client) {
        if (client.getFullName() == null) return "غير معروف";

        StringBuilder sb = new StringBuilder();
        for (String part : client.getFullName().split(" ")) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part.trim());
            }
        }
        return sb.length() > 0 ? sb.toString() : "غير معروف";
    }

    // =================================================================================
    // إنشاء التقرير الإداري والمالي الملخّص لفترة زمنية محددة
    // =================================================================================
    public static void createAdminFinancialReport(AdminFinancialSummary summary, String filePath) throws Exception {

        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        ReportStyles styles = new ReportStyles();

        // العنوان
        PdfPTable titleBox = new PdfPTable(1);
        titleBox.setWidthPercentage(100);
        titleBox.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        titleBox.addCell(createCell("التقرير الإداري والمالي", styles.fontCoverTitle, Element.ALIGN_CENTER, Rectangle.NO_BORDER, null));

        String periodText = "الفترة من " + summary.getStartDate() + " إلى " + summary.getEndDate();
        titleBox.addCell(createCell(periodText, styles.fontNormal, Element.ALIGN_CENTER, Rectangle.NO_BORDER, null));

        document.add(titleBox);
        document.add(new Paragraph(" "));

        // جدول الأرقام الأساسية
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        summaryTable.addCell(createCell("إجمالي عدد الجلسات", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        summaryTable.addCell(createCell(String.valueOf(summary.getTotalSessions()), styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));

        summaryTable.addCell(createCell("إجمالي الدخل", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        summaryTable.addCell(createCell(String.format("%.2f", summary.getTotalRevenue()), styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));

        summaryTable.addCell(createCell("عملاء جدد في الفترة", styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
        summaryTable.addCell(createCell(String.valueOf(summary.getNewClients()), styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));

        document.add(summaryTable);
        document.add(new Paragraph(" "));

        // توزيع مستوى الالتزام
        Map<String, Integer> distribution = summary.getCommitmentDistribution();
        if (!distribution.isEmpty()) {
            PdfPTable commitmentHeader = new PdfPTable(1);
            commitmentHeader.setWidthPercentage(100);
            commitmentHeader.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            commitmentHeader.addCell(createCell("توزيع مستوى الالتزام", styles.fontSection, Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null));
            document.add(commitmentHeader);

            PdfPTable commitmentTable = new PdfPTable(2);
            commitmentTable.setWidthPercentage(100);
            commitmentTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                commitmentTable.addCell(createCell(entry.getKey(), styles.fontBold, Element.ALIGN_RIGHT, Rectangle.BOX, styles.COLOR_BG_LIGHT));
                commitmentTable.addCell(createCell(entry.getValue() + " جلسة", styles.fontNormal, Element.ALIGN_RIGHT, Rectangle.BOX, null));
            }

            document.add(commitmentTable);
        }

        document.close();
        System.out.println("✅ تم إنشاء التقرير الإداري والمالي بنجاح: " + filePath);
    }
}