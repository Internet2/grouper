/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperRemedyUtils {

  /**
   * 
   */
  public GrouperRemedyUtils() {
  }

  /**
   * folder for duo groups with colon appended
   * @return the config folder for duo groups
   */
  public static String configFolderForRemedyGroups() {
  
    //get the configs and register the quartz job
    //# put groups in here which go to duo, the name in duo will be the extension here
    //grouperDuo.folder.name.withDuoGroups = a:b:c
    String duoFolderName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperRemedy.folder.name.withRemedyGroups");
    if (!duoFolderName.endsWith(":")) {
      duoFolderName += ":";
    }
    return duoFolderName;
  }

  /**
   * subject attribute to get the remedy username from the subject, could be "id" for subject id
   * @return the subject attribute name
   */
  public static String configSubjectAttributeForRemedyUsername() {
    return GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperRemedy.subjectAttributeForRemedyUsername");
  }
  
  /**
   * sources for subjects
   * @return the config sources for subjects
   */
  public static Set<String> configSourcesForSubjects() {
  
    //# put the comma separated list of sources to send to duo
    //grouperDuo.sourcesForSubjects = someSource
    String sourcesForSubjectsString = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperRemedy.sourcesForSubjects");
    
    return GrouperClientUtils.splitTrimToSet(sourcesForSubjectsString, ",");

  }

  /**
   * must be in stem and not have invalid suffix
   * @param groupName
   * @return true if valid group name
   */
  public static boolean validRemedyGroupName(String groupName) {
    
    String boxFolderName = configFolderForRemedyGroups();
  
    if (groupName == null || !groupName.startsWith(boxFolderName)) {
      return false;
    }
    
    groupName = groupName.substring(boxFolderName.length());
    
    if (groupName.length() > 30) {
      return false;
    }

    //must be directly in folder
    if (groupName.contains(":")) {
      return false;
    }

    //cant be include/exclude and not overall
    List<String> groupNameSuffixesToIgnore = GrouperClientUtils.splitTrimToList(GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.ignoreGroupSuffixes"), ",");
    for (String suffixToIgnore : GrouperClientUtils.nonNull(groupNameSuffixesToIgnore)) {
      if (groupName.endsWith(suffixToIgnore)) {
        return false;
      }
    }

    return true;
  }

  /**
   * escape url chars (e.g. a # is %23)
   * @param string input
   * @return the encoded string
   */
  public static String escapeUrlEncode(String string) {
    String result = null;
    try {
      result = URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }

}
