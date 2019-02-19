package net.etalia.jalia.boot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import net.etalia.jalia.spring.JaliaHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@ConditionalOnBean(RequestMappingHandlerAdapter.class)
@AutoConfigureAfter({InstallHttpMessageConverter.class, WebMvcAutoConfiguration.class})
public class IdPathRequestBodyMethodProcessorConfig {

    private final static Logger LOG = Logger.getLogger(IdPathRequestBodyMethodProcessorConfig.class.getName());

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Autowired
    private JaliaHttpMessageConverter converter;

    @Autowired
    private JaliaProperties jaliaProperties;

    @PostConstruct
    public void init() {
        if (!jaliaProperties.isWithPathRequestBody()) {
            LOG.info("Not installing Jalia @IdPathRequestBody processor");
            return;
        }

        LOG.info("Installing Jalia @IdPathRequestBody processor");

        IdPathRequestBodyMethodProcessor processor = new IdPathRequestBodyMethodProcessor(
                Collections.singletonList(converter));
        List<HandlerMethodArgumentResolver> mangledResolvers = new ArrayList<>();
        mangledResolvers.add(processor);
        mangledResolvers.addAll(requestMappingHandlerAdapter.getArgumentResolvers());
        requestMappingHandlerAdapter.setArgumentResolvers(mangledResolvers);
    }

}
