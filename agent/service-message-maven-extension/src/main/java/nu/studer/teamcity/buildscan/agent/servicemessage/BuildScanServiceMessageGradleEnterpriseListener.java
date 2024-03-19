package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.develocity.agent.maven.adapters.enterprise.GradleEnterpriseApiAdapter;
import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

@SuppressWarnings("unused")
@Component(
    role = GradleEnterpriseListener.class,
    hint = "build-scan-service-message-gradle-enterprise",
    description = "Interacts with Maven builds invoked from TeamCity"
)
public final class BuildScanServiceMessageGradleEnterpriseListener extends BuildScanServiceMessageListener implements GradleEnterpriseListener {

    @Override
    public void configure(GradleEnterpriseApi api, MavenSession session) {
        super.configure(new GradleEnterpriseApiAdapter(api), session);
    }

}
