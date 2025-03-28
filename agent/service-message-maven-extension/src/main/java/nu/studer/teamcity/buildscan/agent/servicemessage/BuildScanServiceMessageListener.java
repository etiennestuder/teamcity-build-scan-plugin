package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.develocity.agent.maven.adapters.BuildScanApiAdapter;
import com.gradle.develocity.agent.maven.adapters.DevelocityAdapter;
import org.apache.maven.execution.MavenSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class BuildScanServiceMessageListener {

    private static final AtomicBoolean hasBeenCalled = new AtomicBoolean(false);
    private final Logger logger = LoggerFactory.getLogger(BuildScanServiceMessageListener.class);

    protected void configure(DevelocityAdapter develocity, MavenSession session) {
        // A bug in DV extension 1.23.1 can result in duplicate listener calls.
        // This guard will ensure that we only capture the build scan link once per Maven execution.
        if (hasBeenCalled.getAndSet(true)) {
            return; // Ignore subsequent calls
        }

        logger.debug("Executing extension: " + getClass().getSimpleName());

        // When a Maven build step has an explicitly defined pom.xml file location, TeamCity will run an info goal on it. Ignore build scan publication in this case.
        BuildScanApiAdapter buildScan = develocity.getBuildScan();
        if (invokesJetBrainsInfoGoal(session)) {
            String goal = getJetBrainsInfoGoal(session);
            logger.debug("Skipping build scan publishing for goal " + goal);
            buildScan.publishOnDemand();
        } else {
            logger.debug("Registering listener capturing build scan link");
            BuildScanServiceMessageSender.register(buildScan);
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
