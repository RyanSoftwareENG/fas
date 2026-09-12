package user_interface.offer;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ExmintionEditController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button backEmintion;

    @FXML
    private Button deleteEmintion;

    @FXML
    private TextField nameExmintion;

    @FXML
    private TextArea noteExmintion;

    @FXML
    private Button editImageExamintion;

    @FXML
    private ImageView imageExamintion;

    @FXML
    void handleBackEmintion(ActionEvent event) {

    }
    private String pathTextField;
    private String imagePath = "";
    @FXML
    void handleDeleteEmintion(ActionEvent event) {
// 1. إنشاء كائن FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر ملف الصورة");

        // 2. تحديد أنواع الصور المسموح بها فقط
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // 3. الحصول على النافذة الحالية لفتح نافذة الملفات فوقها
        // قمنا بجلب الـ Stage من الزر الذي ضغط عليه المستخدم
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        // 4. التحقق من أن المستخدم اختار ملفاً
        if (selectedFile != null) {

            // الحصول على المسار الكامل للصورة وحفظه في المتغير
            imagePath = selectedFile.getAbsolutePath();

            // (اختياري) عرض المسار في الحقل النصي ليراه المستخدم
            if (pathTextField != null) {
                pathTextField = imagePath;
            }

            // طباعة المسار في الـ Console للتأكد
            System.out.println("تم حفظ رابط الصورة بنجاح: " + imagePath);

        } else {
            System.out.println("تم إلغاء عملية اختيار الصورة.");
        }
    }


    @FXML
    void initialize() {
        assert backEmintion != null : "fx:id=\"backEmintion\" was not injected: check your FXML file 'ExmintionEdit.fxml'.";
        assert deleteEmintion != null : "fx:id=\"deleteEmintion\" was not injected: check your FXML file 'ExmintionEdit.fxml'.";
        assert nameExmintion != null : "fx:id=\"nameExmintion\" was not injected: check your FXML file 'ExmintionEdit.fxml'.";
        assert noteExmintion != null : "fx:id=\"noteExmintion\" was not injected: check your FXML file 'ExmintionEdit.fxml'.";

    }

}
