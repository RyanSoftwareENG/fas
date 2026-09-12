package api;

public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    // =====================================================
    // Server
    // =====================================================

    public static final String BASE_URL =
            "http://localhost:8080";

    // =====================================================
    // Client
    // =====================================================

    public static final String CLIENTS =
            BASE_URL + "/api/clients";

    public static final String PATIENT =
            BASE_URL + "/api/patient";

    // =====================================================
    // Food
    // =====================================================

    public static final String FOOD =
            BASE_URL + "/api/food";

    // =====================================================
    // Sessions
    // =====================================================

    public static final String SESSIONS =
            BASE_URL + "/api/sessions";

    // =====================================================
    // Session Reports
    // =====================================================

    public static final String SESSION_REPORT =
            BASE_URL + "/api/session-report";

    public static final String SESSION_REPORT_ALL =
            SESSION_REPORT + "/all";

    public static String sessionReportBySessionId(
            Long sessionId
    ) {

        return SESSION_REPORT
                + "/session/"
                + sessionId;
    }

    // =====================================================
    // Device
    // =====================================================

    public static final String DEVICE =
            BASE_URL + "/api/device";

    public static final String DEVICE_REGISTER =
            DEVICE + "/register";

    public static final String DEVICE_VERIFY =
            DEVICE + "/verify";

    public static final String DEVICE_HEARTBEAT =
            DEVICE + "/heartbeat";

    // =====================================================
    // Authentication
    // =====================================================

    public static final String AUTH =
            BASE_URL + "/api/auth";

    public static final String LOGIN =
            AUTH + "/login";

    public static final String LOGOUT =
            AUTH + "/logout";
    public static final String REGISTER_DEVICE =
            AUTH + "/register-device";

    // =====================================================
    // Helper URLs
    // =====================================================

    public static String clientById(
            Long clientId
    ) {

        return CLIENTS
                + "/"
                + clientId;
    }

    public static String clientFullById(
            Long clientId
    ) {

        return CLIENTS
                + "/full/"
                + clientId;
    }

    public static String clientHealth(
            Long clientId
    ) {

        return CLIENTS
                + "/"
                + clientId
                + "/health";
    }

    public static String clientLifestyle(
            Long clientId
    ) {

        return CLIENTS
                + "/"
                + clientId
                + "/lifestyle";
    }

    public static String patientDiseases() {

        return PATIENT
                + "/diseases";
    }

    public static String patientDisease(
            Long diseaseId
    ) {

        return PATIENT
                + "/diseases/"
                + diseaseId;
    }

    public static String patientAllergies() {

        return PATIENT
                + "/allergies";
    }

    public static String patientAllergy(
            Long allergyId
    ) {

        return PATIENT
                + "/allergies/"
                + allergyId;
    }

    public static String foodById(
            Long foodId
    ) {

        return FOOD
                + "/"
                + foodId;
    }

    public static String sessionById(
            Long sessionId
    ) {

        return SESSIONS
                + "/"
                + sessionId;
    }

    public static String sessionReportById(
            Long reportId
    ) {

        return SESSION_REPORT
                + "/"
                + reportId;
    }
    // =====================================================
// Subscription Requests
// =====================================================

    public static final String SUBSCRIPTION_REQUESTS =
            BASE_URL + "/api/subscription-requests";

    public static String subscriptionRequestById(
            Long requestId
    ) {

        return SUBSCRIPTION_REQUESTS
                + "/"
                + requestId;
    }
// =====================================================
// Subscriptions
// =====================================================

    public static final String SUBSCRIPTIONS =
            BASE_URL + "/api/subscriptions";
    public static final String SUBSCRIPTION_PLANS =
            BASE_URL + "/api/subscription-plans";

    public static final String SUBSCRIPTION_PLANS_ACTIVE =
            SUBSCRIPTION_PLANS + "/active";

// =====================================================
// Reports
// =====================================================

    public static final String REPORTS =
            BASE_URL + "/api/reports";

    public static final String REPORTS_DASHBOARD =
            REPORTS + "/dashboard";

    public static final String REPORTS_DASHBOARD_FULL =
            REPORTS + "/dashboard/full";

    public static final String REPORTS_SESSIONS_TREND =
            REPORTS + "/sessions/trend";

    public static final String REPORTS_SESSIONS_RECENT =
            REPORTS + "/sessions/recent";

    public static final String REPORTS_REVENUE_TREND =
            REPORTS + "/revenue/trend";

    public static final String REPORTS_PLAN_STATUS =
            REPORTS + "/nutrition-plans/status";

    public static final String REPORTS_CHRONIC_DISEASES =
            REPORTS + "/chronic-diseases";

    public static final String REPORTS_ALLERGIES =
            REPORTS + "/allergies";

    public static final String REPORTS_ALERTS =
            REPORTS + "/alerts";

// =====================================================
// Report Helper URLs
// =====================================================

    public static String reportsSessionsTrend(
            int days
    ) {

        return REPORTS_SESSIONS_TREND
                + "?days="
                + days;
    }

    public static String reportsSessionsRecent(
            int limit
    ) {

        return REPORTS_SESSIONS_RECENT
                + "?limit="
                + limit;
    }

    public static String reportsRevenueTrend(
            int days
    ) {

        return REPORTS_REVENUE_TREND
                + "?days="
                + days;
    }

    public static String reportsBodyProgress(
            Long clientId
    ) {

        return REPORTS
                + "/body-progress/"
                + clientId;
    }

    public static String reportsBodyProgressSummary(
            Long clientId
    ) {

        return REPORTS
                + "/body-progress/"
                + clientId
                + "/summary";
    }
}