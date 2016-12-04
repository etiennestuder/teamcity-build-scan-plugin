package nu.studer.teamcity.buildscan;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class SlackIntegration {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    static void handle(@NotNull BuildScanReferences buildScans, Map<String, String> params) {
        if (buildScans.isEmpty()) {
            return;
        }

        URL webhookUrlString = getWebhookURL(params);
        if (webhookUrlString != null) {
            EXECUTOR.submit(() -> notifySlack(buildScans, params, webhookUrlString));
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

    private static void notifySlack(@NotNull BuildScanReferences buildScans, Map<String, String> params, URL webhookUrl) {
        LOGGER.info("Invoking Slack webhook: " + webhookUrl);
        try {
            SlackNotifier.forWebhook(webhookUrl).notify(buildScans, params);
        } catch (Exception e) {
            LOGGER.error("Invoking Slack webhook failed", e);
        }
    }

}
