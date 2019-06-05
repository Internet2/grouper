/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 *
 */
public class GrouperDuoUtils {

  /**
   * 
   */
  public GrouperDuoUtils() {
  }

  /**
   * cache the folder for duo
   */
  private static ExpirableCache<Boolean, Stem> duoStemCache = new ExpirableCache<Boolean, Stem>(5);

  /**
   * get duo stem from expirable cache or from database
   * duo stem
   * @param debugMap
   * @return the stem
   */
  public static Stem duoStem(Map<String, Object> debugMap) {
    
    Stem duoStem  = duoStemCache.get(Boolean.TRUE);
    if (debugMap != null) {
      debugMap.put("duoStemInExpirableCache", duoStem != null);
    }

    if (duoStem == null) {
      duoStem = duoStemHelper(debugMap);
      duoStemCache.put(Boolean.TRUE, duoStem);
    }
    return duoStem;
  }
  
  /**
   * duo stem
   * @param debugMap
   * @return the stem
   */
  public static Stem duoStemHelper(Map<String, Object> debugMap) {

    //# put groups in here which go to duo, the name in duo will be the extension here
    //grouperDuo.folder.name.withDuoGroups = duo
    String grouperDuoFolderName = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.folder.name.withDuoGroups");
    boolean useUiProvisioningConfiguration = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.use.ui.provisioning.configuration", true);
    
    if (useUiProvisioningConfiguration && !StringUtils.isBlank(grouperDuoFolderName)) {
      throw new RuntimeException("If you are using ui provisioning configuration, you cant configure a folder in the grouper-loader.properties 'grouperDuo.folder.name.withDuoGroups'!!!!");
    }
    
    Stem grouperDuoFolder = null;
    
    if (useUiProvisioningConfiguration) {
      
      String uiProvisioningTargetName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.ui.provisioning.targetName");
      
      if (debugMap != null) {
        debugMap.put("uiProvisioningTargetName", uiProvisioningTargetName);
      }
      
      List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)
          .addAttributeValuesOnAssignment(uiProvisioningTargetName)
          .assignNameOfAttributeDefName2(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)
          .addAttributeValuesOnAssignment2("true")
          .findStems());

      GrouperUtil.stemRemoveChildStemsOfTopStem(stems);
      
      if (debugMap != null) {
        debugMap.put("folderCount", GrouperUtil.length(stems));
      }
      
      if (GrouperUtil.length(stems) > 1) {
        throw new RuntimeException("Folder count can only be 0 or 1!!! " + GrouperUtil.length(stems));
      }
      if (GrouperUtil.length(stems) == 1) {
        grouperDuoFolder = stems.iterator().next();
      }
    } else {
    
      grouperDuoFolder = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperDuoFolderName, true);
    }
    return grouperDuoFolder;
  }
  
  /**
   * folder for duo groups, ends in colon
   * @return the config folder for duo groups
   */
  public static String configFolderForDuoGroups() {
    return duoStem(null).getName() + ":";
  }

  /**
   * subject attribute to get the duo username from the subject, could be "id" for subject id
   * @return the subject attribute name
   */
  public static String configSubjectAttributeForDuoUsername() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.subjectAttributeForDuoUsername");
  }
  
  /**
   * sources for subjects
   * @return the config sources for subjects
   */
  public static Set<String> configSourcesForSubjects() {
  
    //# put the comma separated list of sources to send to duo
    //grouperDuo.sourcesForSubjects = someSource
    String sourcesForSubjectsString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.sourcesForSubjects");
    
    return GrouperUtil.splitTrimToSet(sourcesForSubjectsString, ",");
  }

  /**
   * must be in stem and not have invalid suffix
   * @param groupName
   * @return true if valid group name
   */
  public static boolean validDuoGroupName(String groupName) {
    
    String duoFolderName = configFolderForDuoGroups();
  
    if (!groupName.startsWith(duoFolderName)) {
      return false;
    }
    
    groupName = groupName.substring(duoFolderName.length());
    
    //must be directly in folder
    if (groupName.contains(":")) {
      return false;
    }
  
    //cant be include/exclude and not overall
    if (groupName.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordExtensionSuffix())
        || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordAndIncludesExtensionSuffix())
        || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.includeExtensionSuffix())
        || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.excludeExtensionSuffix())) {
      return false;
    }
  
    return true;
  }

}
