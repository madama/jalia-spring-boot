package net.etalia.jalia.boot;

import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import net.etalia.jalia.DefaultOptions;
import net.etalia.jalia.EntityFactory;
import net.etalia.jalia.EntityNameProvider;
import net.etalia.jalia.JsonClassDataFactory;
import net.etalia.jalia.JsonClassDataFactoryImpl;
import net.etalia.jalia.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(JaliaProperties.class)
public class AutoConfiguration {

    private final static Logger LOG = Logger.getLogger(AutoConfiguration.class.getName());

    @ConditionalOnMissingBean({ObjectMapper.class, EntityManagerFactory.class})
    @Bean
    public ObjectMapper jaliaObjectMapper(JaliaProperties properties, EntityFactory entityfactory,
                                          EntityNameProvider nameProvider, JsonClassDataFactory classDataFactory) {
        LOG.info("Creating simple Jalia ObjectMapper");
        ObjectMapper mapper = new ObjectMapper();
        setupMapper(properties, entityfactory, nameProvider, classDataFactory, mapper);
        return mapper;
    }

    @ConditionalOnMissingBean({JsonClassDataFactory.class, EntityManagerFactory.class})
    @Bean
    public JsonClassDataFactory jsonClassDataFactory() {
        LOG.info("Creating Jalia Class Data Factory");
        return new JsonClassDataFactoryImpl();
    }

    @ConditionalOnBean(EntityManagerFactory.class)
    @ConditionalOnMissingBean(JsonClassDataFactory.class)
    @Bean
    public JsonClassDataFactory jpaJsonClassDataFactory() {
        LOG.info("Creating Jalia JPA Class Data Factory");
        return new JpaJsonClassDataFactoryImpl();
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
