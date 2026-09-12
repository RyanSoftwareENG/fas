package api;

import dto.FullDashboardResponse;

import java.io.IOException;

public class ReportsAPI {

    private final ApiClient apiClient;

    public ReportsAPI() {

        this.apiClient =
                ClientApiManager
                        .getInstance()
                        .getApiClient();
    }

// =====================================================
// Full Dashboard
// =====================================================

    public FullDashboardResponse getFullDashboard(
            int days,
            int recentLimit
    ) throws IOException, InterruptedException {

        if (days <= 0) {
            days = 30;
        }

        if (days > 365) {
            days = 365;
        }

        if (recentLimit <= 0) {
            recentLimit = 10;
        }

        if (recentLimit > 50) {
            recentLimit = 50;
        }

        String url =
                ApiEndpoints.REPORTS_DASHBOARD_FULL
                        + "?days="
                        + days
                        + "&recentLimit="
                        + recentLimit;

        return apiClient.getAuthenticated(
                url,
                FullDashboardResponse.class
        );
    }
}
