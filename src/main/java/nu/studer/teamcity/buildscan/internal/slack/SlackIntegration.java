package nu.studer.teamcity.buildscan.internal.slack;

import nu.studer.teamcity.buildscan.BuildScanReferences;
import nu.studer.teamcity.buildscan.ExternalIntegration;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class SlackIntegration implements ExternalIntegration {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final SlackPayloadFactory payloadFactory = SlackPayloadFactory.create();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void handle(@NotNull BuildScanReferences buildScans, @NotNull Map<String, String> params) {
        if (buildScans.isEmpty()) {
            return;
        }

        URL webhookUrlString = getWebhookURL(params);
        if (webhookUrlString != null) {
            executor.submit(() -> notifySlack(buildScans, params, webhookUrlString));
        }
    }

    private static URL getWebhookURL(Map<String, String> params) {
        String webhookUrlString = params.get("BUILD_SCAN_SLACK_WEBHOOK_URL");
        if (webhookUrlString == null) {
            return null;
        }

        try {
            return new URL(webhookUrlString);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid Slack webhook URL: " + webhookUrlString, e);
            return null;
        }
    }

    private void notifySlack(@NotNull BuildScanReferences buildScans, Map<String, String> params, URL webhookUrl) {
        LOGGER.info("Notifying Slack via webhook: " + webhookUrl);
        try {
            SlackPayload payload = payloadFactory.from(buildScans, params);
            SlackHttpNotifier.forWebhook(webhookUrl).notify(payload);
        } catch (Exception e) {
            LOGGER.error("Notifying Slack via webhook failed", e);
        }
    }

    @SuppressWarnings("unused")
    void shutdown() {
        LOGGER.info("Terminating Slack executor");
        try {
            executor.shutdown();
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("Error awaiting Slack executor termination", e);
        }
    }

}
