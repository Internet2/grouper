package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningSettings {
  
  
  
  /**
   * if provisioning in ui is enabled
   * @return if provisioning in ui is enabled
   */
  public static boolean provisioningInUiEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("provisioningInUi.enable", false);
  }
  
  /**
   * 
   * @return the stem name with no last colon
   */
  public static String provisioningConfigStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("provisioningInUi.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":provisioning"), ":");
  }
  
  /**
   * all the provisioning targets
   * @return
   */
  public static List<String> getTargetNames() {
    
    //TODO: read targets from config using regex
    String allTargetsString = GrouperConfig.retrieveConfig().propertyValueString("provisioning.targets");
    
    if (!StringUtils.isBlank(allTargetsString)) {
      Set<String> targetsLabels = GrouperUtil.splitTrimToSet(allTargetsString, ",");
      return Collections.unmodifiableList(new ArrayList<String>(targetsLabels));
    }
    
    return Collections.unmodifiableList(new ArrayList<String>()); 
  }

}
