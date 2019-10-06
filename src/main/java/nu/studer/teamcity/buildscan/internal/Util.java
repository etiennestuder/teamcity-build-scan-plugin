package nu.studer.teamcity.buildscan.internal;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Util {

    // Includes:
    // 1. Optional `[INFO] ` prefix, which is present in Maven scans
    // 2. A capturing group for the full URL (necessary for Maven scans)
    // 3. A capturing group for the scan ID (necessary for the `BuildScanReference` instance).
    private static final Pattern BUILD_SCAN_URL_PATTERN = Pattern.compile("(?:\\[INFO] )?(https?://.*/s/(.*))");

    static boolean isBuildScanUrl(String text) {
        return doGetBuildScanId(text).isPresent();
    }

    static String getBuildScanId(String text) {
        //noinspection OptionalGetWithoutIsPresent
        return doGetBuildScanId(text).get();
    }

    static String getBuildScanUrl(String text) {
        //noinspection OptionalGetWithoutIsPresent
        return doGetBuildScanUrl(text).get();
    }

    private static Optional<String> doGetBuildScanId(String text) {
        Matcher matcher = BUILD_SCAN_URL_PATTERN.matcher(text);
        return matcher.matches() ? Optional.of(matcher.group(2)) : Optional.empty();
    }

    private static Optional<String> doGetBuildScanUrl(String text) {
        Matcher matcher = BUILD_SCAN_URL_PATTERN.matcher(text);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private Util() {
    }

}
