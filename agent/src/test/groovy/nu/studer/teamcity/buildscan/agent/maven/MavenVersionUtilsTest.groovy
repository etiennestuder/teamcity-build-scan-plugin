package nu.studer.teamcity.buildscan.agent.maven

import groovy.text.SimpleTemplateEngine
import spock.lang.Specification

import static nu.studer.teamcity.buildscan.agent.MavenVersionUtils.*

class MavenVersionUtilsTest extends Specification {

    private static final def MVN_VERSION_OUTPUT_TEMPLATE = """
Apache Maven \$version (84538c9988a25aec085021c365c560670ad80f63)
Maven home: /opt/homebrew/Cellar/maven/\$version/libexec
Java version: 1.8.0_312, vendor: BellSoft, runtime: /Users/user/.asdf/installs/java/liberica-8u312+7/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "12.4", arch: "aarch64", family: "mac"

"""

    def "parseVersion returns null when no match is found"() {
        given:
        def output = ""

        when:
        def parsedVersion = parseVersion(output)

        then:
        assert !parsedVersion
    }

    def "parseVersion correctly parses full maven version from `mvn --version` output (#version)"() {
        given:
        def output = new SimpleTemplateEngine().createTemplate(MVN_VERSION_OUTPUT_TEMPLATE).make(version: version).toString()

        when:
        def parsedVersion = parseVersion(output)

        then:
        assert parsedVersion == version

        where:
        // sampling of actual maven versions that are distinct from a parsing perspective
        version << [
            "1.0-RC2",
            "1.1-RC-1",
            "2.1.0-M1",
            "3.0",
            "3.0-beta-3",
            "3.3.1",
        ]
    }

    def "isVersionAtLeast correctly evaluates version requirements when required version is #versionRequirement and version is #version" () {
        when:
        def versionMeetsRequirement = isVersionAtLeast(version, versionRequirement)

        then:
        assert versionMeetsRequirement == expected

        where:
        versionRequirement | version         | expected
        "3.3.1"            | "2.0"           | false
        "3.3.1"            | "2.2.1"         | false
        "3.3.1"            | "3.0"           | false
        "3.3.1"            | "3.1.0-alpha-1" | false
        "3.3.1"            | "3.0.5"         | false
        "3.3.1"            | "3.3.0"         | false
        "3.3.1"            | "3.3.1"         | true
        "3.3.1"            | "3.5.0-beta-1"  | true
        "3.3.1"            | "3.8.6"         | true
        "3.3.1"            | "3.8.6"         | true
        "3.3.1"            | "4.0"           | true
        "3.3.1"            | "4.0.0"         | true
    }

    def "isVersionAtLeast returns false when version is null" () {
        when:
        def versionMeetsRequirement = isVersionAtLeast(null, "3.3.1")

        then:
        assert !versionMeetsRequirement
    }

    def "isVersionAtLeast returns false when versionRequirement is null" () {
        when:
        def versionMeetsRequirement = isVersionAtLeast("3.3.1", null)

        then:
        assert !versionMeetsRequirement
    }

}
