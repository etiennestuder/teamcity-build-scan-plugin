package nu.studer.teamcity.buildscan.internal.slack;

import com.google.common.collect.ImmutableMap;
import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import nu.studer.teamcity.buildscan.TeamCityConfiguration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SlackNotificationTestInvocation {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Webhook URL must be specified.");
        }

        Map<String, String> params = new HashMap<>();
        params.put("system.teamcity.buildConfName", "My Configuration");
        params.put("teamcity.serverUrl", "http://tc.server.org");
        params.put("teamcity.build.id", "23");

        BuildScanReferences buildScanReferences = BuildScanReferences.of(Arrays.asList(
            new BuildScanReference("myId", "http://www.myUrl.org/s/h4b32e"),
            new BuildScanReference("myOtherId", "http://www.myOtherUrl.org/ef3gh2"),
            new BuildScanReference("myAgainOtherId", "http://www.myAgainOtherUrl.org/bf4g1k")));

        BuildScanPayload buildScanPayloadMyId = new BuildScanPayload();
        buildScanPayloadMyId.state = "complete";
        buildScanPayloadMyId.data().publicId = "myId";
        buildScanPayloadMyId.data().summary().failed = false;
        buildScanPayloadMyId.data().summary().startTime = System.currentTimeMillis();
        buildScanPayloadMyId.data().summary().rootProjectName = "sample-project";
        buildScanPayloadMyId.data().summary().rootProjectName = "sample-project";
        buildScanPayloadMyId.data().summary().identity().identityName = "etienne";
        buildScanPayloadMyId.data().summary().identity().avatarChecksum = "dc0bedcea8ba69085791f75bf84f3af7";

        BuildScanPayload buildScanPayloadMyOtherId = new BuildScanPayload();
        buildScanPayloadMyOtherId.state = "complete";
        buildScanPayloadMyOtherId.data().publicId = "myOtherId";
        buildScanPayloadMyOtherId.data().summary().failed = true;

        ImmutableMap<String, BuildScanPayload> buildScanPayloads = ImmutableMap.of(
            "myId", buildScanPayloadMyId,
            "myOtherId", buildScanPayloadMyOtherId
        );

        SlackPayloadFactory payloadFactory = SlackPayloadFactory.create();
        TeamCityConfiguration teamCityConfiguration = new TeamCityConfiguration(params);
        SlackPayload payload = payloadFactory.from(buildScanReferences, buildScanPayloads, teamCityConfiguration);

        SlackHttpNotifier slackHttpNotifier = SlackHttpNotifier.forWebhook(new URL(args[0]));
        slackHttpNotifier.notify(payload);
    }

}
