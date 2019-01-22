package net.etalia.jalia.boot;

import net.etalia.jalia.EntityFactory;
import net.etalia.jalia.JsonContext;

import javax.persistence.EntityManager;

public class JpaEntityFactory implements EntityFactory {

    private final EntityManager entityManager;

    public JpaEntityFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String getId(Object entity, JsonContext context) {
        Object identifier = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (identifier == null) {
            return null;
        }
        return identifier.toString();
    }

    public Object buildEntity(Class<?> clazz, String id, JsonContext context) {
        return entityManager.find(clazz, id);
    }

    public Object prepare(Object obj, boolean serializing, JsonContext context) {
        return obj;
    }

    public Object finish(Object obj, boolean serializing, JsonContext context) {
        return obj;
    }
}
