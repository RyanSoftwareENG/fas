package api;

import dto.DeviceRegistrationRequest;
import dto.SessionValidationResponse;
import dto.UserLoginRequest;
import dto.UserLoginResponse;
import security.ClientSecurityGuard;
import session.ClientSession;
import session.ClientSessionStore;

import java.io.IOException;

public class AuthAPI {

    private final ApiClient apiClient;

    // =====================================================
    // Constructor
    // =====================================================

    public AuthAPI(
            ApiClient apiClient
    ) {

        if (apiClient == null) {

            throw new IllegalArgumentException(
                    "ApiClient مطلوب."
            );
        }

        this.apiClient =
                apiClient;
    }

    // =====================================================
    // تسجيل الدخول
    // POST /api/auth/login
    // =====================================================

    public UserLoginResponse login(
            UserLoginRequest request
    ) throws IOException, InterruptedException {

        validateLoginRequest(
                request
        );

        // -------------------------------------------------
        // تسجيل الدخول له معالجة خاصة لـ 401
        // -------------------------------------------------

        UserLoginResponse response =
                apiClient.postAuthResponse(
                        ApiEndpoints.LOGIN,
                        request,
                        UserLoginResponse.class
                );

        if (response == null) {

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة."
            );
        }

        // -------------------------------------------------
        // تسجيل الدخول مرفوض
        // -------------------------------------------------

        if (!response.isSuccess()) {

            return response;
        }

        // =================================================
        // جهاز يحتاج إلى التسجيل
        // =================================================

        if (response.isNeedsSetup()) {

            if (response.getSetupToken() == null ||
                    response.getSetupToken().isBlank()) {

                throw new IOException(
                        "الجهاز يحتاج إلى التسجيل، "
                                + "لكن السيرفر لم يُرجع Setup Token."
                );
            }

            /*
             * Setup Token ليس Session Token.
             *
             * لا نضعه داخل ApiClient.
             * لا نحفظه داخل ClientSession.
             */

            return response;
        }

        // =================================================
        // جهاز مسجل
        // =================================================

        if (response.getToken() == null ||
                response.getToken().isBlank()) {

            throw new IOException(
                    "تم قبول تسجيل الدخول، "
                            + "لكن السيرفر لم يُرجع Session Token."
            );
        }

        // -------------------------------------------------
        // حفظ Session Token
        // -------------------------------------------------

        apiClient.setToken(
                response.getToken()
        );

        // -------------------------------------------------
        // إنشاء الجلسة المحلية
        // -------------------------------------------------

        ClientSession session =
                new ClientSession(
                        response.getUserId(),
                        response.getClinicId(),
                        response.getDeviceId(),
                        response.getUsername(),
                        response.getFullName(),
                        response.getRoleName(),
                        response.getToken()
                );

        ClientSessionStore.save(
                session
        );

        // -------------------------------------------------
        // مزامنة Security Guard
        // -------------------------------------------------

        ClientSecurityGuard
                .getInstance()
                .adoptSession(
                        session
                );

        return response;
    }

    // =====================================================
    // تسجيل جهاز جديد
    // POST /api/auth/register-device
    // =====================================================

    public UserLoginResponse registerDevice(
            String setupToken,
            DeviceRegistrationRequest request
    ) throws IOException, InterruptedException {

        if (setupToken == null ||
                setupToken.isBlank()) {

            throw new IllegalArgumentException(
                    "رمز تهيئة الجهاز مطلوب."
            );
        }

        validateDeviceRegistrationRequest(
                request
        );

        // -------------------------------------------------
        // إرسال Setup Token في Header
        // -------------------------------------------------

        UserLoginResponse response =
                apiClient.postWithHeaderAuthResponse(
                        ApiEndpoints.REGISTER_DEVICE,
                        request,
                        UserLoginResponse.class,
                        "X-Device-Setup-Token",
                        setupToken
                );

        if (response == null) {

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة أثناء تسجيل الجهاز."
            );
        }

        // -------------------------------------------------
        // فشل تسجيل الجهاز
        // -------------------------------------------------

        if (!response.isSuccess()) {

            return response;
        }

        // =================================================
        // يجب أن يعيد السيرفر Session Token
        // =================================================

        if (response.getToken() == null ||
                response.getToken().isBlank()) {

            throw new IOException(
                    "تم تسجيل الجهاز، "
                            + "لكن السيرفر لم يُرجع Session Token."
            );
        }

        // -------------------------------------------------
        // حفظ Session Token
        // -------------------------------------------------

        apiClient.setToken(
                response.getToken()
        );

        // -------------------------------------------------
        // إنشاء الجلسة المحلية
        // -------------------------------------------------

        ClientSession session =
                new ClientSession(
                        response.getUserId(),
                        response.getClinicId(),
                        response.getDeviceId(),
                        response.getUsername(),
                        response.getFullName(),
                        response.getRoleName(),
                        response.getToken()
                );

        ClientSessionStore.save(
                session
        );

        // -------------------------------------------------
        // مزامنة Security Guard
        // -------------------------------------------------

        ClientSecurityGuard
                .getInstance()
                .adoptSession(
                        session
                );

        return response;
    }

    // =====================================================
    // تسجيل الخروج
    // POST /api/auth/logout
    // =====================================================

    public void logout()
            throws IOException, InterruptedException {

        try {

            if (apiClient.hasToken()) {

                apiClient.postAuthenticated(
                        ApiEndpoints.LOGOUT,
                        null,
                        Void.class
                );
            }

        } finally {

            /*
             * تنظيف الجلسة محليًا
             * حتى لو فشل الاتصال بالسيرفر.
             */

            apiClient.clearToken();

            try {

                ClientSessionStore.clear();

            } finally {

                ClientSecurityGuard
                        .getInstance()
                        .clearLocalSession();
            }
        }
    }

    // =====================================================
    // تحميل الجلسة المحلية
    // =====================================================

    public ClientSession loadLocalSession()
            throws IOException {

        return ClientSessionStore.load();
    }

    // =====================================================
    // استعادة Token
    // =====================================================

    public boolean restoreLocalToken()
            throws IOException {

        ClientSession session =
                ClientSessionStore.load();

        if (session == null ||
                !session.hasToken()) {

            return false;
        }

        apiClient.setToken(
                session.getToken()
        );

        ClientSecurityGuard
                .getInstance()
                .adoptSession(
                        session
                );

        return true;
    }

    // =====================================================
    // مسح الجلسة المحلية
    // =====================================================

    public void clearLocalSession()
            throws IOException {

        apiClient.clearToken();

        ClientSessionStore.clear();
    }

    // =====================================================
    // التحقق من الجلسة
    // GET /api/auth/session
    // =====================================================

    public SessionValidationResponse validateSession()
            throws IOException, InterruptedException {

        if (!apiClient.hasToken()) {

            return null;
        }

        return apiClient.getAuthenticated(
                ApiEndpoints.AUTH + "/session",
                SessionValidationResponse.class
        );
    }

    // =====================================================
    // التحقق من طلب Login
    // =====================================================

    private void validateLoginRequest(
            UserLoginRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "بيانات تسجيل الدخول مطلوبة."
            );
        }

        if (request.getUsername() == null ||
                request.getUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "اسم المستخدم مطلوب."
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "كلمة المرور مطلوبة."
            );
        }

        if (request.getInstallationId() == null ||
                request.getInstallationId().isBlank()) {

            throw new IllegalArgumentException(
                    "معرف التثبيت مطلوب."
            );
        }
    }

    // =====================================================
    // التحقق من طلب تسجيل الجهاز
    // =====================================================

    private void validateDeviceRegistrationRequest(
            DeviceRegistrationRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "بيانات تسجيل الجهاز مطلوبة."
            );
        }

        if (request.getActivationCode() == null ||
                request.getActivationCode().isBlank()) {

            throw new IllegalArgumentException(
                    "كود التفعيل مطلوب."
            );
        }

        if (request.getInstallationId() == null ||
                request.getInstallationId().isBlank()) {

            throw new IllegalArgumentException(
                    "معرف تثبيت الجهاز مطلوب."
            );
        }
    }
}