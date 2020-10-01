package com.gradle.enterprise.gradleplugin

import org.gradle.api.Action

interface BuildScanExtension {

    void buildScanPublished(Action<? super PublishedBuildScan> action)
    
    void buildFinished()
    
}
