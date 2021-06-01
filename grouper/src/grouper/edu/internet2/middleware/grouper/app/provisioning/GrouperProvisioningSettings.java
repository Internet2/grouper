package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class GrouperProvisioningSettings {
  
  public final static ObjectMapper objectMapper = new ObjectMapper();
  
  static {
    objectMapper.setSerializationInclusion(Include.NON_NULL);
  }
  
  private static final Pattern grouperProvisioningTargetKey = Pattern.compile("^provisioner\\.(\\w+)\\.class$");
  
  private static ExpirableCache<Boolean, Map<String, GrouperProvisioningTarget>> __targetsCacheInternal;

  private static ExpirableCache<Boolean, Map<String, GrouperProvisioningTarget>> targetsCache() {
    if (__targetsCacheInternal == null) {
      __targetsCacheInternal = new ExpirableCache<Boolean, Map<String, GrouperProvisioningTarget>>(5);
      __targetsCacheInternal.registerDatabaseClearableCache("grouperProvisioningTargetsCache");
    }
    
    return __targetsCacheInternal;
  }
  
  public static void clearTargetsCache() {
    targetsCache().notifyDatabaseOfChanges();
    targetsCache().clear();
  }
  
  private static Map<String, GrouperProvisioningTarget> populateTargets() {
    
    Map<String, GrouperProvisioningTarget> result = new HashMap<String, GrouperProvisioningTarget>();

    Map<String, String> propertiesMap = GrouperLoaderConfig.retrieveConfig().propertiesMap(grouperProvisioningTargetKey);
    
    for (Entry<String, String> entry: propertiesMap.entrySet()) {
          
      String property = entry.getKey();
      
      Matcher matcher = grouperProvisioningTargetKey.matcher(property);
      
      if (matcher.matches()) {
        // name is the part of the property key
        String name = matcher.group(1);
        
        String groupAllowedToAssign = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner."+name+".groupAllowedToAssign", null);
        boolean allowAssignmentsOnlyOnOneStem = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner."+name+".allowAssignmentsOnlyOnOneStem", false);
        boolean readOnly = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner."+name+".readOnly", false);
        
        GrouperProvisioningTarget target = new GrouperProvisioningTarget(name, name);
        target.setGroupAllowedToAssign(groupAllowedToAssign);
        target.setAllowAssignmentsOnlyOnOneStem(allowAssignmentsOnlyOnOneStem);
        target.setReadOnly(readOnly);
        result.put(name, target);
      }
      
    }
    return result;
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
    return GrouperUtil.stripSuffix(GrouperConfig.retrieveConfig().propertyValueString("provisioningInUi.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":provisioning"), ":");
  }
  
  /**
   * all the provisioning targets
   * @return targets
   */
  public static Map<String, GrouperProvisioningTarget> getTargets(boolean useCache) {
    Map<String, GrouperProvisioningTarget> result = targetsCache().get(Boolean.TRUE);
    if (result == null  || !useCache) {
      result = populateTargets();
      targetsCache().put(Boolean.TRUE, result);
    }
    return result;
  }

}
