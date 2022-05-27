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

        // When a maven build step has an explicitly defined pom file location, TeamCity will run an info goal on it.
        // This logic prevents that scan from being published.
        if (isJetbrainsInfoGoal(api, session)) {
            String goal = session.getGoals().get(0); // `isJetbrainsInfoGoal` guarantees exactly 1 element
            logger.debug("Skipping Build Scan publish for goal " + goal);
            api.getBuildScan().publishOnDemand();
        } else {
            logger.debug("Registering listener capturing build scan link");
            BuildScanServiceMessageSender.register(api.getBuildScan());
            logger.debug("Finished registering listener capturing build scan link");
        }
    }

    private boolean isJetbrainsInfoGoal(GradleEnterpriseApi api, MavenSession session) {
        List<String> goals = session.getGoals();
        if (goals.size() == 1) {
            String goal = goals.get(0);
            return goal.startsWith("org.jetbrains.maven:info-maven3-plugin:") && goal.endsWith(":info");
        } else {
            return false;
        }
    }

}
