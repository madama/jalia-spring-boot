package net.etalia.jalia.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;
import net.etalia.jalia.EntityNameProvider;

/**
 * Entity name provider that scans for JPA repositories to gather entity classes.
 */
public class JpaNameProvider implements EntityNameProvider {

    private EntityManager entityManager;
    private Map<String, String> packagePrefixes = new HashMap<>();

    private final Map<String, Class<?>> namesToClass = new HashMap<String, Class<?>>();
    private final Map<Class<?>, String> classToNames = new HashMap<Class<?>, String>();

    public JpaNameProvider() {
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setPackagePrefixes(Map<String, String> packagePrefixes) {
        this.packagePrefixes = packagePrefixes;
    }

    public void addPackagePrefix(String packageName, String prefix) {
        packagePrefixes.put(packageName, prefix);
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
            String packageName = javaType.getPackage().getName();
            int packageLength = Integer.MAX_VALUE;
            String prefix = null;
            for (Map.Entry<String, String> entry : packagePrefixes.entrySet()) {
                String key = entry.getKey();
                if (packageName.startsWith(key) && packageLength > key.length()) {
                    prefix = entry.getValue();
                    if (prefix.length() > 1) {
                        prefix += ".";
                    }
                    packageLength = key.length();
                }
            }
            if (prefix != null) {
                register(prefix + javaType.getName().substring(packageLength + 1), javaType);
            } else {
                register(javaType.getSimpleName(), javaType);
            }
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
     * thing by {@link #scanEntities()} and classes already registered by this methos will be ignored there.
     */
    protected void doCustomSetup() {
        // Enpty
    }

    public String getEntityName(Class<?> clazz) {
        String name = classToNames.get(clazz);
        if (name != null) {
            return name;
        }
        if (clazz.getSuperclass() != null) {
            return getEntityName(clazz.getSuperclass());
        }
        return null;
    }

    public Class<?> getEntityClass(String name) {
        return namesToClass.get(name);
    }
}
