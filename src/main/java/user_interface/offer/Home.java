package user_interface.offer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class Home {

    private HomeView mainHomeView;

    @FXML
    private AnchorPane mainContentArea;

    @FXML
    private Button clientManagementBtn;

    @FXML
    private Button mealsBtn;

    @FXML
    private Button sessionBtn;

    @FXML
    private Button diseasesAndAllergyBtn;

    @FXML
    private Button reportBtn;

    @FXML
    private Button homeBtn;

    public void setMainHomeView(
            HomeView mainHomeView
    ) {

        this.mainHomeView =
                mainHomeView;
    }

    public AnchorPane getMainContentArea() {

        return mainContentArea;
    }

    // =====================================================
    // HOME
    // =====================================================

    @FXML
    void handleHomeBtn(
            ActionEvent event
    ) {

        if (mainHomeView != null) {

            mainHomeView.showHomeWelcome();
        }

        setActiveButton(
                homeBtn
        );
    }

    // =====================================================
    // CLIENTS
    // =====================================================

    @FXML
    void handleClientManagementBtn(
            ActionEvent event
    ) {

        if (mainHomeView != null) {

            mainHomeView.setViewToClientList();
        }

        setActiveButton(
                clientManagementBtn
        );
    }

    // =====================================================
    // MEALS
    // =====================================================

    @FXML
    void handleMealsBtn(
            ActionEvent event
    ) {

        if (mainHomeView != null) {

            mainHomeView.setViewToMealsList();
        }

        setActiveButton(
                mealsBtn
        );
    }

    // =====================================================
    // SESSIONS
    // =====================================================

    @FXML
    void handleSessionBtn(
            ActionEvent event
    ) {

        if (mainHomeView != null) {

            mainHomeView.setViewToSessionList();
        }

        setActiveButton(
                sessionBtn
        );
    }

    // =====================================================
    // HEALTH
    // =====================================================

    @FXML
    void handleDiseasesAndAllergyBtn(
            ActionEvent event
    ) {

        if (mainHomeView != null) {

            mainHomeView.setViewToHealthList();
        }

        setActiveButton(
                diseasesAndAllergyBtn
        );
    }

    // =====================================================
    // REPORTS
    // =====================================================

    @FXML
    void handleReportBtn(
            ActionEvent event
    ) {

        if (mainHomeView != null) {

            mainHomeView.setViewToReport();
        }

        setActiveButton(
                reportBtn
        );
    }

    // =====================================================
    // ACTIVE BUTTON
    // =====================================================

    private void setActiveButton(
            Button activeButton
    ) {

        Button[] buttons = {
                homeBtn,
                clientManagementBtn,
                mealsBtn,
                sessionBtn,
                diseasesAndAllergyBtn,
                reportBtn
        };

        for (Button button : buttons) {

            if (button == null) {
                continue;
            }

            button.getStyleClass().remove(
                    "fas-home-nav-active"
            );
        }

        if (activeButton != null) {

            if (!activeButton
                    .getStyleClass()
                    .contains(
                            "fas-home-nav-active"
                    )) {

                activeButton
                        .getStyleClass()
                        .add(
                                "fas-home-nav-active"
                        );
            }
        }
    }
}