package nu.studer.teamcity.buildscan.internal

import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.util.Calculator
import jetbrains.buildServer.util.cache.CacheProvider
import jetbrains.buildServer.util.cache.SCache
import nu.studer.teamcity.buildscan.BuildScanLookup
import nu.studer.teamcity.buildscan.BuildScanReference
import nu.studer.teamcity.buildscan.BuildScanReferences
import spock.lang.Specification

class CachedBuildScanLookupTest extends Specification {

    def "cache is only queried iff build has already finished"() {
        given:
        SCache cache = Stub(SCache)
        CacheProvider cacheProvider = Stub(CacheProvider)
        cacheProvider.getOrCreateCache(_ as String, _ as Class) >> cache

        CachedBuildScanLookup lookup = new CachedBuildScanLookup(cacheProvider, Stub(BuildScanLookup))

        and:
        BuildScanReference buildScanReference = new BuildScanReference('someScanId', 'someScanUrl')
        cache.fetch(_ as String, _ as Calculator) >> BuildScanReferences.of(buildScanReference)

        and:
        SBuild sbuild = Stub(SBuild)
        sbuild.isFinished() >> true

        when:
        def buildScans = lookup.getBuildScansForBuild(sbuild)

        then:
        buildScans.all() == [buildScanReference]
    }

    def "cache is not queried if build has not yet finished"() {
        given:
        CacheProvider cacheProvider = Stub(CacheProvider)
        cacheProvider.getOrCreateCache(_ as String, _ as Class) >> Stub(SCache)

        BuildScanLookup delegateLookup = Stub(BuildScanLookup)

        CachedBuildScanLookup lookup = new CachedBuildScanLookup(cacheProvider, delegateLookup)

        and:
        BuildScanReference buildScanReference = new BuildScanReference('someScanId', 'someScanUrl')
        delegateLookup.getBuildScansForBuild(_ as SBuild) >> { BuildScanReferences.of(buildScanReference) }

        and:
        SBuild sbuild = Stub(SBuild)
        sbuild.isFinished() >> false

        when:
        def buildScans = lookup.getBuildScansForBuild(sbuild)

        then:
        buildScans.all() == [buildScanReference]
    }

}
