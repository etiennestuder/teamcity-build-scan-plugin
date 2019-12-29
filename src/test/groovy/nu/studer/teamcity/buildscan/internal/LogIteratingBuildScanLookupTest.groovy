package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.buildLog.BuildLog
import jetbrains.buildServer.serverSide.buildLog.LogMessage
import nu.studer.teamcity.buildscan.TeamCityConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class LogIteratingBuildScanLookupTest extends Specification {

    def "iterates build log if configuration parameter not present"() {
        given:
        LogIteratingBuildScanLookup lookup = new LogIteratingBuildScanLookup()

        and:
        ParametersProvider parametersProvider = Mock(ParametersProvider)

        BuildLog buildLog = Mock(BuildLog)
        buildLog.messagesIterator >> {
            [
                log("Publishing build scan..."),
                log("http://scans.grdev.net/s/fgb5fkqivexry")
            ].iterator()
        }

        SBuild build = Mock(SBuild) {
            isFinished() >> true
            getParametersProvider() >> parametersProvider
            getBuildLog() >> buildLog
        }

        when:
        def buildScans = lookup.getBuildScansForBuild(build)

        then:
        buildScans.size() == 1
    }

    def "iterates build log if configuration parameter present and set to true"() {
        given:
        LogIteratingBuildScanLookup lookup = new LogIteratingBuildScanLookup()

        and:
        ParametersProvider parametersProvider = Mock(ParametersProvider) {
            get(TeamCityConfiguration.BUILD_SCAN_LOG_PARSING) >> 'true'
        }

        BuildLog buildLog = Mock(BuildLog)
        buildLog.messagesIterator >> {
            [
                log("Publishing build scan..."),
                log("http://scans.grdev.net/s/fgb5fkqivexry")
            ].iterator()
        }

        SBuild build = Mock(SBuild) {
            isFinished() >> true
            getParametersProvider() >> parametersProvider
            getBuildLog() >> buildLog
        }

        when:
        def buildScans = lookup.getBuildScansForBuild(build)

        then:
        buildScans.size() == 1
    }

    def "skips iterating build log if configuration parameter present but not set to true"() {
        given:
        LogIteratingBuildScanLookup lookup = new LogIteratingBuildScanLookup()

        and:
        ParametersProvider parametersProvider = Mock(ParametersProvider) {
            get(TeamCityConfiguration.BUILD_SCAN_LOG_PARSING) >> 'dummy'
        }

        BuildLog buildLog = Mock(BuildLog)
        buildLog.messagesIterator >> {
            [
                log("Publishing build scan..."),
                log("http://scans.grdev.net/s/fgb5fkqivexry")
            ].iterator()
        }

        SBuild build = Mock(SBuild) {
            isFinished() >> true
            getParametersProvider() >> parametersProvider
            getBuildLog() >> buildLog
        }

        when:
        def buildScans = lookup.getBuildScansForBuild(build)

        then:
        buildScans.isEmpty()
    }

    def "skips iterating build log if build is still running"() {
        given:
        LogIteratingBuildScanLookup lookup = new LogIteratingBuildScanLookup()

        and:
        ParametersProvider parametersProvider = Mock(ParametersProvider)

        BuildLog buildLog = Mock(BuildLog)
        buildLog.messagesIterator >> {
            [
                log("Publishing build scan..."),
                log("http://scans.grdev.net/s/fgb5fkqivexry")
            ].iterator()
        }

        SBuild build = Mock(SBuild) {
            isFinished() >> false
            getParametersProvider() >> parametersProvider
            getBuildLog() >> buildLog
        }

        when:
        def buildScans = lookup.getBuildScansForBuild(build)

        then:
        buildScans.isEmpty()
    }

    @Unroll
    def "finds one or more build scan publications if preceded by publishing message"() {
        given:
        LogIteratingBuildScanLookup lookup = new LogIteratingBuildScanLookup()

        and:
        ParametersProvider parametersProvider = Mock(ParametersProvider)

        BuildLog buildLog = Mock(BuildLog)
        buildLog.messagesIterator >> {
            logMessages.iterator()
        }

        SBuild build = Mock(SBuild) {
            isFinished() >> true
            getParametersProvider() >> parametersProvider
            getBuildLog() >> buildLog
        }

        when:
        def buildScans = lookup.getBuildScansForBuild(build)

        then:
        buildScans.all()*.id == expectedBuildScanIds

        where:
        logMessages                                               | expectedBuildScanIds
        [
            log("Publishing build scan..."),
            log("http://scans.grdev.net/s/fgb5fkqivexry")]        | ["fgb5fkqivexry"]
        [
            log("[INFO] Publishing build scan..."),
            log("[INFO] http://scans.grdev.net/s/fgb5fkqivexry")] | ["fgb5fkqivexry"]
        [
            log("Publishing build scan..."),
            log("http://scans.grdev.net/s/fgb5fkqivexry")]        | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build scan..."),
            log("https://scans.grdev.net/s/fgb5fkqivexry")]       | ["fgb5fkqivexry"]
        [
            log("Publishing build scan..."),
            log("https://scans.gradle.com/s/fgb5fkqivexry"),
            log("some text")]                                     | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build scan..."),
            log("https://scans.gradle.com/s/fgb5fkqivexry"),
            log("some text")]                                     | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build scan..."),
            log("https://scans.gradle.com/s/fgb5fkqivexry"),
            log("https://scans.gradle.com/s/nfwou3cmx3f5e"),
            log("some text")]                                     | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build scan..."),
            log("https://scans.grdev.net/s/fgb5fkqivexry"),
            log("some text"),
            log("Publishing build scan..."),
            log("https://scans.grdev.net/s/nfwou3cmx3f5e"),
            log("Publishing build scan..."),
            log("https://scans.grdev.net/s/gixqsq36jmtpw"),
            log("some text")]                                     | ["fgb5fkqivexry", "nfwou3cmx3f5e", "gixqsq36jmtpw"]
        [
            log("some text"),
            log("https://scans.grdev.net/s/fgb5fkqivexry")]       | []
        [
            log("some text"),
            log("Publishing build scan...")]                      | []
        [
            log("some text"),
            log("Publishing build scan..."),
            log("some text"),
            log("https://scans.grdev.net/s/fgb5fkqivexry")]       | []
    }

    private static LogMessage log(String text) {
        new LogMessage(text, Status.NORMAL, new Date(), "", false, 1)
    }

}
