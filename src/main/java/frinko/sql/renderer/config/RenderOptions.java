package frinko.sql.renderer.config;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public final class RenderOptions {
    public boolean boolAsNumber = false;
    public String datePattern = "yyyy-MM-dd";
    public String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
    public String timePattern = "HH:mm:ss";
    public ZoneId zoneId = ZoneId.systemDefault();
    public boolean enumAsOrdinal = false;

    public enum EmptyForeachBehavior { KEEP_EMPTY_PAIR, YIELD_FALSE_CONDITION }
    public EmptyForeachBehavior emptyForeachBehavior = EmptyForeachBehavior.KEEP_EMPTY_PAIR;

    /**
     * 允许在 ${} 中使用的原始占位符白名单
     */
    public Set<String> allowedRawPlaceholders = new HashSet<>();

    /**
     * 是否启用原始占位符检查，默认为 true（开启检查）
     * 当设置为 true 时，只有在 allowedRawPlaceholders 中的值才能用于 ${} 占位符
     * 当设置为 false 时，关闭检查，任何值都可以用于 ${} 占位符（不安全，可能存在 SQL 注入风险）
     */
    public boolean checkRawPlaceholders = true;

    public boolean failOnUnknownProperty = true;
    public boolean strictTextNodeTrim = true;
    public boolean exposeDefaultParamNames = true;
}

