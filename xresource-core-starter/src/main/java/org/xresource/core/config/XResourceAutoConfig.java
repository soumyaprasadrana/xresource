package org.xresource.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.xresource.core.annotation.XResource;
import org.xresource.core.logging.XLogger;
import org.xresource.core.registry.XResourceMetadataRegistry;
import org.xresource.core.scanner.XMetadataScanner;
import org.xresource.core.util.XResourceEntityScanner;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

@Configuration
@ComponentScan("org.xresource.core")
@AutoConfiguration
@RequiredArgsConstructor
public class XResourceAutoConfig {

    @Value("${xresource.scan.base-package}")
    private String basePackage;
    private final Environment environment;
    private final XLogger log = XLogger.forClass(XResourceAutoConfig.class);

    @PostConstruct
    public void init() {
        XLogger.configureFromSpring(this.environment);
        log.success(
                "XLogger initialized with level: " + environment.getProperty("xresource.logging.level", "INFO"));

    }

    @Bean
    public XMetadataScanner xMetadataScanner(XResourceMetadataRegistry registry) {
        return new XMetadataScanner(registry);
    }

    @Bean
    public XResourceRegistrar xResourceRegistrar(
            EntityManagerFactory entityManagerFactory,
            XMetadataScanner metadataScanner,
            XResourceMetadataRegistry registry,
            ApplicationContext applicationContext) {
        return new XResourceRegistrar(entityManagerFactory, metadataScanner, registry, applicationContext, basePackage);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .modules(new Hibernate6Module())
                .mixIn(HibernateProxy.class, HibernateProxyMixin.class)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS) // Disable errors for empty beans
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT); // Handle empty strings as
                                                                                              // null
    }

    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public abstract static class HibernateProxyMixin {
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static class XResourceRegistrar {

        private static final XLogger log = XLogger.forClass(XResourceRegistrar.class);

        private final XMetadataScanner metadataScanner;
        private final String basePackage;

        public XResourceRegistrar(EntityManagerFactory entityManagerFactory,
                XMetadataScanner metadataScanner,
                XResourceMetadataRegistry registry,
                ApplicationContext applicationContext,
                String basePackage) {
            this.metadataScanner = metadataScanner;
            this.basePackage = basePackage;
            log.debug("XResourceRegistrar initialized with base package: %s", basePackage);
        }

        @PostConstruct
        public void init() {
            log.enter("init");

            log.info("Scanning for repositories annotated with @XResource in package: %s", basePackage);
            Set<Class<?>> resourceRepositories = XResourceEntityScanner.scanRepositoriesWithXResource(basePackage);
            log.debug("Found %s repositories with @XResource annotation", resourceRepositories.size());

            for (Class<?> repositoryClass : resourceRepositories) {
                log.trace("Processing repository:", repositoryClass.getName());

                XResource xResource = repositoryClass.getAnnotation(XResource.class);
                if (xResource == null) {
                    log.warn("Repository %s does not have @XResource annotation; skipping.", repositoryClass.getName());
                    continue;
                }

                Class<?> entityClass = extractEntityClassFromRepository(repositoryClass);
                if (entityClass != null) {
                    log.debug("Extracted entity class %s from repository %s", entityClass.getName(),
                            repositoryClass.getName());
                    log.trace("Invoking metadataScanner.scan with table: %s, entityClass: %s, repositoryClass: %s",
                            xResource.table(), entityClass.getName(), repositoryClass.getName());
                    metadataScanner.scan(xResource.table(), entityClass, repositoryClass);
                } else {
                    log.error("Could not determine entity class for repository: %s", repositoryClass.getName());
                }
            }

            log.success("Completed scanning and registration of XResource repositories.");
            log.exit("init", null);
        }

        @SuppressWarnings("unchecked")
        private Class<?> extractEntityClassFromRepository(Class<?> repositoryClass) {
            log.enter("extractEntityClassFromRepository", repositoryClass);

            for (Type genericInterface : repositoryClass.getGenericInterfaces()) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericInterface;
                    Type rawType = paramType.getRawType();

                    if (rawType instanceof Class && JpaRepository.class.isAssignableFrom((Class<?>) rawType)) {
                        Type entityType = paramType.getActualTypeArguments()[0];
                        if (entityType instanceof Class<?>) {
                            Class<?> entityClass = (Class<?>) entityType;
                            log.debug("Found entity class %s in repository %s", entityClass.getName(),
                                    repositoryClass.getName());
                            log.exit("extractEntityClassFromRepository", entityClass);
                            return entityClass;
                        }
                    }
                }
            }

            // If not found directly, check superclass (e.g., for proxies or intermediate
            // abstract repos)
            Class<?> superclass = repositoryClass.getSuperclass();
            if (superclass != null && !superclass.equals(Object.class)) {
                log.debug("Checking superclass %s for entity class", superclass.getName());
                return extractEntityClassFromRepository(superclass);
            }

            log.warn("Entity class not found for repository %s", repositoryClass.getName());
            log.exit("extractEntityClassFromRepository", null);
            return null;
        }

        @SuppressWarnings("unchecked")
        private Class<?> getEntityClassFromRepository(Class<?> repository) {
            log.enter("getEntityClassFromRepository", repository);

            for (Type type : repository.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType pt) {
                    Type rawType = pt.getRawType();
                    if (rawType instanceof Class<?> rawClass && JpaRepository.class.isAssignableFrom(rawClass)) {
                        Type entityType = pt.getActualTypeArguments()[0];
                        if (entityType instanceof Class<?> entityClass) {
                            log.debug("Found entity class %s in repository %s", entityClass.getName(),
                                    repository.getClass().getName());
                            log.exit("getEntityClassFromRepository", entityClass);
                            return entityClass;
                        }
                    }
                }
            }

            log.warn("Entity class not found for repository %s", repository.getClass().getName());
            log.exit("getEntityClassFromRepository", null);
            return null;
        }
    }

}
