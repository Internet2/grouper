/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.attestation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.EmailObject;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 * attestation daemon
 */
@DisallowConcurrentExecution
public class GrouperAttestationJob extends OtherJobBase {
  
  /**
   * two weeks days left
   */
  public static Set<Object> TWO_WEEKS_DAYS_LEFT = GrouperUtil.toSetObjectType("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
  
  /**
   * 
   */
  public static final String ATTESTATION_LAST_EMAILED_DATE = "attestationLastEmailedDate";

  /**
   * last emailed attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailedDate() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_LAST_EMAILED_DATE, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation emailed date attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   */
  public static final String ATTESTATION_CALCULATED_DAYS_LEFT = "attestationCalculatedDaysLeft";

  /**
   * calculated days left attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameCalculatedDaysLeft() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_CALCULATED_DAYS_LEFT, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation calculated days attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   * @return the def
   */
  public static AttributeDef retrieveAttributeDef() {
    AttributeDef attributeDef = AttributeDefFinder.findByName(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DEF, false);
    
    if (attributeDef == null) {
      throw new RuntimeException("Why cant attestation attributeDef not be found?");
    }
    
    return attributeDef;
  }

  /**
   * 
   */
  public static final String ATTESTATION_SEND_EMAIL = "attestationSendEmail";

  /**
   * send email attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameSendEmail() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_SEND_EMAIL, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation send email attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * attestationType
   */
  public static final String ATTESTATION_TYPE = "attestationType";
  
  /**
   * attestationReportId
   */
  public static final String ATTESTATION_REPORT_CONFIGURATION_ID = "attestationReportConfigurationId";
  
  /**
   * attestationAuthorizedGroupId
   */
  public static final String ATTESTATION_AUTHORIZED_GROUP_ID = "attestationAuthorizedGroupId";
  
  
  /**
   * type of attestation (group or report)
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameType() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_TYPE, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation type attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * report id
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameReportConfigurationId() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_REPORT_CONFIGURATION_ID, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation report configuration id attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * authorized group id
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAuthorizedGroupId() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_AUTHORIZED_GROUP_ID, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation authorized group id attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * attestationHasAttestation
   */
  public static final String ATTESTATION_HAS_ATTESTATION = "attestationHasAttestation";

  /**
   * if this object has attestation assigned (either in group or stem)
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameHasAttestation() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_HAS_ATTESTATION, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation has attestation attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   * @param nameOfAttributeDefName
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameByName(String nameOfAttributeDefName) {
    
    if (!nameOfAttributeDefName.startsWith(GrouperAttestationJob.attestationStemName() + ":")) {
      throw new RuntimeException("Why doesnt attribute start with '" 
          + GrouperAttestationJob.attestationStemName() + ":' ???? '" + nameOfAttributeDefName + "'");
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(nameOfAttributeDefName, true);
    
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation attribute def name be found?");
    }
    return attributeDefName;

  }
  
 /**
   * 
   */
  public static final String ATTESTATION_DATE_CERTIFIED = "attestationDateCertified";

  /**
   * date certified attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDateCertified() {

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DATE_CERTIFIED, true);
    
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation date certified attribute def name be found?");
    }
    return attributeDefName;

  }  

  /**
   * 
   */
  private static final String ATTESTATION_DEF = "attestationDef";
  /**
   * 
   */
  public static final String ATTESTATION_DAYS_UNTIL_RECERTIFY = "attestationDaysUntilRecertify";
  
  /**
   * days until recertify attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDaysUntilRecertify() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_UNTIL_RECERTIFY, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation days until recertify attribute def name be found?");
    }
    return attributeDefName;

  }
  

  /**
   * 
   */
  public static final String ATTESTATION_DAYS_BEFORE_TO_REMIND = "attestationDaysBeforeToRemind";
  
  /**
   * days before remind attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDaysBeforeToRemind() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_BEFORE_TO_REMIND, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation days before to remind attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   */
  public static final String ATTESTATION_EMAIL_ADDRESSES = "attestationEmailAddresses";
  
  /**
   * email addresses attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailAddresses() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_EMAIL_ADDRESSES, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation email addresses attribute def name be found?");
    }
    return attributeDefName;

  }
  

  /**
   * 
   */
  public static final String ATTESTATION_DIRECT_ASSIGNMENT = "attestationDirectAssignment";
  
  /**
   * direct assignment attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDirectAssignment() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DIRECT_ASSIGNMENT, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation direct assignment attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   */
  public static final String ATTESTATION_VALUE_DEF = "attestation";
  
  /**
   * attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameValueDef() {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_VALUE_DEF, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation value def attribute def name be found?");
    }
    return attributeDefName;
  }
  
  
  /**
   * 
   */
  public static final String ATTESTATION_STEM_SCOPE = "attestationStemScope";

  /**
   * attribute def name of which scope when assigned to stem
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameStemScope() {

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByNameAsRoot(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_STEM_SCOPE, true);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation def name for stem scope be found?");
    }
    return attributeDefName;
  }

  /**
   * 
   * @return the stem name
   */
  public static String attestationStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:attestation";
  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperAttestationJob.class);

  /**
   * @param groupAttributeAssign
   * @param group
   */
  public static void updateObjectAttributesToPatch81(Group group, AttributeAssign groupAttributeAssign) {
    updateObjectAttributesToPatch81(group, groupAttributeAssign, true);
  }
  
  /**
   * @param stem
   * @return attribute assign
   */
  public static AttributeAssign findParentFolderAssign(Stem stem) {
    AttributeAssign parentFolderAssign = null;
    Stem currentGrouperObject = stem;
    while (true) {
      parentFolderAssign = null;

      if (currentGrouperObject.isRootStem()) {
        break;
      }
      
      AttributeAssignable attributeAssignable = currentGrouperObject.getAttributeDelegate()
          .getAttributeOrAncestorAttribute(retrieveAttributeDefNameValueDef().getName(), false);
      parentFolderAssign = attributeAssignable == null ? null : attributeAssignable.getAttributeDelegate().retrieveAssignment(
          null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
      
      if (parentFolderAssign == null) {
        break;
      }
      // we have a folder assignment
      // see if has assignment
      String folderStemScopeAttributeValue = parentFolderAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameStemScope().getName());
      Stem.Scope scope = StringUtils.isBlank(folderStemScopeAttributeValue) ? Scope.SUB : Stem.Scope.valueOfIgnoreCase(folderStemScopeAttributeValue, true);

      // if scope is one and we are too far up the chain then we're done
      boolean selfAssignment = stem.getUuid().equals(((Stem)attributeAssignable).getUuid());
      if (scope == Scope.ONE && !selfAssignment) {
        parentFolderAssign = null;
        break;
      }
      
      String folderTypeAttributeValue = parentFolderAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameType().getName());
      if ("report".equals(folderTypeAttributeValue)) {
        // not what we're looking for
        parentFolderAssign = null;
        currentGrouperObject = ((Stem)attributeAssignable).getParentStem();
        continue;
      }
      
      String folderHasAttestationAttributeValue = parentFolderAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());
      boolean folderHasAttestationAttributeValueBoolean = GrouperUtil.booleanValue(folderHasAttestationAttributeValue, false);

      // if the folder doesnt have attestation, then we're done
      if (!folderHasAttestationAttributeValueBoolean) {
        parentFolderAssign = null;
        break;
      }
      
      //we good
      break;
    }
    
    return parentFolderAssign;
  }

  /**
   * @param group
   * @param resetCalculatedDaysLeft
   */
  public static void updateAttestationMetadataForSingleObject(Group group, boolean resetCalculatedDaysLeft) {

    if (GrouperCheckConfig.isInCheckConfig()) {
      //things might not be setup yet
      return;
    }
    
    if (group == null) {
      return;
    }
    
    AttributeAssignBaseDelegate attributeAssignBaseDelegate = group.getAttributeDelegate();
    
    AttributeAssign attributeAssign = attributeAssignBaseDelegate.retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);

    //if has attestation then fine
    String hasAttestationAttributeValue = attributeAssign == null ? null : attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());
    boolean hasAttestationAttributeValueBoolean = GrouperUtil.booleanValue(hasAttestationAttributeValue, false);

    String directAssignmentAttributeValue = attributeAssign == null ? null : attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDirectAssignment().getName());
    boolean directAssignmentAttributeValueBoolean = GrouperUtil.booleanValue(directAssignmentAttributeValue, false);
    
    // if has and direct then we good, dont worry about it 
    if (hasAttestationAttributeValueBoolean && directAssignmentAttributeValueBoolean) {
      return;
    }

    //get the ancestor folder assignment if exists
    AttributeAssign parentFolderAssign = findParentFolderAssign(group.getParentStem());
    
    // there is not a parent folder
    if (parentFolderAssign == null) {
      
      if (hasAttestationAttributeValueBoolean) {
        attributeAssign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "false");
      }
      return;
    }
    
    if (attributeAssign == null) {
      attributeAssign = attributeAssignBaseDelegate.assignAttribute(retrieveAttributeDefNameValueDef()).getAttributeAssign();
    }

    attributeAssign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
    attributeAssign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "false");
    
    String attestationDateCertified = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDateCertified().getName());
    
    String configuredAttestationDaysUntilRecertify = parentFolderAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysUntilRecertify().getName());
    
    updateCalculatedDaysLeft(attributeAssign, attestationDateCertified, configuredAttestationDaysUntilRecertify, resetCalculatedDaysLeft);
  }

  /**
   * 
   * @param attributeAssign
   * @param attestationDateCertified
   * @param configuredAttestationDaysUntilRecertify
   * @param resetCalculatedDaysLeft
   * @return days needed until recertify
   */
  private static int updateCalculatedDaysLeft(AttributeAssign attributeAssign,
      String attestationDateCertified, String configuredAttestationDaysUntilRecertify, boolean resetCalculatedDaysLeft) {
    int configuredDaysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (! StringUtils.isBlank(configuredAttestationDaysUntilRecertify)) {
      configuredDaysUntilRecertify = Integer.valueOf(configuredAttestationDaysUntilRecertify);
    }
    
    // find the difference between today's date and last certified date
    // and if the difference is greater than daysUntilRecertify minus attestationDaysBeforeToRemind, then sendEmail
    int daysUntilNeedsCertify = 0;
    if (!StringUtils.isBlank(attestationDateCertified)) {
      Date lastCertifiedDate = null;
      try {
        lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(attestationDateCertified);
      } catch (ParseException e) {
        LOG.error("Could not convert "+attestationDateCertified+" to date. Attribute assign id is: "+attributeAssign.getId(), e);
        return -1;
      }
      long millisSinceCertify = new Date().getTime() - lastCertifiedDate.getTime();
      int daysSinceCertify = (int)TimeUnit.DAYS.convert(millisSinceCertify, TimeUnit.MILLISECONDS);
      daysUntilNeedsCertify = configuredDaysUntilRecertify - daysSinceCertify;
      if (daysUntilNeedsCertify < 0) {
        daysUntilNeedsCertify = 0;
      }
    }
    
    if (resetCalculatedDaysLeft) {
      daysUntilNeedsCertify = configuredDaysUntilRecertify;
      
      String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
      attributeAssign.getAttributeValueDelegate().assignValue(
          GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), date);

    }
    
    attributeAssign.getAttributeValueDelegate().assignValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "" + daysUntilNeedsCertify);
    return daysUntilNeedsCertify;
  }

  
  /**
   * @param groupAttributeAssign
   * @param group
   * @param updateLastCertified
   */
  public static void updateObjectAttributesToPatch81(Group group, AttributeAssign groupAttributeAssign, boolean updateLastCertified) {
    
    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "updateObjectAttributesToPatch81");
      debugMap.put("entered", GrouperUtil.timestampToString(new Date()));
      debugMap.put("groupAttributeAssignInitialNull", groupAttributeAssign == null);
    }
    try {
      if (groupAttributeAssign == null) {
        groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("groupAttributeAssignRetrieveNull", groupAttributeAssign == null);
      }
  
      AttributeAssign attributeAssign = groupAttributeAssign;
      
      // not sure why not attestation but thats ok
      if (groupAttributeAssign == null) {
        return;
      }
      
      String hasAttestationAttributeValue = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());
  
      // if this is blank then we need to upgrade
      if (StringUtils.isBlank(hasAttestationAttributeValue)) {
  
        groupAttributeAssign.getAttributeValueDelegate().assignValueString(
            GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
  
        //this is not direct
        if (!StringUtils.equals("true", groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()))) {
          
          groupAttributeAssign.getAttributeValueDelegate().assignValueString(
              GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "false");
          
          // TODO I dont think this is correct but I can't call findParentFolderAssign since the parent may still need to be upgraded too???
          Stem configStem = (Stem)group.getParentStem().getAttributeDelegate()
              .getAttributeOrAncestorAttribute(retrieveAttributeDefNameValueDef().getName(), false);
  
          //there is no direct assignment and no stem with attestation
          if (configStem == null) {
            LOG.error("Why is there no direct assignment or parent stem with attestation on group: " + group.getName() + ", " + group.getUuid());
            return;
          }
  
          updateObjectAttributesToPatch81(configStem, null);
          
          attributeAssign = configStem.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
  
        }
        
        if (updateLastCertified) {
          //calculate
          GrouperAttestationJob.updateCalculatedDaysUntilRecertify(group, attributeAssign);
        }      
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }
  
  /**
   * @param stemAttributeAssign
   * @param stem
   */
  public static void updateObjectAttributesToPatch81(Stem stem, AttributeAssign stemAttributeAssign) {
    
    if (stemAttributeAssign == null) {
      stemAttributeAssign = stem.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    }

    // not sure why not attestation but thats ok
    if (stemAttributeAssign == null) {
      return;
    }
    
    String hasAttestationAttributeValue = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());

    // if this is blank then we need to upgrade
    if (StringUtils.isBlank(hasAttestationAttributeValue)) {
      
      //needs has attestation attribute
      stemAttributeAssign.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      
    }
  }

//  /**
//   * 
//   * @param debugMap
//   */
//  public static void debugQueries(Map<String, Object> debugMap) {
//
//    String hasAttestationCount = HibernateSession.bySqlStatic().select(String.class, "SELECT COUNT(1) FROM grouper_aval_asn_asn_group_v WHERE group_name = 'testAttestation:attestable' AND attribute_def_name_name2 = 'etc:attribute:attestation:attestationCalculatedDaysLeft' AND value_String = 'etc:attribute:attestation:attestationHasAttestation'");
//    
//    if (!StringUtils.equals(hasAttestationCount, "0")) {
//
//      debugMap.put("dbHasAttestation", HibernateSession.bySqlStatic().select(String.class, "SELECT value_string FROM grouper_aval_asn_asn_group_v WHERE group_name = 'testAttestation:attestable' AND attribute_def_name_name2 = 'etc:attribute:attestation:attestationHasAttestation'"));
//      
//    } else {
//      debugMap.put("dbHasAttestation", "false");
//      
//    }
//    debugMap.put("dbCalculatedDayUntilAttestation", HibernateSession.bySqlStatic().select(String.class, "SELECT COUNT(1) FROM grouper_aval_asn_asn_group_v WHERE group_name = 'testAttestation:attestable' AND attribute_def_name_name2 = 'etc:attribute:attestation:attestationCalculatedDaysLeft' AND value_String IS NOT NULL"));
//
//  }
  
  /**
   * update the calculated days until recertify (i.e. if the stem has a report based attestation)
   * @param stem stem to calculate
   */
  public static void updateCalculatedDaysUntilRecertify(Stem stem) {

    AttributeAssign stemAttributeAssign = stem.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    String attestationDateCertified = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDateCertified().getName());    
    String hasAttestationAttributeValue = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());

    if (StringUtils.isBlank(hasAttestationAttributeValue) || !GrouperUtil.booleanValue(hasAttestationAttributeValue)) {
      stemAttributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft());
    } else {
      String configuredAttestationDaysUntilRecertify = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysUntilRecertify().getName());
      updateCalculatedDaysLeft(stemAttributeAssign, attestationDateCertified, configuredAttestationDaysUntilRecertify, false);
    }
  }
  
  /**
   * update the calculated days until recertify
   * @param group group to calculate
   * @param attributeAssign that has the settings, stem or group
   */
  public static void updateCalculatedDaysUntilRecertify(Group group, AttributeAssign attributeAssign) {

    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "updateCalculatedDaysUntilRecertify");
      debugMap.put("entered", GrouperUtil.timestampToString(new Date()));
      debugMap.put("attributeAssignInitialNull", attributeAssign == null);
      debugMap.put("group", group.getName());
    }
    try {
      AttributeAssign groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);

      if (LOG.isDebugEnabled()) {
        debugMap.put("groupAttributeAssignInitialNull", groupAttributeAssign == null);
      }

      String attestationDateCertified = null;
      
      if (groupAttributeAssign != null) {
        
        //see if converting from previous patch
        updateObjectAttributesToPatch81(group, groupAttributeAssign, false);
        
        attestationDateCertified = groupAttributeAssign.getAttributeValueDelegate()
            .retrieveValueString(retrieveAttributeDefNameDateCertified().getName());
        if (LOG.isDebugEnabled()) {
          debugMap.put("attestationDateCertified", attestationDateCertified);
        }
      }
      
      String hasAttestationAttributeValue = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());

      if (LOG.isDebugEnabled()) {
        debugMap.put("hasAttestationAttributeValue", hasAttestationAttributeValue);
      }

      // if this is blank then we need to upgrade
      if (StringUtils.isBlank(hasAttestationAttributeValue) || !GrouperUtil.booleanValue(hasAttestationAttributeValue)) {
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("removeAttribute", true);
        }
        groupAttributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft());

      } else {
      
        String configuredAttestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysUntilRecertify().getName());
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("configuredAttestationDaysUntilRecertify", configuredAttestationDaysUntilRecertify);
        }

        int configuredDaysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
        if (! StringUtils.isBlank(configuredAttestationDaysUntilRecertify)) {
          configuredDaysUntilRecertify = Integer.valueOf(configuredAttestationDaysUntilRecertify);
        }
        
        if (LOG.isDebugEnabled()) {
          debugMap.put("configuredDaysUntilRecertify", configuredDaysUntilRecertify);
        }

        int daysUntilNeedsCertify = updateCalculatedDaysLeft(groupAttributeAssign, attestationDateCertified, configuredAttestationDaysUntilRecertify, false);

        if (LOG.isDebugEnabled()) {
          debugMap.put("assigningCalculatedDaysLeft",daysUntilNeedsCertify);
        }
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }
  
  /**
   * get map of email addresses to email objects for group attributes
   * @param stemAttributeAssign
   * @param groupAttributeAssigns
   * @return the map of email objects
   */
  protected static Map<String, Set<EmailObject>> buildAttestationGroupEmails(AttributeAssign stemAttributeAssign, Set<AttributeAssign> groupAttributeAssigns) {
    
    // map of email address to email object (group id, group name, ccList)
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign groupAttributeAssign: groupAttributeAssigns) {

      Group group = groupAttributeAssign.getOwnerGroup();
      
      AttributeAssign configurationAttributeAssign = GrouperUtil.defaultIfNull(stemAttributeAssign, groupAttributeAssign);

      {
        String hasAttestationString = configurationAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());
        
        // it needs this if it doesnt have it
        if (!StringUtils.isBlank(hasAttestationString)) {
          
          //make sure group is in sync
          groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameHasAttestation().getName(), hasAttestationString);
          
          if (!GrouperUtil.booleanValue(hasAttestationString, true)) {
            continue;
          }
        }
      }
      
      updateCalculatedDaysUntilRecertify(group, configurationAttributeAssign);

      String daysUntilRecertifyString = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameCalculatedDaysLeft().getName());
      
      //should never be blank
      if (StringUtils.isBlank(daysUntilRecertifyString)) {
        continue;
      }
      
      int daysUntilRecertify = GrouperUtil.intValue(daysUntilRecertifyString);
      
      String attestationSendEmail = configurationAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
      String attestationDaysBeforeToRemind = configurationAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysBeforeToRemind().getName());
      
      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+group.getDisplayName()+" attestationSendEmail attribute is set to true so skipping sending email.");
        continue;
      }

      
      String attestationLastEmailedDate = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameEmailedDate().getName());
      if (!StringUtils.isBlank(attestationLastEmailedDate)) {
        String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        if (StringUtils.equals(attestationLastEmailedDate, today)) {
          LOG.debug("For "+groupAttributeAssign.getOwnerGroup().getDisplayName()+" attestationLastEmailedDate attribute is set to today so skipping sending email.");
          continue;
        }
      }

      
      int daysBeforeReminderEmail = 0;
      if (! StringUtils.isBlank(attestationDaysBeforeToRemind)) {
        daysBeforeReminderEmail = Integer.valueOf(attestationDaysBeforeToRemind);
      }
      
      boolean sendEmail = daysUntilRecertify <= daysBeforeReminderEmail;
            
      if (sendEmail) {
        // grab the list of email addresses from the attribute
        String[] emailAddresses = getEmailAddresses(configurationAttributeAssign, groupAttributeAssign.getOwnerGroup());
        if (emailAddresses != null && emailAddresses.length > 0) {          
          addEmailObject(configurationAttributeAssign, emailAddresses, emails, groupAttributeAssign.getOwnerGroup());
        }
      }
      
    }
    
    return emails;
    
  }
  
  /**
   * get map of email addresses to email objects for stem attributes for reports
   * @param stemAttributeAssign
   * @return the map of email objects
   */
  private static Map<String, Set<EmailObject>> buildAttestationStemReportEmails(AttributeAssign stemAttributeAssign) {
    
    // map of email address to email object (stem id, stem name, ccList)
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();

    updateCalculatedDaysUntilRecertify(stemAttributeAssign.getOwnerStem());

    String daysUntilRecertifyString = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameCalculatedDaysLeft().getName());
    
    //should never be blank
    if (StringUtils.isBlank(daysUntilRecertifyString)) {
      return emails;
    }
    
    int daysUntilRecertify = GrouperUtil.intValue(daysUntilRecertifyString);
    
    String attestationSendEmail = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
    String attestationDaysBeforeToRemind = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysBeforeToRemind().getName());
    
    boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
    // skip sending email for this attribute assign
    if (!sendEmailAttributeValue) {
      LOG.debug("For "+stemAttributeAssign.getOwnerStem().getDisplayName()+" attestationSendEmail attribute is set to true so skipping sending email.");
      return emails;
    }

    
    String attestationLastEmailedDate = stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameEmailedDate().getName());
    if (!StringUtils.isBlank(attestationLastEmailedDate)) {
      String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

      if (StringUtils.equals(attestationLastEmailedDate, today)) {
        LOG.debug("For "+stemAttributeAssign.getOwnerStem().getDisplayName()+" attestationLastEmailedDate attribute is set to today so skipping sending email.");
        return emails;
      }
    }

    
    int daysBeforeReminderEmail = 0;
    if (! StringUtils.isBlank(attestationDaysBeforeToRemind)) {
      daysBeforeReminderEmail = Integer.valueOf(attestationDaysBeforeToRemind);
    }
    
    boolean sendEmail = daysUntilRecertify <= daysBeforeReminderEmail;
          
    if (sendEmail) {
      // grab the list of email addresses from the attribute
      String[] emailAddresses = getEmailAddressesForStemReport(stemAttributeAssign);
      if (emailAddresses != null && emailAddresses.length > 0) {          
        addEmailObject(stemAttributeAssign, emailAddresses, emails, stemAttributeAssign.getOwnerStem());
      }
    }
  
    return emails;
    
  }
  
  /**
   * build array of email addresses from either the attribute itself or from the group admins/readers/updaters.
   * @param attributeAssign
   * @param group
   * @return the email addresses
   */
  private static String[] getEmailAddressesForStemReport(AttributeAssign attributeAssign) {
    
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameEmailAddresses().getName());
    String[] emailAddresses = null;
    if (StringUtils.isBlank(attestationEmailAddresses)) {
      
      String attestationAuthorizedGroupId = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameAuthorizedGroupId().getName());
      if (!StringUtils.isEmpty(attestationAuthorizedGroupId)) {
        Group authorizedGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), attestationAuthorizedGroupId, true);

        // get members
        Set<Member> groupMembers = GrouperUtil.nonNull(authorizedGroup.getMembers());
        
        Set<String> addresses = new HashSet<String>();
        
        // go through each subject and find the email address.
        for (Member member: groupMembers) {
          if (StringUtils.equals(member.getSubject().getType().getName(), SubjectTypeEnum.PERSON.getName())) { 
            String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(member.getSubject().getSourceId());
            if (!StringUtils.isBlank(emailAttributeName)) {
              String emailAddress = member.getSubject().getAttributeValue(emailAttributeName);
              if (!StringUtils.isBlank(emailAddress)) {
                addresses.add(emailAddress);
              }
            }
          }
        }
        
        emailAddresses = addresses.toArray(new String[addresses.size()]); 
      }
    } else {
      emailAddresses = GrouperUtil.splitTrim(attestationEmailAddresses, ",");
    }
    
    return emailAddresses;
    
  }
  
  /**
   * build array of email addresses from either the attribute itself or from the group admins/readers/updaters.
   * @param attributeAssign
   * @param group
   * @return the email addresses
   */
  private static String[] getEmailAddresses(AttributeAssign attributeAssign, Group group) {
    
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameEmailAddresses().getName());
    String[] emailAddresses = null;
    if (StringUtils.isBlank(attestationEmailAddresses)) {
      
      // get the group's admins and updaters-and-readers (must be both)
      Set<Subject> groupMembers = GrouperUtil.nonNull(group.getAdmins());

      //if someone is a reader and an updater then add their email address
      for (Subject subject : GrouperUtil.nonNull(group.getUpdaters())) {
        if (SubjectHelper.inList(GrouperUtil.nonNull(group.getReaders()), subject)) {
          groupMembers.add(subject);
        }
      }
      
      Set<String> addresses = new HashSet<String>();
      
      // go through each subject and find the email address.
      for (Subject subject: groupMembers) {
        if (StringUtils.equals(subject.getType().getName(), SubjectTypeEnum.PERSON.getName())) {
          String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
          if (!StringUtils.isBlank(emailAttributeName)) {
            String emailAddress = subject.getAttributeValue(emailAttributeName);
            if (!StringUtils.isBlank(emailAddress)) {
              addresses.add(emailAddress);
            }
          }
        }
      }
      
      emailAddresses = addresses.toArray(new String[addresses.size()]);
      
    } else {
      emailAddresses = GrouperUtil.splitTrim(attestationEmailAddresses, ",");
    }
    
    return emailAddresses;
    
  }
  
  /**
   * Add new key (email address) to map or update the value (set of email objects) 
   * @param attributeAssign
   * @param emailAddresses
   * @param emails
   * @param group
   */
  private static void addEmailObject(AttributeAssign attributeAssign, String[] emailAddresses, Map<String, Set<EmailObject>> emails, GrouperObject grouperObject) {
    
    for (int i=0; i<emailAddresses.length; i++) {
      
      String primaryEmailAddress = emailAddresses[i].trim();
      
      Set<String> ccEmailAddresses =  getElements(emailAddresses, i);
      
      EmailObject emailObject = null;
      if (grouperObject instanceof Group) {
        emailObject = new EmailObject(grouperObject.getId(), grouperObject.getDisplayName(), ccEmailAddresses);
      } else if (grouperObject instanceof Stem) {
        emailObject = new EmailObject(null, null, grouperObject.getId(), grouperObject.getDisplayName(), ccEmailAddresses);
      } else {
        throw new RuntimeException("Unexpected type " + grouperObject);
      }
      
      
      if (emails.containsKey(primaryEmailAddress)) {
        Set<EmailObject> emailObjects = emails.get(primaryEmailAddress);
        emailObjects.add(emailObject);
      } else {
        Set<EmailObject> emailObjects = new HashSet<EmailObject>();
        emailObjects.add(emailObject);
        emails.put(primaryEmailAddress, emailObjects);
      }
    }
      
  }
  
  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_attestationDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperAttestationJob().run(otherJobInput);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    AttributeDef attributeDef = retrieveAttributeDef();
    if (attributeDef == null) {
      LOG.error(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DEF + " attribute def doesn't exist. Job will not proceed.");
      return null;
    }
    
    Set<AttributeAssign> groupAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.group,
        attributeDef.getId(), null, null,
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    //take out inherited
    Iterator<AttributeAssign> iterator = groupAttributeAssigns.iterator();
    
    while (iterator.hasNext()) {
      AttributeAssign attributeAssign = iterator.next();
      
      // make sure type is assigned - it was added in a patch so may not be there
      String typeString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameType().getName());
      if (StringUtils.isEmpty(typeString)) {
        typeString = "group";
        attributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameType().getName(), typeString);
      }
      
      String directAssignmentString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDirectAssignment().getName());

      // group has inherited attestation, don't process as group, this will be processed as stem descendent
      if (!GrouperUtil.booleanValue(directAssignmentString, false) ) { 
        
        iterator.remove();
        
        String groupId = attributeAssign.getOwnerGroupId();
        Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);
        
        if (group != null) {
          removeDirectGroupAttestation(group);
        }
        continue;
      }
      
      if (typeString.equals("report")) {
        iterator.remove();
        continue;
      }
    }
    
    Map<String, Set<EmailObject>> emails = buildAttestationGroupEmails(null, groupAttributeAssigns);
    
    LOG.info("got "+emails.size()+" from group attributes, starting building map from stem attributes.");

    otherJobInput.getHib3GrouperLoaderLog().store();
    
    Map<String, Set<EmailObject>> stemEmails = buildAttestationStemEmails();
    
    LOG.info("got "+stemEmails.size()+" from stem attributes for groups, start merging group and stem attributes.");
    otherJobInput.getHib3GrouperLoaderLog().store();

    mergeEmailObjects(emails, stemEmails);
    
    Map<String, Set<EmailObject>> stemReportEmails = buildAttestationStemReportEmails();
    
    LOG.info("got "+stemReportEmails.size()+" from stem attributes for reports, start merging group and stem attributes.");
    otherJobInput.getHib3GrouperLoaderLog().store();

    mergeEmailObjects(emails, stemReportEmails);
    
    otherJobInput.getHib3GrouperLoaderLog().setInsertCount(emails.size());
    otherJobInput.getHib3GrouperLoaderLog().store();
    
    LOG.info("start sending emails to "+emails.size()+" email addresses.");

    sendEmail(emails);

    otherJobInput.getHib3GrouperLoaderLog().store();

    LOG.info("Set attestationLastEmailedDate attribute to each of the groups/stems.");
    setLastEmailedDate(emails, otherJobInput.getGrouperSession());

    //count line items
    int lineItems = 0;
    for (String email : GrouperUtil.nonNull(emails).keySet()) {
      lineItems += GrouperUtil.length(emails.get(email));
    }
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Sent " + emails.size() + " emails with " 
        + lineItems + " line items about " + GrouperUtil.length(groupAttributeAssigns) + " attestation assignments");

    LOG.info("GrouperAttestationJob finished successfully.");
    return null;
    
  }

  /**
   * build email body/subject and send email.
   * @param emailObjects
   */
  private void sendEmail(Map<String, Set<EmailObject>> emailObjects) {
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    if (StringUtils.isBlank(uiUrl)) {
      LOG.error("grouper.properties grouper.ui.url is blank/null. Please fix that first. GrouperAttestationJob will not proceed. No emails have been sent.");
      return;
    }
    
    String subject = GrouperConfig.retrieveConfig().propertyValueString("attestation.reminder.email.subject");
    String body = GrouperConfig.retrieveConfig().propertyValueString("attestation.reminder.email.body");
    if (StringUtils.isBlank(subject)) {
      subject = "You have $objectCount$ groups and/or folders that require attestation";
    }
    if (StringUtils.isBlank(body)) {
      body = "You need to attest the following groups and/or folders.  Review each group/folder and click the button to mark it as reviewed.";
    }
    
    for (Map.Entry<String, Set<EmailObject>> entry: emailObjects.entrySet()) {

      String sub = StringUtils.replace(subject, "$objectCount$", String.valueOf(entry.getValue().size()));
      
      // build body of the email
      StringBuilder emailBody = new StringBuilder(body);
      emailBody.append("\n");
      int start = 1; // show only attestation.email.group.count groups/folders in one email
      int end = GrouperConfig.retrieveConfig().propertyValueInt("attestation.email.group.count", 100);
      lbl: for (EmailObject emailObject: entry.getValue()) {
       emailBody.append("\n");
       if (emailObject.getGroupName() != null) {
         emailBody.append(start+". "+emailObject.getGroupName()+"  ");
       } else {
         emailBody.append(start+". "+emailObject.getStemName()+"  ");
       }
       // set the cc if any
       if (emailObject.getCcEmails() != null && emailObject.getCcEmails().size() > 0) {
         emailBody.append("(cc'd ");
         emailBody.append(StringUtils.join(emailObject.getCcEmails(), ","));
         emailBody.append(")");
       }
       emailBody.append("\n");
       emailBody.append(uiUrl);
       if (emailObject.getGroupId() != null) {
         emailBody.append("grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId="+emailObject.getGroupId());
       } else {
         emailBody.append("grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemId="+emailObject.getStemId());
       }
       start = start + 1;
       if (start > end) {
         String more = GrouperConfig.retrieveConfig().propertyValueString("attestation.reminder.email.body.greaterThan100");
         if (StringUtils.isBlank(more)) {
           more = "There are $remaining$ more groups and/or folders to be attested.";
         }
         more = StringUtils.replace(more, "$remaining$", String.valueOf(entry.getValue().size() - end));
         emailBody.append("\n");
         emailBody.append(more);
         break lbl;
       }
      }
      try {
        new GrouperEmail().setBody(emailBody.toString()).setSubject(sub).setTo(entry.getKey()).send();
      } catch (Exception e) {
        LOG.error("Error sending email", e);
      }
    }
  }
  
  /**
   * set last emailed date attribute to each of the groups.
   * @param emailObjects
   * @param session
   */
  private void setLastEmailedDate(Map<String, Set<EmailObject>> emailObjects, GrouperSession session) {
    
    for (Map.Entry<String, Set<EmailObject>> entry: emailObjects.entrySet()) { 
      
      for (EmailObject emailObject: entry.getValue()) { 
        AttributeAssignable assignable = emailObject.getGroupId() != null ? 
            GroupFinder.findByUuid(session, emailObject.getGroupId(), false) :
              StemFinder.findByUuid(session, emailObject.getStemId(), false);
        if (assignable != null) {
          AttributeAssign attributeAssign = assignable.getAttributeDelegate().retrieveAssignment(
              null, retrieveAttributeDefNameValueDef(), false, false);
          
          String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
          attributeAssign.getAttributeValueDelegate().assignValue(retrieveAttributeDefNameEmailedDate().getName(), date);
         
          attributeAssign.saveOrUpdate(false);
        }
      }
      
    }
    
  }
  
  
  /**
   *  Merge map2 into map1
   * @param map1
   * @param map2
   */
  private static void mergeEmailObjects(Map<String, Set<EmailObject>> map1, Map<String, Set<EmailObject>> map2) {
    
    for (Map.Entry<String, Set<EmailObject>> entry: map1.entrySet()) {
      
      if (map2.containsKey(entry.getKey())) {
        entry.getValue().addAll(map2.get(entry.getKey()));
      }      
    }
    
    for (Map.Entry<String, Set<EmailObject>> entry: map2.entrySet()) {
      
      if (!map1.containsKey(entry.getKey())) {
        map1.put(entry.getKey(), entry.getValue());
      }      
    }
  }
  
  /**
   * get map of email addresses to email objects for stem attributes 
   * @param attributeDef
   * @return
   */
  private Map<String, Set<EmailObject>> buildAttestationStemEmails() {
  
    //TODO just get directly assigned ones?  or are indirect folders assigned copies of the settings?
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        null, retrieveAttributeDefNameValueDef().getId(), null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      // make sure type is assigned - it was added in a patch so may not be there
      String typeString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameType().getName());
      if (StringUtils.isEmpty(typeString)) {
        typeString = "group";
        attributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameType().getName(), typeString);
      }
      
      if (typeString.equals("report")) {
        continue;
      }
      
      String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
      
      Map<String, Set<EmailObject>> localEmailMap = stemAttestationProcessHelper(attributeAssign.getOwnerStem(), attributeAssign);

      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" attestationSendEmail attribute is not set to true so skipping sending email.");
        continue;
      }

      if (sendEmailAttributeValue) {
        mergeEmailObjects(emails, localEmailMap);
      }
    }
    
    return emails;
    
  }
  
  /**
   * get map of email addresses to email objects for stem attributes using reports
   * @param attributeDef
   * @return
   */
  private Map<String, Set<EmailObject>> buildAttestationStemReportEmails() {
  
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        null, retrieveAttributeDefNameValueDef().getId(), null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      // make sure type is assigned - it was added in a patch so may not be there
      String typeString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameType().getName());
      
      if (!"report".equals(typeString)) {
        continue;
      }
      
      String stemHasAttestationString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());
      if (!GrouperUtil.booleanValue(stemHasAttestationString, true)) {
        continue;
      }
      
      String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
      
      Map<String, Set<EmailObject>> localEmailMap = buildAttestationStemReportEmails(attributeAssign);

      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" attestationSendEmail attribute is not set to true so skipping sending email.");
        continue;
      }

      mergeEmailObjects(emails, localEmailMap);
    }
    
    return emails;
    
  }

  /**
   * take a stem attribute assign and process it
   * @param stem is the stem the attribute is on
   * @param stemAttributeAssign
   * @return the email objects
   */
  public static Map<String, Set<EmailObject>> stemAttestationProcessHelper(Stem stem, AttributeAssign stemAttributeAssign) {
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    String stemHasAttestationString = "false";

    if (stemAttributeAssign != null) {

      GrouperAttestationJob.updateObjectAttributesToPatch81((Stem)stem, stemAttributeAssign);

      stemHasAttestationString = stemAttributeAssign.getAttributeValueDelegate()
          .retrieveValueString(retrieveAttributeDefNameHasAttestation().getName());
        
      // it needs this if it doesnt have it (from earlier upgrade)
      if (StringUtils.isBlank(stemHasAttestationString)) {
        
        stemAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameHasAttestation().getName(), "true");
        stemHasAttestationString = "true";
      }
    }
        
    Set<Group> childGroups = stem.getChildGroups(Scope.SUB);
    
    for (Group group: childGroups) {
      
      AttributeAssign groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, retrieveAttributeDefNameValueDef(), false, false);
      String directAssignmentString = null;
      
      if (groupAttributeAssign != null) {
        directAssignmentString = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDirectAssignment().getName());

        // group has direct attestation, don't use stem attributes at all.  This will be in group assignment calculations
        if (GrouperUtil.booleanValue(directAssignmentString, false)) { 
          continue;
        }
      }
      
      AttributeAssign parentFolderAssign = findParentFolderAssign(group.getParentStem());
      
      //make sure its the right stem that has the assignment
      if (parentFolderAssign != null && !StringUtils.equals(parentFolderAssign.getOwnerStemId(), stem.getUuid())) {
        continue;
      }
              
      //there is no direct assignment and no stem with attestation
      if (parentFolderAssign == null) {
        
        if (groupAttributeAssign != null) {
          groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameHasAttestation().getName(), "false");
          groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameDirectAssignment().getName(), "false");
          groupAttributeAssign.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameCalculatedDaysLeft());
        }
        
        continue;
      }
      
      if (groupAttributeAssign == null) {
        groupAttributeAssign = group.getAttributeDelegate().assignAttribute(retrieveAttributeDefNameValueDef()).getAttributeAssign();
      }
      
      
      if (StringUtils.isBlank(directAssignmentString)) {
        groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameDirectAssignment().getName(), "false");
        directAssignmentString = "false";
      }

      //make sure group is in sync with stem
      groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameHasAttestation().getName(), stemHasAttestationString);
      
      if (!GrouperUtil.booleanValue(stemHasAttestationString, true)) {
        continue;
      }

      Set<AttributeAssign> singleGroupAttributeAssign = new HashSet<AttributeAssign>();
      singleGroupAttributeAssign.add(groupAttributeAssign);
      
      // skip sending email for this attribute assign
      Map<String, Set<EmailObject>> buildAttestationGroupEmails = buildAttestationGroupEmails(stemAttributeAssign, singleGroupAttributeAssign);
     
      mergeEmailObjects(emails, buildAttestationGroupEmails);

    }
    
    return emails;
  }
  
  /**
   * get unique elements from array except specified by index except.
   * @param array
   * @param except
   * @return the set
   */
  private static Set<String> getElements(String[] array, int except) {
    Set<String> result = new HashSet<String>();
    for (int j=0; j<array.length; j++) {
      if (except != j) {
        result.add(array[j].trim());
      }
    }
    return result;
  }
  
  /**
   * remove direct group assignment in favor of stem assignment
   * @param group
   */
  public static void removeDirectGroupAttestation(Group group) {
    //if doesnt have a parent thats configured, remove this one
    //start at stem and look for assignment
    // AttributeAssignable attributeAssignable = group.getParentStem().getAttributeDelegate()
    //  .getAttributeOrAncestorAttribute(retrieveAttributeDefNameValueDef().getName(), false);
    
    AttributeAssign attributeAssign = GrouperAttestationJob.findParentFolderAssign(group.getParentStem());
  
    //if no parent has it, then remove most attributes
    if (attributeAssign == null) {
      removeDirectGroupAttestation(group, false);
    } else {
      removeDirectGroupAttestation(group, true);
    }
  }

  /**
   * remove direct group assignment in favor of stem assignment
   * @param group
   * @param changeToIndirect means keep the indirect attributes
   */
  public static void removeDirectGroupAttestation(Group group, boolean changeToIndirect) {
    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "removeDirectGroupAttestation");
      debugMap.put("entered", GrouperUtil.timestampToString(new Date()));
      debugMap.put("group", group.getName());
      debugMap.put("changeToIndirect", changeToIndirect);
    }
    try {
      //if there is no last certified or emailed, just remove
      AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
      if (LOG.isDebugEnabled()) {
        debugMap.put("attributeAssignNull", attributeAssign == null);
      }
      if (!changeToIndirect) {
        String emailedDate = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailedDate().getName());
        String dateCertified = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName());
        if (LOG.isDebugEnabled()) {
          debugMap.put("emailedDate", emailedDate);
          debugMap.put("dateCertified", dateCertified);
        }
        if (StringUtils.isBlank(emailedDate)
            && StringUtils.isBlank(dateCertified)
            ) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("removeAttribute", true);
          }
          group.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef());
          return;
        }
      }
      
      if (changeToIndirect) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("directAssignment", false);
        }
        attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
        attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "false");
      } else {
        //has no more attestation
        attributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment());
        attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "false");
      }
      attributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameSendEmail());
      attributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses());
      attributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify());
      attributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind());

      if (LOG.isDebugEnabled()) {
        debugMap.put("calculatedDays", "null");
      }

      //this has to be recalculated
      attributeAssign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft());
      
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }

}
