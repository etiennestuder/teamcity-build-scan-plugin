package nu.studer.teamcity.buildscan.internal.slack;

import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

final class SlackPayloadFactory {

    private SlackPayloadFactory() {
    }

    @NotNull
    static SlackPayloadFactory create() {
        return new SlackPayloadFactory();
    }

    @NotNull
    SlackPayload from(@NotNull BuildScanReferences buildScans, @NotNull Map<String, BuildScanPayload> buildScanPayloads, @NotNull Map<String, String> params) {
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
            SlackPayload.Attachment attachment = new SlackPayload.Attachment()
                .fallback(String.format("Build scan %s", buildScan.getUrl()))
                .field(new SlackPayload.Attachment.Field()
                    .title("Build scan")
                    .value(buildScan.getUrl())
                    .isShort(true));

            BuildScanPayload buildScanPayload = buildScanPayloads.get(buildScan.getId());

            String color = color(buildScanPayload);
            attachment.color(color);

            Optional<String> rootProjectName = rootProjectName(buildScanPayload);
            rootProjectName.ifPresent(name -> attachment.field(new SlackPayload.Attachment.Field()
                .title("Root project")
                .value(name)
                .isShort(true)));

            Optional<String> userName = userName(buildScanPayload);
            userName.ifPresent(name -> attachment.field(new SlackPayload.Attachment.Field()
                .title("User")
                .value(name)
                .isShort(true)));

            payload.attachment(attachment);
        }

        return payload;
    }

    @NotNull
    private static String color(@Nullable BuildScanPayload buildScanPayload) {
        String color = "#000000";
        if (buildScanPayload != null) {
            try {
                color = buildScanPayload.data.summary.failed ? "#FB2F08" : "#1EC38A";
            } catch (Exception e) {
                // ignore
            }
        }
        return color;
    }

    @NotNull
    private static Optional<String> rootProjectName(@Nullable BuildScanPayload buildScanPayload) {
        String rootProjectName = null;
        if (buildScanPayload != null) {
            try {
                rootProjectName = buildScanPayload.data.summary.rootProjectName;
            } catch (Exception e) {
                // ignore
            }
        }
        return Optional.ofNullable(rootProjectName);
    }

    @NotNull
    private static Optional<String> userName(@Nullable BuildScanPayload buildScanPayload) {
        String userName = null;
        if (buildScanPayload != null) {
            try {
                userName = buildScanPayload.data.summary.identity.identityName;
            } catch (Exception e) {
                // ignore
            }
        }
        return Optional.ofNullable(userName);
    }

}
