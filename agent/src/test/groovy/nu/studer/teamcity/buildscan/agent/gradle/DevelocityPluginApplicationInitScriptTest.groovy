package nu.studer.teamcity.buildscan.agent.gradle

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

import static org.junit.Assume.assumeTrue

class DevelocityPluginApplicationInitScriptTest extends BaseInitScriptTest {

    private static final GradleVersion GRADLE_5 = GradleVersion.version('5.0')

    def "does not apply Develocity / CCUD plugins when not defined in project and not requested via TC config (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig()
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "applies Develocity plugin via init script when not defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputContainsDevelocityPluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "applies Develocity plugin via project when defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareDevelocityPluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "applies CCUD plugin via init script when not defined in project where Develocity plugin not defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputContainsDevelocityPluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "applies CCUD plugin via init script when not defined in project where Develocity plugin defined in project (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareDevelocityPluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
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
        declareDevelocityPluginAndCcudPluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "ignores Develocity URL and allowUntrustedServer requested via TC config when Develocity plugin is not applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareDevelocityPluginApplication(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: URI.create('https://ge-server.invalid'), develocityAllowUntrustedServer: true, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "configures Develocity URL and allowUntrustedServer requested via TC config when Develocity plugin is applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityAllowUntrustedServer: true, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputContainsDevelocityPluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsDevelocityConnectionInfo(result, mockScansServer.address.toString(), true)
        outputMissesCcudPluginApplicationViaInitScript(result)
        outputContainsPluginRepositoryInfo(result, 'https://plugins.gradle.org/m2')

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "enforces Develocity URL and allowUntrustedServer in project if enforce url parameter is enabled (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        declareDevelocityPluginApplication(jdkCompatibleGradleVersion.gradleVersion, URI.create('https://ge-server.invalid'))

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityAllowUntrustedServer: true, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, develocityEnforceUrl: true)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputEnforcesDevelocityUrl(result, mockScansServer.address.toString(), true)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "can configure alternative repository for plugins when Develocity plugin is applied by the init script (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(gradlePluginRepositoryUrl: new URI('https://plugins.grdev.net/m2'), develocityUrl: mockScansServer.address, develocityAllowUntrustedServer: false, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION)
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputContainsDevelocityPluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsDevelocityConnectionInfo(result, mockScansServer.address.toString(), false)
        outputMissesCcudPluginApplicationViaInitScript(result)
        outputContainsPluginRepositoryInfo(result, 'https://plugins.grdev.net/m2')

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "stops gracefully when CCUD plugin version injected via init script is <1.7 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, ccudPluginVersion: '1.6.6')
        def result = run(jdkCompatibleGradleVersion.gradleVersion, develocityPluginConfig)

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)
        result.output.contains('Common Custom User Data Gradle plugin must be at least 1.7. Configured version is 1.6.6.')

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "can configure Develocity via CCUD system property overrides when CCUD plugin is inject via init script"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: URI.create('https://ge-server.invalid'), develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, ccudPluginVersion: CCUD_PLUGIN_VERSION)
        def result = run(new BuildConfig(
            gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
            tcPluginConfig: develocityPluginConfig,
            additionalJvmArgs: ["-Ddevelocity.url=$mockScansServer.address".toString()]))

        then:
        outputContainsDevelocityPluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputContainsCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_4_AND_HIGHER
    }

    def "does not apply Develocity plugin via init script for command-line runner without opt-in parameter (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, enableCommandLineRunner: false)
        def result = run(new BuildConfig(
            gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
            tcPluginConfig: develocityPluginConfig,
            runType: 'simpleRunner'))

        then:
        outputMissesDevelocityPluginApplicationViaInitScript(result)
        outputMissesCcudPluginApplicationViaInitScript(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "applies Develocity plugin via init script for command-line runner (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION, enableCommandLineRunner: true)
        def result = run(new BuildConfig(
            gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
            tcPluginConfig: develocityPluginConfig,
            runType: 'simpleRunner'))

        then:
        outputContainsDevelocityPluginApplicationViaInitScript(result, jdkCompatibleGradleVersion.gradleVersion)
        outputMissesCcudPluginApplicationViaInitScript(result)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << GRADLE_VERSIONS_3_0_AND_HIGHER
    }

    def "init script is configuration cache compatible (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def develocityPluginConfig = new TcPluginConfig(develocityUrl: mockScansServer.address, develocityPluginVersion: DEVELOCITY_PLUGIN_VERSION)
        def config = new BuildConfig(
                gradleVersion: jdkCompatibleGradleVersion.gradleVersion,
                tcPluginConfig: develocityPluginConfig,
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

    void outputContainsDevelocityPluginApplicationViaInitScript(BuildResult result, GradleVersion gradleVersion, String pluginVersion = DEVELOCITY_PLUGIN_VERSION) {
        def pluginApplicationLogMsgGradle4 = "Applying com.gradle.scan.plugin.BuildScanPlugin with version 1.16 via init script"
        def pluginApplicationLogMsgDevelocity = "Applying com.gradle.develocity.agent.gradle.DevelocityPlugin with version ${pluginVersion} via init script"
        if (gradleVersion < GRADLE_5) {
            assert 1 == result.output.count(pluginApplicationLogMsgGradle4)
            assert !result.output.contains(pluginApplicationLogMsgDevelocity)
        } else {
            assert 1 == result.output.count(pluginApplicationLogMsgDevelocity)
            assert !result.output.contains(pluginApplicationLogMsgGradle4)
        }
    }

    void outputMissesDevelocityPluginApplicationViaInitScript(BuildResult result) {
        def pluginApplicationLogMsgDevelocity = "Applying com.gradle.develocity.agent.gradle.DevelocityPlugin"
        assert !result.output.contains(pluginApplicationLogMsgDevelocity)
    }

    void outputContainsCcudPluginApplicationViaInitScript(BuildResult result, String ccudPluginVersion = CCUD_PLUGIN_VERSION) {
        def pluginApplicationLogMsg = "Applying com.gradle.CommonCustomUserDataGradlePlugin with version ${ccudPluginVersion} via init script"
        assert result.output.contains(pluginApplicationLogMsg)
        assert 1 == result.output.count(pluginApplicationLogMsg)
    }

    void outputMissesCcudPluginApplicationViaInitScript(BuildResult result) {
        def pluginApplicationLogMsg = "Applying com.gradle.CommonCustomUserDataGradlePlugin"
        assert !result.output.contains(pluginApplicationLogMsg)
    }

    void outputContainsDevelocityConnectionInfo(BuildResult result, String geUrl, boolean geAllowUntrustedServer) {
        def geConnectionInfo = "Connection to Develocity: $geUrl, allowUntrustedServer: $geAllowUntrustedServer"
        assert result.output.contains(geConnectionInfo)
        assert 1 == result.output.count(geConnectionInfo)
    }

    void outputContainsPluginRepositoryInfo(BuildResult result, String gradlePluginRepositoryUrl) {
        def repositoryInfo = "Develocity plugins resolution: ${gradlePluginRepositoryUrl}"
        assert result.output.contains(repositoryInfo)
        assert 1 == result.output.count(repositoryInfo)
    }

    void outputEnforcesDevelocityUrl(BuildResult result, String geUrl, boolean geAllowUntrustedServer) {
        def enforceUrl = "Enforcing Develocity: $geUrl, allowUntrustedServer: $geAllowUntrustedServer"
        assert result.output.contains(enforceUrl)
        assert 1 == result.output.count(enforceUrl)
    }

}
