package net.etalia.jalia.boot;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type.PersistenceType;
import org.junit.Before;
import org.junit.Test;

public class JpaNameProviderTest {

    private EntityManager entityManager;
    private JpaNameProvider provider;

    @Before
    public void prepareEntities() {
        entityManager = mock(EntityManager.class);
        Metamodel model = mock(Metamodel.class);
        doReturn(model).when(entityManager).getMetamodel();
        Set<EntityType<?>> entities = new HashSet<>();
        EntityType<EntityA> type = mock(EntityType.class);
        doReturn(EntityA.class).when(type).getJavaType();
        doReturn(PersistenceType.ENTITY).when(type).getPersistenceType();
        entities.add(type);
        doReturn(entities).when(model).getEntities();
        provider = new JpaNameProvider();
        provider.setEntityManager(entityManager);
    }

    @Test
    public void normalSimpleName() {
        provider.scanEntities();
        assertThat(provider.getEntityName(EntityA.class), equalTo("EntityA"));
        assertThat(provider.getEntityClass("EntityA"), equalTo(EntityA.class));
    }

    @Test
    public void predefinedPrefix() {
        provider.addPackagePrefix(JpaNameProviderTest.class.getPackage().getName(), "test");
        provider.scanEntities();
        assertThat(provider.getEntityName(EntityA.class), equalTo("test.EntityA"));
        assertThat(provider.getEntityClass("test.EntityA"), equalTo(EntityA.class));
    }

    @Test
    public void predefinedPrefixOnParentPackage() {
        String packageName = JpaNameProviderTest.class.getPackage().getName();
        provider.addPackagePrefix(packageName.substring(0, packageName.lastIndexOf('.')), "test");
        provider.scanEntities();
        assertThat(provider.getEntityName(EntityA.class), equalTo("test.boot.EntityA"));
        assertThat(provider.getEntityClass("test.boot.EntityA"), equalTo(EntityA.class));
    }

    @Test
    public void emptyPrefixOnParentPackage() {
        String packageName = JpaNameProviderTest.class.getPackage().getName();
        provider.addPackagePrefix(packageName.substring(0, packageName.lastIndexOf('.')), "");
        provider.scanEntities();
        assertThat(provider.getEntityName(EntityA.class), equalTo("boot.EntityA"));
        assertThat(provider.getEntityClass("boot.EntityA"), equalTo(EntityA.class));
    }
}