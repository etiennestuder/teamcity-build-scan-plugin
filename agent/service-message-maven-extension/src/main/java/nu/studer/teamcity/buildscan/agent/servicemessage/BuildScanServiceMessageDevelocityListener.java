package nu.studer.teamcity.buildscan.agent.servicemessage;

import com.gradle.develocity.agent.maven.adapters.develocity.DevelocityApiAdapter;
import com.gradle.develocity.agent.maven.api.DevelocityApi;
import com.gradle.develocity.agent.maven.api.DevelocityListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
@Component(
    role = DevelocityListener.class,
    hint = "build-scan-service-message-develocity",
    description = "Interacts with Maven builds invoked from TeamCity"
)
public final class BuildScanServiceMessageDevelocityListener extends BuildScanServiceMessageListener implements DevelocityListener {
    private static final AtomicBoolean hasBeenCalled = new AtomicBoolean(false);

    @Override
    public void configure(DevelocityApi api, MavenSession session) {
        if (hasBeenCalled.getAndSet(true)) {
            return; // Ignore subsequent calls
        }

        super.configure(new DevelocityApiAdapter(api), session);
    }

}
