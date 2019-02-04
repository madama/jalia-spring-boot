package net.etalia.jalia.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnBean(RequestMappingHandlerAdapter.class)
public class IdPathRequestBodyMethodProcessorConfig {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Autowired
    private List<HttpMessageConverter<?>> converters;

    @Autowired
    private JaliaProperties jaliaProperties;

    @PostConstruct
    public void init() {
        if (!jaliaProperties.isWithPathRequestBody()) {
            return;
        }

        IdPathRequestBodyMethodProcessor processor = new IdPathRequestBodyMethodProcessor(converters);
        List<HandlerMethodArgumentResolver> mangledResolvers = new ArrayList<>();
        mangledResolvers.add(processor);
        mangledResolvers.addAll(requestMappingHandlerAdapter.getArgumentResolvers());
        requestMappingHandlerAdapter.setArgumentResolvers(mangledResolvers);
    }

}
