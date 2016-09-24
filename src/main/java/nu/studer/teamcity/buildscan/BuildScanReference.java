package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public final class BuildScanReference implements Serializable {

    // getters required for access from JSP templates
    private final String id;
    private final String url;

    public BuildScanReference(@NotNull String id, @NotNull String url) {
        this.id = id;
        this.url = url;
    }

    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuildScanReference that = (BuildScanReference) o;
        return id.equals(that.id) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy(this);
    }

    private static final class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 1;

        private transient String id;
        private transient String url;

        private SerializationProxy(BuildScanReference buildScanReference) {
            this.id = buildScanReference.id;
            this.url = buildScanReference.url;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeUTF(id);
            out.writeUTF(url);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            id = in.readUTF();
            url = in.readUTF();
        }

        private void readObjectNoData() throws ObjectStreamException {
            throw new InvalidObjectException("Stream data required");
        }

        private Object readResolve() throws ObjectStreamException {
            return new BuildScanReference(id, url);
        }

    }

}
