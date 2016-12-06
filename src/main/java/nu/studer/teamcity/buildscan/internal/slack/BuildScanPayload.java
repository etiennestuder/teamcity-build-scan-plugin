package nu.studer.teamcity.buildscan.internal.slack;

final class BuildScanPayload {

    String state;
    Data data;

    static final class Data {

        String publicId;
        Summary summary;

        static final class Summary {

            boolean failed;
            long startTime;
            long finishTime;
            String rootProjectName;
            Identity identity;

            static final class Identity {

                String identityName;
                String avatarChecksum;

                @Override
                public String toString() {
                    return "Identity{" +
                        "identityName='" + identityName + '\'' +
                        ", avatarChecksum='" + avatarChecksum + '\'' +
                        '}';
                }

            }

            @Override
            public String toString() {
                return "Summary{" +
                    "failed=" + failed +
                    ", startTime=" + startTime +
                    ", finishTime=" + finishTime +
                    ", rootProjectName='" + rootProjectName + '\'' +
                    ", identity=" + identity +
                    '}';
            }

        }

        @Override
        public String toString() {
            return "Data{" +
                "publicId='" + publicId + '\'' +
                ", summary=" + summary +
                '}';
        }

    }

    @Override
    public String toString() {
        return "BuildScanPayload{" +
            "state='" + state + '\'' +
            ", data=" + data +
            '}';
    }

}
