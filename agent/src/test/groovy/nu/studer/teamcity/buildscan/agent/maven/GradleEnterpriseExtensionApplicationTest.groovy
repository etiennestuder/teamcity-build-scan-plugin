package nu.studer.teamcity.buildscan.agent.maven


import static org.junit.Assume.assumeTrue

class GradleEnterpriseExtensionApplicationTest extends BaseExtensionApplicationTest {

    static final String GE_URL = System.getenv('GRADLE_ENTERPRISE_TEST_INSTANCE') ?: null
    static final String GE_EXTENSION_VERSION = '1.14.3'
    static final String CCUD_EXTENSION_VERSION = '1.10.1'

    def "does not apply GE / CCUD extensions when not defined in project and not requested via TC config (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration().applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project where pom location set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pathToPomFile: getRelativePath(checkoutDir, project.pom),
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via project when defined in project where pom location set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pathToPomFile: getRelativePath(checkoutDir, project.pom),
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project where checkout dir and working dir not set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        runnerParameters.remove('teamcity.build.checkoutDir')
        runnerParameters.remove('teamcity.build.workingDir')

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not inject GE extension when not defined in project but matching custom coordinates defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            // using Guava as surrogate since we do not have a custom extension at hand that pulls in the GE Maven extension transitively
            customExtension: new GroupArtifactVersion(group: 'com.google.guava', artifact: 'guava', version: '31.1-jre')
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            geExtensionCustomCoordinates: 'com.google.guava:guava'
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension not defined in project and not applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not inject CCUD extension when not defined in project but matching custom coordinates defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            // using Guava as surrogate since we do not have a custom extension at hand that pulls in the GE Maven extension transitively
            customExtension: new GroupArtifactVersion(group: 'com.google.guava', artifact: 'guava', version: '31.1-jre')
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
            ccudExtensionCustomCoordinates: 'com.google.guava:guava'
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "ignores GE URL requested via TC config when GE extension is not applied via the classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: 'https://ge-server.invalid',
            allowUntrustedServer: true,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "configures GE URL requested via TC config when GE extension is applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: 'https://ge-server.invalid',
            geExtensionVersion: null,
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geUrl: GE_URL,
            allowUntrustedServer: true,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not publish build scan for TeamCity specific info goal invocation (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            goals: 'org.jetbrains.maven:info-maven3-plugin:1.0.2:info',
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "build succeeds when service message maven extension is applied to a project without GE in the extension classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration().buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            commandLineBuildStepEnabled: true,
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and extensions.xml is in project root directory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pomDirName: 'subdir'
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geExtensionVersion: GE_EXTENSION_VERSION,
            pathToPomFile: getRelativePath(checkoutDir, project.pom),
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and subdirectory is specified as pom path and extensions.xml is in project root directory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pomDirName: 'subdir'
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geExtensionVersion: GE_EXTENSION_VERSION,
            pathToPomFile: getRelativePath(checkoutDir, project.pom.parentFile),
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and extensions.xml is in a higher subdirectory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        Project project = new Project.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pomDirName: 'subdir1/subdir2',
            dotMvnParentDirName: 'subdir1',
        ).buildIn(checkoutDir)

        and:
        new MavenBuildStepConfiguration(
            geExtensionVersion: GE_EXTENSION_VERSION,
            pathToPomFile: getRelativePath(checkoutDir, project.pom),
        ).applyTo(configParameters, runnerParameters)

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, project)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    void outputContainsTeamCityServiceMessageBuildStarted(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']"
        assert output.contains(serviceMsg)
        assert 1 == output.count(serviceMsg)
    }

    void outputMissesTeamCityServiceMessageBuildStarted(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_STARTED']"
        assert !output.contains(serviceMsg)
    }

    void outputContainsTeamCityServiceMessageBuildScanUrl(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${GE_URL}/s/"
        assert output.contains(serviceMsg)
        assert 1 == output.count(serviceMsg)
    }

    void outputMissesTeamCityServiceMessageBuildScanUrl(String output) {
        def serviceMsg = "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle 'BUILD_SCAN_URL:${GE_URL}/s/"
        assert !output.contains(serviceMsg)
    }

    void outputContainsBuildSuccess(String output) {
        assert output.contains("[INFO] BUILD SUCCESS")
    }

    static String getRelativePath(File parent, File child) {
        parent.toPath().relativize(child.toPath()).toString()
    }

}
