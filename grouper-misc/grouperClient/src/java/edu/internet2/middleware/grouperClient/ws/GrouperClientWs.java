/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: GrouperClientWs.java,v 1.11 2009-11-17 06:25:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import edu.internet2.middleware.grouperClient.GrouperClientState;
import edu.internet2.middleware.grouperClient.GrouperClientWsException;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestResultProblem;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.morphString.Crypto;


/**
 * this is the client that all requests go through.  if you add an instance field, make sure to add to copyFrom()
 */
public class GrouperClientWs {
  
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
   * result marshaled from WS
   */
  private Object result = null;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * ws pass
   */
  private String wsPass;

  /**
   * ws user
   */
  private String wsUser;
  
  /**
   * copy from the argument to this object
   * @param grouperClientWs
   */
  public void copyFrom(GrouperClientWs grouperClientWs) {
    this.response = grouperClientWs.response;
    //dont copy result
    this.resultCode = grouperClientWs.resultCode;
    this.success = grouperClientWs.success;
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperClientWs.class);

  /**
   * 
   */
  public GrouperClientWs() {
  }
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentRequest = null;
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentResponse = null;

  /** readonly failover config name */
  public static final String READ_ONLY_FAILOVER_CONFIG_NAME = "grouperWsReadOnly";
  
  /** readwrite failover config name */
  public static final String READ_WRITE_FAILOVER_CONFIG_NAME = "grouperWsReadWrite";

  /**
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param readOnly true if readonly, false if readwrite
   * @return the response object
   */
  public Object executeService(final String urlSuffix, final Object toSend, 
      final String labelForLog, final String clientVersion, final String contentType, final boolean readOnly)  {

    GrouperClientWs grouperClientWs = null;

    if (StringUtils.isBlank(this.wsEndpoint)) {

      if (!StringUtils.isBlank(this.wsUser)) {
        throw new RuntimeException("wsUser is forbidden if wsEndpoint is not used");
      }
      if (!StringUtils.isBlank(this.wsPass)) {
        throw new RuntimeException("wsPass is forbidden if wsEndpoint is not used");
      }
      

      String url = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService.url");
      
      grouperClientWs = executeServiceHelper(url, 
          urlSuffix, toSend, labelForLog, clientVersion, contentType, false, this);
      
    } else {
      
      if (StringUtils.isBlank(this.wsUser)) {
        throw new RuntimeException("wsUser is required if wsEndpoint is used");
      }
      if (StringUtils.isBlank(this.wsPass)) {
        throw new RuntimeException("wsPass is required if wsEndpoint is used");
      }

      grouperClientWs = executeServiceHelper(this.wsEndpoint, 
          urlSuffix, toSend, labelForLog, clientVersion, contentType, false, this);
    }

    if (grouperClientWs != null) {
      //copy from the instance back to this
      this.copyFrom(grouperClientWs);
      return grouperClientWs.result;
      
    }
    
    return null;
  }

  /**
   * Response headers.
   */
  private Map<String, String> responseHeaders = new LinkedHashMap<String, String>();
  
  /**
   * Response headers lower case.
   */
  private Map<String, String> responseHeadersLower = new LinkedHashMap<String, String>();


  /**
   * @param url to hit, could be multiple
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param objectToMarshall is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion
   * @param contentType
   * @param exceptionOnNonSuccess if non success should exception be thrown
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  private static GrouperClientWs executeServiceHelper(String url, String urlSuffix, Object objectToMarshall, String labelForLog, 
      String clientVersion, String contentType, boolean exceptionOnNonSuccess, GrouperClientWs originalGrouperClientWs)  {
    
    GrouperClientWs grouperClientWs = new GrouperClientWs();

    if (originalGrouperClientWs != null) {
      grouperClientWs.wsEndpoint = originalGrouperClientWs.wsEndpoint;
      grouperClientWs.wsUser = originalGrouperClientWs.wsUser;
      grouperClientWs.wsPass = originalGrouperClientWs.wsPass;
    }
    
    String logDir = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.logging.webService.documentDir");
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

    // Get an http client.
    CloseableHttpClient closeableHttpClient = null;
    HttpRequestBase httpRequestBase = null;
    CloseableHttpResponse closeableHttpResponse = null;

    try {
      
      //see if invalid SSL
      String httpsSocketFactoryName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.https.customSocketFactory");
      
      //perhaps give a custom factory
      if (StringUtils.equals(httpsSocketFactoryName, "edu.internet2.middleware.grouperClient.ssl.EasySslSocketFactory")) {
        closeableHttpClient = GrouperClientUtils.httpTrustAllClient(true);
      } else {
        closeableHttpClient = GrouperClientUtils.httpClient(true);
      }

      int soTimeoutMillis = GrouperClientConfig.retrieveConfig().propertyValueInt(
          "grouperClient.webService.httpSocketTimeoutMillis", 90000);

      int connectionManagerMillis = GrouperClientConfig.retrieveConfig().propertyValueInt(
          "grouperClient.webService.httpConnectionManagerTimeoutMillis", 90000);

      RequestConfig.Builder config = RequestConfig.custom()
          .setConnectionRequestTimeout(connectionManagerMillis)
          .setConnectTimeout(connectionManagerMillis)
          .setSocketTimeout(soTimeoutMillis);
        

      String theContentType = GrouperClientUtils.defaultIfBlank(contentType, "application/json");
      
      url = GrouperClientUtils.stripEnd(url, "/");
      
      // grouperClient.webService.client.version = v2_5_000
      String webServiceVersion = GrouperClientUtils.grouperClientVersion();
          
      if (!GrouperClientUtils.isBlank(clientVersion)) {
        webServiceVersion = clientVersion;
      }
      
      webServiceVersion = GrouperClientUtils.stripStart(webServiceVersion, "/");
      webServiceVersion = GrouperClientUtils.stripEnd(webServiceVersion, "/");

      url = url + "/" + webServiceVersion + "/" + urlSuffix;

      {
        String debugMessage = "WebService: connecting to URL: '" + url + "'";
        LOG.debug(debugMessage);
        
        if (GrouperClientLog.debugToConsoleByFlag()) {
          System.err.println(debugMessage);
        }

      }
      
      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      httpRequestBase = new HttpPost(url);
      httpRequestBase.setConfig(config.build());

      if (StringUtils.isBlank(grouperClientWs.wsEndpoint)) {

        String userLabel = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService.user.label");
        String user = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService." + userLabel);
    
        {
          String debugMessage = "WebService: connecting as user: '" + user + "'";
          LOG.debug(debugMessage);
          if (GrouperClientLog.debugToConsoleByFlag()) {
            System.err.println(debugMessage);
          }
        }
        
        boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBoolean(
            "encrypt.disableExternalFileLookup", false);
        
        //lets lookup if file
        String theWsPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService.password");
        String wsPassFromFile = GrouperClientUtils.readFromFileIfFile(theWsPass, disableExternalFileLookup);
    
        String passPrefix = null;
    
        if (!GrouperClientUtils.equals(theWsPass, wsPassFromFile)) {
    
          passPrefix = "WebService pass: reading encrypted value from file: " + theWsPass;
    
          String encryptKey = GrouperClientUtils.encryptKey();
          theWsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
          
        } else {
          passPrefix = "WebService pass: reading scalar value from grouper.client.properties";
        }
        
        if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.logging.logMaskedPassword", false)) {
          LOG.debug(passPrefix + ": " + GrouperClientUtils.repeat("*", theWsPass.length()));
        }
    
  
        String authenticationString = GrouperClientUtils.httpBasicAuthenticationString(user, theWsPass); 
        httpRequestBase.addHeader("Authorization", authenticationString);
      } else {
        if (StringUtils.isBlank(grouperClientWs.wsUser)) {
          throw new RuntimeException("wsUser is required!");
        }
        if (StringUtils.isBlank(grouperClientWs.wsPass)) {
          throw new RuntimeException("wsPass is required!");
        }
        String authenticationString = GrouperClientUtils.httpBasicAuthenticationString(grouperClientWs.wsUser, grouperClientWs.wsPass); 
        httpRequestBase.addHeader("Authorization", authenticationString);
        
      }

      //no keep alive so response is easier to indent for tests
      httpRequestBase.setHeader("Connection", "close");
  
      String requestDocument = objectToMarshall instanceof String ? (String)objectToMarshall : marshalObject(objectToMarshall);
      
      if (!GrouperClientUtils.isBlank(requestDocument)) {
        try {
          ((HttpPost)httpRequestBase).setEntity(new StringEntity(requestDocument, ContentType.create(theContentType, Consts.UTF_8)));

        } catch (UnsupportedCharsetException uee) {
          throw new RuntimeException(uee);
        }
      }

      Map<String, String> requestHeaders = new LinkedHashMap<String, String>();

      GrouperClientState grouperClientState = GrouperClientState.retrieveGrouperClientState(false);
      
      if (grouperClientState != null) {
        if (!StringUtils.isBlank(grouperClientState.getXcorrelationId())) {
          httpRequestBase.addHeader("X-Correlation-Id", grouperClientState.getXcorrelationId());
        }
        if (!StringUtils.isBlank(grouperClientState.getXrequestId())) {
          httpRequestBase.addHeader("X-Request-Id", grouperClientState.getXrequestId());
        }
        
        if (!StringUtils.isBlank(grouperClientState.getGrouperActAsSourceId())) {
          
          if (!StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectId()) 
              && !StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectIdentifier())) {
            throw new RuntimeException("You can only have one of grouperActAsSubjectId or grouperActAsSubjectIdentifier set!");
          }

          if (StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectId()) 
              && StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectIdentifier())) {
            throw new RuntimeException("You must have one of grouperActAsSubjectId or grouperActAsSubjectIdentifier set if grouperActAsSourceId is set!");
          }
          
          requestHeaders.put("X-Grouper-actAsSourceId", grouperClientState.getGrouperActAsSourceId());

          if (!StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectId())) {
            requestHeaders.put("X-Grouper-actAsSubjectId", grouperClientState.getGrouperActAsSubjectId());
          } else if (!StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectIdentifier())) {
            requestHeaders.put("X-Grouper-actAsSubjectIdentifier", grouperClientState.getGrouperActAsSubjectIdentifier());
          }

          
        } else {
          if (!StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectId()) 
              || !StringUtils.isBlank(grouperClientState.getGrouperActAsSubjectIdentifier())) {
            throw new RuntimeException("If grouperActAsSubjectId or grouperActAsSubjectIdentifier is set, then you must have a grouperActAsSourceId!");
          }
        }
      }

      for (String requestHeaderKey : requestHeaders.keySet()) {
        httpRequestBase.addHeader(requestHeaderKey, new String(new Base64().encode(requestHeaders.get(requestHeaderKey).getBytes("UTF-8"))));
      }

      if (requestFile != null || GrouperClientLog.debugToConsoleByFlag()) {
        if (requestFile != null) {
          LOG.debug("WebService: logging request to: " + GrouperClientUtils.fileCanonicalPath(requestFile));
        }
        String theRequestDocument = requestDocument;
        Exception indentException = null;
        boolean isIndent = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired("grouperClient.logging.webService.indent");
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
        headers.append("POST ").append(httpRequestBase.getURI().getRawPath()).append(" HTTP/1.1\n");
        headers.append("Connection: close\n");
        headers.append("Authorization: Basic xxxxxxxxxxxxxxxx\n");
        headers.append("User-Agent: Jakarta Commons-HttpClient/3.1\n");
        headers.append("Host: ").append(httpRequestBase.getURI().getHost()).append(":")
          .append(httpRequestBase.getURI().getPort()).append("\n");
        headers.append("Content-Length: ").append(
            requestDocument.length()).append("\n");
        headers.append("Content-Type: ").append(
            theContentType).append("\n");
        if (grouperClientState != null) {
          if (!StringUtils.isBlank(grouperClientState.getXcorrelationId())) {
            headers.append("X-Correlation-Id: ").append(grouperClientState.getXcorrelationId()).append("\n");
          }
          if (!StringUtils.isBlank(grouperClientState.getXrequestId())) {
            headers.append("X-Request-Id: ").append(grouperClientState.getXrequestId()).append("\n");
          }
        }
        for (String requestHeaderKey : requestHeaders.keySet()) {
          headers.append(requestHeaderKey).append(": ").append(new String(new Base64().encode(requestHeaders.get(requestHeaderKey).getBytes("UTF-8")))).append("\n");
        }

        headers.append("\n");
        
        String theRequest = headers + theRequestDocument;
        if (requestFile != null) {
          GrouperClientUtils.saveStringIntoFile(requestFile, theRequest);
        }
        if (GrouperClientLog.debugToConsoleByFlag()) {
          System.err.println("\n################ REQUEST START " + (isIndent ? "(indented) " : "") + "###############\n");
          System.err.println(theRequest);
          System.err.println("\n################ REQUEST END ###############\n\n");
        }
        if (indentException != null) {
          throw new RuntimeException("Problems indenting json (is it valid?), turn off the indenting in the " +
              "grouper.client.properties: grouperClient.logging.webService.indent", indentException);
        }
      }
      
      mostRecentRequest = requestDocument;
      
      closeableHttpResponse = closeableHttpClient.execute(httpRequestBase);
      int responseCodeInt = closeableHttpResponse.getStatusLine().getStatusCode();

      if (responseCode != null && responseCode.length > 0) {
        responseCode[0] = responseCodeInt;
      }
      
      grouperClientWs.responseHeadersLower.clear();
      grouperClientWs.responseHeaders.clear();
      
      for (Header header : GrouperClientUtils.nonNull(closeableHttpResponse.getAllHeaders(), Header.class)) {
        grouperClientWs.responseHeadersLower.put(header.getName().toLowerCase(), header.getValue());
        grouperClientWs.responseHeaders.put(header.getName(), header.getValue());
      }

      //make sure a request came back
      String successString = grouperClientWs.responseHeaders.get("X-Grouper-success");
      if (GrouperClientUtils.isBlank(successString)) {
        if (LOG.isDebugEnabled()) {
          String theResponse = null;
          try {
            theResponse = GrouperClientUtils.responseBodyAsString(closeableHttpResponse);
          } catch (Exception e) {
            //ignore
          }
          LOG.debug("Response: " + theResponse);
        }
        throw new RuntimeException("Web service did not even respond! " + url);
      }
      grouperClientWs.success = "T".equals(successString);
      grouperClientWs.resultCode = grouperClientWs.responseHeaders.get("X-Grouper-resultCode");
      grouperClientWs.response = GrouperClientUtils.responseBodyAsString(closeableHttpResponse);
      
      mostRecentResponse = grouperClientWs.response;

      if (responseFile != null || GrouperClientLog.debugToConsoleByFlag()) {
        if (responseFile != null) {
          LOG.debug("WebService: logging response to: " + GrouperClientUtils.fileCanonicalPath(responseFile));
        }
        
        String theResponse = grouperClientWs.response;
        Exception indentException = null;

        boolean isIndent = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired("grouperClient.logging.webService.indent");
        if (isIndent) {
          try {
            theResponse = GrouperClientUtils.indent(theResponse, true);
          } catch (Exception e) {
            indentException = e;
          }
        }
        
        StringBuilder headers = new StringBuilder();

        headers.append("HTTP/1.1 ").append(responseCode[0]).append("\n");
        
        for (String name : grouperClientWs.responseHeaders.keySet()) {
          String value = grouperClientWs.responseHeaders.get(name);
          
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
        if (GrouperClientLog.debugToConsoleByFlag()) {
          System.err.println("\n################ RESPONSE START " + (isIndent ? "(indented) " : "") + "###############\n");
          System.err.println(theResponseTotal);
          System.err.println("\n################ RESPONSE END ###############\n\n");
        }
        if (indentException != null) {
          throw new RuntimeException("Problems indenting json (is it valid?), turn off the indenting in the " +
              "grouper.client.properties: grouperClient.logging.webService.indent", indentException);
        }
      }

      Object resultObject = objectToMarshall instanceof String ? grouperClientWs.response : GrouperClientUtils.jsonConvertFrom(WsRestClassLookup.getAliasClassMap(), grouperClientWs.response);
      
      //see if problem
      if (resultObject instanceof WsRestResultProblem) {
        throw new GrouperClientWsException(resultObject, ((WsRestResultProblem)resultObject).getResultMetadata().getResultMessage());
      }

      if (exceptionOnNonSuccess && !grouperClientWs.success) {
        throw new GrouperClientWsException(resultObject, "Result code: " + grouperClientWs.resultCode + ", on url: " + url );
      }
      grouperClientWs.result = resultObject;

    } catch (Exception e) {
      
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      
      throw new RuntimeException("Problem in url: " + url, e);
    } finally {
      GrouperClientUtils.httpCloseQuietly(closeableHttpClient, httpRequestBase, closeableHttpResponse, false);
    }
    
    return grouperClientWs;
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
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GrouperClientWs assignWsEndpoint(String theWsEndpoint) {
    this.wsEndpoint = theWsEndpoint;
    return this;
  }

  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GrouperClientWs assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }

  /**
   * ws user
   * @param theWsUser
   * @return this for chaining
   */
  public GrouperClientWs assignWsUser(String theWsUser) {
    this.wsUser = theWsUser;
    return this;
  }

  /**
   * 
   * @param xStream
   * @param object
   * @return the xml
   */
  private static String marshalObject(Object object) {
    return GrouperClientUtils.jsonConvertTo(object, true);
  }
  


}
