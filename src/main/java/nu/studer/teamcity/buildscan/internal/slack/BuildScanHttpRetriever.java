package nu.studer.teamcity.buildscan.internal.slack;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

final class BuildScanHttpRetriever {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final URL scanUrl;
    private final BuildScanPayloadDeserializer payloadDeserializer;

    private BuildScanHttpRetriever(@NotNull URL scanUrl) {
        this.scanUrl = scanUrl;
        this.payloadDeserializer = BuildScanPayloadDeserializer.create();
    }

    @NotNull
    static BuildScanHttpRetriever forUrl(@NotNull URL scanUrl) {
        return new BuildScanHttpRetriever(scanUrl);
    }

    @NotNull
    BuildScanPayload retrieve() throws IOException {
        URLConnection urlConnection = scanUrl.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalArgumentException("HttpURLConnection expected");
        }

        HttpURLConnection con = (HttpURLConnection) urlConnection;

        con.setInstanceFollowRedirects(true);
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        con.setUseCaches(false);

        // connect
        con.connect();

        // read payload
        BuildScanPayload buildScanPayload;
        try (InputStream is = con.getInputStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            buildScanPayload = payloadDeserializer.fromJson(reader);
        }

        // log response code
        int responseCode = con.getResponseCode();
        LOGGER.info("Invoking build scan data end-point returned response code: " + responseCode);

        return buildScanPayload;
    }

}
