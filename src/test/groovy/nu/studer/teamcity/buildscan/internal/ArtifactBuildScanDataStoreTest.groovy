package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.serverSide.SBuild
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class ArtifactBuildScanDataStoreTest extends Specification {

    ReadOnlyBuildScanDataStore fallBackStore = Mock()
    ArtifactBuildScanDataStore store

    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    File artifactsFolder

    void setup() {
        store = new ArtifactBuildScanDataStore(fallBackStore)
        artifactsFolder = tempDir.newFolder()
    }

    def "empty buildscan file is created when build is started"() {
        given:
        SBuild build = Mock()

        when:
        store.mark(build)

        then:
        build.artifactsDirectory >> artifactsFolder
        Files.exists(store.getBuildScanFile(artifactsFolder))
    }

    @Unroll
    def "build scans are stored in text file line by line"() {
        given:
        SBuild build = Mock()

        when:
        urls.each { scanUrl ->
            store.store(build, scanUrl)
        }

        then:
        build.artifactsDirectory >> artifactsFolder
        store.getBuildScanFile(artifactsFolder).readLines() == urls

        where:
        urls << [['http://gradle.com/s/1'], ['http://gradle.com/s/1', 'http://gradle.com/s/2']]

    }

    @Unroll
    def "fetch build scan references contain previously persisted build scan urls"() {
        given:
        SBuild build = Mock()

        when:
        urls.each { scanUrl ->
            store.store(build, scanUrl)
        }
        def scanReferences = store.fetch(build)

        then:
        build.artifactsDirectory >> artifactsFolder
        scanReferences.collect { it.url } == urls

        where:
        urls << [['http://gradle.com/s/1'], ['http://gradle.com/s/1', 'http://gradle.com/s/2']]

    }
}