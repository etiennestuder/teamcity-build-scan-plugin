package nu.studer.teamcity.buildscan.connection

import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor
import spock.lang.Specification
import spock.lang.Unroll

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.*

class GradleEnterpriseConnectionProviderTest extends Specification {

    OAuthProvider connectionProvider

    void setup() {
        connectionProvider = new GradleEnterpriseConnectionProvider(Stub(PluginDescriptor))
    }

    @Unroll
    def "default version of #key is #value"(String key, String value) {
        when:
        def defaultProperties = connectionProvider.getDefaultProperties()

        then:
        defaultProperties.get(key) == value

        where:
        key                                       | value
        GRADLE_ENTERPRISE_PLUGIN_VERSION          | '3.10.1'
        COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION    | '1.7'
        GRADLE_ENTERPRISE_EXTENSION_VERSION       | '1.14.1'
        COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION | '1.10.1'
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
        parameter                                 | value    | text
        GRADLE_ENTERPRISE_URL                     | '3.10.1' | 'Gradle Enterprise Url'
        GRADLE_ENTERPRISE_PLUGIN_VERSION          | '3.10.1' | 'Gradle Enterprise Plugin Version'
        COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION    | '1.7'    | 'Common Custom User Data Plugin Version'
        GRADLE_ENTERPRISE_EXTENSION_VERSION       | '1.14.1' | 'Gradle Enterprise Extension Version'
        COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION | '1.10.1' | 'Common Custom User Data Extension Version'
    }

}
