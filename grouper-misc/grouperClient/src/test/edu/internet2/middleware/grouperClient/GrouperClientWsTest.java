/*
 * @author mchyzer
 * $Id: GrouperClientWsTest.java,v 1.2 2008-12-01 07:40:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
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
    TestRunner.run(new GrouperClientWsTest("testGetMembers"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    
    super.setUp();

    String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
    
    RestClientSettings.resetData(wsUserString, false);
    
    GrouperClientUtils.grouperClientOverrideMap().put("encrypt.key", "sdfklj24lkj34lk34");
    GrouperClientUtils.grouperClientOverrideMap().put("encrypt.disableExternalFileLookup", "false");

    GrouperClientUtils.grouperClientOverrideMap().put("webService.addMember.output", "Index ${index}: success: ${wsAddMemberResult.resultMetadata.success}: code: ${wsAddMemberResult.resultMetadata.resultCode}: ${wsAddMemberResult.wsSubject.id}$newline$");
    GrouperClientUtils.grouperClientOverrideMap().put("webService.getMembers.output", "GroupIndex ${groupIndex}: success: ${wsGetMembersResult.resultMetadata.success}: code: ${wsGetMembersResult.resultMetadata.resultCode}: group: ${wsGetMembersResult.wsGroup.name}: subjectIndex: ${subjectIndex}: ${wsSubject.id}$newline$");
    
    GrouperClient.exitOnError = false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
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
   * note: this will only work at penn
   * @throws Exception 
   */
  public void testAddMember() throws Exception {
    
    //make sure group exists
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null, "aStem:aGroup", "aGroup", null, null, true);
//    
//    //give permissions
//    String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
//    String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
//    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
    
    
    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1", " "));
      System.out.flush();
      String output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
      
      Pattern pattern = Pattern.compile(
          "^Index (\\d+): success: T: code: ([A-Z_]+): (.*+)$");
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

      //#####################################################
      //run again, should be already added
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1", " "));
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
      
      //#####################################################
      //run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      //test a command line template
      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}", " "));
      } catch (Exception e) {
        assertTrue(e.getMessage(), e.getMessage().contains("ousdfsdfate"));
      }
      System.out.flush();
      
      System.setOut(systemOut);
      
      //#####################################################
      //run with custom template
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      //test a command line template
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --outputTemplate=${index}", " "));

      System.out.flush();
      
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      assertEquals("01", output);
      
      //#####################################################
      //run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --fieldName=members", " "));
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

      assertTrue(GrouperClientWs.mostRecentRequest, GrouperClientWs.mostRecentRequest.contains("fieldName")
          && GrouperClientWs.mostRecentRequest.contains("members")
          && !GrouperClientWs.mostRecentRequest.contains("txType"));
      
      //#####################################################
      //run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --txType=NONE", " "));
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

      assertTrue(GrouperClientWs.mostRecentRequest, !GrouperClientWs.mostRecentRequest.contains("fieldName")
          && !GrouperClientWs.mostRecentRequest.contains("members")
          && GrouperClientWs.mostRecentRequest.contains("txType") 
          && GrouperClientWs.mostRecentRequest.contains("NONE")
          && !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail") 
          && !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail")
          && !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail") 
          && !GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      
      //#####################################################
      //run again, with includeGroupDetail and includeSubjectDetail
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true", " "));
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

      assertTrue(
          !GrouperClientWs.mostRecentRequest.contains("txType") 
          && !GrouperClientWs.mostRecentRequest.contains("NONE")
          && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail") 
          && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
      
      //#####################################################
      //run again, with subject attributes
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsAddMemberResult.wsSubject.getAttributeValue(0)}$newline$", " "));
      System.out.flush();
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      outputLines = GrouperClientUtils.splitTrim(output, "\n");
      
      assertTrue(outputLines[0], outputLines[0].contains("my name is test.subject.0"));
      
      assertTrue(outputLines[1], outputLines[1].contains("my name is test.subject.1"));

      assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
      assertTrue(GrouperClientWs.mostRecentResponse.contains("my name is test.subject.0"));
      
      //#####################################################
      //run again, with default subject source
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc", " "));
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

      assertTrue(
          GrouperClientWs.mostRecentRequest.contains("jdbc"));
      
      //#####################################################
      //run again, subjects ids coming from file
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      String subjectIdsFileName = "subjectIdsFile_" + GrouperClientUtils.uniqueId() + ".txt";
      File subjectIdsFile = new File(subjectIdsFileName);
      
      GrouperClientUtils.saveStringIntoFile(subjectIdsFile, "test.subject.0\ntest.subject.1");
      
      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=addMemberWs --groupName=aStem:aGroup --subjectIdsFile="+subjectIdsFileName, " "));
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

        //#####################################################
        //run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --paramName0=whatever --paramValue0=someValue", " "));
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

        assertTrue(
            GrouperClientWs.mostRecentRequest.contains("whatever") 
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
     * note: this will only work at penn
     * @throws Exception 
     */
    public void testGetMembers() throws Exception {
      
      //make sure group exists
      GrouperSession grouperSession = GrouperSession.startRootSession();
      Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null, "aStem:aGroup", "aGroup", null, null, true);
      Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null, "aStem:aGroup2", "aGroup2", null, null, true);
      
      //give permissions
      String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
      String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
      Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
      
      group.grantPriv(wsUser, AccessPrivilege.READ);
      group.grantPriv(wsUser, AccessPrivilege.VIEW);
      group2.grantPriv(wsUser, AccessPrivilege.READ);
      group2.grantPriv(wsUser, AccessPrivilege.VIEW);
      
      //add some subjects
      group.addMember(SubjectTestHelper.SUBJ0);
      group.addMember(SubjectTestHelper.SUBJ1);
      group2.addMember(SubjectTestHelper.SUBJ2);
      group2.addMember(SubjectTestHelper.SUBJ3);
      
      PrintStream systemOut = System.out;
  
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      try {
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2", " "));
        System.out.flush();
        String output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        // match: ^GroupIndex (\d+)\: success\: ([TF])\: code: ([A-Z_]+)\: group\: (.*)\: subjectIndex\: (\d+)\: (.*)$
        Pattern pattern = Pattern.compile(
            "^GroupIndex (\\d+)\\: success\\: ([TF])\\: code: ([A-Z_]+)\\: group\\: (.*)\\: subjectIndex\\: (\\d+)\\: (.*)$");
        Matcher matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals(outputLines[0], "0", matcher.group(1));
        assertEquals(outputLines[0], "T", matcher.group(2));
        assertEquals(outputLines[0], "SUCCESS", matcher.group(3));
        assertEquals(outputLines[0], "aStem:aGroup", matcher.group(4));
        assertEquals(outputLines[0], "0", matcher.group(5));
        String subjectId = matcher.group(6);
        assertTrue(outputLines[0], GrouperClientUtils.equals("test.subject.0", subjectId)
            || GrouperClientUtils.equals("test.subject.1", subjectId));
        
        matcher = pattern.matcher(outputLines[1]);
        
        assertTrue(outputLines[1], matcher.matches());
        
        assertEquals(outputLines[1], "0", matcher.group(1));
        assertEquals(outputLines[1], "T", matcher.group(2));
        assertEquals(outputLines[1], "SUCCESS", matcher.group(3));
        assertEquals(outputLines[1], "aStem:aGroup", matcher.group(4));
        assertEquals(outputLines[1], "1", matcher.group(5));
        subjectId = matcher.group(6);
        assertTrue(outputLines[1], GrouperClientUtils.equals("test.subject.0", subjectId)
            || GrouperClientUtils.equals("test.subject.1", subjectId));
  
        matcher = pattern.matcher(outputLines[2]);
        
        assertTrue(outputLines[2], matcher.matches());
        
        assertEquals(outputLines[2], "1", matcher.group(1));
        assertEquals(outputLines[2], "T", matcher.group(2));
        assertEquals(outputLines[2], "SUCCESS", matcher.group(3));
        assertEquals(outputLines[2], "aStem:aGroup2", matcher.group(4));
        assertEquals(outputLines[2], "0", matcher.group(5));
        subjectId = matcher.group(6);
        assertTrue(outputLines[2], GrouperClientUtils.equals("test.subject.2", subjectId)
            || GrouperClientUtils.equals("test.subject.3", subjectId));
  
        matcher = pattern.matcher(outputLines[3]);
        
        assertTrue(outputLines[3], matcher.matches());
        
        assertEquals(outputLines[3], "1", matcher.group(1));
        assertEquals(outputLines[3], "T", matcher.group(2));
        assertEquals(outputLines[3], "SUCCESS", matcher.group(3));
        assertEquals(outputLines[3], "aStem:aGroup2", matcher.group(4));
        assertEquals(outputLines[3], "1", matcher.group(5));
        subjectId = matcher.group(6);
        assertTrue(outputLines[3], GrouperClientUtils.equals("test.subject.2", subjectId)
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
        
        //######################################################
        // Try a subject attribute name with custom template
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --subjectAttributeNames=name --outputTemplate=${wsSubject.getAttributeValue(0)}$newline$", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        Arrays.sort(outputLines);
        
        assertEquals(outputLines[0], "my name is test.subject.0");
        assertEquals(outputLines[1], "my name is test.subject.1");
        assertEquals(outputLines[2], "my name is test.subject.2");
        assertEquals(outputLines[3], "my name is test.subject.3");

        //#######################################################
        //try member filter
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --memberFilter=Effective", " "));
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
        
        //#######################################################
        //try includeGroupDetail
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --includeGroupDetail=true", " "));
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
        
        //#######################################################
        //try includeSubjectDetail
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --includeSubjectDetail=true", " "));
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
        
        //#######################################################
        //try subjectAttributeNames
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --subjectAttributeNames=name", " "));
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
        
        //#######################################################
        //try params
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --paramName0=someParam --paramValue0=someValue", " "));
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
            && GrouperClientWs.mostRecentRequest.toLowerCase().contains("params")
            && GrouperClientWs.mostRecentRequest.contains("someValue"));
        assertTrue(GrouperClientWs.mostRecentRequest, 
            !GrouperClientWs.mostRecentRequest.contains("fieldName"));
        assertTrue(GrouperClientWs.mostRecentRequest, 
            !GrouperClientWs.mostRecentRequest.contains("actAsSubject"));
        
        //#######################################################
        //try fieldName
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --fieldName=members", " "));
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
        
        //#######################################################
        //try actAsSubject
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --actAsSubjectId=GrouperSystem", " "));
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
  
  
}
