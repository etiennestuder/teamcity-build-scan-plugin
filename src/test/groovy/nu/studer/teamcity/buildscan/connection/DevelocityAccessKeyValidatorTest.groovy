package nu.studer.teamcity.buildscan.connection

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DevelocityAccessKeyValidatorTest extends Specification {

    def "valid access key: #accessKey"() {
        expect:
        DevelocityAccessKeyValidator.isValid(accessKey)

        where:
        accessKey << [
            'server=secret',
            'server=secret ',
            'server = secret',
            ' server= secret',

            'sever1,server2,server3=secret',
            ' sever1, server2 , server3 = secret ',

            'server1=secret1;server2=secret2;server3=secret3',
            ' server1= secret1; server2 , sever3 = secret2 ;'
        ]
    }

    def "invalid access key: #accessKey"() {
        expect:
        !DevelocityAccessKeyValidator.isValid(accessKey)

        where:
        accessKey << [
            null,
            '',
            ' ',
            'server=',
            '=secret',
            'secret',
            'server=secret; ',
            ';server=secret',
            'server1, server2,, server3 = secret '
        ]
    }

}
