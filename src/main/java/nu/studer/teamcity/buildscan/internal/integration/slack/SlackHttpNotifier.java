package nu.studer.teamcity.buildscan.internal.integration.slack;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

final class SlackHttpNotifier {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final URL webhookUrl;
    private final SlackPayloadSerializer payloadSerializer;

    private SlackHttpNotifier(@NotNull URL webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.payloadSerializer = SlackPayloadSerializer.create();
    }

    @NotNull
    static SlackHttpNotifier forWebhook(@NotNull URL webhookUrl) {
        return new SlackHttpNotifier(webhookUrl);
    }

    void notify(SlackPayload payload) throws IOException {
        String json = payloadSerializer.toJson(payload);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        URLConnection urlConnection = webhookUrl.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("Error");
        }

        HttpURLConnection con = (HttpURLConnection) urlConnection;

        con.setInstanceFollowRedirects(true);
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        con.setUseCaches(false);

        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-type", "application/json");

        // connect and send headers
        con.connect();

        // send payload
        try (InputStream is = new ByteArrayInputStream(bytes); OutputStream os = con.getOutputStream()) {
            Util.copy(is, os);
        }

        // log response code
        int responseCode = con.getResponseCode();
        LOGGER.info("Invoking Slack webhook returned response code: " + responseCode);
    }

}
