package nu.studer.teamcity.buildscan.internal.slack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

final class BuildScanPayloadDeserializer {

    private final Gson gson;

    private BuildScanPayloadDeserializer(@NotNull Gson gson) {
        this.gson = gson;
    }

    @NotNull
    static BuildScanPayloadDeserializer create() {
        Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

        return new BuildScanPayloadDeserializer(gson);
    }

    @NotNull
    BuildScanPayload fromJson(@NotNull Reader json) {
        return gson.fromJson(json, BuildScanPayload.class);
    }

}

