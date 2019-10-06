package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import nu.studer.teamcity.buildscan.BuildScanLookup;
import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class LogIteratingBuildScanLookup implements BuildScanLookup {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private static final String PUBLISHING_BUILD_PATTERN_GRADLE = "Publishing build scan...";
    private static final String PUBLISHING_BUILD_PATTERN_MAVEN = "[INFO] Publishing build scan...";

    @Override
    @NotNull
    public BuildScanReferences getBuildScansForBuild(@NotNull SBuild build) {
        // If a build is still running we'll assume our callback-based method of getting scans is going to work.
        // Avoid paying the cost of parsing the build log for currently running builds that just haven't happened
        // to have published a scan yet.
        if (!build.isFinished()) {
            return BuildScanReferences.of();
        }

        LOGGER.info(String.format("Parsing build log of build id: %s for build scan urls", build.getBuildId()));

        List<BuildScanReference> buildScans = new ArrayList<>();
        boolean foundPublishMessage = false;
        for (Iterator<LogMessage> iterator = build.getBuildLog().getMessagesIterator(); iterator.hasNext(); ) {
            LogMessage message = iterator.next();
            String text = message.getText();
            if (!foundPublishMessage && (PUBLISHING_BUILD_PATTERN_GRADLE.equals(text) || PUBLISHING_BUILD_PATTERN_MAVEN.equals(text))) {
                foundPublishMessage = true;
            } else if (foundPublishMessage && Util.isBuildScanUrl(text)) {
                buildScans.add(new BuildScanReference(Util.getBuildScanId(text), Util.getBuildScanUrl(text)));
                foundPublishMessage = false;
            } else {
                foundPublishMessage = false;
            }
        }
        return BuildScanReferences.of(buildScans);
    }

}
