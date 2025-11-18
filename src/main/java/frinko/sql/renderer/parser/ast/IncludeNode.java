package frinko.sql.renderer.parser.ast;

import java.util.List;

import frinko.sql.renderer.expr.Ast;
import frinko.sql.renderer.internal.NamespaceRegistry;
import frinko.sql.renderer.parser.model.SqlFragment;
import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.render.PropertyAccessor;

public final class IncludeNode implements Node {
    public static final class Prop {
        public final String name; public final Ast.Node expr;
        public Prop(String name, Ast.Node expr){ this.name=name; this.expr=expr; }
    }
    private final String namespace;
    private final String refid;
    private final List<Prop> props;
    private final NamespaceRegistry registry;
    private final PropertyAccessor accessor;

    public IncludeNode(String namespace, String refid, List<Prop> props, NamespaceRegistry registry, PropertyAccessor accessor){
        this.namespace=namespace; this.refid=refid; this.props=props; this.registry=registry; this.accessor=accessor;
    }
    public void render(Context ctx, StringBuilder out){
        Context sub = new Context(ctx);
        for (Prop p : props) sub.set(p.name, p.expr.eval(new Ast.EvalCtx(ctx, accessor)));
        SqlFragment sf = registry.getFragment(namespace, refid);
        if (sf != null) sf.getRoot().render(sub, out);
    }
}

