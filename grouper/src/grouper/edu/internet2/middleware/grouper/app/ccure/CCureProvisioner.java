package edu.internet2.middleware.grouper.app.ccure;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class CCureProvisioner extends GrouperProvisioner {

    protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
        return CCureTargetDao.class;
    }

    @Override
    protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
        return CCureConfiguration.class;
    }
}
