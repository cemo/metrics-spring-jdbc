package com.aharwood.metrics.spring.jdbc;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;

public class InstrumentedJdbcTemplate extends JdbcTemplate {

    private static final String GROUP_NAME = "JdbcTemplate";

    CompositeMeterRegistry globalRegistry = Metrics.globalRegistry;

    public InstrumentedJdbcTemplate() {
        super();
    }

    public InstrumentedJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public InstrumentedJdbcTemplate(DataSource dataSource, boolean lazyInit) {
        super(dataSource, lazyInit);
    }

    @Override
    public <T> T execute(StatementCallback<T> action) throws DataAccessException {
        Supplier<T> supplier = () -> InstrumentedJdbcTemplate.super.execute(action);

        if (action instanceof SqlProvider) {
            SqlProvider sqlProvider = (SqlProvider) action;
            if (sqlProvider.getSql() == null) {
                return globalRegistry.timer(GROUP_NAME, "execute", "StatementCallback", "batchUpdate", "true").record(supplier);
            } else {
                return globalRegistry.timer(GROUP_NAME,
                                            "sql", sqlProvider.getSql(),
                                            "execute", "StatementCallback",
                                            "batchUpdate", "false")
                                     .record(supplier);

            }
        }

        return supplier.get();
    }

    @Override
	public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
        Supplier<T> supplier = () -> InstrumentedJdbcTemplate.super.execute(psc, action);
        if (psc instanceof SqlProvider) {
            return globalRegistry.timer(GROUP_NAME,
                                        "sql", ((SqlProvider)psc).getSql(),
                                        "execute", "execute.PreparedStatementCreator.PreparedStatementCallback",
                                        "batchUpdate", "true")
                                 .record(supplier);
        }else{
            return supplier.get();
        }

    }

    @Override
    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
        Supplier<T> supplier = () -> InstrumentedJdbcTemplate.super.execute(csc, action);

        if (csc instanceof SqlProvider) {
            return globalRegistry.timer(GROUP_NAME,
                                        "sql", ((SqlProvider) csc).getSql(),
                                        "execute", "callable.CallableStatementCreator.CallableStatementCallback",
                                        "batchUpdate", "true")
                                 .record(supplier);
        } else {
            return globalRegistry.timer(GROUP_NAME,
                                        "execute", "callable.CallableStatementCreator.CallableStatementCallback",
                                        "batchUpdate", "true")
                                 .record(supplier);
        }
    }

    @Override
    public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
        Supplier<T> supplier = () -> InstrumentedJdbcTemplate.super.execute(action);


        return globalRegistry.timer(GROUP_NAME,
                                    "execute", "connectionCallback")
                             .record(supplier);
    }

}
