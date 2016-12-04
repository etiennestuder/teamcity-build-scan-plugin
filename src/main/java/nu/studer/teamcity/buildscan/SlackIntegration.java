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
    private final SlackPayloadSerializer payloadSerializer;

    private SlackIntegration(@NotNull URL webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.payloadSerializer = SlackPayloadSerializer.create();
    }

    void notify(@NotNull BuildScanReferences buildScans) throws IOException {
        SlackPayload payload = createPayload(buildScans);
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

    @NotNull
    static SlackPayload createPayload(@NotNull BuildScanReferences buildScans) {
        return new SlackPayload()
            .text("<" + buildScans.first().getUrl() + "|Build scan> published.")
            .attachment(new SlackPayload.Attachment()
                .fallback(String.format("Build scan %s published.", buildScans.first().getUrl()))
                .color("#000000")
                .field(new SlackPayload.Attachment.Field()
                    .title("Build scan link")
                    .value(buildScans.first().getUrl())
                    .isShort(true)));
    }

    @NotNull
    static SlackIntegration forWebhook(@NotNull URL webhookUrl) {
        return new SlackIntegration(webhookUrl);
    }

}
