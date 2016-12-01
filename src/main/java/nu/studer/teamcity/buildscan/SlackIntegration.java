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

    private static final String PAYLOAD = "{\n" +
        "  'text': '<https://scans.grdev.net/s/upmesuqf4qmxs|Build scan> published.',\n" +
        "  'attachments': [\n" +
        "    {\n" +
        "      'fallback': 'New open task [Urgent]: <http://url_to_task|Test out Slack message attachments>',\n" +
        "      'color': '#D00000',\n" +
        "      'fields': [\n" +
        "        {\n" +
        "          'title': 'Build scan server',\n" +
        "          'value': 'https://grdev.net',\n" +
        "          'short': true\n" +
        "        },\n" +
        "        {\n" +
        "          'title': 'Build scan id',\n" +
        "          'value': 'gflc873s',\n" +
        "          'short': true\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    private final URL webhookUrl;

    SlackIntegration(@NotNull URL webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    void notify(BuildScanReferences buildScans) {
        LOGGER.info("Invoking Slack webhook");
        try {
            doNotify();
        } catch (Exception e) {
            LOGGER.error("Invoking Slack webhook failed", e);
        }
    }

    private void doNotify() throws IOException {
        byte[] bytes = PAYLOAD.getBytes(StandardCharsets.UTF_8);

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

        int responseCode = con.getResponseCode();
        LOGGER.info(String.format("Response code of invoking Slack webhook at %s :%d", webhookUrl.toString(), responseCode));
    }

}
