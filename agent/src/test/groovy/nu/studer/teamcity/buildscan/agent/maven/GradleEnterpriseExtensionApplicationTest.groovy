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

    ProjectConfiguration projectConfiguration
    TeamCityConfiguration teamCityConfiguration

    void setup() {
        configParameters = new HashMap<String, String>()
        runnerParameters = [
            'teamcity.build.checkoutDir': checkoutDir.absolutePath,
            'teamcity.build.workingDir':  checkoutDir.absolutePath,
        ]

        context = new TestBuildRunnerContext("Maven2", agentTempDir, configParameters, runnerParameters)
        extensionApplicationListener = Mock(ExtensionApplicationListener)
        injector = new BuildScanServiceMessageInjector(EventDispatcher.create(AgentLifeCycleListener.class), extensionApplicationListener)

        projectConfiguration = new ProjectConfiguration()
        teamCityConfiguration = new TeamCityConfiguration()
    }

    def "does not apply GE / CCUD extensions when not defined in project and not requested via TC config (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        def pom = projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePomPath(checkoutDir, pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        File pom = projectConfiguration.with(true){
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePomPath(checkoutDir, pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.buildIn(checkoutDir)

        and:
        runnerParameters.remove('teamcity.build.checkoutDir')
        runnerParameters.remove('teamcity.build.workingDir')

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
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
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
            geExtensionCustomCoordinates = 'com.google.guava:guava'
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            ccudExtensionVersion = CCUD_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
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
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
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
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
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
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.with(true){
            geUrl = 'https://ge-server.invalid'
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            allowUntrustedServer = true
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            goals = 'org.jetbrains.maven:info-maven3-plugin:1.0.2:info'
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        projectConfiguration.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            commandLineBuildStepEnabled = true
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

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
        File pom = projectConfiguration.with (true) {
            geExtensionVersion = '1.14.2'
            pomDir = 'subdir'
        }.buildIn(checkoutDir)

        and:
        teamCityConfiguration.with(true) {
            geUrl = GE_URL
            geExtensionVersion = GE_EXTENSION_VERSION
            pathToPomFile = getRelativePomPath(checkoutDir, pom)
        }.applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, runnerParameters)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    String run(String mavenVersion, Map<String, String> runnerParameters) {
        injector.beforeRunnerStart(context)

        def runner = new MavenRunner()
            .withVersion(mavenVersion)
            .withArguments("${runnerParameters.get('goals')} ${runnerParameters.get('runnerArgs')}".toString().trim().split(/\s+/))
            .withProjectDir(runnerParameters.get('teamcity.build.workingDir') ?: checkoutDir.absolutePath)
            .withInstallationDirectory(agentMavenInstallation)

        if (runnerParameters.containsKey('pomLocation')) {
            runner.withArguments(runner.arguments + ['-f', new File(runnerParameters.get('teamcity.build.checkoutDir'), runnerParameters.get('pomLocation')).absolutePath])
        }

        return runner.build()
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

    static final class GroupArtifactVersion {

        String group
        String artifact
        String version

    }

    static final class TeamCityConfiguration {
        String goals = 'clean package'
        String pathToPomFile = null
        String workingDirectory = null

        String geUrl = null
        String geExtensionVersion = null
        String ccudExtensionVersion = null
        String geExtensionCustomCoordinates = null
        String ccudExtensionCustomCoordinates = null
        Boolean allowUntrustedServer = null
        Boolean commandLineBuildStepEnabled = null

        void applyTo(Map<String, String> configParameters, Map<String, String> runnerParameters) {
            applyRunnerParameters(runnerParameters)
            applyConfigParameters(configParameters)
        }

        private void applyRunnerParameters(Map<String, String> runnerParameters) {
            runnerParameters.put('goals', goals)

            if (workingDirectory) {
                runnerParameters.put('teamcity.build.workingDir', workingDirectory)
            }

            if (pathToPomFile)
                runnerParameters.put('pomLocation', pathToPomFile)
        }

        private void applyConfigParameters(Map<String, String> configParameters) {
            if (geUrl)
                configParameters.put('buildScanPlugin.gradle-enterprise.url', geUrl)

            if (geExtensionVersion)
                configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', geExtensionVersion)

            if (ccudExtensionVersion)
                configParameters.put('buildScanPlugin.ccud.extension.version', ccudExtensionVersion)

            if (geExtensionCustomCoordinates)
                configParameters.put('buildScanPlugin.gradle-enterprise.extension.custom.coordinates', geExtensionCustomCoordinates)

            if (ccudExtensionCustomCoordinates)
                configParameters.put('buildScanPlugin.ccud.extension.custom.coordinates', ccudExtensionCustomCoordinates)

            if (allowUntrustedServer != null)
                configParameters.put('buildScanPlugin.gradle-enterprise.allow-untrusted-server', allowUntrustedServer.toString())

            if (commandLineBuildStepEnabled != null)
                configParameters.put('buildScanPlugin.command-line-build-step.enabled', commandLineBuildStepEnabled.toString())
        }
    }

    static final class ProjectConfiguration {
        String geExtensionVersion = null
        String ccudExtensionVersion = null
        GroupArtifactVersion customExtension = null
        String geUrl = null
        String pomDirName = null
        String dotMvnParentDirName = null

        File buildIn(File directory) {
            def pomDir = pomDirName ? new File(directory, pomDirName) : directory
            def dotMvnParentDir = dotMvnParentDirName ? new File(directory, dotMvnParentDirName) : directory

            [pomDir, dotMvnParentDir].each { it.mkdirs() }

            setProjectDefinedExtensions(dotMvnParentDir, geExtensionVersion, ccudExtensionVersion, customExtension)
            setProjectDefinedGeConfiguration(dotMvnParentDir, geUrl)
            setPomFile(pomDir, 'pom.xml')
        }

        private static File setPomFile(File directory, String name) {
            def pom = new File(directory, name)
            pom << getClass().getResourceAsStream("/pom.xml")
            pom
        }

        private static void setProjectDefinedExtensions(File directory, String geExtensionVersion, String ccudExtensionVersion, GroupArtifactVersion customExtension) {
            def extensionsXml = getFileInDotMvn(directory, "extensions.xml")
            extensionsXml << """<?xml version="1.0" encoding="UTF-8"?><extensions>"""

            if (geExtensionVersion) {
                extensionsXml << """
            <extension>
                <groupId>com.gradle</groupId>
                <artifactId>gradle-enterprise-maven-extension</artifactId>
                <version>$geExtensionVersion</version>
            </extension>"""
            }

            if (ccudExtensionVersion) {
                extensionsXml << """
            <extension>
                <groupId>com.gradle</groupId>
                <artifactId>common-custom-user-data-maven-extension</artifactId>
                <version>$ccudExtensionVersion</version>
            </extension>"""
            }

            if (customExtension) {
                extensionsXml << """
            <extension>
                <groupId>${customExtension.group}</groupId>
                <artifactId>${customExtension.artifact}</artifactId>
                <version>${customExtension.version}</version>
            </extension>"""
            }

            extensionsXml << """</extensions>"""
        }

        private static void setProjectDefinedGeConfiguration(File directory, String geUrl) {
            if (geUrl) {
                def geConfig = getFileInDotMvn(directory, 'gradle-enterprise.xml')
                geConfig << """<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <gradleEnterprise
                xmlns="https://www.gradle.com/gradle-enterprise-maven" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="https://www.gradle.com/gradle-enterprise-maven https://www.gradle.com/schema/gradle-enterprise-maven.xsd">
              <server>
                <url>$geUrl</url>
              </server>
            </gradleEnterprise>"""
            }
        }

        private static File getFileInDotMvn(File parent, String child) {
            def dotMvn = new File(parent, ".mvn")
            dotMvn.mkdirs()
            return new File(dotMvn, child)
        }

    }

    static String getRelativePomPath(File parent, File child) {
        parent.toPath().relativize(child.toPath()).toString()
    }
}
