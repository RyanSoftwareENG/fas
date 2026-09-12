package user_interface.offer;

import api.ClientApiManager;
import entities.Client;
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
import runner.BackgroundRunner;
import api.ClientAPI;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClintList implements Initializable {

    @FXML private ScrollPane clients;
    @FXML private VBox clientList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboSort;
    @FXML private ToggleButton btnReverse;

    private List<Client> allClients;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSortComboBox();
        loadAllClients();
        setupListeners();
    }

    private void initSortComboBox() {
        comboSort.setItems(FXCollections.observableArrayList("الرقم", "الاسم", "آخر إضافة", "آخر تعديل"));
        comboSort.setValue("آخر إضافة");
    }

    /**
     * تحميل العملاء من قاعدة البيانات في الخلفية بشكل متوافق 100% مع الـ BackgroundRunner
     */
    private void loadAllClients() {
        // مصفوفة مؤقتة لنقل البيانات بأمان بين الخيوط
        final List<Client>[] resultHolder = new List[1];

        BackgroundRunner.run("جاري تحميل قائمة العملاء... ⏳", () -> {
            // جلب البيانات في الخلفية وتخزينها في المصفوفة
            resultHolder[0] = ClientApiManager.getInstance().getClientAPI().allClients();
            return true;
        }, () -> {
            // تحديث الواجهة بأمان (دالة بدون معاملات لتجنب الخطأ)
            if (resultHolder[0] != null) {
                allClients = resultHolder[0];
                applyFiltersAndSort();
            }
        });
    }

    /**
     * تحديث القائمة بالكامل في الخلفية بشكل متوافق 100%
     */
    public void refreshList() {
        final List<Client>[] resultHolder = new List[1];

        BackgroundRunner.run("جاري تحديث البيانات... 🔄", () -> {
            resultHolder[0] = ClientApiManager.getInstance().getClientAPI().allClients();
            return true;
        }, () -> {
            if (resultHolder[0] != null) {
                allClients = resultHolder[0];

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
        if (allClients == null) return;

        String searchText = searchField.getText();
        String sortBy = comboSort.getValue();
        boolean isReverse = btnReverse.isSelected();

        // 1. الفلترة
        List<Client> processedList = allClients.stream()
                .filter(clientObj -> {
                    if (searchText == null || searchText.trim().isEmpty()) return true;
                    String lowerCaseFilter = searchText.toLowerCase();

                    boolean matchId = String.valueOf(clientObj.getClientID()).contains(lowerCaseFilter);
                    boolean matchName = clientObj.getFullName() != null && clientObj.getFullName().toLowerCase().contains(lowerCaseFilter);

                    return matchId || matchName;
                })
                .collect(Collectors.toList());

        // 2. الفرز
        if (sortBy != null) {
            Comparator<Client> comparator = null;
            switch (sortBy) {
                case "الرقم":
                    comparator = Comparator.comparing(Client::getClientID);
                    break;
                case "الاسم":
                    comparator = Comparator.comparing(Client::getFullName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                    break;
                case "آخر إضافة":
                    comparator = Comparator.comparing(Client::getUploadDate, Comparator.nullsLast(Comparator.reverseOrder()));
                    break;
                case "آخر تعديل":
                    comparator = Comparator.comparing(Client::getModificationDate, Comparator.nullsLast(Comparator.reverseOrder()));
                    break;
            }

            if (comparator != null) {
                if (isReverse) {
                    comparator = comparator.reversed();
                }
                processedList.sort(comparator);
            }
        }

        // 3. العرض
        renderClients(processedList);
    }

    public javafx.scene.control.ScrollPane getClientsScrollPane() {
        return this.clients;
    }

    private void renderClients(List<Client> clientsToRender) {
        clientList.getChildren().clear();

        if (clientsToRender != null && !clientsToRender.isEmpty()) {
            for (Client client : clientsToRender) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientCard.fxml"));
                    Parent card = loader.load();

                    ClintCard cardController = loader.getController();
                    cardController.setClientData(client, this);

                    clientList.getChildren().add(card);
                } catch (IOException e) {
                    System.err.println("خطأ أثناء تحميل بطاقة العميل: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}