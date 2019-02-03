# teamcity-build-scan-plugin

[TeamCity](https://www.jetbrains.com/teamcity/) plugin that integrates with [build scans](https://scans.gradle.com/), the free service offered by [Gradle Inc.](https://gradle.com)

For each Gradle build that is run from TeamCity, this plugin exposes the link to the created build scan in the TeamCity UI.

## Version compativility

This plugin requires that you use at least build scan plugin 1.8 in your Gradle builds. It is recommended that you use the very [latest build scan plugin version](https://plugins.gradle.org/plugin/com.gradle.build-scan) at all times to get the most insights from your builds.

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

Thanks to Pavel Sher from JetBrains for all the code pointers.

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
