package api;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.SubscriptionResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class SubscriptionAPI {

    private final ApiClient apiClient;

    public SubscriptionAPI(
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
    // الاشتراك الحالي
    // =====================================================

    public SubscriptionResponse
    getCurrent()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.getRawAuthenticated(
                        ApiEndpoints.SUBSCRIPTIONS
                                + "/current"
                );

        checkResponse(
                response
        );

        String body =
                response.body();

        if (body == null ||
                body.isBlank() ||
                "null".equalsIgnoreCase(
                        body.trim()
                )) {

            return null;
        }

        return apiClient
                .getObjectMapper()
                .readValue(
                        body,
                        SubscriptionResponse.class
                );
    }

    // =====================================================
    // جميع اشتراكات العيادة
    // =====================================================

    public List<SubscriptionResponse>
    getMySubscriptions()
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                apiClient.getRawAuthenticated(
                        ApiEndpoints.SUBSCRIPTIONS
                );

        checkResponse(
                response
        );

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
                                List<SubscriptionResponse>
                                >() {}
                );
    }

    // =====================================================
    // Response
    // =====================================================

    private void checkResponse(
            HttpResponse<String> response
    ) throws IOException {

        if (response == null) {

            throw new IOException(
                    "لم تصل استجابة من السيرفر."
            );
        }

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "فشل طلب الاشتراك. HTTP "
                            + response.statusCode()
                            + " - "
                            + response.body()
            );
        }
    }
}
