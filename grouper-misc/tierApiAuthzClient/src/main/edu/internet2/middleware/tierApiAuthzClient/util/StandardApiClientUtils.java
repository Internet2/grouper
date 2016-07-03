package edu.internet2.middleware.tierApiAuthzClient.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolderLookup;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacResultProblem;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;


public class StandardApiClientUtils extends StandardApiClientCommonUtils {

  /**
   * marshal a folder lookup if it is there, else null
   * @return the lookup
   */
  public static AsacFolderLookup argFolderLookup(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {

    String folderLookupName = StandardApiClientUtils.argMapString(argMap, 
        argMapNotUsed, "folderLookupName", false);
    String folderLookupId = StandardApiClientUtils.argMapString(argMap, 
        argMapNotUsed, "folderLookupId", false);
    String folderLookupHandleName = StandardApiClientUtils.argMapString(argMap, 
        argMapNotUsed, "folderLookupHandleName", false);
    String folderLookupHandleValue = StandardApiClientUtils.argMapString(argMap, 
        argMapNotUsed, "folderLookupHandleValue", false);


    AsacFolderLookup asacFolderLookup = null;

    if (!StandardApiClientUtils.isBlank(folderLookupName) || !StandardApiClientUtils.isBlank(folderLookupId) 
        || !StandardApiClientUtils.isBlank(folderLookupHandleName) || !StandardApiClientUtils.isBlank(folderLookupHandleValue)) {

      asacFolderLookup = new AsacFolderLookup();
      
      if (!StandardApiClientUtils.isBlank(folderLookupName)) {
        asacFolderLookup.setName(folderLookupName);
      }

      if (!StandardApiClientUtils.isBlank(folderLookupId)) {
        asacFolderLookup.setName(folderLookupId);
      }

      if (!StandardApiClientUtils.isBlank(folderLookupHandleName)) {
        asacFolderLookup.setName(folderLookupHandleName);
      }

      if (!StandardApiClientUtils.isBlank(folderLookupHandleValue)) {
        asacFolderLookup.setName(folderLookupHandleValue);
      }
    }
    return asacFolderLookup;
  }
  
  /**
   * append to url, escape first
   * @param url
   * @param paramName
   * @param paramValue
   */
  public static void urlEscapeAndAppend(StringBuilder url, String paramName, String paramValue) {
    if (url.indexOf("?") == -1) {
      url.append("?");
    } else {
      url.append("&");
    }
    url.append(escapeUrlEncode(paramName)).append("=").append(escapeUrlEncode(paramValue));
  }

  /**
   * convert a bean to string
   * @param asacResultProblem
   * @param result
   * @return the string representation
   */
  public static void convertBeanToString(AsacResultProblem asacResultProblem, StringBuilder result) {
    if (!StandardApiClientUtils.isBlank(asacResultProblem.getError())) {
      StandardApiClientUtils.append(result, ", ", asacResultProblem.getError());
    }
    if (!StandardApiClientUtils.isBlank(asacResultProblem.getError_description())) {
      StandardApiClientUtils.append(result, ", ", asacResultProblem.getError_description());
    }
    if (!StandardApiClientUtils.isBlank(asacResultProblem.getError_uri())) {
      StandardApiClientUtils.append(result, ", ", asacResultProblem.getError_uri());
    }
    if (asacResultProblem.getMeta() != null) {
      if (!StandardApiClientUtils.isBlank(asacResultProblem.getMeta().getStatus())) {
        StandardApiClientUtils.append(result, ", ", asacResultProblem.getMeta().getStatus());
      }
    }
    if (asacResultProblem.getResponseMeta() != null) {
      if (!StandardApiClientUtils.isBlank(asacResultProblem.getResponseMeta().getHttpStatusCode())) {
        StandardApiClientUtils.append(result, ", ", asacResultProblem.getResponseMeta().getHttpStatusCode().toString());
      }
    }
    if (asacResultProblem.getServiceMeta() != null) {
      if (!StandardApiClientUtils.isBlank(asacResultProblem.getServiceMeta().getServerVersion())) {
        StandardApiClientUtils.append(result, ", ", asacResultProblem.getServiceMeta().getServerVersion());
      }
    }
    if (asacResultProblem.getServiceMeta() != null) {
      if (!StandardApiClientUtils.isBlank(asacResultProblem.getServiceMeta().getServiceRootUri())) {
        StandardApiClientUtils.append(result, ", ", asacResultProblem.getServiceMeta().getServiceRootUri());
      }
    }
  }
  
  /**
   * logger
   */
  private static Log LOG = StandardApiClientUtils.retrieveLog(StandardApiClientUtils.class);

  /**
   * configure jdk14 logs once
   */
  private static boolean configuredLogs = false;

  /**
   * 
   * @return current version for url
   */
  public static String version() {
    return "v1";
  }
  
  /**
   * @param theClass
   * @return the log
   */
  public static Log retrieveLog(Class<?> theClass) {

    Log theLog = LogFactory.getLog(theClass);
    
    //if this isnt here, dont configure yet
    if (isBlank(StandardApiClientConfig.retrieveConfig().propertyValueString("encrypt.disableExternalFileLookup"))
        || theClass.equals(StandardApiClientCommonUtils.class)) {
      return new StandardApiClientLog(theLog);
    }
    
    if (!configuredLogs) {
      String logLevel = StandardApiClientConfig.retrieveConfig().propertyValueString("authzStandardApiClient.logging.logLevel");
      String logFile = StandardApiClientConfig.retrieveConfig().propertyValueString("authzStandardApiClient.logging.logFile");
      String authzStandardApiClientLogLevel = StandardApiClientConfig.retrieveConfig().propertyValueString(
          "authzStandardApiClient.logging.authzStandardApiClientOnly.logLevel");
      
      boolean hasLogLevel = !isBlank(logLevel);
      boolean hasLogFile = !isBlank(logFile);
      boolean hasStandardApiClientLogLevel = !isBlank(authzStandardApiClientLogLevel);
      
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
      
      if (hasStandardApiClientLogLevel) {
        Level level = Level.parse(authzStandardApiClientLogLevel);
        Log authzStandardApiClientLog = LogFactory.getLog("edu.internet2.middleware.authzStandardApiClient");
        if (authzStandardApiClientLog instanceof Jdk14Logger) {
          Jdk14Logger jdkLogger = (Jdk14Logger) authzStandardApiClientLog;
          Logger logger = jdkLogger.getLogger();
          logger.setLevel(level);
        }
      }
      
      configuredLogs = true;
    }
    
    return new StandardApiClientLog(theLog);
    
  }
  
  /**
   * convert an object to json without wrapping it with the simple class name.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertToNoWrap(Object object) {
    if (object == null) {
      throw new NullPointerException();
    }

    JsonConfig jsonConfig = new JsonConfig();
    jsonConfig.setJsonPropertyFilter(new PropertyFilter() {

      public boolean apply(Object source, String name, Object value) {
        //json-lib cannot handle maps where the key is not a string
        if (value != null && value instanceof Map) {
          @SuppressWarnings("rawtypes")
          Map map = (Map) value;
          if (map.size() > 0 && !(map.keySet().iterator().next() instanceof String)) {
            return true;
          }
        }
        return value == null;
      }
    });
    JSONObject jsonObject = JSONObject.fromObject(object, jsonConfig);
    String json = jsonObject.toString();

    return json;
  }

  /**
   * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
   * @param json is the json string, not wrapped with a simple class name
   * @param theClass is the class that the object should be coverted into.
   * Note: only the top level object needs to be registered
   * @return the object
   * @param T is the type
   */
  @SuppressWarnings("unchecked")
  public static <T> T jsonConvertFrom (String json, Class<T> theClass) {
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object object = JSONObject.toBean( jsonObject, theClass );  
      return (T)object;
  }

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
    

    @Override
    public Object get(String name) {
      
      //see if registered      
      Object object = super.get(name);
      
      if (object != null) {
        return object;
      }
      return retrieveClass(name);
    }

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
      jc.set("elUtils", new AsacElUtilsSafe());
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
            throw new AsacExpressionLanguageMissingVariableException(message, je);
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
      if (e instanceof AsacExpressionLanguageMissingVariableException) {
        throw (AsacExpressionLanguageMissingVariableException)e;
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
   * 
   * @return the encrypt key
   */
  public static String encryptKey() {
    String encryptKey = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("encrypt.key");
    
    boolean disableExternalFileLookup = StandardApiClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");
    
    //lets lookup if file
    encryptKey = StandardApiClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
    
    //the server does this, so if the key is blank, it will still have something there, so be consistent
    if (StandardApiClientConfig.retrieveConfig().propertyValueBoolean("encrypt.encryptLikeServer", false)) {
      
      encryptKey += "w";
    }
    
    return encryptKey;
  }

  /**
     * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
     * @param conversionMap is the class simple name to class of objects which are allowed to be brought back.
     * Note: only the top level object needs to be registered
     * @param json
     * @return the object
     */
    public static Object jsonConvertFrom(Map<String, Class<?>> conversionMap, String json) {
  
      //gson does not put the type of the object in the json, but we need that.  so when we convert,
      //put the type in there.  So we need to extract the type out when unmarshaling
      Matcher matcher = jsonPattern.matcher(json);
  
      if (!matcher.matches()) {
        throw new RuntimeException("Cant match this json, should start with simple class name: " + json);
      }
  
      String simpleClassName = matcher.group(1);
      String jsonBody = matcher.group(2);
  
      Class<?> theClass = conversionMap.get(simpleClassName);
      if (theClass == null) {
        throw new RuntimeException("Not allowed to unmarshal json: " + simpleClassName + ", " + json);
      }
  //    Gson gson = new GsonBuilder().create();
  //    Object object = gson.fromJson(jsonBody, theClass);
      JSONObject jsonObject = JSONObject.fromObject( jsonBody );
      Object object = JSONObject.toBean( jsonObject, theClass );
  
      return object;
    }

  /**
   * <pre>
   * detects the front of a json string, pops off the first field, and gives the body as the matcher
   * ^\s*\{\s*\"([^"]+)\"\s*:\s*\{(.*)}$
   * Example matching text:
   * {
   *  "XstreamPocGroup":{
   *    "somethingNotMarshaled":"whatever",
   *    "name":"myGroup",
   *    "someInt":5,
   *    "someBool":true,
   *    "members":[
   *      {
   *        "name":"John",
   *        "description":"John Smith - Employee"
   *      },
   *      {
   *        "name":"Mary",
   *        "description":"Mary Johnson - Student"
   *      }
   *    ]
   *  }
   * }
   *
   * ^\s*          front of string and optional space
   * \{\s*         open bracket and optional space
   * \"([^"]+)\"   quote, simple name of class, quote
   * \s*:\s*       optional space, colon, optional space
   * \{(.*)}$      open bracket, the class info, close bracket, end of string
   *
   *
   * </pre>
   */
  private static Pattern jsonPattern = Pattern.compile("^\\s*\\{\\s*\\\"([^\"]+)\\\"\\s*:\\s*(.*)}$", Pattern.DOTALL);
  
  /** iso date string */
  private static final String YYYY_MM_DD_T_HH_MM_SS_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  /**
   * convert the date to a string
   * 2012-10-04T03:10.123Z
   * @param date
   * @return the string
   */
  public static String convertToIso8601(Date date) {
    
    if (date == null) {
      return null;
    }
    
    DateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_SSS_Z);
    
    return dateFormat.format(date);
    
  }
  
  /**
   * convert the string to a date
   * 2012-10-04T03:10.123Z
   * @param date
   * @return the string
   */
  public static Date convertFromIso8601(String date) {
    
    if (date == null) {
      return null;
    }
    
    DateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_SSS_Z);
    
    try {
      return dateFormat.parse(date);
    } catch (ParseException parseException) {
      throw new RuntimeException(parseException);
    }
  }

}
