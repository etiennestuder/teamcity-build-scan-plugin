package nu.studer.teamcity.buildscan.internal.slack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "MismatchedQueryAndUpdateOfCollection"})
final class SlackPayload {

    String username;
    String text;
    private final List<Attachment> attachments = new ArrayList<>();

    SlackPayload username(String username) {
        this.username = username;
        return this;
    }

    SlackPayload text(String text) {
        this.text = text;
        return this;
    }

    SlackPayload attachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    static final class Attachment {

        String color;
        String title;
        String text;
        String pretext;
        String fallback;
        String author_name;
        String author_icon;
        Long ts;
        private final List<String> mrkdwn_in = new ArrayList<>();
        private final List<Field> fields = new ArrayList<>();

        Attachment color(String color) {
            this.color = color;
            return this;
        }

        Attachment title(String title) {
            this.title = title;
            return this;
        }

        Attachment text(String text) {
            this.text = text;
            return this;
        }

        Attachment pretext(String text) {
            this.pretext = text;
            return this;
        }

        Attachment fallback(String fallback) {
            this.fallback = fallback;
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

        Attachment ts(Long ts) {
            this.ts = ts;
            return this;
        }

        Attachment mrkdwn_in(String... mrkdwn) {
            this.mrkdwn_in.addAll(Arrays.asList(mrkdwn));
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


