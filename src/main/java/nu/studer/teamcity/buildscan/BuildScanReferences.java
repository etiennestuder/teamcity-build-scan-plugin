package nu.studer.teamcity.buildscan;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BuildScanReferences implements Serializable {

    private final List<BuildScanReference> buildScans;

    private BuildScanReferences(@NotNull List<BuildScanReference> buildScans) {
        this.buildScans = buildScans;
    }

    @NotNull
    public List<BuildScanReference> all() {
        return buildScans;
    }

    @NotNull
    public BuildScanReference first() {
        return buildScans.get(0);
    }

    @NotNull
    public BuildScanReference get(int index) {
        return buildScans.get(index);
    }

    public int size() {
        return buildScans.size();
    }

    public boolean isEmpty() {
        return buildScans.isEmpty();
    }

    @NotNull
    public static BuildScanReferences of() {
        return new BuildScanReferences(Collections.emptyList());
    }

    @NotNull
    public static BuildScanReferences of(@NotNull BuildScanReference buildScan) {
        return new BuildScanReferences(Collections.singletonList(buildScan));
    }

    @NotNull
    public static BuildScanReferences of(@NotNull List<BuildScanReference> buildScan) {
        return new BuildScanReferences(Collections.unmodifiableList(buildScan));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuildScanReferences that = (BuildScanReferences) o;
        return buildScans.equals(that.buildScans);
    }

    @Override
    public int hashCode() {
        return buildScans.hashCode();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy(this);
    }

    private static final class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 1;

        private transient List<BuildScanReference> buildScans;

        private SerializationProxy(BuildScanReferences buildScans) {
            this.buildScans = buildScans.buildScans;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeInt(buildScans.size());
            for (BuildScanReference buildScan : buildScans) {
                out.writeObject(buildScan);
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            List<BuildScanReference> buildScans = new ArrayList<>();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                BuildScanReference buildScan = (BuildScanReference) in.readObject();
                buildScans.add(buildScan);
            }
            this.buildScans = buildScans;
        }

        private void readObjectNoData() throws ObjectStreamException {
            throw new InvalidObjectException("Stream data required");
        }

        private Object readResolve() throws ObjectStreamException {
            return BuildScanReferences.of(buildScans);
        }

    }

}
