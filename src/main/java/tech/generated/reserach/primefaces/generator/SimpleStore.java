package tech.generated.reserach.primefaces.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleStore.class);

    private static final long[] PREFILLED = new long[10];

    protected final long[] buf;

    protected int lastIndex = PREFILLED.length - 1;

    private final Lock lock = new ReentrantLock();

    private final ExecutorService executor;

    private final Processor[] processors;

    private CountDownLatch countDownLatch;

    private final AtomicBoolean checked = new AtomicBoolean();

    private long tested;

    public SimpleStore(int initialSize, int threadCount) {
        this.buf = new long[initialSize];
        this.prefill();

        this.executor = Executors.newFixedThreadPool(threadCount);
        this.processors = new Processor[threadCount];
        for (int i = 0; i < threadCount; i++) {
            this.processors[i] = new Processor();
        }
    }

    public void close() throws Exception {
        this.executor.shutdownNow();
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
                    for (this.tested = this.buf[(int) this.lastIndex] + 1; this.lastIndex < index && tested <= Long.MAX_VALUE; tested++) {
                        this.checked.set(true);

                        int step = (this.lastIndex + 1) / this.processors.length;

                        if (step > 0) {
                            this.countDownLatch = new CountDownLatch(this.processors.length);

                            for (int i = 0; i < this.lastIndex && i < this.processors.length - 1; i++) {
                                this.processors[i].start = (i * step) + 1;
                                this.processors[i].end = (i + 1) * step;
                                this.executor.execute(this.processors[i]);
                            }
                        } else {
                            this.countDownLatch = new CountDownLatch(1);
                        }

                        this.processors[this.processors.length - 1].start = (step * (this.processors.length - 1)) + 1;
                        this.processors[this.processors.length - 1].end = this.lastIndex;

                        this.executor.execute(this.processors[this.processors.length - 1]);
                        this.countDownLatch.await();

                        if (this.checked.get()) {
                            this.buf[++this.lastIndex] = this.tested;
                        }

                        if ((this.tested % 100 == 0) && Thread.currentThread().isInterrupted()) {
                            LOG.info("Generation stoped.");
                            throw new InterruptedException();
                        }
                    }

                    result = BigDecimal.valueOf(this.buf[this.lastIndex]);
                }
            } finally {
                this.lock.unlock();
            }
        }
        return result;
    }

    private boolean checkRange(long tested, int start, int stop) {
        boolean checked = true;

        for (long upperBound = tested / 2; start <= stop; start++) {
            if (this.buf[start] <= upperBound) {
                if (tested % this.buf[start] == 0) {
                    checked = false;
                    break;
                }

                if (start % 1000 == 0 && !this.checked.get()) {
                    break;
                }
            } else {
                break;
            }
        }

        return checked;
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
        this.lastIndex = PREFILLED.length - 1;
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

    private class Processor implements Runnable {
        int start;

        int end;

        @Override
        public void run() {
            boolean checked = SimpleStore.this.checkRange(SimpleStore.this.tested, this.start, this.end);
            SimpleStore.this.checked.compareAndSet(true, checked);
            SimpleStore.this.countDownLatch.countDown();
        }
    }
}
