package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public final class TeamCityConfiguration {

    public final Map<String, String> params;

    public TeamCityConfiguration(@NotNull Map<String, String> params) {
        this.params = Collections.unmodifiableMap(params);
    }

}
