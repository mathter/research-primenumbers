package tech.generated.reserach.primefaces.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleStore implements Store, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleStore.class);

    private static final long[] PREFILLED = new long[10];

    protected long[] buf;

    protected int lastIndex = 9;

    private final Lock lock = new ReentrantLock();

    public SimpleStore(int initialSize) {
        this.buf = new long[initialSize];
        this.prefill();
    }

    @Override
    public BigDecimal get(long index) throws InterruptedException {
        final BigDecimal result;

        if (index > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Index must be less the " + Integer.MAX_VALUE);
        }

        if (index <= this.lastIndex) {
            result = BigDecimal.valueOf(this.buf[(int) index]);
        } else {
            try {
                this.lock.lock();

                if (index <= this.lastIndex) {
                    result = BigDecimal.valueOf(this.buf[(int) index]);
                } else {

                    for (long tested = this.buf[(int) this.lastIndex] + 1; this.lastIndex < index && tested <= Long.MAX_VALUE; tested++) {
                        boolean isPrimeNumber = true;

                        for (int i = 1; i <= lastIndex; i++) {
                            if (tested % this.buf[i] == 0) {
                                isPrimeNumber = false;
                                break;
                            }
                        }

                        if (isPrimeNumber) {
                            this.buf[++this.lastIndex] = tested;
                        }

                        if (Thread.currentThread().isInterrupted()) {
                            LOG.info("Generation stoped.");
                            throw new InterruptedException();
                        }

                        LOG.debug("Generated index=" + this.lastIndex + ", value=" + this.buf[this.lastIndex]);
                    }

                    result = BigDecimal.valueOf(this.buf[this.lastIndex]);
                }
            } finally {
                this.lock.unlock();
            }
        }
        return result;
    }

    protected int getLastIndex() {
        return this.lastIndex;
    }

    protected void lock() {
        this.lock.lock();
    }

    protected void unlock() {
        this.lock.unlock();
    }

    protected void updateLastIndex(int lastIndex) {
        // Do nothing.
    }

    protected void prefill() {
        for (int i = 0, count = this.buf.length < PREFILLED.length ? this.buf.length : PREFILLED.length; i < count; i++) {
            this.buf[i] = PREFILLED[i];
        }
    }

    static {
        PREFILLED[0] = 1;
        PREFILLED[1] = 2;
        PREFILLED[2] = 3;
        PREFILLED[3] = 5;
        PREFILLED[4] = 7;
        PREFILLED[5] = 11;
        PREFILLED[6] = 13;
        PREFILLED[7] = 17;
        PREFILLED[8] = 19;
        PREFILLED[9] = 23;
    }
}
