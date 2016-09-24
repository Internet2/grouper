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
package edu.internet2.middleware.grouperClient.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.digest.DigestUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpMethodBase;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.DeleteMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.GetMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PutMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.Expression;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.JexlContext;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.JexlEngine;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.JexlException;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.MapContext;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.impl.Jdk14Logger;

/**
 * utility methods specific to grouper client
 */
public class GrouperClientUtils extends GrouperClientCommonUtils {

  /**
   * configure jdk14 logs once
   */
  private static boolean configuredLogs = false;

  /**
   * @param theClass
   * @return the log
   */
  public static Log retrieveLog(Class<?> theClass) {

    Log theLog = LogFactory.getLog(theClass);
    
    //if this isnt here, dont configure yet
    if (isBlank(GrouperClientConfig.retrieveConfig().propertyValueString("encrypt.disableExternalFileLookup"))
        || theClass.equals(GrouperClientCommonUtils.class)) {
      return new GrouperClientLog(theLog);
    }
    
    if (!configuredLogs) {
      String logLevel = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.logging.logLevel");
      String logFile = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.logging.logFile");
      String grouperClientLogLevel = GrouperClientConfig.retrieveConfig().propertyValueString(
          "grouperClient.logging.grouperClientOnly.logLevel");
      
      boolean hasLogLevel = !isBlank(logLevel);
      boolean hasLogFile = !isBlank(logFile);
      boolean hasGrouperClientLogLevel = !isBlank(grouperClientLogLevel);
      
      if (hasLogLevel || hasLogFile) {
        if (theLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) theLog;
          Logger logger = jdkLogger.getLogger();
          long timeToLive = 60;
          while (logger.getParent() != null && timeToLive-- > 0) {
            //this should be root logger
            logger = logger.getParent();
          }
  
          if (length(logger.getHandlers()) == 1) {
  
            //remove console appender if only one
            if (logger.getHandlers()[0].getClass() == ConsoleHandler.class) {
              logger.removeHandler(logger.getHandlers()[0]);
            }
          }
  
          if (length(logger.getHandlers()) == 0) {
            Handler handler = null;
            if (hasLogFile) {
              try {
                handler = new FileHandler(logFile, true);
              } catch (IOException ioe) {
                throw new RuntimeException(ioe);
              }
            } else {
              handler = new ConsoleHandler();
            }
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);

            logger.setUseParentHandlers(false);
          }
          
          if (hasLogLevel) {
            Level level = Level.parse(logLevel);
            
            logger.setLevel(level);

          }
        }
      }
      
      if (hasGrouperClientLogLevel) {
        Level level = Level.parse(grouperClientLogLevel);
        Log grouperClientLog = LogFactory.getLog("edu.internet2.middleware.grouperClient");
        if (grouperClientLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) grouperClientLog;
          Logger logger = jdkLogger.getLogger();
          logger.setLevel(level);
        }
      }
      
      configuredLogs = true;
    }
    
    return new GrouperClientLog(theLog);
    
  }
  
  /**
   * override map for properties for testing
   * @return the override map
   * @deprecated use GrouperClientConfig.retrieveConfig().propertiesOverrideMap() instead
   */
  @Deprecated
  public static Map<String, String> grouperClientOverrideMap() {
    return GrouperClientConfig.retrieveConfig().propertiesOverrideMap();
  }
  
  /**
   * grouper client properties
   * @return the properties
   * @deprecated use GrouperClientConfig.retrieveConfig().properties() instead
   */
  @Deprecated
  public static Properties grouperClientProperties() {
    return GrouperClientConfig.retrieveConfig().properties();
  }

  /**
   * get a property and validate required from grouper.client.properties
   * @param key 
   * @param required 
   * @return the value
   * @deprecated use GrouperClientConfig.retrieveConfig().propertyValueString instead
   */
  @Deprecated
  public static String propertiesValue(String key, boolean required) {
    if (required) {
      return GrouperClientConfig.retrieveConfig().propertyValueStringRequired(key);
    }
    return GrouperClientConfig.retrieveConfig().propertyValueString(key);
  }


  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   * @deprecated use GrouperClientConfig.retrieveConfig().propertyValueBoolean instead
   */
  @Deprecated
  public static boolean propertiesValueBoolean(String key, boolean defaultValue, boolean required ) {
    if (required) {
      return GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(key);
    }
    return GrouperClientConfig.retrieveConfig().propertyValueBoolean(key, defaultValue);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   * @deprecated GrouperClientConfig.retrieveConfig().propertyValueInt
   */
  @Deprecated
  public static int propertiesValueInt(String key, int defaultValue, boolean required ) {
    if (required) {
      return GrouperClientConfig.retrieveConfig().propertyValueIntRequired(key);
    }
    return GrouperClientConfig.retrieveConfig().propertyValueInt(key, defaultValue);
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperClientUtils.class);

  /** class object for this string */
  private static Map<String, Class<?>> jexlClass = new HashMap<String, Class<?>>();

  /** pattern to see if class or not */
  private static Pattern jexlClassPattern = Pattern.compile("^[a-zA-Z0-9_.]*\\.[A-Z][a-zA-Z0-9_]*$");

  /** true or false for if we know if this is a class or not */
  private static Map<String, Boolean> jexlKnowsIfClass = new HashMap<String, Boolean>();

  /**
   * 
   */
  private static class ElMapContext extends MapContext {
  
    /**
     * retrieve class if class
     * @param name
     * @return class
     */
    private static Object retrieveClass(String name) {
      if (isBlank(name)) {
        return null;
      }
      
      //see if fully qualified class
      
      Boolean knowsIfClass = jexlKnowsIfClass.get(name);
      
      //see if knows answer
      if (knowsIfClass != null) {
        //return class or null
        return jexlClass.get(name);
      }
      
      //see if valid class
      if (jexlClassPattern.matcher(name).matches()) {
        
        jexlKnowsIfClass.put(name, true);
        //try to load
        try {
          Class<?> theClass = Class.forName(name);
          jexlClass.put(name, theClass);
          return theClass;
        } catch (Exception e) {
          LOG.info("Cant load what looks like class: " + name, e);
          //this is ok I guess, dont rethrow, not sure it is a class
        }
      }
      return null;
      
    }
    
    /**
     * @see edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.MapContext#get(java.lang.String)
     */
    @Override
    public Object get(String name) {
      
      //see if registered      
      Object object = super.get(name);
      
      if (object != null) {
        return object;
      }
      return retrieveClass(name);
    }
  
    /**
     * @see edu.internet2.middleware.grouperClientExt.org.apache.commons.jexl2.MapContext#has(java.lang.String)
     */
    @Override
    public boolean has(String name) {
      boolean superHas = super.has(name);
      if (superHas) {
        return true;
      }
      
      return retrieveClass(name) != null;
      
    }
    
  }

  /**
   * substitute an EL for objects.  Dont worry if something returns null
   * @param stringToParse
   * @param variableMap
   * @return the string
   */
  public static String substituteExpressionLanguage(String stringToParse, Map<String, Object> variableMap) {
    
    return substituteExpressionLanguage(stringToParse, variableMap, true, true, true, false);
    
  }
  

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @param allowStaticClasses if true allow static classes not registered with context
   * @param silent if silent mode, swallow exceptions (warn), and dont warn when variable not found
   * @param lenient false if undefined variables should throw an exception.  if lenient is true (default)
   * then undefined variables are null
   * @param logOnNull if null output of substitution should be logged
   * @return the string
   */
  public static String substituteExpressionLanguage(String stringToParse, 
      Map<String, Object> variableMap, boolean allowStaticClasses, boolean silent, boolean lenient, boolean logOnNull) {
    if (isBlank(stringToParse)) {
      return stringToParse;
    }
    String overallResult = null;
    Exception exception = null;
    try {
      JexlContext jc = allowStaticClasses ? new ElMapContext() : new MapContext();

      int index = 0;
      
      variableMap = nonNull(variableMap);
      
      for (String key: variableMap.keySet()) {
        jc.set(key, variableMap.get(key));
      }
      
      //allow utility methods
      jc.set("elUtils", new GcElUtilsSafe());
      //if you add another one here, add it in the logs below
      
      // matching ${ exp }   (non-greedy)
      Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher matcher = pattern.matcher(stringToParse);
      
      StringBuilder result = new StringBuilder();
  
      //loop through and find each script
      while(matcher.find()) {
        result.append(stringToParse.substring(index, matcher.start()));
        
        //here is the script inside the curlies
        String script = matcher.group(1);
        
        index = matcher.end();
  
        if (script.contains("{")) {
          //we need to match up some curlies here...
          int scriptStart = matcher.start(1);
          int openCurlyCount = 0;
          for (int i=scriptStart; i<stringToParse.length();i++) {
            char curChar = stringToParse.charAt(i);
            if (curChar == '{') {
              openCurlyCount++;
            }
            if (curChar == '}') {
              openCurlyCount--;
              //negative 1 since we need to get to the close of the parent one...
              if (openCurlyCount <= -1) {
                script = stringToParse.substring(scriptStart, i);
                index = i+1;
                break;
              }
            }
          }
        }
        
        JexlEngine jexlEngine = new JexlEngine();
        jexlEngine.setSilent(silent);
        jexlEngine.setLenient(lenient);
  
        Expression e = jexlEngine.createExpression(script);
  
        //this is the result of the evaluation
        Object o = null;
        
        try {
          o = e.evaluate(jc);
        } catch (JexlException je) {
          //exception-scrape to see if missing variable
          if (!lenient && trimToEmpty(je.getMessage()).contains("undefined variable")) {
            //clean up the message a little bit
            // e.g. edu.internet2.middleware.grouper.util.GrouperUtil.substituteExpressionLanguage@8846![0,6]: 'amount < 50000 && amount2 < 23;' undefined variable amount
            String message = je.getMessage();
            //Pattern exceptionPattern = Pattern.compile("^" + GrouperUtil.class.getName() + "\\.substituteExpressionLanguage.*?]: '(.*)");
            Pattern exceptionPattern = Pattern.compile("^.*undefined variable (.*)");
            Matcher exceptionMatcher = exceptionPattern.matcher(message);
            if (exceptionMatcher.matches()) {
              //message = "'" + exceptionMatcher.group(1);
              message = "variable '" + exceptionMatcher.group(1) + "' is not defined in script: '" + script + "'";
            }
            throw new GcExpressionLanguageMissingVariableException(message, je);
          }
          throw je;
        }
          
        if (o == null) {
          if (logOnNull) {
            LOG.warn("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
                + toStringForLog(variableMap.keySet()));
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
                  + toStringForLog(variableMap.keySet()));
            }            
          }
          o = "";
        }
        
        if (o instanceof RuntimeException) {
          throw (RuntimeException)o;
        }
        
        result.append(o);
        
      }
      
      result.append(stringToParse.substring(index, stringToParse.length()));
      overallResult = result.toString();
      return overallResult;
      
    } catch (Exception e) {
      exception = e;
      if (e instanceof GcExpressionLanguageMissingVariableException) {
        throw (GcExpressionLanguageMissingVariableException)e;
      }
      throw new RuntimeException("Error substituting string: '" + stringToParse + "'", e);
    } finally {
      if (LOG.isDebugEnabled()) {
        Set<String> keysSet = new LinkedHashSet<String>(nonNull(variableMap).keySet());
        keysSet.add("elUtils");
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Subsituting EL: '").append(stringToParse).append("', and with env vars: ");
        String[] keys = keysSet.toArray(new String[]{});
        for (int i=0;i<keys.length;i++) {
          logMessage.append(keys[i]);
          if (i != keys.length-1) {
            logMessage.append(", ");
          }
        }
        logMessage.append(" with result: '" + overallResult + "'");
        if (exception != null) {
          logMessage.append(", and exception: " + exception + ", " + getFullStackTrace(exception));
        }
        LOG.debug(logMessage.toString());
      }
    }
  }

  /**
   * get the attribute value of an attribute name of a subject
   * @param wsSubject subject
   * @param attributeNames list of attribute names in the subject
   * @param attributeName to query
   * @return the value or null
   */
  public static String subjectAttributeValue(WsSubject wsSubject, String[] attributeNames, String attributeName) {
    
    if (GrouperClientUtils.equals("subject__id", attributeName)) {
      return wsSubject.getId();
    }
    
    if (GrouperClientUtils.equals("subject__name", attributeName)) {
      return wsSubject.getName();
    }
    
    for (int i=0;i<GrouperClientUtils.length(attributeNames);i++) {
      
      if (GrouperClientUtils.equalsIgnoreCase(attributeName, attributeNames[i])
          && GrouperClientUtils.length(wsSubject.getAttributeValues()) > i) {
        //got it
        return wsSubject.getAttributeValue(i);
      }
    }
    return null;
  }

  /**
   * 
   * @return the encrypt key
   */
  public static String encryptKey() {
    String encryptKey = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("encrypt.key");
    
    boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBoolean(
        "encrypt.disableExternalFileLookup", false);
    
    //lets lookup if file
    encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
    
    //the server does this, so if the key is blank, it will still have something there, so be consistent
    if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("encrypt.encryptLikeServer", false)) {
      
      encryptKey += "w";
    }
    
    return encryptKey;
  }

  /**
   * decrypt a pass from a file if the file exists (i.e. passes might have slashes in them)
   * @param pass
   * @param logInfo if not null, put log info in there
   * @return the pass
   */
  public static String decryptFromFileIfFileExists(String pass, StringBuilder logInfo) {

    boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
        "encrypt.disableExternalFileLookup", false, false);
    
    //lets lookup if file
    String passFromFile = GrouperClientUtils.readFromFileIfFileExists(pass, disableExternalFileLookup);

    if (!GrouperClientUtils.equals(pass, passFromFile)) {

      if (logInfo != null) {
        logInfo.append("reading encrypted value from file: " + pass);
      }
      
      String encryptKey = encryptKey();
      pass = new Crypto(encryptKey).decrypt(passFromFile);
      
    } else {
      if (logInfo != null) {
        logInfo.append("pass is a scalar");
      }
    }
    
    return pass;
  }
  

  /**
   * name of the cache directory without trailing slash
   * @return the name of the cache directory
   */
  public static String cacheDirectoryName() {
    
    if (cacheDirectoryName == null) {
      String directoryName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.cacheDirectory");
      if (GrouperClientCommonUtils.isBlank(directoryName)) {
        throw new RuntimeException("grouperClient.cacheDirectory is required in grouper.client.properties");
      }
  
      directoryName = GrouperClientCommonUtils.stripEnd(directoryName, "/");
      directoryName = GrouperClientCommonUtils.stripEnd(directoryName, "\\");
      
      File discoveryDir = new File(directoryName);
      directoryName = discoveryDir.getAbsolutePath();
      if (directoryName.endsWith("/.")) {
        directoryName = directoryName.substring(0, directoryName.length()-2);
      }
      if (directoryName.endsWith("\\.")) {
        directoryName = directoryName.substring(0, directoryName.length()-2);
      }
      cacheDirectoryName = directoryName;
    }    
    return cacheDirectoryName;
  }

  /**
   * encrypt a message to SHA with base 64
   * @param plaintext
   * @return the hash
   */
  public synchronized static String encryptSha(String plaintext) {
    
    try {
    
      byte[] sha1bytes = DigestUtils.sha(plaintext.getBytes("UTF-8"));
      return new String(Base64.encodeBase64(sha1bytes));
      
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /** cache directory name */
  private static String cacheDirectoryName = null;
  
  /**
   * call HTTP with a url, optional request body, get a response body.  this assumes json by default
   * @param urlSuffix is after the configured URL
   * @param serviceAuthn is the config string that identifies the user/pass/url
   * @param httpCallMethod HTTP
   * @param body to send if applicable
   * @return the response
   */
  public static HttpCallResponse httpCall(String urlSuffix, String serviceAuthn, HttpCallMethod httpCallMethod, String body) {
    
    //make sure right content type is in request (e.g. application/xhtml+xml
    HttpClient httpClient = new HttpClient();

    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
    
    int soTimeoutMillis = GrouperClientConfig.retrieveConfig().propertyValueIntRequired(
        "grouperClient.webService.httpSocketTimeoutMillis");

    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);

    int connectionManagerMillis = GrouperClientConfig.retrieveConfig().propertyValueIntRequired(
        "grouperClient.webService.httpConnectionManagerTimeoutMillis");

    httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

    String user = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService." + serviceAuthn + ".user");

    boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");

    //lets lookup if file
    String wsPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService." + serviceAuthn + ".pass");
    String wsPassFromFile = GrouperClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);

    String passPrefix = null;

    if (!GrouperClientUtils.equals(wsPass, wsPassFromFile)) {

      passPrefix = "WebService pass: reading encrypted value from file: " + wsPass;

      String encryptKey = GrouperClientUtils.encryptKey();
      wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
      
    } else {
      passPrefix = "WebService pass: reading scalar value from grouper.client.properties";
    }
    
    if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.logging.logMaskedPassword", false)) {
      LOG.debug(passPrefix + ": " + GrouperClientUtils.repeat("*", wsPass.length()));
    }

    Credentials defaultcreds = new UsernamePasswordCredentials(user, wsPass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);

    String urlBase = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService." + serviceAuthn + ".url");
    if (urlBase == null) {
      throw new RuntimeException("urlBase is null");
    }
    
    String url = urlBase + urlSuffix;

    String contentType = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.webService." + serviceAuthn + ".defaultRequestContentType");
    contentType = GrouperClientUtils.defaultIfEmpty(contentType, "Application/json");

    HttpMethodBase httpMethodBase = null;
    
    if (httpCallMethod == null) {
      throw new RuntimeException("You need to pass in an httpCallMethod");
    }
    
    HttpCallResponse httpCallResponse = new HttpCallResponse();
    httpCallResponse.setHttpCallMethod(httpCallMethod);
    httpCallResponse.setContentType(contentType);
    httpCallResponse.setUrl(url);
    
    //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    switch (httpCallMethod) {
      case GET:
        
        httpMethodBase = new GetMethod(url);
        if (!GrouperClientUtils.isBlank(body)) {
          throw new RuntimeException("GET cannot have a body");
        }
        break;
      case POST:
        
        httpMethodBase = new PostMethod(url);
        if (!GrouperClientUtils.isBlank(body)) {
          try {
            ((PostMethod)httpMethodBase).setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
          } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
          }
        }

        break;
        
      case PUT:
        
        httpMethodBase = new PutMethod(url);
        if (!GrouperClientUtils.isBlank(body)) {
          try {
            ((PutMethod)httpMethodBase).setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
          } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
          }
        }

        break;
        
      case DELETE:
        
        httpMethodBase = new DeleteMethod(url);
        if (!GrouperClientUtils.isBlank(body)) {
          throw new RuntimeException("DELETE cannot have a body");
        }

        break;
        
      default: 
        throw new RuntimeException("Not expecting method: " + httpCallMethod);
    }
    
    httpMethodBase.addRequestHeader("Content-Type", contentType);
    
    //no keep alive so response is easier to indent for tests
    httpMethodBase.setRequestHeader("Connection", "close");
    
    try {

      int responseCodeInt = httpClient.executeMethod(httpMethodBase);

      httpCallResponse.setHttpResponseCode(responseCodeInt);

    } catch (IOException ioe) {
      throw new RuntimeException("Error in url: " + url + ", method: " + httpCallMethod + ", body: " + GrouperClientUtils.abbreviate(body, 5000), ioe);
    }
    
    String theResponse = GrouperClientUtils.responseBodyAsString(httpMethodBase);
    
    httpCallResponse.setResponseBody(theResponse);
    
    return httpCallResponse;
  }

  /**
   * pop first url string, retrieve, and remove, or null if not there
   * @param urlStrings
   * @return the string or null if not there
   */
  public static String popUrlString(List<String> urlStrings) {
    
    int urlStringsLength = length(urlStrings);
  
    if (urlStringsLength > 0) {
      String firstResource = urlStrings.get(0);
      //pop off
      urlStrings.remove(0);
      //return
      return firstResource;
    }
    return null;
  }

}
