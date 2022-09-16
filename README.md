<p align="left">
  <a href="https://github.com/etiennestuder/teamcity-build-scan-plugin/actions?query=workflow%3A%22Build+Gradle+project%22"><img src="https://github.com/etiennestuder/teamcity-build-scan-plugin/workflows/Build%20Gradle%20project/badge.svg"></a>
</p>

teamcity-build-scan-plugin
==========================

> The work on this software project is in no way associated with my employer nor with the role I'm having at my employer. Any requests for changes will be decided upon exclusively by myself based on my personal preferences. I maintain this project as much or as little as my spare time permits.

# Overview

[TeamCity](https://www.jetbrains.com/teamcity/) plugin that integrates with Build Scan for Gradle and Maven. Build scans are available as a free service on [scans.gradle.com](https://scans.gradle.com/) and commercially via [Gradle Enterprise](https://gradle.com/enterprise).

For each Gradle and Maven build that is run from TeamCity, this plugin exposes the links to the created build scans in the TeamCity UI. The plugin can also be configured to instrument Gradle and Maven with Gradle Enterprise.

The plugin is available from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/9326-integration-for-gradle-and-maven-build-scans).

## Build Scan

Recent Build Scan: https://gradle.com/s/yevfzaz2bvvh4

Find out more about Build Scan for Gradle and Maven at https://scans.gradle.com and https://gradle.com.

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

If you use TeamCity's _Gradle_ runner to launch your Gradle builds, there is nothing special to do.

If you use TeamCity's _Command Line_ runner to launch your Gradle builds, you can opt in to enable build scan detection using the `buildScanPlugin.command-line-build-step.enabled` configuration parameter.

If the first two mechanisms will not work for your Gradle build configurations, you can still get integration with build scans, but it requires your build logs being parsed for build scan links. In case of huge build logs, this can put a significant toll on the performance of your TeamCity instance. You can enable the parsing of the build logs using the `buildScanPlugin.log-parsing.enabled` configuration paramter.

### Maven builds

If you use TeamCity's _Maven_ runner to launch Maven builds, there is nothing special to do.

If you use TeamCity's _Command Line_ runner to launch your Maven builds, you can opt in to enable build scan detection using the `buildScanPlugin.command-line-build-step.enabled` configuration parameter.

If the first two mechanisms will not work for your Maven build configurations, you can still get integration with build scans, but it requires your build logs being parsed for build scan links. In case of huge build logs, this can put a significant toll on the performance of your TeamCity instance. You can enable the parsing of the build logs using the `buildScanPlugin.log-parsing.enabled` configuration paramter.

# Installation

## Option 1: Conveniently select plugin

1. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and browse the plugins repository.
2. Select, install, and activate the plugin as described [here](https://www.jetbrains.com/help/teamcity/installing-additional-plugins.html#Installing+a+plugin+from+JetBrains+Plugins+repository).

## Option 2: Manually upload plugin

1. Download the plugin `.zip` file from [https://plugins.jetbrains.com/plugin/9326-integration-for-gradle-and-maven-build-scans](https://plugins.jetbrains.com/plugin/9326-integration-for-gradle-and-maven-build-scans).

2. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and click on the link _Upload plugin zip_ to install the
previously downloaded plugin `.zip` file.

3. Restart TeamCity.

4. Trigger your Gradle builds with build scans enabled.

5. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

# Integrations

## Gradle Enterprise

You can have the [Gradle Enterprise Gradle plugin](https://docs.gradle.com/enterprise/gradle-plugin/) and the [Gradle Enterprise Maven extension](https://docs.gradle.com/enterprise/maven-extension/) automatically injected into your Gradle and Maven builds when the builds are run via TeamCity's _Gradle_ or _Maven_ runner. If a Gradle or Maven build is run via TeamCity's _Command Line_ runner the auto-injection can be opted in to. If a given build is already connected to Gradle Enterprise, the auto-injection is skipped.

The same auto-injection behavior is available for the [Common Custom User Data Gradle plugin](https://github.com/gradle/common-custom-user-data-gradle-plugin) and the [Common Custom User Data Maven extension](https://github.com/gradle/common-custom-user-data-maven-extension).

The higher in TeamCity's project hierarchy the required configuration parameters are applied, the more widely they apply since the configuration parameters are passed on to all child projects. Child projects can override the configuration parameters and even disable the auto-injection by setting the appropriate configuration parameters to empty values.

For convenience, the configuration parameter values can be defined through a Gradle Enterprise connection, as explained below.

### Creating a Gradle Enterprise Connection

A Gradle Enterprise connection is created in the _Connections_ section of the configuration of a given project. In the Add Connection dropdown, select the Gradle Enterprise connection type.

Fill out the Add Connection dialog with the URL for the Gradle Enterprise instance, any plugin or extension versions to be applied, and any other fields in the dialog as needed. Some values, such as the plugin and extension versions, will be pre-populated.

A Gradle Enterprise connection can be created on any project and is automatically inherited by all its child projects.

_Note: For Gradle, the Common Custom User Data Gradle plugin must be at least version 1.7 or newer._

_Note: For Maven, the Gradle Enterprise Maven extension and the Common Custom User Data Maven extension are currently hard-coded to versions 1.15.2 and 1.11.1, respectively._

#### Example Configuration

<img width="591" alt="gradle-enterprise-connection-dialog" src="https://user-images.githubusercontent.com/625514/190648291-0315b67a-e0bf-4c78-b54c-77a2c5858aa2.png">

### Injecting Gradle Enterprise via Configuration Parameters

<details>

<summary>It is possible to inject Gradle Enterprise by manually setting configuration parameters. Click for more details.</summary>

#### Gradle Builds

1. In TeamCity, on the build configuration for which you want to apply Gradle Enterprise, create three configuration parameters:

   - `buildScanPlugin.gradle-enterprise.url` - the URL of the Gradle Enterprise instance to which to publish the Build Scan
   - `buildScanPlugin.gradle-enterprise.plugin.version` - the version of the [Gradle Enterprise Gradle plugin](https://docs.gradle.com/enterprise/gradle-plugin/) to apply
   - `buildScanPlugin.ccud.plugin.version` - the version of the [Common Custom User Data Gradle plugin](https://github.com/gradle/common-custom-user-data-gradle-plugin) to apply (optional)

2. If required, provide additional configuration parameters for your environment (optional):

    - `buildScanPlugin.gradle-enterprise.allow-untrusted-server` - allow communication with an untrusted server; set to _true_ if your Gradle Enterprise instance is using a self-signed certificate
    - `buildScanPlugin.gradle.plugin-repository.url` - the URL of the repository to use when resolving the GE and CCUD plugins; required if your TeamCity agents are not able to access the Gradle Plugin Portal
    - `buildScanPlugin.command-line-build-step.enabled` - enable Gradle Enterprise integration for _Command Line_ build steps; by default only steps using the _Gradle_ runner are enabled
    - `buildScanPlugin.log-parsing.enabled` - use log parsing to extract Build Scan urls (if the default mechanism for capturing Build Scan links is not working)

3. Trigger your Gradle build.

4. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

_Note: For Gradle, the Common Custom User Data Gradle plugin must be at least version 1.7 or newer._

##### Example Gradle Configuration

<img width="1302" alt="image" src="https://user-images.githubusercontent.com/231070/171748029-590c9f99-9382-41f3-8597-8c049ce328ed.png">

#### Maven Builds

1. In TeamCity, on the build configuration for which you want to integrate Gradle Enterprise, create three configuration parameters:

   - `buildScanPlugin.gradle-enterprise.url` - the URL of the Gradle Enterprise instance to which to publish the Build Scan
   - `buildScanPlugin.gradle-enterprise.extension.version` - the version of the [Gradle Enterprise Maven extension](https://docs.gradle.com/enterprise/maven-extension/) to apply
   - `buildScanPlugin.ccud.extension.version` - the version of the [Common Custom User Data Maven extension](https://github.com/gradle/common-custom-user-data-maven-extension) to apply (optional)

2. If required, provide additional configuration parameters for your environment (optional):

    - `buildScanPlugin.gradle-enterprise.allow-untrusted-server` - allow communication with an untrusted server; set to _true_ if your Gradle Enterprise instance is using a self-signed certificate
    - `buildScanPlugin.gradle-enterprise.extension.custom.coordinates` - the coordinates of a custom extension that has a transitive dependency on the Gradle Enterprise Maven Extension
    - `buildScanPlugin.ccud.extension.custom.coordinates` - the coordinates of a custom Common Custom User Data Maven Extension or of a custom extension that has a transitive dependency on it
    - `buildScanPlugin.command-line-build-step.enabled` - enable Gradle Enterprise integration for _Command Line_ build steps; by default only steps using the _Maven_ runner are enabled
    - `buildScanPlugin.log-parsing.enabled` - use log parsing to extract Build Scan urls (if the default mechanism for capturing Build Scan links is not working)
    - `buildScanPlugin.maven-version-check.enabled` - enable a Maven version check to ensure that the Gradle Enterprise Maven Extension is not applied to Maven builds lower than version 3.3.1

3. Trigger your Maven build.

4. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

_Note: For Maven, the Gradle Enterprise Maven extension and the Common Custom User Data Maven extension are currently hard-coded to versions 1.15.2 and 1.11.1, respectively._

##### Example Maven Configuration

<img width="1298" alt="image" src="https://user-images.githubusercontent.com/231070/171748453-243aa862-c31b-4367-bb90-49b257de1f3f.png">

</details>

## Slack

1. In Slack, create a webhook and keep track of the created URL.

2. In TeamCity, on the build configuration for which you want to be notified about published build scans, create a configuration parameter with name `buildScanPlugin.slack-webhook.url` and the value being the URL of the webhook created in step #1.

3. Trigger your Gradle builds with build scans enabled.

4. Find a notification about the published build scans in the Slack channel configured in the webhook.

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements
+ [bigdaz](https://github.com/bigdaz) (several prs related to the Gradle Enterprise integration)
+ [clayburn](https://github.com/clayburn) (several prs related to the Gradle Enterprise integration)
+ [marcphilipp](https://github.com/marcphilipp) (pr #53 that adds Gradle 4/5 compatibility to the Gradle Enterprise integration)
+ [ldaley](https://github.com/ldaley) (pr #53 that adds Gradle 4/5 compatibility to the Gradle Enterprise integration)
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
