import frinko.sql.renderer.api.Param;

/**
 * <p>
 * </p>
 *
 * @author xph
 * @date 2025/11/13
 */
public interface PojoMapper {
    String byEntity(@Param("user") Object user);
    String byEntityParam(@Param("user") Object user);
}
