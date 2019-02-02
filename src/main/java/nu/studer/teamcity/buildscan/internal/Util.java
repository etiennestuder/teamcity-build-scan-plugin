package nu.studer.teamcity.buildscan.internal;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Util {

    private static final Pattern BUILD_SCAN_URL_PATTERN = Pattern.compile("https?://.*/s/(.*)");

    static boolean isBuildScanUrl(String text) {
        return doGetBuildScanId(text).isPresent();
    }

    static String getBuildScanId(String text) {
        //noinspection OptionalGetWithoutIsPresent
        return doGetBuildScanId(text).get();
    }

    private static Optional<String> doGetBuildScanId(String text) {
        Matcher matcher = BUILD_SCAN_URL_PATTERN.matcher(text);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private Util() {
    }

}
