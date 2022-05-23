package nu.studer.teamcity.buildscan.agent;

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

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

}
