package tech.generated.reserach.primefaces.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class Generate {
    private static final Logger LOG = LoggerFactory.getLogger(Generate.class);

    private static final int N;

    private static final String CONNECTION_URL;

    private static final Properties CONNECTION_PROPS = new Properties();

    static {
        N = 10 * 1000 * 1000;
        CONNECTION_URL = "jdbc:postgresql://localhost:5432/postgres";
        CONNECTION_PROPS.put("user", "postgres");
        CONNECTION_PROPS.put("password", "postgres");
    }

    public static void main(String[] args) throws Exception {
        try (JdbcStore store = new JdbcStore(N + 1, CONNECTION_URL, CONNECTION_PROPS)) {
            store.load();
            registerHook(store, Collections.singletonList(calc(store)));

            while (true) {
                Thread.sleep(10 * 1000);
                store.save();
            }
        }
    }

    private static Thread calc(JdbcStore store) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.info("N=" + N + " Value=" + store.get(N));
                } catch (InterruptedException e) {
                    try {
                        store.save();
                    } catch (SQLException ex) {
                        LOG.error("Can't save!", ex);
                    }
                    LOG.info("Interrupted");
                }
            }
        });

        thread.start();

        return thread;
    }

    private static void registerHook(final JdbcStore store, final Collection<Thread> threads) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stoping by system signal...");
            try {
                threads.forEach(Thread::interrupt);
                store.close();
            } catch (Exception e) {
                LOG.error("Can't close object!", e);
            }
            LOG.info("Stoped by system signal!");
        }));
    }
}
