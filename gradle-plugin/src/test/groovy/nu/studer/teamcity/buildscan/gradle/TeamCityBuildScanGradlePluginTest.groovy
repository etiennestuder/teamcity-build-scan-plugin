package nu.studer.teamcity.buildscan.gradle


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.zip.GZIPOutputStream

class TeamCityBuildScanGradlePluginTest extends Specification {

    public static final String PUBLIC_SCAN_ID = "i2wepy2gr7ovw"

    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    File buildScript

    GradleRunner runner

    @AutoCleanup
    def mockScansServer = GroovyEmbeddedApp.of {
        def objectMapper = new ObjectMapper(new SmileFactory())

        handlers {
            post("in/:gradleVersion/:pluginVersion") { ctx ->
                def scanUrlString = "${mockScansServer.address}s/" + PUBLIC_SCAN_ID
                def os = new ByteArrayOutputStream()

                new GZIPOutputStream(os).withCloseable { stream ->
                    def generator = objectMapper.getFactory().createGenerator(stream)
                    generator.writeStartObject()
                    generator.writeFieldName("id")
                    generator.writeString(PUBLIC_SCAN_ID)
                    generator.writeFieldName("scanUrl")
                    generator.writeString(scanUrlString)
                    generator.writeEndObject()
                    generator.close()
                }

                response.contentType("application/vnd.gradle.scan-ack").send(os.toByteArray())
            }
        }
    }

    def setup() {
        buildScript = projectDir.newFile("build.gradle")
        runner = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withPluginClasspath()
    }

    def "service messages emitted for compatible plugin versions"() {
        given:
        applyScanPlugin("2.1")

        when:
        def result = runner.withArguments("tasks", "-S").build()

        then:
        result.output.contains("##teamcity[buildscan '${mockScansServer.address}s/${PUBLIC_SCAN_ID}'")
    }

    private void applyScanPlugin(String version) {
        buildScript.text = """
            plugins {
                id 'com.gradle.build-scan' version '${version}'
                id 'nu.studer.teamcity-build-scan'
            }
            
            buildScan {
                server = '${mockScansServer.address}'
                publishAlways()
            }
        """.stripIndent()
    }
}
