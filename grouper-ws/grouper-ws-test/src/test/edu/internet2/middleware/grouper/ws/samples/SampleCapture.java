/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: SampleCapture.java,v 1.10 2009/12/29 07:38:16 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefNameTest;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.webservicesClient.RampartSampleGetGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAcknowledgeMessage;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributeDefActions;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributeDefNameInheritance;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributeDefNameInheritanceLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributes;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributesBatch;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributesLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributesWithValue;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignAttributesWithValueLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignGrouperPrivileges;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignPermissions;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignPermissionsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefDeleteLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefNameDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefNameDeleteLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefNameSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefNameSaveLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAttributeDefSaveLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleDeleteMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleDeleteMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleExternalSubjectDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleExternalSubjectSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindAttributeAssignAction;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindAttributeAssignActionLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindAttributeDefNames;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindAttributeDefNamesLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindAttributeDefs;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindAttributeDefsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindExternalSubjects;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindGroups;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindStems;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindStemsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetAttributeAssignActions;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetAttributeAssignActionsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetAttributeAssignments;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetAttributeAssignmentsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroups;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsAdmins;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsAdminsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembers;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembersLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMemberships;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembershipsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetPermissionAssignments;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetPermissionAssignmentsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetSubjects;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetSubjectsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupDeleteLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupDetailSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupSaveLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleHasMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleHasMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleMemberChangeSubject;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleMemberChangeSubjectLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleReceiveMessage;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleSendMessage;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemDeleteLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemSaveLite;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributeDefActionsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributeDefNameInheritanceRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributeDefNameInheritanceRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributesBatchRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributesRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributesRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributesWithValueRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributesWithValueRest2;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAssignAttributesWithValueRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefDeleteRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefDeleteRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefNameDeleteRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefNameDeleteRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefNameSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefNameSaveRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleAttributeDefSaveRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleFindAttributeAssignActionRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleFindAttributeAssignActionRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleFindAttributeDefNamesRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleFindAttributeDefNamesRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleFindAttributeDefsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleFindAttributeDefsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleGetAttributeAssignActionsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleGetAttributeAssignActionsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleGetAttributeAssignmentsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.attribute.WsSampleGetAttributeAssignmentsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleExternalSubjectDeleteRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleExternalSubjectSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleFindExternalSubjectsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleFindGroupsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleFindGroupsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGetGroupsAdminsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGetGroupsAdminsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGetGroupsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGetGroupsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGetGroupsRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGroupDeleteRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGroupDeleteRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGroupDeleteRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGroupDetailSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGroupSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.group.WsSampleGroupSaveRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleAssignGrouperPrivilegesRest;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleAssignGrouperPrivilegesRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleGetGrouperPrivilegesListRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleGetGrouperPrivilegesRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleAddMemberRest;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleAddMemberRest2;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleAddMemberRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleAddMemberRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleDeleteMemberRest;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleDeleteMemberRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleDeleteMemberRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleGetMembersRest;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleGetMembersRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleGetMembersRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleHasMemberRest;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleHasMemberRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleHasMemberRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleMemberChangeSubjectRest;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleMemberChangeSubjectRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.membership.WsSampleGetMembershipsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.membership.WsSampleGetMembershipsRest2;
import edu.internet2.middleware.grouper.ws.samples.rest.membership.WsSampleGetMembershipsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.membership.WsSampleGetMembershipsRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.messaging.WsSampleAcknowledgeMessageRest;
import edu.internet2.middleware.grouper.ws.samples.rest.messaging.WsSampleReceiveMessageRest;
import edu.internet2.middleware.grouper.ws.samples.rest.messaging.WsSampleSendMessageRest;
import edu.internet2.middleware.grouper.ws.samples.rest.permission.WsSampleAssignPermissionsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.permission.WsSampleAssignPermissionsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.permission.WsSampleGetPermissionAssignmentsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.permission.WsSampleGetPermissionAssignmentsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleFindStemsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleFindStemsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemDeleteRest;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemDeleteRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemDeleteRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemSaveRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.subject.WsSampleGetSubjectsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.subject.WsSampleGetSubjectsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.subject.WsSampleGetSubjectsRestLite2;
import edu.internet2.middleware.grouper.ws.samples.types.WsSample;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleClientType;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouper.ws.util.TcpCaptureServer;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 * <pre>
 * capture a sample and put in text file.  To run this, I have my properties files set correctly,
 * and run a server (or tcp forwarder) on 8091.  make sure 8092 is available for the capture server port.
 * set the web service properties to go to 8092
 * 
 * edit grouper-ws-generated.properties, make sure all settings correct and port should be 8092
 * 
 * edit grouper-ws.properties
 * 
 * # port for hitting tests
 * ws.testing.port=8092
 *
 * # port that the sample capture proxy will forward to
 * ws.sampleForwardTo.port=8091
 * 
 * make sure that nothing is listening on 8092.  Make sure the app server or tcp/ip port forwarder
 * is listening on 8091
 * </pre>
 */
public class SampleCapture {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(SampleCapture.class);

  /**
   * @param args
   */
  public static void main(String[] args) {

//    setupData();

//    captureAssignAttributesWithValue();

//    captureGetGrouperPrivileges();

//    captureGroupSave();
    
//  captureRampart();
//    captureSample(WsSampleClientType.REST_BEANS,  
//        WsSampleMemberChangeSubjectRest.class, "memberChangeSubject", null);

////    captureAcknowledgeMessage();  
////    captureAddMember();
////    captureAssignAttributeDefActions();
////    captureAssignAttributeDefNameInheritance();
////    captureAssignAttributes();
////    captureAssignAttributesBatch();
////    captureAssignAttributesWithValue();
////    captureAssignGrouperPrivileges();
////    captureAssignPermissions();
////    captureAttributeDefDelete();
////    captureAttributeDefNameDelete();
////    captureAttributeDefNameSave();
////    captureAttributeDefSave();
////    captureDeleteMember();
////    captureExternalSubjectDelete();
////    captureExternalSubjectSave();
////    captureFindAttributeAssignAction();
////    captureFindAttributeDefNames();
////    captureFindAttributeDefs();
////    captureFindExternalSubjects();
////    captureFindGroups();
////    captureFindStems();
////    captureGetAttributeAssignments();
////    captureGetAttributeAssignActions();
////    captureGetGrouperPrivileges();
////    captureGetGroups();
////    captureGetMembers();
////    captureGetMemberships();
////    captureGetPermissionAssignments();
////    captureGetSubjects();
////    captureGroupDelete();
////    captureGroupSave();
////    captureHasMember();
    captureMemberChangeSubject();
////    captureReceiveMessage();
////    captureRampart();
////    captureSendMessage();
////    captureStemDelete();
////    captureStemSave();
    

  }

  /**
   * receive message
   */
  public static void captureSendMessage() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleSendMessage.class, "sendMessage", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleSendMessageRest.class, "sendMessage", null);
    
  }

  /**
   * receive message
   */
  public static void captureReceiveMessage() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleReceiveMessage.class, "receiveMessage", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleReceiveMessageRest.class, "receiveMessage", null);

  }

  /**
   * acknowledge message
   */
  public static void captureAcknowledgeMessage() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAcknowledgeMessage.class, "acknowledgeMessage", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAcknowledgeMessageRest.class, "acknowledgeMessage", null);
    
  }


  /**
   * find attribute defs
   */
  public static void captureFindAttributeDefs() {
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindAttributeDefsRest.class, "findAttributeDefs", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindAttributeDefsRestLite.class, "findAttributeDefs", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindAttributeDefs.class, "findAttributeDefs", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindAttributeDefsLite.class, "findAttributeDefs", null);
    
  }


  /**
   * find action captures
   */
  public static void captureFindAttributeAssignAction() {
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindAttributeAssignActionRest.class, "findAttributeAssignAction", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindAttributeAssignActionRestLite.class, "findAttributeAssignAction", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindAttributeAssignAction.class, "findAttributeAssignAction", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindAttributeAssignActionLite.class, "findAttributeAssignAction", null);
    
  }


  /**
   * assign attribute actions captures
   */
  public static void captureAssignAttributeDefActions() {
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributeDefActionsRest.class, "assignAttributeDefActions", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributeDefActions.class, "assignAttributeDefActions", (String)null);

  }

  /**
   * attribute def save
   */
  public static void captureAttributeDefSave() {
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefSaveRest.class, "attributeDefSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefSaveRestLite.class, "attributeDefSave", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefSave.class, "attributeDefSave", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefSaveLite.class, "attributeDefSave", (String)null);

  }

  /**
   * attribute def delete
   */
  public static void captureAttributeDefDelete() {
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefDeleteRest.class, "attributeDefDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefDeleteRestLite.class, "attributeDefDelete", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefDelete.class, "attributeDefDelete", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefDeleteLite.class, "attributeDefDelete", (String)null);
    
  }


  /** certain data has to exist for samples to run */
  private static void setupData() {
    GrouperSession grouperSession = null;
    HibUtils.assignDisallowCacheThreadLocal();
    try {

      grouperSession = GrouperSession.startRootSession();

      RegistryReset rr = new RegistryReset();
      rr._addSubjects();

      {
        Group testGroup = GroupFinder.findByName(grouperSession, "aStem:testGroup", false);
        // if this created group exists, delete it
        if (testGroup != null) {
          testGroup.delete();
        }
      }
      
      {
        Group testStem = GroupFinder.findByName(grouperSession, "aStem:testStem", false);
        // if this created group exists, delete it
        if (testStem != null) {
          testStem.delete();
        }
        
      }
      
      //make sure to create with the same id
      
      String groupTypeId = null;
      String groupType2Id = null;
      String groupType3Id = null;
      String groupTypeAttribute1Id = null;
      String groupTypeAttribute2Id = null;
      String groupType2Attribute1Id = null;
      String groupType2Attribute2Id = null;
      String groupType3Attribute1Id = null;
      String groupType3Attribute2Id = null;

      {
        String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
        String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");

        GroupType groupType = GroupTypeFinder.find("aType", false);
        if (groupType != null) {
          groupTypeId = groupType.getUuid();
          
          // see if the attribute already exists.
          AttributeDefName attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr_1", false);
          if (attribute != null) {
            groupTypeAttribute1Id = attribute.getId();
          }

          attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr_2", false);
          if (attribute != null) {
            groupTypeAttribute2Id = attribute.getId();
          }

        }
        
        groupType = GroupTypeFinder.find("aType2", false);
        if (groupType != null) {
          groupType2Id = groupType.getUuid();
          
          // see if the attribute already exists.
          AttributeDefName attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr2_1", false);
          if (attribute != null) {
            groupType2Attribute1Id = attribute.getId();
          }

          attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr2_2", false);
          if (attribute != null) {
            groupType2Attribute2Id = attribute.getId();
          }

        }
        
        groupType = GroupTypeFinder.find("aType3", false);
        if (groupType != null) {
          groupType3Id = groupType.getUuid();
          
          // see if the attribute already exists.
          AttributeDefName attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr3_1", false);
          if (attribute != null) {
            groupType3Attribute1Id = attribute.getId();
          }

          attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr3_2", false);
          if (attribute != null) {
            groupType3Attribute2Id = attribute.getId();
          }

        }
        
      }

      Subject grouperSystemSubject = SubjectFinder.findById("GrouperSystem", true);
      
      try {
        SubjectFinder.findById("10039438", true);
      } catch (SubjectNotFoundException snfe) {
        RegistrySubject registrySubject = new RegistrySubject();
        registrySubject.setId("10039438");
        registrySubject.setName("10039438");
        registrySubject.setTypeString("person");
        GrouperDAOFactory.getFactory().getRegistrySubject().create(registrySubject);
      }
      
      Subject subject1 = SubjectFinder.findById("10039438", true);

      try {
        SubjectFinder.findById("10021368", true);
      } catch (SubjectNotFoundException snfe) {
        RegistrySubject registrySubject = new RegistrySubject();
        registrySubject.setId("10021368");
        registrySubject.setName("10021368");
        registrySubject.setTypeString("person");
        GrouperDAOFactory.getFactory().getRegistrySubject().create(registrySubject);
      }
      
      Subject subject2 = SubjectFinder.findById("10021368", true);
      
      try {
        SubjectFinder.findById("mchyzer", true);
      } catch (SubjectNotFoundException snfe) {
        try {
          SubjectFinder.findByIdentifier("mchyzer", true);
        } catch (SubjectNotFoundException snfe2) {
          RegistrySubject registrySubject = new RegistrySubject();
          registrySubject.setId("mchyzer");
          registrySubject.setName("mchyzer");
          registrySubject.setTypeString("person");
          GrouperDAOFactory.getFactory().getRegistrySubject().create(registrySubject);
        }
      }
      
      Stem aStem = Stem.saveStem(grouperSession, "aStem", null,"aStem", "a stem",  "a stem description", null, false);

      Group.saveGroup(grouperSession, "test:testGroup",  null, "test:testGroup", 
          "test group","test group description",  null, true);

      Group aGroup = Group.saveGroup(grouperSession, "aStem:aGroup",  null,"aStem:aGroup", 
          "a group","a group description",  null, false);
      
      new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
      
      aGroup.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.ADMIN, false);
      
      Group aGroup2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null,"aStem:aGroup2", 
          "a group2","a group description2",   null, false);

      //make sure assigned
      aGroup.addMember(grouperSystemSubject, false);
      aGroup.addMember(subject1, false);
      aGroup.addMember(subject2, false);
      
      aGroup2.addMember(grouperSystemSubject, false);
      aGroup2.addMember(subject1, false);
      aGroup2.addMember(subject2, false);
       
      String userGroupName = GrouperWsConfig.retrieveConfig().propertyValueStringRequired(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
      Group wsUserGroup = Group.saveGroup(grouperSession, userGroupName, null, userGroupName, null, null, null, true);
      Subject userSubject = SubjectFinder.findByIdentifier(RestClientSettings.USER, true);
      wsUserGroup.addMember(userSubject, false);

      String actAsGroupName = GrouperWsConfig.retrieveConfig().propertyValueStringRequired(GrouperWsConfig.WS_ACT_AS_GROUP);
      Group actAsGroup = Group.saveGroup(grouperSession, actAsGroupName, null, actAsGroupName, null, null, null, true);
      actAsGroup.addMember(userSubject, false);
      actAsGroup.addMember(subject2, false);
      
      String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueStringRequired(GrouperConfig.PROP_WHEEL_GROUP);
      Group wheelGroup = Group.saveGroup(grouperSession, wheelGroupName, null, wheelGroupName, null, null, null, true);
      wheelGroup.addMember(userSubject, false);
      wheelGroup.addMember(subject2, false);
      
      //add the member
      MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.0", true), true);
      MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.2", true), true);
      MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.3", true), true);
      
      //add some types and attributes
      GroupType groupType = GroupType.createType(grouperSession, "aType", false, groupTypeId);
      GroupType groupType2 = GroupType.createType(grouperSession, "aType2", false, groupType2Id);
      GroupType groupType3 = GroupType.createType(grouperSession, "aType3", false, groupType3Id);
      groupType.addAttribute(grouperSession, "attr_1", false, groupTypeAttribute1Id);
      groupType.addAttribute(grouperSession, "attr_2", false, groupTypeAttribute2Id);
      groupType2.addAttribute(grouperSession, "attr2_1", false, groupType2Attribute1Id);
      groupType2.addAttribute(grouperSession, "attr2_2", false, groupType2Attribute2Id);
      groupType3.addAttribute(grouperSession, "attr3_1", false, groupType3Attribute1Id);
      groupType3.addAttribute(grouperSession, "attr3_2", false, groupType3Attribute2Id);
      
      //new attribute framework
      //###################################
      
      AttributeDefName testAttrName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttrName");
      AttributeDef attrDef = testAttrName.getAttributeDef();
      attrDef.setAssignToImmMembership(true);
      attrDef.setMultiValued(true);
      attrDef.setValueType(AttributeDefValueType.string);
      attrDef.store();

      AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
      AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
      
      final AttributeDef attributeDef = attributeDefName.getAttributeDef();
      
      attributeDef.setValueType(AttributeDefValueType.integer);
      attributeDef.setMultiValued(true);
      attributeDef.store();

      if (attributeDef.getAttributeDefActionDelegate().findAction("assign", false) == null) {
        attributeDef.getAttributeDefActionDelegate().addAction("assign");
      }
      
      final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
      
      attributeDef2.setAssignToGroup(false);
      attributeDef2.setAssignToGroupAssn(true);
      attributeDef2.store();

      if (attributeDef2.getAttributeDefActionDelegate().findAction("assign", false) == null) {
        attributeDef2.getAttributeDefActionDelegate().addAction("assign");
      }

      Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();

      Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:groupTestAttrAssign2").assignName("test:groupTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();

      //test subject 0 can view and read
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW, false);
      attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

      AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
      AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
      
      AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
      @SuppressWarnings("unused")
      AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();

      attributeAssign.getValueDelegate().addValueInteger(5L);
      attributeAssign.getValueDelegate().addValueInteger(15L);
      attributeAssign.getValueDelegate().addValueInteger(5L);
      
      //#############################
      //Permission framework
      
      //parent implies child
      Role role = new GroupSave(grouperSession).assignName("aStem:role").assignTypeOfGroup(TypeOfGroup.role).save();
      Role role2 = new GroupSave(grouperSession).assignName("aStem:role2").assignTypeOfGroup(TypeOfGroup.role).save();
          
      ((Group)role).addMember(SubjectTestHelper.SUBJ0, false);    
      ((Group)role).addMember(SubjectTestHelper.SUBJ4, false);    
      ((Group)role2).addMember(SubjectTestHelper.SUBJ1, false);    
      
      AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("aStem:permissionDef").assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
      
      AttributeDefName permissionDefName = new AttributeDefNameSave(grouperSession, permissionDef).assignName("aStem:permissionDefName").save();
      AttributeDefName permissionDefName2 = new AttributeDefNameSave(grouperSession, permissionDef).assignName("aStem:permissionDefName2").save();
      AttributeDefName permissionDefName3 = new AttributeDefNameSave(grouperSession, permissionDef).assignName("aStem:permissionDefName3").save();
      AttributeDefName permissionDefName4 = new AttributeDefNameSave(grouperSession, permissionDef).assignName("aStem:permissionDefName4").save();

      if (permissionDef.getAttributeDefActionDelegate().findAction("read", false) == null) {
        permissionDef.getAttributeDefActionDelegate().addAction("read");
      }
      if (permissionDef.getAttributeDefActionDelegate().findAction("write", false) == null) {
        permissionDef.getAttributeDefActionDelegate().addAction("write");
      }
      
      //subject 0 has a "role" permission of permissionDefName with "action" in 
      //subject 1 has a "role_subject" permission of permissionDefName2 with action2
      
      role.getPermissionRoleDelegate().assignRolePermission("read", permissionDefName);
      role2.getPermissionRoleDelegate()
        .assignSubjectRolePermission("write", permissionDefName2, SubjectTestHelper.SUBJ1);

      GrouperBuiltinMessagingSystem.createQueue("def");
      GrouperBuiltinMessagingSystem.allowSendToQueue("def", SubjectTestHelper.SUBJ0);
      GrouperBuiltinMessagingSystem.allowReceiveFromQueue("def", SubjectTestHelper.SUBJ0);

      ExternalSubjectSave externalSubjectSave = new ExternalSubjectSave(GrouperServiceUtils.testSession)
          .assignName("My Name")
          .assignIdentifier("a_ident@idp.example.edu").assignEmail("a@b.com");
      
      boolean hasJabber = StringUtils.equals(GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.attributes.jabber.systemName"), "jabber");

      if (hasJabber) {
        externalSubjectSave.addAttribute("jabber", "a_jabber@b.c");
      }
      externalSubjectSave.save();

      GrouperBuiltinMessagingSystem.createQueue("ghi");
      GrouperBuiltinMessagingSystem.allowSendToQueue("ghi", SubjectTestHelper.SUBJ0);
      GrouperBuiltinMessagingSystem.allowReceiveFromQueue("ghi", SubjectTestHelper.SUBJ0);

      GrouperBuiltinMessagingSystem.createQueue("def");
      GrouperBuiltinMessagingSystem.allowSendToQueue("def", SubjectTestHelper.SUBJ0);
      GrouperBuiltinMessagingSystem.allowReceiveFromQueue("def", SubjectTestHelper.SUBJ0);

      //anything else?

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      HibUtils.clearDisallowCacheThreadLocal();
    }
     
  }
  
  /**
   * all add member captures
   */
  public static void captureAddMember() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAddMember.class, "addMember", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAddMemberLite.class, "addMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAddMemberRest.class, "addMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
            WsSampleAddMemberRest2.class, "addMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAddMemberRestLite.class, "addMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAddMemberRestLite2.class, "addMember", "_withInput");
    
  }

  /**
   * all member change subject captures
   */
  public static void captureMemberChangeSubject() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleMemberChangeSubject.class, "memberChangeSubject", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleMemberChangeSubjectLite.class, "memberChangeSubject", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleMemberChangeSubjectRest.class, "memberChangeSubject", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleMemberChangeSubjectRestLite.class, "memberChangeSubject", null);
    
  }

  /**
   * all member change subject captures
   */
  public static void captureGetGrouperPrivileges() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetGrouperPrivilegesLite.class, "getGrouperPrivileges", (String)null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGrouperPrivilegesRestLite.class, "getGrouperPrivileges", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGrouperPrivilegesListRestLite.class, "getGrouperPrivileges", "_list");
    
  }

  /**
   * all member change subject captures
   */
  public static void captureAssignGrouperPrivileges() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignGrouperPrivileges.class, "assignGrouperPrivileges", (String)null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignGrouperPrivilegesRest.class, "assignGrouperPrivileges", null);

    
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignGrouperPrivilegesLite.class, "assignGrouperPrivileges", (String)null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignGrouperPrivilegesRestLite.class, "assignGrouperPrivileges", null);
    
  }

  /**
   * all delete member captures
   */
  public static void captureDeleteMember() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleDeleteMember.class, "deleteMember", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleDeleteMemberLite.class, "deleteMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleDeleteMemberRest.class, "deleteMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleDeleteMemberRestLite.class, "deleteMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleDeleteMemberRestLite2.class, "deleteMember", "_withInput");
    
  }
  
  /**
   * all has member captures
   */
  public static void captureHasMember() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleHasMember.class, "hasMember", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleHasMemberLite.class, "hasMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleHasMemberRest.class, "hasMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleHasMemberRestLite.class, "hasMember", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleHasMemberRestLite2.class, "hasMember", "_withInput");
    
  }

  /**
   * all group delete captures
   */
  public static void captureGroupDelete() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupDelete.class, "groupDelete", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupDeleteLite.class, "groupDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupDeleteRest.class, "groupDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupDeleteRestLite.class, "groupDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupDeleteRestLite2.class, "groupDelete", "_withInput");
    
  }

  /**
   * all stem delete captures
   */
  public static void captureStemDelete() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleStemDelete.class, "stemDelete", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleStemDeleteLite.class, "stemDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleStemDeleteRest.class, "stemDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleStemDeleteRestLite.class, "stemDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleStemDeleteRestLite2.class, "stemDelete", "_withInput");
    
  }

  /**
   * all stem save captures
   */
  public static void captureStemSave() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleStemSave.class, "stemSave", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleStemSaveLite.class, "stemSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleStemSaveRest.class, "stemSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleStemSaveRestLite.class, "stemSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleStemSaveRestLite.class, "stemSave", "_withInput");
    
  }

  /**
   * rampart captures
   */
  public static void captureRampart() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        RampartSampleGetGroupsLite.class, "rampart", (String)null);
    
  }
  
  /**
   * all group save captures
   */
  public static void captureGroupSave() {
    
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupSave.class, "groupSave", (String)null, 5000);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupDetailSave.class, "groupSave", "_withDetail", 5000);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupSaveLite.class, "groupSave", null, 5000);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupSaveRest.class, "groupSave", null, 5000);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupDetailSaveRest.class, "groupSave", "_withDetail", 5000);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupSaveRestLite.class, "groupSave", null, 5000);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupSaveRestLite.class, "groupSave", "_withInput", 5000);
    
  }

  /**
   * all find stems captures
   */
  public static void captureFindStems() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindStems.class, "findStems", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindStemsLite.class, "findStems", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindStemsRest.class, "findStems", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindStemsRestLite.class, "findStems", "_withInput");
    
  }

  /**
   * all get members captures
   */
  public static void captureGetMembers() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetMembers.class, "getMembers", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetMembersLite.class, "getMembers", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetMembersRest.class, "getMembers", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetMembersRestLite.class, "getMembers", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetMembersRestLite2.class, "getMembers", "_withInput");
    
  }
  
  /**
   * all get members captures
   */
  public static void captureGetMemberships() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetMemberships.class, "getMemberships", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetMembershipsLite.class, "getMemberships", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetMembershipsRest.class, "getMemberships", null);
    //shold be commented out?
    captureSample(WsSampleClientType.REST_BEANS,  
            WsSampleGetMembershipsRest2.class, "getMemberships", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetMembershipsRestLite.class, "getMemberships", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetMembershipsRestLite2.class, "getMemberships", "_withInput");
    
  }
  
  /**
   * all get groups captures
   */
  public static void captureGetGroups() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetGroups.class, "getGroups", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetGroupsAdmins.class, "getGroups", "_admins");
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetGroupsLite.class, "getGroups", null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetGroupsAdminsLite.class, "getGroups", "_admins");
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsRest.class, "getGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsRestLite.class, "getGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsAdminsRest.class, "getGroups", "_admins");
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsAdminsRestLite.class, "getGroups", "_admins");
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsRestLite2.class, "getGroups", "_withInput");
    
  }

  /**
   * run a sample and capture the output, and put it in the 
   * @param clientType
   * @param clientClass
   * @param samplesFolderName is the for
   * @param fileNameInfo to specify description of example, or none
   */
  public static void captureSample(WsSampleClientType clientType,
        Class<? extends WsSample> clientClass, 
        String samplesFolderName, String fileNameInfo) {
    captureSample(clientType, clientClass, samplesFolderName, fileNameInfo, 0);
  }

  /**
   * run a sample and capture the output, and put it in the 
   * @param clientType
   * @param clientClass
   * @param samplesFolderName is the for
   * @param fileNameInfo to specify description of example, or none
   * @param sleepMillis how many millis to sleep in between
   */
  public static void captureSample(WsSampleClientType clientType,
        Class<? extends WsSample> clientClass, 
        String samplesFolderName, String fileNameInfo, long sleepMillis) {
    Object[] formats = clientType.formats();
    //just pass null if none
    formats = GrouperUtil.defaultIfNull(formats, new Object[]{null});
    
    for (Object format : formats) {
      //make sure example supports the type
      if (clientType.validFormat(clientClass, format)) {
        setupData();
        captureSample(clientType, clientClass, samplesFolderName, fileNameInfo, format);
        //let the cache clear
        GrouperUtil.sleep(sleepMillis);
      }
    }
  }

  /**
   * run a sample and capture the output, and put it in the 
   * @param clientType
   * @param clientClass
   * @param samplesFolderName is the for
   * @param fileNameInfo to specify description of example, or none
   * @param format
   */
  public static void captureSample(WsSampleClientType clientType,
        Class<? extends WsSample> clientClass, 
        String samplesFolderName, String fileNameInfo, Object format) {
    File resultFile = null;
    try {
      
      //give the old server time to shut down?
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {}
      
      String formatString = format == null ? "" : ("_" + ((Enum<?>)format).name());
      
      //assume parent dirs are there...
      resultFile = new File(
          GrouperWsConfig.getPropertyString("ws.testing.grouper-ws.dir") + 
          "/doc/samples/" + samplesFolderName + "/"
          + clientClass.getSimpleName() + StringUtils.trimToEmpty(fileNameInfo)
          + formatString + ".txt");
      
      //if parent dir doesnt exist, there is probably a problem
      if (!resultFile.getParentFile().exists()) {
        throw new RuntimeException("Parent dir doesnt exist, is everything configured correctly " +
        		"and running in the right dir? " + resultFile.getAbsolutePath());
      }
      
      TcpCaptureServer echoServer = new TcpCaptureServer();
      Thread thread = echoServer.startServer(GrouperWsConfig.getPropertyInt("ws.testing.port", 8092), 
          GrouperWsConfig.getPropertyInt("ws.sampleForwardTo.port", 8091), true);
      
      //capture stdout and stderr
      PrintStream outOrig = System.out;
      ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outBaos));
      
      PrintStream errOrig = System.err;
      ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
      System.setErr(new PrintStream(errBaos));

      try {
        //run the logic for this type and format
        clientType.executeSample(clientClass, format);
      } finally {
        System.setOut(outOrig);
        System.setErr(errOrig);
      }
      
      String stdout = outBaos.toString();
      String stderr = errBaos.toString();
      
      thread.join();

      String request = GrouperServiceUtils.formatHttp(echoServer.getRequest());
      LOG.debug("\n\nRequest: \n\n" + request);
      String response = GrouperServiceUtils.formatHttp(echoServer.getResponse());
      LOG.debug("\n\nResponse: \n\n" + response);
      
      //compose the file:
      StringBuilder result = new StringBuilder();
      
      String fileSuffixString = StringUtils.isBlank(fileNameInfo) ? "" : ("type: " + fileNameInfo + ", ");
      String formatString2 = format == null ? "" : ("format: " + ((Enum<?>)format).name() + ", ");
      result.append("Grouper web service sample of service: " + samplesFolderName + ", "
          + clientClass.getSimpleName() + ", "
          + clientType.friendlyName() + ", "
          + fileSuffixString + formatString2 + "for version: " + GrouperVersion.currentVersion().toString() + "\n");
      
      result.append("\n\n#########################################\n");
      result.append("##\n");
      result.append("## HTTP request sample (could be formatted for view by\n");
      result.append("## indenting or changing dates or other data)\n");
      result.append("##\n");
      result.append("#########################################\n\n\n");
      
      result.append(request);
      
      result.append(result.charAt(result.length()-1) == '\n' ? "" : "\n");
      result.append("\n\n#########################################\n");
      result.append("##\n");
      result.append("## HTTP response sample (could be formatted for view by\n");
      result.append("## indenting or changing dates or other data)\n");
      result.append("##\n");
      result.append("#########################################\n\n\n");
      
      result.append(response);
      
      result.append(result.charAt(result.length()-1) == '\n' ? "" : "\n");
      result.append("\n\n#########################################\n");
      result.append("##\n");
      result.append("## Java source code (note, any programming language / objects\n");
      result.append("## can use used to generate the above request/response.  Nothing\n");
      result.append("## is Java specific.  Also, if you are using Java, the client libraries\n");
      result.append("## are available\n");
      result.append("##\n");
      result.append("#########################################\n\n\n");
      
      result.append(GrouperUtil.readFileIntoString(clientType.sourceFile(clientClass)));
      
      if (!StringUtils.isBlank(stdout)) {
        result.append(result.charAt(result.length()-1) == '\n' ? "" : "\n");
        result.append("\n\n#########################################\n");
        result.append("##\n");
        result.append("## Stdout\n");
        result.append("##\n");
        result.append("#########################################\n\n\n");
        
        result.append(stdout);
        
      }
      
      if (!StringUtils.isBlank(stderr)) {
        result.append(result.charAt(result.length()-1) == '\n' ? "" : "\n");
        result.append("\n\n#########################################\n");
        result.append("##\n");
        result.append("## Stderr\n");
        result.append("##\n");
        result.append("#########################################\n\n\n");
        
        result.append(stderr);
        
      }
      
      boolean saved = GrouperUtil.saveStringIntoFile(resultFile, result.toString(), true, true);
      
      if (saved) {
        System.out.println("Updated File: " + resultFile.getName());
      } else {
        System.out.println("File: " + resultFile.getName() + " had no updates and did not change");
      }
      
      
    } catch (Exception e) {
      String error = "Problem with: " + resultFile.getName() + ", " + clientType.name() + ", " 
          + clientClass.toString() + ", " + format + ", " + e.getMessage();
      System.out.println(error + ", " + ExceptionUtils.getFullStackTrace(e));
      LOG.error(error, e);
    }
  }

  /**
   * all get subjects captures
   */
  public static void captureGetSubjects() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetSubjects.class, "getSubjects", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetSubjectsLite.class, "getSubjects", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetSubjectsRest.class, "getSubjects", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetSubjectsRestLite.class, "getSubjects", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetSubjectsRestLite2.class, "getSubjects", "_withInput");
    
  }

  /**
   * all get members captures
   */
  public static void captureGetAttributeAssignments() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetAttributeAssignments.class, "getAttributeAssignments", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetAttributeAssignmentsLite.class, "getAttributeAssignments", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetAttributeAssignmentsRest.class, "getAttributeAssignments", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetAttributeAssignmentsRestLite.class, "getAttributeAssignments", null);
    
  }

  /**
   * all get members captures
   */
  public static void captureGetAttributeAssignActions() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetAttributeAssignActions.class, "getAttributeAssignActions", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetAttributeAssignActionsLite.class, "getAttributeAssignActions", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetAttributeAssignActionsRest.class, "getAttributeAssignActions", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetAttributeAssignActionsRestLite.class, "getAttributeAssignActions", null);
    
  }

  /**
   * assign attributes captures
   */
  public static void captureAssignAttributes() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributes.class, "assignAttributes", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributesLite.class, "assignAttributes", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributesRest.class, "assignAttributes", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributesRestLite.class, "assignAttributes", null);
    
  }

  /**
   * assign attributes batch captures
   */
  public static void captureAssignAttributesBatch() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
      WsSampleAssignAttributesBatch.class, "assignAttributesBatch", (String)null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributesBatchRest.class, "assignAttributesBatch", null);
  }

  /**
   * all get permissions captures
   */
  public static void captureGetPermissionAssignments() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetPermissionAssignments.class, "getPermissionAssignments", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGetPermissionAssignmentsLite.class, "getPermissionAssignments", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetPermissionAssignmentsRest.class, "getPermissionAssignments", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetPermissionAssignmentsRestLite.class, "getPermissionAssignments", null);
    
  }

  /**
   * assign permissions captures
   */
  public static void captureAssignPermissions() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignPermissions.class, "assignPermissions", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignPermissionsLite.class, "assignPermissions", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignPermissionsRest.class, "assignPermissions", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignPermissionsRestLite.class, "assignPermissions", null);
    
  }

  /**
   * assign attributes captures
   */
  public static void captureAssignAttributesWithValue() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributesWithValue.class, "assignAttributesWithValue", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributesWithValueLite.class, "assignAttributesWithValue", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributesWithValueRest.class, "assignAttributesWithValue", null);
    captureSample(WsSampleClientType.REST_BEANS,  
            WsSampleAssignAttributesWithValueRest2.class, "assignAttributesWithValue", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributesWithValueRestLite.class, "assignAttributesWithValue", null);
    
  }

  /**
   * all find attributeDefNames captures
   */
  public static void captureFindAttributeDefNames() {
    
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindAttributeDefNames.class, "findAttributeDefNames", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindAttributeDefNamesLite.class, "findAttributeDefNames", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindAttributeDefNamesRest.class, "findAttributeDefNames", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindAttributeDefNamesRestLite.class, "findAttributeDefNames", null);
    
  }

  /**
   * all assignAttributeDefNameInheritance captures
   */
  public static void captureAssignAttributeDefNameInheritance() {
    
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributeDefNameInheritance.class, "assignAttributeDefNameInheritance", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAssignAttributeDefNameInheritanceLite.class, "assignAttributeDefNameInheritance", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributeDefNameInheritanceRest.class, "assignAttributeDefNameInheritance", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAssignAttributeDefNameInheritanceRestLite.class, "assignAttributeDefNameInheritance", null);
    
  }

  /**
   * all assignAttributeDefNameInheritance captures
   */
  public static void captureAttributeDefNameSave() {
    
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefNameSave.class, "attributeDefNameSave", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefNameSaveLite.class, "attributeDefNameSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefNameSaveRest.class, "attributeDefNameSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefNameSaveRestLite.class, "attributeDefNameSave", null);
    
  }

  /**
   * all assignAttributeDefNameInheritance captures
   */
  public static void captureAttributeDefNameDelete() {
    
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefNameDelete.class, "attributeDefNameDelete", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleAttributeDefNameDeleteLite.class, "attributeDefNameDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefNameDeleteRest.class, "attributeDefNameDelete", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleAttributeDefNameDeleteRestLite.class, "attributeDefNameDelete", null);
    
  }

  /**
   * all find groups captures
   */
  public static void captureFindGroups() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindGroups.class, "findGroups", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindGroupsLite.class, "findGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindGroupsRest.class, "findGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindGroupsRestLite.class, "findGroups", "_withInput");
    
  }

  /**
   * all find external subjects captures
   */
  public static void captureFindExternalSubjects() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleFindExternalSubjects.class, "findExternalSubjects", (String)null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleFindExternalSubjectsRest.class, "findExternalSubjects", null);
    
  }

  /**
   * all external subject delete captures
   */
  public static void captureExternalSubjectDelete() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleExternalSubjectDelete.class, "externalSubjectDelete", (String)null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleExternalSubjectDeleteRest.class, "externalSubjectDelete", null);
    
  }

  /**
   * all external subject save captures
   */
  public static void captureExternalSubjectSave() {
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleExternalSubjectSave.class, "externalSubjectSave", (String)null, 5000);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleExternalSubjectSaveRest.class, "externalSubjectSave", null, 5000);
    
  }
  
}
