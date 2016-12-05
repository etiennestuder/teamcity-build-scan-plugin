package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ExternalIntegration {

    void handle(@NotNull BuildScanReferences buildScans, @NotNull Map<String, String> params);

}
