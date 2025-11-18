package frinko.sql.renderer.parser.ast;

import java.util.List;
import frinko.sql.renderer.render.Context;

public final class WhereNode implements Node {
    private final List<Node> children;
    public WhereNode(List<Node> children){ this.children=children; }
    public void render(Context ctx, StringBuilder out){
        StringBuilder buf = new StringBuilder();
        for (Node n : children) n.render(ctx, buf);
        String s = buf.toString().trim();
        s = s.replaceFirst("^(?i)(AND|OR)\\s+", "");
        if (!s.isEmpty()) { out.append(" WHERE "); out.append(s); }
    }
}

