package frinko.sql.renderer.parser.ast;

import java.util.List;
import frinko.sql.renderer.render.Context;

public final class ChooseNode implements Node {
    private final List<WhenNode> whens;
    private final OtherwiseNode otherwise;
    public ChooseNode(List<WhenNode> whens, OtherwiseNode otherwise){ this.whens=whens; this.otherwise=otherwise; }
    public void render(Context ctx, StringBuilder out){
        for (WhenNode w : whens) { if (w.test(ctx)) { w.render(ctx, out); return; } }
        if (otherwise != null) otherwise.render(ctx, out);
    }
}

