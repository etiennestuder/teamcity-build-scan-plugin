package nu.studer.teamcity.buildscan.agent

import jetbrains.buildServer.util.FileUtil
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static nu.studer.teamcity.buildscan.agent.BuildScanServiceMessageInjector.BUILD_SCAN_INIT_GRADLE

class BaseInitScriptTest extends Specification {

    private static final String TEAMCITY_BUILD_INIT_CLASS_PATH_SYS_PROP = 'teamCityInitClasspath'

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File initScriptFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        initScriptFile = testProjectDir.newFile('initscript.gradle')
        FileUtil.copyResource(BuildScanServiceMessageInjector.class, '/' + BUILD_SCAN_INIT_GRADLE, initScriptFile)
    }

    BuildResult run(GradleVersion gradleVersion = GradleVersion.current()) {
        def args = ['tasks', '-I', initScriptFile.absolutePath]
        GradleRunner.create()
                .withGradleVersion(gradleVersion.version)
                .withProjectDir(testProjectDir.root)
                .withArguments(args)
                .withEnvironment(['TEAMCITY_BUILD_INIT_PATH': System.getProperty(TEAMCITY_BUILD_INIT_CLASS_PATH_SYS_PROP)])
                .build()
    }

    static void outputContainsTeamCityServiceMessageBuildStarted(BuildResult result) {
        assert result.output.contains("##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']")
    }

}
