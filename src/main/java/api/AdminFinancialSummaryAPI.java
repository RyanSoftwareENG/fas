package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entities.AdminFinancialSummary;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

public class AdminFinancialSummaryAPI {

    private static final String BASE_URL =
            "https://financial-summary-api.herokuapp.com/";

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(20);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AdminFinancialSummaryAPI() {

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(10)
                        )
                        .build();

        this.objectMapper =
                new ObjectMapper();

        this.objectMapper.registerModule(
                new JavaTimeModule()
        );
    }

// =====================================================
// حساب التقرير المالي
// =====================================================

    public AdminFinancialSummary calculate(
            LocalDate start,
            LocalDate end
    ) {

        validateDates(
                start,
                end
        );

        try {

            ClientRequest requestData =
                    new ClientRequest(
                            start,
                            end
                    );

            String json =
                    objectMapper.writeValueAsString(
                            requestData
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            BASE_URL
                                    )
                            )
                            .timeout(
                                    REQUEST_TIMEOUT
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new IOException(
                        "سيرفر التقرير المالي أعاد HTTP "
                                + response.statusCode()
                                + "\n"
                                + response.body()
                );
            }

            if (response.body() == null ||
                    response.body().isBlank()) {

                throw new IOException(
                        "سيرفر التقرير المالي أعاد استجابة فارغة."
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    AdminFinancialSummary.class
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "تمت مقاطعة الاتصال بخدمة التقرير المالي.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "فشل حساب التقرير المالي.",
                    e
            );
        }
    }

// =====================================================
// التحقق من التواريخ
// =====================================================

    private void validateDates(
            LocalDate start,
            LocalDate end
    ) {

        if (start == null) {

            throw new IllegalArgumentException(
                    "تاريخ البداية مطلوب."
            );
        }

        if (end == null) {

            throw new IllegalArgumentException(
                    "تاريخ النهاية مطلوب."
            );
        }

        if (start.isAfter(end)) {

            throw new IllegalArgumentException(
                    "تاريخ البداية لا يمكن أن يكون بعد تاريخ النهاية."
            );
        }
    }

// =====================================================
// Request
// =====================================================

    private record ClientRequest(
            LocalDate start,
            LocalDate end
    ) {
    }
}
