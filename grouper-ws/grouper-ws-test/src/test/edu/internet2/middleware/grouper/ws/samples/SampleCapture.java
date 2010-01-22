/*
 * @author mchyzer
 * $Id: SampleCapture.java,v 1.8 2009-03-15 08:14:42 mchyzer Exp $
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
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.webservicesClient.RampartSampleGetGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAssignGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleDeleteMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleDeleteMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindGroups;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindStems;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindStemsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroups;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsAdmins;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsAdminsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembers;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembersLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupDeleteLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupDetailSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGroupSaveLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleHasMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleHasMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleMemberChangeSubject;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleMemberChangeSubjectLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemDelete;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemDeleteLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemSave;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleStemSaveLite;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
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
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleAssignGrouperPrivilegesRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleGetGrouperPrivilegesListRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.WsSampleGetGrouperPrivilegesRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.member.WsSampleAddMemberRest;
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
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleFindStemsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleFindStemsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemDeleteRest;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemDeleteRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemDeleteRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemSaveRest;
import edu.internet2.middleware.grouper.ws.samples.rest.stem.WsSampleStemSaveRestLite;
import edu.internet2.middleware.grouper.ws.samples.types.WsSample;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleClientType;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouper.ws.util.TcpCaptureServer;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 * capture a sample and put in text file.  To run this, I have my properties files set correctly,
 * and run a server (or tcp forwarder) on 8091.  make sure 8092 is available for the capture server port.
 * set the web service properties to go to 8092
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
    
    setupData();
    
    //captureGetGroups();
    
//  captureRampart();
//    captureSample(WsSampleClientType.REST_BEANS,  
//        WsSampleMemberChangeSubjectRest.class, "memberChangeSubject", null);

    /*
    captureGetGrouperPrivileges();
    captureAssignGrouperPrivileges();
    captureAddMember();
    captureDeleteMember();
    captureHasMember();
    captureGetGroups();
    captureGetMembers();
    captureFindGroups();
    captureFindStems();
    captureStemDelete();
    captureStemSave();
    captureGroupDelete();
    captureGroupSave();
    captureMemberChangeSubject();

    */
    
    captureGroupSave();

  }

  /** certain data has to exist for samples to run */
  private static void setupData() {
    GrouperSession grouperSession = null;
    try {
      
      RegistryReset.internal_resetRegistryAndAddTestSubjects(false);
      
      GrouperCheckConfig.checkGroups();

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
      grouperSession = GrouperSession.start(grouperSystemSubject);
      
      Stem.saveStem(grouperSession, "aStem", null,"aStem", "a stem",  "a stem description", null, false);
      
      Group aGroup = Group.saveGroup(grouperSession, "aStem:aGroup",  null,"aStem:aGroup", 
          "a group","a group description",  null, false);
      Group aGroup2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null,"aStem:aGroup2", 
          "a group2","a group description2",   null, false);
      
      //make sure assigned
      aGroup.addMember(grouperSystemSubject, false);
      aGroup.addMember(subject1, false);
      aGroup.addMember(subject2, false);
      
      aGroup2.addMember(grouperSystemSubject, false);
      aGroup2.addMember(subject1, false);
      aGroup2.addMember(subject2, false);
       
      String userGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
      Group wsUserGroup = Group.saveGroup(grouperSession, userGroupName, null, userGroupName, null, null, null, true);
      Subject userSubject = SubjectFinder.findByIdentifier(RestClientSettings.USER, true);
      wsUserGroup.addMember(userSubject, false);

      String actAsGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_ACT_AS_GROUP);
      Group actAsGroup = Group.saveGroup(grouperSession, actAsGroupName, null, actAsGroupName, null, null, null, true);
      actAsGroup.addMember(userSubject, false);
      actAsGroup.addMember(subject2, false);
      
      String wheelGroupName = GrouperConfig.getProperty(GrouperConfig.PROP_WHEEL_GROUP);
      Group wheelGroup = Group.saveGroup(grouperSession, wheelGroupName, null, wheelGroupName, null, null, null, true);
      wheelGroup.addMember(userSubject, false);
      wheelGroup.addMember(subject2, false);
      
      //add the member
      MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.0", true), true);
      MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.2", true), true);
      MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.3", true), true);
      
      //add some types and attributes
      GroupType groupType = GroupType.createType(grouperSession, "aType", false);
      GroupType groupType2 = GroupType.createType(grouperSession, "aType2", false);
      GroupType groupType3 = GroupType.createType(grouperSession, "aType3", false);
      groupType.addAttribute(grouperSession, "attr_1", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      groupType.addAttribute(grouperSession, "attr_2", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      groupType2.addAttribute(grouperSession, "attr2_1", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      groupType2.addAttribute(grouperSession, "attr2_2", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      groupType3.addAttribute(grouperSession, "attr3_1", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      groupType3.addAttribute(grouperSession, "attr3_2", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      
      //anything else?

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
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
        WsSampleGroupSave.class, "groupSave", (String)null);
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupDetailSave.class, "groupSave", "_withDetail");
    captureSample(WsSampleClientType.GENERATED_SOAP,  
        WsSampleGroupSaveLite.class, "groupSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupSaveRest.class, "groupSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupDetailSaveRest.class, "groupSave", "_withDetail");
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupSaveRestLite.class, "groupSave", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGroupSaveRestLite.class, "groupSave", "_withInput");
    
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
    Object[] formats = clientType.formats();
    //just pass null if none
    formats = GrouperUtil.defaultIfNull(formats, new Object[]{null});
    
    for (Object format : formats) {
      //make sure example supports the type
      if (clientType.validFormat(clientClass, format)) {
        setupData();
        captureSample(clientType, clientClass, samplesFolderName, fileNameInfo, format);
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
    try {
      
      //give the old server time to shut down?
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {}
      
      String formatString = format == null ? "" : ("_" + ((Enum<?>)format).name());
      
      //assume parent dirs are there...
      File resultFile = new File(
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
      Thread thread = echoServer.startServer(8092, 8091, true);
      
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
          + fileSuffixString + formatString2 + "for version: " + GrouperWsVersion.currentVersion().name() + "\n");
      
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
      String error = "Problem with: " + clientType.name() + ", " 
          + clientClass.toString() + ", " + format + ", " + e.getMessage();
      System.out.println(error + ", " + ExceptionUtils.getFullStackTrace(e));
      LOG.error(error, e);
    }
  }
  
}
