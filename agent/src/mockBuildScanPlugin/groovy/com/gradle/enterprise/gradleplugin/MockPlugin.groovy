package com.gradle.enterprise.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.service.ServiceRegistry

class MockPlugin implements Plugin<Object> {

    @Override
    void apply(Object object) {
        if (object instanceof Settings) {
            doApplySettings((Settings) object)
        } else {
            doApplyProject((Project) object)
        }
    }

    static void doApplySettings(Settings settings) {
        boolean pluginAlreadyApplied = pluginAlreadyApplied(settings)
        GradleInternal gradleInternal = (GradleInternal) settings.gradle
        def extension = createGradleEnterpriseExtension(gradleInternal.services)
        settings.extensions.add(GradleEnterpriseExtension.class, pluginAlreadyApplied ? 'gradleEnterprise2' : 'gradleEnterprise1', extension)
        gradleInternal.rootProject { Project project ->
            project.extensions.add(GradleEnterpriseExtension.class, pluginAlreadyApplied ? 'gradleEnterprise2' : 'gradleEnterprise1', extension)
            project.extensions.add(BuildScanExtension.class, pluginAlreadyApplied ? 'buildScan2' : 'buildScan1', extension.buildScan)
        }
        gradleInternal.buildFinished {
            extension.buildScan.buildFinished()
        }
    }

    static void doApplyProject(Project project) {
        GradleInternal gradleInternal = (GradleInternal) project.gradle
        def extension = createBuildScanExtension(gradleInternal.services)
        project.extensions.add(BuildScanExtension.class, 'buildScan', extension)
    }

    private static GradleEnterpriseExtension createGradleEnterpriseExtension(ServiceRegistry serviceRegistry) {
        ObjectFactory objectFactory = serviceRegistry.get(ObjectFactory.class)
        objectFactory.newInstance(
                DefaultGradleEnterpriseExtension.class,
                createBuildScanExtension(serviceRegistry)
        )
    }

    private static BuildScanExtension createBuildScanExtension(ServiceRegistry serviceRegistry) {
        ObjectFactory objectFactory = serviceRegistry.get(ObjectFactory.class)
        objectFactory.newInstance(DefaultBuildScanExtension.class)
    }

    private static boolean pluginAlreadyApplied(Settings settings) {
        return settings.getPlugins().stream().anyMatch { plugin -> plugin.getClass().getName() == MockPlugin.class.getName() }
    }

}
