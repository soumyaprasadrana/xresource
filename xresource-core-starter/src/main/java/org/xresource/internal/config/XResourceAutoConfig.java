package org.xresource.internal.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.xresource.core.aco.ACOEngine;
import org.xresource.core.annotations.XResource;
import org.xresource.core.auth.XAccessEvaluatorDelegate;
import org.xresource.core.hook.XResourceHookRegistry;
import org.xresource.internal.intent.core.parser.IntentToJPQLTransformer;
import org.xresource.internal.intent.core.parser.model.IntentMeta;
import org.xresource.internal.intent.core.util.IntentsFileReader;
import org.xresource.internal.intent.core.util.JPQLExecutorUtility;
import org.xresource.core.logging.XLogger;
import org.xresource.internal.query.XQueryContextProvider;
import org.xresource.internal.query.XQueryExecutor;
import org.xresource.core.response.XResponseTranformersRegistry;
import org.xresource.core.validation.XValidatorRegistry;
import org.xresource.internal.actions.XActionExecutor;
import org.xresource.internal.auth.XAccessManager;
import org.xresource.internal.auth.XRoleBasedAccessEvaluator;
import org.xresource.internal.cron.XJobRegistry;
import org.xresource.internal.cron.XJobRunner;
import org.xresource.internal.exception.XInvalidConfigurationException;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XRelationshipMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;
import org.xresource.internal.scanner.XMetadataScanner;
import org.xresource.internal.util.XResourceGraphBuilder;
import org.xresource.internal.util.XResourceLinkResolver;
import org.xresource.internal.util.XResourceRepositoryScanner;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;

import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.xresource.internal.config.XResourceConfigProperties.BASE_PACKGE;
import static org.xresource.internal.config.XResourceConfigProperties.ACO_ENABLED;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@ComponentScan(basePackages = { "org.xresource.core", "org.xresource.internal" })
@AutoConfiguration
@RequiredArgsConstructor
public class XResourceAutoConfig {

    @Value(BASE_PACKGE)
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
    @ConditionalOnMissingBean(XAccessEvaluatorDelegate.class)
    public XAccessEvaluatorDelegate xAccessEvaluatorDelegate() {
        return new XAccessEvaluatorDelegate((type, level, resource, field, roles) -> {
            return level;
        });
    }

    @Bean
    @ConditionalOnMissingBean(XRoleBasedAccessEvaluator.class)
    public XRoleBasedAccessEvaluator xRoleBasedAccessEvaluator(XAccessEvaluatorDelegate xAccessEvaluatorDelegate) {
        return new XRoleBasedAccessEvaluator(xAccessEvaluatorDelegate);
    }

    @Bean
    @ConditionalOnMissingBean(XResponseTranformersRegistry.class)
    public XResponseTranformersRegistry xResponseTranformersRegistry() {
        XResponseTranformersRegistry registry = new XResponseTranformersRegistry();
        return registry;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XAccessManager.class)
    public XAccessManager xAccessManager(ObjectMapper objectMapper) {
        return new XAccessManager(objectMapper);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(IntentsFileReader.class)
    public IntentsFileReader intentsFileReader() {
        return new IntentsFileReader();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XJobRegistry.class)
    public XJobRegistry xJobRegistry() {
        return new XJobRegistry();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XJobRunner.class)
    @Order(2)
    public XJobRunner xJobRunner(XJobRegistry registry,
            XQueryExecutor executor, XResourceMetadataRegistry resourceRegistry, XQueryContextProvider xQueryProvider) {
        return new XJobRunner(registry, executor, resourceRegistry, xQueryProvider);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XActionExecutor.class)
    public XActionExecutor xActionExecutor() {
        return new XActionExecutor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XMetadataScanner.class)
    public XMetadataScanner xMetadataScanner(XResourceMetadataRegistry registry, XValidatorRegistry validatorRegistry) {
        return new XMetadataScanner(registry, validatorRegistry);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XResourceMetadataRegistry.class)
    public XQueryExecutor xQueryExecutor() {
        return new XQueryExecutor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XResourceHookRegistry.class)
    public XResourceHookRegistry xResourceHookRegistry() {
        return new XResourceHookRegistry();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XQueryContextProvider.class)
    public XQueryContextProvider xQueryContextProvider() {
        return new XQueryContextProvider();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XResourceMetadataRegistry.class)
    public XResourceMetadataRegistry xResourceMetadataRegistry() {
        return new XResourceMetadataRegistry();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XResourceLinkResolver.class)
    public XResourceLinkResolver xResourceLinkResolver() {
        return new XResourceLinkResolver();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XResourceRepositoryScanner.class)
    public XResourceRepositoryScanner xResourceRepositoryScanner() {
        return new XResourceRepositoryScanner();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public XResourceRegistrar xResourceRegistrar(
            EntityManagerFactory entityManagerFactory,
            XMetadataScanner metadataScanner,
            XResourceMetadataRegistry registry,
            ApplicationContext applicationContext,
            XResourceRepositoryScanner xResourceEntityScanner,
            @Value(ACO_ENABLED) boolean acoEnabled, IntentsFileReader intentsFileReader) {
        return new XResourceRegistrar(entityManagerFactory, metadataScanner, registry, applicationContext, basePackage,
                xResourceEntityScanner, acoEnabled, intentsFileReader);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .modules(new Hibernate6Module())
                .mixIn(HibernateProxy.class, HibernateProxyMixin.class)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

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

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XValidatorRegistry.class)
    public XValidatorRegistry xValidatorRegistry() {
        return new XValidatorRegistry();
    }

    public static class XResourceRegistrar {

        private static final XLogger log = XLogger.forClass(XResourceRegistrar.class);

        private final EntityManagerFactory entityManagerFactory;
        private final XMetadataScanner metadataScanner;
        private final String basePackage;
        private final XResourceRepositoryScanner xResourceEntityScanner;
        private final XResourceMetadataRegistry registry;
        private final boolean acoEnabled;
        private final IntentsFileReader intentsFileReader;

        public XResourceRegistrar(EntityManagerFactory entityManagerFactory,
                XMetadataScanner metadataScanner,
                XResourceMetadataRegistry registry,
                ApplicationContext applicationContext,
                String basePackage,
                XResourceRepositoryScanner xResourceEntityScanner, boolean acoEnabled,
                IntentsFileReader intentsFileReader) {
            this.entityManagerFactory = entityManagerFactory;
            this.metadataScanner = metadataScanner;
            this.basePackage = basePackage;
            this.xResourceEntityScanner = xResourceEntityScanner;
            this.registry = registry;
            this.acoEnabled = acoEnabled;
            this.intentsFileReader = intentsFileReader;
            log.debug("XResourceRegistrar initialized with base package: %s", basePackage);
        }

        @PostConstruct
        public void init() {
            log.enter("init");

            log.info("Scanning for repositories annotated with @Repository in package: %s", basePackage);
            Set<Class<?>> resourceRepositories = xResourceEntityScanner.scanRepositoriesWithXResource(basePackage);
            log.debug("Found %s repositories with @Repository annotation", resourceRepositories.size());

            for (Class<?> repositoryClass : resourceRepositories) {
                log.trace("Processing repository:%s", repositoryClass.getName());

                XResource xResource = repositoryClass.getAnnotation(XResource.class);
                if (xResource == null && !xResourceEntityScanner.isAutoScanEnabled()) {
                    log.warn("Repository %s does not have @XResource annotation; skipping.", repositoryClass.getName());
                    continue;
                }

                Class<?> entityClass = extractEntityClassFromRepository(repositoryClass);
                if (entityClass != null) {
                    String tableName = xResource != null ? xResource.table()
                            : xResource == null && xResourceEntityScanner.isAutoScanEnabled()
                                    ? extractTableNameFromEntityClass(entityClass)
                                    : null;
                    log.debug("Extracted entity class %s from repository %s", entityClass.getName(),
                            repositoryClass.getName());
                    log.trace("Invoking metadataScanner.scan with table: %s, entityClass: %s, repositoryClass: %s",
                            tableName, entityClass.getName(), repositoryClass.getName());
                    metadataScanner.scan(tableName, entityClass, repositoryClass);
                } else {
                    log.error("Could not determine entity class for repository: %s", repositoryClass.getName());
                }
            }

            log.success("Completed scanning and registration of XResource repositories.");
            if (acoEnabled)
                ACOEngine.getInstance(registry).intialize();
            log.info("Building Resource Relationship Graph");
            Map<String, Map<String, XRelationshipMetadata>> graph = XResourceGraphBuilder.getGraph(registry);

            /*
             * ---------- LOAD IQL FILES -----------
             */
            try {
                intentsFileReader.loadFiles(registry);
            } catch (Exception e) {
                if (e instanceof XInvalidConfigurationException) {
                    e.printStackTrace();
                    throw e;
                } else {
                    // ignore
                    e.printStackTrace();
                }
            }

            /*
             * Test code for intent execution ----- // TO BE REMOVE
             * 
             * try {
             * XResourceMetadata assetMeta = registry.getRegistry().get("Asset");
             * Map<String, IntentMeta> xIntents = assetMeta.getXIntents();
             * if (xIntents.size() > 0) {
             * String jpql =
             * IntentToJPQLTransformer.toJPQL(xIntents.get("assetComplexQuery1"), graph);
             * System.out.println("=====>DEBUGGGGGGGGG====> "
             * + jpql);
             * JPQLExecutorUtility jpqlExecutorUtility = new JPQLExecutorUtility(
             * this.entityManagerFactory.createEntityManager());
             * List<Object[]> result = jpqlExecutorUtility.executeQuery(jpql,
             * Map.of("regionName", "Region 5"));
             * System.out.println(result.toString());
             * }
             * } catch (Exception e) {
             * e.printStackTrace();
             * }
             */
            /**
             * Test code end ---------------------------------//
             */
            log.exit("init");

        }

        /**
         * Extracts the table name from the provided entity class.
         * The method first checks if the entity class is annotated with @Table.
         * If the @Table annotation is present, it returns the name specified in the
         * annotation.
         * If the @Table annotation is not present, it returns the class name as the
         * table name.
         *
         * @param clazz the entity class from which the table name will be extracted
         * @return the table name associated with the entity class
         */
        private String extractTableNameFromEntityClass(Class<?> clazz) {
            log.trace("Extracting table name from entity class: %s", clazz.getName());

            // Check if the class is annotated with @Table
            if (clazz.isAnnotationPresent(Table.class)) {
                Table tableAnnotation = clazz.getAnnotation(Table.class);
                String tableName = tableAnnotation.name();
                log.trace("Found @Table annotation. Table name: %s", tableName);
                return tableName;
            }

            // If the @Table annotation is not present, return the class name as the table
            // name
            String defaultTableName = clazz.getSimpleName();
            log.trace("No @Table annotation found. Using class name as table name: %s", defaultTableName);
            return defaultTableName;
        }

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
            log.exit("extractEntityClassFromRepository");
            return null;
        }

        @SuppressWarnings("unused")
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
            log.exit("getEntityClassFromRepository");
            return null;
        }
    }

}
