package nu.studer.teamcity.buildscan

import spock.lang.Specification

class BuildScanReferenceTest extends Specification {

    def "can serialize and deserialize through custom serialization"() {
        given:
        def buildScanReference = new BuildScanReference("myId", "myUrl")

        when:
        def bytes = new ByteArrayOutputStream()
        def outputStream = new ObjectOutputStream(bytes)
        outputStream.writeObject(buildScanReference)

        def inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))
        BuildScanReference result = inputStream.readObject() as BuildScanReference

        then:
        result == buildScanReference
    }

    def "can get url without protocol"() {
        given:
        def buildScanReference = new BuildScanReference("myId", url)

        when:
        def urlWithoutProtocol = buildScanReference.getUrlWithoutProtocol()

        then:
        urlWithoutProtocol == "scans.gradle.com/s/htyg3re5"

        where:
        url << ["http://scans.gradle.com/s/htyg3re5", "https://scans.gradle.com/s/htyg3re5"]
    }

}
