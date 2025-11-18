package frinko.sql.renderer.parser.ast;

import frinko.sql.renderer.render.Context;

public interface Node {
    void render(Context ctx, StringBuilder out);
}

