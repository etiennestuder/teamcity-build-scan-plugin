<p align="left">
  <a href="https://github.com/etiennestuder/teamcity-build-scan-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/teamcity-build-scan-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

teamcity-build-scan-plugin
==========================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Overview

[TeamCity](https://www.jetbrains.com/teamcity/) plugin that integrates with build scans for Gradle and Maven. Build scans are available as a free service on [scans.gradle.com](https://scans.gradle.com/) and commercially via [Gradle Enterprise](https://gradle.com/enterprise).

For each Gradle and Maven build that is run from TeamCity, this plugin exposes the links to the created build scans in the TeamCity UI.

The plugin is hosted at [Bintray's JCenter](https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin).

## Build scan

Recent build scan: https://scans.gradle.com/s/cv4z5js6g6xga

Find out more about build scans for Gradle and Maven at https://scans.gradle.com.

## Version compatibility

This plugin requires that you use at least Gradle Enterprise Gradle plugin 3.0 or Gradle Build Scan plugin 1.8 in your Gradle builds, or Gradle Enterprise Maven extension 1.0 in your Maven builds.

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

1. Download the plugin `.zip` file from [https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin](https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin) (see _Downloads_ section at the bottom of the page).

1. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and click on the link _Upload plugin zip_ to install the
previously downloaded plugin `.zip` file.

1. Restart TeamCity.

1. Trigger your Gradle builds with build scans enabled.

1. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

## Slack Integration

1. In Slack, create a webhook and keep track of the created URL.

1. In TeamCity, on the build configuration for which you want to be notified about published build scans, create a configuration parameter with name `BUILD_SCAN_SLACK_WEBHOOK_URL` and the value being the URL of the webhook created in step #1.

1. Trigger your Gradle builds with build scans enabled.

1. Find a notification about the published build scans in the Slack channel configured in the webhook.

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements
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
