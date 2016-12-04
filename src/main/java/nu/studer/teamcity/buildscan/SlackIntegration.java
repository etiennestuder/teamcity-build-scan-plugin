package nu.studer.teamcity.buildscan;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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

        String webhookUrlString = params.get("BUILD_SCAN_SLACK_WEBHOOK_URL");
        if (webhookUrlString != null) {
            LOGGER.info("Invoking Slack webhook: " + webhookUrlString);
            try {
                URL webhookUrl = new URL(webhookUrlString);
                SlackNotifier.forWebhook(webhookUrl).notify(buildScans, params);
            } catch (Exception e) {
                LOGGER.error("Invoking Slack webhook failed", e);
            }
        }
    }

}
