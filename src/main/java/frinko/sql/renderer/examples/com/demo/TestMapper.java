package frinko.sql.renderer.examples.com.demo;

import frinko.sql.renderer.api.Param;

public interface TestMapper {
    String rawOrder(@Param("order") String order);
    String paramMulti(@Param("uid") Long uid, @Param("uname") String uname);
}