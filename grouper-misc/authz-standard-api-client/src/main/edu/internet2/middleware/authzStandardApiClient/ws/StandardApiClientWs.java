/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: StandardApiClientWs.java,v 1.11 2009-11-17 06:25:04 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.ws;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacResponseBeanBase;
import edu.internet2.middleware.authzStandardApiClient.exceptions.StandardApiClientWsException;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientConfig;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientLog;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;
import edu.internet2.middleware.authzStandardApiClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.Header;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.HttpException;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.HttpMethodBase;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.HttpStatus;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.protocol.Protocol;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import edu.internet2.middleware.authzStandardApiClientExt.org.apache.commons.logging.Log;

/**
 * this is the client that all requests go through.  if you add an instance field, make sure to add to copyFrom()
 * T is the result type
 */
public class StandardApiClientWs<T extends AsacResponseBeanBase> {
  
  /**
   * 
   */
  private HttpMethodBase method;
  
  /** */
  private String response;
  
  /**
   * logger
   */
  private static Log LOG = StandardApiClientUtils.retrieveLog(StandardApiClientWs.class);

  /**
   * 
   */
  public StandardApiClientWs() {
  }
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentRequest = null;
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentResponse = null;

  /** keep a reference to the most recent for testing */
  public static int mostRecentHttpStatusCode = -1;

  /** keep a reference to the most recent for testing */
  public static HttpMethodBase mostRecentHttpMethod = null;

  /**
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param expectedResultClass is the class that the result should be
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  public T executeService(final String urlSuffix, final Object toSend, 
      final String labelForLog, final String clientVersion,  
      AsacRestContentType asacRestContentType, 
      Class<? extends AsacResponseBeanBase> expectedResultClass,
      AsacRestHttpMethod asacRestHttpMethod)  {
    
    String url = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("authzStandardApiClient.webService.url");
    
    //copy the standardApi client ws instance to this
    
    //if not last connection then throw exception if not success.  If last connection then return the object
    return executeServiceHelper(url, 
        urlSuffix, toSend, labelForLog, clientVersion, asacRestContentType, expectedResultClass,
        asacRestHttpMethod);
    
  }

  /**
   * @param url to hit, could be multiple
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param exceptionOnNonSuccess if non success should exception be thrown
   * @param expectedResultClass is the class which is expected
   * @param asacRestHttpMethod
   * @return the response object
   * @throws UnsupportedEncodingException
   * @throws HttpException
   * @throws IOException
   */
  private T executeServiceHelper(String url, String urlSuffix, Object toSend, String labelForLog, String clientVersion,
      AsacRestContentType asacRestContentType, 
      Class<? extends AsacResponseBeanBase> expectedResultClass, AsacRestHttpMethod asacRestHttpMethod)  {
    
    mostRecentHttpStatusCode = -1;
    mostRecentHttpMethod = null;
        
    String logDir = StandardApiClientConfig.retrieveConfig().propertyValueString("authzStandardApiClient.logging.webService.documentDir");
    File requestFile = null;
    File responseFile = null;
    
    if (!StandardApiClientUtils.isBlank(logDir)) {
      
      logDir = StandardApiClientUtils.stripEnd(logDir, "/");
      logDir = StandardApiClientUtils.stripEnd(logDir, "\\");
      Date date = new Date();
      String logName = logDir  + File.separator + "wsLog_" 
        + new SimpleDateFormat("yyyy_MM").format(date)
        + File.separator + "day_" 
        + new SimpleDateFormat("dd" + File.separator + "HH_mm_ss_SSS").format(date)
        + "_" + ((int)(1000 * Math.random())) + "_" + labelForLog;
      
      requestFile = new File(logName + "_request.log");
      
      responseFile = new File(logName + "_response.log");

      //make parents
      StandardApiClientUtils.mkdirs(requestFile.getParentFile());
      
    }
    int[] responseCode = new int[1];
    
    //make sure right content type is in request (e.g. application/xhtml+xml
    this.method = this.method(url, urlSuffix, 
        toSend, requestFile, responseCode, clientVersion, asacRestContentType,
        asacRestHttpMethod);

    //make sure a request came back
    this.response = StandardApiClientUtils.responseBodyAsString(this.method);

    mostRecentResponse = this.response;

    if (responseFile != null || StandardApiClientLog.debugToConsole()) {
      if (responseFile != null) {
        LOG.debug("WebService: logging response to: " + StandardApiClientUtils.fileCanonicalPath(responseFile));
      }
      
      String theResponse = this.response;
      Exception indentException = null;

      boolean isIndent = StandardApiClientConfig.retrieveConfig().propertyValueBooleanRequired("authzStandardApiClient.logging.webService.indent");
      if (isIndent) {
        try {
          theResponse = asacRestContentType.indent(theResponse);
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
        if (StandardApiClientUtils.equals(name, "Set-Cookie")) {
          value = value.replaceAll("JSESSIONID=(.*)?;", "JSESSIONID=xxxxxxxxxxxx;");
        }
        headers.append(name).append(": ").append(value).append("\n");
      }
      headers.append("\n");
      String theResponseTotal = headers + theResponse;
      if (responseFile != null) {
        StandardApiClientUtils.saveStringIntoFile(responseFile, theResponseTotal);
      }
      if (StandardApiClientLog.debugToConsole()) {
        System.err.println("\n################ RESPONSE START " + (isIndent ? "(indented) " : "") + "###############\n");
        System.err.println(theResponseTotal);
        System.err.println("\n################ RESPONSE END ###############\n\n");
      }
      if (indentException != null) {
        throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
            "standardApi.client.properties: authzStandardApiClient.logging.webService.indent", indentException);
      }
    }

    @SuppressWarnings("unchecked")
    T resultObject = (T)asacRestContentType.parseString(expectedResultClass, this.response, new StringBuilder());
    
    //see if problem
    if (!StandardApiClientUtils.isBlank(resultObject.getError()) ||
        (resultObject.getResponseMeta() != null && !resultObject.getMeta().getSuccess())) {
      throw new StandardApiClientWsException(resultObject, resultObject.getError());
    }

    return resultObject;
  }

//  /**
//   * if failure, handle it
//   * @param responseContainer is the object that everything marshaled to
//   * @param resultMetadataHolders
//   * @param resultMessage
//   * @throws GcWebServiceError if there is a problem
//   */
//  public void handleFailure(Object responseContainer, ResultMetadataHolder[] resultMetadataHolders, String resultMessage) {
//    // see if request worked or not
//    if (!this.success) {
//      StringBuilder error = new StringBuilder("Bad response from web service: resultCode: " + this.resultCode
//        + ", " + resultMessage);
//      int errorIndex = 0;
//      for (int i=0;i<StandardApiClientUtils.length(resultMetadataHolders);i++) {
//        try {
//          WsResultMeta resultMetadata = resultMetadataHolders[i].getResultMetadata();
//          if (!StandardApiClientUtils.equals(resultMetadata.getSuccess(), "T")) {
//            error.append("\nError ").append(errorIndex).append(", result index: ").append(i).append(", code: ").append(resultMetadata.getResultCode())
//              .append(", message: ").append(resultMetadata.getResultMessage());
//            errorIndex++;
//          }
//        } catch (Exception e) {
//          //object not there
//          LOG.debug("issue with error message: ", e);
//        }
//      }
//      throw new GcWebServiceError(responseContainer, error.toString());
//    }
//
//  }
  
  
  /**
   * http client
   * @return the http client
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  private static HttpClient httpClient() {
    
    //see if invalid SSL
    String httpsSocketFactoryName = StandardApiClientConfig.retrieveConfig().propertyValueString("authzStandardApiClient.https.customSocketFactory");
    
    //is there overhead here?  should only do this once?
    //perhaps give a custom factory
    if (!StandardApiClientUtils.isBlank(httpsSocketFactoryName)) {
      Class<? extends SecureProtocolSocketFactory> httpsSocketFactoryClass = StandardApiClientUtils.forName(httpsSocketFactoryName);
      SecureProtocolSocketFactory httpsSocketFactoryInstance = StandardApiClientUtils.newInstance(httpsSocketFactoryClass);
      Protocol easyhttps = new Protocol("https", httpsSocketFactoryInstance, 443);
      Protocol.registerProtocol("https", easyhttps);
    }
    
    HttpClient httpClient = new HttpClient();

    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
    
    int soTimeoutMillis = StandardApiClientConfig.retrieveConfig().propertyValueIntRequired(
        "authzStandardApiClient.webService.httpSocketTimeoutMillis");
    
    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);
    
    int connectionManagerMillis = StandardApiClientConfig.retrieveConfig().propertyValueIntRequired(
        "authzStandardApiClient.webService.httpConnectionManagerTimeoutMillis");
    
    httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

    String user = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("authzStandardApiClient.webService.login");
    
    LOG.debug("WebService: connecting as user: '" + user + "'");
    
    boolean disableExternalFileLookup = StandardApiClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");
    
    //lets lookup if file
    String wsPass = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("authzStandardApiClient.webService.password");
    String wsPassFromFile = StandardApiClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);

    String passPrefix = null;

    if (!StandardApiClientUtils.equals(wsPass, wsPassFromFile)) {

      passPrefix = "WebService pass: reading encrypted value from file: " + wsPass;

      String encryptKey = StandardApiClientUtils.encryptKey();
      wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
      
    } else {
      passPrefix = "WebService pass: reading scalar value from standardApi.client.properties";
    }
    
    if (StandardApiClientConfig.retrieveConfig().propertyValueBoolean("authzStandardApiClient.logging.logMaskedPassword", false)) {
      LOG.debug(passPrefix + ": " + StandardApiClientUtils.repeat("*", wsPass.length()));
    }

    Credentials defaultcreds = new UsernamePasswordCredentials(user, wsPass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);

    return httpClient;
  }

  /**
   * @param url is the url to use
   * @param suffix e.g. groups/aStem:aGroup/members
   * @param webServiceVersion
   * @param asacRestHttpMethod
   * @return the method
   */
  private HttpMethodBase method(String url, String suffix, String webServiceVersion, AsacRestHttpMethod asacRestHttpMethod) {
    
    url = StandardApiClientUtils.stripEnd(url, "/");
    
    webServiceVersion = StandardApiClientUtils.stripStart(webServiceVersion, "/");
    webServiceVersion = StandardApiClientUtils.stripEnd(webServiceVersion, "/");

    suffix = StandardApiClientUtils.trim(suffix);
    suffix = StandardApiClientUtils.stripStart(suffix, "/");
        
    if (suffix != null && suffix.startsWith(".") && StandardApiClientUtils.isBlank(webServiceVersion) ) {
      url = url + suffix;
    } else {
      url = url + (StandardApiClientUtils.isBlank(webServiceVersion) ? "" : ("/" + webServiceVersion)) 
          + (StandardApiClientUtils.isBlank(suffix) ? "" :  ("/" + suffix));
    }

    LOG.debug("WebService: connecting to URL: '" + url + "'");

    //URL e.g. http://localhost:8093/standardApi-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    HttpMethodBase httpMethodBase = asacRestHttpMethod.httpMethod(url);

    //no keep alive so response is easier to indent for tests
    httpMethodBase.setRequestHeader("Connection", "close");
    
    return httpMethodBase;
  }

  /**
   * @param url to use
   * @param theXstream
   * @param urlSuffix to put on end of base url, e.g. groups/aStem:aGroup/members
   * @param objectToMarshall is the bean to convert to XML, or it could be a string of xml
   * @param logFile if not null, log the contents of the request there
   * @param responseCode array of size one to get the response code back
   * @param clientVersion 
   * @param asacRestContentType
   * @return the post method
   * @throws UnsupportedEncodingException 
   * @throws HttpException 
   * @throws IOException 
   */
  private HttpMethodBase method(String url, 
      String urlSuffix, Object objectToMarshall, File logFile, 
      int[] responseCode, String clientVersion, AsacRestContentType asacRestContentType,
      AsacRestHttpMethod asacRestHttpMethod)  {
    
    mostRecentHttpStatusCode = -1;
    mostRecentHttpMethod = null;
    
    try {
      
      HttpClient httpClient = httpClient();
  
      HttpMethodBase method = method(url, urlSuffix, clientVersion, asacRestHttpMethod);
  
      String requestDocument = null;
      
      if (objectToMarshall != null) {
        requestDocument = objectToMarshall instanceof String ? (String)objectToMarshall : asacRestContentType.writeString(objectToMarshall);
      }
      
      if (method instanceof EntityEnclosingMethod) {
        //text/xml
        //text/x-json
        //
        ((EntityEnclosingMethod)method).setRequestEntity(new StringRequestEntity(requestDocument, 
            asacRestContentType.getContentType(), "UTF-8"));
      }
      
      if (logFile != null || StandardApiClientLog.debugToConsole()) {
        if (logFile != null) {
          LOG.debug("WebService: logging request to: " + StandardApiClientUtils.fileCanonicalPath(logFile));
        }
        String theRequestDocument = StandardApiClientUtils.trimToEmpty(requestDocument);
        boolean isIndent = false;
        Exception indentException = null;
        if (!StandardApiClientUtils.isBlank(theRequestDocument)) {
          isIndent = StandardApiClientConfig.retrieveConfig().propertyValueBooleanRequired("authzStandardApiClient.logging.webService.indent");
          if (isIndent) {
            try {
              theRequestDocument = StandardApiClientUtils.indent(theRequestDocument, true);
            } catch (Exception e) {
              indentException = e;
            }
          }
        }
        
        StringBuilder headers = new StringBuilder();
  //      POST /standardApi-ws/servicesRest/v1_4_000/subjects HTTP/1.1
  //      Connection: close
  //      Authorization: Basic bWNoeXplcjpEaxxxxxxxxxx==
  //      User-Agent: Jakarta Commons-HttpClient/3.1
  //      Host: localhost:8090
  //      Content-Length: 226
  //      Content-Type: text/xml; charset=UTF-8
        headers.append(asacRestHttpMethod.name()).append(" ")
          .append(method.getURI().getPathQuery()).append(" HTTP/1.1\n");
        headers.append("Connection: close\n");
        headers.append("Authorization: Basic xxxxxxxxxxxxxxxx\n");
        headers.append("User-Agent: Jakarta Commons-HttpClient/3.1\n");
        headers.append("Host: ").append(method.getURI().getHost()).append(":")
          .append(method.getURI().getPort()).append("\n");
        if (method instanceof EntityEnclosingMethod) {
          headers.append("Content-Length: ").append(
              ((EntityEnclosingMethod)method).getRequestEntity().getContentLength()).append("\n");
          headers.append("Content-Type: ").append(
              ((EntityEnclosingMethod)method).getRequestEntity().getContentType()).append("\n");
        }
        headers.append("\n");
        
        String theRequest = headers + theRequestDocument;
        if (logFile != null) {
          StandardApiClientUtils.saveStringIntoFile(logFile, theRequest);
        }
        if (StandardApiClientLog.debugToConsole()) {
          System.err.println("\n################ REQUEST START " + (isIndent ? "(indented) " : "") + "###############\n");
          System.err.println(theRequest);
          System.err.println("\n################ REQUEST END ###############\n\n");
        }
        if (indentException != null) {
          throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
          		"standardApi.client.properties: authzStandardApiClient.logging.webService.indent", indentException);
        }
      }
      
      mostRecentRequest = requestDocument;
      
      int responseCodeInt = httpClient.executeMethod(method);

      mostRecentHttpStatusCode = responseCodeInt;
      mostRecentHttpMethod = method;
      
      if (responseCode != null && responseCode.length > 0) {
        responseCode[0] = responseCodeInt;
      }
      
      return method;
    } catch (Exception e) {
      
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      
      throw new RuntimeException("Problem in url: " + url, e);
    }
  }
  

}
