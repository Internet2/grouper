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
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;


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
   * The response code of the call.
   * @param _responseCode the responseCode to set
   */
  public GrouperHttpClient assignResponseCode(int _responseCode) {
    this.responseCode = _responseCode;
    return this;
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

  /**
   * Get a custom ClosableHttpClient that uses a truststore with the truststore and password information in grouperHttpCall.
   * 
   * @param grouperHttpCall The call.
   * @return A ClosableHttpClient with the custom truststore.
   * @throws Exception If there's a problem setting up the truststore or sslfactory
   */
  private CloseableHttpClient getCustomTrustStoreClient() throws Exception {
    // Trust own CA and all self-signed certs
    SSLContext sslcontext = SSLContexts.custom()
        .loadTrustMaterial(this.trustStore,
            this.trustStorePassword == null ? "".toCharArray() : this.trustStorePassword.toCharArray(),
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

    return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
  }

  /**
   * <pre>Execute a post with the given parameters, set teh code and the response into the call.
   * @param grouperHttpCall is the configuration object.
   */
  @SuppressWarnings("deprecation")
  public void executeRequest(){

    long start = System.currentTimeMillis();
    // We default to post.
    if (this.grouperHttpMethod == null){
      this.assignGrouperHttpMethod(GrouperHttpMethod.post);
    }

    // Get an http client.
    CloseableHttpClient closeableHttpClient;


    // See if we trust all.
    if (this.trust){

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
        closeableHttpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } 
      // Check for custom truststore
    } else if(this.trustStore != null) {
      try {
        closeableHttpClient = this.getCustomTrustStoreClient();
      } catch (Exception e) {
        throw new RuntimeException("Error getting custom truststore ClosableHttpClient", e);
      }
    } else {
      closeableHttpClient = HttpClients.createDefault();
    }


    HttpUriRequest httpUriRequest = null;

    try{

      // Use multipart for post forms and files.
      MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();        
      multipartEntityBuilder.setMode(this.httpMultipartMode);
      boolean useMultipart = this.filesToSend != null && this.filesToSend.size() > 0;


      // put url params in the url
      if (this.urlParameters != null && this.urlParameters.size() > 0){

        String urlToUse = this.url;
        if (!urlToUse.endsWith("?") && ! urlToUse.contains("?")){
          urlToUse = urlToUse + "?";
        }

        // Add params.
        for (String key : this.urlParameters.keySet()){
          urlToUse = urlToUse + "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(this.urlParameters.get(key), "UTF-8");
        }

        // Set the URL to the URl with quary params.
        this.assignUrl(urlToUse);

      }


      // Create the request.
      httpUriRequest = this.grouperHttpMethod.newHttpMethod(this.url);

      // Set the authorization data
      if (this.user != null && this.password != null){
        String authenticationString = basicAuthenticationString(this.user, this.password); 
        httpUriRequest.addHeader("Authorization", authenticationString);
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
          ((HttpPost)httpUriRequest).setEntity(new UrlEncodedFormEntity(postParams));
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
        ((HttpPost)httpUriRequest).setEntity(multipartEntityBuilder.build());
      } else if (useMultipart && this.grouperHttpMethod == GrouperHttpMethod.patch){
        ((HttpPatch)httpUriRequest).setEntity(multipartEntityBuilder.build());
      }



      // Add headers
      if (this.headers != null){
        for (String key : this.headers.keySet()){
          httpUriRequest.addHeader(key, this.headers.get(key));
        }
      }

      // Add body
      if (this.body != null){
        if (this.grouperHttpMethod == GrouperHttpMethod.post){
          ((HttpPost)httpUriRequest).setEntity(new StringEntity((this.body)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.patch){
          ((HttpPatch)httpUriRequest).setEntity(new StringEntity((this.body)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.put){
          ((HttpPut)httpUriRequest).setEntity(new StringEntity((this.body)));
        } else {
          throw new RuntimeException("Request body may only be used with POST, PATCH or PUT!");
        }
      } else if (this.bodyBytes != null){
        if (this.grouperHttpMethod == GrouperHttpMethod.post){
          ((HttpPost)httpUriRequest).setEntity(new ByteArrayEntity((this.bodyBytes)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.patch){
          ((HttpPatch)httpUriRequest).setEntity(new ByteArrayEntity((this.bodyBytes)));
        } else if (this.grouperHttpMethod == GrouperHttpMethod.put){
          ((HttpPut)httpUriRequest).setEntity(new ByteArrayEntity((this.bodyBytes)));
        } else {
          throw new RuntimeException("Request body may only be used with POST, PATCH or PUT!");
        }
      }

      // Execute the method.
      CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpUriRequest);
      int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
      this.assignResponseCode(responseCode);

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
          EntityUtils.consumeQuietly(closeableHttpResponse.getEntity());
          closeableHttpResponse.close();
        }
      }
    } catch (Exception e){
      throw new RuntimeException(e);
    } finally{
      try{
        closeableHttpClient.close();
      } catch (Exception e){
      }
      // do the logging
      try {
        GrouperHttpClientLog grouperHttpCallLog = threadLocalLog.get();
        if (grouperHttpCallLog != null) {
          
          StringBuilder theLog = grouperHttpCallLog.getLog();
          theLog.append("HTTP method: ").append(this.grouperHttpMethod).append("\n");
          if (!grouperHttpCallLog.getDoNotLogHeaders().contains("URL") && !grouperHttpCallLog.getDoNotLogHeaders().contains("*")) {
            theLog.append("HTTP URL: ").append(this.url).append("\n");
          }
          if (!StringUtils.isBlank(this.user) && !grouperHttpCallLog.getDoNotLogHeaders().contains("user") && !grouperHttpCallLog.getDoNotLogHeaders().contains("*")) {
            theLog.append("HTTP user: ").append(this.user).append("\n");
          }          
          for (String key: GrouperUtil.nonNull(this.headers).keySet()) {
            theLog.append("HTTP request header: ").append(key).append(": ");
            if (!StringUtils.equalsIgnoreCase("Authorization", key)
                && !grouperHttpCallLog.getDoNotLogHeaders().contains(key)
                && !grouperHttpCallLog.getDoNotLogHeaders().contains("*")) {
              theLog.append(this.headers.get(key));
            } else {
              theLog.append("*******");
            }
            theLog.append("\n");
          }
          theLog.append("HTTP response code: ").append(this.responseCode).append(", took ms: ").append(System.currentTimeMillis() - start).append("\n");
          for (String key: GrouperUtil.nonNull(this.responseHeaders).keySet()) {
            theLog.append("HTTP response header: ").append(key).append(": ");
            if (!key.toLowerCase().contains("cookie")
                && !grouperHttpCallLog.getDoNotLogHeaders().contains(key)
                && !grouperHttpCallLog.getDoNotLogHeaders().contains("*")) {
              theLog.append(this.responseHeaders.get(key));
            } else {
              theLog.append("*******");
            }
            theLog.append("\n");
          }
          if (this.responseBodyHolder != null && this.responseBodyHolder.length() > 0) {
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
  public static void logStart(GrouperHttpClientLog grouperHttpCallLog) {
    threadLocalLog.set(grouperHttpCallLog);
  }

}
