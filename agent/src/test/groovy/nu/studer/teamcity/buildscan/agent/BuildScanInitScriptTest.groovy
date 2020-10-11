package nu.studer.teamcity.buildscan.agent

import org.gradle.util.GradleVersion

class BuildScanInitScriptTest extends BaseInitScriptTest {

    private static final List<GradleVersion> NOT_SUPPORTED_GRADLE_VERSIONS = [
        GradleVersion.version('3.5.1'),
        GradleVersion.version('4.0.2')
    ]

    private static final List<GradleVersion> SUPPORTED_GRADLE_VERSIONS = [
        GradleVersion.version('4.1'),
        GradleVersion.version('4.10.3'),
        GradleVersion.version('5.1.1'),
        GradleVersion.version('5.6.4'),
        GradleVersion.version('6.0.1'),
        GradleVersion.current()
    ]

    def "does not fail build when using a Gradle version older than minimum version 4.1 (#gradleVersion)"() {
        when:
        def result = run(gradleVersion)

        then:
        result

        where:
        gradleVersion << NOT_SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build started service message even without declaring Build Scan / Gradle Enterprise plugin (#gradleVersion)"() {
        when:
        def result = run(gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        gradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "send build scan url service message when declaring Build Scan / Gradle Enterprise plugin (#gradleVersion)"() {
        given:
        settingsFile << maybeAddGradleEnterprisePlugin(gradleVersion)
        buildFile << maybeAddBuildScanPlugin(gradleVersion)

        when:
        def result = run(gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        gradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

}
