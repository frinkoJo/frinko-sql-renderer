import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import frinko.sql.renderer.api.RenderedSql;
import frinko.sql.renderer.api.SqlRenderEngine;
import frinko.sql.renderer.config.RenderOptions;
import com.demo.UserMapper;
import com.demo.TestMapper;
import com.demo.PojoMapper;


public class MapperMethodDemo {
    static class Addr { public String city; }
    static class User { public Long id; public String name; public Addr addr; }

    public static void main(String[] args) {
        RenderOptions opts = new RenderOptions();
        //opts.allowedRawPlaceholders.add("name DESC");
        //opts.allowedRawPlaceholders.add("id DESC");
        opts.checkRawPlaceholders = false;
        SqlRenderEngine engine = SqlRenderEngine.fromXmlBaseDirs(Collections.singletonList(Paths.get("examples/mapper1")), opts);

        UserMapper userMapper = engine.mapper(UserMapper.class);
        RenderedSql r1 = userMapper.selectOrder("name DESC");
        System.out.println("UserMapper.selectOrder: " + r1.getSql());

        TestMapper testMapper = engine.mapper(TestMapper.class);
        RenderedSql r2 = testMapper.rawOrder("id DESC");
        System.out.println("TestMapper.rawOrder: " + r2.getSql());

        RenderedSql r3 = testMapper.paramMulti(10L, "Alice");
        System.out.println("TestMapper.paramMulti: " + r3.getSql());

        User u = new User(); 
        u.id = 77L; 
        u.name = "Bob";
        Addr a = new Addr(); 
        a.city = "Shanghai"; 
        u.addr = a;

        PojoMapper pojoMapper = engine.mapper(PojoMapper.class);
        RenderedSql r4 = pojoMapper.byEntity(u);
        System.out.println("PojoMapper.byEntity: " + r4.getSql());

        RenderedSql r5 = pojoMapper.byEntityParam(u);
        System.out.println("PojoMapper.byEntityParam: " + r5.getSql());

        System.out.println("\n链式调用示例:");
        System.out.println(engine.mapper(UserMapper.class).selectOrder("name DESC").getSql());
        System.out.println(engine.mapper(TestMapper.class).paramMulti(100L, "Tom").getSql());
    }
}
