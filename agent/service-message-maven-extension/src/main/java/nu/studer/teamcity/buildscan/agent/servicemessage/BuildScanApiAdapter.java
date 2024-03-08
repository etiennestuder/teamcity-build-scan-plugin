package nu.studer.teamcity.buildscan.agent.servicemessage;

import java.net.URI;
import java.util.function.BiConsumer;

interface BuildScanApiAdapter {

    void publishOnDemand();

    void buildScanPublished(BiConsumer<String, URI> action);
}
