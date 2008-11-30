/*
 * @author mchyzer
 * $Id: GrouperClientWsTest.java,v 1.1 2008-11-30 10:57:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperClientWsTest extends GrouperTest {

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
          "--operation=addMember --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1", " "));
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
          "--operation=addMember --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1", " "));
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
            "--operation=addMember --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --ousdfsdfate=${index}", " "));
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
          "--operation=addMember --groupName=aStem:aGroup --subjectIds=test.subject.0,test.subject.1 --outputTemplate=${index}", " "));

      System.out.flush();
      
      output = new String(baos.toByteArray());
      
      System.setOut(systemOut);
      
      assertEquals("01", output);
      
    } finally {
      System.setOut(systemOut);
    }
    
  }
  
  
}
