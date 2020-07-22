package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.serverSide.SBuild
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
        SBuild sbuild = Stub(SBuild)
        sbuild.artifactsDirectory >> artifactsFolder

        when:
        store.mark(sbuild)

        then:
        Files.exists(store.getBuildScanLinksFile(sbuild))
        store.getBuildScanLinksFile(sbuild).toFile().readLines() == []
    }

    @Unroll
    def "build scans urls are appended to build scan links file line by line"() {
        given:
        SBuild sbuild = Stub(SBuild)
        sbuild.artifactsDirectory >> artifactsFolder

        when:
        buildScanUrls.each { scanUrl -> store.store(sbuild, scanUrl) }

        then:
        Files.exists(store.getBuildScanLinksFile(sbuild))
        store.getBuildScanLinksFile(sbuild).toFile().readLines() == buildScanUrls

        where:
        buildScanUrls << [['http://gradle.com/s/1'], ['http://gradle.com/s/1', 'http://gradle.com/s/2']]
    }

    @Unroll
    def "fetched build scan references contain previously persisted build scan urls"() {
        given:
        SBuild sbuild = Stub(SBuild)
        sbuild.artifactsDirectory >> artifactsFolder

        and:
        buildScanUrls.each { scanUrl -> store.store(sbuild, scanUrl) }

        when:
        def scanReferences = store.fetch(sbuild)

        then:
        scanReferences.collect { it.url } == buildScanUrls

        where:
        buildScanUrls << [[], ['http://gradle.com/s/1'], ['http://gradle.com/s/1', 'http://gradle.com/s/2']]
    }

    def "null is returned when no build scan links file is present"() {
        given:
        SBuild sbuild = Stub(SBuild)
        sbuild.artifactsDirectory >> artifactsFolder

        when:
        def scanReferences = store.fetch(sbuild)

        then:
        scanReferences == null
    }

}
