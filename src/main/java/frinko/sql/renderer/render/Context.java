package frinko.sql.renderer.render;

import java.util.HashMap;
import java.util.Map;

public final class Context {
    private final Map<String, Object> vars = new HashMap<>();
    private final Context parent;

    public Context() { this(null); }

    public Context(Context parent) { this.parent = parent; }

    public Object get(String name) {
        if (vars.containsKey(name)) return vars.get(name);
        return parent == null ? null : parent.get(name);
    }

    public void set(String name, Object value) { vars.put(name, value); }

    public Map<String, Object> snapshot() { return new HashMap<>(vars); }
}

