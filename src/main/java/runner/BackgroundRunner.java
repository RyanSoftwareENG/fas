package runner;

import api.ApiClientException;
import security.ClientSecurityGuard;
import security.NetworkChecker;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;
import java.util.concurrent.Callable;

public final class BackgroundRunner {

    private BackgroundRunner() {
    }

    // ==========================================================
    // رابط السيرفر
    // ==========================================================

    private static final String SERVER_URL =
            "http://localhost:8080/api/health";

    // ==========================================================
    // Security Guard
    // ==========================================================

    private static final ClientSecurityGuard SECURITY_GUARD =
            ClientSecurityGuard.getInstance();

    // ==========================================================
    // عملية بسيطة - غير محمية
    // ==========================================================

    public static void run(
            Callable<Boolean> backendTask
    ) {

        run(
                "جاري تنفيذ العملية، يرجى الانتظار...",
                backendTask,
                null,
                null
        );
    }

    // ==========================================================
    // عملية + رسالة - غير محمية
    // ==========================================================

    public static void run(
            String loadingText,
            Callable<Boolean> backendTask
    ) {

        run(
                loadingText,
                backendTask,
                null,
                null
        );
    }

    // ==========================================================
    // عملية + نجاح - غير محمية
    // ==========================================================

    public static void run(
            String loadingText,
            Callable<Boolean> backendTask,
            Runnable onSuccessUI
    ) {

        run(
                loadingText,
                backendTask,
                onSuccessUI,
                null
        );
    }

    // ==========================================================
    // عملية + نجاح + انتهاء - غير محمية
    // ==========================================================

    public static void run(
            String loadingText,
            Callable<Boolean> backendTask,
            Runnable onSuccessUI,
            Runnable onFinishedUI
    ) {

        execute(
                loadingText,
                backendTask,
                onSuccessUI,
                onFinishedUI,
                false
        );
    }

    // ==========================================================
    // عملية محمية
    // ==========================================================

    public static void runAuthenticated(
            Callable<Boolean> backendTask
    ) {

        runAuthenticated(
                "جاري تنفيذ العملية، يرجى الانتظار...",
                backendTask,
                null,
                null
        );
    }

    // ==========================================================
    // عملية محمية + رسالة
    // ==========================================================

    public static void runAuthenticated(
            String loadingText,
            Callable<Boolean> backendTask
    ) {

        runAuthenticated(
                loadingText,
                backendTask,
                null,
                null
        );
    }

    // ==========================================================
    // عملية محمية + نجاح
    // ==========================================================

    public static void runAuthenticated(
            String loadingText,
            Callable<Boolean> backendTask,
            Runnable onSuccessUI
    ) {

        runAuthenticated(
                loadingText,
                backendTask,
                onSuccessUI,
                null
        );
    }

    // ==========================================================
    // عملية محمية + نجاح + انتهاء
    // ==========================================================

    public static void runAuthenticated(
            String loadingText,
            Callable<Boolean> backendTask,
            Runnable onSuccessUI,
            Runnable onFinishedUI
    ) {

        execute(
                loadingText,
                backendTask,
                onSuccessUI,
                onFinishedUI,
                true
        );
    }

    // ==========================================================
    // التنفيذ المركزي
    // ==========================================================

    private static void execute(
            String loadingText,
            Callable<Boolean> backendTask,
            Runnable onSuccessUI,
            Runnable onFinishedUI,
            boolean authenticated
    ) {

        if (backendTask == null) {

            throw new IllegalArgumentException(
                    "Backend task مطلوب."
            );
        }

        Platform.runLater(() -> {

            Stage loadingStage =
                    createLoadingStage(
                            loadingText
                    );

            Task<Boolean> task =
                    new Task<>() {

                        @Override
                        protected Boolean call()
                                throws Exception {

                            // ==================================================
                            // فحص السيرفر
                            // ==================================================

                            if (!NetworkChecker
                                    .isServerAvailable(
                                            SERVER_URL
                                    )) {

                                if (!NetworkChecker
                                        .isInternetAvailable()) {

                                    throw new NetworkException(
                                            "لا يوجد اتصال بالشبكة حالياً.\n\n"
                                                    + "يرجى التحقق من اتصال الجهاز بالشبكة "
                                                    + "ثم المحاولة مرة أخرى."
                                    );

                                } else {

                                    throw new NetworkException(
                                            "تعذر الوصول إلى خادم التطبيق.\n\n"
                                                    + "قد يكون الخادم متوقفاً أو غير متاح مؤقتاً.\n"
                                                    + "تأكد من تشغيل السيرفر ثم أعد المحاولة."
                                    );
                                }
                            }

                            // ==================================================
                            // Security Guard المحلي
                            // ==================================================

                            if (authenticated) {

                                ClientSecurityGuard.SecurityCheckResult
                                        securityResult =
                                        SECURITY_GUARD
                                                .checkOperation();

                                if (!securityResult
                                        .isAllowed()) {

                                    throw new LocalSecurityException(
                                            securityResult
                                                    .getMessage()
                                    );
                                }
                            }

                            // ==================================================
                            // تنفيذ العملية
                            // ==================================================

                            return backendTask.call();
                        }
                    };

            // ==========================================================
            // نجاح Task
            // ==========================================================

            task.setOnSucceeded(e -> {

                closeLoadingStage(
                        loadingStage
                );

                try {

                    Boolean result =
                            task.getValue();

                    if (Boolean.TRUE.equals(result)) {

                        if (onSuccessUI != null) {

                            onSuccessUI.run();
                        }

                    } else {

                        showError(
                                "تعذر إكمال العملية",
                                "لم تتم العملية المطلوبة بنجاح.",
                                "يرجى المحاولة مرة أخرى."
                        );
                    }

                } finally {

                    runFinishedCallback(
                            onFinishedUI
                    );
                }
            });

            // ==========================================================
            // فشل Task
            // ==========================================================

            task.setOnFailed(e -> {

                closeLoadingStage(
                        loadingStage
                );

                try {

                    Throwable ex =
                            task.getException();

                    if (ex == null) {

                        showError(
                                "خطأ غير معروف",
                                "تعذر تحديد سبب فشل العملية.",
                                "يرجى إعادة المحاولة."
                        );

                        return;
                    }

                    ex.printStackTrace();

                    // ==================================================
                    // خطأ شبكة
                    // ==================================================

                    if (ex instanceof NetworkException) {

                        showConnectionDialog(
                                ex.getMessage(),
                                loadingText,
                                backendTask,
                                onSuccessUI,
                                onFinishedUI,
                                authenticated
                        );

                        return;
                    }

                    // ==================================================
                    // خطأ API من السيرفر
                    // ==================================================

                    if (ex instanceof ApiClientException apiException) {

                        handleApiClientException(
                                apiException
                        );

                        return;
                    }

                    // ==================================================
                    // خطأ أمني محلي
                    // ==================================================

                    if (ex instanceof LocalSecurityException) {

                        showError(
                                "رفض العملية",
                                ex.getMessage() == null
                                        ? "لا يمكن تنفيذ العملية الحالية."
                                        : ex.getMessage(),
                                "تحقق من جلسة المستخدم وصلاحياته."
                        );

                        return;
                    }

                    // ==================================================
                    // خطأ عادي
                    // ==================================================

                    String message =
                            ex.getMessage() == null
                                    ? ex.toString()
                                    : ex.getMessage();

                    showError(
                            "حدث خطأ أثناء تنفيذ العملية",
                            message,
                            "إذا استمرت المشكلة، حاول مرة أخرى أو راجع سجل الأخطاء."
                    );

                } finally {

                    runFinishedCallback(
                            onFinishedUI
                    );
                }
            });

            // ==========================================================
            // إظهار نافذة الانتظار
            // ==========================================================

            loadingStage.show();

            // ==========================================================
            // تشغيل العملية
            // ==========================================================

            Thread thread =
                    new Thread(task);

            thread.setDaemon(true);

            thread.setName(
                    authenticated
                            ? "BackgroundRunner-Authenticated"
                            : "BackgroundRunner"
            );

            thread.start();
        });
    }

    // ==========================================================
    // معالجة خطأ ApiClient
    // ==========================================================

    private static void handleApiClientException(
            ApiClientException exception
    ) {

        String code =
                exception.getErrorCode();

        String message =
                exception.getServerMessage();

        // ------------------------------------------------------
        // جلسة منتهية أو غير صالحة
        // ------------------------------------------------------

        if (exception.isUnauthorized()) {

            SECURITY_GUARD
                    .clearLocalSession();

            showError(
                    "انتهت الجلسة",
                    message == null ||
                            message.isBlank()
                            ? "جلسة المستخدم غير صالحة أو انتهت."
                            : message,
                    "يرجى تسجيل الدخول مرة أخرى."
            );

            return;
        }

        // ------------------------------------------------------
        // حالات منع الوصول
        // ------------------------------------------------------

        if (exception.isForbidden()) {

            switch (
                    code == null
                            ? ""
                            : code
            ) {

                case "USER_INACTIVE":

                    showError(
                            "الحساب غير نشط",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "DEVICE_BLOCKED":

                    showError(
                            "الجهاز موقوف",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "DEVICE_REVOKED":

                    showError(
                            "الجهاز ملغى",
                            message,
                            "يجب تسجيل الجهاز من جديد."
                    );

                    return;

                case "DEVICE_INACTIVE":

                    showError(
                            "الجهاز غير نشط",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "SUBSCRIPTION_INACTIVE":

                    showError(
                            "الاشتراك غير نشط",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "SUBSCRIPTION_EXPIRED":

                    showError(
                            "انتهى الاشتراك",
                            message,
                            "يرجى تجديد اشتراك العيادة."
                    );

                    return;

                case "SUBSCRIPTION_NOT_STARTED":

                    showError(
                            "الاشتراك لم يبدأ",
                            message,
                            "لا يمكن استخدام النظام قبل بدء الاشتراك."
                    );

                    return;

                case "CLINIC_MISMATCH":
                case "DEVICE_CLINIC_MISMATCH":

                    showError(
                            "خطأ في ارتباط العيادة",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "FORBIDDEN":
                case "PERMISSION_DENIED":

                    showError(
                            "لا توجد صلاحية",
                            message,
                            "ليس لديك الصلاحية المطلوبة لتنفيذ هذه العملية."
                    );

                    return;

                default:

                    showError(
                            "الوصول مرفوض",
                            message == null ||
                                    message.isBlank()
                                    ? "تم رفض العملية."
                                    : message,
                            "إذا كنت ترى أن هذا غير صحيح، راجع مدير النظام."
                    );

                    return;
            }
        }

        // ------------------------------------------------------
        // بقية أخطاء API
        // ------------------------------------------------------

        if (exception.isNotFound()) {

            showError(
                    "العنصر غير موجود",
                    message,
                    "قد يكون العنصر قد تم حذفه أو لم يعد متاحًا."
            );

            return;
        }

        if (exception.isConflict()) {

            showError(
                    "تعارض في البيانات",
                    message,
                    "راجع البيانات الحالية وحاول مرة أخرى."
            );

            return;
        }

        if (exception.isServerError()) {

            showError(
                    "خطأ في الخادم",
                    message,
                    "حدث خطأ داخلي. حاول مرة أخرى لاحقًا."
            );

            return;
        }

        // ------------------------------------------------------
        // خطأ HTTP غير مصنف
        // ------------------------------------------------------

        showError(
                "فشل العملية",
                message == null ||
                        message.isBlank()
                        ? "فشل تنفيذ الطلب."
                        : message,
                "يرجى المحاولة مرة أخرى."
        );
    }

    // ==========================================================
    // Callback انتهاء العملية
    // ==========================================================

    private static void runFinishedCallback(
            Runnable onFinishedUI
    ) {

        if (onFinishedUI == null) {
            return;
        }

        if (Platform.isFxApplicationThread()) {

            onFinishedUI.run();

        } else {

            Platform.runLater(
                    onFinishedUI
            );
        }
    }

    // ==========================================================
    // إغلاق نافذة الانتظار
    // ==========================================================

    private static void closeLoadingStage(
            Stage loadingStage
    ) {

        if (loadingStage == null) {
            return;
        }

        if (loadingStage.isShowing()) {

            loadingStage.close();
        }
    }

    // ==========================================================
    // نافذة الاتصال
    // ==========================================================

    private static void showConnectionDialog(
            String message,
            String loadingText,
            Callable<Boolean> backendTask,
            Runnable onSuccessUI,
            Runnable onFinishedUI,
            boolean authenticated
    ) {

        Platform.runLater(() -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "الاتصال بالخادم"
            );

            alert.setHeaderText(null);

            Label title =
                    new Label(
                            "تعذر الاتصال بالخادم"
                    );

            title.setStyle("""
                -fx-font-size:18px;
                -fx-font-weight:bold;
                -fx-text-fill:#1f2937;
                """);

            Label description =
                    new Label(
                            message == null
                                    ? "تعذر الاتصال بالخادم."
                                    : message
                    );

            description.setWrapText(true);

            description.setStyle("""
                -fx-font-size:14px;
                -fx-text-fill:#4b5563;
                """);

            Label hint =
                    new Label(
                            "يمكنك إعادة المحاولة أو إغلاق التطبيق."
                    );

            hint.setWrapText(true);

            hint.setStyle("""
                -fx-font-size:13px;
                -fx-text-fill:#6b7280;
                """);

            VBox content =
                    new VBox(
                            10,
                            title,
                            description,
                            hint
                    );

            content.setPadding(
                    new Insets(10)
            );

            content.setPrefWidth(
                    420
            );

            content.setNodeOrientation(
                    NodeOrientation.RIGHT_TO_LEFT
            );

            alert.getDialogPane()
                    .setContent(content);

            ButtonType retryButton =
                    new ButtonType(
                            "إعادة المحاولة"
                    );

            ButtonType exitButton =
                    new ButtonType(
                            "إغلاق التطبيق"
                    );

            alert.getButtonTypes().setAll(
                    retryButton,
                    exitButton
            );

            alert.getDialogPane()
                    .setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );

            Optional<ButtonType> result =
                    alert.showAndWait();

            if (result.isPresent()
                    && result.get() == retryButton) {

                if (authenticated) {

                    runAuthenticated(
                            loadingText,
                            backendTask,
                            onSuccessUI,
                            onFinishedUI
                    );

                } else {

                    run(
                            loadingText,
                            backendTask,
                            onSuccessUI,
                            onFinishedUI
                    );
                }

            } else {

                Platform.exit();
            }
        });
    }

    // ==========================================================
    // نافذة الخطأ
    // ==========================================================

    private static void showError(
            String title,
            String message,
            String hint
    ) {

        Platform.runLater(() -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    title
            );

            alert.setHeaderText(null);

            Label titleLabel =
                    new Label(
                            title
                    );

            titleLabel.setWrapText(true);

            titleLabel.setStyle("""
                -fx-font-size:18px;
                -fx-font-weight:bold;
                -fx-text-fill:#1f2937;
                """);

            Label messageLabel =
                    new Label(
                            message == null ||
                                    message.isBlank()
                                    ? "حدث خطأ غير معروف."
                                    : message
                    );

            messageLabel.setWrapText(true);

            messageLabel.setStyle("""
                -fx-font-size:14px;
                -fx-text-fill:#4b5563;
                """);

            Label hintLabel =
                    new Label(
                            hint == null
                                    ? ""
                                    : hint
                    );

            hintLabel.setWrapText(true);

            hintLabel.setStyle("""
                -fx-font-size:13px;
                -fx-text-fill:#6b7280;
                """);

            VBox content =
                    new VBox(
                            10,
                            titleLabel,
                            messageLabel,
                            hintLabel
                    );

            content.setPadding(
                    new Insets(10)
            );

            content.setPrefWidth(
                    420
            );

            content.setNodeOrientation(
                    NodeOrientation.RIGHT_TO_LEFT
            );

            alert.getDialogPane()
                    .setContent(
                            content
                    );

            alert.getDialogPane()
                    .setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );

            alert.showAndWait();
        });
    }

    // ==========================================================
    // خطأ اتصال
    // ==========================================================

    private static class NetworkException
            extends Exception {

        public NetworkException(
                String message
        ) {

            super(message);
        }
    }

    // ==========================================================
    // خطأ أمني محلي
    // ==========================================================

    private static class LocalSecurityException
            extends Exception {

        public LocalSecurityException(
                String message
        ) {

            super(message);
        }
    }

    // ==========================================================
    // نافذة الانتظار
    // ==========================================================

    private static Stage createLoadingStage(
            String text
    ) {

        ProgressIndicator indicator =
                new ProgressIndicator();

        indicator.setPrefSize(
                55,
                55
        );

        Label title =
                new Label(
                        "يرجى الانتظار"
                );

        title.setStyle("""
            -fx-text-fill:white;
            -fx-font-size:18px;
            -fx-font-weight:bold;
            """);

        Label label =
                new Label(
                        text
                );

        label.setWrapText(true);

        label.setMaxWidth(
                300
        );

        label.setAlignment(
                Pos.CENTER
        );

        label.setTextAlignment(
                javafx.scene.text.TextAlignment.CENTER
        );

        label.setStyle("""
            -fx-text-fill:#e5e7eb;
            -fx-font-size:14px;
            """);

        VBox box =
                new VBox(
                        14,
                        indicator,
                        title,
                        label
                );

        box.setAlignment(
                Pos.CENTER
        );

        StackPane root =
                new StackPane(box);

        root.setPadding(
                new Insets(32)
        );

        root.setStyle("""
            -fx-background-color:rgba(17,24,39,0.94);
            -fx-background-radius:18;
            """);

        Scene scene =
                new Scene(root);

        scene.setFill(
                Color.TRANSPARENT
        );

        Stage stage =
                new Stage();

        stage.initStyle(
                StageStyle.TRANSPARENT
        );

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        stage.setScene(
                scene
        );

        stage.setResizable(
                false
        );

        return stage;
    }
}