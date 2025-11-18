package frinko.sql.renderer.api;

import java.util.Collections;
import java.util.Map;

public final class RenderedSql {
    private final String sql;
    private final Map<String, Object> params;

    public RenderedSql(String sql, Map<String, Object> params) {
        this.sql = sql;
        this.params = params == null ? Collections.emptyMap() : Collections.unmodifiableMap(params);
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return sql;
    }
}

