package net.etalia.jalia.boot;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.etalia.jalia.ObjectMapper;
import org.apache.commons.logging.Log;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpLogging;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;

public class JaliaCodecSupport {

    protected static final List<MimeType> DEFAULT_MIME_TYPES = Collections.unmodifiableList(
            Arrays.asList(
                    new MimeType("application", "json", StandardCharsets.UTF_8),
                    new MimeType("application", "*+json", StandardCharsets.UTF_8)));
    protected final Log logger = HttpLogging.forLogName(getClass());
    protected final ObjectMapper objectMapper;
    protected final List<MimeType> mimeTypes;

    public JaliaCodecSupport(ObjectMapper objectMapper, MimeType[] mimeTypes) {
        this.objectMapper = objectMapper;
        this.mimeTypes = !ObjectUtils.isEmpty(mimeTypes) ?
                Collections.unmodifiableList(Arrays.asList(mimeTypes)) : DEFAULT_MIME_TYPES;
    }

    @Nullable
    protected MethodParameter getParameter(ResolvableType type) {
        return type.getSource() instanceof MethodParameter ? (MethodParameter) type.getSource() : null;
    }

    protected boolean supportsMimeType(@Nullable MimeType mimeType) {
        return (mimeType == null || this.mimeTypes.stream().anyMatch(m -> m.isCompatibleWith(mimeType)));
    }
}
