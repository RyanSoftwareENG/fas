        package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionRequestCreateRequest;
import dto.SubscriptionRequestResponse;

import java.io.IOException;
import java.util.List;

public class SubscriptionRequestAPI {

    private final ApiClient apiClient;

    public SubscriptionRequestAPI(
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
    // إنشاء طلب اشتراك / تجديد
    // =====================================================

    public SubscriptionRequestResponse createRequest(
            SubscriptionRequestCreateRequest request
    ) throws IOException, InterruptedException {

        return apiClient.postAuthenticated(
                ApiEndpoints.SUBSCRIPTION_REQUESTS,
                request,
                SubscriptionRequestResponse.class
        );
    }

    // =====================================================
    // طلبات العيادة
    // =====================================================

    public List<SubscriptionRequestResponse>
    getMyRequests()
            throws IOException, InterruptedException {

        String responseBody =
                apiClient
                        .getRawAuthenticated(
                                ApiEndpoints.SUBSCRIPTION_REQUESTS
                        )
                        .body();

        return apiClient
                .getObjectMapper()
                .readValue(
                        responseBody,
                        new TypeReference<
                                List<SubscriptionRequestResponse>
                                >() {}
                );
    }
}
