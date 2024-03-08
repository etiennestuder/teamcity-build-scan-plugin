package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import java.net.URI;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
@Component(
    role = GradleEnterpriseListener.class,
    hint = "build-scan-service-message-gradle-enterprise",
    description = "Interacts with Maven builds invoked from TeamCity"
)
public final class BuildScanServiceMessageGradleEnterpriseListener extends BuildScanServiceMessageListener implements GradleEnterpriseListener {

    @Override
    public void configure(GradleEnterpriseApi api, MavenSession session) {
        super.configure(new BuildScanApiAdapter() {
            @Override
            public void publishOnDemand() {
                api.getBuildScan().publishOnDemand();
            }

            @Override
            public void buildScanPublished(BiConsumer<String, URI> action) {
                api.getBuildScan().buildScanPublished(scan -> action.accept(scan.getBuildScanId(), scan.getBuildScanUri()));
            }
        }, session);
    }

}
