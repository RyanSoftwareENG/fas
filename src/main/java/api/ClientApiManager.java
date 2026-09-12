package api;

public final class ClientApiManager {

    private static volatile ClientApiManager instance;

    private final ApiClient apiClient;

    private final ClientAPI clientAPI;
    private final FoodAPI foodAPI;
    private final PatientClientAPI patientClientAPI;
    private final SessionAPI sessionAPI;
    private final SessionReportAPI sessionReportAPI;
    private final DeviceAPI deviceAPI;
    private final AuthAPI authAPI;
    private final SubscriptionRequestAPI subscriptionRequestAPI;
    private final SubscriptionAPI subscriptionAPI;
    private final SubscriptionPlanAPI subscriptionPlanAPI;

    private ClientApiManager() {

        this.apiClient =
                new ApiClient();

        this.clientAPI =
                new ClientAPI(
                        apiClient
                );

        this.foodAPI =
                new FoodAPI(
                        apiClient
                );

        this.patientClientAPI =
                new PatientClientAPI(
                        apiClient
                );

        this.sessionAPI =
                new SessionAPI(
                        apiClient
                );

        this.sessionReportAPI =
                new SessionReportAPI(
                        apiClient
                );

        this.deviceAPI =
                new DeviceAPI(
                        apiClient
                );

        this.authAPI =
                new AuthAPI(
                        apiClient
                );

        this.subscriptionRequestAPI =
                new SubscriptionRequestAPI(
                        apiClient
                );

        this.subscriptionAPI =
                new SubscriptionAPI(
                        apiClient
                );
        this.subscriptionPlanAPI =
                new SubscriptionPlanAPI(
                        apiClient
                );
    }

    public static ClientApiManager getInstance() {

        ClientApiManager result =
                instance;

        if (result == null) {

            synchronized (
                    ClientApiManager.class
            ) {

                result =
                        instance;

                if (result == null) {

                    result =
                            new ClientApiManager();

                    instance =
                            result;
                }
            }
        }

        return result;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public ClientAPI getClientAPI() {
        return clientAPI;
    }

    public FoodAPI getFoodAPI() {
        return foodAPI;
    }

    public PatientClientAPI getPatientClientAPI() {
        return patientClientAPI;
    }

    public SessionAPI getSessionAPI() {
        return sessionAPI;
    }

    public SessionReportAPI getSessionReportAPI() {
        return sessionReportAPI;
    }

    public DeviceAPI getDeviceAPI() {
        return deviceAPI;
    }

    public void setToken(
            String token
    ) {

        apiClient.setToken(
                token
        );
    }

    public void clearToken() {

        apiClient.clearToken();
    }

    public boolean hasToken() {

        return apiClient.hasToken();
    }

    public AuthAPI getAuthAPI() {
        return authAPI;
    }

    public SubscriptionRequestAPI
    getSubscriptionRequestAPI() {

        return subscriptionRequestAPI;
    }
    public SubscriptionAPI getSubscriptionAPI() {

        return subscriptionAPI;
    }
    public SubscriptionPlanAPI getSubscriptionPlanAPI() {

        return subscriptionPlanAPI;
    }
}
