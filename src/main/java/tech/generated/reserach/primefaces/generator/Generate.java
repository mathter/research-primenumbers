package tech.generated.reserach.primefaces.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Generate {
    private static final Logger LOG = LoggerFactory.getLogger(Generate.class);

    private static final File FILE = new File("primenumbers.bin");

    private static final int COUNT = 10 * 1000 * 1000;

    public static void main(String[] args) throws IOException {
        final int size = 1000 * 1000;
        final SimpleLoadedStore store = new SimpleLoadedStore(size);

        if (FILE.exists()) {
            store.load(FILE, COUNT);
        }

        final Thread t = new Thread(() -> {
            LOG.info("Start " + Thread.currentThread().getName());
            try {
                store.get(size - 1);
            } catch (InterruptedException e) {
                try {
                    store.save(FILE);
                } catch (IOException ex) {
                    LOG.error("", ex);
                }
            }
            LOG.info("End " + Thread.currentThread().getName());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Stoping...");
                t.interrupt();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));

        t.setName("THREAD");
        t.start();

        Thread.currentThread().setDaemon(true);
    }
}
