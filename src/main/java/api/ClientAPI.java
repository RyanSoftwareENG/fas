package api;

import entities.Client;
import entities.HealthData;
import entities.LifeStyleInformation;

import java.io.IOException;
import java.util.List;

public class ClientAPI {

    private final ApiClient apiClient;

    public ClientAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

// =====================================================
// 1. إنشاء / حفظ بيانات العميل كاملة
// POST /api/clients
// =====================================================

    public boolean saveFullClientData(
            Client client
    ) throws IOException, InterruptedException {

        if (client == null) {

            throw new IllegalArgumentException(
                    "بيانات العميل مطلوبة."
            );
        }

        apiClient.postAuthenticated(
                ApiEndpoints.CLIENTS,
                client,
                Void.class
        );

        return true;
    }

// =====================================================
// 2. جلب جميع العملاء
// GET /api/clients
// =====================================================

    public List<Client> allClients()
            throws IOException, InterruptedException {

        Client[] clients =
                apiClient.getAuthenticated(
                        ApiEndpoints.CLIENTS,
                        Client[].class
                );

        return clients == null
                ? List.of()
                : List.of(clients);
    }

// =====================================================
// 3. جلب عميل بواسطة ID
// GET /api/clients/{id}
// =====================================================

    public Client getClientByID(
            long id
    ) throws IOException, InterruptedException {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "معرف العميل غير صالح."
            );
        }

        return apiClient.getAuthenticated(
                ApiEndpoints.clientById(id),
                Client.class
        );
    }

// =====================================================
// 4. تحديث بيانات العميل الأساسية
// PUT /api/clients/{id}
// =====================================================

    public boolean updateClient(
            Client client
    ) throws IOException, InterruptedException {

        if (client == null) {

            throw new IllegalArgumentException(
                    "بيانات العميل مطلوبة."
            );
        }

        if (client.getClientID() == null ||
                client.getClientID() <= 0) {

            throw new IllegalArgumentException(
                    "معرف العميل غير صالح."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.clientById(
                        client.getClientID()
                ),
                client,
                Void.class
        );

        return true;
    }

// =====================================================
// 5. حذف عميل
// DELETE /api/clients/{id}
// =====================================================

    public boolean deleteClient(
            long clientId
    ) throws IOException, InterruptedException {

        if (clientId <= 0) {

            throw new IllegalArgumentException(
                    "معرف العميل غير صالح."
            );
        }

        apiClient.delete(
                ApiEndpoints.clientById(
                        clientId
                )
        );

        return true;
    }

// =====================================================
// 6. تحديث البيانات الصحية
// PUT /api/clients/{id}/health
// =====================================================

    public boolean updateHealthData(
            HealthData healthData,
            long id
    ) throws IOException, InterruptedException {

        if (healthData == null) {

            throw new IllegalArgumentException(
                    "البيانات الصحية مطلوبة."
            );
        }

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "معرف العميل غير صالح."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.clientHealth(id),
                healthData,
                Void.class
        );

        return true;
    }

// =====================================================
// 7. تحديث نمط الحياة
// PUT /api/clients/{id}/lifestyle
// =====================================================

    public boolean updateLifeStyle(
            LifeStyleInformation updatedInfo,
            long id
    ) throws IOException, InterruptedException {

        if (updatedInfo == null) {

            throw new IllegalArgumentException(
                    "بيانات نمط الحياة مطلوبة."
            );
        }

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "معرف العميل غير صالح."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.clientLifestyle(id),
                updatedInfo,
                Void.class
        );

        return true;
    }

// =====================================================
// 8. تحميل بيانات العميل الكاملة
// GET /api/clients/full/{id}
// =====================================================

    public Client populateAdditionalData(
            Client currentClient
    ) throws IOException, InterruptedException {

        if (currentClient == null ||
                currentClient.getClientID() == null ||
                currentClient.getClientID() <= 0) {

            return null;
        }

        return apiClient.getAuthenticated(
                ApiEndpoints.clientFullById(
                        currentClient.getClientID()
                ),
                Client.class
        );
    }
}
