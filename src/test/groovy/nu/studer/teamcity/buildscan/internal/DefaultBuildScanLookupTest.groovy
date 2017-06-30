package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.buildLog.BuildLog
import jetbrains.buildServer.serverSide.buildLog.LogMessage
import spock.lang.Specification
import spock.lang.Unroll

class DefaultBuildScanLookupTest extends Specification {

    @Unroll
    def "finds one or more build scan publications if preceded by publishing message"() {
        given:
        DefaultBuildScanLookup lookup = new DefaultBuildScanLookup()

        and:
        SBuild build = Mock(SBuild)
        BuildLog buildLog = Mock(BuildLog)
        build.buildLog >> buildLog
        buildLog.messagesIterator >> {
            logMessages.iterator()
        }

        when:
        def buildScans = lookup.getBuildScansForBuild(build)

        then:
        buildScans.all()*.id == expectedBuildScanIds

        where:
        logMessages                                         | expectedBuildScanIds
        [
            log("Publishing build information..."),
            log("http://scans.grdev.net/s/fgb5fkqivexry")]  | ["fgb5fkqivexry"]
        [
            log("Publishing build scan..."),
            log("http://scans.grdev.net/s/fgb5fkqivexry")]  | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build information..."),
            log("https://scans.grdev.net/s/fgb5fkqivexry")] | ["fgb5fkqivexry"]
        [
            log("Publishing build information..."),
            log("https://scans.gradle.com/s/fgb5fkqivexry"),
            log("some text")]                               | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build information..."),
            log("https://scans.gradle.com/s/fgb5fkqivexry"),
            log("some text")]                               | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build information..."),
            log("https://scans.gradle.com/s/fgb5fkqivexry"),
            log("https://scans.gradle.com/s/nfwou3cmx3f5e"),
            log("some text")]                               | ["fgb5fkqivexry"]
        [
            log("some text"),
            log("Publishing build information..."),
            log("https://scans.grdev.net/s/fgb5fkqivexry"),
            log("some text"),
            log("Publishing build scan..."),
            log("https://scans.grdev.net/s/nfwou3cmx3f5e"),
            log("Publishing build information..."),
            log("https://scans.grdev.net/s/gixqsq36jmtpw"),
            log("some text")]                               | ["fgb5fkqivexry", "nfwou3cmx3f5e", "gixqsq36jmtpw"]
        [
            log("some text"),
            log("https://scans.grdev.net/s/fgb5fkqivexry")] | []
        [
            log("some text"),
            log("Publishing build information...")]         | []
        [
            log("some text"),
            log("Publishing build information..."),
            log("some text"),
            log("https://scans.grdev.net/s/fgb5fkqivexry")] | []
    }

    private static LogMessage log(String text) {
        new LogMessage(text, Status.NORMAL, new Date(), "", false, 1)
    }

}
