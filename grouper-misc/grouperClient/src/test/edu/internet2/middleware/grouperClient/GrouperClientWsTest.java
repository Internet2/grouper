/*
 * @author mchyzer
 * $Id: GrouperClientWsTest.java,v 1.7 2008-12-04 07:51:39 mchyzer Exp $
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
    TestRunner.run(new GrouperClientWsTest("testGroupSave"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    
    //dont do this, it deletes types
    //super.setUp();

    String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
    
    RestClientSettings.resetData(wsUserString, false);
    
    GrouperClientUtils.grouperClientOverrideMap().put("encrypt.key", "sdfklj24lkj34lk34");
    GrouperClientUtils.grouperClientOverrideMap().put("encrypt.disableExternalFileLookup", "false");

    GrouperClientUtils.grouperClientOverrideMap().put("webService.addMember.output", "Index ${index}: success: ${wsAddMemberResult.resultMetadata.success}: code: ${wsAddMemberResult.resultMetadata.resultCode}: ${wsAddMemberResult.wsSubject.id}$newline$");
    GrouperClientUtils.grouperClientOverrideMap().put("webService.getMembers.output", "GroupIndex ${groupIndex}: success: ${wsGetMembersResult.resultMetadata.success}: code: ${wsGetMembersResult.resultMetadata.resultCode}: group: ${wsGetMembersResult.wsGroup.name}: subjectIndex: ${subjectIndex}: ${wsSubject.id}$newline$");
    GrouperClientUtils.grouperClientOverrideMap().put("webService.deleteMember.output", "Index ${index}: success: ${wsDeleteMemberResult.resultMetadata.success}: code: ${wsDeleteMemberResult.resultMetadata.resultCode}: ${wsDeleteMemberResult.wsSubject.id}$newline$");
    
    GrouperClientUtils.grouperClientOverrideMap().put("grouperClient.alias.subjectIds", "pennIds");
    GrouperClientUtils.grouperClientOverrideMap().put("grouperClient.alias.subjectIdentifiers", "pennKeys");
    GrouperClientUtils.grouperClientOverrideMap().put("grouperClient.alias.SubjectId", "PennId");
    GrouperClientUtils.grouperClientOverrideMap().put("grouperClient.alias.SubjectIdentifier", "PennKey");

    GrouperClientUtils.grouperClientOverrideMap().put("webService.hasMember.output", "Index ${index}: success: ${wsHasMemberResult.resultMetadata.success}: code: ${wsHasMemberResult.resultMetadata.resultCode}: ${wsHasMemberResult.wsSubject.id}: ${hasMember}$newline$");

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
            "--operation=addMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0,test.subject.1", " "));
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
            "--operation=addMemberWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${index}", " "));
  
        System.out.flush();
        
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        assertEquals("01", output);
        
        //#####################################################
        //run again, with field
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=addMemberWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0,id.test.subject.1 --fieldName=members", " "));
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
          
          
          //#####################################################
          //run again, with replaceAllExisting
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=addMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --replaceAllExisting=true", " "));
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
              GrouperClientWs.mostRecentRequest.contains("replaceAllExisting"));
          
          
          
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
    public void testGroupSave() throws Exception {
      
      
      PrintStream systemOut = System.out;
  
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      try {
        
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0", " "));
        System.out.flush();
        String output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        Pattern pattern = Pattern.compile(
            "^Success: T: code: ([A-Z_]+): (.*+)$");
        Matcher matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("SUCCESS_INSERTED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));
  
        //#####################################################
        //run again, with clientVersion
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0 --clientVersion=v1_3_000", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("SUCCESS", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));

        //#####################################################
        //run again, should be already added
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
        
        //#####################################################
        //run again, should be already added
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0 --displayExtension=newGroup0displayExtension", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("SUCCESS_UPDATED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));
        
        //#####################################################
        //run with invalid args
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        //test a command line template
        try {
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --ousdfsdfate=${index}", " "));
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
            "--operation=groupSaveWs --name=aStem:newGroup0 --outputTemplate=${index}", " "));
  
        System.out.flush();
        
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        assertEquals("0", output);
        
        //#####################################################
        //run again, with field
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0 --saveMode=UPDATE", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));
  
        assertTrue(GrouperClientWs.mostRecentRequest, GrouperClientWs.mostRecentRequest.contains("saveMode")
            && GrouperClientWs.mostRecentRequest.contains("UPDATE"));
        
        //#####################################################
        //run again, with txType
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
            && !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        
        //#####################################################
        //run again, with includeGroupDetail
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0 --includeGroupDetail=true", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));
  
        assertTrue(
            !GrouperClientWs.mostRecentRequest.contains("txType") 
            && !GrouperClientWs.mostRecentRequest.contains("NONE")
            && GrouperClientWs.mostRecentRequest.contains("includeGroupDetail"));
        
        //#####################################################
        //run again, with groupLookupName
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup1 --groupLookupName=aStem:newGroup0", " "));
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
            && !GrouperClientWs.mostRecentRequest.contains("includeGroupDetail")
            && GrouperClientWs.mostRecentRequest.contains("wsGroupLookup")
            && GrouperClientWs.mostRecentRequest.contains("aStem:newGroup1")
            && GrouperClientWs.mostRecentRequest.contains("aStem:newGroup0"));
        
        //#####################################################
        //run again, with default subject source
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=groupSaveWs --name=aStem:newGroup0 --saveMode=INSERT", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("SUCCESS_INSERTED", matcher.group(1));
        assertEquals("aStem:newGroup0", matcher.group(2));
  
        assertTrue(
            GrouperClientWs.mostRecentRequest.contains("saveMode")
            && GrouperClientWs.mostRecentRequest.contains("INSERT"));
        
        //#####################################################
        //run again, description
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        String subjectIdsFileName = "subjectIdsFile_" + GrouperClientUtils.uniqueId() + ".txt";
        File subjectIdsFile = new File(subjectIdsFileName);
        
        GrouperClientUtils.saveStringIntoFile(subjectIdsFile, "test.subject.0\ntest.subject.1");
        
        try {
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --description=aDescription", " "));
          System.out.flush();
          output = new String(baos.toByteArray());
          
          System.setOut(systemOut);
          
          outputLines = GrouperClientUtils.splitTrim(output, "\n");
          
          matcher = pattern.matcher(outputLines[0]);
          
          assertTrue(outputLines[0], matcher.matches());
          
          assertEquals("SUCCESS_UPDATED", matcher.group(1));
          assertEquals("aStem:newGroup0", matcher.group(2));

          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("description")
              && GrouperClientWs.mostRecentRequest.contains("aDescription"));

          //#####################################################
          //run again, with params
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --paramName0=whatever --paramValue0=someValue", " "));
          System.out.flush();
          output = new String(baos.toByteArray());
          
          System.setOut(systemOut);
          
          outputLines = GrouperClientUtils.splitTrim(output, "\n");
          
          matcher = pattern.matcher(outputLines[0]);
          
          assertTrue(outputLines[0], matcher.matches());
          
          assertEquals("SUCCESS_NO_CHANGES_NEEDED", matcher.group(1));
          assertEquals("aStem:newGroup0", matcher.group(2));
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("whatever") 
              && GrouperClientWs.mostRecentRequest.contains("someValue"));
          
          
          //#####################################################
          //run again, with typeNames
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --typeNames=aType", " "));
          System.out.flush();
          output = new String(baos.toByteArray());
          
          System.setOut(systemOut);
          
          outputLines = GrouperClientUtils.splitTrim(output, "\n");
          
          matcher = pattern.matcher(outputLines[0]);
          
          assertTrue(outputLines[0], matcher.matches());
          
          assertEquals("SUCCESS_UPDATED", matcher.group(1));
          assertEquals("aStem:newGroup0", matcher.group(2));
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("typeNames")
              && GrouperClientWs.mostRecentRequest.contains("aType"));
          
          
          //#####################################################
          //run again, with attributes
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --typeNames=aType --attributeName0=attr_1 --attributeValue0=whatever", " "));
          System.out.flush();
          output = new String(baos.toByteArray());
          
          System.setOut(systemOut);
          
          outputLines = GrouperClientUtils.splitTrim(output, "\n");
          
          matcher = pattern.matcher(outputLines[0]);
          
          assertTrue(outputLines[0], matcher.matches());
          
          assertEquals("SUCCESS_UPDATED", matcher.group(1));
          assertEquals("aStem:newGroup0", matcher.group(2));
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("attr_1")
              && GrouperClientWs.mostRecentRequest.contains("whatever"));
          
          //#####################################################
          //run again, with groupDetailParamName0
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --groupDetailParamName0=something --groupDetailParamValue0=whatever", " "));
          System.out.flush();
          output = new String(baos.toByteArray());
          
          System.setOut(systemOut);
          
          outputLines = GrouperClientUtils.splitTrim(output, "\n");
          
          matcher = pattern.matcher(outputLines[0]);
          
          assertTrue(outputLines[0], matcher.matches());
          
          assertEquals("SUCCESS_UPDATED", matcher.group(1));
          assertEquals("aStem:newGroup0", matcher.group(2));
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("something")
              && GrouperClientWs.mostRecentRequest.contains("whatever"));

          //#####################################################
          //run again, with composite
          
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:leftGroup", " "));
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:rightGroup", " "));
          
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=groupSaveWs --name=aStem:newGroup0 --compositeType=union --leftGroupName=aStem:leftGroup --rightGroupName=aStem:rightGroup --includeGroupDetail=true", " "));
          System.out.flush();
          output = new String(baos.toByteArray());
          
          System.setOut(systemOut);
          
          outputLines = GrouperClientUtils.splitTrim(output, "\n");
          
          matcher = pattern.matcher(outputLines[0]);
          
          assertTrue(outputLines[0], matcher.matches());
          
          assertEquals("SUCCESS_UPDATED", matcher.group(1));
          assertEquals("aStem:newGroup0", matcher.group(2));
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("union")
              && GrouperClientWs.mostRecentRequest.contains("aStem:leftGroup")
              && GrouperClientWs.mostRecentRequest.contains("aStem:rightGroup")
              );
          
          
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
    public void testGetGroups() throws Exception {
      
      //make sure group exists
      GrouperSession grouperSession = GrouperSession.startRootSession();
      Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null, "aStem:aGroup", "aGroup", null, null, true);
      Group group2 = Group.saveGroup(grouperSession, "aStem:aGroup2", null, "aStem:aGroup2", "aGroup2", null, null, true);
      Group group3 = Group.saveGroup(grouperSession, "aStem:aGroup3", null, "aStem:aGroup3", "aGroup3", null, null, true);
      
      //give permissions
      String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
      String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
      Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
      
      group.grantPriv(wsUser, AccessPrivilege.READ, false);
      group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
      group2.grantPriv(wsUser, AccessPrivilege.READ, false);
      group2.grantPriv(wsUser, AccessPrivilege.VIEW, false);
      group3.grantPriv(wsUser, AccessPrivilege.READ, false);
      group3.grantPriv(wsUser, AccessPrivilege.VIEW, false);
      
      //add some subjects
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group2.addMember(SubjectTestHelper.SUBJ0, false);
      group2.addMember(SubjectTestHelper.SUBJ1, false);
      group3.addMember(SubjectTestHelper.SUBJ1, false);

      
      PrintStream systemOut = System.out;
  
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      try {
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1", " "));
        System.out.flush();
        String output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        System.out.println(output);
        
        String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        Pattern pattern = Pattern.compile(
            "^SubjectIndex (\\d+): success: T: code: ([A-Z_]+): subject: (.*): groupIndex: (\\d+): (.*+)$");
        Matcher matcher = pattern.matcher(outputLines[0]);
        
        assertTrue(outputLines[0], matcher.matches());
        
        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
        
        matcher = pattern.matcher(outputLines[1]);
        
        assertTrue(outputLines[1], matcher.matches());
        
        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
  
        matcher = pattern.matcher(outputLines[2]);
        
        assertTrue(outputLines[2], matcher.matches());
        
        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
  
        matcher = pattern.matcher(outputLines[3]);
        
        assertTrue(outputLines[3], matcher.matches());
        
        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
  
        //#####################################################
        //run with invalid args
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        //test a command line template
        try {
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=whatever", " "));
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
            "--operation=getGroupsWs --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${subjectIndex}", " "));
  
        System.out.flush();
        
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        assertEquals("0011", output);
        
        //#####################################################
        //run again, with includeGroupDetail and includeSubjectDetail
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true", " "));
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
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
        
        matcher = pattern.matcher(outputLines[1]);
        
        assertTrue(outputLines[1], matcher.matches());
        
        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
  
        matcher = pattern.matcher(outputLines[2]);
        
        assertTrue(outputLines[2], matcher.matches());
        
        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
  
        matcher = pattern.matcher(outputLines[3]);
        
        assertTrue(outputLines[3], matcher.matches());
        
        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
        
        assertTrue(
            GrouperClientWs.mostRecentRequest.contains("includeGroupDetail") 
            && GrouperClientWs.mostRecentRequest.contains("includeSubjectDetail"));
        
        //#####################################################
        //run again, with subject attributes
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${subjectIndex}:$space$${wsGetGroupsResult.wsSubject.getAttributeValue(0)}$newline$", " "));
        System.out.flush();
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        assertTrue(output, outputLines[0].contains("my name is test.subject.0"));
        
        assertTrue(output, outputLines[2].contains("my name is test.subject.1"));
  
        assertTrue(GrouperClientWs.mostRecentRequest.contains(">name<"));
        assertTrue(GrouperClientWs.mostRecentResponse.contains("my name is test.subject.0"));
        
        //#####################################################
        //run again, with default subject source
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc", " "));
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
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
        
        matcher = pattern.matcher(outputLines[1]);
        
        assertTrue(outputLines[1], matcher.matches());
        
        assertEquals("0", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.0", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
  
        matcher = pattern.matcher(outputLines[2]);
        
        assertTrue(outputLines[2], matcher.matches());
        
        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("0", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
  
        matcher = pattern.matcher(outputLines[3]);
        
        assertTrue(outputLines[3], matcher.matches());
        
        assertEquals("1", matcher.group(1));
        assertEquals("SUCCESS", matcher.group(2));
        assertEquals("test.subject.1", matcher.group(3));
        assertEquals("1", matcher.group(4));
        assertTrue(matcher.group(5), 
            GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
            || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));  

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
              "--operation=getGroupsWs --subjectIdsFile="+subjectIdsFileName, " "));
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
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
          
          matcher = pattern.matcher(outputLines[1]);
          
          assertTrue(outputLines[1], matcher.matches());
          
          assertEquals("0", matcher.group(1));
          assertEquals("SUCCESS", matcher.group(2));
          assertEquals("test.subject.0", matcher.group(3));
          assertEquals("1", matcher.group(4));
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
    
          matcher = pattern.matcher(outputLines[2]);
          
          assertTrue(outputLines[2], matcher.matches());
          
          assertEquals("1", matcher.group(1));
          assertEquals("SUCCESS", matcher.group(2));
          assertEquals("test.subject.1", matcher.group(3));
          assertEquals("0", matcher.group(4));
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
    
          matcher = pattern.matcher(outputLines[3]);
          
          assertTrue(outputLines[3], matcher.matches());
          
          assertEquals("1", matcher.group(1));
          assertEquals("SUCCESS", matcher.group(2));
          assertEquals("test.subject.1", matcher.group(3));
          assertEquals("1", matcher.group(4));
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
          
          //#####################################################
          //run again, with params
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=getGroupsWs --subjectIds=test.subject.0,test.subject.1 --memberFilter=Immediate", " "));
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
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
          
          matcher = pattern.matcher(outputLines[1]);
          
          assertTrue(outputLines[1], matcher.matches());
          
          assertEquals("0", matcher.group(1));
          assertEquals("SUCCESS", matcher.group(2));
          assertEquals("test.subject.0", matcher.group(3));
          assertEquals("1", matcher.group(4));
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5)));
    
          matcher = pattern.matcher(outputLines[2]);
          
          assertTrue(outputLines[2], matcher.matches());
          
          assertEquals("1", matcher.group(1));
          assertEquals("SUCCESS", matcher.group(2));
          assertEquals("test.subject.1", matcher.group(3));
          assertEquals("0", matcher.group(4));
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
    
          matcher = pattern.matcher(outputLines[3]);
          
          assertTrue(outputLines[3], matcher.matches());
          
          assertEquals("1", matcher.group(1));
          assertEquals("SUCCESS", matcher.group(2));
          assertEquals("test.subject.1", matcher.group(3));
          assertEquals("1", matcher.group(4));
          assertTrue(matcher.group(5), 
              GrouperClientUtils.equals("aStem:aGroup2", matcher.group(5))
              || GrouperClientUtils.equals("aStem:aGroup3", matcher.group(5)));
          
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("memberFilter") 
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
     * note: this will only work at penn
     * @throws Exception 
     */
    public void testHasMember() throws Exception {
            
      //make sure group exists
      GrouperSession grouperSession = GrouperSession.startRootSession();
      Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null, "aStem:aGroup", "aGroup", null, null, true);
      
      //give permissions
      String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
      String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
      Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
      
      group.grantPriv(wsUser, AccessPrivilege.READ, false);
      group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
      
      //add some subjects
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      PrintStream systemOut = System.out;
  
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      try {
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1", " "));
        System.out.flush();
        String output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        String[] outputLines = GrouperClientUtils.splitTrim(output, "\n");
        
        Pattern pattern = Pattern.compile(
            "^Index (\\d+): success: T: code: ([A-Z_]+): (.+): (false|true)$");
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
  
        //#####################################################
        //run with invalid args
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        //test a command line template
        try {
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}", " "));
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
            "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${index}", " "));
  
        System.out.flush();
        
        output = new String(baos.toByteArray());
        
        System.setOut(systemOut);
        
        assertEquals("01", output);
        
        //#####################################################
        //run again, with field
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=hasMemberWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0,id.test.subject.1 --fieldName=members", " "));
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
  
        assertTrue(GrouperClientWs.mostRecentRequest, GrouperClientWs.mostRecentRequest.contains("fieldName")
            && GrouperClientWs.mostRecentRequest.contains("members")
            && !GrouperClientWs.mostRecentRequest.contains("txType"));
        
        //#####################################################
        //run again, with includeGroupDetail and includeSubjectDetail
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true", " "));
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
            "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsHasMemberResult.wsSubject.getAttributeValue(0)}$newline$", " "));
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
            "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc", " "));
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
              "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIdsFile="+subjectIdsFileName, " "));
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
  
          //#####################################################
          //run again, with params
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --paramName0=whatever --paramValue0=someValue", " "));
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
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("whatever") 
              && GrouperClientWs.mostRecentRequest.contains("someValue"));
          
          
          //#####################################################
          //run again, with memberFilter
          baos = new ByteArrayOutputStream();
          System.setOut(new PrintStream(baos));
        
          GrouperClient.main(GrouperClientUtils.splitTrim(
              "--operation=hasMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --memberFilter=Immediate", " "));
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
  
          assertTrue(
              GrouperClientWs.mostRecentRequest.contains("memberFilter")
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
   * note: this will only work at penn
   * @throws Exception 
   */
  public void testDeleteMember() throws Exception {
    
    //make sure group exists
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = Group.saveGroup(grouperSession, "aStem:aGroup", null, "aStem:aGroup", "aGroup", null, null, true);
    
    //give permissions
    String wsUserLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue("grouperClient.webService." + wsUserLabel, true);
    Subject wsUser = SubjectFinder.findByIdOrIdentifier(wsUserString, true);
    
    group.grantPriv(wsUser, AccessPrivilege.READ, false);
    group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
    group.grantPriv(wsUser, AccessPrivilege.ADMIN, false);
    
    //add some subjects
    group.addMember(SubjectTestHelper.SUBJ0, false);
    group.addMember(SubjectTestHelper.SUBJ1, false);
    
    
    PrintStream systemOut = System.out;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    
    try {
      
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1", " "));
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
      //run again, should be already deleted
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=deleteMemberWs --groupName=aStem:aGroup --pennIds=test.subject.0,test.subject.1", " "));
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
      
      //#####################################################
      //run with invalid args
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
      
      //test a command line template
      try {
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}", " "));
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
          "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIdentifiers=id.test.subject.0,id.test.subject.1 --outputTemplate=${index}", " "));

      System.out.flush();
      
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      assertEquals("01", output);
      
      //#####################################################
      //run again, with field
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=deleteMemberWs --groupName=aStem:aGroup --pennKeys=id.test.subject.0,id.test.subject.1 --fieldName=members", " "));
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

      assertTrue(GrouperClientWs.mostRecentRequest, GrouperClientWs.mostRecentRequest.contains("fieldName")
          && GrouperClientWs.mostRecentRequest.contains("members")
          && !GrouperClientWs.mostRecentRequest.contains("txType"));
      
      //#####################################################
      //run again, with txType
      baos = new ByteArrayOutputStream();
      System.setOut(new PrintStream(baos));
    
      GrouperClient.main(GrouperClientUtils.splitTrim(
          "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --txType=NONE", " "));
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
          "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --includeGroupDetail=true --includeSubjectDetail=true", " "));
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
          "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --subjectAttributeNames=name --outputTemplate=${index}:$space$${wsDeleteMemberResult.wsSubject.getAttributeValue(0)}$newline$", " "));
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
          "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --defaultSubjectSource=jdbc", " "));
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
            "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIdsFile="+subjectIdsFileName, " "));
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

        //#####################################################
        //run again, with params
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
      
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=deleteMemberWs --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --paramName0=whatever --paramValue0=someValue", " "));
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
      
      group.grantPriv(wsUser, AccessPrivilege.READ, false);
      group.grantPriv(wsUser, AccessPrivilege.VIEW, false);
      group2.grantPriv(wsUser, AccessPrivilege.READ, false);
      group2.grantPriv(wsUser, AccessPrivilege.VIEW, false);
      
      //add some subjects
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);
      group2.addMember(SubjectTestHelper.SUBJ2, false);
      group2.addMember(SubjectTestHelper.SUBJ3, false);
      
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
        
        //#######################################################
        //try actAsSubject but with alias
        
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        
        GrouperClient.main(GrouperClientUtils.splitTrim(
            "--operation=getMembersWs --groupNames=aStem:aGroup,aStem:aGroup2 --actAsPennId=GrouperSystem", " "));
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
