package nu.studer.teamcity.buildscan.agent;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for injecting a Gradle init script into all Gradle build runners. This init script itself registers a callback on the build scan plugin for any
 * published build scans and emits a TeamCity {@link jetbrains.buildServer.messages.serviceMessages.ServiceMessage} containing the scan URL.
 * <p>
 * In the presence of certain configuration parameters, this class will also inject Develocity and Common Custom User Data plugins and extensions into Gradle and Maven
 * builds.
 */
@SuppressWarnings({"SameParameterValue", "ResultOfMethodCallIgnored", "Convert2Diamond"})
public class BuildScanServiceMessageInjector extends AgentLifeCycleAdapter {

    private static final Logger LOG = Logger.getInstance("jetbrains.buildServer.BUILDSCAN");

    // TeamCity Gradle runner

    private static final String GRADLE_RUNNER = "gradle-runner";
    private static final String GRADLE_CMD_PARAMS = "ui.gradleRunner.additional.gradle.cmd.params";
    private static final String BUILD_SCAN_INIT = "build-scan-init";
    private static final String BUILD_SCAN_INIT_GRADLE = BUILD_SCAN_INIT + ".gradle";

    // TeamCity Maven runner

    private static final String MAVEN_RUNNER = "Maven2";
    private static final String MAVEN_CMD_PARAMS = "runnerArgs";
    private static final String BUILD_SCAN_EXT_MAVEN = "service-message-maven-extension-1.0.jar";
    private static final String DEVELOCITY_EXT_MAVEN = "develocity-maven-extension-1.21.4.jar";
    private static final String COMMON_CUSTOM_USER_DATA_EXT_MAVEN = "common-custom-user-data-maven-extension-2.0.1.jar";

    // TeamCity Command-line runner

    private static final String COMMAND_LINE_RUNNER = "simpleRunner";

    // Gradle TeamCity Build Scan plugin

    private static final String GRADLE_BUILDSCAN_TEAMCITY_PLUGIN = "GRADLE_BUILDSCAN_TEAMCITY_PLUGIN";

    // TeamCity Develocity configuration parameters

    private static final String GRADLE_PLUGIN_REPOSITORY_CONFIG_PARAM = "buildScanPlugin.gradle.plugin-repository.url";
    private static final String DEVELOCITY_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.url";
    private static final String DEVELOCITY_ALLOW_UNTRUSTED_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.allow-untrusted-server";
    private static final String DEVELOCITY_ENFORCE_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.enforce-url";
    private static final String DEVELOCITY_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.plugin.version";
    private static final String CCUD_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.plugin.version";
    private static final String DEVELOCITY_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.version";
    private static final String CCUD_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.extension.version";
    private static final String CUSTOM_DEVELOCITY_EXTENSION_COORDINATES_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.custom.coordinates";
    private static final String CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM = "buildScanPlugin.ccud.extension.custom.coordinates";
    private static final String INSTRUMENT_COMMAND_LINE_RUNNER_CONFIG_PARAM = "buildScanPlugin.command-line-build-step.enabled";

    // Environment variables set to instrument the Gradle build

    private static final String GRADLE_PLUGIN_REPOSITORY_VAR = "GRADLE_PLUGIN_REPOSITORY_URL";
    private static final String DEVELOCITY_URL_VAR = "DEVELOCITY_URL";
    private static final String DEVELOCITY_ALLOW_UNTRUSTED_VAR = "DEVELOCITY_ALLOW_UNTRUSTED_SERVER";
    private static final String DEVELOCITY_ENFORCE_URL_VAR = "DEVELOCITY_ENFORCE_URL";
    private static final String DEVELOCITY_PLUGIN_VERSION_VAR = "DEVELOCITY_PLUGIN_VERSION";
    private static final String CCUD_PLUGIN_VERSION_VAR = "DEVELOCITY_CCUD_PLUGIN_VERSION";
    private static final String DEVELOCITY_INJECTION_ENABLED_VAR = "DEVELOCITY_INJECTION_ENABLED";
    private static final String INIT_SCRIPT_NAME_VAR = "DEVELOCITY_INJECTION_INIT_SCRIPT_NAME";
    private static final String DEVELOCITY_AUTO_INJECTION_CUSTOM_VALUE_VAR = "DEVELOCITY_AUTO_INJECTION_CUSTOM_VALUE";

    // Maven system properties passed on the CLI to a Maven build

    private static final String DEVELOCITY_URL_MAVEN_PROPERTY = "develocity.url";
    private static final String DEVELOCITY_ALLOW_UNTRUSTED_MAVEN_PROPERTY = "develocity.allowUntrustedServer";
    private static final String DEVELOCITY_EXTENSION_UPLOAD_IN_BACKGROUND_MAVEN_PROPERTY = "develocity.uploadInBackground";
    private static final String GRADLE_ENTERPRISE_URL_MAVEN_PROPERTY = "gradle.enterprise.url";
    private static final String GRADLE_ENTERPRISE_ALLOW_UNTRUSTED_MAVEN_PROPERTY = "gradle.enterprise.allowUntrustedServer";
    private static final MavenCoordinates DEVELOCITY_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "develocity-maven-extension");
    private static final MavenCoordinates GRADLE_ENTERPRISE_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "gradle-enterprise-maven-extension");
    private static final MavenCoordinates CCUD_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "common-custom-user-data-maven-extension");

    @NotNull
    private final ExtensionApplicationListener extensionApplicationListener;

    public BuildScanServiceMessageInjector(@NotNull EventDispatcher<AgentLifeCycleListener> eventDispatcher,
                                           @NotNull ExtensionApplicationListener extensionApplicationListener) {
        eventDispatcher.addListener(this);
        this.extensionApplicationListener = extensionApplicationListener;
    }

    @Override
    public void agentStarted(@NotNull BuildAgent agent) {
        purgeInitScriptsFromGradleUserHome();
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        if (runner.getRunType().equalsIgnoreCase(GRADLE_RUNNER)) {
            LOG.info("Attempt to instrument Gradle build with Develocity");
            instrumentGradleRunner(runner);
        } else if (runner.getRunType().equalsIgnoreCase(MAVEN_RUNNER)) {
            LOG.info("Attempt to instrument Maven build with Develocity");
            instrumentMavenRunner(runner);
        } else if (runner.getRunType().equalsIgnoreCase(COMMAND_LINE_RUNNER)) {
            if (getBooleanConfigParam(INSTRUMENT_COMMAND_LINE_RUNNER_CONFIG_PARAM, runner)) {
                LOG.info("Attempt to instrument command line build with Develocity");
                instrumentCommandLineRunner(runner);
            }
        }
    }

    @Override
    public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
        removeInitScriptFromGradleUserHome(runner);
    }

    private void instrumentGradleRunner(@NotNull BuildRunnerContext runner) {
        File initScript = getInitScriptInAgentTempDir(runner);
        addGradleInitScriptEnvVars(initScript, runner);

        String initScriptParam = "--init-script " + initScript.getAbsolutePath();
        addGradleCmdParam(initScriptParam, runner);

        addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
    }

    private void instrumentMavenRunner(@NotNull BuildRunnerContext runner) {
        // for now, this intentionally ignores the configured extension versions and applies the bundled jars
        String invocationArgs = getMavenInvocationArgs(runner);
        addMavenCmdParam(invocationArgs, runner);

        addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
    }

    private void instrumentCommandLineRunner(@NotNull BuildRunnerContext runner) {
        // instrument all Gradle builds
        String activationId = String.valueOf(System.currentTimeMillis());
        File initScript = copyInitScriptToGradleUserHome(activationId);
        addGradleInitScriptEnvVars(initScript, runner);

        // instrument all Maven builds
        String invocationArgs = getMavenInvocationArgs(runner);
        appendEnvVar("MAVEN_OPTS", invocationArgs, runner);

        addEnvVar(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN, "1", runner);
    }

    private void addGradleInitScriptEnvVars(@NotNull File initScript, @NotNull BuildRunnerContext runner) {
        addEnvVarIfSet(GRADLE_PLUGIN_REPOSITORY_CONFIG_PARAM, GRADLE_PLUGIN_REPOSITORY_VAR, runner);
        addEnvVarIfSet(DEVELOCITY_URL_CONFIG_PARAM, DEVELOCITY_URL_VAR, runner);
        addEnvVarIfSet(DEVELOCITY_ALLOW_UNTRUSTED_CONFIG_PARAM, DEVELOCITY_ALLOW_UNTRUSTED_VAR, runner);
        addEnvVarIfSet(DEVELOCITY_ENFORCE_URL_CONFIG_PARAM, DEVELOCITY_ENFORCE_URL_VAR, runner);
        addEnvVarIfSet(DEVELOCITY_PLUGIN_VERSION_CONFIG_PARAM, DEVELOCITY_PLUGIN_VERSION_VAR, runner);
        addEnvVarIfSet(CCUD_PLUGIN_VERSION_CONFIG_PARAM, CCUD_PLUGIN_VERSION_VAR, runner);

        // the init-script is inactive by default, supply the script name env var to activate it
        addEnvVar(INIT_SCRIPT_NAME_VAR, initScript.getName(), runner);
        addEnvVar(DEVELOCITY_INJECTION_ENABLED_VAR, "true", runner);
        addEnvVar(DEVELOCITY_AUTO_INJECTION_CUSTOM_VALUE_VAR, "TeamCity", runner);
    }

    private File getInitScriptInAgentTempDir(BuildRunnerContext runner) {
        File targetInitScript = new File(runner.getBuild().getAgentTempDirectory(), BUILD_SCAN_INIT_GRADLE);
        FileUtil.copyResource(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_INIT_GRADLE, targetInitScript);
        return targetInitScript;
    }

    private File copyInitScriptToGradleUserHome(String activationId) {
        File targetInitScript = new File(getInitScriptsDir(), BUILD_SCAN_INIT + "-" + activationId + ".gradle");
        targetInitScript.getParentFile().mkdirs();
        FileUtil.copyResource(BuildScanServiceMessageInjector.class, "/" + BUILD_SCAN_INIT_GRADLE, targetInitScript);
        return targetInitScript;
    }

    private void removeInitScriptFromGradleUserHome(BuildRunnerContext runner) {
        String initScriptName = runner.getBuildParameters().getEnvironmentVariables().get(INIT_SCRIPT_NAME_VAR);
        if (initScriptName != null && !initScriptName.equals(BUILD_SCAN_INIT_GRADLE)) {
            File targetInitScript = new File(getInitScriptsDir(), initScriptName);
            if (targetInitScript.exists()) {
                FileUtil.delete(targetInitScript);
            }
        }
    }

    private void purgeInitScriptsFromGradleUserHome() {
        File[] filesToDelete = getInitScriptsDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(BUILD_SCAN_INIT);
            }
        });
        if (filesToDelete != null) {
            FileUtil.deleteFiles(Arrays.asList(filesToDelete));
        }
    }

    @NotNull
    private File getInitScriptsDir() {
        return new File(getGradleUserHome(), "init.d");
    }

    @NotNull
    protected File getGradleUserHome() {
        String gradleUserHomeOverride = System.getProperty("gradle.user.home", System.getenv("GRADLE_USER_HOME"));
        return gradleUserHomeOverride == null
            ? new File(System.getProperty("user.home", System.getenv("USER_HOME")), ".gradle")
            : new File(gradleUserHomeOverride);
    }

    private String getMavenInvocationArgs(BuildRunnerContext runner) {
        List<File> extensionJars = new ArrayList<File>();
        List<String> sysProps = new ArrayList<String>();

        // add extension to capture build scan URL
        extensionJars.add(getExtensionJar(BUILD_SCAN_EXT_MAVEN, runner));

        // optionally add extensions that connect the Maven build with Develocity
        MavenExtensions extensions = getMavenExtensions(runner);
        String develocityExtensionVersion = getOptionalConfigParam(DEVELOCITY_EXTENSION_VERSION_CONFIG_PARAM, runner);
        if (develocityExtensionVersion != null) {
            String develocityUrl = getOptionalConfigParam(DEVELOCITY_URL_CONFIG_PARAM, runner);
            if (hasNoDevelocityOrGradleEnterpriseExtensionsApplied(runner, extensions)) {
                extensionApplicationListener.develocityExtensionApplied(develocityExtensionVersion);
                extensionJars.add(getExtensionJar(DEVELOCITY_EXT_MAVEN, runner));
                addSysPropIfSet(DEVELOCITY_URL_CONFIG_PARAM, DEVELOCITY_URL_MAVEN_PROPERTY, sysProps, runner);
                addSysPropIfSet(DEVELOCITY_ALLOW_UNTRUSTED_CONFIG_PARAM, DEVELOCITY_ALLOW_UNTRUSTED_MAVEN_PROPERTY, sysProps, runner);
                addSysProp(DEVELOCITY_EXTENSION_UPLOAD_IN_BACKGROUND_MAVEN_PROPERTY, "false", sysProps);
            } else if (develocityUrl != null && Boolean.parseBoolean(getOptionalConfigParam(DEVELOCITY_ENFORCE_URL_CONFIG_PARAM, runner))) {
                // set Develocity properties for extensions 1.21+
                addSysPropIfSet(DEVELOCITY_URL_CONFIG_PARAM, DEVELOCITY_URL_MAVEN_PROPERTY, sysProps, runner);
                addSysPropIfSet(DEVELOCITY_ALLOW_UNTRUSTED_CONFIG_PARAM, DEVELOCITY_ALLOW_UNTRUSTED_MAVEN_PROPERTY, sysProps, runner);
                // set GE properties for extensions 1.20.1 and below
                addSysPropIfSet(DEVELOCITY_URL_CONFIG_PARAM, GRADLE_ENTERPRISE_URL_MAVEN_PROPERTY, sysProps, runner);
                addSysPropIfSet(DEVELOCITY_ALLOW_UNTRUSTED_CONFIG_PARAM, GRADLE_ENTERPRISE_ALLOW_UNTRUSTED_MAVEN_PROPERTY, sysProps, runner);
            }
        }

        String ccudExtensionVersion = getOptionalConfigParam(CCUD_EXTENSION_VERSION_CONFIG_PARAM, runner);
        if (ccudExtensionVersion != null) {
            MavenCoordinates customCcudExtensionCoords = parseCoordinates(getOptionalConfigParam(CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM, runner));
            if (!extensions.hasExtension(CCUD_EXTENSION_MAVEN_COORDINATES) && !extensions.hasExtension(customCcudExtensionCoords)) {
                extensionApplicationListener.ccudExtensionApplied(ccudExtensionVersion);
                extensionJars.add(getExtensionJar(COMMON_CUSTOM_USER_DATA_EXT_MAVEN, runner));
            }
        }

        return "-Dmaven.ext.class.path=" + asClasspath(extensionJars) + " " + asArgs(sysProps);
    }

    private static boolean hasNoDevelocityOrGradleEnterpriseExtensionsApplied(BuildRunnerContext runner, MavenExtensions extensions) {
        MavenCoordinates customDevelocityExtensionCoords = parseCoordinates(getOptionalConfigParam(CUSTOM_DEVELOCITY_EXTENSION_COORDINATES_CONFIG_PARAM, runner));
        return !extensions.hasExtension(DEVELOCITY_EXTENSION_MAVEN_COORDINATES) &&
               !extensions.hasExtension(GRADLE_ENTERPRISE_EXTENSION_MAVEN_COORDINATES) &&
               !extensions.hasExtension(customDevelocityExtensionCoords);
    }

    private File getExtensionJar(String name, BuildRunnerContext runner) {
        File extensionJar = new File(runner.getBuild().getAgentTempDirectory(), name);
        FileUtil.copyResourceIfNotExists(BuildScanServiceMessageInjector.class, "/" + name, extensionJar);
        return extensionJar;
    }

    private MavenExtensions getMavenExtensions(BuildRunnerContext runner) {
        String checkoutDirParam = getOptionalRunnerParam("teamcity.build.checkoutDir", runner);
        String workingDirParam = getOptionalRunnerParam("teamcity.build.workingDir", runner);
        String pomLocationParam = getOptionalRunnerParam("pomLocation", runner);

        LOG.info("Checkout dir: " + checkoutDirParam);
        LOG.info("Working dir: " + workingDirParam);
        LOG.info("POM location: " + pomLocationParam);

        // checkout dir should always be set
        if (checkoutDirParam == null) {
            LOG.warn("Checkout dir is null: unable to determine location of .mvn/extensions.xml");
            return MavenExtensions.empty();
        }

        // working dir should always be set, either the working dir is set explicitly in the TC config, or it is set implicitly as the value of the checkout dir
        if (workingDirParam == null) {
            LOG.warn("Working dir is null: unable to determine location of .mvn/extensions.xml");
            return MavenExtensions.empty();
        }

        final List<File> searchLocations = new ArrayList<File>();

        // in TC, the pomLocation is always relative to the checkout dir, even if a specific working dir has been configured
        if (pomLocationParam != null) {
            File checkoutDir = new File(checkoutDirParam);
            File pomLocation = new File(checkoutDir, pomLocationParam);
            File pomContainingDir = pomLocation.isFile() ? pomLocation.getParentFile() : pomLocation;

            searchLocations.add(pomContainingDir);

            while (!pomContainingDir.getAbsoluteFile().equals(checkoutDir.getAbsoluteFile())) {
                pomContainingDir = pomContainingDir.getParentFile();
                searchLocations.add(pomContainingDir);
            }
        } else {
            searchLocations.add(new File(workingDirParam));
        }

        LOG.info("Searching for extensions file");
        for (File dir : searchLocations) {
            File extensionsFile = new File(dir, ".mvn/extensions.xml");
            if (extensionsFile.exists()) {
                LOG.info("Found extensions file: " + extensionsFile);
                return MavenExtensions.fromFile(extensionsFile);
            } else {
                LOG.info("Extensions file not found: " + extensionsFile);
            }
        }

        LOG.warn("Unable to find extensions file");
        return MavenExtensions.empty();
    }

    @Nullable
    static MavenCoordinates parseCoordinates(String groupAndArtifact) {
        if (groupAndArtifact == null || groupAndArtifact.trim().isEmpty()) {
            return null;
        } else {
            String[] ga = groupAndArtifact.split(":");
            if (ga.length == 2) {
                return new MavenCoordinates(ga[0], ga[1]);
            } else if (ga.length == 3) {
                return new MavenCoordinates(ga[0], ga[1], ga[2]);
            } else {
                return null;
            }
        }
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

    private static void addSysPropIfSet(@NotNull String configParameter, @NotNull String property, @NotNull List<String> sysProps, @NotNull BuildRunnerContext runner) {
        String value = getOptionalConfigParam(configParameter, runner);
        if (value != null) {
            addSysProp(property, value, sysProps);
        }
    }

    private static void addSysProp(@NotNull String property, @NotNull String value, @NotNull List<String> sysProps) {
        String sysProp = String.format("-D%s=%s", property, value);
        sysProps.add(sysProp);
    }

    private static void addGradleCmdParam(@NotNull String param, @NotNull BuildRunnerContext runner) {
        String gradleCmdParam = getOptionalRunnerParam(GRADLE_CMD_PARAMS, runner);
        runner.addRunnerParameter(GRADLE_CMD_PARAMS, gradleCmdParam != null ? param + " " + gradleCmdParam : param);
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

    @NotNull
    private static String asArgs(List<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(element);
        }
        return sb.toString();
    }

}
