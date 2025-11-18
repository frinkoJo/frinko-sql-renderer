package frinko.sql.renderer.examples.com.demo;

import frinko.sql.renderer.api.Param;

public interface UserMapper {
    String selectOrder(@Param("orderBy") String orderBy);
}

