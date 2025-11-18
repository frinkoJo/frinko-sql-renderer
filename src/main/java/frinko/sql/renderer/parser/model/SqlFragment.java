package frinko.sql.renderer.parser.model;

import frinko.sql.renderer.parser.ast.Node;

public final class SqlFragment {
    private final String namespace;
    private final String id;
    private final Node root;

    public SqlFragment(String namespace, String id, Node root) {
        this.namespace = namespace;
        this.id = id;
        this.root = root;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public Node getRoot() {
        return root;
    }
}

