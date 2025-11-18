package frinko.sql.renderer.parser.ast;

import java.util.List;

import frinko.sql.renderer.expr.Ast;
import frinko.sql.renderer.expr.Parser;
import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.render.PropertyAccessor;

public final class IfNode implements Node {
    private final Ast.Node test;
    private final List<Node> children;
    private final PropertyAccessor accessor;

    public IfNode(String testExpr, List<Node> children, PropertyAccessor accessor){ this.test=new Parser().parse(testExpr); this.children=children; this.accessor=accessor; }
    public void render(Context ctx, StringBuilder out){ Object r = test.eval(new Ast.EvalCtx(ctx, accessor)); if (toBool(r)) for (Node n : children) n.render(ctx, out); }
    private boolean toBool(Object v){ return Ast.toBool(v); }
}

