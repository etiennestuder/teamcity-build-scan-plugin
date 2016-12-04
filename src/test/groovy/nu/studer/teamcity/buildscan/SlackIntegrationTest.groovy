package nu.studer.teamcity.buildscan

import spock.lang.Specification

class SlackIntegrationTest extends Specification {

    def "properly converts build scan to payload"() {
        given:
        def buildScanReferences = BuildScanReferences.of(new BuildScanReference("myId", "http://www.myUrl.org"))

        when:
        def payload = SlackIntegration.createPayload(buildScanReferences)
        def json = SlackPayloadSerializer.create().toJson(payload)

        then:
        json == """{
  "text": "<http://www.myUrl.org|Build scan> published.",
  "attachments": [
    {
      "fallback": "Build scan link http://www.myUrl.org",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan link",
          "value": "http://www.myUrl.org",
          "short": false
        }
      ]
    }
  ]
}"""
    }

}
