package nu.studer.teamcity.buildscan.internal.slack;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class PasswordCredentials {

    final String username;
    final String password;

    PasswordCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    String toBase64() {
        byte[] bytes = (username + ":" + password).getBytes(StandardCharsets.ISO_8859_1);
        return Base64.getEncoder().encodeToString(bytes);
    }

}
