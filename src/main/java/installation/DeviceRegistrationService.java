package installation;

import api.ClientApiManager;
import api.DeviceAPI;
import dto.DeviceRegistrationRequest;
import dto.DeviceRegistrationResponse;
import installation.DeviceInfo;
import installation.DeviceLocalData;
import installation.InstallationIdentity;

import java.io.IOException;

public class DeviceRegistrationService {

    private final DeviceAPI deviceAPI;

    public DeviceRegistrationService() {

        this.deviceAPI =
                ClientApiManager
                        .getInstance()
                        .getDeviceAPI();
    }

// =====================================================
// تسجيل الجهاز
// =====================================================

    public DeviceRegistrationResponse register(
            String activationCode
    ) throws IOException, InterruptedException {

        validateActivationCode(
                activationCode
        );

        String installationId =
                InstallationIdentity
                        .getInstallationId();

        String deviceName =
                DeviceInfo
                        .getDeviceName();

        DeviceRegistrationRequest request =
                new DeviceRegistrationRequest();

        request.setActivationCode(
                activationCode.trim()
        );

        request.setInstallationId(
                installationId
        );

        request.setDeviceName(
                deviceName
        );

        DeviceRegistrationResponse response =
                deviceAPI.register(
                        request
                );

        if (response == null) {

            throw new IOException(
                    "لم يستجب السيرفر ببيانات تسجيل الجهاز."
            );
        }

        saveLocalDeviceData(
                response
        );

        return response;
    }

// =====================================================
// حفظ بيانات الجهاز محليًا
// =====================================================

    private void saveLocalDeviceData(
            DeviceRegistrationResponse response
    ) throws IOException {

        if (response.getDeviceId() == null) {

            throw new IOException(
                    "السيرفر لم يُرجع معرف الجهاز."
            );
        }

        if (response.getClinicId() == null) {

            throw new IOException(
                    "السيرفر لم يُرجع معرف العيادة."
            );
        }

        String status =
                response.getStatus();

        if (status == null ||
                status.isBlank()) {

            status = "ACTIVE";
        }

        DeviceLocalData.save(
                response.getDeviceId(),
                response.getClinicId(),
                status
        );
    }

// =====================================================
// التحقق من كود التفعيل
// =====================================================

    private void validateActivationCode(
            String activationCode
    ) {

        if (activationCode == null ||
                activationCode.isBlank()) {

            throw new IllegalArgumentException(
                    "كود تفعيل الجهاز مطلوب."
            );
        }

        String code =
                activationCode.trim();

        if (code.length() < 6) {

            throw new IllegalArgumentException(
                    "كود تفعيل الجهاز غير صالح."
            );
        }
    }

// =====================================================
// هل الجهاز مسجل محليًا؟
// =====================================================

    public boolean isRegisteredLocally() {

        return DeviceLocalData.exists();
    }

// =====================================================
// قراءة معرف الجهاز
// =====================================================

    public Long getDeviceId()
            throws IOException {

        return DeviceLocalData.getDeviceId();
    }

// =====================================================
// قراءة معرف العيادة
// =====================================================

    public Long getClinicId()
            throws IOException {

        return DeviceLocalData.getClinicId();
    }

// =====================================================
// قراءة حالة الجهاز
// =====================================================

    public String getStatus()
            throws IOException {

        return DeviceLocalData.getStatus();
    }
}
