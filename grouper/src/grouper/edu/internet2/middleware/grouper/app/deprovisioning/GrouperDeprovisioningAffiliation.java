package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class GrouperDeprovisioningAffiliation implements Comparable<GrouperDeprovisioningAffiliation> {

  /**
   * label of affiliation from grouper.properties, e.g. "student" or "employee"
   */
  private String label;
  
  /**
   * if you are in this group then it means you are a member of that cohort
   */
  private String groupNameMeansInAffiliation;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningAffiliation.class);

  /**
   * label of affiliation from grouper.properties, e.g. "student" or "employee"
   * @return label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * label of affiliation from grouper.properties, e.g. "student" or "employee"
   * @param label1
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * if you are in this group then it means you are a member of that cohort
   * @return group name
   */
  public String getGroupNameMeansInAffiliation() {
    return this.groupNameMeansInAffiliation;
  }

  /**
   * if you are in this group then it means you are a member of that cohort
   * @param groupNameMeansInAffiliation1
   */
  public void setGroupNameMeansInAffiliation(String groupNameMeansInAffiliation1) {
    this.groupNameMeansInAffiliation = groupNameMeansInAffiliation1;
  }

  /**
   * 
   */
  @Override
  public int compareTo(GrouperDeprovisioningAffiliation o) {
    if (o == null) {
      return 1;
    }
    if (this == o ) {
      return 0;
    }
    return new CompareToBuilder().append(this.label, o.label).toComparison();
  }
  
  /**
   * managersWhoCanDeprovision_<affiliationName>
   * @return managers group name
   */
  public String getManagersGroupName() {
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_" + this.label;
  }

  /**
   * usersWhoHaveBeenDeprovisioned_<affiliationName>
   * @return users who have been deprovisioned
   */
  public String getUsersWhoHaveBeenDeprovisionedGroupName() {
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + this.label;
  }
  
  /**
   * @param membership
   * @return true when subject is deprovisioned successfully, false otherwise.
   */
  public boolean deprovisionSubject(final Membership membership) {
    
    return (Boolean) GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Boolean callback(GrouperSession rootSession) throws GrouperSessionException {
        
        Subject subject =  membership.getMember().getSubject();

        Group ownerGroup = membership.getOwnerGroupId() != null ? membership.getOwnerGroup(): null;
        if (ownerGroup != null) {
          ownerGroup.deleteMember(membership.getMember(), false);
          
          for (Privilege priv: AccessPrivilege.ALL_PRIVILEGES) {
            ownerGroup.revokePriv(subject, priv, false);
          }
          
        }
        
        AttributeDef ownerAttributeDef = membership.getOwnerAttrDefId() != null ? membership.getOwnerAttributeDef(): null;
            
        if (ownerAttributeDef != null) {
          for (Privilege priv: AttributeDefPrivilege.ALL_PRIVILEGES) {
            ownerAttributeDef.getPrivilegeDelegate().revokePriv(subject, priv, false);
          }
        }
        
        Stem ownerStem = membership.getOwnerStemId() != null ? membership.getOwnerStem(): null;
        if (ownerStem != null) {
          
          for (Privilege priv: NamingPrivilege.ALL_PRIVILEGES) {
            ownerStem.revokePriv(subject, priv, false); 
          }
        }
        
        Group deprovisionGroup = getUsersWhoHaveBeenDeprovisionedGroup();
        return deprovisionGroup.addMember(subject, false);
      }
    });
    
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
        
        Group usersWhoHaveBeenDeprovisioned = GroupFinder.findByName(rootSession, GrouperDeprovisioningAffiliation.this.getUsersWhoHaveBeenDeprovisionedGroupName(), false);
        if (usersWhoHaveBeenDeprovisioned == null) {
          throw new RuntimeException("users group deprovisioned is not found '" + GrouperDeprovisioningAffiliation.this.getUsersWhoHaveBeenDeprovisionedGroupName() + "', for affiliation: '" + GrouperDeprovisioningAffiliation.this.getLabel() + "'");
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
        
        Group usersWhoHaveBeenDeprovisioned = GroupFinder.findByName(rootSession, GrouperDeprovisioningAffiliation.this.getUsersWhoHaveBeenDeprovisionedGroupName(), false);
        if (usersWhoHaveBeenDeprovisioned == null) {
          LOG.info("users group deprovisioned is not found '" + GrouperDeprovisioningAffiliation.this.getUsersWhoHaveBeenDeprovisionedGroupName() + "', for affiliation: '" + GrouperDeprovisioningAffiliation.this.getLabel() + "'");
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
        
        Group managersGroup = GroupFinder.findByName(rootSession, GrouperDeprovisioningAffiliation.this.getManagersGroupName(), false);
        if (managersGroup == null) {
          LOG.info("managers group is not found '" + GrouperDeprovisioningAffiliation.this.getManagersGroupName() + "', for affiliation: '" + GrouperDeprovisioningAffiliation.this.getLabel() + "'");
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
        Group managersGroup = GrouperDeprovisioningAffiliation.this.getManagersGroup();
        if (managersGroup == null) {
          return false;
        }
        return managersGroup.hasMember(subject);
      }
    });

  }

  /**
   * retrieve all affiliations configured in the grouper.properties, will not return null
   * @return the affiliations, alphabetical
   */
  public static Map<String, GrouperDeprovisioningAffiliation> retrieveAllAffiliations() {
    
    Map<String, GrouperDeprovisioningAffiliation> allAffiliations = new TreeMap<String, GrouperDeprovisioningAffiliation>();
    
    //  GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    //  GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "faculty, student");
    if (GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      String allAffiliationsString = GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.affiliations");
      if (!StringUtils.isBlank(allAffiliationsString)) {
        Set<String> affiliationLabels = GrouperUtil.splitTrimToSet(allAffiliationsString, ",");
        for (String affiliationLabel : affiliationLabels) {
          GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation = new GrouperDeprovisioningAffiliation();
          grouperDeprovisioningAffiliation.setLabel(affiliationLabel);
          
          // # deprovisioning.affiliation_<affiliationName>.groupNameMeansInAffiliation = a:b:c
          grouperDeprovisioningAffiliation.setGroupNameMeansInAffiliation(GrouperConfig.retrieveConfig().propertyValueString(
              "deprovisioning.affiliation_" + affiliationLabel + ".groupNameMeansInAffiliation"));
          allAffiliations.put(affiliationLabel, grouperDeprovisioningAffiliation);
        }
      }
    }
    return allAffiliations;
  }

  /**
   * get the configured deprovisioning affiliations
   * @return the affiliations
   */
  public static Set<String> retrieveDeprovisioningAffiliations() {
    // dont call the method since could be a circular problem
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.enable", true)) {
      return new HashSet<String>();
    }
    return GrouperConfig.retrieveConfig().deprovisioningAffiliations();
  }

  /**
   * get affiliations a subject manages
   * @param subject who is the manager
   * @return the affiliations
   */
  public static Map<String, GrouperDeprovisioningAffiliation> retrieveAffiliationsForUserManager(final Subject subject) {
    //these need to be looked up as root
    return (Map<String, GrouperDeprovisioningAffiliation>)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        Map<String, GrouperDeprovisioningAffiliation> allAffiliations = retrieveAllAffiliations();
        
        if (PrivilegeHelper.isWheelOrRoot(subject)) {
          return allAffiliations;
        }
        
        Map<String, GrouperDeprovisioningAffiliation> someAffiliations = new TreeMap<String, GrouperDeprovisioningAffiliation>();
        
        Iterator<String> iterator = allAffiliations.keySet().iterator();
        
        while (iterator.hasNext()) {
          String affiliationName = iterator.next();
          GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation = allAffiliations.get(affiliationName);
          if (grouperDeprovisioningAffiliation.getManagersGroup().hasMember(subject)) {
            someAffiliations.put(affiliationName, grouperDeprovisioningAffiliation);
          }
        }
        
        return someAffiliations;
      }
    });
  
  }
  
}
