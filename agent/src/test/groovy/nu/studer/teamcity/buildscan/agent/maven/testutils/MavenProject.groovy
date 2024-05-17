package nu.studer.teamcity.buildscan.agent.maven.testutils

import static nu.studer.teamcity.buildscan.agent.maven.testutils.VersionUtils.isAtLeast

final class MavenProject {

    File pom
    File dotMvn

    static def FIRST_DEVELOCITY_EXTENSION_VERSION = '1.21'

    static final class Configuration {

        String develocityExtensionVersion
        String ccudExtensionVersion
        GroupArtifactVersion customExtension
        String develocityUrl
        String pomDirName
        String dotMvnParentDirName

        MavenProject buildIn(File directory) {
            def pomDir = pomDirName ? new File(directory, pomDirName) : directory
            def dotMvnParentDir = dotMvnParentDirName ? new File(directory, dotMvnParentDirName) : directory
            def dotMvn = new File(dotMvnParentDir, '.mvn')

            [pomDir, dotMvn].each { it.mkdirs() }

            setProjectDefinedExtensions(dotMvn, develocityExtensionVersion, ccudExtensionVersion, customExtension)
            setProjectDefinedDevelocityConfiguration(dotMvn, develocityUrl)

            return new MavenProject(
                    pom: setPomFile(pomDir, 'pom.xml'),
                    dotMvn: dotMvn
            )
        }

        private static File setPomFile(File directory, String name) {
            def pom = new File(directory, name)
            pom << getClass().getResourceAsStream("/pom.xml")
            pom
        }

        private static void setProjectDefinedExtensions(File directory, String develocityExtensionVersion, String ccudExtensionVersion, GroupArtifactVersion customExtension) {
            def extensionsXml = new File(directory, "extensions.xml")
            extensionsXml << """<?xml version="1.0" encoding="UTF-8"?><extensions>"""

            if (develocityExtensionVersion) {
                if (isAtLeast(develocityExtensionVersion, FIRST_DEVELOCITY_EXTENSION_VERSION)) {
                    extensionsXml << """
            <extension>
                <groupId>com.gradle</groupId>
                <artifactId>develocity-maven-extension</artifactId>
                <version>$develocityExtensionVersion</version>
            </extension>"""
                } else {
                    extensionsXml << """
            <extension>
                <groupId>com.gradle</groupId>
                <artifactId>gradle-enterprise-maven-extension</artifactId>
                <version>$develocityExtensionVersion</version>
            </extension>"""
                }
            }

            if (ccudExtensionVersion) {
                extensionsXml << """
            <extension>
                <groupId>com.gradle</groupId>
                <artifactId>common-custom-user-data-maven-extension</artifactId>
                <version>$ccudExtensionVersion</version>
            </extension>"""
            }

            if (customExtension) {
                extensionsXml << """
            <extension>
                <groupId>${customExtension.group}</groupId>
                <artifactId>${customExtension.artifact}</artifactId>
                <version>${customExtension.version}</version>
            </extension>"""
            }

            extensionsXml << """</extensions>"""
        }

        private static void setProjectDefinedDevelocityConfiguration(File directory, String geUrl) {
            if (geUrl) {
                def geConfig = new File(directory, 'gradle-enterprise.xml')
                geConfig << """<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <develocity>
              <server>
                <url>$geUrl</url>
              </server>
            </develocity>"""
            }
        }

    }

}
