/*
 * @author mchyzer
 * $Id: SampleCapture.java,v 1.6 2008-03-29 10:50:45 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleDeleteMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleDeleteMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindGroups;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindStems;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleFindStemsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroups;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetGroupsLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembers;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleGetMembersLite;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleHasMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleHasMemberLite;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleAddMemberRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleAddMemberRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleAddMemberRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleDeleteMemberRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleDeleteMemberRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleDeleteMemberRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleFindGroupsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleFindGroupsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleFindStemsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleFindStemsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleGetGroupsRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleGetGroupsRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleGetGroupsRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleGetMembersRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleGetMembersRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleGetMembersRestLite2;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleHasMemberRest;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleHasMemberRestLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleHasMemberRestLite2;
import edu.internet2.middleware.grouper.ws.samples.types.WsSample;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleClientType;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.TcpCaptureServer;
import edu.internet2.middleware.subject.Subject;


/**
 * capture a sample and put in text file
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
    
//    captureAddMember();
//    captureDeleteMember();
//    captureHasMember();
//    captureGetGroups();
//    captureGetMembers();
//    captureFindGroups();
    captureFindStems();
  }

  /** certain data has to exist for samples to run */
  private static void setupData() {
    GrouperSession grouperSession = null;
    try {
      Subject grouperSystemSubject = SubjectFinder.findById("GrouperSystem");
      Subject subject1 = SubjectFinder.findById("10039438");
      Subject subject2 = SubjectFinder.findById("10021368");
      grouperSession = GrouperSession.start(grouperSystemSubject);
      
      Stem.saveStem(grouperSession, "aStem", "a stem description", "a stem", "aStem", null, null, false);
      
      Group aGroup = Group.saveGroup(grouperSession, "a group description", "a group", "aStem:aGroup", null, null, false);
      Group aGroup2 = Group.saveGroup(grouperSession, "a group description2", "a group2", "aStem:aGroup2", null, null, false);
      
      //make sure assigned
      aGroup.addMember(grouperSystemSubject, false);
      aGroup.addMember(subject1, false);
      aGroup.addMember(subject2, false);
      
      aGroup2.addMember(grouperSystemSubject, false);
      aGroup2.addMember(subject1, false);
      aGroup2.addMember(subject2, false);
      
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
        WsSampleGetGroupsLite.class, "getGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsRest.class, "getGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsRestLite.class, "getGroups", null);
    captureSample(WsSampleClientType.REST_BEANS,  
        WsSampleGetGroupsRestLite2.class, "getGroups", "_withInput");
    
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
        String samplesFolderName, String fileNameInfo) {
    
    Object[] formats = clientType.formats();
    //just pass null if none
    formats = GrouperUtil.defaultIfNull(formats, new Object[]{null});
    
    for (Object format : formats) {
      //make sure example supports the type
      if (clientType.validFormat(clientClass, format)) {
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
      
      String formatString = format == null ? "" : ("_" + ((Enum<?>)format).name());
      
      //assume parent dirs are there...
      File resultFile = new File("doc/samples/" + samplesFolderName + "/"
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
      System.out.println(error);
      LOG.error(error, e);
    }
  }
  
}
