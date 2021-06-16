package net.etalia.jalia.boot;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.etalia.jalia.DefaultOptions;
import net.etalia.jalia.JaliaException;
import net.etalia.jalia.ObjectMapper;
import net.etalia.jalia.OutField;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JaliaJsonEncoder extends JaliaCodecSupport implements HttpMessageEncoder<Object> {

    private static final byte[] NEWLINE_SEPARATOR = {'\n'};
    private static boolean logJson;
    private final OutField fields;

    public static void setLogJson(boolean logJson) {
        JaliaJsonEncoder.logJson = logJson;
    }

    public JaliaJsonEncoder(ObjectMapper objectMapper, MimeType[] mimeTypes, OutField fields) {
        super(objectMapper, mimeTypes);
        this.fields = fields;
    }

    @Override
    public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
        return supportsMimeType(mimeType);
    }

    @Override
    public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
            ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        Charset encoding = getJsonEncoding(mimeType);

        if (inputStream instanceof Mono) {
            return Mono.from(inputStream).map(value ->
                    encodeValue(value, mimeType, bufferFactory, elementType, hints, encoding)).flux();
        }

        for (MediaType streamingMediaType : getStreamingMediaTypes()) {
            if (streamingMediaType.isCompatibleWith(mimeType)) {
                objectMapper.setOption(DefaultOptions.PRETTY_PRINT, false);
                return Flux.from(inputStream).map(value -> {
                    DataBuffer buffer = encodeValue(value, mimeType, bufferFactory, elementType, hints, encoding);
                    buffer.write(NEWLINE_SEPARATOR);
                    return buffer;
                });
            }
        }

        ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, elementType);
        return Flux.from(inputStream).collectList().map(list ->
                encodeValue(list, mimeType, bufferFactory, listType, hints, encoding)).flux();
    }

    protected Charset getJsonEncoding(@Nullable MimeType mimeType) {
        Charset charset = StandardCharsets.UTF_8;
        if (mimeType != null && mimeType.getCharset() != null) {
            charset = mimeType.getCharset();
        }
        return charset;
    }

    @Override
    public DataBuffer encodeValue(Object value, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType,
            Map<String, Object> hints) {
        Charset encoding = getJsonEncoding(mimeType);
        return encodeValue(value, mimeType, bufferFactory, valueType, hints, encoding);
    }

    private DataBuffer encodeValue(Object value, @Nullable MimeType mimeType, DataBufferFactory bufferFactory,
            ResolvableType elementType, @Nullable Map<String, Object> hints, Charset encoding) {

        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
            });
        }

        DataBuffer buffer = bufferFactory.allocateBuffer();
        boolean release = true;
        OutputStream outputStream = buffer.asOutputStream();

        try {
            if (logJson) {
                String asString = objectMapper.writeValueAsString(value, fields);
                logger.info(asString);
                try {
                    outputStream.write(asString.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                objectMapper.writeValue(outputStream, fields, value);
            }
            release = false;
        }
        catch (JaliaException ex) {
            throw new EncodingException("JSON encoding error: ", ex);
        }
        finally {
            if (release) {
                DataBufferUtils.release(buffer);
            }
        }

        return buffer;
    }

    @Override
    public List<MimeType> getEncodableMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<MediaType> getStreamingMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_STREAM_JSON);
    }

    @Override
    public Map<String, Object> getEncodeHints(ResolvableType actualType, ResolvableType elementType,
            MediaType mediaType, ServerHttpRequest request, ServerHttpResponse response) {
        return Hints.none();
    }
}
