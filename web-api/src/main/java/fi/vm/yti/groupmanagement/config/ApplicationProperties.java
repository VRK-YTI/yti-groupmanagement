package fi.vm.yti.groupmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("application")
@Component
public final class ApplicationProperties {

    ApplicationProperties(@Value("${fake.login.allowed:false}") boolean fakeLoginAllowed) {
        this.fakeLoginAllowed = fakeLoginAllowed;
    }

    private String codeListUrl;
    private String dataModelUrl;
    private String terminologyUrl;
    private String commentsUrl;
    private boolean devMode;
    private String env;
    private boolean fakeLoginAllowed;

    public String getCodeListUrl() {
        return codeListUrl;
    }

    public void setCodeListUrl(String codeListUrl) {
        this.codeListUrl = codeListUrl;
    }

    public String getDataModelUrl() {
        return dataModelUrl;
    }

    public void setDataModelUrl(String dataModelUrl) {
        this.dataModelUrl = dataModelUrl;
    }

    public String getTerminologyUrl() {
        return terminologyUrl;
    }

    public void setTerminologyUrl(String terminologyUrl) {
        this.terminologyUrl = terminologyUrl;
    }

    public String getCommentsUrl() {
        return commentsUrl;
    }

    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    public boolean getDevMode() {
        return this.devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public String getEnv() { return env; }

    public void setEnv(String env) { this.env = env; }

    public boolean isFakeLoginAllowed() { return fakeLoginAllowed; }

    public void setFakeLoginAllowed(boolean fakeLoginAllowed) {
        this.fakeLoginAllowed = fakeLoginAllowed; }

}