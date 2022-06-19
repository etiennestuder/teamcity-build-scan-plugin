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

    static final String GE_URL = System.getenv('GRADLE_ENTERPRISE_TEST_INSTANCE')
    static final String GE_EXTENSION_VERSION = '1.14.2'
    static final String CCUD_EXTENSION_VERSION = '1.10.1'

    @TempDir
    File testProjectDir

    @TempDir
    File agentTempDir

    File wrapperDir
    File dotMvn

    Map<String, String> configParameters
    Map<String, String> runnerParameters

    BuildRunnerContext context
    ExtensionApplicationListener extensionApplicationListener
    BuildScanServiceMessageInjector injector

    void setup() {
        extractTestProject()

        configParameters = new HashMap<String, String>()
        runnerParameters = new HashMap<String, String>()

        runnerParameters.put('teamcity.build.checkoutDir', testProjectDir.absolutePath)
        runnerParameters.put('teamcity.build.workingDir', testProjectDir.absolutePath)

        context = new TestBuildRunnerContext("Maven2", agentTempDir, configParameters, runnerParameters)
        extensionApplicationListener = Mock(ExtensionApplicationListener)
        injector = new BuildScanServiceMessageInjector(EventDispatcher.create(AgentLifeCycleListener.class), extensionApplicationListener)
    }

    void extractTestProject() {
        ['pom.xml', 'mvnw', 'mvnw.cmd'].each {
            def file = new File(testProjectDir, it)
            file << getClass().getResourceAsStream("/maven-test-project/$it")
            if (it.startsWith("mvnw")) {
                file.setExecutable(true)
            }
        }

        dotMvn = new File(testProjectDir, '.mvn')
        dotMvn.mkdirs()

        wrapperDir = new File(dotMvn, 'wrapper')
        wrapperDir.mkdirs()

        ['maven-wrapper.jar'].each {
            def file = new File(wrapperDir, it)
            file << getClass().getResourceAsStream("/maven-test-project/.mvn/wrapper/$it")
        }
    }

    def "does not apply GE / CCUD extensions when not defined in project and not requested via TC config (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedExtensions(GE_EXTENSION_VERSION, null)
        setProjectDefinedGeConfiguration()

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        runnerParameters.put('pomLocation', 'pom.xml')

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters, '-f ' + new File(testProjectDir, 'pom.xml').path)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedExtensions(GE_EXTENSION_VERSION, null)
        setProjectDefinedGeConfiguration()

        and:
        runnerParameters.put('pomLocation', 'pom.xml')

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters, '-f ' + new File(testProjectDir, 'pom.xml').path)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        runnerParameters.remove('teamcity.build.checkoutDir')
        runnerParameters.remove('teamcity.build.workingDir')

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension not defined in project and not applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.ccud.extension.version', CCUD_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)
        configParameters.put('buildScanPlugin.ccud.extension.version', CCUD_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedExtensions(GE_EXTENSION_VERSION, null)
        setProjectDefinedGeConfiguration()

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)
        configParameters.put('buildScanPlugin.ccud.extension.version', CCUD_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedExtensions(GE_EXTENSION_VERSION, CCUD_EXTENSION_VERSION)
        setProjectDefinedGeConfiguration()

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)
        configParameters.put('buildScanPlugin.ccud.extension.version', CCUD_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedExtensions(GE_EXTENSION_VERSION, null)
        setProjectDefinedGeConfiguration()

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', 'https://ge-server.invalid')
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedGeConfiguration('https://ge-server.invalid')

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.allow-untrusted-server', 'true')
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

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
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters, '', 'org.jetbrains.maven:info-maven3-plugin:1.0.2:info')

        then:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    void setProjectDefinedExtensions(String geExtensionVersion, String ccudExtensionVersion) {
        def extensionsXml = new File(dotMvn, 'extensions.xml')
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

        extensionsXml << """</extensions>"""
    }

    void setProjectDefinedGeConfiguration(String geUrl = GE_URL) {
        File geConfig = new File(dotMvn, "gradle-enterprise.xml")
        geConfig << """<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <gradleEnterprise
                xmlns="https://www.gradle.com/gradle-enterprise-maven" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="https://www.gradle.com/gradle-enterprise-maven https://www.gradle.com/schema/gradle-enterprise-maven.xsd">
              <server>
                <url>$geUrl</url>
              </server>
            </gradleEnterprise>"""
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

    void setMavenVersion(String version) {
        def mavenWrapperProperties = new File(wrapperDir, 'maven-wrapper.properties')
        mavenWrapperProperties << """distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
"""
    }

    String run(Map<String, String> runnerParameters, String arguments = '', String goals = 'clean package') {
        def mvnExecutable = System.getProperty('os.name').startsWith('Windows') ? './mvnw.cmd' : './mvnw'
        def runnerArgs = runnerParameters.get('runnerArgs')

        def command = [mvnExecutable]
        if (goals.trim()) {
            command += goals.split(' ').toList()
        }
        if (arguments.trim()) {
            command += arguments.split(' ').toList()
        }
        if (runnerArgs.trim()) {
            command += runnerArgs.split(' ').toList()
        }

        new ProcessBuilder(command)
            .directory(testProjectDir)
            .start()
            .text
    }
}
