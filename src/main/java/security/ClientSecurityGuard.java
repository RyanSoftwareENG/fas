package security;

import api.AuthAPI;
import api.ClientApiManager;
import dto.SessionValidationResponse;
import session.ClientSession;
import session.ClientSessionStore;

import java.io.IOException;

public final class ClientSecurityGuard {

    private static volatile ClientSecurityGuard instance;

    private final ClientApiManager apiManager;
    private final AuthAPI authAPI;

    private ClientSession currentSession;

    private ClientSecurityGuard() {

        apiManager =
                ClientApiManager
                        .getInstance();

        authAPI =
                apiManager
                        .getAuthAPI();
    }

    public static ClientSecurityGuard getInstance() {

        ClientSecurityGuard result =
                instance;

        if (result == null) {

            synchronized (
                    ClientSecurityGuard.class
            ) {

                result =
                        instance;

                if (result == null) {

                    result =
                            new ClientSecurityGuard();

                    instance =
                            result;
                }
            }
        }

        return result;
    }

// =====================================================
// التحقق من حالة بدء التشغيل
// =====================================================

    public SecurityCheckResult checkStartup()
            throws IOException, InterruptedException {

        /*
         * لا توجد جلسة محلية.
         */
        if (!ClientSessionStore.exists()) {

            return SecurityCheckResult.failure(
                    SecurityStatus.LOGIN_REQUIRED,
                    "لا توجد جلسة مستخدم محفوظة."
            );
        }

        /*
         * قراءة الجلسة المحلية.
         */
        ClientSession session =
                ClientSessionStore.load();

        if (session == null ||
                !session.hasToken()) {

            clearLocalSession();

            return SecurityCheckResult.failure(
                    SecurityStatus.LOGIN_REQUIRED,
                    "بيانات الجلسة المحلية غير صالحة."
            );
        }

        /*
         * تحميل Token إلى ApiClient المركزي.
         */
        apiManager.setToken(
                session.getToken()
        );

        /*
         * التحقق الحقيقي من السيرفر.
         */
        SessionValidationResponse response =
                authAPI.validateSession();

        if (response == null) {

            clearLocalSession();

            return SecurityCheckResult.failure(
                    SecurityStatus.SERVER_REJECTED,
                    "لم يستجب السيرفر ببيانات التحقق."
            );
        }

        if (!response.isValid()) {

            clearLocalSession();

            return SecurityCheckResult.failure(
                    SecurityStatus.SESSION_INVALID,
                    response.getMessage()
            );
        }

        /*
         * تحديث بيانات الجلسة المحلية بالمعلومات
         * التي أعادها السيرفر.
         */
        ClientSession validatedSession =
                new ClientSession(
                        response.getUserId(),
                        response.getClinicId(),
                        response.getDeviceId(),
                        response.getUsername(),
                        response.getFullName(),
                        response.getRoleName(),
                        session.getToken()
                );

        ClientSessionStore.save(
                validatedSession
        );

        currentSession =
                validatedSession;

        return SecurityCheckResult.success(
                "الجلسة صالحة."
        );
    }

// =====================================================
// التحقق من وجود جلسة صالحة في الذاكرة
// =====================================================

    public SecurityCheckResult requireSession() {

        if (currentSession == null ||
                !currentSession.hasToken()) {

            return SecurityCheckResult.failure(
                    SecurityStatus.SESSION_REQUIRED,
                    "يتطلب تنفيذ هذه العملية تسجيل الدخول."
            );
        }

        return SecurityCheckResult.success(
                "الجلسة متاحة."
        );
    }

// =====================================================
// الجلسة الحالية
// =====================================================

    public ClientSession getCurrentSession() {

        return currentSession;
    }

// =====================================================
// هل توجد جلسة فعالة في الذاكرة؟
// =====================================================

    public boolean isAuthenticated() {

        return currentSession != null &&
                currentSession.hasToken() &&
                apiManager.hasToken();
    }

// =====================================================
// تسجيل خروج محلي
// =====================================================

    public void clearLocalSession() {

        currentSession =
                null;

        try {

            authAPI.clearLocalSession();

        } catch (IOException e) {

            /*
             * لا نرمي الخطأ هنا لأن الهدف الأمني
             * هو إزالة حالة المصادقة المحلية.
             */
            System.err.println(
                    "تعذر حذف ملف الجلسة المحلية: "
                            + e.getMessage()
            );
        }
    }

// =====================================================
// أنواع الحالات الأمنية
// =====================================================

    public enum SecurityStatus {

        ALLOWED,

        LOGIN_REQUIRED,

        SESSION_REQUIRED,

        SESSION_INVALID,

        DEVICE_INVALID,

        DEVICE_NOT_REGISTERED,

        DEVICE_BLOCKED,

        DEVICE_REVOKED,

        SUBSCRIPTION_INVALID,

        USER_DISABLED,

        SERVER_REJECTED
    }

// =====================================================
// نتيجة التحقق
// =====================================================

    public static final class SecurityCheckResult {

        private final boolean allowed;
        private final SecurityStatus status;
        private final String message;

        private SecurityCheckResult(
                boolean allowed,
                SecurityStatus status,
                String message
        ) {
            this.allowed =
                    allowed;

            this.status =
                    status;

            this.message =
                    message;
        }

        public static SecurityCheckResult success(
                String message
        ) {

            return new SecurityCheckResult(
                    true,
                    SecurityStatus.ALLOWED,
                    message
            );
        }

        public static SecurityCheckResult failure(
                SecurityStatus status,
                String message
        ) {

            return new SecurityCheckResult(
                    false,
                    status,
                    message
            );
        }

        public boolean isAllowed() {
            return allowed;
        }

        public SecurityStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
    public SecurityCheckResult checkOperation() {

        if (!isAuthenticated()) {

            return SecurityCheckResult.failure(
                    SecurityStatus.SESSION_REQUIRED,
                    "يتطلب تنفيذ هذه العملية جلسة مستخدم صالحة."
            );
        }

        return SecurityCheckResult.success(
                "الجلسة متاحة."
        );
    }
    public void adoptSession(
            ClientSession session
    ) {
        if (session == null ||
                !session.hasToken()) {

            throw new IllegalArgumentException(
                    "الجلسة غير صالحة."
            );
        }

        currentSession =
                session;
    }
}
