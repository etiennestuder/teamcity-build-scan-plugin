package nu.studer.teamcity.buildscan.internal.slack;

import java.io.IOException;
import java.net.URL;

public final class BuildScanTestInvocation {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Webhook URL must be specified.");
        }

        BuildScanHttpRetriever buildScanHttpRetriever = BuildScanHttpRetriever.forUrl(new URL(args[0]));
        BuildScanPayload payload = buildScanHttpRetriever.retrieve();
        System.out.println("payload = " + payload);
    }

}
