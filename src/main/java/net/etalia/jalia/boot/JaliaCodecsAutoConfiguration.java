package net.etalia.jalia.boot;

import net.etalia.jalia.DefaultOptions;
import net.etalia.jalia.EntityFactory;
import net.etalia.jalia.JsonContext;
import net.etalia.jalia.ObjectMapper;
import net.etalia.jalia.OutField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.support.DefaultClientCodecConfigurer;
import org.springframework.util.MimeType;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for
 * {@link org.springframework.core.codec.Encoder Encoders} and
 * {@link org.springframework.core.codec.Decoder Decoders}.
 *
 */
@Configuration
@ConditionalOnClass(CodecConfigurer.class)
@AutoConfigureAfter(CodecsAutoConfiguration.class)
public class JaliaCodecsAutoConfiguration {

    private static final MimeType[] EMPTY_MIME_TYPES = {};

    @Configuration
    @ConditionalOnClass(ObjectMapper.class)
    static class JacksonCodecConfiguration {

        @Autowired
        private JaliaProperties jaliaProperties;

        @Bean
        @ConditionalOnBean(ObjectMapper.class)
        public CodecCustomizer jaliaCodecCustomizer(ObjectMapper objectMapper) {
            return (configurer) -> {
                ObjectMapper useMapper = objectMapper;
                OutField fields = null;
                if (configurer instanceof DefaultClientCodecConfigurer) {
                    useMapper = new ObjectMapper();
                    useMapper.setEntityNameProvider(objectMapper.getEntityNameProvider());
                    useMapper.setEntityFactory(new IdOnlyFactoryWrapper(objectMapper.getEntityFactory()));
                    useMapper.setOption(DefaultOptions.ALWAYS_SERIALIZE_ON_DEMAND_ONLY, true);
                    useMapper.setOption(DefaultOptions.ALWAYS_ALLOW_NEW_INSTANCES, true);
                    useMapper.setOption(DefaultOptions.ALWAYS_ALLOW_ENTITY_PROPERTY_CHANGES, true);
                    useMapper.setOption(DefaultOptions.OVERRIDE_IGNORES, true);
                    fields = OutField.getRoot("*");
                }
                CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
                if (jaliaProperties.isInstallCodec()) {
                    defaults.jackson2JsonDecoder(
                            new JaliaJsonDecoder(useMapper, EMPTY_MIME_TYPES));
                    defaults.jackson2JsonEncoder(
                            new JaliaJsonEncoder(useMapper, EMPTY_MIME_TYPES, fields));
                }
            };
        }

        private static class IdOnlyFactoryWrapper implements EntityFactory {

            private final EntityFactory delegate;

            public IdOnlyFactoryWrapper(EntityFactory delegate) {
                this.delegate = delegate;
            }

            @Override
            public Object getId(Object entity, JsonContext context) {
                return delegate.getId(entity, context);
            }

            @Override
            public Object buildEntity(Class<?> clazz, Object id, JsonContext context) {
                return null;
            }

            @Override
            public Object prepare(Object obj, boolean serializing, JsonContext context) {
                return obj;
            }

            @Override
            public Object finish(Object obj, boolean serializing, JsonContext context) {
                return obj;
            }
        }
    }
}
