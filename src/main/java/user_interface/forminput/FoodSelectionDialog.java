package user_interface.forminput;

import entities.FoodItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;

public class FoodSelectionDialog
        extends Dialog<FoodItem> {

    // =========================================================
    // Constructor
    // =========================================================

    public FoodSelectionDialog(
            List<FoodItem> foodItems
    ) {

        setTitle(
                "اختيار صنف غذائي"
        );

        setHeaderText(
                "ابحث واختر الصنف الغذائي المناسب"
        );

        // =====================================================
        // RTL
        // =====================================================

        getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        getDialogPane()
                .getStyleClass()
                .add(
                        "fas-food-dialog"
                );

        // =====================================================
        // Table
        // =====================================================

        TableView<FoodItem> table =
                createTable(
                        foodItems
                );

        // =====================================================
        // Search
        // =====================================================

        TextField searchField =
                createSearchField();

        ObservableList<FoodItem> masterData =
                FXCollections.observableArrayList(
                        foodItems == null
                                ? List.of()
                                : foodItems
                );

        FilteredList<FoodItem> filteredData =
                new FilteredList<>(
                        masterData,
                        food -> true
                );

        configureSearch(
                searchField,
                filteredData
        );

        SortedList<FoodItem> sortedData =
                new SortedList<>(
                        filteredData
                );

        sortedData
                .comparatorProperty()
                .bind(
                        table.comparatorProperty()
                );

        table.setItems(
                sortedData
        );

        // =====================================================
        // Search / result counter
        // =====================================================

        Label resultCount =
                new Label(
                        "عدد الأصناف: "
                                + masterData.size()
                );

        resultCount.getStyleClass().add(
                "fas-food-result-count"
        );

        filteredData.addListener(
                (javafx.collections.ListChangeListener<FoodItem>)
                        change -> {

                            resultCount.setText(
                                    "عدد الأصناف: "
                                            + filteredData.size()
                            );
                        }
        );

        // =====================================================
        // Search header
        // =====================================================

        HBox searchBar =
                new HBox(10);

        searchBar.setAlignment(
                Pos.CENTER_RIGHT
        );

        HBox.setHgrow(
                searchField,
                Priority.ALWAYS
        );

        searchBar.getChildren().addAll(
                searchField,
                resultCount
        );

        // =====================================================
        // Content
        // =====================================================

        VBox content =
                new VBox(12);

        content.setPadding(
                new Insets(12)
        );

        content.setFillWidth(
                true
        );

        content.getStyleClass().add(
                "fas-food-dialog-content"
        );

        content.getChildren().addAll(
                searchBar,
                table
        );

        VBox.setVgrow(
                table,
                Priority.ALWAYS
        );

        getDialogPane().setContent(
                content
        );

        // =====================================================
        // Buttons
        // =====================================================

        ButtonType selectButtonType =
                new ButtonType(
                        "اختيار",
                        ButtonBar.ButtonData.OK_DONE
                );

        getDialogPane()
                .getButtonTypes()
                .addAll(
                        selectButtonType,
                        ButtonType.CANCEL
                );

        Button selectButton =
                (Button)
                        getDialogPane()
                                .lookupButton(
                                        selectButtonType
                                );

        selectButton.getStyleClass().add(
                "fas-primary-button"
        );

        Button cancelButton =
                (Button)
                        getDialogPane()
                                .lookupButton(
                                        ButtonType.CANCEL
                                );

        cancelButton.getStyleClass().add(
                "fas-secondary-button"
        );

        /*
         * لا تسمح بالاختيار بدون تحديد صنف.
         */
        selectButton.setDisable(
                true
        );

        table.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                selectButton
                                        .setDisable(
                                                newValue == null
                                        )
                );

        // =====================================================
        // Result converter
        // =====================================================

        setResultConverter(
                dialogButton -> {

                    if (dialogButton
                            == selectButtonType) {

                        return table
                                .getSelectionModel()
                                .getSelectedItem();
                    }

                    return null;
                }
        );

        // =====================================================
        // Double click
        // =====================================================

        table.setOnMouseClicked(
                event -> {

                    if (event.getClickCount() == 2) {

                        FoodItem selected =
                                table
                                        .getSelectionModel()
                                        .getSelectedItem();

                        if (selected != null) {

                            setResult(
                                    selected
                            );

                            close();
                        }
                    }
                }
        );

        // =====================================================
        // Keyboard
        // =====================================================

        table.setOnKeyPressed(
                event -> {

                    if (event.getCode()
                            == KeyCode.ENTER) {

                        FoodItem selected =
                                table
                                        .getSelectionModel()
                                        .getSelectedItem();

                        if (selected != null) {

                            setResult(
                                    selected
                            );

                            close();

                            event.consume();
                        }
                    }
                }
        );

        searchField
                .setOnKeyPressed(
                        event -> {

                            if (event.getCode()
                                    == KeyCode.ENTER) {

                                table.requestFocus();

                                if (!table
                                        .getItems()
                                        .isEmpty()) {

                                    table.getSelectionModel()
                                            .selectFirst();
                                }

                                event.consume();
                            }
                        }
                );

        // =====================================================
        // Tooltips
        // =====================================================

        Tooltip searchTooltip =
                new Tooltip(
                        "ابحث باسم الصنف الغذائي"
                );

        searchTooltip.setShowDelay(
                Duration.millis(300)
        );

        searchField.setTooltip(
                searchTooltip
        );
    }

    // =========================================================
    // Create table
    // =========================================================

    private TableView<FoodItem> createTable(
            List<FoodItem> foodItems
    ) {

        TableView<FoodItem> table =
                new TableView<>();

        table.setPrefWidth(
                760
        );

        table.setPrefHeight(
                420
        );

        table.setMinHeight(
                300
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setPlaceholder(
                new Label(
                        "لا توجد أصناف غذائية."
                )
        );

        table.getStyleClass().add(
                "fas-food-table"
        );

        // =====================================================
        // Name
        // =====================================================

        TableColumn<FoodItem, String> nameCol =
                new TableColumn<>(
                        "الاسم"
                );

        nameCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "foodName"
                )
        );

        nameCol.setMinWidth(
                180
        );

        // =====================================================
        // Calories
        // =====================================================

        TableColumn<FoodItem, Float> calCol =
                new TableColumn<>(
                        "السعرات"
                );

        calCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "calories"
                )
        );

        calCol.setMinWidth(
                90
        );

        // =====================================================
        // Carbs
        // =====================================================

        TableColumn<FoodItem, Float> carbCol =
                new TableColumn<>(
                        "الكارب (g)"
                );

        carbCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "carbohydrates"
                )
        );

        carbCol.setMinWidth(
                95
        );

        // =====================================================
        // Fat
        // =====================================================

        TableColumn<FoodItem, Float> fatCol =
                new TableColumn<>(
                        "الدهون (g)"
                );

        fatCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "fat"
                )
        );

        fatCol.setMinWidth(
                95
        );

        // =====================================================
        // Protein
        // =====================================================

        TableColumn<FoodItem, Float> proteinCol =
                new TableColumn<>(
                        "البروتين (g)"
                );

        proteinCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "protein"
                )
        );

        proteinCol.setMinWidth(
                105
        );

        // =====================================================
        // Cost
        // =====================================================

        TableColumn<FoodItem, String> costCol =
                new TableColumn<>(
                        "مستوى التكلفة"
                );

        costCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "costLevel"
                )
        );

        costCol.setMinWidth(
                115
        );

        table.getColumns().addAll(
                nameCol,
                calCol,
                carbCol,
                fatCol,
                proteinCol,
                costCol
        );

        return table;
    }

    // =========================================================
    // Search field
    // =========================================================

    private TextField createSearchField() {

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "🔍 ابحث باسم الصنف الغذائي..."
        );

        searchField.setPrefHeight(
                40
        );

        searchField.setMinHeight(
                40
        );

        searchField.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        searchField.getStyleClass().add(
                "fas-food-search"
        );

        /*
         * منع الأحرف التحكمية + حد أقصى معقول.
         */
        searchField.setTextFormatter(
                new TextFormatter<>(
                        change -> {

                            String text =
                                    change.getControlNewText();

                            if (text.length() > 100) {
                                return null;
                            }

                            String inserted =
                                    change.getText();

                            if (inserted == null
                                    || inserted.isEmpty()) {

                                return change;
                            }

                            for (char c :
                                    inserted.toCharArray()) {

                                if (Character.isISOControl(c)
                                        && c != '\b') {

                                    return null;
                                }
                            }

                            return change;
                        }
                )
        );

        return searchField;
    }

    // =========================================================
    // Search
    // =========================================================

    private void configureSearch(
            TextField searchField,
            FilteredList<FoodItem> filteredData
    ) {

        searchField.textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            final String query =
                                    newValue == null
                                            ? ""
                                            : newValue
                                            .trim()
                                            .toLowerCase(
                                                    Locale.ROOT
                                            );

                            filteredData.setPredicate(
                                    food -> {

                                        if (query.isEmpty()) {
                                            return true;
                                        }

                                        if (food == null) {
                                            return false;
                                        }

                                        String foodName =
                                                food.getFoodName();

                                        return foodName != null
                                                &&
                                                foodName
                                                        .toLowerCase(
                                                                Locale.ROOT
                                                        )
                                                        .contains(
                                                                query
                                                        );
                                    }
                            );
                        }
                );
    }
}