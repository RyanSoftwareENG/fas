package pdfmanagement;

import entities.GeneratedReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class GetGeneratedReports {

    // استخدام مسار نسبي يشير إلى المجلد داخل المشروع مباشرة
    // سيعمل هذا المسار على أي نظام تشغيل وفي أي مكان تضع فيه المشروع
    private static final String REPORTS_DIR = "data/pdf";

    public List<GeneratedReport> getAllReports() {
        List<GeneratedReport> reports = new ArrayList<>();
        File folder = new File(REPORTS_DIR);

        // التأكد من وجود المجلد وإمكانية الوصول إليه
        if (!folder.exists() || !folder.isDirectory()) {
            // استخدام getAbsolutePath هنا سيطبع المسار الكامل الذي يبحث فيه البرنامج ليساعدك في اكتشاف الأخطاء
            System.err.println("تحذير: مجلد التقارير غير موجود في المسار: " + folder.getAbsolutePath());
            return reports;
        }

        // جلب جميع ملفات PDF داخل المجلد
        File[] pdfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (pdfFiles == null || pdfFiles.length == 0) {
            System.out.println("لم يتم العثور على أي ملفات PDF داخل المجلد.");
            return reports;
        }

        for (File file : pdfFiles) {
            try {
                // 1. جلب تاريخ تعديل/إنشاء الملف من نظام التشغيل
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                LocalDateTime fileDate = LocalDateTime.ofInstant(
                        attrs.lastModifiedTime().toInstant(),
                        ZoneId.systemDefault()
                );

                // 2. معالجة اسم الملف واستخراج اسم العميل
                String fileName = file.getName().replace(".pdf", "");

                long reportId = generateReportIdFromFile(file); // ID فريد يعتمد على الملف
                long clientId = 0;
                String clientName = parseClientName(fileName);

                // 3. إنشاء كائن التقرير مع تعيين المسار الكامل للملف ليتم فتحه لاحقاً
                GeneratedReport report = new GeneratedReport(
                        reportId,
                        clientId,
                        clientName,
                        file.getAbsolutePath(),
                        fileDate
                );

                reports.add(report);

            } catch (IOException e) {
                System.err.println("خطأ أثناء قراءة بيانات الملف: " + file.getName() + " - " + e.getMessage());
            }
        }

        return reports;
    }

    /**
     * حذف ملف الـ PDF الفعلي مباشرة من مجلد المشروع
     */
    public boolean deleteReport(long reportId) {
        List<GeneratedReport> reports = getAllReports();
        for (GeneratedReport report : reports) {
            if (report.getReportId() == reportId) {
                File fileToDelete = new File(report.getFilePath());
                if (fileToDelete.exists()) {
                    return fileToDelete.delete(); // حذف مباشر من القرص
                }
            }
        }
        return false;
    }

    /**
     * استخراج وتنظيف اسم العميل تلقائياً من اسم الملف
     */
    private String parseClientName(String fileName) {
        // تنظيف اسم الملف من الكلمات الشائعة مثل Report والأرقام والشرطات
        String cleanedName = fileName.replaceAll("(?i)report", "")
                .replaceAll("[_-]", " ")
                .replaceAll("\\d+", "")
                .trim();

        return cleanedName.isEmpty() ? fileName : cleanedName;
    }

    /**
     * توليد معرف فريد (ID) يعتمد على Hash المسار لتمييز العناصر في القائمة
     */
    private long generateReportIdFromFile(File file) {
        return Math.abs(file.getAbsolutePath().hashCode());
    }
}