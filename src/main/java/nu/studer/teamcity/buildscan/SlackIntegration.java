package nu.studer.teamcity.buildscan;

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

final class SlackIntegration {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final URL webhookUrl;
    private final SlackPayloadFactory payloadFactory;
    private final SlackPayloadSerializer payloadSerializer;

    private SlackIntegration(@NotNull URL webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.payloadFactory = SlackPayloadFactory.create();
        this.payloadSerializer = SlackPayloadSerializer.create();
    }

    @NotNull
    static SlackIntegration forWebhook(@NotNull URL webhookUrl) {
        return new SlackIntegration(webhookUrl);
    }

    void notify(@NotNull BuildScanReferences buildScans) throws IOException {
        SlackPayload payload = payloadFactory.from(buildScans);
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
