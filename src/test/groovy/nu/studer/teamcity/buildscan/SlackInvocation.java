package nu.studer.teamcity.buildscan;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SlackInvocation {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Webhook URL must be specified.");
        }

        Map<String, String> params = new HashMap<>();
        params.put("teamcity.buildConfName", "My Configuration");
        params.put("teamcity.serverUrl", "http://ci.company.org");
        params.put("teamcity.build.id", "23");

        BuildScanReferences buildScanReferences = BuildScanReferences.of(Arrays.asList(
            new BuildScanReference("myId", "http://www.myUrl.org/s/abcde"),
            new BuildScanReference("myOtherId", "http://www.myOtherUrl.org/efghi")));

        SlackIntegration slackIntegration = SlackIntegration.forWebhook(new URL(args[0]));
        slackIntegration.notify(buildScanReferences, params);
    }

}
