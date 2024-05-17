// send a message to the server that the build has started
buildScanCollector.logger = getLogger()
buildScanCollector.log('BUILD_STARTED')

class BuildScanCollector {
    Logger logger

    void captureBuildScanLink(String link) {
        log("BUILD_SCAN_URL:${link}")
    }

    void log(String message) {
        logger.quiet(generateBuildScanLifeCycleMessage(message))
    }

    private static String generateBuildScanLifeCycleMessage(def attribute) {
        return "##teamcity[nu.studer.teamcity.buildscan.buildScanLifeCycle '${escape(attribute as String)}']" as String
    }

    private static String escape(String value) {
        return value?.toCharArray()?.collect { ch -> escapeChar(ch) }?.join()
    }

    private static String escapeChar(char ch) {
        String escapeCharacter = "|"
        switch (ch) {
            case '\n': return escapeCharacter + "n"
            case '\r': return escapeCharacter + "r"
            case '|': return escapeCharacter + "|"
            case '\'': return escapeCharacter + "\'"
            case '[': return escapeCharacter + "["
            case ']': return escapeCharacter + "]"
            default: return ch < 128 ? ch as String : escapeCharacter + String.format("0x%04x", (int) ch)
        }
    }
}
