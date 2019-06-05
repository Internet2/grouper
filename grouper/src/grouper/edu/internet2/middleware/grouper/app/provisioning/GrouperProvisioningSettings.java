package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningSettings {
  
  private static final Pattern grouperProvisioningTargetKey = Pattern.compile("^provisioning\\.target\\.(\\w+)\\.key$");
  
  private static Map<String, GrouperProvisioningTarget> targets = new HashMap<String, GrouperProvisioningTarget>();
  
  static {
    populateTargets();
  }
  
  private static void populateTargets() {
    
    Map<String, String> propertiesMap = GrouperConfig.retrieveConfig().propertiesMap(grouperProvisioningTargetKey);
    
    for (Entry<String, String> entry: propertiesMap.entrySet()) {
          
      String property = entry.getKey();
      
      // key is the configured key, the value of .key
      String key = entry.getValue();
      
      Matcher matcher = grouperProvisioningTargetKey.matcher(property);
      
      if (matcher.matches()) {
        // name is the part of the property key
        String name = matcher.group(1);
        
        String groupAllowedToAssign = GrouperConfig.retrieveConfig().propertyValueString("provisioning.target."+name+".groupAllowedToAssign", null);
        boolean allowAssignmentsOnlyOnOneStem = GrouperConfig.retrieveConfig().propertyValueBoolean("provisioning.target."+name+".allowAssignmentsOnlyOnOneStem", false);
        boolean readOnly = GrouperConfig.retrieveConfig().propertyValueBoolean("provisioning.target."+name+".readOnly", false);
        
        GrouperProvisioningTarget target = new GrouperProvisioningTarget(key, name);
        target.setGroupAllowedToAssign(groupAllowedToAssign);
        target.setAllowAssignmentsOnlyOnOneStem(allowAssignmentsOnlyOnOneStem);
        target.setReadOnly(readOnly);
        targets.put(name, target);
      }
      
    }
    
  }
  
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
   * @return targets
   */
  public static Map<String, GrouperProvisioningTarget> getTargets() {
    return targets;
  }

}
