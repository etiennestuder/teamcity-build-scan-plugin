package nu.studer.teamcity.buildscan.internal.integration.slack;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

final class SlackPayloadSerializer {

    private final Gson gson;

    private SlackPayloadSerializer(@NotNull Gson gson) {
        this.gson = gson;
    }

    static SlackPayloadSerializer create() {
        Map<Field, FieldNamingStrategy> customNamings = new HashMap<>();
        customNamings.put(declaredField(SlackPayload.Attachment.Field.class, "isShort"), f -> "short");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new FieldNamingStrategyDelegate(customNamings, FieldNamingPolicy.IDENTITY))
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

        return new SlackPayloadSerializer(gson);
    }

    @SuppressWarnings("SameParameterValue")
    private static Field declaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(String.format("Cannot reflectively get field '%s' on class %s", fieldName, clazz));
        }
    }

    @NotNull
    String toJson(@NotNull SlackPayload payload) {
        return gson.toJson(payload);
    }

    private static final class FieldNamingStrategyDelegate implements FieldNamingStrategy {

        private final Map<Field, FieldNamingStrategy> customNamings;
        private final FieldNamingStrategy delegate;

        private FieldNamingStrategyDelegate(Map<Field, FieldNamingStrategy> customNamings, FieldNamingStrategy delegate) {
            this.customNamings = customNamings;
            this.delegate = delegate;
        }

        @Override
        public String translateName(Field f) {
            return customNamings.containsKey(f) ? customNamings.get(f).translateName(f) : delegate.translateName(f);
        }

    }

}

