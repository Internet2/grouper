
/**
 */
package edu.internet2.middleware.grouper.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * <p>HTTP call.  Use this for all HTTP calls as a client
 * <blockquote>
 * <pre>
 * import edu.internet2.middleware.grouper.util.*;
 * GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
 * grouperHttpCall.assignUrl(url);
 * grouperHttpCall.assignGrouperHttpMethod("POST");
 * grouperHttpCall.addHeader("Content-Type", "application/json");
 * grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken);
 * grouperHttpCall.assignBody(body);
 * grouperHttpCall.executeRequest();
 * if (grouperHttpCall.getResponseCode() != 200) {
 *   throw new RuntimeException("Error connecting to '" + url + "': " + grouperHttpCall.getResponseCode());
 * }
 * String json = grouperHttpCall.getResponseBody();
 * </pre>
 * </blockquote>
 */
public class GrouperHttpClient {
 
  public static void main(String[] args) {
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    grouperHttpClient.assignUrl("https://grouperdemo.internet2.edu");
    
//    grouperHttpClient.assignProxyType(GrouperProxyType.PROXY_HTTP);    
//    grouperHttpClient.assignProxyUrl("http://172.23.63.87:3128");
    
    grouperHttpClient.executeRequest();
    System.out.println(grouperHttpClient.getResponseCode() + ", " + grouperHttpClient.getResponseBody());
  }
  

  public static void shutdown() {
    for (CloseableHttpClient closeableHttpClient : customTrustStoreClient.values()) {
      try{
        closeableHttpClient.close();
      } catch (Throwable e){
      }
    }
    customTrustStoreClient.clear();
    for (CloseableHttpClient closeableHttpClient : trustAllClients.values()) {
      try{
        closeableHttpClient.close();
      } catch (Throwable e){
      }
    }
    trustAllClients.clear();
    for (CloseableHttpClient closeableHttpClient : clients.values()) {
      try{
        closeableHttpClient.close();
      } catch (Throwable e){
      }
    }
    clients.clear();
  }
  /**
   * 
   */
  private Set<String> doNotLogParameters = new HashSet<String>();

  /**
   * 
   */
  private Set<String> doNotLogHeaders = new HashSet<String>();

  /**
   * if response body contains sensitive info and shouldnt be logged
   */
  private boolean doNotLogResponseBody = false;

  /**
   * if response body contains sensitive info and shouldnt be logged
   * @param theDoNotLogResponseBody
   * @return this for chaining
   */
  public GrouperHttpClient assignDoNotLogResponseBody(boolean theDoNotLogResponseBody) {
    this.doNotLogResponseBody = theDoNotLogResponseBody;
    return this;
  }
  
  /**
   * if request body contains sensitive info and shouldnt be logged
   */
  private boolean doNotLogRequestBody = false;

  /**
   * if request body contains sensitive info and shouldnt be logged
   * @param theDoNotLogRequestBody
   * @return this for chaining
   */
  public GrouperHttpClient assignDoNotLogRequestBody(boolean theDoNotLogRequestBody) {
    this.doNotLogRequestBody = theDoNotLogRequestBody;
    return this;
  }
  
  public GrouperHttpClient assignDoNotLogParameters(String paramsCommaSeparated) {
    this.doNotLogParameters = GrouperUtil.nonNull(GrouperUtil.toSet(GrouperUtil.splitTrim(paramsCommaSeparated, ",")));
    return this;
  }
  
  public GrouperHttpClient assignDoNotLogHeaders(String headersCommaSeparated) {
    this.doNotLogHeaders = GrouperUtil.nonNull(GrouperUtil.toSet(GrouperUtil.splitTrim(headersCommaSeparated, ",")));
    return this;
  }
  public GrouperHttpClient assignDoNotLogParameters(Set<String> params) {
    this.doNotLogParameters = GrouperUtil.nonNull(params);
    return this;
  }
  
  public GrouperHttpClient assignDoNotLogHeaders(Set<String> headers) {
    this.doNotLogHeaders = GrouperUtil.nonNull(headers);
    return this;
  }


  
  public Set<String> getDoNotLogParameters() {
    return doNotLogParameters;
  }

  
  public Set<String> getDoNotLogHeaders() {
    return doNotLogHeaders;
  }


  
  public boolean isDoNotLogResponseBody() {
    return doNotLogResponseBody;
  }


  /**
   * Truststore (.jks) to add dynamically to list of truststores.
   */
  private File trustStore;
  
  /**
   * Password for truststore.
   */
  private String trustStorePassword;
  
  /**
   * Trust regardless of cert; ONLY use when you KNOW the endpoint.
   */
  private boolean trust;

  /**
   * The url being called.
   */
  private String url;
  
  /**
   * Whether you want the response as a file as opposed to a string in memory.
   */
  private boolean responseAsFile;
  
  /**
   * The filename to use for the response, if the response is retrieved as a file.
   */
  private String responseFileName;
  
  /** 
   * The user for basic auth.
   */
  private String user;
  
  /** 
   * The password for basic auth.
   */
  private String password;
  
  /**
   * If getting the reponse body as a file, this is the file.
   */
  private File responseFile;
  
  /**
   * Response headers.
   */
  private Map<String, String> responseHeaders = new HashMap<String, String>();
  
  /** 
   * Any parameters to add to the URL.
   */
  private Map<String, String> urlParameters;
  
  /** 
   * Any parameters to add to the URL.
   */
  private Map<String, String> bodyParameters;
  
  /**
   * Any files to send.
   */
  private Map<String,File> filesToSend;
  
  /**
   * This get the response body.
   */
  private StringBuilder responseBodyHolder = new StringBuilder();
  
  /**
   * This get the response code.
   */
  private int responseCode = -1;
  
  /**
   * The response body receiver.
   */
  private GrouperHttpResponseBodyCallback httpResponseBodyCallback  = new GrouperHttpResponseBodyCallback(){

    @Override
    public void readBody(InputStream bodyInputStream) {
      try {
        GrouperHttpClient.this.responseBodyHolder.append(IOUtils.toString(bodyInputStream));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }};
  
  /**
   * Any paremeters to add to the header.
   */
  private Map<String, String> headers;

  /**
   * Any body to send.
   */
  private String body;
  
  /**
   * Any body to send.
   */
  private byte[] bodyBytes;
  
  /**
   * The type of method to call.
   */
  private GrouperHttpMethod grouperHttpMethod;
  
  
  public GrouperHttpClient assignGrouperHttpMethod(GrouperHttpMethod grouperHttpMethod) {
    this.grouperHttpMethod = grouperHttpMethod;
    return this;
  }


  public GrouperHttpClient assignGrouperHttpMethod(String grouperHttpMethodType) {
    this.grouperHttpMethod = GrouperHttpMethod.valueOfIgnoreCase(grouperHttpMethodType, false);
    return this;

  }


  /**
   * Use to override HttpMultipartMode.BROWSER_COMPATIBLE which is the default.
   */
  private HttpMultipartMode httpMultipartMode = HttpMultipartMode.BROWSER_COMPATIBLE;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperHttpClient.class);


  
  /**
   * Sets the url being called.
   * @param _url the new url being called
   */
  public GrouperHttpClient assignUrl(String _url) {
    this.url = _url;
    return this;

  }

  /**
   * proxy url to proxy to (override other configuration)
   */
  private String proxyUrl;

  /**
   * proxy url to proxy to (override other configuration)
   * @param proxyUrl1
   * @return this for chaining
   */
  public GrouperHttpClient assignProxyUrl(String proxyUrl1) {
    this.proxyUrl = proxyUrl1;
    return this;
  }

  /**
   * proxy type to override other configuration
   */
  private GrouperProxyType proxyType;

  /**
   * proxy type to override other configuration
   * @param grouperProxyType1
   * @return this for chaining
   */
  public GrouperHttpClient assignProxyType(GrouperProxyType grouperProxyType1) {
    this.proxyType = grouperProxyType1;
    return this;
  }
  
  /**
   * proxy type to override other configuration
   * @param grouperProxyType1
   * @return this for chaining
   */
  public GrouperHttpClient assignProxyType(String grouperProxyTypeString) {
    this.proxyType = GrouperProxyType.valueOfIgnoreCase(grouperProxyTypeString, false);
    return this;
  }

  
  
  /**
   * Sets the user for basic auth.
   * @param _user the new  user for basic auth
   */
  public GrouperHttpClient assignUser(String _user) {
    this.user = _user;
    return this;
  }

  
  /**
   * Sets the password for basic auth.
   * @param _password the new  password for basic auth
   */
  public GrouperHttpClient assignPassword(String _password) {
    this.password = _password;
    return this;
  }

  
  /**
   * Add a parameter to the BODY for a POST body form.
   * @param key is the name.
   * @param value is the value. 
   */
  public GrouperHttpClient addBodyParameter(String key, String value) {
    if((this.body != null && !this.body.trim().isEmpty()) || (this.bodyBytes != null)) {
      throw new RuntimeException("Can't set both bodyParameters and Body in the same http call");
    }
    if (this.bodyParameters == null){
      this.bodyParameters = new HashMap<String, String>();
    }
    this.bodyParameters.put(key, value);
    return this;
  }

  
  /**
   * Add a parameter to the URL.
   * @param key is the name.
   * @param value is the value. 
   */
  public GrouperHttpClient addUrlParameter(String key, String value) {
    
    if (this.urlParameters == null){
      this.urlParameters = new HashMap<String, String>();
    }
    this.urlParameters.put(key, value);
    return this;
  }

  
  /**
   * Adds a parameter to the header.
   * @param key is the name.
   * @param value is the value.
   */
  public GrouperHttpClient addHeader(String key, String value) {
    if (this.headers == null){
      this.headers = new HashMap<String, String>();
    }
    this.headers.put(key, value);
    return this;
  }

  
  /**
   * Sets the body to send.
   * @param _body the body to send
   */
  public GrouperHttpClient assignBodyBytes(byte[] _body) {
    
    if (!StringUtils.isBlank(this.body)){
      throw new RuntimeException("Cannot set both body and bodyBytes!");
    }
    
    // Can't set both params and a body in the same call
    if(this.bodyParameters != null && this.bodyParameters.size() > 0) {
      throw new RuntimeException("Can't set both bodyParameters and Body in the same http call");
    }
    // Can't set both filesToSend and a body in the same call
    if(this.filesToSend != null && this.filesToSend.size() > 0) {
      throw new RuntimeException("Can't set both FilesToSend and Body in the same http call");
    }
    
    this.bodyBytes = _body;
    return this;

  }
  
  
  /**
   * Sets the body to send.
   * @param _body the body to send
   */
  public GrouperHttpClient assignBody(String _body) {
    
    if (this.bodyBytes != null){
      throw new RuntimeException("Cannot set both body and bodyBytes!");
    }
    
    
    // Can't set both params and a body in the same call
    if(this.bodyParameters != null && this.bodyParameters.size() > 0) {
      throw new RuntimeException("Can't set both bodyParameters and Body in the same http call");
    }
    // Can't set both filesToSend and a body in the same call
    if(this.filesToSend != null && this.filesToSend.size() > 0) {
      throw new RuntimeException("Can't set both FilesToSend and Body in the same http call");
    }
    
    this.body = _body;
    return this;
  }

  
  /**
   * Get the response body of the call.
   * @return the responseBodyHolder
   */
  public String getResponseBody() {
    return this.responseBodyHolder.toString();
  }

  
  /**
   * Get the response code.
   * @return the responseCode
   */
  public int getResponseCode() {
    return this.responseCode;
  }

  
  
  /**
   * Truststore (.jks) to add dynamically to list of truststores.
   * @param _trustStore the trustStore to set.
   */
  public GrouperHttpClient assignTrustStore(File _trustStore) {
    this.trustStore = _trustStore;
    return this;
  }

  
  /**
   * Password for truststore.
   * @param _trustStoreKey the trustStoreKey to set.
   */
  public GrouperHttpClient assignTrustStorePassword(String _trustStoreKey) {
    this.trustStorePassword = _trustStoreKey;
    return this;
  }

  
  public GrouperHttpClient assignHttpResponseBodyCallback(
      GrouperHttpResponseBodyCallback httpResponseBodyCallback) {
    this.httpResponseBodyCallback = httpResponseBodyCallback;
    return this;
  }



  /**
   * @param filename The name of the file to send.
   * @param file The file to send.
   */
  public GrouperHttpClient addFileToSend(String filename, File file) {
    // Can't set both filesToSend and a body in the same call
    if((this.body != null && !this.body.trim().isEmpty()) || (this.bodyBytes != null)) {
      throw new RuntimeException("Can't set both FilesToSend and Body in the same http call");
    }
    
    if(this.filesToSend == null) {
      this.filesToSend = new HashMap<>();
    }
    this.filesToSend.put(filename, file);
    return this;
  }


  
  /**
   * Any headers sent back in the resopnse.
   * @return the responseHeaders
   */
  public Map<String, String> getResponseHeaders() {
    return this.responseHeaders;
  }


  
  /**
   * Any headers sent back in the resopnse.
   * @param _responseHeaders the responseHeaders to set
   */
  public GrouperHttpClient assignResponseHeaders(Map<String, String> _responseHeaders) {
    this.responseHeaders = _responseHeaders;
    return this;
  }


  
  /**
   * Use to override HttpMultipartMode.BROWSER_COMPATIBLE which is the default.
   * @param _httpMultipartMode the httpMultipartMode to set
   */
  public GrouperHttpClient assignHttpMultipartMode(HttpMultipartMode _httpMultipartMode) {
    this.httpMultipartMode = _httpMultipartMode;
    return this;
  }


  
  /**
   * Whether you want the response as a file as opposed to a string in memory.
   * @param _responseAsFile the responseAsFile to set,
   */
  public GrouperHttpClient assignResponseAsFile(boolean _responseAsFile) {
    this.responseAsFile = _responseAsFile;
    return this;
  }


  
  /**
   * If getting the reponse body as a file, this is the file.
   * @return the responseFile.
   */
  public File getResponseFile() {
    return this.responseFile;
  }


  
  /**
   * If getting the reponse body as a file, this is the file.
   * @param _responseFile the responseFile to set.
   */
  public GrouperHttpClient assignResponseFile(File _responseFile) {
    this.responseFile = _responseFile;
    return this;
  }


  
  /**
   * The filename to use for the response, if the response is retrieved as a file.
   * @return the responseFileName
   */
  public String getResponseFileName() {
    return this.responseFileName;
  }


  
  /**
   * The filename to use for the response, if the response is retrieved as a file.
   * @param _responseFileName the responseFileName to set
   */
  public GrouperHttpClient assignResponseFileName(String _responseFileName) {
    this.responseFileName = _responseFileName;
    return this;
  }


  
  /**
   * Trust regardless of cert; ONLY use when you KNOW the endpoint.
   * @param _trust the trust to set
   */
  public GrouperHttpClient assignTrust(boolean _trust) {
    this.trust = _trust;
    return this;
  }
  
  
  /**
   * Get the body of the response as a string.
   * @param httpMethod is the method.
   * @return the string.
   */
  public static String responseBodyAsString(HttpMethod httpMethod){
    String result =  null;
    InputStream inputStream = null;
    try{
      inputStream = httpMethod.getResponseBodyAsStream();
      result = IOUtils.toString(inputStream);
    } catch (Exception e){
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return result;
  }

  /**
   * Get the body of the response as a string.
   * @param closeableHttpResponse is the response.
   * @return the string.
   */
  public static String responseBodyAsString(CloseableHttpResponse closeableHttpResponse){
    String result =  null;
    InputStream inputStream = null;
    try{
      inputStream = closeableHttpResponse.getEntity().getContent();
      result = IOUtils.toString(inputStream);
    } catch (Exception e){
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return result;
  }

  /**
   * Create a basic authentication string.
   * @param login is the login.
   * @param password is the password.
   * @return "Basic login:password" where login:password is Base64 encoded.
   */
  public static String basicAuthenticationString(String login, String password){
    String basicBase64 = new String(Base64.encodeBase64((login + ":" + password).getBytes()));
    return "Basic " + basicBase64;
  }

  private static Map<MultiKey, CloseableHttpClient> customTrustStoreClient = new HashMap<>();
  
  /**
   * Get a custom ClosableHttpClient that uses a truststore with the truststore and password information in grouperHttpCall.
   * 
   * @param grouperHttpCall The call.
   * @return A ClosableHttpClient with the custom truststore.
   * @throws Exception If there's a problem setting up the truststore or sslfactory
   */
  private static CloseableHttpClient getCustomTrustStoreClient(File trustStore, String password, int retries) throws Exception {
    
    MultiKey clientKey = new MultiKey(trustStore, retries);
    
    CloseableHttpClient closeableHttpClient = customTrustStoreClient.get(clientKey);
    
    if (closeableHttpClient == null) {
      synchronized (GrouperHttpClient.class) {
        closeableHttpClient = customTrustStoreClient.get(clientKey);
        
        if (closeableHttpClient == null) {
          // Trust own CA and all self-signed certs
          SSLContext sslcontext = SSLContexts.custom()
              .loadTrustMaterial(trustStore,
                  password == null ? "".toCharArray() : password.toCharArray(),
                      new TrustSelfSignedStrategy())
              .build();

          // Allow TLSv1* protocol only
          SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
              sslcontext,
              new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" },
              null,
              new HostnameVerifier() {
                // Trust all of the hostnames
                @Override
                public boolean verify(String hostname, SSLSession session) {
                  return true;
                }
              } 
              );

          closeableHttpClient = httpClientBuilderDecorate(HttpClients.custom()).setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false)).setSSLSocketFactory(sslConnectionSocketFactory).useSystemProperties().build();
          boolean httpClientReuse = GrouperConfig.retrieveConfig().propertyValueBoolean("httpClientReuse", true);
          if (httpClientReuse) {
            customTrustStoreClient.put(clientKey, closeableHttpClient);
          }
          
        }
      }
    }
    return closeableHttpClient;
  }
  
  /**
   * how many times to retry for a non fatal error on idempotent requests
   */
  private int retries = 0;
  
  /**
   * how many times to retry for a non fatal error on idempotent requests
   * @param theRetries
   * @return retries
   */
  public GrouperHttpClient assignRetries(int theRetries) {
    this.retries = theRetries;
    return this;
  }
  
  /**
   * timeout millis defaults to one hour
   */
  private int timeoutMillis = 1000*60*60;
  
  public GrouperHttpClient assignTimeoutMillies(int theTimeoutMillies) {
    this.timeoutMillis = theTimeoutMillies;
    return this;
  }
  
  /**
   * implement this interface to have the grouper http client have the callback to set up the new authorization
   * if there's a retry or delay
   */
  private GrouperHttpClientSetupAuthorization grouperHttpClientSetupAuthorization = null;
  
  /**
   * implement this interface to have the grouper http client have the callback to set up the new authorization
   * if there's a retry or delay
   */
  public void setGrouperHttpClientSetupAuthorization(
      GrouperHttpClientSetupAuthorization grouperHttpClientSetupAuthorization) {
    this.grouperHttpClientSetupAuthorization = grouperHttpClientSetupAuthorization;
  }
  
  
  /**
   * implement this interface to customize to set custom condition on which you want to retry
   */
  private GrouperHttpThrottlingCallback grouperHttpThrottlingCallback = null;
  
  /**
   * implement this interface to customize to set custom condition on which you want to retry
   */
  public void setThrottlingCallback(
      GrouperHttpThrottlingCallback grouperHttpThrottlingCallback) {
    this.grouperHttpThrottlingCallback = grouperHttpThrottlingCallback;
  }
  
  

  /**
   * if there's a 429 or a connection timed out exception then delay for sometime and retry these many times.
   */
  private int retryForThrottlingOrNetworkIssues = 5;
  
  /**
   * if there's a 429 or a connection timed out exception then delay for sometime and retry these many times.
   */
  public void setRetryForThrottlingOrNetworkIssues(int retryForThrottlingOrNetworkIssues) {
    this.retryForThrottlingOrNetworkIssues = retryForThrottlingOrNetworkIssues;
  }
  
  /**
   * if there's a 429 or a connection timed out exception then retry and sleep for these many millis
   */
  private long retryForThrottlingOrNetworkIssuesSleepMillis = 60*1000; // 1 min default
  
  /**
   * if there's a 429 or a connection timed out exception then retry and sleep for these many millis
   */
  public void setRetryForThrottlingOrNetworkIssuesSleepMillis(
      long retryForThrottlingOrNetworkIssuesSleepMillis) {
    this.retryForThrottlingOrNetworkIssuesSleepMillis = retryForThrottlingOrNetworkIssuesSleepMillis;
  }
  
  /**
   * if there's a 429 or a connection timed out exception then after each retry, add to the sleep these many millis
   */
  private int retryForThrottlingOrNetworkIssuesBackOffMillis = 60*1000; // 1 min default
  
  /**
   * if there's a 429 or connection timed out exception then after each retry, add to the sleep these many millis
   */
  public void setRetryForThrottlingOrNetworkIssuesBackOffMillis(
      int retryForThrottlingOrNetworkIssuesBackOffMillis) {
    this.retryForThrottlingOrNetworkIssuesBackOffMillis = retryForThrottlingOrNetworkIssuesBackOffMillis;
  }
  
  /**
   * if there's a 429 or connection timed out then count how many times we retried for logging
   */
  private int retryForThrottlingTimesItWasRetried = 0;
  
  /**
   * if there's a 429 or connection timed out then count how many times we retried for logging
   */
  public int getRetryForThrottlingTimesItWasRetried() {
    return retryForThrottlingTimesItWasRetried;
  }

  /**
   * <pre>Execute a post with the given parameters, set teh code and the response into the call.
   */
  public GrouperHttpClient executeRequest() {
    
    int code = -1;
    
    this.retryForThrottlingTimesItWasRetried = 0;
    this.retryForThrottlingOrNetworkIssues = Math.max(0, retryForThrottlingOrNetworkIssues);
    this.retryForThrottlingOrNetworkIssuesSleepMillis = Math.max(0, retryForThrottlingOrNetworkIssuesSleepMillis);
    this.retryForThrottlingOrNetworkIssuesBackOffMillis = Math.max(0, retryForThrottlingOrNetworkIssuesBackOffMillis);
    
    for (int i=0; i < retryForThrottlingOrNetworkIssues+1; i++) {
      RuntimeException runtimeException = null;
      boolean retry = false;
      try {
      
        this.executeRequestHelper();
        code = this.getResponseCode();
      } catch (Exception e) {
        
        String fullStackTrace = GrouperUtil.getFullStackTrace(e);
        if (StringUtils.isNotBlank(fullStackTrace) && StringUtils.contains(fullStackTrace, "timed out")) {
          retry = true;
        }
        runtimeException = new RuntimeException("Error connecting to '" + this.url + "'", e);
      }
      if ( (code == 429 && this.grouperHttpThrottlingCallback == null) 
          || (this.grouperHttpThrottlingCallback != null && this.grouperHttpThrottlingCallback.setupThrottlingCallback(this))) {
        retry = true;
      }
      
      if (i <= retryForThrottlingOrNetworkIssues && retry) {
        GrouperUtil.sleep(retryForThrottlingOrNetworkIssuesSleepMillis + (i * retryForThrottlingOrNetworkIssuesBackOffMillis)); // 1 min -> 2 mins -> 3 mins ->>>>
        retryForThrottlingTimesItWasRetried++;
        
        if (this.debugMapForCaller != null) {
          GrouperClientUtils.debugMapIncrementLogEntry(this.debugMapForCaller, "httpClient_ThrottleCount", 1);
        }
        
        continue;
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }
      
      if (!retry) {
        break;
      }
    }
    return this;
  }
  
  /**
   * <pre>Execute a post with the given parameters, set teh code and the response into the call.
   * @param grouperHttpCall is the configuration object.
   */
  @SuppressWarnings("deprecation")
  private GrouperHttpClient executeRequestHelper(){

    long start = System.currentTimeMillis();
    // We default to post.
    if (this.grouperHttpMethod == null){
      this.assignGrouperHttpMethod(GrouperHttpMethod.post);
    }

    // Get an http client.
    CloseableHttpClient closeableHttpClient;

    // retries
    // .setRetryHandler(new DefaultHttpRequestRetryHandler(10, false))
    // .disableAutomaticRetries();

    // See if we trust all.
    if (this.trust){

      closeableHttpClient = getTrustAllClient(this.retries); 
      // Check for custom truststore
    } else if(this.trustStore != null) {
      try {
        closeableHttpClient = this.getCustomTrustStoreClient(this.trustStore, this.trustStorePassword, this.retries);
      } catch (Exception e) {
        throw new RuntimeException("Error getting custom truststore ClosableHttpClient", e);
      }
    } else {
      closeableHttpClient = getClient(this.retries);
    }
    
    HttpRequestBase httpRequestBase = null;

    try{

      // Use multipart for post forms and files.
      MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();        
      multipartEntityBuilder.setMode(this.httpMultipartMode);
      boolean useMultipart = this.filesToSend != null && this.filesToSend.size() > 0;


      // put url params in the url
      if (this.urlParameters != null && this.urlParameters.size() > 0){

        String urlToUse = this.url;
        if (urlToUse.endsWith("?")) {
          urlToUse = urlToUse.substring(0, urlToUse.length()-1);
        }
        
        boolean addQuestion = !urlToUse.contains("?");

        // Add params.
        boolean first = true;
        for (String key : this.urlParameters.keySet()){
          urlToUse = urlToUse + ((first && addQuestion) ? "?" : "&") + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(this.urlParameters.get(key), "UTF-8");
          first = false;
        }

        // Set the URL to the URl with quary params.
        this.assignUrl(urlToUse);

      }


      // Create the request.
      httpRequestBase = this.grouperHttpMethod.newHttpMethod(this.url);

      // Set the authorization data
      if (this.user != null && this.password != null){
        String authenticationString = basicAuthenticationString(this.user, this.password); 
        httpRequestBase.addHeader("Authorization", authenticationString);
      }

      if (grouperHttpClientSetupAuthorization != null) {
        grouperHttpClientSetupAuthorization.setupAuthorization(this);
      }
      
      
      // Add the params
      if (this.bodyParameters != null && this.bodyParameters.size() > 0){
        if(this.grouperHttpMethod == GrouperHttpMethod.get) {
          throw new RuntimeException("Body parameters cannot be used with GET!");
        }
        // If we've got params and files, use multipart
        if(useMultipart) {
          for (String key : this.bodyParameters.keySet()){
            multipartEntityBuilder.addTextBody(key, this.bodyParameters.get(key));
          }
          // Otherwise, do it normally
        } else {
          ArrayList<NameValuePair> postParams = new ArrayList<>();
          for (String key : this.bodyParameters.keySet()){
            postParams.add(new BasicNameValuePair(key, this.bodyParameters.get(key)));
          }
          ((HttpPost)httpRequestBase).setEntity(new UrlEncodedFormEntity(postParams));
        }
      }

      // Add the files
      if(useMultipart) {
        if(this.grouperHttpMethod != GrouperHttpMethod.post && this.grouperHttpMethod != GrouperHttpMethod.patch) {
          throw new RuntimeException("Files may only be used with POST!");
        }
        for (String key : this.filesToSend.keySet()){
          multipartEntityBuilder.addPart(key, new FileBody(this.filesToSend.get(key)));
        }
      }

      if (useMultipart && this.grouperHttpMethod == GrouperHttpMethod.post){
        ((HttpPost)httpRequestBase).setEntity(multipartEntityBuilder.build());
      } else if (useMultipart && this.grouperHttpMethod == GrouperHttpMethod.patch){
        ((HttpPatch)httpRequestBase).setEntity(multipartEntityBuilder.build());
      }



      // Add headers
      if (this.headers != null){
        for (String key : this.headers.keySet()){
          httpRequestBase.addHeader(key, this.headers.get(key));
        }
      }

      // Add body
      if (this.body != null){
        if (this.grouperHttpMethod == GrouperHttpMethod.post){
          ((HttpPost)httpRequestBase).setEntity(new StringEntity((this.body)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.patch){
          ((HttpPatch)httpRequestBase).setEntity(new StringEntity((this.body)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.put){
          ((HttpPut)httpRequestBase).setEntity(new StringEntity((this.body)));
        } else {
          throw new RuntimeException("Request body may only be used with POST, PATCH or PUT!");
        }
      } else if (this.bodyBytes != null){
        if (this.grouperHttpMethod == GrouperHttpMethod.post){
          ((HttpPost)httpRequestBase).setEntity(new ByteArrayEntity((this.bodyBytes)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.patch){
          ((HttpPatch)httpRequestBase).setEntity(new ByteArrayEntity((this.bodyBytes)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.put){
          ((HttpPut)httpRequestBase).setEntity(new ByteArrayEntity((this.bodyBytes)));
        } else {
          throw new RuntimeException("Request body may only be used with POST, PATCH or PUT!");
        }
      }

      RequestConfig.Builder config = RequestConfig.custom()
        .setConnectionRequestTimeout(this.timeoutMillis)
        .setConnectTimeout(this.timeoutMillis)
        .setSocketTimeout(this.timeoutMillis);
      
      GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(this.proxyType, this.proxyUrl, this.url);

      if (grouperProxyBean != null) {
        HttpHost proxy = new HttpHost(grouperProxyBean.getHostname(), grouperProxyBean.getPort(), grouperProxyBean.getScheme());
        config.setProxy(proxy);
      }
      httpRequestBase.setConfig(config.build());

      // Execute the method.
      CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpRequestBase);
      this.responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
      
      if (this.debugMapForCaller != null) {
        GrouperClientUtils.debugMapIncrementLogEntry(this.debugMapForCaller, "httpCode_" + this.responseCode, 1);
        GrouperClientUtils.debugMapIncrementLogEntry(this.debugMapForCaller, "wsCalls", 1);
        GrouperClientUtils.debugMapIncrementLogEntry(this.debugMapForCaller, "wsMillis", System.currentTimeMillis() - start);
      }

      if (closeableHttpResponse.getAllHeaders() != null){
        for (Header header : closeableHttpResponse.getAllHeaders()){
          this.getResponseHeaders().put(header.getName(), header.getValue());
        }
      }

      // Get the response.
      InputStream inputStream = null;
      FileOutputStream fileOutputStream = null;
      // Apparently with a delete there is no content.
      if (this.grouperHttpMethod != GrouperHttpMethod.delete && closeableHttpResponse.getEntity() != null ) {
        try{
          inputStream = closeableHttpResponse.getEntity().getContent();
          if (this.responseAsFile){
            String fileName = this.getResponseFileName();
            if (StringUtils.isBlank(fileName)) {
              fileName = GrouperUtil.tmpDir(true) + "this_" + GrouperUtil.timestampToFileString(new Date()) + "_" + GrouperUtil.uniqueId() + ".txt";
            } else {
              if (!fileName.contains(File.separator)) {
                fileName = GrouperUtil.tmpDir(true) + "this_" + GrouperUtil.timestampToFileString(new Date()) + "_" + GrouperUtil.uniqueId() + "_" + fileName;
                if (!fileName.contains(".")) {
                  fileName = fileName + ".txt";
                }
              }
            }
            
            File tempFile = new File(fileName);
            GrouperUtil.assertion(GrouperUtil.fileCreateNewFile(tempFile), "File exists: " + tempFile.getAbsolutePath());
            fileOutputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, fileOutputStream);
            this.assignResponseFile(tempFile);
          }
          else {
            this.httpResponseBodyCallback.readBody(inputStream);
          }
        } catch (Exception e){
          throw new RuntimeException(e);
        } finally {
          IOUtils.closeQuietly(fileOutputStream);
          IOUtils.closeQuietly(inputStream);
          httpRequestBase.releaseConnection();
          EntityUtils.consumeQuietly(closeableHttpResponse.getEntity());
          closeableHttpResponse.close();
        }
      }
      
      if (this.assertResponseCode != null) {
        if (this.assertResponseCode != this.responseCode) {
          StringBuilder responseBody = new StringBuilder();
          if (this.responseBodyHolder != null && this.responseBodyHolder.length() > 0 && !this.doNotLogResponseBody) {
            responseBody.append("\n").append(GrouperUtil.abbreviate(this.responseBodyHolder.toString(), 10000));
          }
          if (this.responseFile != null) {
            responseBody.append("\nResponse file: ").append(this.responseFileName).append(", size: ")
              .append(this.responseFile == null ? "null" : this.responseFile.length()).append("\n");
          }

          throw new RuntimeException("Expected response code: " + this.assertResponseCode + " but received response code: " + this.responseCode + responseBody);
        }
      }
      
      if (!StringUtils.isBlank(this.assertJsonPointer) || !StringUtils.isBlank(this.assertJsonPointerExpectedValueString)) {

        GrouperUtil.assertion(!StringUtils.isBlank(this.assertJsonPointer), "json pointer is required");
        GrouperUtil.assertion(!StringUtils.isBlank(this.assertJsonPointerExpectedValueString), "json pointer expected value is required");
        
        retrieveJsonNode();
        String value = GrouperUtil.jsonJacksonGetStringFromJsonPointer(this.jsonNode, this.assertJsonPointer);
        if (!StringUtils.equals(value, this.assertJsonPointerExpectedValueString)) {
          StringBuilder responseBody = new StringBuilder();
          if (this.responseBodyHolder != null && this.responseBodyHolder.length() > 0 && !this.doNotLogResponseBody) {
            responseBody.append("\n").append(GrouperUtil.abbreviate(this.responseBodyHolder.toString(), 10000));
          }

          throw new RuntimeException("Expected json pointer value: '" + this.assertJsonPointerExpectedValueString + "' at path '" 
              + this.assertJsonPointer + "' but received value: '" + value + "'" + responseBody);
          
        }
        
      }
      return this;
    } catch (Exception e){
      throw new RuntimeException(e);
    } finally{

      // dont close this, just close the methods.  the client is reused
      boolean httpClientReuse = GrouperConfig.retrieveConfig().propertyValueBoolean("httpClientReuse", true);
      if (!httpClientReuse) {
        try{
          closeableHttpClient.close();
        } catch (Throwable e){
        }
      }
      // do the logging
      try {
        GrouperHttpClientLog grouperHttpCallLog = threadLocalLog.get();
        if (grouperHttpCallLog != null) {
          
          StringBuilder theLog = grouperHttpCallLog.getLog();
          theLog.append("HTTP method: ").append(this.grouperHttpMethod).append("\n");
          if (!this.getDoNotLogHeaders().contains("URL") && !this.getDoNotLogHeaders().contains("*")) {
            theLog.append("HTTP URL: ").append(this.url).append("\n");
          }
          if (!StringUtils.isBlank(this.user) && !this.getDoNotLogHeaders().contains("user") && !this.getDoNotLogHeaders().contains("*")) {
            theLog.append("HTTP user: ").append(this.user).append("\n");
          }          
          for (String key: GrouperUtil.nonNull(this.headers).keySet()) {
            theLog.append("HTTP request header: ").append(key).append(": ");
            if (!StringUtils.equalsIgnoreCase("Authorization", key)
                && !this.getDoNotLogHeaders().contains(key)
                && !this.getDoNotLogHeaders().contains("*")) {
              theLog.append(this.headers.get(key));
            } else {
              theLog.append("*******");
            }
            theLog.append("\n");
          }
          if (StringUtils.isNotBlank(this.body)) {
            theLog.append("HTTP request body: ");
            if (this.doNotLogRequestBody) {
              theLog.append("*******");
            } else {
              theLog.append(StringUtils.abbreviate(this.body, 20000));
            }
            theLog.append("\n");
          }
          
          for (String key : GrouperUtil.nonNull(this.bodyParameters).keySet()) {
            theLog.append("HTTP request body param: ").append(key).append(":");
            if (!key.toLowerCase().contains("pass")
                && !key.toLowerCase().contains("secret")
                && !this.getDoNotLogParameters().contains(key)
                && !this.getDoNotLogParameters().contains("*")) {
              theLog.append(this.bodyParameters.get(key));
            } else {
              theLog.append("*******");
            }
            theLog.append("\n");
          }
          
          theLog.append("HTTP response code: ").append(this.responseCode).append(", took ms: ").append(System.currentTimeMillis() - start).append("\n");
          for (String key: GrouperUtil.nonNull(this.responseHeaders).keySet()) {
            theLog.append("HTTP response header: ").append(key).append(": ");
            if (!key.toLowerCase().contains("cookie")
                && !this.getDoNotLogHeaders().contains(key)
                && !this.getDoNotLogHeaders().contains("*")) {
              theLog.append(this.responseHeaders.get(key));
            } else {
              theLog.append("*******");
            }
            theLog.append("\n");
          }
          if (this.responseBodyHolder != null && this.responseBodyHolder.length() > 0 && !this.doNotLogResponseBody) {
            theLog.append(GrouperUtil.abbreviate(this.responseBodyHolder.toString(), 3000)).append("\n");
          }
          if (this.responseFile != null) {
            theLog.append("Response file: ").append(this.responseFileName).append(", size: ")
              .append(this.responseFile == null ? "null" : this.responseFile.length()).append("\n");
          }
          
        }
      } catch (Exception e) {
        LOG.error("error in http logging", e);
      }
    }
  }

  private static Map<Integer, CloseableHttpClient> clients = new HashMap<>();
  
  private static HttpClientBuilder httpClientBuilderDecorate(HttpClientBuilder httpClientBuilder) {
    
    // https://www.baeldung.com/httpclient-connection-management
    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
    int httpClientMaxTotalPoolSize = GrouperConfig.retrieveConfig().propertyValueInt("httpClientMaxTotalPoolSize", 100);
    int httpClientDefaultMaxPerRoute = GrouperConfig.retrieveConfig().propertyValueInt("httpClientDefaultMaxPerRoute", 30);
    
    connManager.setMaxTotal(httpClientMaxTotalPoolSize);
    connManager.setDefaultMaxPerRoute(httpClientDefaultMaxPerRoute);
    httpClientBuilder.setConnectionManager(connManager);
    
    // use the timeout of server, or if not found, use 5 seconds
    final ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

      @Override
      public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        Args.notNull(response, "HTTP response");  
        
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
          HeaderElement he = it.nextElement();
          String param = he.getName();
          String value = he.getValue();
          if (value != null && param.equalsIgnoreCase("timeout")) {
            try {
                return Long.parseLong(value) * 1000;
            } catch(NumberFormatException ignore) {
            }
          }
        }        
        return 5000;

      }  
    };
    
    httpClientBuilder.setKeepAliveStrategy(myStrategy);
    return httpClientBuilder;
  }
  
  private static CloseableHttpClient getClient(int retries) {
    
    CloseableHttpClient closeableHttpClient = clients.get(retries);
    
    if (closeableHttpClient == null) {
      synchronized (GrouperHttpClient.class) {
        closeableHttpClient = clients.get(retries);
        if (closeableHttpClient == null) {
          closeableHttpClient = httpClientBuilderDecorate(HttpClientBuilder.create()).setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false)).useSystemProperties().build();
          boolean httpClientReuse = GrouperConfig.retrieveConfig().propertyValueBoolean("httpClientReuse", true);
          if (httpClientReuse) {
            clients.put(retries, closeableHttpClient);
          }
        }        
      }
    }
    
    return closeableHttpClient;
  }

  private static Map<Integer, CloseableHttpClient> trustAllClients = new HashMap<>();
  
  private static CloseableHttpClient getTrustAllClient(int retries) {
    CloseableHttpClient closeableHttpClient = trustAllClients.get(retries);
    
    if (closeableHttpClient == null) {
      
      synchronized (GrouperHttpClient.class) {
        closeableHttpClient = trustAllClients.get(retries);
        
        if (closeableHttpClient == null) {
          TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
              return true;
            }
          };
      
          // Trust all, ONLY use for connections you are sure of.
          SSLContextBuilder builder = new SSLContextBuilder();
          try {
            builder.loadTrustMaterial(null, trustStrategy);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            closeableHttpClient = httpClientBuilderDecorate(HttpClients.custom()).setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false)).setSSLSocketFactory(sslsf).useSystemProperties().build();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          boolean httpClientReuse = GrouperConfig.retrieveConfig().propertyValueBoolean("httpClientReuse", true);
          if (httpClientReuse) {
            trustAllClients.put(retries, closeableHttpClient);
          }
        }
      }
    }
    return closeableHttpClient;
  }

  private static ThreadLocal<GrouperHttpClientLog> threadLocalLog = new InheritableThreadLocal<GrouperHttpClientLog>();

  /**
   * get the current log
   * log start
   */
  public static GrouperHttpClientLog logCurrent() {
    GrouperHttpClientLog grouperHttpCallLog = threadLocalLog.get();
    return grouperHttpCallLog;
  }


  /**
   * stop a debug log in a finally block
   * @return the log message
   */
  public static String logEnd() {
    GrouperHttpClientLog grouperHttpCallLog = threadLocalLog.get();
    StringBuilder log = grouperHttpCallLog == null ? null : grouperHttpCallLog.getLog();
    threadLocalLog.remove();
    return log == null ? null : log.toString();
  }

  /**
   * start a static debug log
   * log start
   */
  public static boolean logStart(GrouperHttpClientLog grouperHttpCallLog) {
    
    if (threadLocalLog.get() != null ) {
      return false;
    }
    threadLocalLog.set(grouperHttpCallLog);
    return true;

  }

  /**
   * debug map for caller
   */
  private Map<String, Object> debugMapForCaller;

  /**
   * debug map for timing and result code
   * @param debugMap
   * @return this for chaining
   */
  public GrouperHttpClient assignDebugMap(Map<String, Object> debugMap) {
    this.debugMapForCaller = debugMap;
    return this;
  }

  /**
   * json node of response
   */
  private JsonNode jsonNode;
  
  /**
   * get the json node of the response (generate if not there already)
   * @return the json node
   */
  public JsonNode retrieveJsonNode() {
    if (this.jsonNode != null) {
      return this.jsonNode;
    }
    this.jsonNode = GrouperUtil.jsonJacksonNode(this.getResponseBody());
    return this.jsonNode;
  }
  
  /**
   * make sure there is a certain response code
   */
  private Integer assertResponseCode = null;
  
  /**
   * if the response code is not this, then exception and log response
   * @param expectedCode
   * @return this for chaining
   */
  public GrouperHttpClient assignAssertResponseCode(int expectedCode) {
    this.assertResponseCode = expectedCode;
    return this;
  }

  /**
   * check a json pointer for a value to see if request is success.
   * note that the parsed JsonNode is available too
   */
  private String assertJsonPointer = null;
  
  /**
   * check a json pointer for a value to see if request is success.
   * note that the parsed JsonNode is available too.
   * json pointer e.g. /a/b/c
   * @param assertJsonPointer1
   * @return this for chaining
   */
  public GrouperHttpClient assignAssertJsonPointer(String assertJsonPointer1) {
    this.assertJsonPointer = assertJsonPointer1;
    return this;
  }

  /**
   * check a json pointer e.g. /a/b/c and see if it equals this value, if not, exception
   */
  private String assertJsonPointerExpectedValueString = null;
  
  /**
   * if the response code is not this, then exception and log response
   * @param assertJsonPointerExpectedValueString1
   * @return this for chaining
   */
  public GrouperHttpClient assignAssertJsonPointerExpectedValueString(String assertJsonPointerExpectedValueString1) {
    this.assertJsonPointerExpectedValueString = assertJsonPointerExpectedValueString1;
    return this;
  }

}
