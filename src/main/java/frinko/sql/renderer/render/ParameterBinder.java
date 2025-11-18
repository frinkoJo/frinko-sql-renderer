package frinko.sql.renderer.render;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import frinko.sql.renderer.api.Param;
import frinko.sql.renderer.config.RenderOptions;

public final class ParameterBinder {
    private final RenderOptions options;
    public ParameterBinder(RenderOptions options) { this.options = options; }

    public Context bindSingle(Object param) {
        Context ctx = new Context();
        if (param == null) return ctx;
        if (param instanceof Map) {
            Map<?,?> m = (Map<?,?>) param;
            for (Map.Entry<?,?> e : m.entrySet()) ctx.set(String.valueOf(e.getKey()), e.getValue());
        } else {
            if (options.exposeDefaultParamNames) {
                ctx.set("value", param);
                ctx.set("param1", param);
            } else {
                ctx.set("value", param);
            }
        }
        return ctx;
    }

    public Context bindMethod(Method m, Object... args) {
        Context ctx = new Context();
        Annotation[][] anns = m.getParameterAnnotations();
        int n = args == null ? 0 : args.length;
        for (int i = 0; i < n; i++) {
            String named = null;
            for (Annotation a : anns[i]) {
                if (a instanceof Param) { named = ((Param)a).value(); break; }
            }
            Object v = args[i];
            if (named != null && !named.isEmpty()) ctx.set(named, v);
            else if (options.exposeDefaultParamNames) ctx.set("param" + (i+1), v);
            if (v instanceof Map) {
                Map<?,?> m0 = (Map<?,?>) v;
                for (Map.Entry<?,?> e : m0.entrySet()) ctx.set(String.valueOf(e.getKey()), e.getValue());
            }
        }
        if (n == 1 && args != null) {
            Object v = args[0];
            if (options.exposeDefaultParamNames) ctx.set("value", v);
        }
        return ctx;
    }
}

