import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import frinko.sql.renderer.api.RenderedSql;
import frinko.sql.renderer.api.SqlRenderEngine;
import frinko.sql.renderer.api.Param;
import frinko.sql.renderer.config.RenderOptions;
import com.demo.UserMapper;
import com.demo.TestMapper;
import com.demo.PojoMapper;

public class SqlRendererTests {
    static class Addr { public String city; }
    static class User { public Long id; public String name; public Addr addr; }
    enum E { A, B }

    static int pass = 0, fail = 0;

    public static void main(String[] args) {
        RenderOptions opts = new RenderOptions();
        opts.allowedRawPlaceholders.add("name DESC");
        opts.allowedRawPlaceholders.add("id DESC");
        SqlRenderEngine engine = SqlRenderEngine.fromXmlBaseDirs(Arrays.asList(Paths.get("examples/mapper1")), opts);

        RenderedSql r1 = engine.renderByNamespace("com.demo.UserMapper", "selectById", new HashMap<String,Object>(){{ put("id", 1001L); put("name","Tom"); }});
        assertEq("selectById", r1.getSql(), "SELECT id, name, age, city FROM t_user  WHERE id = 1001  AND name = 'Tom'");

        List<Integer> ids = Arrays.asList(1,2,3);
        RenderedSql r2 = engine.renderByNamespace("com.demo.UserMapper", "selectIn", new HashMap<String,Object>(){{ put("ids", ids); }});
        assertEq("selectIn", r2.getSql(), "SELECT * FROM t_user  WHERE id IN ( 1 , 2 , 3 )");
        RenderedSql r3 = engine.renderByMapper(UserMapper.class, "selectOrder", "name DESC");
        assertEq("orderByRaw", r3.getSql(), "SELECT * FROM t_user ORDER BY name DESC");

        User u = new User(); Addr a = new Addr(); a.city = "Shanghai"; u.addr = a;
        RenderedSql r4 = engine.renderByNamespace("com.demo.UserMapper", "nestedPojo", new HashMap<String,Object>(){{ put("user", u); }});
        assertEq("nestedPojo", r4.getSql(), "SELECT * FROM t_addr  WHERE city = 'Shanghai'");

        RenderedSql s1 = engine.renderByNamespace("com.demo.TestMapper", "paramSingle", 100L);
        assertContains("paramSingle", s1.getSql(), "id = 100");

        RenderedSql s2 = engine.renderByMapper(TestMapper.class, "paramMulti", 10L, "A");
        assertContains("paramMulti", s2.getSql(), "id = 10");
        assertContains("paramMulti2", s2.getSql(), "name = 'A'");

        RenderedSql s3 = engine.renderByMapper(TestMapper.class, "rawOrder", "id DESC");
        assertEq("rawOrder", s3.getSql(), "SELECT * FROM t_user ORDER BY id DESC");

        RenderedSql s4 = engine.renderByNamespace("com.demo.TestMapper", "ifTests", new HashMap<String,Object>(){{ put("x", null); put("s", "" ); put("z", 0 ); }});
        assertNotContains("ifNulls", s4.getSql(), "id = ");
        assertNotContains("ifEmpty", s4.getSql(), "name = ");
        assertNotContains("ifZero", s4.getSql(), "age = ");

        RenderedSql s5a = engine.renderByNamespace("com.demo.TestMapper", "chooseTests", new HashMap<String,Object>(){{ put("a", 1 ); }});
        assertContains("chooseA", s5a.getSql(), "id = 1");
        RenderedSql s5b = engine.renderByNamespace("com.demo.TestMapper", "chooseTests", new HashMap<String,Object>(){{ put("b", "BB" ); }});
        assertContains("chooseB", s5b.getSql(), "name = 'BB'");
        RenderedSql s5c = engine.renderByNamespace("com.demo.TestMapper", "chooseTests", new HashMap<String,Object>());
        assertContains("chooseOther", s5c.getSql(), "city = 'X'");

        RenderedSql s6 = engine.renderByNamespace("com.demo.TestMapper", "trimTests", new HashMap<String,Object>(){{ put("name","N"); put("city","C"); }});
        assertEq("trimTests", s6.getSql(), "SELECT * FROM t_user  WHERE name = 'N', city = 'C'");

        RenderedSql s7 = engine.renderByNamespace("com.demo.TestMapper", "setTests", new HashMap<String,Object>(){{ put("name","N"); put("age",20); put("id",1); }});
        assertContains("setTests-part1", s7.getSql(), "SET name = 'N',  age = 20");
        assertContains("setTests-part2", s7.getSql(), "WHERE id = 1");

        RenderedSql s8 = engine.renderByNamespace("com.demo.TestMapper", "foreachList", new HashMap<String,Object>(){{ put("ids", Arrays.asList(1,2)); }});
        assertEq("foreachList", s8.getSql(), "SELECT * FROM t_user  WHERE id IN ( 1 , 2 )");

        RenderedSql s9 = engine.renderByNamespace("com.demo.TestMapper", "foreachArray", new HashMap<String,Object>(){{ put("arr", new int[]{4,5} ); }});
        assertEq("foreachArray", s9.getSql(), "SELECT * FROM t_user  WHERE id IN ( 4 , 5 )");

        Map<String,Integer> mp = new LinkedHashMap<>(); mp.put("a", 7); mp.put("b", 8);
        RenderedSql s10 = engine.renderByNamespace("com.demo.TestMapper", "foreachMap", new HashMap<String,Object>(){{ put("mp", mp ); }});
        assertEq("foreachMap", s10.getSql(), "SELECT * FROM t_user  WHERE id IN ( 7 , 8 )");

        RenderedSql s11 = engine.renderByNamespace("com.demo.TestMapper", "includeWithProp", new HashMap<String,Object>());
        assertContains("includeWithProp", s11.getSql(), "SELECT id, name, age, city FROM t_user");

        RenderedSql s12 = engine.renderByNamespace("com.demo.TestMapper", "includeFrag", new HashMap<String,Object>(){{ put("x", 3 ); }});
        assertEq("includeFrag", s12.getSql(), "SELECT * FROM t_user WHERE  col = 3");

        RenderedSql s13 = engine.renderByNamespace("com.demo.TestMapper", "bindTests", new HashMap<String,Object>(){{ put("a", 9 ); }});
        assertEq("bindTests", s13.getSql(), "SELECT * FROM t_user WHERE id = 10");

        User u2 = new User(); Addr a2 = new Addr(); a2.city = "SZ"; u2.addr = a2;
        RenderedSql s14 = engine.renderByNamespace("com.demo.TestMapper", "nested", new HashMap<String,Object>(){{ put("user", u2 ); }});
        assertEq("nested", s14.getSql(), "SELECT * FROM t_addr  WHERE city = 'SZ'");

        RenderOptions o2 = new RenderOptions();
        o2.allowedRawPlaceholders.add("id");
        o2.boolAsNumber = true;
        SqlRenderEngine engine2 = SqlRenderEngine.fromXmlBaseDirs(Arrays.asList(Paths.get("examples/mapper1")), o2);
        Map<String,Object> lit = new HashMap<>();
        lit.put("s", "X'");
        lit.put("d", LocalDate.of(2020,1,2));
        lit.put("dt", LocalDateTime.of(2020,1,2,3,4,5));
        lit.put("t", LocalTime.of(11,22,33));
        lit.put("n", new BigDecimal("123.45"));
        lit.put("b", Boolean.TRUE);
        lit.put("e", E.A);
        lit.put("u", null);
        RenderedSql s15 = engine2.renderByNamespace("com.demo.TestMapper", "literals", lit);
        assertContains("literals1", s15.getSql(), "s = 'X'''");
        assertContains("literals2", s15.getSql(), "n = 123.45");
        assertContains("literals3", s15.getSql(), "b = 1");
        assertContains("literals4", s15.getSql(), "e = 'A'");
        assertContains("literals5", s15.getSql(), "u = NULL");

        User pu = new User(); pu.id = 77L; pu.name = "PU"; Addr pa = new Addr(); pa.city = "HZ"; pu.addr = pa;
        RenderedSql p1 = engine.renderByNamespace("com.demo.PojoMapper", "byEntity", pu);

        assertContains("pojo-id", p1.getSql(), "id = 77");
        assertContains("pojo-name", p1.getSql(), "name = 'PU'");
        assertContains("pojo-city", p1.getSql(), "city = 'HZ'");
        RenderedSql p2 = engine.renderByMapper(PojoMapper.class, "byEntity", pu);
        assertContains("pojo-mapper-id", p2.getSql(), "id = 77");
        assertContains("pojo-mapper-name", p2.getSql(), "name = 'PU'");
        assertContains("pojo-mapper-city", p2.getSql(), "city = 'HZ'");
        RenderedSql p3 = engine.renderByMapper(PojoMapper.class, "byEntityParam", pu);
        System.out.println(p3);
        assertContains("pojo-param-id", p3.getSql(), "id = 77");
        assertContains("pojo-param-name", p3.getSql(), "name = 'PU'");
        assertContains("pojo-param-city", p3.getSql(), "city = 'HZ'");

        System.out.println("PASS=" + pass + " FAIL=" + fail);
        if (fail > 0) throw new RuntimeException("Tests failed");
    }

    static void assertEq(String name, String a, String b){ if (a.equals(b)) { pass++; System.out.println("PASS " + name); } else { fail++; System.out.println("FAIL " + name + "\nexpect=" + b + "\nactual=" + a); } }
    static void assertContains(String name, String a, String sub){ if (a.contains(sub)) { pass++; System.out.println("PASS " + name); } else { fail++; System.out.println("FAIL " + name + "\nneed=" + sub + "\nactual=" + a); } }
    static void assertNotContains(String name, String a, String sub){ if (!a.contains(sub)) { pass++; System.out.println("PASS " + name); } else { fail++; System.out.println("FAIL " + name + "\nshould-not=" + sub + "\nactual=" + a); } }
}