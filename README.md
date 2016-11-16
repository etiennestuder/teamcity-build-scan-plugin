# teamcity-build-scan-plugin

[TeamCity](https://www.jetbrains.com/teamcity/) plugin that integrates with the Gradle Build Scan Service, the first service offered by [Gradle Cloud Services](https://gradle.com).

For each Gradle build that is run from TeamCity, this plugin exposes the link to the created build scan.

# Installation

1. Download the plugin .zip file from [https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin](https://bintray.com/etienne/teamcity-plugins/teamcity-build-scan-plugin) (see _Downloads_ section at the bottom of the page).

1. Go to the plugin list of your TeamCity installation at `<TeamCityInstanceRootUrl>/admin/admin.html?item=plugins` and click on the _Upload plugin zip_ link.

1. Restart TeamCity.

1. Trigger your Gradle builds with build scans enabled.

1. Find the links of the published build scans in the _Overview_ section of each TeamCity build.

# Feedback and Contributions

Both feedback and contributions are very welcome.

# Acknowledgements

Thanks to Pavel Sher from JetBrains for all the code pointers.

# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

(c) by Etienne Studer
