package nu.studer.teamcity.buildscan.internal.slack;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

final class BuildScanHttpRetriever {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    // giving up after ~5 minutes of trying
    private static final int ATTEMPTS_MAX = 7;
    private static final int RETRY_DELAY_BASELINE_SECS = 15;

    private final URL scanUrl;
    private final PasswordCredentials credentials;
    private final BuildScanPayloadDeserializer payloadDeserializer;

    private BuildScanHttpRetriever(@NotNull URL scanUrl, @Nullable PasswordCredentials credentials) {
        this.scanUrl = scanUrl;
        this.credentials = credentials;
        this.payloadDeserializer = BuildScanPayloadDeserializer.create();
    }

    @NotNull
    static BuildScanHttpRetriever forUrl(@NotNull URL scanUrl, @Nullable PasswordCredentials credentials) {
        return new BuildScanHttpRetriever(scanUrl, credentials);
    }

    @NotNull
    BuildScanPayload retrieve() throws IOException {
        int attempt = 0;

        while (true) {
            try {
                return doRetrieve();
            } catch (IOException e) {
                LOGGER.error(String.format("Failed to retrieve build scan %s: %s", scanUrl, e.getMessage()));

                // back-off or stop trying
                if (++attempt < ATTEMPTS_MAX) {
                    sleepSeconds(RETRY_DELAY_BASELINE_SECS * attempt); // this is not great since we "block" the thread while waiting (even though it runs on a separate execution)
                } else {
                    break;
                }
            }
        }

        throw new RuntimeException("Unable to retrieved build scan at " + scanUrl);
    }

    @NotNull
    private BuildScanPayload doRetrieve() throws IOException {
        URLConnection urlConnection = scanUrl.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("HttpURLConnection expected");
        }

        HttpURLConnection con = (HttpURLConnection) urlConnection;

        con.setInstanceFollowRedirects(true);
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        con.setUseCaches(false);

        if (credentials != null) {
            String basicAuth = "Basic " + credentials.toBase64();
            con.addRequestProperty("Authorization", basicAuth);
        }

        // connect
        con.connect();

        // read payload
        BuildScanPayload buildScanPayload;
        try (InputStream is = con.getInputStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            buildScanPayload = payloadDeserializer.fromJson(reader);
        }

        // log response code
        int responseCode = con.getResponseCode();
        LOGGER.debug("Invoking build scan data end-point returned response code: " + responseCode);

        return buildScanPayload;
    }

    private void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
