package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.util.StringUtil;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CustomDataStorageBuildScanDataStore implements BuildScanDataStore {

    private static final String BUILD_SCAN_STORAGE_ID = "nu.studer.teamcity.buildscan.DefaultBuildScanDataStore";
    private static final String BUILD_SCAN_URLS_SEPARATOR = "|";

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    @Override
    public void mark(SBuild build) {
        String buildId = String.valueOf(build.getBuildId());
        CustomDataStorage customDataStorage = getCustomDataStorage(build);
        String existing = customDataStorage.getValue(buildId);

        if (existing == null) {
            customDataStorage.putValue(buildId, "");
            customDataStorage.flush();
        }
    }

    @Override
    public void store(SBuild build, String buildScanUrl) {
        String buildId = String.valueOf(build.getBuildId());
        CustomDataStorage customDataStorage = getCustomDataStorage(build);
        String existing = customDataStorage.getValue(buildId);

        if (existing == null || existing.isEmpty()) {
            LOGGER.info("Storing build scan URL: " + buildScanUrl + ", for build id: " + buildId);
            customDataStorage.putValue(buildId, buildScanUrl);
            customDataStorage.flush();
        } else {
            List<String> scans = StringUtil.split(existing, BUILD_SCAN_URLS_SEPARATOR);
            scans.add(buildScanUrl);

            String buildScanString = StringUtil.join(BUILD_SCAN_URLS_SEPARATOR, scans);
            LOGGER.info("Storing build scan URLs: " + buildScanString + ", for build id: " + buildId);
            customDataStorage.putValue(buildId, buildScanString);
            customDataStorage.flush();
        }
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        String buildId = String.valueOf(build.getBuildId());
        CustomDataStorage customDataStorage = getCustomDataStorage(build);
        String existing = customDataStorage.getValue(buildId);

        if (existing == null) {
            return null;
        } else {
            List<BuildScanReference> buildScanReferences = new ArrayList<>();
            List<String> scans = StringUtil.split(existing, BUILD_SCAN_URLS_SEPARATOR);
            for (String scanUrl : scans) {
                if (!scanUrl.isEmpty()) {
                    buildScanReferences.add(new BuildScanReference(Util.getBuildScanId(scanUrl), scanUrl));
                }
            }

            return buildScanReferences;
        }
    }

    @NotNull
    private CustomDataStorage getCustomDataStorage(SBuild build) {
        //noinspection ConstantConditions
        return build.getBuildType().getCustomDataStorage(BUILD_SCAN_STORAGE_ID);
    }

}
