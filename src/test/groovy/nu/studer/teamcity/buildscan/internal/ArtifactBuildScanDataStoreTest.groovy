package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.serverSide.SBuild
import nu.studer.teamcity.buildscan.BuildScanDataStore
import nu.studer.teamcity.buildscan.BuildScanReference
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class ArtifactBuildScanDataStoreTest extends Specification {

    ArtifactBuildScanDataStore store
    File artifactsFolder

    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    void setup() {
        store = new ArtifactBuildScanDataStore()
        artifactsFolder = tempDir.newFolder()
    }

    def "empty build scan links file is created when build is marked as started"() {
        given:
        SBuild build = Mock()
        build.artifactsDirectory >> artifactsFolder

        when:
        store.mark(build)

        then:
        Files.exists(store.getBuildScanLinksFile(build))
        store.getBuildScanLinksFile(build).readLines() == []
    }

    @Unroll
    def "build scans urls are appended to build scan links file line by line"() {
        given:
        SBuild build = Mock()
        build.artifactsDirectory >> artifactsFolder

        when:
        buildScanUrls.each { scanUrl -> store.store(build, scanUrl) }

        then:
        Files.exists(store.getBuildScanLinksFile(build))
        store.getBuildScanLinksFile(build).readLines() == buildScanUrls

        where:
        buildScanUrls << [['http://gradle.com/s/1'], ['http://gradle.com/s/1', 'http://gradle.com/s/2']]
    }

    @Unroll
    def "fetched build scan references contain previously persisted build scan urls"() {
        given:
        SBuild build = Mock()
        build.artifactsDirectory >> artifactsFolder

        and:
        buildScanUrls.each { scanUrl -> store.store(build, scanUrl) }

        when:
        def scanReferences = store.fetch(build)

        then:
        scanReferences.collect { it.url } == buildScanUrls

        where:
        buildScanUrls << [[], ['http://gradle.com/s/1'], ['http://gradle.com/s/1', 'http://gradle.com/s/2']]
    }

    def "null is returned when no build scan links file is present"() {
        given:
        SBuild build = Mock()
        build.artifactsDirectory >> artifactsFolder

        when:
        def scanReferences = store.fetch(build)

        then:
        scanReferences == null
    }

}
