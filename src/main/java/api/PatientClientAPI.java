package api;

import entities.Allergy;
import entities.ChronicDisease;
import entities.Client;

import java.io.IOException;
import java.util.List;

public class PatientClientAPI {

    private final ApiClient apiClient;

    public PatientClientAPI(
            ApiClient apiClient
    ) {
        this.apiClient =
                apiClient;
    }

// =====================================================
// 1. حفظ بيانات المريض كاملة
// POST /api/patient
// =====================================================

    public boolean saveNewpatientClient(
            Client patient
    ) throws IOException, InterruptedException {

        if (patient == null) {

            throw new IllegalArgumentException(
                    "بيانات المريض مطلوبة."
            );
        }

        apiClient.postAuthenticated(
                ApiEndpoints.PATIENT,
                patient,
                Void.class
        );

        return true;
    }

// =====================================================
// 2. جلب جميع الأمراض المزمنة
// GET /api/patient/diseases
// =====================================================

    public List<ChronicDisease> getAllChronicDiseases()
            throws IOException, InterruptedException {

        ChronicDisease[] diseases =
                apiClient.getAuthenticated(
                        ApiEndpoints.patientDiseases(),
                        ChronicDisease[].class
                );

        return diseases == null
                ? List.of()
                : List.of(diseases);
    }

// =====================================================
// 3. تعديل اسم مرض مزمن
// PUT /api/patient/diseases/{id}
// =====================================================

    public boolean updateDisease(
            Long id,
            String newName
    ) throws IOException, InterruptedException {

        validateId(
                id,
                "معرف المرض"
        );

        if (newName == null ||
                newName.isBlank()) {

            throw new IllegalArgumentException(
                    "اسم المرض مطلوب."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.patientDisease(id),
                newName.trim(),
                Void.class
        );

        return true;
    }

// =====================================================
// 4. جلب جميع أنواع الحساسية
// GET /api/patient/allergies
// =====================================================

    public List<Allergy> getAllAllergies()
            throws IOException, InterruptedException {

        Allergy[] allergies =
                apiClient.getAuthenticated(
                        ApiEndpoints.patientAllergies(),
                        Allergy[].class
                );

        return allergies == null
                ? List.of()
                : List.of(allergies);
    }

// =====================================================
// 5. تعديل اسم الحساسية
// PUT /api/patient/allergies/{id}
// =====================================================

    public boolean updateAllergy(
            Long id,
            String newName
    ) throws IOException, InterruptedException {

        validateId(
                id,
                "معرف الحساسية"
        );

        if (newName == null ||
                newName.isBlank()) {

            throw new IllegalArgumentException(
                    "اسم الحساسية مطلوب."
            );
        }

        apiClient.putAuthenticated(
                ApiEndpoints.patientAllergy(id),
                newName.trim(),
                Void.class
        );

        return true;
    }

// =====================================================
// التحقق من المعرف
// =====================================================

    private void validateId(
            Long id,
            String fieldName
    ) {

        if (id == null ||
                id <= 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " غير صالح."
            );
        }
    }
}
