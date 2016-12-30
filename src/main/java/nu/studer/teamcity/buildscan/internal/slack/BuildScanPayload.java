package nu.studer.teamcity.buildscan.internal.slack;

import java.util.ArrayList;
import java.util.List;

final class BuildScanPayload {

    String state;
    Data data;

    Data data() {
        if (data == null) {
            data = new Data();
        }
        return data;
    }

    @Override
    public String toString() {
        return "BuildScanPayload{" +
            "state='" + state + '\'' +
            ", data=" + data +
            '}';
    }

    static final class Data {

        String publicId;
        Summary summary;
        Tests tests;

        Summary summary() {
            if (summary == null) {
                summary = new Summary();
            }
            return summary;
        }

        Tests tests() {
            if (tests == null) {
                tests = new Tests();
            }
            return tests;
        }

        @Override
        public String toString() {
            return "Data{" +
                "publicId='" + publicId + '\'' +
                ", summary=" + summary +
                ", tests=" + tests +
                '}';
        }

        static final class Summary {

            boolean failed;
            long startTime;
            String rootProjectName;
            Identity identity;

            Identity identity() {
                if (identity == null) {
                    identity = new Identity();
                }
                return identity;
            }

            @Override
            public String toString() {
                return "Summary{" +
                    "failed=" + failed +
                    ", startTime=" + startTime +
                    ", rootProjectName='" + rootProjectName + '\'' +
                    ", identity=" + identity +
                    '}';
            }

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

        }

        static final class Tests {

            int numFailed;
            List<Row> rows;

            List<Row> rows() {
                if (rows == null) {
                    rows = new ArrayList<>();
                }
                return rows;
            }

            @Override
            public String toString() {
                return "Tests{" +
                    "numFailed=" + numFailed +
                    ", rows=" + rows +
                    '}';
            }

            static final class Row {

                String name;
                String result;

                @Override
                public String toString() {
                    return "Row{" +
                        "name='" + name + '\'' +
                        ", result='" + result + '\'' +
                        '}';
                }

            }

        }

    }

}
