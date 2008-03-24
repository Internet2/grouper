/*
 * @author mchyzer
 * $Id: SampleCapture.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMember;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMemberManualXmlHttp;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMemberSimple;
import edu.internet2.middleware.grouper.webservicesClient.WsSampleAddMemberSimpleManualXmlHttp;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleAddMemberLite;
import edu.internet2.middleware.grouper.ws.samples.rest.WsSampleAddMemberSimpleLite;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.TcpCaptureServer;


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
    
    captureAddMember();
  }

  /**
   * all add member captures
   */
  public static void captureAddMember() {
    captureSample(WsSampleClientType.GENERATED_SOAP_XML_HTTP,  
        WsSampleAddMember.class, "addMember", null);
    captureSample(WsSampleClientType.GENERATED_SOAP_XML_HTTP,  
        WsSampleAddMemberSimple.class, "addMember", null);
    captureSample(WsSampleClientType.MANUAL_XML_HTTP,  
        WsSampleAddMemberManualXmlHttp.class, "addMember", null);
    captureSample(WsSampleClientType.MANUAL_XML_HTTP,  
        WsSampleAddMemberSimpleManualXmlHttp.class, "addMember", null);
    captureSample(WsSampleClientType.MANUAL_LITE_REST,  
        WsSampleAddMemberLite.class, "addMember", null);
    captureSample(WsSampleClientType.MANUAL_LITE_REST,  
        WsSampleAddMemberSimpleLite.class, "addMember", null);
    
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
          + clientClass.toString() + e.getMessage();
      System.out.println(error);
      LOG.error(error, e);
    }
  }
  
}
