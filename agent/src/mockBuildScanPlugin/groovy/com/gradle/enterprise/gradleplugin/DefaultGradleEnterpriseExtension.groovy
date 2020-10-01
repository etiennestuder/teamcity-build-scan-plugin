package com.gradle.enterprise.gradleplugin

import org.gradle.api.Action

import javax.inject.Inject

class DefaultGradleEnterpriseExtension implements GradleEnterpriseExtension {

    private final BuildScanExtension buildScanExtension

    @Inject
    DefaultGradleEnterpriseExtension(BuildScanExtension buildScanExtension) {
        this.buildScanExtension = buildScanExtension
    }

    @Override
    BuildScanExtension getBuildScan() {
        buildScanExtension
    }

    @Override
    void buildScan(Action<? super BuildScanExtension> action) {
        action.execute(getBuildScan())
    }

}
