package nu.studer.teamcity.buildscan.agent

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import spock.lang.Ignore

import static org.junit.Assume.assumeTrue

class AutoApplicationTest extends BaseInitScriptTest {

    private static final String GE_PLUGIN_VERSION = '3.10.1'
    private static final String CCUD_PLUGIN_VERSION = '1.7'

    private static final GradleVersion GRADLE_6 = GradleVersion.version('6.0')

    def "sends build scan url service message when GE plugin is applied by init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when GE plugin is applied by project and init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when GE and CCUD plugins are applied by init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when GE and CCUD plugins are applied by project and init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    @Ignore(value = "This behavior is desired but not yet implemented")
    def "degrades gracefully project applies GE and init script applies CCUD <= 1.6.5 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: '1.6.5')
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "build succeeds when URL is set without applied GE plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        result

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "url system property overrides project server url (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: URI.create('https://ge-server.invalid'))
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan to scans.gradle.com if no URL is given (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toJvmArgs())

        then:
        outputContainsTermsOfServiceDenial(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    void outputContainsGePluginApplicationViaInitScript(BuildResult result, GradleVersion gradleVersion) {
        def pluginApplicationLogMsgGradle4And5 = 'Applying com.gradle.scan.plugin.BuildScanPlugin via init script'
        def pluginApplicationLogMsgGradle6AndHigher = 'Applying com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin via init script'
        if (gradleVersion < GRADLE_6) {
            assert result.output.contains(pluginApplicationLogMsgGradle4And5)
            assert 1 == result.output.count(pluginApplicationLogMsgGradle4And5)
            assert !result.output.contains(pluginApplicationLogMsgGradle6AndHigher)
        } else {
            assert result.output.contains(pluginApplicationLogMsgGradle6AndHigher)
            assert 1 == result.output.count(pluginApplicationLogMsgGradle6AndHigher)
            assert !result.output.contains(pluginApplicationLogMsgGradle4And5)
        }
    }

    void outputContainsTermsOfServiceDenial(BuildResult result) {
        def tosWarning = 'The Gradle Terms of Service have not been agreed to.'
        assert result.output.contains(tosWarning)
        assert 1 == result.output.count(tosWarning)
    }

    static final class TcPluginConfig {

        URI geUrl
        String gePluginVersion
        String ccudPluginVersion

        List<String> toJvmArgs() {
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

    }

}
