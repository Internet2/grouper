/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * group that is a require group
 */
public class RequireGroup {

  /**
   * name of require group
   */
  private String name;
  
  /**
   * group where if you are in it you can use the require group
   */
  private String allowedToUseGroup;
  
  /**
   * the actual require group
   */
  private Group requireGroup;
  
  /**
   * the actual require group
   * @return the requireGroup
   */
  public Group getRequireGroup() {
    return this.requireGroup;
  }
  
  /**
   * the actual require group
   * @param requireGroup1 the requireGroup to set
   */
  public void setRequireGroup(Group requireGroup1) {
    this.requireGroup = requireGroup1;
  }

  /**
   * name of require group
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * name of require group
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }
  
  /**
   * group where if you are in it you can use the require group
   * @return the allowedToUseGroup
   */
  public String getAllowedToUseGroup() {
    return this.allowedToUseGroup;
  }

  
  /**
   * group where if you are in it you can use the require group
   * @param allowedToUseGroup the allowedToUseGroup to set
   */
  public void setAllowedToUseGroup(String allowedToUseGroup) {
    this.allowedToUseGroup = allowedToUseGroup;
  }

  /**
   * 
   */
  public RequireGroup() {
  }

  /**
   * get all require groups configured
   * @param subject subject to retrieve require groups for (or null to return all)
   * @return the list of groups
   */
  public static List<RequireGroup> retrieveAllRequireGroups(final Subject subject) {

    //  # grouper.requireGroup.name.0 = ref:require
    //  # grouper.requireGroup.allowedToUse.0 = ref:requireCanUse

    final GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    final List<RequireGroup> results = new ArrayList<RequireGroup>();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        for (int i=0;i<100;i++) {

          String requireGroupNameKey = "grouper.requireGroup.name." + i;

          if (grouperConfig.containsKey(requireGroupNameKey)) {

            RequireGroup requireGroup = new RequireGroup();
            requireGroup.setName(grouperConfig.propertyValueString(requireGroupNameKey));

            if (StringUtils.isBlank(requireGroup.getName())) {
              continue;
            }
            
            String requireAllowedGroupKey = "grouper.requireGroup.allowedToUse." + i;
            
            if (grouperConfig.containsKey(requireAllowedGroupKey)) {
              final String requireGroupName = grouperConfig.propertyValueString(requireGroupNameKey);
              
              // if controlling who can use
              if (!StringUtils.isBlank(requireGroupName)) {
                if (subject != null && !PrivilegeHelper.isWheelOrRoot(subject)) {
                  Group requireGroupAllowedToUse = GroupFinder.findByName(grouperSession, requireGroupName, true);
                  
                  //if the current subject is not in the group, then not allowed
                  if (!requireGroupAllowedToUse.hasMember(subject)) {
                    continue;
                  }
                }
                requireGroup.setAllowedToUseGroup(requireGroupName);
              }
            }
            Group requireGroupGroup = GroupFinder.findByName(grouperSession, requireGroup.getName(), true);
            requireGroup.setRequireGroup(requireGroupGroup);
            results.add(requireGroup);
          }
        }
        return null;
      }
    });
    
    if (GrouperUtil.length(results) > 1) {
      Collections.sort(results, new Comparator<RequireGroup>() {

        public int compare(RequireGroup o1, RequireGroup o2) {
          if (o1 == o2) {
            return 0;
          }
          if (o1 == null) {
            return 1;
          }
          if (o2 == null) {
            return -1;
          }
          return o1.getName().compareTo(o2.getName());
            
        }
      });
    }
    
    return results;
  }

}
