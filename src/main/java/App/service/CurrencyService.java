package App.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * CurrencyService - lightweight conversion helper that calls exchangerate.host.
 * If the HTTP call fails it returns a fallback conversion (so UI remains usable).
 */
public class CurrencyService {

    // Fallback exchange rate: 1 USD = FALLBACK_RATE PHP
    // Change this to whatever offline default you want.
    private static final double FALLBACK_RATE = 56.0;

    /**
     * Convert an amount in USD to PHP.
     * Tries the exchangerate.host convert endpoint; if parsing fails uses fallback.
     *
     * @param usdAmount amount in USD
     * @return PHP amount (never null)
     * @throws Exception on hard network errors (caller may catch & show details)
     */
    public static Double convertUsdToPhp(double usdAmount) throws Exception {
        String url = String.format(
                "https://api.exchangerate.host/convert?from=USD&to=PHP&amount=%s",
                Double.toString(usdAmount)
        );

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp;
        try {
            resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            // network (DNS/TLS/proxy) - propagate so caller can show the exception text
            throw new RuntimeException("Network error: " + ex.getMessage(), ex);
        }

        if (resp.statusCode() != 200) {
            throw new RuntimeException("HTTP error from exchange API: " + resp.statusCode());
        }

        String json = resp.body();
        // parse "result":<number> quickly without adding a JSON dependency
        int idx = json.indexOf("\"result\":");
        if (idx >= 0) {
            int start = idx + "\"result\":".length();
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || ".-Ee".indexOf(json.charAt(end)) >= 0)) end++;
            String num = json.substring(start, end).trim();
            try {
                return Double.parseDouble(num);
            } catch (NumberFormatException ignored) { /* fall through to rate parsing */ }
        }

        // fallback attempt: parse "rate" inside "info":{"rate":...}
        int idx2 = json.indexOf("\"rate\":");
        if (idx2 >= 0) {
            int start = idx2 + "\"rate\":".length();
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || ".-Ee".indexOf(json.charAt(end)) >= 0)) end++;
            String rateStr = json.substring(start, end).trim();
            try {
                double rate = Double.parseDouble(rateStr);
                return usdAmount * rate;
            } catch (NumberFormatException ignored) { /* fall through to final fallback */ }
        }

        // Last resort: use configured fallback rate
        return usdAmount * FALLBACK_RATE;
    }

    /** Convert using the configured fallback rate (no network). */
    public static double convertUsingFallback(double usdAmount) {
        return usdAmount * FALLBACK_RATE;
    }
}
