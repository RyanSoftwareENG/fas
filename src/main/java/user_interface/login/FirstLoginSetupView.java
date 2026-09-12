package user_interface.login;

import api.AuthAPI;
import app.Main;
import dto.DeviceRegistrationRequest;
import dto.UserLoginResponse;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import runner.BackgroundRunner;

public class FirstLoginSetupView extends BorderPane {

    // =====================================================
    // API
    // =====================================================

    /*
     * نفس AuthAPI المستخدم في LoginView.
     *
     * مهم:
     * لا ننشئ ApiClient جديد هنا.
     */
    private final AuthAPI authAPI;

    // =====================================================
    // بيانات عملية التهيئة
    // =====================================================

    private final String setupToken;
    private final String installationId;

    private final String username;
    private final String fullName;

    // =====================================================
    // عناصر الواجهة
    // =====================================================

    private final TextField activationCodeField =
            new TextField();

    private final TextField deviceNameField =
            new TextField();

    private final Label statusLabel =
            new Label();

    private final Button registerButton =
            new Button(
                    "تسجيل الجهاز والمتابعة"
            );

    // =====================================================
    // Constructor
    // =====================================================

    public FirstLoginSetupView(
            AuthAPI authAPI,
            String setupToken,
            String installationId,
            String username,
            String fullName
    ) {

        if (authAPI == null) {
            throw new IllegalArgumentException(
                    "AuthAPI مطلوب."
            );
        }

        this.authAPI =
                authAPI;

        this.setupToken =
                setupToken;

        this.installationId =
                installationId;

        this.username =
                username;

        this.fullName =
                fullName;

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
                new Insets(30)
        );

        // =================================================
        // الحاوية
        // =================================================

        VBox content =
                new VBox(15);

        content.setAlignment(
                Pos.TOP_RIGHT
        );

        content.setMaxWidth(
                500
        );

        // =================================================
        // العنوان
        // =================================================

        Label title =
                new Label(
                        "تسجيل هذا الجهاز"
                );

        title.setStyle("""
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            """);

        // =================================================
        // معلومات المستخدم
        // =================================================

        Label welcomeLabel =
                new Label(
                        fullName != null &&
                                !fullName.isBlank()

                                ? "مرحبًا " + fullName
                                : "مرحبًا " + username
                );

        welcomeLabel.setStyle("""
            -fx-font-size: 17px;
            -fx-font-weight: bold;
            """);

        // =================================================
        // التعليمات
        // =================================================

        Label informationLabel =
                new Label(
                        "هذا الجهاز غير مسجل في النظام. "
                                + "للمتابعة، أدخل كود التفعيل "
                                + "الخاص بالجهاز."
                );

        informationLabel.setWrapText(
                true
        );

        informationLabel.setStyle("""
            -fx-font-size: 14px;
            """);

        // =================================================
        // كود التفعيل
        // =================================================

        Label activationLabel =
                new Label(
                        "كود تفعيل الجهاز"
                );

        activationLabel.setStyle(
                "-fx-font-weight: bold;"
        );

        activationCodeField.setPromptText(
                "أدخل كود التفعيل"
        );

        activationCodeField.setPrefHeight(
                42
        );

        activationCodeField.setMaxWidth(
                420
        );

        activationCodeField.setStyle("""
            -fx-font-size: 15px;
            -fx-padding: 8 12;
            """);

        // =================================================
        // اسم الجهاز
        // =================================================

        Label deviceNameLabel =
                new Label(
                        "اسم الجهاز"
                );

        deviceNameLabel.setStyle(
                "-fx-font-weight: bold;"
        );

        deviceNameField.setPromptText(
                "مثال: جهاز الاستقبال"
        );

        deviceNameField.setPrefHeight(
                42
        );

        deviceNameField.setMaxWidth(
                420
        );

        deviceNameField.setStyle("""
            -fx-font-size: 15px;
            -fx-padding: 8 12;
            """);

        // =================================================
        // زر التسجيل
        // =================================================

        registerButton.setPrefHeight(
                42
        );

        registerButton.setPrefWidth(
                220
        );

        registerButton.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            """);

        registerButton.setOnAction(
                event ->
                        handleRegisterDevice()
        );

        // =================================================
        // الحالة
        // =================================================

        statusLabel.setWrapText(
                true
        );

        statusLabel.setMaxWidth(
                420
        );

        // =================================================
        // التجميع
        // =================================================

        content.getChildren().addAll(
                title,
                new Separator(),
                welcomeLabel,
                informationLabel,
                activationLabel,
                activationCodeField,
                deviceNameLabel,
                deviceNameField,
                registerButton,
                statusLabel
        );

        // =================================================
        // وضع المحتوى في المنتصف
        // =================================================

        HBox wrapper =
                new HBox(
                        content
                );

        wrapper.setAlignment(
                Pos.TOP_CENTER
        );

        setCenter(
                wrapper
        );

        // =================================================
        // Enter
        // =================================================

        activationCodeField.setOnAction(
                event ->
                        handleRegisterDevice()
        );

        deviceNameField.setOnAction(
                event ->
                        handleRegisterDevice()
        );

        activationCodeField.requestFocus();
    }

    // =====================================================
    // تسجيل الجهاز
    // =====================================================

    private void handleRegisterDevice() {

        // -------------------------------------------------
        // Setup Token
        // -------------------------------------------------

        if (setupToken == null ||
                setupToken.isBlank()) {

            showStatus(
                    "انتهت صلاحية عملية تسجيل الجهاز. "
                            + "يرجى تسجيل الدخول مرة أخرى.",
                    false
            );

            return;
        }

        // -------------------------------------------------
        // Installation ID
        // -------------------------------------------------

        if (installationId == null ||
                installationId.isBlank()) {

            showStatus(
                    "معرف تثبيت الجهاز غير موجود.",
                    false
            );

            return;
        }

        // -------------------------------------------------
        // كود التفعيل
        // -------------------------------------------------

        String activationCode =
                activationCodeField
                        .getText()
                        .trim();

        if (activationCode.isBlank()) {

            showStatus(
                    "يرجى إدخال كود تفعيل الجهاز.",
                    false
            );

            activationCodeField.requestFocus();

            return;
        }

        // -------------------------------------------------
        // اسم الجهاز
        // -------------------------------------------------

        String deviceName =
                deviceNameField
                        .getText()
                        .trim();

        if (deviceName.isBlank()) {

            deviceName =
                    "FAS Device";
        }

        // =================================================
        // إنشاء Request
        // =================================================

        DeviceRegistrationRequest request =
                new DeviceRegistrationRequest();

        request.setActivationCode(
                activationCode
        );

        request.setInstallationId(
                installationId
        );

        request.setDeviceName(
                deviceName
        );

        // =================================================
        // حالة التحميل
        // =================================================

        setLoadingState(
                true
        );

        showStatus(
                "جاري التحقق من كود التفعيل وتسجيل الجهاز... ⏳",
                true
        );

        // =================================================
        // حاويات النتائج
        // =================================================

        final UserLoginResponse[] responseHolder =
                new UserLoginResponse[1];

        final String[] errorHolder =
                new String[1];

        // =================================================
        // تنفيذ الطلب في الخلفية
        // =================================================

        BackgroundRunner.run(

                "جاري تسجيل الجهاز... ⏳",

                // =================================================
                // Backend Task
                // =================================================

                () -> {

                    try {

                        responseHolder[0] =
                                authAPI.registerDevice(
                                        setupToken,
                                        request
                                );

                        return true;

                    } catch (Exception e) {

                        errorHolder[0] =
                                extractMessage(e);

                        return false;
                    }
                },

                // =================================================
                // UI Success
                // =================================================

                () -> {

                    // -------------------------------------------------
                    // خطأ
                    // -------------------------------------------------

                    if (errorHolder[0] != null) {

                        setLoadingState(
                                false
                        );

                        showStatus(
                                errorHolder[0],
                                false
                        );

                        return;
                    }

                    // -------------------------------------------------
                    // Response فارغة
                    // -------------------------------------------------

                    if (responseHolder[0] == null) {

                        setLoadingState(
                                false
                        );

                        showStatus(
                                "لم يستجب السيرفر ببيانات تسجيل الجهاز.",
                                false
                        );

                        return;
                    }

                    UserLoginResponse response =
                            responseHolder[0];

                    // -------------------------------------------------
                    // فشل التسجيل
                    // -------------------------------------------------

                    if (!response.isSuccess()) {

                        setLoadingState(
                                false
                        );

                        showStatus(
                                response.getMessage() != null
                                        ? response.getMessage()
                                        : "فشل تسجيل الجهاز.",
                                false
                        );

                        activationCodeField.requestFocus();

                        return;
                    }

                    // -------------------------------------------------
                    // التأكد من Session Token
                    // -------------------------------------------------

                    if (response.getToken() == null ||
                            response.getToken().isBlank()) {

                        setLoadingState(
                                false
                        );

                        showStatus(
                                "تم تسجيل الجهاز، "
                                        + "لكن لم يتم إنشاء جلسة صالحة.",
                                false
                        );

                        return;
                    }

                    // -------------------------------------------------
                    // النجاح
                    // -------------------------------------------------

                    showStatus(
                            response.getMessage() != null
                                    ? response.getMessage()
                                    : "تم تسجيل الجهاز بنجاح.",
                            true
                    );

                    /*
                     * authAPI.registerDevice()
                     * قام بالفعل بـ:
                     *
                     * 1. apiClient.setToken(...)
                     * 2. ClientSessionStore.save(...)
                     * 3. ClientSecurityGuard.adoptSession(...)
                     *
                     * لذلك لا نكرر هذه العمليات هنا.
                     */

                    setLoadingState(
                            false
                    );

                    // -------------------------------------------------
                    // الانتقال للتطبيق
                    // -------------------------------------------------

                    Platform.runLater(
                            Main::openMainApplication
                    );
                },

                // =================================================
                // Finished
                // =================================================

                () -> {

                    /*
                     * إذا انتهى BackgroundRunner قبل تنفيذ
                     * onSuccessUI أو عند وجود استثناء،
                     * نعيد الحقول لحالتها الطبيعية.
                     */
                    if (errorHolder[0] != null ||
                            responseHolder[0] == null ||
                            !responseHolder[0].isSuccess()) {

                        setNormalState();
                    }
                }
        );
    }

    // =====================================================
    // حالة التحميل
    // =====================================================

    private void setLoadingState(
            boolean loading
    ) {

        activationCodeField.setDisable(
                loading
        );

        deviceNameField.setDisable(
                loading
        );

        registerButton.setDisable(
                loading
        );
    }

    // =====================================================
    // الحالة الطبيعية
    // =====================================================

    private void setNormalState() {

        activationCodeField.setDisable(
                false
        );

        deviceNameField.setDisable(
                false
        );

        registerButton.setDisable(
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

        if (success) {

            statusLabel.setStyle("""
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #2E7D32;
                """);

        } else {

            statusLabel.setStyle("""
                -fx-font-size: 14px;
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

        if (message == null ||
                message.isBlank()) {

            return "تعذر الاتصال بالسيرفر.";
        }

        return message;
    }
}