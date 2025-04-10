package kr.ac.dankook.cs.curation.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    public SQLiteDialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new IdentityColumnSupportImpl() {
            @Override
            public boolean supportsIdentityColumns() {
                return true;
            }

            @Override
            public String getIdentitySelectString(String table, String column, int type) {
                return "select last_insert_rowid()";
            }

            @Override
            public String getIdentityColumnString(int type) {
                return "integer primary key autoincrement";
            }
        };
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    @Override
    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }
}
