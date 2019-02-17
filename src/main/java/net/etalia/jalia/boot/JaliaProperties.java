package net.etalia.jalia.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jalia")
public class JaliaProperties {

    @Value("fields")
    private String fieldsParameter;

    @Value("group")
    private String groupParameter;

    @Value("classpath:/jalia/group*.json")
    private String groupsResource;

    private boolean includeNulls;

    private boolean includeEmpty;

    private boolean prettyPrint;

    private boolean unrollObjects;

    @Value("true")
    private boolean withPathRequestBody;

    @Value("true")
    private boolean installConverter;

    @Value("true")
    private boolean installCodec;

    public String getFieldsParameter() {
        return fieldsParameter;
    }

    public void setFieldsParameter(String fieldsParameter) {
        this.fieldsParameter = fieldsParameter;
    }

    public String getGroupParameter() {
        return groupParameter;
    }

    public void setGroupParameter(String groupParameter) {
        this.groupParameter = groupParameter;
    }

    public boolean isIncludeNulls() {
        return includeNulls;
    }

    public void setIncludeNulls(boolean includeNulls) {
        this.includeNulls = includeNulls;
    }

    public boolean isIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isUnrollObjects() {
        return unrollObjects;
    }

    public void setUnrollObjects(boolean unrollObjects) {
        this.unrollObjects = unrollObjects;
    }

    public String getGroupsResource() {
        return groupsResource;
    }

    public void setGroupsResource(String groupsResource) {
        this.groupsResource = groupsResource;
    }

    public void setWithPathRequestBody(boolean withPathRequestBody) {
        this.withPathRequestBody = withPathRequestBody;
    }

    public boolean isWithPathRequestBody() {
        return withPathRequestBody;
    }

    public void setInstallConverter(boolean installConverter) {
        this.installConverter = installConverter;
    }

    public boolean isInstallConverter() {
        return installConverter;
    }

    public void setInstallCodec(boolean installCodec) {
        this.installCodec = installCodec;
    }

    public boolean isInstallCodec() {
        return installCodec;
    }
}
