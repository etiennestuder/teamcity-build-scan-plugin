package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.serverSide.SBuild
import nu.studer.teamcity.buildscan.BuildScanLookup
import nu.studer.teamcity.buildscan.BuildScanReference
import nu.studer.teamcity.buildscan.BuildScanReferences
import spock.lang.Specification

class DataStoreBuildScanLookupTest extends Specification {

    def "delegates lookup when no data is found in store"() {
        given:
        def store = Mock(BuildScanDataStore)
        def delegate = Mock(BuildScanLookup)
        def lookup = new DataStoreBuildScanLookup(delegate, store)

        when:
        lookup.getBuildScansForBuild(Mock(SBuild))

        then:
        1 * delegate.getBuildScansForBuild(_) >> BuildScanReferences.of()
    }

    def "returns store results"() {
        given:
        def build = Mock(SBuild)
        def reference = new BuildScanReference("someid", "https://gradle.company.com/s/someid")
        def store = Mock(BuildScanDataStore) {
            fetch(build) >> Collections.singletonList(reference)
        }
        def delegate = Mock(BuildScanLookup)
        def lookup = new DataStoreBuildScanLookup(delegate, store)

        when:
        def result = lookup.getBuildScansForBuild(build)

        then:
        result == BuildScanReferences.of(reference)
        0 * delegate.getBuildScansForBuild(_)
    }
}
