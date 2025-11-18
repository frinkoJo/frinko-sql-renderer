package frinko.sql.renderer.expr;

import java.util.ArrayList;
import java.util.List;

public final class Lexer {
    public enum T { IDENT, STRING, NUMBER, TRUE, FALSE, NULL, LP, RP, DOT, COMMA, NOT, AND, OR, EQ, NE, GT, GE, LT, LE, PLUS, MINUS, MUL, DIV, MOD }

    public static final class Token {
        public final T t; public final String s;
        public Token(T t, String s) { this.t = t; this.s = s; }
    }

    public List<Token> lex(String src) {
        List<Token> out = new ArrayList<>();
        int i = 0; int n = src.length();
        while (i < n) {
            char c = src.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '(') { out.add(new Token(T.LP, "(")); i++; continue; }
            if (c == ')') { out.add(new Token(T.RP, ")")); i++; continue; }
            if (c == '.') { out.add(new Token(T.DOT, ".")); i++; continue; }
            if (c == ',') { out.add(new Token(T.COMMA, ",")); i++; continue; }
            if (c == '!') {
                if (i+1<n && src.charAt(i+1)=='=') { out.add(new Token(T.NE, "!=")); i+=2; } else { out.add(new Token(T.NOT, "!")); i++; }
                continue;
            }
            if (c == '&' && i+1<n && src.charAt(i+1)=='&') { out.add(new Token(T.AND, "&&")); i+=2; continue; }
            if (c == '|' && i+1<n && src.charAt(i+1)=='|') { out.add(new Token(T.OR, "||")); i+=2; continue; }
            if (c == '=') { out.add(new Token(T.EQ, "==")); i++; continue; }
            if (c == '>' ) { if (i+1<n && src.charAt(i+1)=='=') { out.add(new Token(T.GE, ">=")); i+=2; } else { out.add(new Token(T.GT, ">")); i++; } continue; }
            if (c == '<' ) { if (i+1<n && src.charAt(i+1)=='=') { out.add(new Token(T.LE, "<=")); i+=2; } else { out.add(new Token(T.LT, "<")); i++; } continue; }
            if (c == '+') { out.add(new Token(T.PLUS, "+")); i++; continue; }
            if (c == '-') { out.add(new Token(T.MINUS, "-")); i++; continue; }
            if (c == '*') { out.add(new Token(T.MUL, "*")); i++; continue; }
            if (c == '/') { out.add(new Token(T.DIV, "/")); i++; continue; }
            if (c == '%') { out.add(new Token(T.MOD, "%")); i++; continue; }
            if (c == '\'' ) {
                StringBuilder sb = new StringBuilder(); i++;
                while (i<n) {
                    char d = src.charAt(i);
                    if (d == '\'') { if (i+1<n && src.charAt(i+1)=='\'') { sb.append('\''); i+=2; continue; } else { i++; break; } }
                    sb.append(d); i++;
                }
                out.add(new Token(T.STRING, sb.toString()));
                continue;
            }
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i<n && (Character.isDigit(src.charAt(i)) || src.charAt(i)=='.')) { sb.append(src.charAt(i)); i++; }
                out.add(new Token(T.NUMBER, sb.toString()));
                continue;
            }
            if (Character.isJavaIdentifierStart(c)) {
                StringBuilder sb = new StringBuilder();
                while (i<n && (Character.isJavaIdentifierPart(src.charAt(i)))) { sb.append(src.charAt(i)); i++; }
                String id = sb.toString();
                if ("true".equals(id)) out.add(new Token(T.TRUE, id));
                else if ("false".equals(id)) out.add(new Token(T.FALSE, id));
                else if ("null".equals(id)) out.add(new Token(T.NULL, id));
                else out.add(new Token(T.IDENT, id));
                continue;
            }
            throw new RuntimeException("Bad char: " + c);
        }
        return out;
    }
}

