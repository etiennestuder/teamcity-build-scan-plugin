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

import static nu.studer.teamcity.buildscan.internal.DefaultBuildScanDisplayArbiter.GRADLE_RUNNER
import static nu.studer.teamcity.buildscan.internal.DefaultBuildScanDisplayArbiter.MAVEN_RUNNER
import static nu.studer.teamcity.buildscan.internal.DefaultBuildScanDisplayArbiter.SIMPLE_RUNNER

class DefaultBuildScanDisplayArbiterTest extends Specification {

    @Unroll
    def "show build scan info if Gradle runner is present"() {
        given:
        DefaultBuildScanDisplayArbiter defaultBuildScanDisplayArbiter = new DefaultBuildScanDisplayArbiter(Stub(BuildScanLookup))

        and:
        SBuildType buildType = Stub(SBuildType)
        buildType.buildRunners >> { buildRunnerTypes }

        and:
        SBuild sbuild = Stub(SBuild)
        sbuild.buildType >> buildType

        when:
        def show = defaultBuildScanDisplayArbiter.showBuildScanInfo(sbuild)

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
        DefaultBuildScanDisplayArbiter defaultBuildScanDisplayArbiter = new DefaultBuildScanDisplayArbiter(Stub(BuildScanLookup))

        and:
        SBuildType buildType = Stub(SBuildType)
        buildType.buildRunners >> { buildRunnerTypes }

        and:
        SBuild sbuild = Stub(SBuild)
        sbuild.buildType >> buildType

        when:
        def show = defaultBuildScanDisplayArbiter.showBuildScanInfo(sbuild)

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
        BuildScanLookup buildScanLookup = Stub(BuildScanLookup)
        buildScanLookup.getBuildScansForBuild(_ as SBuild) >> { buildScanReferencesInBuild }

        DefaultBuildScanDisplayArbiter defaultBuildScanDisplayArbiter = new DefaultBuildScanDisplayArbiter(buildScanLookup)

        and:
        SBuildType buildType = Stub(SBuildType)
        buildType.buildRunners >> { buildRunnerTypes }

        and:
        SBuild sbuild = Stub(SBuild)
        sbuild.buildType >> buildType

        when:
        def show = defaultBuildScanDisplayArbiter.showBuildScanInfo(sbuild)

        then:
        show == expectedToShow

        where:
        buildRunnerTypes                       | buildScanReferencesInBuild                                                  | expectedToShow
        [buildRunnerDescriptor(SIMPLE_RUNNER)] | BuildScanReferences.of(new BuildScanReference('someScanId', 'someScanUrl')) | true
        [buildRunnerDescriptor(SIMPLE_RUNNER)] | BuildScanReferences.of()                                                    | false
    }

    private SBuildRunnerDescriptor buildRunnerDescriptor(String runnerType) {
        SBuildRunnerDescriptor descriptor = Stub(SBuildRunnerDescriptor)
        RunType runType = Stub(RunType)
        runType.getType() >> { runnerType }
        descriptor.getRunType() >> { runType }
        descriptor
    }

}
