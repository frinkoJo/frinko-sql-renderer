package frinko.sql.renderer.parser;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import frinko.sql.renderer.config.RenderOptions;
import frinko.sql.renderer.expr.Parser;
import frinko.sql.renderer.parser.ast.*;
import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.internal.NamespaceRegistry;
import frinko.sql.renderer.parser.model.MappedStatement;
import frinko.sql.renderer.parser.model.SqlFragment;
import frinko.sql.renderer.render.PropertyAccessor;
import frinko.sql.renderer.render.SqlLiteralWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class XmlMapperParser {
    private final RenderOptions options;
    private final NamespaceRegistry registry;
    private final PropertyAccessor accessor;
    private final SqlLiteralWriter literalWriter;

    public XmlMapperParser(RenderOptions options, NamespaceRegistry registry){
        this.options=options; this.registry=registry; this.accessor=new PropertyAccessor(options); this.literalWriter=new SqlLiteralWriter(options);
    }

    public void parse(Path xml) {
        try (InputStream in = Files.newInputStream(xml)) {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder b = f.newDocumentBuilder();
            Document d = b.parse(in);
            Element mapper = (Element) d.getElementsByTagName("mapper").item(0);
            String namespace = mapper.getAttribute("namespace");
            NodeList children = mapper.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node n = children.item(i);
                if (n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
                Element e = (Element) n;
                String tag = e.getTagName();
                if ("sql".equals(tag)) {
                    String id = e.getAttribute("id");
                    Node root = parseChildren(namespace, e);
                    registry.putFragment(namespace, id, new SqlFragment(namespace, id, root));
                } else if ("select".equals(tag) || "insert".equals(tag) || "update".equals(tag) || "delete".equals(tag)) {
                    String id = e.getAttribute("id");
                    Node root = parseChildren(namespace, e);
                    registry.putStatement(namespace, id, new MappedStatement(namespace, id, root));
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Node parseChildren(String namespace, Element parent){
        List<Node> nodes = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node n = children.item(i);
            switch (n.getNodeType()) {
                case org.w3c.dom.Node.TEXT_NODE:
                    String text = n.getTextContent();
                    if (text != null && text.trim().length() > 0) nodes.add(new TextNode(TextNode.tokenize(text), literalWriter, options, accessor));
                    break;
                case org.w3c.dom.Node.ELEMENT_NODE:
                    Element e = (Element) n;
                    String tag = e.getTagName();
                    if ("if".equals(tag)) {
                        String test = e.getAttribute("test");
                        nodes.add(new IfNode(test, collect(namespace, e), accessor));
                    } else if ("choose".equals(tag)) {
                        List<WhenNode> whens = new ArrayList<>(); OtherwiseNode other = null;
                        NodeList ch = e.getChildNodes();
                        for (int j=0;j<ch.getLength();j++){
                            org.w3c.dom.Node wn = ch.item(j);
                            if (wn.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
                            Element we = (Element) wn; String wtag = we.getTagName();
                            if ("when".equals(wtag)) whens.add(new WhenNode(we.getAttribute("test"), collect(namespace, we), accessor));
                            else if ("otherwise".equals(wtag)) other = new OtherwiseNode(collect(namespace, we));
                        }
                        nodes.add(new ChooseNode(whens, other));
                    } else if ("trim".equals(tag)) {
                        nodes.add(new TrimNode(collect(namespace, e), e.getAttribute("prefix"), e.getAttribute("suffix"), e.getAttribute("prefixOverrides"), e.getAttribute("suffixOverrides")));
                    } else if ("where".equals(tag)) {
                        nodes.add(new WhereNode(collect(namespace, e)));
                    } else if ("set".equals(tag)) {
                        nodes.add(new SetNode(collect(namespace, e)));
                    } else if ("foreach".equals(tag)) {
                        nodes.add(new ForeachNode(attr(e, "collection"), attr(e, "item"), attr(e, "index"), attr(e,"open"), attr(e,"close"), attr(e,"separator"), collect(namespace, e), options));
                    } else if ("bind".equals(tag)) {
                        nodes.add(new BindNode(e.getAttribute("name"), e.getAttribute("value"), accessor));
                    } else if ("include".equals(tag)) {
                        String refid = e.getAttribute("refid");
                        List<IncludeNode.Prop> props = new ArrayList<>();
                        NodeList ps = e.getChildNodes();
                        for (int j=0;j<ps.getLength();j++){
                            org.w3c.dom.Node pn = ps.item(j);
                            if (pn.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
                            Element pe = (Element) pn;
                            if ("property".equals(pe.getTagName())) {
                                String name = pe.getAttribute("name");
                                String valueExpr = pe.getAttribute("value");
                                props.add(new IncludeNode.Prop(name, new Parser().parse(valueExpr)));
                            }
                        }
                        nodes.add(new IncludeNode(namespace, refid, props, registry, accessor));
                    }
                    break;
            }
        }
        return new CompositeNode(nodes);
    }

    private List<Node> collect(String namespace, Element e){ List<Node> c = new ArrayList<>(); CompositeNode x = (CompositeNode) parseChildren(namespace, e); c.addAll(x.children); return c; }
    private String attr(Element e, String n){ String v = e.getAttribute(n); return v==null?"":v; }

    public static final class CompositeNode implements Node {
        final List<Node> children;
        public CompositeNode(List<Node> children){ this.children=children; }
        public void render(Context ctx, StringBuilder out){ for (Node n : children) n.render(ctx, out); }
    }
}
