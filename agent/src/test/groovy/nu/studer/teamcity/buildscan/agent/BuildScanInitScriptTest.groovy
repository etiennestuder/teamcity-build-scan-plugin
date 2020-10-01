package nu.studer.teamcity.buildscan.agent

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import spock.lang.AutoCleanup

import java.util.zip.GZIPOutputStream

class BuildScanInitScriptTest extends BaseInitScriptTest {

    private static final String PUBLIC_BUILD_SCAN_ID = 'i2wepy2gr7ovw'
    private static final String DEFAULT_SCAN_UPLOAD_TOKEN = 'scan-upload-token'

    private static final List<GradleVersion> GRADLE_VERSIONS = [
            GradleVersion.version('4.10.3'),
            GradleVersion.version('5.6.4'),
            GradleVersion.current()
    ]

    @AutoCleanup
    def mockScansServer = GroovyEmbeddedApp.of {
        def jsonWriter = new ObjectMapper(new JsonFactory()).writer()
        def smileWriter = new ObjectMapper(new SmileFactory()).writer()
        handlers {
            post('in/:gradleVersion/:pluginVersion') {
                def scanUrlString = "${mockScansServer.address}s/" + PUBLIC_BUILD_SCAN_ID
                def body = [
                        id: PUBLIC_BUILD_SCAN_ID,
                        scanUrl: scanUrlString.toString(),
                ]
                context.response
                        .contentType('application/vnd.gradle.scan-ack')
                        .send(gzip(smileWriter.writeValueAsBytes(body)))
            }
            prefix('scans/publish') {
                post('gradle/:pluginVersion/token') {
                    def pluginVersion = context.pathTokens.pluginVersion
                    def scanUrlString = "${mockScansServer.address}s/" + PUBLIC_BUILD_SCAN_ID
                    def body = [
                            id: PUBLIC_BUILD_SCAN_ID,
                            scanUrl: scanUrlString.toString(),
                            scanUploadUrl: "${mockScansServer.address.toString()}scans/publish/gradle/$pluginVersion/upload".toString(),
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

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "can use build-scan init script without declaring build scan / Gradle Enterprise plugin (#gradleVersion)"() {
        when:
        def result = run(gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "can use build-scan init script in conjunction with the build scan / Gradle Enterprise plugin (#gradleVersion)"() {
        given:
        settingsFile << maybeAddGradleEnterprisePlugin(gradleVersion)
        buildFile << maybeAddBuildScanPlugin(gradleVersion)

        when:
        def result = run(gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        teamCityServiceMessageBuildScanUrl(result)

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    private String maybeAddGradleEnterprisePlugin(GradleVersion gradleVersion) {
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
                server = '${mockScansServer.address}'
                buildScan {
                  publishAlways()
                }
              }
            """
        }
    }

    private String maybeAddBuildScanPlugin(GradleVersion gradleVersion) {
        if (gradleVersion < GradleVersion.version('5.0')) {
            """
              plugins {
                id 'com.gradle.build-scan' version '1.16'
              }
              buildScan {
                server = '${mockScansServer.address}'
                publishAlways()
              }
            """
        } else if (gradleVersion < GradleVersion.version('6.0')) {
            """
              plugins {
                id 'com.gradle.build-scan' version '3.4.1'
              }
              gradleEnterprise {
                server = '${mockScansServer.address}'
                buildScan {
                  publishAlways()
                }
              }
            """
        } else {
            '' // applied in settings.gradle
        }
    }

    private void teamCityServiceMessageBuildScanUrl(BuildResult result) {
        assert result.output.contains("##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${mockScansServer.address}s/$PUBLIC_BUILD_SCAN_ID']")
    }

    private static byte[] gzip(byte[] bytes) {
        def out = new ByteArrayOutputStream()
        new GZIPOutputStream(out).withStream { it.write(bytes) }
        out.toByteArray()
    }

}
