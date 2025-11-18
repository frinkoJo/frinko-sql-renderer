package frinko.sql.renderer.render;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import frinko.sql.renderer.config.RenderOptions;

public final class PropertyAccessor {
    private final RenderOptions options;
    private final Map<Class<?>, Map<String, Accessor>> cache = new HashMap<>();

    public PropertyAccessor(RenderOptions options) { this.options = options; }

    public Object resolve(Object root, String path) {
        if (root == null || path == null) return null;
        String[] parts = path.split("\\.");
        Object cur = root;
        for (String p : parts) {
            if (cur == null) return null;
            cur = getProperty(cur, p);
            if (cur == Accessor.UNKNOWN && options.failOnUnknownProperty) throw new IllegalArgumentException(path);
            if (cur == Accessor.UNKNOWN) return null;
        }
        return cur;
    }

    private Object getProperty(Object obj, String name) {
        if (obj == null) return null;
        if (obj instanceof Map) {
            Map<?,?> m = (Map<?,?>) obj;
            return m.containsKey(name) ? m.get(name) : Accessor.UNKNOWN;
        }
        if ("size".equals(name)) {
            if (obj instanceof CharSequence) return ((CharSequence)obj).length();
            if (obj instanceof Collection) return ((Collection<?>)obj).size();
            if (obj instanceof Map) return ((Map<?,?>)obj).size();
            if (obj.getClass().isArray()) return Array.getLength(obj);
        }
        Class<?> c = obj.getClass();
        Map<String, Accessor> m = cache.computeIfAbsent(c, k -> buildAccessors(k));
        Accessor a = m.get(name);
        if (a == null) return Accessor.UNKNOWN;
        return a.get(obj);
    }

    private Map<String, Accessor> buildAccessors(Class<?> c) {
        Map<String, Accessor> m = new HashMap<>();
        for (Method method : c.getMethods()) {
            String n = method.getName();
            if (method.getParameterCount() == 0) {
                if (n.startsWith("get") && n.length() > 3) {
                    String prop = decap(n.substring(3));
                    m.put(prop, new MethodAccessor(method));
                } else if (n.startsWith("is") && n.length() > 2) {
                    String prop = decap(n.substring(2));
                    m.put(prop, new MethodAccessor(method));
                } else if ("size".equals(n)) {
                    m.put("size", new MethodAccessor(method));
                }
            }
        }
        for (Field f : c.getFields()) {
            m.put(f.getName(), new FieldAccessor(f));
        }
        return m;
    }

    private String decap(String s) {
        if (s.isEmpty()) return s;
        char[] cs = s.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
    }

    private interface Accessor {
        Object UNKNOWN = new Object();
        Object get(Object o);
    }

    private static final class MethodAccessor implements Accessor {
        private final Method m;
        MethodAccessor(Method m) { this.m = m; }
        public Object get(Object o) {
            try { return m.invoke(o); } catch (Exception e) { throw new RuntimeException(e); }
        }
    }

    private static final class FieldAccessor implements Accessor {
        private final Field f;
        FieldAccessor(Field f) { this.f = f; }
        public Object get(Object o) {
            try { if (!f.isAccessible()) f.setAccessible(true); return f.get(o); } catch (Exception e) { throw new RuntimeException(e); }
        }
    }
}
