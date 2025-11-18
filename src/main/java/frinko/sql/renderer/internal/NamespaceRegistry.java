package frinko.sql.renderer.internal;

import java.util.HashMap;
import java.util.Map;
import frinko.sql.renderer.parser.model.MappedStatement;
import frinko.sql.renderer.parser.model.SqlFragment;

public final class NamespaceRegistry {
    private final Map<String, Map<String, MappedStatement>> statements = new HashMap<>();
    private final Map<String, Map<String, SqlFragment>> fragments = new HashMap<>();

    public void putStatement(String namespace, String id, MappedStatement ms) {
        statements.computeIfAbsent(namespace, k -> new HashMap<>()).put(id, ms);
    }

    public void putFragment(String namespace, String id, SqlFragment sf) {
        fragments.computeIfAbsent(namespace, k -> new HashMap<>()).put(id, sf);
    }

    public MappedStatement getStatement(String namespace, String id) {
        Map<String, MappedStatement> m = statements.get(namespace);
        return m == null ? null : m.get(id);
    }

    public SqlFragment getFragment(String namespace, String id) {
        Map<String, SqlFragment> m = fragments.get(namespace);
        return m == null ? null : m.get(id);
    }
}

