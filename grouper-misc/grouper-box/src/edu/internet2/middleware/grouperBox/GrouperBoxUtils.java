/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperBoxUtils {

  /**
   * 
   */
  public GrouperBoxUtils() {
  }

  /**
   * folder for duo groups with colon appended
   * @return the config folder for duo groups
   */
  public static String configFolderForBoxGroups() {
  
    //get the configs and register the quartz job
    //# put groups in here which go to duo, the name in duo will be the extension here
    //grouperDuo.folder.name.withDuoGroups = a:b:c
    String duoFolderName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.folder.name.withBoxGroups");
    if (!duoFolderName.endsWith(":")) {
      duoFolderName += ":";
    }
    return duoFolderName;
  }

  /**
   * subject attribute to get the box username from the subject, could be "id" for subject id
   * @return the subject attribute name
   */
  public static String configSubjectAttributeForBoxUsername() {
    return GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.subjectAttributeForBoxUsername");
  }
  
  /**
   * sources for subjects
   * @return the config sources for subjects
   */
  public static Set<String> configSourcesForSubjects() {
  
    //# put the comma separated list of sources to send to duo
    //grouperDuo.sourcesForSubjects = someSource
    String sourcesForSubjectsString = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.sourcesForSubjects");
    
    return GrouperClientUtils.splitTrimToSet(sourcesForSubjectsString, ",");
  }

  /**
   * must be in stem and not have invalid suffix
   * @param groupName
   * @return true if valid group name
   */
  public static boolean validBoxGroupName(String groupName) {
    
    String boxFolderName = configFolderForBoxGroups();
  
    if (!groupName.startsWith(boxFolderName)) {
      return false;
    }
    
    groupName = groupName.substring(boxFolderName.length());
    
    //must be directly in folder
    if (groupName.contains(":")) {
      return false;
    }
  
    //cant be include/exclude and not overall
    List<String> groupNameSuffixesToIgnore = GrouperClientUtils.splitTrimToList(GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.ignoreGroupSuffixes"), ",");
    for (String suffixToIgnore : GrouperClientUtils.nonNull(groupNameSuffixesToIgnore)) {
      if (groupName.endsWith(suffixToIgnore)) {
        return false;
      }
    }
  
    return true;
  }

}
