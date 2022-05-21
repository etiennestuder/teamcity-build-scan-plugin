package nu.studer.teamcity.buildscan.agent

import static org.junit.Assume.assumeTrue

class BuildScanInitScriptTest extends BaseInitScriptTest {

    def "does not fail build when using a Gradle version older than minimum version 4.1 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        result

        where:
        jdkCompatibleGradleVersion << NOT_SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build started service message even without declaring Gradle Enterprise / Build Scan plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "send build scan url service message when declaring Gradle Enterprise / Build Scan plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

}
