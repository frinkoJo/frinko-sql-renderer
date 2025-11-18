package frinko.sql.renderer.expr;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private List<Lexer.Token> ts; private int i;

    public Ast.Node parse(String src){ this.ts=new Lexer().lex(src); this.i=0; return expr(); }

    private Ast.Node expr(){ return or(); }
    private Ast.Node or(){ Ast.Node a = and(); while(match(Lexer.T.OR)) a = new Ast.Binary("||", a, and()); return a; }
    private Ast.Node and(){ Ast.Node a = eq(); while(match(Lexer.T.AND)) a = new Ast.Binary("&&", a, eq()); return a; }
    private Ast.Node eq(){ Ast.Node a = rel(); while(true){ if(match(Lexer.T.EQ)) a=new Ast.Binary("==",a,rel()); else if(match(Lexer.T.NE)) a=new Ast.Binary("!=",a,rel()); else break; } return a; }
    private Ast.Node rel(){ Ast.Node a = add(); while(true){ if(match(Lexer.T.GT)) a=new Ast.Binary(">",a,add()); else if(match(Lexer.T.GE)) a=new Ast.Binary(">=",a,add()); else if(match(Lexer.T.LT)) a=new Ast.Binary("<",a,add()); else if(match(Lexer.T.LE)) a=new Ast.Binary("<=",a,add()); else break; } return a; }
    private Ast.Node add(){ Ast.Node a = mul(); while(true){ if(match(Lexer.T.PLUS)) a=new Ast.Binary("+",a,mul()); else if(match(Lexer.T.MINUS)) a=new Ast.Binary("-",a,mul()); else break; } return a; }
    private Ast.Node mul(){ Ast.Node a = unary(); while(true){ if(match(Lexer.T.MUL)) a=new Ast.Binary("*",a,unary()); else if(match(Lexer.T.DIV)) a=new Ast.Binary("/",a,unary()); else if(match(Lexer.T.MOD)) a=new Ast.Binary("%",a,unary()); else break; } return a; }
    private Ast.Node unary(){ if(match(Lexer.T.NOT)) return new Ast.Unary("!", unary()); if(match(Lexer.T.PLUS)) return new Ast.Unary("+", unary()); if(match(Lexer.T.MINUS)) return new Ast.Unary("-", unary()); return primary(); }
    private Ast.Node primary(){
        if(match(Lexer.T.LP)) { Ast.Node e = expr(); expect(Lexer.T.RP); return e; }
        if(peek(Lexer.T.STRING)) return new Ast.Literal(next().s);
        if(peek(Lexer.T.NUMBER)) { String s = next().s; if(s.indexOf('.')>=0) return new Ast.Literal(Double.valueOf(s)); else return new Ast.Literal(Long.valueOf(s)); }
        if(match(Lexer.T.TRUE)) return new Ast.Literal(Boolean.TRUE);
        if(match(Lexer.T.FALSE)) return new Ast.Literal(Boolean.FALSE);
        if(match(Lexer.T.NULL)) return new Ast.Literal(null);
        return identChain();
    }

    private Ast.Node identChain(){
        Lexer.Token id = expect(Lexer.T.IDENT);
        if(match(Lexer.T.LP)){
            List<Ast.Node> args = new ArrayList<>();
            if(!peek(Lexer.T.RP)) { args.add(expr()); while(match(Lexer.T.COMMA)) args.add(expr()); }
            expect(Lexer.T.RP);
            return new Ast.Call(id.s, args);
        }
        java.util.List<String> props = new java.util.ArrayList<>();
        while(match(Lexer.T.DOT)) props.add(expect(Lexer.T.IDENT).s);
        if(props.isEmpty()) return new Ast.Var(id.s);
        return new Ast.Path(id.s, props);
    }

    private boolean match(Lexer.T t){ if(peek(t)) { i++; return true; } return false; }
    private Lexer.Token expect(Lexer.T t){ if(!peek(t)) throw new RuntimeException("Expect " + t); return ts.get(i++); }
    private boolean peek(Lexer.T t){ return i<ts.size() && ts.get(i).t==t; }
    private Lexer.Token next(){ return ts.get(i++); }
}

