package security;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkChecker {

    /**
     * فحص الاتصال بالإنترنت العام عبر فتح اتصال سريع (Socket)
     * لخوادم DNS من Google (8.8.8.8) أو Cloudflare (1.1.1.1)
     *
     * @return true إذا كان الإنترنت متصلاً، false إذا كان مقطوعاً
     */
    public static boolean isInternetAvailable() {
        return isHostReachable("8.8.8.8", 53, 2000) || isHostReachable("1.1.1.1", 53, 2000);
    }

    /**
     * فحص جاهزية سيرفر الـ Backend الخاص بالتطبيق
     *
     * @param serverUrl رابط السيرفر (مثال: http://localhost:8080/api)
     * @return true إذا كان السيرفر يعمل ويستجيب
     */
    public static boolean isServerAvailable(String serverUrl) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            // طلب HEAD خفيف جداً بدون تحميل بيانات من السيرفر
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(3))
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            // أي رمز استجابة بين 200 و 499 يعني أن السيرفر يعمل وموجود
            return response.statusCode() >= 200 && response.statusCode() < 500;

        } catch (Exception e) {
            return false; // السيرفر غير متصل أو غائب
        }
    }

    /**
     * فحص مباشر لمضيف ومنفذ محدد عبر Socket
     */
    private static boolean isHostReachable(String host, int port, int timeoutMillis) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}