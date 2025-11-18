package com.demo;

import frinko.sql.renderer.api.Param;
import frinko.sql.renderer.api.RenderedSql;

public interface UserMapper {
    RenderedSql selectOrder(@Param("orderBy") String orderBy);
}
