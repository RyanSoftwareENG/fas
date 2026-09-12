package user_interface.navbar;

import app.Main;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import security.ClientSecurityGuard;
import session.ClientSession;
import settings.AppSettings;
import user_interface.forminput.ClientView;
import user_interface.forminput.MealsView;
import user_interface.forminput.SessionView;
import user_interface.login.LoginView;
import user_interface.offer.HomeView;
import user_interface.subscription.SubscriptionView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Navbar extends VBox {

    // =========================================================
    // Navigation buttons
    // =========================================================

    private final List<Button> navButtons =
            new ArrayList<>();

    private Button btnHome;
    private Button btnClient;
    private Button btnSession;
    private Button btnMeal;
    private Button btnSubscription;

    // =========================================================
    // Security
    // =========================================================

    private final ClientSecurityGuard securityGuard =
            ClientSecurityGuard.getInstance();

    // =========================================================
    // Constructor
    // =========================================================

    public Navbar() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setSpacing(0);

        setFillWidth(true);

        getStyleClass().add(
                "fas-navbar"
        );

        VBox topBar =
                createTopBar();

        HBox navigationBar =
                createNavigationBar();

        getChildren().addAll(
                topBar,
                navigationBar
        );

        configureShortcuts();
        configureLastView();
    }

    // =========================================================
    // Top bar
    // =========================================================

// =========================================================
// Top bar
// =========================================================

    private VBox createTopBar() {

        VBox top =
                new VBox();

        top.getStyleClass().add(
                "fas-navbar-top"
        );

        HBox content =
                new HBox(18);

        content.setAlignment(
                Pos.CENTER_RIGHT
        );

        content.setPadding(
                new Insets(
                        12,
                        18,
                        12,
                        18
                )
        );

        // -----------------------------------------------------
        // Logo
        // -----------------------------------------------------

        VBox logoBox =
                new VBox(1);

        Label logo =
                new Label("FAS");

        logo.getStyleClass().add(
                "fas-navbar-logo"
        );

        Label subtitle =
                new Label(
                        "Food Assistant System"
                );

        subtitle.getStyleClass().add(
                "fas-navbar-subtitle"
        );

        logoBox.getChildren().addAll(
                logo,
                subtitle
        );

        // -----------------------------------------------------
        // Separator
        // -----------------------------------------------------

        Region separator =
                new Region();

        separator.setPrefWidth(1);
        separator.setMinWidth(1);
        separator.setMaxWidth(1);

        separator.setPrefHeight(38);

        separator.getStyleClass().add(
                "fas-navbar-separator"
        );

        // -----------------------------------------------------
        // Clinic
        // -----------------------------------------------------

        VBox clinicBox =
                createClinicSection();

        // -----------------------------------------------------
        // Separator 2
        // -----------------------------------------------------

        Region separator2 =
                new Region();

        separator2.setPrefWidth(1);
        separator2.setMinWidth(1);
        separator2.setMaxWidth(1);

        separator2.setPrefHeight(38);

        separator2.getStyleClass().add(
                "fas-navbar-separator"
        );

        // -----------------------------------------------------
        // Current section
        // -----------------------------------------------------

        VBox sectionBox =
                new VBox(2);

        Label sectionTitle =
                new Label(
                        "نظام العيادة"
                );

        sectionTitle.getStyleClass().add(
                "fas-navbar-section-title"
        );

        Label sectionSubtitle =
                new Label(
                        "إدارة المرضى والجلسات والتغذية"
                );

        sectionSubtitle.getStyleClass().add(
                "fas-navbar-section-subtitle"
        );

        sectionBox.getChildren().addAll(
                sectionTitle,
                sectionSubtitle
        );

        // -----------------------------------------------------
        // Spacer
        // -----------------------------------------------------

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        // -----------------------------------------------------
        // User
        // -----------------------------------------------------

        HBox userSection =
                createUserSection();

        content.getChildren().addAll(
                logoBox,
                separator,
                clinicBox,
                separator2,
                sectionBox,
                spacer,
                userSection
        );

        top.getChildren().add(
                content
        );

        return top;
    }
    // =========================================================
    // Navigation bar
    // =========================================================

    private HBox createNavigationBar() {

        HBox navigation =
                new HBox(8);

        navigation.setAlignment(
                Pos.CENTER_RIGHT
        );

        navigation.setPadding(
                new Insets(
                        7,
                        18,
                        7,
                        18
                )
        );

        navigation.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        navigation.getStyleClass().add(
                "fas-navigation"
        );

        // -----------------------------------------------------
        // Buttons
        // -----------------------------------------------------

        btnHome =
                createNavButton(
                        "الرئيسية",
                        "⌂",
                        "Ctrl + 1"
                );

        btnClient =
                createNavButton(
                        "إضافة مريض",
                        "👤",
                        "Ctrl + 2"
                );

        btnSession =
                createNavButton(
                        "إضافة جلسة",
                        "📋",
                        "Ctrl + 3"
                );

        btnMeal =
                createNavButton(
                        "إضافة وجبة",
                        "🍎",
                        "Ctrl + 4"
                );

        btnSubscription =
                createNavButton(
                        "الاشتراك",
                        "💳",
                        "Ctrl + 5"
                );

        navigation.getChildren().addAll(
                btnHome,
                btnClient,
                btnSession,
                btnMeal,
                btnSubscription
        );

        // -----------------------------------------------------
        // Events
        // -----------------------------------------------------

        btnHome.setOnAction(
                e -> openHome()
        );

        btnClient.setOnAction(
                e -> openClient()
        );

        btnSession.setOnAction(
                e -> openSession()
        );

        btnMeal.setOnAction(
                e -> openMeal()
        );

        btnSubscription.setOnAction(
                e -> openSubscription()
        );

        return navigation;
    }

    // =========================================================
    // Create navigation button
    // =========================================================

    private Button createNavButton(
            String title,
            String icon,
            String shortcut
    ) {

        Button button =
                new Button();

        // -----------------------------------------------------
        // Button content
        // -----------------------------------------------------

        HBox content =
                new HBox(9);

        content.setAlignment(
                Pos.CENTER
        );

        content.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        // -----------------------------------------------------
        // Icon
        // -----------------------------------------------------

        Label iconLabel =
                new Label(icon);

        iconLabel.setMinWidth(
                22
        );

        iconLabel.setAlignment(
                Pos.CENTER
        );

        iconLabel.getStyleClass().add(
                "fas-nav-icon"
        );

        // -----------------------------------------------------
        // Title
        // -----------------------------------------------------

        Label titleLabel =
                new Label(title);

        titleLabel.setWrapText(
                false
        );

        titleLabel.setTextAlignment(
                TextAlignment.CENTER
        );

        titleLabel.getStyleClass().add(
                "fas-nav-title"
        );

        // -----------------------------------------------------
        // Shortcut
        // -----------------------------------------------------

        Label shortcutLabel =
                new Label(shortcut);

        shortcutLabel.setMinWidth(
                55
        );

        shortcutLabel.setAlignment(
                Pos.CENTER
        );

        shortcutLabel.getStyleClass().add(
                "fas-nav-shortcut"
        );

        // -----------------------------------------------------
        // Content
        // -----------------------------------------------------

        content.getChildren().addAll(
                iconLabel,
                titleLabel,
                shortcutLabel
        );

        button.setGraphic(
                content
        );

        // -----------------------------------------------------
        // Size
        // -----------------------------------------------------

        button.setMinHeight(
                44
        );

        button.setPrefHeight(
                46
        );

        /*
         * تم توسيع الأزرار حتى لا ينقص النص.
         */
        button.setMinWidth(
                165
        );

        button.setPrefWidth(
                200
        );

        button.setMaxWidth(
                220
        );

        button.setFocusTraversable(
                false
        );

        button.setMnemonicParsing(
                false
        );

        button.getStyleClass().add(
                "fas-nav-button"
        );

        // -----------------------------------------------------
        // Tooltip
        // -----------------------------------------------------

        Tooltip tooltip =
                new Tooltip(
                        title
                                + "\n"
                                + shortcut
                );

        tooltip.setShowDelay(
                javafx.util.Duration.millis(
                        300
                )
        );

        button.setTooltip(
                tooltip
        );

        navButtons.add(
                button
        );

        return button;
    }

    // =========================================================
    // Open Home
    // =========================================================

    private void openHome() {

        Main.setView(
                new HomeView(),
                "Home"
        );

        updateActiveButton(
                btnHome
        );
    }

    // =========================================================
    // Open Client
    // =========================================================

    private void openClient() {

        Main.setView(
                new ClientView(),
                "Client"
        );

        updateActiveButton(
                btnClient
        );
    }

    // =========================================================
    // Open Session
    // =========================================================

    private void openSession() {

        try {

            Main.setView(
                    new SessionView(),
                    "Session"
            );

            updateActiveButton(
                    btnSession
            );

        } catch (
                IOException |
                InterruptedException ex
        ) {

            ex.printStackTrace();

            showError(
                    "تعذر فتح واجهة الجلسة",
                    ex.getMessage()
            );
        }
    }

    // =========================================================
    // Open Meal
    // =========================================================

    private void openMeal() {

        Main.setView(
                new MealsView(),
                "Meal"
        );

        updateActiveButton(
                btnMeal
        );
    }

    // =========================================================
    // Open Subscription
    // =========================================================

    private void openSubscription() {

        Main.setView(
                new SubscriptionView(),
                "Subscription"
        );

        updateActiveButton(
                btnSubscription
        );
    }

    // =========================================================
    // User section
    // =========================================================

    private HBox createUserSection() {

        ClientSession session =
                securityGuard.getCurrentSession();

        String fullName =
                "المستخدم";

        String username =
                "";

        if (session != null) {

            if (session.getFullName() != null
                    && !session.getFullName().isBlank()) {

                fullName =
                        session.getFullName();
            }

            if (session.getUsername() != null
                    && !session.getUsername().isBlank()) {

                username =
                        session.getUsername();
            }
        }

        // -----------------------------------------------------
        // Avatar
        // -----------------------------------------------------

        Label avatar =
                new Label(
                        createInitials(
                                fullName
                        )
                );

        avatar.setAlignment(
                Pos.CENTER
        );

        avatar.setMinSize(
                38,
                38
        );

        avatar.setPrefSize(
                38,
                38
        );

        avatar.setMaxSize(
                38,
                38
        );

        avatar.getStyleClass().add(
                "fas-user-avatar"
        );

        // -----------------------------------------------------
        // User text
        // -----------------------------------------------------

        Label fullNameLabel =
                new Label(
                        fullName
                );

        fullNameLabel.getStyleClass().add(
                "fas-user-name"
        );

        Label usernameLabel =
                new Label(
                        username.isBlank()
                                ? ""
                                : "@"
                                + username
                );

        usernameLabel.getStyleClass().add(
                "fas-user-username"
        );

        VBox userText =
                new VBox(
                        1,
                        fullNameLabel,
                        usernameLabel
                );

        userText.setAlignment(
                Pos.CENTER_RIGHT
        );

        // -----------------------------------------------------
        // Logout
        // -----------------------------------------------------

        Button logoutButton =
                new Button(
                        "تسجيل الخروج"
                );

        logoutButton.setPrefHeight(
                38
        );

        logoutButton.getStyleClass().add(
                "fas-logout-button"
        );

        Tooltip logoutTooltip =
                new Tooltip(
                        "تسجيل الخروج من الحساب"
                );

        logoutButton.setTooltip(
                logoutTooltip
        );

        logoutButton.setOnAction(
                e -> confirmLogout()
        );

        // -----------------------------------------------------
        // User container
        // -----------------------------------------------------

        HBox userSection =
                new HBox(
                        10,
                        avatar,
                        userText,
                        logoutButton
                );

        userSection.setAlignment(
                Pos.CENTER
        );

        userSection.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        return userSection;
    }

    // =========================================================
    // Active button
    // =========================================================

    private void updateActiveButton(
            Button selectedButton
    ) {

        for (Button button : navButtons) {

            button.getStyleClass()
                    .remove(
                            "fas-nav-active"
                    );
        }

        if (selectedButton != null
                && !selectedButton
                .getStyleClass()
                .contains(
                        "fas-nav-active"
                )) {

            selectedButton.getStyleClass()
                    .add(
                            "fas-nav-active"
                    );
        }
    }

    // =========================================================
    // Last view
    // =========================================================

    private void configureLastView() {

        AppSettings appSettings =
                new AppSettings();

        String lastActiveView =
                appSettings.getLastView();

        switch (lastActiveView) {

            case "Client" ->
                    updateActiveButton(
                            btnClient
                    );

            case "Session" ->
                    updateActiveButton(
                            btnSession
                    );

            case "Meal" ->
                    updateActiveButton(
                            btnMeal
                    );

            case "Subscription" ->
                    updateActiveButton(
                            btnSubscription
                    );

            default ->
                    updateActiveButton(
                            btnHome
                    );
        }
    }

    // =========================================================
    // Keyboard shortcuts
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

        if (!event.isControlDown()) {
            return;
        }

        switch (event.getCode()) {

            case DIGIT1 -> {

                openHome();

                event.consume();
            }

            case DIGIT2 -> {

                openClient();

                event.consume();
            }

            case DIGIT3 -> {

                openSession();

                event.consume();
            }

            case DIGIT4 -> {

                openMeal();

                event.consume();
            }

            case DIGIT5 -> {

                openSubscription();

                event.consume();
            }

            default -> {
                // لا شيء
            }
        }
    }

    // =========================================================
    // Logout confirmation
    // =========================================================

    private void confirmLogout() {

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "تسجيل الخروج"
        );

        confirmation.setHeaderText(
                "هل تريد تسجيل الخروج؟"
        );

        confirmation.setContentText(
                "سيتم إنهاء الجلسة الحالية."
        );

        confirmation.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        confirmation.getButtonTypes()
                .setAll(
                        new ButtonType(
                                "تسجيل الخروج",
                                ButtonBar.ButtonData.OK_DONE
                        ),
                        new ButtonType(
                                "إلغاء",
                                ButtonBar.ButtonData.CANCEL_CLOSE
                        )
                );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                || result.get()
                .getButtonData()
                ==
                ButtonBar.ButtonData.CANCEL_CLOSE) {

            return;
        }

        performLogout();
    }

    // =========================================================
    // Logout
    // =========================================================

    private void performLogout() {

        try {

            securityGuard.clearLocalSession();

            Main.setView(
                    new LoginView(),
                    "Login"
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "فشل تسجيل الخروج",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // Initials
    // =========================================================

    private String createInitials(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            return "U";
        }

        String[] parts =
                name.trim()
                        .split("\\s+");

        if (parts.length == 1) {

            return parts[0]
                    .substring(
                            0,
                            Math.min(
                                    1,
                                    parts[0].length()
                            )
                    )
                    .toUpperCase();
        }

        String first =
                parts[0]
                        .substring(
                                0,
                                1
                        );

        String last =
                parts[parts.length - 1]
                        .substring(
                                0,
                                1
                        );

        return (
                first + last
        ).toUpperCase();
    }

    // =========================================================
    // Error
    // =========================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);

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

        alert.showAndWait();
    }
    // =========================================================
// Clinic section
// =========================================================

    private VBox createClinicSection() {

        ClientSession session =
                securityGuard.getCurrentSession();

        String clinicName =
                "العيادة";

        if (session != null
                && session.getClinicName() != null
                && !session.getClinicName().isBlank()) {

            clinicName =
                    session.getClinicName().trim();
        }

        // -----------------------------------------------------
        // Small label
        // -----------------------------------------------------

        Label label =
                new Label(
                        "العيادة"
                );

        label.getStyleClass().add(
                "fas-navbar-clinic-label"
        );

        // -----------------------------------------------------
        // Clinic name
        // -----------------------------------------------------

        Label name =
                new Label(
                        clinicName
                );

        name.setMaxWidth(
                180
        );

        name.setEllipsisString(
                "..."
        );

        name.getStyleClass().add(
                "fas-navbar-clinic-name"
        );

        // -----------------------------------------------------
        // Container
        // -----------------------------------------------------

        VBox clinicBox =
                new VBox(
                        2,
                        label,
                        name
                );

        clinicBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        clinicBox.getStyleClass().add(
                "fas-navbar-clinic-box"
        );

        return clinicBox;
    }
}