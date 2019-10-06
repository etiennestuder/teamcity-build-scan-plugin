# teamcity-build-scan-plugin

[TeamCity](https://www.jetbrains.com/teamcity/) plugin that integrates with [build scans](https://scans.gradle.com/), the free service offered by [Gradle Inc.](https://gradle.com)

For each Gradle and Maven build that is run from TeamCity, this plugin exposes the link to the created build scan in the TeamCity UI.

## Version compatibility

This plugin requires that you use at least Gradle Build Scan plugin 1.8 in your Gradle builds, or Gradle Enterprise Maven extension 1.0 in your Maven builds. It is recommended that you use the very [latest Gradle Build Scan plugin version](https://plugins.gradle.org/plugin/com.gradle.build-scan) and the very [latest Gradle Enterprise Maven extension version](https://search.maven.org/search?q=a:gradle-enterprise-maven-extension) at all times to get the most insights from your builds.

## TeamCity build runner requirements

When using TeamCity's GradleRunner to launch Gradle builds, there is nothing special to do.

When not using TeamCity's GradleRunner to launch Gradle builds, use the [TeamCity build scan Gradle plugin](https://github.com/etiennestuder/gradle-build-scan-teamcity-plugin) to 
notify TeamCity about the build scans that were published while running a build. If you use the GradleRunner to launch Gradle builds, there is no need to apply that TeamCity build scan Gradle plugin. 

When using TeamCity's MavenRunner to launch Maven builds, there is nothing special to do.

When not using TeamCity's MavenRunner to launch Maven builds, no build scans are captured.

# Installation

1. Download the plugin .zip file from [https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin](https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin) (see _Downloads_ section at the bottom of the page).

1. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and click on the link _Upload plugin zip_ to install the 
previously downloaded plugin .zip file.

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

+ [autonomousapps](https://github.com/autonomousapps) (pr #9 that provides build scans for Maven builds)
+ [mark-vieira](https://github.com/mark-vieira) (pr #6 that provides message service functionality)
+ [pavelsher](https://github.com/pavelsher) (several code pointers)

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
