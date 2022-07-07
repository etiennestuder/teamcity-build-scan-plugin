package nu.studer.teamcity.buildscan.agent.maven

import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.util.EventDispatcher
import nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector
import nu.studer.teamcity.buildscan.agent.ExtensionApplicationListener
import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.TestBuildRunnerContext
import spock.lang.Specification
import spock.lang.TempDir

class BaseExtensionApplicationTest extends Specification {

    static final List<JdkCompatibleMavenVersion> SUPPORTED_MAVEN_VERSIONS = [
        new JdkCompatibleMavenVersion('3.5.0', 7, 11),
        new JdkCompatibleMavenVersion('3.5.4', 7, 11),
        new JdkCompatibleMavenVersion('3.6.0', 7, 11),
        new JdkCompatibleMavenVersion('3.6.3', 7, 11),
        new JdkCompatibleMavenVersion('3.8.1', 7, 11),
        new JdkCompatibleMavenVersion('3.8.6', 7, 11)
    ]

    static final List<JdkCompatibleMavenVersion> UNSUPPORTED_MAVEN_VERSIONS = [
        new JdkCompatibleMavenVersion('3.0', 7, 11),
        new JdkCompatibleMavenVersion('3.0.5', 7, 11),
        new JdkCompatibleMavenVersion('3.1.0', 7, 11),
        new JdkCompatibleMavenVersion('3.1.1', 7, 11),
        new JdkCompatibleMavenVersion('3.2.1', 7, 11),
        new JdkCompatibleMavenVersion('3.2.5', 7, 11),
    ]

    @TempDir
    File checkoutDir

    @TempDir
    File agentTempDir

    @TempDir
    File agentMavenInstallation

    ExtensionApplicationListener extensionApplicationListener

    void setup() {
        extensionApplicationListener = Mock(ExtensionApplicationListener)
    }

    String run(String mvnVersion, MavenProject mvnProject, TcPluginConfig tcPluginConfig) {
        run(mvnVersion, mvnProject, tcPluginConfig, new MavenBuildStepConfig(checkoutDir: checkoutDir))
    }

    String run(String mvnVersion, MavenProject mvnProject, TcPluginConfig tcPluginConfig, MavenBuildStepConfig mvnBuildStepConfig) {
        def injector = new BuildScanServiceMessageInjector(EventDispatcher.create(AgentLifeCycleListener.class), extensionApplicationListener)

        new MavenInstaller(
            version: mvnVersion,
            installationDir: agentMavenInstallation,
        ).installMaven()

        def configParameters = tcPluginConfig.toConfigParameters()
        def runnerParameters = mvnBuildStepConfig.toRunnerParameters()

        def context = new TestBuildRunnerContext('Maven2', agentTempDir, configParameters, runnerParameters, ['maven': agentMavenInstallation.absolutePath], mvnBuildStepConfig.isVirtualContext)
        injector.beforeRunnerStart(context)

        def runner = new MavenRunner(
            installationDir: agentMavenInstallation,
            projectDir: new File(runnerParameters.get('teamcity.build.workingDir') ?: checkoutDir.absolutePath),
            multiModuleProjectDir: mvnProject.dotMvn.parentFile,
            arguments: ("${runnerParameters.get('goals')} ${runnerParameters.get('runnerArgs')}".toString().trim().split(/\s+/))
        )

        if (runnerParameters.containsKey('pomLocation')) {
            runner.arguments += ['-f', new File(runnerParameters.get('teamcity.build.checkoutDir'), runnerParameters.get('pomLocation')).absolutePath]
        }

        return runner.run()
    }

    static final class JdkCompatibleMavenVersion {

        final String mavenVersion
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
