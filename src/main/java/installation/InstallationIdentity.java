package installation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public final class InstallationIdentity {

    private static final Path INSTALLATION_FILE =
            Paths.get(
                    System.getenv("PROGRAMDATA"),
                    "FAS",
                    "installation.id"
            );

    private InstallationIdentity() {
    }

    public static synchronized String getInstallationId()
            throws IOException {

        if (Files.exists(INSTALLATION_FILE)) {

            String existing =
                    Files.readString(
                            INSTALLATION_FILE,
                            StandardCharsets.UTF_8
                    ).trim();

            if (!existing.isBlank()) {
                return existing;
            }
        }

        Path parent =
                INSTALLATION_FILE.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        String installationId =
                UUID.randomUUID().toString();

        Files.writeString(
                INSTALLATION_FILE,
                installationId,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        return installationId;
    }

    public static boolean exists() {

        return Files.exists(
                INSTALLATION_FILE
        );
    }

    public static Path getFilePath() {

        return INSTALLATION_FILE;
    }

}
