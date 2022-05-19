package nu.studer.teamcity.buildscan.agent

import org.gradle.testkit.runner.BuildResult

import static org.junit.Assume.assumeTrue

class AutoApplicationTest extends BaseInitScriptTest {

    private static final String GE_VERSION = '3.10'
    private static final String CCUD_VERSION = '1.6.6'

    def "sends build scan url service message when auto applying Build Scan / Gradle Enterprise plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
                "-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=$GE_VERSION".toString(),
                "-DteamCityBuildScanPlugin.ccud.plugin.version=$CCUD_VERSION".toString(),
        ])

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when Build Scan / Gradle Enterprise plugin is configured and auto-applied (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion)
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
                "-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=$GE_VERSION".toString(),
                "-DteamCityBuildScanPlugin.ccud.plugin.version=$CCUD_VERSION".toString(),
        ])

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "url system property overrides gradle enterprise configuration (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion, 'https://ge-server.invalid/')
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion, 'https://ge-server.invalid/')

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
        ])

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan to scans.gradle.com no URL is given (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=$GE_VERSION".toString(),
                "-DteamCityBuildScanPlugin.ccud.plugin.version=$CCUD_VERSION".toString(),
        ])

        then:
        outputContainsTermsOfServiceDenial(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    void outputContainsTermsOfServiceDenial(BuildResult result) {
        assert 1 == result.output.count('The Gradle Terms of Service have not been agreed to.')
    }
}
