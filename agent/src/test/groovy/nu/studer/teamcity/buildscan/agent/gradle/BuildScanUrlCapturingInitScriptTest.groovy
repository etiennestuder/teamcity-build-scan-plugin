package nu.studer.teamcity.buildscan.agent.gradle

import static org.junit.Assume.assumeTrue

class BuildScanUrlCapturingInitScriptTest extends BaseInitScriptTest {

    def "sends build started service message even without declaring Develocity / Build Scan plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "send build scan url service message when declaring Develocity / Build Scan plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareDevelocityPluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

}
