package nu.studer.teamcity.buildscan.internal.slack;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class Util {

    static void copy(@NotNull InputStream in, @NotNull OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

    private Util() {
    }

}
