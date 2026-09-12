package user_interface.offer;

import api.ClientApiManager;
import entities.Allergy;
import entities.ChronicDisease;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner;
import api.PatientClientAPI;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HealthList implements Initializable {

    @FXML private ToggleButton btnReverse;
    @FXML private ComboBox<?> comboSort;
    @FXML private ScrollPane healths;
    @FXML private VBox helthList;
    @FXML private TextField searchField;

    private PatientClientAPI manageDiseaseAllergy;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // استدعاء دالة التحميل مباشرة، وسيتم فتح الاتصال وجلب البيانات في الخلفية بأمان
        loadHealthData();
    }

    private void loadHealthData() {
        // مصفوفات مؤقتة لنقل البيانات بأمان بين خيط الخلفية وخيط الواجهة (متوافق مع نظامك 100%)
        final List<ChronicDisease>[] diseasesHolder = new List[1];
        final List<Allergy>[] allergiesHolder = new List[1];

        BackgroundRunner.run("جاري تحميل البيانات الطبية... ⏳", () -> {
            try {
                // 1. فتح الاتصال وجلب البيانات الثقيلة في الخلفية لتفادي التجميد
                if (manageDiseaseAllergy == null) {
                    manageDiseaseAllergy = ClientApiManager.getInstance().getPatientClientAPI();
                }
                diseasesHolder[0] = manageDiseaseAllergy.getAllChronicDiseases();
                allergiesHolder[0] = manageDiseaseAllergy.getAllAllergies();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }, () -> {
            // 2. تحديث الواجهة بأمان بعد انتهاء التحميل (دالة بدون معاملات لتجنب الخطأ)
            helthList.getChildren().clear();

            // عرض الأمراض المزمنة
            if (diseasesHolder[0] != null) {
                for (ChronicDisease disease : diseasesHolder[0]) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HealthCard.fxml"));
                        Node card = loader.load();

                        HealthCardController controller = loader.getController();
                        controller.setDiseaseData(disease);

                        helthList.getChildren().add(card);
                    } catch (IOException e) {
                        System.err.println("❌ خطأ في تحميل كرت المرض: " + e.getMessage());
                    }
                }
            }

            // عرض الحساسية
            if (allergiesHolder[0] != null) {
                for (Allergy allergy : allergiesHolder[0]) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HealthCard.fxml"));
                        Node card = loader.load();

                        HealthCardController controller = loader.getController();
                        controller.setAllergyData(allergy);

                        helthList.getChildren().add(card);
                    } catch (IOException e) {
                        System.err.println("❌ خطأ في تحميل كرت الحساسية: " + e.getMessage());
                    }
                }
            }
        });
    }
}