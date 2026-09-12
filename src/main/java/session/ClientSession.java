package session;

public class ClientSession {

    private String clinicName;
    private Long userId;
    private Long clinicId;
    private Long deviceId;

    private String username;
    private String fullName;
    private String roleName;

    private String token;

    public ClientSession() {
    }

    public ClientSession(
            Long userId,
            Long clinicId,
            Long deviceId,
            String username,
            String fullName,
            String roleName,
            String token
    ) {
        this.userId = userId;
        this.clinicId = clinicId;
        this.deviceId = deviceId;
        this.username = username;
        this.fullName = fullName;
        this.roleName = roleName;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getToken() {
        return token;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean hasToken() {
        return token != null &&
                !token.isBlank();
    }
    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }
}
