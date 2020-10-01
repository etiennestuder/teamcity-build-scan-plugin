package com.gradle.enterprise.gradleplugin

import org.gradle.api.Action

interface GradleEnterpriseExtension {

    BuildScanExtension getBuildScan()

    void buildScan(Action<? super BuildScanExtension> action)

}
