package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Future;

public interface ExternalIntegration {

    @NotNull
    Optional<Future> handle(@NotNull BuildScanReferences buildScans, @NotNull TeamCityConfiguration teamCityConfiguration);

}
