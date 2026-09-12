package user_interface.forminput;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import entities.FoodItem;
import java.util.List;

public class FoodSelectionDialog extends Dialog<FoodItem> {

    public FoodSelectionDialog(List<FoodItem> foodItems) {
        setTitle("اختيار صنف غذائي");
        setHeaderText("ابحث ورتب حسب السعرات، الكارب، الدهون، أو التكلفة لاختيار الأنسب.");

        // إعداد الجدول
        TableView<FoodItem> table = new TableView<>();
        table.setPrefWidth(550);
        table.setPrefHeight(300);

        TableColumn<FoodItem, String> nameCol = new TableColumn<>("الاسم");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        nameCol.setPrefWidth(120);

        TableColumn<FoodItem, Float> calCol = new TableColumn<>("السعرات");
        calCol.setCellValueFactory(new PropertyValueFactory<>("calories"));

        TableColumn<FoodItem, Float> carbCol = new TableColumn<>("الكارب (g)");
        carbCol.setCellValueFactory(new PropertyValueFactory<>("carbohydrates"));

        TableColumn<FoodItem, Float> fatCol = new TableColumn<>("الدهون (g)");
        fatCol.setCellValueFactory(new PropertyValueFactory<>("fat"));

        TableColumn<FoodItem, Float> proteinCol = new TableColumn<>("البروتين (g)");
        proteinCol.setCellValueFactory(new PropertyValueFactory<>("protein"));

        TableColumn<FoodItem, String> costCol = new TableColumn<>("مستوى التكلفة");
        costCol.setCellValueFactory(new PropertyValueFactory<>("costLevel"));

        table.getColumns().addAll(nameCol, calCol, carbCol, fatCol, proteinCol, costCol);

        // إعداد ميزة البحث والفلترة
        ObservableList<FoodItem> masterData = FXCollections.observableArrayList(foodItems);
        FilteredList<FoodItem> filteredData = new FilteredList<>(masterData, p -> true);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 ابحث باسم الوجبة...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(food -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return food.getFoodName().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        // ربط الترتيب (Sorting) بالجدول للسماح بترتيب الأعلى والأقل
        SortedList<FoodItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // تخطيط النافذة
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(searchField, table);
        getDialogPane().setContent(content);

        // أزرار التحكم
        ButtonType selectButtonType = new ButtonType("اختيار", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // إرجاع الصنف المحدد عند الضغط على "اختيار" أو النقر المزدوج
        setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return table.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // دعم النقر المزدوج للاختيار السريع
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                setResult(table.getSelectionModel().getSelectedItem());
                close();
            }
        });
    }
}