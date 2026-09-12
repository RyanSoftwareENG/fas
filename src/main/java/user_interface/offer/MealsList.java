package user_interface.offer;

import api.ClientApiManager;
import entities.FoodItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner; // 👈 إضافة الاستيراد
import api.FoodAPI;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MealsList implements Initializable {

    @FXML private ScrollPane meals;
    @FXML private VBox meal;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboSort;
    @FXML private ToggleButton btnReverse;

    private List<FoodItem> allMeals;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initSortComboBox();
        loadAllMeals();
        setupListeners();
    }

    private void initSortComboBox() {
        comboSort.setItems(FXCollections.observableArrayList("الرقم", "الاسم", "السعرات"));
        comboSort.setValue("الرقم");
    }

    /**
     * تحميل الوجبات من قاعدة البيانات في الخلفية بدون تجميد للواجهة
     */
    private void loadAllMeals() {
        // مصفوفة مؤقتة لنقل البيانات بين الخيوط بأمان
        final List<FoodItem>[] resultHolder = new List[1];

        BackgroundRunner.run("جاري تحميل قائمة الوجبات... ⏳", () -> {
            // جلب البيانات الثقيلة في خيط منفصل
            resultHolder[0] =
                    ClientApiManager
                            .getInstance()
                            .getFoodAPI()
                            .getAllFood();
            return true;
        }, () -> {
            // تحديث الواجهة بأمان بعد الاستلام (بدون معاملات)
            if (resultHolder[0] != null) {
                allMeals = resultHolder[0];
                applyFiltersAndSort();
            }
        });
    }

    /**
     * تحديث القائمة بالكامل في الخلفية بشكل آمن
     */
    public void refreshList() {
        final List<FoodItem>[] resultHolder = new List[1];

        BackgroundRunner.run("جاري تحديث البيانات... 🔄", () -> {
            resultHolder[0] =
                    ClientApiManager
                            .getInstance()
                            .getFoodAPI()
                            .getAllFood();
            return true;
        }, () -> {
            if (resultHolder[0] != null) {
                allMeals = resultHolder[0];

                // تصفير عناصر البحث والترتيب في خيط الواجهة
                if (searchField != null) searchField.clear();
                if (comboSort != null) comboSort.setValue("الرقم");
                if (btnReverse != null) btnReverse.setSelected(false);

                applyFiltersAndSort();
            }
        });
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        comboSort.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        btnReverse.selectedProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    private void applyFiltersAndSort() {
        if (allMeals == null) return;

        String searchText = searchField.getText();
        String sortBy = comboSort.getValue();
        boolean isReverse = btnReverse.isSelected();

        // 1. الفلترة الشاملة
        List<FoodItem> processedList = allMeals.stream()
                .filter(foodObj -> {
                    // إذا كان حقل البحث فارغاً، اعرض كل شيء
                    if (searchText == null || searchText.trim().isEmpty()) return true;

                    String lowerCaseFilter = searchText.toLowerCase().trim();

                    // البحث في الأرقام
                    boolean matchId = String.valueOf(foodObj.getFoodItemId()).contains(lowerCaseFilter);
                    boolean matchCalories = String.valueOf(foodObj.getCalories()).contains(lowerCaseFilter);
                    boolean matchProtein = String.valueOf(foodObj.getProtein()).contains(lowerCaseFilter);
                    boolean matchCarbs = String.valueOf(foodObj.getCarbohydrates()).contains(lowerCaseFilter);
                    boolean matchFat = String.valueOf(foodObj.getFat()).contains(lowerCaseFilter);

                    // البحث في النصوص
                    boolean matchName = foodObj.getFoodName() != null && foodObj.getFoodName().toLowerCase().contains(lowerCaseFilter);
                    boolean matchCost = foodObj.getCostLevel() != null && foodObj.getCostLevel().toLowerCase().contains(lowerCaseFilter);

                    // البحث في حالة التوفر
                    String availabilityText = foodObj.isAvailable() ? "متوفر متاح true" : "غير متوفر false";
                    boolean matchAvailability = availabilityText.contains(lowerCaseFilter);

                    // إذا تطابق أي شرط من الشروط السابقة، سيتم عرض العنصر
                    return matchId || matchName || matchCalories || matchProtein || matchCarbs || matchFat || matchCost || matchAvailability;
                })
                .collect(Collectors.toList());

        // 2. الفرز
        if (sortBy != null) {
            Comparator<FoodItem> comparator = null;
            switch (sortBy) {
                case "الرقم":
                    comparator = Comparator.comparing(FoodItem::getFoodItemId);
                    break;
                case "الاسم":
                    comparator = Comparator.comparing(FoodItem::getFoodName, String.CASE_INSENSITIVE_ORDER);
                    break;
                case "السعرات":
                    comparator = Comparator.comparing(FoodItem::getCalories);
                    break;
            }

            if (comparator != null) {
                if (isReverse) comparator = comparator.reversed();
                processedList.sort(comparator);
            }
        }

        // 3. العرض
        renderMeals(processedList);
    }

    private void renderMeals(List<FoodItem> mealsToRender) {
        meal.getChildren().clear();

        if (mealsToRender != null && !mealsToRender.isEmpty()) {
            for (FoodItem foodItem : mealsToRender) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MealCard.fxml"));
                    Parent card = loader.load();

                    MealsCard cardController = loader.getController();
                    cardController.setFoodtData(foodItem, this);

                    meal.getChildren().add(card);
                } catch (IOException e) {
                    System.err.println("خطأ أثناء تحميل بطاقة الصنف: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}