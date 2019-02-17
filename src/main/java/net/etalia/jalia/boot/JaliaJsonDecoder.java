package net.etalia.jalia.boot;

import java.util.List;
import java.util.Map;
import net.etalia.jalia.JaliaException;
import net.etalia.jalia.ObjectMapper;
import net.etalia.jalia.TypeUtil;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JaliaJsonDecoder extends JaliaCodecSupport implements HttpMessageDecoder<Object> {

    public JaliaJsonDecoder(ObjectMapper objectMapper, MimeType[] mimeTypes) {
        super(objectMapper, mimeTypes);
    }

    @Override
    public Map<String, Object> getDecodeHints(ResolvableType actualType, ResolvableType elementType,
            ServerHttpRequest request, ServerHttpResponse response) {
        return Hints.none();
    }

    @Override
    public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
        return supportsMimeType(mimeType);
    }

    @Override
    public Mono<Object> decodeToMono(org.reactivestreams.Publisher<DataBuffer> inputStream,
            ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return decode(inputStream, elementType, mimeType, hints).singleOrEmpty();
    }

    @Override
    public Flux<Object> decode(org.reactivestreams.Publisher<DataBuffer> inputStream,
            ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {


        MethodParameter param = getParameter(elementType);
        Class<?> contextClass = (param != null ? param.getContainingClass() : null);

        //JavaType javaType = getJavaType(elementType.getType(), contextClass);
        //Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);

        return Flux.from(inputStream).map(data -> {
            try {
                Object value = objectMapper.readValue(data.asInputStream(), TypeUtil.get(elementType.getType()));
                if (!Hints.isLoggingSuppressed(hints)) {
                    LogFormatUtils.traceDebug(logger, traceOn -> {
                        String formatted = LogFormatUtils.formatValue(value, !traceOn);
                        return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
                    });
                }
                return value;
            }
            catch (JaliaException ex) {
                throw new DecodingException("JSON decoding error: ", ex);
            }
        });
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return this.mimeTypes;
    }

}
