package org.test.ia.mariadbtest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.ai.data")
public class ConfigProperties {
    private Boolean initializeStore;

    public Boolean getInitializeStore() {
        return initializeStore;
    }

    public void setInitializeStore(Boolean initializeStore) {
        this.initializeStore = initializeStore;
    }
}
