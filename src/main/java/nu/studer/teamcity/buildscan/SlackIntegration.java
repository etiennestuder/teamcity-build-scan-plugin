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
        SlackPayload payload = new SlackPayload();

        // main text, only hyper-linking to the build scan if there is only one build scan
        if (buildScans.size() == 1) {
            payload.text(String.format("<%s|Build scan> published.", buildScans.first().getUrl()));
        } else {
            payload.text(String.format("%d build scans published.", buildScans.size()));
        }

        // for each build scan, add a separate attachment
        for (BuildScanReference buildScan : buildScans.all()) {
            payload.attachment(new SlackPayload.Attachment()
                .fallback(String.format("Build scan link %s", buildScan.getUrl()))
                .color("#000000")
                .field(new SlackPayload.Attachment.Field()
                    .title("Build scan link")
                    .value(buildScan.getUrl())
                    .isShort(true)));
        }

        return payload;
    }

    @NotNull
    static SlackIntegration forWebhook(@NotNull URL webhookUrl) {
        return new SlackIntegration(webhookUrl);
    }

}
