package frinko.sql.renderer.spring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frinko.sql.renderer.api.SqlRenderEngine;
import frinko.sql.renderer.config.RenderOptions;
import frinko.sql.renderer.parser.XmlMapperParser;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

@Configuration
@ConditionalOnClass(ApplicationContext.class)
@EnableConfigurationProperties(SqlRendererProperties.class)
public class SqlRendererAutoConfiguration {
    @Bean
    public SqlRenderEngine sqlRenderEngine(SqlRendererProperties props) {
        RenderOptions options = new RenderOptions();
        options.exposeDefaultParamNames = props.isExposeDefaultParamNames();
        options.checkRawPlaceholders = props.isCheckRawPlaceholders();
        frinko.sql.renderer.internal.NamespaceRegistry reg = new frinko.sql.renderer.internal.NamespaceRegistry();
        XmlMapperParser parser = new XmlMapperParser(options, reg);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (String loc : props.getMapperLocations()) {
            try {
                for (Resource r : resolver.getResources(loc)) {
                    try (InputStream in = r.getInputStream()) { parser.parse(in); }
                }
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        return SqlRenderEngine.fromRegistry(reg, options);
    }

    @Bean
    public SmartInitializingSingleton sqlRendererMapperRegistrar(ApplicationContext ctx, SqlRenderEngine engine, SqlRendererProperties props) {
        return () -> {
            DefaultListableBeanFactory factory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
            Set<Class<?>> toRegister = new HashSet<>();
            SimpleMetadataReaderFactory mrf = new SimpleMetadataReaderFactory();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            for (String base : props.getMapperScanPackages()) {
                String pattern = "classpath*:" + base.replace('.', '/') + "/**/*.class";
                try {
                    for (Resource r : resolver.getResources(pattern)) {
                        MetadataReader mr = mrf.getMetadataReader(r);
                        if (mr.getClassMetadata().isInterface()) {
                            Class<?> clazz = Class.forName(mr.getClassMetadata().getClassName());
                            toRegister.add(clazz);
                        }
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            }
            for (Class<?> mapper : toRegister) {
                String beanName = Character.toLowerCase(mapper.getSimpleName().charAt(0)) + mapper.getSimpleName().substring(1);
                if (!factory.containsBean(beanName)) factory.registerSingleton(beanName, engine.mapper(mapper));
            }
        };
    }
}