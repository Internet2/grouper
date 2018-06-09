package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 *
 */
public class GrouperDeprovisioningAttributeNames {

  /**
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   */
  public static final String DEPROVISIONING_ALLOW_ADDS_WHILE_DEPROVISIONED = "deprovisioningAllowAddsWhileDeprovisioned";
  /**
   * If this is a loader job, if being in a deprovisioned job means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   */
  public static final String DEPROVISIONING_AUTO_CHANGE_LOADER = "deprovisioningAutoChangeLoader";
  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   */
  public static final String DEPROVISIONING_AUTOSELECT_FOR_REMOVAL = "deprovisioningAutoselectForRemoval";

  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String DEPROVISIONING_DEF = "deprovisioningDef";
  
  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String DEPROVISIONING_BASE = "deprovisioning";
  
  
  /**
   * if this object should be in consideration for the deprovisioning system.
   * can be: blank, true, or false.  Defaults to true
   */
  public static final String DEPROVISIONING_DEPROVISION = "deprovisioningDeprovision";
  /**
   * if deprovisioning configuration is directly assigned to the group or folder or inherited from parent.
   * true for direct, false for inherited, blank for not assigned
   */
  public static final String DEPROVISIONING_DIRECT_ASSIGNMENT = "deprovisioningDirectAssignment";
  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   */
  public static final String DEPROVISIONING_EMAIL_ADDRESSES = "deprovisioningEmailAddresses";
  /**
   * custom email body for emails, if blank use the default configured body. 
   * Note there are template variables $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  public static final String DEPROVISIONING_EMAIL_BODY = "deprovisioningEmailBody";
  
  /**
   * yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day
   */
  public static final String DEPROVISIONING_LAST_EMAILED_DATE = "deprovisioningLastEmailedDate";
  
  /**
   * (String) number of millis since 1970 that this group was certified for deprovisioning. i.e. the group managers 
   * indicate that the deprovisioned users are ok being in the group and do not send email reminders about it 
   * anymore until there are newly deprovisioned entities
   */
  public static final String DEPROVISIONING_CERTIFIED_MILLIS = "deprovisioningCertifiedMillis";
  
  /**
   * custom subject for emails, if blank use the default configured subject. 
   * Note there are template variables $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  public static final String DEPROVISIONING_EMAIL_SUBJECT = "deprovisioningEmailSubject";
  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   */
  public static final String DEPROVISIONING_INHERITED_FROM_FOLDER_ID = "deprovisioningInheritedFromFolderId";
  /**
   * Group ID which holds people to email members of that group to send deprovisioning messages (mutually exclusive with deprovisioningEmailAddresses)
   */
  public static final String DEPROVISIONING_MAIL_TO_GROUP = "deprovisioningMailToGroup";
  /**
   * required, is the affiliation for this metadata
   */
  public static final String DEPROVISIONING_AFFILIATION = "deprovisioningAffiliation";
  /**
   * If this is true, then send an email about the deprovisioning event.  If the assignments were removed, then give a description of the action.  
   * If assignments were not removed, then remind the managers to unassign.  Can be <blank>, true, or false.  Defaults to false unless the assignments 
   * were not removed.
   */
  public static final String DEPROVISIONING_SEND_EMAIL = "deprovisioningSendEmail";
  /**
   * If the deprovisioning screen should show this object if the user as an assignment.
   * can be: blank, true, or false.  If blank, will default to true unless auto change loader is false.
   */
  public static final String DEPROVISIONING_SHOW_FOR_REMOVAL = "deprovisioningShowForRemoval";
  /**
   * If configuration is assigned to a folder, then this is "one" or "sub".  "one" means only applicable to objects
   * directly in this folder.  "sub" (default) means applicable to all objects in this folder and
   * subfolders.  Note, the inheritance stops when a sub folder or object has configuration assigned.
   */
  public static final String DEPROVISIONING_STEM_SCOPE = "deprovisioningStemScope";

  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String DEPROVISIONING_VALUE_DEF = "deprovisioningValueDef";
  /**
   * deprovisioning allow adds while deprovisioned attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAllowAddsWhileDeprovisioned() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_ALLOW_ADDS_WHILE_DEPROVISIONED);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning allow adds while deprovisioned attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning auto change loader attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAutoChangeLoader() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_AUTO_CHANGE_LOADER);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning auto change loader attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning auto-select for removal attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAutoSelectForRemoval() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_AUTOSELECT_FOR_REMOVAL);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning auto select for removal attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_BASE);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning def attribute def name be found?");
    }
    return attributeDefName;
  }
  /**
   * deprovisioning deprovision attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDeprovision() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_DEPROVISION);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning deprovision attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning direct assignment attribute def name.
   * true for direct, false for inherited, blank for not assigned
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDirectAssignment() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_DIRECT_ASSIGNMENT);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning direct assignment attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning email addresses attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailAddresses() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_EMAIL_ADDRESSES);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning email addresses attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * email body attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailBody() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_EMAIL_BODY);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning email body attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * email subject attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailSubject() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_EMAIL_SUBJECT);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning email subject attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDefName retrieveAttributeDefNameFromDbOrCache(final String name) {
    
    AttributeDefName attributeDefName = attributeDefNameCache.get(name);
  
    if (attributeDefName == null) {
      
      attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefNameFinder.findByName(name, false);
          
        }
        
      });
      if (attributeDefName == null) {
        return null;
      }
      attributeDefNameCache.put(name, attributeDefName);
    }
    
    return attributeDefName;
  }
  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameInheritedFromFolderId() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_INHERITED_FROM_FOLDER_ID);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning inherited from folder id attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning mail to group attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameMailToGroup() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_MAIL_TO_GROUP);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning mail to group attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning send email attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAffiliation() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_AFFILIATION);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning affiliation attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning send email attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameSendEmail() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_SEND_EMAIL);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning send email attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning show for removal attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameShowForRemoval() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_SHOW_FOR_REMOVAL);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning show for removal attribute def name be found?");
    }
    return attributeDefName;
  
  }
  
  /**
   * deprovisioning last emailed date attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameLastEmailedDate() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_LAST_EMAILED_DATE);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning last emailed date attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * deprovisioning certified millis attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameCertifiedMillis() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_CERTIFIED_MILLIS);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning certified millis attribute def name be found?");
    }
    return attributeDefName;
  
  }
  
  /**
   * deprovisioning stem scope attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameStemScope() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_STEM_SCOPE);
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning stem scope attribute def name be found?");
    }
    return attributeDefName;
  
  }
  /**
   * attribute value def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefNameValueDef() {
    
    AttributeDef attributeDef = retrieveAttributeDefFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_VALUE_DEF);
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant deprovisioning def attribute value def be found?");
    }
    return attributeDef;
  }
  /** attribute def name cache */
  private static ExpirableCache<String, AttributeDefName> attributeDefNameCache = new ExpirableCache<String, AttributeDefName>(5);
  /** attribute def cache */
  private static ExpirableCache<String, AttributeDef> attributeDefCache = new ExpirableCache<String, AttributeDef>(5);

  /**
   * attribute value def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    AttributeDef attributeDef = retrieveAttributeDefFromDbOrCache(
        GrouperDeprovisioningSettings.deprovisioningStemName() + ":" + DEPROVISIONING_DEF);
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant deprovisioning def base def be found?");
    }
    return attributeDef;
  }

  /**
   * cache this.  note, not sure if its necessary
   * @param name 
   * @return attribute def
   */
  private static AttributeDef retrieveAttributeDefFromDbOrCache(final String name) {
    
    AttributeDef attributeDef = attributeDefCache.get(name);
  
    if (attributeDef == null) {
      
      attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
  
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(name, false);
          
        }
        
      });
      if (attributeDef == null) {
        return null;
      }
      attributeDefCache.put(name, attributeDef);
    }
    
    return attributeDef;
  }

}
