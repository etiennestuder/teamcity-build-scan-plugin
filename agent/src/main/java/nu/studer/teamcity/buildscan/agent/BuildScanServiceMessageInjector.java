package nu.studer.teamcity.buildscan.agent;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

/**
 * This class is responsible for injecting a Gradle init script into all Gradle build runners. This init script itself
 * registers a callback on the build scan plugin for any published build scans and emits a TeamCity
 * {@link jetbrains.buildServer.messages.serviceMessages.ServiceMessage} containing the scan URL.
 */
public final class BuildScanServiceMessageInjector extends AgentLifeCycleAdapter {

    private static final String GRADLE_RUNNER = "gradle-runner";
    private static final String GRADLE_CMD_PARAMS = "ui.gradleRunner.additional.gradle.cmd.params";
    private static final String BUILD_SCAN_INIT_GRADLE = "build-scan-init.gradle";

    private static final String MAVEN_RUNNER = "Maven2";
    private static final String MAVEN_CMD_PARAMS = "runnerArgs";
    private static final String BUILD_SCAN_EXT_MAVEN = "service-message-maven-extension-1.0.jar";

    private static final String GRADLE_BUILDSCAN_TEAMCITY_PLUGIN = "GRADLE_BUILDSCAN_TEAMCITY_PLUGIN";

    private static final String GRADLE_ENTERPRISE_URL_CONFIG_PARAM = "GRADLE_ENTERPRISE_URL";

    private static final String GRADLE_ENTERPRISE_PLUGIN_VERSION_CONFIG_PARAM = "GRADLE_ENTERPRISE_PLUGIN_VERSION";

    private static final String CCUD_PLUGIN_VERSION_CONFIG_PARAM = "CCUD_PLUGIN_VERSION";

    private static final String GE_URL_GRADLE_PROPERTY = "teamCityBuildScanPlugin.gradle.enterprise.url";

    private static final String GE_PLUGIN_VERSION_GRADLE_PROPERTY = "teamCityBuildScanPlugin.gradle.enterprise.plugin.version";

    private static final String CCUD_VERSION_GRADLE_PROPERTY = "teamCityBuildScanPlugin.ccud.plugin.version";

    public BuildScanServiceMessageInjector(@NotNull EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        if (runner.getRunType().equalsIgnoreCase(GRADLE_RUNNER)) {
            addGradleSysPropIfSet(GRADLE_ENTERPRISE_URL_CONFIG_PARAM, GE_URL_GRADLE_PROPERTY, runner);
            addGradleSysPropIfSet(GRADLE_ENTERPRISE_PLUGIN_VERSION_CONFIG_PARAM, GE_PLUGIN_VERSION_GRADLE_PROPERTY, runner);
            addGradleSysPropIfSet(CCUD_PLUGIN_VERSION_CONFIG_PARAM, CCUD_VERSION_GRADLE_PROPERTY, runner);

            String initScriptParam = "--init-script " + getInitScript(runner).getAbsolutePath();
            addGradleCmdParam(initScriptParam, runner);
            runner.addEnvironmentVariable(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1");
        } else if (runner.getRunType().equalsIgnoreCase(MAVEN_RUNNER)) {
            String existingParams = getOrDefault(MAVEN_CMD_PARAMS, runner);
            String extJarParam = "-Dmaven.ext.class.path=" + getExtensionJar(runner).getAbsolutePath();

            runner.addRunnerParameter(MAVEN_CMD_PARAMS, extJarParam + " " + existingParams);
            runner.addEnvironmentVariable(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1");
        }
    }

    private File getInitScript(BuildRunnerContext runner) {
        File initScript = new File(runner.getBuild().getAgentTempDirectory(), BUILD_SCAN_INIT_GRADLE);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_INIT_GRADLE, initScript);
        return initScript;
    }

    private File getExtensionJar(BuildRunnerContext runner) {
        File extensionJar = new File(runner.getBuild().getAgentTempDirectory(), BUILD_SCAN_EXT_MAVEN);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_EXT_MAVEN, extensionJar);
        return extensionJar;
    }

    @SuppressWarnings("Java8MapApi") // support JDK6
    private static String getOrDefault(@NotNull String paramName, @NotNull BuildRunnerContext runner) {
        Map<String, String> runnerParameters = runner.getRunnerParameters();
        return runnerParameters.containsKey(paramName) ? runnerParameters.get(paramName) : "";
    }

    private static void addGradleSysPropIfSet(@NotNull String configParameter, @NotNull String gradleProperty, @NotNull BuildRunnerContext runner) {
        String value = getOptionalConfigParam(runner, configParameter);
        if (value != null) {
            addGradleSysProp(gradleProperty, value, runner);
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

    @Nullable
    private static String getOptionalConfigParam(@NotNull BuildRunnerContext runner, @NotNull String paramName) {
        Map<String, String> configParameters = runner.getConfigParameters();
        if (!configParameters.containsKey(paramName)) {
            return null;
        }

        String value = configParameters.get(paramName).trim();
        return value.isEmpty() ? null : value;
    }
}
