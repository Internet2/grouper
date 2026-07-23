package edu.internet2.middleware.grouper.app.ccure;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class CCureConfiguration extends GrouperProvisioningConfiguration {

    private String externalSystemConfigId;

    @Override
    public void configureSpecificSettings() {
        this.externalSystemConfigId = this.retrieveConfigString("externalSystemConfigId", true);
    }

    public String getExternalSystemConfigId() {
        return externalSystemConfigId;
    }

    public void setExternalSystemConfigId(String externalSystemConfigId) {
        this.externalSystemConfigId = externalSystemConfigId;
    }
}
