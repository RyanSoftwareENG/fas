package dto;

public class DeviceRegistrationResponse {

    private boolean success;
    private String message;

    private Long deviceId;
    private Long clinicId;

    private String deviceName;
    private String installationId;
    private String status;

    public DeviceRegistrationResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getInstallationId() {
        return installationId;
    }

    public String getStatus() {
        return status;
    }
}
