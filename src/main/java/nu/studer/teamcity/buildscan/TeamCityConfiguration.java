package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public final class TeamCityConfiguration {

    public final String fullBuildName;
    public final Map<String, String> params;

    public TeamCityConfiguration(@NotNull String fullBuildName, @NotNull Map<String, String> params) {
        this.fullBuildName = fullBuildName;
        this.params = Collections.unmodifiableMap(params);
    }

}
