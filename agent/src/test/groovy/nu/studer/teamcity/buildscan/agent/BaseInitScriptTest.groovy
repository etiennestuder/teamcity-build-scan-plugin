package nu.studer.teamcity.buildscan.agent

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import jetbrains.buildServer.util.FileUtil
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.gradle.util.GradleVersion
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.TempDir

import java.util.zip.GZIPOutputStream

import static nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector.BUILD_SCAN_INIT_GRADLE

class BaseInitScriptTest extends Specification {

    static final List<JdkCompatibleGradleVersion> NOT_SUPPORTED_GRADLE_VERSIONS = [
            new JdkCompatibleGradleVersion(GradleVersion.version('3.5.1'), 7, 9),
            new JdkCompatibleGradleVersion(GradleVersion.version('4.0.2'), 7, 9)
    ]

    static final List<JdkCompatibleGradleVersion> SUPPORTED_GRADLE_VERSIONS = [
            new JdkCompatibleGradleVersion(GradleVersion.version('4.1'), 7, 9),
            new JdkCompatibleGradleVersion(GradleVersion.version('4.10.3'), 7, 10),
            new JdkCompatibleGradleVersion(GradleVersion.version('5.1.1'), 8, 11),
            new JdkCompatibleGradleVersion(GradleVersion.version('5.6.4'), 8, 12),
            new JdkCompatibleGradleVersion(GradleVersion.version('6.0.1'), 8, 13),
            new JdkCompatibleGradleVersion(GradleVersion.version('6.7'), 8, 15),
            new JdkCompatibleGradleVersion(GradleVersion.version('7.0.2'), 8, 16),
            new JdkCompatibleGradleVersion(GradleVersion.version('7.4.2'), 8, 17),
    ]

    static final String PUBLIC_BUILD_SCAN_ID = 'i2wepy2gr7ovw'
    static final String DEFAULT_SCAN_UPLOAD_TOKEN = 'scan-upload-token'

    File initScriptFile
    File settingsFile
    File buildFile

    @TempDir
    File testProjectDir

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

        FileUtil.copyResource(BuildScanServiceMessageInjector.class, '/' + BUILD_SCAN_INIT_GRADLE, initScriptFile)
        settingsFile << ""
        buildFile << ""
    }

    String maybeAddGradleEnterprisePlugin(GradleVersion gradleVersion, String server = mockScansServer.address) {
        if (gradleVersion < GradleVersion.version('5.0')) {
            '' // applied in build.gradle
        } else if (gradleVersion < GradleVersion.version('6.0')) {
            '' // applied in build.gradle
        } else {
            """
              plugins {
                id 'com.gradle.enterprise' version '3.4.1'
              }
              gradleEnterprise {
                server = '$server'
                buildScan {
                  publishAlways()
                }
              }
            """
        }
    }

    String maybeAddBuildScanPlugin(GradleVersion gradleVersion, String server = mockScansServer.address) {
        if (gradleVersion < GradleVersion.version('5.0')) {
            """
              plugins {
                id 'com.gradle.build-scan' version '1.16'
              }
              buildScan {
                server = '$server'
                publishAlways()
              }
            """
        } else if (gradleVersion < GradleVersion.version('6.0')) {
            """
              plugins {
                id 'com.gradle.build-scan' version '3.4.1'
              }
              gradleEnterprise {
                server = '$server'
                buildScan {
                  publishAlways()
                }
              }
            """
        } else {
            '' // applied in settings.gradle
        }
    }

    BuildResult run(GradleVersion gradleVersion = GradleVersion.current(), jvmArgs = []) {
        createRunner(gradleVersion, jvmArgs).build()
    }

    BuildResult runAndFail(GradleVersion gradleVersion = GradleVersion.current(), jvmArgs = []) {
        createRunner(gradleVersion, jvmArgs).buildAndFail()
    }

    GradleRunner createRunner(GradleVersion gradleVersion = GradleVersion.current(), jvmArgs = []) {
        def args = ['tasks', '-I', initScriptFile.absolutePath]

        ((DefaultGradleRunner) GradleRunner.create())
                .withJvmArguments(jvmArgs)
                .withGradleVersion(gradleVersion.version)
                .withProjectDir(testProjectDir)
                .withArguments(args)
                .forwardOutput()
    }

    void outputContainsTeamCityServiceMessageBuildStarted(BuildResult result) {
        assert result.output.contains("##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']")
    }

    void outputContainsTeamCityServiceMessageBuildScanUrl(BuildResult result) {
        assert 1 == result.output.count("##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${mockScansServer.address}s/$PUBLIC_BUILD_SCAN_ID']")
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
