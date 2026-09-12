package installation;

public final class DeviceInfo {

    private DeviceInfo() {
    }

    public static String getDeviceName() {

        String deviceName =
                System.getenv("COMPUTERNAME");

        if (deviceName == null ||
                deviceName.isBlank()) {

            deviceName =
                    System.getProperty(
                            "user.name"
                    );
        }

        if (deviceName == null ||
                deviceName.isBlank()) {

            return "FAS-DEVICE";
        }

        return deviceName.trim();
    }

}
