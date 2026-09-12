package installation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class DeviceLocalData {

    private static final Path DEVICE_FILE =
            InstallationIdentity
                    .getFilePath()
                    .getParent()
                    .resolve("device.dat");

    private DeviceLocalData() {
    }

    public static void save(
            Long deviceId,
            Long clinicId,
            String status
    ) throws IOException {

        String content =
                "deviceId=" + deviceId + "\n"
                        + "clinicId=" + clinicId + "\n"
                        + "status=" + status + "\n";

        Files.writeString(
                DEVICE_FILE,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public static boolean exists() {

        return Files.exists(
                DEVICE_FILE
        );
    }

    public static String read(
            String key
    ) throws IOException {

        if (!exists()) {
            return null;
        }

        for (String line :
                Files.readAllLines(
                        DEVICE_FILE,
                        StandardCharsets.UTF_8
                )) {

            if (line.startsWith(
                    key + "="
            )) {

                return line.substring(
                        key.length() + 1
                );
            }
        }

        return null;
    }

    public static Long getDeviceId()
            throws IOException {

        String value =
                read("deviceId");

        return value == null
                ? null
                : Long.valueOf(value);
    }

    public static Long getClinicId()
            throws IOException {

        String value =
                read("clinicId");

        return value == null
                ? null
                : Long.valueOf(value);
    }

    public static String getStatus()
            throws IOException {

        return read("status");
    }
}
