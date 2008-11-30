/*
 * @author mchyzer
 * $Id: GrouperClientWs.java,v 1.1 2008-11-30 10:57:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientXstreamUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestResultProblem;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.CompactWriter;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Header;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpException;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * 
 */
public class GrouperClientWs {
  
  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperClientWs.class);

  /**
   * 
   */
  private XStream xStream;
  
  /**
   * 
   */
  private PostMethod method;
  
  /**
   * 
   */
  public GrouperClientWs() {
    this.xStream = GrouperClientXstreamUtils.retrieveXstream();
  }
  
  /** */
  private String response;
  
  /**
   * 
   */
  private boolean success = false;

  /**
   * 
   */
  private String resultCode = null;
  
  /**
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend
   * @param labelForLog label if the request is logged to file
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  public Object executeService(String urlSuffix, Object toSend, String labelForLog) 
      throws UnsupportedEncodingException, HttpException, IOException {
    
    String logDir = GrouperClientUtils.propertiesValue("grouperClient.logging.webService.documentDir", false);
    File requestFile = null;
    File responseFile = null;
    
    if (!GrouperClientUtils.isBlank(logDir)) {
      
      logDir = GrouperClientUtils.stripEnd(logDir, "/");
      logDir = GrouperClientUtils.stripEnd(logDir, "\\");
      String logName = logDir  + File.separator + "wsLog_" 
        + new SimpleDateFormat(
            "yyyy_MM" + File.separator + "dd_HH_mm_ss_SSS").format(new Date())      
        + "_" + ((int)(1000 * Math.random())) + "_" + labelForLog;
      
      requestFile = new File(logName + "_request.log");
      responseFile = new File(logName + "_response.log");
      
      //make parents
      GrouperClientUtils.mkdirs(requestFile.getParentFile());
      
    }
    
    //make sure right content type is in request (e.g. application/xhtml+xml
    this.method = postMethod(this.xStream, urlSuffix, toSend, requestFile);

    //make sure a request came back
    Header successHeader = this.method.getResponseHeader("X-Grouper-success");
    String successString = successHeader == null ? null : successHeader.getValue();
    if (GrouperClientUtils.isBlank(successString)) {
      throw new RuntimeException("Web service did not even respond!");
    }
    this.success = "T".equals(successString);
    this.resultCode = this.method.getResponseHeader("X-Grouper-resultCode").getValue();
    
    this.response = GrouperClientUtils.responseBodyAsString(this.method);

    if (responseFile != null) {
      LOG.debug("WebService: logging response to: " + GrouperClientUtils.fileCanonicalPath(responseFile));
      
      String theResponse = this.response;
      if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true)) {
        theResponse = GrouperClientUtils.indent(theResponse, true);
      }
      
      GrouperClientUtils.saveStringIntoFile(responseFile, theResponse);
    }

    Object resultObject = this.xStream.fromXML(this.response);
    
    //see if problem
    if (resultObject instanceof WsRestResultProblem) {
      throw new RuntimeException(((WsRestResultProblem)resultObject).getResultMetadata().getResultMessage());
    }

    return resultObject;
  }

  /**
   * if failure, handle it
   * @param resultMessage
   */
  public void handleFailure(String resultMessage) {
    // see if request worked or not
    if (!this.success) {
      throw new RuntimeException("Bad response from web service: resultCode: " + this.resultCode
          + ", " + resultMessage);
    }

  }
  
  
  /**
     * http client
     * @return the http client
     */
    private static HttpClient httpClient() {
      HttpClient httpClient = new HttpClient();

      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      httpClient.getParams().setAuthenticationPreemptive(true);
      
      int soTimeoutMillis = GrouperClientUtils.propertiesValueInt(
          "grouperClient.webService.httpSocketTimeoutMillis", 90000, true);
      
      httpClient.getParams().setSoTimeout(soTimeoutMillis);

      int connectionManagerMillis = GrouperClientUtils.propertiesValueInt(
          "grouperClient.webService.httpConnectionManagerTimeoutMillis", 90000, true);
      
      httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

      String userLabel = GrouperClientUtils.propertiesValue("grouperClient.webService.user.label", true);
      String user = GrouperClientUtils.propertiesValue("grouperClient.webService." + userLabel, true);
      
      LOG.debug("WebService: connecting as user: '" + user + "'");
      
      boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
          "encrypt.disableExternalFileLookup", false, true);
      
      //lets lookup if file
      String wsPass = GrouperClientUtils.propertiesValue("grouperClient.webService.password", true);
      String wsPassFromFile = GrouperClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);

      String passPrefix = null;

      if (!GrouperClientUtils.equals(wsPass, wsPassFromFile)) {

        passPrefix = "WebService pass: reading encrypted value from file: " + wsPass;

        String encryptKey = GrouperClientUtils.propertiesValue("encrypt.key", true);
        
        wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
        
      } else {
        passPrefix = "WebService pass: reading scalar value from grouper.client.properties";
      }
      
      LOG.debug(passPrefix + ": " + GrouperClientUtils.repeat("*", wsPass.length()));

      Credentials defaultcreds = new UsernamePasswordCredentials(user, wsPass);
  
      //set auth scope to null and negative so it applies to all hosts and ports
      httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);
  
      return httpClient;
    }

  /**
   * @param suffix e.g. groups/aStem:aGroup/members
   * @return the method
   */
  private static PostMethod postMethod(String suffix) {
    
    suffix = GrouperClientUtils.trimToEmpty(suffix);
    
    suffix = GrouperClientUtils.stripStart(suffix, "/");
    
    String url = GrouperClientUtils.propertiesValue("grouperClient.webService.url", true);
    
    url = GrouperClientUtils.stripEnd(url, "/");
    
    String webServiceVersion = GrouperClientUtils.propertiesValue("grouperClient.webService.client.version", true);

    webServiceVersion = GrouperClientUtils.stripStart(webServiceVersion, "/");
    webServiceVersion = GrouperClientUtils.stripEnd(webServiceVersion, "/");

    url = url + "/" + webServiceVersion + "/" + suffix;

    LOG.debug("WebService: connecting to URL: '" + url + "'");

    //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    PostMethod method = new PostMethod(url);

    //no keep alive so response if easier to indent for tests
    method.setRequestHeader("Connection", "close");
    
    return method;
  }

  /**
   * 
   * @param xStream
   * @param urlSuffix to put on end of base url, e.g. groups/aStem:aGroup/members
   * @param objectToMarshall
   * @param logFile if not null, log the contents of the request there
   * @return the post method
   * @throws UnsupportedEncodingException 
   * @throws HttpException 
   * @throws IOException 
   */
  private static PostMethod postMethod(XStream xStream, String urlSuffix, Object objectToMarshall, File logFile) 
      throws UnsupportedEncodingException, HttpException, IOException {
    
    String contentType = "text/xml";
    
    HttpClient httpClient = httpClient();

    PostMethod method = postMethod(urlSuffix);

    String requestDocument = marshalObject(xStream, objectToMarshall);
    
    if (logFile != null) {
      LOG.debug("WebService: logging request to: " + GrouperClientUtils.fileCanonicalPath(logFile));
      String theRequestDocument = requestDocument;
      if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true)) {
        theRequestDocument = GrouperClientUtils.indent(theRequestDocument, true);
      }

      GrouperClientUtils.saveStringIntoFile(logFile, theRequestDocument);
    }
    
    method.setRequestEntity(new StringRequestEntity(requestDocument, contentType, "UTF-8"));
    
    httpClient.executeMethod(method);

    return method;

  }
  
  /**
   * 
   * @param xStream
   * @param object
   * @return the xml
   */
  private static String marshalObject(XStream xStream, Object object) {
    StringWriter stringWriter = new StringWriter();
    //dont indent
    xStream.marshal(object, new CompactWriter(stringWriter));

    String requestDocument = stringWriter.toString();
    return requestDocument;
  }
  


}