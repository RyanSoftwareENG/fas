package pdfmanagement;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * مولد التقرير الشامل لـ FAS.
 *
 * التصميم الحالي يركز على أن يكون ملف الـ PDF مرجع متابعة مشتركاً بين العميل،
 * الممرض، وأخصائي التغذية، ولذلك يشمل:
 * - بيانات العميل الأساسية.
 * - التاريخ الصحي ونمط الحياة عند توفرهما.
 * - بيانات الزيارة الحالية وبيانات الجلسة.
 * - التشخيص والتقييم ونتائج الجلسة والتوصيات والأهداف التالية.
 * - القياسات الجسدية والمؤشرات المحسوبة.
 * - الفحوصات ومرفقاتها وملاحظاتها عند توفرها.
 * - سجل التطور عبر جميع الجلسات.
 * - الخطة الغذائية الكاملة والماكروز والمياه وتوزيع الوجبات.
 * - جميع الأطعمة المختارة مع الوجبة والكمية والوحدة.
 * - ملخص مرجعي للزيارة القادمة ومكان التوقيع.
 *
 * المولد يعتمد على Reflection لقراءة الحقول الاختيارية، لذلك يبقى متوافقاً
 * مع اختلافات الإصدارات الحالية في Entities ولا يحتاج إلى ربط مباشر بـ SessionReport.
 */
public final class PDFGenerator {

    private PDFGenerator() {
        // Utility class.
    }

    // =====================================================================
    // الهوية البصرية - ألوان هادئة ومناسبة لتقرير تغذية/صحة
    // =====================================================================

    private static final BaseColor PRIMARY = new BaseColor(47, 111, 106);          // #2F6F6A
    private static final BaseColor PRIMARY_DARK = new BaseColor(36, 79, 75);      // #244F4B
    private static final BaseColor ACCENT = new BaseColor(217, 139, 95);          // #D98B5F
    private static final BaseColor TEXT = new BaseColor(38, 50, 56);              // #263238
    private static final BaseColor MUTED = new BaseColor(102, 116, 124);          // #66747C
    private static final BaseColor BORDER = new BaseColor(220, 231, 228);         // #DCE7E4
    private static final BaseColor PAGE_BG = new BaseColor(247, 250, 249);         // #F7FAF9
    private static final BaseColor SOFT_GREEN = new BaseColor(236, 247, 241);      // #ECF7F1
    private static final BaseColor SOFT_BLUE = new BaseColor(234, 243, 246);       // #EAF3F6
    private static final BaseColor SOFT_ORANGE = new BaseColor(251, 241, 234);     // #FBF1EA
    private static final BaseColor SOFT_GRAY = new BaseColor(244, 247, 247);       // #F4F7F7
    private static final BaseColor WHITE = BaseColor.WHITE;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm", Locale.US);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    // =====================================================================
    // Entry Points
    // =====================================================================

    /**
     * توافق مع الاستدعاء القديم.
     */
    public static void createComprehensiveReport(
            Object client,
            List<?> sessions,
            String filePath
    ) throws Exception {
        createComprehensiveReport(client, sessions, null, filePath);
    }

    /**
     * الاستدعاء الرئيسي.
     * sessionReport يمكن أن يكون SessionReport Entity أو DTO أو أي نسخة مشابهة.
     */
    public static void createComprehensiveReport(
            Object client,
            List<?> sessions,
            Object sessionReport,
            String filePath
    ) throws Exception {

        if (client == null) {
            throw new IllegalArgumentException("بيانات العميل غير موجودة.");
        }

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("مسار ملف الـ PDF غير صالح.");
        }

        List<Object> reportSessions = normalizeSessions(client, sessions);
        if (reportSessions.isEmpty()) {
            throw new IllegalArgumentException("لا توجد جلسات مسجلة لإنشاء التقرير.");
        }

        // ترتيب زمني من الأقدم إلى الأحدث، دون تعديل القائمة الأصلية.
        reportSessions.sort(Comparator.comparing(
                PDFGenerator::sessionDateTime,
                Comparator.nullsFirst(Comparator.naturalOrder())
        ));

        Object firstSession = reportSessions.get(0);
        Object currentSession = reportSessions.get(reportSessions.size() - 1);

        if (sessionReport == null) {
            sessionReport = invokeAny(currentSession,
                    "getSessionReport", "getReport", "getSessionReportData");
        }

        Object currentBody = invokeAny(currentSession, "getBodyData", "getBody_data", "getBody");
        Object currentPlan = invokeAny(currentSession, "getNutritionPlan", "getNutritionplan", "getPlan");

        File output = new File(filePath);
        File parent = output.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("تعذر إنشاء مجلد حفظ التقرير: " + parent);
        }

        BaseFont regularBaseFont = resolveArabicFont(false);
        BaseFont boldBaseFont = resolveArabicFont(true);

        Font coverTitle = font(boldBaseFont, 26, Font.BOLD, WHITE);
        Font coverSubtitle = font(regularBaseFont, 12, Font.NORMAL, new BaseColor(232, 242, 239));
        Font h1 = font(boldBaseFont, 16, Font.BOLD, PRIMARY_DARK);
        Font h2 = font(boldBaseFont, 12, Font.BOLD, PRIMARY);
        Font h3 = font(boldBaseFont, 11, Font.BOLD, TEXT);
        Font normal = font(regularBaseFont, 10.2f, Font.NORMAL, TEXT);
        Font normalSmall = font(regularBaseFont, 9.2f, Font.NORMAL, TEXT);
        Font muted = font(regularBaseFont, 8.8f, Font.NORMAL, MUTED);
        Font bold = font(boldBaseFont, 10.2f, Font.BOLD, TEXT);
        Font tiny = font(regularBaseFont, 8.2f, Font.NORMAL, MUTED);
        Font tableHeader = font(boldBaseFont, 9.2f, Font.BOLD, WHITE);
        Font tableHeaderDark = font(boldBaseFont, 9.0f, Font.BOLD, PRIMARY_DARK);

        Document document = new Document(PageSize.A4, 34, 34, 54, 48);
        FileOutputStream outputStream = new FileOutputStream(output);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setCompressionLevel(9);
            writer.setPageEvent(new ReportPageEvent(regularBaseFont, boldBaseFont));
            document.open();

            // =============================================================
            // PAGE 1 - الغلاف
            // =============================================================
            addCoverPage(document, client, reportSessions, currentSession,
                    coverTitle, coverSubtitle, h2, normal, muted, regularBaseFont, boldBaseFont);
            document.newPage();

            // =============================================================
            // PAGE 2 - ملف العميل والصحة ونمط الحياة
            // =============================================================
            addClientProfilePage(document, client, reportSessions,
                    h1, h2, h3, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);
            document.newPage();

            // =============================================================
            // PAGE 3 - ملخص الزيارة الحالية + التقرير السريري
            // =============================================================
            addCurrentVisitPage(document, client, currentSession, sessionReport,
                    h1, h2, h3, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);
            document.newPage();

            // =============================================================
            // PAGE 4 - القياسات الحالية والمؤشرات
            // =============================================================
            addCurrentMeasurementsPage(document, client, currentSession, currentBody,
                    h1, h2, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);
            document.newPage();

            // =============================================================
            // PAGE 5 - الفحوصات
            // =============================================================
            addExaminationsPage(document, currentSession,
                    h1, h2, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);
            document.newPage();

            // =============================================================
            // PAGE 6 - التطور والمقارنة التاريخية
            // =============================================================
            addProgressPage(document, reportSessions, currentSession,
                    h1, h2, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);
            document.newPage();

            // =============================================================
            // PAGE 7 - الخطة الغذائية
            // =============================================================
            addNutritionSummaryPage(document, client, currentPlan,
                    h1, h2, h3, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);

            // =============================================================
            // PAGE 8 وما بعدها - الوجبات والأطعمة
            // =============================================================
            if (currentPlan != null) {
                document.newPage();
                addMealsPage(document, currentPlan,
                        h1, h2, normal, normalSmall, muted, bold,
                        tableHeader, tableHeaderDark);
            }

            // =============================================================
            // الصفحة الختامية - خطة المتابعة والتوقيع
            // =============================================================
            document.newPage();
            addFollowUpPage(document, client, currentSession, sessionReport, currentBody, currentPlan,
                    h1, h2, h3, normal, normalSmall, muted, bold,
                    tableHeader, tableHeaderDark);

        } finally {
            try {
                if (document.isOpen()) {
                    document.close();
                }
            } finally {
                outputStream.close();
            }
        }

        System.out.println("تم إنشاء تقرير FAS الشامل بنجاح: " + output.getAbsolutePath());
    }

    // =====================================================================
    // الصفحة الأولى
    // =====================================================================

    private static void addCoverPage(
            Document document,
            Object client,
            List<Object> sessions,
            Object currentSession,
            Font coverTitle,
            Font coverSubtitle,
            Font sectionFont,
            Font normal,
            Font muted,
            BaseFont regularBaseFont,
            BaseFont boldBaseFont
    ) throws DocumentException {

        PdfPTable page = new PdfPTable(1);
        page.setWidthPercentage(100);
        page.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        PdfPCell top = new PdfPCell();
        top.setBorder(Rectangle.NO_BORDER);
        top.setBackgroundColor(PRIMARY_DARK);
        top.setPaddingTop(42);
        top.setPaddingBottom(42);
        top.setPaddingLeft(28);
        top.setPaddingRight(28);

        Paragraph brand = rtlParagraph("FAS", font(boldBaseFont, 18, Font.BOLD, WHITE), Element.ALIGN_CENTER);
        brand.setSpacingAfter(9);
        top.addElement(brand);

        Paragraph systemName = rtlParagraph("Food Assistant System", font(boldBaseFont, 14, Font.BOLD, new BaseColor(206, 231, 226)), Element.ALIGN_CENTER);
        systemName.setSpacingAfter(14);
        top.addElement(systemName);

        Paragraph title = rtlParagraph("ملف المتابعة التغذوية والصحية", coverTitle, Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        top.addElement(title);

        Paragraph subtitle = rtlParagraph(
                "مرجع متابعة متكامل للعميل والتمريض وأخصائي التغذية",
                coverSubtitle,
                Element.ALIGN_CENTER
        );
        top.addElement(subtitle);

        page.addCell(top);

        addSpacerCell(page, 18);

        String clientName = displayClientName(client);
        String clientId = stringValue(invokeAny(client, "getClientID", "getClientId", "getId"));
        String age = clientAge(client);
        String gender = genderArabic(invokeAny(client, "getGender", "getSex"));
        String phone = stringValue(invokeAny(client, "getContactNumber", "getPhone", "getMobile"));
        String firstDate = formatDateTime(sessionDateTime(sessions.get(0)));
        String lastDate = formatDateTime(sessionDateTime(currentSession));

        PdfPTable identity = cardTable(1);
        addKeyValue(identity, "اسم العميل", clientName, true, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "رقم الملف", emptyDash(clientId), false, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "العمر", emptyDash(age) + " سنة", true, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "الجنس", emptyDash(gender), false, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "الهاتف", emptyDash(phone), true, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "عدد الزيارات المسجلة", String.valueOf(sessions.size()), false, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "أول زيارة", firstDate, true, regularBaseFont, boldBaseFont);
        addKeyValue(identity, "آخر زيارة", lastDate, false, regularBaseFont, boldBaseFont);

        PdfPCell identityContainer = new PdfPCell(identity);
        identityContainer.setBorderColor(BORDER);
        identityContainer.setBackgroundColor(WHITE);
        identityContainer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        identityContainer.setPadding(12);
        page.addCell(identityContainer);

        addSpacerCell(page, 18);

        PdfPCell note = new PdfPCell();
        note.setBorderColor(BORDER);
        note.setBackgroundColor(SOFT_GREEN);
        note.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        note.setPadding(14);
        Paragraph noteTitle = rtlParagraph("ملاحظة الاستخدام", font(boldBaseFont, 10, Font.BOLD, PRIMARY_DARK), Element.ALIGN_RIGHT);
        noteTitle.setSpacingAfter(5);
        note.addElement(noteTitle);
        note.addElement(rtlParagraph(
                "هذا الملف مخصص للمتابعة والمرجعية أثناء المراجعات. القيم الطبية والتشخيصية الواردة فيه هي البيانات المسجلة في النظام بواسطة الفريق المختص، بينما المؤشرات المحسوبة توضح أنها محسوبة آلياً.",
                normal,
                Element.ALIGN_RIGHT
        ));
        page.addCell(note);

        addSpacerCell(page, 25);

        Paragraph generated = rtlParagraph(
                "تاريخ إصدار التقرير: " + DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                muted,
                Element.ALIGN_CENTER
        );
        generated.setSpacingAfter(4);
        page.addCell(noBorderCell(generated, 0));

        Paragraph reference = rtlParagraph(
                "مرجع التقرير: جلسة #" + stringValue(invokeAny(currentSession, "getId", "getSessionId")),
                muted,
                Element.ALIGN_CENTER
        );
        page.addCell(noBorderCell(reference, 0));

        document.add(page);
    }

    // =====================================================================
    // الصفحة الثانية
    // =====================================================================

    private static void addClientProfilePage(
            Document document,
            Object client,
            List<Object> sessions,
            Font h1,
            Font h2,
            Font h3,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "ملف العميل والصحة ونمط الحياة", "01 / Client Profile", h1, muted);

        PdfPTable profileGrid = new PdfPTable(2);
        profileGrid.setWidthPercentage(100);
        profileGrid.setWidths(new float[]{1f, 1f});
        profileGrid.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        profileGrid.setSpacingBefore(8);

        addInfoCard(profileGrid, "البيانات الأساسية", SOFT_BLUE, h2, normal, bold,
                "الاسم", displayClientName(client),
                "رقم العميل", stringValue(invokeAny(client, "getClientID", "getClientId", "getId")),
                "تاريخ الميلاد", formatDate(invokeAny(client, "getBirthDate", "getDateOfBirth")),
                "العمر", clientAge(client) + " سنة",
                "الجنس", genderArabic(invokeAny(client, "getGender", "getSex")));

        addInfoCard(profileGrid, "التواصل", SOFT_GREEN, h2, normal, bold,
                "الهاتف", stringValue(invokeAny(client, "getContactNumber", "getPhone", "getMobile")),
                "تاريخ التسجيل", formatDateTime(invokeAny(client, "getUploadDate", "getCreationDate", "getCreatedAt")),
                "آخر تعديل", formatDateTime(invokeAny(client, "getModificationDate", "getUpdateDate", "getUpdatedAt")),
                "عدد الجلسات", String.valueOf(sessions.size()));

        document.add(profileGrid);
        document.add(spacer(8));

        addSectionTitle(document, "التاريخ الصحي", "Health History", h2);
        PdfPTable health = cardTable(1);
        Object healthData = invokeAny(client, "getHealthData", "getHealth_data", "getHealth");
        addLongField(health, "التاريخ المرضي", stringValue(invokeAny(healthData, "getMedicalHistory", "getHistory")), normal);
        addLongField(health, "التاريخ العائلي", stringValue(invokeAny(healthData, "getFamilyMedicalHistory", "getFamilyHistory")), normal);
        addLongField(health, "الأدوية الحالية", stringValue(invokeAny(healthData, "getCurrentMedication", "getCurrentMedications", "getMedication")), normal);
        addLongField(health, "ملاحظات صحية", stringValue(invokeAny(healthData, "getNotes", "getHealthNotes")), normal);
        document.add(health);
        document.add(spacer(8));

        addSectionTitle(document, "الأمراض المزمنة والحساسيات", "Chronic Diseases & Allergies", h2);
        PdfPTable conditions = new PdfPTable(2);
        conditions.setWidthPercentage(100);
        conditions.setWidths(new float[]{1f, 1f});
        conditions.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Object chronic = invokeAny(client,
                "getChronicDiseases", "getChronicDisease", "getDiseases",
                "getClientChronicDiseases", "getPatientDiseases");
        Object allergies = invokeAny(client,
                "getAllergies", "getAllergicFoods", "getAllergy",
                "getClientAllergies", "getFoodAllergies");

        addRelationCard(conditions, "الأمراض المزمنة", chronic, SOFT_ORANGE, normalSmall, bold);
        addRelationCard(conditions, "الحساسيات والأطعمة المسببة", allergies, SOFT_GREEN, normalSmall, bold);
        document.add(conditions);
        document.add(spacer(8));

        addSectionTitle(document, "نمط الحياة", "Lifestyle Information", h2);
        PdfPTable lifestyleGrid = new PdfPTable(2);
        lifestyleGrid.setWidthPercentage(100);
        lifestyleGrid.setWidths(new float[]{1f, 1f});
        lifestyleGrid.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Object lifestyle = invokeAny(client, "getLifeStyleInformation", "getLifestyleInformation", "getLifestyle");

        addInfoCard(lifestyleGrid, "العادات اليومية", SOFT_ORANGE, h3, normalSmall, bold,
                "الفطور", stringValue(invokeAny(lifestyle, "getBreakfast")),
                "الغداء", stringValue(invokeAny(lifestyle, "getLunch")),
                "العشاء", stringValue(invokeAny(lifestyle, "getDinner")),
                "الوجبات اليومية", stringValue(invokeAny(lifestyle, "getMealsPerDay", "getMeals_per_Day")),
                "الوجبات الخفيفة", stringValue(invokeAny(lifestyle, "getSnacks")));

        addInfoCard(lifestyleGrid, "السلوك والروتين", SOFT_GRAY, h3, normalSmall, bold,
                "المشروبات", stringValue(invokeAny(lifestyle, "getDrinks")),
                "العادات غير المرغوبة", stringValue(invokeAny(lifestyle, "getBadHabits", "getBad_Habits")),
                "الأطعمة غير المفضلة", stringValue(invokeAny(lifestyle, "getFoodDislike", "getFood_Dislike")),
                "ساعات النوم", stringValue(invokeAny(lifestyle, "getSleepHours", "getSleep_Hours")),
                "الميزانية", stringValue(invokeAny(lifestyle, "getBudget")));

        document.add(lifestyleGrid);

        document.add(spacer(8));
        addSectionTitle(document, "ملخص تاريخ الزيارات", "Visit Timeline", h2);

        PdfPTable timeline = new PdfPTable(4);
        timeline.setWidthPercentage(100);
        timeline.setWidths(new float[]{1.2f, 1.4f, 2.0f, 3.0f});
        timeline.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        timeline.setHeaderRows(1);

        addHeaderCell(timeline, "الجلسة", tableHeader, PRIMARY_DARK);
        addHeaderCell(timeline, "التاريخ", tableHeader, PRIMARY_DARK);
        addHeaderCell(timeline, "المدة", tableHeader, PRIMARY_DARK);
        addHeaderCell(timeline, "ملاحظات الجلسة", tableHeader, PRIMARY_DARK);

        for (int i = 0; i < sessions.size(); i++) {
            Object session = sessions.get(i);
            BaseColor row = (i % 2 == 0) ? WHITE : SOFT_GRAY;
            addDataCell(timeline, "#" + stringValue(invokeAny(session, "getId", "getSessionId")), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(timeline, formatDateTime(sessionDateTime(session)), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(timeline, formatTime(invokeAny(session, "getDuration")), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(timeline, safeText(stringValue(invokeAny(session, "getNotes", "getSessionNotes"))), normalSmall, row, Element.ALIGN_RIGHT);
        }

        document.add(timeline);
    }

    // =====================================================================
    // الصفحة الثالثة - الزيارة والتقرير السريري
    // =====================================================================

    private static void addCurrentVisitPage(
            Document document,
            Object client,
            Object session,
            Object report,
            Font h1,
            Font h2,
            Font h3,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "الزيارة الحالية والتقييم السريري", "02 / Current Visit", h1, muted);

        PdfPTable visitGrid = new PdfPTable(4);
        visitGrid.setWidthPercentage(100);
        visitGrid.setWidths(new float[]{1f, 1f, 1f, 1f});
        visitGrid.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addMetricCard(visitGrid, "رقم الجلسة", "#" + stringValue(invokeAny(session, "getId", "getSessionId")), SOFT_BLUE, h3, normal);
        addMetricCard(visitGrid, "تاريخ الزيارة", formatDateTime(sessionDateTime(session)), SOFT_GREEN, h3, normal);
        addMetricCard(visitGrid, "مدة الجلسة", formatTime(invokeAny(session, "getDuration")), SOFT_ORANGE, h3, normal);
        addMetricCard(visitGrid, "القيمة", numberText(invokeAny(session, "getPrice"), "", 2), SOFT_GRAY, h3, normal);

        document.add(visitGrid);
        document.add(spacer(8));

        Object body = invokeAny(session, "getBodyData", "getBody_data", "getBody");
        Object plan = invokeAny(session, "getNutritionPlan", "getNutritionplan", "getPlan");

        PdfPTable quick = new PdfPTable(2);
        quick.setWidthPercentage(100);
        quick.setWidths(new float[]{1f, 1f});
        quick.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        String goal = stringValue(invokeAny(plan, "getTargetGoal", "getGoal"));
        String status = stringValue(invokeAny(body, "getStatus", "getHealthStatus", "getBmiStatus"));
        addInfoCard(quick, "الهدف الحالي", SOFT_GREEN, h2, normalSmall, bold,
                "الهدف المسجل", emptyDash(goal),
                "حالة المؤشر", emptyDash(status),
                "النشاط البدني", emptyDash(stringValue(invokeAny(body, "getPhysicalActivity", "getActivity"))));

        addInfoCard(quick, "الخطة الحالية", SOFT_BLUE, h2, normalSmall, bold,
                "حالة الخطة", emptyDash(stringValue(invokeAny(plan, "getPlanStatus", "getStatus"))),
                "المدة", numberText(invokeAny(plan, "getPlanDuration", "getDuration"), " يوم", 0),
                "عدد الوجبات", numberText(invokeAny(plan, "getMealsCount"), "", 0));
        document.add(quick);
        document.add(spacer(8));

        addSectionTitle(document, "التشخيص والتقييم", "Clinical Assessment", h2);
        document.add(narrativeTable(
                "التشخيص",
                stringValue(invokeAny(report, "getDiagnosis")),
                SOFT_ORANGE,
                h3,
                normal
        ));
        document.add(spacer(6));
        document.add(narrativeTable(
                "التقييم",
                stringValue(invokeAny(report, "getAssessment")),
                SOFT_BLUE,
                h3,
                normal
        ));
        document.add(spacer(6));
        document.add(narrativeTable(
                "نتائج الجلسة",
                stringValue(invokeAny(report, "getSessionResults", "getResults")),
                SOFT_GREEN,
                h3,
                normal
        ));
        document.add(spacer(6));
        document.add(narrativeTable(
                "ملاحظات الجلسة",
                joinText(
                        stringValue(invokeAny(session, "getNotes", "getSessionNotes")),
                        stringValue(invokeAny(report, "getNotes"))
                ),
                SOFT_GRAY,
                h3,
                normal
        ));

        document.add(spacer(8));
        PdfPTable reportMeta = new PdfPTable(4);
        reportMeta.setWidthPercentage(100);
        reportMeta.setWidths(new float[]{1f, 1f, 1f, 1f});
        reportMeta.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addMetricCard(reportMeta, "الأخصائي", emptyDash(stringValue(invokeAny(report, "getNutritionistName", "getNutritionist"))), SOFT_GREEN, h3, normalSmall);
        addMetricCard(reportMeta, "مستوى الالتزام", emptyDash(stringValue(invokeAny(report, "getCommitmentLevel", "getCommitment"))), SOFT_BLUE, h3, normalSmall);
        addMetricCard(reportMeta, "الموعد القادم", formatDate(invokeAny(report, "getNextAppointment", "getNextVisitDate")), SOFT_ORANGE, h3, normalSmall);
        addMetricCard(reportMeta, "آخر تعديل للتقرير", formatDateTime(invokeAny(report, "getModifiedAt", "getUpdatedAt", "getModificationDate", "getCreatedAt", "getCreationDate")), SOFT_GRAY, h3, normalSmall);

        document.add(reportMeta);
    }

    // =====================================================================
    // الصفحة الرابعة - القياسات الحالية
    // =====================================================================

    private static void addCurrentMeasurementsPage(
            Document document,
            Object client,
            Object session,
            Object body,
            Font h1,
            Font h2,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "القياسات الجسدية والمؤشرات الحالية", "03 / Current Measurements", h1, muted);

        if (body == null) {
            document.add(narrativeTable("القياسات", "لا توجد بيانات BodyData مسجلة لهذه الجلسة.", SOFT_ORANGE, h2, normal));
            return;
        }

        char gender = genderChar(invokeAny(client, "getGender", "getSex"));
        int age = safeInt(invokeAny(client, "getAge"));

        double height = safeDouble(invokeAny(body, "getHeight"));
        double weight = safeDouble(invokeAny(body, "getWeight"));
        double fat = safeDouble(invokeAny(body, "getBodyFatPercentage", "getBodyFat"));
        double smm = safeDouble(invokeAny(body, "getSMM", "getSmm"));
        double muscle = safeDouble(invokeAny(body, "getMuscleMass"));
        double activityFactor = safeDouble(invokeAny(body, "getActivityFactor"));
        double bmi = safeDouble(invokeAny(body, "getBMI", "getBmi"));
        double bmr = calculateBmr(body, gender, age);
        double tdee = calculateTdee(body, gender, age);
        double lbm = (weight > 0 && fat > 0) ? weight * (1.0 - fat / 100.0) : 0;

        PdfPTable metrics = new PdfPTable(4);
        metrics.setWidthPercentage(100);
        metrics.setWidths(new float[]{1f, 1f, 1f, 1f});
        metrics.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addMetricCard(metrics, "الطول", numberText(height, " سم", 1), SOFT_BLUE, h2, normal);
        addMetricCard(metrics, "الوزن", numberText(weight, " كغ", 1), SOFT_GREEN, h2, normal);
        addMetricCard(metrics, "BMI", numberText(bmi, "", 2), SOFT_ORANGE, h2, normal);
        addMetricCard(metrics, "حالة BMI", emptyDash(stringValue(invokeAny(body, "getStatus", "getHealthStatus"))), SOFT_GRAY, h2, normal);

        addMetricCard(metrics, "نسبة الدهون", numberText(fat, " %", 1), SOFT_ORANGE, h2, normal);
        addMetricCard(metrics, "SMM", numberText(smm, " كغ", 1), SOFT_GREEN, h2, normal);
        addMetricCard(metrics, "كتلة العضلات", numberText(muscle, " كغ", 1), SOFT_BLUE, h2, normal);
        addMetricCard(metrics, "LBM محسوبة", numberText(lbm, " كغ", 1), SOFT_GRAY, h2, normal);

        addMetricCard(metrics, "BMR محسوب", numberText(bmr, " سعرة", 0), SOFT_BLUE, h2, normal);
        addMetricCard(metrics, "TDEE محسوب", numberText(tdee, " سعرة", 0), SOFT_GREEN, h2, normal);
        addMetricCard(metrics, "عامل النشاط", numberText(activityFactor, "", 2), SOFT_ORANGE, h2, normal);
        addMetricCard(metrics, "النشاط البدني", emptyDash(stringValue(invokeAny(body, "getPhysicalActivity", "getActivity"))), SOFT_GRAY, h2, normalSmall);

        document.add(metrics);
        document.add(spacer(10));

        addSectionTitle(document, "محيطات الجسم", "Body Circumferences", h2);
        PdfPTable circumference = new PdfPTable(4);
        circumference.setWidthPercentage(100);
        circumference.setWidths(new float[]{1f, 1f, 1f, 1f});
        circumference.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        circumference.setHeaderRows(1);

        addHeaderCell(circumference, "المؤشر", tableHeader, PRIMARY_DARK);
        addHeaderCell(circumference, "القيمة", tableHeader, PRIMARY_DARK);
        addHeaderCell(circumference, "المؤشر", tableHeader, PRIMARY_DARK);
        addHeaderCell(circumference, "القيمة", tableHeader, PRIMARY_DARK);

        Object[][] circumferenceRows = new Object[][]{
                {"الذراع", invokeAny(body, "getArm_C", "getArmC"), "الصدر", invokeAny(body, "getChest_C", "getChestC")},
                {"الخصر", invokeAny(body, "getWaist_C", "getWaistC"), "البطن", invokeAny(body, "getAbdominal_C", "getAbdominalC")},
                {"الحوض", invokeAny(body, "getHip_C", "getHipC"), "منتصف الفخذ", invokeAny(body, "getMidThigh_C", "getMidThighC")},
                {"الساق", invokeAny(body, "getCalf_C", "getCalfC"), "تاريخ القياس", invokeAny(body, "getUploadDate", "getMeasurementDate")}
        };

        for (int i = 0; i < circumferenceRows.length; i++) {
            BaseColor row = i % 2 == 0 ? WHITE : SOFT_GRAY;
            addDataCell(circumference, stringValue(circumferenceRows[i][0]), normalSmall, row, Element.ALIGN_RIGHT);
            addDataCell(circumference, numberText(circumferenceRows[i][1], " سم", 1), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(circumference, stringValue(circumferenceRows[i][2]), normalSmall, row, Element.ALIGN_RIGHT);
            addDataCell(circumference, i == 3
                            ? formatDateTime(circumferenceRows[i][3])
                            : numberText(circumferenceRows[i][3], " سم", 1),
                    normalSmall, row, Element.ALIGN_CENTER);
        }

        document.add(circumference);
        document.add(spacer(8));

        addSectionTitle(document, "البيانات الحسابية والملاحظات", "Calculated Indicators", h2);
        PdfPTable calculated = cardTable(1);
        addLongField(calculated, "BMR / معدل الأيض الأساسي", numberText(bmr, " سعرة يومياً - محسوب آلياً حسب بيانات العميل والقياس.", 0), normalSmall);
        addLongField(calculated, "TDEE / الاستهلاك اليومي الكلي", numberText(tdee, " سعرة يومياً - محسوب آلياً باستخدام عامل النشاط.", 0), normalSmall);
        addLongField(calculated, "LBM / الكتلة الخالية من الدهون", numberText(lbm, " كغ - محسوبة من الوزن ونسبة الدهون عند توفر القيم اللازمة.", 1), normalSmall);
        addLongField(calculated, "مصدر القياس", "BodyData / الجلسة الحالية - #" + stringValue(invokeAny(session, "getId", "getSessionId")), normalSmall);
        document.add(calculated);
    }

    // =====================================================================
    // الصفحة الخامسة - الفحوصات
    // =====================================================================

    private static void addExaminationsPage(
            Document document,
            Object session,
            Font h1,
            Font h2,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "الفحوصات والمرفقات", "04 / Examinations", h1, muted);

        Object examsObject = invokeAny(session, "getExaminations", "getExams", "getExaminationList");
        List<Object> exams = toObjectList(examsObject);

        if (exams.isEmpty()) {
            document.add(narrativeTable(
                    "الفحوصات",
                    "لا توجد فحوصات مسجلة لهذه الجلسة.",
                    SOFT_GRAY,
                    h2,
                    normal
            ));
            return;
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.7f, 2.3f, 1.55f, 1.55f, 3.3f});
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        table.setHeaderRows(1);
        table.setSplitLate(false);

        addHeaderCell(table, "اسم الفحص", tableHeader, PRIMARY_DARK);
        addHeaderCell(table, "المرفق", tableHeader, PRIMARY_DARK);
        addHeaderCell(table, "تاريخ الرفع", tableHeader, PRIMARY_DARK);
        addHeaderCell(table, "آخر تعديل", tableHeader, PRIMARY_DARK);
        addHeaderCell(table, "الملاحظات", tableHeader, PRIMARY_DARK);

        for (int i = 0; i < exams.size(); i++) {
            Object exam = exams.get(i);
            BaseColor row = i % 2 == 0 ? WHITE : SOFT_GRAY;

            String name = stringValue(invokeAny(exam, "getExaminationName", "getName", "getExamName"));
            String attachment = stringValue(invokeAny(exam, "getExaminationImage", "getImage", "getFile", "getAttachment"));
            String upload = formatDateTime(invokeAny(exam, "getUploadDate", "getCreatedAt"));
            String modified = formatDateTime(invokeAny(exam, "getModificationDate", "getUpdatedAt"));
            String notes = stringValue(invokeAny(exam, "getNotes", "getNote"));

            addDataCell(table, emptyDash(name), normalSmall, row, Element.ALIGN_RIGHT);
            addDataCell(table, emptyDash(attachment), tinyFont(tableHeaderDark), row, Element.ALIGN_RIGHT);
            addDataCell(table, upload, normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(table, modified, normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(table, emptyDash(notes), normalSmall, row, Element.ALIGN_RIGHT);
        }

        document.add(table);
        document.add(spacer(8));

        PdfPTable note = cardTable(1);
        addLongField(note, "ملاحظة", "تُعرض هنا البيانات المسجلة للفحوصات كما هي في النظام. لا يتم إنشاء نتائج مخبرية جديدة أو تفسير غير مسجل في التقرير.", normalSmall);
        document.add(note);
    }

    // =====================================================================
    // الصفحة السادسة - التاريخ والتطور
    // =====================================================================

    private static void addProgressPage(
            Document document,
            List<Object> sessions,
            Object currentSession,
            Font h1,
            Font h2,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "سجل التطور والمقارنة التاريخية", "05 / Progress & Comparison", h1, muted);

        PdfPTable history = new PdfPTable(7);
        history.setWidthPercentage(100);
        history.setWidths(new float[]{0.8f, 1.5f, 1.1f, 1.1f, 1.1f, 1.1f, 1.25f});
        history.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        history.setHeaderRows(1);
        history.setSplitLate(false);

        addHeaderCell(history, "الجلسة", tableHeader, PRIMARY_DARK);
        addHeaderCell(history, "التاريخ", tableHeader, PRIMARY_DARK);
        addHeaderCell(history, "الوزن", tableHeader, PRIMARY_DARK);
        addHeaderCell(history, "BMI", tableHeader, PRIMARY_DARK);
        addHeaderCell(history, "الدهون %", tableHeader, PRIMARY_DARK);
        addHeaderCell(history, "SMM", tableHeader, PRIMARY_DARK);
        addHeaderCell(history, "العضلات", tableHeader, PRIMARY_DARK);

        for (int i = 0; i < sessions.size(); i++) {
            Object session = sessions.get(i);
            Object body = invokeAny(session, "getBodyData", "getBody_data", "getBody");
            BaseColor row = session == currentSession ? SOFT_GREEN : (i % 2 == 0 ? WHITE : SOFT_GRAY);

            addDataCell(history, "#" + stringValue(invokeAny(session, "getId", "getSessionId")), bold, row, Element.ALIGN_CENTER);
            addDataCell(history, formatDateTime(sessionDateTime(session)), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(history, numberText(invokeAny(body, "getWeight"), " كغ", 1), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(history, numberText(invokeAny(body, "getBMI", "getBmi"), "", 2), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(history, numberText(invokeAny(body, "getBodyFatPercentage", "getBodyFat"), " %", 1), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(history, numberText(invokeAny(body, "getSMM", "getSmm"), " كغ", 1), normalSmall, row, Element.ALIGN_CENTER);
            addDataCell(history, numberText(invokeAny(body, "getMuscleMass"), " كغ", 1), normalSmall, row, Element.ALIGN_CENTER);
        }

        document.add(history);
        document.add(spacer(10));

        Object firstBody = invokeAny(sessions.get(0), "getBodyData", "getBody_data", "getBody");
        Object currentBody = invokeAny(currentSession, "getBodyData", "getBody_data", "getBody");
        Object previousSession = sessions.size() >= 2 ? sessions.get(sessions.size() - 2) : null;
        Object previousBody = previousSession == null ? null : invokeAny(previousSession, "getBodyData", "getBody_data", "getBody");

        addSectionTitle(document, "الفرق بين الجلسة الحالية والسابقة", "Current vs Previous", h2);
        PdfPTable compare = new PdfPTable(4);
        compare.setWidthPercentage(100);
        compare.setWidths(new float[]{2.2f, 1.3f, 1.3f, 2.2f});
        compare.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        compare.setHeaderRows(1);

        addHeaderCell(compare, "المؤشر", tableHeader, PRIMARY_DARK);
        addHeaderCell(compare, "الحالية", tableHeader, PRIMARY_DARK);
        addHeaderCell(compare, "السابقة", tableHeader, PRIMARY_DARK);
        addHeaderCell(compare, "الفرق", tableHeader, PRIMARY_DARK);

        addComparisonRow(compare, "الوزن (كغ)", currentBody, previousBody, "getWeight", " كغ", normalSmall, bold);
        addComparisonRow(compare, "BMI", currentBody, previousBody, "getBMI", "", normalSmall, bold);
        addComparisonRow(compare, "نسبة الدهون (%)", currentBody, previousBody, "getBodyFatPercentage", " %", normalSmall, bold);
        addComparisonRow(compare, "SMM (كغ)", currentBody, previousBody, "getSMM", " كغ", normalSmall, bold);
        addComparisonRow(compare, "كتلة العضلات (كغ)", currentBody, previousBody, "getMuscleMass", " كغ", normalSmall, bold);
        document.add(compare);

        document.add(spacer(10));
        addSectionTitle(document, "الفرق من أول زيارة إلى الزيارة الحالية", "First vs Current", h2);
        PdfPTable total = new PdfPTable(4);
        total.setWidthPercentage(100);
        total.setWidths(new float[]{2.2f, 1.3f, 1.3f, 2.2f});
        total.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        total.setHeaderRows(1);

        addHeaderCell(total, "المؤشر", tableHeader, PRIMARY_DARK);
        addHeaderCell(total, "الحالية", tableHeader, PRIMARY_DARK);
        addHeaderCell(total, "الأولى", tableHeader, PRIMARY_DARK);
        addHeaderCell(total, "الفرق المسجل", tableHeader, PRIMARY_DARK);

        addComparisonRow(total, "الوزن (كغ)", currentBody, firstBody, "getWeight", " كغ", normalSmall, bold);
        addComparisonRow(total, "BMI", currentBody, firstBody, "getBMI", "", normalSmall, bold);
        addComparisonRow(total, "نسبة الدهون (%)", currentBody, firstBody, "getBodyFatPercentage", " %", normalSmall, bold);
        addComparisonRow(total, "SMM (كغ)", currentBody, firstBody, "getSMM", " كغ", normalSmall, bold);
        addComparisonRow(total, "كتلة العضلات (كغ)", currentBody, firstBody, "getMuscleMass", " كغ", normalSmall, bold);
        document.add(total);

        document.add(spacer(7));
        document.add(narrativeTable(
                "طريقة قراءة المقارنة",
                "الفرق المسجل هو طرح القيمة المرجعية من القيمة الحالية. التقرير لا يفترض أن الزيادة أو النقصان تحسن أو تراجع بشكل مستقل عن الهدف السريري المسجل في الخطة والتقرير.",
                SOFT_GRAY,
                h2,
                normalSmall
        ));
    }

    // =====================================================================
    // الصفحة السابعة - ملخص الخطة الغذائية
    // =====================================================================

    private static void addNutritionSummaryPage(
            Document document,
            Object client,
            Object plan,
            Font h1,
            Font h2,
            Font h3,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "الخطة الغذائية المعتمدة", "06 / Nutrition Plan", h1, muted);

        if (plan == null) {
            document.add(narrativeTable(
                    "الخطة الغذائية",
                    "لا توجد خطة غذائية مرتبطة بالجلسة الحالية.",
                    SOFT_ORANGE,
                    h2,
                    normal
            ));
            return;
        }

        PdfPTable goalCard = cardTable(1);
        addLongField(goalCard, "الهدف الرئيسي", emptyDash(stringValue(invokeAny(plan, "getTargetGoal", "getGoal"))), normal);
        addLongField(goalCard, "حالة الخطة", emptyDash(stringValue(invokeAny(plan, "getPlanStatus", "getStatus"))), normal);
        addLongField(goalCard, "الفترة", formatDate(invokeAny(plan, "getStartDate", "getStart")) + "  إلى  " + formatDate(invokeAny(plan, "getEndDate", "getEnd")), normal);
        addLongField(goalCard, "مدة الخطة", numberText(invokeAny(plan, "getPlanDuration", "getDuration"), " يوم", 0), normal);
        document.add(goalCard);
        document.add(spacer(8));

        addSectionTitle(document, "القيم الغذائية اليومية", "Daily Nutrition Targets", h2);
        PdfPTable macros = new PdfPTable(4);
        macros.setWidthPercentage(100);
        macros.setWidths(new float[]{1f, 1f, 1f, 1f});
        macros.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addMetricCard(macros, "السعرات", numberText(invokeAny(plan, "getTotalCalories"), " سعرة", 0), SOFT_BLUE, h2, normal);
        addMetricCard(macros, "البروتين", numberText(invokeAny(plan, "getProteinAmount"), " غ", 1), SOFT_GREEN, h2, normal);
        addMetricCard(macros, "الكربوهيدرات", numberText(invokeAny(plan, "getCarbohydratesAmount", "getCarbsAmount"), " غ", 1), SOFT_ORANGE, h2, normal);
        addMetricCard(macros, "الدهون", numberText(invokeAny(plan, "getFatAmount"), " غ", 1), SOFT_GRAY, h2, normal);
        document.add(macros);
        document.add(spacer(8));

        PdfPTable practical = new PdfPTable(4);
        practical.setWidthPercentage(100);
        practical.setWidths(new float[]{1f, 1f, 1f, 1f});
        practical.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Object body = null;
        // plan may carry its session; try to reach body through common relations.
        Object planSession = invokeAny(plan, "getSession");
        if (planSession != null) {
            body = invokeAny(planSession, "getBodyData", "getBody_data", "getBody");
        }

        Object water = invokeAny(plan, "getWaterIntake", "getWater_INTAKE", "getWater");

        addMetricCard(practical, "الماء", emptyDash(stringValue(water)), SOFT_BLUE, h3, normalSmall);
        addMetricCard(practical, "عدد الوجبات", numberText(invokeAny(plan, "getMealsCount"), "", 0), SOFT_GREEN, h3, normalSmall);
        addMetricCard(practical, "توزيع الوجبات", emptyDash(stringValue(invokeAny(plan, "getMealDistribution", "getDistribution"))), SOFT_ORANGE, h3, normalSmall);
        addMetricCard(practical, "رقم الخطة", emptyDash(stringValue(invokeAny(plan, "getPlanId", "getId"))), SOFT_GRAY, h3, normalSmall);
        document.add(practical);
        document.add(spacer(8));

        addSectionTitle(document, "ملاحظات أخصائي التغذية", "Nutritionist Notes", h2);
        document.add(narrativeTable(
                "تعليمات الخطة",
                stringValue(invokeAny(plan, "getNotes", "getPlanNotes")),
                SOFT_GREEN,
                h3,
                normal
        ));
    }

    // =====================================================================
    // صفحة الأطعمة والوجبات
    // =====================================================================

    private static void addMealsPage(
            Document document,
            Object plan,
            Font h1,
            Font h2,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "تفاصيل الوجبات والأطعمة المختارة", "07 / Meals & Foods", h1, muted);

        Object foodsObject = invokeAny(plan, "getSelectedFoods", "getPlanFoodItems", "getFoodItems");
        List<Object> foods = toObjectList(foodsObject);

        if (foods.isEmpty()) {
            document.add(narrativeTable(
                    "الأطعمة",
                    "لا توجد أطعمة مختارة مسجلة في الخطة الحالية.",
                    SOFT_ORANGE,
                    h2,
                    normal
            ));
            return;
        }

        Map<String, List<Object>> grouped = new LinkedHashMap<>();
        grouped.put("Breakfast", new ArrayList<>());
        grouped.put("Lunch", new ArrayList<>());
        grouped.put("Dinner", new ArrayList<>());
        grouped.put("Snack", new ArrayList<>());
        grouped.put("Other", new ArrayList<>());

        for (Object item : foods) {
            String meal = stringValue(invokeAny(item, "getMealType", "getMeal", "getMealCategory"));
            String key = normalizeMealKey(meal);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<String, List<Object>> group : grouped.entrySet()) {
            if (group.getValue().isEmpty()) {
                continue;
            }

            addSectionTitle(document, mealArabic(group.getKey()), englishMeal(group.getKey()), h2);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.6f, 1.6f, 1.5f, 3.3f});
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.setHeaderRows(1);
            table.setSplitLate(false);

            addHeaderCell(table, "الغذاء", tableHeader, PRIMARY_DARK);
            addHeaderCell(table, "الكمية", tableHeader, PRIMARY_DARK);
            addHeaderCell(table, "الوحدة", tableHeader, PRIMARY_DARK);
            addHeaderCell(table, "نوع الوجبة / معلومات إضافية", tableHeader, PRIMARY_DARK);

            for (int i = 0; i < group.getValue().size(); i++) {
                Object item = group.getValue().get(i);
                BaseColor row = i % 2 == 0 ? WHITE : SOFT_GRAY;
                Object foodObject = invokeAny(item, "getFoodItem", "getFood", "getItem");
                String foodName = stringValue(invokeAny(foodObject, "getFoodName", "getName"));
                if (foodName.isBlank()) {
                    foodName = stringValue(invokeAny(item, "getFoodName", "getName"));
                }

                String quantity = numberText(invokeAny(item, "getQuantity"), "", 2);
                String unit = stringValue(invokeAny(item, "getUnit"));
                String mealType = stringValue(invokeAny(item, "getMealType", "getMeal"));
                String extra = "الوجبة: " + mealArabic(normalizeMealKey(mealType));

                addDataCell(table, emptyDash(foodName), normalSmall, row, Element.ALIGN_RIGHT);
                addDataCell(table, emptyDash(quantity), normalSmall, row, Element.ALIGN_CENTER);
                addDataCell(table, emptyDash(unit), normalSmall, row, Element.ALIGN_CENTER);
                addDataCell(table, extra, normalSmall, row, Element.ALIGN_RIGHT);
            }

            document.add(table);
            document.add(spacer(7));
        }

        document.add(narrativeTable(
                "ملاحظة مهمة",
                "تم عرض الأصناف والكميات والوحدات كما سُجلت داخل الخطة. لا يتم اختراع مواعيد للوجبات أو بدائل غير موجودة في بيانات النظام.",
                SOFT_GRAY,
                h2,
                normalSmall
        ));
    }

    // =====================================================================
    // الصفحة الختامية - المتابعة والتوقيع
    // =====================================================================

    private static void addFollowUpPage(
            Document document,
            Object client,
            Object session,
            Object report,
            Object body,
            Object plan,
            Font h1,
            Font h2,
            Font h3,
            Font normal,
            Font normalSmall,
            Font muted,
            Font bold,
            Font tableHeader,
            Font tableHeaderDark
    ) throws DocumentException {

        addPageTitle(document, "خطة المتابعة والمرجعية للزيارة القادمة", "08 / Follow-up", h1, muted);

        PdfPTable followMeta = new PdfPTable(4);
        followMeta.setWidthPercentage(100);
        followMeta.setWidths(new float[]{1f, 1f, 1f, 1f});
        followMeta.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addMetricCard(followMeta, "العميل", displayClientName(client), SOFT_BLUE, h3, normalSmall);
        addMetricCard(followMeta, "الجلسة", "#" + stringValue(invokeAny(session, "getId", "getSessionId")), SOFT_GREEN, h3, normalSmall);
        addMetricCard(followMeta, "الموعد القادم", formatDate(invokeAny(report, "getNextAppointment", "getNextVisitDate")), SOFT_ORANGE, h3, normalSmall);
        addMetricCard(followMeta, "الحالة", emptyDash(stringValue(invokeAny(plan, "getPlanStatus", "getStatus"))), SOFT_GRAY, h3, normalSmall);
        document.add(followMeta);
        document.add(spacer(10));

        addSectionTitle(document, "الأهداف القادمة المسجلة", "Next Goals", h2);
        document.add(narrativeTable(
                "الأهداف",
                stringValue(invokeAny(report, "getNextGoals", "getGoals", "getNextGoal")),
                SOFT_GREEN,
                h3,
                normal
        ));

        document.add(spacer(7));
        addSectionTitle(document, "التوصيات", "Recommendations", h2);
        document.add(narrativeTable(
                "التوصيات المسجلة",
                stringValue(invokeAny(report, "getRecommendations", "getAdvice")),
                SOFT_BLUE,
                h3,
                normal
        ));

        document.add(spacer(7));
        addSectionTitle(document, "ما يُستحسن مراجعته في الزيارة القادمة", "Suggested Recheck Items", h2);
        PdfPTable checklist = new PdfPTable(2);
        checklist.setWidthPercentage(100);
        checklist.setWidths(new float[]{1f, 1f});
        checklist.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        String[] items = {
                "الوزن",
                "BMI",
                "نسبة الدهون",
                "SMM",
                "كتلة العضلات",
                "محيط الذراع",
                "محيط الصدر",
                "محيط الخصر",
                "محيط البطن",
                "محيط الحوض",
                "محيط منتصف الفخذ",
                "محيط الساق",
                "مدى الالتزام بالخطة",
                "الفحوصات المسجلة ومرفقاتها عند الحاجة"
        };

        for (int i = 0; i < items.length; i++) {
            BaseColor bg = i % 2 == 0 ? WHITE : SOFT_GRAY;
            String text = "☐  " + items[i];
            // استخدم مربعاً نصياً بسيطاً، لكن من دون emoji حتى لا تظهر مربعات سوداء في بعض الخطوط.
            text = "[ ]  " + items[i];
            addDataCell(checklist, text, normalSmall, bg, Element.ALIGN_RIGHT);
        }

        // ضمان عدد خلايا زوجي.
        if (items.length % 2 != 0) {
            addDataCell(checklist, "", normalSmall, WHITE, Element.ALIGN_RIGHT);
        }

        document.add(checklist);
        document.add(spacer(8));

        addSectionTitle(document, "إقرار ومرجعية الملف", "Reference & Sign-off", h2);
        document.add(narrativeTable(
                "ملاحظات إضافية",
                stringValue(invokeAny(report, "getNotes", "getAdditionalNotes")),
                SOFT_GRAY,
                h3,
                normalSmall
        ));
        document.add(spacer(12));

        PdfPTable signatures = new PdfPTable(3);
        signatures.setWidthPercentage(100);
        signatures.setWidths(new float[]{1f, 1f, 1f});
        signatures.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        addSignatureCell(signatures, "أخصائي التغذية", emptyDash(stringValue(invokeAny(report, "getNutritionistName", "getNutritionist"))), normalSmall, bold);
        addSignatureCell(signatures, "الممرض / المراجعة", "الاسم والتوقيع: ____________________", normalSmall, bold);
        addSignatureCell(signatures, "العميل", "الاسم والتوقيع: ____________________", normalSmall, bold);

        document.add(signatures);
        document.add(spacer(12));

        Paragraph footerNote = rtlParagraph(
                "المؤشرات المحسوبة آلياً مثل BMR و TDEE و LBM مبنية على البيانات المسجلة في النظام في وقت إنشاء التقرير. التشخيص والتقييم والتوصيات والأهداف القادمة هي حقول التقرير المسجلة في الجلسة.",
                muted,
                Element.ALIGN_RIGHT
        );
        footerNote.setSpacingBefore(7);
        document.add(footerNote);
    }

    // =====================================================================
    // Page decorations
    // =====================================================================

    private static void addPageTitle(Document document, String title, String englishTitle, Font titleFont, Font mutedFont)
            throws DocumentException {

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(PAGE_BG);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell.setPaddingBottom(10);
        cell.setPaddingTop(2);

        Paragraph p = rtlParagraph(title, titleFont, Element.ALIGN_RIGHT);
        p.setSpacingAfter(3);
        cell.addElement(p);
        cell.addElement(rtlParagraph(englishTitle, mutedFont, Element.ALIGN_RIGHT));

        header.addCell(cell);
        document.add(header);
    }

    private static void addSectionTitle(Document document, String title, String englishTitle, Font font)
            throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(PRIMARY);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(8);

        Paragraph p = rtlParagraph(title, font, Element.ALIGN_RIGHT);
        p.getFont().setColor(WHITE);
        titleCell.addElement(p);
        Paragraph en = rtlParagraph(englishTitle, font, Element.ALIGN_RIGHT);
        en.getFont().setSize(8.3f);
        en.getFont().setColor(new BaseColor(216, 235, 232));
        titleCell.addElement(en);

        table.addCell(titleCell);
        document.add(table);
    }

    private static PdfPTable cardTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        table.setSplitLate(false);
        return table;
    }

    private static void addInfoCard(
            PdfPTable parent,
            String title,
            BaseColor background,
            Font titleFont,
            Font normal,
            Font bold,
            String... pairs
    ) {
        PdfPCell outer = new PdfPCell();
        outer.setBackgroundColor(background);
        outer.setBorderColor(BORDER);
        outer.setPadding(10);
        outer.setVerticalAlignment(Element.ALIGN_TOP);
        outer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph titleP = rtlParagraph(title, titleFont, Element.ALIGN_RIGHT);
        titleP.setSpacingAfter(6);
        outer.addElement(titleP);

        for (int i = 0; i + 1 < pairs.length; i += 2) {
            String label = pairs[i];
            String value = pairs[i + 1];
            Paragraph p = new Paragraph();
            p.setAlignment(Element.ALIGN_RIGHT);
            p.setLeading(14f);
            p.add(new Chunk(label + ": ", bold));
            p.add(new Chunk(emptyDash(value), normal));
            p.setSpacingAfter(3);
            outer.addElement(p);
        }

        parent.addCell(outer);
    }

    private static void addMetricCard(
            PdfPTable table,
            String label,
            String value,
            BaseColor background,
            Font labelFont,
            Font valueFont
    ) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(background);
        cell.setBorderColor(BORDER);
        cell.setPadding(9);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph title = rtlParagraph(label, labelFont, Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        cell.addElement(title);

        Paragraph valueP = rtlParagraph(emptyDash(value), valueFont, Element.ALIGN_CENTER);
        cell.addElement(valueP);
        table.addCell(cell);
    }

    private static void addRelationCard(
            PdfPTable parent,
            String title,
            Object relationObject,
            BaseColor background,
            Font normal,
            Font bold
    ) {
        PdfPCell outer = new PdfPCell();
        outer.setBackgroundColor(background);
        outer.setBorderColor(BORDER);
        outer.setPadding(10);
        outer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph heading = rtlParagraph(title, bold, Element.ALIGN_RIGHT);
        heading.setSpacingAfter(6);
        outer.addElement(heading);

        List<Object> values = toObjectList(relationObject);
        if (values.isEmpty()) {
            outer.addElement(rtlParagraph("لا توجد بيانات مسجلة.", normal, Element.ALIGN_RIGHT));
        } else {
            for (Object item : values) {
                String name = stringValue(invokeAny(item,
                        "getDiseaseName", "getChronicDiseaseName", "getName",
                        "getAllergyName", "getAllergiesFood", "getFoodName"));
                String status = stringValue(invokeAny(item, "getStatus"));
                String severity = stringValue(invokeAny(item, "getSeverity"));
                String note = stringValue(invokeAny(item, "getNotes", "getNote"));
                String contraindicated = stringValue(invokeAny(item,
                        "getContraindicatedFood", "getContraindicatedFoods"));

                String text = emptyDash(name);
                if (!status.isBlank()) text += "\nالحالة: " + status;
                if (!severity.isBlank()) text += "\nالشدة: " + severity;
                if (!contraindicated.isBlank()) text += "\nأطعمة ممنوعة: " + contraindicated;
                if (!note.isBlank()) text += "\nملاحظات: " + note;

                outer.addElement(rtlParagraph(text, normal, Element.ALIGN_RIGHT));
            }
        }

        parent.addCell(outer);
    }

    private static void addLongField(PdfPTable table, String label, String value, Font normal) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(WHITE);
        cell.setBorderColor(BORDER);
        cell.setPadding(9);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setLeading(15f);
        p.add(new Chunk(label + ":\n", fontClone(normal, Font.BOLD, PRIMARY_DARK)));
        p.add(new Chunk(emptyDash(value), normal));
        cell.addElement(p);
        table.addCell(cell);
    }

    private static PdfPTable narrativeTable(
            String title,
            String text,
            BaseColor background,
            Font titleFont,
            Font bodyFont
    ) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(background);
        cell.setBorderColor(BORDER);
        cell.setPadding(11);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph heading = rtlParagraph(title, titleFont, Element.ALIGN_RIGHT);
        heading.setSpacingAfter(5);
        cell.addElement(heading);

        String content = emptyDash(text);
        Paragraph body = rtlParagraph(content, bodyFont, Element.ALIGN_RIGHT);
        body.setLeading(16f);
        body.setSpacingAfter(1);
        cell.addElement(body);

        table.addCell(cell);
        return table;
    }

    private static void addKeyValue(PdfPTable table, String label, String value, boolean shaded,
                                    BaseFont regular, BaseFont bold) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(shaded ? SOFT_GRAY : WHITE);
        cell.setBorderColor(BORDER);
        cell.setPadding(9);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setLeading(15f);
        p.add(new Chunk(label + ": ", font(bold, 9.5f, Font.BOLD, PRIMARY_DARK)));
        p.add(new Chunk(emptyDash(value), font(9.5f, Font.NORMAL, regular, TEXT)));
        cell.addElement(p);
        table.addCell(cell);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Phrase(emptyDash(text), font));
        cell.setBackgroundColor(background);
        cell.setBorderColor(background);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        table.addCell(cell);
    }

    private static void addDataCell(PdfPTable table, String text, Font font, BaseColor background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(emptyDash(text), font));
        cell.setBackgroundColor(background);
        cell.setBorderColor(BORDER);
        cell.setPadding(7);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell.setNoWrap(false);
        table.addCell(cell);
    }

    private static void addComparisonRow(PdfPTable table,
                                         String label,
                                         Object currentBody,
                                         Object referenceBody,
                                         String getter,
                                         String suffix,
                                         Font normal,
                                         Font bold) {
        double current = safeDouble(invokeGetter(currentBody, getter));
        double reference = safeDouble(invokeGetter(referenceBody, getter));
        double delta = current - reference;

        addDataCell(table, label, bold, SOFT_GRAY, Element.ALIGN_RIGHT);
        addDataCell(table, numberText(current, suffix, 2), normal, WHITE, Element.ALIGN_CENTER);
        addDataCell(table, referenceBody == null ? "-" : numberText(reference, suffix, 2), normal, WHITE, Element.ALIGN_CENTER);
        addDataCell(table, referenceBody == null ? "-" : numberTextWithSign(delta, suffix, 2), normal, SOFT_GREEN, Element.ALIGN_CENTER);
    }

    private static void addSignatureCell(PdfPTable table, String role, String value, Font normal, Font bold) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(WHITE);
        cell.setPadding(12);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        Paragraph p = rtlParagraph(role, bold, Element.ALIGN_RIGHT);
        p.setSpacingAfter(10);
        cell.addElement(p);
        Paragraph v = rtlParagraph(value, normal, Element.ALIGN_RIGHT);
        v.setSpacingBefore(12);
        cell.addElement(v);
        table.addCell(cell);
    }

    private static PdfPCell noBorderCell(Paragraph paragraph, float padding) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(padding);
        cell.addElement(paragraph);
        return cell;
    }

    private static void addSpacerCell(PdfPTable table, float height) {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        table.addCell(cell);
    }

    private static Paragraph spacer(float spacing) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(spacing);
        return p;
    }

    private static Paragraph rtlParagraph(String text, Font font, int alignment) {
        Paragraph p = new Paragraph(emptyDash(text), font);
        p.setAlignment(alignment);
        p.setLeading(font.getSize() * 1.45f);
        return p;
    }

    // =====================================================================
    // Financial/Admin PDF - يحافظ على الوظيفة الإدارية السابقة دون ربط صلب
    // =====================================================================

    /**
     * تقرير مالي إداري مرن. يستقبل AdminFinancialSummary مباشرة أو أي DTO مطابق له.
     */
    public static void createAdminFinancialReport(Object summary, String filePath) throws Exception {
        if (summary == null) {
            throw new IllegalArgumentException("الملخص المالي غير موجود.");
        }

        File output = prepareOutput(filePath);
        BaseFont regular = resolveArabicFont(false);
        BaseFont boldBase = resolveArabicFont(true);

        Font title = font(boldBase, 23, Font.BOLD, PRIMARY_DARK);
        Font sub = font(regular, 10, Font.NORMAL, MUTED);
        Font h = font(boldBase, 14, Font.BOLD, PRIMARY_DARK);
        Font normal = font(regular, 10, Font.NORMAL, TEXT);
        Font bold = font(boldBase, 10, Font.BOLD, TEXT);
        Font tableHeader = font(boldBase, 9.2f, Font.BOLD, WHITE);

        Document document = new Document(PageSize.A4, 36, 36, 54, 48);
        FileOutputStream out = new FileOutputStream(output);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setCompressionLevel(9);
            writer.setPageEvent(new ReportPageEvent(regular, boldBase));
            document.open();

            addPageTitle(document, "التقرير المالي والإداري", "Administrative Financial Report", title, sub);

            Object start = invokeAny(summary, "getStartDate", "getFromDate");
            Object end = invokeAny(summary, "getEndDate", "getToDate");
            int totalSessions = safeInt(invokeAny(summary, "getTotalSessions", "getSessionsCount"));
            double revenue = safeDouble(invokeAny(summary, "getTotalRevenue", "getRevenue"));
            int newClients = safeInt(invokeAny(summary, "getNewClients", "getNewClientsCount"));

            PdfPTable cards = new PdfPTable(4);
            cards.setWidthPercentage(100);
            cards.setWidths(new float[]{1, 1, 1, 1});
            cards.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            addMetricCard(cards, "إجمالي الجلسات", String.valueOf(totalSessions), SOFT_BLUE, h, normal);
            addMetricCard(cards, "إجمالي الإيرادات", numberText(revenue, "", 2), SOFT_GREEN, h, normal);
            addMetricCard(cards, "العملاء الجدد", String.valueOf(newClients), SOFT_ORANGE, h, normal);
            addMetricCard(cards, "الفترة", formatDate(start) + " - " + formatDate(end), SOFT_GRAY, h, normal);
            document.add(cards);
            document.add(spacer(12));

            addSectionTitle(document, "توزيع مستوى الالتزام", "Commitment Distribution", h);
            Object distributionObject = invokeAny(summary, "getCommitmentDistribution", "getCommitmentMap", "getDistribution");
            Map<Object, Object> distribution = toMap(distributionObject);

            if (distribution.isEmpty()) {
                document.add(narrativeTable("التوزيع", "لا توجد بيانات مسجلة.", SOFT_GRAY, h, normal));
            } else {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 1.5f});
                table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                table.setHeaderRows(1);
                addHeaderCell(table, "مستوى الالتزام", tableHeader, PRIMARY_DARK);
                addHeaderCell(table, "عدد الجلسات", tableHeader, PRIMARY_DARK);

                int i = 0;
                for (Map.Entry<Object, Object> entry : distribution.entrySet()) {
                    BaseColor row = i++ % 2 == 0 ? WHITE : SOFT_GRAY;
                    addDataCell(table, stringValue(entry.getKey()), normal, row, Element.ALIGN_RIGHT);
                    addDataCell(table, stringValue(entry.getValue()), normal, row, Element.ALIGN_CENTER);
                }
                document.add(table);
            }

            document.add(spacer(15));
            document.add(narrativeTable(
                    "ملاحظات",
                    "هذا التقرير إداري/مالي ويعتمد على البيانات الواردة في الملخص المالي الممرر إلى المولد.",
                    SOFT_GRAY,
                    h,
                    normal
            ));
        } finally {
            try {
                if (document.isOpen()) {
                    document.close();
                }
            } finally {
                out.close();
            }
        }
    }

    // =====================================================================
    // Footer / header event
    // =====================================================================

    private static final class ReportPageEvent extends PdfPageEventHelper {
        private final Font footerFont;
        private final Font footerBold;

        private ReportPageEvent(BaseFont regular, BaseFont bold) {
            footerFont = font(regular, 8.2f, Font.NORMAL, MUTED);
            footerBold = font(bold, 8.3f, Font.BOLD, PRIMARY_DARK);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();

            // خط علوي خفيف بعد الغلاف.
            canvas.saveState();
            canvas.setColorStroke(BORDER);
            canvas.setLineWidth(0.8f);
            canvas.moveTo(document.left(), document.top() + 16);
            canvas.lineTo(document.right(), document.top() + 16);
            canvas.stroke();

            PdfPTable footer = new PdfPTable(2);
            footer.setTotalWidth(document.right() - document.left());
            footer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            try {
                footer.setWidths(new float[]{3f, 1f});

                PdfPCell right = new PdfPCell(new Phrase("FAS - Food Assistant System", footerBold));
                right.setBorder(Rectangle.NO_BORDER);
                right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                right.setPadding(0);

                PdfPCell left = new PdfPCell(new Phrase("صفحة " + writer.getPageNumber(), footerFont));
                left.setBorder(Rectangle.NO_BORDER);
                left.setHorizontalAlignment(Element.ALIGN_LEFT);
                left.setPadding(0);

                footer.addCell(right);
                footer.addCell(left);
                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 13, canvas);
            } catch (Exception ignored) {
                // لا ينبغي أن يفشل إنشاء التقرير بسبب التذييل.
            }

            canvas.restoreState();
        }
    }

    // =====================================================================
    // Font handling
    // =====================================================================

    private static BaseFont resolveArabicFont(boolean bold) throws Exception {
        List<String> candidates = new ArrayList<>();

        String configured = System.getProperty("fas.pdf.font");
        if (configured != null && !configured.isBlank()) {
            candidates.add(configured);
        }

        if (bold) {
            candidates.add("C:/Windows/Fonts/arialbd.ttf");
            candidates.add("C:/Windows/Fonts/tahomabd.ttf");
        } else {
            candidates.add("C:/Windows/Fonts/arial.ttf");
            candidates.add("C:/Windows/Fonts/tahoma.ttf");
        }

        candidates.add("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");
        candidates.add("/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf");
        candidates.add("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf");

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.isFile()) {
                try {
                    return BaseFont.createFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } catch (Exception ignored) {
                    // Try next candidate.
                }
            }
        }

        // في حالة وجود arialbd فقط أو أي خط عربي مناسب في النظام.
        String fallback = bold ? "C:/Windows/Fonts/arialbd.ttf" : "C:/Windows/Fonts/arial.ttf";
        if (new File(fallback).isFile()) {
            return BaseFont.createFont(fallback, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }

        throw new IllegalStateException(
                "لم يتم العثور على خط عربي مناسب. ضع arial.ttf / tahoma.ttf في C:/Windows/Fonts أو عرّف -Dfas.pdf.font=<path>."
        );
    }

    // =====================================================================
    // Reflection / normalization helpers
    // =====================================================================

    private static List<Object> normalizeSessions(Object client, List<?> sessions) {
        List<Object> result = new ArrayList<>();

        if (sessions != null) {
            for (Object session : sessions) {
                if (session != null) {
                    result.add(session);
                }
            }
        }

        if (!result.isEmpty()) {
            return result;
        }

        Object clientSessions = invokeAny(client, "getSessions", "getSession", "getSessionList");
        result.addAll(toObjectList(clientSessions));
        return result;
    }

    private static Object invokeAny(Object target, String... methodNames) {
        if (target == null || methodNames == null) {
            return null;
        }

        for (String name : methodNames) {
            if (name == null || name.isBlank()) {
                continue;
            }

            Method method = findNoArgMethod(target.getClass(), name);
            if (method == null) {
                continue;
            }

            try {
                if (!method.canAccess(target) && !Modifier.isPublic(method.getModifiers())) {
                    method.setAccessible(true);
                }
                return method.invoke(target);
            } catch (Exception ignored) {
                // Try next alias.
            }
        }

        return null;
    }

    private static Method findNoArgMethod(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == 0) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }

        try {
            return type.getMethod(name);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Object invokeGetter(Object target, String getter) {
        if (target == null || getter == null) {
            return null;
        }
        return invokeAny(target, getter);
    }

    private static List<Object> toObjectList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }

        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }

        if (value instanceof Map<?, ?>) {
            return new ArrayList<>();
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(value, i));
            }
            return list;
        }

        return new ArrayList<>(Collections.singletonList(value));
    }

    private static Map<Object, Object> toMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return new LinkedHashMap<>();
        }

        Map<Object, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // =====================================================================
    // Value helpers
    // =====================================================================

    private static String displayClientName(Object client) {
        if (client == null) {
            return "غير معروف";
        }

        Object fullName = invokeAny(client, "getFullName", "getFullNmae");
        String full = stringValue(fullName);
        if (!full.isBlank() && !full.equals("null")) {
            return normalizeSpaces(full);
        }

        Object name = invokeAny(client, "getName");
        full = stringValue(name);
        if (!full.isBlank()) {
            return normalizeSpaces(full);
        }

        String first = stringValue(invokeAny(client, "getFirstName"));
        String last = stringValue(invokeAny(client, "getLastName"));
        return normalizeSpaces((first + " " + last).trim());
    }

    private static String clientAge(Object client) {
        int age = safeInt(invokeAny(client, "getAge"));
        if (age > 0) {
            return String.valueOf(age);
        }

        Object birth = invokeAny(client, "getBirthDate", "getDateOfBirth");
        LocalDate birthDate = asLocalDate(birth);
        if (birthDate != null) {
            return String.valueOf(Period.between(birthDate, LocalDate.now()).getYears());
        }

        return "-";
    }

    private static String genderArabic(Object gender) {
        String value = stringValue(gender).trim();
        if (value.equalsIgnoreCase("M") || value.equalsIgnoreCase("MALE") || value.equals("ذكر")) {
            return "ذكر";
        }
        if (value.equalsIgnoreCase("F") || value.equalsIgnoreCase("FEMALE") || value.equals("أنثى")) {
            return "أنثى";
        }
        return emptyDash(value);
    }

    private static char genderChar(Object gender) {
        String value = stringValue(gender).trim();
        return (value.equalsIgnoreCase("F") || value.equalsIgnoreCase("FEMALE") || value.equals("أنثى")) ? 'F' : 'M';
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String string) {
            return string.trim();
        }

        if (value instanceof Character character) {
            return String.valueOf(character);
        }

        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }

        if (value instanceof LocalDate date) {
            return DATE_FORMATTER.format(date);
        }

        if (value instanceof LocalDateTime dateTime) {
            return DATE_TIME_FORMATTER.format(dateTime);
        }

        if (value instanceof LocalTime time) {
            return TIME_FORMATTER.format(time);
        }

        if (value instanceof Collection<?> collection) {
            List<String> values = new ArrayList<>();
            for (Object item : collection) {
                String text = stringValue(item);
                if (!text.isBlank()) {
                    values.add(text);
                }
            }
            return String.join(" ", values).trim();
        }

        if (value.getClass().isArray()) {
            List<String> values = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                String text = stringValue(Array.get(value, i));
                if (!text.isBlank()) {
                    values.add(text);
                }
            }
            return String.join(" ", values).trim();
        }

        return String.valueOf(value).trim();
    }

    private static String normalizeSpaces(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String emptyDash(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("null")) {
            return "-";
        }
        return value.trim();
    }

    private static String safeText(String value) {
        return emptyDash(value);
    }

    private static String joinText(String first, String second) {
        String a = first == null ? "" : first.trim();
        String b = second == null ? "" : second.trim();
        if (a.isBlank()) return b;
        if (b.isBlank()) return a;
        return a + "\n\n" + b;
    }

    private static String numberText(Object value, String suffix, int scale) {
        if (value == null) {
            return "-";
        }
        return numberText(safeDouble(value), suffix, scale);
    }

    private static String numberText(double value, String suffix, int scale) {
        if (!Double.isFinite(value) || Math.abs(value) < 0.0000001d) {
            return "-";
        }
        String pattern = scale <= 0 ? "%.0f" : (scale == 1 ? "%.1f" : "%.2f");
        return String.format(Locale.US, pattern, value) + (suffix == null ? "" : suffix);
    }

    private static String numberTextWithSign(double value, String suffix, int scale) {
        if (!Double.isFinite(value)) {
            return "-";
        }
        String pattern = scale <= 0 ? "%+.0f" : (scale == 1 ? "%+.1f" : "%+.2f");
        return String.format(Locale.US, pattern, value) + (suffix == null ? "" : suffix);
    }

    private static double safeDouble(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }

        String text = stringValue(value).replace(",", "").trim();
        if (text.isBlank()) {
            return 0;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static int safeInt(Object value) {
        double d = safeDouble(value);
        return Double.isFinite(d) ? (int) Math.round(d) : 0;
    }

    private static double calculateBmr(Object body, char gender, int age) {
        if (body == null) {
            return 0;
        }
        Object direct = invokeAnyWithArgs(body, "getBMR", gender, age);
        double value = safeDouble(direct);
        if (value > 0) {
            return value;
        }

        double weight = safeDouble(invokeAny(body, "getWeight"));
        double height = safeDouble(invokeAny(body, "getHeight"));
        if (weight <= 0 || height <= 0 || age < 0) {
            return 0;
        }

        return gender == 'F'
                ? (10 * weight + 6.25 * height - 5 * age - 161)
                : (10 * weight + 6.25 * height - 5 * age + 5);
    }

    private static double calculateTdee(Object body, char gender, int age) {
        if (body == null) {
            return 0;
        }
        Object direct = invokeAnyWithArgs(body, "getTDEE", gender, age);
        double value = safeDouble(direct);
        if (value > 0) {
            return value;
        }

        double bmr = calculateBmr(body, gender, age);
        double factor = safeDouble(invokeAny(body, "getActivityFactor"));
        return bmr > 0 && factor > 0 ? bmr * factor : 0;
    }

    private static Object invokeAnyWithArgs(Object target, String methodName, Object... args) {
        if (target == null || methodName == null) {
            return null;
        }

        Method method = findCompatibleMethod(target.getClass(), methodName, args);
        if (method == null) {
            return null;
        }

        try {
            if (!method.canAccess(target) && !Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Method findCompatibleMethod(Class<?> type, String name, Object[] args) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
                    continue;
                }
                if (parametersCompatible(method.getParameterTypes(), args)) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static boolean parametersCompatible(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            Class<?> type = parameterTypes[i];
            if (arg == null) {
                if (type.isPrimitive()) return false;
                continue;
            }
            if (type.isPrimitive()) {
                if (!primitiveCompatible(type, arg.getClass())) return false;
            } else if (!type.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private static boolean primitiveCompatible(Class<?> primitive, Class<?> wrapper) {
        if (primitive == char.class) return wrapper == Character.class;
        if (primitive == int.class) return Number.class.isAssignableFrom(wrapper);
        if (primitive == long.class) return Number.class.isAssignableFrom(wrapper);
        if (primitive == float.class) return Number.class.isAssignableFrom(wrapper);
        if (primitive == double.class) return Number.class.isAssignableFrom(wrapper);
        if (primitive == short.class) return Number.class.isAssignableFrom(wrapper);
        if (primitive == byte.class) return Number.class.isAssignableFrom(wrapper);
        if (primitive == boolean.class) return wrapper == Boolean.class;
        return false;
    }

    // =====================================================================
    // Date / time helpers
    // =====================================================================

    private static LocalDateTime sessionDateTime(Object session) {
        Object value = invokeAny(session,
                "getUploadTime",
                "getSessionDate",
                "getDate",
                "getCreatedAt",
                "getCreationDate");

        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof LocalDate date) {
            return date.atStartOfDay();
        }
        if (value instanceof String text) {
            try {
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDate.parse(text).atStartOfDay();
                } catch (DateTimeParseException ignoredAgain) {
                    return null;
                }
            }
        }
        return null;
    }

    private static String formatDateTime(Object value) {
        if (value == null) return "-";
        if (value instanceof LocalDateTime dateTime) return DATE_TIME_FORMATTER.format(dateTime);
        if (value instanceof LocalDate date) return DATE_FORMATTER.format(date);
        if (value instanceof LocalTime time) return TIME_FORMATTER.format(time);
        if (value instanceof String text) {
            LocalDateTime dt = parseDateTime(text);
            if (dt != null) return DATE_TIME_FORMATTER.format(dt);
            LocalDate d = parseDate(text);
            if (d != null) return DATE_FORMATTER.format(d);
            return emptyDash(text);
        }
        return emptyDash(stringValue(value));
    }

    private static String formatDate(Object value) {
        if (value == null) return "-";
        if (value instanceof LocalDate date) return DATE_FORMATTER.format(date);
        if (value instanceof LocalDateTime dateTime) return DATE_FORMATTER.format(dateTime.toLocalDate());
        if (value instanceof String text) {
            LocalDate date = parseDate(text);
            if (date != null) return DATE_FORMATTER.format(date);
            LocalDateTime dt = parseDateTime(text);
            if (dt != null) return DATE_FORMATTER.format(dt.toLocalDate());
            return emptyDash(text);
        }
        return emptyDash(stringValue(value));
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMATTER.format(value);
    }

    private static String formatTime(Object value) {
        if (value == null) return "-";
        if (value instanceof LocalTime time) return TIME_FORMATTER.format(time);
        if (value instanceof String text) {
            try {
                return TIME_FORMATTER.format(LocalTime.parse(text));
            } catch (DateTimeParseException ignored) {
                return emptyDash(text);
            }
        }
        return emptyDash(stringValue(value));
    }

    private static LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDate date) return date;
        if (value instanceof LocalDateTime dateTime) return dateTime.toLocalDate();
        if (value instanceof String text) return parseDate(text);
        return null;
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    // =====================================================================
    // Meal helpers
    // =====================================================================

    private static String normalizeMealKey(String mealType) {
        if (mealType == null) return "Other";
        String value = mealType.trim().toLowerCase(Locale.ROOT);

        if (value.equals("breakfast") || value.equals("إفطار") || value.equals("فطور")) return "Breakfast";
        if (value.equals("lunch") || value.equals("غداء")) return "Lunch";
        if (value.equals("dinner") || value.equals("عشاء")) return "Dinner";
        if (value.equals("snack") || value.equals("سناك") || value.equals("وجبة خفيفة")) return "Snack";
        return "Other";
    }

    private static String mealArabic(String meal) {
        return switch (meal) {
            case "Breakfast" -> "الإفطار";
            case "Lunch" -> "الغداء";
            case "Dinner" -> "العشاء";
            case "Snack" -> "الوجبات الخفيفة";
            default -> "أخرى";
        };
    }

    private static String englishMeal(String meal) {
        return switch (meal) {
            case "Breakfast" -> "Breakfast";
            case "Lunch" -> "Lunch";
            case "Dinner" -> "Dinner";
            case "Snack" -> "Snack";
            default -> "Other";
        };
    }

    // =====================================================================
    // Font helpers
    // =====================================================================

    private static Font font(BaseFont baseFont, float size, int style, BaseColor color) {
        return new Font(baseFont, size, style, color);
    }

    private static Font font(float size, int style, BaseFont baseFont, BaseColor color) {
        return new Font(baseFont, size, style, color);
    }

    private static Font fontClone(Font source, int style, BaseColor color) {
        return new Font(source.getBaseFont(), source.getSize(), style, color);
    }

    private static Font tinyFont(Font source) {
        return new Font(source.getBaseFont(), 8.0f, Font.NORMAL, TEXT);
    }

    // =====================================================================
    // File helpers
    // =====================================================================

    private static File prepareOutput(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("مسار الملف غير صالح.");
        }

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("تعذر إنشاء المجلد: " + parent);
        }
        return file;
    }
}