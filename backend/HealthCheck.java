import java.net.URI;
import java.net.HttpURLConnection;

public class HealthCheck {
    public static void main(String[] args) {
        try {
            var connection = (HttpURLConnection) URI.create("http://localhost:8084/actuator/health").toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int status = connection.getResponseCode();
            connection.disconnect();
            if (status != 200) {
                System.err.println("Backend health check returned HTTP " + status);
            }
            System.exit(status == 200 ? 0 : 1);
        } catch (Exception exception) {
            System.err.println("Backend health check failed: " + exception.getMessage());
            System.exit(1);
        }
    }
}
