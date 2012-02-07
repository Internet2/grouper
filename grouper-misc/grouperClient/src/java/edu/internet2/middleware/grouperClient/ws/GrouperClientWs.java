/*
 * @author mchyzer
 * $Id: GrouperClientWs.java,v 1.11 2009-11-17 06:25:04 mchyzer Exp $
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
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestResultProblem;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
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
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.Protocol;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
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
   * content type
   */
  private String contentType = null;
  
  /**
   * assign the content type, defaults to xml
   * @param theContentType
   * @return this for chaining
   */
  public GrouperClientWs assignContentType(String theContentType) {
    this.contentType = theContentType;
    return this;
  }
  
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
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
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
      if (LOG.isDebugEnabled()) {
        String theResponse = null;
        try {
          theResponse = GrouperClientUtils.responseBodyAsString(this.method);
        } catch (Exception e) {
          //ignore
        }
        LOG.debug("Response: " + theResponse);
      }
      throw new RuntimeException("Web service did not even respond! " + webServiceUrl(urlSuffix));
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
      Exception indentException = null;

      boolean isIndent = GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true);
      if (isIndent) {
        try {
          theResponse = GrouperClientUtils.indent(theResponse, true);
        } catch (Exception e) {
          indentException = e;
        }
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
        System.err.println("\n################ RESPONSE START " + (isIndent ? "(indented) " : "") + "###############\n");
        System.err.println(theResponseTotal);
        System.err.println("\n################ RESPONSE END ###############\n\n");
      }
      if (indentException != null) {
        throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
            "grouper.client.properties: grouperClient.logging.webService.indent", indentException);
      }
    }

    Object resultObject = toSend instanceof String ? this.response : this.xStream.fromXML(this.response);
    
    //see if problem
    if (resultObject instanceof WsRestResultProblem) {
      throw new RuntimeException(((WsRestResultProblem)resultObject).getResultMetadata().getResultMessage());
    }

    return resultObject;
  }

  /**
   * if failure, handle it
   * @param responseContainer is the object that everything marshaled to
   * @param resultMetadataHolders
   * @param resultMessage
   * @throws GcWebServiceError if there is a problem
   */
  public void handleFailure(Object responseContainer, ResultMetadataHolder[] resultMetadataHolders, String resultMessage) {
    // see if request worked or not
    if (!this.success) {
      StringBuilder error = new StringBuilder("Bad response from web service: resultCode: " + this.resultCode
        + ", " + resultMessage);
      int errorIndex = 0;
      for (int i=0;i<GrouperClientUtils.length(resultMetadataHolders);i++) {
        try {
          WsResultMeta resultMetadata = resultMetadataHolders[i].getResultMetadata();
          if (!GrouperClientUtils.equals(resultMetadata.getSuccess(), "T")) {
            error.append("\nError ").append(errorIndex).append(", result index: ").append(i).append(", code: ").append(resultMetadata.getResultCode())
              .append(", message: ").append(resultMetadata.getResultMessage());
            errorIndex++;
          }
        } catch (Exception e) {
          //object not there
          LOG.debug("issue with error message: ", e);
        }
      }
      throw new GcWebServiceError(responseContainer, error.toString());
    }

  }
  
  
  /**
   * http client
   * @return the http client
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  private static HttpClient httpClient() {
    
    //see if invalid SSL
    String httpsSocketFactoryName = GrouperClientUtils.propertiesValue("grouperClient.https.customSocketFactory", false);
    
    //is there overhead here?  should only do this once?
    //perhaps give a custom factory
    if (!GrouperClientUtils.isBlank(httpsSocketFactoryName)) {
      Class<? extends SecureProtocolSocketFactory> httpsSocketFactoryClass = GrouperClientUtils.forName(httpsSocketFactoryName);
      SecureProtocolSocketFactory httpsSocketFactoryInstance = GrouperClientUtils.newInstance(httpsSocketFactoryClass);
      Protocol easyhttps = new Protocol("https", httpsSocketFactoryInstance, 443);
      Protocol.registerProtocol("https", easyhttps);
    }
    
    HttpClient httpClient = new HttpClient();

    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
    
    int soTimeoutMillis = GrouperClientUtils.propertiesValueInt(
        "grouperClient.webService.httpSocketTimeoutMillis", 90000, true);
    
    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);
    
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

      String encryptKey = GrouperClientUtils.encryptKey();
      wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
      
    } else {
      passPrefix = "WebService pass: reading scalar value from grouper.client.properties";
    }
    
    if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.logMaskedPassword", false, false)) {
      LOG.debug(passPrefix + ": " + GrouperClientUtils.repeat("*", wsPass.length()));
    }

    Credentials defaultcreds = new UsernamePasswordCredentials(user, wsPass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);

    return httpClient;
  }

  /**
   * 
   * @param suffix of the url
   * @return the url
   */
  private String webServiceUrl(String suffix) {
    suffix = GrouperClientUtils.trimToEmpty(suffix);
    
    suffix = GrouperClientUtils.stripStart(suffix, "/");
    
    String url = GrouperClientUtils.propertiesValue("grouperClient.webService.url", true);
    
    url = GrouperClientUtils.stripEnd(url, "/");
    return url;
  }
    
  /**
   * @param suffix e.g. groups/aStem:aGroup/members
   * @param clientVersion
   * @return the method
   */
  private PostMethod postMethod(String suffix, String clientVersion) {
    
    String url = webServiceUrl(suffix);
    
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
    PostMethod postMethod = new PostMethod(url);

    //no keep alive so response is easier to indent for tests
    postMethod.setRequestHeader("Connection", "close");
    
    return postMethod;
  }

  /**
   * 
   * @param theXstream
   * @param urlSuffix to put on end of base url, e.g. groups/aStem:aGroup/members
   * @param objectToMarshall is the bean to convert to XML, or it could be a string of xml
   * @param logFile if not null, log the contents of the request there
   * @param responseCode array of size one to get the response code back
   * @param clientVersion 
   * @return the post method
   * @throws UnsupportedEncodingException 
   * @throws HttpException 
   * @throws IOException 
   */
  private PostMethod postMethod(XStream theXstream, 
      String urlSuffix, Object objectToMarshall, File logFile, int[] responseCode, String clientVersion) 
      throws UnsupportedEncodingException, HttpException, IOException {
    
    String theContentType = GrouperClientUtils.defaultIfBlank(this.contentType, "text/xml");
    
    HttpClient httpClient = httpClient();

    PostMethod postMethod = postMethod(urlSuffix, clientVersion);

    String requestDocument = objectToMarshall instanceof String ? (String)objectToMarshall : marshalObject(theXstream, objectToMarshall);
    
    postMethod.setRequestEntity(new StringRequestEntity(requestDocument, theContentType, "UTF-8"));
    
    if (logFile != null || GrouperClientLog.debugToConsole()) {
      if (logFile != null) {
        LOG.debug("WebService: logging request to: " + GrouperClientUtils.fileCanonicalPath(logFile));
      }
      String theRequestDocument = requestDocument;
      Exception indentException = null;
      boolean isIndent = GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.webService.indent", true, true);
      if (isIndent) {
        try {
          theRequestDocument = GrouperClientUtils.indent(theRequestDocument, true);
        } catch (Exception e) {
          indentException = e;
        }
      }

      StringBuilder headers = new StringBuilder();
//      POST /grouper-ws/servicesRest/v1_4_000/subjects HTTP/1.1
//      Connection: close
//      Authorization: Basic bWNoeXplcjpEaxxxxxxxxxx==
//      User-Agent: Jakarta Commons-HttpClient/3.1
//      Host: localhost:8090
//      Content-Length: 226
//      Content-Type: text/xml; charset=UTF-8
      headers.append("POST ").append(postMethod.getURI().getPathQuery()).append(" HTTP/1.1\n");
      headers.append("Connection: close\n");
      headers.append("Authorization: Basic xxxxxxxxxxxxxxxx\n");
      headers.append("User-Agent: Jakarta Commons-HttpClient/3.1\n");
      headers.append("Host: ").append(postMethod.getURI().getHost()).append(":")
        .append(postMethod.getURI().getPort()).append("\n");
      headers.append("Content-Length: ").append(
          postMethod.getRequestEntity().getContentLength()).append("\n");
      headers.append("Content-Type: ").append(
          postMethod.getRequestEntity().getContentType()).append("\n");
      headers.append("\n");
      
      String theRequest = headers + theRequestDocument;
      if (logFile != null) {
        GrouperClientUtils.saveStringIntoFile(logFile, theRequest);
      }
      if (GrouperClientLog.debugToConsole()) {
        System.err.println("\n################ REQUEST START " + (isIndent ? "(indented) " : "") + "###############\n");
        System.err.println(theRequest);
        System.err.println("\n################ REQUEST END ###############\n\n");
      }
      if (indentException != null) {
        throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
        		"grouper.client.properties: grouperClient.logging.webService.indent", indentException);
      }
    }
    
    mostRecentRequest = requestDocument;
    
    int responseCodeInt = httpClient.executeMethod(postMethod);

    if (responseCode != null && responseCode.length > 0) {
      responseCode[0] = responseCodeInt;
    }
    
    return postMethod;

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