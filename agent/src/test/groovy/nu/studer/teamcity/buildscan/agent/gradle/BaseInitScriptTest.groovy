package nu.studer.teamcity.buildscan.agent.gradle

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.FileUtil
import nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector
import nu.studer.teamcity.buildscan.agent.ExtensionApplicationListener
import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.TestBuildRunnerContext
import nu.studer.teamcity.buildscan.agent.gradle.testutils.TestBuildScanServiceMessageInjector
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.gradle.util.GradleVersion
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.TempDir

import java.util.stream.Collectors
import java.util.zip.GZIPOutputStream

import static nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector.BUILD_SCAN_INIT_GRADLE

class BaseInitScriptTest extends Specification {

    static final JdkCompatibleGradleVersion GRADLE_2_6 = new JdkCompatibleGradleVersion(GradleVersion.version('2.6'), 6, 8)
    static final JdkCompatibleGradleVersion GRADLE_2_14 = new JdkCompatibleGradleVersion(GradleVersion.version('2.14.1'), 6, 8)
    static final JdkCompatibleGradleVersion GRADLE_3_0 = new JdkCompatibleGradleVersion(GradleVersion.version('3.0'), 7, 9)
    static final JdkCompatibleGradleVersion GRADLE_3_5 = new JdkCompatibleGradleVersion(GradleVersion.version('3.5.1'), 7, 9)
    static final JdkCompatibleGradleVersion GRADLE_4_0 = new JdkCompatibleGradleVersion(GradleVersion.version('4.0.2'), 7, 9)
    static final JdkCompatibleGradleVersion GRADLE_4_10 = new JdkCompatibleGradleVersion(GradleVersion.version('4.10.3'), 7, 10)
    static final JdkCompatibleGradleVersion GRADLE_5_0 = new JdkCompatibleGradleVersion(GradleVersion.version('5.0'), 8, 11)
    static final JdkCompatibleGradleVersion GRADLE_5_6 = new JdkCompatibleGradleVersion(GradleVersion.version('5.6.4'), 8, 12)
    static final JdkCompatibleGradleVersion GRADLE_6_0 = new JdkCompatibleGradleVersion(GradleVersion.version('6.0.1'), 8, 13)
    static final JdkCompatibleGradleVersion GRADLE_6_9 = new JdkCompatibleGradleVersion(GradleVersion.version('6.9.4'), 8, 15)
    static final JdkCompatibleGradleVersion GRADLE_7_0 = new JdkCompatibleGradleVersion(GradleVersion.version('7.0.2'), 8, 16)
    static final JdkCompatibleGradleVersion GRADLE_7_6 = new JdkCompatibleGradleVersion(GradleVersion.version('7.6.1'), 8, 19)
    static final JdkCompatibleGradleVersion GRADLE_8_0 = new JdkCompatibleGradleVersion(GradleVersion.version('8.0.2'), 8, 19)
    static final JdkCompatibleGradleVersion GRADLE_8_7 = new JdkCompatibleGradleVersion(GradleVersion.version('8.7'), 8, 21)

    static final List<JdkCompatibleGradleVersion> GRADLE_VERSIONS_2_AND_HIGHER = [
        GRADLE_2_6, // first version supported byTestKit
        GRADLE_2_14,
        GRADLE_3_0,
        GRADLE_3_5,
        GRADLE_4_0,
        GRADLE_4_10,
        GRADLE_5_0,
        GRADLE_5_6,
        GRADLE_6_0,
        GRADLE_6_9,
        GRADLE_7_0,
        GRADLE_7_6,
        GRADLE_8_0,
        GRADLE_8_7,
    ]

    static final List<JdkCompatibleGradleVersion> GRADLE_VERSIONS_3_0_AND_HIGHER =
        GRADLE_VERSIONS_2_AND_HIGHER - [GRADLE_2_6, GRADLE_2_14]

    static final List<JdkCompatibleGradleVersion> GRADLE_VERSIONS_3_5_AND_HIGHER =
            GRADLE_VERSIONS_3_0_AND_HIGHER - [GRADLE_3_0]

    static final List<JdkCompatibleGradleVersion> GRADLE_VERSIONS_4_AND_HIGHER =
        GRADLE_VERSIONS_3_5_AND_HIGHER - [GRADLE_3_5]

    static final List<JdkCompatibleGradleVersion> GRADLE_VERSIONS_CONFIGURATION_CACHE_COMPATIBLE =
        GRADLE_VERSIONS_4_AND_HIGHER.findAll { it.gradleVersion >= GradleVersion.version('6.9') }

    static final String PUBLIC_BUILD_SCAN_ID = 'i2wepy2gr7ovw'
    static final String DEFAULT_SCAN_UPLOAD_TOKEN = 'scan-upload-token'

    static final String DEVELOCITY_PLUGIN_VERSION = '3.19.2'
    static final String CCUD_PLUGIN_VERSION = '2.2.1'

    File initScriptFile
    File settingsFile
    File buildFile

    @TempDir
    File testProjectDir

    @TempDir
    File agentTempDir

    @AutoCleanup
    def mockScansServer = GroovyEmbeddedApp.of {
        def jsonWriter = new ObjectMapper(new JsonFactory()).writer()
        def smileWriter = new ObjectMapper(new SmileFactory()).writer()

        handlers {
            post('in/:gradleVersion/:pluginVersion') {
                def scanUrlString = "${mockScansServer.address}s/$PUBLIC_BUILD_SCAN_ID"
                def body = [
                    id     : PUBLIC_BUILD_SCAN_ID,
                    scanUrl: scanUrlString.toString(),
                ]
                def out = new ByteArrayOutputStream()
                new GZIPOutputStream(out).withStream { smileWriter.writeValue(it, body) }
                context.response
                    .contentType('application/vnd.gradle.scan-ack')
                    .send(out.toByteArray())
            }
            prefix('scans/publish') {
                post('gradle/:pluginVersion/token') {
                    def pluginVersion = context.pathTokens.pluginVersion
                    def scanUrlString = "${mockScansServer.address}s/$PUBLIC_BUILD_SCAN_ID"
                    def body = [
                        id             : PUBLIC_BUILD_SCAN_ID,
                        scanUrl        : scanUrlString.toString(),
                        scanUploadUrl  : "${mockScansServer.address.toString()}scans/publish/gradle/$pluginVersion/upload".toString(),
                        scanUploadToken: DEFAULT_SCAN_UPLOAD_TOKEN
                    ]
                    context.response
                        .contentType('application/vnd.gradle.scan-ack+json')
                        .send(jsonWriter.writeValueAsBytes(body))
                }
                post('gradle/:pluginVersion/upload') {
                    context.request.getBody(1024 * 1024 * 10).then {
                        context.response
                            .contentType('application/vnd.gradle.scan-upload-ack+json')
                            .send()
                    }
                }
                notFound()
            }
        }
    }

    def setup() {
        initScriptFile = new File(testProjectDir, 'initscript.gradle')
        settingsFile = new File(testProjectDir, 'settings.gradle')
        buildFile = new File(testProjectDir, 'build.gradle')

        //noinspection GroovyAccessibility, GrDeprecatedAPIUsage
        FileUtil.copyResource(BuildScanServiceMessageInjector.class, '/' + BUILD_SCAN_INIT_GRADLE, initScriptFile)
        settingsFile << ''
        buildFile << ''
    }

    def declareDevelocityPluginApplication(GradleVersion gradleVersion, URI geUrl = mockScansServer.address) {
        settingsFile << maybeAddPluginsToSettings(gradleVersion, geUrl)
        buildFile << maybeAddPluginsToRootProject(gradleVersion, geUrl)
    }

    def declareDevelocityPluginAndCcudPluginApplication(GradleVersion gradleVersion, URI geUrl = mockScansServer.address) {
        settingsFile << maybeAddPluginsToSettings(gradleVersion, geUrl, CCUD_PLUGIN_VERSION)
        buildFile << maybeAddPluginsToRootProject(gradleVersion, geUrl, CCUD_PLUGIN_VERSION)
    }

    String maybeAddPluginsToSettings(GradleVersion gradleVersion, URI geUrl, String ccudPluginVersion = null) {
        if (gradleVersion < GradleVersion.version('5.0')) {
            '' // applied in build.gradle
        } else if (gradleVersion < GradleVersion.version('6.0')) {
            '' // applied in build.gradle
        } else {
            """
              plugins {
                id 'com.gradle.develocity' version '${DEVELOCITY_PLUGIN_VERSION}'
                ${ccudPluginVersion ? "id 'com.gradle.common-custom-user-data-gradle-plugin' version '$ccudPluginVersion'" : ""}
              }
              develocity.server = '$geUrl'
            """
        }
    }

    String maybeAddPluginsToRootProject(GradleVersion gradleVersion, URI geUrl, String ccudPluginVersion = null) {
        if (gradleVersion < GradleVersion.version('5.0')) {
            """
              plugins {
                id 'com.gradle.build-scan' version '1.16'
                ${ccudPluginVersion ? "id 'com.gradle.common-custom-user-data-gradle-plugin' version '$ccudPluginVersion'" : ""}
              }
              buildScan {
                server = '$geUrl'
                publishAlways()
              }
            """
        } else if (gradleVersion < GradleVersion.version('6.0')) {
            """
              plugins {
                id 'com.gradle.develocity' version '${DEVELOCITY_PLUGIN_VERSION}'
                ${ccudPluginVersion ? "id 'com.gradle.common-custom-user-data-gradle-plugin' version '$ccudPluginVersion'" : ""}
              }
              develocity.server = '$geUrl'
            """
        } else {
            '' // applied in settings.gradle
        }
    }

    BuildResult run(GradleVersion gradleVersion) {
        run(new BuildConfig(gradleVersion: gradleVersion))
    }

    BuildResult run(GradleVersion gradleVersion, TcPluginConfig tcPluginConfig) {
        run(new BuildConfig(gradleVersion: gradleVersion, tcPluginConfig: tcPluginConfig))
    }

    BuildResult run(BuildConfig config) {
        DefaultGradleRunner testKitRunner = new DefaultGradleRunner()
            .withProjectDir(testProjectDir)
            .withGradleVersion(config.gradleVersion.version)
            .forwardOutput() as DefaultGradleRunner

        File gradleUserHome = testKitRunner.testKitDirProvider.dir
        def injector = new TestBuildScanServiceMessageInjector(gradleUserHome, EventDispatcher.create(AgentLifeCycleListener.class), Mock(ExtensionApplicationListener))

        TestBuildRunnerContext context = new TestBuildRunnerContext(config.runType, agentTempDir, config.tcPluginConfig.toConfigParameters(), [:])
        injector.beforeRunnerStart(context)

        def args = ['tasks']
        def gradleArgs = context.runnerParameters.get("ui.gradleRunner.additional.gradle.cmd.params")
        if (gradleArgs) {
            args += (gradleArgs.split(' ') as List<String>)
        }
        testKitRunner.withArguments(args)

        if (testKitSupportsEnvVars(config)) {
            testKitRunner.withEnvironment(context.buildParameters.environmentVariables)
            testKitRunner.withJvmArguments(config.additionalJvmArgs)
        } else {
            def systemProps = mapEnvVarsToSystemProps(context.buildParameters.environmentVariables)
            testKitRunner.withJvmArguments(systemProps + config.additionalJvmArgs)
        }

        try {
            return testKitRunner.build()
        } finally {
            // Run the finish hook, and check that any file was deleted from Gradle User Home
            injector.runnerFinished(context, BuildFinishedStatus.FINISHED_SUCCESS)

            def initScriptsDir = new File(gradleUserHome, 'init.d')
            if (initScriptsDir.exists()) {
                assert initScriptsDir.list() as List<String> == []
            }
        }
    }

    private boolean testKitSupportsEnvVars(BuildConfig config) {
        // TestKit supports env vars for Gradle 3.5+, except on M1 Mac where only 6.9+ is supported
        def isM1Mac = System.getProperty("os.arch") == "aarch64"
        if (isM1Mac) {
            return config.gradleVersion.baseVersion >= GRADLE_6_9.gradleVersion
        } else {
            return config.gradleVersion.baseVersion >= GRADLE_3_5.gradleVersion
        }
    }

    void outputContainsTeamCityServiceMessageBuildStarted(BuildResult result) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']"
        assert result.output.contains(serviceMsg)
        assert 1 == result.output.count(serviceMsg)
    }

    void outputContainsTeamCityServiceMessageBuildScanUrl(BuildResult result) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${mockScansServer.address}s/$PUBLIC_BUILD_SCAN_ID']"
        assert result.output.contains(serviceMsg)
        assert 1 == result.output.count(serviceMsg)
    }

    // for TestKit versions that don't support environment variables, map those vars to system properties
    private static List<String> mapEnvVarsToSystemProps(Map<String, String> envVars) {
        def mapping = [
            DEVELOCITY_URL                        : "develocity.url",
            DEVELOCITY_ALLOW_UNTRUSTED_SERVER     : "develocity.allow-untrusted-server",
            DEVELOCITY_ENFORCE_URL                : "develocity.enforce-url",
            DEVELOCITY_PLUGIN_VERSION             : "develocity.plugin.version",
            DEVELOCITY_CCUD_PLUGIN_VERSION        : "develocity.ccud-plugin.version",
            GRADLE_PLUGIN_REPOSITORY_URL          : "gradle.plugin-repository.url",
            DEVELOCITY_INJECTION_INIT_SCRIPT_NAME : "develocity.injection.init-script-name",
            DEVELOCITY_INJECTION_ENABLED          : "develocity.injection-enabled",
            DEVELOCITY_AUTO_INJECTION_CUSTOM_VALUE: "develocity.auto-injection.custom-value"
        ]

        return envVars.entrySet().stream().map(e -> {
            def sysPropName = mapping.get(e.key) ?: e.key
            return "-D" + sysPropName + "=" + e.value
        }).collect(Collectors.toList())
    }

    static final class BuildConfig {

        GradleVersion gradleVersion = GradleVersion.current()
        TcPluginConfig tcPluginConfig = new TcPluginConfig()
        String runType = "gradle-runner"
        List<String> additionalJvmArgs = []
    }

    static final class JdkCompatibleGradleVersion {

        final GradleVersion gradleVersion
        private final Integer jdkMin
        private final Integer jdkMax

        JdkCompatibleGradleVersion(GradleVersion gradleVersion, Integer jdkMin, Integer jdkMax) {
            this.gradleVersion = gradleVersion
            this.jdkMin = jdkMin
            this.jdkMax = jdkMax
        }

        boolean isJvmVersionCompatible() {
            def jvmVersion = getJvmVersion()
            jdkMin <= jvmVersion && jvmVersion <= jdkMax
        }

        private static int getJvmVersion() {
            String version = System.getProperty('java.version')
            if (version.startsWith('1.')) {
                Integer.parseInt(version.substring(2, 3))
            } else {
                Integer.parseInt(version.substring(0, version.indexOf('.')))
            }
        }

        @Override
        String toString() {
            return "JdkCompatibleGradleVersion{" +
                "Gradle " + gradleVersion.version +
                ", JDK " + jdkMin + "-" + jdkMax +
                '}'
        }

    }

}
