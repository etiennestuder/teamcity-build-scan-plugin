package com.gradle.enterprise.gradleplugin

import org.gradle.api.Action

import javax.inject.Inject

class DefaultBuildScanExtension implements BuildScanExtension {

    private final List<Action<? super PublishedBuildScan>> buildScanPublishedActions

    @Inject
    DefaultBuildScanExtension() {
        this.buildScanPublishedActions = []
    }

    @Override
    void buildScanPublished(Action<? super PublishedBuildScan> action) {
        buildScanPublishedActions.add(action)
    }

    @Override
    void buildFinished() {
        def publishedBuildScan = new PublishedBuildScan() {
            @Override
            String getBuildScanId() {
                'buildScanID'
            }

            @Override
            URI getBuildScanUri() {
                URI.create('https://server.com/' + buildScanId)
            }
        }
        buildScanPublishedActions.each {
            it.execute(publishedBuildScan)
        }
    }

}
