package nu.studer.teamcity.buildscan.agent

import org.gradle.util.GradleVersion

import static org.junit.Assume.assumeTrue

class BuildScanInitScriptTest extends BaseInitScriptTest {

    private static final List<JdkCompatibleGradleVersion> NOT_SUPPORTED_GRADLE_VERSIONS = [
        new JdkCompatibleGradleVersion(GradleVersion.version('3.5.1'), 7, 9),
        new JdkCompatibleGradleVersion(GradleVersion.version('4.0.2'), 7, 9)
    ]

    private static final List<JdkCompatibleGradleVersion> SUPPORTED_GRADLE_VERSIONS = [
        new JdkCompatibleGradleVersion(GradleVersion.version('4.1'), 7, 9),
        new JdkCompatibleGradleVersion(GradleVersion.version('4.10.3'), 7, 10),
        new JdkCompatibleGradleVersion(GradleVersion.version('5.1.1'), 8, 11),
        new JdkCompatibleGradleVersion(GradleVersion.version('5.6.4'), 8, 12),
        new JdkCompatibleGradleVersion(GradleVersion.version('6.0.1'), 8, 13),
        new JdkCompatibleGradleVersion(GradleVersion.version('6.7'), 8, 15),
        new JdkCompatibleGradleVersion(GradleVersion.version('7.0.2'), 8, 16),
        new JdkCompatibleGradleVersion(GradleVersion.version('7.4.2'), 8, 17),
    ]

    def "does not fail build when using a Gradle version older than minimum version 4.1 (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        result

        where:
        jdkCompatibleGradleVersion << NOT_SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build started service message even without declaring Build Scan / Gradle Enterprise plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ""
        buildFile << ""

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "send build scan url service message when declaring Build Scan / Gradle Enterprise plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion)
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion)

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when auto applying Build Scan / Gradle Enterprise plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
                '-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=3.10',
                '-DteamCityBuildScanPlugin.ccud.plugin.version=1.6.5',
        ])

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
                '-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=3.10',
                '-DteamCityBuildScanPlugin.ccud.plugin.version=1.6.5',
        ])

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
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
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS.findAll {
            it.gradleVersion >= GradleVersion.version('6.0')
        }
    }

    def "url system property does not override gradle enterprise configuration (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion)
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=https://ge-server.invalid/".toString(),
        ])

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS.findAll {
            it.gradleVersion < GradleVersion.version('6.0')
        }
    }

    static final class JdkCompatibleGradleVersion {

        private final GradleVersion gradleVersion
        private final Integer jdkMin
        private final Integer jdkMax

        JdkCompatibleGradleVersion(GradleVersion gradleVersion, Integer jdkMin, Integer jdkMax) {
            this.gradleVersion = gradleVersion
            this.jdkMin = jdkMin
            this.jdkMax = jdkMax
        }

        boolean isJvmVersionCompatible() {
            def jvmVersion = getJvmVersion()
            jdkMin <= jvmVersion && jvmVersion <= jdkMax
        }

        private static int getJvmVersion() {
            String version = System.getProperty('java.version');
            if (version.startsWith('1.')) {
                Integer.parseInt(version.substring(2, 3))
            } else {
                Integer.parseInt(version.substring(0, version.indexOf('.')))
            }
        }

        @Override
        String toString() {
            return "JdkCompatibleGradleVersion{" +
                "Gradle " + gradleVersion.version +
                ", JDK " + jdkMin + "-" + jdkMax +
                '}';
        }

    }

}
