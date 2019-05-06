package net.etalia.jalia.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.etalia.jalia.OutField;
import net.etalia.jalia.spring.JaliaParametersFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
@EnableConfigurationProperties(JaliaProperties.class)
public class JaliaParametersFilterConfiguration {

    private final static Logger LOG = Logger.getLogger(JaliaParametersFilterConfiguration.class.getName());

    @Bean
    public FilterRegistrationBean<JaliaParametersFilter> jaliaFilter(JaliaProperties properties) {
        LOG.info("Installing Jalia parameters filter");
        FilterRegistrationBean<JaliaParametersFilter> registrationBean = new FilterRegistrationBean<JaliaParametersFilter>();
        registrationBean.setFilter(new JaliaParametersFilter());
        registrationBean.addInitParameter(JaliaParametersFilter.PARAMETER_NAME, properties.getFieldsParameter());
        registrationBean.addInitParameter(JaliaParametersFilter.GROUP_PARAMETER_NAME, properties.getGroupParameter());
        registrationBean.addUrlPatterns();

        try {
            ClassLoader cl = getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = new Resource[0];
            try {
                resources = resolver.getResources(properties.getGroupsResource());
            } catch (FileNotFoundException e) {
                LOG.log(Level.WARNING, "No Jalia groups found for '" + properties.getGroupsResource() + "'");
            }
            OutField.cleanGroups();
            for (Resource resource : resources) {
                LOG.fine("Reading groups from " + resource.getURI());
                try (InputStream resin = resource.getInputStream()) {
                    OutField.parseGroupsJson(new InputStreamReader(resin));
                }
            }
            LOG.info("Loaded " + OutField.getGroups().keySet().size() + " Jalia groups");
        } catch (IOException e) {
            throw new IllegalStateException("Error accessing Jalia groups on '" + properties.getGroupsResource() + "'",
                    e);
        }
        return registrationBean;
    }


}
