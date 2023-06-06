package nu.studer.teamcity.buildscan.agent;

/**
 * Describes a set of Maven coordinates, represented as a GAV.
 */
final class MavenCoordinates {

    private final String groupId;

    private final String artifactId;

    private final String version;

    MavenCoordinates(String groupId, String artifactId) {
        this(groupId, artifactId, "unspecified");
    }

    MavenCoordinates(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    MavenCoordinates(MavenCoordinates mavenCoordinates, String version) {
        this(mavenCoordinates.groupId, mavenCoordinates.artifactId, version);
    }

    String getGroupId() {
        return groupId;
    }

    String getArtifactId() {
        return artifactId;
    }

    String getVersion() {
        return version;
    }

    public String toFileName() {
        return String.format("%s-%s.jar", artifactId, version);
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

}
