package frinko.sql.renderer.parser.ast;

import java.util.ArrayList;
import java.util.List;

import frinko.sql.renderer.config.RenderOptions;
import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.render.SqlLiteralWriter;
import frinko.sql.renderer.render.PropertyAccessor;

public final class TextNode implements Node {
    public static final class Segment {
        public enum Kind { PLAIN, HASH, RAW }
        public final Kind kind; public final String text;
        public Segment(Kind kind, String text){ this.kind=kind; this.text=text; }
    }

    private final List<Segment> segments;
    private final SqlLiteralWriter literalWriter;
    private final RenderOptions options;
    private final PropertyAccessor accessor;

    public TextNode(List<Segment> segments, SqlLiteralWriter literalWriter, RenderOptions options, PropertyAccessor accessor){ this.segments=segments; this.literalWriter=literalWriter; this.options=options; this.accessor=accessor; }

    public void render(Context ctx, StringBuilder out) {
        for (Segment s : segments) {
            if (s.kind == Segment.Kind.PLAIN) {
                out.append(options.strictTextNodeTrim ? normalizeWhitespace(s.text) : s.text);
            } else if (s.kind == Segment.Kind.HASH) {
                Object v = resolvePath(ctx, s.text);
                literalWriter.write(v, out);
            } else {
                Object v = resolvePath(ctx, s.text);
                String raw = String.valueOf(v);
                // 只有当开启检查时，才验证白名单
                if (options.checkRawPlaceholders) {
                    // 如果白名单为空，或者值不在白名单中，则拒绝
                    if (options.allowedRawPlaceholders.isEmpty() || !options.allowedRawPlaceholders.contains(raw)) {
                        throw new RuntimeException("Raw placeholder not allowed: " + raw + ". Add it to allowedRawPlaceholders or set checkRawPlaceholders=false");
                    }
                }
                out.append(raw);
            }
        }
    }

    public static List<Segment> tokenize(String text) {
        List<Segment> list = new ArrayList<>();
        int i = 0; int n = text.length();
        while (i < n) {
            int h = text.indexOf("#{", i);
            int r = text.indexOf("${", i);
            int next = minPos(h, r);
            if (next < 0) { list.add(new Segment(Segment.Kind.PLAIN, text.substring(i))); break; }
            if (next > i) list.add(new Segment(Segment.Kind.PLAIN, text.substring(i, next)));
            boolean isHash = next == h;
            int end = text.indexOf('}', next+2);
            String path = text.substring(next+2, end).trim();
            list.add(new Segment(isHash ? Segment.Kind.HASH : Segment.Kind.RAW, path));
            i = end + 1;
        }
        return list;
    }

    private static int minPos(int a, int b){ if(a<0) return b; if(b<0) return a; return Math.min(a,b); }

    private String normalizeWhitespace(String s){
        StringBuilder sb = new StringBuilder(); boolean lastSpace = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean space = Character.isWhitespace(c);
            if (space) { if (!lastSpace) sb.append(' '); lastSpace = true; }
            else { sb.append(c); lastSpace = false; }
        }
        return sb.toString();
    }

    private Object resolvePath(Context ctx, String path){
        int dot = path.indexOf('.');
        if (dot < 0) {
            Object v = ctx.get(path);
            if (v != null) return v;
            Object root = ctx.get("value");
            if (root != null) return accessor.resolve(root, path);
            return null;
        }
        String base = path.substring(0, dot);
        String rest = path.substring(dot+1);
        Object root = ctx.get(base);
        if (root != null) return accessor.resolve(root, rest);
        Object single = ctx.get("value");
        if (single != null) return accessor.resolve(single, path);
        return null;
    }
}
