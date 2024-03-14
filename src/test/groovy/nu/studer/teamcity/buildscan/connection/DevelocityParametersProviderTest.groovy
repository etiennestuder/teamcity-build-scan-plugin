package nu.studer.teamcity.buildscan.connection

import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider
import spock.lang.Specification
import spock.lang.Unroll

import static DevelocityConnectionConstants.ALLOW_UNTRUSTED_SERVER
import static DevelocityConnectionConstants.ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM
import static DevelocityConnectionConstants.CCUD_EXTENSION_VERSION
import static DevelocityConnectionConstants.CCUD_EXTENSION_VERSION_CONFIG_PARAM
import static DevelocityConnectionConstants.CCUD_PLUGIN_VERSION
import static DevelocityConnectionConstants.CCUD_PLUGIN_VERSION_CONFIG_PARAM
import static DevelocityConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES
import static DevelocityConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM
import static DevelocityConnectionConstants.CUSTOM_DEVELOCITY_EXTENSION_COORDINATES
import static DevelocityConnectionConstants.CUSTOM_DEVELOCITY_EXTENSION_COORDINATES_CONFIG_PARAM
import static DevelocityConnectionConstants.DEVELOCITY_EXTENSION_VERSION
import static DevelocityConnectionConstants.DEVELOCITY_EXTENSION_VERSION_CONFIG_PARAM
import static DevelocityConnectionConstants.DEVELOCITY_PLUGIN_VERSION
import static DevelocityConnectionConstants.DEVELOCITY_PLUGIN_VERSION_CONFIG_PARAM
import static DevelocityConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY
import static DevelocityConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR
import static DevelocityConnectionConstants.DEVELOCITY_CONNECTION_PROVIDER
import static DevelocityConnectionConstants.DEVELOCITY_URL
import static DevelocityConnectionConstants.DEVELOCITY_URL_CONFIG_PARAM
import static DevelocityConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL
import static DevelocityConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM
import static DevelocityConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP
import static DevelocityConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM
import static DevelocityConnectionConstants.ENFORCE_DEVELOCITY_URL
import static DevelocityConnectionConstants.ENFORCE_DEVELOCITY_URL_CONFIG_PARAM

@Unroll
class DevelocityParametersProviderTest extends Specification {

    BuildParametersProvider buildParametersProvider

    Map<String, String> descriptorParams
    Map<String, String> higherDescriptorParams

    SBuild sBuild
    SBuildType sBuildType
    SProject sProject
    SProjectFeatureDescriptor sProjectFeatureDescriptor
    SProjectFeatureDescriptor higherProjectFeatureDescriptor

    void setup() {
        buildParametersProvider = new DevelocityParametersProvider()

        descriptorParams = [(OAuthConstants.OAUTH_TYPE_PARAM): DEVELOCITY_CONNECTION_PROVIDER]
        higherDescriptorParams = [(OAuthConstants.OAUTH_TYPE_PARAM): DEVELOCITY_CONNECTION_PROVIDER]

        sBuild = Stub()
        sBuildType = Stub()
        sProject = Stub()
        sProjectFeatureDescriptor = Stub()
        higherProjectFeatureDescriptor = Stub()

        sBuild.getBuildType() >> sBuildType
        sBuildType.getProject() >> sProject
        sProject.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE) >> [sProjectFeatureDescriptor, higherProjectFeatureDescriptor]
        sProjectFeatureDescriptor.getParameters() >> descriptorParams
        higherProjectFeatureDescriptor.getParameters() >> higherDescriptorParams
    }

    def "returns no elements when no buildType is defined"() {
        given:
        sBuild.getBuildType() >> null

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.isEmpty()
    }

    def "returns no elements when no OAuth providers found"() {
        given:
        sProject.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE) >> []

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.isEmpty()
    }

    def "returns no elements when no matching OAuth provider found"() {
        given:
        descriptorParams[OAuthConstants.OAUTH_TYPE_PARAM] = "other"

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.isEmpty()
    }

    def "sets #configParam config param when #descriptorParam descriptor param is set"() {
        given:
        descriptorParams[descriptorParam] = value

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(configParam) == value

        where:
        descriptorParam                    | configParam                                     | value
        GRADLE_PLUGIN_REPOSITORY_URL       | GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM       | 'https://plugins.example.com'
        DEVELOCITY_URL | DEVELOCITY_URL_CONFIG_PARAM | 'https://develocity.example.com'
        ALLOW_UNTRUSTED_SERVER             | ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM             | 'true'
        ENFORCE_DEVELOCITY_URL | ENFORCE_DEVELOCITY_URL_CONFIG_PARAM | 'true'
        DEVELOCITY_PLUGIN_VERSION | DEVELOCITY_PLUGIN_VERSION_CONFIG_PARAM | '1.0.0'
        CCUD_PLUGIN_VERSION                | CCUD_PLUGIN_VERSION_CONFIG_PARAM                | '1.0.0'
        DEVELOCITY_EXTENSION_VERSION       | DEVELOCITY_EXTENSION_VERSION_CONFIG_PARAM | '1.0.0'
        CCUD_EXTENSION_VERSION             | CCUD_EXTENSION_VERSION_CONFIG_PARAM             | '1.0.0'
        CUSTOM_DEVELOCITY_EXTENSION_COORDINATES | CUSTOM_DEVELOCITY_EXTENSION_COORDINATES_CONFIG_PARAM | '1.0.0'
        CUSTOM_CCUD_EXTENSION_COORDINATES  | CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM  | '1.0.0'
        INSTRUMENT_COMMAND_LINE_BUILD_STEP | INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM | 'true'
        GRADLE_ENTERPRISE_ACCESS_KEY       | GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR            | 'develocity.example.com=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
    }

    def "gets configuration from first descriptor"() {
        given:
        def value = 'https://develocity.example.com'
        descriptorParams[DEVELOCITY_URL] = value
        higherDescriptorParams[DEVELOCITY_URL] = 'https://develocity.example.invalid'

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(DEVELOCITY_URL_CONFIG_PARAM) == value
    }

    def "inherits configuration parameter from last descriptor when not set in first descriptor"() {
        given:
        def value = 'https://develocity.example.com'
        higherDescriptorParams[DEVELOCITY_URL] = value

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(DEVELOCITY_URL_CONFIG_PARAM) == value
    }

    def "inherits configuration parameter from last descriptor when set to null in first descriptor"() {
        given:
        def value = 'https://develocity.example.com'
        descriptorParams[DEVELOCITY_URL] = null
        higherDescriptorParams[DEVELOCITY_URL] = value

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(DEVELOCITY_URL_CONFIG_PARAM) == value
    }

}
