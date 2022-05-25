package nu.studer.teamcity.buildscan.agent.maven

import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.util.EventDispatcher
import nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification
import spock.lang.TempDir

import static org.junit.Assume.assumeTrue

class GradleEnterpriseExtensionApplicationTest extends Specification {

    static final List<JdkCompatibleMavenVersion> SUPPORTED_MAVEN_VERSIONS = [
        new JdkCompatibleMavenVersion('3.6.3', 7, 11),
        new JdkCompatibleMavenVersion('3.8.5', 7, 11)
    ]

    static final String GE_URL = System.getenv('GRADLE_ENTERPRISE_TEST_INSTANCE')
    static final String GE_EXTENSION_VERSION = '1.14.1'
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

    BuildScanServiceMessageInjector injector

    void setup() {
        extractTestProject()

        configParameters = new HashMap<String, String>()
        runnerParameters = new HashMap<String, String>()

        runnerParameters.put('teamcity.build.workingDir', testProjectDir.absolutePath)

        context = new TestContext(agentTempDir, configParameters, runnerParameters)

        injector = new BuildScanServiceMessageInjector(EventDispatcher.create(AgentLifeCycleListener.class))
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
        run(runnerParameters)

        then:
        classpathOmitsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)

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
        classpathContainsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)
//        output.contains('Applying common-custom-user-data-maven-extension')

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
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        classpathOmitsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via project when defined in project and pomLocation set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        setProjectDefinedExtensions(GE_EXTENSION_VERSION, null)
        setProjectDefinedGeConfiguration()
        runnerParameters.put('teamcity.build.checkoutDir', testProjectDir.absolutePath)
        runnerParameters.put('pomLocation', 'pom.xml')
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters, ['-f', "${new File(testProjectDir, 'pom.xml')}".toString()])

        then:
        classpathOmitsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when workingDir not set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        setMavenVersion(jdkCompatibleMavenVersion.mavenVersion)

        and:
        runnerParameters.remove('teamcity.build.workingDir')
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        classpathContainsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension not defined in project (#jdkCompatibleMavenVersion)"() {
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
        classpathOmitsGeExtension(runnerParameters)
        classpathContainsCcudExtension(runnerParameters)

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
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)
        configParameters.put('buildScanPlugin.ccud.extension.version', CCUD_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        classpathContainsGeExtension(runnerParameters)
        classpathContainsCcudExtension(runnerParameters)
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
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)
        configParameters.put('buildScanPlugin.ccud.extension.version', CCUD_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        classpathOmitsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
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
        configParameters.put('buildScanPlugin.gradle-enterprise.url', 'https://ge-server.invalid')
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        classpathOmitsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
        systemPropertiesPropertiesOmitsGeUrl(runnerParameters)
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
        configParameters.put('buildScanPlugin.gradle-enterprise.url', GE_URL)
        configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', GE_EXTENSION_VERSION)

        when:
        injector.beforeRunnerStart(context)
        def output = run(runnerParameters)

        then:
        classpathContainsGeExtension(runnerParameters)
        classpathOmitsCcudExtension(runnerParameters)
        systemPropertiesPropertiesContainsGeUrl(runnerParameters)
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

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

    void classpathContainsGeExtension(Map<String, String> runnerParameters, boolean contains = true) {
        assert runnerParameters.containsKey("runnerArgs")
        assert contains == runnerParameters.get("runnerArgs").contains(new File(agentTempDir, "gradle-enterprise-maven-extension-${GE_EXTENSION_VERSION}.jar").absolutePath)
    }

    void classpathOmitsGeExtension(Map<String, String> runnerParameters) {
        classpathContainsGeExtension(runnerParameters, false)
    }

    void classpathContainsCcudExtension(Map<String, String> runnerParameters, boolean contains = true) {
        assert runnerParameters.containsKey("runnerArgs")
        assert contains == runnerParameters.get("runnerArgs").contains(new File(agentTempDir, "common-custom-user-data-maven-extension-${CCUD_EXTENSION_VERSION}.jar").absolutePath)
    }

    void classpathOmitsCcudExtension(Map<String, String> runnerParameters) {
        classpathContainsCcudExtension(runnerParameters, false)
    }

    void systemPropertiesPropertiesContainsGeUrl(Map<String, String> runnerParameters, boolean contains = true) {
        assert runnerParameters.containsKey("runnerArgs")
        assert contains == runnerParameters.get('runnerArgs').contains("-Dgradle.enterprise.url=$GE_URL")
    }

    void systemPropertiesPropertiesOmitsGeUrl(Map<String, String> runnerParameters) {
        systemPropertiesPropertiesContainsGeUrl(runnerParameters, false)
    }

    void outputContainsTeamCityServiceMessageBuildStarted(BuildResult result) {
        outputContainsTeamCityServiceMessageBuildStarted(result.output)
    }

    void outputContainsTeamCityServiceMessageBuildStarted(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']"
        assert output.contains(serviceMsg)
        assert 1 == output.count(serviceMsg)
    }

    void outputContainsTeamCityServiceMessageBuildScanUrl(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${GE_URL}/s/"
        assert output.contains(serviceMsg)
        assert 1 == output.count(serviceMsg)
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

    String run(Map<String, String> runnerParameters, List<String> arguments = []) {
        def mvnExecutable = System.getProperty('os.name').startsWith('Windows') ? './mvnw.cmd' : './mvnw'
        def runnerArgs = runnerParameters.get('runnerArgs')

        def command = [mvnExecutable, 'clean', 'package']
        runnerArgs.split(' ').each {
            command += it
        }
        command += arguments

        new ProcessBuilder(command)
            .directory(testProjectDir)
            .start()
            .text
    }

}
