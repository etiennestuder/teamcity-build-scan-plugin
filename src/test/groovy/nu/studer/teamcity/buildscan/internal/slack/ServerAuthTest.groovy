package nu.studer.teamcity.buildscan.internal.slack

import spock.lang.Specification

class ServerAuthTest extends Specification {

    def "reads from config string"() {
        when:
        def serverAuth = ServerAuth.fromConfigString("https://scans.company.com=>me:secret?=>:?!")

        then:
        serverAuth.server == 'https://scans.company.com'
        with(serverAuth.credentials) {
            username == 'me'
            password == 'secret?=>:?!'
        }
    }

    def "throws exception when parsing error"() {
        when:
        ServerAuth.fromConfigString("https://scans.company.com->me:secret")

        then:
        thrown(Exception)

        when:
        ServerAuth.fromConfigString("https://scans.company.com->me")

        then:
        thrown(Exception)
    }

}
