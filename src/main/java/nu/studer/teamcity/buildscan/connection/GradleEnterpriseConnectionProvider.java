package nu.studer.teamcity.buildscan.connection;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor;
import jetbrains.buildServer.serverSide.oauth.OAuthProvider;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.*;

public class GradleEnterpriseConnectionProvider extends OAuthProvider {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");
    private static final String DEFAULT_PLUGIN_VERSIONS_RESOURCE = "default-plugin-versions.properties";

    private final PluginDescriptor descriptor;

    private static final PropertiesProcessor EMPTY_PROPERTIES_PROCESSOR = new EmptyPropertiesProcessor();

    public GradleEnterpriseConnectionProvider(PluginDescriptor pluginDescriptor) {
        super();
        this.descriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return GRADLE_ENTERPRISE_CONNECTION_PROVIDER;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Gradle Enterprise";
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return descriptor.getPluginResourcesPath("ge-connection-dialog.jsp");
    }

    @NotNull
    @Override
    public String describeConnection(@NotNull OAuthConnectionDescriptor connection) {
        Map<String, String> params = connection.getParameters();
        String description = "";

        String geUrl = params.get(GRADLE_ENTERPRISE_URL);
        if (geUrl != null) {
            description += String.format("Gradle Enterprise Url: %s\n", geUrl);
        }
        
        String gePluginVersion = params.get(GRADLE_ENTERPRISE_PLUGIN_VERSION);
        if (gePluginVersion != null) {
            description += String.format("Gradle Enterprise Plugin Version: %s\n", gePluginVersion);
        }

        String ccudPluginVersion = params.get(COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION);
        if (ccudPluginVersion != null) {
            description += String.format("Common Custom User Data Plugin Version: %s\n", ccudPluginVersion);
        }

        String geExtensionVersion = params.get(GRADLE_ENTERPRISE_EXTENSION_VERSION);
        if (geExtensionVersion != null) {
            description += String.format("Gradle Enterprise Extension Version: %s\n", geExtensionVersion);
        }

        String ccudExtensionVersion = params.get(COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION);
        if (ccudExtensionVersion != null) {
            description += String.format("Common Custom User Data Extension Version: %s\n", ccudExtensionVersion);
        }

        return description;
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultProperties() {
        Properties properties = new Properties();
        Map<String, String> defaultProperties = new HashMap<>();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PLUGIN_VERSIONS_RESOURCE);

        if (inputStream != null) {
            try {
                properties.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Unable to load default plugin version from " + DEFAULT_PLUGIN_VERSIONS_RESOURCE, e);
            }
        }

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            defaultProperties.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return defaultProperties;
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        // this can be used to invalidate properties set by the user
        // by returning empty list, no validation is done
        return EMPTY_PROPERTIES_PROCESSOR;
    }

    private static class EmptyPropertiesProcessor implements PropertiesProcessor {
        @Override
        public Collection<InvalidProperty> process(Map<String, String> properties) {
            return Collections.emptyList();
        }
    }
}
