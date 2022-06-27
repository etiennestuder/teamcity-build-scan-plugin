package nu.studer.teamcity.buildscan.agent

import org.w3c.dom.Document
import spock.lang.Specification
import spock.lang.TempDir

class MavenExtensionsTest extends Specification {

    private static final GE_MAVEN_EXTENSION_COORDINATES = new MavenCoordinates("com.gradle", "gradle-enterprise-maven-extension", "0.1.0")
    private static final CCUD_MAVEN_EXTENSION_COORDINATES = new MavenCoordinates("com.gradle", "common-custom-user-data-maven-extension", "0.2.0")

    @TempDir
    File extensionsDir
    File extensionsXml

    def setup() {
        extensionsXml = new File(extensionsDir, "extensions.xml")
    }

    def 'hasExtension returns false when MavenExtensions is created via empty'() {
        when:
        def mavenExtensions = MavenExtensions.empty()

        then:
        assert !mavenExtensions.hasExtension(GE_MAVEN_EXTENSION_COORDINATES)
        assert !mavenExtensions.hasExtension(CCUD_MAVEN_EXTENSION_COORDINATES)
    }

    def 'hasExtension returns false when extensions xml file is not valid xml'() {
        given:
        extensionsXml << ""

        when:
        def mavenExtensions = MavenExtensions.fromFile(extensionsXml)

        then:
        assert !mavenExtensions.hasExtension(GE_MAVEN_EXTENSION_COORDINATES)
        assert !mavenExtensions.hasExtension(CCUD_MAVEN_EXTENSION_COORDINATES)
    }

    def 'hasExtension returns false when extensions xml file is not present'() {
        given:
        extensionsXml.delete()

        when:
        def mavenExtensions = MavenExtensions.fromFile(extensionsXml)

        then:
        assert !mavenExtensions.hasExtension(GE_MAVEN_EXTENSION_COORDINATES)
        assert !mavenExtensions.hasExtension(CCUD_MAVEN_EXTENSION_COORDINATES)
    }

    def 'hasExtension returns true when extensions xml contains extensions'() {
        given:
        extensionsXml << generateExtensionsXml(GE_MAVEN_EXTENSION_COORDINATES, CCUD_MAVEN_EXTENSION_COORDINATES)

        when:
        def mavenExtensions = MavenExtensions.fromFile(extensionsXml)

        then:
        assert mavenExtensions.hasExtension(GE_MAVEN_EXTENSION_COORDINATES)
        assert mavenExtensions.hasExtension(CCUD_MAVEN_EXTENSION_COORDINATES)
    }

    def 'hasExtension returns false when extension argument is null'() {
        given:
        MavenCoordinates nullCoordinates = null
        extensionsXml << generateExtensionsXml(GE_MAVEN_EXTENSION_COORDINATES, CCUD_MAVEN_EXTENSION_COORDINATES)

        when:
        def mavenExtensions = MavenExtensions.fromFile(extensionsXml)

        then:
        assert !mavenExtensions.hasExtension(nullCoordinates)
    }

    def 'hasExtension returns false when Document is null'() {
        given:
        Document document = null

        when:
        def mavenExtensions = new MavenExtensions(document)

        then:
        assert !mavenExtensions.hasExtension(GE_MAVEN_EXTENSION_COORDINATES)
        assert !mavenExtensions.hasExtension(CCUD_MAVEN_EXTENSION_COORDINATES)
    }

    def 'hasExtension returns false when extension has invalid characters for xpath expression'() {
        given:
        def extensionWithInvalidCharacters = new MavenCoordinates("com.example", "\'", "0.1.0")
        extensionsXml << generateExtensionsXml(GE_MAVEN_EXTENSION_COORDINATES, CCUD_MAVEN_EXTENSION_COORDINATES)

        when:
        def mavenExtensions = MavenExtensions.fromFile(extensionsXml)
        mavenExtensions.hasExtension(extensionWithInvalidCharacters)

        then:
        assert !mavenExtensions.hasExtension(extensionWithInvalidCharacters)
    }

    def generateExtensionsXml(MavenCoordinates... extensions) {
        """<?xml version="1.0" encoding="UTF-8"?>
            <extensions>
                ${ extensions.collect {
                    extension ->
                        """<extension>
                            <groupId>${extension.groupId}</groupId>
                            <artifactId>${extension.artifactId}</artifactId>
                            <version>${extension.version}</version>
                        </extension>"""
                }.join("\n") }
            </extensions>"""
    }
}
