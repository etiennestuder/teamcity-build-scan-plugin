package nu.studer.teamcity.buildscan.connection

import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor
import spock.lang.Specification
import spock.lang.Unroll

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_GE_EXTENSION_COORDINATES
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP

class GradleEnterpriseConnectionProviderTest extends Specification {

    OAuthProvider connectionProvider

    void setup() {
        connectionProvider = new GradleEnterpriseConnectionProvider(Stub(PluginDescriptor))
    }

    @Unroll
    def "default version of #key is set"(String key) {
        when:
        def defaultProperties = connectionProvider.getDefaultProperties()

        then:
        defaultProperties.containsKey(key)

        where:
        key << [
            GE_PLUGIN_VERSION,
            CCUD_PLUGIN_VERSION,
            GE_EXTENSION_VERSION,
            CCUD_EXTENSION_VERSION
        ]
    }

    @Unroll
    def "description includes value of #parameter"(String parameter, String value, String text) {
        given:
        OAuthConnectionDescriptor connection = Stub()
        connection.getParameters() >> [(parameter): value]

        when:
        def description = connectionProvider.describeConnection(connection)

        then:
        description.contains("$text: $value")

        where:
        parameter                          | value                           | text
        GRADLE_PLUGIN_REPOSITORY_URL       | 'https://plugins.example.com'   | 'Gradle Plugin Repository URL'
        GRADLE_ENTERPRISE_URL              | 'https://ge.example.com'        | 'Gradle Enterprise Server URL'
        ALLOW_UNTRUSTED_SERVER             | 'true'                          | 'Allow Untrusted Server'
        GE_PLUGIN_VERSION                  | '3.10.2'                        | 'Gradle Enterprise Gradle Plugin Version'
        CCUD_PLUGIN_VERSION                | '1.7.2'                         | 'Common Custom User Data Gradle Plugin Version'
        GE_EXTENSION_VERSION               | '1.14.3'                        | 'Gradle Enterprise Maven Extension Version'
        CCUD_EXTENSION_VERSION             | '1.10.1'                        | 'Common Custom User Data Maven Extension Version'
        CUSTOM_GE_EXTENSION_COORDINATES    | 'com.company:my-ge-extension'   | 'Gradle Enterprise Maven Extension Custom Coordinates'
        CUSTOM_CCUD_EXTENSION_COORDINATES  | 'com.company:my-ccud-extension' | 'Common Custom User Data Maven Extension Custom Coordinates'
        INSTRUMENT_COMMAND_LINE_BUILD_STEP | 'true'                          | 'Instrument Command Line Build Steps'
    }

}
