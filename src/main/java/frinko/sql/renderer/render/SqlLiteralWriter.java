package frinko.sql.renderer.render;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import frinko.sql.renderer.config.RenderOptions;

public final class SqlLiteralWriter {
    private final RenderOptions options;
    public SqlLiteralWriter(RenderOptions options) { this.options = options; }

    public void write(Object v, StringBuilder out) {
        if (v == null) { out.append("NULL"); return; }
        if (v instanceof String) { out.append('\''); escapeString((String)v, out); out.append('\''); return; }
        if (v instanceof Character) { out.append('\''); escapeString(String.valueOf(v), out); out.append('\''); return; }
        if (v instanceof Boolean) { out.append(options.boolAsNumber ? (((Boolean)v) ? "1" : "0") : (((Boolean)v) ? "TRUE" : "FALSE")); return; }
        if (v instanceof BigDecimal) { out.append(((BigDecimal)v).toPlainString()); return; }
        if (v instanceof Number) { out.append(String.valueOf(v)); return; }
        if (v instanceof Enum) { out.append(options.enumAsOrdinal ? String.valueOf(((Enum<?>)v).ordinal()) : '\'' + ((Enum<?>)v).name() + '\''); return; }
        if (v instanceof Date) { writeDate((Date)v, out); return; }
        if (v instanceof LocalDate) { out.append('\'').append(DateTimeFormatter.ofPattern(options.datePattern).format((LocalDate)v)).append('\''); return; }
        if (v instanceof LocalDateTime) { out.append('\'').append(DateTimeFormatter.ofPattern(options.dateTimePattern).format((LocalDateTime)v)).append('\''); return; }
        if (v instanceof LocalTime) { out.append('\'').append(DateTimeFormatter.ofPattern(options.timePattern).format((LocalTime)v)).append('\''); return; }
        out.append('\''); escapeString(String.valueOf(v), out); out.append('\'');
    }

    private void escapeString(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') out.append("''"); else out.append(c);
        }
    }

    private void writeDate(Date d, StringBuilder out) {
        SimpleDateFormat fmt = new SimpleDateFormat(options.dateTimePattern);
        out.append('\'').append(fmt.format(Date.from(d.toInstant()))).append('\'');
    }
}

