/**
 * 
 */
package edu.internet2.middleware.grouper.util;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCloneable;
import edu.internet2.middleware.subject.Subject;


/**
 * utility methods for grouper.
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GrouperUtil {

  /**
   * append and maybe put a separator in there
   * @param result
   * @param separatorIfResultNotEmpty
   * @param stringToAppend
   */
  public static void append(StringBuilder result, 
      String separatorIfResultNotEmpty, String stringToAppend) {
    if (result.length() != 0) {
      result.append(separatorIfResultNotEmpty);
    }
    result.append(stringToAppend);
  }
  
  /**
   * 
   */
  public static final String LOG_ERROR = "Error trying to make parent dirs for logger or logging first statement, check to make " +
            		"sure you have proper file permissions, and that your servlet container is giving " +
            		"your app rights to access the log directory (e.g. for tomcat set TOMCAT5_SECURITY=no), g" +
            		"oogle it for more info";

  /**
   * The number of bytes in a kilobyte.
   */
  public static final long ONE_KB = 1024;

  /**
   * The number of bytes in a megabyte.
   */
  public static final long ONE_MB = ONE_KB * ONE_KB;

  /**
   * The number of bytes in a gigabyte.
   */
  public static final long ONE_GB = ONE_KB * ONE_MB;
  
  /**
   * The number of bytes in a gigabyte.
   */
  public static final String grouperHome = System.getProperty("grouper.home");
  

  /**
   * Returns a human-readable version of the file size (original is in
   * bytes).
   *
   * @param size The number of bytes.
   * @return     A human-readable display value (includes units).
   * @todo need for I18N?
   */
  public static String byteCountToDisplaySize(long size) {
    String displaySize;

    if (size / ONE_GB > 0) {
      displaySize = String.valueOf(size / ONE_GB) + " GB";
    } else if (size / ONE_MB > 0) {
      displaySize = String.valueOf(size / ONE_MB) + " MB";
    } else if (size / ONE_KB > 0) {
      displaySize = String.valueOf(size / ONE_KB) + " KB";
    } else {
      displaySize = String.valueOf(size) + " bytes";
    }

    return displaySize;
  }
  /**
   * get a logger, and auto-create log dirs if havent done yet
   * @param theClass
   * @return the logger
   */
  public static Log getLog(Class<?> theClass) {
    logDirsCreateIfNotDone();
    return LogFactory.getLog(theClass);
  }
  
  /**
   * see if created log dirs
   */
  private static boolean logDirsCreated = false;
  
   
  /**
   * auto-create log dirs if not done yet
   */
  private static void logDirsCreateIfNotDone() {
    if (logDirsCreated) {
      return;
    }
    logDirsCreated = true;
    
    String location = "log4j.properties";
    Properties properties = GrouperUtil.propertiesFromResourceName(location);
    Set<String> keySet = (Set<String>)(Object)properties.keySet();
    for (String key : keySet) {
      //if its a file property
      if (key.endsWith(".File")) {
        try {
          String fileName = properties.getProperty(key);
          if(fileName.startsWith("${grouper.home}")) {
        	  if(grouperHome==null) {
        		throw new IllegalStateException("The System property grouper.home is referenced in log4j configuration " +
        				"however, it is not set.");  
        	  }
        	  fileName=grouperHome+fileName.substring(15);
          }
          File file = new File(fileName);
          File parent = file.getParentFile();

          if (parent != null && !parent.exists()) {
            //dont have a logger yet, so just print to stdout
            System.out.println("Grouper warning: parent dir of log file doesnt exist: " + fileCanonicalPath(parent));
            //create the parent
            mkdirs(parent);
            System.out.println("Grouper note: auto-created parent dir of log file: " + fileCanonicalPath(parent));
            
          }

        } catch (RuntimeException re) {
          //this is bad, print to stderr rightaway (though might dupe)
          System.err.println(GrouperUtil.LOG_ERROR);
          re.printStackTrace();
          throw new RuntimeException(LOG_ERROR, re);
        }
      }
    }
  }
  
  /**
   * see if options have a specific option by int bits
   * @param options
   * @param option
   * @return if the option is there
   */
  public static boolean hasOption(int options, int option) {
    return (options & option) > 0;
  }
  
  /** keep a cache of db change whitelists */
  private static Set<MultiKey> dbChangeWhitelist = new HashSet<MultiKey>();
  
  /**
   * store if we are writing default logs to console
   */
  private static Boolean printGrouperLogsToConsole = null;

  /**
   * if the grouper logs go to the console or not
   * @return if 
   */
  public static boolean isPrintGrouperLogsToConsole() {
    if (printGrouperLogsToConsole == null) {
      logDirPrint();
    }
    return printGrouperLogsToConsole;
  }
  
  /**
   * get canonical path of file
   * @param file
   * @return the path
   */
  public static String fileCanonicalPath(File file) {
    try {
      return file.getCanonicalPath();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
  /**
   * log dir message
   */
  private static String logDirMessage = null;
  
  /**
   * print the log dir to the console so the logs are easy to find 
   * @return the log dir message
   */
  public static String logDirPrint() {
    logDirsCreateIfNotDone();
    //only do this once
    if (printGrouperLogsToConsole != null) {
      return logDirMessage;
    }
    StringBuilder resultMessage = new StringBuilder();
    printGrouperLogsToConsole = false;
    Log rootLogger = LogFactory.getLog("edu.internet2.middleware.grouper");
    StringBuilder rootLoggerAppender = new StringBuilder();
    boolean writesLogs = false;
    
    
    if (rootLogger instanceof Log4JLogger) {
      Category log4jLogger = ((Log4JLogger)rootLogger).getLogger();
      int timeToLive = 30;
      //if level is null, then go to next.  well, honestly, I dont know
      //how the exact algorithm works... :)
      while (log4jLogger.getLevel() == null) {
        Category parent = log4jLogger.getParent();
        if (parent == null) {
          break;
        }
        log4jLogger = parent;
        if (timeToLive-- < 0) {
          break;
        }
      }
      //add all appenders from here and parents
      Set<Appender> allAppenders = new LinkedHashSet<Appender>();
      Category currentAppenderLogger = log4jLogger;
      while (currentAppenderLogger != null) {
        Enumeration allAppendersEnumeration = currentAppenderLogger.getAllAppenders();
        while (allAppendersEnumeration.hasMoreElements()) {
          allAppenders.add((Appender)allAppendersEnumeration.nextElement());
        }
        currentAppenderLogger = currentAppenderLogger.getParent();
      }
      
      for (Appender appender : allAppenders) {
        writesLogs = true;
        if (appender instanceof ConsoleAppender) {
          printGrouperLogsToConsole = true;
          rootLoggerAppender.append("console, ");
        } else if (appender instanceof FileAppender) {
          String path = ((FileAppender)appender).getFile();
          if (StringUtils.isBlank(path)) {
            resultMessage.append("Grouper error, file appender path is empty, maybe dir doesnt exist\n");
          } else {
            File logFile = new File(path);
            if (logFile.getParentFile() != null && !logFile.getParentFile().exists()) {
              resultMessage.append("Grouper warning: parent dir of log file doesnt exist: " + logFile.getAbsolutePath() + "\n");
              mkdirs(logFile.getParentFile());
              resultMessage.append("Grouper note: auto-created parent dir of log file: " + logFile.getAbsolutePath() + "\n");
            }
            rootLoggerAppender.append(logFile.getAbsolutePath()).append(", ");
          }
        } else {
          rootLoggerAppender.append("appender type: " + appender.getClass().getSimpleName()).append(", ");
        }
      }
      if (!writesLogs || !rootLogger.isErrorEnabled()) {
        resultMessage.append("Grouper warning, it is detected that you are not logging errors for " +
        		"package edu.internet2.middleware.grouper, you should enable logging at " +
        		"least at the WARN level in log4j.properties\n");
      } else {
        if (rootLogger.isErrorEnabled() && !rootLogger.isWarnEnabled()) {
          resultMessage.append("Grouper warning, it is detected that you are logging " +
          		"edu.internet2.middleware.grouper as ERROR and not WARN level.  It is " +
          		"recommended to log at at least WARN level in log4j.properties\n");
        }
        String logLevel = null;
        if (rootLogger.isTraceEnabled()) {
          logLevel = "TRACE";
        } else if (rootLogger.isDebugEnabled()) {
          logLevel = "DEBUG";
        } else if (rootLogger.isInfoEnabled()) {
          logLevel = "INFO";
        } else if (rootLogger.isWarnEnabled()) {
          logLevel = "WARN";
        } else if (rootLogger.isErrorEnabled()) {
          logLevel = "ERROR";
        } else if (rootLogger.isFatalEnabled()) {
          logLevel = "FATAL";
        }
        resultMessage.append("Grouper is logging to file:   " + rootLoggerAppender + "at min level " 
            + logLevel + " for package: edu.internet2.middleware.grouper, based on log4j.properties\n"); 
      }
    } else {
      resultMessage.append("Grouper logs are not using log4j: " + (rootLogger == null ? null : rootLogger.getClass()) + "\n");
    }
    logDirMessage = resultMessage.toString();
    return logDirMessage;
  }
  
  /**
   * return the suffix after a char.  If the char doesnt exist, just return the string
   * @param input string
   * @param theChar char
   * @return new string
   */
  public static String suffixAfterChar(String input, char theChar) {
    if (input == null) {
      return null;
    }
    //get the real type off the end
    int lastIndex = input.lastIndexOf(theChar);
    if (lastIndex > -1) {
      input = input.substring(lastIndex + 1, input.length());
    }
    return input;
  }

  /**
   * get the oracle underscore name e.g. javaNameHere -> JAVA_NAME_HERE
   *
   * @param javaName
   *          the java convention name
   *
   * @return the oracle underscore name based on the java name
   */
  public static String oracleStandardNameFromJava(String javaName) {
  
    StringBuilder result = new StringBuilder();
  
    if ((javaName == null) || (0 == "".compareTo(javaName))) {
      return javaName;
    }
  
    //if package is specified, only look at class name
    javaName = suffixAfterChar(javaName, '.');
  
    //dont check the first char
    result.append(javaName.charAt(0));
  
    char currChar;
  
    boolean previousCap = false;
    
    //loop through the string, looking for uppercase
    for (int i = 1; i < javaName.length(); i++) {
      currChar = javaName.charAt(i);
  
      //if uppcase append an underscore
      if (!previousCap && (currChar >= 'A') && (currChar <= 'Z')) {
        result.append("_");
      }
  
      result.append(currChar);
      if ((currChar >= 'A') && (currChar <= 'Z')) {
        previousCap = true;
      } else {
        previousCap = false;
      }
    }
  
    //this is in upper-case
    return result.toString().toUpperCase();
  }

  
  /**
   * see if two maps are the equivalent (based on number of entries, 
   * and the equals() method of the keys and values)
   * @param <K> 
   * @param <V> 
   * @param first
   * @param second
   * @return true if equal
   */
  public static <K,V> boolean mapEquals(Map<K,V> first, Map<K,V> second) {
    Set<K> keysMismatch = new HashSet<K>();
    mapDifferences(first, second, keysMismatch, null);
    //if any keys mismatch, then not equal
    return keysMismatch.size() == 0;
    
  }
  
  /**
   * empty map
   */
  private static final Map EMPTY_MAP = Collections.unmodifiableMap(new HashMap());
  
  /**
   * see if two maps are the equivalent (based on number of entries, 
   * and the equals() method of the keys and values)
   * @param <K> 
   * @param <V> 
   * @param first map to check diffs
   * @param second map to check diffs
   * @param differences set of keys (with prefix) of the diffs
   * @param prefix for the entries in the diffs (e.g. "attribute__"
   */
  public static <K,V> void mapDifferences(Map<K,V> first, Map<K,V> second, Set<K> differences, String prefix) {
    if (first == second) {
      return;
    }
    //put the collections in new collections so we can remove and keep track
    if (first == null) {
      first = EMPTY_MAP;
    }
    if (second == null) {
      second = EMPTY_MAP;
    } else {
      //make linked so the results are ordered
      second = new LinkedHashMap<K,V>(second);
    }
    int firstSize = first == null ? 0 : first.size();
    int secondSize = second == null ? 0 : second.size();
    //if both empty then all good
    if (firstSize == 0 && secondSize == 0) {
      return;
    }
   
    for (K key : first.keySet()) {

      if (second.containsKey(key)) {
        V firstValue = first.get(key);
        V secondValue = second.get(key);
        //keep track by removing from second
        second.remove(key);
        if (ObjectUtils.equals(firstValue, secondValue)) {
          continue;
        }
      }
      differences.add(StringUtils.isNotBlank(prefix) ? (K)(prefix + key) : key);
    }
    //add the ones left over in the second map which are not in the first map
    for (K key : second.keySet()) {
      differences.add(StringUtils.isNotBlank(prefix) ? (K)(prefix + key) : key);
    }
  }
  
  /**
   * sleep, if interrupted, throw runtime
   * @param millis
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
  }
  
  /**
   * If we can, inject this into the exception, else return false
   * @param t
   * @param message
   * @return true if success, false if not
   */
  public static boolean injectInException(Throwable t, String message) {
    
    String throwableFieldName = GrouperConfig.getProperty("throwable.data.field.name");
    
    if (StringUtils.isBlank(throwableFieldName)) {
      //this is the field for sun java 1.5
      throwableFieldName = "detailMessage";
    }
    try {
      String currentValue = t.getMessage();
      if (!StringUtils.isBlank(currentValue)) {
        currentValue += ",\n" + message;
      } else {
        currentValue = message;
      }
      assignField(t, throwableFieldName, currentValue);
      return true;
    } catch (Throwable t2) {
      //dont worry about what the problem is, return false so the caller can log
      return false;
    }
    
  }
  
  /**
   * see if there is a grouper properties db match
   * @param whitelist true if whitelist, false if blacklist
   * @param user is the db user
   * @param url is the db url
   * @return true if found match, false if not
   */
  public static boolean findGrouperPropertiesDbMatch(boolean whitelist, String user, String url) {

    //lets check the whitelist and blacklist first
    Properties grouperProperties = GrouperUtil.propertiesFromResourceName("grouper.properties");

    //check blacklist
    //db.change.deny.user.0=
    //db.change.deny.url.0=
    //db.change.allow.user.0=grouper
    //db.change.allow.url.0=jdbc:mysql://localhost:3306/grouper

    int index = 0;
    String typeString = whitelist ? "allow" : "deny";
    while (true) {
      String currentUser = StringUtils.trim(grouperProperties.getProperty(
          "db.change." + typeString + ".user." + index));
      String currentUrl = StringUtils.trim(grouperProperties.getProperty(
          "db.change." + typeString + ".url." + index));
      
      //if we are done checking
      if (StringUtils.isBlank(currentUser) || StringUtils.isBlank(currentUrl)) {
        break;
      }
      if (StringUtils.equals(currentUser, user) && StringUtils.equals(currentUrl, url)) {
        return true;
      }
      index++;
    }
    return false;
  }
  
  /** prompt key for schema export */
  public static String PROMPT_KEY_SCHEMA_EXPORT_ALL_TABLES = "schemaexport all tables";
  
  /** prompt key for reset data */
  public static String PROMPT_KEY_RESET_DATA = "delete all grouper data";
  
  /** dont prompt while testing etc, but make sure there has been at least one prompt */
  public static boolean stopPromptingUser = false;
  
  /** if string is not in here, echo to screen */
  private static Set<String> stopPromptingUserPrintlns = new HashSet<String>();
  
  /**
   * prompt the user about db changes
   * @param reason e.g. delete all tables
   * @param checkResponse true if the response from the user should be checked, or just display the prompt
   */
  public static void promptUserAboutDbChanges(String reason, boolean checkResponse) {
    
    Properties grouperHibernateProperties = GrouperUtil.propertiesFromResourceName("grouper.hibernate.properties");
    
    String url = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.url"));
    String user = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.username"));

    MultiKey cacheKey = new MultiKey(reason, url, user);
    
    //if already ok'ed this question in the jre instance, then we are all set
    if (dbChangeWhitelist.contains(cacheKey)) {
      return;
    }

    //maybe stop due to testing and at least one
    if (dbChangeWhitelist.size() > 0 && stopPromptingUser) {
      String message = "DB prompting has been disabled (e.g. due to testing), so this user '"
          + user + "' and url '" + url + "' are allowed for: " + reason;
      if (!stopPromptingUserPrintlns.contains(message)) { 
        System.out.println(message);
      }
      stopPromptingUserPrintlns.add(message);
      return;
    }
    
    //this might be set from junit ant task
    String allow = System.getProperty("grouper.allow.db.changes");
    if (StringUtils.equals("true", allow)) {
      System.out.println("System property grouper.allow.db.changes is true which allows db changes to user '" 
          + user + "' and url '" + url + "'");
      //all good, add to cache so we dont have to repeatedly tell user
      dbChangeWhitelist.add(cacheKey);
      return;
    }

    //check blacklist
    if (findGrouperPropertiesDbMatch(false, user, url)) {
      System.out.println("This DB user '" + user + "' and url '" + url + "' are denied to be " +
      		"changed in the grouper.properties");
      System.exit(1);

    }
    
    //check whitelist
    if (findGrouperPropertiesDbMatch(true, user, url)) {
      System.out.println("This DB user '" + user + "' and url '" + url + "' are allowed to be " +
          "changed in the grouper.properties");
      if (!checkResponse) {
        System.out.println("Unfortunately this is checked from ant so you have to type 'y' anyways...");
      }
    } else {
    
      BufferedReader stdin = null;
      String message = null; // Creates a variable called message for input
  
      try {
        stdin = new BufferedReader(new InputStreamReader(System.in));
        
        //CH 20080506: THIS DOESNT WORK!
        //make sure there is nothing already on stdin
        //int available = System.in.available();
        //System.out.println("Available: " + available);
        //
        ////read these on stdin
        //if (available > 0) {
        //  stdin.read(new char[available]);
        //}
        
        //ask user if ok
        System.out.println("(note, might need to type in your response multiple times (Java stdin is flaky))");
        System.out.println("(note, you can whitelist or blacklist db urls and users in the grouper.properties)");
        //note the following must be println and not just print so it will show up in ant
        String prompt = "Are you sure you want to " + reason + " in db user '" + user + "', db url '" + url + "'? (y|n): ";
        System.out.println(prompt);
        System.out.flush(); // empties buffer, before you input text
        if (!checkResponse) {
          return;
        }
        //we want to read until we dont get empty, and until we get a y or an n
        for (int i=0;i<10;i++) {
          message = stdin.readLine();
          message = StringUtils.trimToEmpty(message);
          if (!StringUtils.isEmpty(message)) {
            if (StringUtils.equalsIgnoreCase(message, "y") || StringUtils.equalsIgnoreCase(message, "n")) {
              break;
            }
            System.out.println("Didn't receive 'y' or 'n', received '" + message + "'...");       
            System.out.println(prompt);
            System.out.flush(); // empties buffer, before you input text
          }
        }
        if (!StringUtils.equalsIgnoreCase(message, "y") && !StringUtils.equalsIgnoreCase(message, "n")) {
          System.out.println("Sorry you are having trouble, try the whitelist in grouper.properties");
          System.exit(1);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      //CH: 20080506: Maybe we shouldnt close stdin... wont be able to use again?
      //} finally {
      //  GrouperUtil.closeQuietly(stdin);
      }
      if (!StringUtils.equalsIgnoreCase(message, "y")) {
        System.out.println("Didn't receive 'y', received '" + message + "', OK, exiting");
        System.exit(1);
      }
    }    
    //all good, add to cache so we dont have to repeatedly ask user
    dbChangeWhitelist.add(cacheKey);
    System.out.println("Continuing...");
  }

  /**
   * get a unique string identifier based on the current time,
   * this is not globally unique, just unique for as long as this
   * server is running...
   * 
   * @return String
   */
  public static String uniqueId() {
    //this needs to be threadsafe since we are using a static field
    synchronized (GrouperUtil.class) {
      lastId = incrementStringInt(lastId);
    }

    return String.valueOf(lastId);
  }

  /**
   * get a file name from a resource name
   * 
   * @param resourceName
   *          is the classpath location
   * 
   * @return the file path on the system
   */
  public static File fileFromResourceName(String resourceName) {
    
    URL url = computeUrl(resourceName, true);

    if (url == null) {
      return null;
    }

    File configFile = new File(url.getFile());

    return configFile;
  }
  

  /**
   * compute a url of a resource
   * @param resourceName
   * @param canBeNull if cant be null, throw runtime
   * @return the URL
   */
  public static URL computeUrl(String resourceName, boolean canBeNull) {
    //get the url of the navigation file
    ClassLoader cl = classLoader();

    URL url = null;

    try {
      //CH 20081012: sometimes it starts with slash and it shouldnt...
      String newResourceName = resourceName.startsWith("/") 
        ? resourceName.substring(1) : resourceName;
      url = cl.getResource(newResourceName);
    } catch (NullPointerException npe) {
      String error = "computeUrl() Could not find resource file: " + resourceName;
      throw new RuntimeException(error, npe);
    }

    if (!canBeNull && url == null) {
      throw new RuntimeException("Cant find resource: " + resourceName);
    }

    return url;
  }


  /**
   * fast class loader
   * @return the class loader
   */
  public static ClassLoader classLoader() {
    return GrouperUtil.class.getClassLoader();
  }

  /**
   * make sure a array is non null.  If null, then return an empty array.
   * Note: this will probably not work for primitive arrays (e.g. int[])
   * @param <T>
   * @param array
   * @return the list or empty list if null
   */
  public static <T> T[] nonNull(T[] array) {
    return array == null ? ((T[])new Object[0]) : array;
  }
  
  /**
   * get the prefix or suffix of a string based on a separator
   * 
   * @param startString
   *          is the string to start with
   * @param separator
   *          is the separator to split on
   * @param isPrefix
   *          if thre prefix or suffix should be returned
   * 
   * @return the prefix or suffix, if the separator isnt there, return the
   *         original string
   */
  public static String prefixOrSuffix(String startString, String separator,
      boolean isPrefix) {
    String prefixOrSuffix = null;

    //no nulls
    if (startString == null) {
      return startString;
    }

    //where is the separator
    int separatorIndex = startString.indexOf(separator);

    //if none exists, dont proceed
    if (separatorIndex == -1) {
      return startString;
    }

    //maybe the separator isnt on character
    int separatorLength = separator.length();

    if (isPrefix) {
      prefixOrSuffix = startString.substring(0, separatorIndex);
    } else {
      prefixOrSuffix = startString.substring(separatorIndex + separatorLength,
          startString.length());
    }

    return prefixOrSuffix;
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
   * For json, if the input is: {"a":{"b\"b":{"c\\":"d"},"e":"f","g":["h":"i"]}}
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
    string = StringUtils.trim(string);
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
   * get the extension from name.  if name is a:b:c, name is c
   * @param name
   * @return the name
   */
  public static String extensionFromName(String name) {
    if (StringUtils.isBlank(name)) {
      return name;
    }
    int lastColonIndex = name.lastIndexOf(':');
    if (lastColonIndex == -1) {
      return name;
    }
    String extension = name.substring(lastColonIndex+1);
    return extension;
  }
  
  /**
   * <pre>Returns the class object.</pre>
   * @param origClassName is fully qualified
   * @return the class
   */
  public static Class forName(String origClassName) {
        
    try {
      return Class.forName(origClassName);
    } catch (Throwable t) {
      throw new RuntimeException("Problem loading class: " + origClassName, t);
    }
    
  }
  
  /**
   * Construct a class
   * @param <T> template type
   * @param theClass
   * @return the instance
   */
  public static <T> T newInstance(Class<T> theClass) {
    try {
      return theClass.newInstance();
    } catch (Throwable e) {
      if (theClass != null && Modifier.isAbstract(theClass.getModifiers())) {
        throw new RuntimeException("Problem with class: " + theClass + ", maybe because it is abstract!", e);        
      }
      throw new RuntimeException("Problem with class: " + theClass, e);
    }
  }
  
  /**
   * get the parent stem name from name.  if already a root stem
   * then just return null.  e.g. if the name is a:b:c then
   * the return value is a:b
   * @param name
   * @return the parent stem name or null if none
   */
  public static String parentStemNameFromName(String name) {
    int lastColonIndex = name.lastIndexOf(':');
    if (lastColonIndex == -1) {
      return null;
    }
    String parentStemName = name.substring(0,lastColonIndex);
    return parentStemName;

  }
  
  /**
   * return the string or the other if the first is blank
   * @param string
   * @param defaultStringIfBlank
   * @return the string or the default one
   */
  public static String defaultIfBlank(String string, String defaultStringIfBlank) {
    return StringUtils.isBlank(string) ? defaultStringIfBlank : string;
  }
  
  /**
   * genericized method to see if first is null, if so then return second, else first.
   * @param <T>
   * @param theValue first input
   * @param defaultIfTheValueIsNull second input
   * @return the first if not null, second if no
   */
  public static <T> T defaultIfNull(T theValue, T defaultIfTheValueIsNull) {
    return theValue != null ? theValue : defaultIfTheValueIsNull;
  }
  
  /**
   * add each element of listToAdd if it is not already in list
   * @param <T>
   * @param list to add to
   * @param listToAdd each element will be added to list, or null if none
   */
  public static <T> void addIfNotThere(Collection<T> list, Collection<T> listToAdd) {
    //maybe nothing to do
    if (listToAdd == null) {
      return;
    }
    for (T t : listToAdd) {
      if (!list.contains(t)) {
        list.add(t);
      }
    }
  }

  
  /**
   * print out various types of objects
   * 
   * @param object
   * @param maxChars is where it should stop when figuring out object.  note, result might be longer than max...
   * need to abbreviate when back
   * @param result is where to append to
   */
  private static void toStringForLogHelper(Object object, int maxChars, StringBuilder result) {
    
    try {
      if (object == null) {
        result.append("null");
      } else if (object.getClass().isArray()) {
        // handle arrays
        int length = Array.getLength(object);
        if (length == 0) {
          result.append("Empty array");
        } else {
          result.append("Array size: ").append(length).append(": ");
          for (int i = 0; i < length; i++) {
            result.append("[").append(i).append("]: ").append(
                Array.get(object, i)).append("\n");
            if (maxChars != -1 && result.length() > maxChars) {
              return;
            }
          }
        }
      } else if (object instanceof Collection) {
        //give size and type if collection
        Collection<Object> collection = (Collection<Object>) object;
        int collectionSize = collection.size();
        if (collectionSize == 0) {
          result.append("Empty ").append(object.getClass().getSimpleName());
        } else {
          result.append(object.getClass().getSimpleName()).append(" size: ").append(collectionSize).append(": ");
          int i=0;
          for (Object collectionObject : collection) {
            result.append("[").append(i).append("]: ").append(
                collectionObject).append("\n");
            if (maxChars != -1 && result.length() > maxChars) {
              return;
            }
          }
        }
      } else {
        result.append(object.toString());
      }
    } catch (Exception e) {
      result.append("<<exception>> ").append(object.getClass()).append(":\n")
        .append(ExceptionUtils.getFullStackTrace(e)).append("\n");
    }
  }

  /**
   * convert a set to a string (comma separate)
   * @param set
   * @return the String
   */
  public static String setToString(Set set) {
    if (set == null) {
      return "null";
    }
    if (set.size() == 0) {
      return "empty";
    }
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Object object : set) {
      if (!first) {
        result.append(", ");
      }
      first = false;
      result.append(object);
    }
    return result.toString();
  }
  
  /**
   * convert a set to a string (comma separate)
   * @param map
   * @return the String
   */
  public static String MapToString(Map map) {
    if (map == null) {
      return "null";
    }
    if (map.size() == 0) {
      return "empty";
    }
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Object object : map.keySet()) {
      if (!first) {
        result.append(", ");
      }
      first = false;
      result.append(object).append(": ").append(map.get(object));
    }
    return result.toString();
  }

  /**
   * print out various types of objects
   * 
   * @param object
   * @return the string value
   */
  public static String toStringForLog(Object object) {
    StringBuilder result = new StringBuilder();
    toStringForLogHelper(object, -1, result);
    return result.toString();
  }

  /**
   * print out various types of objects
   * 
   * @param object
   * @param maxChars is the max chars that should be returned (abbreviate if longer), or -1 for any amount
   * @return the string value
   */
  public static String toStringForLog(Object object, int maxChars) {
    StringBuilder result = new StringBuilder();
    toStringForLogHelper(object, -1, result);
    String resultString = result.toString();
    if (maxChars != -1) {
      return StringUtils.abbreviate(resultString, maxChars);
    }
    return resultString;
  }

  /**
   * If batching this is the number of batches
   * @param count is size of set
   * @param batchSize
   * @return the number of batches
   */
  public static int batchNumberOfBatches(int count, int batchSize) {
    int batches = 1 + ((count - 1) / batchSize);
    return batches;

  }

  /**
   * If batching this is the number of batches
   * @param collection
   * @param batchSize
   * @return the number of batches
   */
  public static int batchNumberOfBatches(Collection<?> collection, int batchSize) {
    int arrraySize = length(collection);
    return batchNumberOfBatches(arrraySize, batchSize);

  }

  /**
   * retrieve a batch by 0 index. Will return an array of size batchSize or
   * the remainder. the array will be full of elements. Note, this requires an
   * ordered input (so use linkedhashset not hashset if doing sets)
   * @param <T> template type
   * @param collection
   * @param batchSize
   * @param batchIndex
   * @return the list
   *         This never returns null, only empty list
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> batchList(Collection<T> collection, int batchSize,
      int batchIndex) {

    int numberOfBatches = batchNumberOfBatches(collection, batchSize);
    int arraySize = length(collection);

    // short circuit
    if (arraySize == 0) {
      return new ArrayList<T>();
    }

    List<T> theBatchObjects = new ArrayList<T>();

    // lets get the type of the first element if possible
//    Object first = get(arrayOrCollection, 0);
//
//    Class theType = first == null ? Object.class : first.getClass();

    // if last batch
    if (batchIndex == numberOfBatches - 1) {

      // needs to work to 1-n
      //int thisBatchSize = 1 + ((arraySize - 1) % batchSize);

      int collectionIndex = 0;
      for (T t : collection) {
        if (collectionIndex++ < batchIndex * batchSize) {
          continue;
        }
        //just copy the rest
        //if (collectionIndex >= (batchIndex * batchSize) + arraySize) {
        //  break;
        //}
        //we are in the copy mode
        theBatchObjects.add(t);
      }

    } else {
      // if non-last batch
      //int newIndex = 0;
      int collectionIndex = 0;
      for (T t : collection) {
        if (collectionIndex < batchIndex * batchSize) {
          collectionIndex++;
          continue;
        }
        //done with batch
        if (collectionIndex >= (batchIndex + 1) * batchSize) {
          break;
        }
        theBatchObjects.add(t);
        collectionIndex++;
      }
    }
    return theBatchObjects;
  }
  /**
   * split a string based on a separator into an array, and trim each entry (see
   * the Commons Util StringUtils.trim() for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * 
   * @return the array of items after split and trimmed, or null if input is null.  will be trimmed to empty
   */
  public static String[] splitTrim(String input, String separator) {
    return splitTrim(input, separator, true);
  }

  /**
   * split a string based on a separator into an array, and trim each entry (see
   * the Commons Util StringUtils.trim() for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * @param treatAdjacentSeparatorsAsOne
   * @return the array of items after split and trimmed, or null if input is null.  will be trimmed to empty
   */
  public static String[] splitTrim(String input, String separator, boolean treatAdjacentSeparatorsAsOne) {
    if (StringUtils.isBlank(input)) {
      return null;
    }

    //first split
    String[] items = treatAdjacentSeparatorsAsOne ? StringUtils.split(input, separator) : 
      StringUtils.splitPreserveAllTokens(input, separator);

    //then trim
    for (int i = 0; (items != null) && (i < items.length); i++) {
      items[i] = StringUtils.trim(items[i]);
    }

    //return the array
    return items;
  }

  /**
   * escape url chars (e.g. a # is %23)
   * @param string input
   * @return the encoded string
   */
  public static String escapeUrlEncode(String string) {
    String result = null;
    try {
      result = URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }
  
  /**
   * unescape url chars (e.g. a space is %20)
   * @param string input
   * @return the encoded string
   */
  public static String escapeUrlDecode(String string) {
    String result = null;
    try {
      result = URLDecoder.decode(string, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("UTF-8 not supported", ex);
    }
    return result;
  }

  /**
   * make sure a list is non null.  If null, then return an empty list
   * @param <T>
   * @param list
   * @return the list or empty list if null
   */
  public static <T> List<T> nonNull(List<T> list) {
    return list == null ? new ArrayList<T>() : list;
  }
  
  /**
   * make sure a list is non null.  If null, then return an empty set
   * @param <T>
   * @param set
   * @return the set or empty set if null
   */
  public static <T> Set<T> nonNull(Set<T> set) {
    return set == null ? new HashSet<T>() : set;
  }
  
  /**
   * make sure it is non null, if null, then give new map
   * 
   * @param <K> key of map
   * @param <V> value of map
   * @param map is map
   * @return set non null
   */
  public static <K,V> Map<K,V> nonNull(Map<K,V> map) {
    return map == null ? new HashMap<K,V>() : map;
  }

  /**
   * return a list of objects from varargs.  Though if there is one
   * object, and it is a list, return it.
   * 
   * @param <T>
   *            template type of the objects
   * @param objects
   * @return the list or null if objects is null
   */
  public static <T> List<T> toList(T... objects) {
    if (objects == null) {
      return null;
    }
    if (objects.length == 1 && objects[0] instanceof List) {
      return (List<T>)objects[0];
    }
    
    List<T> result = new ArrayList<T>();
    for (T object : objects) {
      result.add(object);
    }
    return result;
  }

  /**
   * convert classes to a list
   * @param classes
   * @return list of classes
   */
  public static List<Class<?>> toListClasses(Class<?>... classes) {
    return toList(classes);
  }
  

  
  /**
   * return a set of objects from varargs.
   * 
   * @param <T> template type of the objects
   * @param objects
   * @return the set
   */
  public static <T> Set<T> toSet(T... objects) {

    Set<T> result = new LinkedHashSet<T>();
    for (T object : objects) {
      result.add(object);
    }
    return result;
  }

  /**
   * cache separator
   */
  private static final String CACHE_SEPARATOR = "__";

  /**
   * string format of dates
   */
  public static final String DATE_FORMAT = "yyyyMMdd";

  /**
   * format including minutes and seconds: yyyy/MM/dd HH:mm:ss
   */
  public static final String DATE_MINUTES_SECONDS_FORMAT = "yyyy/MM/dd HH:mm:ss";

  /**
   * format including minutes and seconds: yyyyMMdd HH:mm:ss
   */
  public static final String DATE_MINUTES_SECONDS_NO_SLASH_FORMAT = "yyyyMMdd HH:mm:ss";

  /**
   * format on screen of config for milestone: yyyy/MM/dd HH:mm:ss.SSS
   */
  public static final String TIMESTAMP_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /**
   * format on screen of config for milestone: yyyyMMdd HH:mm:ss.SSS
   */
  public static final String TIMESTAMP_NO_SLASH_FORMAT = "yyyyMMdd HH:mm:ss.SSS";

  /**
   * date format, make sure to synchronize
   */
  final static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

  /**
   * synchronize code that uses this standard formatter for dates with minutes and seconds
   */
  final static SimpleDateFormat dateMinutesSecondsFormat = new SimpleDateFormat(
      DATE_MINUTES_SECONDS_FORMAT);

  /**
   * synchronize code that uses this standard formatter for dates with minutes and seconds
   */
  final static SimpleDateFormat dateMinutesSecondsNoSlashFormat = new SimpleDateFormat(
      DATE_MINUTES_SECONDS_NO_SLASH_FORMAT);

  /**
   * <pre> format: yyyy/MM/dd HH:mm:ss.SSS synchronize code that uses this standard formatter for timestamps </pre>
   */
  final static SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

  /**
   * synchronize code that uses this standard formatter for timestamps
   */
  final static SimpleDateFormat timestampNoSlashFormat = new SimpleDateFormat(
      TIMESTAMP_NO_SLASH_FORMAT);

  /**
   * If false, throw an assertException, and give a reason
   * 
   * @param isTrue
   * @param reason
   */
  public static void assertion(boolean isTrue, String reason) {
    if (!isTrue) {
      throw new RuntimeException(reason);
    }

  }

  /**
   * use the field cache, expire every day (just to be sure no leaks)
   */
  private static GrouperCache<String, Set<Field>> fieldSetCache = null;
  
  /**
   * lazy load
   * @return field set cache
   */
  private static GrouperCache<String, Set<Field>> fieldSetCache() {
    if (fieldSetCache == null) {
      fieldSetCache = new GrouperCache<String, Set<Field>>("edu.internet2.middleware.grouper.util.GrouperUtil.fieldSetCache",
          2000, false, 0, 60*60*24, false);
    }
    return fieldSetCache;
  }
    

  /**
   * make a cache with max size to cache declared methods
   */
  private static GrouperCache<Class, Method[]> declaredMethodsCache = null;
  
  /**
   * lazy load
   * @return declared method cache
   */
  private static GrouperCache<Class, Method[]> declaredMethodsCache() {
    if (declaredMethodsCache == null) {
      declaredMethodsCache = new GrouperCache<Class, Method[]>("edu.internet2.middleware.grouper.util.GrouperUtil.declaredMethodsCache",
          2000, false, 0, 60*60*24, false);
    }
    return declaredMethodsCache;
  }
  
    

  /**
   * use the field cache, expire every day (just to be sure no leaks) 
   */
  private static GrouperCache<String, Set<Method>> getterSetCache = null;
    

  /**
   * lazy load
   * @return getter cache
   */
  private static GrouperCache<String, Set<Method>> getterSetCache() {
    if (getterSetCache == null) {
      getterSetCache = new GrouperCache<String, Set<Method>>("edu.internet2.middleware.grouper.util.GrouperUtil.getterSetCache",
          2000, false, 0, 60*60*24, false);
    }
    return getterSetCache;
  }
  
    

  /**
   * use the field cache, expire every day (just to be sure no leaks) 
   */
  private static GrouperCache<String, Set<Method>> setterSetCache = null;
    

  /**
   * lazy load
   * @return setter cache
   */
  private static GrouperCache<String, Set<Method>> setterSetCache() {
    if (setterSetCache == null) {
      setterSetCache = new GrouperCache<String, Set<Method>>("edu.internet2.middleware.grouper.util.GrouperUtil.setterSetCache",
          2000, false, 0, 60*60*24, false);
    }
    return setterSetCache;
  }
  
  
  /**
   * Field lastId.
   */
  private static char[] lastId = convertLongToStringSmall(new Date().getTime())
      .toCharArray();

  /**
   * cache the properties read from resource 
   */
  private static Map<String, Properties> resourcePropertiesCache = new HashMap<String, Properties>();

  /**
   * assign data to a field
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param dataToAssign
   *            data
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @param typeCast
   *            true if we should typecast
   * @param annotationWithValueOverride
   *            annotation with value of override
   */
  public static void assignField(Class theClass, Object invokeOn,
      String fieldName, Object dataToAssign, boolean callOnSupers,
      boolean overrideSecurity, boolean typeCast,
      Class<? extends Annotation> annotationWithValueOverride) {
    if (theClass == null && invokeOn != null) {
      theClass = invokeOn.getClass();
    }
    Field field = field(theClass, fieldName, callOnSupers, true);
    assignField(field, invokeOn, dataToAssign, overrideSecurity, typeCast,
        annotationWithValueOverride);
  }

  /**
   * assign data to a field. Will find the field in superclasses, will
   * typecast, and will override security (private, protected, etc)
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param dataToAssign
   *            data
   * @param annotationWithValueOverride
   *            annotation with value of override
   */
  public static void assignField(Class theClass, Object invokeOn,
      String fieldName, Object dataToAssign,
      Class<? extends Annotation> annotationWithValueOverride) {
    assignField(theClass, invokeOn, fieldName, dataToAssign, true, true,
        true, annotationWithValueOverride);
  }

  /**
   * assign data to a field
   * 
   * @param field
   *            is the field to assign to
   * @param invokeOn
   *            to call on or null for static
   * @param dataToAssign
   *            data
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @param typeCast
   *            true if we should typecast
   */
  public static void assignField(Field field, Object invokeOn,
      Object dataToAssign, boolean overrideSecurity, boolean typeCast) {

    try {
      Class fieldType = field.getType();
      // typecast
      if (typeCast) {
        dataToAssign = 
                 typeCast(dataToAssign, fieldType,
                 true, true);
      }
      if (overrideSecurity) {
        field.setAccessible(true);
      }
      field.set(invokeOn, dataToAssign);
    } catch (Exception e) {
      throw new RuntimeException("Cant assign reflection field: "
          + (field == null ? null : field.getName()) + ", on: "
          + className(invokeOn) + ", with args: "
          + classNameCollection(dataToAssign), e);
    }
  }

  /**
   * null safe iterator getter if the type if collection
   * 
   * @param collection
   * @return the iterator
   */
  public static Iterator iterator(Object collection) {
    if (collection == null) {
      return null;
    }
    // array list doesnt need an iterator
    if (collection instanceof Collection
        && !(collection instanceof ArrayList)) {
      return ((Collection) collection).iterator();
    }
    return null;
  }

  /**
   * Null safe array length or map
   * 
   * @param arrayOrCollection
   * @return the length of the array (0 for null)
   */
  public static int length(Object arrayOrCollection) {
    if (arrayOrCollection == null) {
      return 0;
    }
    if (arrayOrCollection.getClass().isArray()) {
      return Array.getLength(arrayOrCollection);
    }
    if (arrayOrCollection instanceof Collection) {
      return ((Collection) arrayOrCollection).size();
    }
    if (arrayOrCollection instanceof Map) {
      return ((Map) arrayOrCollection).size();
    }
    // simple non array non collection object
    return 1;
  }

  /**
   * If array, get the element based on index, if Collection, get it based on
   * iterator.
   * 
   * @param arrayOrCollection
   * @param iterator
   * @param index
   * @return the object
   */
  public static Object next(Object arrayOrCollection, Iterator iterator,
      int index) {
    if (arrayOrCollection.getClass().isArray()) {
      return Array.get(arrayOrCollection, index);
    }
    if (arrayOrCollection instanceof ArrayList) {
      return ((ArrayList) arrayOrCollection).get(index);
    }
    if (arrayOrCollection instanceof Collection) {
      return iterator.next();
    }
    // simple object
    if (0 == index) {
      return arrayOrCollection;
    }
    throw new RuntimeException("Invalid class type: "
        + arrayOrCollection.getClass().getName());
  }

  /**
   * Remove the iterator or index
   * 
   * @param arrayOrCollection
   * @param index
   * @return the object list or new array
   */
  public static Object remove(Object arrayOrCollection, 
      int index) {
    return remove(arrayOrCollection, null, index);
  }
  
  /**
   * Remove the iterator or index
   * 
   * @param arrayOrCollection
   * @param iterator
   * @param index
   * @return the object list or new array
   */
  public static Object remove(Object arrayOrCollection, Iterator iterator,
      int index) {
    
    //if theres an iterator, just use that
    if (iterator != null) {
      iterator.remove();
      return arrayOrCollection;
    }
    if (arrayOrCollection.getClass().isArray()) {
      int newLength = Array.getLength(arrayOrCollection) - 1;
      Object newArray = Array.newInstance(arrayOrCollection.getClass().getComponentType(), newLength);
      if (newLength == 0) {
        return newArray;
      }
      if (index > 0) {
        System.arraycopy(arrayOrCollection, 0, newArray, 0, index);
      }
      if (index < newLength) {
        System.arraycopy(arrayOrCollection, index+1, newArray, index, newLength - index);
      }
      return newArray;
    }
    if (arrayOrCollection instanceof List) {
      ((List)arrayOrCollection).remove(index);
      return arrayOrCollection;
    } else if (arrayOrCollection instanceof Collection) {
      //this should work unless there are duplicates or something weird
      ((Collection)arrayOrCollection).remove(get(arrayOrCollection, index));
      return arrayOrCollection;
    }
    throw new RuntimeException("Invalid class type: "
        + arrayOrCollection.getClass().getName());
  }

  /**
   * print the simple names of a list of classes
   * @param object
   * @return the simple names
   */
  public static String classesString(Object object) {
    StringBuilder result = new StringBuilder();
    if (object.getClass().isArray()) {
      int length = Array.getLength(object);
      for (int i=0;i<length;i++) {
        result.append(((Class)object).getSimpleName());
        if (i < length-1) {
          result.append(", ");
        }
      }
      return result.toString();
    }
    
    throw new RuntimeException("Not implemented: " + className(object));
  }
  
  /**
   * null safe classname method, max out at 20
   * 
   * @param object
   * @return the classname
   */
  public static String classNameCollection(Object object) {
    if (object == null) {
      return null;
    }
    StringBuffer result = new StringBuffer();
    
    Iterator iterator = iterator(object);
    int length = length(object);
    for (int i = 0; i < length && i < 20; i++) {
      result.append(className(next(object, iterator, i)));
      if (i != length - 1) {
        result.append(", ");
      }
    }
    return result.toString();
  }

  /**
   * null safe classname method, gets the unenhanced name
   * 
   * @param object
   * @return the classname
   */
  public static String className(Object object) {
    return object == null ? null : unenhanceClass(object.getClass())
        .getName();
  }

  /**
   * if a class is enhanced, get the unenhanced version
   * 
   * @param theClass
   * @return the unenhanced version
   */
  public static Class unenhanceClass(Class theClass) {
    try {
      while (Enhancer.isEnhanced(theClass)) {
        theClass = theClass.getSuperclass();
      }
      return theClass;
    } catch (Exception e) {
      throw new RuntimeException("Problem unenhancing " + theClass, e);
    }
  }

  /**
   * assign data to a field
   * 
   * @param field
   *            is the field to assign to
   * @param invokeOn
   *            to call on or null for static
   * @param dataToAssign
   *            data
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @param typeCast
   *            true if we should typecast
   * @param annotationWithValueOverride
   *            annotation with value of override, or null if none
   */
  @SuppressWarnings("unchecked")
  public static void assignField(Field field, Object invokeOn,
      Object dataToAssign, boolean overrideSecurity, boolean typeCast,
      Class<? extends Annotation> annotationWithValueOverride) {

    if (annotationWithValueOverride != null) {
      // see if in annotation
      Annotation annotation = field
          .getAnnotation(annotationWithValueOverride);
      if (annotation != null) {
        
         // type of the value, or String if not specific Class
          // typeOfAnnotationValue = typeCast ? field.getType() :
          // String.class; dataToAssign =
          // AnnotationUtils.retrieveAnnotationValue(
          // typeOfAnnotationValue, annotation, "value");
        
        throw new RuntimeException("Not supported");
      }
    }
    assignField(field, invokeOn, dataToAssign, overrideSecurity, typeCast);
  }

  /**
   * assign data to a field. Will find the field in superclasses, will
   * typecast, and will override security (private, protected, etc)
   * 
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param dataToAssign
   *            data
   */
  public static void assignField(Object invokeOn, String fieldName,
      Object dataToAssign) {
    assignField(null, invokeOn, fieldName, dataToAssign, true, true, true,
        null);
  }

  /**
   * get a field object for a class, potentially in superclasses
   * 
   * @param theClass
   * @param fieldName
   * @param callOnSupers
   *            true if superclasses should be looked in for the field
   * @param throwExceptionIfNotFound
   *            will throw runtime exception if not found
   * @return the field object or null if not found (or exception if param is
   *         set)
   */
  public static Field field(Class theClass, String fieldName,
      boolean callOnSupers, boolean throwExceptionIfNotFound) {
    try {
      Field field = theClass.getDeclaredField(fieldName);
      // found it
      return field;
    } catch (NoSuchFieldException e) {
      // if method not found
      // if traversing up, and not Object, and not instance method
      if (callOnSupers && !theClass.equals(Object.class)) {
        return field(theClass.getSuperclass(), fieldName, callOnSupers,
            throwExceptionIfNotFound);
      }
    }
    // maybe throw an exception
    if (throwExceptionIfNotFound) {
      throw new RuntimeException("Cant find field: " + fieldName
          + ", in: " + theClass + ", callOnSupers: " + callOnSupers);
    }
    return null;
  }

  /**
   * return a set of Strings for a class and type. This is not for any
   * supertypes, only for the type at hand. includes final fields
   * 
   * @param theClass
   * @param fieldType
   *            or null for all
   * @param includeStaticFields
   * @return the set of strings, or the empty Set if none
   */
  @SuppressWarnings("unchecked")
  public static Set<String> fieldNames(Class theClass, Class fieldType,
      boolean includeStaticFields) {
    return fieldNamesHelper(theClass, theClass, fieldType, true, true,
        includeStaticFields, null, true);
  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @return the set of field names or empty set if none
   */
  public static Set<String> fieldNames(Class theClass,
      Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, boolean includeStaticFields,
      boolean includeFinalFields) {
    return fieldNamesHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, null, true);

  }

  /**
   * get all field names from a class, including superclasses (if specified).
   * ignore a certain marker annotation
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotationToIngore
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list
   * @return the set of field names
   */
  public static Set<String> fieldNames(Class theClass,
      Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, boolean includeStaticFields,
      boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotationToIngore) {
    return fieldNamesHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotationToIngore, false);

  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * (up to and including the specified superclass). ignore a certain marker
   * annotation. Dont get static or final field, and get fields of all types
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param markerAnnotationToIngore
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list
   * @return the set of field names or empty set if none
   */
  public static Set<String> fieldNames(Class theClass,
      Class superclassToStopAt,
      Class<? extends Annotation> markerAnnotationToIngore) {
    return fieldNamesHelper(theClass, superclassToStopAt, null, true,
        false, false, markerAnnotationToIngore, false);
  }

  /**
   * get all field names from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            true to include finals
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of field names or empty set if none
   */
  @SuppressWarnings("unchecked")
  static Set<String> fieldNamesHelper(Class theClass,
      Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, boolean includeStaticFields,
      boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    Set<Field> fieldSet = fieldsHelper(theClass, superclassToStopAt,
        fieldType, includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, includeAnnotation);
    Set<String> fieldNameSet = new LinkedHashSet<String>();
    for (Field field : fieldSet) {
      fieldNameSet.add(field.getName());
    }
    return fieldNameSet;

  }

  /**
   * get all fields from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  public static Set<Field> fields(Class theClass, Class superclassToStopAt,
      Class fieldType, boolean includeSuperclassToStopAt,
      boolean includeStaticFields, boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    return fieldsHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, includeAnnotation);
  }

  /**
   * get all fields from a class, including superclasses (if specified) (up to
   * and including the specified superclass). ignore a certain marker
   * annotation, or only include it. Dont get static or final field, and get
   * fields of all types
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of field names or empty set if none
   */
  @SuppressWarnings("unchecked")
  public static Set<Field> fields(Class theClass, Class superclassToStopAt,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    return fieldsHelper(theClass, superclassToStopAt, null, true, false,
        false, markerAnnotation, includeAnnotation);
  }

  /**
   * get all fields from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list (if includeAnnotation is false)
   * @param includeAnnotation
   *            true if the attribute should be included if annotation is
   *            present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  static Set<Field> fieldsHelper(Class theClass, Class superclassToStopAt,
      Class<?> fieldType, boolean includeSuperclassToStopAt,
      boolean includeStaticFields, boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation,
      boolean includeAnnotation) {
    // MAKE SURE IF ANY MORE PARAMS ARE ADDED, THE CACHE KEY IS CHANGED!

    Set<Field> fieldNameSet = null;
    String cacheKey = theClass + CACHE_SEPARATOR + superclassToStopAt
        + CACHE_SEPARATOR + fieldType + CACHE_SEPARATOR
        + includeSuperclassToStopAt + CACHE_SEPARATOR
        + includeStaticFields + CACHE_SEPARATOR + includeFinalFields
        + CACHE_SEPARATOR + markerAnnotation + CACHE_SEPARATOR
        + includeAnnotation;
    fieldNameSet = fieldSetCache().get(cacheKey);
    if (fieldNameSet != null) {
      return fieldNameSet;
    }

    fieldNameSet = new LinkedHashSet<Field>();
    fieldsHelper(theClass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, fieldNameSet,
        includeAnnotation);

    // add to cache
    fieldSetCache().put(cacheKey, fieldNameSet);

    return fieldNameSet;

  }

  /**
   * compare two objects, compare primitives, Strings, maps of string attributes.
   * if both objects equal each others references, then return empty set.
   * then, if not, then if either is null, return all fields
   * @param first
   * @param second
   * @param fieldsToCompare
   * @param mapPrefix is the prefix for maps which are compared (e.g. attribute__)
   * @return the set of fields which are different.  never returns null
   */
  public static Set<String> compareObjectFields(Object first, Object second, 
      Set<String> fieldsToCompare, String mapPrefix) {
    
    Set<String> differentFields = new LinkedHashSet<String>();
    
    if (first == second) {
      return differentFields;
    }
    
    //if either null, then all fields are different
    if (first == null || second == null) {
      differentFields.addAll(fieldsToCompare);
    }

    for (String fieldName : fieldsToCompare) {
      try {
        Object firstValue = fieldValue(first, fieldName);
        Object secondValue = fieldValue(second, fieldName);
        
        if (firstValue == secondValue) {
          continue;
        }
        if (firstValue instanceof Map || secondValue instanceof Map) {
          mapDifferences((Map)firstValue, (Map)secondValue, differentFields, mapPrefix);
          continue;
        }
        //compare things...
        //for strings, null is equal to empty
        if (firstValue instanceof String || secondValue instanceof String) {
          if (!StringUtils.equals(StringUtils.defaultString((String)firstValue),
              StringUtils.defaultString((String)secondValue))) {
            differentFields.add(fieldName);
          }
          continue;
        }
        //if one is null, that is not good
        if (firstValue == null || secondValue == null) {
          differentFields.add(fieldName);
          continue;
        }
        //everything (numbers, dates, etc) should work with equals method...
        if (!firstValue.equals(secondValue)) {
          differentFields.add(fieldName);
          continue;
        }
        
      } catch (RuntimeException re) {
        throw new RuntimeException("Problem comparing field " + fieldName 
            + " on objects: " + className(first) + ", " + className(second));
      }
      
      
    }
    return differentFields;
  }
  
  /**
   * clone an object, assign primitives, Strings, maps of string attributes.  Clone GrouperCloneable fields.
   * @param <T> template
   * @param object
   * @param fieldsToClone
   * @return the cloned object or null if input is null
   */
  public static <T> T clone(T object, Set<String> fieldsToClone) {
    
    //make a return object
    T result = (T)GrouperUtil.newInstance(object.getClass());
    
    cloneFields(object, result, fieldsToClone);
    
    return result;
  }
  
  /**
   * clone an object, assign primitives, Strings, maps of string attributes.  Clone GrouperCloneable fields.
   * @param <T> template
   * @param object
   * @param result 
   * @param fieldsToClone
   */
  public static <T> void cloneFields(T object, T result,
      Set<String> fieldsToClone) {
    
    if (object == result) {
      return;
    }
    
    //if either null, then all fields are different
    if (object == null || result == null) {
      throw new RuntimeException("Cant copy from or to null: " + className(object) + ", " + className(result));
    }
    
    Class<?> fieldValueClass = null;
    
    for (String fieldName : GrouperUtil.nonNull(fieldsToClone)) {
      try {
        
        Object fieldValue = fieldValue(object, fieldName);
        fieldValueClass = fieldValue == null ? null : fieldValue.getClass();
        
        Object fieldValueToAssign = cloneValue(fieldValue);
        
        //assign the field to the clone
        GrouperUtil.assignField(result, fieldName, fieldValueToAssign);
        
      } catch (RuntimeException re) {
        throw new RuntimeException("Problem cloning field: " + object.getClass() 
              + ", " + fieldName + ", " + fieldValueClass, re);
      }
    }
  }
  
  /**
   * helper method to clone the value of a field.  just returns the same
   * reference for primitives and immutables.  Will subclone GrouperCloneables, 
   * and will throw exception if not expecting the type.  Will clone sets, lists, maps.
   * @param <T> template
   * 
   * @param value
   * @return the cloned value
   */
  public static <T> T cloneValue(T value) {

    Object clonedValue = value;
    
    if (value == null || value instanceof String 
        || value.getClass().isPrimitive() || value instanceof Number
        || value instanceof Boolean
        || value instanceof Date || value instanceof Configuration
        || value instanceof Subject) {
      //clone things
      //for strings, and immutable classes, just assign
      //nothing to do, just assign the value
    } else if (value instanceof GrouperCloneable) {
      
      //lets clone the object
      clonedValue = ((GrouperCloneable)value).clone();
      
    } else if (value instanceof Map) {
      clonedValue = new LinkedHashMap();
      Map mapValue = (Map)value;
      Map clonedMapValue = (Map)clonedValue;
      for (Object key : mapValue.keySet()) {
        clonedMapValue.put(cloneValue(key), cloneValue(mapValue.get(key)));
      }
    } else if (value instanceof Set) {
        clonedValue = new LinkedHashSet();
        Set setValue = (Set)value;
        Set clonedSetValue = (Set)clonedValue;
        for (Object each : setValue) {
          clonedSetValue.add(cloneValue(each));
        }
    } else if (value instanceof List) {
      clonedValue = new ArrayList();
      List listValue = (List)value;
      List clonedListValue = (List)clonedValue;
      for (Object each : listValue) {
        clonedListValue.add(cloneValue(each));
      }
    } else if (value.getClass().isArray()) {
      clonedValue = Array.newInstance(value.getClass().getComponentType(), Array.getLength(value));
      for (int i=0;i<Array.getLength(value);i++) {
        Array.set(clonedValue, i, cloneValue(Array.get(value, i)));
      }
      
      
    } else {

      //this means lets add support for a new type of object
      throw new RuntimeException("Unexpected class in clone method: " + value.getClass());
    
    }
    return (T)clonedValue;
  }
  
  /**
   * simple method to get method names
   * @param theClass
   * @param superclassToStopAt 
   * @param includeSuperclassToStopAt 
   * @param includeStaticMethods 
   * @return the set of method names
   */
  public static Set<String> methodNames(Class<?> theClass, Class<?> superclassToStopAt, 
      boolean includeSuperclassToStopAt, boolean includeStaticMethods) {

    Set<Method> methods = new LinkedHashSet<Method>();
    methodsHelper(theClass, superclassToStopAt, includeSuperclassToStopAt, includeStaticMethods, 
        null, false, methods);
    Set<String> methodNames = new HashSet<String>();
    for (Method method : methods) {
      methodNames.add(method.getName());
    }
    return methodNames;
  }

  /**
   * get the set of methods
   * @param theClass
   * @param superclassToStopAt 
   * @param includeSuperclassToStopAt 
   * @param includeStaticMethods
   * @param markerAnnotation 
   * @param includeAnnotation 
   * @param methodSet
   */
  public static void methodsHelper(Class<?> theClass, Class<?> superclassToStopAt, 
      boolean includeSuperclassToStopAt,
      boolean includeStaticMethods, Class<? extends Annotation> markerAnnotation, 
      boolean includeAnnotation, Set<Method> methodSet) {
    theClass = unenhanceClass(theClass);
    Method[] methods = theClass.getDeclaredMethods();
    if (length(methods) != 0) {
      for (Method method : methods) {
        // if not static, then continue
        if (!includeStaticMethods
            && Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        // if checking for annotation
        if (markerAnnotation != null
            && (includeAnnotation != method
                .isAnnotationPresent(markerAnnotation))) {
          continue;
        }
        // go for it
        methodSet.add(method);
      }
    }
    // see if done recursing (if superclassToStopAt is null, then stop at
    // Object
    if (theClass.equals(superclassToStopAt)
        || theClass.equals(Object.class)) {
      return;
    }
    Class superclass = theClass.getSuperclass();
    if (!includeSuperclassToStopAt && superclass.equals(superclassToStopAt)) {
      return;
    }
    // recurse
    methodsHelper(superclass, superclassToStopAt,
        includeSuperclassToStopAt, includeStaticMethods,
        markerAnnotation, includeAnnotation, methodSet);
    
  }
  
  /**
   * get the set of methods
   * @param theClass
   * @param methodName 
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param superclassToStopAt 
   * @param includeSuperclassToStopAt 
   * @param isStaticOrInstance true if static
   * @param markerAnnotation 
   * @param includeAnnotation 
   * @return the method or null if not found
   *            
   */
  public static Method method(Class<?> theClass, 
      String methodName, Object paramTypesOrArrayOrList,
      Class<?> superclassToStopAt, 
      boolean includeSuperclassToStopAt,
      boolean isStaticOrInstance, Class<? extends Annotation> markerAnnotation, 
      boolean includeAnnotation) {
    theClass = unenhanceClass(theClass);

    Class[] paramTypesArray = (Class[]) toArray(paramTypesOrArrayOrList);

    Method method = null;
    
    try {
      method = theClass.getDeclaredMethod(methodName, paramTypesArray);
    } catch (NoSuchMethodException nsme) {
      //this is ok
    } catch (Exception e) {
      throw new RuntimeException("Problem retrieving method: " + theClass.getSimpleName() + ", " + methodName, e);
    }
    
    if (method != null) {
      //we found a method, make sure it is valid
      // if not static, then return null (dont worry about superclass)
      if (!isStaticOrInstance
          && Modifier.isStatic(method.getModifiers())) {
        return null;
      }
      // if checking for annotation, if not there, then recurse
      if (markerAnnotation == null
          || (includeAnnotation == method
              .isAnnotationPresent(markerAnnotation))) {
        return method;
      }
    }
    // see if done recursing (if superclassToStopAt is null, then stop at
    // Object
    if (theClass.equals(superclassToStopAt)
        || theClass.equals(Object.class)) {
      return null;
    }
    Class superclass = theClass.getSuperclass();
    if (!includeSuperclassToStopAt && superclass.equals(superclassToStopAt)) {
      return null;
    }
    // recurse
    return method(superclass, methodName, paramTypesArray, superclassToStopAt,
        includeSuperclassToStopAt, isStaticOrInstance, markerAnnotation, includeAnnotation);
  }
  
  /**
   * get all field names from a class, including superclasses (if specified)
   * 
   * @param theClass
   *            to look for fields in
   * @param superclassToStopAt
   *            to go up to or null to go up to Object
   * @param fieldType
   *            is the type of the field to get
   * @param includeSuperclassToStopAt
   *            if we should include the superclass
   * @param includeStaticFields
   *            if include static fields
   * @param includeFinalFields
   *            if final fields should be included
   * @param markerAnnotation
   *            if this is not null, then if the field has this annotation,
   *            then do not include in list
   * @param fieldSet
   *            set to add fields to
   * @param includeAnnotation
   *            if include or exclude
   */
  @SuppressWarnings("unchecked")
  private static void fieldsHelper(Class theClass, Class superclassToStopAt,
      Class<?> fieldType, boolean includeSuperclassToStopAt,
      boolean includeStaticFields, boolean includeFinalFields,
      Class<? extends Annotation> markerAnnotation, Set<Field> fieldSet,
      boolean includeAnnotation) {
    theClass = unenhanceClass(theClass);
    Field[] fields = theClass.getDeclaredFields();
    if (length(fields) != 0) {
      for (Field field : fields) {
        // if checking for type, and not right type, continue
        if (fieldType != null
            && !fieldType.isAssignableFrom(field.getType())) {
          continue;
        }
        // if not static, then continue
        if (!includeStaticFields
            && Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        // if not final constinue
        if (!includeFinalFields
            && Modifier.isFinal(field.getModifiers())) {
          continue;
        }
        // if checking for annotation
        if (markerAnnotation != null
            && (includeAnnotation != field
                .isAnnotationPresent(markerAnnotation))) {
          continue;
        }
        // go for it
        fieldSet.add(field);
      }
    }
    // see if done recursing (if superclassToStopAt is null, then stop at
    // Object
    if (theClass.equals(superclassToStopAt)
        || theClass.equals(Object.class)) {
      return;
    }
    Class superclass = theClass.getSuperclass();
    if (!includeSuperclassToStopAt && superclass.equals(superclassToStopAt)) {
      return;
    }
    // recurse
    fieldsHelper(superclass, superclassToStopAt, fieldType,
        includeSuperclassToStopAt, includeStaticFields,
        includeFinalFields, markerAnnotation, fieldSet,
        includeAnnotation);
  }

  /**
   * find out a field value
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @return the current value
   */
  public static Object fieldValue(Class theClass, Object invokeOn,
      String fieldName, boolean callOnSupers, boolean overrideSecurity) {
    Field field = null;

    // only if the method exists, try to execute
    try {
      // ok if null
      if (theClass == null) {
        theClass = invokeOn.getClass();
      }
      field = field(theClass, fieldName, callOnSupers, true);
      return fieldValue(field, invokeOn, overrideSecurity);
    } catch (Exception e) {
      throw new RuntimeException("Cant execute reflection field: "
          + fieldName + ", on: " + className(invokeOn), e);
    }
  }

  /**
   * get the value of a field, override security if needbe
   * 
   * @param field
   * @param invokeOn
   * @return the value of the field
   */
  public static Object fieldValue(Field field, Object invokeOn) {
    return fieldValue(field, invokeOn, true);
  }

  /**
   * get the value of a field
   * 
   * @param field
   * @param invokeOn
   * @param overrideSecurity
   * @return the value of the field
   */
  public static Object fieldValue(Field field, Object invokeOn,
      boolean overrideSecurity) {
    if (overrideSecurity) {
      field.setAccessible(true);
    }
    try {
      return field.get(invokeOn);
    } catch (Exception e) {
      throw new RuntimeException("Cant execute reflection field: "
          + field.getName() + ", on: " + className(invokeOn), e);

    }

  }

  /**
   * find out a field value (invoke on supers, override security)
   * 
   * @param invokeOn
   *            to call on or null for static
   * @param fieldName
   *            method name to call
   * @return the current value
   */
  public static Object fieldValue(Object invokeOn, String fieldName) {
    return fieldValue(null, invokeOn, fieldName, true, true);
  }

  /**
   * get the decalred methods for a class, perhaps from cache
   * 
   * @param theClass
   * @return the declared methods
   */
  @SuppressWarnings("unused")
  private static Method[] retrieveDeclaredMethods(Class theClass) {
    Method[] methods = declaredMethodsCache().get(theClass);
    // get from cache if we can
    if (methods == null) {
      methods = theClass.getDeclaredMethods();
      declaredMethodsCache().put(theClass, methods);
    }
    return methods;
  }

  /**
   * helper method for calling a method with no params (could be in
   * superclass)
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName) {
    return callMethod(theClass, invokeOn, methodName, null, null);
  }

  /**
   * helper method for calling a method (could be in superclass)
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName, Object paramTypesOrArrayOrList,
      Object paramsOrListOrArray) {
    return callMethod(theClass, invokeOn, methodName,
        paramTypesOrArrayOrList, paramsOrListOrArray, true);
  }

  /**
   * helper method for calling a method
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName, Object paramTypesOrArrayOrList,
      Object paramsOrListOrArray, boolean callOnSupers) {
    return callMethod(theClass, invokeOn, methodName,
        paramTypesOrArrayOrList, paramsOrListOrArray, callOnSupers,
        false);
  }

  /**
   * construct an instance by reflection
   * @param <T>
   * @param theClass
   * @param args
   * @param types
   * @return the instance
   */
  public static <T> T construct(Class<T> theClass, Class[] types, Object[] args) {
    try {
      Constructor<T> constructor = theClass.getConstructor(types);
      
      return constructor.newInstance(args);
      
    } catch (Exception e) {
      throw new RuntimeException("Having trouble with constructor for class: " + theClass.getSimpleName()
          + " and args: " + classesString(types), e);
     }
  }
  
  /**
   * helper method for calling a method
   * 
   * @param theClass
   *            the class which has the method
   * @param invokeOn
   *            to call on or null for static
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @param callOnSupers
   *            if static and method not exists, try on supers
   * @param overrideSecurity
   *            true to call on protected or private etc methods
   * @return the data
   */
  public static Object callMethod(Class theClass, Object invokeOn,
      String methodName, Object paramTypesOrArrayOrList,
      Object paramsOrListOrArray, boolean callOnSupers,
      boolean overrideSecurity) {
    Method method = null;

    Class[] paramTypesArray = (Class[]) toArray(paramTypesOrArrayOrList);

    try {
      method = theClass.getDeclaredMethod(methodName, paramTypesArray);
      if (overrideSecurity) {
        method.setAccessible(true);
      }
    } catch (Exception e) {
      // if method not found
      if (e instanceof NoSuchMethodException) {
        // if traversing up, and not Object, and not instance method
        // CH 070425 not sure why invokeOn needs to be null, removing
        // this
        if (callOnSupers /* && invokeOn == null */
            && !theClass.equals(Object.class)) {
          return callMethod(theClass.getSuperclass(), invokeOn,
              methodName, paramTypesOrArrayOrList,
              paramsOrListOrArray, callOnSupers, overrideSecurity);
        }
      }
      throw new RuntimeException("Problem calling method " + methodName
          + " on " + theClass.getName(), e);
    }

    return invokeMethod(method, invokeOn, paramsOrListOrArray);

  }
  
  /** pass this in the invokeOn to signify no params */
  private static final Object NO_PARAMS = new Object();
  
  /**
   * Safely invoke a reflection method that takes no args
   * 
   * @param method
   *            to invoke
   * @param invokeOn
   * if GrouperUtil.NO_PARAMS then will not pass in params.
   * @return the result
   */
  public static Object invokeMethod(Method method, Object invokeOn) {
    return invokeMethod(method, invokeOn, NO_PARAMS);
  }

  /**
   * Safely invoke a reflection method
   * 
   * @param method
   *            to invoke
   * @param invokeOn
   * @param paramsOrListOrArray must be an arg.  If null, will pass null.
   * if GrouperUtil.NO_PARAMS then will not pass in params.
   * @return the result
   */
  public static Object invokeMethod(Method method, Object invokeOn,
      Object paramsOrListOrArray) {

    Object[] args = paramsOrListOrArray == NO_PARAMS ? null : (Object[]) toArray(paramsOrListOrArray);

    //we want to make sure things are accessible
    method.setAccessible(true);

    //only if the method exists, try to execute
    Object result = null;
    Exception e = null;
    try {
      result = method.invoke(invokeOn, args);
    } catch (IllegalAccessException iae) {
      e = iae;
    } catch (IllegalArgumentException iae) {
      e = iae;
    } catch (InvocationTargetException ite) {
      //this means the underlying call caused exception... its ok if runtime
      if (ite.getCause() instanceof RuntimeException) {
        throw (RuntimeException)ite.getCause();
      }
      //else throw as invocation target...
      e = ite;
    }
    if (e != null) {
      throw new RuntimeException("Cant execute reflection method: "
          + method.getName() + ", on: " + className(invokeOn)
          + ", with args: " + classNameCollection(args), e);
    }
    return result;
  }

  /**
   * Convert a list to an array with the type of the first element e.g. if it
   * is a list of Person objects, then the array is Person[]
   * 
   * @param objectOrArrayOrCollection
   *            is a list
   * @return the array of objects with type of the first element in the list
   */
  public static Object toArray(Object objectOrArrayOrCollection) {
    // do this before length since if array with null in it, we want ti get
    // it back
    if (objectOrArrayOrCollection != null
        && objectOrArrayOrCollection.getClass().isArray()) {
      return objectOrArrayOrCollection;
    }
    int length = length(objectOrArrayOrCollection);
    if (length == 0) {
      return null;
    }

    if (objectOrArrayOrCollection instanceof Collection) {
      Collection collection = (Collection) objectOrArrayOrCollection;
      Object first = collection.iterator().next();
      return toArray(collection, first == null ? Object.class : first
          .getClass());
    }
    // make an array of the type of object passed in, size one
    Object array = Array.newInstance(objectOrArrayOrCollection.getClass(),
        1);
    Array.set(array, 0, objectOrArrayOrCollection);
    return array;
  }

  /**
   * convert a list into an array of type of theClass
   * @param <T> is the type of the array
   * @param collection list to convert
   * @param theClass type of array to return
   * @return array of type theClass[] filled with the objects from list
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Collection collection, Class<T> theClass) {
    if (collection == null || collection.size() == 0) {
      return null;
    }

    return (T[])collection.toArray((Object[]) Array.newInstance(theClass,
        collection.size()));

  }

  /**
   * helper method for calling a static method up the stack. method takes no
   * args (could be in superclass)
   * 
   * @param theClass
   *            the class which has the method
   * @param methodName
   *            method name to call
   * @return the data
   */
  public static Object callMethod(Class theClass, String methodName) {
    return callMethod(theClass, null, methodName, null, null);
  }

  /**
   * helper method for calling a static method with no params
   * 
   * @param theClass
   *            the class which has the method
   * @param methodName
   *            method name to call
   * @param callOnSupers
   *            if we should try the super classes if not exists in this class
   * @return the data
   */
  public static Object callMethod(Class theClass, String methodName,
      boolean callOnSupers) {
    return callMethod(theClass, null, methodName, null, null, callOnSupers);
  }

  /**
   * helper method for calling a static method up the stack
   * 
   * @param theClass
   *            the class which has the method
   * @param methodName
   *            method name to call
   * @param paramTypesOrArrayOrList
   *            types of the params
   * @param paramsOrListOrArray
   *            data
   * @return the data
   */
  public static Object callMethod(Class theClass, String methodName,
      Object paramTypesOrArrayOrList, Object paramsOrListOrArray) {
    return callMethod(theClass, null, methodName, paramTypesOrArrayOrList,
        paramsOrListOrArray);
  }

  /**
   * helper method for calling a method with no params (could be in
   * superclass), will override security
   * 
   * @param invokeOn
   *            instance to invoke on
   * @param methodName
   *            method name to call not exists in this class
   * @return the data
   */
  public static Object callMethod(Object invokeOn, String methodName) {
    if (invokeOn == null) {
      throw new NullPointerException("invokeOn is null: " + methodName);
    }
    return callMethod(invokeOn.getClass(), invokeOn, methodName, null,
        null, true, true);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer. This does not recurse
   * 
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @return the string
   */
  public static String replace(String text, Object searchFor,
      Object replaceWith) {
    return replace(null, null, text, searchFor, replaceWith, false, 0,
        false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @return the string
   */
  public static String replace(String text, Object searchFor,
      Object replaceWith, boolean recurse) {
    return replace(null, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @param removeIfFound
   *            true if removing from searchFor and replaceWith if found
   * @return the string
   */
  public static String replace(String text, Object searchFor,
      Object replaceWith, boolean recurse, boolean removeIfFound) {
    return replace(null, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, removeIfFound);
  }

  /**
   * <p>
   * Replaces all occurrences of a String within another String.
   * </p>
   * 
   * <p>
   * A <code>null</code> reference passed to this method is a no-op.
   * </p>
   * 
   * <pre>
   * StringUtils.replace(null, *, *)        = null
   * StringUtils.replace(&quot;&quot;, *, *)          = &quot;&quot;
   * StringUtils.replace(&quot;any&quot;, null, *)    = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, *, null)    = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, &quot;&quot;, *)      = &quot;any&quot;
   * StringUtils.replace(&quot;aba&quot;, &quot;a&quot;, null)  = &quot;aba&quot;
   * StringUtils.replace(&quot;aba&quot;, &quot;a&quot;, &quot;&quot;)    = &quot;b&quot;
   * StringUtils.replace(&quot;aba&quot;, &quot;a&quot;, &quot;z&quot;)   = &quot;zbz&quot;
   * </pre>
   * 
   * @see #replace(String text, String repl, String with, int max)
   * @param text
   *            text to search and replace in, may be null
   * @param repl
   *            the String to search for, may be null
   * @param with
   *            the String to replace with, may be null
   * @return the text with any replacements processed, <code>null</code> if
   *         null String input
   */
  public static String replace(String text, String repl, String with) {
    return replace(text, repl, with, -1);
  }

  /**
   * <p>
   * Replaces a String with another String inside a larger String, for the
   * first <code>max</code> values of the search String.
   * </p>
   * 
   * <p>
   * A <code>null</code> reference passed to this method is a no-op.
   * </p>
   * 
   * <pre>
   * StringUtils.replace(null, *, *, *)         = null
   * StringUtils.replace(&quot;&quot;, *, *, *)           = &quot;&quot;
   * StringUtils.replace(&quot;any&quot;, null, *, *)     = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, *, null, *)     = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, &quot;&quot;, *, *)       = &quot;any&quot;
   * StringUtils.replace(&quot;any&quot;, *, *, 0)        = &quot;any&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, null, -1) = &quot;abaa&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;&quot;, -1)   = &quot;b&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 0)   = &quot;abaa&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 1)   = &quot;zbaa&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 2)   = &quot;zbza&quot;
   * StringUtils.replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, -1)  = &quot;zbzz&quot;
   * </pre>
   * 
   * @param text
   *            text to search and replace in, may be null
   * @param repl
   *            the String to search for, may be null
   * @param with
   *            the String to replace with, may be null
   * @param max
   *            maximum number of values to replace, or <code>-1</code> if
   *            no maximum
   * @return the text with any replacements processed, <code>null</code> if
   *         null String input
   */
  public static String replace(String text, String repl, String with, int max) {
    if (text == null || isEmpty(repl) || with == null || max == 0) {
      return text;
    }

    StringBuffer buf = new StringBuffer(text.length());
    int start = 0, end = 0;
    while ((end = text.indexOf(repl, start)) != -1) {
      buf.append(text.substring(start, end)).append(with);
      start = end + repl.length();

      if (--max == 0) {
        break;
      }
    }
    buf.append(text.substring(start));
    return buf.toString();
  }

  /**
   * <p>
   * Checks if a String is empty ("") or null.
   * </p>
   * 
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty(&quot;&quot;)        = true
   * StringUtils.isEmpty(&quot; &quot;)       = false
   * StringUtils.isEmpty(&quot;bob&quot;)     = false
   * StringUtils.isEmpty(&quot;  bob  &quot;) = false
   * </pre>
   * 
   * <p>
   * NOTE: This method changed in Lang version 2.0. It no longer trims the
   * String. That functionality is available in isBlank().
   * </p>
   * 
   * @param str
   *            the String to check, may be null
   * @return <code>true</code> if the String is empty or null
   */
  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer. This does not recurse
   * 
   * @param outBuffer
   *            stringbuffer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   */
  public static void replace(StringBuffer outBuffer, String text,
      Object searchFor, Object replaceWith) {
    replace(outBuffer, null, text, searchFor, replaceWith, false, 0, false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param outBuffer
   *            stringbuffer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   */
  public static void replace(StringBuffer outBuffer, String text,
      Object searchFor, Object replaceWith, boolean recurse) {
    replace(outBuffer, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, false);
  }

  /**
   * replace a string with other strings, and either write to outWriter, or
   * StringBuffer, and if StringBuffer potentially return a string. If
   * outBuffer and outWriter are null, then return the String
   * 
   * @param outBuffer
   *            stringbuffer to write to, or null to not
   * @param outWriter
   *            Writer to write to, or null to not.
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for, or string, or list
   * @param replaceWith
   *            string array to replace with, or string, or list
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @param timeToLive
   *            if recursing, prevent endless loops
   * @param removeIfFound
   *            true if removing from searchFor and replaceWith if found
   * @return the String if outBuffer and outWriter are null
   * @throws IndexOutOfBoundsException
   *             if the lengths of the arrays are not the same (null is ok,
   *             and/or size 0)
   * @throws IllegalArgumentException
   *             if the search is recursive and there is an endless loop due
   *             to outputs of one being inputs to another
   */
  private static String replace(StringBuffer outBuffer, Writer outWriter,
      String text, Object searchFor, Object replaceWith, boolean recurse,
      int timeToLive, boolean removeIfFound) {

    // if recursing, we need to get the string, then print to buffer (since
    // we need multiple passes)
    if (!recurse) {
      return replaceHelper(outBuffer, outWriter, text, searchFor,
          replaceWith, recurse, timeToLive, removeIfFound);
    }
    // get the string
    String result = replaceHelper(null, null, text, searchFor, replaceWith,
        recurse, timeToLive, removeIfFound);
    if (outBuffer != null) {
      outBuffer.append(result);
      return null;
    }

    if (outWriter != null) {
      try {
        outWriter.write(result);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      return null;
    }

    return result;

  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer. This does not recurse
   * 
   * @param outWriter
   *            writer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   */
  public static void replace(Writer outWriter, String text, Object searchFor,
      Object replaceWith) {
    replace(null, outWriter, text, searchFor, replaceWith, false, 0, false);
  }

  /**
   * replace a string or strings from a string, and put the output in a string
   * buffer
   * 
   * @param outWriter
   *            writer to write to
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for
   * @param replaceWith
   *            string array to replace with
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   */
  public static void replace(Writer outWriter, String text, Object searchFor,
      Object replaceWith, boolean recurse) {
    replace(null, outWriter, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0, false);
  }

  /**
   * replace a string with other strings, and either write to outWriter, or
   * StringBuffer, and if StringBuffer potentially return a string. If
   * outBuffer and outWriter are null, then return the String
   * 
   * @param outBuffer
   *            stringbuffer to write to, or null to not
   * @param outWriter
   *            Writer to write to, or null to not.
   * @param text
   *            string to look in
   * @param searchFor
   *            string array to search for, or string, or list
   * @param replaceWith
   *            string array to replace with, or string, or list
   * @param recurse
   *            if true then do multiple replaces (on the replacements)
   * @param timeToLive
   *            if recursing, prevent endless loops
   * @param removeIfFound
   *            true if removing from searchFor and replaceWith if found
   * @return the String if outBuffer and outWriter are null
   * @throws IllegalArgumentException
   *             if the search is recursive and there is an endless loop due
   *             to outputs of one being inputs to another
   * @throws IndexOutOfBoundsException
   *             if the lengths of the arrays are not the same (null is ok,
   *             and/or size 0)
   */
  private static String replaceHelper(StringBuffer outBuffer,
      Writer outWriter, String text, Object searchFor,
      Object replaceWith, boolean recurse, int timeToLive,
      boolean removeIfFound) {

    try {
      // if recursing, this shouldnt be less than 0
      if (timeToLive < 0) {
        throw new IllegalArgumentException("TimeToLive under 0: "
            + timeToLive + ", " + text);
      }

      int searchForLength = length(searchFor);
      boolean done = false;
      // no need to do anything
      if (isEmpty(text)) {
        return text;
      }
      // need to write the input to output, later
      if (searchForLength == 0) {
        done = true;
      }

      boolean[] noMoreMatchesForReplIndex = null;
      int inputIndex = -1;
      int replaceIndex = -1;
      long resultPacked = -1;

      if (!done) {
        // make sure lengths are ok, these need to be equal
        if (searchForLength != length(replaceWith)) {
          throw new IndexOutOfBoundsException("Lengths dont match: "
              + searchForLength + ", " + length(replaceWith));
        }

        // keep track of which still have matches
        noMoreMatchesForReplIndex = new boolean[searchForLength];

        // index of replace array that will replace the search string
        // found
        

        resultPacked = findNextIndexHelper(searchForLength, searchFor,
            replaceWith, 
            noMoreMatchesForReplIndex, text, 0);

        inputIndex = unpackInt(resultPacked, true);
        replaceIndex = unpackInt(resultPacked, false);
      }

      // get a good guess on the size of the result buffer so it doesnt
      // have to double if it
      // goes over a bit
      boolean writeToWriter = outWriter != null;

      // no search strings found, we are done
      if (done || inputIndex == -1) {
        if (writeToWriter) {
          outWriter.write(text, 0, text.length());
          return null;
        }
        if (outBuffer != null) {
          appendSubstring(outBuffer, text, 0, text.length());
          return null;
        }
        return text;
      }

      // no buffer if writing to writer
      StringBuffer bufferToWriteTo = outBuffer != null ? outBuffer
          : (writeToWriter ? null : new StringBuffer(text.length()
              + replaceStringsBufferIncrease(text, searchFor,
                  replaceWith)));

      String searchString = null;
      String replaceString = null;

      int start = 0;

      while (inputIndex != -1) {

        searchString = (String) get(searchFor, replaceIndex);
        replaceString = (String) get(replaceWith, replaceIndex);
        if (writeToWriter) {
          outWriter.write(text, start, inputIndex - start);
          outWriter.write(replaceString);
        } else {
          appendSubstring(bufferToWriteTo, text, start, inputIndex)
              .append(replaceString);
        }

        if (removeIfFound) {
          // better be an iterator based find replace
          searchFor = remove(searchFor, replaceIndex);
          replaceWith = remove(replaceWith, replaceIndex);
          noMoreMatchesForReplIndex = (boolean[])remove(noMoreMatchesForReplIndex, replaceIndex);
          // we now have a lesser size if we removed one
          searchForLength--;
        }

        start = inputIndex + searchString.length();

        resultPacked = findNextIndexHelper(searchForLength, searchFor,
            replaceWith, 
            noMoreMatchesForReplIndex, text, start);
        inputIndex = unpackInt(resultPacked, true);
        replaceIndex = unpackInt(resultPacked, false);
      }
      if (writeToWriter) {
        outWriter.write(text, start, text.length() - start);

      } else {
        appendSubstring(bufferToWriteTo, text, start, text.length());
      }

      // no need to convert to string if incoming buffer or writer
      if (writeToWriter || outBuffer != null) {
        if (recurse) {
          throw new IllegalArgumentException(
              "Cannot recurse and write to existing buffer or writer!");
        }
        return null;
      }
      String resultString = bufferToWriteTo.toString();

      if (recurse) {
        return replaceHelper(outBuffer, outWriter, resultString,
            searchFor, replaceWith, recurse, timeToLive - 1, false);
      }
      // this might be null for writer
      return resultString;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * give a best guess on buffer increase for String[] replace get a good
   * guess on the size of the result buffer so it doesnt have to double if it
   * goes over a bit
   * 
   * @param text
   * @param repl
   * @param with
   * @return the increase, with 20% cap
   */
  static int replaceStringsBufferIncrease(String text, Object repl,
      Object with) {
    // count the greaters
    int increase = 0;
    Iterator iteratorReplace = iterator(repl);
    Iterator iteratorWith = iterator(with);
    int replLength = length(repl);
    String currentRepl = null;
    String currentWith = null;
    for (int i = 0; i < replLength; i++) {
      currentRepl = (String) next(repl, iteratorReplace, i);
      currentWith = (String) next(with, iteratorWith, i);
      if (currentRepl == null || currentWith == null) {
        throw new NullPointerException("Replace string is null: "
            + text + ", " + currentRepl + ", " + currentWith);
      }
      int greater = currentWith.length() - currentRepl.length();
      increase += greater > 0 ? 3 * greater : 0; // assume 3 matches
    }
    // have upper-bound at 20% increase, then let Java take over
    increase = Math.min(increase, text.length() / 5);
    return increase;
  }

  /**
   * Helper method to find the next match in an array of strings replace
   * 
   * @param searchForLength
   * @param searchFor
   * @param replaceWith
   * @param noMoreMatchesForReplIndex
   * @param input
   * @param start
   *            is where to start looking
   * @return result packed into a long, inputIndex first, then replaceIndex
   */
  private static long findNextIndexHelper(int searchForLength,
      Object searchFor, Object replaceWith, boolean[] noMoreMatchesForReplIndex,
      String input, int start) {

    int inputIndex = -1;
    int replaceIndex = -1;

    Iterator iteratorSearchFor = iterator(searchFor);
    Iterator iteratorReplaceWith = iterator(replaceWith);

    String currentSearchFor = null;
    String currentReplaceWith = null;
    int tempIndex = -1;
    for (int i = 0; i < searchForLength; i++) {
      currentSearchFor = (String) next(searchFor, iteratorSearchFor, i);
      currentReplaceWith = (String) next(replaceWith,
          iteratorReplaceWith, i);
      if (noMoreMatchesForReplIndex[i] || isEmpty(currentSearchFor)
          || currentReplaceWith == null) {
        continue;
      }
      tempIndex = input.indexOf(currentSearchFor, start);

      // see if we need to keep searching for this
      noMoreMatchesForReplIndex[i] = tempIndex == -1;

      if (tempIndex != -1 && (inputIndex == -1 || tempIndex < inputIndex)) {
        inputIndex = tempIndex;
        replaceIndex = i;
      }

    }
    // dont create an array, no more objects
    long resultPacked = packInts(inputIndex, replaceIndex);
    return resultPacked;
  }

  /**
   * pack two ints into a long. Note: the first is held in the left bits, the
   * second is held in the right bits
   * 
   * @param first
   *            is first int
   * @param second
   *            is second int
   * @return the long which has two ints in there
   */
  public static long packInts(int first, int second) {
    long result = first;
    result <<= 32;
    result |= second;
    return result;
  }

  /**
   * take a long
   * 
   * @param theLong
   *            to unpack
   * @param isFirst
   *            true for first, false for second
   * @return one of the packed ints, first or second
   */
  public static int unpackInt(long theLong, boolean isFirst) {

    int result = 0;
    // put this in the position of the second one
    if (isFirst) {
      theLong >>= 32;
    }
    // only look at right part
    result = (int) (theLong & 0xffffffff);
    return result;
  }

  /**
   * append a substring to a stringbuffer. removes dependency on substring
   * which creates objects
   * 
   * @param buf
   *            stringbuffer
   * @param string
   *            source string
   * @param start
   *            start index of source string
   * @param end
   *            end index of source string
   * @return the string buffer for chaining
   */
  private static StringBuffer appendSubstring(StringBuffer buf,
      String string, int start, int end) {
    for (int i = start; i < end; i++) {
      buf.append(string.charAt(i));
    }
    return buf;
  }

  /**
   * Get a specific index of an array or collection (note for collections and
   * iterating, it is more efficient to get an iterator and iterate
   * 
   * @param arrayOrCollection
   * @param index
   * @return the object at that index
   */
  public static Object get(Object arrayOrCollection, int index) {

    if (arrayOrCollection == null) {
      if (index == 0) {
        return null;
      }
      throw new RuntimeException("Trying to access index " + index
          + " of null");
    }

    // no need to iterator on list (e.g. FastProxyList has no iterator
    if (arrayOrCollection instanceof List) {
      return ((List) arrayOrCollection).get(index);
    }
    if (arrayOrCollection instanceof Collection) {
      Iterator iterator = iterator(arrayOrCollection);
      for (int i = 0; i < index; i++) {
        next(arrayOrCollection, iterator, i);
      }
      return next(arrayOrCollection, iterator, index);
    }

    if (arrayOrCollection.getClass().isArray()) {
      return Array.get(arrayOrCollection, index);
    }

    if (index == 0) {
      return arrayOrCollection;
    }

    throw new RuntimeException("Trying to access index " + index
        + " of and object: " + arrayOrCollection);
  }


  
  /**
   * fail safe toString for Exception blocks, and include the stack
   * if there is a problem with toString()
   * @param object
   * @return the toStringSafe string
   */
  public static String toStringSafe(Object object) {
    if (object == null) {
      return null;
    }
    
    try {
      //give size and type if collection
      if (object instanceof Collection) {
        Collection<Object> collection = (Collection<Object>) object;
        int collectionSize = collection.size();
        if (collectionSize == 0) {
          return "Empty " + object.getClass().getSimpleName();
        }
        Object first = collection.iterator().next();
        return object.getClass().getSimpleName() + " of size " 
          + collectionSize + " with first type: " + 
          (first == null ? null : first.getClass());
      }
    
      return object.toString();
    } catch (Exception e) {
      return "<<exception>> " + object.getClass() + ":\n" + ExceptionUtils.getFullStackTrace(e) + "\n";
    }
  }

  /**
   * get the boolean value for an object, cant be null or blank
   * 
   * @param object
   * @return the boolean
   */
  public static boolean booleanValue(Object object) {
  	// first handle blanks
  	if (nullOrBlank(object)) {
  		throw new RuntimeException(
  				"Expecting something which can be converted to boolean, but is null or blank: '"
  						+ object + "'");
  	}
  	// its not blank, just convert
  	if (object instanceof Boolean) {
  		return (Boolean) object;
  	}
  	if (object instanceof String) {
  		String string = (String) object;
  		if (StringUtils.equalsIgnoreCase(string, "true")
  				|| StringUtils.equalsIgnoreCase(string, "t")
  				|| StringUtils.equalsIgnoreCase(string, "yes")
  				|| StringUtils.equalsIgnoreCase(string, "y")) {
  			return true;
  		}
  		if (StringUtils.equalsIgnoreCase(string, "false")
  				|| StringUtils.equalsIgnoreCase(string, "f")
  				|| StringUtils.equalsIgnoreCase(string, "no")
  				|| StringUtils.equalsIgnoreCase(string, "n")) {
  			return false;
  		}
  		throw new RuntimeException(
  				"Invalid string to boolean conversion: '" + string
  						+ "' expecting true|false or t|f or yes|no or y|n case insensitive");
  
  	}
  	throw new RuntimeException("Cant convert object to boolean: "
  			+ object.getClass());
  
  }

  /**
   * get the boolean value for an object
   * 
   * @param object
   * @param defaultBoolean
   *            if object is null or empty
   * @return the boolean
   */
  public static boolean booleanValue(Object object, boolean defaultBoolean) {
  	if (nullOrBlank(object)) {
  		return defaultBoolean;
  	}
  	return booleanValue(object);
  }

  /**
   * get the Boolean value for an object
   * 
   * @param object
   * @return the Boolean or null if null or empty
   */
  public static Boolean booleanObjectValue(Object object) {
    if (GrouperUtil.nullOrBlank(object)) {
      return null;
    }
    return GrouperUtil.booleanValue(object);
  }

  /**
   * is an object null or blank
   * 
   * @param object
   * @return true if null or blank
   */
  public static boolean nullOrBlank(Object object) {
  	// first handle blanks and nulls
  	if (object == null) {
  		return true;
  	}
  	if (object instanceof String && StringUtils.isBlank(((String) object))) {
  		return true;
  	}
  	return false;
  
  }

  /**
   * get a getter method object for a class, potentially in superclasses
   * @param theClass
   * @param fieldName
   * @param callOnSupers true if superclasses should be looked in for the getter
   * @param throwExceptionIfNotFound will throw runtime exception if not found
   * @return the getter object or null if not found (or exception if param is set)
   */
  public static Method getter(Class theClass, String fieldName, boolean callOnSupers, 
      boolean throwExceptionIfNotFound) {
    String getterName = getterNameFromPropertyName(fieldName);
    return getterHelper(theClass, fieldName, getterName, callOnSupers, throwExceptionIfNotFound);
  }

  /**
   * get a setter method object for a class, potentially in superclasses
   * @param theClass
   * @param fieldName
   * @param getterName name of setter
   * @param callOnSupers true if superclasses should be looked in for the setter
   * @param throwExceptionIfNotFound will throw runtime exception if not found
   * @return the setter object or null if not found (or exception if param is set)
   */
  public static Method getterHelper(Class theClass, String fieldName, String getterName, 
      boolean callOnSupers, boolean throwExceptionIfNotFound) {
    Method[] methods = retrieveDeclaredMethods(theClass);
    if (methods != null) {
      for (Method method : methods) {
        if (StringUtils.equals(getterName, method.getName()) && isGetter(method)) {
          return method;
        }
      }
    }
    //if method not found
    //if traversing up, and not Object, and not instance method
    if (callOnSupers && !theClass.equals(Object.class)) {
      return getterHelper(theClass.getSuperclass(), fieldName, getterName, 
          callOnSupers, throwExceptionIfNotFound);
    }
    //maybe throw an exception
    if (throwExceptionIfNotFound) {
      throw new PropertyDoesNotExistUnchecked("Cant find getter: "
          + getterName + ", in: " + theClass
          + ", callOnSupers: " + callOnSupers);
    }
    return null;
  }

  /**
   * generate getBb from bb
   * @param propertyName
   * @return the getter 
   */
  public static String getterNameFromPropertyName(String propertyName) {
    return "get" + StringUtils.capitalize(propertyName);
  }

  /**
   * get all getters from a class, including superclasses (if specified) (up to and including the specified superclass).  
   * ignore a certain marker annotation, or only include it.
   * Dont get static or final getters, and get getters of all types
   * @param theClass to look for fields in
   * @param superclassToStopAt to go up to or null to go up to Object
   * @param markerAnnotation if this is not null, then if the field has this annotation, then do not
   * include in list (if includeAnnotation is false)
   * @param includeAnnotation true if the attribute should be included if annotation is present, false if exclude
   * @return the set of field names or empty set if none
   */
  @SuppressWarnings("unchecked")
  public static Set<Method> getters(Class theClass, Class superclassToStopAt,
      Class<? extends Annotation> markerAnnotation, Boolean includeAnnotation) {
    return gettersHelper(theClass, superclassToStopAt, null, true, 
        markerAnnotation, includeAnnotation);
  }

  /**
   * get all getters from a class, including superclasses (if specified)
   * @param theClass to look for fields in
   * @param superclassToStopAt to go up to or null to go up to Object
   * @param fieldType is the type of the field to get
   * @param includeSuperclassToStopAt if we should include the superclass
   * @param markerAnnotation if this is not null, then if the field has this annotation, then do not
   * include in list (if includeAnnotation is false)
   * @param includeAnnotation true if the attribute should be included if annotation is present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  static Set<Method> gettersHelper(Class theClass, Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, 
      Class<? extends Annotation> markerAnnotation, Boolean includeAnnotation) {
    //MAKE SURE IF ANY MORE PARAMS ARE ADDED, THE CACHE KEY IS CHANGED!
    
    Set<Method> getterSet = null;
    String cacheKey = theClass + CACHE_SEPARATOR + superclassToStopAt + CACHE_SEPARATOR + fieldType + CACHE_SEPARATOR
      + includeSuperclassToStopAt + CACHE_SEPARATOR + markerAnnotation + CACHE_SEPARATOR + includeAnnotation;
    getterSet = getterSetCache().get(cacheKey);
    if (getterSet != null) {
      return getterSet;
    }
    
    getterSet = new LinkedHashSet<Method>();
    gettersHelper(theClass, superclassToStopAt, fieldType, includeSuperclassToStopAt, 
        markerAnnotation, getterSet, includeAnnotation);
  
    //add to cache
    getterSetCache().put(cacheKey, getterSet);
    
    return getterSet;
    
  }

  /**
   * get all getters from a class, including superclasses (if specified)
   * @param theClass to look for fields in
   * @param superclassToStopAt to go up to or null to go up to Object
   * @param propertyType is the type of the field to get
   * @param includeSuperclassToStopAt if we should include the superclass
   * @param markerAnnotation if this is not null, then if the field has this annotation, then do not
   * include in list
   * @param getterSet set to add fields to
   * @param includeAnnotation if include or exclude
   */
  @SuppressWarnings("unchecked")
  private static void gettersHelper(Class theClass, Class superclassToStopAt, Class<?> propertyType,
      boolean includeSuperclassToStopAt,  
      Class<? extends Annotation> markerAnnotation, Set<Method> getterSet, Boolean includeAnnotation) {
    theClass = unenhanceClass(theClass);
    Method[] methods = retrieveDeclaredMethods(theClass);
    if (length(methods) != 0) {
      for (Method method: methods) {
        //must be a getter
        if (!isGetter(method)) {
          continue;
        }
        //if checking for annotation
        if (markerAnnotation != null
            && (includeAnnotation != method.isAnnotationPresent(markerAnnotation))) {
          continue;
        }
        //if checking for type, and not right type, continue
        if (propertyType != null && !propertyType.isAssignableFrom(method.getReturnType())) {
          continue;
        }
        
        //go for it
        getterSet.add(method);
      }
    }
    //see if done recursing (if superclassToStopAt is null, then stop at Object
    if (theClass.equals(superclassToStopAt) || theClass.equals(Object.class)) {
      return;
    }
    Class superclass = theClass.getSuperclass();
    if (!includeSuperclassToStopAt && superclass.equals(superclassToStopAt)) {
      return;
    }
    //recurse
    gettersHelper(superclass, superclassToStopAt, propertyType, 
        includeSuperclassToStopAt, markerAnnotation, getterSet,
        includeAnnotation);
  }

  /**
   * if the method name starts with get, and takes no args, and returns something, 
   * then getter
   * @param method 
   * @return true if getter
   */
  public static boolean isGetter(Method method) {
    
    //must start with get
    String methodName = method.getName();
    if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
      return false;
    }
  
    //must not be void
    if (method.getReturnType() == Void.TYPE) {
      return false;
    }
    
    //must not take args
    if (length(method.getParameterTypes()) != 0) {
      return false;
    }
    
    //must not be static
    if (Modifier.isStatic(method.getModifiers())) {
      return false;
    }
    
    return true;
  }

  /**
   * assign data to a setter.  Will find the field in superclasses, will typecast, 
   * and will override security (private, protected, etc)
   * @param invokeOn to call on or null for static
   * @param fieldName method name to call
   * @param dataToAssign data  
   * @param typeCast will typecast if true
   * @throws PropertyDoesNotExistUnchecked if not there
   */
  public static void assignSetter(Object invokeOn, 
      String fieldName, Object dataToAssign, boolean typeCast) {
    Class invokeOnClass = invokeOn.getClass();
    try {
      Method setter = setter(invokeOnClass, fieldName, true, true);
      setter.setAccessible(true);
      if (typeCast) {
        dataToAssign = typeCast(dataToAssign, setter.getParameterTypes()[0]);
      }
      setter.invoke(invokeOn, new Object[]{dataToAssign});
    } catch (Exception e) {
      throw new RuntimeException("Problem assigning setter: " + fieldName
          + " on class: " + invokeOnClass + ", type of data is: " + GrouperUtil.className(dataToAssign), e);
    }
  }

  /**
   * if the method name starts with get, and takes no args, and returns something, 
   * then getter
   * @param method 
   * @return true if getter
   */
  public static boolean isSetter(Method method) {
    
    //must start with get
    if (!method.getName().startsWith("set")) {
      return false;
    }
  
    //must be void
    if (method.getReturnType() != Void.TYPE) {
      return false;
    }
    
    //must take one arg
    if (length(method.getParameterTypes()) != 1) {
      return false;
    }
    
    //must not be static
    if (Modifier.isStatic(method.getModifiers())) {
      return false;
    }
    
    return true;
  }

  /**
   * get a setter method object for a class, potentially in superclasses
   * @param theClass
   * @param fieldName
   * @param callOnSupers true if superclasses should be looked in for the setter
   * @param throwExceptionIfNotFound will throw runtime exception if not found
   * @return the setter object or null if not found (or exception if param is set)
   */
  public static Method setter(Class theClass, String fieldName, boolean callOnSupers, 
      boolean throwExceptionIfNotFound) {
    String setterName = setterNameFromPropertyName(fieldName);
    return setterHelper(theClass, fieldName, setterName, callOnSupers, throwExceptionIfNotFound);
  }

  /**
   * get a setter method object for a class, potentially in superclasses
   * @param theClass
   * @param fieldName
   * @param setterName name of setter
   * @param callOnSupers true if superclasses should be looked in for the setter
   * @param throwExceptionIfNotFound will throw runtime exception if not found
   * @return the setter object or null if not found (or exception if param is set)
   */
  public static Method setterHelper(Class theClass, String fieldName, String setterName, 
      boolean callOnSupers, boolean throwExceptionIfNotFound) {
    Method[] methods = retrieveDeclaredMethods(theClass);
    if (methods != null) {
      for (Method method : methods) {
        if (StringUtils.equals(setterName, method.getName()) && isSetter(method)) {
          return method;
        }
      }
    }
    //if method not found
    //if traversing up, and not Object, and not instance method
    if (callOnSupers && !theClass.equals(Object.class)) {
      return setterHelper(theClass.getSuperclass(), fieldName, setterName, 
          callOnSupers, throwExceptionIfNotFound);
    }
    //maybe throw an exception
    if (throwExceptionIfNotFound) {
      throw new PropertyDoesNotExistUnchecked("Cant find setter: "
          + setterName + ", in: " + theClass
          + ", callOnSupers: " + callOnSupers);
    }
    return null;
  }

  /**
   * generate setBb from bb
   * @param propertyName
   * @return the setter 
   */
  public static String setterNameFromPropertyName(String propertyName) {
    return "set" + StringUtils.capitalize(propertyName);
  }

  /**
   * get all setters from a class, including superclasses (if specified)
   * @param theClass to look for fields in
   * @param superclassToStopAt to go up to or null to go up to Object
   * @param fieldType is the type of the field to get
   * @param includeSuperclassToStopAt if we should include the superclass
   * @param markerAnnotation if this is not null, then if the field has this annotation, then do not
   * include in list (if includeAnnotation is false)
   * @param includeAnnotation true if the attribute should be included if annotation is present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  public static Set<Method> setters(Class theClass, Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, 
      Class<? extends Annotation> markerAnnotation, boolean includeAnnotation) {
    return settersHelper(theClass, superclassToStopAt, fieldType, 
        includeSuperclassToStopAt, markerAnnotation, includeAnnotation);
  }

  /**
   * get all setters from a class, including superclasses (if specified)
   * @param theClass to look for fields in
   * @param superclassToStopAt to go up to or null to go up to Object
   * @param fieldType is the type of the field to get
   * @param includeSuperclassToStopAt if we should include the superclass
   * @param markerAnnotation if this is not null, then if the field has this annotation, then do not
   * include in list (if includeAnnotation is false)
   * @param includeAnnotation true if the attribute should be included if annotation is present, false if exclude
   * @return the set of fields (wont return null)
   */
  @SuppressWarnings("unchecked")
  static Set<Method> settersHelper(Class theClass, Class superclassToStopAt, Class<?> fieldType,
      boolean includeSuperclassToStopAt, 
      Class<? extends Annotation> markerAnnotation, boolean includeAnnotation) {
    //MAKE SURE IF ANY MORE PARAMS ARE ADDED, THE CACHE KEY IS CHANGED!
    
    Set<Method> setterSet = null;
    String cacheKey = theClass + CACHE_SEPARATOR + superclassToStopAt + CACHE_SEPARATOR + fieldType + CACHE_SEPARATOR
      + includeSuperclassToStopAt + CACHE_SEPARATOR + markerAnnotation + CACHE_SEPARATOR + includeAnnotation;
    setterSet = setterSetCache().get(cacheKey);
    if (setterSet != null) {
      return setterSet;
    }
    
    setterSet = new LinkedHashSet<Method>();
    settersHelper(theClass, superclassToStopAt, fieldType, includeSuperclassToStopAt, 
        markerAnnotation, setterSet, includeAnnotation);
  
    //add to cache
    setterSetCache().put(cacheKey, setterSet);
    
    return setterSet;
    
  }

  /**
   * get all setters from a class, including superclasses (if specified)
   * @param theClass to look for fields in
   * @param superclassToStopAt to go up to or null to go up to Object
   * @param propertyType is the type of the field to get
   * @param includeSuperclassToStopAt if we should include the superclass
   * @param markerAnnotation if this is not null, then if the field has this annotation, then do not
   * include in list
   * @param setterSet set to add fields to
   * @param includeAnnotation if include or exclude (or null if not looking for annotations)
   */
  @SuppressWarnings("unchecked")
  private static void settersHelper(Class theClass, Class superclassToStopAt, Class<?> propertyType,
      boolean includeSuperclassToStopAt,  
      Class<? extends Annotation> markerAnnotation, Set<Method> setterSet, Boolean includeAnnotation) {
    theClass = unenhanceClass(theClass);
    Method[] methods = retrieveDeclaredMethods(theClass);
    if (length(methods) != 0) {
      for (Method method: methods) {
        //must be a getter
        if (!isSetter(method)) {
          continue;
        }
        //if checking for annotation
        if (markerAnnotation != null
            && (includeAnnotation != method.isAnnotationPresent(markerAnnotation))) {
          continue;
        }
        //if checking for type, and not right type, continue
        if (propertyType != null && !propertyType.isAssignableFrom(method.getParameterTypes()[0])) {
          continue;
        }
        
        //go for it
        setterSet.add(method);
      }
    }
    //see if done recursing (if superclassToStopAt is null, then stop at Object
    if (theClass.equals(superclassToStopAt) || theClass.equals(Object.class)) {
      return;
    }
    Class superclass = theClass.getSuperclass();
    if (!includeSuperclassToStopAt && superclass.equals(superclassToStopAt)) {
      return;
    }
    //recurse
    settersHelper(superclass, superclassToStopAt, propertyType, 
        includeSuperclassToStopAt, markerAnnotation, setterSet,
        includeAnnotation);
  }

  /**
   * If this is a getter or setter, then get the property name
   * @param method
   * @return the property name
   */
  public static String propertyName(Method method) {
    String methodName = method.getName();
    boolean isGetter = methodName.startsWith("get");
    boolean isSetter = methodName.startsWith("set");
    boolean isIsser = methodName.startsWith("is");
    int expectedLength = isIsser ? 2 : 3; 
    int length = methodName.length();
    if ((!(isGetter || isSetter || isIsser)) || (length <= expectedLength)) {
      throw new RuntimeException("Not a getter or setter: " + methodName);
    }
    char fourthCharLower = Character.toLowerCase(methodName.charAt(expectedLength));
    //if size 4, then return the string
    if (length == expectedLength +1) {
      return Character.toString(fourthCharLower);
    }
    //return the lower appended with the rest
    return fourthCharLower + methodName.substring(expectedLength+1, length);
  }

  /**
   * use reflection to get a property type based on getter or setter or field
   * @param theClass
   * @param propertyName
   * @return the property type
   */
  public static Class propertyType(Class theClass, String propertyName) {
    theClass = unenhanceClass(theClass);
    Method method = getter(theClass, propertyName, true, false);
    if (method != null) {
      return method.getReturnType();
    }
    //use setter
    method = setter(theClass, propertyName, true, false);
    if (method != null) {
      return method.getParameterTypes()[0];
    }
    //no setter or getter, use field
    Field field = field(theClass, propertyName, true, true);
    return field.getType();
  }

  /**
   * If necessary, convert an object to another type.  if type is Object.class, just return the input.
   * Do not convert null to an empty primitive
   * @param <T> is template type
   * @param value
   * @param theClass
   * @return the object of that instance converted into something else
   */
  public static <T> T typeCast(Object value, Class<T> theClass) {
    //default behavior is not to convert null to empty primitive
    return typeCast(value, theClass, false, false);
  }

  /**
   * <pre>
   * make a new file in the name prefix dir.  If parent dir name is c:\temp
   * and namePrefix is grouperDdl and nameSuffix is sql, then the file will be:
   * 
   * c:\temp\grouperDdl_20080721_13_45_43_123.sql
   *  
   * If the file exists, it will make a new filename, and create the empty file, and return it
   * </pre>
   *  
   * @param parentDirName can be blank for current dir
   * @param namePrefix the part before the date part
   * @param nameSuffix the last part of file name (can contain dot or will be the extension
   * @param createFile true to create the file
   * @return the created file
   */
  public static File newFileUniqueName(String parentDirName, String namePrefix, String nameSuffix, boolean createFile) {
    DateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS");
    if (!StringUtils.isBlank(parentDirName)) {
    	parentDirName=fixRelativePath(parentDirName);
      if (!parentDirName.endsWith("/") && !parentDirName.endsWith("\\")) {
        parentDirName += File.separator;
      }
      
      //make sure it exists and is a dir
      File parentDir = new File(parentDirName);
      if (!parentDir.exists()) {
        if (!parentDir.mkdirs()) {
          throw new RuntimeException("Cant make dir: " + parentDir.getAbsolutePath());
        }
      } else {
        if (!parentDir.isDirectory()) {
          throw new RuntimeException("Parent dir is not a directory: " + parentDir.getAbsolutePath());
        } 
      }
      
    } else {
      //make it empty string so it will concatenate well
      parentDirName = "";
    }
    //make sure suffix has a dot in it
    if (!nameSuffix.contains(".")) {
      nameSuffix = "." + nameSuffix;
    }
    
    String fileName = parentDirName + namePrefix + "_" + fileNameFormat.format(new Date()) + nameSuffix;
    int dotLocation = fileName.lastIndexOf('.');
    String fileNamePre = fileName.substring(0,dotLocation);
    String fileNamePost = fileName.substring(dotLocation);
    File theFile = new File(fileName);

    int i;
    
    for (i=0;i<1000;i++) {
      
      if (!theFile.exists()) {
        break;
      }
      
      fileName = fileNamePre + "_" + i + fileNamePost;
      theFile = new File(fileName);
      
    }
    
    if (i>=1000) {
      throw new RuntimeException("Cant find filename to create: " + fileName);
    }
    
    if (createFile) {
      try {
        if (!theFile.createNewFile()) {
          throw new RuntimeException("Cant create file, it returned false");
        }
      } catch (Exception e) {
        throw new RuntimeException("Cant create file: " + fileName + ", make sure " +
        		"permissions and such are ok, or change file location in grouper.properties if applicable", e);
      }
    }
    return theFile;
  }
  
  /**
   * <pre>
   * Convert an object to a java.util.Date.  allows, dates, null, blank, 
   * yyyymmdd or yyyymmdd hh24:mm:ss
   * or yyyy/MM/dd HH:mm:ss.SSS
   * </pre>
   * @param inputObject
   *          is the String or Date to convert
   * 
   * @return the Date
   */
  public static Date dateValue(Object inputObject) {
    if (inputObject == null) {
      return null;
    } 

    if (inputObject instanceof java.util.Date) {
      return (Date)inputObject;
    }

    if (inputObject instanceof String) {
      String input = (String)inputObject;
      //trim and handle null and empty
      if (StringUtils.isBlank(input)) {
        return null;
      }

      try {
        if (input.length() == 8) {
          
          return dateFormat().parse(input);
        }
        if (!StringUtils.contains(input, '.')) {
          if (StringUtils.contains(input, '/')) {
            return dateMinutesSecondsFormat.parse(input);
          }
          //else no slash
          return dateMinutesSecondsNoSlashFormat.parse(input);
        }
        if (StringUtils.contains(input, '/')) {
          //see if the period is 6 back
          int lastDotIndex = input.lastIndexOf('.');
          if (lastDotIndex == input.length() - 7) {
            String nonNanoInput = input.substring(0,input.length()-3);
            Date date = timestampFormat.parse(nonNanoInput);
            //get the last 3
            String lastThree = input.substring(input.length()-3,input.length());
            int lastThreeInt = Integer.parseInt(lastThree);
            Timestamp timestamp = new Timestamp(date.getTime());
            timestamp.setNanos(timestamp.getNanos() + (lastThreeInt * 1000));
            return timestamp;
          }
          return timestampFormat.parse(input);
        }
        //else no slash
        return timestampNoSlashFormat.parse(input);
      } catch (ParseException pe) {
        throw new RuntimeException(errorStart + toStringForLog(input));
      }
    }
    
    throw new RuntimeException("Cannot convert Object to date : " + toStringForLog(inputObject));
  }

  /**
   * See if the input is null or if string, if it is empty or blank (whitespace)
   * @param input
   * @return true if blank
   */
  public static boolean isBlank(Object input) {
    if (null == input) {
      return true;
    }
    return (input instanceof String && StringUtils.isBlank((String)input));
  }

  /**
   * If necessary, convert an object to another type.  if type is Object.class, just return the input
   * @param <T> is the type to return
   * @param value
   * @param theClass
   * @param convertNullToDefaultPrimitive if the value is null, and theClass is primitive, should we
   * convert the null to a primitive default value
   * @param useNewInstanceHooks if theClass is not recognized, then honor the string "null", "newInstance",
   * or get a constructor with one param, and call it
   * @return the object of that instance converted into something else
   */
  @SuppressWarnings("unchecked")
  public static <T> T typeCast(Object value, Class<T> theClass, 
      boolean convertNullToDefaultPrimitive, boolean useNewInstanceHooks) {
    
    if (Object.class.equals(theClass)) {
      return (T)value;
    }
    
    if (value==null) {
      if (convertNullToDefaultPrimitive && theClass.isPrimitive()) {
        if ( theClass == boolean.class ) {
          return (T)Boolean.FALSE;
        }
        if ( theClass == char.class ) {
          return (T)(Object)0;
        }
        //convert 0 to the type
        return typeCast(0, theClass, false, false);
      }
      return null;
    }
  
    if (theClass.isInstance(value)) {
      return (T)value;
    }
    
    //if array, get the base class
    if (theClass.isArray() && theClass.getComponentType() != null) {
      theClass = (Class<T>)theClass.getComponentType();
    }
    Object resultValue = null;
    //loop through and see the primitive types etc
    if (theClass.equals(Date.class)) {
      resultValue = dateValue(value);
    } else if (theClass.equals(String.class)) {
      resultValue = stringValue(value);
    } else if (theClass.equals(Timestamp.class)) {
      resultValue = toTimestamp(value);
    } else if (theClass.equals(Boolean.class) || theClass.equals(boolean.class)) {
      resultValue = booleanObjectValue(value);
    } else if (theClass.equals(Integer.class) || theClass.equals(int.class)) {
      resultValue = intObjectValue(value, true);
    } else if (theClass.equals(Double.class) || theClass.equals(double.class)) {
      resultValue = doubleObjectValue(value, true);
    } else if (theClass.equals(Float.class) || theClass.equals(float.class)) {
      resultValue = floatObjectValue(value, true);
    } else if (theClass.equals(Long.class) || theClass.equals(long.class)) {
      resultValue = longObjectValue(value, true);
    } else if (theClass.equals(Byte.class) || theClass.equals(byte.class)) {
      resultValue = byteObjectValue(value);
    } else if (theClass.equals(Character.class) || theClass.equals(char.class)) {
      resultValue = charObjectValue(value);
    } else if (theClass.equals(Short.class) || theClass.equals(short.class)) {
      resultValue = shortObjectValue(value);
    } else if ( theClass.isEnum() && (value instanceof String) ) {
      resultValue = Enum.valueOf((Class)theClass, (String) value);
    } else if ( theClass.equals(Class.class) && (value instanceof String) ) {
      resultValue = forName((String)value);
    } else if (useNewInstanceHooks && value instanceof String) {
      String stringValue = (String)value;
      if ( StringUtils.equals("null", stringValue)) {
        resultValue = null;
      } else if (StringUtils.equals("newInstance", stringValue)) {
        resultValue = newInstance(theClass);
      } else { // instantiate using string
        //note, we could typecast this to fit whatever is there... right now this is used for annotation
        try {
          Constructor constructor = theClass.getConstructor(new Class[] {String.class} );
          resultValue = constructor.newInstance(new Object[] {stringValue} );            
        } catch (Exception e) {
          throw new RuntimeException("Cant find constructor with string for class: " + theClass);
        }
      }
    } else {
      throw new RuntimeException("Cannot convert from type: " + value.getClass() + " to type: " + theClass);
    }
  
    return (T)resultValue;
  }
  
  /**
   * see if a class is a scalar (not bean, not array or list, etc)
   * @param type
   * @return true if scalar
   */
  public static boolean isScalar(Class<?> type) {
    
    if (type.isArray()) {
      return false;
    }
    
    //definitely all primitives
    if (type.isPrimitive()) {
      return true;
    }
    //Integer, Float, etc
    if (Number.class.isAssignableFrom(type)) {
      return true;
    }
    //Date, Timestamp
    if (Date.class.isAssignableFrom(type)) {
      return true;
    }
    if (Character.class.equals(type)) {
      return true;
    }
    //handles strings and string builders
    if (CharSequence.class.equals(type) || CharSequence.class.isAssignableFrom(type)) {
      return true;
    }
    if (Class.class == type || Boolean.class == type || type.isEnum()) {
      return true;
    }
    //appears not to be a scalar
    return false;
  }
  
  
  /**
   * <pre>
   * Convert a string or object to a timestamp (could be string, date, timestamp, etc)
   * yyyymmdd
   * or
   * yyyy/MM/dd HH:mm:ss
   * or
   * yyyy/MM/dd HH:mm:ss.SSS
   * or
   * yyyy/MM/dd HH:mm:ss.SSSSSS
   * 
   * </pre>
   * 
   * @param input
   * @return the timestamp 
   * @throws RuntimeException if invalid format
   */
  public static Timestamp toTimestamp(Object input) {

    if (null == input) {
      return null;
    } else if (input instanceof java.sql.Timestamp) {
      return (Timestamp) input;
    } else if (input instanceof String) {
      return stringToTimestamp((String) input);
    } else if (input instanceof Date) {
      return new Timestamp(((Date)input).getTime());
    } else if (input instanceof java.sql.Date) {
      return new Timestamp(((java.sql.Date)input).getTime());
    } else {
      throw new RuntimeException("Cannot convert Object to timestamp : " + input);
    }

  }

  /**
   * convert an object to a string
   * 
   * @param input
   *          is the object to convert
   * 
   * @return the String conversion of the object
   */
  public static String stringValue(Object input) {
    //this isnt needed
    if (input == null) {
      return (String) input;
    }

    if (input instanceof Timestamp) {
      //convert to yyyy/MM/dd HH:mm:ss.SSS
      return timestampToString((Timestamp) input);
    }

    if (input instanceof Date) {
      //convert to yyyymmdd
      return stringValue((Date) input);
    }

    if (input instanceof Number) {
      DecimalFormat decimalFormat = new DecimalFormat(
          "###################.###############");
      return decimalFormat.format(((Number) input).doubleValue());

    }

    return input.toString();
  }

  /**
   * Convert a timestamp into a string: yyyy/MM/dd HH:mm:ss.SSS
   * @param timestamp
   * @return the string representation
   */
  public synchronized static String timestampToString(Date timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestampFormat.format(timestamp);
  }

  /**
   * get the timestamp format for this thread
   * if you call this make sure to synchronize on FastDateUtils.class
   * @return the timestamp format
   */
  synchronized static SimpleDateFormat dateFormat() {
    return dateFormat;
  }

  /**
   * convert a date to the standard string yyyymmdd
   * @param date 
   * @return the string value
   */
  public static String stringValue(java.util.Date date) {
    synchronized (GrouperUtil.class) {
      if (date == null) {
        return null;
      }
  
      String theString = dateFormat().format(date);
  
      return theString;
    }
  }

  /**
   * <pre>convert a string to timestamp based on the following formats:
   * yyyyMMdd
   * yyyy/MM/dd HH:mm:ss
   * yyyy/MM/dd HH:mm:ss.SSS
   * yyyy/MM/dd HH:mm:ss.SSSSSS
   * </pre>
   * @param input
   * @return the timestamp object
   */
  public static Timestamp stringToTimestamp(String input) {
    Date date = stringToTimestampHelper(input);
    if (date == null) {
      return null;
    }
    //maybe already a timestamp
    if (date instanceof Timestamp) {
      return (Timestamp)date; 
    }
    return new Timestamp(date.getTime());
  }

  /**
   * return a date based on input, null safe.  Allow any of the three 
   * formats:
   * yyyyMMdd
   * yyyy/MM/dd HH:mm:ss
   * yyyy/MM/dd HH:mm:ss.SSS
   * yyyy/MM/dd HH:mm:ss.SSSSSS
   * 
   * @param input
   * @return the millis, -1 for null
   */
  synchronized static Date stringToTimestampHelper(String input) {
    //trim and handle null and empty
    if (StringUtils.isBlank(input)) {
      return null;
    }
  
    try {
      //convert mainframe
      if (StringUtils.equals("99999999", input)
          || StringUtils.equals("999999", input)) {
        input = "20991231";
      }
      if (input.length() == 8) {
        
        return dateFormat().parse(input);
      }
      if (!StringUtils.contains(input, '.')) {
        if (StringUtils.contains(input, '/')) {
          return dateMinutesSecondsFormat.parse(input);
        }
        //else no slash
        return dateMinutesSecondsNoSlashFormat.parse(input);
      }
      if (StringUtils.contains(input, '/')) {
        //see if the period is 6 back
        int lastDotIndex = input.lastIndexOf('.');
        if (lastDotIndex == input.length() - 7) {
          String nonNanoInput = input.substring(0,input.length()-3);
          Date date = timestampFormat.parse(nonNanoInput);
          //get the last 3
          String lastThree = input.substring(input.length()-3,input.length());
          int lastThreeInt = Integer.parseInt(lastThree);
          Timestamp timestamp = new Timestamp(date.getTime());
          timestamp.setNanos(timestamp.getNanos() + (lastThreeInt * 1000));
          return timestamp;
        }
        return timestampFormat.parse(input);
      }
      //else no slash
      return timestampNoSlashFormat.parse(input);
    } catch (ParseException pe) {
      throw new RuntimeException(errorStart + input);
    }
  }

  /**
   * start of error parsing messages
   */
  private static final String errorStart = "Invalid timestamp, please use any of the formats: "
    + DATE_FORMAT + ", " + TIMESTAMP_FORMAT 
    + ", " + DATE_MINUTES_SECONDS_FORMAT + ": ";

  /**
   * Convert an object to a byte, allow nulls
   * @param input
   * @return the boolean object value
   */
  public static BigDecimal bigDecimalObjectValue(Object input) {
    if (input instanceof BigDecimal) {
      return (BigDecimal)input;
    }
    if (isBlank(input)) {
      return null;
    }
    return BigDecimal.valueOf(doubleValue(input));
  }

  /**
   * Convert an object to a byte, allow nulls
   * @param input
   * @return the boolean object value
   */
  public static Byte byteObjectValue(Object input) {
    if (input instanceof Byte) {
      return (Byte)input;
    }
    if (isBlank(input)) {
      return null;
    }
    return Byte.valueOf(byteValue(input));
  }

  /**
   * convert an object to a byte
   * @param input
   * @return the byte
   */
  public static byte byteValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Byte.parseByte(string);
    }
    if (input instanceof Number) {
      return ((Number)input).byteValue();
    }
    throw new RuntimeException("Cannot convert to byte: " + className(input));
  }

  /**
   * get the Double value of an object
   * 
   * @param input
   *          is a number or String
   * @param allowNullBlank used to default to false, if true, return null if nul inputted 
   * 
   * @return the Double equivalent
   */
  public static Double doubleObjectValue(Object input, boolean allowNullBlank) {
  
    if (input instanceof Double) {
      return (Double) input;
    } 
    
    if (allowNullBlank && isBlank(input)) {
      return null;
    }
    
    return Double.valueOf(doubleValue(input));
  }

  /**
   * get the double value of an object
   * 
   * @param input
   *          is a number or String
   * 
   * @return the double equivalent
   */
  public static double doubleValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Double.parseDouble(string);
    }
    if (input instanceof Number) {
      return ((Number)input).doubleValue();
    }
    throw new RuntimeException("Cannot convert to double: "  + className(input));
  }

  /**
   * get the double value of an object, do not throw an 
   * exception if there is an
   * error
   * 
   * @param input
   *          is a number or String
   * 
   * @return the double equivalent
   */
  public static double doubleValueNoError(Object input) {
    if (input == null || (input instanceof String 
        && StringUtils.isBlank((String)input))) {
      return NOT_FOUND;
    }
  
    try {
      return doubleValue(input);
    } catch (Exception e) {
      //no need to log here
    }
  
    return NOT_FOUND;
  }

  /**
   * get the Float value of an object
   * 
   * @param input
   *          is a number or String
   * @param allowNullBlank true if allow null or blank
   * 
   * @return the Float equivalent
   */
  public static Float floatObjectValue(Object input, boolean allowNullBlank) {
  
    if (input instanceof Float) {
      return (Float) input;
    } 
  
    if (allowNullBlank && isBlank(input)) {
      return null;
    }
    return Float.valueOf(floatValue(input));
  }

  /**
   * get the float value of an object
   * 
   * @param input
   *          is a number or String
   * 
   * @return the float equivalent
   */
  public static float floatValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Float.parseFloat(string);
    }
    if (input instanceof Number) {
      return ((Number)input).floatValue();
    }
    throw new RuntimeException("Cannot convert to float: " + className(input));
  }

  /**
   * get the float value of an object, do not throw an exception if there is an
   * error
   * 
   * @param input
   *          is a number or String
   * 
   * @return the float equivalent
   */
  public static float floatValueNoError(Object input) {
    if (input == null || (input instanceof String 
        && StringUtils.isBlank((String)input))) {
      return NOT_FOUND;
    }
    try {
      return floatValue(input);
    } catch (Exception e) {
      LOG.error(e);
    }
  
    return NOT_FOUND;
  }

  /**
   * get the Integer value of an object
   * 
   * @param input
   *          is a number or String
   * @param allowNullBlank true if convert null or blank to null
   * 
   * @return the Integer equivalent
   */
  public static Integer intObjectValue(Object input, boolean allowNullBlank) {
  
    if (input instanceof Integer) {
      return (Integer) input;
    } 
  
    if (allowNullBlank && isBlank(input)) {
      return null;
    }
    
    return Integer.valueOf(intValue(input));
  }

  /**
   * convert an object to a int
   * @param input
   * @return the number
   */
  public static int intValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Integer.parseInt(string);
    }
    if (input instanceof Number) {
      return ((Number)input).intValue();
    }
    if (false) {
      if (input == null) {
        return 0;
      }
      if (input instanceof String || StringUtils.isBlank((String)input)) {
        return 0;
      }
    }
    
    throw new RuntimeException("Cannot convert to int: " + className(input));
  }

  /**
   * convert an object to a int
   * @param input
   * @param valueIfNull is if the input is null or empty, return this value
   * @return the number
   */
  public static int intValue(Object input, int valueIfNull) {
    if (input == null || "".equals(input)) {
      return valueIfNull;
    }
    return intObjectValue(input, false);
  }

  /**
   * get the int value of an object, do not throw an exception if there is an
   * error
   * 
   * @param input
   *          is a number or String
   * 
   * @return the int equivalent
   */
  public static int intValueNoError(Object input) {
    if (input == null || (input instanceof String 
        && StringUtils.isBlank((String)input))) {
      return NOT_FOUND;
    }
    try {
      return intValue(input);
    } catch (Exception e) {
      //no need to log here
    }
  
    return NOT_FOUND;
  }

  /** special number when a number is not found */
  public static final int NOT_FOUND = -999999999;

  /**
   * logger 
   */
  private static final Log LOG = getLog(GrouperUtil.class);

  /**
   * The name says it all.
   */
  public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  /**
   * get the Long value of an object
   * 
   * @param input
   *          is a number or String
   * @param allowNullBlank true if null or blank converts to null
   * 
   * @return the Long equivalent
   */
  public static Long longObjectValue(Object input, boolean allowNullBlank) {
  
    if (input instanceof Long) {
      return (Long) input;
    } 
  
    if (allowNullBlank && isBlank(input)) {
      return null;
    } 
    
    return Long.valueOf(longValue(input));
  }

  /**
   * convert an object to a long
   * @param input
   * @return the number
   */
  public static long longValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Long.parseLong(string);
    }
    if (input instanceof Number) {
      return ((Number)input).longValue();
    }
    throw new RuntimeException("Cannot convert to long: " + className(input));
  }

  /**
   * convert an object to a long
   * @param input
   * @param valueIfNull is if the input is null or empty, return this value
   * @return the number
   */
  public static long longValue(Object input, long valueIfNull) {
    if (input == null || "".equals(input)) {
      return valueIfNull;
    }
    return longObjectValue(input, false);
  }

  /**
   * get the long value of an object, do not throw an exception if there is an
   * error
   * 
   * @param input
   *          is a number or String
   * 
   * @return the long equivalent
   */
  public static long longValueNoError(Object input) {
    if (input == null || (input instanceof String 
        && StringUtils.isBlank((String)input))) {
      return NOT_FOUND;
    }
    try {
      return longValue(input);
    } catch (Exception e) {
      //no need to log here
    }
  
    return NOT_FOUND;
  }

  /**
   * get the Short value of an object.  converts null or blank to null
   * 
   * @param input
   *          is a number or String
   * 
   * @return the Long equivalent
   */
  public static Short shortObjectValue(Object input) {
  
    if (input instanceof Short) {
      return (Short) input;
    }
  
    if (isBlank(input)) {
      return null;
    } 
    
    return Short.valueOf(shortValue(input));
  }

  /**
   * convert an object to a short
   * @param input
   * @return the number
   */
  public static short shortValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Short.parseShort(string);
    }
    if (input instanceof Number) {
      return ((Number)input).shortValue();
    }
    throw new RuntimeException("Cannot convert to short: " + className(input));
  }

  /**
   * get the Character wrapper value for the input
   * @param input allow null, return null
   * @return the Character object wrapper
   */
  public static Character charObjectValue(Object input) {
    if (input instanceof Character) {
      return (Character) input;
    }
    if (isBlank(input)) {
      return null;
    }
    return new Character(charValue(input));
  }

  /**
   * convert an object to a char
   * @param input
   * @return the number
   */
  public static char charValue(Object input) {
    if (input instanceof Character) {
      return ((Character) input).charValue();
    }
    //if string length 1, thats ok
    if (input instanceof String) {
      String inputString = (String) input;
      if (inputString.length() == 1) {
        return inputString.charAt(0);
      }
    }
    throw new RuntimeException("Cannot convert to char: "
        + (input == null ? null : (input.getClass() + ", " + input)));
  }

  /**
   * Create the parent directories for a file if they do not already exist
   * @param file
   */
  public static void createParentDirectories(File file) {
    if (!file.getParentFile().exists()) {
      if (!file.getParentFile().mkdirs()) {
        throw new RuntimeException("Could not create directory : " + file.getParentFile());
      }
    }
  }

  /**
   * save a string into a file, file does not have to exist
   * 
   * @param file
   *          is the file to save to
   * @param contents
   *          is the contents of the file
   */
  public static void saveStringIntoFile(File file, String contents) {
    try {
      writeStringToFile(file, contents, "ISO-8859-1");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * save a string into a file, file does not have to exist
   * 
   * @param file
   *          is the file to save to
   * @param contents
   *          is the contents of the file
   * @param onlyIfDifferentContents true if only saving due to different contents
   * @param ignoreWhitespace true to ignore whitespace
   * @return true if contents were saved (thus different if param set)
   */
  public static boolean saveStringIntoFile(File file, String contents, 
      boolean onlyIfDifferentContents, boolean ignoreWhitespace) {
    if (onlyIfDifferentContents && file.exists()) {
      String fileContents = readFileIntoString(file);
      String compressedContents = contents;
      if (ignoreWhitespace) {
        compressedContents = replaceWhitespaceWithSpace(compressedContents);
        fileContents = replaceWhitespaceWithSpace(fileContents);
      }
      
      //they are the same, dont worry about it
      if (StringUtils.equals(fileContents, compressedContents)) {
        return false;
      }
  
    }
    GrouperUtil.saveStringIntoFile(file, contents);
    return true;
  }

  /**
   * <p>
   * Writes data to a file. The file will be created if it does not exist.
   * </p>
   * <p>
   * There is no readFileToString method without encoding parameter because
   * the default encoding can differ between platforms and therefore results
   * in inconsistent results.
   * </p>
   *
   * @param file the file to write.
   * @param data The content to write to the file.
   * @param encoding encoding to use
   * @throws IOException in case of an I/O error
   * @throws UnsupportedEncodingException if the encoding is not supported
   *   by the VM
   */
  public static void writeStringToFile(File file, String data, String encoding)
      throws IOException {
    OutputStream out = new java.io.FileOutputStream(file);
    try {
      out.write(data.getBytes(encoding));
    } finally {
      closeQuietly(out);
    }
  }

  /**
   * @param file
   *          is the file to read into a string
   * 
   * @return String
   */
  public static String readFileIntoString(File file) {
  
    if (file == null) {
      return null;
    }
    try {
      return readFileToString(file, "ISO-8859-1");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * @param resourceName is the string resource from classpath to read (e.g. grouper.properties)
   * @param allowNull is true if its ok if the resource is null or if it is not found or blank or whatever.
   * 
   * @return String or null if allowed or RuntimeException if not allowed
   */
  public static String readResourceIntoString(String resourceName, boolean allowNull) {
    if (StringUtils.isBlank(resourceName)) {
      if (allowNull) {
        return null;
      }
      throw new RuntimeException("Resource name is blank");
    }
    URL url = computeUrl(resourceName, allowNull);

    //this is ok
    if (url == null && allowNull) {
      return null;
    }
    
    InputStream inputStream = null;
    StringWriter stringWriter = new StringWriter();
    try {
      inputStream = url.openStream();
      copy(inputStream, stringWriter, "ISO-8859-1");
    } catch (IOException ioe) {
      throw new RuntimeException("Error reading resource: '" + resourceName + "'", ioe);
    } finally {
      closeQuietly(inputStream);
      closeQuietly(stringWriter);
    }
    return stringWriter.toString();
  }

  /**
   * <p>
   * Reads the contents of a file into a String.
   * </p>
   * <p>
   * There is no readFileToString method without encoding parameter because
   * the default encoding can differ between platforms and therefore results
   * in inconsistent results.
   * </p>
   *
   * @param file the file to read.
   * @param encoding the encoding to use
   * @return The file contents or null if read failed.
   * @throws IOException in case of an I/O error
   */
  public static String readFileToString(File file, String encoding) throws IOException {
    InputStream in = new java.io.FileInputStream(file);
    try {
      return toString(in, encoding);
    } finally {
      closeQuietly(in);
    }
  }

  /**
   * replace all whitespace with space
   * @param input
   * @return the string
   */
  public static String replaceWhitespaceWithSpace(String input) {
    if (input == null) {
      return input;
    }
    return input.replaceAll("\\s+", " ");
  }

  /**
   * Unconditionally close an <code>InputStream</code>.
   * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
   * @param input A (possibly null) InputStream
   */
  public static void closeQuietly(InputStream input) {
    if (input == null) {
      return;
    }
  
    try {
      input.close();
    } catch (IOException ioe) {
    }
  }

  /**
   * Unconditionally close an <code>OutputStream</code>.
   * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
   * @param output A (possibly null) OutputStream
   */
  public static void closeQuietly(OutputStream output) {
    if (output == null) {
      return;
    }
  
    try {
      output.close();
    } catch (IOException ioe) {
    }
  }

  /**
   * Unconditionally close an <code>Reader</code>.
   * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
   *
   * @param input A (possibly null) Reader
   */
  public static void closeQuietly(Reader input) {
    if (input == null) {
      return;
    }
  
    try {
      input.close();
    } catch (IOException ioe) {
    }
  }

  /**
   * close a writer quietly
   * @param writer
   */
  public static void closeQuietly(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        //swallow, its ok
      }
    }
  }

  /**
   * Get the contents of an <code>InputStream</code> as a String.
   * @param input the <code>InputStream</code> to read from
   * @param encoding The name of a supported character encoding. See the
   *   <a href="http://www.iana.org/assignments/character-sets">IANA
   *   Charset Registry</a> for a list of valid encoding types.
   * @return the requested <code>String</code>
   * @throws IOException In case of an I/O problem
   */
  public static String toString(InputStream input, String encoding) throws IOException {
    StringWriter sw = new StringWriter();
    copy(input, sw, encoding);
    return sw.toString();
  }

  /**
   * Copy and convert bytes from an <code>InputStream</code> to chars on a
   * <code>Writer</code>, using the specified encoding.
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>Writer</code> to write to
   * @param encoding The name of a supported character encoding. See the
   * <a href="http://www.iana.org/assignments/character-sets">IANA
   * Charset Registry</a> for a list of valid encoding types.
   * @throws IOException In case of an I/O problem
   */
  public static void copy(InputStream input, Writer output, String encoding)
      throws IOException {
    InputStreamReader in = new InputStreamReader(input, encoding);
    copy(in, output);
  }

  /**
   * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
   * @param input the <code>Reader</code> to read from
   * @param output the <code>Writer</code> to write to
   * @return the number of characters copied
   * @throws IOException In case of an I/O problem
   */
  public static int copy(Reader input, Writer output) throws IOException {
    char[] buffer = new char[DEFAULT_BUFFER_SIZE];
    int count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * this method takes a long (less than 62) and converts it to a 1 character
   * string (a-z, A-Z, 0-9)
   * 
   * @param theLong
   *          is the long (less than 62) to convert to a 1 character string
   * 
   * @return a one character string
   */
  public static String convertLongToChar(long theLong) {
    if ((theLong < 0) || (theLong >= 62)) {
      throw new RuntimeException("StringUtils.convertLongToChar() "
          + " invalid input (not >=0 && <62: " + theLong);
    } else if (theLong < 26) {
      return "" + (char) ('a' + theLong);
    } else if (theLong < 52) {
      return "" + (char) ('A' + (theLong - 26));
    } else {
      return "" + (char) ('0' + (theLong - 52));
    }
  }

  /**
   * this method takes a long (less than 36) and converts it to a 1 character
   * string (A-Z, 0-9)
   * 
   * @param theLong
   *          is the long (less than 36) to convert to a 1 character string
   * 
   * @return a one character string
   */
  public static String convertLongToCharSmall(long theLong) {
    if ((theLong < 0) || (theLong >= 36)) {
      throw new RuntimeException("StringUtils.convertLongToCharSmall() "
          + " invalid input (not >=0 && <36: " + theLong);
    } else if (theLong < 26) {
      return "" + (char) ('A' + theLong);
    } else {
      return "" + (char) ('0' + (theLong - 26));
    }
  }

  /**
   * convert a long to a string by converting it to base 62 (26 lower, 26 upper,
   * 10 digits)
   * 
   * @param theLong
   *          is the long to convert
   * 
   * @return the String conversion of this
   */
  public static String convertLongToString(long theLong) {
    long quotient = theLong / 62;
    long remainder = theLong % 62;
  
    if (quotient == 0) {
      return convertLongToChar(remainder);
    }
    StringBuffer result = new StringBuffer();
    result.append(convertLongToString(quotient));
    result.append(convertLongToChar(remainder));
  
    return result.toString();
  }

  /**
   * convert a long to a string by converting it to base 36 (26 upper, 10
   * digits)
   * 
   * @param theLong
   *          is the long to convert
   * 
   * @return the String conversion of this
   */
  public static String convertLongToStringSmall(long theLong) {
    long quotient = theLong / 36;
    long remainder = theLong % 36;
  
    if (quotient == 0) {
      return convertLongToCharSmall(remainder);
    }
    StringBuffer result = new StringBuffer();
    result.append(convertLongToStringSmall(quotient));
    result.append(convertLongToCharSmall(remainder));
  
    return result.toString();
  }

  /**
   * increment a character (A-Z then 0-9)
   * 
   * @param theChar
   * 
   * @return the value
   */
  public static char incrementChar(char theChar) {
    if (theChar == 'Z') {
      return '0';
    }
  
    if (theChar == '9') {
      return 'A';
    }
  
    return ++theChar;
  }

  /**
   * Increment a string with A-Z and 0-9 (no lower case so case insensitive apps
   * like windows IE will still work)
   * 
   * @param string
   * 
   * @return the value
   */
  public static char[] incrementStringInt(char[] string) {
    if (string == null) {
      return string;
    }
  
    //loop through the string backwards
    int i = 0;
  
    for (i = string.length - 1; i >= 0; i--) {
      char inc = string[i];
      inc = incrementChar(inc);
      string[i] = inc;
  
      if (inc != 'A') {
        break;
      }
    }
  
    //if we are at 0, then it means we hit AAAAAAA (or more)
    if (i < 0) {
      return ("A" + new String(string)).toCharArray();
    }
  
    return string;
  }

  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @return the properties
   */
  public synchronized static Properties propertiesFromResourceName(String resourceName) {
    return propertiesFromResourceName(resourceName, true, true);
  }

  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @param useCache 
   * @param exceptionIfNotExist 
   * @return the properties or null if not exist
   */
  public synchronized static Properties propertiesFromResourceName(String resourceName, boolean useCache, 
      boolean exceptionIfNotExist) {

    Properties properties = resourcePropertiesCache.get(resourceName);
    
    if (!useCache || !resourcePropertiesCache.containsKey(resourceName)) {
  
      properties = new Properties();

      URL url = computeUrl(resourceName, true);
      InputStream inputStream = null;
      try {
        inputStream = url.openStream();
        properties.load(inputStream);
      } catch (Exception e) {
        if (exceptionIfNotExist) {
          throw new RuntimeException("Problem with resource: '" + resourceName + "'", e);
        }
        properties = null;
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }
      if (useCache) {
        resourcePropertiesCache.put(resourceName, properties);
      }
    }
    //Hack; Gary 7th Nov 2008
    fixHibernateConnectionUrl(properties);
    
    return properties;
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
      boolean exceptionOnNotFound) throws RuntimeException {
    
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (E e : theEnumClass.getEnumConstants()) {
      if (StringUtils.equalsIgnoreCase(string, e.name())) {
        return e;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find " + theEnumClass.getSimpleName() + " from string: '").append(string);
    error.append("', expecting one of: ");
    for (E e : theEnumClass.getEnumConstants()) {
      error.append(e.name()).append(", ");
    }
    throw new RuntimeException(error.toString());
  
  }

  /**
   * if there is a valid accessible property descriptor, get it
   * @param object
   * @param property
   * @return the property descriptor
   */
  public static PropertyDescriptor retrievePropertyDescriptor(Object object, String property) {
    try {
      return PropertyUtils.getPropertyDescriptor(object, property);
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * this assumes the property exists, and is a simple property
   * @param object
   * @param property
   * @return the value
   */
  public static Object propertyValue(Object object, String property)  {
    Method getter = getter(object.getClass(), property, true, true);
    Object result = invokeMethod(getter, object);
    return result;
  }

  /**
   * get a value (trimmed to e) from a property file
   * @param properties
   * @param key
   * @return the property value
   */
  public static String propertiesValue(Properties properties, String key) {
    return propertiesValue(properties, null, key);
  }
  
  /**
   * get a value (trimmed to e) from a property file
   * @param properties
   * @param overrideMap for testing, to override some properties values
   * @param key
   * @return the property value
   */
  public static String propertiesValue(Properties properties, Map<String, String> overrideMap, String key) {
    String value = overrideMap == null ? null : overrideMap.get(key);
    if (StringUtils.isBlank(value)) {
      value = properties.getProperty(key);
    }
    return StringUtils.trim(value);
  }
  
  /**
   * get a boolean property, or the default if cant find
   * @param properties
   * @param propertyName
   * @param defaultValue 
   * @return the boolean
   */
  public static boolean propertiesValueBoolean(Properties properties,
      String propertyName, boolean defaultValue) {
    return propertiesValueBoolean(properties, null, propertyName, defaultValue);
  }
  
  /**
   * get a boolean property, or the default if cant find
   * @param properties
   * @param overrideMap for testing to override properties
   * @param propertyName
   * @param defaultValue 
   * @return the boolean
   */
  public static boolean propertiesValueBoolean(Properties properties, 
      Map<String, String> overrideMap, String propertyName, boolean defaultValue) {
    
      
    String value = propertiesValue(properties, overrideMap, propertyName);
    if (StringUtils.isBlank(value)) {
      return defaultValue;
    }
    
    if ("true".equalsIgnoreCase(value)) {
      return true;
    }
    if ("false".equalsIgnoreCase(value)) {
      return false;
    }
    if ("t".equalsIgnoreCase(value)) {
      return true;
    }
    if ("f".equalsIgnoreCase(value)) {
      return false;
    }
    throw new RuntimeException("Invalid value: '" + value + "' for property: " + propertyName + " in grouper.properties");

  }
  
  /**
   * close a connection null safe and dont throw exception
   * @param connection
   */
  public static void closeQuietly(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * close a session null safe and dont throw exception
   * @param session
   */
  public static void closeQuietly(Session session) {
    if (session != null) {
      try {
        session.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * close a statement null safe and dont throw exception
   * @param statement
   */
  public static void closeQuietly(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * close a resultSet null safe and dont throw exception
   * @param resultSet
   */
  public static void closeQuietly(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /** cache the hostname, it wont change */
  private static String hostname = null;

  /**
   * get the hostname of this machine
   * @return the hostname
   */
  public static String hostname() {
  
    if (StringUtils.isBlank(hostname)) {
  
      //get the hostname
      hostname = "unknown";
      try {
        InetAddress addr = InetAddress.getLocalHost();
  
        // Get hostname
        hostname = addr.getHostName();
      } catch (Exception e) {
        LOG.error("Cant find servers hostname: ", e);
      }
    }
  
    return hostname;
  }

  /**
   * is ascii char
   * @param input
   * @return true if ascii
   */
  public static boolean isAscii(char input) {
    return input < 128;
  }

  /**
   * find the length of ascii chars (non ascii are counted as two)
   * @param input
   * @return the length of ascii chars
   */
  public static int lengthAscii(String input) {
    if (input == null) {
      return 0;
    }
    //see what real length is
    int utfLength = input.length();
    //count how many non asciis
    int extras = 0;
    for (int i=0;i<utfLength;i++) {
      //keep count of non ascii chars
      if (!isAscii(input.charAt(i))) {
        extras++;
      }
    }
    return utfLength + extras;
  }

  /**
   * rollback a transaction quietly
   * @param transaction
   */
  public static void rollbackQuietly(Transaction transaction) {
    if (transaction != null && transaction.isActive()) {
      try {
        transaction.rollback();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * rollback a connection quietly
   * @param connection
   */
  public static void rollbackQuietly(Connection connection) {
    if (connection != null) {
      try {
        connection.rollback();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * find the length of ascii chars (non ascii are counted as two)
   * @param input is the string to operate on
   * @param requiredLength length we need the string to be
   * @return the length of ascii chars
   */
  public static String truncateAscii(String input, int requiredLength) {
    if (input == null) {
      return input;
    }
    //see what real length is
    int utfLength = input.length();
    
    //see if not worth checking
    if (utfLength * 2 < requiredLength) {
      return input;
    }
    
    //count how many non asciis
    int asciiLength = 0;
    for (int i=0;i<utfLength;i++) {
      
      asciiLength++;
      
      //keep count of non ascii chars
      if (!isAscii(input.charAt(i))) {
        asciiLength++;
      }
      
      //see if we are over 
      if (asciiLength > requiredLength) {
        //do not include the current char
        return input.substring(0,i);
      }
    }
    //must have fit
    return input;
  }

  /**
   * convert a subject to string safely
   * @param subject
   * @return the string value of subject (might be null)
   */
  public static String subjectToString(Subject subject) {
    if (subject == null) {
      return null;
    }
    try {
      return "Subject id: " + subject.getId() + ", sourceId: " + subject.getSource().getId();
    } catch (RuntimeException e) {
      //might be subject not found if lazy subject
      return subject.toString();
    }
  }

  /**
   * if the input is a file, read string from file.  if not, or if disabled from grouper.properties, return the input
   * @param in
   * @return the result
   */
  public static String readFromFileIfFile(String in) {
    //convert both slashes to file slashes
    if (File.separatorChar == '/') {
      in = StringUtils.replace(in, "\\", "/");
    } else {
      in = StringUtils.replace(in, "/", "\\");
    }
    
    //see if it is a file reference
    if (in.indexOf(File.separatorChar) != -1 && !GrouperConfig.getPropertyBoolean("grouper.encrypt.disableExternalFileLookup", false)) {
      //read the contents of the file into a string
      in = readFileIntoString(new File(in));
    }
    return in;
  
  }

  /**
   * Create directories, throw exception if not possible.
   * This is will be ok if the directory already exists (will not delete it)
   * @param dir
   */
  public static void mkdirs(File dir) {
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new RuntimeException("Could not create directory : " + dir.getParentFile());
      }
      return;
    }
    if (!dir.isDirectory()) {
      throw new RuntimeException("Should be a directory but is not: " + dir);
    }
  }
  
  /**
   * 
   * @param inPath
   * @return string
   */
  public static String fixRelativePath(String inPath) {
	  if(grouperHome==null || inPath.matches("^(/|\\\\|\\w:).*")) {
		  return inPath;
	  }
	  String sep = "";
	  if(!grouperHome.matches(".*?(\\\\|/)$")) {
		  sep = File.separator;
	  }
	  
	  String outPath=grouperHome + sep + inPath;
	  
	  return outPath;
  }
  
  /**
   * 
   * @param props
   */
  private static void fixHibernateConnectionUrl(Properties props) {
	  String url = props.getProperty("hibernate.connection.url");
	  if(StringUtils.isBlank(url)) {
		  return;
	  }
	  if(url.matches("^jdbc:hsqldb:(mem|hsql):.*")) {
		  return;
	  }
	  int spliceAt=12;
	  if(url.startsWith("jdbc:hsqldb:file:")) {
		  spliceAt=17;
	  }
	  String file = url.substring(spliceAt);
	  String newUrl = url.substring(0,spliceAt) + fixRelativePath(file);
	  props.setProperty("hibernate.connection.url", newUrl);
  }

  

}
