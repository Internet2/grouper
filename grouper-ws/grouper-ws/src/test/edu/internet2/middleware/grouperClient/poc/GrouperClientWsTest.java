/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameTest;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefTest;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.GrouperClient;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsMemberChangeSubjectResults;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class GrouperClientWsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperClientWsTest("testAttributeDefNameSave"));
    //TestRunner.run(new GrouperClientWsTest("testGroupSaveLookupNameSame"));
    //TestRunner.run(new GrouperClientWsTest("testGroupSaveNoLookup"));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {

    // dont do this, it deletes types
    // super.setUp();

    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);

    RestClientSettings.resetData(wsUserString, false);

    GrouperClientUtils.grouperClientOverrideMap().put("encrypt.key",
        "sdfklj24lkj34lk34");
    GrouperClientUtils.grouperClientOverrideMap().put(
        "encrypt.disableExternalFileLookup", "false");

    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.addMember.output",
            "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsSubject.id}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.getMembers.output",
            "GroupIndex ${groupIndex}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: group: ${wsGroup.name}: subjectIndex: ${subjectIndex}: ${wsSubject.id}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.deleteMember.output",
            "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsSubject.id}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.hasMember.output",
            "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsSubject.id}: ${hasMember}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.getGroups.output",
            "SubjectIndex ${subjectIndex}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: subject: ${wsSubject.id}: groupIndex: ${groupIndex}: ${wsGroup.name}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.groupSave.output",
            "Success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsGroup.name}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.stemSave.output",
            "Success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsStem.name}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.groupDelete.output",
            "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsGroup.name}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.stemDelete.output",
            "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsStem.name}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.getGrouperPrivilegesLite.output",
            "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${objectType}: ${objectName}: subject: ${wsSubject.id}: ${wsGrouperPrivilegeResult.privilegeType}: ${wsGrouperPrivilegeResult.privilegeName}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.assignGrouperPrivilegesLite.output",
            "Success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${objectType}: ${objectName}: subject: ${wsSubject.id}: ${wsAssignGrouperPrivilegesLiteResult.privilegeType}: ${wsAssignGrouperPrivilegesLiteResult.privilegeName}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.findGroups.output",
            "Index ${index}: name: ${wsGroup.name}, displayName: ${wsGroup.displayName}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.findStems.output",
            "Index ${index}: name: ${wsStem.name}, displayName: ${wsStem.displayName}$newline$");
    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.memberChangeSubject.output",
            "Success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: oldSubject: ${wsSubjectOld.id}, newSubject: ${wsSubjectNew.id}$newline$");

    GrouperClientUtils
    .grouperClientOverrideMap()
    .put(
        "webService.getSubjects.output",
        "Index: ${index}: success: ${success}, code: ${wsSubject.resultCode}, subject: ${wsSubject.id}$newline$");
    
    GrouperClientUtils
    .grouperClientOverrideMap()
    .put(
        "webService.assignAttributeDefNameInheritance.output",
        "Success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}, message: ${resultMetadata.resultMessage}$newline$");
    GrouperClientUtils
    .grouperClientOverrideMap()
    .put(
        "webService.attributeDefNameSave.output",
        "Success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsAttributeDefName.name}$newline$");
    GrouperClientUtils
    .grouperClientOverrideMap()
    .put(
        "webService.attributeDefNameDelete.output",
        "Index ${index}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: ${wsAttributeDefName.name}$newline$");
    GrouperClientUtils
    .grouperClientOverrideMap()
    .put(
        "webService.findAttributeDefNames.output",
        "Index ${index}: name: ${wsAttributeDefName.name}, displayName: ${wsAttributeDefName.displayName}$newline$");
    
    GrouperClientUtils.grouperClientOverrideMap().put(
        "grouperClient.alias.subjectIds", "pennIds");
    GrouperClientUtils.grouperClientOverrideMap().put(
        "grouperClient.alias.subjectIdentifiers", "pennKeys");
    GrouperClientUtils.grouperClientOverrideMap().put(
        "grouperClient.alias.SubjectId", "PennId");
    GrouperClientUtils.grouperClientOverrideMap().put(
        "grouperClient.alias.SubjectIdentifier", "PennKey");
    GrouperClientUtils.grouperClientOverrideMap().put(
        "grouperClient.alias.subjectId", "pennId");
    GrouperClientUtils.grouperClientOverrideMap().put(
        "grouperClient.alias.subjectIdentifier", "pennKey");

    GrouperClientUtils
        .grouperClientOverrideMap()
        .put(
            "webService.hasMember.output",
            "Index ${index}: success: ${wsHasMemberResult.resultMetadata.success}: code: ${wsHasMemberResult.resultMetadata.resultCode}: ${wsHasMemberResult.wsSubject.id}: ${hasMember}$newline$");

    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = GroupFinder.findByName(grouperSession, "aStem:aGroup4", false);
    
    if (group != null) {
      group.delete();
    }
    
    GrouperClient.exitOnError = false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {

    GrouperClientUtils.grouperClientOverrideMap().clear();

    super.tearDown();

  }

  /**
   * @param name
   */
  public GrouperClientWsTest(String name) {
    super(name);
  }

  /**
   * @throws Exception
   */
  public void testAddMember() throws Exception {

    // make sure group exists
    // GrouperSession grouperSession = GrouperSession.startRootSession();
    // Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
    // "aStem:aGroup", "aGroup", null, null, true);
    //    
    // //give permissions
    // String wsUserLabel =
    // GrouperClientUtils.propertiesValue("grouperClient.webService.user.label",
    // true);
    // String wsUserString =
    // GrouperClientUtils.propertiesValue("grouperClient.webService." +
    // wsUserLabel, true);
    // Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = GroupFinder.findByName(grouperSession, "aStem:aGroup", true);
    
    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "SUCCESS", matcher.group(2));
      assertEquals(outputLines[0], "test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run again, should be already added
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run again, with enabled date
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0 --enabledTime=2010/02/03",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      
      Membership membership = group.getImmediateMembership(Group.getDefaultList(), SubjectTestHelper.SUBJ0, false, true);
      assertEquals(GrouperClientUtils.stringToDate("2010/02/03 00:00:00.000"), membership.getEnabledTime());
      assertEquals(null, membership.getDisabledTime());

      // #####################################################
      // run again, with disabled date
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0 --disabledTime=2010/02/03",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      
      membership = group.getImmediateMembership(Group.getDefaultList(), SubjectTestHelper.SUBJ0, false, true);
      assertEquals(GrouperClientUtils.stringToDate("2010/02/03 00:00:00.000"), membership.getDisabledTime());
      assertEquals(null, membership.getEnabledTime());

      // #####################################################
      // run again, remove enabled disabled
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      
      membership = group.getImmediateMembership(Group.getDefaultList(), SubjectTestHelper.SUBJ0, false, true);
      assertEquals(null, membership.getDisabledTime());
      assertEquals(null, membership.getEnabledTime());

      // #####################################################
      // run again, with uuid
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupUuid=" + group.getUuid() + " --pennIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("01", output);

      // #####################################################
      // run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0,id.test.subject.1 --fieldName=members",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("fieldName")
              && GrouperClientWs.mostRecentRequest.contains("members")
              && !GrouperClientWs.mostRecentRequest.contains("txType"));

      // #####################################################
      // run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --txType=NONE",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName")
              && !GrouperClientWs.mostRecentRequest.contains("members")
              && GrouperClientWs.mostRecentRequest.contains("txType")
              && GrouperClientWs.mostRecentRequest.contains("NONE")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeSubjectDetail")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeSubjectDetail"));

      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(!GrouperClientWs.mostRecentRequest.contains("txType")
          && !GrouperClientWs.mostRecentRequest.contains("NONE")
          && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsAddMemberResult.wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));

      assertTrue(outputLines[1], outputLines[1]
          .contains("my name is test.subject.1"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with default subject source
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("jdbc"));

      // #####################################################
      // run again, subjects ids coming from file
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      String subjectIdsFileName = "subjectIdsFile_"
          + GrouperClientUtils.uniqueId() + ".txt";
      File subjectIdsFile = new File(subjectIdsFileName);

      GrouperClientUtils.saveStringIntoFile(subjectIdsFile,
          "test.subject.0\ntest.subject.1");

      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=addMemberWs --groupName=aStem:aGroup --subjectIdsFile="
                + subjectIdsFileName, " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));

        // #####################################################
        // run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --paramName0=whatever --paramValue0=someValue",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
            && GrouperClientWs.mostRecentRequest.contains("someValue"));

        // #####################################################
        // run again, with addExternalSubjectIfNotFound
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        //NOTE FOR THIS TO WORK YOU NEED TO ENABLE AUTO CREATE EXTERNAL SUBJECTS IN GROUPER.PROPERTIES ON WS
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=addMemberWs --groupName=aStem:aGroup --subjectIdentifiers=a@b.c --addExternalSubjectIfNotFound=true",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        assertEquals(1, outputLines.length);

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS_CREATED", matcher.group(2));
        //cant do that since not the id... its the identifier
        //assertEquals("a@b.c", matcher.group(3));

        assertTrue(GrouperClientWs.mostRecentRequest
            .contains("addExternalSubjectIfNotFound"));

        // #####################################################
        // run again, with replaceAllExisting
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --replaceAllExisting=true",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS_ALREADY_EXISTED", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));

        assertTrue(GrouperClientWs.mostRecentRequest
            .contains("replaceAllExisting"));

        // #######################################################
        // get members, make sure all there

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        pattern = Pattern
            .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.+): (false|true)$");
        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("true", matcher.group(4));

      } finally {
        if (subjectIdsFile.exists()) {
          subjectIdsFile.delete();
        }
      }
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testGetGrouperPrivilegeLite() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (group|stem): (.+): subject: (.+): (.+): (.+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 3);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("admin", matcher.group(7));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("read", matcher.group(7));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("2", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("view", matcher.group(7));

      // #####################################################
      // run again with subject identifier, and privilege type
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectIdentifier=id.test.subject.0 --privilegeType=access",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 3);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("admin", matcher.group(7));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("read", matcher.group(7));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("2", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("view", matcher.group(7));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("access")
              && GrouperClientWs.mostRecentRequest.contains("privilegeType")
              && GrouperClientWs.mostRecentRequest
                  .contains("id.test.subject.0"));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --pennKey=id.test.subject.0 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      assertEquals("012", output);

      System.setOut(systemOut);

      // #####################################################
      // run with privilege name
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --pennKey=id.test.subject.0 --privilegeName=admin",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals(outputLines[0], "SUCCESS_ALLOWED", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("admin", matcher.group(7));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("admin")
              && GrouperClientWs.mostRecentRequest.contains("privilegeName")
              && GrouperClientWs.mostRecentRequest
                  .contains("id.test.subject.0"));

      // #####################################################
      // run again, with stem
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --stemName=aStem --pennKey=id.test.subject.0",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 2);
      assertTrue(outputLines[0], matcher.matches());

      // #####################################################
      // run again, with stem with no results
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --stemName=aStem --pennKey=id.test.subject.6",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(outputLines[0], StringUtils.isBlank(output));

      // Index 0: success: T: code: SUCCESS: stem: aStem: subject:
      // test.subject.0: naming: create
      // Index 1: success: T: code: SUCCESS: stem: aStem: subject:
      // test.subject.0: naming: stem

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("stem", matcher.group(3));
      assertEquals("aStem", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("naming", matcher.group(6));
      assertEquals("create", matcher.group(7));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("stem", matcher.group(3));
      assertEquals("aStem", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("naming", matcher.group(6));
      assertEquals("stem", matcher.group(7));

      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --includeGroupDetail=true --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(3, GrouperClientUtils.length(outputLines));
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("admin", matcher.group(7));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("read", matcher.group(7));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("2", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("view", matcher.group(7));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));

      assertTrue(outputLines[1], outputLines[1]
          .contains("my name is test.subject.0"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 3);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("admin", matcher.group(7));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("read", matcher.group(7));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("2", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("group", matcher.group(3));
      assertEquals("aStem:aGroup", matcher.group(4));
      assertEquals("test.subject.0", matcher.group(5));
      assertEquals("access", matcher.group(6));
      assertEquals("view", matcher.group(7));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testAssignGrouperPrivilegeLite() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --privilegeName=optin --allowed=true",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Success: T: code: ([A-Z_]+): (group|stem): (.+): subject: (.+): (.+): (.+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_ALLOWED", matcher.group(1));
      assertEquals("group", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));
      assertEquals("test.subject.0", matcher.group(4));
      assertEquals("access", matcher.group(5));
      assertEquals("optin", matcher.group(6));

      // #####################################################
      // run again with subject identifier, and privilege type
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectIdentifier=id.test.subject.0 --privilegeType=access --privilegeName=optin --allowed=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_ALLOWED_ALREADY_EXISTED", matcher.group(1));
      assertEquals("group", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));
      assertEquals("test.subject.0", matcher.group(4));
      assertEquals("access", matcher.group(5));
      assertEquals("optin", matcher.group(6));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("access")
              && GrouperClientWs.mostRecentRequest.contains("privilegeType")
              && GrouperClientWs.mostRecentRequest
                  .contains("id.test.subject.0"));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --privilegeName=optin --allowed=true --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --pennKey=id.test.subject.0 --privilegeName=optin --allowed=true --outputTemplate=${wsSubject.identifierLookup}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      assertEquals("id.test.subject.0", output);

      System.setOut(systemOut);

      // #####################################################
      // run again, with stem
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --stemName=aStem --pennKey=id.test.subject.0 --privilegeName=stem --allowed=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_ALLOWED_ALREADY_EXISTED", matcher.group(1));
      assertEquals("stem", matcher.group(2));
      assertEquals("aStem", matcher.group(3));
      assertEquals("test.subject.0", matcher.group(4));
      assertEquals("naming", matcher.group(5));
      assertEquals("stem", matcher.group(6));

      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --includeGroupDetail=true --includeSubjectDetail=true --privilegeName=optin --allowed=false",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NOT_ALLOWED", matcher.group(1));
      assertEquals("group", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));
      assertEquals("test.subject.0", matcher.group(4));
      assertEquals("access", matcher.group(5));
      assertEquals("optin", matcher.group(6));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --subjectId=test.subject.0 --subjectAttributeNames=name --privilegeName=optin --allowed=false --outputTemplate=${index}:$space$${wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesLiteWs --groupName=aStem:aGroup --privilegeName=optin --allowed=false --subjectId=test.subject.0 --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NOT_ALLOWED_DIDNT_EXIST", matcher.group(1));
      assertEquals("group", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));
      assertEquals("test.subject.0", matcher.group(4));
      assertEquals("access", matcher.group(5));
      assertEquals("optin", matcher.group(6));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testGroupDelete() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupDeleteWs --groupNames=aStem:aGroup", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));

      // #####################################################
      // run again, should be already deleted
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupDeleteWs --groupNames=aStem:aGroup", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_GROUP_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupDeleteWs --groupNames=aStem:aGroup --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupDeleteWs --groupNames=aStem:aGroup --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupDeleteWs --groupNames=aStem:aGroup --txType=NONE",
          " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_GROUP_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("txType")
              && GrouperClientWs.mostRecentRequest.contains("NONE"));

      // #####################################################
      // run again, with includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupDeleteWs --groupNames=aStem:aGroup --includeGroupDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_GROUP_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));

      assertTrue(!GrouperClientWs.mostRecentRequest.contains("txType")
          && !GrouperClientWs.mostRecentRequest.contains("NONE")
          && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupDeleteWs --groupNames=aStem:aGroup --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_GROUP_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testStemDelete() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=stemDeleteWs --stemNames=aStem:aStem0", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("aStem:aStem0", matcher.group(3));

      // #####################################################
      // run again, should be already deleted
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=stemDeleteWs --stemNames=aStem:aStem0", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_STEM_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aStem0", matcher.group(3));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=stemDeleteWs --stemNames=aStem:aStem0 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemDeleteWs --stemNames=aStem:aStem0 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=stemDeleteWs --stemNames=aStem:aStem0 --txType=NONE",
          " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_STEM_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aStem0", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("txType")
              && GrouperClientWs.mostRecentRequest.contains("NONE"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemDeleteWs --stemNames=aStem:aStem0 --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_STEM_NOT_FOUND", matcher.group(2));
      assertEquals("aStem:aStem0", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * normal save
   * @throws Exception
   */
  public void testGroupSaveNoLookup() throws Exception {
    
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setSaveMode("INSERT");

    WsGroup wsGroup = new WsGroup();
    wsGroup.setDisplayExtension("a group4");
    wsGroup.setExtension("aGroup4");
    wsGroup.setName("aStem:aGroup4");
    wsGroupToSave.setWsGroup(wsGroup);
    WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
    
    //prints SUCCESS_INSERTED when it works
    String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
    
    assertEquals("SUCCESS_INSERTED", resultCode);
  }

  /**
   * normal save
   * @throws Exception
   */
  public void testGroupSaveLookupNameSame() throws Exception {
    
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setSaveMode("INSERT");
    wsGroupToSave.setWsGroupLookup(new WsGroupLookup("aStem:aGroup4", null));
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDisplayExtension("a group4");
    wsGroup.setExtension("aGroup4");
    wsGroup.setName("aStem:aGroup4");
    wsGroupToSave.setWsGroup(wsGroup);
    WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
    
    //prints SUCCESS_INSERTED when it works
    String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
    
    assertEquals("SUCCESS_INSERTED", resultCode);
  }

  /**
   * normal save
   * @throws Exception
   */
  public void testGroupSaveInsertAlreadyExists() throws Exception {
    
    {
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      wsGroupToSave.setSaveMode("INSERT");
      wsGroupToSave.setWsGroupLookup(new WsGroupLookup(null, null));
      WsGroup wsGroup = new WsGroup();
      wsGroup.setDisplayExtension("a group4");
      wsGroup.setExtension("aGroup4");
      wsGroup.setName("aStem:aGroup4");
      wsGroupToSave.setWsGroup(wsGroup);
      WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
      
      //prints SUCCESS_INSERTED when it works
      String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
      
      assertEquals("SUCCESS_INSERTED", resultCode);
    }
    
    {
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      wsGroupToSave.setSaveMode("INSERT");
      wsGroupToSave.setWsGroupLookup(new WsGroupLookup(null, null));
      WsGroup wsGroup = new WsGroup();
      wsGroup.setDisplayExtension("a group4");
      wsGroup.setExtension("aGroup4");
      wsGroup.setName("aStem:aGroup4");
      wsGroupToSave.setWsGroup(wsGroup);
      try {
        @SuppressWarnings("unused")
        WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
        
        fail("Should not insert twice");
  
        //System.out.println(resultCode);
      } catch (GcWebServiceError gwse) {
        WsGroupSaveResults wsGroupSaveResults = (WsGroupSaveResults)gwse.getContainerResponseObject();
  
        String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
        
        assertEquals("GROUP_ALREADY_EXISTS", resultCode);
      }
    }    
  }
  
  /**
   * update a group to an existing name
   * @throws Exception
   */
  public void testGroupSaveUpdateExistingName() throws Exception {
    
    {
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      wsGroupToSave.setSaveMode("INSERT");
      wsGroupToSave.setWsGroupLookup(new WsGroupLookup(null, null));
      WsGroup wsGroup = new WsGroup();
      wsGroup.setDisplayExtension("a group4");
      wsGroup.setExtension("aGroup4");
      wsGroup.setName("aStem:aGroup4");
      wsGroupToSave.setWsGroup(wsGroup);
      WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
      
      //prints SUCCESS_INSERTED when it works
      String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
      
      assertEquals("SUCCESS_INSERTED", resultCode);
    }
    String uuid = null;
    {
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      wsGroupToSave.setSaveMode("INSERT");
      wsGroupToSave.setWsGroupLookup(new WsGroupLookup(null, null));
      WsGroup wsGroup = new WsGroup();
      wsGroup.setDisplayExtension("a group5");
      wsGroup.setExtension("aGroup5");
      wsGroup.setName("aStem:aGroup5");
      wsGroupToSave.setWsGroup(wsGroup);
      WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
      
      uuid = wsGroupSaveResults.getResults()[0].getWsGroup().getUuid();
      
      //prints SUCCESS_INSERTED when it works
      String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
      
      assertEquals("SUCCESS_INSERTED", resultCode);
    
    }
    
    {
      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      wsGroupToSave.setSaveMode("UPDATE");
      wsGroupToSave.setWsGroupLookup(new WsGroupLookup(null, uuid));
      WsGroup wsGroup = new WsGroup();
      wsGroup.setDisplayExtension("a group4");
      wsGroup.setExtension("aGroup4");
      wsGroup.setName("aStem:aGroup4");
      wsGroupToSave.setWsGroup(wsGroup);
      try {
        @SuppressWarnings("unused")
        WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
        
        fail("Should not update to existing name");
  
        //System.out.println(resultCode);
      } catch (GcWebServiceError gwse) {
        WsGroupSaveResults wsGroupSaveResults = (WsGroupSaveResults)gwse.getContainerResponseObject();
  
        String resultCode = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultCode();
        
        assertEquals("EXCEPTION", resultCode);
      }
    }    
      
  }

  /**
   * @throws Exception
   */
  public void testGroupSave() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    String output = null;
    String[] outputLines = null;
    Pattern pattern = null;
    Matcher matcher = null;
    try {
      systemOut.println("Umlaut: ä");
      //try with name with slash
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup0ä", " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      systemOut.println(output);
      
      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");
//ä
      pattern = Pattern.compile("^Success: T: code: ([A-Z_]+): (.*+)$");
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_INSERTED", matcher.group(1));
      assertEquals("aStem:newGroup0ä", matcher.group(2));
      
      // ##########################
      //try with name with slash

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup0/1", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      pattern = Pattern.compile("^Success: T: code: ([A-Z_]+): (.*+)$");
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_INSERTED", matcher.group(1));
      assertEquals("aStem:newGroup0/1", matcher.group(2));

      // #####################################################
      // run again, with clientVersion

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup0", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      pattern = Pattern.compile("^Success: T: code: ([A-Z_]+): (.*+)$");
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_INSERTED", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      // #####################################################
      // run again, with clientVersion

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupSaveWs --name=aStem:newGroup0 --clientVersion=v1_3_000",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      // #####################################################
      // run again, should be already added
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup0", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      // #####################################################
      // run again, should be already added
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupSaveWs --name=aStem:newGroup0 --displayExtension=newGroup0displayExtension",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_UPDATED", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup0 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupSaveWs --name=aStem:newGroup0 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup0 --saveMode=UPDATE",
          " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("saveMode")
              && GrouperClientWs.mostRecentRequest.contains("UPDATE"));

      // #####################################################
      // run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup0 --txType=NONE", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("txType")
              && GrouperClientWs.mostRecentRequest.contains("NONE")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail"));

      // #####################################################
      // run again, with includeGroupDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupSaveWs --name=aStem:newGroup0 --includeGroupDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newGroup0", matcher.group(2));

      assertTrue(!GrouperClientWs.mostRecentRequest.contains("txType")
          && !GrouperClientWs.mostRecentRequest.contains("NONE")
          && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));

      // #####################################################
      // run again, with groupLookupName
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=groupSaveWs --name=aStem:newGroup1 --groupLookupName=aStem:newGroup0",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_UPDATED", matcher.group(1));
      assertEquals("aStem:newGroup1", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("txType")
              && !GrouperClientWs.mostRecentRequest.contains("NONE")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail")
              && GrouperClientWs.mostRecentRequest.contains("wsGroupLookup")
              && GrouperClientWs.mostRecentRequest.contains("aStem:newGroup1")
              && GrouperClientWs.mostRecentRequest.contains("aStem:newGroup0"));

      // #####################################################
      // run again, with saveMode
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=groupSaveWs --name=aStem:newGroup3 --saveMode=INSERT",
          " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_INSERTED", matcher.group(1));
      assertEquals("aStem:newGroup3", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("saveMode")
          && GrouperClientWs.mostRecentRequest.contains("INSERT"));

      // #####################################################
      // run again, description
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup0 --description=aDescription",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("description")
            && GrouperClientWs.mostRecentRequest.contains("aDescription"));

        // #####################################################
        // run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup0 --paramName0=whatever --paramValue0=someValue",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
            && GrouperClientWs.mostRecentRequest.contains("someValue"));

        // #####################################################
        // run again, with typeNames
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0 --typeNames=aType",
            " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("typeNames")
            && GrouperClientWs.mostRecentRequest.contains("aType"));

        // #####################################################
        // run again, with attributes
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup0 --typeNames=aType --attributeName0=attr_1 --attributeValue0=whatever",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("attr_1")
            && GrouperClientWs.mostRecentRequest.contains("whatever"));

        // #####################################################
        // run again, with groupDetailParamName0
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup0 --groupDetailParamName0=something --groupDetailParamValue0=whatever",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("something")
            && GrouperClientWs.mostRecentRequest.contains("whatever"));

        // #####################################################
        // run again, with composite

        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:leftGroup", " "));
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:rightGroup", " "));

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup0 --compositeType=union --leftGroupName=aStem:leftGroup --rightGroupName=aStem:rightGroup --includeGroupDetail=true",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("union")
            && GrouperClientWs.mostRecentRequest.contains("aStem:leftGroup")
            && GrouperClientWs.mostRecentRequest.contains("aStem:rightGroup"));

        
        // #####################################################
        // run again, with typeOfGroup
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=groupSaveWs --name=aStem:newGroup4 --typeOfGroup=entity",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("SUCCESS_INSERTED", matcher.group(1));
        assertEquals("aStem:newGroup4", matcher.group(2));

        assertTrue(GrouperClientWs.mostRecentRequest, GrouperClientWs.mostRecentRequest.contains("<typeOfGroup>entity</typeOfGroup>"));

        
      } finally {
      }
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testStemSave() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=stemSaveWs --name=aStem:newStem0", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern.compile("^Success: T: code: ([A-Z_]+): (.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_INSERTED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      // #####################################################
      // run again, with clientVersion

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem0 --clientVersion=v1_3_000",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      // #####################################################
      // run again, should be already added
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=stemSaveWs --name=aStem:newStem0", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      // #####################################################
      // run again, should be already added
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem0 --displayExtension=newStem0displayExtension",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_UPDATED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=stemSaveWs --name=aStem:newStem0 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem0 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run with custom template with function
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem0 --outputTemplate=a${grouperClientUtils.defaultString(wsStemSaveResult.resultMetadata.resfsdfCode)}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("a", output);

      // #####################################################
      // run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils
          .splitTrim(
              "--operation=stemSaveWs --name=aStem:newStem0 --saveMode=UPDATE",
              " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("saveMode")
              && GrouperClientWs.mostRecentRequest.contains("UPDATE"));

      // #####################################################
      // run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=stemSaveWs --name=aStem:newStem0 --txType=NONE", " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("txType")
              && GrouperClientWs.mostRecentRequest.contains("NONE")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail"));

      // #####################################################
      // run again, with groupLookupName
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem1 --stemLookupName=aStem:newStem0",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_UPDATED", matcher.group(1));
      assertEquals("aStem:newStem1", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsStemLookup")
              && GrouperClientWs.mostRecentRequest.contains("aStem:newStem1")
              && GrouperClientWs.mostRecentRequest.contains("aStem:newStem0"));

      //lets delete and recreate this stem...
      GrouperSession grouperSession = GrouperSession.startRootSession();
      try {
        Stem stem = StemFinder.findByName(grouperSession, "aStem:newStem0", true, new QueryOptions().secondLevelCache(false));
        stem.delete();
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      
      // #####################################################
      // run again, with saveMode
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils
          .splitTrim(
              "--operation=stemSaveWs --name=aStem:newStem0 --saveMode=INSERT",
              " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_INSERTED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("saveMode")
          && GrouperClientWs.mostRecentRequest.contains("INSERT"));

      // #####################################################
      // run again, description
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem0 --description=aDescription",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_UPDATED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("description")
          && GrouperClientWs.mostRecentRequest.contains("aDescription"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=stemSaveWs --name=aStem:newStem0 --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS_UPDATED", matcher.group(1));
      assertEquals("aStem:newStem0", matcher.group(2));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testGetGroups() throws Exception {

    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);
    Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null,
        "aStem:aGroup2", "aGroup2", null, null, true);
    Group group3 = Group.saveGroup(grouperSession, "aStem:aGroup3", null,
        "aStem:aGroup3", "aGroup3", null, null, true);
    Group group4 = Group.saveGroup(grouperSession, "aStem:aGroup4", null,
        "aStem:aGroup4", "aGroup4", null, null, true);
    Group group5 = Group.saveGroup(grouperSession, "aStem:aGroup5", null,
        "aStem:aGroup5", "aGroup5", null, null, true);
    Group group6 = Group.saveGroup(grouperSession, "aStem:aGroup6", null,
        "aStem:aGroup6", "aGroup6", null, null, true);

    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group2.grantPriv(wsUser, AccessPrivilege.READ, false);
    group2.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group3.grantPriv(wsUser, AccessPrivilege.READ, false);
    group3.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group4.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group4.grantPriv(wsUser, AccessPrivilege.READ, false);
    group5.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group5.grantPriv(wsUser, AccessPrivilege.READ, false);
    group6.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group6.grantPriv(wsUser, AccessPrivilege.READ, false);

    // add some subjects for PIT
    group4.addMember(SubjectTestHelper.SUBJ0, false);
    group4.deleteMember(SubjectTestHelper.SUBJ0, false);
    Thread.sleep(100);
    Timestamp pointInTimeFrom = new Timestamp(new Date().getTime());
    Thread.sleep(100);

    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group2.addMember(SubjectTestHelper.SUBJ0, false);
    group2.addMember(SubjectTestHelper.SUBJ1, false);
    group3.addMember(SubjectTestHelper.SUBJ1, false);

    // add some subjects for PIT
    group5.addMember(SubjectTestHelper.SUBJ0, false);
    group5.deleteMember(SubjectTestHelper.SUBJ0, false);
    Thread.sleep(100);
    Timestamp pointInTimeTo = new Timestamp(new Date().getTime());
    Thread.sleep(100);
    group6.addMember(SubjectTestHelper.SUBJ0, false);
    group6.deleteMember(SubjectTestHelper.SUBJ0, false);
    ChangeLogTempToEntity.convertRecords();


    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      System.out.println(output);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      matcher = pattern.matcher(outputLines[3]);

      assertTrue(outputLines[3], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      // ######################################################
      // Try point in time

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      ArrayList<String> args = new ArrayList<String>();
      args.add("--operation=getGroupsWs");
      args.add("--subjectIds=test.subject.0,test.subject.1");
      args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTimeFrom));
      args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTimeTo));
      GrouperClient.main(args.toArray(new String[0]));

      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      System.out.println(output);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup5", matcher.group(5)));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup5", matcher.group(5)));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("2", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup5", matcher.group(5)));

      matcher = pattern.matcher(outputLines[3]);

      assertTrue(outputLines[3], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      matcher = pattern.matcher(outputLines[4]);

      assertTrue(outputLines[4], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=whatever",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${subjectIndex}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0011", output);

      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      matcher = pattern.matcher(outputLines[3]);

      assertTrue(outputLines[3], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${subjectIndex}:$space$${wsGetGroupsResult.wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(output, outputLines[0].contains("my name is test.subject.0"));

      assertTrue(output, outputLines[2].contains("my name is test.subject.1"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with default subject source
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("0", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      matcher = pattern.matcher(outputLines[3]);

      assertTrue(outputLines[3], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("1", matcher.group(4));
      assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
          matcher.group(5))
          || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("jdbc"));

      // #####################################################
      // run again, subjects ids coming from file
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      String subjectIdsFileName = "subjectIdsFile_"
          + GrouperClientUtils.uniqueId() + ".txt";
      File subjectIdsFile = new File(subjectIdsFileName);

      GrouperClientUtils.saveStringIntoFile(subjectIdsFile,
          "test.subject.0\ntest.subject.1");

      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getGroupsWs --subjectIdsFile=" + subjectIdsFileName,
            " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

        matcher = pattern.matcher(outputLines[2]);

        assertTrue(outputLines[2], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

        matcher = pattern.matcher(outputLines[3]);

        assertTrue(outputLines[3], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

        // #####################################################
        // run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --memberFilter=Immediate",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));

        matcher = pattern.matcher(outputLines[2]);

        assertTrue(outputLines[2], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

        matcher = pattern.matcher(outputLines[3]);

        assertTrue(outputLines[3], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), GrouperClientUtils.equals("aStem:aGroup2",
            matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("memberFilter")
            && GrouperClientWs.mostRecentRequest.contains("Immediate"));

      } finally {
        if (subjectIdsFile.exists()) {
          subjectIdsFile.delete();
        }
      }
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testHasMemberNotFound() throws Exception {
    
    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);

    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);

    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=rwjdfskjlwirwklj",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(1, GrouperUtil.length(outputLines));

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.+): (false|true)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("IS_NOT_MEMBER", matcher.group(2));
      assertEquals("rwjdfskjlwirwklj", matcher.group(3));
      assertEquals("false", matcher.group(4));

    } finally {
      System.setOut(systemOut);
    }

  }
  
  /**
   * @throws Exception
   */
  public void testHasMember() throws Exception {

    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);

    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);

    // add some subjects for PIT
    group.addMember(SubjectTestHelper.SUBJ2, false);
    group.deleteMember(SubjectTestHelper.SUBJ2, false);
    Thread.sleep(100);
    Timestamp pointInTimeFrom = new Timestamp(new Date().getTime());
    Thread.sleep(100);
    
    group.addMember(SubjectTestHelper.SUBJ3, false);
    group.deleteMember(SubjectTestHelper.SUBJ3, false);

    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);

    // add some subjects for PIT
    group.addMember(SubjectTestHelper.SUBJ4, false);
    group.deleteMember(SubjectTestHelper.SUBJ4, false);
    Thread.sleep(100);
    Timestamp pointInTimeTo = new Timestamp(new Date().getTime());
    Thread.sleep(100);
    
    group.addMember(SubjectTestHelper.SUBJ5, false);
    group.deleteMember(SubjectTestHelper.SUBJ5, false);
    ChangeLogTempToEntity.convertRecords();

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.+): (false|true)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("true", matcher.group(4));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("true", matcher.group(4));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("01", output);

      // #####################################################
      // run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0,id.test.subject.1 --fieldName=members",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("true", matcher.group(4));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("true", matcher.group(4));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("fieldName")
              && GrouperClientWs.mostRecentRequest.contains("members")
              && !GrouperClientWs.mostRecentRequest.contains("txType"));

      // #####################################################
      // run again, with uuid
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupUuid=" + group.getUuid() + " --pennKeys=id.test.subject.0,id.test.subject.1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("true", matcher.group(4));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("true", matcher.group(4));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("uuid"));

      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("true", matcher.group(4));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("true", matcher.group(4));

      assertTrue(!GrouperClientWs.mostRecentRequest.contains("txType")
          && !GrouperClientWs.mostRecentRequest.contains("NONE")
          && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsHasMemberResult.wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));

      assertTrue(outputLines[1], outputLines[1]
          .contains("my name is test.subject.1"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with default subject source
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));
      assertEquals("true", matcher.group(4));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("IS_MEMBER", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));
      assertEquals("true", matcher.group(4));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("jdbc"));

      // #####################################################
      // run again, subjects ids coming from file
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      String subjectIdsFileName = "subjectIdsFile_"
          + GrouperClientUtils.uniqueId() + ".txt";
      File subjectIdsFile = new File(subjectIdsFileName);

      GrouperClientUtils.saveStringIntoFile(subjectIdsFile,
          "test.subject.0\ntest.subject.1");

      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIdsFile="
                + subjectIdsFileName, " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("true", matcher.group(4));

        // #####################################################
        // run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --paramName0=whatever --paramValue0=someValue",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("true", matcher.group(4));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
            && GrouperClientWs.mostRecentRequest.contains("someValue"));

        // #####################################################
        // run again, with memberFilter
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --memberFilter=Immediate",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("true", matcher.group(4));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("memberFilter")
            && GrouperClientWs.mostRecentRequest.contains("Immediate"));


        // #####################################################
        // run again, with point in time params 
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        ArrayList<String> args = new ArrayList<String>();
        args.add("--operation=hasMemberWs");
        args.add("--groupName=aStem:aGroup");
        args.add("--subjectIds=test.subject.0,test.subject.1,test.subject.2,test.subject.3,test.subject.4,test.subject.5");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTimeFrom));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTimeTo));
        GrouperClient.main(args.toArray(new String[0]));

        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);
        assertTrue(outputLines[0], matcher.matches());
        assertEquals("0", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[1]);
        assertTrue(outputLines[1], matcher.matches());
        assertEquals("1", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[2]);
        assertTrue(outputLines[2], matcher.matches());
        assertEquals("2", matcher.group(1));
        assertEquals("IS_NOT_MEMBER", matcher.group(2));
        assertEquals("test.subject.2", matcher.group(3));
        assertEquals("false", matcher.group(4));

        matcher = pattern.matcher(outputLines[3]);
        assertTrue(outputLines[3], matcher.matches());
        assertEquals("3", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.3", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[4]);
        assertTrue(outputLines[4], matcher.matches());
        assertEquals("4", matcher.group(1));
        assertEquals("IS_MEMBER", matcher.group(2));
        assertEquals("test.subject.4", matcher.group(3));
        assertEquals("true", matcher.group(4));

        matcher = pattern.matcher(outputLines[5]);
        assertTrue(outputLines[5], matcher.matches());
        assertEquals("5", matcher.group(1));
        assertEquals("IS_NOT_MEMBER", matcher.group(2));
        assertEquals("test.subject.5", matcher.group(3));
        assertEquals("false", matcher.group(4));

      } finally {
        if (subjectIdsFile.exists()) {
          subjectIdsFile.delete();
        }
      }
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testDeleteMember() throws Exception {

    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);

    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group.grantPriv(wsUser, AccessPrivilege.ADMIN, false);

    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient.main(GrouperClientUtils.splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): success: T: code: ([A-Z_]+): (.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run again, should be already deleted
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals(outputLines[0], "test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run again, with uuid
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient.main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupUuid=" + group.getUuid() + " --pennIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals(outputLines[0], "test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("01", output);

      // #####################################################
      // run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0,id.test.subject.1 --fieldName=members",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("fieldName")
              && GrouperClientWs.mostRecentRequest.contains("members")
              && !GrouperClientWs.mostRecentRequest.contains("txType"));

      // #####################################################
      // run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --txType=NONE",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName")
              && !GrouperClientWs.mostRecentRequest.contains("members")
              && GrouperClientWs.mostRecentRequest.contains("txType")
              && GrouperClientWs.mostRecentRequest.contains("NONE")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeSubjectDetail")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeGroupDetail")
              && !GrouperClientWs.mostRecentRequest
                  .contains("includeSubjectDetail"));

      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(!GrouperClientWs.mostRecentRequest.contains("txType")
          && !GrouperClientWs.mostRecentRequest.contains("NONE")
          && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsDeleteMemberResult.wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));

      assertTrue(outputLines[1], outputLines[1]
          .contains("my name is test.subject.1"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with default subject source
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals("0", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals("1", matcher.group(1));
      assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("jdbc"));

      // #####################################################
      // run again, subjects ids coming from file
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      String subjectIdsFileName = "subjectIdsFile_"
          + GrouperClientUtils.uniqueId() + ".txt";
      File subjectIdsFile = new File(subjectIdsFileName);

      GrouperClientUtils.saveStringIntoFile(subjectIdsFile,
          "test.subject.0\ntest.subject.1");

      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIdsFile="
                + subjectIdsFileName, " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));

        // #####################################################
        // run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --paramName0=whatever --paramValue0=someValue",
                    " "));
        System.out.flush();
        output = new String(baos.toByteArray());

        System.setOut(systemOut);

        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        matcher = pattern.matcher(outputLines[0]);

        assertTrue(outputLines[0], matcher.matches());

        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));

        matcher = pattern.matcher(outputLines[1]);

        assertTrue(outputLines[1], matcher.matches());

        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS_WASNT_IMMEDIATE", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));

        assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
            && GrouperClientWs.mostRecentRequest.contains("someValue"));

      } finally {
        if (subjectIdsFile.exists()) {
          subjectIdsFile.delete();
        }
      }
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testMemberChangeSubject() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=memberChangeSubjectWs --oldSubjectId=test.subject.0 --newSubjectId=test.subject.1 --actAsSubjectId=GrouperSystem",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Success: T: code: ([A-Z_]+): oldSubject: (.+), newSubject: (.+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertEquals(1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS", matcher.group(1));
      assertEquals("test.subject.0", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      // #####################################################
      // run again, should be already moved
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // this should fail with member not found
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=memberChangeSubjectWs --oldSubjectId=test.subject.0 --newSubjectId=test.subject.1 --actAsSubjectId=GrouperSystem",
                    " "));
        fail("Should not get here");
      } catch (GcWebServiceError gwse) {
        WsMemberChangeSubjectResults wsMemberChangeSubjectResults = (WsMemberChangeSubjectResults) gwse
            .getContainerResponseObject();
        assertEquals("PROBLEM_WITH_CHANGE", wsMemberChangeSubjectResults
            .getResultMetadata().getResultCode());
      }

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=memberChangeSubjectWs --oldSubjectId=test.subject.0 --newSubjectId=test.subject.1 --actAsSubjectId=GrouperSystem --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template and pennkeys
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=memberChangeSubjectWs --oldPennKey=id.test.subject.1 --newPennKey=id.test.subject.0 --actAsPennId=GrouperSystem --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run again, with delete old member
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=memberChangeSubjectWs --oldSubjectIdentifier=id.test.subject.0 --newSubjectIdentifier=id.test.subject.1 --actAsSubjectId=GrouperSystem --deleteOldMember=false",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS", matcher.group(1));
      assertEquals("test.subject.0", matcher.group(2));
      assertEquals("test.subject.1", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("deleteOldMember"));

      // #####################################################
      // run again, with includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=memberChangeSubjectWs --oldSubjectId=test.subject.1 --newSubjectId=test.subject.0 --actAsSubjectId=GrouperSystem --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS", matcher.group(1));
      assertEquals("test.subject.1", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("includeSubjectDetail"));

      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=memberChangeSubjectWs --oldSubjectId=test.subject.0 --newSubjectId=test.subject.1 --actAsSubjectId=GrouperSystem --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsSubjectOld.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=memberChangeSubjectWs --oldSubjectId=test.subject.1 --newSubjectId=test.subject.0 --actAsSubjectId=GrouperSystem --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals("SUCCESS", matcher.group(1));
      assertEquals("test.subject.1", matcher.group(2));
      assertEquals("test.subject.0", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testSendFile() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    File file = new File("testSendFile.txt");
    file.deleteOnExit();

    try {
      String contents = "<WsRestAddMemberRequest>"
          + "<wsGroupLookup><groupName>aStem:aGroup</groupName></wsGroupLookup>"
          + "<subjectLookups><WsSubjectLookup>"
          + "<subjectId>test.subject.0</subjectId></WsSubjectLookup>"
          + "</subjectLookups></WsRestAddMemberRequest>";
      GrouperClientUtils.saveStringIntoFile(file, contents);

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=sendFile --fileName=testSendFile.txt --urlSuffix=groups/aStem:aGroup/members",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(output.contains("<resultCode>SUCCESS</resultCode>")
          && output.contains("        "));

      // #####################################################
      // run again, with contents, and no formatting
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=sendFile --fileContents="
                      + contents
                      + " --urlSuffix=groups/aStem:aGroup/members --indentOutput=false",
                  " "));

      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(output.contains("<resultCode>SUCCESS</resultCode>")
          && !output.contains("        "));

    } finally {
      System.setOut(systemOut);
      file.delete();
    }

  }

  /**
   * @throws Exception
   */
  public void testFindGroups() throws Exception {

    // set some stuff to query
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = GroupFinder.findByName(grouperSession, "aStem:aGroup", true);

    GroupType groupType = GroupTypeFinder.find("aType", true);
    group.addType(groupType, false);
    group.setAttribute("attr_1", "something");
    group.store();

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): name: (.+), displayName: (.+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      // #####################################################
      // filter by stem which doesnt exist, should be success
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=findGroupsWs --queryFilterType=FIND_BY_STEM_NAME --stemName=a:b:doesntExist",
                " "));
      System.out.flush();
      output = new String(baos.toByteArray());
    
      System.setOut(systemOut);
    
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
      assertEquals(output, 0, GrouperUtil.length(outputLines));

      System.out.flush();

      System.setOut(systemOut);

      
      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run again, with includeGroupDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs  --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --includeGroupDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("includeGroupDetail"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

      // #####################################################
      // run again, with uuid
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_UUID --groupUuid=abc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(output, GrouperClientUtils.isBlank(output));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("groupUuid")
          && GrouperClientWs.mostRecentRequest.contains("abc"));

      // #####################################################
      // run again, with stem
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --stemName=aStem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("stemName")
          && GrouperClientWs.mostRecentRequest.contains(">aStem<"));

      // #####################################################
      // run again, with group type
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_TYPE --groupTypeName=aType",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("FIND_BY_TYPE")
          && GrouperClientWs.mostRecentRequest.contains("groupTypeName")
          && GrouperClientWs.mostRecentRequest.contains("aType"));

      // #####################################################
      // run again, with group attribute
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_EXACT_ATTRIBUTE --groupAttributeName=attr_1 --groupAttributeValue=something",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("FIND_BY_EXACT_ATTRIBUTE")
          && GrouperClientWs.mostRecentRequest.contains("groupAttributeName")
          && GrouperClientWs.mostRecentRequest.contains("attr_1"));

      // #####################################################
      // run again, with sub filters
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=OR --queryFilterType0=OR --queryFilterType00=FIND_BY_GROUP_NAME_APPROXIMATE --groupName00=aStem:aGroup --queryFilterType01=FIND_BY_GROUP_NAME_APPROXIMATE --groupName01=aStem:aGroup --queryFilterType1=FIND_BY_GROUP_NAME_APPROXIMATE --groupName1=aStem:aGroup",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("OR")
          && GrouperClientWs.mostRecentRequest
              .contains("FIND_BY_GROUP_NAME_APPROXIMATE"));

      // #####################################################
      // run again, search by names

      Group group1 = Group.saveGroup(grouperSession, "aStem:aGroup1", null, "aStem:aGroup1", null, null, null, true);
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --groupNames=aStem:aGroup,aStem:aGroup1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aGroup1", matcher.group(2));
      assertEquals(output, "aStem:aGroup1", matcher.group(3));

      // #####################################################
      // run again, search by uuids

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --groupUuids=" + group.getId() + "," + group1.getId(),
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aGroup1", matcher.group(2));
      assertEquals(output, "aStem:aGroup1", matcher.group(3));

      // #####################################################
      // run again, sort and page

      Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null, "aStem:aGroup2", null, null, null, true);

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:a% --stemName=aStem --ascending=T --sortString=name --pageNumber=1 --pageSize=2",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aGroup1", matcher.group(2));
      assertEquals(output, "aStem:aGroup1", matcher.group(3));

      // #####################################################
      // run again, sort and page

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:a% --stemName=aStem --ascending=T --sortString=name --pageNumber=1 --pageSize=2",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aGroup1", matcher.group(2));
      assertEquals(output, "aStem:aGroup1", matcher.group(3));

      
      // #####################################################
      // run again, sort and page

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_STEM_NAME --stemName=aStem --ascending=T --sortString=name --pageNumber=2 --pageSize=2",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 1, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup2", matcher.group(2));
      assertEquals(output, "aStem:aGroup2", matcher.group(3));

      // #####################################################
      // run again, with typeOfGroup = group
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --stemName=aStem --typeOfGroups=group",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 3, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aGroup", matcher.group(2));
      assertEquals(output, "aStem:aGroup", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);
      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aGroup1", matcher.group(2));
      assertEquals(output, "aStem:aGroup1", matcher.group(3));

      matcher = pattern.matcher(outputLines[2]);
      assertTrue(outputLines[2], matcher.matches());

      assertEquals(output, "2", matcher.group(1));
      assertEquals(output, "aStem:aGroup2", matcher.group(2));
      assertEquals(output, "aStem:aGroup2", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("typeOfGroups")
          && GrouperClientWs.mostRecentRequest.contains(">group<"));

      // #####################################################
      // run again, with typeOfGroup = role
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup --stemName=aStem --typeOfGroups=role",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 0, GrouperUtil.length(outputLines));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("typeOfGroups")
          && GrouperClientWs.mostRecentRequest.contains(">role<"));

      
      
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testFindStems() throws Exception {

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_NAME --stemName=aStem",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Pattern pattern = Pattern
          .compile("^Index (\\d+): name: (.+), displayName: (.+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem", matcher.group(2));
      assertEquals(output, "aStem", matcher.group(3));

      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_NAME --groupName=aStem:aGroup --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();

      System.setOut(systemOut);

      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_NAME --stemName=aStem --outputTemplate=${index}",
                  " "));

      System.out.flush();

      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertEquals("0", output);

      // #####################################################
      // run again, with parentStemScope
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs  --stemQueryFilterType=FIND_BY_PARENT_STEM_NAME --parentStemName=aStem --parentStemNameScope=ALL_IN_SUBTREE",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aStem0", matcher.group(2));
      assertEquals(output, "aStem:aStem0", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("parentStemName")
              && GrouperClientWs.mostRecentRequest
                  .contains("parentStemNameScope"));

      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_NAME --stemName=aStem --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem", matcher.group(2));
      assertEquals(output, "aStem", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));

      // #####################################################
      // run again, with uuid
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_UUID --stemUuid=abc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(output, GrouperClientUtils.isBlank(output));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("stemUuid")
          && GrouperClientWs.mostRecentRequest.contains("abc"));

      // #####################################################
      // run again, with stem attribute
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_APPROXIMATE_ATTRIBUTE --stemAttributeName=name --stemAttributeValue=aStem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 2, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem", matcher.group(2));
      assertEquals(output, "aStem", matcher.group(3));

      matcher = pattern.matcher(outputLines[1]);
      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aStem0", matcher.group(2));
      assertEquals(output, "aStem:aStem0", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("FIND_BY_APPROXIMATE_ATTRIBUTE")
          && GrouperClientWs.mostRecentRequest.contains("stemAttributeName")
          && GrouperClientWs.mostRecentRequest.contains("aStem"));

      // #####################################################
      // run again, with sub filters
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=OR --stemQueryFilterType0=OR --stemQueryFilterType00=FIND_BY_STEM_NAME --stemName00=aStem --stemQueryFilterType01=FIND_BY_STEM_NAME --stemName01=aStem --stemQueryFilterType1=FIND_BY_STEM_NAME --stemName1=aStem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertEquals(output, 1, outputLines.length);
      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem", matcher.group(2));
      assertEquals(output, "aStem", matcher.group(3));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("OR")
          && GrouperClientWs.mostRecentRequest.contains("FIND_BY_STEM_NAME"));

      // #####################################################
      // run again, search by names
      GrouperSession grouperSession = GrouperSession.startRootSession();
      Stem aStem = StemFinder.findByName(grouperSession, "aStem", true);
      Stem aStem0 = StemFinder.findByName(grouperSession, "aStem:aStem0", true);
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemNames=aStem,aStem:aStem0",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem", matcher.group(2));
      assertEquals(output, "aStem", matcher.group(3));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aStem0", matcher.group(2));
      assertEquals(output, "aStem:aStem0", matcher.group(3));

      // #####################################################
      // run again, search by uuids

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemUuids=" + aStem.getUuid() + "," + aStem0.getUuid(),
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem", matcher.group(2));
      assertEquals(output, "aStem", matcher.group(3));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aStem0", matcher.group(2));
      assertEquals(output, "aStem:aStem0", matcher.group(3));

      // #####################################################
      // run again, sort and page
      
      new StemSave(grouperSession).assignName("aStem:aStem1").save();
      new StemSave(grouperSession).assignName("aStem:aStem2").save();
      

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_NAME_APPROXIMATE --stemName=aStem:a% --parentStemName=aStem  --ascending=T --sortString=name --pageNumber=1 --pageSize=2",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 2, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aStem0", matcher.group(2));
      
      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(output, "1", matcher.group(1));
      assertEquals(output, "aStem:aStem1", matcher.group(2));

      
      // #####################################################
      // run again, sort and page
      
      new StemSave(grouperSession).assignName("aStem:aStem1").save();
      new StemSave(grouperSession).assignName("aStem:aStem2").save();
      

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=findStemsWs --stemQueryFilterType=FIND_BY_PARENT_STEM_NAME --parentStemName=aStem  --ascending=T --sortString=name --pageNumber=2 --pageSize=2",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(output, 1, outputLines.length);
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(output, "0", matcher.group(1));
      assertEquals(output, "aStem:aStem2", matcher.group(2));
      

      
    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * try get members with a slash
   * Note: for this test to work, you need this in tomcat start:
   * -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true
   * @throws Exception
   */
  public void testGetMembersSlash() throws Exception {
    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup/1", null,
        "aStem:aGroup/1", "aGroup", null, null, true);

    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group.grantPriv(wsUser, AccessPrivilege.ADMIN, false);

    // add some subjects
    //group.addMember(SubjectTestHelper.SUBJ0, false);

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      //add member
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=addMemberWs --groupName=aStem:aGroup/1 --subjectIds=test.subject.0 --outputTemplate=Index${index}:success:${resultMetadata.success}:code:${resultMetadata.resultCode}:${wsSubject.id}:${wsAddMemberResults.wsGroupAssigned.name}$newline$${index}",
              " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
    
      System.setOut(systemOut);
    
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
      //match this: Index0:success:T:code:SUCCESS:test.subject.0:aStem:aGroup/1
      Pattern pattern = Pattern
          .compile("^Index(\\d+):success:T:code:([A-Z_]+?):(.+?):(.*+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);
    
      assertTrue(outputLines[0], matcher.matches());
    
      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "SUCCESS", matcher.group(2));
      assertEquals(outputLines[0], "test.subject.0", matcher.group(3));
      assertEquals(outputLines[0], "aStem:aGroup/1", matcher.group(4));
    
      
      //get members
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersWs --groupNames=aStem:aGroup/1",
          " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      // match: ^GroupIndex (\d+)\: success\: ([TF])\: code: ([A-Z_]+)\: group\:
      // (.*)\: subjectIndex\: (\d+)\: (.*)$
      pattern = Pattern
          .compile("^GroupIndex (\\d+)\\: success\\: ([TF])\\: code: ([A-Z_]+)\\: group\\: (.*)\\: subjectIndex\\: (\\d+)\\: (.*)$");
      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "T", matcher.group(2));
      assertEquals(outputLines[0], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[0], "aStem:aGroup/1", matcher.group(4));
      assertEquals(outputLines[0], "0", matcher.group(5));
      String subjectId = matcher.group(6);
      assertTrue(outputLines[0], GrouperClientUtils.equals("test.subject.0",
          subjectId));
    } finally {
      System.setOut(systemOut);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testGetMembers() throws Exception {

    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);
    Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null,
        "aStem:aGroup2", "aGroup2", null, null, true);

    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group2.grantPriv(wsUser, AccessPrivilege.READ, false);
    group2.grantPriv(wsUser, AccessPrivilege.VIEW, false);

    // add some subjects for PIT
    group.addMember(SubjectTestHelper.SUBJ4, false);
    group.deleteMember(SubjectTestHelper.SUBJ4, false);
    Thread.sleep(100);
    Timestamp pointInTimeFrom = new Timestamp(new Date().getTime());
    Thread.sleep(100);
    
    group.addMember(SubjectTestHelper.SUBJ5, false);
    group.deleteMember(SubjectTestHelper.SUBJ5, false);
    
    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);
    group2.addMember(SubjectTestHelper.SUBJ2, false);
    group2.addMember(SubjectTestHelper.SUBJ3, false);

    // add some subjects for PIT
    group.addMember(SubjectTestHelper.SUBJ6, false);
    group.deleteMember(SubjectTestHelper.SUBJ6, false);
    Thread.sleep(100);
    Timestamp pointInTimeTo = new Timestamp(new Date().getTime());
    Thread.sleep(100);
    
    group.addMember(SubjectTestHelper.SUBJ7, false);
    group.deleteMember(SubjectTestHelper.SUBJ7, false);
    ChangeLogTempToEntity.convertRecords();

    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {

      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());

      System.setOut(systemOut);

      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      // match: ^GroupIndex (\d+)\: success\: ([TF])\: code: ([A-Z_]+)\: group\:
      // (.*)\: subjectIndex\: (\d+)\: (.*)$
      Pattern pattern = Pattern
          .compile("^GroupIndex (\\d+)\\: success\\: ([TF])\\: code: ([A-Z_]+)\\: group\\: (.*)\\: subjectIndex\\: (\\d+)\\: (.*)$");
      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "T", matcher.group(2));
      assertEquals(outputLines[0], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[0], "aStem:aGroup", matcher.group(4));
      assertEquals(outputLines[0], "0", matcher.group(5));
      String subjectId = matcher.group(6);
      assertTrue(outputLines[0], GrouperClientUtils.equals("test.subject.0",
          subjectId)
          || GrouperClientUtils.equals("test.subject.1", subjectId));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(outputLines[1], "0", matcher.group(1));
      assertEquals(outputLines[1], "T", matcher.group(2));
      assertEquals(outputLines[1], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[1], "aStem:aGroup", matcher.group(4));
      assertEquals(outputLines[1], "1", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[1], GrouperClientUtils.equals("test.subject.0",
          subjectId)
          || GrouperClientUtils.equals("test.subject.1", subjectId));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals(outputLines[2], "1", matcher.group(1));
      assertEquals(outputLines[2], "T", matcher.group(2));
      assertEquals(outputLines[2], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[2], "aStem:aGroup2", matcher.group(4));
      assertEquals(outputLines[2], "0", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[2], GrouperClientUtils.equals("test.subject.2",
          subjectId)
          || GrouperClientUtils.equals("test.subject.3", subjectId));

      matcher = pattern.matcher(outputLines[3]);

      assertTrue(outputLines[3], matcher.matches());

      assertEquals(outputLines[3], "1", matcher.group(1));
      assertEquals(outputLines[3], "T", matcher.group(2));
      assertEquals(outputLines[3], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[3], "aStem:aGroup2", matcher.group(4));
      assertEquals(outputLines[3], "1", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[3], GrouperClientUtils.equals("test.subject.2",
          subjectId)
          || GrouperClientUtils.equals("test.subject.3", subjectId));

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // ######################################################
      // Try point in time
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      ArrayList<String> args = new ArrayList<String>();
      args.add("--operation=getMembersWs");
      args.add("--groupNames=aStem:aGroup,aStem:aGroup2");
      args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTimeFrom));
      args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTimeTo));
      GrouperClient.main(args.toArray(new String[0]));

      System.out.flush();
      output = new String(baos.toByteArray());
      System.setOut(systemOut);
      
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLines[0], matcher.matches());

      assertEquals(outputLines[0], "0", matcher.group(1));
      assertEquals(outputLines[0], "T", matcher.group(2));
      assertEquals(outputLines[0], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[0], "aStem:aGroup", matcher.group(4));
      assertEquals(outputLines[0], "0", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[0], GrouperClientUtils.equals("test.subject.0", subjectId) || 
          GrouperClientUtils.equals("test.subject.1", subjectId) ||
          GrouperClientUtils.equals("test.subject.5", subjectId) ||
          GrouperClientUtils.equals("test.subject.6", subjectId));

      matcher = pattern.matcher(outputLines[1]);

      assertTrue(outputLines[1], matcher.matches());

      assertEquals(outputLines[1], "0", matcher.group(1));
      assertEquals(outputLines[1], "T", matcher.group(2));
      assertEquals(outputLines[1], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[1], "aStem:aGroup", matcher.group(4));
      assertEquals(outputLines[1], "1", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[1], GrouperClientUtils.equals("test.subject.0", subjectId) || 
          GrouperClientUtils.equals("test.subject.1", subjectId) ||
          GrouperClientUtils.equals("test.subject.5", subjectId) ||
          GrouperClientUtils.equals("test.subject.6", subjectId));

      matcher = pattern.matcher(outputLines[2]);

      assertTrue(outputLines[2], matcher.matches());

      assertEquals(outputLines[2], "0", matcher.group(1));
      assertEquals(outputLines[2], "T", matcher.group(2));
      assertEquals(outputLines[2], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[2], "aStem:aGroup", matcher.group(4));
      assertEquals(outputLines[2], "2", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[2], GrouperClientUtils.equals("test.subject.0", subjectId) || 
          GrouperClientUtils.equals("test.subject.1", subjectId) ||
          GrouperClientUtils.equals("test.subject.5", subjectId) ||
          GrouperClientUtils.equals("test.subject.6", subjectId));

      matcher = pattern.matcher(outputLines[3]);

      assertTrue(outputLines[3], matcher.matches());

      assertEquals(outputLines[3], "0", matcher.group(1));
      assertEquals(outputLines[3], "T", matcher.group(2));
      assertEquals(outputLines[3], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[3], "aStem:aGroup", matcher.group(4));
      assertEquals(outputLines[3], "3", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[3], GrouperClientUtils.equals("test.subject.0", subjectId) || 
          GrouperClientUtils.equals("test.subject.1", subjectId) ||
          GrouperClientUtils.equals("test.subject.5", subjectId) ||
          GrouperClientUtils.equals("test.subject.6", subjectId));
      
      matcher = pattern.matcher(outputLines[4]);

      assertTrue(outputLines[4], matcher.matches());

      assertEquals(outputLines[4], "1", matcher.group(1));
      assertEquals(outputLines[4], "T", matcher.group(2));
      assertEquals(outputLines[4], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[4], "aStem:aGroup2", matcher.group(4));
      assertEquals(outputLines[4], "0", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[4], GrouperClientUtils.equals("test.subject.2",
          subjectId)
          || GrouperClientUtils.equals("test.subject.3", subjectId));

      matcher = pattern.matcher(outputLines[5]);

      assertTrue(outputLines[5], matcher.matches());

      assertEquals(outputLines[5], "1", matcher.group(1));
      assertEquals(outputLines[5], "T", matcher.group(2));
      assertEquals(outputLines[5], "SUCCESS", matcher.group(3));
      assertEquals(outputLines[5], "aStem:aGroup2", matcher.group(4));
      assertEquals(outputLines[5], "1", matcher.group(5));
      subjectId = matcher.group(6);
      assertTrue(outputLines[5], GrouperClientUtils.equals("test.subject.2",
          subjectId)
          || GrouperClientUtils.equals("test.subject.3", subjectId));
      
      
      // ######################################################
      // Try a sourceId

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --sourceIds=jdbc,g:gsa",
          " "));

      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(4, GrouperUtil.length(outputLines));
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --sourceIds=g:gsa",
          " "));

      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(0, GrouperUtil.length(outputLines));
      
      
      // ######################################################
      // Try uuid

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      Group aGroup = GroupFinder.findByName(grouperSession, "aStem:aGroup", true);
      Group aGroup2 = GroupFinder.findByName(grouperSession, "aStem:aGroup2", true);
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembersWs --groupUuids=" + aGroup.getUuid() + "," + aGroup2.getUuid(),
          " "));

      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(4, GrouperUtil.length(outputLines));
      
      
      // ######################################################
      // Try a subject attribute name with custom template

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --subjectAttributeNames=a,name --outputTemplate=${wsSubject.getAttributeValue(1)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      Arrays.sort(outputLines);

      assertEquals(outputLines[0], "my name is test.subject.0");
      assertEquals(outputLines[1], "my name is test.subject.1");
      assertEquals(outputLines[2], "my name is test.subject.2");
      assertEquals(outputLines[3], "my name is test.subject.3");

      // #######################################################
      // try member filter

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --memberFilter=Effective",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try member filter nonimmediate

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --memberFilter=NonImmediate",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try includeGroupDetail

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --includeGroupDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try includeSubjectDetail

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try subjectAttributeNames

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --subjectAttributeNames=name",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try params

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --paramName0=someParam --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("someParam")
              && GrouperClientWs.mostRecentRequest.toLowerCase().contains(
                  "params")
              && GrouperClientWs.mostRecentRequest.contains("someValue"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try fieldName

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --fieldName=members",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("fieldName")
              && GrouperClientWs.mostRecentRequest.contains("members"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // #######################################################
      // try actAsSubject

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --actAsSubjectId=GrouperSystem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubject")
              && GrouperClientWs.mostRecentRequest.contains("GrouperSystem"));

      // #######################################################
      // try actAsSubject but with alias

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --actAsPennId=GrouperSystem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());

      System.setOut(systemOut);

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubject")
              && GrouperClientWs.mostRecentRequest.contains("GrouperSystem"));

    } finally {
      System.setOut(systemOut);
    }

  }

  /**
   * @throws Exception
   */
  public void testGetGroups2() throws Exception {
  
    // make sure group exists
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject         subj  = SubjectTestHelper.SUBJ0;
    Subject         subj1  = SubjectTestHelper.SUBJ1;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Stem            eduSub   = edu.addChildStem("eduSub", "eduSub");
    Stem            edu2   = root.addChildStem("edu2", "edu2");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           i2sub    = eduSub.addChildGroup("i2sub", "i2sub");
    Group           edu2i2sub    = edu2.addChildGroup("edu2i2sub", "edu2i2sub");
    Group           comp1    = edu.addChildGroup("comp1", "comp1");
    Group           compLeft    = edu.addChildGroup("compLeft", "compRight");
    Group           compRight    = edu.addChildGroup("compRight", "compRight");
    
    comp1.addCompositeMember(CompositeType.INTERSECTION, compLeft, compRight);
    
    compLeft.addMember(subj);  
    compRight.addMember(subj);  
    
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    
    i2sub.addMember(subj1);
    edu2i2sub.addMember(subj1);
  
    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
  
    i2.grantPriv(wsUser, AccessPrivilege.READ, false);
    i2sub.grantPriv(wsUser, AccessPrivilege.READ, false);
    edu2i2sub.grantPriv(wsUser, AccessPrivilege.READ, false);
    comp1.grantPriv(wsUser, AccessPrivilege.READ, false);
    compLeft.grantPriv(wsUser, AccessPrivilege.READ, false);
    compRight.grantPriv(wsUser, AccessPrivilege.READ, false);
    uofc.grantPriv(wsUser, AccessPrivilege.READ, false);
  
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    //Set<Group> groups = member.getImmediateGroups();
    //assertEquals(3, groups.size());
    //assertTrue(groups.contains(compLeft));
    //assertTrue(groups.contains(compRight));
    //assertTrue(groups.contains(uofc));

    GrouperClient.main(GrouperClientUtils.splitTrim(
        "--operation=getGroupsWs --subjectIds=test.subject.0 --memberFilter=Immediate --sortString=name",
        " "));
    System.out.flush();
    String output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);

    String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(3, outputLines.length);
    
    Pattern pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    Matcher matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.0", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), compLeft.getName(), matcher.group(5));

    matcher = pattern.matcher(outputLines[1]);

    assertTrue(outputLines[1], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.0", matcher.group(3));
    assertEquals("1", matcher.group(4));
    assertEquals(matcher.group(5), compRight.getName(), matcher.group(5));

    matcher = pattern.matcher(outputLines[2]);
    
    assertTrue(outputLines[2], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.0", matcher.group(3));
    assertEquals("2", matcher.group(4));
    assertEquals(matcher.group(5), uofc.getName(), matcher.group(5));


    // #####################################################
    //groups = member.getNonImmediateGroups();
    //assertEquals(2, groups.size());
    //assertTrue(groups.contains(comp1));
    //assertTrue(groups.contains(i2));
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.0 --memberFilter=NonImmediate --sortString=name",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(2, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.0", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), comp1.getName(), matcher.group(5));

    matcher = pattern.matcher(outputLines[1]);

    assertTrue(outputLines[1], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.0", matcher.group(3));
    assertEquals("1", matcher.group(4));
    assertEquals(matcher.group(5), i2.getName(), matcher.group(5));
  
    // #####################################################
    //groups = member1.getEffectiveGroups();
    //assertEquals(0, groups.size());    
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Effective",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(0, GrouperClientUtils.length(outputLines));
    
    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), "whatever", null, null, null, true);
    //assertEquals(0, groups.size());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --fieldName=members --enabled=T --scope=whatever",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(0, GrouperClientUtils.length(outputLines));
    
    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), "edu:eduSub", null, null, null, true);
    //assertEquals(1, groups.size());
    //assertEquals("edu:eduSub:i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --scope=edu:eduSub",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(1, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.1", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), i2sub.getName(), matcher.group(5));

    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), "edu2", null, null, null, true);
    //assertEquals(1, groups.size());
    //assertEquals("edu2:edu2i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --scope=edu2",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(1, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.1", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), edu2i2sub.getName(), matcher.group(5));

    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), "edu2", null, null, null, false);
    //assertEquals(0, groups.size());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --fieldName=members --enabled=F --scope=edu2",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(0, GrouperClientUtils.length(outputLines));
    

    // #####################################################
    //try {
    //  groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, null, null, true);
    //  fail("Need stemScope");
    //} catch (Exception e) {
    //  //good
    //}
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    try {
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --stem=" + edu.getName(),
                  " "));
      fail("Need stemScope");
    } catch (Exception e) {
      //good
    } finally {
      System.out.flush();
  
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      System.out.println(output);
    }
    
    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, Scope.ONE, null, true);
    //assertEquals(0, groups.size());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --stemName=" + edu.getName() + " --stemScope=ONE_LEVEL",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(0, GrouperClientUtils.length(outputLines));

    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu2, Scope.ONE, null, true);
    //assertEquals(1, groups.size());
    //assertEquals("edu2:edu2i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --stemName=" + edu2.getName() + " --stemScope=ONE_LEVEL",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(1, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.1", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), edu2i2sub.getName(), matcher.group(5));
    
    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu, Scope.SUB, null, true);
    //assertEquals(1, groups.size());
    //assertEquals("edu:eduSub:i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --stemName=" + edu.getName() + " --stemScope=ALL_IN_SUBTREE",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(1, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.1", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), i2sub.getName(), matcher.group(5));

    // #####################################################
    //groups = member1.getImmediateGroups(Group.getDefaultList(), null, edu2, Scope.SUB, null, true);
    //assertEquals(1, groups.size());
    //assertEquals("edu2:edu2i2sub", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.1 --memberFilter=Immediate --stemName=" + edu2.getName() + " --stemScope=ALL_IN_SUBTREE",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(1, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.1", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), edu2i2sub.getName(), matcher.group(5));

    // #####################################################
    //QueryOptions queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("name");
    //groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
    //assertEquals(1, groups.size());
    //assertEquals("edu:compLeft", ((Group)GrouperUtil.get(groups, 0)).getName());
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    // test a command line template
    GrouperClient
        .main(GrouperClientUtils
            .splitTrim(
                "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.0 --memberFilter=Immediate --pageSize=1 --pageNumber=1 --sortString=name",
                " "));

    System.out.flush();

    output = new String(baos.toByteArray());

    System.setOut(systemOut);

    System.out.println(output);
    
    outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
    assertEquals(1, outputLines.length);
    
    pattern = Pattern
        .compile("^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
    matcher = pattern.matcher(outputLines[0]);

    assertTrue(outputLines[0], matcher.matches());

    assertEquals("0", matcher.group(1));
    assertEquals("SUCCESS", matcher.group(2));
    assertEquals("test.subject.0", matcher.group(3));
    assertEquals("0", matcher.group(4));
    assertEquals(matcher.group(5), compLeft.getName(), matcher.group(5));


    // #####################################################
    //queryOptions = new QueryOptions().paging(1, 1, true).sortAsc("non existent column");
    //try {
    //  groups = member.getImmediateGroups(Group.getDefaultList(), null, null, null, queryOptions, true);
    //  fail("Column doesnt exist");
    //} catch (Exception e) {
    //  //good
    //}
    
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {
      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.0 --memberFilter=Immediate --pageSize=1 --pageNumber=1 --sortString=doesntExist",
                  " "));
      fail("Column doesnt exist");
    } catch (Exception e) {
      //good
    } finally {
      System.out.flush();
  
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      System.out.println(output);
    }    
  
  }

  /**
   * @throws Exception
   */
  public void testGetMemberships() throws Exception {
  
    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);
    Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null,
        "aStem:aGroup2", "aGroup2", null, null, true);
  
    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
  
    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group2.grantPriv(wsUser, AccessPrivilege.READ, false);
    group2.grantPriv(wsUser, AccessPrivilege.VIEW, false);
  
    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);
    group2.addMember(SubjectTestHelper.SUBJ2, false);
    group2.addMember(SubjectTestHelper.SUBJ3, false);
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: group: aStem:aGroup, subject: GrouperSystem, list: members, type: immediate, enabled: T
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: group\\: (.+), subject\\: (.+), list: (.+), type\\: (.+), enabled\\: (T|F)$");
      String outputLine = outputLines[0];

      Matcher matcher = pattern.matcher(outputLines[0]);

      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "aStem:aGroup", matcher.group(2));
      assertEquals(outputLine, "test.subject.0", matcher.group(3));
      assertEquals(outputLine, "members", matcher.group(4));
      assertEquals(outputLine, "immediate", matcher.group(5));
      assertEquals(outputLine, "T", matcher.group(6));
      
      outputLine = outputLines[1];

      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "aStem:aGroup", matcher.group(2));
      assertEquals(outputLine, "test.subject.1", matcher.group(3));
      assertEquals(outputLine, "members", matcher.group(4));
      assertEquals(outputLine, "immediate", matcher.group(5));
      assertEquals(outputLine, "T", matcher.group(6));
  
      outputLine = outputLines[2];

      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "2", matcher.group(1));
      assertEquals(outputLine, "aStem:aGroup2", matcher.group(2));
      assertEquals(outputLine, "test.subject.2", matcher.group(3));
      assertEquals(outputLine, "members", matcher.group(4));
      assertEquals(outputLine, "immediate", matcher.group(5));
      assertEquals(outputLine, "T", matcher.group(6));

      outputLine = outputLines[3];

      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "3", matcher.group(1));
      assertEquals(outputLine, "aStem:aGroup2", matcher.group(2));
      assertEquals(outputLine, "test.subject.3", matcher.group(3));
      assertEquals(outputLine, "members", matcher.group(4));
      assertEquals(outputLine, "immediate", matcher.group(5));
      assertEquals(outputLine, "T", matcher.group(6));

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // ######################################################
      // Try a sourceId
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --sourceIds=jdbc,g:gsa",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));

      //##########################################################
      //Try a source id with no results
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --sourceIds=g:gsa",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
      
      
      // ######################################################
      // Try uuid
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      Group aGroup = GroupFinder.findByName(grouperSession, "aStem:aGroup", true);
      Group aGroup2 = GroupFinder.findByName(grouperSession, "aStem:aGroup2", true);
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getMembershipsWs --groupUuids=" + aGroup.getUuid() + "," + aGroup2.getUuid(),
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
      
      
      // ######################################################
      // Try a subject attribute name with custom template
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --subjectAttributeNames=a,name --outputTemplate=${wsSubject.getAttributeValue(1)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      Arrays.sort(outputLines);
  
      assertEquals(outputLines[0], "my name is test.subject.0");
      assertEquals(outputLines[1], "my name is test.subject.1");
      assertEquals(outputLines[2], "my name is test.subject.2");
      assertEquals(outputLines[3], "my name is test.subject.3");
  
      // #######################################################
      // try member filter
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --memberFilter=Effective",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try member filter nonimmediate
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --memberFilter=NonImmediate",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try includeGroupDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --includeGroupDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try includeSubjectDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try subjectAttributeNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --subjectAttributeNames=name",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try params
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --paramName0=someParam --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("someParam")
              && GrouperClientWs.mostRecentRequest.toLowerCase().contains(
                  "params")
              && GrouperClientWs.mostRecentRequest.contains("someValue"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try fieldName
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --fieldName=members",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("fieldName")
              && GrouperClientWs.mostRecentRequest.contains("members"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try actAsSubject
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --actAsSubjectId=GrouperSystem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubject")
              && GrouperClientWs.mostRecentRequest.contains("GrouperSystem"));
  
      // #######################################################
      // try actAsSubject but with alias
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --actAsPennId=GrouperSystem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubject")
              && GrouperClientWs.mostRecentRequest.contains("GrouperSystem"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      
      // #######################################################
      // try enabled
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --enabled=F",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      // #######################################################
      // try membership ids
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --membershipIds=123",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      // #######################################################
      // try scope
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --scope=abc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      // #######################################################
      // try subjectSources
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --sourceIds=g:gsa",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      // #######################################################
      // try one level stem
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --stemName=aStem --stemScope=ONE_LEVEL",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("ONE_LEVEL"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      // #######################################################
      // try all in subtree
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --groupNames=aStem:aGroup,aStem:aGroup2 --stemName=aStem --stemScope=ALL_IN_SUBTREE",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(4, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("ALL_IN_SUBTREE"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      // #######################################################
      // try all in subtree
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getMembershipsWs --subjectIds=test.subject.0,test.subject.1",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(2, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("scope"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("membershipIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsStemLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("stemScope"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("ALL_IN_SUBTREE"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));

      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetSubjects() throws Exception {
  
    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", null, null, true);
    Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null,
        "aStem:aGroup2", "aGroup2", null, null, true);
  
    // give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
  
    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group2.grantPriv(wsUser, AccessPrivilege.READ, false);
    group2.grantPriv(wsUser, AccessPrivilege.VIEW, false);
  
    // add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);
    group2.addMember(SubjectTestHelper.SUBJ2, false);
    group2.addMember(SubjectTestHelper.SUBJ3, false);
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ1_ID,
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertEquals(2, outputLines.length);
      
      // match: Index: 0: subject: GrouperSystem
      // match: ^Index: (\d+)\: subject\: (.+)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: success: (T|F), code: (.+), subject\\: (.+)$");

      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      outputLine = outputLines[1];
  
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.1", matcher.group(4));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // ######################################################
      // Try sourceIds
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ1_ID + " --subjectSources=jdbc,g:gsa",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 2, GrouperUtil.length(outputLines));

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      //since this is a different source, it is first (g:gsa)
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "F", matcher.group(2));
      assertEquals(outputLine, "SUBJECT_NOT_FOUND", matcher.group(3));
      assertEquals(outputLine, "test.subject.1", matcher.group(4));
      
      outputLine = outputLines[1];
  
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(output, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // ######################################################
      // Try sourceId
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ1_ID + " --subjectSources=jdbc",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 2, GrouperUtil.length(outputLines));

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      outputLine = outputLines[1];
  
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.1", matcher.group(4));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      // ######################################################
      // Try sourceId not found
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ1_ID + " --subjectSources=g:gsa",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 2, GrouperUtil.length(outputLines));

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "F", matcher.group(2));
      assertEquals(outputLine, "SUBJECT_NOT_FOUND", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      outputLine = outputLines[1];
  
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "F", matcher.group(2));
      assertEquals(outputLine, "SUBJECT_NOT_FOUND", matcher.group(3));
      assertEquals(outputLine, "test.subject.1", matcher.group(4));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      
      //##########################################################
      //Try a source id with no results
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ1_ID + " --subjectSources=g:gsa,g:gsa",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 2, GrouperUtil.length(outputLines));

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "F", matcher.group(2));
      assertEquals(outputLine, "SUBJECT_NOT_FOUND", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      outputLine = outputLines[1];
  
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "F", matcher.group(2));
      assertEquals(outputLine, "SUBJECT_NOT_FOUND", matcher.group(3));
      assertEquals(outputLine, "test.subject.1", matcher.group(4));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      
      //##########################################################
      //Try a identifier
      
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --subjectIdentifiers=id.test.subject.0,id.test.subject.1",
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 2, GrouperUtil.length(outputLines));

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      outputLine = outputLines[1];
  
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
  
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.1", matcher.group(4));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));

      
      // ######################################################
      // Try filter by group
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      Group aGroup = GroupFinder.findByName(grouperSession, "aStem:aGroup", true);
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --groupUuid=" + aGroup.getUuid() + " --subjectIds=" + SubjectTestHelper.SUBJ5_ID + "," + SubjectTestHelper.SUBJ6_ID,
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 0, GrouperUtil.length(outputLines));
      
      // ######################################################
      // Try filter by group, success
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --groupUuid=" + aGroup.getUuid() + " --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ7_ID,
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
      
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("<wsGroupLookup><uuid>"));
      
      // ######################################################
      // Try filter by group, success
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getSubjectsWs --groupName=" + aGroup.getName() + " --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "," + SubjectTestHelper.SUBJ7_ID,
          " "));
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
      
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("<wsGroupLookup><groupName>"));
      
      // ######################################################
      // Try a subject attribute name with custom template
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + " --subjectAttributeNames=a,name --outputTemplate=${wsSubject.getAttributeValue(1)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
      
      assertEquals(1, outputLines.length);
      assertEquals(outputLines[0], "my name is test.subject.0");
  
      // #######################################################
      // try member filter
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --groupName=" + aGroup.getName() + " --subjectIds=" + SubjectTestHelper.SUBJ0_ID + " --memberFilter=Effective",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try member filter immediate
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --groupName=" + aGroup.getName() + " --subjectIds=" + SubjectTestHelper.SUBJ0_ID + " --memberFilter=Immediate",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "T", matcher.group(2));
      assertEquals(outputLine, "SUCCESS", matcher.group(3));
      assertEquals(outputLine, "test.subject.0", matcher.group(4));

      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try includeGroupDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --groupName=" + aGroup.getName() + " --subjectIds=" + SubjectTestHelper.SUBJ0_ID + " --includeGroupDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try includeSubjectDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "  --includeSubjectDetail=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try subjectAttributeNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "  --subjectAttributeNames=name",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try params
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "  --paramName0=someParam --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("someParam")
              && GrouperClientWs.mostRecentRequest.toLowerCase().contains(
                  "params")
              && GrouperClientWs.mostRecentRequest.contains("someValue"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try fieldName
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --groupName=aStem:aGroup --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "  --fieldName=members",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("fieldName")
              && GrouperClientWs.mostRecentRequest.contains("members"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
  
      // #######################################################
      // try actAsSubject
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "  --actAsSubjectId=GrouperSystem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubject")
              && GrouperClientWs.mostRecentRequest.contains("GrouperSystem"));
  
      // #######################################################
      // try actAsSubject but with alias
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --subjectIds=" + SubjectTestHelper.SUBJ0_ID + "  --actAsPennId=GrouperSystem",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(1, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubject")
              && GrouperClientWs.mostRecentRequest.contains("GrouperSystem"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
  
      // #######################################################
      // try searchString
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --searchString=test",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertTrue(output, 8 < GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));
  
      
      // #######################################################
      // try searchString
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --searchString=test --sourceIds=jdbc",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertTrue(output, 8 < GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));
  
      
      // #######################################################
      // try subjectSources
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=getSubjectsWs --searchString=test --sourceIds=g:gsa",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertEquals(output, 0, GrouperUtil.length(outputLines));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("memberFilter"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("fieldName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
      //subjectSources
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("sourceIds"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookup"));
  
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignGrouperPrivileges() throws Exception {
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --subjectIds=test.subject.0 --privilegeNames=optin --allowed=true",
                  " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      Pattern pattern = Pattern
          .compile("^Index: ([0-9]+), success: (T|F), code: (.+), (group|stem): (.*), subject: (.+), (.+): (.+)$");
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertEquals(output, 1, GrouperClientUtils.length(outputLines));
      assertTrue(outputLines[0], matcher.matches());
  
      assertEquals("0", matcher.group(1));
      assertEquals("T", matcher.group(2));
      assertEquals("SUCCESS_ALLOWED", matcher.group(3));
      assertEquals("group", matcher.group(4));
      assertEquals("aStem:aGroup", matcher.group(5));
      assertEquals("test.subject.0", matcher.group(6));
      assertEquals("access", matcher.group(7));
      assertEquals("optin", matcher.group(8));
  
      // #####################################################
      // run again with subject identifier, and privilege type
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0 --privilegeType=access --privilegeNames=optin --allowed=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());
  
      assertEquals("0", matcher.group(1));
      assertEquals("T", matcher.group(2));
      assertEquals("SUCCESS_ALLOWED_ALREADY_EXISTED", matcher.group(3));
      assertEquals("group", matcher.group(4));
      assertEquals("aStem:aGroup", matcher.group(5));
      assertEquals("test.subject.0", matcher.group(6));
      assertEquals("access", matcher.group(7));
      assertEquals("optin", matcher.group(8));
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("access")
              && GrouperClientWs.mostRecentRequest.contains("privilegeType")
              && GrouperClientWs.mostRecentRequest
                  .contains("id.test.subject.0"));
  
      // #####################################################
      // run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      // test a command line template
      try {
        GrouperClient
            .main(GrouperClientUtils
                .splitTrim(
                    "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --subjectIds=test.subject.0 --privilegeNames=optin --allowed=true --ousdfsdfate=${index}",
                    " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();
  
      System.setOut(systemOut);
  
      // #####################################################
      // run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      // test a command line template
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0 --privilegeNames=optin --allowed=true --outputTemplate=${wsSubject.identifierLookup}",
                  " "));
  
      System.out.flush();
  
      output = new String(baos.toByteArray());
  
      assertEquals("id.test.subject.0", output);
  
      System.setOut(systemOut);
  
      // #####################################################
      // run again, with stem
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --stemName=aStem --pennKeys=id.test.subject.0 --privilegeNames=stem --allowed=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());
  
      assertEquals("0", matcher.group(1));
      assertEquals("T", matcher.group(2));
      assertEquals("SUCCESS_ALLOWED_ALREADY_EXISTED", matcher.group(3));
      assertEquals("stem", matcher.group(4));
      assertEquals("aStem", matcher.group(5));
      assertEquals("test.subject.0", matcher.group(6));
      assertEquals("naming", matcher.group(7));
      assertEquals("stem", matcher.group(8));
  
      // #####################################################
      // run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --subjectIds=test.subject.0 --includeGroupDetail=true --includeSubjectDetail=true --privilegeNames=optin --allowed=false",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());
  
      assertEquals("0", matcher.group(1));
      assertEquals("T", matcher.group(2));
      assertEquals("SUCCESS_NOT_ALLOWED", matcher.group(3));
      assertEquals("group", matcher.group(4));
      assertEquals("aStem:aGroup", matcher.group(5));
      assertEquals("test.subject.0", matcher.group(6));
      assertEquals("access", matcher.group(7));
      assertEquals("optin", matcher.group(8));

      assertTrue(GrouperClientWs.mostRecentRequest
          .contains("includeGroupDetail")
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
  
      // #####################################################
      // run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --subjectIds=test.subject.0 --subjectAttributeNames=name --privilegeNames=optin --allowed=false --outputTemplate=${index}:$space$${wsSubject.getAttributeValue(0)}$newline$",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      assertTrue(outputLines[0], outputLines[0]
          .contains("my name is test.subject.0"));
  
      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse
          .contains("my name is test.subject.0"));
  
      // #####################################################
      // run again, with params
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
  
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:aGroup --privilegeNames=optin --allowed=false --subjectIds=test.subject.0 --paramName0=whatever --paramValue0=someValue",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());
  
      assertEquals("0", matcher.group(1));
      assertEquals("T", matcher.group(2));
      assertEquals("SUCCESS_NOT_ALLOWED_DIDNT_EXIST", matcher.group(3));
      assertEquals("group", matcher.group(4));
      assertEquals("aStem:aGroup", matcher.group(5));
      assertEquals("test.subject.0", matcher.group(6));
      assertEquals("access", matcher.group(7));
      assertEquals("optin", matcher.group(8));

      assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
          && GrouperClientWs.mostRecentRequest.contains("someValue"));
  
      // #####################################################
      // run again, replace existing
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));

      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Group replaceGroup = new GroupSave(grouperSession).assignGroupNameToEdit("aStem:replaceExisting")
        .assignName("aStem:replaceExisting").assignCreateParentStemsIfNotExist(true).save();
      
      Set<Subject> subjects = grouperSession.getAccessResolver().getSubjectsWithPrivilege(replaceGroup, AccessPrivilege.UPDATE);

      assertEquals(0, GrouperUtil.length(subjects));
      
      replaceGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);
      
      GrouperClient
          .main(GrouperClientUtils
              .splitTrim(
                  "--operation=assignGrouperPrivilegesWs --groupName=aStem:replaceExisting --privilegeNames=update --allowed=true --subjectIds=test.subject.4 --replaceAllExisting=true",
                  " "));
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertEquals(GrouperClientUtils.length(outputLines), 1);
      assertTrue(outputLines[0], matcher.matches());
  
      assertEquals("0", matcher.group(1));
      assertEquals("T", matcher.group(2));
      assertEquals("SUCCESS_ALLOWED", matcher.group(3));
      assertEquals("group", matcher.group(4));
      assertEquals("aStem:replaceExisting", matcher.group(5));
      assertEquals("test.subject.4", matcher.group(6));
      assertEquals("access", matcher.group(7));
      assertEquals("update", matcher.group(8));

      subjects = grouperSession.getAccessResolver().getSubjectsWithPrivilege(replaceGroup, AccessPrivilege.UPDATE);

      assertEquals(1, GrouperUtil.length(subjects));
      assertEquals(SubjectTestHelper.SUBJ4_ID, subjects.iterator().next().getId());
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetAttributeAssignsGroup() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignAssignName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();
    
    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //test subject 0 can view and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 0 can read the assignment on assignment
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignResult attributeAssignResult2 = attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName2);
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeDefNames=test:testAttributeAssignDefNameDef",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName: (.+), action\\: (.+), values: (.+), enabled\\: (T|F), id: (.+)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try attributeDefId
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeDefUuids=" + attributeDef.getId(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try attributeDefNameName
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeDefNameNames=" + attributeDefName.getName(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      // ######################################################
      // Try attributeDefNameUuid
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeDefNameUuids=" + attributeDefName.getId(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try ownerGroupNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupNames=" + group.getName(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      
      // ######################################################
      // Try ownerGroupUuids
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      // ######################################################
      // Try enabled
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --enabled=F",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try actions
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --actions=a",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try includeAssignmentsOnAssignments
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --includeAssignmentsOnAssignments=T",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

//      assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));
      
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      outputLine = outputLines[1];
      
      matcher = pattern.matcher(outputLine);
  
      //Index: 1: attributeAssignType: group_asgn, owner: e11b0b9174ec474184e878ac3e5e27e3, attributeDefNameName: test:testAttributeAssignAssignName, 
      //action: assign, values: none, enabled: T, id: e11b0b9174ec474184e878ac3e5e27e3 expected:<test:groupTestAttrAssign> but was:<e11b0b9174ec474184e878ac3e5e27e3>
      
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "1", matcher.group(1));
      assertEquals(outputLine, "group_asgn", matcher.group(2));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignAssignName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign2.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      
      // ######################################################
      // Try includeGroupDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --includeGroupDetail=T",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("hasComposite"));
      
      
      
      
      // ######################################################
      // Try includeSubjectDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --includeSubjectDetail=T",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      
      
      // ######################################################
      // Try subjectAttributeNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --subjectAttributeNames=abc",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames") 
          && GrouperClientWs.mostRecentRequest.contains("abc"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      
      // ######################################################
      // Try params
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --ownerGroupUuids=" + group.getId()
          + " --paramName0=a --paramValue0=b",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      // ######################################################
      // Try attributeAssignLookups
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeAssignUuids=" + attributeAssign.getId(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
      // ######################################################
      // Try attributeAssignLookups custom template
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeAssignUuids=" + attributeAssign.getId()
          + " --outputTemplate=${wsAttributeAssign.attributeAssignType}$newline$",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = GrouperClientUtils.trim(outputLines[0]);
      
      assertEquals(outputLine, "group", outputLine);
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try actAs
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=group --attributeAssignUuids=" + attributeAssign.getId()
          + " --actAsSubjectId=GrouperSystem",
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetAttributeAssignsStem() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.store();

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
      
    AttributeAssignResult attributeAssignResult = stem.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=stem --attributeDefNames=test:testAttributeAssignDefNameDef",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: stem, owner: test:stemTestAttrAssign, attributeDefNameName test:testAttributeAssignDefName, action: assign, values: none, enable: T, id: a9c83eeb78c04ae5befcea36272d318c
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName: (.+), action\\: (.+), values: (.+), enabled\\: (T|F), id: (.+)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "stem", matcher.group(2));
      assertEquals(outputLine, "test:stemTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try wsOwnerStemUuids
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=stem --ownerStemUuids=" + stem.getUuid(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "stem", matcher.group(2));
      assertEquals(outputLine, "test:stemTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try wsOwnerStemNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=stem --ownerStemNames=" + stem.getName(),
          " "));

  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");

      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "stem", matcher.group(2));
      assertEquals(outputLine, "test:stemTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetAttributeAssignsMember() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    AttributeAssignResult attributeAssignResult = member.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=member --attributeDefNames=test:testAttributeAssignDefNameDef",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: member, owner: test:stemTestAttrAssign, attributeDefNameName test:testAttributeAssignDefName, action: assign, values: none, enable: T, id: a9c83eeb78c04ae5befcea36272d318c
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName: (.+), action\\: (.+), values: (.+), enabled\\: (T|F), id: (.+)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "member", matcher.group(2));
      assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      // ######################################################
      // Try wsOwnerMemberUuids
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=member --owner0SubjectId=" + member.getSubjectId(),
          " "));
  
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "member", matcher.group(2));
      assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
      // ######################################################
      // Try wsOwnerStemNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=member --owner0SubjectIdentifier=id.test.subject.0",
          " "));
  
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "member", matcher.group(2));
      assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetAttributeAssignsMembership() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
    
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
  
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=imm_mem --attributeDefNames=test:testAttributeAssignDefNameDef",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: member, owner: test:stemTestAttrAssign, attributeDefNameName test:testAttributeAssignDefName, action: assign, values: none, enable: T, id: a9c83eeb78c04ae5befcea36272d318c
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName: (.+), action\\: (.+), values: (.+), enabled\\: (T|F), id: (.+)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "imm_mem", matcher.group(2));
      assertEquals(outputLine, membership.getImmediateMembershipId(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      // ######################################################
      // Try wsOwnerMemberUuids
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=imm_mem --ownerMembershipUuids=" + membership.getUuid(),
          " "));
  
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "imm_mem", matcher.group(2));
      assertEquals(outputLine, membership.getImmediateMembershipId(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetAttributeAssignsAnyMembership() throws Exception {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign").assignName("test:anyMembershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign2").assignName("test:anyMembershipTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //add one group to another to make effective membership and add attribute to that membership
    group1.addMember(group2.toSubject());
    group2.addMember(SubjectTestHelper.SUBJ0);    
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);

    Membership membership = (Membership)MembershipFinder.findMemberships(GrouperUtil.toSet(group1.getId()), 
        GrouperUtil.toSet(member.getUuid()), null, null, FieldFinder.find("members", true), null, null, null, null, null).iterator().next()[0];
    
    AttributeAssignResult attributeAssignResult = membership.getAttributeDelegateEffMship().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
  
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=any_mem --attributeDefNames=test:testAttributeAssignDefNameDef",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: any_mem, owner: test:stemTestAttrAssign, attributeDefNameName test:testAttributeAssignDefName, action: assign, values: none, enable: T, id: a9c83eeb78c04ae5befcea36272d318c
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName: (.+), action\\: (.+), values: (.+), enabled\\: (T|F), id: (.+)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "any_mem", matcher.group(2));
      assertEquals(outputLine, "test:anyMembershipTestAttrAssign - jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      // ######################################################
      // Try wsOwnerMemberAnyLookup
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=any_mem --ownerMembershipAny0SubjectId=" + member.getSubjectId()
          + " --ownerMembershipAny0GroupName=" + group1.getName(),
          " "));
  
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "any_mem", matcher.group(2));
      assertEquals(outputLine, "test:anyMembershipTestAttrAssign - jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testGetAttributeAssignsAttributeDef() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDef attributeDefAssignTo = AttributeDefTest.exampleAttributeDefDb("test", "testAttributeDefAssignTo");
    
    AttributeAssignResult attributeAssignResult = attributeDefAssignTo.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=attr_def --attributeDefNames=test:testAttributeAssignDefNameDef",
          " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: stem, owner: test:stemTestAttrAssign, attributeDefNameName test:testAttributeAssignDefName, action: assign, values: none, enable: T, id: a9c83eeb78c04ae5befcea36272d318c
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName: (.+), action\\: (.+), values: (.+), enabled\\: (T|F), id: (.+)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "attr_def", matcher.group(2));
      assertEquals(outputLine, "test:testAttributeDefAssignTo", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      // ######################################################
      // Try wsAttrDefUuids
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=getAttributeAssignmentsWs --attributeAssignType=attr_def --ownerAttributeDefUuids=" + attributeDefAssignTo.getUuid(),
          " "));
  
  
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
      
      matcher = pattern.matcher(outputLine);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "attr_def", matcher.group(2));
      assertEquals(outputLine, "test:testAttributeDefAssignTo", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("enabled"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
      
      
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesGroup() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.setMultiValued(true);
    attributeDef.store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //test subject 0 can view and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    AttributeAssign attributeAssign = null;
    //    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    //     = attributeAssignResult.getAttributeAssign();
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), " +
        		"values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
      // ######################################################
      // Try attributeDefNameId

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameUuids=" + attributeDefName.getId() +  " --ownerGroupNames=test:groupTestAttrAssign",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeDefName.getId()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try ownerGroupUuid

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupUuids=" + group.getUuid(),
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains(group.getUuid()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try enabledTime

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --assignmentEnabledTime=2010/03/05_17:05:13.123",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime") 
          && GrouperClientWs.mostRecentRequest.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try disabledTime

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --assignmentDisabledTime=2010/03/05_17:05:13.123",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "F", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime") 
          && GrouperClientWs.mostRecentRequest.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try assignmentNotes

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --assignmentNotes=theNotes",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("assignmentNotes") 
          && GrouperClientWs.mostRecentRequest.contains("theNotes") );
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
      // ######################################################
      // Try delegatable

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --delegatable=FALSE",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      //assertTrue(GrouperClientWs.mostRecentResponse,
      //    GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("delegatable")
          && GrouperClientWs.mostRecentRequest.contains("FALSE"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try actions

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --actions=assign",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      //assertTrue(GrouperClientWs.mostRecentResponse,
      //    GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // Try values

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --values0System=3 --values1System=4 --values2System=5 --attributeAssignValueOperation=replace_values",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "3,4,5", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "T", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      //assertTrue(GrouperClientWs.mostRecentResponse,
      //    GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("values")
          && GrouperClientWs.mostRecentRequest.contains(">3<")
          && GrouperClientWs.mostRecentRequest.contains(">4<")
          && GrouperClientWs.mostRecentRequest.contains(">5<"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // includeGroupDetail

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --includeGroupDetail=T",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "3,4,5", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));


      // ######################################################
      // includeSubjectDetail

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --includeSubjectDetail=T",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "3,4,5", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
      // ######################################################
      // subjectAttributeNames

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --subjectAttributeNames=abc",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "3,4,5", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
      // ######################################################
      // params

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerGroupNames=test:groupTestAttrAssign "
          + " --paramName0=a --paramValue0=b",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "3,4,5", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
      // ######################################################
      // attribute assign lookups

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);

      String attributeAssignId = attributeAssign.getId();
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=remove_attr "
          + " --attributeAssignUuids=" + attributeAssign.getId(),
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);

      assertNull("Should be deleted", attributeAssign);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssignId, matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "T", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      // ######################################################
      // attribute assign lookups custom tempflate

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();

      attributeAssignId = attributeAssign.getId();
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=remove_attr "
          + " --attributeAssignUuids=" + attributeAssign.getId() + " --outputTemplate=${wsAttributeAssign.attributeAssignType}$newline$",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);

      assertNull("Should be deleted", attributeAssign);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = GrouperClientUtils.trim(outputLines[0]);
      
      assertEquals(outputLine, "group", outputLine);      

      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
      // ######################################################
      // attribute assign lookups

      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();

      attributeAssignId = attributeAssign.getId();
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=group --attributeAssignOperation=remove_attr "
          + " --attributeAssignUuids=" + attributeAssign.getId() + " --actAsSubjectId=GrouperSystem",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);

      assertNull("Should be deleted", attributeAssign);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "group", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssignId, matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "T", matcher.group(10));
      assertEquals(outputLine, "F", matcher.group(11));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));


      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesStem() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToStem(true);
    attributeDef.store();

    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("test:stemTestAttrAssign").assignName("test:stemTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
      
  
    AttributeAssign attributeAssign = null;
    //    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    //     = attributeAssignResult.getAttributeAssign();
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=stem --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerStemNames=test:stemTestAttrAssign",
          " "));
      
      attributeAssign = stem.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "stem", matcher.group(2));
      assertEquals(outputLine, "test:stemTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:stemTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      // stem uuids
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=stem --attributeAssignOperation=assign_attr " +
          "--attributeDefNameUuids=" + attributeDefName.getId() +  " --ownerStemUuids=" + stem.getUuid(),
          " "));
      
      attributeAssign = stem.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "stem", matcher.group(2));
      assertEquals(outputLine, "test:stemTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains(stem.getUuid()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups")
          && GrouperClientWs.mostRecentRequest.contains(stem.getUuid()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));

      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesMember() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToMember(true);
    attributeDef.store();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      
  
    AttributeAssign attributeAssign = null;
    //    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    //     = attributeAssignResult.getAttributeAssign();
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=member --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --owner0SubjectId=" + SubjectTestHelper.SUBJ0_ID,
          " "));
      
      attributeAssign = member.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "member", matcher.group(2));
      assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups")
          && GrouperClientWs.mostRecentRequest.contains(SubjectTestHelper.SUBJ0_ID));
  
      // subject identifier
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=member --attributeAssignOperation=assign_attr " +
          "--attributeDefNameUuids=" + attributeDefName.getId() +  " --owner0SubjectIdentifier=id.test.subject.0",
          " "));
      
      attributeAssign = member.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "member", matcher.group(2));
      assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeDefName.getId()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups")
          && GrouperClientWs.mostRecentRequest.contains("id.test.subject.0"));
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesAttributeDef() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    
    AttributeDef attributeDefAssignTo = AttributeDefTest.exampleAttributeDefDb("test", "testAttributeDefAssignTo");
    
  
    AttributeAssign attributeAssign = null;
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=attr_def --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerAttributeDefNames=test:testAttributeDefAssignTo",
          " "));
      
      attributeAssign = attributeDefAssignTo.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "attr_def", matcher.group(2));
      assertEquals(outputLine, attributeDefAssignTo.getName(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeDefAssignTo.getName()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      // attribute def uuid
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=attr_def --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerAttributeDefUuids=" + attributeDefAssignTo.getUuid(),
          " "));
      
      attributeAssign = attributeDefAssignTo.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "attr_def", matcher.group(2));
      assertEquals(outputLine, attributeDefAssignTo.getName(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeDefAssignTo.getId()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeDefAssignTo.getUuid()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesMembership() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
    
    
  
    AttributeAssign attributeAssign = null;
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=imm_mem --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerMembershipUuids=" + membership.getUuid(),
          " "));
      
      attributeAssign = membership.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "imm_mem", matcher.group(2));
      assertEquals(outputLine, membership.getImmediateMembershipId(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups")
          && GrouperClientWs.mostRecentRequest.contains(membership.getUuid()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesAnyMembership() throws Exception {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign").assignName("test:anyMembershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign2").assignName("test:anyMembershipTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //add one group to another to make effective membership and add attribute to that membership
    group1.addMember(group2.toSubject());
    group2.addMember(SubjectTestHelper.SUBJ0);    
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);

    Membership membership = (Membership)MembershipFinder.findMemberships(GrouperUtil.toSet(group1.getId()), 
        GrouperUtil.toSet(member.getUuid()), null, null, FieldFinder.find("members", true), null, null, null, null, null).iterator().next()[0];
    
  
    AttributeAssign attributeAssign = null;
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=any_mem --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName --ownerMembershipAny0SubjectId=" + member.getSubjectId()
          + " --ownerMembershipAny0GroupName=" + group1.getName(),
          " "));
      
      attributeAssign = membership.getAttributeDelegateEffMship().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "any_mem", matcher.group(2));
      assertEquals(outputLine, "test:anyMembershipTestAttrAssign - jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups")
          && GrouperClientWs.mostRecentRequest.contains(group1.getName())
          && GrouperClientWs.mostRecentRequest.contains(member.getSubjectId()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignAttributesMembershipAssn() throws Exception {
  
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
  
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignDefName2");
    
    final AttributeDef attributeDef2 = attributeDefName2.getAttributeDef();

    attributeDef2.setAssignToGroup(false);
    attributeDef2.setAssignToImmMembershipAssn(true);
    attributeDef2.store();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTestAttrAssign").assignName("test:membershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    group1.addMember(SubjectTestHelper.SUBJ0);
    
    Membership membership = group1.getMemberships(FieldFinder.find("members", true)).iterator().next();
    
    AttributeAssign attributeAssign = membership.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    
    //we need to wait some seconds for the cache to clear
    GrouperUtil.sleep(20000);

    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignAttributesWs --attributeAssignType=imm_mem_asgn --attributeAssignOperation=assign_attr " +
          "--attributeDefNameNames=test:testAttributeAssignDefName2 --ownerAttributeAssignUuids=" + attributeAssign.getId(),
          " "));
      
      AttributeAssign attributeAssign2 = attributeAssign.getAttributeDelegate().retrieveAssignment("assign", attributeDefName2, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: attributeAssignType\\: (.+), owner\\: (.+), attributeDefNameName\\: (.+), action\\: (.+), values\\: (.+), enabled\\: (T|F), id\\: (.+), changed\\: (T|F), deleted\\: (T|F), valuesChanged\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "imm_mem_asgn", matcher.group(2));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName2", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "none", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign2.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      assertEquals(outputLine, "F", matcher.group(10));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("attributeAssignType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("values"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeAssignLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeAssign.getId()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerAttributeDefLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerGroupLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipAnyLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerMembershipLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerStemLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsOwnerSubjectLookups"));
  
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
     * @throws Exception
     */
    public void testGetPermissionAssigns() throws Exception {
    
      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem aStem = Stem.saveStem(grouperSession, "aStem", null,"aStem", "a stem",  "a stem description", null, false);

      //parent implies child
      Role role = aStem.addChildRole("role", "role");
      Role role2 = aStem.addChildRole("role2", "role2");
          
      ((Group)role).addMember(SubjectTestHelper.SUBJ0);    
      ((Group)role2).addMember(SubjectTestHelper.SUBJ1);    
      
      AttributeDef permissionDef = aStem.addChildAttributeDef("permissionDef", AttributeDefType.perm);
      permissionDef.setAssignToEffMembership(true);
      permissionDef.setAssignToGroup(true);
      permissionDef.store();
      AttributeDefName permissionDefName = aStem.addChildAttributeDefName(permissionDef, "permissionDefName", "permissionDefName");
      AttributeDefName permissionDefName2 = aStem.addChildAttributeDefName(permissionDef, "permissionDefName2", "permissionDefName2");

      permissionDef.getAttributeDefActionDelegate().addAction("action");
      permissionDef.getAttributeDefActionDelegate().addAction("action2");
      
      //subject 0 has a "role" permission of permissionDefName with "action" in 
      //subject 1 has a "role_subject" permission of permissionDefName2 with action2
      
      role.getPermissionRoleDelegate().assignRolePermission("action", permissionDefName);
      role2.getPermissionRoleDelegate()
        .assignSubjectRolePermission("action2", permissionDefName2, SubjectTestHelper.SUBJ1);
    
      PrintStream systemOut = System.out;
    
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      try {
    
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --attributeDefNames=aStem:permissionDef",
            " "));
        System.out.flush();
        String output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");

        // match: Index: 0: permissionType: role, role: aStem:role, subject: jdbc - test.subject.0, attributeDefNameName: aStem:permissionDefName, action: action, allowedOverall: T, enabled: T
        // match: ^Index: (\d+)\: permissionType\: (.+), role\: (.+), subject\: (.+), attributeDefNameName\: (.+), action\: (.+), allowedOverall: (T|F), enabled\: (T|F)
        Pattern pattern = Pattern
            .compile("^Index: (\\d+)\\: permissionType\\: (.+), role\\: (.+), subject\\: (.+), attributeDefNameName: (.+), action\\: (.+), allowedOverall\\: (.+), enabled\\: (T|F)$");
        
        assertEquals(2, outputLines.length);
        String outputLine = outputLines[0];
    
        Matcher matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        outputLine = outputLines[1];
        
        matcher = pattern.matcher(outputLines[1]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "1", matcher.group(1));
        assertEquals(outputLine, "role_subject", matcher.group(2));
        assertEquals(outputLine, "aStem:role2", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.1", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName2", matcher.group(5));
        assertEquals(outputLine, "action2", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        
        
  
        // ######################################################
        // Try attributeDefId
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --attributeDefUuids=" + permissionDef.getId(),
            " "));
  
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(2, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        outputLine = outputLines[1];
        
        matcher = pattern.matcher(outputLines[1]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "1", matcher.group(1));
        assertEquals(outputLine, "role_subject", matcher.group(2));
        assertEquals(outputLine, "aStem:role2", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.1", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName2", matcher.group(5));
        assertEquals(outputLine, "action2", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        
        
        // ######################################################
        // Try attributeDefNameName
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --attributeDefNameNames=" + permissionDefName.getName(),
            " "));
  
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));


        // ######################################################
        // Try attributeDefNameUuid
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --attributeDefNameUuids=" + permissionDefName.getId(),
            " "));
  
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));


        // ######################################################
        // Try roleName
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName(),
            " "));
  
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));


        // ######################################################
        // Try roleUuid
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleUuids=" + role.getId(),
            " "));
  
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        // ######################################################
        // Try roleName and enabled
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --enabled=F",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and actions
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --actions=a",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeAttributeAssignments
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --includeAttributeAssignments=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("</WsAttributeAssign>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeAttributeAssignments and includeAssignmentsOnAssignments
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --includeAttributeAssignments=T --includeAssignmentsOnAssignments=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("</WsAttributeAssign>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<attributeDefNameSetDepth>"));

        // ######################################################
        // Try roleName and includePermissionAssignDetail

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --includePermissionAssignDetail=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("<attributeDefNameSetDepth>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<WsAttributeDefName>"));

        // ######################################################
        // Try roleName and includeAttributeDefNames

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --includeAttributeDefNames=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("<WsAttributeDefName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeGroupDetail

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --includeGroupDetail=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeSubjectDetail

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --includeSubjectDetail=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeSubjectDetail

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --subjectAttributeNames=abc",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames") && GrouperClientWs.mostRecentRequest.contains("abc"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and params

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --paramName0=a --paramValue0=b",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        assertEquals(outputLine, "T", matcher.group(7));
        assertEquals(outputLine, "T", matcher.group(8));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and custom template

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --outputTemplate=${wsPermissionAssign.permissionType}$newline$",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0].trim();
    
        assertEquals(outputLine, "role", outputLine);

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and act as subject

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --actAsSubjectId=" + SubjectTestHelper.SUBJ0_ID,
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(0, GrouperUtil.length(outputLines));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("<immediateOnly>T</immediateOnly>"));

        // ######################################################
        // Try roleName and immediateOnly

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --immediateOnly=T",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(0, GrouperUtil.length(outputLines));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<immediateOnly>T</immediateOnly>"));
        
        // ######################################################
        // Try roleName and permissionType

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --permissionType=role_subject",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, GrouperUtil.length(outputLines));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("<immediateOnly>T</immediateOnly>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<permissionType>role_subject</permissionType>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("<permissionProcessor>role_subject</permissionProcessor>"));

        // ######################################################
        // Try roleName and permissionProcessor

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getPermissionAssignmentsWs --roleNames=" + role.getName() + " --permissionProcessor=FILTER_REDUNDANT_PERMISSIONS",
            " "));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, GrouperUtil.length(outputLines));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("<immediateOnly>T</immediateOnly>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("<permissionType>role_subject</permissionType>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<permissionProcessor>FILTER_REDUNDANT_PERMISSIONS</permissionProcessor>"));

      } finally {
        System.setOut(systemOut);
      }
    
    }
    
    /**
     * @throws Exception
     */
    public void testGetPermissionAssignsPIT() throws Exception {
    
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem aStem = Stem.saveStem(grouperSession, "aStem", null,"aStem", "a stem",  "a stem description", null, false);

      //parent implies child
      Role role = aStem.addChildRole("role", "role");
      Role role2 = aStem.addChildRole("role2", "role2");
          
      ((Group)role).addMember(SubjectTestHelper.SUBJ0);    
      ((Group)role2).addMember(SubjectTestHelper.SUBJ1);    
      
      AttributeDef permissionDef = aStem.addChildAttributeDef("permissionDef", AttributeDefType.perm);
      permissionDef.setAssignToEffMembership(true);
      permissionDef.setAssignToGroup(true);
      permissionDef.store();
      AttributeDefName permissionDefName = aStem.addChildAttributeDefName(permissionDef, "permissionDefName", "permissionDefName");
      AttributeDefName permissionDefName2 = aStem.addChildAttributeDefName(permissionDef, "permissionDefName2", "permissionDefName2");

      permissionDef.getAttributeDefActionDelegate().addAction("action");
      permissionDef.getAttributeDefActionDelegate().addAction("action2");
      
      Thread.sleep(100);
      Timestamp before = new Timestamp(new Date().getTime());
      Thread.sleep(100);
      
      //subject 0 has a "role" permission of permissionDefName with "action" in 
      //subject 1 has a "role_subject" permission of permissionDefName2 with action2
      
      AttributeAssignResult result1 = role.getPermissionRoleDelegate().assignRolePermission("action", permissionDefName);
      AttributeAssignResult result2 = role2.getPermissionRoleDelegate()
        .assignSubjectRolePermission("action2", permissionDefName2, SubjectTestHelper.SUBJ1);
      ChangeLogTempToEntity.convertRecords();

      Thread.sleep(100);
      Timestamp pointInTime = new Timestamp(new Date().getTime());
      Thread.sleep(100);
      
      // delete the permissions...
      result1.getAttributeAssign().delete();
      result2.getAttributeAssign().delete();
      ChangeLogTempToEntity.convertRecords();
      
      Thread.sleep(100);
      Timestamp after = new Timestamp(new Date().getTime());
      Thread.sleep(100);
      
      PrintStream systemOut = System.out;
    
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      try {
    
        ArrayList<String> args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--attributeDefNames=aStem:permissionDef");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
        
        System.out.flush();
        String output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
    
        // match: Index: 0: permissionType: role, role: aStem:role, subject: jdbc - test.subject.0, attributeDefNameName: aStem:permissionDefName, action: action, enabled: null
        // match: ^Index: (\d+)\: permissionType\: (.+), role\: (.+), subject\: (.+), attributeDefNameName\: (.+), action\: (.+), enabled\: null
        Pattern pattern = Pattern
            .compile("^Index: (\\d+)\\: permissionType\\: (.+), role\\: (.+), subject\\: (.+), attributeDefNameName: (.+), action\\: (.+), allowedOverall\\: T|F enabled\\: null$");
        
        assertEquals(2, outputLines.length);
        String outputLine = outputLines[0];
    
        Matcher matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        outputLine = outputLines[1];
        
        matcher = pattern.matcher(outputLines[1]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "1", matcher.group(1));
        assertEquals(outputLine, "role_subject", matcher.group(2));
        assertEquals(outputLine, "aStem:role2", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.1", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName2", matcher.group(5));
        assertEquals(outputLine, "action2", matcher.group(6));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        
        
  
        // ######################################################
        // Try attributeDefId
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--attributeDefUuids=" + permissionDef.getId());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(2, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        outputLine = outputLines[1];
        
        matcher = pattern.matcher(outputLines[1]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "1", matcher.group(1));
        assertEquals(outputLine, "role_subject", matcher.group(2));
        assertEquals(outputLine, "aStem:role2", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.1", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName2", matcher.group(5));
        assertEquals(outputLine, "action2", matcher.group(6));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        
        
        // ######################################################
        // Try attributeDefNameName
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--attributeDefNameNames=" + permissionDefName.getName());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups") && GrouperClientWs.mostRecentRequest.contains("<name>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));


        // ######################################################
        // Try attributeDefNameUuid
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--attributeDefNameUuids=" + permissionDefName.getId());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("roleLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));


        // ######################################################
        // Try roleName
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));


        // ######################################################
        // Try roleUuid
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleUuids=" + role.getId());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
    
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<uuid>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        // ######################################################
        // Try roleName and actions
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--actions=a");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeAttributeAssignments
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--includeAttributeAssignments=T");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("</WsAttributeAssign>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeAttributeAssignments and includeAssignmentsOnAssignments
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--includeAttributeAssignments=T");
        args.add("--includeAssignmentsOnAssignments=T");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("</WsAttributeAssign>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<attributeDefNameSetDepth>"));

        // ######################################################
        // Try roleName and includePermissionAssignDetail

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--includePermissionAssignDetail=T");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("<attributeDefNameSetDepth>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<WsAttributeDefName>"));

        // ######################################################
        // Try roleName and includeAttributeDefNames

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--includeAttributeDefNames=T");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            GrouperClientWs.mostRecentResponse.contains("<WsAttributeDefName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and includeSubjectDetail

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--includeSubjectDetail=T");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and subjectAttributeNames

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--subjectAttributeNames=abc");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames") && GrouperClientWs.mostRecentRequest.contains("abc"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and params

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--paramName0=a");
        args.add("--paramValue0=b");
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));
        
        // ######################################################
        // Try roleName and actAsSubjectId

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--actAsSubjectId=" + SubjectTestHelper.SUBJ0_ID);
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(pointInTime));
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(pointInTime));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        System.setOut(systemOut);
        
        
        assertEquals(0, GrouperUtil.length(outputLines));

        
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("actions"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("enabled"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAssignmentsOnAssignments"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeDefNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeAttributeAssignments"));
        assertTrue(GrouperClientWs.mostRecentResponse,
            !GrouperClientWs.mostRecentResponse.contains("<hasComposite>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includePermissionAssignDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("params"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookups"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("roleLookups") && GrouperClientWs.mostRecentRequest.contains("<groupName>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            !GrouperClientWs.mostRecentRequest.contains("wsSubjectLookups"));

        
        // ######################################################
        // Try roleName and pointInTimeFrom only
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(after));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));
    
        // ######################################################
        // Try roleName and pointInTimeFrom only
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--pointInTimeFrom=" + GrouperClientUtils.timestampToString(before));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        
        // ######################################################
        // Try roleName and pointInTimeTo only
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(before));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertTrue(outputLines == null || outputLines.length == 0 || StringUtils.isBlank(outputLines[0]));
    
        // ######################################################
        // Try roleName and pointInTimeTo only
    
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        args = new ArrayList<String>();
        args.add("--operation=getPermissionAssignmentsWs");
        args.add("--roleNames=" + role.getName());
        args.add("--pointInTimeTo=" + GrouperClientUtils.timestampToString(after));
        GrouperClient.main(args.toArray(new String[0]));
  
        System.out.flush();
        output = new String(baos.toByteArray());
    
        System.setOut(systemOut);
    
        outputLines = GrouperClientUtils.splitTrim(output, "\n");

        assertEquals(1, outputLines.length);
        outputLine = outputLines[0];
    
        matcher = pattern.matcher(outputLines[0]);
    
        assertTrue(outputLine, matcher.matches());
        assertEquals(outputLine, "0", matcher.group(1));
        assertEquals(outputLine, "role", matcher.group(2));
        assertEquals(outputLine, "aStem:role", matcher.group(3));
        assertEquals(outputLine, "jdbc - test.subject.0", matcher.group(4));
        assertEquals(outputLine, "aStem:permissionDefName", matcher.group(5));
        assertEquals(outputLine, "action", matcher.group(6));
        
        
      } finally {
        System.setOut(systemOut);
      }
    
    }

  /**
   * @throws Exception
   */
  public void testAssignPermissions() throws Exception {

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb(
        AttributeDefType.perm, "test", "testAttributeAssignDefName");

    final AttributeDef attributeDef = attributeDefName.getAttributeDef();

    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();

    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrAssign").assignName("test:groupTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").assignTypeOfGroup(TypeOfGroup.role).save();

    //test subject 0 can view and read
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    AttributeAssign attributeAssign = null;
    //    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    //     = attributeAssignResult.getAttributeAssign();
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
          .compile("^Index\\: (\\d+)\\: permissionType\\: (.+), owner\\: (.+), permissionDefNameName\\: (.+), action\\: (.+), disallowed\\: (.+), enabled\\: (T|F), attributeAssignId\\: (.+), changed\\: (T|F), deleted\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));      
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("disallowed"));

      
      // ######################################################
      // Try permissionId
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameUuids=" + attributeDefName.getId() +  " --roleNames=test:groupTestAttrAssign",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains(attributeDefName.getId()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // Try ownerGroupUuid
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleUuids=" + group.getUuid(),
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains(group.getUuid()));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // Try enabledTime
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --assignmentEnabledTime=2010/03/05_17:05:13.123",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime") 
          && GrouperClientWs.mostRecentRequest.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // Try disabledTime
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --assignmentDisabledTime=2010/03/05_17:05:13.123",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "F", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime") 
          && GrouperClientWs.mostRecentRequest.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("2010/03/05 17:05:13.123"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // Try assignmentNotes
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --assignmentNotes=theNotes",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentResponse,
          GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("assignmentNotes") 
          && GrouperClientWs.mostRecentRequest.contains("theNotes") );
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      
      // ######################################################
      // Try delegatable
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --delegatable=FALSE",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      //assertTrue(GrouperClientWs.mostRecentResponse,
      //    GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("delegatable")
          && GrouperClientWs.mostRecentRequest.contains("FALSE"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // Try actions
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --actions=assign",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      //assertTrue(GrouperClientWs.mostRecentResponse,
      //    GrouperClientWs.mostRecentResponse.contains("theNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // includeGroupDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --includeGroupDetail=T",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
  
      // ######################################################
      // includeSubjectDetail
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --includeSubjectDetail=T",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      
      // ######################################################
      // subjectAttributeNames
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --subjectAttributeNames=abc",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      
      // ######################################################
      // params
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign "
          + " --paramName0=a --paramValue0=b",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "F", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      
      // ######################################################
      // attribute assign lookups
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, true);
  
      String attributeAssignId = attributeAssign.getId();
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=remove_permission "
          + " --attributeAssignUuids=" + attributeAssign.getId(),
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
  
      assertNull("Should be deleted", attributeAssign);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssignId, matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // attribute assign lookups custom tempflate
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
  
      attributeAssignId = attributeAssign.getId();
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=remove_permission "
          + " --attributeAssignUuids=" + attributeAssign.getId() + " --outputTemplate=${wsAttributeAssign.attributeAssignType}$newline$",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
  
      assertNull("Should be deleted", attributeAssign);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = GrouperClientUtils.trim(outputLines[0]);
      
      assertEquals(outputLine, "group", outputLine);      
  
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      
      // ######################################################
      // attribute assign lookups
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      attributeAssign = group.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
  
      attributeAssignId = attributeAssign.getId();
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=remove_permission "
          + " --attributeAssignUuids=" + attributeAssign.getId() + " --actAsSubjectId=GrouperSystem",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
  
      assertNull("Should be deleted", attributeAssign);
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssignId, matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
  
      // ######################################################
      // disallow
  
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      group.getAttributeDelegate().removeAttribute(attributeDefName);
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --roleNames=test:groupTestAttrAssign --disallowed=true",
          " "));
      
      attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
      attributeAssignId = attributeAssign.getId();
      
      assertTrue("Should be disallowed", attributeAssign.isDisallowed());
      
      System.out.flush();
      output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      outputLine = outputLines[0];
  
      matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role", matcher.group(2));
      assertEquals(outputLine, "test:groupTestAttrAssign", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "T", matcher.group(6));
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssignId, matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("disallowed"));
  
      
      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * @throws Exception
   */
  public void testAssignPermissionsAnyMembership() throws Exception {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb(AttributeDefType.perm, "test", "testAttributeAssignDefName");
    
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    
    attributeDef.setAssignToGroup(false);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    
    Group group1 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign").assignName("test:anyMembershipTestAttrAssign").assignCreateParentStemsIfNotExist(true)
      .assignTypeOfGroup(TypeOfGroup.role).assignDescription("description").save();
  
    Group group2 = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:anyMembershipTestAttrAssign2").assignName("test:anyMembershipTestAttrAssign2").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //add one group to another to make effective membership and add attribute to that membership
    group1.addMember(group2.toSubject());
    group2.addMember(SubjectTestHelper.SUBJ0);    
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
  
    Membership membership = (Membership)MembershipFinder.findMemberships(GrouperUtil.toSet(group1.getId()), 
        GrouperUtil.toSet(member.getUuid()), null, null, FieldFinder.find("members", true), null, null, null, null, null).iterator().next()[0];
    
  
    AttributeAssign attributeAssign = null;
    
    PrintStream systemOut = System.out;
  
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  
    try {
  
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=assignPermissionsWs --permissionType=role_subject --permissionAssignOperation=assign_permission " +
          "--permissionDefNameNames=test:testAttributeAssignDefName --subjectRole0SubjectId=" + member.getSubjectId()
          + " --subjectRole0RoleName=" + group1.getName(),
          " "));
      
      attributeAssign = membership.getAttributeDelegateEffMship().retrieveAssignment("assign", attributeDefName, false, true);
      
      System.out.flush();
      String output = new String(baos.toByteArray());
  
      System.setOut(systemOut);
  
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
      // match: Index: 0: attributeAssignType: group, owner: test:groupTestAttrAssign, attributeDefNameNameName test:testAttributeAssignDefName, action: assign, values: 15,5,5, enable: T, id: a9c83eeb78c04ae5befcea36272d318c, changed: true, valuesChanged: false
      // match: ^Index: (\d+)\: group\: (.+), subject\: (.+), list: (.+), type\: (.+), enabled\: (T|F), changed\: (T|F), valuesChanged\: (T|F)$
      Pattern pattern = Pattern
        .compile("^Index\\: (\\d+)\\: permissionType\\: (.+), owner\\: (.+), permissionDefNameName\\: (.+), action\\: (.+), disallowed\\: (T|F), enabled\\: (T|F), attributeAssignId\\: (.+), changed\\: (T|F), deleted\\: (T|F)$");
      String outputLine = outputLines[0];
  
      Matcher matcher = pattern.matcher(outputLines[0]);
  
      assertTrue(outputLine, matcher.matches());
      assertEquals(outputLine, "0", matcher.group(1));
      assertEquals(outputLine, "role_subject", matcher.group(2));
      assertEquals(outputLine, "test:anyMembershipTestAttrAssign - jdbc - test.subject.0", matcher.group(3));
      assertEquals(outputLine, "test:testAttributeAssignDefName", matcher.group(4));
      assertEquals(outputLine, "assign", matcher.group(5));
      
      assertEquals(outputLine, "F", matcher.group(6));      
      assertEquals(outputLine, "T", matcher.group(7));
      assertEquals(outputLine, attributeAssign.getId(), matcher.group(8));
      assertEquals(outputLine, "T", matcher.group(9));
      
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("actions"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentEnabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentDisabledTime"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("assignmentNotes"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionAssignOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("attributeAssignValueOperation"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionType"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("clientVersion"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("delegatable"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("params"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("subjectAttributeNames"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("wsAttributeAssignLookups"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("permissionDefNameLookups")
          && GrouperClientWs.mostRecentRequest.contains("test:testAttributeAssignDefName"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          !GrouperClientWs.mostRecentRequest.contains("roleLookups")
          && !GrouperClientWs.mostRecentRequest.contains("test:groupTestAttrAssign"));
      assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("subjectRoleLookups")
          && GrouperClientWs.mostRecentRequest.contains(group1.getName())
          && GrouperClientWs.mostRecentRequest.contains(member.getSubjectId()));

      
    } finally {
      System.setOut(systemOut);
    }
  
  }

  /**
   * 
   * @throws Exception
   */
  public void atestGetGroupsCache() throws Exception {

    // make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null,
        "aStem:aGroup", "aGroup", "description1", null, true);

    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);

    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);

    // add a subject
    group.addMember(SubjectTestHelper.SUBJ0, false);

    WsGetGroupsResults wsGetGroupsResults = new GcGetGroups().addSubjectId(SubjectTestHelper.SUBJ0_ID).execute();

    assertEquals("description1", wsGetGroupsResults.getResults()[0].getWsGroups()[0].getDescription());
    
    //change the description
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setWsGroupLookup(new WsGroupLookup("aStem:aGroup", null));
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription("description2");
    wsGroup.setName("aStem:aGroup");
    wsGroup.setDisplayExtension("aGroup");
    wsGroupToSave.setWsGroup(wsGroup);
    WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
    assertEquals("description2", wsGroupSaveResults.getResults()[0].getWsGroup().getDescription());
    
    //get groups for user again
    wsGetGroupsResults = new GcGetGroups().addSubjectId(SubjectTestHelper.SUBJ0_ID).execute();

    assertEquals("description2", wsGetGroupsResults.getResults()[0].getWsGroups()[0].getDescription());
    
    
  }

  /**
     * @throws Exception
     */
    public void testAttributeDefNameSave() throws Exception {
  
      PrintStream systemOut = System.out;
  
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      String output = null;
      String[] outputLines = null;
      Pattern pattern = null;
      Matcher matcher = null;
      try {
        
        GrouperSession grouperSession = GrouperSession.startRootSession();
        
        AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("aStem:newAttributeDef")
          .assignCreateParentStemsIfNotExist(true).save();
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        systemOut.println(output);
        
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        assertEquals(1, outputLines.length);
        
        pattern = Pattern.compile("^Success: (T|F): code: ([A-Z_]+): (.*+)$");
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_INSERTED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));
        
        // ##########################
        //try with name with slash
  
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName0/1 --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_INSERTED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName0/1", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("saveMode"));

        //########################################################
        // run again with save mode  --saveMode=INSERT_OR_UPDATE|INSERT|UPDATE
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName0/1 --nameOfAttributeDef=aStem:newAttributeDef --saveMode=UPDATE", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName0/1", matcher.group(3));

        assertTrue(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("saveMode")
              && GrouperClientWs.mostRecentRequest.contains(">UPDATE<"));
        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("wsAttributeDefNameLookup"));
        
        //########################################################
        // run again with lookup  --attributeDefNameLookupName=aStem:newAttributeDefName
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --attributeDefNameLookupName=aStem:newAttributeDefName --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("saveMode"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<wsAttributeDefNameLookup>"));
        
        
        //########################################################
        // run again with lookup  --attributeDefNameLookupUuid=aStem:newAttributeDefName
        
        AttributeDefName newAttributeDefName = AttributeDefNameFinder.findByName("aStem:newAttributeDefName", true);
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --attributeDefNameLookupUuid=" + newAttributeDefName.getId() + " --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
          GrouperClientWs.mostRecentRequest.contains("description"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<wsAttributeDefNameLookup>")
            && GrouperClientWs.mostRecentRequest.contains(newAttributeDefName.getId()));
        
        //########################################################
        // run again with --description=theDescription
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --description=theDescription --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_UPDATED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

//        assertFalse(GrouperClientWs.mostRecentRequest,
//            GrouperClientWs.mostRecentRequest.contains("<displayExtension>"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<description>")
            && GrouperClientWs.mostRecentRequest.contains("theDescription"));
        
        //########################################################
        // run again with --displayExtension=theDisplayExtension
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --displayExtension=theDisplayExtension --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_UPDATED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("createParentStemsIfNotExist"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<displayExtension>")
            && GrouperClientWs.mostRecentRequest.contains("theDisplayExtension"));
        
        //########################################################
        // run again with --createParentStemsIfNotExist=true
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --createParentStemsIfNotExist=true --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_UPDATED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("attributeDefId"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("createParentStemsIfNotExist"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<attributeDefName>"));
        
        //########################################################
        // run again with --uuidOfAttributeDef=abc
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --uuidOfAttributeDef=" + attributeDef.getId(), " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("actAsSubjectLookup"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<attributeDefId>"));
        
        
        //########################################################
        // run again with --actAsSubjectId=subjId
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --actAsSubjectId=GrouperSystem --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("subjectIdentifier"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<actAsSubjectLookup><subjectId>"));
        
        
        //########################################################
        // run again with --actAsSubjectIdentifier=subjId
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --actAsSubjectIdentifier=GrouperSystem --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("sourceId"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("<actAsSubjectLookup><subjectIdentifier>"));
        
        
        //########################################################
        // run again with --actAsSubjectSource=subjId
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
  
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=attributeDefNameSaveWs --name=aStem:newAttributeDefName --actAsSubjectIdentifier=GrouperSystem --actAsSubjectSource=g:isa --nameOfAttributeDef=aStem:newAttributeDef", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
  
        System.setOut(systemOut);
  
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
  
        assertEquals(1, outputLines.length);
        
        matcher = pattern.matcher(outputLines[0]);
  
        assertTrue(outputLines[0], matcher.matches());
  
        assertEquals(outputLines[0], "T", matcher.group(1));
        assertEquals(outputLines[0], "SUCCESS_NO_CHANGES_NEEDED", matcher.group(2));
        assertEquals(outputLines[0], "aStem:newAttributeDefName", matcher.group(3));

        assertFalse(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("somethingelse"));
        assertTrue(GrouperClientWs.mostRecentRequest,
            GrouperClientWs.mostRecentRequest.contains("subjectSourceId")
            && GrouperClientWs.mostRecentRequest.contains("g:isa"));
        
//        // #####################################################
//        // run again, with clientVersion
//  
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient.main(GrouperClientUtils.splitTrim(
//            "--operation=groupSaveWs --name=aStem:newGroup0", " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        pattern = Pattern.compile("^Success: T: code: ([A-Z_]+): (.*+)$");
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_INSERTED", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        // #####################################################
//        // run again, with clientVersion
//  
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient
//            .main(GrouperClientUtils
//                .splitTrim(
//                    "--operation=groupSaveWs --name=aStem:newGroup0 --clientVersion=v1_3_000",
//                    " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        // #####################################################
//        // run again, should be already added
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient.main(GrouperClientUtils.splitTrim(
//            "--operation=groupSaveWs --name=aStem:newGroup0", " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        // #####################################################
//        // run again, should be already added
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient
//            .main(GrouperClientUtils
//                .splitTrim(
//                    "--operation=groupSaveWs --name=aStem:newGroup0 --displayExtension=newGroup0displayExtension",
//                    " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_UPDATED", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        // #####################################################
//        // run with invalid args
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        // test a command line template
//        try {
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup0 --ousdfsdfate=${index}",
//                      " "));
//        } catch (Exception e) {
//          assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
//        }
//        System.out.flush();
//  
//        System.setOut(systemOut);
//  
//        // #####################################################
//        // run with custom template
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        // test a command line template
//        GrouperClient
//            .main(GrouperClientUtils
//                .splitTrim(
//                    "--operation=groupSaveWs --name=aStem:newGroup0 --outputTemplate=${index}",
//                    " "));
//  
//        System.out.flush();
//  
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        assertEquals("0", output);
//  
//        // #####################################################
//        // run again, with field
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient.main(GrouperClientUtils.splitTrim(
//            "--operation=groupSaveWs --name=aStem:newGroup0 --saveMode=UPDATE",
//            " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        assertTrue(GrouperClientWs.mostRecentRequest,
//            GrouperClientWs.mostRecentRequest.contains("saveMode")
//                && GrouperClientWs.mostRecentRequest.contains("UPDATE"));
//  
//        // #####################################################
//        // run again, with txType
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient.main(GrouperClientUtils.splitTrim(
//            "--operation=groupSaveWs --name=aStem:newGroup0 --txType=NONE", " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        assertTrue(GrouperClientWs.mostRecentRequest,
//            GrouperClientWs.mostRecentRequest.contains("txType")
//                && GrouperClientWs.mostRecentRequest.contains("NONE")
//                && !GrouperClientWs.mostRecentRequest
//                    .contains("includeGroupDetail"));
//  
//        // #####################################################
//        // run again, with includeGroupDetail
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient
//            .main(GrouperClientUtils
//                .splitTrim(
//                    "--operation=groupSaveWs --name=aStem:newGroup0 --includeGroupDetail=true",
//                    " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
//        assertEquals("aStem:newGroup0", matcher.group(2));
//  
//        assertTrue(!GrouperClientWs.mostRecentRequest.contains("txType")
//            && !GrouperClientWs.mostRecentRequest.contains("NONE")
//            && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
//  
//        // #####################################################
//        // run again, with groupLookupName
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient
//            .main(GrouperClientUtils
//                .splitTrim(
//                    "--operation=groupSaveWs --name=aStem:newGroup1 --groupLookupName=aStem:newGroup0",
//                    " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_UPDATED", matcher.group(1));
//        assertEquals("aStem:newGroup1", matcher.group(2));
//  
//        assertTrue(GrouperClientWs.mostRecentRequest,
//            !GrouperClientWs.mostRecentRequest.contains("txType")
//                && !GrouperClientWs.mostRecentRequest.contains("NONE")
//                && !GrouperClientWs.mostRecentRequest
//                    .contains("includeGroupDetail")
//                && GrouperClientWs.mostRecentRequest.contains("wsGroupLookup")
//                && GrouperClientWs.mostRecentRequest.contains("aStem:newGroup1")
//                && GrouperClientWs.mostRecentRequest.contains("aStem:newGroup0"));
//  
//        // #####################################################
//        // run again, with saveMode
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        GrouperClient.main(GrouperClientUtils.splitTrim(
//            "--operation=groupSaveWs --name=aStem:newGroup3 --saveMode=INSERT",
//            " "));
//        System.out.flush();
//        output = new String(baos.toByteArray());
//  
//        System.setOut(systemOut);
//  
//        outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//        matcher = pattern.matcher(outputLines[0]);
//  
//        assertTrue(outputLines[0], matcher.matches());
//  
//        assertEquals("SUCCESS_INSERTED", matcher.group(1));
//        assertEquals("aStem:newGroup3", matcher.group(2));
//  
//        assertTrue(GrouperClientWs.mostRecentRequest.contains("saveMode")
//            && GrouperClientWs.mostRecentRequest.contains("INSERT"));
//  
//        // #####################################################
//        // run again, description
//        baos = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(baos));
//  
//        try {
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup0 --description=aDescription",
//                      " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_UPDATED", matcher.group(1));
//          assertEquals("aStem:newGroup0", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest.contains("description")
//              && GrouperClientWs.mostRecentRequest.contains("aDescription"));
//  
//          // #####################################################
//          // run again, with params
//          baos = new ByteArrayOutputStream();
//          System.setOut(new PrintStream(baos));
//  
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup0 --paramName0=whatever --paramValue0=someValue",
//                      " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_UPDATED", matcher.group(1));
//          assertEquals("aStem:newGroup0", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest.contains("whatever")
//              && GrouperClientWs.mostRecentRequest.contains("someValue"));
//  
//          // #####################################################
//          // run again, with typeNames
//          baos = new ByteArrayOutputStream();
//          System.setOut(new PrintStream(baos));
//  
//          GrouperClient.main(GrouperClientUtils.splitTrim(
//              "--operation=groupSaveWs --name=aStem:newGroup0 --typeNames=aType",
//              " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_UPDATED", matcher.group(1));
//          assertEquals("aStem:newGroup0", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest.contains("typeNames")
//              && GrouperClientWs.mostRecentRequest.contains("aType"));
//  
//          // #####################################################
//          // run again, with attributes
//          baos = new ByteArrayOutputStream();
//          System.setOut(new PrintStream(baos));
//  
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup0 --typeNames=aType --attributeName0=attr_1 --attributeValue0=whatever",
//                      " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_UPDATED", matcher.group(1));
//          assertEquals("aStem:newGroup0", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest.contains("attr_1")
//              && GrouperClientWs.mostRecentRequest.contains("whatever"));
//  
//          // #####################################################
//          // run again, with groupDetailParamName0
//          baos = new ByteArrayOutputStream();
//          System.setOut(new PrintStream(baos));
//  
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup0 --groupDetailParamName0=something --groupDetailParamValue0=whatever",
//                      " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_UPDATED", matcher.group(1));
//          assertEquals("aStem:newGroup0", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest.contains("something")
//              && GrouperClientWs.mostRecentRequest.contains("whatever"));
//  
//          // #####################################################
//          // run again, with composite
//  
//          GrouperClient.main(GrouperClientUtils.splitTrim(
//              "--operation=groupSaveWs --name=aStem:leftGroup", " "));
//          GrouperClient.main(GrouperClientUtils.splitTrim(
//              "--operation=groupSaveWs --name=aStem:rightGroup", " "));
//  
//          baos = new ByteArrayOutputStream();
//          System.setOut(new PrintStream(baos));
//  
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup0 --compositeType=union --leftGroupName=aStem:leftGroup --rightGroupName=aStem:rightGroup --includeGroupDetail=true",
//                      " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_UPDATED", matcher.group(1));
//          assertEquals("aStem:newGroup0", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest.contains("union")
//              && GrouperClientWs.mostRecentRequest.contains("aStem:leftGroup")
//              && GrouperClientWs.mostRecentRequest.contains("aStem:rightGroup"));
//  
//          
//          // #####################################################
//          // run again, with typeOfGroup
//          baos = new ByteArrayOutputStream();
//          System.setOut(new PrintStream(baos));
//  
//          GrouperClient
//              .main(GrouperClientUtils
//                  .splitTrim(
//                      "--operation=groupSaveWs --name=aStem:newGroup4 --typeOfGroup=entity",
//                      " "));
//          System.out.flush();
//          output = new String(baos.toByteArray());
//  
//          System.setOut(systemOut);
//  
//          outputLines = GrouperClientUtils.splitTrim(output, "\n");
//  
//          matcher = pattern.matcher(outputLines[0]);
//  
//          assertTrue(outputLines[0], matcher.matches());
//  
//          assertEquals("SUCCESS_INSERTED", matcher.group(1));
//          assertEquals("aStem:newGroup4", matcher.group(2));
//  
//          assertTrue(GrouperClientWs.mostRecentRequest, GrouperClientWs.mostRecentRequest.contains("<typeOfGroup>entity</typeOfGroup>"));
//  
//          
//        } finally {
//        }
      } finally {
        System.setOut(systemOut);
      }
  
    }

}
