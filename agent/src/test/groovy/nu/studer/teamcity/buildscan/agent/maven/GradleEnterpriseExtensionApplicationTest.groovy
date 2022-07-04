package nu.studer.teamcity.buildscan.agent.maven

import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.util.EventDispatcher
import nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector
import nu.studer.teamcity.buildscan.agent.ExtensionApplicationListener
import nu.studer.teamcity.buildscan.agent.TestBuildRunnerContext
import spock.lang.Specification
import spock.lang.TempDir

import static org.junit.Assume.assumeTrue

class GradleEnterpriseExtensionApplicationTest extends Specification {

    static final List<JdkCompatibleMavenVersion> SUPPORTED_MAVEN_VERSIONS = [
        new JdkCompatibleMavenVersion('3.5.0', 7, 11),
        new JdkCompatibleMavenVersion('3.5.4', 7, 11),
        new JdkCompatibleMavenVersion('3.6.0', 7, 11),
        new JdkCompatibleMavenVersion('3.6.3', 7, 11),
        new JdkCompatibleMavenVersion('3.8.1', 7, 11),
        new JdkCompatibleMavenVersion('3.8.6', 7, 11)
    ]

    static final String GE_URL = System.getenv('GRADLE_ENTERPRISE_TEST_INSTANCE') ?: null
    static final String GE_EXTENSION_VERSION = '1.14.3'
    static final String CCUD_EXTENSION_VERSION = '1.10.1'

    @TempDir
    File checkoutDir

    @TempDir
    File agentTempDir

    @TempDir
    File agentMavenInstallation

    Map<String, String> configParameters
    Map<String, String> runnerParameters

    BuildRunnerContext context
    ExtensionApplicationListener extensionApplicationListener
    BuildScanServiceMessageInjector injector

    Project.Configuration projectConfiguration
    MavenBuildStepConfiguration teamCityConfiguration

    void setup() {
        configParameters = [:]
        runnerParameters = [
            'teamcity.build.checkoutDir': checkoutDir.absolutePath,
            'teamcity.build.workingDir' : checkoutDir.absolutePath,
        ]

        context = new TestBuildRunnerContext("Maven2", agentTempDir, configParameters, runnerParameters)
        extensionApplicationListener = Mock(ExtensionApplicationListener)
        injector = new BuildScanServiceMessageInjector(EventDispatcher.create(AgentLifeCycleListener.class), extensionApplicationListener)

        projectConfiguration = new Project.Configuration()
        teamCityConfiguration = new MavenBuildStepConfiguration()
    }

    def "does not apply GE / CCUD extensions when not defined in project and not requested via TC config (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project where pom location set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePath(checkoutDir, project.pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via project when defined in project where pom location set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePath(checkoutDir, project.pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project where checkout dir and working dir not set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        runnerParameters.remove('teamcity.build.checkoutDir')
        runnerParameters.remove('teamcity.build.workingDir')

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not inject GE extension when not defined in project but matching custom coordinates defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            // using Guava as surrogate since we do not have a custom extension at hand that pulls in the GE Maven extension transitively
            customExtension = new GroupArtifactVersion(group: 'com.google.guava', artifact: 'guava', version: '31.1-jre')
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            geExtensionCustomCoordinates = 'com.google.guava:guava'
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension not defined in project and not applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not inject CCUD extension when not defined in project but matching custom coordinates defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            // using Guava as surrogate since we do not have a custom extension at hand that pulls in the GE Maven extension transitively
            customExtension = new GroupArtifactVersion(group: 'com.google.guava', artifact: 'guava', version: '31.1-jre')
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
            ccudExtensionCustomCoordinates = 'com.google.guava:guava'
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "ignores GE URL requested via TC config when GE extension is not applied via the classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = 'https://ge-server.invalid'
            allowUntrustedServer = true
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "configures GE URL requested via TC config when GE extension is applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = 'https://ge-server.invalid'
            geExtensionVersion = null
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            allowUntrustedServer = true
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not publish build scan for TeamCity specific info goal invocation (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            goals = 'org.jetbrains.maven:info-maven3-plugin:1.0.2:info'
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "build succeeds when service message maven extension is applied to a project without GE in the extension classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            commandLineBuildStepEnabled = true
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and extensions.xml is in project root directory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pomDirName = 'subdir'
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePath(checkoutDir, project.pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and subdirectory is specified as pom path and extensions.xml is in project root directory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pomDirName = 'subdir'
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePath(checkoutDir, project.pom.parentFile)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and extensions.xml is in a higher subdirectory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = projectConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pomDirName = 'subdir1/subdir2'
            dotMvnParentDirName = 'subdir1'
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePath(checkoutDir, project.pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    String run(String mavenVersion, Project project) {
        injector.beforeRunnerStart(context)

        def runner = new MavenRunner(
            version: mavenVersion,
            projectDir: new File(runnerParameters.get('teamcity.build.workingDir') ?: checkoutDir.absolutePath),
            installationDir: agentMavenInstallation,
            multiModuleProjectDir: project.dotMvn.parentFile,
            arguments: ("${runnerParameters.get('goals')} ${runnerParameters.get('runnerArgs')}".toString().trim().split(/\s+/))
        )

        if (runnerParameters.containsKey('pomLocation')) {
            runner.arguments += ['-f', new File(runnerParameters.get('teamcity.build.checkoutDir'), runnerParameters.get('pomLocation')).absolutePath]
        }

        return runner.run()
    }

    void outputContainsTeamCityServiceMessageBuildStarted(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']"
        assert output.contains(serviceMsg)
        assert 1 == output.count(serviceMsg)
    }

    void outputMissesTeamCityServiceMessageBuildStarted(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']"
        assert !output.contains(serviceMsg)
    }

    void outputContainsTeamCityServiceMessageBuildScanUrl(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${GE_URL}/s/"
        assert output.contains(serviceMsg)
        assert 1 == output.count(serviceMsg)
    }

    void outputMissesTeamCityServiceMessageBuildScanUrl(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${GE_URL}/s/"
        assert !output.contains(serviceMsg)
    }

    void outputContainsBuildSuccess(String output) {
        assert output.contains("[INFO] BUILD SUCCESS")
    }

    static String getRelativePath(File parent, File child) {
        parent.toPath().relativize(child.toPath()).toString()
    }

    static final class JdkCompatibleMavenVersion {

        private final String mavenVersion
        private final Integer jdkMin
        private final Integer jdkMax

        JdkCompatibleMavenVersion(String mavenVersion, Integer jdkMin, Integer jdkMax) {
            this.mavenVersion = mavenVersion
            this.jdkMin = jdkMin
            this.jdkMax = jdkMax
        }

        boolean isJvmVersionCompatible() {
            def jvmVersion = getJvmVersion()
            jdkMin <= jvmVersion && jvmVersion <= jdkMax
        }

        private static int getJvmVersion() {
            String version = System.getProperty('java.version')
            if (version.startsWith('1.')) {
                Integer.parseInt(version.substring(2, 3))
            } else {
                Integer.parseInt(version.substring(0, version.indexOf('.')))
            }
        }

        @Override
        String toString() {
            return "JdkCompatibleMavenVersion{" +
                "Maven " + mavenVersion +
                ", JDK " + jdkMin + "-" + jdkMax +
                '}'
        }

    }

}
