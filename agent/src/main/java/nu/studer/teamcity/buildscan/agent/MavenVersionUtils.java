package nu.studer.teamcity.buildscan.agent;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MavenVersionUtils {

    private static final Pattern VERSION_OUTPUT_REGEX = Pattern.compile("Apache Maven\\s+(.*)\\s+\\(.*\\)");

    @Nullable
    public static String parseVersion(String output) {
        Matcher matcher = VERSION_OUTPUT_REGEX.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static boolean isVersionAtLeast(String version, String requiredVersion) {
        if (version == null || requiredVersion == null) {
            return false;
        }

        String[] versionParts = version.split("-")[0].split("\\.");
        String[] requiredVersionParts = requiredVersion.split("-")[0].split("\\.");
        for (int i = 0; i < versionParts.length; i++) {
            if (i >= requiredVersionParts.length) {
                return false;
            }
            int versionPart = Integer.parseInt(versionParts[i]);
            int requiredVersionPart = Integer.parseInt(requiredVersionParts[i]);
            if (versionPart != requiredVersionPart) {
                return versionPart > requiredVersionPart;
            }
        }
        return true;
    }

    private MavenVersionUtils() {}
}
