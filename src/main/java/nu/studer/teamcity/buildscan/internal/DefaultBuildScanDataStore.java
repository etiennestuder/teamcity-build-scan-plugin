package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.util.StringUtil;
import nu.studer.teamcity.buildscan.BuildScanReference;

import java.util.ArrayList;
import java.util.List;

public class DefaultBuildScanDataStore implements BuildScanDataStore {

    private static final String BUILD_SCAN_STORAGE = "build-scan-storage";
    private static final String SEPARATOR = "|";

    @Override
    public void mark(SBuild build) {
        String buildId = String.valueOf(build.getBuildId());
        CustomDataStorage customDataStorage = build.getBuildType().getCustomDataStorage(BUILD_SCAN_STORAGE);
        String existing = customDataStorage.getValue(buildId);

        if (existing != null) {
            return;
        }

        customDataStorage.putValue(buildId, "");
    }

    @Override
    public void store(SBuild build, String buildScanUrl) {
        String buildId = String.valueOf(build.getBuildId());
        CustomDataStorage customDataStorage = build.getBuildType().getCustomDataStorage(BUILD_SCAN_STORAGE);
        String existing = customDataStorage.getValue(buildId);

        if (existing == null) {
            customDataStorage.putValue(buildId, buildScanUrl);
        } else {
            List<String> scans = StringUtil.split(existing, SEPARATOR);
            scans.add(buildScanUrl);

            customDataStorage.putValue(buildId, StringUtil.join(SEPARATOR, scans));
        }
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        String buildId = String.valueOf(build.getBuildId());
        String rawScanList = build.getBuildType().getCustomDataStorage(BUILD_SCAN_STORAGE).getValue(buildId);

        if (rawScanList == null) {
            return null;
        }

        List<BuildScanReference> buildScanReferences = new ArrayList<>();
        List<String> scans = StringUtil.split(rawScanList, SEPARATOR);
        for (String scanUrl : scans) {
            if (!scanUrl.isEmpty()) {
                buildScanReferences.add(new BuildScanReference(Util.getBuildScanId(scanUrl), scanUrl));
            }
        }

        return buildScanReferences;
    }
}
