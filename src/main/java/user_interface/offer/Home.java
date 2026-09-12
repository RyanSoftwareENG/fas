package user_interface.offer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class Home {

    private HomeView mainHomeView;

    @FXML
    private AnchorPane mainContentArea; // الحيز الممتد للواجهات الفرعية

    @FXML private Button clientManagementBtn;
    @FXML private Button mealsBtn;
    @FXML private Button sessionBtn;
    @FXML private Button diseasesAndAllergyBtn;
    @FXML private Button reportBtn;
    @FXML private Pane logo;

    public void setMainHomeView(HomeView mainHomeView) {
        this.mainHomeView = mainHomeView;
    }

    public AnchorPane getMainContentArea() {
        return mainContentArea;
    }

    @FXML
    void handleClientManagementBtn(ActionEvent event) {
        if (mainHomeView != null) {
            mainHomeView.setViewToClientList();
        }
    }

    @FXML
    void handleMealsBtn(ActionEvent event) {
        if (mainHomeView != null) {
            mainHomeView.setViewToMealsList();
        }
    }

    @FXML
    void handleSessionBtn(ActionEvent event) {
        System.out.println("تم الضغط على إدارة الجلسات");
        if (mainHomeView != null) {
            mainHomeView.setViewToSessionList();
        }
    }

    @FXML
    void handleDiseasesAndAllergyBtn(ActionEvent event) {
        System.out.println("تم الضغط على إدارة بيانات الأمراض والحساسية");
        if (mainHomeView != null) {
            mainHomeView.setViewToHealthList();
        }
    }

    @FXML
    void handleReportBtn(ActionEvent event) {

        if (mainHomeView != null) {

            mainHomeView.setViewToReport();
        }
    }
}