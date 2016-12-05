package nu.studer.teamcity.buildscan.internal.slack;

import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;

import java.util.Map;

final class SlackPayloadFactory {

    private SlackPayloadFactory() {
    }

    static SlackPayloadFactory create() {
        return new SlackPayloadFactory();
    }

    SlackPayload from(BuildScanReferences buildScans, Map<String, String> params) {
        SlackPayload payload = new SlackPayload();

        // extract TeamCity build info
        String buildConfigName = params.get("system.teamcity.buildConfName");
        String serverUrl = params.get("teamcity.serverUrl");
        String buildId = params.get("teamcity.build.id");
        String buildUrl = String.format("%s/viewLog.html?buildId=%s", serverUrl, buildId);

        // main text, only hyper-linking to the build scan if there is only one build scan
        if (buildScans.size() == 1) {
            String buildScanUrl = buildScans.first().getUrl();
            payload.text(String.format("<%s|Build scan> published in TeamCity configuration <%s|%s>.", buildScanUrl, buildUrl, buildConfigName));
        } else {
            payload.text(String.format("%d build scans published in TeamCity configuration <%s|%s>.", buildScans.size(), buildUrl, buildConfigName));
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

}
