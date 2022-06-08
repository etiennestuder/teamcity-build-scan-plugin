package nu.studer.teamcity.buildscan.agent.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

import static org.junit.Assume.assumeTrue

class GradleEnterprisePluginApplicationTest extends BaseInitScriptTest {

    private static final String GE_PLUGIN_VERSION = '3.10.1'
    private static final String CCUD_PLUGIN_VERSION = '1.7.2'

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
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "applies CCUD plugin via init script when not defined in project where GE plugin defined in project, using sys props configuration (#jdkCompatibleGradleVersion)"() {
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
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "applies CCUD plugin via init script when not defined in project where GE plugin defined in project, using env vars configuration (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [], gePluginConfig.toEnvVars())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "ignores GE URL and allowUntrustedServer requested via TC config when GE plugin is not applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: URI.create('https://ge-server.invalid'), geAllowUntrustedServer: true, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "configures GE URL and allowUntrustedServer requested via TC config when GE plugin is applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, geAllowUntrustedServer: true, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsGeConnectionInfo(result, mockScansServer.address.toString(), true)
        outputMissesCcudPluginApplicationViaInitScript(result)
        outputMissesCustomPluginRepositoryInfo(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "can configure alternative repository for plugins when GE plugin is applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(
            geUrl: mockScansServer.address,
            geAllowUntrustedServer: true,
            gePluginVersion: GE_PLUGIN_VERSION,
            gradlePluginRepositoryUrl: new URI('https://plugins.grdev.net/m2'))
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps())

        then:
        outputContainsCustomPluginRepositoryInfo(result, 'https://plugins.grdev.net/m2')
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)

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

    def "can configure GE via CCUD system property overrides when CCUD plugin is inject via init script"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: URI.create('https://ge-server.invalid'), gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig.toSysProps() + ["-Dgradle.enterprise.url=$mockScansServer.address".toString()])

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
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

    void outputContainsGeConnectionInfo(BuildResult result, String geUrl, boolean geAllowUntrustedServer) {
        def geConnectionInfo = "Connection to Gradle Enterprise: $geUrl, allowUntrustedServer: $geAllowUntrustedServer"
        assert result.output.contains(geConnectionInfo)
        assert 1 == result.output.count(geConnectionInfo)
    }

    void outputContainsCustomPluginRepositoryInfo(BuildResult result, String repositoryUrl) {
        def repositoryInfo = "Resolving Gradle Enterprise plugins from ${repositoryUrl}"
        assert result.output.contains(repositoryInfo)
    }

    void outputMissesCustomPluginRepositoryInfo(BuildResult result) {
        def repositoryInfo = "Resolving Gradle Enterprise plugins from"
        assert !result.output.contains(repositoryInfo)
    }

    static final class TcPluginConfig {

        URI geUrl
        boolean geAllowUntrustedServer
        String gePluginVersion
        String ccudPluginVersion
        URI gradlePluginRepositoryUrl

        List<String> toSysProps() {
            def jvmArgs = []
            if (geUrl) {
                jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.url=$geUrl".toString()
            }
            if (geAllowUntrustedServer) {
                jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.allow-untrusted-server=true"
            }
            if (gePluginVersion) {
                jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=$gePluginVersion".toString()
            }
            if (ccudPluginVersion) {
                jvmArgs << "-DteamCityBuildScanPlugin.ccud.plugin.version=$ccudPluginVersion".toString()
            }
            if (gradlePluginRepositoryUrl) {
                jvmArgs << "-DteamCityBuildScanPlugin.gradle.plugin-repository.url=$gradlePluginRepositoryUrl".toString()
            }
            jvmArgs
        }

        Map<String, String> toEnvVars() {
            Map<String, String> envVars = [:]
            if (geUrl) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_URL', geUrl.toString()
            }
            if (geAllowUntrustedServer) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_ALLOW_UNTRUSTED_SERVER', 'true'
            }
            if (gePluginVersion) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_GRADLE_ENTERPRISE_PLUGIN_VERSION', gePluginVersion
            }
            if (ccudPluginVersion) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_CCUD_PLUGIN_VERSION', ccudPluginVersion
            }
            if (gradlePluginRepositoryUrl) {
                envVars.put 'TEAMCITYBUILDSCANPLUGIN_GRADLE_PLUGIN_REPOSITORY_URL', gradlePluginRepositoryUrl.toString()
            }
            envVars
        }

    }

}
