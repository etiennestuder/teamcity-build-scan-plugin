package nu.studer.teamcity.buildscan.internal.slack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
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
        String author_name;
        String author_icon;
        String title;
        String text;
        Long ts;
        private final List<Field> fields = new ArrayList<>();

        Attachment fallback(String fallback) {
            this.fallback = fallback;
            return this;
        }

        Attachment color(String color) {
            this.color = color;
            return this;
        }

        Attachment author_name(String author_name) {
            this.author_name = author_name;
            return this;
        }

        Attachment author_icon(String author_icon) {
            this.author_icon = author_icon;
            return this;
        }

        public Attachment title(String title) {
            this.title = title;
            return this;
        }

        public Attachment text(String text) {
            this.text = text;
            return this;
        }

        public Attachment ts(Long ts) {
            this.ts = ts;
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


