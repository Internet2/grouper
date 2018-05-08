package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperDeprovisioningRealm implements Comparable<GrouperDeprovisioningRealm> {

  /**
   * label of realm from grouper.properties, e.g. "student" or "employee"
   */
  private String label;
  
  /**
   * if you are in this group then it means you are a member of that cohort
   */
  private String groupNameMeansInRealm;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningRealm.class);

  /**
   * label of realm from grouper.properties, e.g. "student" or "employee"
   * @return label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * label of realm from grouper.properties, e.g. "student" or "employee"
   * @param label1
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * if you are in this group then it means you are a member of that cohort
   * @return group name
   */
  public String getGroupNameMeansInRealm() {
    return this.groupNameMeansInRealm;
  }

  /**
   * if you are in this group then it means you are a member of that cohort
   * @param groupNameMeansInRealm1
   */
  public void setGroupNameMeansInRealm(String groupNameMeansInRealm1) {
    this.groupNameMeansInRealm = groupNameMeansInRealm1;
  }

  /**
   * 
   */
  @Override
  public int compareTo(GrouperDeprovisioningRealm o) {
    if (o == null) {
      return 1;
    }
    if (this == o ) {
      return 0;
    }
    return new CompareToBuilder().append(this.label, o.label).toComparison();
  }
  
  /**
   * managersWhoCanDeprovision_<realmName>
   * @return managers group name
   */
  public String getManagersGroupName() {
    return GrouperDeprovisioningJob.deprovisioningStemName() + ":managersWhoCanDeprovision_" + this.label;
  }

  /**
   * usersWhoHaveBeenDeprovisioned_<realmName>
   * @return users who have been deprovisioned
   */
  public String getUsersWhoHaveBeenDeprovisionedGroupName() {
    return GrouperDeprovisioningJob.deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + this.label;
  }

  /**
   * get users members who have been deprovisioned
   * @return users
   */
  public Set<Member> getUsersWhoHaveBeenDeprovisioned() {
    //these need to be looked up as root
    return (Set<Member>)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        Group usersWhoHaveBeenDeprovisioned = GroupFinder.findByName(rootSession, GrouperDeprovisioningRealm.this.getUsersWhoHaveBeenDeprovisionedGroupName(), false);
        if (usersWhoHaveBeenDeprovisioned == null) {
          throw new RuntimeException("users group deprovisioned is not found '" + GrouperDeprovisioningRealm.this.getUsersWhoHaveBeenDeprovisionedGroupName() + "', for realm: '" + GrouperDeprovisioningRealm.this.getLabel() + "'");
        }
        return usersWhoHaveBeenDeprovisioned.getMembers();
      }
    });

  }

  /**
   * get managers group oro null if not found
   * @return managers group
   */
  public Group getUsersWhoHaveBeenDeprovisionedGroup() {
    //these need to be looked up as root
    return (Group)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        Group usersWhoHaveBeenDeprovisioned = GroupFinder.findByName(rootSession, GrouperDeprovisioningRealm.this.getUsersWhoHaveBeenDeprovisionedGroupName(), false);
        if (usersWhoHaveBeenDeprovisioned == null) {
          LOG.info("users group deprovisioned is not found '" + GrouperDeprovisioningRealm.this.getUsersWhoHaveBeenDeprovisionedGroupName() + "', for realm: '" + GrouperDeprovisioningRealm.this.getLabel() + "'");
        }
        return usersWhoHaveBeenDeprovisioned;
      }
    });

  }

  /**
   * get managers group oro null if not found
   * @return managers group
   */
  public Group getManagersGroup() {
    //these need to be looked up as root
    return (Group)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        Group managersGroup = GroupFinder.findByName(rootSession, GrouperDeprovisioningRealm.this.getManagersGroupName(), false);
        if (managersGroup == null) {
          LOG.info("managers group is not found '" + GrouperDeprovisioningRealm.this.getManagersGroupName() + "', for realm: '" + GrouperDeprovisioningRealm.this.getLabel() + "'");
        }
        return managersGroup;
      }
    });

  }

  /**
   * 
   * @param subject
   * @return true if manager
   */
  public boolean subjectIsManager(final Subject subject) {
    //these need to be looked up as root
    return (Boolean)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        if (PrivilegeHelper.isWheelOrRoot(subject)) {
          return true;
        }
        Group managersGroup = GrouperDeprovisioningRealm.this.getManagersGroup();
        if (managersGroup == null) {
          return false;
        }
        return managersGroup.hasMember(subject);
      }
    });

  }
  
}
