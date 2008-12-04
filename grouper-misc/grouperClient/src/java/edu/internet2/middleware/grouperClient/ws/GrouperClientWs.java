/*
 * @author mchyzer
 * $Id: GrouperClientWs.java,v 1.4 2008-12-04 07:51:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
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
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpStatus;
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
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentRequest = null;
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentResponse = null;
  
  /**
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  public Object executeService(String urlSuffix, Object toSend, String labelForLog, String clientVersion) 
      throws UnsupportedEncodingException, HttpException, IOException {
    
    String logDir = GrouperClientUtils.propertiesValue("grouperClient.logging.webService.documentDir", false);
    File requestFile = null;
    File responseFile = null;
    
    if (!GrouperClientUtils.isBlank(logDir)) {
      
      logDir = GrouperClientUtils.stripEnd(logDir, "/");
      logDir = GrouperClientUtils.stripEnd(logDir, "\\");
      Date date = new Date();
      String logName = logDir  + File.separator + "wsLog_" 
        + new SimpleDateFormat("yyyy_MM").format(date)
        + File.separator + "day_" 
        + new SimpleDateFormat("dd" + File.separator + "HH_mm_ss_SSS").format(date)
        + "_" + ((int)(1000 * Math.random())) + "_" + labelForLog;
      
      requestFile = new File(logName + "_request.log");
      
      responseFile = new File(logName + "_response.log");

      //make parents
      GrouperClientUtils.mkdirs(requestFile.getParentFile());
      
    }
    int[] responseCode = new int[1];
    
    //make sure right content type is in request (e.g. application/xhtml+xml
    this.method = postMethod(this.xStream, urlSuffix, toSend, requestFile, responseCode, clientVersion);

    //make sure a request came back
    Header successHeader = this.method.getResponseHeader("X-Grouper-success");
    String successString = successHeader == null ? null : successHeader.getValue();
    if (GrouperClientUtils.isBlank(successString)) {
      throw new RuntimeException("Web service did not even respond!");
    }
    this.success = "T".equals(successString);
    this.resultCode = this.method.getResponseHeader("X-Grouper-resultCode").getValue();
    
    this.response = GrouperClientUtils.responseBodyAsString(this.method);

    mostRecentResponse = this.response;

    if (responseFile != null || GrouperClientLog.debugToConsole()) {
      if (responseFile != null) {
        LOG.debug("WebService: logging response to: " + GrouperClientUtils.fileCanonicalPath(responseFile));
      }
      
      String theResponse = this.response;
      if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true)) {
        theResponse = GrouperClientUtils.indent(theResponse, true);
      }
      
      StringBuilder headers = new StringBuilder();

      headers.append("HTTP/1.1 ").append(responseCode[0]).append(" ").append(HttpStatus.getStatusText(responseCode[0])).append("\n");
      
      for (Header header : this.method.getResponseHeaders()) {
        String name = header.getName();
        String value = header.getValue();
        
        //dont allow cookies to go to logs
        if (GrouperClientUtils.equals(name, "Set-Cookie")) {
          value = value.replaceAll("JSESSIONID=(.*)?;", "JSESSIONID=xxxxxxxxxxxx;");
        }
        headers.append(name).append(": ").append(value).append("\n");
      }
      headers.append("\n");
      String theResponseTotal = headers + theResponse;
      if (responseFile != null) {
        GrouperClientUtils.saveStringIntoFile(responseFile, theResponseTotal);
      }
      if (GrouperClientLog.debugToConsole()) {
        System.err.println("\n################ RESPONSE START ###############\n");
        System.err.println(theResponseTotal);
        System.err.println("\n################ RESPONSE END ###############\n\n");
      }
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
   * @param clientVersion
   * @return the method
   */
  private static PostMethod postMethod(String suffix, String clientVersion) {
    
    suffix = GrouperClientUtils.trimToEmpty(suffix);
    
    suffix = GrouperClientUtils.stripStart(suffix, "/");
    
    String url = GrouperClientUtils.propertiesValue("grouperClient.webService.url", true);
    
    url = GrouperClientUtils.stripEnd(url, "/");
    
    String webServiceVersion = GrouperClientUtils.propertiesValue("grouperClient.webService.client.version", true);

    if (!GrouperClientUtils.isBlank(clientVersion)) {
      webServiceVersion = clientVersion;
    }
    
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
   * @param responseCode array of size one to get the response code back
   * @param clientVersion 
   * @return the post method
   * @throws UnsupportedEncodingException 
   * @throws HttpException 
   * @throws IOException 
   */
  private static PostMethod postMethod(XStream xStream, 
      String urlSuffix, Object objectToMarshall, File logFile, int[] responseCode, String clientVersion) 
      throws UnsupportedEncodingException, HttpException, IOException {
    
    String contentType = "text/xml";
    
    HttpClient httpClient = httpClient();

    PostMethod method = postMethod(urlSuffix, clientVersion);

    String requestDocument = marshalObject(xStream, objectToMarshall);
    
    method.setRequestEntity(new StringRequestEntity(requestDocument, contentType, "UTF-8"));
    
    if (logFile != null || GrouperClientLog.debugToConsole()) {
      if (logFile != null) {
        LOG.debug("WebService: logging request to: " + GrouperClientUtils.fileCanonicalPath(logFile));
      }
      String theRequestDocument = requestDocument;
      if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true)) {
        theRequestDocument = GrouperClientUtils.indent(theRequestDocument, true);
      }

      StringBuilder headers = new StringBuilder();
//      POST /grouperWs/servicesRest/v1_4_000/subjects HTTP/1.1
//      Connection: close
//      Authorization: Basic bWNoeXplcjpEaTlyZWRwbw==
//      User-Agent: Jakarta Commons-HttpClient/3.1
//      Host: localhost:8090
//      Content-Length: 226
//      Content-Type: text/xml; charset=UTF-8
      headers.append("POST ").append(method.getURI().getPathQuery()).append(" HTTP/1.1\n");
      headers.append("Connection: close\n");
      headers.append("Authorization: Basic xxxxxxxxxxxxxxxx\n");
      headers.append("User-Agent: Jakarta Commons-HttpClient/3.1\n");
      headers.append("Host: ").append(method.getURI().getHost()).append(":")
        .append(method.getURI().getPort()).append("\n");
      headers.append("Content-Length: ").append(
          method.getRequestEntity().getContentLength()).append("\n");
      headers.append("Content-Type: ").append(
          method.getRequestEntity().getContentType()).append("\n");
      headers.append("\n");
      
      String theRequest = headers + theRequestDocument;
      if (logFile != null) {
        GrouperClientUtils.saveStringIntoFile(logFile, theRequest);
      }
      if (GrouperClientLog.debugToConsole()) {
        System.err.println("\n################ REQUEST START ###############\n");
        System.err.println(theRequest);
        System.err.println("\n################ REQUEST END ###############\n\n");
      }
    }
    
    mostRecentRequest = requestDocument;
    
    int responseCodeInt = httpClient.executeMethod(method);

    if (responseCode != null && responseCode.length > 0) {
      responseCode[0] = responseCodeInt;
    }
    
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