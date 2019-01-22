package net.etalia.jalia.boot;

import net.etalia.jalia.EntityNameProvider;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Entity name provider that scans for JPA repositories to gather entity classes.
 */
public class JpaNameProvider implements EntityNameProvider {

    private final EntityManager entityManager;

    private final Map<String, Class<?>> namesToClass = new HashMap<String, Class<?>>();
    private final Map<Class<?>, String> classToNames = new HashMap<Class<?>, String>();

    public JpaNameProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PostConstruct
    protected void scanEntities() {
        doCustomSetup();
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        for (EntityType<?> entity : entities) {
            if (!entity.getPersistenceType().equals(Type.PersistenceType.ENTITY)) {
                continue;
            }
            Class<?> javaType = entity.getJavaType();
            // Skip if it was already registered someway
            if (classToNames.containsKey(javaType)) {
                continue;
            }
            register(javaType.getSimpleName(), javaType);
        }
    }

    protected void register(String name, Class<?> entityClass) {
        if (namesToClass.containsKey(name)) {
            throw new IllegalStateException("Name '" + name + "' cannot be mapped to " + entityClass +
                    " cause it's already mapped to " + namesToClass.get(name));
        }
        if (classToNames.containsKey(entityClass)) {
            throw new IllegalStateException("Class " + entityClass + " cannot be mapped to name '" + name +
                    " cause it's already mapped to " + classToNames.get(entityClass));
        }
        namesToClass.put(name, entityClass);
        classToNames.put(entityClass, name);
    }


    /**
     * Handly hook to setup some class->names bindings in a non standard way. This is called as first
     * thing by {@link #setup()} and classes already registered will be ignored.
     */
    protected void doCustomSetup() {
        // Enpty
    }

    public String getEntityName(Class<?> clazz) {
        return classToNames.get(clazz);
    }

    public Class<?> getEntityClass(String name) {
        return namesToClass.get(name);
    }
}
