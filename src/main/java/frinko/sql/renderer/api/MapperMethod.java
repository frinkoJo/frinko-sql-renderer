package frinko.sql.renderer.api;

/**
 * Mapper 方法描述符，用于流式调用
 */
public final class MapperMethod<T> {
    private final Class<T> mapperType;
    private final String methodName;

    private MapperMethod(Class<T> mapperType, String methodName) {
        this.mapperType = mapperType;
        this.methodName = methodName;
    }

    public static <T> MapperMethod<T> of(Class<T> mapperType, String methodName) {
        return new MapperMethod<>(mapperType, methodName);
    }

    public Class<T> getMapperType() {
        return mapperType;
    }

    public String getMethodName() {
        return methodName;
    }
}
