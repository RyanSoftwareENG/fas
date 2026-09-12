package user_interface.offer;

import api.ApiClient;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import runner.BackgroundRunner;

import java.io.IOException;

public class HomeView extends BorderPane {

    // =====================================================
    // API Client
    // =====================================================

    private ApiClient apiClient;

    // =====================================================
    // Main Content
    // =====================================================

    private final StackPane contentArea =
            new StackPane();

    private AnchorPane centerContentHolder;

    // =====================================================
    // Constructors
    // =====================================================

    public HomeView() {

        this(null);
    }

    public HomeView(
            ApiClient apiClient
    ) {

        this.apiClient =
                apiClient;

        initialize();
    }

    // =====================================================
    // Initialization
    // =====================================================

    private void initialize() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setCenter(
                contentArea
        );

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/Home.fxml"
                            )
                    );

            Parent homeNode =
                    loader.load();

            Home controller =
                    loader.getController();

            if (controller != null) {

                controller.setMainHomeView(
                        this
                );

                this.centerContentHolder =
                        controller.getMainContentArea();

            } else {

                System.err.println(
                        "تحذير: لم يتم العثور على Controller "
                                + "لملف Home.fxml"
                );
            }

            if (homeNode instanceof Region region) {

                region.setMaxWidth(
                        Double.MAX_VALUE
                );

                region.setMaxHeight(
                        Double.MAX_VALUE
                );
            }

            contentArea
                    .getChildren()
                    .add(homeNode);

        } catch (IOException e) {

            System.err.println(
                    "خطأ: تعذر تحميل Home.fxml"
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // ApiClient
    // =====================================================

    public ApiClient getApiClient() {

        return apiClient;
    }

    public void setApiClient(
            ApiClient apiClient
    ) {

        this.apiClient =
                apiClient;
    }

    // =====================================================
    // Client List
    // =====================================================

    public void setViewToClientList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري فتح إدارة العملاء... ⏳",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/ClientList.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () -> {

                    displayView(
                            rootHolder[0]
                    );
                }
        );
    }

    // =====================================================
    // Meals
    // =====================================================

    public void setViewToMealsList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري تحميل قائمة الوجبات... ⏳",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/Meals.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () -> {

                    displayView(
                            rootHolder[0]
                    );
                }
        );
    }

    // =====================================================
    // Sessions
    // =====================================================

    public void setViewToSessionList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري تحميل إدارة الجلسات... ⏳",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/Session.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () -> {

                    displayView(
                            rootHolder[0]
                    );
                }
        );
    }

    // =====================================================
    // Health
    // =====================================================

    public void setViewToHealthList() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري فتح واجهة الأمراض والحساسية... ⏳",

                () -> {

                    FXMLLoader loader =
                            new FXMLLoader(
                                    getClass().getResource(
                                            "/fxml/HealthList.fxml"
                                    )
                            );

                    rootHolder[0] =
                            loader.load();

                    return true;
                },

                () -> {

                    displayView(
                            rootHolder[0]
                    );
                }
        );
    }

    // =====================================================
    // Reports Dashboard
    // =====================================================

    public void setViewToReport() {

        if (centerContentHolder == null) {
            return;
        }

        final Parent[] rootHolder =
                new Parent[1];

        BackgroundRunner.run(
                "جاري فتح لوحة التقارير... ⏳",

                () -> {

                    rootHolder[0] =
                            new ReportsDashboardView();

                    return true;
                },

                () -> {

                    displayView(
                            rootHolder[0]
                    );
                }
        );
    }
    // =====================================================
    // Display View
    // =====================================================

    private void displayView(
            Parent root
    ) {

        if (root == null
                || centerContentHolder == null) {

            return;
        }

        centerContentHolder
                .getChildren()
                .clear();

        centerContentHolder
                .getChildren()
                .add(root);

        AnchorPane.setTopAnchor(
                root,
                0.0
        );

        AnchorPane.setBottomAnchor(
                root,
                0.0
        );

        AnchorPane.setLeftAnchor(
                root,
                0.0
        );

        AnchorPane.setRightAnchor(
                root,
                0.0
        );

        if (root instanceof Region region) {

            region.setMaxWidth(
                    Double.MAX_VALUE
            );

            region.setMaxHeight(
                    Double.MAX_VALUE
            );
        }
    }
}