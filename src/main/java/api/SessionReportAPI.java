package api;

import entities.SessionReport;

import java.io.IOException;
import java.util.List;

public class SessionReportAPI {

    private final ApiClient apiClient;

    private String lastErrorMessage;

    public SessionReportAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

// =====================================================
// آخر رسالة خطأ
// =====================================================

    public String getExceptionMessage() {
        return lastErrorMessage;
    }

    private void clearLastError() {
        lastErrorMessage = null;
    }

    private void setLastError(
            Throwable throwable
    ) {

        if (throwable == null) {
            lastErrorMessage =
                    "حدث خطأ غير معروف.";
            return;
        }

        Throwable cause =
                throwable;

        while (
                cause.getCause() != null
        ) {
            cause =
                    cause.getCause();
        }

        String message =
                cause.getMessage();

        lastErrorMessage =
                message == null ||
                        message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message;
    }

// =====================================================
// جلب جميع التقارير
// GET /api/session-report/all
// =====================================================

    public List<SessionReport> getAllReport()
            throws IOException, InterruptedException {

        clearLastError();

        try {

            SessionReport[] reports =
                    apiClient.getAuthenticated(
                            ApiEndpoints.SESSION_REPORT_ALL,
                            SessionReport[].class
                    );

            return reports == null
                    ? List.of()
                    : List.of(reports);

        } catch (IOException |
                 InterruptedException e) {

            setLastError(e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

// =====================================================
// جلب تقرير جلسة
// GET /api/session-report/session/{id}
// =====================================================

    public SessionReport getReportBySessionId(
            Long sessionId
    ) throws IOException, InterruptedException {

        clearLastError();

        validateId(
                sessionId,
                "معرف الجلسة"
        );

        try {

            return apiClient.getAuthenticated(
                    ApiEndpoints.sessionReportBySessionId(
                            sessionId
                    ),
                    SessionReport.class
            );

        } catch (IOException |
                 InterruptedException e) {

            setLastError(e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

// =====================================================
// حفظ تقرير
// POST /api/session-report
// =====================================================

    public boolean saveReport(
            SessionReport report
    ) throws IOException, InterruptedException {

        clearLastError();

        if (report == null) {

            throw new IllegalArgumentException(
                    "بيانات التقرير مطلوبة."
            );
        }

        try {

            apiClient.postAuthenticated(
                    ApiEndpoints.SESSION_REPORT,
                    report,
                    Void.class
            );

            return true;

        } catch (IOException |
                 InterruptedException e) {

            setLastError(e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

// =====================================================
// تحديث تقرير
// PUT /api/session-report/{id}
// =====================================================

    public boolean updateReport(
            SessionReport report
    ) throws IOException, InterruptedException {

        clearLastError();

        if (report == null) {

            throw new IllegalArgumentException(
                    "بيانات التقرير مطلوبة."
            );
        }

        if (report.getReportId() == null ||
                report.getReportId() <= 0) {

            throw new IllegalArgumentException(
                    "معرف التقرير غير صالح."
            );
        }

        try {

            apiClient.putAuthenticated(
                    ApiEndpoints.sessionReportById(
                            report.getReportId()
                    ),
                    report,
                    Void.class
            );

            return true;

        } catch (IOException |
                 InterruptedException e) {

            setLastError(e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

// =====================================================
// حذف تقرير
// DELETE /api/session-report/{id}
// =====================================================

    public boolean deleteReport(
            long reportId
    ) throws IOException, InterruptedException {

        clearLastError();

        if (reportId <= 0) {

            throw new IllegalArgumentException(
                    "معرف التقرير غير صالح."
            );
        }

        try {

            apiClient.delete(
                    ApiEndpoints.sessionReportById(
                            reportId
                    )
            );

            return true;

        } catch (IOException |
                 InterruptedException e) {

            setLastError(e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

// =====================================================
// التحقق من ID
// =====================================================

    private void validateId(
            Long id,
            String fieldName
    ) {

        if (id == null ||
                id <= 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " غير صالح."
            );
        }
    }
}
