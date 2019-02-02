package nu.studer.teamcity.buildscan.gradle;

public class ServiceMessage {
    private static final String SERVICE_MESSAGE_START = "##teamcity[";
    private static final String SERVICE_MESSAGE_END = "]";

    private final String name;
    private final String argument;

    private ServiceMessage(String name, String argument) {
        this.name = name;
        this.argument = argument;
    }

    public static ServiceMessage of(String name, String argument) {
        return new ServiceMessage(name, argument);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SERVICE_MESSAGE_START);
        sb.append(name);
        sb.append(" '");
        sb.append(escape(argument));
        sb.append("'");
        sb.append(SERVICE_MESSAGE_END);

        return sb.toString();
    }

    private String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(escape(c));
        }

        return sb.toString();
    }

    private String escape(final char c) {
        switch (c) {
            case '\n':
                return "n";
            case '\r':
                return "r";
            case '|':
                return "|";
            case '\'':
                return "\'";
            case '[':
                return "[";
            case ']':
                return "]";
            default:
                return c < 128 ? Character.toString(c) : String.format("0x%04x", (int) c);
        }
    }
}
