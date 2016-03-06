package edu.internet2.middleware.tierApiAuthzServer.util;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiFolderInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderLookup;
import edu.internet2.middleware.tierApiAuthzServer.j2ee.TaasFilterJ2ee;
import edu.internet2.middleware.tierApiAuthzServer.version.TaasWsVersion;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.JSONObject;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.JsonConfig;
import edu.internet2.middleware.tierApiAuthzServerExt.net.sf.json.util.PropertyFilter;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.jexl2.Expression;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.jexl2.JexlContext;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.jexl2.JexlEngine;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.jexl2.JexlException;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.jexl2.MapContext;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.impl.Jdk14Logger;

/**
 * utility methods for the authz standard api server
 * 
 * @author mchyzer
 *
 */
public class StandardApiServerUtils extends StandardApiServerCommonUtils {

  /**
   * convert a uri to folder lookup
   * @param folderUri
   */
  public static AsasApiFolderLookup folderConvertUriToLookup(String folderUri) {

    AsasApiFolderLookup asasApiFolderLookup = new AsasApiFolderLookup();

    if (folderUri.startsWith("name:")) {

      String name = folderUri.substring(5);
      asasApiFolderLookup.setName(name);

    } else if (folderUri.startsWith("id:")) {
  
      String id = folderUri.substring(3);
      asasApiFolderLookup.setId(id);
  
    } else if (folderUri.contains(":")) {
  
      String handleName = StandardApiServerUtils.prefixOrSuffix(folderUri, ":", true);
      String handleValue = StandardApiServerUtils.prefixOrSuffix(folderUri, ":", false);
      asasApiFolderLookup.setHandleName(handleName);
      asasApiFolderLookup.setHandleValue(handleValue);
      
    } else {
      throw new AsasRestInvalidRequest("folderUri needs to contain a colon, start " +
          "with name: id: or another uri prefix that is server specific: '" + folderUri + "'");
    }
    return asasApiFolderLookup;
  }
  
  /**
   * 
   * @return current version for url
   */
  public static String version() {
    //TODO: this should be based on the request
    return TaasWsVersion.serverVersion().name();
  }

  /**
   * if the path is the root folder
   * @param objectName
   * @return true if the path is the root folder
   */
  public static boolean pathIsRootFolder(String objectName) {
    
    return equals(":", objectName);
    
  }

  /**
   * get the parent folder name from an object name
   * @param objectName
   * @return the parent folder name, or null if it is the root folder (":")
   */
  public static String pathParentFolderName(String objectName) {
    
    if (objectName == null) {
      throw new NullPointerException("Why is there a null objectName?");
    }
    
    //there is no parent of the top parent
    if (pathIsRootFolder(objectName)) {
      return null;
    }
    
    //lets get the list of folders here:
    List<String> extensions = convertPathToExtensionList(objectName);
    
    //if top level, then return colon
    if (extensions.size() == 1) {
      return ":";
    }
    
    //else, pop off the top and convert back
    extensions.remove(extensions.size()-1);
    
    return convertPathFromExtensionList(extensions);
  }
  
  /**
   * convert the authz system path to use the standard api server path char
   * @param path a path that needs to be converted
   * @param currentSeparatorChar the separator of the current path
   * @param newSeparatorChar the separator of the new path, typically this would be:
   * String configuredSeparatorChar = StandardApiServerConfig.retrieveConfig().configItemPathSeparatorChar();
   * @return the new path with the configured separator
   */
  @Deprecated
  public static String convertPathToUseSeparatorAndEscapeOld(String path, String currentSeparatorChar, String newSeparatorChar) {
    
    if (StandardApiServerUtils.isBlank(newSeparatorChar) || newSeparatorChar.length() > 1) {
      throw new RuntimeException("pathSeparatorChar must be one char: '" + newSeparatorChar + "'");
    }

    if (StandardApiServerUtils.isBlank(currentSeparatorChar) || currentSeparatorChar.length() > 1) {
      throw new RuntimeException("separatorChar must be one char: '" + currentSeparatorChar + "'");
    }

    //split the path
    String[] extensions = StandardApiServerUtils.split(path, currentSeparatorChar.charAt(0));

    //loop through
    StringBuilder result = new StringBuilder();

    String hexString = "%" + Integer.toHexString(currentSeparatorChar.charAt(0)).toLowerCase();

    for (String extension : extensions) {
      if (result.length() > 0) {
        result.append(newSeparatorChar);
      }

      //escape percents
      extension = StandardApiServerUtils.replace(extension, "%", "%25");

      //escape the separator
      extension = StandardApiServerUtils.replace(extension, newSeparatorChar, hexString);
      
      result.append(extension);
    }
    
    return result.toString();
    
  }
  
  /**
   * convert an authz standard path (colon separated)
   * @param authzStandardPath
   * @return the list of strings
   */
  public static List<String> convertPathToExtensionList(String authzStandardPath) {
    List<String> resultList = new ArrayList<String>();
    if (StringUtils.isBlank(authzStandardPath)) {
      return resultList;
    }
    
    StringBuilder currentExtension = new StringBuilder();
    
    //lets loop through, and look for slash or colon
    char[] theChars = authzStandardPath.toCharArray();
    for (int i=0;i<theChars.length;i++) {
      char curChar = theChars[i];

      //end of the extension
      if (curChar == ':') {
        resultList.add(currentExtension.toString());
        currentExtension = new StringBuilder();
        continue;
      }
      
      //escaped character
      if (curChar == '\\') {
        
        //if there is no next char, then there is an error
        if (i==theChars.length-1) {
          throw new RuntimeException("There is a slash at the end of the path, " +
          		"but there is no next char, this is an error: '" + authzStandardPath + "'" );
        }
        
        //we arent at the end of the string, so what is the next char
        i++;
        char nextChar = theChars[i];
        
        //if the next char is colon or slash, then it is an escaped colon or slash
        if (nextChar == ':' || nextChar == '\\') {
          curChar = nextChar;
        } else {
          throw new RuntimeException("There is a slash in the path, " +
              "but there the next char is not colon or slash: '" + nextChar 
              + "', this is an error: '" + authzStandardPath + "'" );
        }
        
        //dont continue, since we need to append that char below
      }

      //normal character
      currentExtension.append(curChar);
    }
    //add the last extension on
    resultList.add(currentExtension.toString());
    
    //this doenst work for complex escaped cases
    //String[] extensions = split(authzStandardPath, ':');
    //for (int i=0;i<extensions.length;i++) {
    //  extensions[i] = extensions[i].replace("\\:", ":");
    //  resultList.add(extensions[i].replace("\\\\", "\\"));
    //}
    return resultList;
  }
  
  /**
   * convert an authz standard path (colon separated)
   * @param extensionList
   * @return the list of strings
   */
  public static String convertPathFromExtensionList(List<String> extensionList) {
    StringBuilder result = new StringBuilder();
    if (length(extensionList) > 0) {
      for (String extension : extensionList) {
        if (extension == null) {
          throw new NullPointerException("Why is extension null????");
        }
         
        if (result.length() > 0) {
          result.append(":");
        }
        
        extension = extension.replace("\\", "\\\\");
        extension = extension.replace(":", "\\:");
        result.append(extension);
      }
    }
    return result.toString();
  }
  
  /**
   * convert a path passed in from client to use the separator that the authz server understands
   * note: the path cannot contain any of the new separator chars...
   * @param path a path that needs to be converted
   * @param newSeparatorChar the separator of the current path
   * @return the new path with the configured separator
   */
  @Deprecated
  public static String convertPathToUseSeparatorAndUnescapeOld(String path, String currentSeparatorChar, String newSeparatorChar) {
    
    if (StandardApiServerUtils.isBlank(currentSeparatorChar) || currentSeparatorChar.length() > 1) {
      throw new RuntimeException("separatorChar must be one char: '" + currentSeparatorChar + "'");
    }

    if (StandardApiServerUtils.isBlank(newSeparatorChar) || newSeparatorChar.length() > 1) {
      throw new RuntimeException("separatorChar must be one char: '" + newSeparatorChar + "'");
    }

    //split the path
    String[] extensions = StandardApiServerUtils.split(path, currentSeparatorChar.charAt(0));

    //loop through
    StringBuilder result = new StringBuilder();

    String hexString = "%" + Integer.toHexString(currentSeparatorChar.charAt(0)).toLowerCase();

    for (String extension : extensions) {
      if (result.length() > 0) {
        result.append(newSeparatorChar);
      }

      //unescape the separator
      extension = StandardApiServerUtils.replace(extension, hexString, currentSeparatorChar);

      //escape percents
      extension = StandardApiServerUtils.replace(extension, "%25", "%");
      
      if (extension.contains(newSeparatorChar)) {
        throw new RuntimeException("extensions cannot contain the server separator char: extension: '" 
            + extension + "', separator: '" + newSeparatorChar + "'");
      }
      
      result.append(extension);

    }
    
    return result.toString();
    
  }

  /**
   * see which group interface is configured and make an instance of it
   * @return an instance of the group interface
   */
  public static AsasApiGroupInterface interfaceGroupInstance() {
    
    String className = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.interface.group");
    
    @SuppressWarnings("unchecked")
    Class<AsasApiGroupInterface> theClass = (Class<AsasApiGroupInterface>)forName(className);
    
    return newInstance(theClass);
  }

  /**
   * see which folder interface is configured and make an instance of it
   * @return an instance of the folder interface
   */
  public static AsasApiFolderInterface interfaceFolderInstance() {
    
    String className = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.interface.folder");
    
    @SuppressWarnings("unchecked")
    Class<AsasApiFolderInterface> theClass = (Class<AsasApiFolderInterface>)forName(className);
    
    return newInstance(theClass);
  }
  
  /**
   * do a case-insensitive matching
   * @param theEnumClass class of the enum
   * @param <E> generic type
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws RuntimeException if there is a problem
   */
  public static <E extends Enum<?>> E enumValueOfIgnoreCase(Class<E> theEnumClass, String string, 
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {

    try {
      return StandardApiServerCommonUtils.enumValueOfIgnoreCase(theEnumClass, string, exceptionOnNotFound);
    } catch (RuntimeException re) {
      if (!(re instanceof AsasRestInvalidRequest)) {
        throw new AsasRestInvalidRequest(re.getMessage(), re);
      }
      throw re;
    }
  }

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
    
    HttpServletRequest httpServletRequest = TaasFilterJ2ee.retrieveHttpServletRequest();
    
    if (httpServletRequest == null) {
      String servletUrl = StandardApiServerConfig.retrieveConfig().propertyValueStringRequired("tierApiAuthzServer.servletUrl");
      if (StandardApiServerUtils.isBlank(servletUrl)) {
        throw new RuntimeException("Cant find servlet URL!  you can set it in authzStandardapi.server.properties as tierApiAuthzServer.servletUrl");
      }
      return servletUrl;
    }
    
    String fullUrl = httpServletRequest.getRequestURL().toString();
    
    String servletPath = httpServletRequest.getServletPath();
    
    return fullUrlToServletUrl(fullUrl, servletPath, AsasRestContentType.retrieveContentType());
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
           @SuppressWarnings("rawtypes")
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
             @SuppressWarnings("rawtypes")
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
