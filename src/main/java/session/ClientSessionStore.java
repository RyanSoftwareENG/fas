package session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class ClientSessionStore {

    private static final Path SESSION_FILE =
            Paths.get(
                    System.getenv("PROGRAMDATA"),
                    "FAS",
                    "session.dat"
            );

    private static final ObjectMapper OBJECT_MAPPER =
            createObjectMapper();

    private ClientSessionStore() {
    }

// =====================================================
// حفظ الجلسة
// =====================================================

    public static synchronized void save(
            ClientSession session
    ) throws IOException {

        if (session == null) {

            throw new IllegalArgumentException(
                    "بيانات الجلسة مطلوبة."
            );
        }

        if (!session.hasToken()) {

            throw new IllegalArgumentException(
                    "لا يمكن حفظ جلسة بدون Token."
            );
        }

        Path parent =
                SESSION_FILE.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        String json =
                OBJECT_MAPPER.writeValueAsString(
                        session
                );

        Files.writeString(
                SESSION_FILE,
                json,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

// =====================================================
// قراءة الجلسة
// =====================================================

    public static synchronized ClientSession load()
            throws IOException {

        if (!exists()) {
            return null;
        }

        String json =
                Files.readString(
                        SESSION_FILE,
                        StandardCharsets.UTF_8
                );

        if (json == null ||
                json.isBlank()) {

            return null;
        }

        return OBJECT_MAPPER.readValue(
                json,
                ClientSession.class
        );
    }

// =====================================================
// هل توجد جلسة محلية؟
// =====================================================

    public static boolean exists() {

        return Files.exists(
                SESSION_FILE
        );
    }

// =====================================================
// حذف الجلسة
// =====================================================

    public static synchronized void clear()
            throws IOException {

        Files.deleteIfExists(
                SESSION_FILE
        );
    }

// =====================================================
// مسار الملف
// =====================================================

    public static Path getFilePath() {

        return SESSION_FILE;
    }

// =====================================================
// إنشاء ObjectMapper
// =====================================================

    private static ObjectMapper createObjectMapper() {

        ObjectMapper mapper =
                new ObjectMapper();

        mapper.registerModule(
                new JavaTimeModule()
        );

        return mapper;
    }
}
