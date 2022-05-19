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
        def jvmArgs = generateJvmArgs(mockScansServer.address, GE_VERSION, CCUD_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

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
        def jvmArgs = generateJvmArgs(mockScansServer.address, GE_VERSION, CCUD_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

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
        def jvmArgs = generateJvmArgs(mockScansServer.address, null, null)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

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
        def jvmArgs = generateJvmArgs(null, GE_VERSION, null)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

        then:
        outputContainsTermsOfServiceDenial(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    ArrayList<String> generateJvmArgs(URI geUrl, String gePluginVersion, String ccudPluginVersion) {
        def jvmArgs = []

        if (geUrl) {
            jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.url=$geUrl".toString()
        }

        if (gePluginVersion) {
            jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=$gePluginVersion".toString()
        }

        if (ccudPluginVersion) {
            jvmArgs << "-DteamCityBuildScanPlugin.ccud.plugin.version=$ccudPluginVersion".toString()
        }

        jvmArgs
    }

    void outputContainsTermsOfServiceDenial(BuildResult result) {
        assert 1 == result.output.count('The Gradle Terms of Service have not been agreed to.')
    }
}
