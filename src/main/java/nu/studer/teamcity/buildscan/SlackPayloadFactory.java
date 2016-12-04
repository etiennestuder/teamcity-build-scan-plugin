package nu.studer.teamcity.buildscan;

final class SlackPayloadFactory {

    private SlackPayloadFactory() {
    }

    static SlackPayloadFactory create() {
        return new SlackPayloadFactory();
    }

    SlackPayload from(BuildScanReferences buildScans) {
        SlackPayload payload = new SlackPayload();

        // main text, only hyper-linking to the build scan if there is only one build scan
        if (buildScans.size() == 1) {
            payload.text(String.format("<%s|Build scan> published.", buildScans.first().getUrl()));
        } else {
            payload.text(String.format("%d build scans published.", buildScans.size()));
        }

        // for each build scan, add a separate attachment
        for (BuildScanReference buildScan : buildScans.all()) {
            payload.attachment(new SlackPayload.Attachment()
                .fallback(String.format("Build scan link %s", buildScan.getUrl()))
                .color("#000000")
                .field(new SlackPayload.Attachment.Field()
                    .title("Build scan link")
                    .value(buildScan.getUrl())
                    .isShort(true)));
        }

        return payload;
    }

}
