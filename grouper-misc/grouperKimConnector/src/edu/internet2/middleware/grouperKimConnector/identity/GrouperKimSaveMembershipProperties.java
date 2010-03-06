/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * <pre>
 * properties about a post processor which adds a member to a group
 * 
 * kuali.edoclite.saveMembership.docTypeName.0 = sampleProvisionGroup.doctype
 * kuali.edoclite.saveMembership.groupRegex.0 = ^temp:[^:]+rovisionGroup$
 * kuali.edoclite.saveMembership.addMembershipToGroups.0 = temp:provisionGroup
 * kuali.edoclite.saveMembership.removeMembershipFromGroups.0 = temp:anotherProvisionGroup
 * kuali.edoclite.saveMembership.emailAdmins.0 = mchyzer@isc.upenn.edu
 * </pre>
 */
public class GrouperKimSaveMembershipProperties {

  /** doctype name that this applies to */
  private String docTypeName;
  
  /** regex of group allowed to assign to, extra layer of security, optional */
  private String groupRegex;
  
  /** groups (comma separated) id or name which the initiator will be assigned to when the document is final */
  private String addMembershipToGroups;

  /** groups (comma separated) id or name which the initiator will be unassigned from when the document is final */
  private String removeMembershipFromGroups;
  
  /** email addresses for sending a message to admins */
  private String emailAdmins;
  
  
  /**
   * email addresses for sending a message to admins
   * @return the emailAdmins
   */
  public String getEmailAdmins() {
    return this.emailAdmins;
  }


  
  /**
   * email addresses for sending a message to admins
   * @param emailAdmins1 the emailAdmins to set
   */
  public void setEmailAdmins(String emailAdmins1) {
    this.emailAdmins = emailAdmins1;
  }


  /**
   * doctype name that this applies to
   * @return the docTypeName
   */
  public String getDocTypeName() {
    return this.docTypeName;
  }

  
  /**
   * doctype name that this applies to
   * @param docTypeName1 the docTypeName to set
   */
  public void setDocTypeName(String docTypeName1) {
    this.docTypeName = docTypeName1;
  }

  
  /**
   * regex of group allowed to assign to, extra layer of security, optional
   * @return the groupRegex
   */
  public String getGroupRegex() {
    return this.groupRegex;
  }

  
  /**
   * regex of group allowed to assign to, extra layer of security, optional
   * @param groupRegex1 the groupRegex to set
   */
  public void setGroupRegex(String groupRegex1) {
    this.groupRegex = groupRegex1;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be assigned to when the document is final
   * @return the addMembershipToGroups
   */
  public String getAddMembershipToGroups() {
    return this.addMembershipToGroups;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be assigned to when the document is final
   * @param addMembershipToGroups1 the addMembershipToGroups to set
   */
  public void setAddMembershipToGroups(String addMembershipToGroups1) {
    this.addMembershipToGroups = addMembershipToGroups1;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be unassigned from when the document is final
   * @return the removeMembershipFromGroups
   */
  public String getRemoveMembershipFromGroups() {
    return this.removeMembershipFromGroups;
  }

  
  /**
   * groups (comma separated) id or name which the initiator will be unassigned from when the document is final
   * @param removeMembershipFromGroups1 the removeMembershipFromGroups to set
   */
  public void setRemoveMembershipFromGroups(String removeMembershipFromGroups1) {
    this.removeMembershipFromGroups = removeMembershipFromGroups1;
  }

  

  /**
   * cache of grouper source configs
   */
  private static ExpirableCache<String, GrouperKimSaveMembershipProperties> grouperKimSaveMembershipPropertiesCache 
    = new ExpirableCache<String, GrouperKimSaveMembershipProperties>(5);

  /**
   * get the source properties for source name (current source name)
   * @param docTypeName
   * @return properties for source and app name
   */
  public static GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties(String docTypeName) {
    
    //kuali.edoclite.saveMembership.docTypeName.0 = sampleProvisionGroup.doctype
    //kuali.edoclite.saveMembership.groupRegex.0 = ^temp:[^:]+rovisionGroup$
    //kuali.edoclite.saveMembership.addMembershipToGroups.0 = temp:provisionGroup
    //kuali.edoclite.saveMembership.removeMembershipFromGroups.0 = temp:anotherProvisionGroup
    
    GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties = 
      grouperKimSaveMembershipPropertiesCache.get(docTypeName);
    if (grouperKimSaveMembershipProperties == null) {
      grouperKimSaveMembershipProperties = new GrouperKimSaveMembershipProperties();
      grouperKimSaveMembershipProperties.setDocTypeName(docTypeName);
      
      //loop through and find this config
      
      for (int i=0;i<100;i++) {
        
        String currentDocTypeName = GrouperClientUtils.propertiesValue("kuali.edoclite.saveMembership.docTypeName." + i, false);
        if (GrouperClientUtils.isBlank(currentDocTypeName)) {
          break;
        }
        if (GrouperClientUtils.equals(docTypeName, currentDocTypeName)) {
          
          //we found it
          {
            String groupRegex = GrouperClientUtils.propertiesValue("kuali.edoclite.saveMembership.groupRegex." + i, false);
            grouperKimSaveMembershipProperties.setGroupRegex(groupRegex);
          }
          
          {
            String addMembershipToGroups = GrouperClientUtils.propertiesValue("kuali.edoclite.saveMembership.addMembershipToGroups." + i, false);
            grouperKimSaveMembershipProperties.setAddMembershipToGroups(addMembershipToGroups);
          }
          
          {
            String removeMembershipFromGroups = GrouperClientUtils.propertiesValue("kuali.edoclite.saveMembership.removeMembershipFromGroups." + i, false);
            grouperKimSaveMembershipProperties.setRemoveMembershipFromGroups(removeMembershipFromGroups);
          }
          
          {
            String emailAdmins = GrouperClientUtils.propertiesValue("kuali.edoclite.saveMembership.emailAdmins." + i, false);
            grouperKimSaveMembershipProperties.setEmailAdmins(emailAdmins);
          }
          
          break;
        }
        
      }
      grouperKimSaveMembershipPropertiesCache.put(docTypeName, grouperKimSaveMembershipProperties);
    }
    return grouperKimSaveMembershipProperties;
  }
  
}
