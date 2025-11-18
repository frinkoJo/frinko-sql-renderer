package frinko.sql.renderer.examples;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import frinko.sql.renderer.api.RenderedSql;
import frinko.sql.renderer.api.SqlRenderEngine;
import frinko.sql.renderer.config.RenderOptions;


public class DemoMain {
    static class Addr { public String city; }
    static class User { public Long id; public String name; public Addr addr; }

    public static void main(String[] args) {
        RenderOptions opts = new RenderOptions();
        opts.allowedRawPlaceholders.add("name DESC");
        SqlRenderEngine engine = SqlRenderEngine.fromXmlBaseDirs(Arrays.asList(Paths.get("src/main/java/frinko/sql/renderer/examples/mapper1")), opts);

        RenderedSql r1 = engine.renderByNamespace("com.demo.UserMapper", "selectById", new HashMap<String,Object>(){{ put("id", 1001L); put("name","Tom"); }});
        System.out.println(r1.getSql());

        List<Integer> ids = Arrays.asList(1,2,3);
        RenderedSql r2 = engine.renderByNamespace("com.demo.UserMapper", "selectIn", new HashMap<String,Object>(){{ put("ids", ids); }});
        System.out.println(r2.getSql());

        //RenderedSql r3 = engine.renderByMapper(UserMapper.class, "selectOrder", "name DESC");
        //System.out.println(r3.getSql());

        User u = new User();
        Addr a = new Addr(); a.city = "Shanghai"; u.addr = a;
        RenderedSql r4 = engine.renderByNamespace("com.demo.UserMapper", "nestedPojo", new HashMap<String,Object>(){{ put("user", u); }});
        System.out.println(r4.getSql());
    }
}
