package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.develocity.agent.maven.api.DevelocityApi;
import com.gradle.develocity.agent.maven.api.DevelocityListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import java.net.URI;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
@Component(
    role = DevelocityListener.class,
    hint = "build-scan-service-message-develocity",
    description = "Interacts with Maven builds invoked from TeamCity"
)
public final class BuildScanServiceMessageDevelocityListener extends BuildScanServiceMessageListener implements DevelocityListener {

    @Override
    public void configure(DevelocityApi api, MavenSession session) {
        super.configure(new BuildScanApiAdapter() {
            @Override
            public void publishOnDemand() {
                // publication is controlled via a system property in this case
                api.getBuildScan().publishing(p -> p.onlyIf(ctx -> false));
            }

            @Override
            public void buildScanPublished(BiConsumer<String, URI> action) {
                api.getBuildScan().buildScanPublished(scan -> action.accept(scan.getBuildScanId(), scan.getBuildScanUri()));
            }
        }, session);
    }

}
