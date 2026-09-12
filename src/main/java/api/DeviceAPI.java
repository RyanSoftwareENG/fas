package api;

import dto.DeviceRegistrationRequest;
import dto.DeviceRegistrationResponse;

import java.io.IOException;

public class DeviceAPI {

    private final ApiClient apiClient;

    public DeviceAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

    public DeviceRegistrationResponse register(
            DeviceRegistrationRequest request
    ) throws IOException, InterruptedException {

        if (request == null) {
            throw new IllegalArgumentException(
                    "بيانات تسجيل الجهاز مطلوبة."
            );
        }

        return apiClient.post(
                ApiEndpoints.DEVICE_REGISTER,
                request,
                DeviceRegistrationResponse.class
        );
    }
}