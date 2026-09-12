package user_interface.forminput;

import api.ClientApiManager;
import entities.FoodItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import api.FoodAPI;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import static user_interface.forminput.Desgin.*;

public class MealsView extends VBox {

    private TextField nameField = new TextField();
    private TextField caloriesField = new TextField();
    private TextField proteinField = new TextField();
    private TextField carbsField = new TextField();
    private TextField fatField = new TextField();

    private CheckBox availabilityBox = new CheckBox("متوفر حالياً");
    private ComboBox<String> costLevelCombo = new ComboBox<>();
    private Button saveButton = new Button("حفظ الوجبة");

    public MealsView() {
        this.setSpacing(15);
        this.setPadding(new Insets(20));

        Label header = new Label("إضافة وجبة غذائية للنظام");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        GridPane grid = createMealsGrid();

        saveButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        saveButton.prefWidthProperty().bind(this.widthProperty().multiply(0.3));
        saveButton.setOnAction(e -> handleSave());

        // بناء قائمة النقر بزر الماوس الأيمن لإعادة التعيين
        ContextMenu contextMenu = new ContextMenu();
        MenuItem refreshItem = new MenuItem("✨ إعادة تعيين حقول الوجبة");
        refreshItem.setOnAction(e -> clearFields());
        contextMenu.getItems().add(refreshItem);

        // استخدام Event Filter لضمان التقاط النقرة في أي مكان بالواجهة
        this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
                event.consume();
            } else {
                contextMenu.hide();
            }
        });

        HBox buttonWrapper = new HBox(saveButton);
        buttonWrapper.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(header, grid, buttonWrapper);
    }

    private GridPane createMealsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        grid.add(new Label("اسم الوجبة:"), 0, 0);
        grid.add(nameField, 1, 0);
        nameField.setPromptText("مثال: صدر دجاج مشوي");

        grid.add(new Label("السعرات:"), 0, 1);
        grid.add(caloriesField, 1, 1);
        caloriesField.setPromptText("0.0");

        grid.add(new Label("البروتين:"), 0, 2);
        grid.add(proteinField, 1, 2);
        proteinField.setPromptText("0.0");

        grid.add(new Label("الكربوهيدرات:"), 2, 0);
        grid.add(carbsField, 3, 0);
        carbsField.setPromptText("0.0");

        grid.add(new Label("الدهون:"), 2, 1);
        grid.add(fatField, 3, 1);
        fatField.setPromptText("0.0");

        grid.add(new Label("مستوى التكلفة:"), 2, 2);
        costLevelCombo.getItems().addAll("Low", "Medium", "High");
        costLevelCombo.setValue("Medium");
        grid.add(costLevelCombo, 3, 2);

        availabilityBox.setSelected(true);
        grid.add(availabilityBox, 1, 3);

        setupFieldStyles(grid);

        return grid;
    }

    private void setupFieldStyles(GridPane grid) {
        for (Node node : grid.getChildren()) {
            if (node instanceof TextField tf) {
                tf.setPrefWidth(150);
                if (tf != nameField) {
                    makeDecimalOnly(tf);
                }
                tf.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #bdc3c7; -fx-padding: 5;");
            } else if (node instanceof ComboBox<?> combo) {
                combo.setPrefWidth(150);
            } else if (node instanceof Label lbl) {
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13px;");
            }
        }
    }

    private void handleSave() {
        try {
            FoodItem item = saveFoodItem();

            if (item == null) {
                throw new IllegalArgumentException("بيانات ناقصة: يرجى إدخال اسم الوجبة الغذائية أولاً.");
            }

            FoodAPI saveFood =
                    ClientApiManager
                            .getInstance()
                            .getFoodAPI();
            boolean isSaved = saveFood.save(item);

            if (isSaved) {
                showAlert(Alert.AlertType.INFORMATION, "تم الحفظ بنجاح ✔", "تم حفظ وتوثيق الوجبة الجديدة بنظام العيادة.");
                clearFields();
            } else {
                throw new Exception("فشلت عملية الحفظ الفعلي في النظام، يرجى مراجعة سجل الأخطاء.");
            }

        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "تنبيه في البيانات ⚠", ex.getMessage());
        } catch (TimeoutException ex) {
            showAlert(Alert.AlertType.ERROR, "انقطاع الاتصال ⏳", ex.getMessage());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "خطأ في قاعدة البيانات 💾", "تعذر إتمام عملية الحفظ الفعلي: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "خطأ غير متوقع ❌", "حدث خطأ طارئ: " + ex.getMessage());
        }
    }

    public FoodItem saveFoodItem() {
        if (nameField.getText().trim().isEmpty()) return null;
        String costLevel = costLevelCombo.getValue();
        return new FoodItem(
                nameField.getText().trim(),
                getDecimal(caloriesField,"السعرات الحرارية"),
                getDecimal(proteinField , "البروتين"),
                getDecimal(carbsField, "الكاربوهيدرات"),
                getDecimal(fatField,"الدهون"),
                availabilityBox.isSelected(),costLevel
        );
    }

    private void clearFields() {
        nameField.clear();
        caloriesField.clear();
        proteinField.clear();
        carbsField.clear();
        fatField.clear();
        availabilityBox.setSelected(true);
        costLevelCombo.setValue("Medium");
        System.out.println("تم إعادة تعيين حقول الوجبة.");
    }
}