package nu.studer.teamcity.buildscan.internal.slack

import nu.studer.teamcity.buildscan.BuildScanReference
import nu.studer.teamcity.buildscan.BuildScanReferences
import spock.lang.Specification

class SlackPayloadFactoryTest extends Specification {

    def factory = SlackPayloadFactory.create()

    def "properly converts single build scan to payload"() {
        given:
        def buildScanReferences = BuildScanReferences.of(new BuildScanReference("myId", "http://www.myUrl.org/s/abcde"))
        def params = [
            "system.teamcity.buildConfName": "My Configuration",
            "teamcity.serverUrl"           : "http://tc.server.org",
            "teamcity.build.id"            : "23"
        ]

        when:
        def payload = factory.from(buildScanReferences, params)
        def json = SlackPayloadSerializer.create().toJson(payload)

        then:
        json == """{
  "text": "<http://www.myUrl.org/s/abcde|Build scan> published in TeamCity configuration <http://tc.server.org/viewLog.html?buildId=23|My Configuration>.",
  "attachments": [
    {
      "fallback": "Build scan http://www.myUrl.org/s/abcde",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan",
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
        def params = [
            "system.teamcity.buildConfName": "My Configuration",
            "teamcity.serverUrl"           : "http://tc.server.org",
            "teamcity.build.id"            : "23"
        ]
        when:
        def payload = factory.from(buildScanReferences, params)
        def json = SlackPayloadSerializer.create().toJson(payload)

        then:
        json == """{
  "text": "2 build scans published in TeamCity configuration <http://tc.server.org/viewLog.html?buildId=23|My Configuration>.",
  "attachments": [
    {
      "fallback": "Build scan http://www.myUrl.org/s/abcde",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan",
          "value": "http://www.myUrl.org/s/abcde",
          "short": true
        }
      ]
    },
    {
      "fallback": "Build scan http://www.myOtherUrl.org/efghi",
      "color": "#000000",
      "fields": [
        {
          "title": "Build scan",
          "value": "http://www.myOtherUrl.org/efghi",
          "short": true
        }
      ]
    }
  ]
}"""
    }

}
