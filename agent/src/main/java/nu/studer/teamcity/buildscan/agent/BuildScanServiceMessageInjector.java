package nu.studer.teamcity.buildscan.agent;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is responsible for injecting a Gradle init script into all Gradle build runners. This init script itself
 * registers a callback on the build scan plugin for any published build scans and emits a TeamCity
 * {@link jetbrains.buildServer.messages.serviceMessages.ServiceMessage} containing the scan URL.
 *
 * In the presence of certain configuration parameters, this class will also inject Gradle Enterprise and
 * Common Custom User Data plugins and extensions into Gradle and Maven builds.
 */
public final class BuildScanServiceMessageInjector extends AgentLifeCycleAdapter {

    // TeamCity Gradle runner

    private static final String GRADLE_RUNNER = "gradle-runner";
    private static final String GRADLE_CMD_PARAMS = "ui.gradleRunner.additional.gradle.cmd.params";
    private static final String BUILD_SCAN_INIT_GRADLE = "build-scan-init.gradle";

    // TeamCity Maven runner

    private static final String MAVEN_RUNNER = "Maven2";
    private static final String MAVEN_CMD_PARAMS = "runnerArgs";
    private static final String BUILD_SCAN_EXT_MAVEN = "service-message-maven-extension-1.0.jar";
    private static final String GRADLE_ENTERPRISE_EXT_MAVEN = "gradle-enterprise-maven-extension-1.14.1.jar";
    private static final String COMMON_CUSTOM_USER_DATA_EXT_MAVEN = "common-custom-user-data-maven-extension-1.10.1.jar";

    // Gradle TeamCity Build Scan plugin

    private static final String GRADLE_BUILDSCAN_TEAMCITY_PLUGIN = "GRADLE_BUILDSCAN_TEAMCITY_PLUGIN";

    // TeamCity GE configuration parameters

    private static final String GE_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.url";

    private static final String GE_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.plugin.version";

    private static final String CCUD_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.plugin.version";

    private static final String GE_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.version";

    private static final String CCUD_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.extension.version";

    // Gradle properties and Maven system properties passed to the artifact instrumenting the Gradle / Maven build

    private static final String GE_URL_GRADLE_PROPERTY = "teamCityBuildScanPlugin.gradle-enterprise.url";

    private static final String GE_PLUGIN_VERSION_GRADLE_PROPERTY = "teamCityBuildScanPlugin.gradle-enterprise.plugin.version";

    private static final String CCUD_PLUGIN_VERSION_GRADLE_PROPERTY = "teamCityBuildScanPlugin.ccud.plugin.version";

    private static final String GE_URL_MAVEN_PROPERTY = "gradle.enterprise.url";

    private static final MavenCoordinates GE_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "gradle-enterprise-maven-extension");

    private static final MavenCoordinates CCUD_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "common-custom-user-data-maven-extension");

    public BuildScanServiceMessageInjector(@NotNull EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        if (runner.getRunType().equalsIgnoreCase(GRADLE_RUNNER)) {
            addGradleSysPropIfSet(GE_URL_CONFIG_PARAM, GE_URL_GRADLE_PROPERTY, runner);
            addGradleSysPropIfSet(GE_PLUGIN_VERSION_CONFIG_PARAM, GE_PLUGIN_VERSION_GRADLE_PROPERTY, runner);
            addGradleSysPropIfSet(CCUD_PLUGIN_VERSION_CONFIG_PARAM, CCUD_PLUGIN_VERSION_GRADLE_PROPERTY, runner);

            String initScriptParam = "--init-script " + getInitScript(runner).getAbsolutePath();
            addGradleCmdParam(initScriptParam, runner);

            addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
        } else if (runner.getRunType().equalsIgnoreCase(MAVEN_RUNNER)) {
            addMavenSysPropIfSet(GE_URL_CONFIG_PARAM, GE_URL_MAVEN_PROPERTY, runner);

            // For now, this intentionally ignores the configured extension versions and applies the bundled jars
            String extJarParam = "-Dmaven.ext.class.path=" + getExtensionsClasspath(runner);
            addMavenCmdParam(extJarParam, runner);

            addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
        }
    }

    private File getInitScript(BuildRunnerContext runner) {
        File initScript = new File(runner.getBuild().getAgentTempDirectory(), BUILD_SCAN_INIT_GRADLE);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_INIT_GRADLE, initScript);
        return initScript;
    }

    private String getExtensionsClasspath(BuildRunnerContext runner) {
        List<File> extensionJars = new ArrayList<>();

        // add extension to capture build scan URL
        extensionJars.add(getExtensionJar(BUILD_SCAN_EXT_MAVEN, runner));

        // optionally add extensions that connect the Maven build with Gradle Enterprise
        MavenExtensions extensions = getMavenExtensions(runner);
        String geExtensionVersion = getOptionalConfigParam(GE_EXTENSION_VERSION_CONFIG_PARAM, runner);
        if (geExtensionVersion != null) {
            if (!extensions.hasExtension(GE_EXTENSION_MAVEN_COORDINATES)) {
                extensionJars.add(getExtensionJar(GRADLE_ENTERPRISE_EXT_MAVEN, runner));
            }
        }

        String ccudExtensionVersion = getOptionalConfigParam(CCUD_EXTENSION_VERSION_CONFIG_PARAM, runner);
        if (ccudExtensionVersion != null) {
            if (!extensions.hasExtension(CCUD_EXTENSION_MAVEN_COORDINATES)) {
                extensionJars.add(getExtensionJar(COMMON_CUSTOM_USER_DATA_EXT_MAVEN, runner));
            }
        }

        return extensionJars.stream().map(File::getAbsolutePath).collect(Collectors.joining(File.pathSeparator));
    }

    private File getExtensionJar(String name, BuildRunnerContext runner) {
        File extensionJar = new File(runner.getBuild().getAgentTempDirectory(), name);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + name, extensionJar);
        return extensionJar;
    }

    private MavenExtensions getMavenExtensions(BuildRunnerContext runner) {
        String checkoutDir = getOrDefault("teamcity.build.checkoutDir", runner);
        File extensionFile = new File(checkoutDir, ".mvn/extensions.xml");
        return MavenExtensions.fromFile(extensionFile);
    }

    private boolean needsExtension(String configParam, MavenCoordinates extension, MavenExtensions extensions, BuildRunnerContext runner) {
        String version = getOptionalConfigParam(configParam, runner);
        return version != null && !extensions.hasExtension(extension);
    }

    @SuppressWarnings("SameParameterValue")
    private static void addEnvVar(@NotNull String key, @NotNull String value, @NotNull BuildRunnerContext runner) {
        runner.addEnvironmentVariable(key, value);
    }

    private static void addGradleSysPropIfSet(@NotNull String configParameter, @NotNull String property, @NotNull BuildRunnerContext runner) {
        String value = getOptionalConfigParam(configParameter, runner);
        if (value != null) {
            addGradleSysProp(property, value, runner);
        }
    }

    private static void addGradleSysProp(@NotNull String key, @NotNull String value, @NotNull BuildRunnerContext runner) {
        String systemProp = String.format("-D%s=%s", key, value);
        addGradleCmdParam(systemProp, runner);
    }

    private static void addGradleCmdParam(@NotNull String param, @NotNull BuildRunnerContext runner) {
        String existingParams = getOrDefault(GRADLE_CMD_PARAMS, runner);
        runner.addRunnerParameter(GRADLE_CMD_PARAMS, param + " " + existingParams);
    }

    private static void addMavenSysPropIfSet(@NotNull String configParameter, @NotNull String property, @NotNull BuildRunnerContext runner) {
        String value = getOptionalConfigParam(configParameter, runner);
        if (value != null) {
            addMavenSysProp(property, value, runner);
        }
    }

    private static void addMavenSysProp(@NotNull String key, @NotNull String value, @NotNull BuildRunnerContext runner) {
        String systemProp = String.format("-D%s=%s", key, value);
        addMavenCmdParam(systemProp, runner);
    }

    private static void addMavenCmdParam(@NotNull String param, @NotNull BuildRunnerContext runner) {
        String existingParams = getOrDefault(MAVEN_CMD_PARAMS, runner);
        runner.addRunnerParameter(MAVEN_CMD_PARAMS, param + " " + existingParams);
    }

    @Nullable
    private static String getOptionalConfigParam(@NotNull String paramName, @NotNull BuildRunnerContext runner) {
        Map<String, String> configParameters = runner.getConfigParameters();
        if (!configParameters.containsKey(paramName)) {
            return null;
        }

        String value = configParameters.get(paramName).trim();
        return value.isEmpty() ? null : value;
    }

    @SuppressWarnings("Java8MapApi") // support JDK6
    private static String getOrDefault(@NotNull String paramName, @NotNull BuildRunnerContext runner) {
        Map<String, String> runnerParameters = runner.getRunnerParameters();
        return runnerParameters.containsKey(paramName) ? runnerParameters.get(paramName) : "";
    }

}
