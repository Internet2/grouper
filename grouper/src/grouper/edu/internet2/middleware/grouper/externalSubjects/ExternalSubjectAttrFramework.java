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

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * data and logic about attributes for external subjects
 * @author mchyzer
 *
 */
public class ExternalSubjectAttrFramework {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ExternalSubjectAttrFramework.class);

  /**
   * invite external users to register with grouper.  Note, there needs to a wheel/root 
   * grouper session open when calling this method
   * @param emailAddresses
   * @param externalSubjectInviteBean put most of the data in this bean
   * @param email to send, must have $inviteLink$ variable in it, or it will be added to the end
   * @param emailSubject of the email or null to get the default
   * @return the error message or null if ok
   */
  public static String inviteExternalUsers(Set<String> emailAddresses, 
      ExternalSubjectInviteBean externalSubjectInviteBean, String emailSubject, final String email) {

    if (GrouperUtil.length(emailAddresses) == 0) {
      throw new RuntimeException("Must pass in email addresses");
    }
    
    if (StringUtils.isBlank(externalSubjectInviteBean.getMemberId())) {
      throw new RuntimeException("Must pass in who invited the users");
    }
    
    if (GrouperUtil.length(externalSubjectInviteBean.getGroupIds()) > 50) {
      throw new RuntimeException("Cant pass in more than 50 groups to assign");
    }

    long expireMillisAfter1970 = expireMillisAfter1970();
    
    String emailTemplate = email;
    if (StringUtils.isBlank(emailTemplate)) {
      
      //use a standard one
      emailTemplate = GrouperConfig.retrieveConfig().propertyValueString("externalSubjectsInviteDefaultEmail");

    }

    if (StringUtils.isBlank(emailTemplate)) {
      throw new RuntimeException("Email template is blank, so is the default in grouper.properties: externalSubjectsInviteDefaultEmail");
    }
    
    if (!emailTemplate.contains("$inviteLink$")) {
      emailTemplate = emailTemplate + "$newline$$newline$$inviteLink$";
    }

    String uiUrl = GrouperConfig.getGrouperUiUrl(true);

    emailSubject = StringUtils.isBlank(emailSubject) ? GrouperConfig.retrieveConfig().propertyValueString("externalSubjectsInviteDefaultEmailSubject") : emailSubject;
    
    if (StringUtils.isBlank(emailSubject)) {
      throw new RuntimeException("Email subject cannot be blank.  One must be specified or " +
      		"set in the grouper.properties: externalSubjectsInviteDefaultEmailSubject");
    }
    

    StringBuilder errors = new StringBuilder();
    
    for (String emailAddress : emailAddresses) {
      
      try {
        
        ExternalSubjectInviteBean currentExternalSubjectInviteBean = (ExternalSubjectInviteBean)externalSubjectInviteBean.clone();
        
        currentExternalSubjectInviteBean.setInviteDate(System.currentTimeMillis());
        
        currentExternalSubjectInviteBean.setEmailAddress(emailAddress);
        
        String theEmail = StringUtils.replace(emailTemplate, "$newline$", "\n" );
        
        String uuid = GrouperUuid.getUuid();
        
        String theUrl = uiUrl + "grouperExternal/appHtml/grouper.html?operation=ExternalSubjectSelfRegister.externalSubjectSelfRegister&externalSubjectInviteId=" + uuid;
        
        currentExternalSubjectInviteBean.setUuid(uuid);
  
        theEmail = StringUtils.replace(theEmail, "$inviteLink$", theUrl);

        currentExternalSubjectInviteBean.setEmail(StringUtils.abbreviate(theEmail, 2000));
        
        if (expireMillisAfter1970 > 0) {

          currentExternalSubjectInviteBean.setExpireDate(expireMillisAfter1970);

        }
        
        currentExternalSubjectInviteBean.storeToDb();
        
        new GrouperEmail().setTo(emailAddress).setSubject(emailSubject).setBody(theEmail).send();
      
      } catch (Exception e) {
        LOG.error("error with email address: " + emailAddress, e);
        if (errors.length() > 0) {
          errors.append(", ");
        }
        errors.append(emailAddress).append(" had a problem: ").append(e.getMessage());
      }
    }
    
    if (errors.length() == 0) {
      return null;
    }

    return errors.toString();
    
  }

  /**
   * @return millis that this will expire
   */
  public static long expireMillisAfter1970() {
    long expireMillisAfter1970 = GrouperConfig.retrieveConfig().propertyValueInt("externalSubjectsInviteExpireAfterDays", 7);

    if (expireMillisAfter1970 > 0) {
      
      expireMillisAfter1970 = System.currentTimeMillis() + (expireMillisAfter1970 * 1000 * 60 * 60 * 24);
      
    }
    return expireMillisAfter1970;
  }
  
  /**
   * return the rule attribute def name, assign this to an object to attach a rule.
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName externalSubjectInviteAttributeDefName() {
    return AttributeDefNameFinder.findByName(attributeExternalSubjectInviteStemName() + ":externalSubjectInvite", true);
  }

  /**
   * return the uuid attribute def name
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName externalSubjectInviteUuidAttributeDefName() {
    return AttributeDefNameFinder.findByName(externalSubjectInviteUuidName(), true);
  }

  /**
   * return the email address attribute def name
   * this throws exception if cant find
   * @return the attribute def name
   */
  public static AttributeDefName externalSubjectInviteEmailAddressAttributeDefName() {
    return AttributeDefNameFinder.findByName(externalSubjectInviteEmailAddressName(), true);
  }

  //- attribute: emails to tell when registered

  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_EMAIL_WHEN_REGISTERED = "externalSubjectInviteEmailWhenRegistered";
  
  /**
   * rule externalSubjectInviteEmailWhenRegistered
   */
  private static String externalSubjectInviteEmailWhenRegisteredName = null;

  /**
   * full externalSubjectInviteEmailWhenRegisteredName
   * @return name
   */
  public static String externalSubjectInviteEmailWhenRegisteredName() {
    if (externalSubjectInviteEmailWhenRegisteredName == null) {
      externalSubjectInviteEmailWhenRegisteredName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_EMAIL_WHEN_REGISTERED;
    }
    return externalSubjectInviteEmailWhenRegisteredName;
  }
  
  //- attribute: email sent

  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_EMAIL = "externalSubjectInviteEmail";
  
  /**
   * rule externalSubjectInviteEmailName
   */
  private static String externalSubjectInviteEmailName = null;

  /**
   * full externalSubjectInviteEmailName
   * @return name
   */
  public static String externalSubjectInviteEmailName() {
    if (externalSubjectInviteEmailName == null) {
      externalSubjectInviteEmailName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_EMAIL;
    }
    return externalSubjectInviteEmailName;
  }
  
  //- attribute: memberId who invited
  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_MEMBER_ID = "externalSubjectInviteMemberId";
  
  /**
   * rule externalSubjectInviteMemberIdName
   */
  private static String externalSubjectInviteMemberIdName = null;

  /**
   * full externalSubjectInviteMemberIdName
   * @return name
   */
  public static String externalSubjectInviteMemberIdName() {
    if (externalSubjectInviteMemberIdName == null) {
      externalSubjectInviteMemberIdName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_MEMBER_ID;
    }
    return externalSubjectInviteMemberIdName;
  }


  
  //- attribute: group uuid list comma separated
  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_GROUP_UUIDS = "externalSubjectInviteGroupUuids";
  
  /**
   * rule externalSubjectInviteGroupUuidsName
   */
  private static String externalSubjectInviteGroupUuidsName = null;

  /**
   * full externalSubjectInviteGroupUuidsName
   * @return name
   */
  public static String externalSubjectInviteGroupUuidsName() {
    if (externalSubjectInviteGroupUuidsName == null) {
      externalSubjectInviteGroupUuidsName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_GROUP_UUIDS;
    }
    return externalSubjectInviteGroupUuidsName;
  }


  //- attribute: uuid
  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_UUID = "externalSubjectInviteUuid";
  
  /**
   * rule externalSubjectInviteUuidName
   */
  private static String externalSubjectInviteUuidName = null;

  /**
   * full externalSubjectInviteUuidName
   * @return name
   */
  public static String externalSubjectInviteUuidName() {
    if (externalSubjectInviteUuidName == null) {
      externalSubjectInviteUuidName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_UUID;
    }
    return externalSubjectInviteUuidName;
  }

  //- attribute: email address

  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_EMAIL_ADDRESS = "externalSubjectEmailAddress";
  
  /**
   * rule externalSubjectInviteEmailAddress
   */
  private static String externalSubjectInviteEmailAddressName = null;

  /**
   * full externalSubjectInviteEmailAddress
   * @return name
   */
  public static String externalSubjectInviteEmailAddressName() {
    if (externalSubjectInviteEmailAddressName == null) {
      externalSubjectInviteEmailAddressName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_EMAIL_ADDRESS;
    }
    return externalSubjectInviteEmailAddressName;
  }

  //- attribute: expire date

  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_EXPIRE_DATE = "externalSubjectInviteExpireDate";
  
  /**
   * rule externalSubjectInviteExpireDateName
   */
  private static String externalSubjectInviteExpireDateName = null;

  /**
   * full externalSubjectInviteExpireDateName
   * @return name
   */
  public static String externalSubjectInviteExpireDateName() {
    if (externalSubjectInviteExpireDateName == null) {
      externalSubjectInviteExpireDateName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_EXPIRE_DATE;
    }
    return externalSubjectInviteExpireDateName;
  }

  //- attribute: issued date

  /**
   * 
   */
  public static final String EXTERNAL_SUBJECT_INVITE_DATE = "externalSubjectInviteDate";
  
  /**
   * rule externalSubjectInviteDateName
   */
  private static String externalSubjectInviteDateName = null;

  /**
   * full externalSubjectInviteDateName
   * @return name
   */
  public static String externalSubjectInviteDateName() {
    if (externalSubjectInviteDateName == null) {
      externalSubjectInviteDateName = attributeExternalSubjectInviteStemName() + ":" + EXTERNAL_SUBJECT_INVITE_DATE;
    }
    return externalSubjectInviteDateName;
  }

  /**
   * return the stem name where the attribute extenral subject attributes go, without colon on end
   * @return stem name
   */
  public static String attributeExternalSubjectInviteStemName() {
    String rootStemName = GrouperCheckConfig.attributeRootStemName();
    
    //namespace this separate from other builtins
    rootStemName += ":attrExternalSubjectInvite";
    return rootStemName;
  }
  

  
}
