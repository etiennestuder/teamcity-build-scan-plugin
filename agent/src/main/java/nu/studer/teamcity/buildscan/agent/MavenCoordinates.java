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

    String getGroupId() {
        return groupId;
    }

    String getArtifactId() {
        return artifactId;
    }

    String getVersion() {
        return version;
    }

    MavenCoordinates withVersion(String version) {
        return new MavenCoordinates(groupId, artifactId, version);
    }

    String getGavFormat() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    String getDefaultFilename() {
        return String.format("%s-%s.jar", artifactId, version);
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version == null ? "unspecified" : version);
    }

}
