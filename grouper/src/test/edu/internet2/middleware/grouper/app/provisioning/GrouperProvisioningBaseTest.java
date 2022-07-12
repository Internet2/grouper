package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Base test for provisioning
 */
public class GrouperProvisioningBaseTest extends GrouperTest {
  
  public GrouperProvisioningBaseTest(String name) {
    super(name);
  }
  
  public GrouperProvisioningBaseTest() {
    super();
  }
  
  /**
   * @param grouperProvisioner
   * @return GrouperProvisioningOutput
   */
  public static GrouperProvisioningOutput fullProvision(GrouperProvisioner grouperProvisioner) {
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    GrouperUtil.sleep(1000);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
   
    return grouperProvisioningOutput;
  }
  
}
