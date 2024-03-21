package nu.studer.teamcity.buildscan.internal.slack;

import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import nu.studer.teamcity.buildscan.TeamCityBuildStatus;
import nu.studer.teamcity.buildscan.TeamCityConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class SlackPayloadFactory {

    private SlackPayloadFactory() {
    }

    @NotNull
    static SlackPayloadFactory create() {
        return new SlackPayloadFactory();
    }

    @NotNull
    SlackPayload from(@NotNull BuildScanReferences buildScans, @NotNull Map<String, BuildScanPayload> buildScanPayloads, @NotNull TeamCityBuildStatus teamCityBuildStatus, @NotNull TeamCityConfiguration teamCityConfiguration) {
        SlackPayload payload = new SlackPayload();

        // extract TeamCity build info
        String buildConfigName = teamCityConfiguration.fullBuildName;
        String serverUrl = teamCityConfiguration.params.get("teamcity.serverUrl");
        String buildId = teamCityConfiguration.params.get("teamcity.build.id");
        String buildUrl = String.format("%s/viewLog.html?buildId=%s", serverUrl, buildId);

        // hard-code username
        payload.username("Develocity");

        // main text
        String tcBuildOutcome =
            teamCityBuildStatus == TeamCityBuildStatus.SUCCESS ? " succeeded." :
                teamCityBuildStatus == TeamCityBuildStatus.FAILURE ? " failed." :
                    teamCityBuildStatus == TeamCityBuildStatus.ERROR ? " encountered an error." : "";

        if (buildScans.size() == 1) {
            payload.text(String.format("TeamCity <%s|[%s]>%s 1 build scan published:", buildUrl, buildConfigName, tcBuildOutcome));
        } else {
            payload.text(String.format("TeamCity <%s|[%s]>%s %d build scans published:", buildUrl, buildConfigName, tcBuildOutcome, buildScans.size()));
        }

        // for each build scan, add a separate attachment
        for (BuildScanReference buildScan : buildScans.all()) {
            String buildScanId = buildScan.getId();
            String buildScanUrl = buildScan.getUrl();

            SlackPayload.Attachment attachment = new SlackPayload.Attachment()
                .fallback(String.format("Build scan %s", buildScanUrl))
                .pretext(String.format("Build scan %s", buildScanUrl))
                .mrkdwn_in("text", "pretext", "fields");

            BuildScanPayload buildScanPayload = buildScanPayloads.get(buildScanId);

            String color = color(buildScanPayload);
            attachment.color(color);

            Optional<String> userName = userName(buildScanPayload);
            userName.ifPresent(attachment::author_name);

            Optional<String> gravatarLink = gravatarLink(buildScanPayload);
            gravatarLink.ifPresent(attachment::author_icon);

            Optional<Long> buildStartTime = buildStartTime(buildScanPayload);
            buildStartTime.ifPresent(attachment::ts);

            Optional<String> rootProjectName = rootProjectName(buildScanPayload);
            rootProjectName.ifPresent(p -> {
                SlackPayload.Attachment.Field field = new SlackPayload.Attachment.Field();
                field.title("Project");
                field.isShort(true);
                field.value(p);

                attachment.field(field);
            });

            List<String> failedTests = failedTests(buildScanPayload, buildScanUrl);
            if (!failedTests.isEmpty()) {
                SlackPayload.Attachment.Field field = new SlackPayload.Attachment.Field();
                field.title(String.format("%d failed %s", buildScanPayload.data.tests.numFailed, buildScanPayload.data.tests.numFailed > 1 ? "tests" : "test"));
                field.isShort(false);
                field.value(failedTests.stream().collect(Collectors.joining("\n")));

                attachment.field(field);
            }

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

    @NotNull
    private static Optional<String> gravatarLink(@Nullable BuildScanPayload buildScanPayload) {
        String gravatarLink = null;
        if (buildScanPayload != null) {
            try {
                String checksum = buildScanPayload.data.summary.identity.avatarChecksum;
                if (checksum != null) {
                    gravatarLink = String.format("https://www.gravatar.com/avatar/%s?s=16", checksum);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return Optional.ofNullable(gravatarLink);
    }

    @NotNull
    private static Optional<Long> buildStartTime(@Nullable BuildScanPayload buildScanPayload) {
        Long buildStartTime = null;
        if (buildScanPayload != null) {
            try {
                buildStartTime = buildScanPayload.data.summary.startTime / 1000;
            } catch (Exception e) {
                // ignore
            }
        }
        return Optional.ofNullable(buildStartTime);
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

    private List<String> failedTests(@Nullable BuildScanPayload buildScanPayload, String buildScanUrl) {
        List<String> failedTests = Collections.emptyList();
        if (buildScanPayload != null) {
            try {
                failedTests = buildScanPayload.data.tests.rows()
                    .stream()
                    .filter(r -> r.result.equals("failed"))
                    .map(r -> String.format("• <%s|%s>", String.format("%s/tests/%s", buildScanUrl, r.id), r.name)).collect(Collectors.toList());
            } catch (Exception e) {
                // ignore
            }
        }
        return failedTests;
    }

}
