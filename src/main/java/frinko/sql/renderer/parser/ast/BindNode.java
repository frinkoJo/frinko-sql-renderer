package frinko.sql.renderer.parser.ast;

import frinko.sql.renderer.expr.Ast;
import frinko.sql.renderer.expr.Parser;
import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.render.PropertyAccessor;

public final class BindNode implements Node {
    private final String name;
    private final Ast.Node expr;
    private final PropertyAccessor accessor;
    public BindNode(String name, String valueExpr, PropertyAccessor accessor){ this.name=name; this.expr=new Parser().parse(valueExpr); this.accessor=accessor; }
    public void render(Context ctx, StringBuilder out){ Object v = expr.eval(new Ast.EvalCtx(ctx, accessor)); ctx.set(name, v); }
}

