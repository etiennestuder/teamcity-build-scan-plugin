package nu.studer.teamcity.buildscan

import spock.lang.Specification
import spock.lang.Unroll

class BuildScanReferencesTest extends Specification {

    @Unroll
    def "can serialize and deserialize through custom serialization"() {
        when:
        def bytes = new ByteArrayOutputStream()
        def outputStream = new ObjectOutputStream(bytes)
        outputStream.writeObject(buildScanReferences)

        def inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))
        BuildScanReferences result = inputStream.readObject() as BuildScanReferences

        then:
        result == buildScanReferences
        result.all() == buildScanReferences.all()

        where:
        buildScanReferences << [
            BuildScanReferences.of(),
            BuildScanReferences.of(new BuildScanReference("myId", "myUrl")),
            BuildScanReferences.of([new BuildScanReference("myId", "myUrl"), new BuildScanReference("myOtherId", "myOtherUrl")])
        ]
    }

    def "can check for empty"() {
        when:
        def buildScanReferences = BuildScanReferences.of()

        then:
        buildScanReferences.isEmpty()

        when:
        buildScanReferences = BuildScanReferences.of(new BuildScanReference("myId", "myUrl"))

        then:
        !buildScanReferences.isEmpty()
    }

    @Unroll
    def "can get number of elements"() {
        expect:
        buildScanReferences.size() == expectedSize

        where:
        buildScanReferences                                                                                                  | expectedSize
        BuildScanReferences.of()                                                                                             | 0
        BuildScanReferences.of(new BuildScanReference("myId", "myUrl"))                                                      | 1
        BuildScanReferences.of([new BuildScanReference("myId", "myUrl"), new BuildScanReference("myOtherId", "myOtherUrl")]) | 2
    }

    def "can access by index"() {
        when:
        def first = new BuildScanReference("myId", "myUrl")
        def second = new BuildScanReference("myOtherId", "myOtherUrl")
        def buildScanReferences = BuildScanReferences.of([first, second])

        then:
        buildScanReferences.get(0) == first
        buildScanReferences.get(1) == second

        when:
        buildScanReferences.get(2)

        then:
        thrown IndexOutOfBoundsException
    }

    def "can get get first element"() {
        when:
        def first = new BuildScanReference("myId", "myUrl")
        def second = new BuildScanReference("myOtherId", "myOtherUrl")

        then:
        def buildScanReferences = BuildScanReferences.of([first, second])
        buildScanReferences.first() == first

        when:
        buildScanReferences = BuildScanReferences.of()
        buildScanReferences.first()

        then:
        thrown IndexOutOfBoundsException
    }

}
