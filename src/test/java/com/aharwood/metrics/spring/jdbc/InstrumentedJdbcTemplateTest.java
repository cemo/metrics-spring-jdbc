package com.aharwood.metrics.spring.jdbc;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertTrue;

@SuppressWarnings("unused")
public class InstrumentedJdbcTemplateTest {

  private static InstrumentedJdbcTemplate jdbcTemplate;

  @BeforeClass
  public static void setupJdbcTemplate() {
    SimpleDriverDataSource dataSource =
        new SimpleDriverDataSource(new EmbeddedDriver(), "jdbc:derby:memory:TestDB;create=true");
    jdbcTemplate = new InstrumentedJdbcTemplate(dataSource);

    //create a test table
    jdbcTemplate.execute("CREATE TABLE Test (ID INT NOT NULL, NAME VARCHAR(32) NOT NULL)");
  }

  @Before
  public void setup() {
    //reset the state of metrics
    //jdbcTemplate.globalRegistry.getRegistries().forEach(new Consumer<MeterRegistry>() {
    //  @Override
    //  public void accept(MeterRegistry meterRegistry) {
    //    meterRegistry.forEachMeter(new Consumer<Meter>() {
    //      @Override
    //      public void accept(Meter meter) {
    //        meter.
    //      }
    //    });
    //  }
    //});
  }

  @Test
  public void batchUpdate() {
    String[] sql = {"INSERT INTO Test VALUES (1, 'batchUpdate0')", "INSERT INTO Test VALUES (2, 'batchUpdate1')"};
    jdbcTemplate.batchUpdate(sql);
    verify();
  }

  @Test
  public void batchUpdateBatchPreparedStatementSetter() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.batchUpdate(sql, new MockBatchPreparedStatementSetter());
    verify();
  }

  @Test
  public void batchUpdateCollectionIntParameterizedPreparedStatementSetter() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.batchUpdate(sql, emptyList(), 1, new MockParameterizedPreparedStatementSetter());
    verify();
  }

  @Test
  public void batchUpdateList() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.batchUpdate(sql, emptyList());
    verify();
  }

  @Test
  public void batchUpdateListIntArray() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.batchUpdate(sql, emptyList(), new int[0]);
    verify();
  }

  @Test
  public void call() {
    jdbcTemplate.call(new MockCallableStatementCreator(), emptyList());
    verify();
  }

  @Test
  public void executeCallableStatementCreatorCallableStatementCallback() {
    jdbcTemplate.execute(new MockCallableStatementCreator(), new MockCallableStatementCallback());
    verify();
  }

  @Test
  public void executeConnectionCallback() {
    jdbcTemplate.execute(new MockConnectionCallback());
    verify();
  }

  @Test
  public void executePreparedStatementCreatorPreparedStatementCallback() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.execute(new MockPreparedStatementCreator(sql), new MockPreparedStatementCallback());
    verify(sql);
  }

  @Test
  public void executeStatementCallback() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.execute(new MockStatementCallback(sql));
    verify(sql);
  }

  @Test
  public void executeString() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.execute(sql);
    verify(sql);
  }

  @Test
  public void executeStringCallableStatementCallback() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.execute(sql, new MockCallableStatementCallback());
    verify(sql);
  }

  @Test
  public void executeStringPreparedStatementCallback() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.execute(sql, new MockPreparedStatementCallback());
    verify(sql);
  }

  @Test
  public void queryPreparedStatementCreatorPreparedStatementSetterResultSetExtractor() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(new MockPreparedStatementCreator(sql), new MockPreparedStatementSetter(), new
        MockResultSetExtractor());
    verify(sql);
  }

  @Test
  public void queryPreparedStatementCreatorResultSetExtractor() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(new MockPreparedStatementCreator(sql), new MockResultSetExtractor());
    verify(sql);
  }

  @Test
  public void queryPreparedStatementCreatorRowCallbackHandler() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(new MockPreparedStatementCreator(sql), new MockRowCallbackHandler());
    verify(sql);
  }

  @Test
  public void queryPreparedStatementCreatorRowMapper() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(new MockPreparedStatementCreator(sql), new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryStringObjectArrayIntArrayResultSetExtractor() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new Object[0], new int[0], new MockResultSetExtractor());
    verify(sql);
  }

  @Test
  public void queryStringObjectArrayIntArrayRowCallbackHandler() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new Object[0], new int[0], new MockRowCallbackHandler());
    verify(sql);
  }

  @Test
  public void queryStringObjectArrayIntArrayRowMapper() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new Object[0], new int[0], new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryStringObjectArrayResultSetExtractor() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new Object[0], new MockResultSetExtractor());
    verify(sql);
  }

  @Test
  public void queryStringObjectArrayRowCallbackHandler() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new Object[0], new MockRowCallbackHandler());
    verify(sql);
  }

  @Test
  public void queryStringObjectArrayRowMapper() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new Object[0], new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryStringPreparedStatementSetterResultSetExtractor() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockPreparedStatementSetter(), new MockResultSetExtractor());
    verify(sql);
  }

  @Test
  public void queryStringPreparedStatementSetterRowCallbackHandler() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockPreparedStatementSetter(), new MockRowCallbackHandler());
    verify(sql);
  }

  @Test
  public void queryStringPreparedStatementSetterRowMapper() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockPreparedStatementSetter(), new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryStringResultSetExtractor() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockResultSetExtractor());
    verify(sql);
  }

  @Test
  public void queryStringResultSetExtractorVarArgs() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockResultSetExtractor(), new Object[0]);
    verify(sql);
  }

  @Test
  public void queryStringRowCallbackHandler() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockRowCallbackHandler());
    verify(sql);
  }

  @Test
  public void queryStringRowCallbackHandlerVarArgs() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockRowCallbackHandler(), new Object[0]);
    verify(sql);
  }

  @Test
  public void queryStringRowMapper() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryStringRowMapperVarArgs() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.query(sql, new MockRowMapper(), new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForIntString() {
    String sql = "select count(0) from SYS.SYSTABLES";
    jdbcTemplate.queryForObject(sql, Integer.class);
    verify(sql);
  }

  @Test
  public void queryForIntStringVarArgs() {
    String sql = "select count(0) from SYS.SYSTABLES";
    jdbcTemplate.queryForObject(sql, new Object[0], Integer.class);
    verify(sql);
  }

  @Test
  public void queryForIntStringObjectArrayIntArray() {
    String sql = "select count(0) from SYS.SYSTABLES";
    jdbcTemplate.queryForObject(sql, new Object[0], new int[0], Integer.class);
    verify(sql);
  }

  @Test
  public void queryForList() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.queryForList(sql);
    verify(sql);
  }

  @Test
  public void queryForListClass() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForList(sql, String.class);
    verify(sql);
  }

  @Test
  public void queryForListClassVarArgs() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForList(sql, String.class, new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForListVarArgs() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.queryForList(sql, new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForListObjectArrayClass() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForList(sql, new Object[0], String.class);
    verify(sql);
  }

  @Test
  public void queryForListObjectArrayIntArray() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.queryForList(sql, new Object[0], new int[0]);
    verify(sql);
  }

  @Test
  public void queryForListObjectArrayIntArrayClass() {
    String sql = "select TABLETYPE from SYS.SYSTABLES";
    jdbcTemplate.queryForList(sql, new Object[0], new int[0], String.class);
    verify(sql);
  }

  @Test
  public void queryForLong() {
    String sql = "select count(0) from SYS.SYSTABLES";
    jdbcTemplate.queryForObject(sql, Long.class);
    verify(sql);
  }

  @Test
  public void queryForLongVarArgs() {
    String sql = "select count(0) from SYS.SYSTABLES";
    jdbcTemplate.queryForObject(sql, new Object[0], Long.class);
    verify(sql);
  }

  @Test
  public void queryForLongObjectArrayIntArray() {
    String sql = "select count(0) from SYS.SYSTABLES";
    jdbcTemplate.queryForObject(sql, new Object[0], new int[0], Long.class);
    verify(sql);
  }

  @Test
  public void queryForMap() {
    String sql = "select * from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForMap(sql);
    verify(sql);
  }

  @Test
  public void queryForMapVarArgs() {
    String sql = "select * from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForMap(sql, new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForMapObjectArrayIntArray() {
    String sql = "select * from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForMap(sql, new Object[0], new int[0]);
    verify(sql);
  }

  @Test
  public void queryForObject() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, String.class);
    verify(sql);
  }

  @Test
  public void queryForObjectVarArgs() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, String.class, new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForObjectObjectArray() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, new Object[0], String.class);
    verify(sql);
  }

  @Test
  public void queryForObjectObjectArrayIntArray() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, new Object[0], new int[0], String.class);
    verify(sql);
  }

  @Test
  public void queryForObjectObjectArrayIntArrayRowMapper() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, new Object[0], new int[0], new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryForObjectObjectArrayRowMapper() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, new Object[0], new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryForObjectRowMapper() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, new MockRowMapper());
    verify(sql);
  }

  @Test
  public void queryForObjectRowMapperVarArgs() {
    String sql = "select TABLETYPE from SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY";
    jdbcTemplate.queryForObject(sql, new MockRowMapper(), new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForRowSet() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.queryForRowSet(sql);
    verify(sql);
  }

  @Test
  public void queryForRowSetVarArgs() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.queryForRowSet(sql, new Object[0]);
    verify(sql);
  }

  @Test
  public void queryForRowSetObjectArrayIntArray() {
    String sql = "select * from SYS.SYSTABLES";
    jdbcTemplate.queryForRowSet(sql, new Object[0], new int[0]);
    verify(sql);
  }

  @Test
  public void updatePreparedStatementCreator() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.update(new MockPreparedStatementCreator(sql));
    verify(sql);
  }

  @Test
  public void updatePreparedStatementCreatorKeyHolder() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.update(new MockPreparedStatementCreator(sql), new GeneratedKeyHolder());
    verify(sql);
  }

  @Test
  public void updateString() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.update(sql);
    verify(sql);
  }

  @Test
  public void updateStringVarArgs() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.update(sql, new Object[0]);
    verify(sql);
  }

  @Test
  public void updateStringObjectArrayIntArray() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.update(sql, new Object[0], new int[0]);
    verify(sql);
  }

  @Test
  public void updateStringPreparedStatementSetter() {
    String sql = "INSERT INTO Test VALUES (1, 'batchUpdate0')";
    jdbcTemplate.update(sql, new MockPreparedStatementSetter());
    verify(sql);
  }

  private void verify() {
    assertTrue(jdbcTemplate.globalRegistry.getMeters().size() > 0);

    jdbcTemplate.globalRegistry.getMeters().forEach(meter -> {
      Iterable<Measurement> measure = meter.measure();
      Collection<Measurement> collection = new ArrayList<>();
      measure.forEach(collection::add);
      assertTrue(collection.size() > 0);
    });
  }

  //
  private void verify(String sql) {
    verify();
    Iterator<Meter> iterator = jdbcTemplate.globalRegistry.getMeters().iterator();
    final Boolean[] found = {false};
    iterator.forEachRemaining(meter -> found[0] = found[0] || sql.equals(meter.getId().getTag("sql")));

    assertTrue(found[0]);
  }

  private static class MockRowMapper implements RowMapper {

    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
      return null;
    }
  }

  private static class MockRowCallbackHandler implements RowCallbackHandler {

    public void processRow(ResultSet rs) throws SQLException {
    }
  }

  private static class MockResultSetExtractor implements ResultSetExtractor {

    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      return null;
    }
  }

  private static class MockPreparedStatementSetter implements PreparedStatementSetter {

    public void setValues(PreparedStatement ps) throws SQLException {
    }
  }

  private static class MockPreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

    private String sql;

    private MockPreparedStatementCreator(String sql) {
      this.sql = sql;
    }

    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
      return new PreparedStatement() {
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
          return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
          return false;
        }

        @Override
        public ResultSet executeQuery(String sql) throws SQLException {
          return null;
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
          return 1;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public int getMaxFieldSize() throws SQLException {
          return 0;
        }

        @Override
        public void setMaxFieldSize(int max) throws SQLException {

        }

        @Override
        public int getMaxRows() throws SQLException {
          return 0;
        }

        @Override
        public void setMaxRows(int max) throws SQLException {

        }

        @Override
        public void setEscapeProcessing(boolean enable) throws SQLException {

        }

        @Override
        public int getQueryTimeout() throws SQLException {
          return 0;
        }

        @Override
        public void setQueryTimeout(int seconds) throws SQLException {

        }

        @Override
        public void cancel() throws SQLException {

        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
          return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public void setCursorName(String name) throws SQLException {

        }

        @Override
        public boolean execute(String sql) throws SQLException {
          return false;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
          return null;
        }

        @Override
        public int getUpdateCount() throws SQLException {
          return 0;
        }

        @Override
        public boolean getMoreResults() throws SQLException {
          return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {

        }

        @Override
        public int getFetchDirection() throws SQLException {
          return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {

        }

        @Override
        public int getFetchSize() throws SQLException {
          return 0;
        }

        @Override
        public int getResultSetConcurrency() throws SQLException {
          return 0;
        }

        @Override
        public int getResultSetType() throws SQLException {
          return 0;
        }

        @Override
        public void addBatch(String sql) throws SQLException {

        }

        @Override
        public void clearBatch() throws SQLException {

        }

        @Override
        public int[] executeBatch() throws SQLException {
          return new int[0];
        }

        @Override
        public Connection getConnection() throws SQLException {
          return null;
        }

        @Override
        public boolean getMoreResults(int current) throws SQLException {
          return false;
        }

        @Override
        public ResultSet getGeneratedKeys() throws SQLException {
          return null;
        }

        @Override
        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
          return 0;
        }

        @Override
        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
          return 0;
        }

        @Override
        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
          return 0;
        }

        @Override
        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
          return false;
        }

        @Override
        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
          return false;
        }

        @Override
        public boolean execute(String sql, String[] columnNames) throws SQLException {
          return false;
        }

        @Override
        public int getResultSetHoldability() throws SQLException {
          return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
          return false;
        }

        @Override
        public void setPoolable(boolean poolable) throws SQLException {

        }

        @Override
        public boolean isPoolable() throws SQLException {
          return false;
        }

        @Override
        public void closeOnCompletion() throws SQLException {

        }

        @Override
        public boolean isCloseOnCompletion() throws SQLException {
          return false;
        }

        @Override
        public ResultSet executeQuery() throws SQLException {
          return new ResultSet() {
            @Override
            public boolean next() throws SQLException {
              return false;
            }

            @Override
            public void close() throws SQLException {

            }

            @Override
            public boolean wasNull() throws SQLException {
              return false;
            }

            @Override
            public String getString(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public boolean getBoolean(int columnIndex) throws SQLException {
              return false;
            }

            @Override
            public byte getByte(int columnIndex) throws SQLException {
              return 0;
            }

            @Override
            public short getShort(int columnIndex) throws SQLException {
              return 0;
            }

            @Override
            public int getInt(int columnIndex) throws SQLException {
              return 0;
            }

            @Override
            public long getLong(int columnIndex) throws SQLException {
              return 0;
            }

            @Override
            public float getFloat(int columnIndex) throws SQLException {
              return 0;
            }

            @Override
            public double getDouble(int columnIndex) throws SQLException {
              return 0;
            }

            @Override
            public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
              return null;
            }

            @Override
            public byte[] getBytes(int columnIndex) throws SQLException {
              return new byte[0];
            }

            @Override
            public Date getDate(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Time getTime(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Timestamp getTimestamp(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public InputStream getAsciiStream(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public InputStream getUnicodeStream(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public InputStream getBinaryStream(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public String getString(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public boolean getBoolean(String columnLabel) throws SQLException {
              return false;
            }

            @Override
            public byte getByte(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public short getShort(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public int getInt(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public long getLong(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public float getFloat(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public double getDouble(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
              return null;
            }

            @Override
            public byte[] getBytes(String columnLabel) throws SQLException {
              return new byte[0];
            }

            @Override
            public Date getDate(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Time getTime(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Timestamp getTimestamp(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public InputStream getAsciiStream(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public InputStream getUnicodeStream(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public InputStream getBinaryStream(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public SQLWarning getWarnings() throws SQLException {
              return null;
            }

            @Override
            public void clearWarnings() throws SQLException {

            }

            @Override
            public String getCursorName() throws SQLException {
              return null;
            }

            @Override
            public ResultSetMetaData getMetaData() throws SQLException {
              return null;
            }

            @Override
            public Object getObject(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Object getObject(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public int findColumn(String columnLabel) throws SQLException {
              return 0;
            }

            @Override
            public Reader getCharacterStream(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Reader getCharacterStream(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public boolean isBeforeFirst() throws SQLException {
              return false;
            }

            @Override
            public boolean isAfterLast() throws SQLException {
              return false;
            }

            @Override
            public boolean isFirst() throws SQLException {
              return false;
            }

            @Override
            public boolean isLast() throws SQLException {
              return false;
            }

            @Override
            public void beforeFirst() throws SQLException {

            }

            @Override
            public void afterLast() throws SQLException {

            }

            @Override
            public boolean first() throws SQLException {
              return false;
            }

            @Override
            public boolean last() throws SQLException {
              return false;
            }

            @Override
            public int getRow() throws SQLException {
              return 0;
            }

            @Override
            public boolean absolute(int row) throws SQLException {
              return false;
            }

            @Override
            public boolean relative(int rows) throws SQLException {
              return false;
            }

            @Override
            public boolean previous() throws SQLException {
              return false;
            }

            @Override
            public void setFetchDirection(int direction) throws SQLException {

            }

            @Override
            public int getFetchDirection() throws SQLException {
              return 0;
            }

            @Override
            public void setFetchSize(int rows) throws SQLException {

            }

            @Override
            public int getFetchSize() throws SQLException {
              return 0;
            }

            @Override
            public int getType() throws SQLException {
              return 0;
            }

            @Override
            public int getConcurrency() throws SQLException {
              return 0;
            }

            @Override
            public boolean rowUpdated() throws SQLException {
              return false;
            }

            @Override
            public boolean rowInserted() throws SQLException {
              return false;
            }

            @Override
            public boolean rowDeleted() throws SQLException {
              return false;
            }

            @Override
            public void updateNull(int columnIndex) throws SQLException {

            }

            @Override
            public void updateBoolean(int columnIndex, boolean x) throws SQLException {

            }

            @Override
            public void updateByte(int columnIndex, byte x) throws SQLException {

            }

            @Override
            public void updateShort(int columnIndex, short x) throws SQLException {

            }

            @Override
            public void updateInt(int columnIndex, int x) throws SQLException {

            }

            @Override
            public void updateLong(int columnIndex, long x) throws SQLException {

            }

            @Override
            public void updateFloat(int columnIndex, float x) throws SQLException {

            }

            @Override
            public void updateDouble(int columnIndex, double x) throws SQLException {

            }

            @Override
            public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

            }

            @Override
            public void updateString(int columnIndex, String x) throws SQLException {

            }

            @Override
            public void updateBytes(int columnIndex, byte[] x) throws SQLException {

            }

            @Override
            public void updateDate(int columnIndex, Date x) throws SQLException {

            }

            @Override
            public void updateTime(int columnIndex, Time x) throws SQLException {

            }

            @Override
            public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

            }

            @Override
            public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

            }

            @Override
            public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

            }

            @Override
            public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

            }

            @Override
            public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

            }

            @Override
            public void updateObject(int columnIndex, Object x) throws SQLException {

            }

            @Override
            public void updateNull(String columnLabel) throws SQLException {

            }

            @Override
            public void updateBoolean(String columnLabel, boolean x) throws SQLException {

            }

            @Override
            public void updateByte(String columnLabel, byte x) throws SQLException {

            }

            @Override
            public void updateShort(String columnLabel, short x) throws SQLException {

            }

            @Override
            public void updateInt(String columnLabel, int x) throws SQLException {

            }

            @Override
            public void updateLong(String columnLabel, long x) throws SQLException {

            }

            @Override
            public void updateFloat(String columnLabel, float x) throws SQLException {

            }

            @Override
            public void updateDouble(String columnLabel, double x) throws SQLException {

            }

            @Override
            public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

            }

            @Override
            public void updateString(String columnLabel, String x) throws SQLException {

            }

            @Override
            public void updateBytes(String columnLabel, byte[] x) throws SQLException {

            }

            @Override
            public void updateDate(String columnLabel, Date x) throws SQLException {

            }

            @Override
            public void updateTime(String columnLabel, Time x) throws SQLException {

            }

            @Override
            public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

            }

            @Override
            public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

            }

            @Override
            public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

            }

            @Override
            public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

            }

            @Override
            public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

            }

            @Override
            public void updateObject(String columnLabel, Object x) throws SQLException {

            }

            @Override
            public void insertRow() throws SQLException {

            }

            @Override
            public void updateRow() throws SQLException {

            }

            @Override
            public void deleteRow() throws SQLException {

            }

            @Override
            public void refreshRow() throws SQLException {

            }

            @Override
            public void cancelRowUpdates() throws SQLException {

            }

            @Override
            public void moveToInsertRow() throws SQLException {

            }

            @Override
            public void moveToCurrentRow() throws SQLException {

            }

            @Override
            public Statement getStatement() throws SQLException {
              return null;
            }

            @Override
            public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
              return null;
            }

            @Override
            public Ref getRef(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Blob getBlob(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Clob getClob(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Array getArray(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
              return null;
            }

            @Override
            public Ref getRef(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Blob getBlob(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Clob getClob(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Array getArray(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Date getDate(int columnIndex, Calendar cal) throws SQLException {
              return null;
            }

            @Override
            public Date getDate(String columnLabel, Calendar cal) throws SQLException {
              return null;
            }

            @Override
            public Time getTime(int columnIndex, Calendar cal) throws SQLException {
              return null;
            }

            @Override
            public Time getTime(String columnLabel, Calendar cal) throws SQLException {
              return null;
            }

            @Override
            public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
              return null;
            }

            @Override
            public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
              return null;
            }

            @Override
            public URL getURL(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public URL getURL(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public void updateRef(int columnIndex, Ref x) throws SQLException {

            }

            @Override
            public void updateRef(String columnLabel, Ref x) throws SQLException {

            }

            @Override
            public void updateBlob(int columnIndex, Blob x) throws SQLException {

            }

            @Override
            public void updateBlob(String columnLabel, Blob x) throws SQLException {

            }

            @Override
            public void updateClob(int columnIndex, Clob x) throws SQLException {

            }

            @Override
            public void updateClob(String columnLabel, Clob x) throws SQLException {

            }

            @Override
            public void updateArray(int columnIndex, Array x) throws SQLException {

            }

            @Override
            public void updateArray(String columnLabel, Array x) throws SQLException {

            }

            @Override
            public RowId getRowId(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public RowId getRowId(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public void updateRowId(int columnIndex, RowId x) throws SQLException {

            }

            @Override
            public void updateRowId(String columnLabel, RowId x) throws SQLException {

            }

            @Override
            public int getHoldability() throws SQLException {
              return 0;
            }

            @Override
            public boolean isClosed() throws SQLException {
              return false;
            }

            @Override
            public void updateNString(int columnIndex, String nString) throws SQLException {

            }

            @Override
            public void updateNString(String columnLabel, String nString) throws SQLException {

            }

            @Override
            public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

            }

            @Override
            public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

            }

            @Override
            public NClob getNClob(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public NClob getNClob(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public SQLXML getSQLXML(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public SQLXML getSQLXML(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

            }

            @Override
            public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

            }

            @Override
            public String getNString(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public String getNString(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public Reader getNCharacterStream(int columnIndex) throws SQLException {
              return null;
            }

            @Override
            public Reader getNCharacterStream(String columnLabel) throws SQLException {
              return null;
            }

            @Override
            public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

            }

            @Override
            public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

            }

            @Override
            public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

            }

            @Override
            public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

            }

            @Override
            public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

            }

            @Override
            public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

            }

            @Override
            public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

            }

            @Override
            public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

            }

            @Override
            public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

            }

            @Override
            public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

            }

            @Override
            public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

            }

            @Override
            public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

            }

            @Override
            public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

            }

            @Override
            public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

            }

            @Override
            public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

            }

            @Override
            public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

            }

            @Override
            public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

            }

            @Override
            public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

            }

            @Override
            public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

            }

            @Override
            public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

            }

            @Override
            public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

            }

            @Override
            public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

            }

            @Override
            public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

            }

            @Override
            public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

            }

            @Override
            public void updateClob(int columnIndex, Reader reader) throws SQLException {

            }

            @Override
            public void updateClob(String columnLabel, Reader reader) throws SQLException {

            }

            @Override
            public void updateNClob(int columnIndex, Reader reader) throws SQLException {

            }

            @Override
            public void updateNClob(String columnLabel, Reader reader) throws SQLException {

            }

            @Override
            public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
              return null;
            }

            @Override
            public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
              return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
              return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
              return false;
            }
          };
        }

        @Override
        public int executeUpdate() throws SQLException {
          return 0;
        }

        @Override
        public void setNull(int parameterIndex, int sqlType) throws SQLException {

        }

        @Override
        public void setBoolean(int parameterIndex, boolean x) throws SQLException {

        }

        @Override
        public void setByte(int parameterIndex, byte x) throws SQLException {

        }

        @Override
        public void setShort(int parameterIndex, short x) throws SQLException {

        }

        @Override
        public void setInt(int parameterIndex, int x) throws SQLException {

        }

        @Override
        public void setLong(int parameterIndex, long x) throws SQLException {

        }

        @Override
        public void setFloat(int parameterIndex, float x) throws SQLException {

        }

        @Override
        public void setDouble(int parameterIndex, double x) throws SQLException {

        }

        @Override
        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

        }

        @Override
        public void setString(int parameterIndex, String x) throws SQLException {

        }

        @Override
        public void setBytes(int parameterIndex, byte[] x) throws SQLException {

        }

        @Override
        public void setDate(int parameterIndex, Date x) throws SQLException {

        }

        @Override
        public void setTime(int parameterIndex, Time x) throws SQLException {

        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void clearParameters() throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x) throws SQLException {

        }

        @Override
        public boolean execute() throws SQLException {
          return false;
        }

        @Override
        public void addBatch() throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

        }

        @Override
        public void setRef(int parameterIndex, Ref x) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, Blob x) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Clob x) throws SQLException {

        }

        @Override
        public void setArray(int parameterIndex, Array x) throws SQLException {

        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
          return null;
        }

        @Override
        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

        }

        @Override
        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

        }

        @Override
        public void setURL(int parameterIndex, URL x) throws SQLException {

        }

        @Override
        public ParameterMetaData getParameterMetaData() throws SQLException {
          return null;
        }

        @Override
        public void setRowId(int parameterIndex, RowId x) throws SQLException {

        }

        @Override
        public void setNString(int parameterIndex, String value) throws SQLException {

        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, NClob value) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
            throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, Reader reader) throws SQLException {

        }
      };
    }

    public String getSql() {
      return this.sql;
    }
  }

  private static class MockCallableStatementCreator implements CallableStatementCreator {

    public CallableStatement createCallableStatement(Connection con) throws SQLException {
      return new CallableStatement() {
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
          return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
          return false;
        }

        @Override
        public ResultSet executeQuery(String sql) throws SQLException {
          return null;
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
          return 0;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public int getMaxFieldSize() throws SQLException {
          return 0;
        }

        @Override
        public void setMaxFieldSize(int max) throws SQLException {

        }

        @Override
        public int getMaxRows() throws SQLException {
          return 0;
        }

        @Override
        public void setMaxRows(int max) throws SQLException {

        }

        @Override
        public void setEscapeProcessing(boolean enable) throws SQLException {

        }

        @Override
        public int getQueryTimeout() throws SQLException {
          return 0;
        }

        @Override
        public void setQueryTimeout(int seconds) throws SQLException {

        }

        @Override
        public void cancel() throws SQLException {

        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
          return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public void setCursorName(String name) throws SQLException {

        }

        @Override
        public boolean execute(String sql) throws SQLException {
          return false;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
          return null;
        }

        @Override
        public int getUpdateCount() throws SQLException {
          return -1;
        }

        @Override
        public boolean getMoreResults() throws SQLException {
          return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {

        }

        @Override
        public int getFetchDirection() throws SQLException {
          return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {

        }

        @Override
        public int getFetchSize() throws SQLException {
          return 0;
        }

        @Override
        public int getResultSetConcurrency() throws SQLException {
          return 0;
        }

        @Override
        public int getResultSetType() throws SQLException {
          return 0;
        }

        @Override
        public void addBatch(String sql) throws SQLException {

        }

        @Override
        public void clearBatch() throws SQLException {

        }

        @Override
        public int[] executeBatch() throws SQLException {
          return new int[0];
        }

        @Override
        public Connection getConnection() throws SQLException {
          return null;
        }

        @Override
        public boolean getMoreResults(int current) throws SQLException {
          return false;
        }

        @Override
        public ResultSet getGeneratedKeys() throws SQLException {
          return null;
        }

        @Override
        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
          return 0;
        }

        @Override
        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
          return 0;
        }

        @Override
        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
          return 0;
        }

        @Override
        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
          return false;
        }

        @Override
        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
          return false;
        }

        @Override
        public boolean execute(String sql, String[] columnNames) throws SQLException {
          return false;
        }

        @Override
        public int getResultSetHoldability() throws SQLException {
          return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
          return false;
        }

        @Override
        public void setPoolable(boolean poolable) throws SQLException {

        }

        @Override
        public boolean isPoolable() throws SQLException {
          return false;
        }

        @Override
        public void closeOnCompletion() throws SQLException {

        }

        @Override
        public boolean isCloseOnCompletion() throws SQLException {
          return false;
        }

        @Override
        public ResultSet executeQuery() throws SQLException {
          return null;
        }

        @Override
        public int executeUpdate() throws SQLException {
          return 0;
        }

        @Override
        public void setNull(int parameterIndex, int sqlType) throws SQLException {

        }

        @Override
        public void setBoolean(int parameterIndex, boolean x) throws SQLException {

        }

        @Override
        public void setByte(int parameterIndex, byte x) throws SQLException {

        }

        @Override
        public void setShort(int parameterIndex, short x) throws SQLException {

        }

        @Override
        public void setInt(int parameterIndex, int x) throws SQLException {

        }

        @Override
        public void setLong(int parameterIndex, long x) throws SQLException {

        }

        @Override
        public void setFloat(int parameterIndex, float x) throws SQLException {

        }

        @Override
        public void setDouble(int parameterIndex, double x) throws SQLException {

        }

        @Override
        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

        }

        @Override
        public void setString(int parameterIndex, String x) throws SQLException {

        }

        @Override
        public void setBytes(int parameterIndex, byte[] x) throws SQLException {

        }

        @Override
        public void setDate(int parameterIndex, Date x) throws SQLException {

        }

        @Override
        public void setTime(int parameterIndex, Time x) throws SQLException {

        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void clearParameters() throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x) throws SQLException {

        }

        @Override
        public boolean execute() throws SQLException {
          return false;
        }

        @Override
        public void addBatch() throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

        }

        @Override
        public void setRef(int parameterIndex, Ref x) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, Blob x) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Clob x) throws SQLException {

        }

        @Override
        public void setArray(int parameterIndex, Array x) throws SQLException {

        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
          return null;
        }

        @Override
        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

        }

        @Override
        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

        }

        @Override
        public void setURL(int parameterIndex, URL x) throws SQLException {

        }

        @Override
        public ParameterMetaData getParameterMetaData() throws SQLException {
          return null;
        }

        @Override
        public void setRowId(int parameterIndex, RowId x) throws SQLException {

        }

        @Override
        public void setNString(int parameterIndex, String value) throws SQLException {

        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, NClob value) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
            throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

        }

        @Override
        public void setClob(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

        }

        @Override
        public void setNClob(int parameterIndex, Reader reader) throws SQLException {

        }

        @Override
        public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {

        }

        @Override
        public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {

        }

        @Override
        public boolean wasNull() throws SQLException {
          return false;
        }

        @Override
        public String getString(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public boolean getBoolean(int parameterIndex) throws SQLException {
          return false;
        }

        @Override
        public byte getByte(int parameterIndex) throws SQLException {
          return 0;
        }

        @Override
        public short getShort(int parameterIndex) throws SQLException {
          return 0;
        }

        @Override
        public int getInt(int parameterIndex) throws SQLException {
          return 0;
        }

        @Override
        public long getLong(int parameterIndex) throws SQLException {
          return 0;
        }

        @Override
        public float getFloat(int parameterIndex) throws SQLException {
          return 0;
        }

        @Override
        public double getDouble(int parameterIndex) throws SQLException {
          return 0;
        }

        @Override
        public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
          return null;
        }

        @Override
        public byte[] getBytes(int parameterIndex) throws SQLException {
          return new byte[0];
        }

        @Override
        public Date getDate(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Time getTime(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Timestamp getTimestamp(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Object getObject(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
          return null;
        }

        @Override
        public Ref getRef(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Blob getBlob(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Clob getClob(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Array getArray(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
          return null;
        }

        @Override
        public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
          return null;
        }

        @Override
        public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
          return null;
        }

        @Override
        public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {

        }

        @Override
        public void registerOutParameter(String parameterName, int sqlType) throws SQLException {

        }

        @Override
        public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {

        }

        @Override
        public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {

        }

        @Override
        public URL getURL(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public void setURL(String parameterName, URL val) throws SQLException {

        }

        @Override
        public void setNull(String parameterName, int sqlType) throws SQLException {

        }

        @Override
        public void setBoolean(String parameterName, boolean x) throws SQLException {

        }

        @Override
        public void setByte(String parameterName, byte x) throws SQLException {

        }

        @Override
        public void setShort(String parameterName, short x) throws SQLException {

        }

        @Override
        public void setInt(String parameterName, int x) throws SQLException {

        }

        @Override
        public void setLong(String parameterName, long x) throws SQLException {

        }

        @Override
        public void setFloat(String parameterName, float x) throws SQLException {

        }

        @Override
        public void setDouble(String parameterName, double x) throws SQLException {

        }

        @Override
        public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {

        }

        @Override
        public void setString(String parameterName, String x) throws SQLException {

        }

        @Override
        public void setBytes(String parameterName, byte[] x) throws SQLException {

        }

        @Override
        public void setDate(String parameterName, Date x) throws SQLException {

        }

        @Override
        public void setTime(String parameterName, Time x) throws SQLException {

        }

        @Override
        public void setTimestamp(String parameterName, Timestamp x) throws SQLException {

        }

        @Override
        public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {

        }

        @Override
        public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {

        }

        @Override
        public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {

        }

        @Override
        public void setObject(String parameterName, Object x) throws SQLException {

        }

        @Override
        public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {

        }

        @Override
        public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {

        }

        @Override
        public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {

        }

        @Override
        public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {

        }

        @Override
        public String getString(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public boolean getBoolean(String parameterName) throws SQLException {
          return false;
        }

        @Override
        public byte getByte(String parameterName) throws SQLException {
          return 0;
        }

        @Override
        public short getShort(String parameterName) throws SQLException {
          return 0;
        }

        @Override
        public int getInt(String parameterName) throws SQLException {
          return 0;
        }

        @Override
        public long getLong(String parameterName) throws SQLException {
          return 0;
        }

        @Override
        public float getFloat(String parameterName) throws SQLException {
          return 0;
        }

        @Override
        public double getDouble(String parameterName) throws SQLException {
          return 0;
        }

        @Override
        public byte[] getBytes(String parameterName) throws SQLException {
          return new byte[0];
        }

        @Override
        public Date getDate(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Time getTime(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Timestamp getTimestamp(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Object getObject(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public BigDecimal getBigDecimal(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
          return null;
        }

        @Override
        public Ref getRef(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Blob getBlob(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Clob getClob(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Array getArray(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Date getDate(String parameterName, Calendar cal) throws SQLException {
          return null;
        }

        @Override
        public Time getTime(String parameterName, Calendar cal) throws SQLException {
          return null;
        }

        @Override
        public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
          return null;
        }

        @Override
        public URL getURL(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public RowId getRowId(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public RowId getRowId(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public void setRowId(String parameterName, RowId x) throws SQLException {

        }

        @Override
        public void setNString(String parameterName, String value) throws SQLException {

        }

        @Override
        public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {

        }

        @Override
        public void setNClob(String parameterName, NClob value) throws SQLException {

        }

        @Override
        public void setClob(String parameterName, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void setNClob(String parameterName, Reader reader, long length) throws SQLException {

        }

        @Override
        public NClob getNClob(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public NClob getNClob(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public SQLXML getSQLXML(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public SQLXML getSQLXML(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public String getNString(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public String getNString(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Reader getNCharacterStream(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Reader getNCharacterStream(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public Reader getCharacterStream(int parameterIndex) throws SQLException {
          return null;
        }

        @Override
        public Reader getCharacterStream(String parameterName) throws SQLException {
          return null;
        }

        @Override
        public void setBlob(String parameterName, Blob x) throws SQLException {

        }

        @Override
        public void setClob(String parameterName, Clob x) throws SQLException {

        }

        @Override
        public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {

        }

        @Override
        public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {

        }

        @Override
        public void setAsciiStream(String parameterName, InputStream x) throws SQLException {

        }

        @Override
        public void setBinaryStream(String parameterName, InputStream x) throws SQLException {

        }

        @Override
        public void setCharacterStream(String parameterName, Reader reader) throws SQLException {

        }

        @Override
        public void setNCharacterStream(String parameterName, Reader value) throws SQLException {

        }

        @Override
        public void setClob(String parameterName, Reader reader) throws SQLException {

        }

        @Override
        public void setBlob(String parameterName, InputStream inputStream) throws SQLException {

        }

        @Override
        public void setNClob(String parameterName, Reader reader) throws SQLException {

        }

        @Override
        public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
          return null;
        }

        @Override
        public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
          return null;
        }
      };
    }
  }

  private static class MockCallableStatementCallback implements CallableStatementCallback {

    public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
      return null;
    }
  }

  private static class MockConnectionCallback implements ConnectionCallback {

    public Object doInConnection(Connection con) throws SQLException, DataAccessException {
      return null;
    }
  }

  private static class MockPreparedStatementCallback implements PreparedStatementCallback {

    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
      return null;
    }
  }

  private static class MockStatementCallback implements StatementCallback, SqlProvider {

    private String sql;

    private MockStatementCallback(String sql) {
      this.sql = sql;
    }

    public Object doInStatement(Statement stmt) throws SQLException, DataAccessException {
      return null;
    }

    public String getSql() {
      return sql;
    }
  }

  private static class MockBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

    public int getBatchSize() {
      return 1;
    }

    public void setValues(PreparedStatement ps, int i) throws SQLException {
    }
  }

  private static class MockParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter {

    public void setValues(PreparedStatement ps, Object argument) throws SQLException {
    }
  }
}
