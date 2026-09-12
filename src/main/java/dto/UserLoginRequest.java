package dto;

public class UserLoginRequest {

    private String username;
    private String password;
    private String installationId;

    public UserLoginRequest() {
    }

    public UserLoginRequest(
            String username,
            String password,
            String installationId
    ) {
        this.username =
                username;

        this.password =
                password;

        this.installationId =
                installationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username
    ) {
        this.username =
                username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password
    ) {
        this.password =
                password;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(
            String installationId
    ) {
        this.installationId =
                installationId;
    }
}
