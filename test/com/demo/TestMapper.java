package com.demo;

import frinko.sql.renderer.api.Param;
import frinko.sql.renderer.api.RenderedSql;

public interface TestMapper {
    RenderedSql rawOrder(@Param("order") String order);
    RenderedSql paramMulti(@Param("uid") Long uid, @Param("uname") String uname);
}
