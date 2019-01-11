package nu.studer.teamcity.buildscan.internal;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
    private static final Pattern BUILD_SCAN_URL_PATTERN = Pattern.compile("https?://.*/s/(.*)");

    private Util() {
    }

    public static void copy(@NotNull InputStream in, @NotNull OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

    public static boolean isBuildScanUrl(String text) {
        return doGetBuildScanId(text).isPresent();
    }

    public static String getBuildScanId(String text) {
        //noinspection OptionalGetWithoutIsPresent
        return doGetBuildScanId(text).get();
    }

    private static Optional<String> doGetBuildScanId(String text) {
        Matcher matcher = BUILD_SCAN_URL_PATTERN.matcher(text);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
