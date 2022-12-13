package nu.studer.teamcity.buildscan.agent.gradle

import static org.junit.Assume.assumeTrue

class BuildScanUrlCapturingTest extends BaseInitScriptTest {

    def "send build scan url service message when declaring Gradle Enterprise / Build Scan plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

}
