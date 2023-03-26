package nu.studer.teamcity.buildscan.connection;

import static com.google.common.base.Strings.isNullOrEmpty;

public final class GradleEnterpriseAccessKeyValidator {

    public static boolean isValid(String value) {
        if (isNullOrEmpty(value)) {
            return false;
        }

        String[] entries = value.split(";");

        for (String entry : entries) {
            String[] parts = entry.split("=", 2);
            if (parts.length < 2) {
                return false;
            }

            String servers = parts[0];
            String accessKey = parts[1];

            if (isNullOrEmpty(servers) || isNullOrEmpty(accessKey)) {
                return false;
            }

            for (String server : servers.split(",")) {
                if (isNullOrEmpty(server)) {
                    return false;
                }
            }
        }

        return true;
    }

    private GradleEnterpriseAccessKeyValidator() {
    }

}
