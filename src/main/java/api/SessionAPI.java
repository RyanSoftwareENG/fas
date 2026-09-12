package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dto.SessionListDTO;
import entities.Session;

import java.io.IOException;
import java.util.List;

public class SessionAPI {

    private final ApiClient apiClient;

    public SessionAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

// =====================================================
// 1. حفظ جلسة
// POST /api/sessions
// =====================================================

    public boolean save(
            Session session
    ) throws IOException, InterruptedException {

        if (session == null) {

            throw new IllegalArgumentException(
                    "بيانات الجلسة مطلوبة."
            );
        }

        if (session.getClient() == null ||
                session.getClient().getClientID() == null ||
                session.getClient().getClientID() <= 0) {

            throw new IllegalArgumentException(
                    "لا يوجد Client صالح داخل الجلسة."
            );
        }

        Long clientId =
                session.getClient()
                        .getClientID();

        /*
         * Session يحتوي على علاقة Client.
         * نحافظ على السلوك السابق:
         * نضع Client ID فقط داخل JSON.
         */

        ObjectNode node =
                apiClient.getObjectMapper()
                        .valueToTree(
                                session
                        );

        ObjectNode clientNode =
                apiClient.getObjectMapper()
                        .createObjectNode();

        clientNode.put(
                "clientID",
                clientId
        );

        node.set(
                "client",
                clientNode
        );

        apiClient.postAuthenticated(
                ApiEndpoints.SESSIONS,
                node,
                Void.class
        );

        return true;
    }

// =====================================================
// 2. جلب جلسة واحدة
// GET /api/sessions/{id}
// =====================================================

    public Session getAllDataSession(
            Long id
    ) throws IOException, InterruptedException {

        validateId(
                id,
                "معرف الجلسة"
        );

        return apiClient.getAuthenticated(
                ApiEndpoints.sessionById(id),
                Session.class
        );
    }

// =====================================================
// 3. تحديث الجلسة
// PUT /api/sessions/{id}
// =====================================================

    public boolean updateSession(
            Session session
    ) throws IOException, InterruptedException {

        if (session == null) {

            throw new IllegalArgumentException(
                    "بيانات الجلسة مطلوبة."
            );
        }

        if (session.getId() == null ||
                session.getId() <= 0) {

            throw new IllegalArgumentException(
                    "معرف الجلسة غير صالح."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.sessionById(
                        session.getId()
                ),
                session,
                Void.class
        );

        return true;
    }

// =====================================================
// 4. حذف الجلسة
// DELETE /api/sessions/{id}
// =====================================================

    public boolean deleteSession(
            Long id
    ) throws IOException, InterruptedException {

        validateId(
                id,
                "معرف الجلسة"
        );

        apiClient.delete(
                ApiEndpoints.sessionById(id)
        );

        return true;
    }

// =====================================================
// 5. جلب جميع الجلسات
// GET /api/sessions
// =====================================================

    public List<SessionListDTO> getAllSessions()
            throws IOException, InterruptedException {

        SessionListDTO[] sessions =
                apiClient.getAuthenticated(
                        ApiEndpoints.SESSIONS,
                        SessionListDTO[].class
                );

        return sessions == null
                ? List.of()
                : List.of(sessions);
    }

// =====================================================
// التحقق من ID
// =====================================================

    private void validateId(
            Long id,
            String fieldName
    ) {

        if (id == null ||
                id <= 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " غير صالح."
            );
        }
    }
}
