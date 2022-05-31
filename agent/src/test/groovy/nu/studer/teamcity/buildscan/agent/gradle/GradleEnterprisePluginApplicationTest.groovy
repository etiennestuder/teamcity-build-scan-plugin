package nu.studer.teamcity.buildscan.agent.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

import static org.junit.Assume.assumeTrue

class GradleEnterprisePluginApplicationTest extends BaseInitScriptTest {

    private static final String GE_PLUGIN_VERSION = '3.10.1'
    private static final String CCUD_PLUGIN_VERSION = '1.7'

    private static final GradleVersion GRADLE_6 = GradleVersion.version('6.0')

    def "does not apply GE / CCUD plugins when not defined in project and not requested via TC config (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig()
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "applies GE plugin via init script when not defined in project, using sys props configuration (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "applies GE plugin via init script when not defined in project, using env vars configuration (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [], gePluginConfig.toEnvVars())

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_5_AND_HIGHER
    }

    def "applies GE plugin via project when defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "applies CCUD plugin via init script when not defined in project where GE plugin not defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "applies CCUD plugin via init script when not defined in project where GE plugin defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "applies CCUD plugin via project when defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginAndCcudPluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "ignores GE URL requested via TC config when GE plugin is not applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: URI.create('https://ge-server.invalid'), gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "configures GE URL requested via TC config when GE plugin is applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "stops gracefully when CCUD plugin version injected via init script is <1.7 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: '1.6.6')
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)
        result.output.contains('Common Custom User Data Gradle plugin must be at least 1.7. Configured version is 1.6.6.')

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    void outputContainsGePluginApplicationViaInitScript(BuildResult result, GradleVersion gradleVersion) {
        def pluginApplicationLogMsgGradle4And5 = "Applying com.gradle.scan.plugin.BuildScanPlugin via init script"
        def pluginApplicationLogMsgGradle6AndHigher = "Applying com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin via init script"
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

    void outputMissesGePluginApplicationViaInitScript(BuildResult result) {
        def pluginApplicationLogMsgGradle4And5 = "Applying com.gradle.scan.plugin.BuildScanPlugin via init script"
        def pluginApplicationLogMsgGradle6AndHigher = "Applying com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin via init script"
        assert !result.output.contains(pluginApplicationLogMsgGradle4And5)
        assert !result.output.contains(pluginApplicationLogMsgGradle6AndHigher)
    }

    void outputContainsCcudPluginApplicationViaInitScript(BuildResult result) {
        def pluginApplicationLogMsg = "Applying com.gradle.CommonCustomUserDataGradlePlugin via init script"
        assert result.output.contains(pluginApplicationLogMsg)
        assert 1 == result.output.count(pluginApplicationLogMsg)
    }

    void outputMissesCcudPluginApplicationViaInitScript(BuildResult result) {
        def pluginApplicationLogMsg = "Applying com.gradle.CommonCustomUserDataGradlePlugin via init script"
        assert !result.output.contains(pluginApplicationLogMsg)
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

        List<String> toSysProps() {
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

        Map<String, String> toEnvVars() {
            Map<String, String> envVars = [:]
            if (geUrl) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_URL', geUrl.toString()
            }
            if (gePluginVersion) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_PLUGIN_VERSION', gePluginVersion
            }
            if (ccudPluginVersion) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_CCUD_PLUGIN_VERSION', ccudPluginVersion
            }
            envVars
        }

    }

}
