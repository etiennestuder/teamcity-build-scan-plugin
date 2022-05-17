package nu.studer.teamcity.buildscan.agent;

final class MavenCoordinates {

    private final String groupId;

    private final String artifactId;

    private final String version;

    public MavenCoordinates(String groupId, String artifactId) {
        this(groupId, artifactId, "unspecified");
    }

    public MavenCoordinates(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public MavenCoordinates withVersion(String version) {
        return new MavenCoordinates(groupId, artifactId, version);
    }

    public String getGavFormat() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    public String getDefaultFilename() {
        return String.format("%s-%s.jar", artifactId, version);
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version == null ? "unspecified" : version);
    }
}