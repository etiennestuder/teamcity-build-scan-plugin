package nu.studer.teamcity.buildscan.internal.slack;

import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import nu.studer.teamcity.buildscan.TeamCityBuildStatus;
import nu.studer.teamcity.buildscan.TeamCityConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class SlackTestInvocation {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Build scan id, URL, and webhook URL must be specified.");
        }

        String fullBuildName = "My Configuration";

        Map<String, String> params = new HashMap<>();
        params.put(TeamCityConfiguration.SLACK_WEBHOOK_URL_CONFIG_PARAM, args[2]);
        params.put("teamcity.serverUrl", "http://tc.server.org");
        params.put("teamcity.build.id", "23");

        if (args.length > 3) {
            params.put("BUILD_SCAN_SERVER_AUTH", args[3]);
        }
        if (args.length > 4) {
            params.put("BUILD_SCAN_SERVER_AUTH_2", args[4]);
        }

        BuildScanReferences buildScanReferences = BuildScanReferences.of(Collections.singletonList(
            new BuildScanReference(args[0], args[1])
        ));

        SlackIntegration slackIntegration = new SlackIntegration();
        TeamCityBuildStatus teamCityBuildStatus = TeamCityBuildStatus.SUCCESS;
        TeamCityConfiguration teamCityConfiguration = new TeamCityConfiguration(fullBuildName, params);
        Optional<Future> future = slackIntegration.handle(buildScanReferences, teamCityBuildStatus, teamCityConfiguration);
        future.get().get(20, TimeUnit.SECONDS);

        slackIntegration.shutdown();
    }

}
