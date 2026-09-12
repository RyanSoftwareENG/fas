package user_interface.login;

import api.AuthAPI;
import api.ClientApiManager;
import app.Main;
import dto.UserLoginRequest;
import dto.UserLoginResponse;
import installation.InstallationIdentity;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner;

public class LoginView extends BorderPane {

    // =====================================================
    // الحقول
    // =====================================================

    private final TextField usernameField =
            new TextField();

    private final PasswordField passwordField =
            new PasswordField();

    private final Button loginButton =
            new Button("تسجيل الدخول");

    private final Label statusLabel =
            new Label();

    // =====================================================
    // API
    // =====================================================

    private final AuthAPI authAPI;

    // =====================================================
    // Constructor
    // =====================================================

    public LoginView() {

        authAPI =
                ClientApiManager
                        .getInstance()
                        .getAuthAPI();

        buildUI();
    }

    // =====================================================
    // بناء الواجهة
    // =====================================================

    private void buildUI() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setPadding(
                new Insets(40)
        );

        VBox container =
                new VBox(18);

        container.setAlignment(
                Pos.CENTER
        );

        container.setMaxWidth(
                430
        );

        // =================================================
        // العنوان
        // =================================================

        Label title =
                new Label(
                        "تسجيل الدخول إلى FAS"
                );

        title.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: #263238;
            """);

        Label subtitle =
                new Label(
                        "أدخل بيانات الحساب للمتابعة إلى النظام."
                );

        subtitle.setWrapText(
                true
        );

        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #607D8B;
            """);

        // =================================================
        // اسم المستخدم
        // =================================================

        Label usernameLabel =
                new Label(
                        "اسم المستخدم"
                );

        usernameLabel.setStyle(
                "-fx-font-weight: bold;"
        );

        usernameField.setPromptText(
                "اسم المستخدم"
        );

        usernameField.setPrefHeight(
                42
        );

        usernameField.setStyle("""
            -fx-font-size: 15px;
            -fx-padding: 8 12;
            """);

        // =================================================
        // كلمة المرور
        // =================================================

        Label passwordLabel =
                new Label(
                        "كلمة المرور"
                );

        passwordLabel.setStyle(
                "-fx-font-weight: bold;"
        );

        passwordField.setPromptText(
                "كلمة المرور"
        );

        passwordField.setPrefHeight(
                42
        );

        passwordField.setStyle("""
            -fx-font-size: 15px;
            -fx-padding: 8 12;
            """);

        // =================================================
        // زر الدخول
        // =================================================

        loginButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loginButton.setPrefHeight(
                44
        );

        loginButton.setStyle("""
            -fx-background-color: #1565C0;
            -fx-text-fill: white;
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-background-radius: 7;
            -fx-cursor: hand;
            """);

        loginButton.setOnAction(
                event -> handleLogin()
        );

        // =================================================
        // Enter
        // =================================================

        usernameField.setOnAction(
                event ->
                        passwordField.requestFocus()
        );

        passwordField.setOnAction(
                event ->
                        handleLogin()
        );

        // =================================================
        // رسالة الحالة
        // =================================================

        statusLabel.setWrapText(
                true
        );

        statusLabel.setVisible(
                false
        );

        statusLabel.setManaged(
                false
        );

        statusLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            """);

        // =================================================
        // التجميع
        // =================================================

        container.getChildren().addAll(

                title,

                subtitle,

                new Separator(),

                usernameLabel,
                usernameField,

                passwordLabel,
                passwordField,

                loginButton,

                statusLabel
        );

        setCenter(
                container
        );

        usernameField.requestFocus();
    }

    // =====================================================
    // تسجيل الدخول
    // =====================================================

    private void handleLogin() {

        // =================================================
        // قراءة البيانات
        // =================================================

        String username =
                usernameField
                        .getText()
                        .trim();

        String password =
                passwordField
                        .getText();

        // =================================================
        // التحقق المحلي
        // =================================================

        if (username.isBlank()) {

            showStatus(
                    "يرجى إدخال اسم المستخدم.",
                    false
            );

            usernameField.requestFocus();

            return;
        }

        if (password == null ||
                password.isBlank()) {

            showStatus(
                    "يرجى إدخال كلمة المرور.",
                    false
            );

            passwordField.requestFocus();

            return;
        }
        // =================================================
        // Installation ID
        // =================================================

        final String installationId;

        try {

            installationId =
                    InstallationIdentity
                            .getInstallationId();

        } catch (Exception e) {

            showStatus(
                    "تعذر قراءة معرف تثبيت الجهاز.",
                    false
            );

            return;
        }

        // =================================================
        // إنشاء الطلب
        // =================================================

        UserLoginRequest request =
                new UserLoginRequest(
                        username,
                        password,
                        installationId
                );

        // =================================================
        // تجهيز الواجهة
        // =================================================

        setLoadingState();

        // =================================================
        // حاويات النتائج
        // =================================================

        final UserLoginResponse[] responseHolder =
                new UserLoginResponse[1];

        final String[] errorHolder =
                new String[1];

        // =================================================
        // تنفيذ الاتصال بالخلفية
        // =================================================

        BackgroundRunner.run(

                "جاري التحقق من بيانات الدخول... ⏳",

                // =================================================
                // Backend Task
                // =================================================

                () -> {

                    try {

                        responseHolder[0] =
                                authAPI.login(
                                        request
                                );

                        /*
                         * وصول Response من السيرفر
                         * يعتبر نجاحاً تقنياً للـ Task.
                         *
                         * بعد ذلك نفحص:
                         *
                         * response.isSuccess()
                         *
                         * داخل JavaFX Thread.
                         */

                        return true;

                    } catch (Exception e) {

                        errorHolder[0] =
                                extractMessage(e);

                        /*
                         * نعيد false حتى يعرف
                         * BackgroundRunner أن العملية
                         * لم تنجح.
                         */

                        return false;
                    }
                },

                // =================================================
                // onSuccessUI
                //
                // ينفذ فقط عندما يرجع backendTask = true
                // =================================================

                () -> {

                    // =================================================
                    // Exception حدث داخل authAPI
                    // =================================================

                    if (errorHolder[0] != null) {

                        showStatus(
                                errorHolder[0],
                                false
                        );

                        return;
                    }

                    // =================================================
                    // Response فارغة
                    // =================================================

                    if (responseHolder[0] == null) {

                        showStatus(
                                "لم يستجب السيرفر ببيانات تسجيل الدخول.",
                                false
                        );

                        return;
                    }

                    // =================================================
                    // الحصول على Response
                    // =================================================

                    UserLoginResponse response =
                            responseHolder[0];

                    // =================================================
                    // تسجيل الدخول فشل
                    // =================================================

                    if (!response.isSuccess()) {

                        showStatus(
                                response.getMessage() == null
                                        ? "فشل تسجيل الدخول."
                                        : response.getMessage(),
                                false
                        );

                        passwordField.clear();

                        passwordField.requestFocus();

                        return;
                    }

                    // =================================================
                    // تسجيل الدخول نجح
                    // =================================================

                    showStatus(
                            response.getMessage() == null
                                    ? "تم تسجيل الدخول بنجاح."
                                    : response.getMessage(),
                            true
                    );

                    // =================================================
                    // أول دخول + يحتاج تهيئة
                    // =================================================

                    if (response.isNeedsSetup()) {

                        FirstLoginSetupView setupView =
                                new FirstLoginSetupView(
                                        authAPI,
                                        response.getSetupToken(),
                                        request.getInstallationId(),
                                        response.getUsername(),
                                        response.getFullName()
                                );

                        Main.setView(
                                setupView,
                                "FirstLoginSetup"
                        );

                        return;
                    }

                    // =================================================
                    // دخول عادي
                    // =================================================

                    Main.openMainApplication();
                },

                // =================================================
                // onFinishedUI
                //
                // هذا هو الجزء المهم.
                //
                // ينفذ سواء:
                //
                // 1. نجاح
                // 2. فشل login
                // 3. Exception
                //
                // وبالتالي لن تبقى الواجهة معطلة.
                // =================================================

                this::setNormalState
        );
    }

    // =====================================================
    // حالة التحميل
    // =====================================================

    private void setLoadingState() {

        loginButton.setDisable(
                true
        );

        usernameField.setDisable(
                true
        );

        passwordField.setDisable(
                true
        );

        showStatus(
                "جاري تسجيل الدخول...",
                true
        );
    }

    // =====================================================
    // الحالة الطبيعية
    // =====================================================

    private void setNormalState() {

        loginButton.setDisable(
                false
        );

        usernameField.setDisable(
                false
        );

        passwordField.setDisable(
                false
        );
    }

    // =====================================================
    // عرض الحالة
    // =====================================================

    private void showStatus(
            String message,
            boolean success
    ) {

        statusLabel.setText(
                message
        );

        statusLabel.setVisible(
                true
        );

        statusLabel.setManaged(
                true
        );

        if (success) {

            statusLabel.setStyle("""
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-text-fill: #2E7D32;
                """);

        } else {

            statusLabel.setStyle("""
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-text-fill: #C62828;
                """);
        }
    }

    // =====================================================
    // استخراج رسالة الخطأ
    // =====================================================

    private String extractMessage(
            Throwable throwable
    ) {

        if (throwable == null) {

            return "حدث خطأ غير معروف.";
        }

        Throwable cause =
                throwable;

        while (
                cause.getCause() != null
        ) {

            cause =
                    cause.getCause();
        }

        String message =
                cause.getMessage();

        return message == null ||
                message.isBlank()
                ? "تعذر الاتصال بالسيرفر."
                : message;
    }
}