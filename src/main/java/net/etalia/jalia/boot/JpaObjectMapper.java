package net.etalia.jalia.boot;

import net.etalia.jalia.*;
import net.etalia.jalia.stream.JsonReader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static net.etalia.jalia.boot.IdPathRequestBodyMethodProcessor.REQUEST_ID;
import static net.etalia.jalia.boot.IdPathRequestBodyMethodProcessor.REQUEST_ID_CLASS;

public class JpaObjectMapper extends ObjectMapper {

    public Object readValue(JsonContext ctx, Object pre, TypeUtil hint) {
        return readValueTransactional(ctx, pre, hint);
    }

    @Transactional(readOnly=true)
    public Object readValueTransactional(JsonContext ctx, Object pre, TypeUtil hint) {
        if (pre == null) {
            // Use the _ID_ attribute set by IdPathRequestBody and IdPathRequestBodyProcessor
            if (RequestContextHolder.getRequestAttributes() != null) {
                String id = (String) RequestContextHolder.getRequestAttributes().getAttribute(REQUEST_ID, RequestAttributes.SCOPE_REQUEST);
                if (id != null) {
                    Class<?> clazz = (Class<?>) RequestContextHolder.getRequestAttributes().getAttribute(REQUEST_ID_CLASS, RequestAttributes.SCOPE_REQUEST);
                    pre = getEntityFactory().buildEntity(clazz, id, ctx);
                    if (pre == null) {
                        throw new JaliaException("Cannot find entity of type " + clazz + " with id " + id + " from path");
                    }
                    ctx.putLocalStack(BeanJsonDeSer.ALLOW_NEW, false);
                    RequestContextHolder.getRequestAttributes().removeAttribute(REQUEST_ID, RequestAttributes.SCOPE_REQUEST);
                    RequestContextHolder.getRequestAttributes().removeAttribute(REQUEST_ID_CLASS, RequestAttributes.SCOPE_REQUEST);
                }
            }
        }
        return super.readValue(ctx, pre, hint);
    }
}
