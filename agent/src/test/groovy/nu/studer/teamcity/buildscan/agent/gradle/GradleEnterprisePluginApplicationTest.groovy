package nu.studer.teamcity.buildscan.agent.gradle

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

import static org.junit.Assume.assumeTrue

class GradleEnterprisePluginApplicationTest extends BaseInitScriptTest {

    private static final String GE_PLUGIN_VERSION = '3.12.3'
    private static final String CCUD_PLUGIN_VERSION = '1.8.2'

    private static final GradleVersion GRADLE_6 = GradleVersion.version('6.0')

    def "does not apply GE / CCUD plugins when not defined in project and not requested via TC config (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig()
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "applies GE plugin via init script when not defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "applies GE plugin via project when defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareGePluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

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
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

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
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

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
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

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
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsGeConnectionInfo(result, mockScansServer.address.toString(), true)
        outputMissesCcudPluginApplicationViaInitScript(result)
        outputContainsPluginRepositoryInfo(result, 'https://plugins.gradle.org/m2')

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "can configure alternative repository for plugins when GE plugin is applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(gradlePluginRepositoryUrl: new URI('https://plugins.grdev.net/m2'), geUrl: mockScansServer.address, geAllowUntrustedServer: false, gePluginVersion: GE_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsGeConnectionInfo(result, mockScansServer.address.toString(), false)
        outputMissesCcudPluginApplicationViaInitScript(result)
        outputContainsPluginRepositoryInfo(result, 'https://plugins.grdev.net/m2')

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "stops gracefully when CCUD plugin version injected via init script is <1.7 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, ccudPluginVersion: '1.6.6')
        def result = run(jdkCompatibleGradleVersion.gradleVersion, gePluginConfig)

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
        def result = run(new BuildConfig(
            gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
            tcPluginConfig: gePluginConfig,
            additionalJvmArgs: ["-Dgradle.enterprise.url=$mockScansServer.address".toString()]))

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "does not apply GE plugin via init script for command-line runner without opt-in parameter (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, enableCommandLineRunner: false)
        def result = run(new BuildConfig(
            gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
            tcPluginConfig: gePluginConfig,
            runType: 'simpleRunner'))

        then:
        outputMissesGePluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "applies GE plugin via init script for command-line runner (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION, enableCommandLineRunner: true)
        def result = run(new BuildConfig(
            gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
            tcPluginConfig: gePluginConfig,
            runType: 'simpleRunner'))

        then:
        outputContainsGePluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_2_AND_HIGHER
    }

    def "init script is configuration cache compatible (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def gePluginConfig = new TcPluginConfig(geUrl: mockScansServer.address, gePluginVersion: GE_PLUGIN_VERSION)
        def config = new BuildConfig(
                gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
                tcPluginConfig: gePluginConfig,
                additionalJvmArgs: ["-Dorg.gradle.unsafe.configuration-cache=true"])
        def result = run(config)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        when:
        result = run(config)

        then:
        // does not send build started message when build is loaded from configuration cache
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        // scoped to 7.2+ due to https://github.com/gradle/gradle/issues/17340
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_CONFIGURATION_CACHE_COMPATIBLE.findAll { it.gradleVersion >= GradleVersion.version('7.2') }
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

    void outputContainsPluginRepositoryInfo(BuildResult result, String gradlePluginRepositoryUrl) {
        def repositoryInfo = "Gradle Enterprise plugins resolution: ${gradlePluginRepositoryUrl}"
        assert result.output.contains(repositoryInfo)
        assert 1 == result.output.count(repositoryInfo)
    }

}
