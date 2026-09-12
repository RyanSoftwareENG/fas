package api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ApiClient {

    // =====================================================
    // إعدادات الاتصال
    // =====================================================

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(10);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(20);

    private static final int MAX_REQUEST_BODY_SIZE =
            2 * 1024 * 1024;

    private static final int MAX_RESPONSE_BODY_SIZE =
            10 * 1024 * 1024;

    // =====================================================
    // HTTP + JSON
    // =====================================================

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    // =====================================================
    // Authentication
    // =====================================================

    private String token;

    // =====================================================
    // Constructor
    // =====================================================

    public ApiClient() {

        httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                CONNECT_TIMEOUT
                        )
                        .build();

        objectMapper =
                new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        objectMapper.configure(
                DeserializationFeature
                        .FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );
    }

    // =====================================================
    // Token
    // =====================================================

    public void setToken(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            this.token = null;

            return;
        }

        this.token =
                token.trim();
    }

    public void clearToken() {

        this.token = null;
    }

    public String getToken() {

        return token;
    }

    public boolean hasToken() {

        return token != null &&
                !token.isBlank();
    }

    // =====================================================
    // GET
    // =====================================================

    public <T> T get(
            String url,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "GET",
                        url,
                        null,
                        false
                );

        return parseResponse(
                response,
                responseType
        );
    }

    public HttpResponse<String> getRaw(
            String url
    ) throws IOException, InterruptedException {

        return send(
                "GET",
                url,
                null,
                false
        );
    }

    // =====================================================
    // GET Authenticated
    // =====================================================

    public <T> T getAuthenticated(
            String url,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "GET",
                        url,
                        null,
                        true
                );

        return parseResponse(
                response,
                responseType
        );
    }

    public HttpResponse<String> getRawAuthenticated(
            String url
    ) throws IOException, InterruptedException {

        return send(
                "GET",
                url,
                null,
                true
        );
    }

    // =====================================================
    // POST
    // =====================================================

    public <T> T post(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "POST",
                        url,
                        body,
                        false
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // POST Authenticated
    // =====================================================

    public <T> T postAuthenticated(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "POST",
                        url,
                        body,
                        true
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // POST مع Header إضافي
    // =====================================================

    public <T> T postWithHeader(
            String url,
            Object body,
            Class<T> responseType,
            String headerName,
            String headerValue
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                sendWithHeader(
                        "POST",
                        url,
                        body,
                        headerName,
                        headerValue,
                        false
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // POST مع Header إضافي + Authentication
    // =====================================================

    public <T> T postWithHeaderAuthenticated(
            String url,
            Object body,
            Class<T> responseType,
            String headerName,
            String headerValue
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                sendWithHeader(
                        "POST",
                        url,
                        body,
                        headerName,
                        headerValue,
                        true
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // PUT
    // =====================================================

    public <T> T put(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "PUT",
                        url,
                        body,
                        false
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // PUT Authenticated
    // =====================================================

    public <T> T putAuthenticated(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "PUT",
                        url,
                        body,
                        true
                );

        return parseResponse(
                response,
                responseType
        );
    }

    // =====================================================
    // DELETE Authenticated
    // =====================================================

    public void delete(
            String url
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "DELETE",
                        url,
                        null,
                        true
                );

        ensureSuccess(
                response
        );
    }

    // =====================================================
    // DELETE Public
    // =====================================================

    public void deletePublic(
            String url
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "DELETE",
                        url,
                        null,
                        false
                );

        ensureSuccess(
                response
        );
    }

    // =====================================================
    // الإرسال المركزي
    // =====================================================

    private HttpResponse<String> send(
            String method,
            String url,
            Object body,
            boolean authenticationRequired
    ) throws IOException, InterruptedException {

        validateMethod(
                method
        );

        validateUrl(
                url
        );

        if (authenticationRequired &&
                !hasToken()) {

            throw new IOException(
                    "لا يمكن تنفيذ الطلب. جلسة المستخدم غير موجودة."
            );
        }

        String json =
                serializeBody(
                        body
                );

        validateRequestSize(
                json
        );

        HttpRequest.Builder builder =
                createBuilder(
                        url
                );

        if (authenticationRequired) {

            builder.header(
                    "Authorization",
                    "Bearer " + token
            );
        }

        switch (
                method.toUpperCase()
        ) {

            case "GET":

                builder.GET();

                break;

            case "POST":

                builder.header(
                        "Content-Type",
                        "application/json"
                );

                builder.POST(
                        json.isBlank()
                                ? HttpRequest.BodyPublishers.noBody()
                                : HttpRequest.BodyPublishers.ofString(
                                json
                        )
                );

                break;

            case "PUT":

                builder.header(
                        "Content-Type",
                        "application/json"
                );

                builder.PUT(
                        json.isBlank()
                                ? HttpRequest.BodyPublishers.noBody()
                                : HttpRequest.BodyPublishers.ofString(
                                json
                        )
                );

                break;

            case "DELETE":

                builder.DELETE();

                break;

            default:

                throw new IOException(
                        "طريقة HTTP غير مدعومة: "
                                + method
                );
        }

        HttpResponse<String> response =
                httpClient.send(
                        builder.build(),
                        HttpResponse.BodyHandlers
                                .ofString()
                );

        validateResponseSize(
                response.body()
        );

        return response;
    }

    // =====================================================
    // إرسال مع Header
    // =====================================================

    private HttpResponse<String> sendWithHeader(
            String method,
            String url,
            Object body,
            String headerName,
            String headerValue,
            boolean authenticationRequired
    ) throws IOException, InterruptedException {

        validateMethod(
                method
        );

        validateUrl(
                url
        );

        if (headerName == null ||
                headerName.isBlank()) {

            throw new IOException(
                    "اسم الـ Header مطلوب."
            );
        }

        if (headerValue == null ||
                headerValue.isBlank()) {

            throw new IOException(
                    "قيمة الـ Header مطلوبة."
            );
        }

        if (authenticationRequired &&
                !hasToken()) {

            throw new IOException(
                    "لا يمكن تنفيذ الطلب. جلسة المستخدم غير موجودة."
            );
        }

        String json =
                serializeBody(
                        body
                );

        validateRequestSize(
                json
        );

        HttpRequest.Builder builder =
                createBuilder(
                        url
                );

        builder.header(
                headerName,
                headerValue.trim()
        );

        if (authenticationRequired) {

            builder.header(
                    "Authorization",
                    "Bearer " + token
            );
        }

        builder.header(
                "Content-Type",
                "application/json"
        );

        if (method.equalsIgnoreCase("POST")) {

            builder.POST(
                    json.isBlank()
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(
                            json
                    )
            );

        } else if (method.equalsIgnoreCase("PUT")) {

            builder.PUT(
                    json.isBlank()
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(
                            json
                    )
            );

        } else {

            throw new IOException(
                    "هذه الطريقة غير مدعومة مع Header إضافي."
            );
        }

        HttpResponse<String> response =
                httpClient.send(
                        builder.build(),
                        HttpResponse.BodyHandlers
                                .ofString()
                );

        validateResponseSize(
                response.body()
        );

        return response;
    }

    // =====================================================
    // بناء الطلب
    // =====================================================

    private HttpRequest.Builder createBuilder(
            String url
    ) {

        return HttpRequest.newBuilder()
                .uri(
                        URI.create(url)
                )
                .timeout(
                        REQUEST_TIMEOUT
                )
                .header(
                        "Accept",
                        "application/json"
                );
    }

    // =====================================================
    // Serialization
    // =====================================================

    private String serializeBody(
            Object body
    ) throws IOException {

        if (body == null) {

            return "";
        }

        if (body instanceof String stringBody) {

            return stringBody;
        }

        return objectMapper.writeValueAsString(
                body
        );
    }

    // =====================================================
    // Method Validation
    // =====================================================

    private void validateMethod(
            String method
    ) throws IOException {

        if (method == null ||
                method.isBlank()) {

            throw new IOException(
                    "HTTP Method مطلوب."
            );
        }

        switch (
                method.toUpperCase()
        ) {

            case "GET":
            case "POST":
            case "PUT":
            case "DELETE":
                break;

            default:

                throw new IOException(
                        "HTTP Method غير مسموح: "
                                + method
                );
        }
    }

    // =====================================================
    // URL Validation
    // =====================================================

    private void validateUrl(
            String url
    ) throws IOException {

        if (url == null ||
                url.isBlank()) {

            throw new IOException(
                    "عنوان API مطلوب."
            );
        }

        if (!url.startsWith(
                ApiEndpoints.BASE_URL
        )) {

            throw new IOException(
                    "محاولة إرسال طلب إلى عنوان غير مصرح به."
            );
        }

        try {

            URI uri =
                    URI.create(url);

            if (!"http".equalsIgnoreCase(
                    uri.getScheme()
            )) {

                throw new IOException(
                        "بروتوكول الاتصال غير مسموح."
                );
            }

        } catch (IllegalArgumentException e) {

            throw new IOException(
                    "عنوان API غير صالح.",
                    e
            );
        }
    }

    // =====================================================
    // Request Size
    // =====================================================

    private void validateRequestSize(
            String json
    ) throws IOException {

        if (json == null) {

            return;
        }

        int size =
                json.getBytes(
                        StandardCharsets.UTF_8
                ).length;

        if (size >
                MAX_REQUEST_BODY_SIZE) {

            throw new IOException(
                    "حجم البيانات المرسلة يتجاوز الحد المسموح."
            );
        }
    }

    // =====================================================
    // Response Size
    // =====================================================

    private void validateResponseSize(
            String body
    ) throws IOException {

        if (body == null) {

            return;
        }

        int size =
                body.getBytes(
                        StandardCharsets.UTF_8
                ).length;

        if (size >
                MAX_RESPONSE_BODY_SIZE) {

            throw new IOException(
                    "حجم استجابة السيرفر كبير جدًا."
            );
        }
    }

    // =====================================================
    // Parse Response
    // =====================================================

    private <T> T parseResponse(
            HttpResponse<String> response,
            Class<T> responseType
    ) throws IOException {

        ensureSuccess(
                response
        );

        if (responseType == null ||
                responseType == Void.class) {

            return null;
        }

        String body =
                response.body();

        if (body == null ||
                body.isBlank()) {

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة."
            );
        }

        return objectMapper.readValue(
                body,
                responseType
        );
    }

    // =====================================================
    // Status Validation
    // =====================================================

    private void ensureSuccess(
            HttpResponse<String> response
    ) throws IOException {

        if (response == null) {

            throw new ApiClientException(
                    0,
                    "NO_RESPONSE",
                    "السيرفر لم يُرجع استجابة."
            );
        }

        int status =
                response.statusCode();

        if (status < 200 ||
                status >= 300) {

            throw createApiClientException(
                    response
            );
        }
    }

    // =====================================================
    // إنشاء ApiClientException
    // =====================================================

    private ApiClientException createApiClientException(
            HttpResponse<String> response
    ) {

        int status =
                response.statusCode();

        String body =
                response.body();

        String code =
                null;

        String message =
                null;

        try {

            if (body != null &&
                    !body.isBlank()) {

                JsonNode root =
                        objectMapper.readTree(
                                body
                        );

                if (root.hasNonNull(
                        "code"
                )) {

                    code =
                            root.get(
                                    "code"
                            ).asText();
                }

                if (root.hasNonNull(
                        "message"
                )) {

                    message =
                            root.get(
                                    "message"
                            ).asText();
                }
            }

        } catch (Exception ignored) {
            /*
             * الاستجابة ليست JSON صالحًا.
             * نستخدم الرسالة الافتراضية حسب HTTP Status.
             */
        }

        if (message == null ||
                message.isBlank()) {

            message =
                    defaultMessage(
                            status
                    );
        }

        if (code == null ||
                code.isBlank()) {

            code =
                    defaultErrorCode(
                            status
                    );
        }

        return new ApiClientException(
                status,
                code,
                message
        );
    }

    // =====================================================
    // الرسالة الافتراضية
    // =====================================================

    private String defaultMessage(
            int status
    ) {

        return switch (status) {

            case 400 ->
                    "البيانات المرسلة غير صحيحة.";

            case 401 ->
                    "الجلسة غير صالحة أو انتهت.";

            case 403 ->
                    "تم رفض العملية.";

            case 404 ->
                    "المورد المطلوب غير موجود.";

            case 409 ->
                    "تعذر تنفيذ العملية بسبب تعارض في البيانات.";

            case 422 ->
                    "تعذر معالجة البيانات المرسلة.";

            case 429 ->
                    "تم تجاوز عدد الطلبات المسموح بها.";

            case 500 ->
                    "حدث خطأ داخلي في السيرفر.";

            case 502, 503, 504 ->
                    "السيرفر غير متاح حاليًا.";

            default ->
                    "فشل طلب API. HTTP "
                            + status;
        };
    }

    // =====================================================
    // Code الافتراضي
    // =====================================================

    private String defaultErrorCode(
            int status
    ) {

        return switch (status) {

            case 400 ->
                    "BAD_REQUEST";

            case 401 ->
                    "UNAUTHORIZED";

            case 403 ->
                    "FORBIDDEN";

            case 404 ->
                    "NOT_FOUND";

            case 409 ->
                    "CONFLICT";

            case 422 ->
                    "UNPROCESSABLE_ENTITY";

            case 429 ->
                    "TOO_MANY_REQUESTS";

            case 500 ->
                    "INTERNAL_SERVER_ERROR";

            case 502 ->
                    "BAD_GATEWAY";

            case 503 ->
                    "SERVICE_UNAVAILABLE";

            case 504 ->
                    "GATEWAY_TIMEOUT";

            default ->
                    "API_ERROR";
        };
    }

    // =====================================================
    // ObjectMapper
    // =====================================================

    public ObjectMapper getObjectMapper() {

        return objectMapper;
    }

    // =====================================================
    // HttpClient
    // =====================================================

    public HttpClient getHttpClient() {

        return httpClient;
    }

    // =====================================================
    // Login Authentication Response
    // =====================================================

    public <T> T postAuthResponse(
            String url,
            Object body,
            Class<T> responseType
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                send(
                        "POST",
                        url,
                        body,
                        false
                );

        if (responseType == null ||
                responseType == Void.class) {

            return null;
        }

        String responseBody =
                response.body();

        if (responseBody == null ||
                responseBody.isBlank()) {

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw createApiClientException(
                        response
                );
            }

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة."
            );
        }

        /*
         * Login:
         *
         * 200 = نجاح
         * 401 = فشل منطقي مع UserLoginResponse
         */
        if (response.statusCode() == 200 ||
                response.statusCode() == 401) {

            return objectMapper.readValue(
                    responseBody,
                    responseType
            );
        }

        ensureSuccess(
                response
        );

        return objectMapper.readValue(
                responseBody,
                responseType
        );
    }

    // =====================================================
    // Register Device Authentication Response
    // =====================================================

    public <T> T postWithHeaderAuthResponse(
            String url,
            Object body,
            Class<T> responseType,
            String headerName,
            String headerValue
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                sendWithHeader(
                        "POST",
                        url,
                        body,
                        headerName,
                        headerValue,
                        false
                );

        if (responseType == null ||
                responseType == Void.class) {

            return null;
        }

        String responseBody =
                response.body();

        if (responseBody == null ||
                responseBody.isBlank()) {

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw createApiClientException(
                        response
                );
            }

            throw new IOException(
                    "السيرفر أعاد استجابة فارغة."
            );
        }

        /*
         * Register Device:
         *
         * 200 = نجاح
         * 401 = فشل منطقي مع UserLoginResponse
         */
        if (response.statusCode() == 200 ||
                response.statusCode() == 401) {

            return objectMapper.readValue(
                    responseBody,
                    responseType
            );
        }

        ensureSuccess(
                response
        );

        return objectMapper.readValue(
                responseBody,
                responseType
        );
    }
}