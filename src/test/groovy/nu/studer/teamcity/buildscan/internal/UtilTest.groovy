package nu.studer.teamcity.buildscan.internal

import spock.lang.Specification
import spock.lang.Unroll

class UtilTest extends Specification {

    @Unroll
    def "build scan url detection works for Gradle and Maven builds"() {
        expect:
        Util.isBuildScanUrl(text) == isScanUrl

        where:
        text                                              | isScanUrl
        'https://gradle.chess.com/s/urh37ke7awlrk'        | true
        '[INFO] https://gradle.chess.com/s/urh37ke7awlrk' | true
        '> Task :help'                                    | false
    }

    @Unroll
    def "can get build scan ID"() {
        expect:
        Util.getBuildScanId(text) == buildScanId

        where:
        text                                              | buildScanId
        'https://gradle.chess.com/s/urh37ke7awlrk'        | 'urh37ke7awlrk'
        '[INFO] https://gradle.chess.com/s/urh37ke7awlrk' | 'urh37ke7awlrk'
    }

    @Unroll
    def "can get build scan url"() {
        expect:
        Util.getBuildScanUrl(text) == buildScanUrl

        where:
        text                                              | buildScanUrl
        'https://gradle.chess.com/s/urh37ke7awlrk'        | 'https://gradle.chess.com/s/urh37ke7awlrk'
        '[INFO] https://gradle.chess.com/s/urh37ke7awlrk' | 'https://gradle.chess.com/s/urh37ke7awlrk'
    }
}
