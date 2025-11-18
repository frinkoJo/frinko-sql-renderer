package com.demo;

import frinko.sql.renderer.api.Param;
import frinko.sql.renderer.api.RenderedSql;

public interface PojoMapper {
    RenderedSql byEntity(Object user);
    RenderedSql byEntityParam(@Param("user") Object user);
}
