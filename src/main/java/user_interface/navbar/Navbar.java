package user_interface.navbar;

import app.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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

public class Navbar extends HBox {

    private final List<Button> navButtons =
            new ArrayList<>();

    private final ClientSecurityGuard securityGuard =
            ClientSecurityGuard.getInstance();

    public Navbar() {

        // =====================================================
        // إعداد الـNavbar
        // =====================================================

        setSpacing(12);

        setAlignment(
                Pos.CENTER_LEFT
        );

        setPadding(
                new Insets(
                        10,
                        18,
                        10,
                        18
                )
        );

        setStyle("""
            -fx-background-color: #8bb531;
            -fx-border-color: #8d4c20;
            -fx-border-width: 0 0 1 0;
        """);

        // =====================================================
        // أزرار التنقل
        // =====================================================

        Button btnHome =
                createNavButton(
                        "الرئيسية"
                );

        Button btnClient =
                createNavButton(
                        "إضافة مريض"
                );

        Button btnSession =
                createNavButton(
                        "إضافة جلسة"
                );

        Button btnMeal =
                createNavButton(
                        "إضافة وجبة"
                );

        Button btnSubscription =
                createNavButton(
                        "الاشتراك"
                );

        getChildren().addAll(
                btnHome,
                btnClient,
                btnSession,
                btnMeal,
                btnSubscription
        );

        // =====================================================
        // أحداث التنقل
        // =====================================================

        btnHome.setOnAction(
                e -> {

                    Main.setView(
                            new HomeView(),
                            "Home"
                    );

                    updateActiveButton(
                            btnHome
                    );
                }
        );

        btnClient.setOnAction(
                e -> {

                    Main.setView(
                            new ClientView(),
                            "Client"
                    );

                    updateActiveButton(
                            btnClient
                    );
                }
        );

        btnSession.setOnAction(
                e -> {

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
        );

        btnMeal.setOnAction(
                e -> {

                    Main.setView(
                            new MealsView(),
                            "Meal"
                    );

                    updateActiveButton(
                            btnMeal
                    );
                }
        );

        // =====================================================
        // الاشتراك
        // =====================================================

        btnSubscription.setOnAction(
                e -> {

                    Main.setView(
                            new SubscriptionView(),
                            "Subscription"
                    );

                    updateActiveButton(
                            btnSubscription
                    );
                }
        );

        // =====================================================
        // مساحة مرنة
        // =====================================================

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        getChildren().add(
                spacer
        );

        // =====================================================
        // معلومات المستخدم
        // =====================================================

        HBox userSection =
                createUserSection();

        getChildren().add(
                userSection
        );

        // =====================================================
        // تحديد الزر النشط
        // =====================================================

        AppSettings appSettings =
                new AppSettings();

        String lastActiveView =
                appSettings.getLastView();

        switch (
                lastActiveView
        ) {

            case "Client":

                updateActiveButton(
                        btnClient
                );

                break;

            case "Session":

                updateActiveButton(
                        btnSession
                );

                break;

            case "Meal":

                updateActiveButton(
                        btnMeal
                );

                break;

            case "Subscription":

                updateActiveButton(
                        btnSubscription
                );

                break;

            default:

                updateActiveButton(
                        btnHome
                );

                break;
        }
    }

    // =====================================================
    // User Section
    // =====================================================

    private HBox createUserSection() {

        ClientSession session =
                securityGuard.getCurrentSession();

        String fullName =
                "المستخدم";

        String username =
                "";

        if (session != null) {

            if (session.getFullName() != null &&
                    !session.getFullName().isBlank()) {

                fullName =
                        session.getFullName();
            }

            if (session.getUsername() != null &&
                    !session.getUsername().isBlank()) {

                username =
                        session.getUsername();
            }
        }

        // -------------------------------------------------
        // صورة المستخدم الرمزية
        // -------------------------------------------------

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

        avatar.setStyle("""
            -fx-background-color: rgba(255,255,255,0.22);
            -fx-background-radius: 50%;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
        """);

        // -------------------------------------------------
        // اسم المستخدم
        // -------------------------------------------------

        Label fullNameLabel =
                new Label(
                        fullName
                );

        fullNameLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
        """);

        Label usernameLabel =
                new Label(
                        username.isBlank()
                                ? ""
                                : "@" + username
                );

        usernameLabel.setStyle("""
            -fx-text-fill: rgba(255,255,255,0.85);
            -fx-font-size: 11px;
        """);

        VBox userText =
                new VBox(
                        1,
                        fullNameLabel,
                        usernameLabel
                );

        userText.setAlignment(
                Pos.CENTER_RIGHT
        );

        // -------------------------------------------------
        // تسجيل الخروج
        // -------------------------------------------------

        Button logoutButton =
                new Button(
                        "تسجيل الخروج"
                );

        logoutButton.setPrefHeight(
                38
        );

        logoutButton.setPadding(
                new Insets(
                        0,
                        14,
                        0,
                        14
                )
        );

        logoutButton.setStyle("""
            -fx-background-color: rgba(255,255,255,0.15);
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-background-radius: 7px;
            -fx-border-color: rgba(255,255,255,0.35);
            -fx-border-radius: 7px;
            -fx-cursor: hand;
        """);

        logoutButton.setOnMouseEntered(
                e -> logoutButton.setStyle("""
                    -fx-background-color: #c62828;
                    -fx-text-fill: white;
                    -fx-font-size: 13px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 7px;
                    -fx-border-color: #c62828;
                    -fx-border-radius: 7px;
                    -fx-cursor: hand;
                """)
        );

        logoutButton.setOnMouseExited(
                e -> logoutButton.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.15);
                    -fx-text-fill: white;
                    -fx-font-size: 13px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 7px;
                    -fx-border-color: rgba(255,255,255,0.35);
                    -fx-border-radius: 7px;
                    -fx-cursor: hand;
                """)
        );

        logoutButton.setOnAction(
                e -> confirmLogout()
        );

        // -------------------------------------------------
        // الحاوية
        // -------------------------------------------------

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
                javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
        );

        return userSection;
    }

    // =====================================================
    // تأكيد تسجيل الخروج
    // =====================================================

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
                        javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
                );

        confirmation.getButtonTypes()
                .setAll(
                        new javafx.scene.control.ButtonType(
                                "تسجيل الخروج",
                                javafx.scene.control.ButtonBar.ButtonData.OK_DONE
                        ),
                        new javafx.scene.control.ButtonType(
                                "إلغاء",
                                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE
                        )
                );

        java.util.Optional<javafx.scene.control.ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get().getButtonData() ==
                        javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE) {

            return;
        }

        performLogout();
    }

    // =====================================================
    // تنفيذ تسجيل الخروج
    // =====================================================

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

    // =====================================================
    // إنشاء Initials
    // =====================================================

    private String createInitials(
            String name
    ) {

        if (name == null ||
                name.isBlank()) {

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
                first +
                        last
        ).toUpperCase();
    }

    // =====================================================
    // إنشاء زر التنقل
    // =====================================================

    private Button createNavButton(
            String text
    ) {

        Button btn =
                new Button(
                        text
                );

        btn.setMinSize(
                Button.USE_PREF_SIZE,
                Button.USE_PREF_SIZE
        );

        btn.setPadding(
                new Insets(
                        8,
                        22,
                        8,
                        22
                )
        );

        btn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);

        btn.setOnMouseEntered(
                e -> {

                    if (!btn.getStyle()
                            .contains(
                                    "#c86c2d"
                            )) {

                        btn.setStyle("""
                            -fx-background-color: #e88b4d;
                            -fx-text-fill: white;
                            -fx-font-size: 14px;
                            -fx-font-weight: bold;
                            -fx-background-radius: 6px;
                            -fx-cursor: hand;
                        """);
                    }
                }
        );

        btn.setOnMouseExited(
                e -> {

                    if (!btn.getStyle()
                            .contains(
                                    "#c86c2d"
                            )) {

                        btn.setStyle("""
                            -fx-background-color: transparent;
                            -fx-text-fill: white;
                            -fx-font-size: 14px;
                            -fx-font-weight: bold;
                            -fx-cursor: hand;
                        """);
                    }
                }
        );

        navButtons.add(
                btn
        );

        return btn;
    }

    // =====================================================
    // تحديث الزر النشط
    // =====================================================

    private void updateActiveButton(
            Button selectedBtn
    ) {

        for (
                Button btn :
                navButtons
        ) {

            if (btn == selectedBtn) {

                btn.setStyle("""
                    -fx-background-color: #c86c2d;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-font-size: 14px;
                    -fx-background-radius: 6px;
                    -fx-cursor: hand;
                """);

            } else {

                btn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-cursor: hand;
                """);
            }
        }
    }

    // =====================================================
    // Error
    // =====================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message == null ||
                        message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }
}