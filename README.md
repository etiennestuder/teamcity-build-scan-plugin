<p align="left">
  <a href="https://github.com/etiennestuder/teamcity-build-scan-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/teamcity-build-scan-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

teamcity-build-scan-plugin
==========================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Overview

[TeamCity](https://www.jetbrains.com/teamcity/) plugin that integrates with build scans for Gradle and Maven. Build scans are available as a free service on [scans.gradle.com](https://scans.gradle.com/) and commercially via [Gradle Enterprise](https://gradle.com/enterprise).

For each Gradle and Maven build that is run from TeamCity, this plugin exposes the links to the created build scans in the TeamCity UI.

The plugin is available from the [JetBrains Plugins Repository](https://plugins.jetbrains.com/plugin/9326-integration-for-gradle-and-maven-build-scans).

## Build scan

Recent build scan: https://gradle.com/s/yevfzaz2bvvh4

Find out more about build scans for Gradle and Maven at https://scans.gradle.com.

## Version compatibility

### <=0.22
These plugin versions require that you use at least Gradle Enterprise Gradle plugin 3.0 or Gradle Build Scan plugin 1.8 in your Gradle builds, or Gradle Enterprise Maven extension 1.0 in your Maven builds.

### >=0.23
These plugin versions require that you use at least Gradle Enterprise Gradle plugin 3.0 or Gradle Build Scan plugin 1.8 in your Gradle builds, or Gradle Enterprise Maven extension 1.11 in your Maven builds.

It is recommended that you use the [latest Gradle Enterprise Gradle plugin version](https://plugins.gradle.org/plugin/com.gradle.enterprise) or
[latest Gradle Build Scan plugin version](https://plugins.gradle.org/plugin/com.gradle.build-scan) and the
[latest Gradle Enterprise Maven extension version](https://search.maven.org/search?q=a:gradle-enterprise-maven-extension) at all times to get the most insights from your builds.

## TeamCity build runner requirements

### Gradle builds

If you use TeamCity's GradleRunner to launch your Gradle builds, there is nothing special to do.

If, and only if, you do not use TeamCity's GradleRunner to launch Gradle builds, apply the [TeamCity build scan Gradle plugin](https://github.com/etiennestuder/gradle-build-scan-teamcity-plugin) to your Gradle builds in order to
notify TeamCity about the build scans that were published while running a build.

If you do not use TeamCity's GradleRunner to launch Gradle builds and you do not apply the [TeamCity build scan Gradle plugin](https://github.com/etiennestuder/gradle-build-scan-teamcity-plugin) to your Gradle builds, you can still get integration with build scans, but it requires your build logs being parsed for build scan links. In case of huge build logs, this can put a significant toll on the performance of your TeamCity instance. You can enable the parsing of the build logs by creating a TeamCity configuration parameter with name `BUILD_SCAN_LOG_PARSING` and setting the value to `true`.

### Maven builds

If you use TeamCity's MavenRunner to launch Maven builds, there is nothing special to do.

If you do not use TeamCity's MavenRunner to launch Maven builds, you can still get integration with build scans, but it requires your build logs being parsed for build scan links. In case of huge build logs, this can put a significant toll on the performance of your TeamCity instance. You can enable the parsing of the build logs by creating a TeamCity configuration parameter with name `BUILD_SCAN_LOG_PARSING` and setting the value to `true`.

# Installation

## Option 1: Conveniently select plugin

1. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and browse the plugins repository.
2. Select, install, and activate the plugin as described [here](https://www.jetbrains.com/help/teamcity/installing-additional-plugins.html#Installing+a+plugin+from+JetBrains+Plugins+repository).

## Option 2: Manually upload plugin

1. Download the plugin `.zip` file from [https://plugins.jetbrains.com/plugin/9326-integration-for-gradle-and-maven-build-scans](https://plugins.jetbrains.com/plugin/9326-integration-for-gradle-and-maven-build-scans).

1. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and click on the link _Upload plugin zip_ to install the
previously downloaded plugin `.zip` file.

1. Restart TeamCity.

1. Trigger your Gradle builds with build scans enabled.

1. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

# Integrations

## Gradle Enterprise

You can have the [Gradle Enterprise Gradle plugin](https://docs.gradle.com/enterprise/gradle-plugin/) and the [Gradle Enterprise Maven extension](https://docs.gradle.com/enterprise/maven-extension/) automatically injected into your Gradle and Maven builds when they are run via TeamCity's Gradle or Maven runner. If a given build is already connected to Gradle Enterprise, the auto-injection is skipped.

The same behavior applies to the [Common Custom User Data Gradle plugin](https://github.com/gradle/common-custom-user-data-gradle-plugin) and the [Common Custom User Data Maven extension](https://github.com/gradle/common-custom-user-data-maven-extension).

### Gradle Builds

1. In TeamCity, on the build configuration for which you want to apply Gradle Enterprise, create 3 configuration parameters:

   - `buildScanPlugin.gradle-enterprise.url` - the URL of the Gradle Enterprise instance to which you want to publish build scans.
   - `buildScanPlugin.gradle-enterprise.plugin.version` - the version of the [Gradle Enterprise Gradle plugin](https://docs.gradle.com/enterprise/gradle-plugin/) to apply to the build.
   - `buildScanPlugin.ccud.plugin.version` - the version of the [Common Custom User Data Gradle plugin](https://github.com/gradle/common-custom-user-data-gradle-plugin) to apply to the build.

1. Trigger your Gradle build.

1. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

### Maven Builds

1. In TeamCity, on the build configuration for which you want to integrate Gradle Enterprise, create 3 configuration parameters:

   - `buildScanPlugin.gradle-enterprise.url` - the URL of the Gradle Enterprise instance to which you want to publish build scans.
   - `buildScanPlugin.gradle-enterprise.extension.version` - the version of the [Gradle Enterprise Maven extension](https://docs.gradle.com/enterprise/maven-extension/) to apply to the build.
   - `buildScanPlugin.ccud.extension.version` - the version of the [Common Custom User Data Maven extension](https://github.com/gradle/common-custom-user-data-maven-extension) to apply to the build.

1. Trigger your Maven build.

1. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

_Note: The Gradle Enterprise Maven extension and the Common Custom User Data Maven extension are currently hard-coded to versions 1.14 and 1.10.1, respectively._

## Slack

1. In Slack, create a webhook and keep track of the created URL.

1. In TeamCity, on the build configuration for which you want to be notified about published build scans, create a configuration parameter with name `BUILD_SCAN_SLACK_WEBHOOK_URL` and the value being the URL of the webhook created in step #1.

1. Trigger your Gradle builds with build scans enabled.

1. Find a notification about the published build scans in the Slack channel configured in the webhook.

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements
+ [ldaley](https://github.com/ldaley) (pr #53 that adds Gradle 4/5 compatibility to the Gradle Enterprise integration)
+ [clayburn](https://github.com/clayburn) (several prs related to the Gradle Enterprise integration)
+ [madlexa](https://github.com/madlexa) (pr #47 that adds TeamCity 2022.04 compatibility)
+ [facewindu](https://github.com/facewindu) (pr #21 that includes init script test coverage)
+ [dmitry-treskunov](https://github.com/dmitry-treskunov) (bug report and proposed fix)
+ [pbielicki](https://github.com/pbielicki) (pr #17 that adds a hint to the BuildScanServiceMessageMavenExtension @Component)
+ [jonnybbb](https://github.com/jonnybbb) (pr #14 and #15 that store build scan links under artifacts and clean up legacy data)
+ [davidburstromspotify](https://github.com/davidburstromspotify) (bug report and proposed fix)
+ [guylabs](https://github.com/guylabs) (pr #10 that provides support for the Gradle Enterprise Gradle plugin)
+ [autonomousapps](https://github.com/autonomousapps) (pr #9 that provides build scans for Maven builds)
+ [mark-vieira](https://github.com/mark-vieira) (pr #6 that provides message service functionality)
+ [pavelsher](https://github.com/pavelsher) (several code pointers)

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
