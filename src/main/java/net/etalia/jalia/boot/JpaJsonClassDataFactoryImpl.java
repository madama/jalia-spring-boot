package net.etalia.jalia.boot;

import net.etalia.jalia.JsonClassData;
import net.etalia.jalia.JsonClassDataFactoryImpl;
import net.etalia.jalia.JsonContext;

public class JpaJsonClassDataFactoryImpl extends JsonClassDataFactoryImpl {

    @Override
    public JsonClassData getClassData(Class<?> clazz, JsonContext context) {
        JsonClassData ret = super.getClassData(clazz, context);
        if (!ret.isNew()) {
            return ret;
        }
        ret.ignoreGetter("hibernateLazyInitializer");
        ret.ignoreSetter("hibernateLazyInitializer");
        return ret;
    }
}
