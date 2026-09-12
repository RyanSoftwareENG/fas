package user_interface.offer;

import api.ClientApiManager;
import dto.SessionListDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner; // 👈 إضافة الاستيراد
import api.SessionAPI;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SessionListController implements Initializable {

    @FXML
    private ScrollPane session;

    @FXML
    private VBox sessionlist;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> comboSort;

    @FXML
    private ToggleButton btnReverse;

    private List<SessionListDTO> sessionListDTOs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSortComboBox();
        setupListeners(); // تفعيل المستمعين لكل عناصر الفلترة والترتيب مبكراً[cite: 11]
        loadAllSessions();
    }

    // تهيئة خيارات القائمة المنسدلة للفرز[cite: 11]
    private void initSortComboBox() {
        comboSort.setItems(FXCollections.observableArrayList("الرقم", "آخر إضافة", "آخر تعديل"));
        comboSort.setValue("آخر إضافة"); // الخيار الافتراضي[cite: 11]
    }

    private void loadAllSessions() {
        // إضافة مؤشر تحميل بصري للمستخدم أثناء جلب البيانات في الخلفية
        Label loadingLabel = new Label("جاري تحميل قائمة الجلسات... ⏳");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 30px;");
        loadingLabel.setAlignment(Pos.CENTER);
        sessionlist.getChildren().setAll(loadingLabel);

        // تنفيذ جلب البيانات في الخلفية لمنع تجميد الواجهة
        BackgroundRunner.run("جاري تحديث الجلسات... ⏳", () -> {
            try {
                sessionListDTOs =  ClientApiManager.getInstance().getSessionAPI().getAllSessions();
            } catch (Exception e) {
                System.err.println("خطأ أثناء جلب الجلسات: " + e.getMessage());
            }
            return true;
        }, () -> {
            // بمجرد انتهاء الجلب، يتم تطبيق الفلاتر وعرض النتائج على واجهة المستخدم[cite: 11]
            applyFiltersAndSort();
        });
    }

    public void refreshList() {
        // تصفير حقول البحث والترتيب قبل التحديث[cite: 11]
        if (searchField != null) searchField.clear();
        comboSort.setValue("الرقم");
        btnReverse.setSelected(false);

        // إعادة جلب الجلسات
        loadAllSessions();
    }

    // إعداد مستمعين لكافة عناصر التحكم بربطهم بالدالة المركزية[cite: 11]
    private void setupListeners() {
        // 1. مستمع حقل البحث[cite: 11]
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());

        // 2. مستمع تغيير نوع الفرز[cite: 11]
        comboSort.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());

        // 3. مستمع تغيير اتجاه الترتيب (عكس الترتيب)[cite: 11]
        btnReverse.selectedProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    // الدالة المركزية لمعالجة البيانات (بحث + فرز + عكس) ثم عرضها[cite: 11]
    private void applyFiltersAndSort() {
        if (sessionListDTOs == null) return;

        String searchText = searchField.getText();
        String sortBy = comboSort.getValue();
        boolean isReverse = btnReverse.isSelected();

        // المرحلة الأولى: الفلترة والبحث[cite: 11]
        List<SessionListDTO> processedList = sessionListDTOs.stream()
                .filter(sessionObj -> {
                    if (searchText == null || searchText.trim().isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = searchText.toLowerCase();

                    // البحث بالرقم[cite: 11]
                    return String.valueOf(sessionObj.getId()).contains(lowerCaseFilter)||String.valueOf(sessionObj.getId()).contains(lowerCaseFilter);
                })
                .collect(Collectors.toList());

        // المرحلة الثانية: الفرز والترتيب (Sorting)[cite: 11]
        if (sortBy != null) {
            Comparator<SessionListDTO> comparator = null;

            switch (sortBy) {
                case "الرقم":
                    comparator = Comparator.comparing(SessionListDTO::getId);
                    break;
                case "آخر إضافة":
                    comparator = Comparator.comparing(SessionListDTO::getUploadTime, Comparator.nullsLast(Comparator.naturalOrder()));
                    break;
//                case "آخر تعديل":
//                    comparator = Comparator.comparing(Session::get, Comparator.nullsLast(Comparator.naturalOrder()));
//                    break;
            }

            if (comparator != null) {
                // إذا تم تفعيل زر "عكس الترتيب" نقوم بقلب المقارنة[cite: 11]
                if (isReverse) {
                    comparator = comparator.reversed();
                }
                processedList.sort(comparator);
            }
        }

        // المرحلة الثالثة: عرض القائمة النهائية المفلترة والمُرتبة[cite: 11]
        renderSessions(processedList);
    }

    private void renderSessions(List<SessionListDTO> sessionsToRender) {
        sessionlist.getChildren().clear();

        if (sessionsToRender != null && !sessionsToRender.isEmpty()) {
            for (SessionListDTO sessionObj : sessionsToRender) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SessionsCard.fxml"));
                    Parent card = loader.load();

                    SessionsCardController controller = loader.getController();
                    controller.setSessionData(sessionObj, this);

                    sessionlist.getChildren().add(card);
                } catch (IOException e) {
                    System.err.println("فشل تحميل الكرت: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            // رسالة بديلة في حال عدم العثور على جلسات مطابقة
            Label emptyLabel = new Label("لا توجد جلسات مطابقة للبحث.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 30px;");
            sessionlist.getChildren().add(emptyLabel);
        }
    }
}