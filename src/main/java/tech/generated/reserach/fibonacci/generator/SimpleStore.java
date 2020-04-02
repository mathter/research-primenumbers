package tech.generated.reserach.fibonacci.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleStore implements Store {

    private static final Logger LOG = LoggerFactory.getLogger(tech.generated.reserach.primefaces.generator.SimpleStore.class);

    private static final long[] PREFILLED = new long[6];

    protected final BigDecimal[] buf;

    protected int lastIndex;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();

    private final Lock writeLock = lock.writeLock();

    public SimpleStore(int initialSize) {
        this.buf = new BigDecimal[initialSize];
        this.init();
    }

    @Override
    public BigDecimal get(long index) {
        final BigDecimal result;

        if (index > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Index must be less the " + Integer.MAX_VALUE);
        }

        if (index > this.lastIndex) {
            try {
                this.readLock.lock();
                if (index >= this.lastIndex) {
                    this.readLock.unlock();
                    this.writeLock.lock();

                    try {
                        while (this.lastIndex <= index) {
                            this.buf[this.lastIndex + 1] = this.buf[this.lastIndex].add(this.buf[(this.lastIndex++) - 1]);
                        }
                    } finally {
                        this.readLock.lock();
                        this.writeLock.unlock();
                    }


                }
            } finally {
                this.readLock.unlock();
            }
        }

        return this.buf[(int) index];
    }

    protected void init() {
        for (int i = 0; i < PREFILLED.length && i < this.buf.length; i++) {
            this.buf[i] = BigDecimal.valueOf(PREFILLED[i]);
            this.lastIndex = i;
        }
    }

    static {
        PREFILLED[0] = 1;
        PREFILLED[1] = 2;
        PREFILLED[2] = 3;
        PREFILLED[3] = 5;
        PREFILLED[4] = 8;
        PREFILLED[5] = 13;
    }
}
