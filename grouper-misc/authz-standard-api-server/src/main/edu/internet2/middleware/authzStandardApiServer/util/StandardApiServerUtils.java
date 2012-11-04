package edu.internet2.middleware.authzStandardApiServer.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;
import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasFilterJ2ee;
import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasRestServlet;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.authzStandardApiServerExt.com.thoughtworks.xstream.io.xml.CompactWriter;
import edu.internet2.middleware.authzStandardApiServerExt.net.sf.json.JSONObject;
import edu.internet2.middleware.authzStandardApiServerExt.net.sf.json.JsonConfig;
import edu.internet2.middleware.authzStandardApiServerExt.net.sf.json.util.PropertyFilter;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.jexl2.Expression;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.jexl2.JexlContext;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.jexl2.JexlEngine;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.jexl2.JexlException;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.jexl2.MapContext;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.impl.Jdk14Logger;

public class StandardApiServerUtils extends StandardApiServerCommonUtils {

  /**
   * structure name of class
   * @param theClass
   * @return the structure name
   */
  public static String structureName(Class<?> theClass) {
    String registerByName = theClass.getSimpleName();
    if (registerByName.toLowerCase().startsWith("asas")) {
      registerByName = registerByName.substring(4);
    }
    registerByName = StandardApiServerUtils.lowerFirstLetter(registerByName);
    return registerByName;
  }
  
  /**
   * logger
   */
  private static Log LOG = StandardApiServerUtils.retrieveLog(StandardApiServerUtils.class);

  /**
   * return something like https://server.whatever.ext/appName/servlet
   * @return the url of the servlet with no slash
   */
  public static String servletUrl() {
    
    HttpServletRequest httpServletRequest = AsasFilterJ2ee.retrieveHttpServletRequest();
    
    String fullUrl = httpServletRequest.getRequestURL().toString();
    
    String servletPath = httpServletRequest.getServletPath();
    
    return fullUrlToServletUrl(fullUrl, servletPath, AsasRestServlet.retrieveContentType());
  }
  
  /**
   * 
   * @param fullUrl https://whatever/appName/servlet
   * @oaram servletPath /servlet
   * @return the servlet url
   */
  static String fullUrlToServletUrl(String fullUrl, String servletPath, AsasRestContentType wsRestContentType) {
    
    if (servletPath.endsWith("." + wsRestContentType.name())) {
      servletPath = servletPath.substring(0,servletPath.length()-(1+wsRestContentType.name().length()));
    }
    
    int fromIndex = 0;
    for (int i=0;i<4;i++) {
      fromIndex = fullUrl.indexOf('/', fromIndex+1);
    }
    
    int servletIndex = fullUrl.indexOf(servletPath, fromIndex);
    
    return fullUrl.substring(0, servletIndex + servletPath.length());

  }
  
  /**
   * generate a uuid
   * @return uuid
   */
  public static String uuid() {
    String uuid = UUID.randomUUID().toString();
    
    char[] result = new char[32];
    int resultIndex = 0;
    for (int i=0;i<uuid.length();i++) {
      char theChar = uuid.charAt(i);
      if (theChar != '-') {
        if (resultIndex >= result.length) {
          throw new RuntimeException("Why is resultIndex greater than result.length ???? " 
              + resultIndex + " , " + result.length + ", " + uuid);
        }
        result[resultIndex++] = theChar;
      }
    }
    return new String(result);

  }
  
  /**
   * configure jdk14 logs once
   */
  private static boolean configuredLogs = false;

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
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    return dateFormat.format(date);
    
  }
  
  /**
   * @param theClass
   * @return the log
   */
  public static Log retrieveLog(Class<?> theClass) {

    Log theLog = LogFactory.getLog(theClass);
    
    //if this isnt here, dont configure yet
    if (isBlank(StandardApiServerConfig.retrieveConfig().propertyValueString("encrypt.disableExternalFileLookup"))
        || theClass.equals(StandardApiServerCommonUtils.class)) {
      return new StandardApiServerLog(theLog);
    }
    
    if (!configuredLogs) {
      String logLevel = StandardApiServerConfig.retrieveConfig().propertyValueString("grouperClient.logging.logLevel");
      String logFile = StandardApiServerConfig.retrieveConfig().propertyValueString("grouperClient.logging.logFile");
      String grouperClientLogLevel = StandardApiServerConfig.retrieveConfig().propertyValueString(
          "grouperClient.logging.grouperClientOnly.logLevel");
      
      boolean hasLogLevel = !isBlank(logLevel);
      boolean hasLogFile = !isBlank(logFile);
      boolean hasStandardApiClientLogLevel = !isBlank(grouperClientLogLevel);
      
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
    
    return new StandardApiServerLog(theLog);
    
  }
  
  /**
   * <pre>
   * this method will indent xml or json.
   * this is for logging or documentations purposes only and should
   * not be used for a production use (since it is not 100% tested
   * or compliant with all constructs like xml CDATA
   *
   * For xml, assumes elements either have text or sub elements, not both.
   * No cdata, nothing fancy.
   *
   * If the input is &lt;a&gt;&lt;b&gt;&lt;c&gt;hey&lt;/c&gt;&lt;d&gt;&lt;e&gt;there&lt;/e&gt;&lt;/d&gt;&lt;/b&gt;&lt;/a&gt;
   * It would output:
   * &lt;a&gt;
   *   &lt;b&gt;
   *     &lt;c&gt;hey&lt;/c&gt;
   *     &lt;d&gt;
   *       &lt;e&gt;there&lt;/e&gt;
   *     &lt;/d&gt;
   *   &lt;/b&gt;
   * &lt;/a&gt;
   *
   * For , if the input is: {"a":{"b\"b":{"c\\":"d"},"e":"f","g":["h":"i"]}}
   * It would output:
   * {
   *   "a":{
   *     "b\"b":{
   *       "c\\":"d"
   *     },
   *     "e":"f",
   *     "g":[
   *       "h":"i"
   *     ]
   *   }
   * }
   *
   *
   * <pre>
   * @param string
   * @param failIfTypeNotFound
   * @return the indented string, 2 spaces per line
   */
  public static String indent(String string, boolean failIfTypeNotFound) {
    if (string == null) {
      return null;
    }
    string = trim(string);
    if (string.startsWith("<")) {
      //this is xml
      return new XmlIndenter(string).result();
    }
    if (string.startsWith("{")) {
      return new JsonIndenter(string).result();
    }
    if (!failIfTypeNotFound) {
      //just return if cant indent
      return string;
    }
    throw new RuntimeException("Cant find type of string: " + string);
  }

  /**
   * convert an object to json.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertTo(Object object) {
    return jsonConvertTo(object, true);
  }

  /**
   * convert an object to json.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertTo(Object object, boolean includeObjectNameWrapper) {

    if (object == null) {
      throw new NullPointerException();
    }
//    Gson gson = new GsonBuilder().create();
//    String json = gson.toJson(object);

//    JSONObject jsonObject = net.sf.json.JSONObject.fromObject( object );
//    String json = jsonObject.toString();

    JsonConfig jsonConfig = new JsonConfig();
    jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
       public boolean apply( Object source, String name, Object value ) {
         //json-lib cannot handle maps where the key is not a string
         if( value != null && value instanceof Map ){
           Map map = (Map)value;
           if (map.size() > 0 && !(map.keySet().iterator().next() instanceof String)) {
             return true;
           }
         }
         return value == null;
       }
    });
    JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );
    String json = jsonObject.toString();

    if (!includeObjectNameWrapper) {
      return json;
    }
    return "{\"" + object.getClass().getSimpleName() + "\":" + json + "}";
  }
  /**
   * convert an object to json without wrapping it with the simple class name.
   * @param object
   * @return the string of json
   */
  public static String jsonConvertToNoWrap(Object object) {
    //TODO call the other jsonConvertTo() method
      if (object == null) {
        throw new NullPointerException();
      }

      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
         public boolean apply( Object source, String name, Object value ) {
           //json-lib cannot handle maps where the key is not a string
           if( value != null && value instanceof Map ){
             Map map = (Map)value;
             if (map.size() > 0 && !(map.keySet().iterator().next() instanceof String)) {
               return true;
             }
           }
           return value == null;
         }
      });
      JSONObject jsonObject = JSONObject.fromObject( object, jsonConfig );
      String json = jsonObject.toString();

      return json;
    }

  /**
   * convert an object to json.  note this wraps the gson with the object simple name so it can be revived
   * @param object
   * @param writer
   */
  public static void jsonConvertTo(Object object, Writer writer) {
    String json = jsonConvertTo(object);
    try {
      writer.write(json);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
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
   * convert an object from json.  note this works well if there are no collections, just real types, arrays, etc.
   * @param json is the json string, not wrapped with a simple class name
   * @param theClass is the class that the object should be coverted into.
   * Note: only the top level object needs to be registered
   * @return the object
   */
  public static Object jsonConvertFrom (String json, Class<?> theClass) {
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object object = JSONObject.toBean( jsonObject, theClass );
      return object;

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
      jc.set("elUtils", new AsasElUtilsSafe());
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
            throw new AsasExpressionLanguageMissingVariableException(message, je);
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
      if (e instanceof AsasExpressionLanguageMissingVariableException) {
        throw (AsasExpressionLanguageMissingVariableException)e;
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
   * convert from String to XML
   * @return the instances
   */
  @SuppressWarnings("unchecked")
  public static <T> T xmlConvertFrom(String xml, Class<T> theClass) {
    XStream xStream = StandardApiServerXstreamUtils.retrieveXstream();
    
    Object marshaledInstance = xStream.fromXML(xml);
    
    if (marshaledInstance != null) {
      if (!theClass.isAssignableFrom(marshaledInstance.getClass())) {
        throw new RuntimeException("Expecting XML class: " + theClass + ", but was: " 
            + marshaledInstance.getClass() + ", " + StandardApiServerUtils.abbreviate(xml, 300));
      }
    }
    
    return (T)marshaledInstance;
    
  }

  /**
   * 
   * @param object
   * @return the xml
   */
  public static String xmlConvertTo(Object object) {
    XStream xStream = StandardApiServerXstreamUtils.retrieveXstream();
    StringWriter stringWriter = new StringWriter();
    //dont indent
    xStream.marshal(object, new CompactWriter(stringWriter));

    String requestDocument = stringWriter.toString();
    return requestDocument;

  }
  
  /**
   * convert a query string to a map (for testing purposes only, in a real system the
   * HttpServletRequest would be used.  no dupes allowed
   * @param queryString
   * @return the map (null if no query string)
   */
  public static Map<String, String> convertQueryStringToMap(String queryString) {
    
    if (StandardApiServerUtils.isBlank(queryString)) {
      return null;
    }
    
    Map<String, String> paramMap = new HashMap<String, String>();
    
    String[] queryStringPairs = StandardApiServerUtils.splitTrim(queryString, "&");
    
    for (String queryStringPair : queryStringPairs) {
      
      String key = StandardApiServerUtils.prefixOrSuffix(queryStringPair, "=", true);
      String value = StandardApiServerUtils.prefixOrSuffix(queryStringPair, "=", false);
      //unescape the value
      value = StandardApiServerUtils.escapeUrlDecode(value);
      
      if (paramMap.containsKey(key)) {
        throw new RuntimeException("Query string contains two of the same key: " 
            + key + ", " + queryString);
        
      }
      paramMap.put(key, value);
    }
    return paramMap;
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
