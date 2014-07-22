/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAttributeConfigBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAutoaddBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectConfigBean;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtilsMapping;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.ExternalSubjectHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksExternalSubjectBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * database object for external subject
 * @author mchyzer
 *
 */
@SuppressWarnings({ "serial", "unchecked" })
public class ExternalSubject extends GrouperAPI implements GrouperHasContext, 
   Hib3GrouperVersioned {

  /**
   * 
   */
  public ExternalSubject() {
    super();
  }

  /** uuid for row */
  private String uuid;
  
  /** the thing that the subject uses to login */
  private String identifier;
  
  /** name of subject */
  private String name;

  /** description, which is generated from other attributes */
  private String description;

  /** email address */
  private String email;

  /** institution where the user is from */
  private String institution;

  /** search string to find a subject, in all lower case */
  private String searchStringLower;
  
  /** contextId links to audit tables */
  private String contextId;

  /** time created */
  private long createTime = System.currentTimeMillis();

  /** who created this */
  private String creatorMemberId;

  /** who last modified this */
  private String modifierMemberId;

  /** comma separated vetted email addresses */
  private String vettedEmailAddresses;
  
  /** when last modified */
  private long modifyTime = System.currentTimeMillis(); 

  /** when this was disabled, or when it will be disabled */
  private Long disabledTime = null; 

  /** is this is currently enabled */
  private boolean enabled = true;
  
  /**
   * comma separated vetted email addresses
   * @return comma separated vetted email addresses
   */
  public String getVettedEmailAddresses() {
    return this.vettedEmailAddresses;
  }

  /**
   * comma separated vetted email addresses
   * @param vettedEmailAddresses1
   */
  public void setVettedEmailAddresses(String vettedEmailAddresses1) {
    this.vettedEmailAddresses = vettedEmailAddresses1;
  }

  /**
   * notify watcher about a registration.  If there is a problem, just log the exception
   * @param identifier
   * @param emailAddressToNotify
   * @param emailAddressOfInvitee
   */
  public static void notifyWatcherAboutRegistration(final String identifier,
      String emailAddressToNotify, String emailAddressOfInvitee) {
    try {
      
      String theEmail = GrouperConfig.retrieveConfig().propertyValueString("externalSubjectsNotifyInviterEmail");
      if (StringUtils.isBlank(theEmail)) {
        theEmail = "Hello,$newline$$newline$This is a notification that user $inviteeIdentifier$ from email address " +
            "$inviteeEmailAddress$ has registered with the identity management service.  They can now use applications " +
            "at this institution.$newline$$newline$Regards.";               
      }
      String theSubject = GrouperConfig.retrieveConfig().propertyValueString("externalSubjectsNotifyInviterSubject");
      if (StringUtils.isBlank(theSubject)) {
        theSubject = "$inviteeIdentifier$ has registered";
      }
      //$newline$, $inviteeIdentifier$, $inviteeEmailAddress$
      theEmail = StringUtils.replace(theEmail, "$newline$", "\n");
      theEmail = StringUtils.replace(theEmail, "$inviteeIdentifier$", identifier);
      theEmail = StringUtils.replace(theEmail, "$inviteeEmailAddress$", emailAddressOfInvitee);
      theSubject = StringUtils.replace(theSubject, "$inviteeIdentifier$", identifier);
      theSubject = StringUtils.replace(theSubject, "$inviteeEmailAddress$", emailAddressOfInvitee);

      new GrouperEmail().setTo(emailAddressToNotify).setSubject(theSubject).setBody(theEmail).send();
      
    } catch (Exception e) {
      //maybe they typed in a bad email address or something...
      LOG.error("Problem sending notification of registration to: '" + emailAddressToNotify + "' for external subject invite for: " + identifier, e);
    }
  }
  

  /**
   * add a vetted email address and store this object if necessary
   * @param vettedEmailAddress
   */
  public void addVettedEmailAddress(String vettedEmailAddress) {
    if (!StringUtils.isBlank(this.vettedEmailAddresses)) {
      Set<String> vettedEmailAddresses = GrouperUtil.splitTrimToSet(this.vettedEmailAddresses, ",");
      //see if it is already there, then dont worry about it
      if (vettedEmailAddresses.contains(vettedEmailAddress)) {
        return;
      }
      //there are some there
      this.vettedEmailAddresses = this.vettedEmailAddresses + ", " + vettedEmailAddress;
    } else {
      this.vettedEmailAddresses = vettedEmailAddress;
    }
    this.store();
  }
  
  /**
   * when this was disabled, or when it will be disabled
   * @return the millis from 1970
   */
  public Long getDisabledTimeDb() {
    return this.disabledTime;
  }

  /**
   * when this was disabled, or when it will be disabled
   * @param disabledTime1
   */
  public void setDisabledTimeDb(Long disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /**
   * when this was disabled, or when it will be disabled, millis from 1970
   * @return disabled time
   */
  public Date getDisabledTime() {
    return this.disabledTime == null ? null : new Date(this.disabledTime);
  }

  /**
   * when this was disabled, or when it will be disabled, millis from 1970
   * @param theDisabledTime1
   */
  public void setDisabledTime(Date theDisabledTime1) {
    this.disabledTime = theDisabledTime1 == null ? null : theDisabledTime1.getTime();
  }
  
  /**
   * if this is enabled
   * @return true if enabled
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * if this is enabled
   * @return T or F
   */
  public String getEnabledDb() {
    return this.enabled ? "T" : "F";
  }

  /**
   * if this is enabled
   * @param enabled1
   */
  public void setEnabled(boolean enabled1) {
    this.enabled = enabled1;
  }

  /**
   * if this is enabled, T or F
   * @param enabled1
   */
  public void setEnabledDb(String enabled1) {
    this.enabled = GrouperUtil.booleanValue(enabled1);
  }

  /** table name for external subjects */
  public static final String TABLE_GROUPER_EXT_SUBJ = "grouper_ext_subj";
  
  /** column name for context id */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** column name for description */
  public static final String COLUMN_DESCRIPTION = "description";
  
  /** column name for disabled time */
  public static final String COLUMN_DISABLED_TIME = "disabled_time";
  
  /** column name for email */
  public static final String COLUMN_EMAIL = "email";
  
  /** column name for enabled */
  public static final String COLUMN_ENABLED = "enabled";
  
  /** column name for identifier */
  public static final String COLUMN_IDENTIFIER = "identifier";
  
  /** column name for institution */
  public static final String COLUMN_INSTITUTION = "institution";
  
  /** column name for name */
  public static final String COLUMN_NAME = "name";
  
  /** column name for searchStringLower */
  public static final String COLUMN_SEARCH_STRING_LOWER = "search_string_lower";
  
  /** column name for vettedEmailAddresses */
  public static final String COLUMN_VETTED_EMAIL_ADDRESSES = "vetted_email_addresses";
  
  /** column name for uuid */
  public static final String COLUMN_UUID = "uuid";
  
  /** column name for create time */
  public static final String COLUMN_CREATE_TIME = "create_time";
  
  /** column name for creator member id */
  public static final String COLUMN_CREATOR_MEMBER_ID = "creator_member_id";
  
  /** column name for modify time */
  public static final String COLUMN_MODIFY_TIME = "modify_time";
  
  /** column name for modifier member id */
  public static final String COLUMN_MODIFIER_MEMBER_ID = "modifier_member_id";
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorMemberId */
  public static final String FIELD_CREATOR_MEMBER_ID = "creatorMemberId";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: disabledTime */
  public static final String FIELD_DISABLED_TIME = "disabledTime";

  /** constant for field name for: email */
  public static final String FIELD_EMAIL = "email";

  /** constant for field name for: enabled */
  public static final String FIELD_ENABLED = "enabled";

  /** constant for field name for: identifier */
  public static final String FIELD_IDENTIFIER = "identifier";

  /** constant for field name for: institution */
  public static final String FIELD_INSTITUTION = "institution";

  /** constant for field name for: modifierMemberId */
  public static final String FIELD_MODIFIER_MEMBER_ID = "modifierMemberId";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: searchStringLower */
  public static final String FIELD_SEARCH_STRING_LOWER = "searchStringLower";

  /** constant for field name for: vettedEmailAddresses */
  public static final String FIELD_VETTED_EMAIL_ADDRESSES = "vettedEmailAddresses";

  
  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_MEMBER_ID, FIELD_DESCRIPTION, 
      FIELD_DISABLED_TIME, FIELD_EMAIL, FIELD_ENABLED, FIELD_IDENTIFIER, 
      FIELD_INSTITUTION, FIELD_MODIFIER_MEMBER_ID, FIELD_MODIFY_TIME, FIELD_NAME, 
      FIELD_SEARCH_STRING_LOWER, FIELD_VETTED_EMAIL_ADDRESSES, FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_MEMBER_ID, FIELD_DESCRIPTION, 
      FIELD_DISABLED_TIME, FIELD_EMAIL, FIELD_ENABLED, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_IDENTIFIER, FIELD_INSTITUTION, FIELD_MODIFIER_MEMBER_ID, FIELD_MODIFY_TIME, 
      FIELD_NAME, FIELD_SEARCH_STRING_LOWER, FIELD_VETTED_EMAIL_ADDRESSES, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /**
   * contextId links to audit tables
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }


  /**
   * search string to find a subject, in all lower case
   * @return search string lower
   */
  public String getSearchStringLower() {
    return searchStringLower;
  }


  /**
   * search string to find a subject, in all lower case
   * @param searchStringLower1
   */
  public void setSearchStringLower(String searchStringLower1) {
    this.searchStringLower = searchStringLower1;
  }

  /**
   * uuid for row
   * @return uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * uuid for row
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * the thing that the subject uses to login
   * @return identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * the thing that the subject uses to login
   * @param identifier1
   */
  public void setIdentifier(String identifier1) {
    this.identifier = identifier1;
  }

  /**
   * name of subject
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * name of subject
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * description, which is generated from other attributes
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * description, which is generated from other attributes
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * email address
   * @return email
   */
  public String getEmail() {
    return email;
  }

  /**
   * email address
   * @param email1
   */
  public void setEmail(String email1) {
    this.email = email1;
  }

  /**
   * institution where the user is from
   * @return institution
   */
  public String getInstitution() {
    return institution;
  }

  /**
   * institution where the user is from
   * @param institution1
   */
  public void setInstitution(String institution1) {
    this.institution = institution1;
  }


  /**
   * deep clone the fields in this object
   */
  @Override
  public ExternalSubject clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /**
   * contextId links to audit tables
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }


  /**
   * Get creation time for this subject.
   * @return  {@link Date} that this subject was created.
   */
  public Date getCreateTime() {
    return new Date(this.getCreateTimeDb());
  }


  /**
   * @return create time
   */
  public long getCreateTimeDb() {
    return this.createTime;
  }


  /**
   * @return creator
   */
  public String getCreatorMemberId() {
    return this.creatorMemberId;
  }

  /**
   * 
   * @return the modifier member id
   */
  public String getModifierMemberId() {
    return this.modifierMemberId;
  }
  
  /**
   * create time
   * @param createTime1 
   */
  public void setCreateTimeDb(long createTime1) {
    this.createTime = createTime1;
  
  }


  /**
   * member id of creator
   * @param creatorMemberId1
   */
  public void setCreatorMemberId(String creatorMemberId1) {
    this.creatorMemberId = creatorMemberId1;
  
  }


  /**
   * member id of modifier
   * @param modifierMemberId1
   */
  public void setModifierMemberId(String modifierMemberId1) {
    this.modifierMemberId = modifierMemberId1;
  
  }


  /**
   * last time modified
   * @param modifyTime1 
   */
  public void setModifyTimeDb(long modifyTime1) {
    this.modifyTime = modifyTime1;
  
  }


  /**
   * Get modify time for this subject.
   * @return  {@link Date} that this subject was created.
   */
  public Date getModifyTime() {
    return new Date(this.getModifyTimeDb());
  }


  /**
   * @return modify time
   */
  public long getModifyTimeDb() {
    return this.modifyTime;
  }

  /**
   * cache if someone can edit external subjects
   */
  private static GrouperCache<MultiKey, Boolean> subjectCanEditExternalUser = 
    new GrouperCache(ExternalSubject.class.getName(), 200, false, 60, 60, false);
  
  /**
   * see if someone is allowed to edit, cache for 1 minute
   * @param subject
   * @return true if allowed to edit
   */
  public static boolean subjectCanEditExternalUser(final Subject subject) {
    
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
    
    Boolean result = subjectCanEditExternalUser.get(multiKey);
    
    if (result != null) {
      return result;
    }
    
    //figure it out
    boolean wheelOrRootCanEdit = GrouperConfig.retrieveConfig().propertyValueBoolean("externalSubjects.wheelOrRootCanEdit", true);
    final String groupAllowedForEdit = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.groupAllowedForEdit");
    
    if (wheelOrRootCanEdit) {
      if (PrivilegeHelper.isWheelOrRoot(subject)) {
        result = true;
      }
    }
    
    if (result == null || !result) {
      if (!StringUtils.isBlank(groupAllowedForEdit)) {
        
        //use root since the current user might not be able to read the group of allowed users
        GrouperSession rootSession = GrouperSession.staticGrouperSession().internal_getRootSession();
        result = (Boolean)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {
          
          public Object callback(GrouperSession theRootSession) throws GrouperSessionException {
            
            Group theGroupAllowedForEdit = GroupFinder.findByName(theRootSession, groupAllowedForEdit, true);
            
            return theGroupAllowedForEdit.hasMember(subject);
            
          }
        });
      }
      
    }
    
    //just cache the positives...
    if (result != null && result) {
      subjectCanEditExternalUser.put(multiKey, result);
    }
    return result != null ? result : false;
  }
  
  /**
   * @see GrouperAPI#onPreSave(HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    if (StringUtils.isBlank(this.getUuid())) {
      this.setUuid(GrouperUuid.getUuid());
    }
    
    this.setModifierMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeDb( System.currentTimeMillis() );

    this.setCreatorMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setCreateTimeDb( System.currentTimeMillis() );
  }


  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    
    super.onPreUpdate(hibernateSession);
    
    this.setModifierMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeDb( System.currentTimeMillis() );

  }

  /**
   * make sure if a field is required it is there
   * @param externalSubjectAttributes
   * @param attributeToDelete 
   */
  private void assertRequiredFieldsAreThere(Set<ExternalSubjectAttribute> externalSubjectAttributes, String attributeToDelete) {
    ExternalSubjectConfigBean externalSubjectConfigBean = ExternalSubjectConfig.externalSubjectConfigBean();
    
    //check name
    if (externalSubjectConfigBean.isNameRequired()) {
      if (StringUtils.isBlank(this.getName())) {
        throw new RuntimeException("Name is a required field.  If unsure what it should be, use the identifier or something: " + this);
      }
    }
    //check email
    if (externalSubjectConfigBean.isEmailRequired()) {
      if (StringUtils.isBlank(this.getEmail())) {
        throw new RuntimeException("Email is a required field: " + this);
      }
    }
    //check institution
    if (externalSubjectConfigBean.isInstitutionRequired()) {
      if (StringUtils.isBlank(this.getInstitution())) {
        throw new RuntimeException("Institution is a required field: " + this);
      }
    }
    
    //check attributes
    for (ExternalSubjectAttributeConfigBean externalSubjectAttributeConfigBean : 
        externalSubjectConfigBean.getExternalSubjectAttributeConfigBeans()) {
      
      if (externalSubjectAttributeConfigBean.isRequired()) {
        
        ExternalSubjectAttribute externalSubjectAttribute = null;
        //first check to see if we sent in attributes...
        if (externalSubjectAttributes != null) {
          
          for (ExternalSubjectAttribute current : externalSubjectAttributes) {
            
            if (StringUtils.equals(current.getAttributeSystemName(), 
                externalSubjectAttributeConfigBean.getSystemName())) {
              externalSubjectAttribute = current;
              break;
            }
            
          }
          
        } else {
          //else see if already in the database
          externalSubjectAttribute = this.retrieveAttribute(externalSubjectAttributeConfigBean.getSystemName(), false);
        }

        //at this point, make sure there is a value
        if (externalSubjectAttribute == null || StringUtils.isBlank(externalSubjectAttribute.getAttributeValue())
            || (!StringUtils.isBlank(attributeToDelete) 
                && StringUtils.equals(attributeToDelete, externalSubjectAttributeConfigBean.getSystemName()))) {
          throw new RuntimeException("External subject attribute: " 
              + externalSubjectAttributeConfigBean.getSystemName() + " is a required field");
        }
      }
    }
  }

  /**
   * store this object to the DB.
   */
  public void store() {    
    this.store(null, null, true, true, false);
  }

  /**
   * 
   * @return the substitute map
   */
  static Map<String, Object> substitutionMap() {

    Map<String, Object> substituteMap = new HashMap<String, Object>();
    substituteMap.put("grouperUtil", new GrouperUtil());

    //middleware.grouper.rules.MyRuleUtils
    String customElClasses = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.customElClasses");

    if (!StringUtils.isBlank(customElClasses)) {
      String[] customElClassesArray = GrouperUtil.splitTrim(customElClasses, ",");
      for (String customElClass : customElClassesArray) {
        Class<?> customClassClass = GrouperUtil.forName(customElClass);
        String simpleName = StringUtils.uncapitalize(customClassClass.getSimpleName());
        substituteMap.put(simpleName, GrouperUtil.newInstance(customClassClass));
      }
    }
    
    return substituteMap;
  }
  
  /**
   * if there are dynamically configured fields, edit that here
   */
  void changeDynamicFields() {
    
    boolean manualDescription = GrouperConfig.retrieveConfig().propertyValueBoolean("externalSubjects.desc.manual", false);
    if (!manualDescription) {
      //description
      String el = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.desc.el");
      if (StringUtils.isBlank(el)) {
        throw new RuntimeException("externalSubjects.desc.el is required in the grouper.properties");
      }
      Map<String, Object> substitutionMap = substitutionMap();
      substitutionMap.put("externalSubject", this);
      
      //do silent since there are warnings on null...
      String description = GrouperUtil.substituteExpressionLanguage(el, substitutionMap, false, true);
      this.setDescription(description);
    }
    
    //lower search string, take the fieldOrAttribute list,
    String searchFields = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.searchStringFields");
    if (StringUtils.isBlank(searchFields)) {
      throw new RuntimeException("externalSubjects.searchStringFields is required in the grouper.properties");
    }
    Set<String> searchFieldSet = GrouperUtil.splitTrimToSet(searchFields, ",");
    StringBuilder lowerSearchString = new StringBuilder();
    for (String searchField : searchFieldSet) {
      String fieldValue = this.retrieveFieldValue(searchField);
      fieldValue = StringUtils.trimToEmpty(fieldValue);
      if (!StringUtils.isBlank(fieldValue)) {
        if (lowerSearchString.length() > 0) {
          lowerSearchString.append(", ");
        }
        lowerSearchString.append(fieldValue.toLowerCase());
      }
      
    }
    
    this.setSearchStringLower(lowerSearchString.toString());
    
  }
  
  /**
   * get the value by field name or attribute
   * @param fieldOrAttributeName
   * @return the value
   */
  public String retrieveFieldValue(String fieldOrAttributeName) {
    String fieldValue = null;
    if (StringUtils.equalsIgnoreCase("name", fieldOrAttributeName)) {
      fieldValue = this.getName();
    } else if (StringUtils.equalsIgnoreCase("uuid", fieldOrAttributeName)) {
      fieldValue = this.getUuid();
    } else if (StringUtils.equalsIgnoreCase("email", fieldOrAttributeName)) {
      fieldValue = this.getEmail();
    } else if (StringUtils.equalsIgnoreCase("identifier", fieldOrAttributeName)) {
      fieldValue = this.getIdentifier();
    } else if (StringUtils.equalsIgnoreCase("description", fieldOrAttributeName)) {
      fieldValue = this.getDescription();
    } else if (StringUtils.equalsIgnoreCase("institution", fieldOrAttributeName)) {
      fieldValue = this.getInstitution();
    } else {
      //must be an attribute
      ExternalSubjectAttribute externalSubjectAttribute = this.retrieveAttribute(fieldOrAttributeName, false);
      fieldValue = externalSubjectAttribute == null ? null : externalSubjectAttribute.getAttributeValue();
    }
    return fieldValue;
  }
  
  /**
   * store this object to the DB.
   * @param externalSubjectAttributes null to not worry, not null to affect the external subject attributes too
   * @param externalSubjectInviteName is a variable you could put in the URL to pass to the hook so you can
   * add the users to custom groups or whatnot
   * @param validateAttributes if required attributes should be validated
   * @param autoaddGroups if groups should be auto-added (e.g. not from daemon)
   * @param fromRecalcDaemon if the recalc daemon is doing the storing
   */
  public void store(final Set<ExternalSubjectAttribute> externalSubjectAttributes, 
      final String externalSubjectInviteName, final boolean validateAttributes, 
      final boolean autoaddGroups,
      final boolean fromRecalcDaemon) {    
    
    this.assertCurrentUserCanEditExternalUsers();
    
    this.changeDynamicFields();
    
    if (validateAttributes) {
      this.assertRequiredFieldsAreThere(externalSubjectAttributes, null);
    }
    
    this.calculateDisabledFlag();
    
    this.validateIdentifier();
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          /**
           * 
           */
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            boolean isInsert = (Boolean)HibernateSession.callbackHibernateSession(
                GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
                new HibernateHandler() {

                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
            
                    hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

                    boolean isInsert = HibUtilsMapping.isInsert(ExternalSubject.this);

                    //GrouperDAOFactory.getFactory().getExternalSubject().saveOrUpdate( ExternalSubject.this );
                    ExternalSubjectStorageController.saveOrUpdate(ExternalSubject.this);

                    if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                      AuditEntry auditEntry = null;

                      if (isInsert) {
                        auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJECT_ADD, "id", 
                            ExternalSubject.this.getUuid(), "name", ExternalSubject.this.getName(), "identifier", ExternalSubject.this.getIdentifier());
                        auditEntry.setDescription("Added external subject: " + ExternalSubject.this.getDescription());
                      } else {
                        auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJECT_UPDATE, "id", 
                            ExternalSubject.this.getUuid(), "name", ExternalSubject.this.getName(), "identifier", ExternalSubject.this.getIdentifier());
                        auditEntry.setDescription("Updated external subject: " + ExternalSubject.this.getDescription());

                      }
                      auditEntry.saveOrUpdate(true);
                    }

                    return isInsert;
                  }
                });

            for (ExternalSubjectAttribute externalSubjectAttribute : GrouperUtil.nonNull(externalSubjectAttributes)) {
              
              externalSubjectAttribute.store(ExternalSubject.this);
              
            }
            
            if (autoaddGroups && ExternalSubject.this.isEnabled()) {
              //#put some group names comma separated for groups to auto add subjects to
              //externalSubjects.autoaddGroups=
              //#should be insert, update, or insert,update
              //externalSubjects.autoaddGroupActions=insert,update
              //#if a number is here, expire the group assignment after a certain number of days
              //externalSubjects.autoaddGroupExpireAfterDays=
              String actions = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.autoaddGroupActions");
              String groups = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.autoaddGroups");
              int expireAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("externalSubjects.autoaddGroupExpireAfterDays", -1);
  
              assignGroups(groups, actions, isInsert, expireAfterDays);
  
              //
              //#add multiple group assignment actions by URL param: externalSubjectInviteName
              //externalSubject.autoadd.testingLibrary.externalSubjectInviteName=library
              //#comma separated groups to add for this type of invite
              //externalSubject.autoadd.testingLibrary.groups=
              //#should be insert, update, or insert,update
              //externalSubject.autoadd.testingLibrary.actions=insert,update
              //#should be insert, update, or insert,update
              //externalSubject.autoadd.testingLibrary.expireAfterDays=
  
              if (!StringUtils.isBlank(externalSubjectInviteName)) {
                
                ExternalSubjectAutoaddBean externalSubjectAutoaddBean = 
                  ExternalSubjectConfig.externalSubjectAutoaddConfigBean().get(externalSubjectInviteName);
                
                if (externalSubjectAutoaddBean != null) {
                  assignGroups(externalSubjectAutoaddBean.getGroups(), externalSubjectAutoaddBean.getActions(), isInsert, externalSubjectAutoaddBean.getExpireAfterDays());
                }
                
              }
            }
            
            // high level external subject hook
            GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.EXTERNAL_SUBJECT, 
                ExternalSubjectHooks.METHOD_POST_EDIT_EXTERNAL_SUBJECT,
                HooksExternalSubjectBean.class, new Object[]{ExternalSubject.this, isInsert, !isInsert, externalSubjectAttributes, externalSubjectInviteName, autoaddGroups, fromRecalcDaemon}, 
                new Class[]{ExternalSubject.class, boolean.class, boolean.class, Set.class, String.class, boolean.class, boolean.class}, 
                VetoTypeGrouper.EXTERNAL_SUBJECT_POST_EDIT);

            return null;
          }

        });

  }

  /**
   * validate that the identifier is ok, based on grouper.properties
   */
  public void validateIdentifier() {
    
    if (StringUtils.isBlank(this.identifier)) {
      throw new RuntimeException("Identifier cannot be blank");
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("externalSubjects.validateIndentiferLikeEmail", true)) {
      if (!GrouperUtil.validEmail(this.identifier)) {
        throw new RuntimeException("Not allowed to register this identifier, should be something like a@b.c: '" + this.identifier + "'");
      }
    }
     
    for (int i=0;i<100;i++) {
      String regex = GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.regexForInvalidIdentifier." + i);
      if (StringUtils.isBlank(regex)) {
        break;
      }
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(this.identifier);
      if (matcher.matches()) {
        throw new RuntimeException("Identifier '" + this.identifier + "' cannot match regex: " + regex);
      }
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ExternalSubject.class);

  /**
   * assign groups to this subject
   * @param groups
   * @param actions insert, update, or insert,update
   * @param isInsert 
   * @param expireAfterDays
   */
  private void assignGroups(String groups, String actions, boolean isInsert, final int expireAfterDays) {
    
    boolean addGroups = true;
    if (!StringUtils.isBlank(actions)) {
      if (isInsert && !actions.contains("insert")) {
        addGroups = false;
      }
      if (!isInsert && !actions.contains("update")) {
        addGroups = false;
      }
    }
    
    if (StringUtils.isBlank(groups)) {
      addGroups = false;
    }
    
    if (!addGroups) {
      return;
    }

    final Subject subject = SubjectFinder.findByIdAndSource(this.getUuid(), sourceId(), true);
    final Set<String> groupNameSet = GrouperUtil.splitTrimToSet(groups, ",");
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    boolean startedGrouperSession = false;
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession(false);
      startedGrouperSession = true;
    }
    if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
      grouperSession = grouperSession.internal_getRootSession();
    }
    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          for (String groupName : groupNameSet) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Assigning external subject to group: " + groupName + ", and expireAfterDays: " + expireAfterDays);
            }
            Group group = GroupFinder.findByName(grouperSession, groupName, true);
            group.addMember(subject, false);
            if (expireAfterDays > 0) {
              Membership membership = group.getImmediateMembership(Group.getDefaultList(), subject, true, true);
              membership.setEnabledTime(new Timestamp(System.currentTimeMillis() + ((long)expireAfterDays*24*60*60*1000)));
              membership.update();
            }
          }

          return null;
        }
      });
    } finally {
      if (startedGrouperSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * source name for external subjects
   * @return name
   */
  public static String sourceName() {
    String sourceName = GrouperConfig.retrieveConfig().propertyValueString("externalSubject.sourceName");
    if (StringUtils.isBlank(sourceName)) {
      return "External Users";
    }
    return sourceName;
  }
  
  /**
   * source id for external subjects
   * @return id
   */
  public static String sourceId() {
    String sourceId = GrouperConfig.retrieveConfig().propertyValueString("externalSubject.sourceId");
    if (StringUtils.isBlank(sourceId)) {
      return "grouperExternal";
    }
    return sourceId;
  }
  
  /**
   * delete this object from the DB.
   */
  public void delete() {    
    
    assertCurrentUserCanEditExternalUsers();
    
    HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
  
          //GrouperDAOFactory.getFactory().getExternalSubject().delete( ExternalSubject.this );
          ExternalSubjectStorageController.delete(ExternalSubject.this);  
          
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = null;
            
            auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJECT_DELETE, "id", 
                ExternalSubject.this.getUuid(), "name", ExternalSubject.this.getName(), "identifier", ExternalSubject.this.getIdentifier());
            auditEntry.setDescription("Deleted external subject: " + ExternalSubject.this.getDescription());
            auditEntry.saveOrUpdate(true);
          }
  
          return null;
        }
      });
    
  }

  /**
   * calculate the disabled flag on this record
   */
  private void calculateDisabledFlag() {
    
    this.enabled = this.disabledTime == null || this.disabledTime > System.currentTimeMillis();
    
  }
  
  /** keep this reference for testing */
  static int lastDisabledFixCount = -1;
  
  /**
   * fix enabled and disabled memberships, and return the count of how many were fixed
   * @return the number of records affected
   */
  public static int internal_fixDisabled() {
    
    //Set<ExternalSubject> externalSubjects = GrouperDAOFactory.getFactory().getExternalSubject().findAllDisabledMismatch();
    Set<ExternalSubject> externalSubjects = ExternalSubjectStorageController.findAllDisabledMismatch();
    
    for (ExternalSubject externalSubject : externalSubjects) {
      //store will fix the disabled flag
      externalSubject.store();
    }
    lastDisabledFixCount = externalSubjects.size();
    return externalSubjects.size();
  }

  
  /**
   * fix enabled and disabled memberships, and return the count of how many were fixed
   * @return the number of records affected
   */
  public static int internal_daemonCalcFields() {
    
    //Set<ExternalSubject> externalSubjects = GrouperDAOFactory.getFactory().getExternalSubject().findAll();
    Set<ExternalSubject> externalSubjects = ExternalSubjectStorageController.findAll();
    int countChanged = 0;
    for (ExternalSubject externalSubject : externalSubjects) {

      String description = externalSubject.getDescription();
      String searchStringLower = externalSubject.getSearchStringLower();
      boolean enabled = externalSubject.isEnabled();
      
      externalSubject.changeDynamicFields();
      externalSubject.calculateDisabledFlag();
      
      //see if something changed
      if (!StringUtils.equals(description, externalSubject.getDescription())
          || !StringUtils.equals(searchStringLower, externalSubject.getSearchStringLower())
          || enabled != externalSubject.isEnabled()) {
        externalSubject.store(null, null, false, false, true);
        countChanged++;
      }
      
    }
    lastDisabledFixCount = countChanged;
    return countChanged;
  }

  
  /**
   * assign an attribute to this subject, change value if already exists, add if not
   * @param attributeName
   * @param attributeValue
   * @return true if changed anything, false if not
   */
  public boolean assignAttribute(String attributeName, String attributeValue) {
    assertCurrentUserCanEditExternalUsers();
    
    if (StringUtils.isBlank(this.getUuid())) {
      throw new RuntimeException("uuid cannot be null! " + this);
    }
    
    ExternalSubjectAttribute externalSubjectAttribute = this.retrieveAttribute(attributeName, false);
    if (externalSubjectAttribute == null) {
      externalSubjectAttribute = new ExternalSubjectAttribute();
      externalSubjectAttribute.setAttributeSystemName(attributeName);
      externalSubjectAttribute.setAttributeValue(attributeValue);
      externalSubjectAttribute.setSubjectUuid(this.getUuid());
      externalSubjectAttribute.store(this);
      //recalculate dynamic fields in the store() method
      this.store();
      return true;
    }

    if (!StringUtils.equals(externalSubjectAttribute.getAttributeValue(), attributeValue)) {
      externalSubjectAttribute.setAttributeValue(attributeValue);
      externalSubjectAttribute.store(this);
      //recalculate dynamic fields in the store() method
      this.store();
      return true;
    }
    
    //didnt change
    return false;
  }
  
  /**
   * get all attributes for this subject
   * @return the attributes
   */
  public Set<ExternalSubjectAttribute> retrieveAttributes() {
    return ExternalSubjectAttributeStorageController.findBySubject(this.getUuid(), new QueryOptions().secondLevelCache(false));
  }
  
  /**
   * get an attributes for this subject
   * @param attributeName
   * @param exceptionIfNotFound 
   * @return the attributes
   */
  public ExternalSubjectAttribute retrieveAttribute(String attributeName, boolean exceptionIfNotFound) {
    
    assertCurrentUserCanEditExternalUsers();
    
    //if this attribute is invalid, then throw exception
    ExternalSubjectAttribute.assertValidAttribute(attributeName);

    Set<ExternalSubjectAttribute> externalSubjectAttributes = this.retrieveAttributes();
    for (ExternalSubjectAttribute externalSubjectAttribute : GrouperUtil.nonNull(externalSubjectAttributes)) {
      if (StringUtils.equals(attributeName, externalSubjectAttribute.getAttributeSystemName())) {
        return externalSubjectAttribute;
      }
    }
    
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find attribute assignment: " + attributeName + " for subject: " + this);
    }
    return null;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    try {
      if (this.uuid != null) {
        result.append("uuid: ").append(this.uuid).append(", ");
      }
      if (this.identifier != null) {
        result.append("identifier: ").append(this.identifier).append(", ");
      }
      if (this.name != null) {
        result.append("name: ").append(this.name).append(", ");
      }
      if (this.description != null) {
        result.append("description: ").append(this.description).append(", ");
      }
    } catch (Exception e) {
      //ignore, we did the best we could
    }
    return result.toString();
  }

  
  /**
   * remove an attribute
   * @param attributeName
   * @return true if did anything
   */
  public boolean removeAttribute(String attributeName) {
    
    assertCurrentUserCanEditExternalUsers();

    assertRequiredFieldsAreThere(null, attributeName);
    
    ExternalSubjectAttribute externalSubjectAttribute = this.retrieveAttribute(attributeName, false);
    if (externalSubjectAttribute == null) {
      return false;
    }
    externalSubjectAttribute.delete(this);
    //recalculate dynamic fields in the store() method
    this.store();
    return true;
  }


  /**
   * make sure security is ok
   */
  private void assertCurrentUserCanEditExternalUsers() {
    Subject currentSubject = GrouperSession.staticGrouperSession().getSubject();

    if (!subjectCanEditExternalUser(currentSubject)) {
      throw new RuntimeException("Subject cannot edit external users (per grouper.properties): " + GrouperUtil.subjectToString(currentSubject));
    }
  }
  
}
