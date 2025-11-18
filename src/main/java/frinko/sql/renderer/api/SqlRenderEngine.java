package frinko.sql.renderer.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import frinko.sql.renderer.config.RenderOptions;
import frinko.sql.renderer.internal.NamespaceRegistry;
import frinko.sql.renderer.parser.MapperScanner;
import frinko.sql.renderer.parser.XmlMapperParser;
import frinko.sql.renderer.parser.model.MappedStatement;
import frinko.sql.renderer.render.Context;
import frinko.sql.renderer.render.ParameterBinder;

public final class SqlRenderEngine {
    private final NamespaceRegistry registry;
    private final RenderOptions options;
    private final Map<Class<?>, Object> mapperProxyCache = new ConcurrentHashMap<>();

    private SqlRenderEngine(NamespaceRegistry registry, RenderOptions options){ this.registry=registry; this.options=options; }

    public static SqlRenderEngine fromXmlBaseDirs(List<Path> xmlBaseDirs){ return fromXmlBaseDirs(xmlBaseDirs, new RenderOptions()); }

    public static SqlRenderEngine fromXmlBaseDirs(List<Path> xmlBaseDirs, RenderOptions options){
        NamespaceRegistry reg = new NamespaceRegistry();
        MapperScanner scanner = new MapperScanner();
        XmlMapperParser parser = new XmlMapperParser(options, reg);
        for (Path p : scanner.scan(xmlBaseDirs)) parser.parse(p);
        return new SqlRenderEngine(reg, options);
    }

    public RenderedSql renderByNamespace(String namespace, String statementId, Object paramObject){
        MappedStatement ms = registry.getStatement(namespace, statementId);
        if (ms == null) throw new RuntimeException(namespace + ":" + statementId);
        Context ctx = new ParameterBinder(options).bindSingle(paramObject);
        StringBuilder out = new StringBuilder();
        ms.getRoot().render(ctx, out);
        return new RenderedSql(out.toString().trim(), ctx.snapshot());
    }

    public RenderedSql renderByMapper(Class<?> mapperType, String methodName, Object... args){
        Method m = null;
        for (Method mm : mapperType.getMethods()) { if (mm.getName().equals(methodName)) { m = mm; break; } }
        if (m == null) throw new RuntimeException(methodName);
        return renderByMethod(m, args);
    }

    /**
     * 获取 Mapper 代理对象，可以直接调用方法返回 RenderedSql
     * 示例: engine.mapper(UserMapper.class).selectOrder("name DESC")
     */
    @SuppressWarnings("unchecked")
    public <T> T mapper(Class<T> mapperInterface) {
        return (T) mapperProxyCache.computeIfAbsent(mapperInterface, clazz -> {
            return Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new MapperProxy(clazz)
            );
        });
    }

    /**
     * Mapper 动态代理处理器
     */
    private class MapperProxy implements InvocationHandler {
        private final Class<?> mapperInterface;

        MapperProxy(Class<?> mapperInterface) {
            this.mapperInterface = mapperInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 处理 Object 的方法
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            // 调用 SQL 渲染
            return renderByMethod(method, args == null ? new Object[0] : args);
        }
    }

    public RenderedSql renderByMethod(Method mapperMethod, Object... args){
        String namespace = mapperMethod.getDeclaringClass().getName();
        String id = mapperMethod.getName();
        MappedStatement ms = registry.getStatement(namespace, id);
        if (ms == null) throw new RuntimeException(namespace + ":" + id);
        Context ctx = new ParameterBinder(options).bindMethod(mapperMethod, args);
        StringBuilder out = new StringBuilder();
        ms.getRoot().render(ctx, out);
        return new RenderedSql(out.toString().trim(), ctx.snapshot());
    }

    /**
     * 通过 Mapper 接口类型和方法名简化调用（无参数）
     */
    public <T> RenderedSql render(Class<T> mapperType, String methodName){
        return renderByMapper(mapperType, methodName);
    }

    /**
     * 通过 Mapper 接口类型和方法名简化调用（1个参数）
     */
    public <T> RenderedSql render(Class<T> mapperType, String methodName, Object arg1){
        return renderByMapper(mapperType, methodName, arg1);
    }

    /**
     * 通过 Mapper 接口类型和方法名简化调用（2个参数）
     */
    public <T> RenderedSql render(Class<T> mapperType, String methodName, Object arg1, Object arg2){
        return renderByMapper(mapperType, methodName, arg1, arg2);
    }

    /**
     * 通过 Mapper 接口类型和方法名简化调用（3个参数）
     */
    public <T> RenderedSql render(Class<T> mapperType, String methodName, Object arg1, Object arg2, Object arg3){
        return renderByMapper(mapperType, methodName, arg1, arg2, arg3);
    }

    /**
     * 通过 MapperMethod 描述符简化调用（无参数）
     */
    public <T> RenderedSql render(MapperMethod<T> method){
        return renderByMapper(method.getMapperType(), method.getMethodName());
    }

    /**
     * 通过 MapperMethod 描述符简化调用（1个参数）
     */
    public <T> RenderedSql render(MapperMethod<T> method, Object arg1){
        return renderByMapper(method.getMapperType(), method.getMethodName(), arg1);
    }

    /**
     * 通过 MapperMethod 描述符简化调用（2个参数）
     */
    public <T> RenderedSql render(MapperMethod<T> method, Object arg1, Object arg2){
        return renderByMapper(method.getMapperType(), method.getMethodName(), arg1, arg2);
    }

    /**
     * 通过 MapperMethod 描述符简化调用（3个参数）
     */
    public <T> RenderedSql render(MapperMethod<T> method, Object arg1, Object arg2, Object arg3){
        return renderByMapper(method.getMapperType(), method.getMethodName(), arg1, arg2, arg3);
    }

    /**
     * 通过 MapperMethod 描述符简化调用（可变参数）
     */
    public <T> RenderedSql render(MapperMethod<T> method, Object... args){
        return renderByMapper(method.getMapperType(), method.getMethodName(), args);
    }
}

