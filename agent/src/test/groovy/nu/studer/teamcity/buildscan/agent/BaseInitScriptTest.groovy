package nu.studer.teamcity.buildscan.agent

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
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
                context.response
                        .contentType('application/vnd.gradle.scan-ack')
                        .send(gzip(smileWriter.writeValueAsBytes(body)))
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
        def args = ['tasks', '-I', initScriptFile.absolutePath]

        ((DefaultGradleRunner) GradleRunner.create())
                .withJvmArguments(jvmArgs)
                .withGradleVersion(gradleVersion.version)
                .withProjectDir(testProjectDir)
                .withArguments(args)
                .forwardOutput()
                .build()
    }

    void outputContainsTeamCityServiceMessageBuildStarted(BuildResult result) {
        assert result.output.contains("##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']")
    }

    void outputContainsTeamCityServiceMessageBuildScanUrl(BuildResult result) {
        assert result.output.contains("##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${mockScansServer.address}s/$PUBLIC_BUILD_SCAN_ID']")
    }

    static byte[] gzip(byte[] bytes) {
        def out = new ByteArrayOutputStream()
        new GZIPOutputStream(out).withStream { it.write(bytes) }
        out.toByteArray()
    }

}
