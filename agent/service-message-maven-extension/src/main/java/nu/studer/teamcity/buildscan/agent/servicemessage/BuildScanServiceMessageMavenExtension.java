package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.maven.extension.api.scan.BuildScanApi;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Component(role = AbstractMavenLifecycleParticipant.class)
public final class BuildScanServiceMessageMavenExtension extends AbstractMavenLifecycleParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(BuildScanServiceMessageMavenExtension.class);

    private final PlexusContainer container;

    @Inject
    public BuildScanServiceMessageMavenExtension(PlexusContainer container) {
        this.container = container;
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        LOG.debug("Executing extension: {}", getClass().getSimpleName());
        BuildScanApi buildScan = BuildScanApiAccessor.lookup(container, getClass());
        if (buildScan != null) {
            LOG.debug("Registering listener capturing build scan link");
            BuildScanServiceMessageSender.register(buildScan);
            LOG.debug("Finished registering listener capturing build scan link");
        }
    }

}
