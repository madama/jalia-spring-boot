package net.etalia.jalia.boot;

import net.etalia.jalia.EntityFactory;
import net.etalia.jalia.JsonContext;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

public class JpaEntityFactory implements EntityFactory {

    private EntityManager entityManager;

    public JpaEntityFactory() {
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Object getId(Object entity, JsonContext context) {
        if (entity == null) return null;

        try {
            entityManager.getMetamodel().entity(entity.getClass());
        } catch (IllegalArgumentException ignored) {
            // It means the class is not an entity
            return null;
        }
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    }

    public Object buildEntity(Class<?> clazz, Object id, JsonContext context) {
        if (id == null) {
            return null;
        }
        EntityType<?> entity = entityManager.getMetamodel().entity(clazz);
        Class<?> javaType = entity.getIdType().getJavaType();
        if (javaType == null) {
            throw new IllegalStateException("Cannot find id type of entity " + clazz);
        }
        Object finalId = id;
        if (!javaType.isAssignableFrom(id.getClass())) {
            if (javaType.equals(Long.class)) {
                finalId = Long.parseLong(id.toString());
            }
        }
        return load(clazz, finalId);
    }

    /**
     * This method is intended to stub (using mockito spy) the load part in unit tests, while leaving all the
     * rest of the implementation.
     * @param clazz the entity class to load
     * @param id the id to load
     * @return the entity loaded from the db or null if the entity was not found
     */
    public Object load(Class<?> clazz, Object id) {
        return entityManager.find(clazz, id);
    }

    public Object prepare(Object obj, boolean serializing, JsonContext context) {
        return obj;
    }

    public Object finish(Object obj, boolean serializing, JsonContext context) {
        return obj;
    }
}
