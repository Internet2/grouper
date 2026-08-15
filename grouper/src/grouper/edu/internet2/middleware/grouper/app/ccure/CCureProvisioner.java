package edu.internet2.middleware.grouper.app.ccure;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * CCure clearance-pair provisioner. Grouper groups map to CCure Clearances and Grouper entities to
 * CCure Personnel records; provisioning a membership creates the PersonnelClearancePair that grants
 * a person a clearance.
 *
 * Personnel and Clearance records themselves are managed in CCure, not by Grouper -- this
 * provisioner only pairs objects that already exist on both sides.
 */
public class CCureProvisioner extends GrouperProvisioner {

    protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
        return CCureTargetDao.class;
    }

    @Override
    protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
        return CCureConfiguration.class;
    }

    @Override
    protected Class<? extends GrouperProvisioningTargetNativeSync> grouperProvisioningTargetNativeSyncClass() {
        return CCureProvisioningTargetNativeSync.class;
    }
}
