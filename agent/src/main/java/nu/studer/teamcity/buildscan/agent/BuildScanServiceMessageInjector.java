package nu.studer.teamcity.buildscan.agent;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for injecting a Gradle init script into all Gradle build runners. This init script itself registers a callback on the build scan plugin for any
 * published build scans and emits a TeamCity {@link jetbrains.buildServer.messages.serviceMessages.ServiceMessage} containing the scan URL.
 * <p>
 * In the presence of certain configuration parameters, this class will also inject Gradle Enterprise and Common Custom User Data plugins and extensions into Gradle and Maven
 * builds.
 */
@SuppressWarnings("SameParameterValue")
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

    // TeamCity Command-line runner

    private static final String COMMAND_LINE_RUNNER = "simpleRunner";

    // Gradle TeamCity Build Scan plugin

    private static final String GRADLE_BUILDSCAN_TEAMCITY_PLUGIN = "GRADLE_BUILDSCAN_TEAMCITY_PLUGIN";

    // TeamCity GE configuration parameters

    private static final String GE_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.url";

    private static final String GE_ALLOW_UNTRUSTED_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.allow-untrusted-server";

    private static final String GE_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.plugin.version";

    private static final String CCUD_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.plugin.version";

    private static final String GE_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.version";

    private static final String CCUD_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.extension.version";

    private static final String INSTRUMENT_COMMAND_LINE_RUNNER_CONFIG_PARAM = "buildScanPlugin.command-line-build-steps.enable";

    // Environment variables set to instrument the Gradle build

    private static final String GE_URL_VAR = "TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_URL";

    private static final String GE_ALLOW_UNTRUSTED_VAR = "TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_ALLOW_UNTRUSTED_SERVER";

    private static final String GE_PLUGIN_VERSION_VAR = "TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_PLUGIN_VERSION";

    private static final String CCUD_PLUGIN_VERSION_VAR = "TEAMCITYBUILDSCANPLUGIN_CCUD_PLUGIN_VERSION";

    // Maven system properties passed on the CLI to a Maven build

    private static final String GE_URL_MAVEN_PROPERTY = "gradle.enterprise.url";

    private static final String GE_ALLOW_UNTRUSTED_MAVEN_PROPERTY = "gradle.enterprise.allow-untrusted-server";

    private static final MavenCoordinates GE_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "gradle-enterprise-maven-extension");

    private static final MavenCoordinates CCUD_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "common-custom-user-data-maven-extension");

    @NotNull
    private final ExtensionApplicationListener extensionApplicationListener;

    public BuildScanServiceMessageInjector(@NotNull EventDispatcher<AgentLifeCycleListener> eventDispatcher,
                                           @NotNull ExtensionApplicationListener extensionApplicationListener) {
        eventDispatcher.addListener(this);
        this.extensionApplicationListener = extensionApplicationListener;
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        if (runner.getRunType().equalsIgnoreCase(GRADLE_RUNNER)) {
            instrumentGradleRunner(runner);
        } else if (runner.getRunType().equalsIgnoreCase(MAVEN_RUNNER)) {
            instrumentMavenRunner(runner);
        } else if (runner.getRunType().equalsIgnoreCase(COMMAND_LINE_RUNNER)) {
            if (getBooleanConfigParam(INSTRUMENT_COMMAND_LINE_RUNNER_CONFIG_PARAM, runner)) {
                instrumentCommandLineRunner(runner);
            }
        }
    }

    private void instrumentGradleRunner(@NotNull BuildRunnerContext runner) {
        addEnvVarIfSet(GE_URL_CONFIG_PARAM, GE_URL_VAR, runner);
        addEnvVarIfSet(GE_ALLOW_UNTRUSTED_CONFIG_PARAM, GE_ALLOW_UNTRUSTED_VAR, runner);
        addEnvVarIfSet(GE_PLUGIN_VERSION_CONFIG_PARAM, GE_PLUGIN_VERSION_VAR, runner);
        addEnvVarIfSet(CCUD_PLUGIN_VERSION_CONFIG_PARAM, CCUD_PLUGIN_VERSION_VAR, runner);

        String initScriptParam = "--init-script " + getInitScript(runner).getAbsolutePath();
        addGradleCmdParam(initScriptParam, runner);

        addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
    }

    private void instrumentMavenRunner(@NotNull BuildRunnerContext runner) {
        // for now, this intentionally ignores the configured extension versions and applies the bundled jars
        String extJarParam = "-Dmaven.ext.class.path=" + getExtensionsClasspath(runner);
        addMavenCmdParam(extJarParam, runner);

        addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
    }

    private void instrumentCommandLineRunner(@NotNull BuildRunnerContext runner) {
        // Instrument all Gradle builds
        copyInitScriptToGradleUserHome(runner);
        addEnvVarIfSet(GE_URL_CONFIG_PARAM, GE_URL_VAR, runner);
        addEnvVarIfSet(GE_ALLOW_UNTRUSTED_CONFIG_PARAM, GE_ALLOW_UNTRUSTED_VAR, runner);
        addEnvVarIfSet(GE_PLUGIN_VERSION_CONFIG_PARAM, GE_PLUGIN_VERSION_VAR, runner);
        addEnvVarIfSet(CCUD_PLUGIN_VERSION_CONFIG_PARAM, CCUD_PLUGIN_VERSION_VAR, runner);

        // Instrument all Maven builds
        String mavenOpts = "-Dmaven.ext.class.path=" + getExtensionsClasspath(runner);
        String geUrl = getOptionalConfigParam(GE_URL_CONFIG_PARAM, runner);
        if (geUrl != null) {
            mavenOpts = mavenOpts + " -D" + GE_URL_MAVEN_PROPERTY + "=" + geUrl;
        }
        if (getBooleanConfigParam(GE_ALLOW_UNTRUSTED_CONFIG_PARAM, runner)) {
            mavenOpts = mavenOpts + " -D" + GE_ALLOW_UNTRUSTED_MAVEN_PROPERTY + "=true";
        }
        appendEnvVar("MAVEN_OPTS", mavenOpts, runner);

        addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
    }

    @Override
    public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
        // delete init script from Gradle User Home
        File targetInitScript = getInitScriptInGradleUserHome(runner);
        if (targetInitScript.exists()) {
            FileUtil.delete(targetInitScript);
        }
    }

    private File getInitScript(BuildRunnerContext runner) {
        File initScript = new File(runner.getBuild().getAgentTempDirectory(), BUILD_SCAN_INIT_GRADLE);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_INIT_GRADLE, initScript);
        return initScript;
    }

    private void copyInitScriptToGradleUserHome(BuildRunnerContext runner) {
        File targetInitScript = getInitScriptInGradleUserHome(runner);
        if (!targetInitScript.exists()) {
            targetInitScript.getParentFile().mkdirs();
            FileUtil.copyResource(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_INIT_GRADLE, targetInitScript);
        }
    }

    private File getInitScriptInGradleUserHome(BuildRunnerContext runner) {
        String gradleUserHomeEnv = System.getenv("GRADLE_USER_HOME");
        File gradleUserHome = gradleUserHomeEnv == null
            ? new File(System.getProperty("user.home"), ".gradle")
            : new File(gradleUserHomeEnv);
        File initDir = new File(gradleUserHome, "init.d");
        // Include namespace in script name to avoid clashing with existing scripts
        return new File(initDir, "com.gradle.enterprise." + BUILD_SCAN_INIT_GRADLE);
    }

    private String getExtensionsClasspath(BuildRunnerContext runner) {
        List<File> extensionJars = new ArrayList<File>();

        // add extension to capture build scan URL
        extensionJars.add(getExtensionJar(BUILD_SCAN_EXT_MAVEN, runner));

        // optionally add extensions that connect the Maven build with Gradle Enterprise
        MavenExtensions extensions = getMavenExtensions(runner);
        String geExtensionVersion = getOptionalConfigParam(GE_EXTENSION_VERSION_CONFIG_PARAM, runner);
        if (geExtensionVersion != null) {
            if (!extensions.hasExtension(GE_EXTENSION_MAVEN_COORDINATES)) {
                extensionApplicationListener.geExtensionApplied(geExtensionVersion);
                extensionJars.add(getExtensionJar(GRADLE_ENTERPRISE_EXT_MAVEN, runner));
                addMavenSysPropIfSet(GE_URL_CONFIG_PARAM, GE_URL_MAVEN_PROPERTY, runner);
                addMavenSysPropIfSet(GE_ALLOW_UNTRUSTED_CONFIG_PARAM, GE_ALLOW_UNTRUSTED_MAVEN_PROPERTY, runner);
            }
        }

        String ccudExtensionVersion = getOptionalConfigParam(CCUD_EXTENSION_VERSION_CONFIG_PARAM, runner);
        if (ccudExtensionVersion != null) {
            if (!extensions.hasExtension(CCUD_EXTENSION_MAVEN_COORDINATES)) {
                extensionApplicationListener.ccudExtensionApplied(ccudExtensionVersion);
                extensionJars.add(getExtensionJar(COMMON_CUSTOM_USER_DATA_EXT_MAVEN, runner));
            }
        }

        return asClasspath(extensionJars);
    }

    private File getExtensionJar(String name, BuildRunnerContext runner) {
        File extensionJar = new File(runner.getBuild().getAgentTempDirectory(), name);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + name, extensionJar);
        return extensionJar;
    }

    private MavenExtensions getMavenExtensions(BuildRunnerContext runner) {
        String checkoutDirParam = getOptionalRunnerParam("teamcity.build.checkoutDir", runner);
        String workingDirParam = getOptionalRunnerParam("teamcity.build.workingDir", runner);
        String pomLocation = getOptionalRunnerParam("pomLocation", runner);

        File workingDir;
        if (checkoutDirParam != null && pomLocation != null) {
            // in TC, the pomLocation is always relative to the checkout dir, even if a specific working dir has been configured
            workingDir = new File(checkoutDirParam, pomLocation).getParentFile();
        } else if (workingDirParam != null) {
            // either the working dir is set explicitly in the TC config, or it is set implicitly as the value of the checkout dir
            workingDir = new File(workingDirParam);
        } else {
            // should never be the case
            workingDir = null;
        }

        return workingDir != null ? MavenExtensions.fromFile(new File(workingDir, ".mvn/extensions.xml")) : MavenExtensions.empty();
    }

    private static void addEnvVarIfSet(@NotNull String configParameter, @NotNull String key, @NotNull BuildRunnerContext runner) {
        String value = getOptionalConfigParam(configParameter, runner);
        if (value != null) {
            addEnvVar(key, value, runner);
        }
    }

    private static void addEnvVar(@NotNull String key, @NotNull String value, @NotNull BuildRunnerContext runner) {
        runner.addEnvironmentVariable(key, value);
    }

    private static void appendEnvVar(@NotNull String key, @NotNull String value, @NotNull BuildRunnerContext runner) {
        String existingValue = runner.getBuildParameters().getEnvironmentVariables().get(key);
        if (existingValue == null) {
            runner.addEnvironmentVariable(key, value);
        } else {
            runner.addEnvironmentVariable(key, existingValue + " " + value);
        }
    }

    private static void addGradleCmdParam(@NotNull String param, @NotNull BuildRunnerContext runner) {
        String gradleCmdParam = getOptionalRunnerParam(GRADLE_CMD_PARAMS, runner);
        runner.addRunnerParameter(GRADLE_CMD_PARAMS, gradleCmdParam != null ? param + " " + gradleCmdParam : param);
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
        String mavenCmdParam = getOptionalRunnerParam(MAVEN_CMD_PARAMS, runner);
        runner.addRunnerParameter(MAVEN_CMD_PARAMS, mavenCmdParam != null ? param + " " + mavenCmdParam : param);
    }

    private static boolean getBooleanConfigParam(@NotNull String paramName, @NotNull BuildRunnerContext runner) {
        return Boolean.parseBoolean(getOptionalConfigParam(paramName, runner));
    }

    @Nullable
    private static String getOptionalConfigParam(@NotNull String paramName, @NotNull BuildRunnerContext runner) {
        return getOptionalParam(paramName, runner.getConfigParameters());
    }

    @Nullable
    private static String getOptionalRunnerParam(@NotNull String paramName, @NotNull BuildRunnerContext runner) {
        return getOptionalParam(paramName, runner.getRunnerParameters());
    }

    @Nullable
    private static String getOptionalParam(@NotNull String paramName, @NotNull Map<String, String> params) {
        if (!params.containsKey(paramName)) {
            return null;
        }

        String value = params.get(paramName).trim();
        return value.isEmpty() ? null : value;
    }

    @NotNull
    private static String asClasspath(List<File> files) {
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(file.getAbsolutePath());
        }
        return sb.toString();
    }

}
