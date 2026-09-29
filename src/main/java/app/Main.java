package app;

import api.ApiClientException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import security.ClientSecurityGuard;
import security.NetworkChecker;
import settings.AppSettings;
import user_interface.forminput.ClientView;
import user_interface.forminput.MealsView;
import user_interface.forminput.SessionView;
import user_interface.login.LoginView;
import user_interface.navbar.Navbar;
import user_interface.offer.HomeView;
import user_interface.subscription.SubscriptionView;

import java.net.URL;
import java.util.Optional;

/**
 * نقطة تشغيل تطبيق FAS.
 *
 * المسؤوليات:
 * - تشغيل JavaFX.
 * - فحص الخادم.
 * - استعادة الجلسة.
 * - عرض تسجيل الدخول.
 * - تشغيل التطبيق الرئيسي.
 * - التنقل بين الواجهات.
 * - إدارة Navbar.
 * - حفظ إعدادات النافذة.
 * - تحميل CSS.
 */
public class Main extends Application {

    // =========================================================
    // ROOT
    // =========================================================

    private static final BorderPane root =
            new BorderPane();

    private static AppSettings appSettings;

    private static Stage mainStage;

    private static String currentViewName =
            "Home";

    // =========================================================
    // NAVBAR
    // =========================================================

    private static Navbar navbar;

    // =========================================================
    // CSS
    // =========================================================

    private static final String MAIN_STYLESHEET =
            "/css/fas.css";

    // =========================================================
    // SECURITY
    // =========================================================

    private static final ClientSecurityGuard SECURITY_GUARD =
            ClientSecurityGuard.getInstance();

    // =========================================================
    // SERVER
    // =========================================================

    private static final String SERVER_URL =
            "http://localhost:8080/api/health";

    // =========================================================
    // ROOT ACCESS
    // =========================================================

    public static BorderPane getRoot() {
        return root;
    }

    // =========================================================
    // START
    // =========================================================

    @Override
    public void start(Stage stage) {

        mainStage = stage;

        mainStage.setResizable(true);

        appSettings =
                new AppSettings();

        /*
         * بدء التشغيل بشاشة تحميل.
         */
        showStartupLoading();

        /*
         * فحص الشبكة والجلسة.
         */
        startSecurityBootstrap();
    }

    // =========================================================
    // CSS
    // =========================================================

    private static void applyStylesheet(
            Scene scene
    ) {

        if (scene == null) {
            return;
        }

        URL css =
                Main.class.getResource(
                        MAIN_STYLESHEET
                );

        if (css == null) {

            throw new IllegalStateException(
                    "لم يتم العثور على ملف CSS:\n"
                            + MAIN_STYLESHEET
                            + "\n\n"
                            + "تأكد أن الملف موجود في:\n"
                            + "src/main/resources/css/fas.css"
            );
        }

        String stylesheet =
                css.toExternalForm();

        if (!scene.getStylesheets()
                .contains(stylesheet)) {

            scene.getStylesheets()
                    .add(stylesheet);
        }
    }

    // =========================================================
    // SECURITY BOOTSTRAP
    // =========================================================

    private void startSecurityBootstrap() {

        Task<StartupResult> task =
                new Task<>() {

                    @Override
                    protected StartupResult call()
                            throws Exception {

                        // =================================================
                        // SERVER
                        // =================================================

//                        if (!NetworkChecker.isServerAvailable(
//                                SERVER_URL
//                        )) {
//
//                            if (!NetworkChecker
//                                    .isInternetAvailable()) {
//
//                                return StartupResult.networkError(
//                                        "لا يوجد اتصال بالشبكة."
//                                );
//                            }
//
//                            return StartupResult.networkError(
//                                    "تعذر الاتصال بالخادم."
//                            );
//                        }

                        // =================================================
                        // SECURITY
                        // =================================================

                        ClientSecurityGuard.SecurityCheckResult result =
                                SECURITY_GUARD.checkStartup();

                        return StartupResult.securityResult(
                                result
                        );
                    }
                };

        // =========================================================
        // SUCCESS
        // =========================================================

        task.setOnSucceeded(
                event -> {

                    StartupResult result =
                            task.getValue();

                    handleStartupResult(
                            result
                    );
                }
        );

        // =========================================================
        // FAILURE
        // =========================================================

        task.setOnFailed(
                event -> {

                    Throwable exception =
                            task.getException();

                    handleStartupException(
                            exception
                    );
                }
        );

        // =========================================================
        // THREAD
        // =========================================================

        Thread thread =
                new Thread(
                        task,
                        "FAS-Startup-Security"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // =========================================================
    // STARTUP RESULT
    // =========================================================

    private void handleStartupResult(
            StartupResult result
    ) {

        System.out.println(
                "========== STARTUP RESULT =========="
        );

        if (result == null) {

            showStartupError(
                    "تعذر تشغيل التطبيق."
            );

            return;
        }

        // =========================================================
        // NETWORK
        // =========================================================

        if (result.networkError()) {

            showConnectionDialog(
                    result.message()
            );

            return;
        }

        // =========================================================
        // SECURITY
        // =========================================================

        ClientSecurityGuard.SecurityCheckResult securityResult =
                result.securityResult();

        if (securityResult == null) {

            showStartupError(
                    "تعذر التحقق من حالة الجلسة."
            );

            return;
        }

        System.out.println(
                "Security Result = "
                        + securityResult
        );

        System.out.println(
                "Security Status = "
                        + securityResult.getStatus()
        );

        System.out.println(
                "Security Allowed = "
                        + securityResult.isAllowed()
        );

        // =========================================================
        // VALID SESSION
        // =========================================================

        if (securityResult.isAllowed()) {

            openMainApplication();

            return;
        }

        // =========================================================
        // LOGIN REQUIRED
        // =========================================================

        if (securityResult.getStatus()
                ==
                ClientSecurityGuard.SecurityStatus.LOGIN_REQUIRED) {

            openLogin();

            return;
        }

        // =========================================================
        // INVALID SESSION
        // =========================================================

        if (securityResult.getStatus()
                ==
                ClientSecurityGuard.SecurityStatus.SESSION_INVALID) {

            showLoginAfterInvalidSession();

            return;
        }

        // =========================================================
        // UNKNOWN
        // =========================================================

        showStartupError(
                securityResult.getMessage() == null
                        ||
                        securityResult
                                .getMessage()
                                .isBlank()
                        ? "تعذر التحقق من حالة المستخدم."
                        : securityResult.getMessage()
        );
    }

    // =========================================================
    // STARTUP EXCEPTION
    // =========================================================

    private void handleStartupException(
            Throwable exception
    ) {

        if (exception != null) {
            exception.printStackTrace();
        }

        Throwable actualException =
                unwrapException(
                        exception
                );

        if (actualException
                instanceof ApiClientException apiException) {

            handleApiClientException(
                    apiException
            );

            return;
        }

        showConnectionDialog(
                "تعذر الاتصال بالخادم."
        );
    }

    // =========================================================
    // API EXCEPTION
    // =========================================================

    private void handleApiClientException(
            ApiClientException exception
    ) {

        String code =
                exception.getErrorCode();

        String message =
                exception.getServerMessage();

        // =========================================================
        // 401
        // =========================================================

        if (exception.isUnauthorized()) {

            SECURITY_GUARD.clearLocalSession();

            showStartupError(
                    message == null
                            || message.isBlank()
                            ? "جلسة المستخدم غير صالحة أو انتهت."
                            : message
            );

            openLogin();

            return;
        }

        // =========================================================
        // 403
        // =========================================================

        if (exception.isForbidden()) {

            switch (
                    code == null
                            ? ""
                            : code
            ) {

                case "DEVICE_BLOCKED":

                    showAccessError(
                            "الجهاز موقوف",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "DEVICE_REVOKED":

                    showAccessError(
                            "الجهاز ملغى",
                            message,
                            "يجب تسجيل الجهاز من جديد."
                    );

                    return;

                case "DEVICE_INACTIVE":

                    showAccessError(
                            "الجهاز غير نشط",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "SUBSCRIPTION_EXPIRED":

                    showAccessError(
                            "انتهى الاشتراك",
                            message,
                            "يرجى تجديد اشتراك العيادة."
                    );

                    return;

                case "SUBSCRIPTION_INACTIVE":

                    showAccessError(
                            "الاشتراك غير نشط",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                case "SUBSCRIPTION_NOT_STARTED":

                    showAccessError(
                            "الاشتراك لم يبدأ",
                            message,
                            "لا يمكن استخدام النظام قبل بدء الاشتراك."
                    );

                    return;

                case "USER_INACTIVE":

                    showAccessError(
                            "الحساب غير نشط",
                            message,
                            "يرجى التواصل مع مدير النظام."
                    );

                    return;

                default:

                    showAccessError(
                            "تم رفض الوصول",
                            message,
                            "يرجى مراجعة مدير النظام."
                    );
            }

            return;
        }

        // =========================================================
        // OTHER
        // =========================================================

        showAccessError(
                "خطأ أثناء تشغيل التطبيق",
                message,
                "يرجى المحاولة مرة أخرى."
        );
    }

    // =========================================================
    // UNWRAP
    // =========================================================

    private Throwable unwrapException(
            Throwable exception
    ) {

        Throwable current =
                exception;

        while (current != null
                && current.getCause() != null
                && current != current.getCause()) {

            if (current instanceof ApiClientException) {
                return current;
            }

            current =
                    current.getCause();
        }

        return current;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void openLogin() {

        Platform.runLater(() -> {

            try {

                LoginView loginView =
                        new LoginView();

                /*
                 * مهم جدًا:
                 *
                 * لا نضع LoginView في Scene Root مباشرة.
                 * نبقي root الرئيسي للتطبيق،
                 * ونزيل Navbar منه.
                 */
                root.setTop(null);

                root.setCenter(
                        loginView
                );

                currentViewName =
                        "Login";

                setRegionFill(
                        loginView
                );

                showRootScene();

                mainStage.setTitle(
                        "FAS - تسجيل الدخول"
                );

                mainStage.show();

            } catch (Exception e) {

                e.printStackTrace();

                showStartupError(
                        "تعذر فتح شاشة تسجيل الدخول."
                );
            }
        });
    }

    // =========================================================
    // MAIN APPLICATION
    // =========================================================

    public static void openMainApplication() {

        if (mainStage == null) {
            return;
        }

        Platform.runLater(() -> {

            try {

                // =====================================================
                // LAST VIEW
                // =====================================================

                String savedView =
                        appSettings.getLastView();

                if (savedView == null
                        || savedView.isBlank()
                        || "Subscription".equals(
                        savedView
                )
                        || "Login".equals(
                        savedView
                )) {

                    currentViewName =
                            "Home";

                } else {

                    currentViewName =
                            savedView;
                }

                // =====================================================
                // ROOT
                // =====================================================

                root.setTop(null);

                root.setCenter(null);

                // =====================================================
                // NAVBAR
                // =====================================================

                navbar =
                        new Navbar();

                root.setTop(
                        navbar
                );

                // =====================================================
                // LAST VIEW
                // =====================================================

                Node lastViewNode =
                        loadViewByName(
                                currentViewName
                        );

                if (lastViewNode == null) {

                    currentViewName =
                            "Home";

                    lastViewNode =
                            new HomeView();
                }

                setRegionFill(
                        lastViewNode
                );

                root.setCenter(
                        lastViewNode
                );

                // =====================================================
                // SCENE
                // =====================================================

                showRootScene();

                // =====================================================
                // CSS
                // =====================================================

                Scene scene =
                        mainStage.getScene();

                applyStylesheet(
                        scene
                );

                // =====================================================
                // WINDOW
                // =====================================================

                mainStage.setMinWidth(
                        800
                );

                mainStage.setMinHeight(
                        600
                );

                mainStage.setResizable(
                        true
                );

                mainStage.setTitle(
                        "FAS - Food Assistant System"
                );

                // =====================================================
                // SIZE
                // =====================================================

                if (!mainStage.isMaximized()) {

                    double width =
                            appSettings.getWidth();

                    double height =
                            appSettings.getHeight();

                    double x =
                            appSettings.getPosX();

                    double y =
                            appSettings.getPosY();

                    if (width < 800) {
                        width = 1200;
                    }

                    if (height < 600) {
                        height = 750;
                    }

                    mainStage.setWidth(
                            width
                    );

                    mainStage.setHeight(
                            height
                    );

                    if (!Double.isNaN(x)
                            && !Double.isInfinite(x)) {

                        mainStage.setX(
                                x
                        );
                    }

                    if (!Double.isNaN(y)
                            && !Double.isInfinite(y)) {

                        mainStage.setY(
                                y
                        );
                    }
                }

                // =====================================================
                // MAXIMIZED
                // =====================================================

                if (appSettings.isMaximized()) {

                    mainStage.setMaximized(
                            true
                    );
                }

                mainStage.show();

            } catch (Exception e) {

                e.printStackTrace();

                showStartupError(
                        "تعذر تشغيل التطبيق."
                                + "\n\n"
                                + e.getMessage()
                );
            }
        });
    }

    // =========================================================
    // CHANGE VIEW
    // =========================================================

    public static void setView(
            Node newView,
            String viewName
    ) {

        if (newView == null
                || mainStage == null) {

            return;
        }

        Platform.runLater(() -> {

            try {

                /*
                 * نتأكد أن Scene تستخدم root الرئيسي.
                 */
                showRootScene();

                // =====================================================
                // LOGIN
                // =====================================================

                if ("Login".equals(
                        viewName
                )) {

                    /*
                     * أهم إصلاح:
                     *
                     * إزالة Navbar بالكامل
                     * عند فتح LoginView.
                     */
                    root.setTop(null);

                    root.setCenter(
                            newView
                    );

                    /*
                     * Login ليس View يتم حفظه
                     * باعتباره آخر صفحة داخل النظام.
                     */
                    currentViewName =
                            "Login";

                    setRegionFill(
                            newView
                    );

                    Scene scene =
                            mainStage.getScene();

                    applyStylesheet(
                            scene
                    );

                    mainStage.setTitle(
                            "FAS - تسجيل الدخول"
                    );

                    mainStage.show();

                    return;
                }

                // =====================================================
                // INTERNAL APPLICATION VIEW
                // =====================================================

                /*
                 * إذا لم يكن هناك Navbar،
                 * نعيد إنشاءه.
                 *
                 * أما إذا كان موجودًا،
                 * فنستخدم نفس الـNavbar.
                 */
                if (!(root.getTop()
                        instanceof Navbar)) {

                    navbar =
                            new Navbar();

                    root.setTop(
                            navbar
                    );
                }

                // =====================================================
                // CENTER
                // =====================================================

                setRegionFill(
                        newView
                );

                root.setCenter(
                        newView
                );

                currentViewName =
                        normalizeViewName(
                                viewName
                        );

                // =====================================================
                // CSS
                // =====================================================

                Scene scene =
                        mainStage.getScene();

                applyStylesheet(
                        scene
                );

                mainStage.setTitle(
                        "FAS - Food Assistant System"
                );

                mainStage.show();

            } catch (Exception e) {

                e.printStackTrace();

                showStartupError(
                        "تعذر تغيير الواجهة."
                );
            }
        });
    }

    // =========================================================
    // LOAD VIEW
    // =========================================================

    private static Node loadViewByName(
            String viewName
    ) {

        try {

            if (viewName == null
                    || viewName.isBlank()) {

                return new HomeView();
            }

            return switch (viewName) {

                case "Home" ->

                        new HomeView();

                case "Client" ->

                        new ClientView();

                case "Session" ->

                        new SessionView();

                case "Meal" ->

                        new MealsView();

                case "Subscription" ->

                        new SubscriptionView();

                default ->

                        new HomeView();
            };

        } catch (Exception e) {

            e.printStackTrace();

            return new HomeView();
        }
    }

    // =========================================================
    // NORMALIZE VIEW
    // =========================================================

    private static String normalizeViewName(
            String viewName
    ) {

        if (viewName == null
                || viewName.isBlank()) {

            return "Home";
        }

        return viewName;
    }

    // =========================================================
    // INITIAL WIDTH
    // =========================================================

    private static double getInitialWidth() {

        double width =
                appSettings.getWidth();

        if (width < 800) {
            return 1200;
        }

        return width;
    }

    // =========================================================
    // INITIAL HEIGHT
    // =========================================================

    private static double getInitialHeight() {

        double height =
                appSettings.getHeight();

        if (height < 600) {
            return 750;
        }

        return height;
    }

    // =========================================================
    // REGION SIZE
    // =========================================================

    private static void setRegionFill(
            Node node
    ) {

        if (node instanceof javafx.scene.layout.Region region) {

            region.setMaxWidth(
                    Double.MAX_VALUE
            );

            region.setMaxHeight(
                    Double.MAX_VALUE
            );
        }
    }

    // =========================================================
    // SHOW ROOT SCENE
    // =========================================================

    private static void showRootScene() {

        if (mainStage == null) {
            return;
        }

        Scene scene =
                mainStage.getScene();

        // =====================================================
        // CREATE SCENE
        // =====================================================

        if (scene == null) {

            scene =
                    new Scene(
                            root,
                            getInitialWidth(),
                            getInitialHeight()
                    );

            mainStage.setScene(
                    scene
            );

        } else {

            /*
             * إذا كان هناك LoginView أو شاشة البداية
             * كـ Scene Root، نعيد root الرئيسي.
             */
            if (scene.getRoot()
                    != root) {

                scene.setRoot(
                        root
                );
            }
        }

        // =====================================================
        // CSS
        // =====================================================

        applyStylesheet(
                scene
        );
    }

    // =========================================================
    // STARTUP LOADING
    // =========================================================

    private void showStartupLoading() {

        Platform.runLater(() -> {

            if (mainStage == null) {
                return;
            }

            ProgressIndicator indicator =
                    new ProgressIndicator();

            indicator.setPrefSize(
                    55,
                    55
            );

            Label message =
                    new Label(
                            "يرجى الانتظار..."
                    );

            message.setStyle("""
                -fx-font-size:16px;
                -fx-font-weight:bold;
                -fx-text-fill:#475569;
            """);

            VBox box =
                    new VBox(
                            18,
                            indicator,
                            message
                    );

            box.setAlignment(
                    Pos.CENTER
            );

            box.setPadding(
                    new Insets(40)
            );

            box.setNodeOrientation(
                    NodeOrientation.RIGHT_TO_LEFT
            );

            BorderPane startupPane =
                    new BorderPane();

            startupPane.setCenter(
                    box
            );

            Scene scene =
                    new Scene(
                            startupPane,
                            600,
                            400
                    );

            applyStylesheet(
                    scene
            );

            mainStage.setScene(
                    scene
            );

            mainStage.setTitle(
                    "FAS"
            );

            mainStage.show();
        });
    }

    // =========================================================
    // CONNECTION DIALOG
    // =========================================================

    private void showConnectionDialog(
            String message
    ) {

        Platform.runLater(() -> {

            if (mainStage == null) {
                return;
            }

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "تعذر الاتصال"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    message == null
                            || message.isBlank()
                            ? "تعذر الاتصال بالخادم."
                            : message
            );

            ButtonType retry =
                    new ButtonType(
                            "إعادة المحاولة"
                    );

            ButtonType exit =
                    new ButtonType(
                            "إغلاق التطبيق"
                    );

            alert.getButtonTypes()
                    .setAll(
                            retry,
                            exit
                    );

            alert.getDialogPane()
                    .setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );

            Optional<ButtonType> result =
                    alert.showAndWait();

            if (result.isPresent()
                    && result.get() == retry) {

                showStartupLoading();

                startSecurityBootstrap();

            } else {

                Platform.exit();
            }
        });
    }

    // =========================================================
    // INVALID SESSION
    // =========================================================

    private void showLoginAfterInvalidSession() {

        Platform.runLater(() -> {

            if (mainStage == null) {
                return;
            }

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setTitle(
                    "تسجيل الدخول"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "انتهت جلسة المستخدم.\n\n"
                            + "يرجى تسجيل الدخول مرة أخرى."
            );

            alert.getDialogPane()
                    .setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );

            alert.showAndWait();

            openLogin();
        });
    }

    // =========================================================
    // ACCESS ERROR
    // =========================================================

    private static void showAccessError(
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

            alert.setHeaderText(
                    null
            );

            String finalMessage =
                    message == null
                            || message.isBlank()
                            ? "تم رفض الوصول."
                            : message;

            alert.setContentText(
                    finalMessage
                            + "\n\n"
                            + (
                            hint == null
                                    ? ""
                                    : hint
                    )
            );

            alert.getDialogPane()
                    .setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );

            alert.showAndWait();
        });
    }

    // =========================================================
    // STARTUP ERROR
    // =========================================================

    private static void showStartupError(
            String message
    ) {

        Platform.runLater(() -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "خطأ"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    message == null
                            || message.isBlank()
                            ? "تعذر تشغيل التطبيق."
                            : message
            );

            alert.getDialogPane()
                    .setNodeOrientation(
                            NodeOrientation.RIGHT_TO_LEFT
                    );

            alert.showAndWait();
        });
    }

    // =========================================================
    // STOP
    // =========================================================

    @Override
    public void stop() {

        if (appSettings == null
                || mainStage == null) {

            return;
        }

        try {

            /*
             * لا نحفظ Login باعتباره آخر View.
             */
            String viewToSave =
                    "Login".equals(
                            currentViewName
                    )
                            ? "Home"
                            : currentViewName;

            appSettings.saveSettings(
                    mainStage.getWidth(),
                    mainStage.getHeight(),
                    mainStage.getX(),
                    mainStage.getY(),
                    mainStage.isMaximized(),
                    viewToSave
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args
    ) {

        launch(args);
    }

    // =========================================================
    // STARTUP RESULT
    // =========================================================

    private record StartupResult(
            boolean networkError,
            String message,
            ClientSecurityGuard.SecurityCheckResult securityResult
    ) {

        static StartupResult networkError(
                String message
        ) {

            return new StartupResult(
                    true,
                    message,
                    null
            );
        }

        static StartupResult securityResult(
                ClientSecurityGuard.SecurityCheckResult result
        ) {

            return new StartupResult(
                    false,
                    null,
                    result
            );
        }
    }
}