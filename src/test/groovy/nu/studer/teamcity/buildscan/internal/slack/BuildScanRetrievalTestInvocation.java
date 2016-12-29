package nu.studer.teamcity.buildscan.internal.slack;

import java.io.IOException;
import java.net.URL;

public final class BuildScanRetrievalTestInvocation {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Webhook URL must be specified.");
        }

        PasswordCredentials passwordCredentials = null;
        if (args.length > 2) {
            passwordCredentials = new PasswordCredentials(args[1], args[2]);
        }

        BuildScanHttpRetriever buildScanHttpRetriever = BuildScanHttpRetriever.forUrl(new URL(args[0]), passwordCredentials);
        BuildScanPayload payload = buildScanHttpRetriever.retrieve();
        System.out.println("payload = " + payload);
    }

}
