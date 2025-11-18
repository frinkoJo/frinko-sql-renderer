package frinko.sql.renderer.parser.ast;

import java.util.List;
import frinko.sql.renderer.render.Context;

public final class OtherwiseNode implements Node {
    private final List<Node> children;
    public OtherwiseNode(List<Node> children){ this.children=children; }
    public void render(Context ctx, StringBuilder out){ for (Node n : children) n.render(ctx, out); }
}

