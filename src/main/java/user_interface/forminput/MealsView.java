package user_interface.forminput;

import api.ClientApiManager;
import api.FoodAPI;
import entities.FoodItem;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import static user_interface.forminput.Desgin.*;

/**
 * واجهة إضافة صنف غذائي.
 *
 * الاختصارات:
 * - Ctrl + S         حفظ
 * - Ctrl + Shift + R إعادة ضبط
 * - F2              مسح الحقول
 */
public class MealsView extends VBox {

    // =========================================================
    // MAIN FIELDS
    // =========================================================

    private final TextField nameField =
            new TextField();

    private final TextField caloriesField =
            new TextField();

    private final TextField proteinField =
            new TextField();

    private final TextField carbsField =
            new TextField();

    private final TextField fatField =
            new TextField();

    private final CheckBox availabilityBox =
            new CheckBox("متوفر حالياً");

    private final ComboBox<String> costLevelCombo =
            new ComboBox<>();

    private final Button saveButton =
            new Button("حفظ الصنف الغذائي");

    private final Label statusLabel =
            new Label("جاهز");

    // =========================================================
    // ROOT
    // =========================================================

    private final VBox content =
            new VBox(16);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MealsView() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setFillWidth(true);

        getStyleClass().add(
                "fas-root"
        );

        configureGeneralUI();
        configureFields();
        configureContextMenu();
        configureShortcuts();

        VBox.setVgrow(
                content,
                Priority.ALWAYS
        );

        getChildren().addAll(
                content,
                createFooter()
        );
    }

    // =========================================================
    // GENERAL UI
    // =========================================================

    private void configureGeneralUI() {

        content.setFillWidth(true);

        content.setPadding(
                new Insets(
                        6,
                        8,
                        8,
                        8
                )
        );

        content.getChildren().add(
                createMainCard()
        );
    }

    // =========================================================
    // FIELDS
    // =========================================================

    private void configureFields() {

        nameField.setPromptText(
                "مثال: صدر دجاج مشوي"
        );

        caloriesField.setPromptText(
                "مثال: 250"
        );

        proteinField.setPromptText(
                "مثال: 30"
        );

        carbsField.setPromptText(
                "مثال: 10"
        );

        fatField.setPromptText(
                "مثال: 5"
        );

        // -----------------------------------------------------
        // Numeric protection
        // -----------------------------------------------------

        makeDecimalOnly(
                caloriesField
        );

        makeDecimalOnly(
                proteinField
        );

        makeDecimalOnly(
                carbsField
        );

        makeDecimalOnly(
                fatField
        );

        // -----------------------------------------------------
        // Cost level
        // -----------------------------------------------------

        costLevelCombo.getItems().setAll(
                "منخفض",
                "متوسط",
                "مرتفع"
        );

        costLevelCombo.setValue(
                "متوسط"
        );

        costLevelCombo.setMaxWidth(
                Double.MAX_VALUE
        );

        // -----------------------------------------------------
        // Availability
        // -----------------------------------------------------

        availabilityBox.setSelected(
                true
        );

        availabilityBox.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        // -----------------------------------------------------
        // Shared FAS input class
        // -----------------------------------------------------

        addInputStyle(
                nameField,
                caloriesField,
                proteinField,
                carbsField,
                fatField
        );
    }

    private void addInputStyle(
            Control... controls
    ) {

        for (Control control : controls) {

            control.getStyleClass().add(
                    "fas-session-input"
            );
        }
    }

    // =========================================================
    // MAIN CARD
    // =========================================================

    private VBox createMainCard() {

        VBox card =
                createCard();

        HBox header =
                createHeader();

        GridPane grid =
                createMealsGrid();

        HBox infoBox =
                createInfoBox();

        card.getChildren().addAll(
                header,
                createSeparator(),
                grid,
                createSeparator(),
                infoBox
        );

        return card;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader() {

        HBox header =
                new HBox(12);

        header.setAlignment(
                Pos.CENTER_RIGHT
        );

        VBox titleBox =
                new VBox(4);

        titleBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label title =
                new Label(
                        "إضافة صنف غذائي"
                );

        title.getStyleClass().add(
                "fas-card-title"
        );

        Label subtitle =
                new Label(
                        "أدخل المعلومات الغذائية الأساسية للصنف"
                );

        subtitle.getStyleClass().add(
                "fas-card-subtitle"
        );

        titleBox.getChildren().addAll(
                title,
                subtitle
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label badge =
                new Label(
                        "🍎 إضافة غذاء"
                );

        badge.getStyleClass().add(
                "fas-status-label"
        );

        header.getChildren().addAll(
                titleBox,
                spacer,
                badge
        );

        return header;
    }

    // =========================================================
    // FORM GRID
    // =========================================================

    private GridPane createMealsGrid() {

        GridPane grid =
                new GridPane();

        grid.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        grid.setHgap(18);
        grid.setVgap(14);

        grid.setPadding(
                new Insets(
                        4,
                        2,
                        4,
                        2
                )
        );

        configureGridColumns(
                grid
        );

        // -----------------------------------------------------
        // Row 0
        // -----------------------------------------------------

        addField(
                grid,
                0,
                0,
                "اسم الصنف",
                nameField,
                true
        );

        addField(
                grid,
                0,
                2,
                "السعرات",
                caloriesField,
                true
        );

        // -----------------------------------------------------
        // Row 1
        // -----------------------------------------------------

        addField(
                grid,
                1,
                0,
                "البروتين (g)",
                proteinField,
                true
        );

        addField(
                grid,
                1,
                2,
                "الكربوهيدرات (g)",
                carbsField,
                true
        );

        // -----------------------------------------------------
        // Row 2
        // -----------------------------------------------------

        addField(
                grid,
                2,
                0,
                "الدهون (g)",
                fatField,
                true
        );

        addField(
                grid,
                2,
                2,
                "مستوى التكلفة",
                costLevelCombo,
                true
        );

        // -----------------------------------------------------
        // Row 3
        // -----------------------------------------------------

        grid.add(
                availabilityBox,
                0,
                3,
                4,
                1
        );

        GridPane.setHalignment(
                availabilityBox,
                HPos.RIGHT
        );

        return grid;
    }

    private void configureGridColumns(
            GridPane grid
    ) {

        for (int i = 0; i < 4; i++) {

            ColumnConstraints constraint =
                    new ColumnConstraints();

            if (i % 2 == 0) {

                constraint.setMinWidth(
                        120
                );

                constraint.setPrefWidth(
                        140
                );

                constraint.setMaxWidth(
                        155
                );

                constraint.setHalignment(
                        HPos.RIGHT
                );

            } else {

                constraint.setHgrow(
                        Priority.ALWAYS
                );

                constraint.setFillWidth(
                        true
                );

                constraint.setMinWidth(
                        140
                );
            }

            grid.getColumnConstraints()
                    .add(
                            constraint
                    );
        }
    }

    // =========================================================
    // ADD FIELD
    // =========================================================

    private void addField(
            GridPane grid,
            int row,
            int startColumn,
            String labelText,
            Node node,
            boolean required
    ) {

        Label label =
                new Label(
                        required
                                ? labelText + " *"
                                : labelText
                );

        label.getStyleClass().add(
                required
                        ? "fas-field-label-required"
                        : "fas-field-label"
        );

        grid.add(
                label,
                startColumn,
                row
        );

        grid.add(
                node,
                startColumn + 1,
                row
        );

        GridPane.setHgrow(
                node,
                Priority.ALWAYS
        );

        GridPane.setFillWidth(
                node,
                true
        );

        if (node instanceof Region region) {

            region.setMaxWidth(
                    Double.MAX_VALUE
            );
        }
    }

    // =========================================================
    // INFO BOX
    // =========================================================

    private HBox createInfoBox() {

        HBox box =
                new HBox(10);

        box.setAlignment(
                Pos.CENTER_RIGHT
        );

        box.getStyleClass().add(
                "fas-inner-info"
        );

        Label icon =
                new Label("ℹ️");

        Label text =
                new Label(
                        "تأكد من إدخال القيم الغذائية بدقة قبل الحفظ."
                );

        text.getStyleClass().add(
                "fas-card-subtitle"
        );

        box.getChildren().addAll(
                icon,
                text
        );

        return box;
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void handleSave() {

        try {

            setStatus(
                    "جاري التحقق من البيانات..."
            );

            FoodItem item =
                    saveFoodItem();

            if (item == null) {

                throw new IllegalArgumentException(
                        "يرجى إدخال اسم الصنف الغذائي."
                );
            }

            FoodAPI foodAPI =
                    ClientApiManager
                            .getInstance()
                            .getFoodAPI();

            boolean saved =
                    foodAPI.save(item);

            if (!saved) {

                throw new IllegalStateException(
                        "فشلت عملية حفظ الصنف الغذائي."
                );
            }

            setStatus(
                    "تم الحفظ بنجاح"
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "تم الحفظ بنجاح",
                    "تم حفظ وتوثيق الصنف الغذائي بنجاح."
            );

            clearFields();

        } catch (IllegalArgumentException ex) {

            setStatus(
                    "بيانات غير صالحة"
            );

            showAlert(
                    Alert.AlertType.WARNING,
                    "تنبيه في البيانات",
                    ex.getMessage()
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            setStatus(
                    "حدث خطأ"
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "خطأ غير متوقع",
                    ex.getMessage() == null
                            ? "حدث خطأ غير متوقع."
                            : ex.getMessage()
            );
        }
    }

    // =========================================================
    // CREATE FOOD ITEM
    // =========================================================

    public FoodItem saveFoodItem() {

        String name =
                nameField.getText()
                        .trim();

        if (name.isEmpty()) {

            return null;
        }

        validateNumericFields();

        String costLevel =
                mapCostLevel(
                        costLevelCombo.getValue()
                );

        return new FoodItem(
                name,
                getDecimal(
                        caloriesField,
                        "السعرات الحرارية"
                ),
                getDecimal(
                        proteinField,
                        "البروتين"
                ),
                getDecimal(
                        carbsField,
                        "الكربوهيدرات"
                ),
                getDecimal(
                        fatField,
                        "الدهون"
                ),
                availabilityBox.isSelected(),
                costLevel
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateNumericFields() {

        validateNumber(
                caloriesField,
                "السعرات الحرارية"
        );

        validateNumber(
                proteinField,
                "البروتين"
        );

        validateNumber(
                carbsField,
                "الكربوهيدرات"
        );

        validateNumber(
                fatField,
                "الدهون"
        );
    }

    private void validateNumber(
            TextField field,
            String fieldName
    ) {

        String value =
                field.getText()
                        .trim();

        if (value.isEmpty()) {

            throw new IllegalArgumentException(
                    "يرجى إدخال " + fieldName + "."
            );
        }

        BigDecimalValidator.validate(
                value,
                fieldName
        );
    }

    // =========================================================
    // COST LEVEL
    // =========================================================

    private String mapCostLevel(
            String value
    ) {

        if (value == null) {

            return "Medium";
        }

        return switch (value) {

            case "منخفض" ->
                    "Low";

            case "مرتفع" ->
                    "High";

            default ->
                    "Medium";
        };
    }

    // =========================================================
    // CLEAR
    // =========================================================

    private void clearFields() {

        nameField.clear();

        caloriesField.clear();

        proteinField.clear();

        carbsField.clear();

        fatField.clear();

        availabilityBox.setSelected(
                true
        );

        costLevelCombo.setValue(
                "متوسط"
        );

        setStatus(
                "تمت إعادة ضبط الحقول"
        );

        nameField.requestFocus();
    }

    // =========================================================
    // CONTEXT MENU
    // =========================================================

    private void configureContextMenu() {

        ContextMenu contextMenu =
                new ContextMenu();

        MenuItem clearItem =
                new MenuItem(
                        "↻ إعادة تعيين حقول الصنف"
                );

        clearItem.getStyleClass().add(
                "fas-context-menu-item"
        );

        clearItem.setOnAction(
                event ->
                        clearFields()
        );

        contextMenu.getItems().add(
                clearItem
        );

        addEventFilter(
                javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                event -> {

                    if (event.getButton()
                            == MouseButton.SECONDARY) {

                        contextMenu.show(
                                this,
                                event.getScreenX(),
                                event.getScreenY()
                        );

                        event.consume();

                    } else {

                        contextMenu.hide();
                    }
                }
        );
    }

    // =========================================================
    // SHORTCUTS
    // =========================================================

    private void configureShortcuts() {

        addEventFilter(
                KeyEvent.KEY_PRESSED,
                this::handleShortcut
        );
    }

    private void handleShortcut(
            KeyEvent event
    ) {

        if (event.isControlDown()
                && event.getCode()
                == KeyCode.S) {

            handleSave();

            event.consume();

            return;
        }

        if (event.isControlDown()
                && event.isShiftDown()
                && event.getCode()
                == KeyCode.R) {

            clearFields();

            event.consume();

            return;
        }

        if (event.getCode()
                == KeyCode.F2) {

            clearFields();

            event.consume();

            return;
        }

        if (event.getCode()
                == KeyCode.ESCAPE) {

            setStatus(
                    "جاهز"
            );

            event.consume();
        }
    }

    // =========================================================
    // FOOTER
    // =========================================================

    private Node createFooter() {

        HBox footer =
                new HBox(12);

        footer.setAlignment(
                Pos.CENTER_RIGHT
        );

        footer.setPadding(
                new Insets(
                        10,
                        14,
                        10,
                        14
                )
        );

        footer.getStyleClass().add(
                "fas-footer"
        );

        statusLabel.getStyleClass().add(
                "fas-status-label"
        );

        Region statusDot =
                new Region();

        statusDot.getStyleClass().add(
                "fas-status-dot"
        );

        HBox status =
                new HBox(
                        7,
                        statusLabel,
                        statusDot
                );

        status.setAlignment(
                Pos.CENTER_RIGHT
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button clear =
                new Button(
                        "إعادة ضبط"
                );

        clear.getStyleClass().add(
                "fas-secondary-button"
        );

        clear.setMinHeight(
                40
        );

        applyTooltip(
                clear,
                "إعادة ضبط الحقول\n"
                        + "Ctrl + Shift + R"
        );

        clear.setOnAction(
                event ->
                        clearFields()
        );

        saveButton.getStyleClass().add(
                "fas-primary-button"
        );

        saveButton.setMinWidth(
                190
        );

        saveButton.setPrefHeight(
                42
        );

        saveButton.setOnAction(
                event ->
                        handleSave()
        );

        applyTooltip(
                saveButton,
                "حفظ الصنف الغذائي\n"
                        + "Ctrl + S"
        );

        Label shortcut =
                new Label(
                        "Ctrl + S"
                );

        shortcut.getStyleClass().add(
                "fas-footer-shortcut"
        );

        footer.getChildren().addAll(
                status,
                spacer,
                clear,
                shortcut,
                saveButton
        );

        return footer;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private VBox createCard() {

        VBox card =
                new VBox(14);

        card.getStyleClass().add(
                "fas-card"
        );

        card.setFillWidth(
                true
        );

        return card;
    }

    private Separator createSeparator() {

        Separator separator =
                new Separator();

        separator.getStyleClass().add(
                "fas-inner-separator"
        );

        return separator;
    }

    private void applyTooltip(
            Control control,
            String text
    ) {

        Tooltip tooltip =
                new Tooltip(text);

        tooltip.setShowDelay(
                Duration.millis(300)
        );

        tooltip.setHideDelay(
                Duration.millis(100)
        );

        control.setTooltip(
                tooltip
        );
    }

    private void setStatus(
            String text
    ) {

        statusLabel.setText(
                text
        );
    }

    // =========================================================
    // SMALL VALIDATOR
    // =========================================================

    private static final class BigDecimalValidator {

        private BigDecimalValidator() {
        }

        private static void validate(
                String value,
                String fieldName
        ) {

            try {

                new java.math.BigDecimal(
                        value
                );

            } catch (NumberFormatException ex) {

                throw new IllegalArgumentException(
                        "قيمة "
                                + fieldName
                                + " غير صحيحة."
                );
            }
        }
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        type
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message == null
                        || message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.getDialogPane()
                .getStyleClass()
                .add(
                        "fas-alert"
                );

        if (getScene() != null
                && getScene().getWindow() != null) {

            alert.initOwner(
                    getScene().getWindow()
            );
        }

        alert.showAndWait();
    }
}