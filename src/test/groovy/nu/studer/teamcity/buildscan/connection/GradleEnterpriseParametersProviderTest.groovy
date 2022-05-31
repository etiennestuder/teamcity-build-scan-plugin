package nu.studer.teamcity.buildscan.connection


import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider
import spock.lang.Specification
import spock.lang.Unroll

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.*

class GradleEnterpriseParametersProviderTest extends Specification {

    BuildParametersProvider buildParametersProvider

    Map<String, String> descriptorParams

    SBuild sBuild
    SBuildType sBuildType
    SProject sProject
    SProjectFeatureDescriptor sProjectFeatureDescriptor

    void setup() {
        buildParametersProvider = new GradleEnterpriseParametersProvider()

        descriptorParams = [(OAuthConstants.OAUTH_TYPE_PARAM): GRADLE_ENTERPRISE_CONNECTION_PROVIDER]

        sBuild = Stub()
        sBuildType = Stub()
        sProject = Stub()
        sProjectFeatureDescriptor = Stub()

        sBuild.getBuildType() >> sBuildType
        sBuildType.getProject() >> sProject
        sProject.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE) >> [sProjectFeatureDescriptor]
        sProjectFeatureDescriptor.getParameters() >> descriptorParams
    }

    def "returns no elements when no buildType is defined"() {
        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        sBuild.getBuildType() >> null
        parameters.isEmpty()
    }

    def "returns no elements when no OAuth providers found"() {
        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        sProject.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE) >> []
        parameters.isEmpty()
    }

    def "returns no elements when no matching OAuth provider found"() {
        given:
        descriptorParams[OAuthConstants.OAUTH_TYPE_PARAM] = "invalid"

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.isEmpty()
    }

    @Unroll
    def "sets #configParam config param when #descriptorParam descriptor param is set"(String descriptorParam, String configParam, String value) {
        given:
        descriptorParams[descriptorParam] = value

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        parameters.get(configParam) == value

        where:
        descriptorParam                           | configParam                                            | value
        GRADLE_ENTERPRISE_URL                     | GRADLE_ENTERPRISE_URL_CONFIG_PARAM                     | 'https://ge.example.com'
        GRADLE_ENTERPRISE_ACCESS_KEY              | GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR                   | 'ge.example.com=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
        GRADLE_ENTERPRISE_PLUGIN_VERSION          | GRADLE_ENTERPRISE_PLUGIN_VERSION_CONFIG_PARAM          | '3.10.1'
        COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION    | COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION_CONFIG_PARAM    | '1.7'
        GRADLE_ENTERPRISE_EXTENSION_VERSION       | GRADLE_ENTERPRISE_EXTENSION_VERSION_CONFIG_PARAM       | '1.14.1'
        COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION | COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION_CONFIG_PARAM | '1.10.1'
    }

    def "gets configuration from first descriptor"() {
        given:
        def value = 'https://ge.example.com'
        descriptorParams[GRADLE_ENTERPRISE_URL] = value

        def sProjectFeatureDescriptor2 = Stub(SProjectFeatureDescriptor)
        def descriptorParams2 = [
                (OAuthConstants.OAUTH_TYPE_PARAM): GRADLE_ENTERPRISE_CONNECTION_PROVIDER,
                (GRADLE_ENTERPRISE_URL): 'https://ge.example.invalid',
        ]
        sProjectFeatureDescriptor2.getParameters() >> descriptorParams2

        when:
        def parameters = buildParametersProvider.getParameters(sBuild, false)

        then:
        sProject.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE) >> [sProjectFeatureDescriptor, sProjectFeatureDescriptor2]
        parameters.get(GRADLE_ENTERPRISE_URL_CONFIG_PARAM) == value
    }

}
