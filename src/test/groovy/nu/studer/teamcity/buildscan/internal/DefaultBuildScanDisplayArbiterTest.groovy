package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import nu.studer.teamcity.buildscan.BuildScanLookup
import nu.studer.teamcity.buildscan.BuildScanReference
import nu.studer.teamcity.buildscan.BuildScanReferences
import spock.lang.Specification
import spock.lang.Unroll

import static nu.studer.teamcity.buildscan.internal.DefaultBuildScanDisplayArbiter.*

class DefaultBuildScanDisplayArbiterTest extends Specification {

    @Unroll
    def "show build scan info if Gradle runner is present"() {
        given:
        DefaultBuildScanDisplayArbiter defaultBuildScanDisplayArbiter = new DefaultBuildScanDisplayArbiter(Mock(BuildScanLookup))

        and:
        SBuildType buildType = Mock(SBuildType)
        buildType.buildRunners >> { buildRunnerTypes }

        and:
        SBuild build = Mock(SBuild)
        build.buildType >> buildType

        when:
        def show = defaultBuildScanDisplayArbiter.showBuildScanInfo(build)

        then:
        show == expectedToShow

        where:
        buildRunnerTypes                                                                   | expectedToShow
        [buildRunnerDescriptor(GRADLE_RUNNER)]                                             | true
        [buildRunnerDescriptor(GRADLE_RUNNER), buildRunnerDescriptor('some other runner')] | true
        [buildRunnerDescriptor(GRADLE_RUNNER), buildRunnerDescriptor(GRADLE_RUNNER)]       | true
        [buildRunnerDescriptor('some other runner')]                                       | false
        []                                                                                 | false
    }

    @Unroll
    def "show build scan info if Maven runner is present"() {
        given:
        DefaultBuildScanDisplayArbiter defaultBuildScanDisplayArbiter = new DefaultBuildScanDisplayArbiter(Mock(BuildScanLookup))

        and:
        SBuildType buildType = Mock(SBuildType)
        buildType.buildRunners >> { buildRunnerTypes }

        and:
        SBuild build = Mock(SBuild)
        build.buildType >> buildType

        when:
        def show = defaultBuildScanDisplayArbiter.showBuildScanInfo(build)

        then:
        show == expectedToShow

        where:
        buildRunnerTypes                                                                  | expectedToShow
        [buildRunnerDescriptor(MAVEN_RUNNER)]                                             | true
        [buildRunnerDescriptor(MAVEN_RUNNER), buildRunnerDescriptor('some other runner')] | true
        [buildRunnerDescriptor(MAVEN_RUNNER), buildRunnerDescriptor(MAVEN_RUNNER)]        | true
        [buildRunnerDescriptor('some other runner')]                                      | false
        []                                                                                | false
    }

    @Unroll
    def "show build scan info if CmdLine runner is present and build created a build scan"() {
        given:
        BuildScanLookup buildScanLookup = Mock(BuildScanLookup)
        buildScanLookup.getBuildScansForBuild(_ as SBuild) >> { buildScanReferencesInBuild }

        DefaultBuildScanDisplayArbiter defaultBuildScanDisplayArbiter = new DefaultBuildScanDisplayArbiter(buildScanLookup)

        and:
        SBuildType buildType = Mock(SBuildType)
        buildType.buildRunners >> { buildRunnerTypes }

        and:
        SBuild build = Mock(SBuild)
        build.buildType >> buildType

        when:
        def show = defaultBuildScanDisplayArbiter.showBuildScanInfo(build)

        then:
        show == expectedToShow

        where:
        buildRunnerTypes                       | buildScanReferencesInBuild                                                  | expectedToShow
        [buildRunnerDescriptor(SIMPLE_RUNNER)] | BuildScanReferences.of(new BuildScanReference('someScanId', 'someScanUrl')) | true
        [buildRunnerDescriptor(SIMPLE_RUNNER)] | BuildScanReferences.of()                                                    | false
    }

    private SBuildRunnerDescriptor buildRunnerDescriptor(String runnerType) {
        SBuildRunnerDescriptor descriptor = Mock(SBuildRunnerDescriptor)
        RunType runType = Mock(RunType)
        runType.getType() >> { runnerType }
        descriptor.getRunType() >> { runType }
        descriptor
    }

}
