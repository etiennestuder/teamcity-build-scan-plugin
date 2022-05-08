package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.maven.extension.api.scan.BuildScanApi;

final class BuildScanServiceMessageSender {

    private static final String BUILD_SCAN_SERVICE_MESSAGE_NAME = "nu.studer.teamcity.buildscan.buildScanLifeCycle";
    private static final String BUILD_SCAN_SERVICE_STARTED_MESSAGE_ARGUMENT = "BUILD_STARTED";
    private static final String BUILD_SCAN_SERVICE_URL_MESSAGE_ARGUMENT_PREFIX = "BUILD_SCAN_URL:";

    static void register(BuildScanApi buildScan) {
        System.out.println(ServiceMessage.of(
            BUILD_SCAN_SERVICE_MESSAGE_NAME,
            BUILD_SCAN_SERVICE_STARTED_MESSAGE_ARGUMENT
        ));

        buildScan.buildScanPublished(publishedBuildScan ->
            System.out.println(ServiceMessage.of(
                BUILD_SCAN_SERVICE_MESSAGE_NAME,
                BUILD_SCAN_SERVICE_URL_MESSAGE_ARGUMENT_PREFIX + publishedBuildScan.getBuildScanUri().toString()
            ))
        );
    }

    private BuildScanServiceMessageSender() {
    }

}
