package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import nu.studer.teamcity.buildscan.BuildScanLookup;
import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DefaultBuildScanLookup implements BuildScanLookup {

    private static final String PUBLISHING_BUILD_PATTERN = "Publishing build scan...";
    private static final Pattern BUILD_SCAN_URL_PATTERN = Pattern.compile("https?://.*/s/(.*)");

    @Override
    @NotNull
    public BuildScanReferences getBuildScansForBuild(@NotNull SBuild build) {
        List<BuildScanReference> buildScans = new ArrayList<>();
        boolean foundPublishMessage = false;
        for (Iterator<LogMessage> iterator = build.getBuildLog().getMessagesIterator(); iterator.hasNext(); ) {
            LogMessage message = iterator.next();
            String text = message.getText();
            if (!foundPublishMessage && PUBLISHING_BUILD_PATTERN.equals(text)) {
                foundPublishMessage = true;
            } else if (foundPublishMessage && isBuildScanUrl(text)) {
                buildScans.add(new BuildScanReference(getBuildScanId(text), text));
                foundPublishMessage = false;
            } else {
                foundPublishMessage = false;
            }
        }
        return BuildScanReferences.of(buildScans);
    }

    private static boolean isBuildScanUrl(String text) {
        return doGetBuildScanId(text).isPresent();
    }

    private static String getBuildScanId(String text) {
        //noinspection OptionalGetWithoutIsPresent
        return doGetBuildScanId(text).get();
    }

    private static Optional<String> doGetBuildScanId(String text) {
        Matcher matcher = BUILD_SCAN_URL_PATTERN.matcher(text);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

}
