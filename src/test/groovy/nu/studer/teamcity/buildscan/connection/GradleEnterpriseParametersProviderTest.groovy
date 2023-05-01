package nu.studer.teamcity.buildscan.connection

import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider
import spock.lang.Specification
import spock.lang.Unroll

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_GE_EXTENSION_COORDINATES
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_GE_EXTENSION_COORDINATES_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_CONNECTION_PROVIDER
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.OVERRIDE_EXISTING_SERVER
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.OVERRIDE_EXISTING_SERVER_URL_CONFIG_PARAM

@Unroll
class GradleEnterpriseParametersProviderTest extends Specification {

    BuildParametersProvider buildParametersProvider

    Map<String, String> descriptorParams
    Map<String, String> higherDescriptorParams

    SBuild sBuild
    SBuildType sBuildType
    SProject sProject
    SProjectFeatureDescriptor sProjectFeatureDescriptor
    SProjectFeatureDescriptor higherProjectFeatureDescriptor

    void setup() {
        buildParametersProvider = new GradleEnterpriseParametersProvider()

        descriptorParams = [(OAuthConstants.OAUTH_TYPE_PARAM): GRADLE_ENTERPRISE_CONNECTION_PROVIDER]
        higherDescriptorParams = [(OAuthConstants.OAUTH_TYPE_PARAM): GRADLE_ENTERPRISE_CONNECTION_PROVIDER]

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
        GRADLE_ENTERPRISE_URL              | GRADLE_ENTERPRISE_URL_CONFIG_PARAM              | 'https://ge.example.com'
        ALLOW_UNTRUSTED_SERVER             | ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM             | 'true'
        OVERRIDE_EXISTING_SERVER           | OVERRIDE_EXISTING_SERVER_URL_CONFIG_PARAM       | 'true'
        GE_PLUGIN_VERSION                  | GE_PLUGIN_VERSION_CONFIG_PARAM                  | '1.0.0'
        CCUD_PLUGIN_VERSION                | CCUD_PLUGIN_VERSION_CONFIG_PARAM                | '1.0.0'
        GE_EXTENSION_VERSION               | GE_EXTENSION_VERSION_CONFIG_PARAM               | '1.0.0'
        CCUD_EXTENSION_VERSION             | CCUD_EXTENSION_VERSION_CONFIG_PARAM             | '1.0.0'
        CUSTOM_GE_EXTENSION_COORDINATES    | CUSTOM_GE_EXTENSION_COORDINATES_CONFIG_PARAM    | '1.0.0'
        CUSTOM_CCUD_EXTENSION_COORDINATES  | CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM  | '1.0.0'
        INSTRUMENT_COMMAND_LINE_BUILD_STEP | INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM | 'true'
        GRADLE_ENTERPRISE_ACCESS_KEY       | GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR            | 'ge.example.com=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
    }

    def "gets configuration from first descriptor"() {
        given:
        def value = 'https://ge.example.com'
        descriptorParams[GRADLE_ENTERPRISE_URL] = value
        higherDescriptorParams[GRADLE_ENTERPRISE_URL] = 'https://ge.example.invalid'

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(GRADLE_ENTERPRISE_URL_CONFIG_PARAM) == value
    }

    def "inherits configuration parameter from last descriptor when not set in first descriptor"() {
        given:
        def value = 'https://ge.example.com'
        higherDescriptorParams[GRADLE_ENTERPRISE_URL] = value

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(GRADLE_ENTERPRISE_URL_CONFIG_PARAM) == value
    }

    def "inherits configuration parameter from last descriptor when set to null in first descriptor"() {
        given:
        def value = 'https://ge.example.com'
        descriptorParams[GRADLE_ENTERPRISE_URL] = null
        higherDescriptorParams[GRADLE_ENTERPRISE_URL] = value

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(GRADLE_ENTERPRISE_URL_CONFIG_PARAM) == value
    }

}
