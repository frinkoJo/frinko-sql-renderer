package frinko.sql.renderer.expr;

import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.render.PropertyAccessor;

import java.util.List;

public final class Ast {
    public interface Node { Object eval(EvalCtx c); }

    public static final class EvalCtx {
        public final Context ctx;
        public final PropertyAccessor accessor;
        public EvalCtx(Context ctx, PropertyAccessor accessor) { this.ctx = ctx; this.accessor = accessor; }
        public Object getVar(String name) {
            Object v = ctx.get(name);
            if (v != null) return v;
            Object root = ctx.get("value");
            if (root != null) return accessor.resolve(root, name);
            return null;
        }
        public Object prop(Object root, String path) { return accessor.resolve(root, path); }
    }

    public static final class Literal implements Node {
        public final Object v; public Literal(Object v) { this.v = v; }
        public Object eval(EvalCtx c) { return v; }
    }

    public static final class Var implements Node {
        public final String name; public Var(String name) { this.name = name; }
        public Object eval(EvalCtx c) { return c.getVar(name); }
    }

    public static final class Path implements Node {
        public final String base; public final List<String> props;
        public Path(String base, List<String> props) { this.base = base; this.props = props; }
        public Object eval(EvalCtx c) {
            Object root = c.getVar(base);
            if (root == null) return null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < props.size(); i++) {
                if (i > 0) sb.append('.');
                sb.append(props.get(i));
            }
            return c.prop(root, sb.toString());
        }
    }

    public static final class Unary implements Node {
        public final String op; public final Node a;
        public Unary(String op, Node a) { this.op = op; this.a = a; }
        public Object eval(EvalCtx c) {
            Object v = a.eval(c);
            if ("!".equals(op)) return toBool(v) ? Boolean.FALSE : Boolean.TRUE;
            if ("+".equals(op)) return toNumber(v);
            if ("-".equals(op)) return negate(v);
            return null;
        }
        private Object negate(Object v) { Number n = toNumber(v); if (n == null) return null; if (n instanceof Double || n instanceof Float) return -n.doubleValue(); return -n.longValue(); }
    }

    public static final class Binary implements Node {
        public final String op; public final Node a,b;
        public Binary(String op, Node a, Node b) { this.op = op; this.a = a; this.b = b; }
        public Object eval(EvalCtx c) {
            Object x = a.eval(c); Object y = b.eval(c);
            if ("+".equals(op)) return plus(x,y);
            if ("-".equals(op)) return minus(x,y);
            if ("*".equals(op)) return mul(x,y);
            if ("/".equals(op)) return div(x,y);
            if ("%".equals(op)) return mod(x,y);
            if ("==".equals(op)) return eq(x,y);
            if ("!=".equals(op)) return !eqBool(x,y);
            if (">".equals(op)) return gt(x,y);
            if (">=".equals(op)) return ge(x,y);
            if ("<".equals(op)) return lt(x,y);
            if ("<=".equals(op)) return le(x,y);
            if ("&&".equals(op)) return toBool(x) && toBool(y);
            if ("||".equals(op)) return toBool(x) || toBool(y);
            return null;
        }
        private Object plus(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null){ if(a instanceof Double||a instanceof Float||b instanceof Double||b instanceof Float) return a.doubleValue()+b.doubleValue(); return a.longValue()+b.longValue(); } String sx=s(x), sy=s(y); if(sx!=null||sy!=null) return (sx==null?"null":sx)+(sy==null?"null":sy); return null; }
        private Object minus(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null){ if(a instanceof Double||a instanceof Float||b instanceof Double||b instanceof Float) return a.doubleValue()-b.doubleValue(); return a.longValue()-b.longValue(); } return null; }
        private Object mul(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null){ if(a instanceof Double||a instanceof Float||b instanceof Double||b instanceof Float) return a.doubleValue()*b.doubleValue(); return a.longValue()*b.longValue(); } return null; }
        private Object div(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null){ return a.doubleValue()/b.doubleValue(); } return null; }
        private Object mod(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null){ return a.longValue()%b.longValue(); } return null; }
        private boolean eqBool(Object x,Object y){ Object r = eq(x,y); return r instanceof Boolean && ((Boolean)r); }
        private Object eq(Object x,Object y){ if(x==y) return Boolean.TRUE; if(x==null||y==null) return Boolean.FALSE; if(x instanceof Number && y instanceof Number){ double a=((Number)x).doubleValue(); double b=((Number)y).doubleValue(); return a==b; } return x.equals(y); }
        private Object gt(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null) return a.doubleValue()>b.doubleValue(); if(x instanceof Comparable && y!=null) return ((Comparable)x).compareTo(y)>0; return false; }
        private Object ge(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null) return a.doubleValue()>=b.doubleValue(); if(x instanceof Comparable && y!=null) return ((Comparable)x).compareTo(y)>=0; return false; }
        private Object lt(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null) return a.doubleValue()<b.doubleValue(); if(x instanceof Comparable && y!=null) return ((Comparable)x).compareTo(y)<0; return false; }
        private Object le(Object x,Object y){ Number a=n(x),b=n(y); if(a!=null&&b!=null) return a.doubleValue()<=b.doubleValue(); if(x instanceof Comparable && y!=null) return ((Comparable)x).compareTo(y)<=0; return false; }
        private Number n(Object v){ return toNumber(v); }
        private String s(Object v){ return v==null?null:String.valueOf(v); }
    }

    public static final class Call implements Node {
        public final String name; public final java.util.List<Node> args;
        public Call(String name, java.util.List<Node> args){ this.name=name; this.args=args; }
        public Object eval(EvalCtx c){
            if ("isEmpty".equals(name)) return isEmpty(args.get(0).eval(c));
            if ("notEmpty".equals(name)) return !isEmpty(args.get(0).eval(c));
            if ("startsWith".equals(name)) return String.valueOf(args.get(0).eval(c)).startsWith(String.valueOf(args.get(1).eval(c)));
            if ("endsWith".equals(name)) return String.valueOf(args.get(0).eval(c)).endsWith(String.valueOf(args.get(1).eval(c)));
            if ("contains".equals(name)) return String.valueOf(args.get(0).eval(c)).contains(String.valueOf(args.get(1).eval(c)));
            if ("trim".equals(name)) return String.valueOf(args.get(0).eval(c)).trim();
            return null;
        }
        private boolean isEmpty(Object v){ if(v==null) return true; if(v instanceof CharSequence) return ((CharSequence)v).length()==0; if(v instanceof java.util.Collection) return ((java.util.Collection<?>)v).isEmpty(); if(v instanceof java.util.Map) return ((java.util.Map<?,?>)v).isEmpty(); if(v.getClass().isArray()) return java.lang.reflect.Array.getLength(v)==0; return false; }
    }

    public static boolean toBool(Object v){ if(v==null) return false; if(v instanceof Boolean) return (Boolean)v; if(v instanceof Number) return ((Number)v).doubleValue()!=0; if(v instanceof CharSequence) return ((CharSequence)v).length()!=0; return true; }
    public static Number toNumber(Object v){ if(v==null) return null; if(v instanceof Number) return (Number)v; try { return Double.valueOf(String.valueOf(v)); } catch(Exception e) { return null; } }
}
