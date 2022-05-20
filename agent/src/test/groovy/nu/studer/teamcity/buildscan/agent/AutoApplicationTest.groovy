package nu.studer.teamcity.buildscan.agent

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import spock.lang.Ignore

import static org.junit.Assume.assumeTrue

class AutoApplicationTest extends BaseInitScriptTest {

    private static final String GE_VERSION = '3.10.1'
    private static final String CCUD_VERSION = '1.7'

    def "sends build scan url service message when GE plugin is applied by init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def jvmArgs = generateJvmArgs(mockScansServer.address, GE_VERSION, null)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when GE plugin is applied by project and init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion)
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def jvmArgs = generateJvmArgs(mockScansServer.address, GE_VERSION, null)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when GE and CCUD plugins are applied by init script (#jdkCompatibleGradleVersion)"() {
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

    def "sends build scan url service message when GE and CCUD plugins are applied by project init script (#jdkCompatibleGradleVersion)"() {
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

    @Ignore(value = "This behavior is desired but not yet implemented")
    def "degrades gracefully project applies GE and init script applies CCUD <= 1.6.5 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion)
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def jvmArgs = generateJvmArgs(mockScansServer.address, GE_VERSION, '1.6.5')
        def result = run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "build succeeds when URL is set without applied GE plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def jvmArgs = generateJvmArgs(mockScansServer.address, null, null)

        then:
        run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "build doesn't apply CCUD is applied without GE plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def jvmArgs = generateJvmArgs(mockScansServer.address, null, CCUD_VERSION)

        then:
        if (jdkCompatibleGradleVersion.gradleVersion < GradleVersion.version('5.2')) {
            run(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)
        } else {
            def result = runAndFail(jdkCompatibleGradleVersion.gradleVersion, jvmArgs)
            result.output.contains("Could not create plugin of type 'CommonCustomUserDataGradlePlugin'.")
        }

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "url system property overrides project server url (#jdkCompatibleGradleVersion)"() {
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

    def "sends build scan to scans.gradle.com if no URL is given (#jdkCompatibleGradleVersion)"() {
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
