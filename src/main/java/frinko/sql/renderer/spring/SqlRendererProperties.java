package frinko.sql.renderer.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sql-renderer")
public class SqlRendererProperties {
    private List<String> mapperLocations = new ArrayList<>();
    private List<String> mapperScanPackages = new ArrayList<>();
    private boolean exposeDefaultParamNames = false;
    private boolean checkRawPlaceholders = true;

    public List<String> getMapperLocations() { return mapperLocations; }
    public void setMapperLocations(List<String> mapperLocations) { this.mapperLocations = mapperLocations; }

    public List<String> getMapperScanPackages() { return mapperScanPackages; }
    public void setMapperScanPackages(List<String> mapperScanPackages) { this.mapperScanPackages = mapperScanPackages; }

    public boolean isExposeDefaultParamNames() { return exposeDefaultParamNames; }
    public void setExposeDefaultParamNames(boolean exposeDefaultParamNames) { this.exposeDefaultParamNames = exposeDefaultParamNames; }

    public boolean isCheckRawPlaceholders() { return checkRawPlaceholders; }
    public void setCheckRawPlaceholders(boolean checkRawPlaceholders) { this.checkRawPlaceholders = checkRawPlaceholders; }
}