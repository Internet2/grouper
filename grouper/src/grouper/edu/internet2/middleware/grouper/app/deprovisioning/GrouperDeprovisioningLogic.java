/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Date;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperObject;
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
public class GrouperDeprovisioningLogic {

  /**
   * 
   */
  public GrouperDeprovisioningLogic() {
  }

  /**
   * update last certified date to now
   * @param grouperObject
   */
  public static void updateLastCertifiedDate(GrouperObject grouperObject) {
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);

    for (String affiliation : grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().keySet()) {

      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);
      
      if (grouperDeprovisioningConfiguration.isHasDatabaseConfiguration() && grouperDeprovisioningConfiguration.getOriginalConfig().isDeprovision()) {
        
        grouperDeprovisioningConfiguration.getNewConfig().setCertifiedDate(new Date());
        
      }
      
    }
    
  }
  
  /**
   * @param membership
   */
  public static void removeAccess(final Membership membership) {
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        Subject subject =  membership.getMember().getSubject();

        Group ownerGroup = membership.getOwnerGroupId() != null ? membership.getOwnerGroup(): null;
        if (ownerGroup != null) {
          removeAccess(ownerGroup, subject);
        }

        AttributeDef ownerAttributeDef = membership.getOwnerAttrDefId() != null ? membership.getOwnerAttributeDef(): null;

        if (ownerAttributeDef != null) {
          removeAccess(ownerAttributeDef, subject);
        }

        Stem ownerStem = membership.getOwnerStemId() != null ? membership.getOwnerStem(): null;
        if (ownerStem != null) {
          removeAccess(ownerStem, subject);
        }
        return null;
      }
    });

  }

  /**
   * @param membership
   */
  public static void removeAccess(final Group group, final Subject subject) {
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        group.deleteMember(subject, false);
        
        for (Privilege priv: AccessPrivilege.ALL_PRIVILEGES) {
          group.revokePriv(subject, priv, false);
        }
          
        return null;
      }
    });
    
  }
  /**
   * @param attributeDef
   * @param subject
   */
  public static void removeAccess(final AttributeDef attributeDef, final Subject subject) {
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        for (Privilege priv: AttributeDefPrivilege.ALL_PRIVILEGES) {
          attributeDef.getPrivilegeDelegate().revokePriv(subject, priv, false);
        }

        return null;
      }
    });
    
  }


  /**
   * @param membership
   */
  public static void removeAccess(final Stem stem, final Subject subject) {
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        for (Privilege priv: NamingPrivilege.ALL_PRIVILEGES) {
          stem.revokePriv(subject, priv, false); 
        }
        return null;
      }
    });
    
  }



  /**
   * if user is allowed to deprovision
   * @param subject
   * @return true if allowed
   */
  public static boolean allowedToDeprovision(Subject subject) {
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }

    Map<String, GrouperDeprovisioningAffiliation> map =  GrouperDeprovisioningAffiliation.retrieveAffiliationsForUserManager(subject);

    return GrouperUtil.length(map) > 0;
  }
  
  /**
   * @param grouperObject 
   */
  public static void updateDeprovisioningMetadataForSingleObject(GrouperObject grouperObject) {
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
    updateDeprovisioningMetadataForSingleObject(grouperObject, grouperDeprovisioningOverallConfiguration);
  }

  /**
   * @param grouperObject 
   * @param grouperDeprovisioningOverallConfiguration 
   */
  public static void updateDeprovisioningMetadataForSingleObject(GrouperObject grouperObject, GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration) {

    for (String affiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
      
      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);

      // we good
      GrouperDeprovisioningAttributeValue originalConfig = grouperDeprovisioningConfiguration.getOriginalConfig();
      if (originalConfig != null && originalConfig.isDirectAssignment()) {
        continue;
      }
      
      GrouperDeprovisioningConfiguration inheritedConfiguration = grouperDeprovisioningConfiguration.getInheritedConfig();

      if (inheritedConfiguration != null) {

        GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
        GrouperDeprovisioningAttributeValue inheritedAttributeValue = inheritedConfiguration.getOriginalConfig();

        grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(inheritedAttributeValue.getAllowAddsWhileDeprovisionedString());
        grouperDeprovisioningAttributeValue.setAutoChangeLoaderString(inheritedAttributeValue.getAutoChangeLoaderString());
        grouperDeprovisioningAttributeValue.setAutoselectForRemovalString(inheritedAttributeValue.getAutoselectForRemovalString());
        // dont set certified date
        grouperDeprovisioningAttributeValue.setDeprovisionString(inheritedAttributeValue.getDeprovisionString());
        grouperDeprovisioningAttributeValue.setDirectAssignment(false);
        grouperDeprovisioningAttributeValue.setEmailAddressesString(inheritedAttributeValue.getEmailAddressesString());
        grouperDeprovisioningAttributeValue.setEmailBodyString(inheritedAttributeValue.getEmailBodyString());
        grouperDeprovisioningAttributeValue.setInheritedFromFolderIdString(inheritedAttributeValue.getGrouperDeprovisioningConfiguration().getAttributeAssignBase().getOwnerStemId());
        // dont set last emailed
        grouperDeprovisioningAttributeValue.setMailToGroupString(inheritedAttributeValue.getMailToGroupString());
        grouperDeprovisioningAttributeValue.setAffiliationString(inheritedAttributeValue.getAffiliationString());
        grouperDeprovisioningAttributeValue.setSendEmailString(inheritedAttributeValue.getSendEmailString());
        grouperDeprovisioningAttributeValue.setShowForRemovalString(inheritedAttributeValue.getShowForRemovalString());
        grouperDeprovisioningAttributeValue.setStemScopeString(inheritedAttributeValue.getStemScopeString());
        
      } else {

        // there is no local config or inherited config, delete it all (well most of it)
        grouperDeprovisioningConfiguration.clearOutConfigurationButLeaveMetadata();

      }
      grouperDeprovisioningConfiguration.storeConfiguration();
      
    }

  }
  
  /**
   * go through groups and folders marked with deprovisioning metadata and make sure its up to date with inheritance
   * @param stem 
   */
  public static void updateDeprovisioningMetadata(Stem stem) {

    Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> grouperDeprovisioningOverallConfigurationMap 
      = GrouperDeprovisioningOverallConfiguration.retrieveConfigurationForStem(stem, true);

    for (GrouperObject grouperObject: grouperDeprovisioningOverallConfigurationMap.keySet()) {
      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = grouperDeprovisioningOverallConfigurationMap.get(grouperObject);
      updateDeprovisioningMetadataForSingleObject(grouperObject, grouperDeprovisioningOverallConfiguration);
    }
    
  }
  

}
