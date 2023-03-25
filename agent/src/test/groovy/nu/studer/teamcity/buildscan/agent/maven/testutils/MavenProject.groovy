package nu.studer.teamcity.buildscan.agent.maven.testutils

final class MavenProject {

    File pom
    File dotMvn

    static final class Configuration {

        String geExtensionVersion
        String ccudExtensionVersion
        GroupArtifactVersion customExtension
        String geUrl
        String pomDirName
        String dotMvnParentDirName

        MavenProject buildIn(File directory) {
            def pomDir = pomDirName ? new File(directory, pomDirName) : directory
            def dotMvnParentDir = dotMvnParentDirName ? new File(directory, dotMvnParentDirName) : directory
            def dotMvn = new File(dotMvnParentDir, '.mvn')

            [pomDir, dotMvn].each { it.mkdirs() }

            setProjectDefinedExtensions(dotMvn, geExtensionVersion, ccudExtensionVersion, customExtension)
            setProjectDefinedGeConfiguration(dotMvn, geUrl)

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

        private static void setProjectDefinedExtensions(File directory, String geExtensionVersion, String ccudExtensionVersion, GroupArtifactVersion customExtension) {
            def extensionsXml = new File(directory, "extensions.xml")
            extensionsXml << """<?xml version="1.0" encoding="UTF-8"?><extensions>"""

            if (geExtensionVersion) {
                extensionsXml << """
            <extension>
                <groupId>com.gradle</groupId>
                <artifactId>gradle-enterprise-maven-extension</artifactId>
                <version>$geExtensionVersion</version>
            </extension>"""
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

        private static void setProjectDefinedGeConfiguration(File directory, String geUrl) {
            if (geUrl) {
                def geConfig = new File(directory, 'gradle-enterprise.xml')
                geConfig << """<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <gradleEnterprise
                xmlns="https://www.gradle.com/gradle-enterprise-maven" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="https://www.gradle.com/gradle-enterprise-maven https://www.gradle.com/schema/gradle-enterprise-maven.xsd">
              <server>
                <url>$geUrl</url>
              </server>
            </gradleEnterprise>"""
            }
        }

    }

}
