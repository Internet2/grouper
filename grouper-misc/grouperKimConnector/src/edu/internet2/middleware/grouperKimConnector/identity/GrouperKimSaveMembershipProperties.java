/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /** this will be prefixed to the entered group name so the whole stem doesnt have to be put on screen (also helps sandbox out the security) */
  private String enteredGroupNamePrefix;
  
  
  /**
   * this will be prefixed to the entered group name so the whole stem doesnt have to be put on screen (also helps sandbox out the security)
   * @return the enteredGroupNamePrefix
   */
  public String getEnteredGroupNamePrefix() {
    return this.enteredGroupNamePrefix;
  }

  /**
   * this will be prefixed to the entered group name so the whole 
   * stem doesnt have to be put on screen (also helps sandbox out the security)
   * @param enteredGroupNamePrefix1 the enteredGroupNamePrefix to set
   */
  public void setEnteredGroupNamePrefix(String enteredGroupNamePrefix1) {
    this.enteredGroupNamePrefix = enteredGroupNamePrefix1;
  }




  /** 
   * if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
   * so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
   * the value of the field is the group to add to
   */
  private String edocliteFieldPrefix;
  
  
  /**
   * if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
   * so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
   * the value of the field is the group to add to
   * @return the edocliteFieldPrefix
   */
  public String getEdocliteFieldPrefix() {
    return this.edocliteFieldPrefix;
  }



  
  /**
   * if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
   * so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
   * the value of the field is the group to add to
   * @param edocliteFieldPrefix1 the edocliteFieldPrefix to set
   */
  public void setEdocliteFieldPrefix(String edocliteFieldPrefix1) {
    this.edocliteFieldPrefix = edocliteFieldPrefix1;
  }



  /** set of strings of groups allowed to be used (if empty, then allow all) */
  private Set<String> allowedGroups = new HashSet<String>();
  
  /**
   * set of strings of groups allowed to be used (if empty, then allow all)
   * @return the groups
   */
  public Set<String> getAllowedGroups() {
    return this.allowedGroups;
  }



  /**
   * email addresses for sending a message to admins
   * @return the emailAdmins
   */
  public String getEmailAdmins() {
    return this.emailAdmins;
  }

  /**
   * if allowed to access this group by name
   * @param groupName
   * @return true if allowed to access group by name
   */
  public boolean allowedToAccessGroup(String groupName) {
    
    //see if fails the regex (if there is a regex)
    if (!GrouperClientUtils.isBlank(this.groupRegex)) {
      if (!groupName.matches(this.groupRegex)) {
        return false;
      }
    }
    
    //see if not in the list, if there is a list
    if (this.getAllowedGroups().size() > 0) {
      if (!this.getAllowedGroups().contains(groupName)) {
        return false;
      }
    }
    return true;
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
    
    //###############################
    //# configure postprocessor actions on document types.  The string "sampleProvisioning" ties the configs
    //# together, change that label for multiple
    //
    //# doctype name that this applies to
    //kuali.edoclite.saveMembership.sampleProvisioning.docTypeName = sampleProvisioning.doctype
    //
    //# regex of group allowed to assign to, extra layer of security, optional
    //kuali.edoclite.saveMembership.sampleProvisioning.groupRegex = ^temp:[^:]+rovisionGroup$
    //
    //# list of allowed to assign to (comma separate), extra layer of security, optional, 
    //#generally mutually exclusive with the groupRegex
    //kuali.edoclite.saveMembership.sampleProvisioning.allowedGroups = a:b:c, d:e:f:G
    //
    //# edocliteFieldPrefix if checkboxes or textfields or whatever, put the prefix of the edoclite field here.
    //#so if the field prefix is "groups", then it will look for groups0, groups1, etc to groups200...
    //#the value of the field is the group to add to
    //kuali.edoclite.saveMembership.sampleProvisioning.edocliteFieldPrefix = provisionGroup
    //
    //#this will be prefixed to the entered group name so the whole stem doesnt 
    //#have to be put on screen (also helps sandbox out the security)
    //kuali.edoclite.saveMembership.sampleProvisioning.enteredGroupNamePrefix = school:some:prefix:
    //
    //# groups (comma separated) id or name which the initiator will be assigned to when the document is final
    //kuali.edoclite.saveMembership.sampleProvisioning.addMembershipToGroups = temp:provisionGroup
    //
    //# groups (comma separated) id or name which the initiator will be unassigned from when the document is final
    //kuali.edoclite.saveMembership.sampleProvisioning.removeMembershipFromGroups = temp:anotherProvisionGroup
    //
    //# email addresses (comma separated) that should get an admin email that this was done (or errors)
    //kuali.edoclite.saveMembership.sampleProvisioning.emailAdmins = mchyzer@isc.upenn.edu
    
    
    GrouperKimSaveMembershipProperties grouperKimSaveMembershipProperties = 
      grouperKimSaveMembershipPropertiesCache.get(docTypeName);
    if (grouperKimSaveMembershipProperties == null) {
      grouperKimSaveMembershipProperties = new GrouperKimSaveMembershipProperties();
      grouperKimSaveMembershipProperties.setDocTypeName(docTypeName);
      
      //loop through and find this config
      
      Pattern pattern = Pattern.compile("^kuali\\.edoclite\\.saveMembership\\.(.+)\\.docTypeName$");
      
      Properties properties = GrouperClientUtils.grouperClientProperties();
      for (Object keyObject : properties.keySet()) {
        String key = (String)keyObject;
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
          String configName = matcher.group(1);
          
          String currentDocTypeName = GrouperClientUtils.propertiesValue(key, true);
  
          
          if (GrouperClientUtils.equals(docTypeName, currentDocTypeName)) {
            
            //we found it
            {
              String groupRegex = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".groupRegex", false);
              grouperKimSaveMembershipProperties.setGroupRegex(groupRegex);
            }
            
            {
              String groupsString = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".allowedGroups", false);
              if (!GrouperClientUtils.isBlank(groupsString)) {
                List<String> groupsList = GrouperClientUtils.splitTrimToList(groupsString, ",");
                grouperKimSaveMembershipProperties.getAllowedGroups().addAll(groupsList);
              }
            }
            
            {
              String edocliteFieldPrefix = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".edocliteFieldPrefix", false);
              grouperKimSaveMembershipProperties.setEdocliteFieldPrefix(edocliteFieldPrefix);
            }
            {
              String enteredGroupNamePrefix = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".enteredGroupNamePrefix", false);
              grouperKimSaveMembershipProperties.setEnteredGroupNamePrefix(enteredGroupNamePrefix);
            }
                        
            {
              String addMembershipToGroups = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".addMembershipToGroups", false);
              grouperKimSaveMembershipProperties.setAddMembershipToGroups(addMembershipToGroups);
            }
            
            {
              String removeMembershipFromGroups = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".removeMembershipFromGroups", false);
              grouperKimSaveMembershipProperties.setRemoveMembershipFromGroups(removeMembershipFromGroups);
            }
            
            {
              String emailAdmins = GrouperClientUtils.propertiesValue(
                  "kuali.edoclite.saveMembership." + configName + ".emailAdmins", false);
              grouperKimSaveMembershipProperties.setEmailAdmins(emailAdmins);
            }
            
            break;
          }
        }        
      }
      grouperKimSaveMembershipPropertiesCache.put(docTypeName, grouperKimSaveMembershipProperties);
    }
    return grouperKimSaveMembershipProperties;
  }
  
}
