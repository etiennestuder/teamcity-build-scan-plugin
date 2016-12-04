package nu.studer.teamcity.buildscan.internal.integration.slack;

import java.util.ArrayList;
import java.util.List;

final class SlackPayload {

    String text;
    private final List<Attachment> attachments = new ArrayList<>();

    SlackPayload text(String text) {
        this.text = text;
        return this;
    }

    SlackPayload attachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    static final class Attachment {

        String fallback;
        String color;
        private final List<Field> fields = new ArrayList<>();

        Attachment fallback(String fallback) {
            this.fallback = fallback;
            return this;
        }

        Attachment color(String color) {
            this.color = color;
            return this;
        }

        Attachment field(Field field) {
            this.fields.add(field);
            return this;
        }

        static final class Field {

            String title;
            String value;
            boolean isShort;

            Field title(String title) {
                this.title = title;
                return this;
            }

            Field value(String value) {
                this.value = value;
                return this;
            }

            Field isShort(boolean aShort) {
                isShort = aShort;
                return this;
            }

        }

    }

}


