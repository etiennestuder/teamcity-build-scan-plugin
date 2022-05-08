package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@Component(
    role = GradleEnterpriseListener.class,
    hint = "build-scan-service-message",
    description = "Interacts with Maven builds invoked from TeamCity"
)
public final class BuildScanServiceMessageMavenExtension implements GradleEnterpriseListener {

    private final Logger logger = LoggerFactory.getLogger(BuildScanServiceMessageMavenExtension.class);

    @Override
    public void configure(GradleEnterpriseApi api, MavenSession session) {
        logger.debug("Executing extension: " + getClass().getSimpleName());

        logger.debug("Registering listener capturing build scan link");
        BuildScanServiceMessageSender.register(api.getBuildScan());
        logger.debug("Finished registering listener capturing build scan link");
    }

}
