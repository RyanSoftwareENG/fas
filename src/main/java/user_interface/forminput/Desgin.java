package user_interface.forminput;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public class Desgin {
    // تنسيق الرسائل الظاهرة للعميل
   public static void showAlert(Alert.AlertType type, String title, String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

    }

    // تنسيق الحقول لتستقبل قيم عشرية فقط
    public static TextField makeDecimalOnly(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            // Regex يسمح بالأرقام ويسمح بنقطة عشرية واحدة فقط
            if (newText.matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        };

        textField.setTextFormatter(new TextFormatter<>(filter));
        return textField;
    }

    // استخراج القيم العشرية من الحقول
    public static BigDecimal getDecimal(TextField tf, String fieldName) {
        String text = tf.getText().trim();
        if (text.isEmpty()) {
            return BigDecimal.valueOf(0.0); // إذا كان الحقل فارغاً، نعتبره 0 بشكل طبيعي
        }
        try {
            return BigDecimal.valueOf(Float.parseFloat(text));
        } catch (NumberFormatException e) {
            // رمي خطأ مخصص باسم الحقل ليلتقطه handleSave
            throw new NumberFormatException("خطأ في حقل [" + fieldName + "]: القيمة المدخلة '" + text + "' غير صالحة، يرجى إدخال رقم صحيح أو عشري.");
        }
    }
}
