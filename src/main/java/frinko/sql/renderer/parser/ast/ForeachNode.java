package frinko.sql.renderer.parser.ast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import frinko.sql.renderer.config.RenderOptions;
import frinko.sql.renderer.render.Context;

public final class ForeachNode implements Node {
    private final String collectionExpr;
    private final String itemName;
    private final String indexName;
    private final String open;
    private final String close;
    private final String separator;
    private final List<Node> children;
    private final RenderOptions options;

    public ForeachNode(String collectionExpr, String itemName, String indexName, String open, String close, String separator, List<Node> children, RenderOptions options){
        this.collectionExpr=collectionExpr; this.itemName=itemName; this.indexName=indexName; this.open=open; this.close=close; this.separator=separator; this.children=children; this.options=options;
    }

    public void render(Context ctx, StringBuilder out){
        Object coll = ctx.get(collectionExpr);
        Iterable<?> it = asIterable(coll);
        if (it == null) {
            if (options.emptyForeachBehavior == RenderOptions.EmptyForeachBehavior.YIELD_FALSE_CONDITION) out.append("1=0");
            else { out.append(open).append(close); }
            return;
        }
        Iterator<?> iter = it.iterator();
        boolean any = false; int idx = 0;
        if (open != null) out.append(open);
        while (iter.hasNext()) {
            any = true; Object v = iter.next();
            Context sub = new Context(ctx);
            if (itemName != null && !itemName.isEmpty()) sub.set(itemName, v);
            if (indexName != null && !indexName.isEmpty()) sub.set(indexName, idx);
            StringBuilder buf = new StringBuilder();
            for (Node n : children) n.render(sub, buf);
            if (idx > 0 && separator != null) out.append(separator);
            out.append(buf.toString());
            idx++;
        }
        if (!any) {
            if (options.emptyForeachBehavior == RenderOptions.EmptyForeachBehavior.YIELD_FALSE_CONDITION) out.append("1=0");
        }
        if (close != null) out.append(close);
    }

    private Iterable<?> asIterable(Object coll){
        if (coll == null) return null;
        if (coll instanceof Iterable) return (Iterable<?>) coll;
        if (coll.getClass().isArray()) {
            int n = Array.getLength(coll);
            List<Object> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) list.add(Array.get(coll, i));
            return list;
        }
        if (coll instanceof Map) return ((Map<?,?>)coll).values();
        return null;
    }
}

