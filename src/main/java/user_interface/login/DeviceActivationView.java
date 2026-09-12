package user_interface.login;

import installation.DeviceRegistrationService;
import installation.DeviceInfo;
import installation.InstallationIdentity;

import dto.DeviceRegistrationResponse;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import runner.BackgroundRunner;

public class DeviceActivationView extends BorderPane {

    private final TextField activationCodeField =
            new TextField();

    private final Label deviceNameLabel =
            new Label();

    private final Label installationIdLabel =
            new Label();

    private final Label statusLabel =
            new Label("الجهاز غير مفعّل");

    private final Button activateButton =
            new Button("🔐 تفعيل الجهاز");

    private final DeviceRegistrationService
            registrationService =
            new DeviceRegistrationService();

    public DeviceActivationView() {

        setPadding(
                new Insets(40)
        );

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        VBox container =
                new VBox(18);

        container.setMaxWidth(520);

        container.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label("تفعيل جهاز FAS");

        title.setStyle("""
        -fx-font-size: 28px;
        -fx-font-weight: bold;
    """);

        Label description =
                new Label(
                        "أدخل كود التفعيل الذي تم إصداره لهذا الجهاز من إدارة FAS."
                );

        description.setWrapText(true);

        description.setStyle("""
        -fx-font-size: 14px;
        -fx-text-fill: #607D8B;
    """);

        // =====================================================
        // اسم الجهاز
        // =====================================================

        Label deviceTitle =
                new Label("اسم الجهاز");

        deviceTitle.setStyle(
                "-fx-font-weight: bold;"
        );

        deviceNameLabel.setText(
                getDeviceName()
        );

        deviceNameLabel.setStyle("""
        -fx-background-color: #F5F7F9;
        -fx-border-color: #D0D7DE;
        -fx-border-radius: 6;
        -fx-background-radius: 6;
        -fx-padding: 10;
        -fx-font-size: 14px;
    """);

        // =====================================================
        // Installation ID
        // =====================================================

        Label installationTitle =
                new Label("معرف التثبيت");

        installationTitle.setStyle(
                "-fx-font-weight: bold;"
        );

        installationIdLabel.setText(
                getInstallationId()
        );

        installationIdLabel.setWrapText(true);

        installationIdLabel.setStyle("""
        -fx-background-color: #F5F7F9;
        -fx-border-color: #D0D7DE;
        -fx-border-radius: 6;
        -fx-background-radius: 6;
        -fx-padding: 10;
        -fx-font-size: 12px;
    """);

        // =====================================================
        // Activation Code
        // =====================================================

        Label codeTitle =
                new Label("كود التفعيل");

        codeTitle.setStyle(
                "-fx-font-weight: bold;"
        );

        activationCodeField.setPromptText(
                "أدخل كود التفعيل"
        );

        activationCodeField.setPrefHeight(
                42
        );

        activationCodeField.setStyle("""
        -fx-font-size: 16px;
        -fx-padding: 8 12;
    """);

        // =====================================================
        // Status
        // =====================================================

        statusLabel.setWrapText(true);

        statusLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-text-fill: #C62828;
        -fx-font-weight: bold;
    """);

        // =====================================================
        // Button
        // =====================================================

        activateButton.setMaxWidth(
                Double.MAX_VALUE
        );

        activateButton.setPrefHeight(
                44
        );

        activateButton.setStyle("""
        -fx-background-color: #1565C0;
        -fx-text-fill: white;
        -fx-font-size: 15px;
        -fx-font-weight: bold;
        -fx-background-radius: 6;
        -fx-cursor: hand;
    """);

        activateButton.setOnAction(
                e -> handleActivation()
        );

        // =====================================================
        // Layout
        // =====================================================

        container.getChildren().addAll(

                title,

                description,

                new Separator(),

                deviceTitle,
                deviceNameLabel,

                installationTitle,
                installationIdLabel,

                codeTitle,
                activationCodeField,

                activateButton,

                statusLabel
        );

        setCenter(
                container
        );
    }

// =====================================================
// تنفيذ التفعيل
// =====================================================

    private void handleActivation() {

        String activationCode =
                activationCodeField
                        .getText();

        if (activationCode == null ||
                activationCode.isBlank()) {

            showWarning(
                    "كود التفعيل مطلوب."
            );

            return;
        }

        final DeviceRegistrationResponse[] responseHolder =
                new DeviceRegistrationResponse[1];

        final String[] errorHolder =
                new String[1];

        setLoadingState();

        BackgroundRunner.run(
                "جاري تفعيل الجهاز... ⏳",

                () -> {

                    try {

                        responseHolder[0] =
                                registrationService.register(
                                        activationCode
                                );

                        /*
                         * وصول استجابة من السيرفر
                         * يعني أن العملية التقنية اكتملت.
                         *
                         * success الحقيقي نعالجه في
                         * onSuccessUI.
                         */
                        return true;

                    } catch (Exception e) {

                        errorHolder[0] =
                                extractMessage(e);

                        return false;
                    }
                },

                () -> {

                    setNormalState();

                    if (errorHolder[0] != null) {

                        showError(
                                errorHolder[0]
                        );

                        return;
                    }

                    handleActivationResult(
                            responseHolder[0]
                    );
                }
        );
    }


// =====================================================
// نتيجة التفعيل
// =====================================================

    private void handleActivationResult(
            Object result
    ) {

        if (!(result instanceof DeviceRegistrationResponse response)) {

            setNormalState();

            showError(
                    "استجابة غير صالحة من السيرفر."
            );

            return;
        }

        setNormalState();

        if (!response.isSuccess()) {

            String message =
                    response.getMessage();

            statusLabel.setText(
                    message == null ||
                            message.isBlank()
                            ? "فشل تفعيل الجهاز."
                            : message
            );

            statusLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #C62828;
            -fx-font-weight: bold;
        """);

            return;
        }

        statusLabel.setText(
                response.getMessage() == null
                        ? "تم تفعيل الجهاز بنجاح."
                        : response.getMessage()
        );

        statusLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-text-fill: #2E7D32;
        -fx-font-weight: bold;
    """);

        showInformation(
                response.getMessage() == null
                        ? "تم تفعيل الجهاز بنجاح."
                        : response.getMessage()
        );

        /*
         * هنا سننتقل لاحقًا إلى LoginView.
         *
         * لن نضع الانتقال الآن حتى نربط
         * دورة تشغيل البرنامج كاملة:
         *
         * Device → Login → USER_SESSION → Main
         */
    }

// =====================================================
// حالة التحميل
// =====================================================

    private void setLoadingState() {

        activateButton.setDisable(
                true
        );

        activationCodeField.setDisable(
                true
        );

        statusLabel.setText(
                "جاري الاتصال بالسيرفر..."
        );

        statusLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-text-fill: #1565C0;
        -fx-font-weight: bold;
    """);
    }

    private void setNormalState() {

        activateButton.setDisable(
                false
        );

        activationCodeField.setDisable(
                false
        );
    }

// =====================================================
// Installation ID
// =====================================================

    private String getInstallationId() {

        try {

            return InstallationIdentity
                    .getInstallationId();

        } catch (Exception e) {

            return "تعذر قراءة معرف التثبيت";
        }
    }

// =====================================================
// Device Name
// =====================================================

    private String getDeviceName() {

        try {

            return DeviceInfo
                    .getDeviceName();

        } catch (Exception e) {

            return "FAS-DEVICE";
        }
    }

// =====================================================
// رسالة الخطأ
// =====================================================

    private String extractMessage(
            Throwable throwable
    ) {

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

// =====================================================
// تنبيهات
// =====================================================

    private void showWarning(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "تنبيه"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "فشل العملية"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }

    private void showInformation(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "نجاح"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.getDialogPane()
                .setNodeOrientation(
                        NodeOrientation.RIGHT_TO_LEFT
                );

        alert.showAndWait();
    }
}
