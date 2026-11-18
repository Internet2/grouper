/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;
import edu.internet2.middleware.subject.Subject;


/**
 * group that is a red button lockout group
 */
public class LockoutGroup {

  /**
   * logger
   */
  private static final Log LOG = GrouperUtil.getLog(LockoutGroup.class);

  /**
   * name of lockout group
   */
  private String name;
  
  /**
   * group where if you are in it you can use the lockout group
   */
  private String allowedToUseGroup;
  
  /**
   * the actual lockout group
   */
  private Group lockoutGroup;
  
  /**
   * the actual lockout group
   * @return the lockoutGroup
   */
  public Group getLockoutGroup() {
    return this.lockoutGroup;
  }
  
  /**
   * the actual lockout group
   * @param lockoutGroup1 the lockoutGroup to set
   */
  public void setLockoutGroup(Group lockoutGroup1) {
    this.lockoutGroup = lockoutGroup1;
  }

  /**
   * name of lockout group
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * name of lockout group
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }
  
  /**
   * group where if you are in it you can use the lockout group
   * @return the allowedToUseGroup
   */
  public String getAllowedToUseGroup() {
    return this.allowedToUseGroup;
  }

  
  /**
   * group where if you are in it you can use the lockout group
   * @param allowedToUseGroup the allowedToUseGroup to set
   */
  public void setAllowedToUseGroup(String allowedToUseGroup) {
    this.allowedToUseGroup = allowedToUseGroup;
  }

  /**
   * 
   */
  public LockoutGroup() {
  }

  /**
   * get all lockout groups configured
   * @param subject subject to retrieve lockout groups for (or null to return all)
   * @return the list of groups
   */
  public static List<LockoutGroup> retrieveAllLockoutGroups(final Subject subject) {

    //  # grouper.lockoutGroup.name.0 = ref:lockout
    //  # grouper.lockoutGroup.allowedToUse.0 = ref:lockoutCanUse

    final GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    final List<LockoutGroup> results = new ArrayList<LockoutGroup>();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        for (int i=0;i<100;i++) {

          String lockoutGroupNameKey = "grouper.lockoutGroup.name." + i;

          if (grouperConfig.containsKey(lockoutGroupNameKey)) {

            LockoutGroup lockoutGroup = new LockoutGroup();
            lockoutGroup.setName(grouperConfig.propertyValueString(lockoutGroupNameKey));

            // GrouperUtil.isBlank instead of StringUtils so one file compiles on 5.x (shaded lang3) through 7.x
            if (GrouperUtil.isBlank(lockoutGroup.getName())) {
              continue;
            }
            
            String lockoutAllowedGroupKey = "grouper.lockoutGroup.allowedToUse." + i;
            
            if (grouperConfig.containsKey(lockoutAllowedGroupKey)) {
              // read the allowedToUse group name (this used to re-read the lockout group name by mistake,
              // so the allowedToUse restriction was never applied)
              final String allowedToUseGroupName = grouperConfig.propertyValueString(lockoutAllowedGroupKey);
              
              // if controlling who can use
              if (!GrouperUtil.isBlank(allowedToUseGroupName)) {
                if (subject != null && !PrivilegeHelper.isWheelOrRoot(subject)) {
                  Group lockoutGroupAllowedToUse = GroupFinder.findByName(grouperSession, allowedToUseGroupName, false);
                  
                  // fail closed: if the allowedToUse group is misconfigured, nobody but wheel/root can use this entry
                  if (lockoutGroupAllowedToUse == null) {
                    LOG.warn("Cannot find lockout group allowedToUse group '" + allowedToUseGroupName 
                        + "' from config '" + lockoutAllowedGroupKey + "', so not allowing non-wheel subjects to use '" 
                        + lockoutGroup.getName() + "'");
                    continue;
                  }
                  
                  //if the current subject is not in the allowedToUse group, then not allowed
                  if (!lockoutGroupAllowedToUse.hasMember(subject)) {
                    continue;
                  }
                }
                lockoutGroup.setAllowedToUseGroup(allowedToUseGroupName);
              }
            }
            Group lockoutGroupGroup = GroupFinder.findByName(grouperSession, lockoutGroup.getName(), true);
            lockoutGroup.setLockoutGroup(lockoutGroupGroup);
            results.add(lockoutGroup);
          }
        }
        return null;
      }
    });
    
    if (GrouperUtil.length(results) > 1) {
      Collections.sort(results, new Comparator<LockoutGroup>() {

        public int compare(LockoutGroup o1, LockoutGroup o2) {
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
