package fi.vm.yti.groupmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
public class VersionInformation {

    @Value(value = "${git.commit.id.abbrev:dev}")
    private String commitId;

    @Value(value = "${git.branch}")
    private String branch;

    public String getCommitId() {
        return commitId;
    }

    public String getBranch() {
        return branch;
    }
}
