package net.etalia.jalia.boot;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import net.etalia.jalia.ObjectMapper;
import net.etalia.jalia.spring.JaliaHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class InstallHttpMessageConverter extends WebMvcConfigurationSupport {

    private final static Logger LOG = Logger.getLogger(InstallHttpMessageConverter.class.getName());

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JaliaProperties jaliaProperties;

    @Bean
    public JaliaHttpMessageConverter jaliaHttpMessageConverter() {
        if (jaliaProperties == null || !jaliaProperties.isInstallConverter()) {
            return null;
        }
        if (objectMapper == null) {
            LOG.info("Not installing Jalia message converter cause no ObjectMapper is available");
            return null;
        }
        LOG.info("Installing Jalia message converter");
        JaliaHttpMessageConverter converter = new JaliaHttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.addDefaultHttpMessageConverters(converters);
        if (jaliaProperties == null || !jaliaProperties.isInstallConverter()) {
            return;
        }
        JaliaHttpMessageConverter converter = jaliaHttpMessageConverter();
        if (converter == null) {
            return;
        }
        LOG.info("Forcing use of Jalia message converter");

        MediaType json = new MediaType("application","json");
        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> pre = iterator.next();
            for (MediaType mediaType : pre.getSupportedMediaTypes()) {
                if (mediaType.equals(json)) {
                    LOG.info("Removing " + pre.getClass().getName() + " cause it handles json");
                    iterator.remove();
                }
            }
        }

        converters.add(0, converter);
    }
}
