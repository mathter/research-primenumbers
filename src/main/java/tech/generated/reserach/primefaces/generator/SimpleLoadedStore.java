package tech.generated.reserach.primefaces.generator;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SimpleLoadedStore extends SimpleStore {

    private final Thread thread = new Thread();

    public SimpleLoadedStore(int initialSize) {
        super(initialSize);

    }

    public void save(File file) throws IOException {
        final RandomAccessFile da = new RandomAccessFile(file, "rw");
        int fileLastIndex;
        da.seek(0);

        try {
            fileLastIndex = da.readInt();
        } catch (EOFException e) {
            fileLastIndex = this.lastIndex;
            da.writeInt(fileLastIndex);
        }


        if (this.lastIndex >= fileLastIndex) {
            da.seek(8 * (fileLastIndex + 1));

            for (; fileLastIndex <= this.lastIndex; fileLastIndex++) {
                da.writeLong(this.buf[fileLastIndex]);
            }

            da.seek(0);
            da.writeInt(fileLastIndex);
        }

        da.close();
    }

    public void load(File file, int initialSize) throws IOException {
        final DataInput di = new DataInputStream(new FileInputStream(file));
        final int fileLastIndex = di.readInt();

        this.buf = new long[initialSize <= 0 ? fileLastIndex : initialSize];
        int count = this.buf.length > fileLastIndex ? fileLastIndex : this.buf.length;

        for (int i = 0; i < count; i++) {
            this.buf[i] = di.readLong();
        }

        this.lastIndex = count;

        this.prefill();
    }
}
