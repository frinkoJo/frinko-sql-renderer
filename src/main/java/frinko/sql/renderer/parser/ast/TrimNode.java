package frinko.sql.renderer.parser.ast;

import java.util.List;

import frinko.sql.renderer.render.Context;

public final class TrimNode implements Node {
    private final List<Node> children;
    private final String prefix;
    private final String suffix;
    private final String prefixOverrides;
    private final String suffixOverrides;

    public TrimNode(List<Node> children, String prefix, String suffix, String prefixOverrides, String suffixOverrides){
        this.children=children; this.prefix=prefix; this.suffix=suffix; this.prefixOverrides=prefixOverrides; this.suffixOverrides=suffixOverrides;
    }

    public void render(Context ctx, StringBuilder out){
        StringBuilder buf = new StringBuilder();
        for (Node n : children) n.render(ctx, buf);
        String s = buf.toString().trim();
        if (prefixOverrides != null && !prefixOverrides.isEmpty()) s = s.replaceFirst("^("+joinRegex(prefixOverrides)+")\\s*", "");
        if (suffixOverrides != null && !suffixOverrides.isEmpty()) s = s.replaceFirst("\\s*("+joinRegex(suffixOverrides)+")$", "");
        s = s.replaceAll(",\\s*$", "");
        s = s.replaceAll(",\\s*(?i)(AND|OR)\\s+", ", ");
        if (s.isEmpty()) return;
        if (prefix != null && !prefix.isEmpty()) { out.append(prefix); out.append(' '); }
        out.append(s);
        if (suffix != null && !suffix.isEmpty()) out.append(suffix);
    }

    private String joinRegex(String overrides){
        String[] parts = overrides.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<parts.length;i++){ if(i>0) sb.append('|'); sb.append("\\Q").append(parts[i].trim()).append("\\E"); }
        return sb.toString();
    }
}

