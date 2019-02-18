package net.etalia.jalia.boot;

import java.util.List;
import java.util.Map;
import net.etalia.jalia.JaliaException;
import net.etalia.jalia.ObjectMapper;
import net.etalia.jalia.TypeUtil;
import org.reactivestreams.Publisher;
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
    public Mono<Object> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType,
            Map<String, Object> hints) {
        return internalDecode(inputStream, hints, TypeUtil.get(elementType.getType())).singleOrEmpty();
    }

    @Override
    public Flux<Object> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType,
            Map<String, Object> hints) {
        return internalDecode(inputStream, hints, TypeUtil.getList(List.class, elementType.getType()));
    }

    private Flux<Object> internalDecode(Publisher<DataBuffer> inputStream, Map<String, Object> hints, TypeUtil type) {
        FluxSequenceInputStream fluxin = new FluxSequenceInputStream();
        inputStream.subscribe(fluxin);
        try {
            Object value = objectMapper.readValue(fluxin, type);
            if (!Hints.isLoggingSuppressed(hints)) {
                LogFormatUtils.traceDebug(logger, traceOn -> {
                    String formatted = LogFormatUtils.formatValue(value, !traceOn);
                    return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
                });
            }
            if (value instanceof Iterable) {
                return Flux.fromIterable((Iterable) value);
            } else {
                return Flux.just(value);
            }
        } catch (JaliaException ex) {
            throw new DecodingException("JSON decoding error: ", ex);
        }
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return mimeTypes;
    }

}
