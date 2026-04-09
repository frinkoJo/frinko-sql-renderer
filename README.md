# Frinko SQL Renderer

A lightweight SQL rendering engine for JPA / Spring Boot projects.

`Frinko SQL Renderer` extracts the dynamic SQL generation capability commonly associated with MyBatis and turns it into an independent library focused on one thing only: **rendering maintainable SQL from XML mappers and runtime parameters**.

It is especially useful in projects that primarily use JPA, but still need to handle complex native SQL in a cleaner and more structured way.

---

## Overview

In many JPA-based systems, simple queries are easy to express, but once the query logic becomes more dynamic, the code often starts to degrade:

- dynamic `WHERE` conditions
- optional update fields
- reusable SQL fragments
- batch `IN` queries
- conditional branches
- cross-database or highly customized native SQL

At that point, teams often fall back to `Native Query`, but the SQL usually ends up embedded in Java strings, which makes it difficult to read, reuse, and maintain.

This project solves that problem by separating **SQL rendering** from **SQL execution**:

- JPA remains the execution layer
- XML Mapper remains the SQL definition layer
- `Frinko SQL Renderer` becomes the rendering layer

---

## What It Solves

This library is designed to improve maintainability for complex native SQL scenarios.

It helps with:

- avoiding hand-written SQL string concatenation in Java
- organizing complex SQL in XML instead of scattered code
- reusing common SQL fragments
- expressing dynamic conditions clearly
- reducing errors around commas, `AND/OR`, and empty collections
- adding safer control over raw SQL placeholders

---

## Core Features

### MyBatis-style XML Mapper parsing

Supports parsing:

- `<mapper>`
- `<select>`
- `<insert>`
- `<update>`
- `<delete>`
- `<sql>`

Statements and reusable fragments are registered by namespace and ID, then rendered on demand.

### Dynamic SQL node support

Supported dynamic tags:

- `<if>`
- `<choose>`
- `<when>`
- `<otherwise>`
- `<trim>`
- `<where>`
- `<set>`
- `<foreach>`
- `<bind>`
- `<include>`

These tags cover the majority of common dynamic SQL use cases.

### Lightweight expression engine

The project includes a built-in expression parser and evaluator for `test` and `value` expressions.

Supported capabilities include:

- boolean operators: `&&`, `||`, `!`
- comparison operators: `==`, `!=`, `>`, `>=`, `<`, `<=`
- arithmetic operators: `+`, `-`, `*`, `/`, `%`
- variable lookup
- property path access such as `user.name`
- simple built-in functions

Built-in functions:

- `isEmpty(...)`
- `notEmpty(...)`
- `startsWith(...)`
- `endsWith(...)`
- `contains(...)`
- `trim(...)`

### Parameter binding

Supports binding parameters from:

- single objects
- `Map`
- multi-argument mapper methods
- `@Param` annotated parameters
- default names like `param1`, `param2`, `value`

### Mapper interface proxy support

In addition to rendering by namespace and statement ID, the engine can render SQL through mapper interfaces:

- `renderByNamespace(...)`
- `renderByMapper(...)`
- `renderByMethod(...)`
- `engine.mapper(YourMapper.class)`

### Spring Boot auto-configuration

The library includes Spring Boot integration for:

- loading mapper XML files
- creating the `SqlRenderEngine`
- scanning mapper interfaces
- registering mapper proxy beans automatically

### SQL literal rendering

The renderer handles common Java types when writing SQL literals:

- `String`
- `Character`
- `Boolean`
- `Number`
- `BigDecimal`
- `Enum`
- `Date`
- `LocalDate`
- `LocalDateTime`
- `LocalTime`

String escaping is also handled for SQL output.

---

## Design Highlights

### Focused responsibility

This project is not an ORM and not a full persistence framework.

Its responsibility is intentionally narrow:

**take XML + parameters, produce final SQL**

That narrow scope keeps the library lightweight and easy to integrate into existing JPA-based systems.

### AST-based rendering

The XML is not rendered through ad hoc string concatenation. It is first parsed into an internal node tree, then rendered recursively.

This makes the implementation:

- easier to extend
- easier to reason about
- easier to test
- cleaner to maintain

### Built-in expression parsing

Instead of depending on a large external expression framework, the project implements its own lightweight lexer, parser, and AST evaluator. This keeps behavior explicit and dependencies minimal.

### Safe handling of raw placeholders

The project clearly distinguishes between:

- `#{}` for rendered values
- `${}` for raw output

Raw placeholders can be restricted through an allowlist, which is especially useful for controlled dynamic fragments such as sort fields or sort directions.

### Practical SQL cleanup behavior

Several common dynamic SQL edge cases are handled automatically:

- leading `AND/OR` cleanup in `where`
- trailing comma cleanup in `set`
- `trim` prefix/suffix override support
- collection expansion in `foreach`
- configurable behavior for empty `foreach`

These details make the rendered SQL much more robust in real-world usage.

---

## Architecture

The codebase is roughly organized into four parts:

### Parser layer

Responsible for scanning XML files, parsing mapper content, and building internal statement models.

Key classes:

- `MapperScanner`
- `XmlMapperParser`
- `MappedStatement`
- `SqlFragment`
- `NamespaceRegistry`

### Expression layer

Responsible for tokenizing, parsing, and evaluating expressions used by dynamic SQL nodes.

Key classes:

- `Lexer`
- `Parser`
- `Ast`

### Render layer

Responsible for binding parameters, resolving properties, and rendering the final SQL output.

Key classes:

- `ParameterBinder`
- `Context`
- `PropertyAccessor`
- `SqlLiteralWriter`

### Spring integration layer

Responsible for Spring Boot configuration and mapper proxy registration.

Key classes:

- `SqlRendererAutoConfiguration`
- `SqlRendererProperties`

---

## Quick Start

### Maven

```xml
<dependency>
    <groupId>io.github.frinkojo</groupId>
    <artifactId>sql-renderer</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Mapper interface

```java
package com.example.mapper;

import frinko.sql.renderer.api.Param;
import frinko.sql.renderer.api.RenderedSql;

import java.util.List;

public interface UserQueryMapper {
    RenderedSql queryUsers(
        @Param("name") String name,
        @Param("status") Integer status,
        @Param("ids") List<Long> ids,
        @Param("orderBy") String orderBy
    );
}
```

### XML mapper

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<mapper namespace="com.example.mapper.UserQueryMapper">

    <sql id="baseColumns">
        id, username, status, created_at
    </sql>

    <select id="queryUsers">
        SELECT
        <include refid="baseColumns" />
        FROM user
        <where>
            <if test="notEmpty(name)">
                AND username LIKE #{name}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="notEmpty(ids)">
                AND id IN
                <foreach collection="ids" item="id" open="(" close=")" separator=",">
                    #{id}
                </foreach>
            </if>
        </where>
        <if test="notEmpty(orderBy)">
            ORDER BY ${orderBy}
        </if>
    </select>
</mapper>
```

### Standalone usage

```java
import frinko.sql.renderer.api.RenderedSql;
import frinko.sql.renderer.api.SqlRenderEngine;
import frinko.sql.renderer.config.RenderOptions;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

public class Demo {
    public static void main(String[] args) {
        RenderOptions options = new RenderOptions();
        options.allowedRawPlaceholders = new HashSet<>(Arrays.asList(
            "created_at desc",
            "created_at asc",
            "id desc"
        ));

        SqlRenderEngine engine = SqlRenderEngine.fromXmlBaseDirs(
            Arrays.asList(Paths.get("src/main/resources/mapper")),
            options
        );

        RenderedSql sql = engine.renderByMapper(
            com.example.mapper.UserQueryMapper.class,
            "queryUsers",
            "Tom%",
            1,
            Arrays.asList(1L, 2L, 3L),
            "created_at desc"
        );

        System.out.println(sql.getSql());
        System.out.println(sql.getParams());
    }
}
```

### Spring Boot configuration

```yaml
sql-renderer:
  mapper-locations:
    - classpath*:mapper/**/*.xml
  mapper-scan-packages:
    - com.example.mapper
  expose-default-param-names: true
  check-raw-placeholders: true
```

---

## Scope

`Frinko SQL Renderer` focuses on SQL rendering only.

It does not try to replace:

- JPA
- MyBatis
- JDBC
- query execution frameworks

Instead, it is best viewed as a small infrastructure component that improves how complex native SQL is defined, reused, and rendered.

---

## License

MIT
