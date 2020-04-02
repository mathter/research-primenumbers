package tech.generated.reserach.primefaces.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcStore extends SimpleStore {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcStore.class);

    private static final String QUERY_CHECK_TABLE = "select exists (select * from information_schema.tables where table_schema = current_schema() and table_name = 'prime_numbers')";

    private static final String QUERY_CREATE = "create table prime_numbers (id numeric primary key, number numeric not null)";

    private static final String QUERY_MAX_INDEX = "select max(id) as id from prime_numbers";

    private static final String QUERY_SELECT = "select id, number from prime_numbers where id < ? order by id";

    private static final String QUERY_INSERT = "insert into prime_numbers (id, number) values (?, ?)";

    private static final int BATCH_SIZE = 100;

    private final Connection connection;

    public JdbcStore(int initialSize, String connectionString, Properties props) throws SQLException {
        super(initialSize, Runtime.getRuntime().availableProcessors());

        this.connection = DriverManager.getConnection(connectionString, props);
    }

    public void close() throws Exception {
        super.close();
        this.connection.close();
    }

    public void load() throws SQLException {
        this.checkAndCreateTable();
        this.fill();
    }

    public void save() throws SQLException {
        this.checkAndCreateTable();
        this.saveData();
    }

    private void checkAndCreateTable() throws SQLException {
        if (!this.checkTable()) {
            LOG.debug("Table don't exists.");
            this.createTable();
        }
    }

    private boolean checkTable() throws SQLException {
        LOG.debug("Check existing of table ...");
        try (PreparedStatement ptst = this.connection.prepareStatement(QUERY_CHECK_TABLE)) {
            try (ResultSet rs = ptst.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private void createTable() throws SQLException {
        LOG.debug("Creating table ...");
        try (PreparedStatement ptst = this.connection.prepareStatement(QUERY_CREATE)) {
            ptst.executeUpdate();
            LOG.debug("Table created.");
        }
    }

    private void saveData() throws SQLException {
        LOG.debug("Save data (count=" + this.lastIndex + ") ...");
        boolean autoCommit = this.connection.getAutoCommit();

        if (this.lastIndex > this.getCount()) {
            try {
                this.connection.setAutoCommit(false);
                int lastIndex = this.getCount();

                try (PreparedStatement ptst = this.connection.prepareStatement(QUERY_INSERT)) {
                    if (lastIndex < this.lastIndex) {
                        for (++lastIndex; lastIndex <= this.lastIndex; lastIndex++) {
                            ptst.setInt(1, lastIndex);
                            ptst.setLong(2, this.buf[lastIndex]);
                            ptst.addBatch();

                            if (lastIndex % BATCH_SIZE == 0) {
                                ptst.executeBatch();
                            }
                        }

                        ptst.executeBatch();
                        this.connection.commit();
                    }
                }
            } finally {
                this.connection.setAutoCommit(autoCommit);
            }
            LOG.debug("Data saved.");
        } else {
            LOG.debug("Data saving not required!");
        }
    }

    private void fill() throws SQLException {
        LOG.debug("Load data ...");
        this.prefill();
        int lastIndex = 0;

        try (PreparedStatement ptst = this.connection.prepareStatement(QUERY_SELECT)) {
            ptst.setInt(1, this.buf.length);

            try (ResultSet rs = ptst.executeQuery()) {
                this.lastIndex = 0;

                while (rs.next()) {
                    lastIndex = rs.getInt(1);
                    this.buf[lastIndex] = rs.getLong(2);

                    if (lastIndex > this.lastIndex) {
                        this.lastIndex = lastIndex;
                    }
                }
            }
        }

        LOG.debug("Data loaded.");
    }

    private int getCount() throws SQLException {
        final int result;

        try (PreparedStatement ptst = this.connection.prepareStatement(QUERY_MAX_INDEX)) {
            try (ResultSet rs = ptst.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                } else {
                    result = -1;
                }
            }
        }

        return result;
    }
}
