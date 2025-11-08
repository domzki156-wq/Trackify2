import java.net.URI;
import java.net.http.*;
import java.time.Duration;
public class NetworkTest {
    public static void main(String[] args) throws Exception {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var req = HttpRequest.newBuilder(URI.create("https://api.exchangerate.host/convert?from=USD&to=PHP&amount=1"))
                .timeout(Duration.ofSeconds(6)).GET().build();
        try {
            var r = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + r.statusCode());
            System.out.println(r.body());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
