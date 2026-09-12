package api;

import entities.FoodItem;

import java.io.IOException;
import java.util.List;

public class FoodAPI {

    private final ApiClient apiClient;

    public FoodAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

// =====================================================
// إضافة طعام
// POST /api/food
// =====================================================

    public boolean save(
            FoodItem item
    ) throws IOException, InterruptedException {

        if (item == null) {

            throw new IllegalArgumentException(
                    "بيانات الطعام مطلوبة."
            );
        }

        apiClient.postAuthenticated(
                ApiEndpoints.FOOD,
                item,
                Void.class
        );

        return true;
    }

// =====================================================
// جلب جميع الأطعمة
// GET /api/food
// =====================================================

    public List<FoodItem> getAllFood()
            throws IOException, InterruptedException {

        FoodItem[] food =
                apiClient.getAuthenticated(
                        ApiEndpoints.FOOD,
                        FoodItem[].class
                );

        return food == null
                ? List.of()
                : List.of(food);
    }

// =====================================================
// جلب طعام واحد
// GET /api/food/{id}
// =====================================================

    public FoodItem getFoodById(
            Long id
    ) throws IOException, InterruptedException {

        if (id == null ||
                id <= 0) {

            throw new IllegalArgumentException(
                    "معرف الطعام غير صالح."
            );
        }

        return apiClient.getAuthenticated(
                ApiEndpoints.foodById(id),
                FoodItem.class
        );
    }

// =====================================================
// تحديث طعام
// PUT /api/food/{id}
// =====================================================

    public boolean updateFood(
            FoodItem item
    ) throws IOException, InterruptedException {

        if (item == null) {

            throw new IllegalArgumentException(
                    "بيانات الطعام مطلوبة."
            );
        }

        if (item.getFoodItemId() == null ||
                item.getFoodItemId() <= 0) {

            throw new IllegalArgumentException(
                    "معرف الطعام غير صالح."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.foodById(
                        item.getFoodItemId()
                ),
                item,
                Void.class
        );

        return true;
    }

// =====================================================
// حذف طعام
// DELETE /api/food/{id}
// =====================================================

    public boolean deleteFood(
            Long id
    ) throws IOException, InterruptedException {

        if (id == null ||
                id <= 0) {

            throw new IllegalArgumentException(
                    "معرف الطعام غير صالح."
            );
        }

        apiClient.delete(
                ApiEndpoints.foodById(id)
        );

        return true;
    }
}
