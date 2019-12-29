package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public final class TeamCityConfiguration {

    public static final String BUILD_SCAN_LOG_PARSING = "BUILD_SCAN_LOG_PARSING";
    public static final String BUILD_SCAN_SLACK_WEBHOOK_URL = "BUILD_SCAN_SLACK_WEBHOOK_URL";

    public final String fullBuildName;
    public final Map<String, String> params;

    public TeamCityConfiguration(@NotNull String fullBuildName, @NotNull Map<String, String> params) {
        this.fullBuildName = fullBuildName;
        this.params = Collections.unmodifiableMap(params);
    }

}
