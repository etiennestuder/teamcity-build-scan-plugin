package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

        // When a Maven build step has an explicitly defined pom.xml file location, TeamCity will run an info goal on it. Ignore build scan publication in this case.
        if (invokesJetBrainsInfoGoal(session)) {
            String goal = getJetBrainsInfoGoal(session);
            logger.debug("Skipping build scan publishing for goal " + goal);
            api.getBuildScan().publishOnDemand();
        } else {
            logger.debug("Registering listener capturing build scan link");
            BuildScanServiceMessageSender.register(api.getBuildScan());
            logger.debug("Finished registering listener capturing build scan link");
        }
    }

    private static boolean invokesJetBrainsInfoGoal(MavenSession session) {
        return getJetBrainsInfoGoal(session) != null;
    }

    private static String getJetBrainsInfoGoal(MavenSession session) {
        List<String> goals = session.getGoals();
        return goals.size() == 1 && goals.get(0).startsWith("org.jetbrains.maven:info-maven3-plugin:") && goals.get(0).endsWith(":info") ? goals.get(0) : null;
    }

}
