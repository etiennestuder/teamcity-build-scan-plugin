package nu.studer.teamcity.buildscan.agent

import org.gradle.testkit.runner.BuildResult

class MockBuildScanInitScriptTest extends BaseInitScriptTest {

    private static final String MOCK_PLUGIN_JAR_SYS_PROP = 'mockPluginJar'
    File mockPluginJar
    File mockPluginJar2

    def setup() {
        mockPluginJar = new File(System.getProperty(MOCK_PLUGIN_JAR_SYS_PROP))
        mockPluginJar2 = new File(mockPluginJar.parent, 'mockPluginJar2.jar')
        mockPluginJar2.bytes = mockPluginJar.bytes
    }

    def "properly reacts to multiple applications"() {
        given:
        def firstApplication = testProjectDir.newFile('first.gradle')
        firstApplication << mockPluginApplication(mockPluginJar)
        def secondApplication = testProjectDir.newFile('second.gradle')
        secondApplication << mockPluginApplication(mockPluginJar2)
        settingsFile << """
          apply from: '${firstApplication.absolutePath}'
          apply from: '${secondApplication.absolutePath}'
        """

        when:
        def result = run()

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTwoTeamCityServiceMessageBuildScanUrl(result)
    }

    private static String mockPluginApplication(File mockPluginJar) {
        """
          buildscript {
            dependencies {
              classpath(files("${mockPluginJar.absolutePath}"))
            }
          }
          apply plugin: getClass().classLoader.loadClass('com.gradle.enterprise.gradleplugin.MockPlugin')
        """
    }

    private static void outputContainsTwoTeamCityServiceMessageBuildScanUrl(BuildResult result) {
        assert result.output.contains("""
##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:https://server.com/buildScanID']
##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:https://server.com/buildScanID']
""")
    }

}
