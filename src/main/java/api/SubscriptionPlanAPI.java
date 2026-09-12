package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionPlanResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class SubscriptionPlanAPI {

    private final ApiClient apiClient;

    public SubscriptionPlanAPI(
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
    // الخطط النشطة
    // =====================================================

    public List<SubscriptionPlanResponse>
    getActive()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.getRawAuthenticated(
                        ApiEndpoints.SUBSCRIPTION_PLANS_ACTIVE
                );

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل تحميل خطط الاشتراك. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }

        String body =
                response.body();

        if (body == null ||
                body.isBlank()) {

            return List.of();
        }

        return apiClient
                .getObjectMapper()
                .readValue(
                        body,
                        new TypeReference<
                                List<SubscriptionPlanResponse>
                                >() {}
                );
    }
}