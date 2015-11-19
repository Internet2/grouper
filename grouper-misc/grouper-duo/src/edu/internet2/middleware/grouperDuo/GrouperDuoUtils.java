/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.util.Set;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
   * folder for duo groups
   * @return the config folder for duo groups
   */
  public static String configFolderForDuoGroups() {
  
    //get the configs and register the quartz job
    //# put groups in here which go to duo, the name in duo will be the extension here
    //grouperDuo.folder.name.withDuoGroups = a:b:c
    String duoFolderName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.folder.name.withDuoGroups");
    if (!duoFolderName.endsWith(":")) {
      duoFolderName += ":";
    }
    return duoFolderName;
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
