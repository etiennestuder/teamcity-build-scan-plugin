package nu.studer.teamcity.buildscan

import spock.lang.Specification

class SlackIntegrationTest extends Specification {

    def "properly converts single build scan to payload"() {
        given:
        def buildScanReferences = BuildScanReferences.of(new BuildScanReference("myId", "http://www.myUrl.org/s/abcde"))

        when:
        def payload = SlackIntegration.createPayload(buildScanReferences)
        def json = SlackPayloadSerializer.create().toJson(payload)

        then:
        json == """{
  "text": "<http://www.myUrl.org/s/abcde|Build scan> published.",
  "attachments": [
    {
      "fallback": "Build scan link http://www.myUrl.org/s/abcde",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan link",
          "value": "http://www.myUrl.org/s/abcde",
          "short": true
        }
      ]
    }
  ]
}"""
    }

    def "properly converts multiple build scans to payload"() {
        given:
        def buildScanReferences = BuildScanReferences.of([
            new BuildScanReference("myId", "http://www.myUrl.org/s/abcde"),
            new BuildScanReference("myOtherId", "http://www.myOtherUrl.org/efghi")
        ])

        when:
        def payload = SlackIntegration.createPayload(buildScanReferences)
        def json = SlackPayloadSerializer.create().toJson(payload)

        then:
        json == """{
  "text": "2 build scans published.",
  "attachments": [
    {
      "fallback": "Build scan link http://www.myUrl.org/s/abcde",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan link",
          "value": "http://www.myUrl.org/s/abcde",
          "short": true
        }
      ]
    },
    {
      "fallback": "Build scan link http://www.myOtherUrl.org/efghi",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan link",
          "value": "http://www.myOtherUrl.org/efghi",
          "short": true
        }
      ]
    }
  ]
}"""
    }

}
