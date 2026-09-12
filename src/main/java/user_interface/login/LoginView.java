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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import runner.BackgroundRunner;


/**
 * ============================================================
 * FAS - Login View
 * ============================================================
 *
 * واجهة تسجيل الدخول للعميل.
 *
 * المسؤوليات:
 *
 * 1. عرض واجهة تسجيل الدخول.
 * 2. التحقق المحلي من البيانات.
 * 3. قراءة Installation ID.
 * 4. إرسال طلب تسجيل الدخول إلى Backend.
 * 5. عرض حالة الاتصال للمستخدم.
 * 6. التعامل مع First Login Setup.
 * 7. فتح الواجهة الرئيسية بعد نجاح الدخول.
 *
 * ============================================================
 */
public class LoginView extends BorderPane {

    // ============================================================
    // Constants
    // ============================================================

    private static final String BACKGROUND_IMAGE =
            "/img/fas_login_background.png";

    private static final String PRIMARY =
            "#176B5A";

    private static final String PRIMARY_DARK =
            "#105044";

    private static final String PRIMARY_LIGHT =
            "#E8F5F1";

    private static final String TEXT_PRIMARY =
            "#20332E";

    private static final String TEXT_SECONDARY =
            "#6E7D78";

    private static final String BORDER =
            "#D9E5E0";

    private static final String ERROR =
            "#C62828";

    private static final String ERROR_BACKGROUND =
            "#FFF4F4";

    private static final String SUCCESS =
            "#2E7D32";

    private static final String SUCCESS_BACKGROUND =
            "#EDF8F2";


    // ============================================================
    // Fields
    // ============================================================

    private final TextField usernameField =
            new TextField();

    private final PasswordField passwordField =
            new PasswordField();

    private final Button loginButton =
            new Button("تسجيل الدخول");

    private final Label statusLabel =
            new Label();

    private final ProgressIndicator progressIndicator =
            new ProgressIndicator();

    private final AuthAPI authAPI;


    // ============================================================
    // Constructor
    // ============================================================

    public LoginView() {

        authAPI =
                ClientApiManager
                        .getInstance()
                        .getAuthAPI();

        buildUI();
    }


    // ============================================================
    // Build UI
    // ============================================================

    private void buildUI() {

        setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );

        setStyle("""
            -fx-background-color: #EAF1EE;
        """);


        // --------------------------------------------------------
        // Background
        // --------------------------------------------------------

        StackPane root =
                createBackground();


        // --------------------------------------------------------
        // Login Card
        // --------------------------------------------------------

        VBox loginCard =
                createLoginCard();

        StackPane.setAlignment(
                loginCard,
                Pos.CENTER
        );

        StackPane.setMargin(
                loginCard,
                new Insets(35)
        );


        root.getChildren().add(
                loginCard
        );


        setCenter(root);


        usernameField.requestFocus();
    }


    // ============================================================
    // Background
    // ============================================================

    private StackPane createBackground() {

        StackPane root = new StackPane();

        // يجب أن تتمدد الخلفية مع مساحة الـ BorderPane في كل حالات العرض،
        // بما فيها الانتقال إلى شاشة تسجيل الدخول بعد تسجيل الخروج.
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setMinSize(0, 0);
        root.setStyle("""
            -fx-background-color:
                linear-gradient(
                    to bottom right,
                    #EAF5F1,
                    #D9EBE5
                );
        """);

        Image backgroundImage = loadBackgroundImage();

        if (backgroundImage != null) {

            ImageView imageView = new ImageView(backgroundImage);

            /*
             * الخلفية هنا يجب أن تملأ الحاوية بالكامل.
             * وضع preserveRatio = false مهم لأننا نربط العرض
             * والارتفاع مع أبعاد الواجهة معاً.
             *
             * بهذه الطريقة لا يبقى فراغ في اليمين أو اليسار
             * عندما تتغير أبعاد النافذة.
             */
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageView.setCache(true);
            imageView.setManaged(false);
            imageView.setMouseTransparent(true);

            imageView.fitWidthProperty().bind(root.widthProperty());
            imageView.fitHeightProperty().bind(root.heightProperty());

            StackPane.setAlignment(imageView, Pos.CENTER);

            root.getChildren().add(imageView);
        }

        // طبقة خفيفة فوق الصورة حتى تبقى عناصر تسجيل الدخول واضحة.
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(245, 250, 248, 0.40));
        overlay.setMouseTransparent(true);

        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());

        StackPane.setAlignment(overlay, Pos.CENTER);
        root.getChildren().add(overlay);

        return root;
    }


    // ============================================================
    // Load Background Image
    // ============================================================

    private Image loadBackgroundImage() {

        try {

            var stream =
                    getClass()
                            .getResourceAsStream(
                                    BACKGROUND_IMAGE
                            );


            if (stream == null) {

                System.err.println(
                        "FAS: Background image not found: "
                                + BACKGROUND_IMAGE
                );

                return null;
            }


            return new Image(stream);

        } catch (Exception e) {

            System.err.println(
                    "FAS: Failed to load background image."
            );

            e.printStackTrace();

            return null;
        }
    }


    // ============================================================
    // Login Card
    // ============================================================

    private VBox createLoginCard() {

        VBox card =
                new VBox(16);

        card.setAlignment(
                Pos.TOP_CENTER
        );

        card.setPadding(
                new Insets(
                        38,
                        42,
                        32,
                        42
                )
        );

        card.setPrefWidth(
                445
        );

        card.setMaxWidth(
                445
        );

        card.setMaxHeight(
                620
        );

        card.setNodeOrientation(
                NodeOrientation.RIGHT_TO_LEFT
        );


        card.setStyle("""
            -fx-background-color:
                rgba(255,255,255,0.94);

            -fx-background-radius: 22;

            -fx-border-color:
                rgba(255,255,255,0.95);

            -fx-border-width: 1;

            -fx-border-radius: 22;
        """);


        DropShadow shadow =
                new DropShadow();

        shadow.setRadius(35);

        shadow.setSpread(0.02);

        shadow.setOffsetX(0);

        shadow.setOffsetY(12);

        shadow.setColor(
                Color.rgb(
                        15,
                        60,
                        48,
                        0.22
                )
        );

        card.setEffect(shadow);


        // --------------------------------------------------------
        // Logo
        // --------------------------------------------------------

        StackPane logo =
                createLogo();


        // --------------------------------------------------------
        // Title
        // --------------------------------------------------------

        Label title =
                new Label(
                        "مرحبًا بك في FAS"
                );

        title.setStyle("""
            -fx-font-size: 27px;
            -fx-font-weight: bold;
            -fx-text-fill: #20332E;
        """);

        title.setAlignment(
                Pos.CENTER
        );


        // --------------------------------------------------------
        // Subtitle
        // --------------------------------------------------------

        Label subtitle =
                new Label(
                        "سجّل الدخول للمتابعة إلى النظام"
                );

        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6E7D78;
        """);

        subtitle.setWrapText(true);

        subtitle.setAlignment(
                Pos.CENTER
        );


        // --------------------------------------------------------
        // Accent
        // --------------------------------------------------------

        Region accent =
                new Region();

        accent.setMinSize(
                45,
                3
        );

        accent.setMaxSize(
                45,
                3
        );

        accent.setStyle("""
            -fx-background-color: #1E8B73;
            -fx-background-radius: 5;
        """);


        // --------------------------------------------------------
        // Username
        // --------------------------------------------------------

        VBox usernameBox =
                createUsernameField();


        // --------------------------------------------------------
        // Password
        // --------------------------------------------------------

        VBox passwordBox =
                createPasswordField();


        // --------------------------------------------------------
        // Login Button
        // --------------------------------------------------------

        configureLoginButton();


        // --------------------------------------------------------
        // Status
        // --------------------------------------------------------

        HBox statusBox =
                createStatusBox();


        // --------------------------------------------------------
        // Footer
        // --------------------------------------------------------

        Region divider =
                new Region();

        divider.setPrefHeight(1);

        divider.setMaxWidth(
                Double.MAX_VALUE
        );

        divider.setStyle(
                "-fx-background-color: #E7EEEB;"
        );


        Label footer =
                new Label(
                        "FAS Client  •  Food Assistant System"
                );

        footer.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #94A29D;
        """);

        footer.setAlignment(
                Pos.CENTER
        );


        // --------------------------------------------------------
        // Spacer
        // --------------------------------------------------------

        Region spacer =
                new Region();

        spacer.setMinHeight(1);


        // --------------------------------------------------------
        // Add Components
        // --------------------------------------------------------

        card.getChildren().addAll(

                logo,

                title,

                subtitle,

                accent,

                spacer,

                usernameBox,

                passwordBox,

                loginButton,

                statusBox,

                divider,

                footer
        );


        return card;
    }


    // ============================================================
    // Logo
    // ============================================================

    private StackPane createLogo() {

        StackPane logo =
                new StackPane();

        logo.setMinSize(
                82,
                82
        );

        logo.setMaxSize(
                82,
                82
        );

        logo.setStyle("""
            -fx-background-color: #E8F5F1;

            -fx-background-radius: 24;

            -fx-border-color: #CEE6DE;

            -fx-border-width: 1;

            -fx-border-radius: 24;
        """);


        Label fas =
                new Label("FAS");

        fas.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #176B5A;
        """);


        logo.getChildren().add(fas);

        return logo;
    }


    // ============================================================
    // Username Field
    // ============================================================

    private VBox createUsernameField() {

        VBox box =
                new VBox(7);

        box.setFillWidth(true);


        Label label =
                new Label(
                        "اسم المستخدم"
                );

        label.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #34443F;
        """);


        usernameField.setPromptText(
                "أدخل اسم المستخدم"
        );

        usernameField.setPrefHeight(48);

        usernameField.setMaxWidth(
                Double.MAX_VALUE
        );

        usernameField.setStyle(
                normalFieldStyle()
        );


        addFocusBehavior(
                usernameField
        );


        usernameField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (!newValue.isBlank()) {

                                clearFieldError(
                                        usernameField
                                );
                            }
                        }
                );


        box.getChildren().addAll(
                label,
                usernameField
        );

        return box;
    }


    // ============================================================
    // Password Field
    // ============================================================

    private VBox createPasswordField() {

        VBox box =
                new VBox(7);

        box.setFillWidth(true);


        Label label =
                new Label(
                        "كلمة المرور"
                );

        label.setStyle("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #34443F;
        """);


        passwordField.setPromptText(
                "أدخل كلمة المرور"
        );

        passwordField.setPrefHeight(48);

        passwordField.setMaxWidth(
                Double.MAX_VALUE
        );

        passwordField.setStyle(
                normalFieldStyle()
        );


        addFocusBehavior(
                passwordField
        );


        passwordField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (!newValue.isBlank()) {

                                clearFieldError(
                                        passwordField
                                );
                            }
                        }
                );


        box.getChildren().addAll(
                label,
                passwordField
        );

        return box;
    }


    // ============================================================
    // Focus Behavior
    // ============================================================

    private void addFocusBehavior(
            TextField field
    ) {

        field.focusedProperty()
                .addListener(
                        (observable, oldValue, focused) -> {

                            if (focused) {

                                field.setStyle(
                                        focusedFieldStyle()
                                );

                            } else {

                                if (!isErrorStyle(field)) {

                                    field.setStyle(
                                            normalFieldStyle()
                                    );
                                }
                            }
                        }
                );
    }


    // ============================================================
    // Login Button
    // ============================================================

    private void configureLoginButton() {

        loginButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loginButton.setPrefHeight(50);

        loginButton.setStyle(
                normalButtonStyle()
        );


        loginButton.setOnMouseEntered(
                event -> {

                    if (!loginButton.isDisabled()) {

                        loginButton.setStyle(
                                hoverButtonStyle()
                        );
                    }
                }
        );


        loginButton.setOnMouseExited(
                event -> {

                    if (!loginButton.isDisabled()) {

                        loginButton.setStyle(
                                normalButtonStyle()
                        );
                    }
                }
        );


        loginButton.setOnAction(
                event ->
                        handleLogin()
        );


        usernameField.setOnAction(
                event ->
                        passwordField.requestFocus()
        );


        passwordField.setOnAction(
                event ->
                        handleLogin()
        );
    }


    // ============================================================
    // Status Box
    // ============================================================

    private HBox createStatusBox() {

        HBox box =
                new HBox(9);

        box.setAlignment(
                Pos.CENTER
        );

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setPadding(
                new Insets(
                        9,
                        12,
                        9,
                        12
                )
        );

        box.setVisible(false);

        box.setManaged(false);


        progressIndicator.setMaxSize(
                17,
                17
        );

        progressIndicator.setPrefSize(
                17,
                17
        );

        progressIndicator.setVisible(false);


        box.getChildren().addAll(
                progressIndicator,
                statusLabel
        );

        return box;
    }


    // ============================================================
    // Login
    // ============================================================

    private void handleLogin() {

        clearStatus();


        String username =
                usernameField
                        .getText()
                        .trim();

        String password =
                passwordField.getText();


        // --------------------------------------------------------
        // Username Validation
        // --------------------------------------------------------

        if (username.isBlank()) {

            showFieldError(
                    usernameField,
                    "يرجى إدخال اسم المستخدم."
            );

            usernameField.requestFocus();

            return;
        }


        // --------------------------------------------------------
        // Password Validation
        // --------------------------------------------------------

        if (password == null ||
                password.isBlank()) {

            showFieldError(
                    passwordField,
                    "يرجى إدخال كلمة المرور."
            );

            passwordField.requestFocus();

            return;
        }


        // --------------------------------------------------------
        // Installation ID
        // --------------------------------------------------------

        final String installationId;

        try {

            installationId =
                    InstallationIdentity
                            .getInstallationId();

        } catch (Exception e) {

            showStatus(
                    "تعذر التحقق من هوية هذا الجهاز.",
                    false,
                    false
            );

            return;
        }


        // --------------------------------------------------------
        // Request
        // --------------------------------------------------------

        UserLoginRequest request =
                new UserLoginRequest(
                        username,
                        password,
                        installationId
                );


        // --------------------------------------------------------
        // Loading
        // --------------------------------------------------------

        setLoadingState();


        final UserLoginResponse[] responseHolder =
                new UserLoginResponse[1];

        final String[] errorHolder =
                new String[1];


        // --------------------------------------------------------
        // Background
        // --------------------------------------------------------

        BackgroundRunner.run(

                "جاري التحقق من بيانات الدخول...",

                () -> {

                    try {

                        responseHolder[0] =
                                authAPI.login(
                                        request
                                );

                        return true;

                    } catch (Exception e) {

                        errorHolder[0] =
                                extractMessage(e);

                        return false;
                    }
                },


                // -------------------------------------------------
                // UI After Completion
                // -------------------------------------------------

                () -> {

                    if (errorHolder[0] != null) {

                        showStatus(
                                errorHolder[0],
                                false,
                                false
                        );

                        return;
                    }


                    if (responseHolder[0] == null) {

                        showStatus(
                                "لم يستجب السيرفر ببيانات تسجيل الدخول.",
                                false,
                                false
                        );

                        return;
                    }


                    UserLoginResponse response =
                            responseHolder[0];


                    if (!response.isSuccess()) {

                        String message =
                                response.getMessage();

                        if (message == null ||
                                message.isBlank()) {

                            message =
                                    "اسم المستخدم أو كلمة المرور غير صحيحة.";
                        }


                        showStatus(
                                message,
                                false,
                                false
                        );


                        passwordField.clear();

                        passwordField.requestFocus();

                        return;
                    }


                    showStatus(
                            response.getMessage() == null
                                    ? "تم تسجيل الدخول بنجاح."
                                    : response.getMessage(),
                            true,
                            false
                    );


                    // ------------------------------------------------
                    // First Login Setup
                    // ------------------------------------------------

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


                    // ------------------------------------------------
                    // Normal Login
                    // ------------------------------------------------

                    Main.openMainApplication();
                },


                this::setNormalState
        );
    }


    // ============================================================
    // Loading State
    // ============================================================

    private void setLoadingState() {

        loginButton.setDisable(true);

        usernameField.setDisable(true);

        passwordField.setDisable(true);


        loginButton.setText(
                "جاري تسجيل الدخول..."
        );

        loginButton.setOpacity(0.82);


        progressIndicator.setVisible(true);


        showStatus(
                "جاري التحقق من بيانات الدخول...",
                true,
                true
        );
    }


    // ============================================================
    // Normal State
    // ============================================================

    private void setNormalState() {

        loginButton.setDisable(false);

        usernameField.setDisable(false);

        passwordField.setDisable(false);


        loginButton.setText(
                "تسجيل الدخول"
        );

        loginButton.setOpacity(1);


        progressIndicator.setVisible(false);
    }


    // ============================================================
    // Show Status
    // ============================================================

    private void showStatus(
            String message,
            boolean success,
            boolean loading
    ) {

        statusLabel.setText(
                message == null ||
                        message.isBlank()
                        ? "حدث خطأ غير معروف."
                        : message
        );


        statusLabel.setWrapText(true);

        statusLabel.setAlignment(
                Pos.CENTER
        );


        if (loading) {

            statusLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-text-fill: #176B5A;
            """);

        } else if (success) {

            statusLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-text-fill: #2E7D32;
            """);

        } else {

            statusLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-text-fill: #C62828;
            """);
        }


        Node parent =
                statusLabel.getParent();


        if (parent instanceof HBox statusBox) {

            if (loading) {

                statusBox.setStyle("""
                    -fx-background-color: #EEF8F5;
                    -fx-border-color: #D6EDE5;
                    -fx-border-width: 1;
                    -fx-border-radius: 9;
                    -fx-background-radius: 9;
                """);

            } else if (success) {

                statusBox.setStyle(
                        SUCCESS_BACKGROUND_STYLE()
                );

            } else {

                statusBox.setStyle("""
                    -fx-background-color: #FFF4F4;
                    -fx-border-color: #F3D0D0;
                    -fx-border-width: 1;
                    -fx-border-radius: 9;
                    -fx-background-radius: 9;
                """);
            }


            statusBox.setVisible(true);

            statusBox.setManaged(true);
        }
    }


    // ============================================================
    // Success Background
    // ============================================================

    private String SUCCESS_BACKGROUND_STYLE() {

        return """
            -fx-background-color: #EDF8F2;
            -fx-border-color: #D2EBD9;
            -fx-border-width: 1;
            -fx-border-radius: 9;
            -fx-background-radius: 9;
        """;
    }


    // ============================================================
    // Clear Status
    // ============================================================

    private void clearStatus() {

        statusLabel.setText("");

        progressIndicator.setVisible(false);


        Node parent =
                statusLabel.getParent();


        if (parent instanceof HBox statusBox) {

            statusBox.setVisible(false);

            statusBox.setManaged(false);
        }
    }


    // ============================================================
    // Field Error
    // ============================================================

    private void showFieldError(
            TextField field,
            String message
    ) {

        field.setStyle(
                errorFieldStyle()
        );


        javafx.scene.control.Tooltip tooltip =
                new javafx.scene.control.Tooltip(
                        message
                );


        tooltip.setStyle("""
            -fx-background-color: #C62828;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-padding: 8 12;
            -fx-background-radius: 7;
        """);


        javafx.scene.control.Tooltip.install(
                field,
                tooltip
        );


        field.requestFocus();
    }


    // ============================================================
    // Clear Field Error
    // ============================================================

    private void clearFieldError(
            TextField field
    ) {

        javafx.scene.control.Tooltip.uninstall(
                field,
                null
        );


        if (field.isFocused()) {

            field.setStyle(
                    focusedFieldStyle()
            );

        } else {

            field.setStyle(
                    normalFieldStyle()
            );
        }
    }


    // ============================================================
    // Error Style Check
    // ============================================================

    private boolean isErrorStyle(
            TextField field
    ) {

        return field.getStyle()
                .contains(
                        "#C62828"
                );
    }


    // ============================================================
    // Normal Field Style
    // ============================================================

    private String normalFieldStyle() {

        return """
            -fx-background-color: #FBFDFC;

            -fx-border-color: #D9E5E0;
            -fx-border-width: 1.2;
            -fx-border-radius: 10;
            -fx-background-radius: 10;

            -fx-padding: 0 14;

            -fx-font-size: 14px;

            -fx-text-fill: #20332E;

            -fx-prompt-text-fill: #A3AEAA;
        """;
    }


    // ============================================================
    // Focused Field Style
    // ============================================================

    private String focusedFieldStyle() {

        return """
            -fx-background-color: white;

            -fx-border-color: #176B5A;
            -fx-border-width: 1.7;
            -fx-border-radius: 10;
            -fx-background-radius: 10;

            -fx-padding: 0 14;

            -fx-font-size: 14px;

            -fx-text-fill: #20332E;

            -fx-prompt-text-fill: #A3AEAA;
        """;
    }


    // ============================================================
    // Error Field Style
    // ============================================================

    private String errorFieldStyle() {

        return """
            -fx-background-color: #FFF9F9;

            -fx-border-color: #C62828;
            -fx-border-width: 1.6;
            -fx-border-radius: 10;
            -fx-background-radius: 10;

            -fx-padding: 0 14;

            -fx-font-size: 14px;

            -fx-text-fill: #20332E;

            -fx-prompt-text-fill: #A3AEAA;
        """;
    }


    // ============================================================
    // Normal Button Style
    // ============================================================

    private String normalButtonStyle() {

        return """
            -fx-background-color:
                linear-gradient(
                    to right,
                    #176B5A,
                    #1E8B73
                );

            -fx-background-radius: 10;

            -fx-text-fill: white;

            -fx-font-size: 15px;

            -fx-font-weight: bold;

            -fx-cursor: hand;

            -fx-effect:
                dropshadow(
                    gaussian,
                    rgba(23,107,90,0.23),
                    12,
                    0.15,
                    0,
                    4
                );
        """;
    }


    // ============================================================
    // Hover Button Style
    // ============================================================

    private String hoverButtonStyle() {

        return """
            -fx-background-color:
                linear-gradient(
                    to right,
                    #105044,
                    #176B5A
                );

            -fx-background-radius: 10;

            -fx-text-fill: white;

            -fx-font-size: 15px;

            -fx-font-weight: bold;

            -fx-cursor: hand;

            -fx-effect:
                dropshadow(
                    gaussian,
                    rgba(23,107,90,0.34),
                    16,
                    0.18,
                    0,
                    5
                );
        """;
    }


    // ============================================================
    // Extract Error Message
    // ============================================================

    private String extractMessage(
            Throwable throwable
    ) {

        if (throwable == null) {

            return "تعذر الاتصال بالسيرفر.";
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


        String lower =
                message.toLowerCase();


        if (
                lower.contains("connect") ||
                        lower.contains("connection") ||
                        lower.contains("timeout") ||
                        lower.contains("refused") ||
                        lower.contains("unreachable") ||
                        lower.contains("connection reset")
        ) {

            return """
                    تعذر الاتصال بسيرفر FAS.

                    تحقق من:
                    • اتصال الشبكة.
                    • تشغيل سيرفر FAS.
                    • صحة إعدادات الاتصال.
                    """;
        }


        return message;
    }
}