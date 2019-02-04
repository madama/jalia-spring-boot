package net.etalia.jalia.boot;

import net.etalia.jalia.*;
import net.etalia.jalia.spring.JaliaParametersFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(JaliaProperties.class)
public class AutoConfiguration {

    private final static Logger LOG = Logger.getLogger(AutoConfiguration.class.getName());

    @ConditionalOnMissingBean
    @Bean
    public FilterRegistrationBean<JaliaParametersFilter> jaliaFilter(JaliaProperties properties) {
        LOG.info("Installing Jalia parameters filter");
        FilterRegistrationBean<JaliaParametersFilter> registrationBean = new FilterRegistrationBean<JaliaParametersFilter>();
        registrationBean.setFilter(new JaliaParametersFilter());
        registrationBean.addInitParameter(JaliaParametersFilter.PARAMETER_NAME, properties.getFieldsParameter());
        registrationBean.addInitParameter(JaliaParametersFilter.GROUP_PARAMETER_NAME, properties.getGroupParameter());

        try {
            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = new Resource[0];
            try {
                resources = resolver.getResources(properties.getGroupsResource());
            } catch (FileNotFoundException e) {
                LOG.log(Level.WARNING,"No Jalia groups found for '" + properties.getGroupsResource() + "'");
            }
            for (Resource resource : resources) {
                LOG.fine("Reading groups from " + resource.getURI());
                try (InputStream resin = resource.getInputStream()) {
                    OutField.parseGroupsJson(new InputStreamReader(resin));
                }
            }
            LOG.info("Loaded " + OutField.getGroups().keySet().size() + " Jalia groups");
        } catch (IOException e) {
            throw new IllegalStateException("Error accessing Jalia groups on '" + properties.getGroupsResource() + "'", e);
        }
        return registrationBean;
    }

    @ConditionalOnMissingBean({ObjectMapper.class, EntityManagerFactory.class})
    @Bean
    public ObjectMapper jaliaObjectMapper(JaliaProperties properties, EntityFactory entityfactory,
                                          EntityNameProvider nameProvider, JsonClassDataFactory classDataFactory) {
        LOG.info("Creating simple Jalia ObjectMapper");
        ObjectMapper mapper = new ObjectMapper();
        setupMapper(properties, entityfactory, nameProvider, classDataFactory, mapper);
        return mapper;
    }

    @ConditionalOnMissingBean
    @Bean
    public JsonClassDataFactory jsonClassDataFactory() {
        LOG.info("Creating Jalia Class Data Factory");
        return new JsonClassDataFactoryImpl();
    }

    @ConditionalOnBean(EntityManagerFactory.class)
    @ConditionalOnMissingBean(EntityFactory.class)
    @Bean
    public JpaEntityFactory jpaEntityFactory(EntityManager entityManager) {
        LOG.info("Creating JPA Jalia Entity Factory");
        JpaEntityFactory jpaEntityFactory = new JpaEntityFactory();
        jpaEntityFactory.setEntityManager(entityManager);
        return jpaEntityFactory;
    }


    @ConditionalOnBean(EntityManagerFactory.class)
    @ConditionalOnMissingBean
    @Bean
    public EntityNameProvider jpaNameProvider(EntityManager entityManager) {
        LOG.info("Creating JPA Jalia Name Provider");
        JpaNameProvider jpaNameProvider = new JpaNameProvider();
        jpaNameProvider.setEntityManager(entityManager);
        return jpaNameProvider;
    }

    @ConditionalOnMissingBean(ObjectMapper.class)
    @ConditionalOnBean(EntityManagerFactory.class)
    @Bean
    public ObjectMapper jpaJaliaObjectMapper(JaliaProperties properties, EntityFactory entityfactory,
                                          EntityNameProvider nameProvider, JsonClassDataFactory classDataFactory) {
        LOG.info("Creating JPA Jalia ObjectMapper");
        JpaObjectMapper mapper = new JpaObjectMapper();
        setupMapper(properties, entityfactory, nameProvider, classDataFactory, mapper);
        return mapper;
    }

    private void setupMapper(JaliaProperties properties, EntityFactory entityfactory, EntityNameProvider nameProvider, JsonClassDataFactory classDataFactory, ObjectMapper mapper) {
        mapper.setEntityNameProvider(nameProvider);
        mapper.setEntityFactory(entityfactory);
        mapper.setClassDataFactory(classDataFactory);
        mapper.setOption(DefaultOptions.INCLUDE_EMPTY, properties.isIncludeEmpty());
        mapper.setOption(DefaultOptions.INCLUDE_NULLS, properties.isIncludeNulls());
        mapper.setOption(DefaultOptions.PRETTY_PRINT, properties.isPrettyPrint());
        mapper.setOption(DefaultOptions.UNROLL_OBJECTS, properties.isUnrollObjects());
    }
}
