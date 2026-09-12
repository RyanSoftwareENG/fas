package api;

public class ApiClientException
        extends java.io.IOException {

    private final int statusCode;
    private final String errorCode;
    private final String serverMessage;

    public ApiClientException(
            int statusCode,
            String errorCode,
            String serverMessage
    ) {

        super(
                serverMessage == null ||
                        serverMessage.isBlank()
                        ? "فشل طلب API."
                        : serverMessage
        );

        this.statusCode =
                statusCode;

        this.errorCode =
                errorCode;

        this.serverMessage =
                serverMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isConflict() {
        return statusCode == 409;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }

    @Override
    public String getMessage() {
        return serverMessage;
    }

    @Override
    public String toString() {

        return "ApiClientException{" +
                "statusCode=" + statusCode +
                ", errorCode='" + errorCode + '\'' +
                ", serverMessage='" + serverMessage + '\'' +
                '}';
    }
}