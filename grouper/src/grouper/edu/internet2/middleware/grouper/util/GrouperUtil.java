/**
 * 
 */
package edu.internet2.middleware.grouper.util;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
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
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.util.proxy.ProxyObject;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.exception.Nestable;
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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.ExpressionLanguageMissingVariableException;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCloneable;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.util.ExpirableCache;


/**
 * utility methods for grouper.  Generally these are static methods.
 * 
 * @author mchyzer
 *
 */
public class GrouperUtil {

  /** override map for properties in thread local to be used in a web server or the like */
  private static ThreadLocal<Map<String, Map<String, String>>> propertiesThreadLocalOverrideMap = new ThreadLocal<Map<String, Map<String, String>>>();

  /**
   * take email addresses from a textarea and turn them into semi separated
   * @param emailAddresses can be whitespace, comma, or semi separated
   * @return the email addresses semi separated
   */
  public static String normalizeEmailAddresses(String emailAddresses) {
    if (emailAddresses == null) {
      return null;
    }
    emailAddresses = StringUtils.replace(emailAddresses, ",", " ");
    emailAddresses = StringUtils.replace(emailAddresses, ";", " ");
    emailAddresses = StringUtils.replace(emailAddresses, "\n", " ");
    emailAddresses = StringUtils.replace(emailAddresses, "\t", " ");
    emailAddresses = StringUtils.replace(emailAddresses, "\r", " ");
    emailAddresses = GrouperUtil.join(GrouperUtil.splitTrim(emailAddresses, " "), ";");
    return emailAddresses;
  }
  
  /** 
   * pattern as simple validation for email.  need text, @ sign, then text, dot, and text.
   * granted this could be better, but this is a first step
   */
  private static Pattern emailPattern = Pattern.compile("^[^@]+@[^.]+\\..+$");
  
  /**
   * 
   * @param email
   * @return true if valid, false if not
   */
  public static boolean validEmail(String email) {
    Matcher matcher = emailPattern.matcher(email);
    return matcher.matches();
  }
  
  /**
   * see if a subject has an attribute
   * @param subject
   * @param attributeName
   * @return true if the subject has an attribute
   */
  public static boolean subjectHasAttribute(Subject subject, String attributeName) {
    if (subject == null) {
      return false;
    }
    String attributeValue = subject.getAttributeValue(attributeName);
    return !isBlank(attributeValue);
  }
  
  /**
   * see if a sql like string matches a real string.
   * e.g. if the input is a:b:%, and the input is: a:b:test:that, then it returns true
   * @param sqlMatcher
   * @param testText
   * @return true if matches, false if not
   */
  public static boolean matchSqlString(String sqlMatcher, String testText) {
    
    //first do slashes
    String regexString = StringUtils.replace(sqlMatcher, "\\", "\\\\");
    
    //then do everything else
    regexString = replace(regexString, 
        new String[]{"$",   "^",   "*",   "(",   ")",   "+",   "[",   "{",   "]",   "}",   "|",   "\"", ".",   "?"},
        new String[]{"\\$", "\\^", "\\*", "\\(", "\\)", "\\+", "\\[", "\\{", "\\]", "\\}", "\\|", "\"", "\\.", "\\?", }); 
    
    //then do the underscores and percents
    regexString = StringUtils.replace(regexString, "_", ".");
    regexString = "^" + StringUtils.replace(regexString, "%", ".*") + "$";
    
    Pattern pattern = Pattern.compile(regexString, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(testText);
    return matcher.matches();
  }
  
  /**
   * shorten a set if it is too long
   * @param <T>
   * @param theSet
   * @param maxSize
   * @return the new set
   */
  public static <T> Set<T> setShorten(Set<T> theSet, int maxSize) {
    
    if (length(theSet) < maxSize) {
      return theSet;
    }
    
    //truncate the list
    Set<T> newList = new LinkedHashSet<T>();
    int i=0;

    //TODO test this logic
    for (T t : theSet) {
      
      if (i>=maxSize) {
        break;
      }
      
      newList.add(t);
      i++;
    }
    return newList;
  }
  
  /**
   * 
   * @param number e.g. 12345678
   * @return the string, e.g. 12,345,678
   */
  public static String formatNumberWithCommas(Long number) {
    if (number == null) {
      return "null";
    }
    DecimalFormat df = new DecimalFormat();
    return df.format(number);
  }
  
  /**
   * compare null safe
   * @param first
   * @param second
   * @return 0 for equal, 1 for greater, -1 for less
   */
  public static int compare(Comparable first, Comparable second) {
    if (first == second) {
      return 0;
    }
    if (first == null) {
      return -1;
    }
    if (second == null) {
      return 1;
    }
    return first.compareTo(second);
  }
  
  /**
   * e.g. ('g:gsa', 'jdbc')
   * @param sources is comma separated source id's
   * @return the set of sources
   */
  public static Set<Source> convertSources(String sources) {
    if (StringUtils.isBlank(sources)) {
      return null;
    }
    String[] sourceStrings = splitTrim(sources, ",");
        
    Set<Source> sourceSet = new HashSet<Source>();
    for (String source : sourceStrings) {
      sourceSet.add(SourceManager.getInstance().getSource(source));
    }
    
    return sourceSet;
  }
  

  /**
   * e.g. ['g:gsa', 'jdbc']
   * @param sourceIds is an array of source ids
   * @return the set of sources
   */
  public static Set<Source> convertSources(String[] sourceIds) {
    if (GrouperUtil.length(sourceIds) == 0) {
      return null;
    }
    Set<Source> sourceSet = new HashSet<Source>();
    for (String source : sourceIds) {
      if (!StringUtils.isBlank(source)) {
        sourceSet.add(SourceManager.getInstance().getSource(source));
      }
    }
    
    return sourceSet;
  }
  
  /**
   * e.g. ('g:gsa', 'jdbc')
   * @param sources
   * @return the in string, of sources sorted alphabetically
   * @deprecated moved to @See HibUtils
   */
  @Deprecated
  public static String convertSourcesToSqlInString(Set<Source> sources) {
    return HibUtils.convertSourcesToSqlInString(sources);
  }
  
  /**
   * turn some strings into a map
   * @param strings
   * @return the map (never null)
   */
  public static Map<String, String> toMap(String... strings) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    if (strings != null) {
      if (strings.length % 2 != 0) {
        throw new RuntimeException("Must pass in an even number of strings: " + strings.length);
      }
      for (int i=0;i<strings.length;i+=2) {
        map.put(strings[i], strings[i+1]);
      }
    }
    return map;
  }
  
  /**
   * turn some strings into a map
   * @param stringObjects is an array of String,Object,String,Object etc where the 
   * Strings are the key, and the Object is the value
   * @return the map (never null)
   */
  public static Map<String, Object> toStringObjectMap(Object... stringObjects) {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    if (stringObjects != null) {
      if (stringObjects.length % 2 != 0) {
        throw new RuntimeException("Must pass in an even number of strings: " + stringObjects.length);
      }
      for (int i=0;i<stringObjects.length;i+=2) {
        String key = (String)stringObjects[i];
        map.put(key, stringObjects[i+1]);
      }
    }
    return map;
  }
  
  /**
   * convert millis to friendly string
   * @param duration
   * @return the friendly string
   */
  public static String convertMillisToFriendlyString(Integer duration) {
    if (duration == null) {
      return convertMillisToFriendlyString((Long)null);
    }
    return convertMillisToFriendlyString(new Long(duration.intValue()));
  }
  
  /**
   * convert millis to friendly string
   * @param duration
   * @return the friendly string
   */
  public static String convertMillisToFriendlyString(Long duration) {
    
    if (duration == null) {
      return "";
    }
    
    if (duration < 1000) {
      return duration + "ms";
    }
    
    long ms = duration % 1000;
    duration = duration / 1000;
    long s = duration % 60;
    duration = duration / 60;
    
    if (duration == 0) {
      return s + "s, " + ms + "ms";
    }
    
    long m = duration % 60;
    duration = duration / 60;
    
    if (duration == 0) {
      return m + "m, " + s + "s, " + ms + "ms";
    }
    
    long h = duration % 24;
    duration = duration / 24;

    if (duration == 0) {
      return h + "h, " + m + "m, " + s + "s, " + ms + "ms";
    }
    
    long d = duration;
    
    return d + "d, " + h + "h, " + m + "m, " + s + "s, " + ms + "ms";
  }
  
  /**
   * Delete a file, throw exception if cannot
   * @param file
   */
  public static void deleteFile(File file) {
    //delete and create
    if (file.exists()) {
      if (!file.delete()) {
        throw new RuntimeException("Couldnt delete file: " + file.toString());
      }
    }
  }


  /**
   * return the arg after the argBefore, or null if not there, or exception
   * if argBefore is not found
   * @param args
   * @param argBefore
   * @return the arg
   */
  public static String argAfter(String[] args, String argBefore) {
    if (length(args) <= 1) {
      return null;
    }
    int argBeforeIndex = -1;
    for (int i=0;i<args.length;i++) {
      if (equals(args[i], argBefore)) {
        argBeforeIndex = i;
        break;
      }
    }
    if (argBeforeIndex == -1) {
      throw new RuntimeException("Cant find arg before");
    }
    if (argBeforeIndex < args.length - 1) {
      return args[argBeforeIndex + 1];
    }
    return null;
  }
  
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
   * Grouper home dir
   */
  static String grouperHome;
  
  static {
    
    String theGrouperHome = System.getProperty("grouper.home");
    if (isBlank(theGrouperHome)) {
      grouperHome = new File("").getAbsolutePath();
    } else {
      grouperHome = theGrouperHome;
    }
  }
  

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
  public static void logDirsCreateIfNotDone() {
    if (logDirsCreated) {
      return;
    }
    logDirsCreated = true;
    
    String location = "log4j.properties";
    Properties properties = propertiesFromResourceName(location);
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
            if (!grouperHome.endsWith("/") && !grouperHome.endsWith("\\")) {
              fileName = grouperHome + File.separator + fileName.substring(15);
            } else {
              fileName = grouperHome + fileName.substring(15);
            }
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
          System.err.println(LOG_ERROR);
          re.printStackTrace();
          throw new RuntimeException(LOG_ERROR, re);
        }
      }
    }
  }
  
  /**
   * find a next exception in the stack and log it
   * @param log logger
   * @param throwable exception to look for next exceptions in
   * @param timeToLive so we dont loop forever
   */
  public static void logErrorNextException(Log log, Throwable throwable, int timeToLive) {
    if (throwable == null) {
      return;
    }
    if (timeToLive < 0) {
      throw new RuntimeException("TimeToLive less than 0", throwable);
    }
    //this is only applicable to sql exceptions
    if (throwable instanceof SQLException) {
      SQLException sqlException = (SQLException)throwable;
      SQLException nextException = sqlException.getNextException();
      if (nextException != null) {
        log.error("Next exception", nextException);
        //maybe there are nested next exceptions....
        logErrorNextException(log, nextException, timeToLive-1);
      }
    }
    //recurse to find the next exception
    Throwable cause = throwable.getCause();
    if (cause != null) {
      logErrorNextException(log, cause, timeToLive-1);
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
      throw new RuntimeException("Problem with file: " + file.getAbsolutePath(), ioe);
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
          if (isBlank(path)) {
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
        if (equals(firstValue, secondValue)) {
          continue;
        }
      }
      differences.add(isNotBlank(prefix) ? (K)(prefix + key) : key);
    }
    //add the ones left over in the second map which are not in the first map
    for (K key : second.keySet()) {
      differences.add(isNotBlank(prefix) ? (K)(prefix + key) : key);
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
   * 
   * @param seconds
   */
  public static void sleepWithStdoutCountdown(int seconds) {
    for (int i=seconds;i>0;i--) {
      System.out.println("Sleeping: " + i);
      sleep(1000);
    }
  }
  
  /**
   * encrypt a message to SHA
   * @param plaintext
   * @return the hash
   */
  public synchronized static String encryptSha(String plaintext) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA"); //step 2
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    try {
      md.update(plaintext.getBytes("UTF-8")); //step 3
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
  }
    byte raw[] = md.digest(); //step 4
    byte[] encoded = Base64.encodeBase64(raw); //step 5
    String hash = new String(encoded);
    //String hash = (new BASE64Encoder()).encode(raw); //step 5
    return hash; //step 6
  }
  
  /**
   * If we can, inject this into the exception, else return false
   * @param t
   * @param message
   * @return true if success, false if not
   */
  public static boolean injectInException(Throwable t, String message) {
    
    String throwableFieldName = GrouperConfig.getProperty("throwable.data.field.name");
    
    if (isBlank(throwableFieldName)) {
      //this is the field for sun java 1.5
      throwableFieldName = "detailMessage";
    }
    try {
      String currentValue = t.getMessage();
      if (!isBlank(currentValue)) {
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
    Properties grouperProperties = propertiesFromResourceName("grouper.properties");

    //check blacklist
    //db.change.deny.user.0=
    //db.change.deny.url.0=
    //db.change.allow.user.0=grouper
    //db.change.allow.url.0=jdbc:mysql://localhost:3306/grouper

    int index = 0;
    String typeString = whitelist ? "allow" : "deny";
    while (true) {
      String currentUser = trim(grouperProperties.getProperty(
          "db.change." + typeString + ".user." + index));
      String currentUrl = trim(grouperProperties.getProperty(
          "db.change." + typeString + ".url." + index));
      
      //if we are done checking
      if (isBlank(currentUser) || isBlank(currentUrl)) {
        break;
      }
      if (equals(currentUser, user) && equals(currentUrl, url)) {
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
    
    Properties grouperHibernateProperties = propertiesFromResourceName("grouper.hibernate.properties");
    
    String url = trim(grouperHibernateProperties.getProperty("hibernate.connection.url"));
    String user = trim(grouperHibernateProperties.getProperty("hibernate.connection.username"));
    
    promptUserAboutChanges(reason, checkResponse, "db", url, user);
  }
  
  /**
   * prompt the user about db changes
   * @param reason e.g. delete all tables
   * @param checkResponse true if the response from the user should be checked, or just display the prompt
   * @param dbType should be db or ldap
   * @param url to check for
   * @param user user for db
   */
  public static void promptUserAboutChanges(String reason, boolean checkResponse, String dbType, String url, String user) {
    
    MultiKey cacheKey = stopPromptingUser ? new MultiKey(url, user) : new MultiKey(reason, url, user);
    
    //if already ok'ed this question in the jre instance, then we are all set
    if (dbChangeWhitelist.contains(cacheKey)) {
      //maybe stop due to testing and at least one
      if (stopPromptingUser) {
        String message = dbType + " prompting has been disabled (e.g. due to testing), so this user '"
            + user + "' and url '" + url + "' are allowed for: " + reason;
        if (!stopPromptingUserPrintlns.contains(message)) { 
          System.out.println(message);
        }
        stopPromptingUserPrintlns.add(message);
        return;
      }
      return;
    }

    
    //this might be set from junit ant task
    String allow = System.getProperty("grouper.allow.db.changes");
    if (equals("true", allow)) {
      System.out.println("System property grouper.allow.db.changes is true which allows " + dbType + " changes to user '" 
          + user + "' and url '" + url + "'");
      //all good, add to cache so we dont have to repeatedly tell user
      dbChangeWhitelist.add(cacheKey);
      return;
    }

    //check blacklist
    if (findGrouperPropertiesDbMatch(false, user, url)) {
      System.out.println("This " + dbType + " user '" + user + "' and url '" + url + "' are denied to be " +
          "changed in the grouper.properties");
      System.exit(1);

    }
    
    //check whitelist
    if (findGrouperPropertiesDbMatch(true, user, url)) {
      System.out.println("This " + dbType + " user '" + user + "' and url '" + url + "' are allowed to be " +
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
        System.out.println("(note, you can whitelist or blacklist " + dbType + " urls and users in the grouper.properties)");
        //note the following must be println and not just print so it will show up in ant
        String prompt = "Are you sure you want to " + reason + " in " + dbType + " user '" + user + "', " + dbType + " url '" + url + "'? (y|n): ";
        System.out.println(prompt);
        System.out.flush(); // empties buffer, before you input text
        if (!checkResponse) {
          return;
        }
        //we want to read until we dont get empty, and until we get a y or an n
        for (int i=0;i<10;i++) {
          message = stdin.readLine();
          message = trimToEmpty(message);
          if (!isEmpty(message)) {
            if (equalsIgnoreCase(message, "y") || equalsIgnoreCase(message, "n")) {
              break;
            }
            System.out.println("Didn't receive 'y' or 'n', received '" + message + "'...");       
            System.out.println(prompt);
            System.out.flush(); // empties buffer, before you input text
          }
        }
        if (!equalsIgnoreCase(message, "y") && !equalsIgnoreCase(message, "n")) {
          System.out.println("Sorry you are having trouble, try the whitelist in grouper.properties");
          System.exit(1);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      //CH: 20080506: Maybe we shouldnt close stdin... wont be able to use again?
      //} finally {
      //  closeQuietly(stdin);
      }
      if (!equalsIgnoreCase(message, "y")) {
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
    try {
      String fileName = URLDecoder.decode(url.getFile(), "UTF-8");

      File configFile = new File(fileName);

    return configFile;
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
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
   * @param <T>
   * @param array
   * @param theClass to make array from
   * @return the list or empty list if null
   */
  @SuppressWarnings({ "unchecked", "cast" })
  public static <T> T[] nonNull(T[] array, Class<?> theClass) {
    if (int.class.equals(theClass)) {
      return (T[])(Object)new int[0];
    }
    if (float.class.equals(theClass)) {
      return (T[])(Object)new float[0];
    }
    if (double.class.equals(theClass)) {
      return (T[])(Object)new double[0];
    }
    if (short.class.equals(theClass)) {
      return (T[])(Object)new short[0];
    }
    if (long.class.equals(theClass)) {
      return (T[])(Object)new long[0];
    }
    if (byte.class.equals(theClass)) {
      return (T[])(Object)new byte[0];
    }
    if (boolean.class.equals(theClass)) {
      return (T[])(Object)new boolean[0];
    }
    if (char.class.equals(theClass)) {
      return (T[])(Object)new char[0];
    }
    return array == null ? ((T[])Array.newInstance(theClass, 0)) : array;
  }
  
  /**
   * strip the suffix off
   * @param string
   * @param suffix
   * @return the string without the suffix
   */
  public static String stripSuffix(String string, String suffix) {
    if (string == null || suffix == null) {
      return string;
    }
    if (string.endsWith(suffix)) {
      return string.substring(0, string.length() - suffix.length());
    }
    return string;
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
    
    return "{\"" + object.getClass().getSimpleName() + "\":" + json + "}";
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
  /**
   * get the extension from name.  if name is a:b:c, name is c
   * @param name
   * @return the name
   */
  public static String extensionFromName(String name) {
    if (isBlank(name)) {
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
    return parentStemNameFromName(name, true);
  }
  /**
   * get the parent stem name from name.  if already a root stem
   * then just return null.  e.g. if the name is a:b:c then
   * the return value is a:b
   * @param name
   * @param nullForRoot null for root, otherwise colon
   * @return the parent stem name or null if none
   */
  public static String parentStemNameFromName(String name, boolean nullForRoot) {
    
    //null safe
    if (GrouperUtil.isBlank(name)) {
      return name;
    }
    
    int lastColonIndex = name.lastIndexOf(':');
    if (lastColonIndex == -1) {
      
      if (nullForRoot) { 
      return null;
      }
      return ":";
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
    return isBlank(string) ? defaultStringIfBlank : string;
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
            i++;
          }
        }
      } else {
        result.append(object.toString());
      }
    } catch (Exception e) {
      result.append("<<exception>> ").append(object.getClass()).append(":\n")
        .append(getFullStackTrace(e)).append("\n");
    }
  }

  /**
   * print out an object by fields
   * @param object
   * @param fieldNames
   * @return the string representation or null if null
   */
  public static String toStringFields(Object object, Set<String> fieldNames) {
    
    if (object == null) {
      return null;
    }
    
    StringBuilder result = new StringBuilder(object.getClass().getSimpleName() + ": ");
    
    //loop through fields
    for (String fieldName : nonNull(fieldNames)) {
      
      Object value = fieldValue(object, fieldName);
      if (value != null) {
        result.append(fieldName).append(": '").append(value).append("', ");
      }
      
    }
    
    //take off last comma (assume there was at least one field)
    result.delete(result.length()-2, result.length());
    return result.toString();
    
  }
  
  /**
   * convert a set to a string (comma separate)
   * @param collection
   * @return the String
   */
  public static String collectionToString(Collection collection) {
    if (collection == null) {
      return "null";
    }
    if (collection.size() == 0) {
      return "empty";
    }
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Object object : collection) {
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
   * @param set
   * @return the String
   */
  public static String setToString(Set set) {
    return collectionToString(set);
  }
  
  /**
   * convert a set to a string (comma separate)
   * @param map
   * @return the String
   * @deprecated use mapToString(map)
   */
  @Deprecated
  public static String MapToString(Map map) {
    return mapToString(map);
  }

  /**
   * convert a set to a string (comma separate)
   * @param map
   * @return the String
   */
  public static String mapToString(Map map) {
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
      return abbreviate(resultString, maxChars);
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
    //not sure why this would be 0...
    if (batchSize == 0) {
      return 0;
    }
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
   * if the groups are: a:b:c:d and a:d:r, then return the strings:
   * :, a, a:b, a:b:c, a:d
   * 
   * @param groups
   * @return the set of stem names
   */
  public static Set<String> findParentStemNames(Collection<Group> groups) {
    Set<String> result = new LinkedHashSet<String>();
    if (groups == null || groups.size() == 0) {
      return result;
    }
    for (Group group : groups) {
      String name = group.getName();
      result.addAll(findParentStemNames(name));
    }
    return result;
  }
  
  /**
   * if the groups are: a:b:c:d, then return the strings:
   * :, a, a:b, a:b:c
   * 
   * @param objectName
   * @return the set of stem names
   */
  public static Set<String> findParentStemNames(String objectName) {
    List<String> result = new ArrayList<String>();
    String currentName = objectName;
    while(true) {
      currentName = parentStemNameFromName(currentName);
      if (isEmpty(currentName)) {
        //add root
        result.add(":");
        break;
      }
      result.add(currentName);
    }
    Collections.reverse(result); 
    return new LinkedHashSet(result);
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
   * the Commons Util trim() for more details)
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
   * the Commons Util trim() for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * 
   * @return the list of items after split and trimmed, or null if input is null.  will be trimmed to empty
   */
  public static List<String> splitTrimToList(String input, String separator) {
    if (isBlank(input)) {
      return null;
    }
    String[] array =  splitTrim(input, separator);
    return toList(array);
  }

  /**
   * split a string based on a separator into an array, and trim each entry (see
   * the Commons Util trim() for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * 
   * @return the set of items after split and trimmed, or null if input is null.  will be trimmed to empty
   */
  public static Set<String> splitTrimToSet(String input, String separator) {
    if (isBlank(input)) {
      return null;
    }
    String[] array =  splitTrim(input, separator);
    return toSet(array);
  }

  /**
   * split a string based on a separator into an array, and trim each entry (see
   * the Commons Util trim() for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * @param treatAdjacentSeparatorsAsOne
   * @return the array of items after split and trimmed, or null if input is null.  will be trimmed to empty
   */
  public static String[] splitTrim(String input, String separator, boolean treatAdjacentSeparatorsAsOne) {
    if (isBlank(input)) {
      return null;
    }

    //first split
    String[] items = treatAdjacentSeparatorsAsOne ? splitByWholeSeparator(input, separator) : 
      split(input, separator);

    //then trim
    for (int i = 0; (items != null) && (i < items.length); i++) {
      items[i] = trim(items[i]);
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
   * make sure a collection is non null.  If null, then return an empty list
   * @param <T>
   * @param list
   * @return the list or empty list if null
   */
  public static <T> Collection<T> nonNull(Collection<T> list) {
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
   * return a list of objects from varargs.  Though if there is one
   * object, and it is a list, return it.
   * 
   * @param <T>
   *            template type of the objects
   * @param objects
   * @return the list or null if objects is null
   */
  public static List<Object> toListObject(Object... objects) {
    if (objects == null) {
      return null;
    }
    List<Object> result = new ArrayList<Object>();
    for (Object object : objects) {
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
    if (objects == null) {
      return null;
    }
    Set<T> result = new LinkedHashSet<T>();
    for (T object : objects) {
      result.add(object);
    }
    return result;
  }

  /**
   * return a set of string
   * 
   * @param <T> template type of the objects
   * @param object
   * @return the set
   */
  public static <T> Set<T> toSetObject(T object) {
    if (object == null) {
      return null;
    }
    Set<T> result = new LinkedHashSet<T>();
    result.add(object);
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
   * string format of dates for file names
   */
  public static final String TIMESTAMP_FILE_FORMAT = "yyyy_MM_dd__HH_mm_ss_SSS";

  /**
   * timestamp format, make sure to synchronize
   */
  final static SimpleDateFormat timestampFileFormat = new SimpleDateFormat(TIMESTAMP_FILE_FORMAT);

  /**
   * string format of dates
   */
  public static final String DATE_FORMAT2 = "yyyy/MM/dd";

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
   * date format, make sure to synchronize
   */
  final static SimpleDateFormat dateFormat2 = new SimpleDateFormat(DATE_FORMAT2);

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
      synchronized(GrouperStartup.class) {
        if (fieldSetCache == null) {
      fieldSetCache = new GrouperCache<String, Set<Field>>("edu.internet2.middleware.grouper.util.fieldSetCache",
          2000, false, 0, 60*60*24, false);
    }
      }
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
      synchronized(GrouperStartup.class) {
        if (declaredMethodsCache == null) {
      declaredMethodsCache = new GrouperCache<Class, Method[]>("edu.internet2.middleware.grouper.util.declaredMethodsCache",
          2000, false, 0, 60*60*24, false);
    }
      }
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
      synchronized(GrouperStartup.class) {
        if (getterSetCache == null) {
      getterSetCache = new GrouperCache<String, Set<Method>>("edu.internet2.middleware.grouper.util.getterSetCache",
          2000, false, 0, 60*60*24, false);
    }
      }
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
      synchronized(GrouperStartup.class) {
        if (setterSetCache == null) {
      setterSetCache = new GrouperCache<String, Set<Method>>("edu.internet2.middleware.grouper.util.setterSetCache",
          2000, false, 0, 60*60*24, false);
    }
      }
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
  private static ExpirableCache<String, Properties> resourcePropertiesCache = null;
  
  /**
   * lazy load this since it prevents the class from loading
   * @return the cache
   */
  private static ExpirableCache<String, Properties> resourcePropertiesCache() {
    if (resourcePropertiesCache == null) {
      synchronized(GrouperStartup.class) {
        if (resourcePropertiesCache == null) {
          //note, if this relies on the config file to configure, and the config file uses this, then we need a simpler cache here than an ehcache...
          //resourcePropertiesCache = new GrouperCache<String, Properties>(
          //  GrouperUtil.class.getName() + ".resourcePropertiesCache", 200, false, 300, 300, false);
          resourcePropertiesCache = new ExpirableCache<String, Properties>(5);
          
        }
      }
    }
    return resourcePropertiesCache;
  }

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
        result.append(((Class)Array.get(object, i)).getSimpleName());
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
//this was cglib
//      while (Enhancer.isEnhanced(theClass)) {
//        theClass = theClass.getSuperclass();
//      }
      
      while (ProxyObject.class.isAssignableFrom(theClass)) {
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
   * put in english how an object changed
   * @param theOld
   * @param theNew
   * @param differentFieldNames
   * @return the differences
   */
  public static String dbVersionDescribeDifferences(Object theOld, Object theNew, Set<String> differentFieldNames) {

    StringBuilder result = new StringBuilder(theOld == null ? "Old state unknown, new state is: " : "Fields changed: ");
    
    int length = length(differentFieldNames);
    
    if (length == 0) {
      result.append("none");
      return result.toString();
    }
    int i=0;
    for (String fieldName : nonNull(differentFieldNames)) {
      result.append(fieldName);
      if (i < length-1) {
        result.append(", ");
      } else {
        result.append(".\n");
      }
      i++;
    }
    //do each field
    i=0;
    for (String fieldName : nonNull(differentFieldNames)) {
      String oldString = theOld == null ? "?" : stringValue(fieldValue(theOld, fieldName));
      oldString = StringUtils.abbreviate(oldString, 200);
      String newString = stringValue(fieldValue(theNew, fieldName));
      newString = StringUtils.abbreviate(newString, 200);
      result.append(fieldName).append(": FROM: '").append(oldString).append("', TO: '").append(newString).append("'");
      if (i < length-1) {
        result.append("\n");
      }
      i++;
    }
    return result.toString();
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
          if (!equals(defaultString((String)firstValue),
              defaultString((String)secondValue))) {
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
    T result = (T)newInstance(object.getClass());
    
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
    
    for (String fieldName : nonNull(fieldsToClone)) {
      try {
        
        Object fieldValue = fieldValue(object, fieldName);
        fieldValueClass = fieldValue == null ? null : fieldValue.getClass();
        
        Object fieldValueToAssign = cloneValue(fieldValue);
        
        //assign the field to the clone
        assignField(result, fieldName, fieldValueToAssign);
        
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
    
    if (value == null || value instanceof String || value instanceof Class<?>
        || value instanceof Enum<?>
        || value.getClass().isPrimitive() || value instanceof Number
        || value instanceof Boolean
        || value instanceof Date || value instanceof Configuration
        || value instanceof Subject || value.getClass().isEnum()) {
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
   * simple method to get method names
   * @param theClass
   * @param methodName
   * @param superclassToStopAt 
   * @param includeSuperclassToStopAt 
   * @param includeStaticMethods 
   * @param exceptionIfNotFound 
   * @return the set of method names
   */
  public static Method methodByName(Class<?> theClass, String methodName, Class<?> superclassToStopAt, 
      boolean includeSuperclassToStopAt, boolean includeStaticMethods, boolean exceptionIfNotFound) {

    Set<Method> methods = new LinkedHashSet<Method>();
    methodsByNameHelper(theClass, methodName, superclassToStopAt, 
        includeSuperclassToStopAt, includeStaticMethods, 
        null, false, methods);
    if (methods.size() > 1) {
      throw new RuntimeException("There are more than one method with name " + methodName + " in class: " + theClass);
    }
    
    if (methods.size() == 1) {
      return methods.iterator().next();
    }
    
    if (exceptionIfNotFound) {
      throw new RuntimeException("Could not find method " + methodName + " in class: " + theClass);
    }
    
    return null;
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
   * @param considerFieldValuable if the FieldValueable interface should be considered
   * @return the current value
   */
  public static Object fieldValue(Class theClass, Object invokeOn,
      String fieldName, boolean callOnSupers, boolean overrideSecurity, boolean considerFieldValuable) {

    //if it has the interface to customize
    if (considerFieldValuable && (invokeOn instanceof FieldValuable)) {
      return ((FieldValuable)invokeOn).fieldValue(fieldName);
    }
    
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
   * has fieldValue method
   */
  public static interface FieldValuable {
    /**
     * call this method to get the field value (e.g. from dbVersionDifferentFields).
     * some objects have different interpretations (e.g. Group will process attribute__whatever)
     * @param fieldName
     * @return the value
     */
    public Object fieldValue(String fieldName);

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
    return fieldValue(null, invokeOn, fieldName, true, true, true);
  }

  /**
   * get the decalred methods for a class, perhaps from cache
   * 
   * @param theClass
   * @return the declared methods
   */
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
   * <pre>
   * turn a list into map
   * </pre>
   * @param list is the list to convert
   * @param valueClass type of the result (can typecast)
   * @param keyClass is the type of the key of the map
   * @param <K> is the template of the key of the map
   * @param <V> is the template of the value of the map
   * @param keyPropertyName name of the javabeans property for the key in the map
   * @return the ordered set or the empty set if not found (never null)
   */
  public static <K, V> Map<K, V> listToMap(List<V> list, @SuppressWarnings("unused") final Class<K> keyClass, 
      @SuppressWarnings("unused") final Class<V> valueClass, String keyPropertyName)  {
    Map<K,V> result = new LinkedHashMap<K, V>();
    for (V value : nonNull(list)) {
      K key = (K)GrouperUtil.propertyValue(value, keyPropertyName);
      result.put(key, value);
    }
    return result;
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
    try {
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
    } catch (RuntimeException re) {
      String message = "Problem calling method " + methodName
            + " on " + (theClass == null ? null : theClass.getName());
      if (injectInException(re, message)) {
        throw re;
      }
      throw new RuntimeException(message, re);
    }
  }
  
  /** pass this in the invokeOn to signify no params */
  private static final Object NO_PARAMS = new Object();
  
  /**
   * Safely invoke a reflection method that takes no args
   * 
   * @param method
   *            to invoke
   * @param invokeOn
   * if NO_PARAMS then will not pass in params.
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
   * if NO_PARAMS then will not pass in params.
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
   * replace(null, *, *)        = null
   * replace(&quot;&quot;, *, *)          = &quot;&quot;
   * replace(&quot;any&quot;, null, *)    = &quot;any&quot;
   * replace(&quot;any&quot;, *, null)    = &quot;any&quot;
   * replace(&quot;any&quot;, &quot;&quot;, *)      = &quot;any&quot;
   * replace(&quot;aba&quot;, &quot;a&quot;, null)  = &quot;aba&quot;
   * replace(&quot;aba&quot;, &quot;a&quot;, &quot;&quot;)    = &quot;b&quot;
   * replace(&quot;aba&quot;, &quot;a&quot;, &quot;z&quot;)   = &quot;zbz&quot;
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
   * replace(null, *, *, *)         = null
   * replace(&quot;&quot;, *, *, *)           = &quot;&quot;
   * replace(&quot;any&quot;, null, *, *)     = &quot;any&quot;
   * replace(&quot;any&quot;, *, null, *)     = &quot;any&quot;
   * replace(&quot;any&quot;, &quot;&quot;, *, *)       = &quot;any&quot;
   * replace(&quot;any&quot;, *, *, 0)        = &quot;any&quot;
   * replace(&quot;abaa&quot;, &quot;a&quot;, null, -1) = &quot;abaa&quot;
   * replace(&quot;abaa&quot;, &quot;a&quot;, &quot;&quot;, -1)   = &quot;b&quot;
   * replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 0)   = &quot;abaa&quot;
   * replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 1)   = &quot;zbaa&quot;
   * replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, 2)   = &quot;zbza&quot;
   * replace(&quot;abaa&quot;, &quot;a&quot;, &quot;z&quot;, -1)  = &quot;zbzz&quot;
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
   * isEmpty(null)      = true
   * isEmpty(&quot;&quot;)        = true
   * isEmpty(&quot; &quot;)       = false
   * isEmpty(&quot;bob&quot;)     = false
   * isEmpty(&quot;  bob  &quot;) = false
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
   * convert a collection of sources to a string
   * @param sources
   * @return the string
   */
  public static String toString(Collection<Source> sources) {
    if (length(sources) == 0) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (Source source : sources) {
      result.append(source.getId()).append(", ");
    }
    result.delete(result.length()-2, result.length());
    return result.toString();
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
      return "<<exception>> " + object.getClass() + ":\n" + getFullStackTrace(e) + "\n";
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
      if (equalsIgnoreCase(string, "true")
          || equalsIgnoreCase(string, "t")
          || equalsIgnoreCase(string, "yes")
          || equalsIgnoreCase(string, "y")) {
        return true;
      }
      if (equalsIgnoreCase(string, "false")
          || equalsIgnoreCase(string, "f")
          || equalsIgnoreCase(string, "no")
          || equalsIgnoreCase(string, "n")) {
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
    if (nullOrBlank(object)) {
      return null;
    }
    return booleanValue(object);
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
    if (object instanceof String && isBlank(((String) object))) {
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
        if (equals(getterName, method.getName()) && isGetter(method)) {
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
    return "get" + capitalize(propertyName);
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
          + " on class: " + invokeOnClass + ", type of data is: " + className(dataToAssign), e);
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
        if (equals(setterName, method.getName()) && isSetter(method)) {
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
    return "set" + capitalize(propertyName);
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
   * take a collection of beans, and go through and get all the values
   * of one of the javabean properties, and make a list of those values.
   * @param <T>
   * @param collection
   * @param propertyName
   * @param fieldType
   * @return the list
   */
  public static <T> List<T> propertyList(Collection<?> collection, 
      String propertyName, @SuppressWarnings("unused") Class<T> fieldType) {
    
    if (collection == null) {
      return null;
    }
    
    List<T> list = new ArrayList<T>();
    
    for (Object object : collection) {
      T value = (T)propertyValue(object, propertyName);
      list.add(value);
    }
    
    return list;
    
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
    if (!isBlank(parentDirName)) {
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
   * convert a string date into a long date (e.g. for xml export)
   * @param date
   * @return the long or null if the date was null or blank
   */
  public static Long dateLongValue(String date) {
    if (isBlank(date)) {
      return null;
    }
    Date dateObject = dateValue(date);
    return dateObject.getTime();
  }

  /**
   * web service format string
   */
  private static final String TIMESTAMP_XML_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /**
   * date object to a string: 
   * @param date
   * @return the long or null if the date was null or blank
   */
  public static String dateStringValue(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_XML_FORMAT);
    return simpleDateFormat.format(date);
  }

  /**
   * date object to a string: 
   * @param theDate
   * @return the long or null if the date was null or blank
   */
  public static String dateStringValue(Long theDate) {
    if (theDate == null) {
      return null;
    }
    return dateStringValue(new Date(theDate));
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
      if (isBlank(input)) {
        return null;
      }

      try {
        if (input.length() == 8) {
          
          return dateFormat().parse(input);
        }
        if (!contains(input, '.')) {
          if (contains(input, '/')) {
            return dateMinutesSecondsFormat.parse(input);
          }
          //else no slash
          return dateMinutesSecondsNoSlashFormat.parse(input);
        }
        if (contains(input, '/')) {
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
   * match regex pattern yyyy-mm-dd or yyyy/mm/dd
   */
  private static Pattern datePattern_yyyy_mm_dd = Pattern.compile("^(\\d{4})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})$");
  
  /**
   * match regex pattern dd-mon-yyyy or dd/mon/yyyy
   */
  private static Pattern datePattern_dd_mon_yyyy = Pattern.compile("^(\\d{1,2})[^\\d]+([a-zA-Z]{3,15})[^\\d]+(\\d{4})$");
  
  /**
   * match regex pattern yyyy-mm-dd hh:mm:ss or yyyy/mm/dd hh:mm:ss
   */
  private static Pattern datePattern_yyyy_mm_dd_hhmmss = Pattern.compile("^(\\d{4})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})$");
  
  /**
   * match regex pattern dd-mon-yyyy hh:mm:ss or dd/mon/yyyy hh:mm:ss
   */
  private static Pattern datePattern_dd_mon_yyyy_hhmmss = Pattern.compile("^(\\d{1,2})[^\\d]+([a-zA-Z]{3,15})[^\\d]+(\\d{4})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})$");
  
  /**
   * match regex pattern yyyy-mm-dd hh:mm:ss.SSS or yyyy/mm/dd hh:mm:ss.SSS
   */
  private static Pattern datePattern_yyyy_mm_dd_hhmmss_SSS = Pattern.compile("^(\\d{4})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,3})$");
  
  /**
   * match regex pattern dd-mon-yyyy hh:mm:ss.SSS or dd/mon/yyyy hh:mm:ss.SSS
   */
  private static Pattern datePattern_dd_mon_yyyy_hhmmss_SSS = Pattern.compile("^(\\d{1,2})[^\\d]+([a-zA-Z]{3,15})[^\\d]+(\\d{4})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,3})$");
  
  /**
   * take as input:
   * yyyy/mm/dd
   * yyyy-mm-dd
   * dd-mon-yyyy
   * yyyy/mm/dd hh:mm:ss
   * dd-mon-yyyy hh:mm:ss
   * yyyy/mm/dd hh:mm:ss.SSS
   * dd-mon-yyyy hh:mm:ss.SSS
   * @param input
   * @return the date
   */
  public static Date stringToDate2(String input) {
    
    if (isBlank(input)) {
      return null;
    }
    input = input.trim();
    Matcher matcher = null;
    
    int month = 0;
    int day = 0;
    int year = 0;
    int hour = 0;
    int minute = 0;
    int second = 0;
    int milli = 0;
    
    boolean foundMatch = false;

    //yyyy/mm/dd
    if (!foundMatch) {
      matcher = datePattern_yyyy_mm_dd.matcher(input);
      if (matcher.matches()) {
        year = intValue(matcher.group(1));
        month =  intValue(matcher.group(2));
        day = intValue(matcher.group(3));
        foundMatch = true;
      }
    }
    
    //dd-mon-yyyy
    if (!foundMatch) {
      matcher = datePattern_dd_mon_yyyy.matcher(input);
      if (matcher.matches()) {
        day = intValue(matcher.group(1));
        month =  monthInt(matcher.group(2));
        year = intValue(matcher.group(3));
        foundMatch = true;
      }
    }
    
    //yyyy/mm/dd hh:mm:ss
    if (!foundMatch) {
      matcher = datePattern_yyyy_mm_dd_hhmmss.matcher(input);
      if (matcher.matches()) {
        year = intValue(matcher.group(1));
        month =  intValue(matcher.group(2));
        day = intValue(matcher.group(3));
        hour = intValue(matcher.group(4));
        minute = intValue(matcher.group(5));
        second = intValue(matcher.group(6));
        foundMatch = true;
      }      
    }
    
    //dd-mon-yyyy hh:mm:ss
    if (!foundMatch) {
      matcher = datePattern_dd_mon_yyyy_hhmmss.matcher(input);
      if (matcher.matches()) {
        day = intValue(matcher.group(1));
        month =  monthInt(matcher.group(2));
        year = intValue(matcher.group(3));
        hour = intValue(matcher.group(4));
        minute = intValue(matcher.group(5));
        second = intValue(matcher.group(6));
        foundMatch = true;
      }
    }
    
    //yyyy/mm/dd hh:mm:ss.SSS
    if (!foundMatch) {
      matcher = datePattern_yyyy_mm_dd_hhmmss_SSS.matcher(input);
      if (matcher.matches()) {
        year = intValue(matcher.group(1));
        month =  intValue(matcher.group(2));
        day = intValue(matcher.group(3));
        hour = intValue(matcher.group(4));
        minute = intValue(matcher.group(5));
        second = intValue(matcher.group(6));
        milli = intValue(matcher.group(7));
        foundMatch = true;
      }      
    }
    
    //dd-mon-yyyy hh:mm:ss.SSS
    if (!foundMatch) {
      matcher = datePattern_dd_mon_yyyy_hhmmss_SSS.matcher(input);
      if (matcher.matches()) {
        day = intValue(matcher.group(1));
        month =  monthInt(matcher.group(2));
        year = intValue(matcher.group(3));
        hour = intValue(matcher.group(4));
        minute = intValue(matcher.group(5));
        second = intValue(matcher.group(6));
        milli = intValue(matcher.group(7));
        foundMatch = true;
      }
    }
    
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month-1);
    calendar.set(Calendar.DAY_OF_MONTH, day);
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, milli);
    return calendar.getTime();
  }
  
  /**
   * convert a month string to an int (1 indexed).
   * e.g. if input is feb or Feb or february or February return 2
   * @param mon
   * @return the month
   */
  public static int monthInt(String mon) {
    
    if (!isBlank(mon)) {
      mon = mon.toLowerCase();
      
      if (equals(mon, "jan") || equals(mon, "january")) {
        return 1;
      }
      
      if (equals(mon, "feb") || equals(mon, "february")) {
        return 2;
      }
      
      if (equals(mon, "mar") || equals(mon, "march")) {
        return 3;
      }
      
      if (equals(mon, "apr") || equals(mon, "april")) {
        return 4;
      }
      
      if (equals(mon, "may")) {
        return 5;
      }
      
      if (equals(mon, "jun") || equals(mon, "june")) {
        return 6;
      }
      
      if (equals(mon, "jul") || equals(mon, "july")) {
        return 7;
      }
      
      if (equals(mon, "aug") || equals(mon, "august")) {
        return 8;
      }
      
      if (equals(mon, "sep") || equals(mon, "september")) {
        return 9;
      }
      
      if (equals(mon, "oct") || equals(mon, "october")) {
        return 10;
      }
      
      if (equals(mon, "nov") || equals(mon, "november")) {
        return 11;
      }
      
      if (equals(mon, "dec") || equals(mon, "december")) {
        return 12;
      }
      
    }
    
    throw new RuntimeException("Invalid month: " + mon);
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
    return (input instanceof String && isBlank((String)input));
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
      if ( equals("null", stringValue)) {
        resultValue = null;
      } else if (equals("newInstance", stringValue)) {
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
   * yyyy/MM/dd
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
   * Convert a timestamp into a string: yyyy/MM/dd HH:mm:ss.SSS
   * @param timestamp
   * @return the string representation
   */
  public synchronized static String timestampToFileString(Date timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestampFileFormat.format(timestamp);
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
   * get the timestamp format for this thread
   * if you call this make sure to synchronize on FastDateUtils.class
   * @return the timestamp format
   */
  synchronized static SimpleDateFormat dateFormat2() {
    return dateFormat2;
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
   * yyyy/MM/dd
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
   * yyyy/MM/dd
   * yyyy/MM/dd HH:mm:ss
   * yyyy/MM/dd HH:mm:ss.SSS
   * yyyy/MM/dd HH:mm:ss.SSSSSS
   * 
   * @param input
   * @return the millis, -1 for null
   */
  synchronized static Date stringToTimestampHelper(String input) {
    //trim and handle null and empty
    if (isBlank(input)) {
      return null;
    }
    input = input.trim();
    try {
      //convert mainframe
      if (equals("99999999", input)
          || equals("999999", input)) {
        input = "20991231";
      }
      if (input.length() == 8) {
        
        return dateFormat().parse(input);
      }
      if (input.length() == 10) {
        
        return dateFormat2().parse(input);
      }
      if (!contains(input, '.')) {
        if (contains(input, '/')) {
          return dateMinutesSecondsFormat.parse(input);
        }
        //else no slash
        return dateMinutesSecondsNoSlashFormat.parse(input);
      }
      if (contains(input, '/')) {
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
        && isBlank((String)input))) {
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
        && isBlank((String)input))) {
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
        && isBlank((String)input))) {
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
        && isBlank((String)input))) {
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
      if (equals(fileContents, compressedContents)) {
        return false;
      }
  
    }
    saveStringIntoFile(file, contents);
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
    if (isBlank(resourceName)) {
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
   * close a writer quietly
   * @param writer
   */
  public static void closeQuietly(XMLStreamWriter writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (XMLStreamException e) {
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
      throw new RuntimeException("convertLongToChar() "
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
      throw new RuntimeException("convertLongToCharSmall() "
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
   * cache properties
   */
  private static GrouperCache<File, Properties> propertiesFromFileCache = null;
  
  /**
   * properties from File cache
   * @return cache
   */
  private static GrouperCache<File, Properties> propertiesFromFileCache() {
    if (propertiesFromFileCache == null) {
      synchronized(GrouperStartup.class) {
        if (propertiesFromFileCache == null) {
      propertiesFromFileCache = new GrouperCache<File,Properties>(
          GrouperUtil.class.getName() + ".propertiesFromFileCache", 200, false, 300, 300, false);
    }
      }
    }
    return propertiesFromFileCache;
  }

  /**
   * cache properties
   */
  static GrouperCache<String, Properties> propertiesFromUrlCache = null;

  /**
   * properties from url cache
   * @return cache
   */
  private static GrouperCache<String, Properties> propertiesFromUrlCache() {
    if (propertiesFromUrlCache == null) {
      synchronized(GrouperStartup.class) {
        if (propertiesFromUrlCache == null) {
      propertiesFromUrlCache = new GrouperCache<String,Properties>(
          GrouperUtil.class.getName() + ".propertiesFromUrlCache", 200, false, 300, 300, false);
    }
      }
    }
    return propertiesFromUrlCache;
  }

  /**
   * cache properties
   */
  private static GrouperCache<String, Properties> propertiesFromUrlFailsafeCache = null;
  
  /**
   * properties from url failsafe cache
   * @return cache
   */
  private static GrouperCache<String, Properties> propertiesFromUrlFailsafeCache() {
    if (propertiesFromUrlFailsafeCache == null) {
      synchronized(GrouperStartup.class) {
        if (propertiesFromUrlFailsafeCache == null) {
      propertiesFromUrlFailsafeCache = new GrouperCache<String,Properties>(
          GrouperUtil.class.getName() + ".propertiesFromUrlFailsafeCache", 200, false, 60*60*24, 60*60*24, false);
    }
      }
    }
    return propertiesFromUrlFailsafeCache;
  }

  /** variable for testing */
  static int propertiesFromUrlHttpCount = 0;
  
  /** variable for testing */
  static int propertiesFromUrlFailsafeGetCount = 0;
  
  /** variable for testing */
  static boolean propertiesFromUrlFailForTest = false;
  
  /**
   * this will get the properties from an external url.  It will cache these (failsafe),
   * and will escape them based on grouper's properties escaper (configurable)
   * @param urlString e.g. http://localhost:8090/grouper/test.properties
   * @param useCache if should cache for 2 minutes
   * @param useFailSafeCache if should use this cache for 1 day if the url cant connect
   * @param grouperHtmlFilter 
   * @return the properties
   */
  public static Properties propertiesFromUrl(String urlString, boolean useCache, 
      boolean useFailSafeCache, GrouperHtmlFilter grouperHtmlFilter) {
    
    Properties properties = null;
    
    if (useCache) {
      properties = propertiesFromUrlCache().get(urlString);
      if (properties != null) {
        if (useFailSafeCache) {
          //update this
          propertiesFromUrlFailsafeCache().put(urlString, properties);
        }
        return properties;
      }
    }
    
    InputStream inputStream = null;
    try {
      if (propertiesFromUrlFailForTest) {
        //reset
        propertiesFromUrlFailForTest=false;
        throw new RuntimeException("testing here!!!!");
      }
      URL url = new URL(urlString);
      properties = new Properties();
      inputStream = url.openConnection().getInputStream();
      properties.load(inputStream);
      
      //for testing
      propertiesFromUrlHttpCount++;
      
      if (grouperHtmlFilter != null) {
        for (Object key : properties.keySet()) {
          String value = (String)properties.get(key);
          String formattedValue = grouperHtmlFilter.filterHtml(value);
          properties.put(key, formattedValue);
        }
      }

    } catch (Exception e) {
      //failsafe means if problem, keep on keeping on
      if (useFailSafeCache) {
        properties = propertiesFromUrlFailsafeCache().get(urlString);
      }
      String error = "Problem with url: " + urlString;
      //always log since could have security problems with throwing exceptions
      LOG.error(error, e);
      
      error = "Problem with url: " + StringUtils.abbreviate(urlString, 19);
      
      if (!useFailSafeCache || properties == null) {
        throw new RuntimeException(error, e);
      } 
      //just log if got from failsafe
      if (useCache) {
        propertiesFromUrlCache().put(urlString, properties);
      }
      //for testing
      propertiesFromUrlFailsafeGetCount++;
      //note: dont put in failsafe cache again...
      return properties;
    } finally {
      closeQuietly(inputStream);
    }

    //add to cache if should
    if (useCache) {
      propertiesFromUrlCache().put(urlString, properties);
    }
    if (useFailSafeCache) {
      //update this
      propertiesFromUrlFailsafeCache().put(urlString, properties);
    }
    return properties;


  }

  /**
   * properties from file
   * @param file
   * @param useCache
   * @return properties
   */
  public synchronized static Properties propertiesFromFile(File file, boolean useCache) {
    Properties properties = null;
    if (useCache) {
      properties = propertiesFromFileCache().get(file);
      if (properties != null) {
        return properties;
      }
    }
    
    FileInputStream fileInputStream = null;
    
    try {
      fileInputStream = new FileInputStream(file);
      properties = new Properties();
      properties.load(fileInputStream);
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with file: " + file, ioe);
    } finally {
      
      GrouperUtil.closeQuietly(fileInputStream);
      
    }
    
    if (useCache) {
      propertiesFromFileCache().put(file, properties);      
    }
    return properties;
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

    Properties properties = resourcePropertiesCache() == null ? null : resourcePropertiesCache().get(resourceName);
    
    if (resourcePropertiesCache() == null || !useCache || resourcePropertiesCache().get(resourceName) == null) {
  
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
        closeQuietly(inputStream);

        if (useCache && resourcePropertiesCache() != null) {
          resourcePropertiesCache().put(resourceName, properties);
        }
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
    return enumValueOfIgnoreCase(theEnumClass, string, exceptionOnNotFound, true);
  }
    

  /**
   * do a case-insensitive matching
   * @param theEnumClass class of the enum
   * @param <E> generic type
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @param exceptionIfInvalid if there is a string, but it is invalid, if should throw exception
   * @return the enum or null or exception if not found
   * @throws RuntimeException if there is a problem
   */
  public static <E extends Enum<?>> E enumValueOfIgnoreCase(Class<E> theEnumClass, String string, 
      boolean exceptionOnNotFound, boolean exceptionIfInvalid) throws RuntimeException {
    
    if (!exceptionOnNotFound && isBlank(string)) {
      return null;
    }
    for (E e : theEnumClass.getEnumConstants()) {
      if (equalsIgnoreCase(string, e.name())) {
        return e;
      }
    }
    if (!exceptionIfInvalid) {
      return null;
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
    return propertiesValue(properties, overrideMap, null, key);
  }

  /**
   * get a value (trimmed to e) from a property file
   * @param properties
   * @param overrideMap for testing or threadlocal, to override some properties values
   * @param overrideMap2 for testing, to provide some properties values
   * @param key
   * @return the property value
   */
  public static String propertiesValue(Properties properties, Map<String, String> overrideMap, Map<String, String> overrideMap2, String key) {
    String value = overrideMap == null ? null : overrideMap.get(key);
    if (isBlank(value)) {
      value = overrideMap2 == null ? null : overrideMap2.get(key);
    }
    if (isBlank(value)) {
      value = properties.getProperty(key);
    }
    return trim(value);
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
    return propertiesValueBoolean(properties, overrideMap, null, propertyName, defaultValue);
  }

  /**
   * get a boolean property, or the default if cant find
   * @param properties
   * @param overrideMap for testing or threadlocal to override properties
   * @param overrideMap2 for testing or threadlocal to override properties
   * @param propertyName
   * @param defaultValue 
   * @return the boolean
   */
  public static boolean propertiesValueBoolean(Properties properties, 
      Map<String, String> overrideMap, Map<String, String> overrideMap2, String propertyName, boolean defaultValue) {
    
      
    String value = propertiesValue(properties, overrideMap, overrideMap2, propertyName);
    if (isBlank(value)) {
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
    throw new RuntimeException("Invalid boolean value: '" + value + "' for property: " + propertyName + " in properties file");

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
  
    if (isBlank(hostname)) {
  
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
   * string length
   * @param string
   * @return string length
   */
  public static int stringLength(String string) {
    return string == null ? 0 : string.length();
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
   * @param disableExternalFileLookup 
   * @return the result
   */
  public static String readFromFileIfFile(String in, boolean disableExternalFileLookup) {
    String theIn = in;
    //convert both slashes to file slashes
    if (File.separatorChar == '/') {
      theIn = replace(theIn, "\\", "/");
    } else {
      theIn = replace(theIn, "/", "\\");
    }
    
    //see if it is a file reference
    if (theIn.indexOf(File.separatorChar) != -1 && disableExternalFileLookup) {
      //read the contents of the file into a string
      theIn = readFileIntoString(new File(theIn));
      return theIn;
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
  static void fixHibernateConnectionUrl(Properties props) {
    String url = props.getProperty("hibernate.connection.url");
    if (isBlank(url)) {
      return;
    }
    if (!url.startsWith("jdbc:hsqldb:")) {
      return;
    }
    if (url.matches("^jdbc:hsqldb:(mem|hsql|res|hsql|hsqls|http|https):.*")) {
      return;
    }
    int spliceAt = 12;
    if (url.startsWith("jdbc:hsqldb:file:")) {
      spliceAt = 17;
    }
    String file = url.substring(spliceAt);
    String newUrl = url.substring(0, spliceAt) + fixRelativePath(file);
    props.setProperty("hibernate.connection.url", newUrl);
  }

  /**
   * null safe string compare
   * @param first
   * @param second
   * @return true if equal
   */
  public static boolean equals(String first, String second) {
    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    return first.equals(second);
  }

  /**
   * <p>Checks if a String is whitespace, empty ("") or null.</p>
   *
   * <pre>
   * isBlank(null)      = true
   * isBlank("")        = true
   * isBlank(" ")       = true
   * isBlank("bob")     = false
   * isBlank("  bob  ") = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if the String is null, empty or whitespace
   * @since 2.0
   */
  public static boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if ((Character.isWhitespace(str.charAt(i)) == false)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param str
   * @return true if not blank
   */
  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  /**
   * trim whitespace from string
   * @param str
   * @return trimmed string
   */
  public static String trim(String str) {
    return str == null ? null : str.trim();
  }

  /**
   * equalsignorecase
   * @param str1
   * @param str2
   * @return true if the strings are equal ignore case
   */
  public static boolean equalsIgnoreCase(String str1, String str2) {
    return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
  }

  /**
   * trim to empty, convert null to empty
   * @param str
   * @return trimmed
   */
  public static String trimToEmpty(String str) {
    return str == null ? "" : str.trim();
  }

  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "Now is the time for..."</p>
   *
   * <p>Specifically:
   * <ul>
   *   <li>If <code>str</code> is less than <code>maxWidth</code> characters
   *       long, return it.</li>
   *   <li>Else abbreviate it to <code>(substring(str, 0, max-3) + "...")</code>.</li>
   *   <li>If <code>maxWidth</code> is less than <code>4</code>, throw an
   *       <code>IllegalArgumentException</code>.</li>
   *   <li>In no case will it return a String of length greater than
   *       <code>maxWidth</code>.</li>
   * </ul>
   * </p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *)      = null
   * StringUtils.abbreviate("", 4)        = ""
   * StringUtils.abbreviate("abcdefg", 6) = "abc..."
   * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
   * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
   * StringUtils.abbreviate("abcdefg", 4) = "a..."
   * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param maxWidth  maximum length of result String, must be at least 4
   * @return abbreviated String, <code>null</code> if null String input
   * @throws IllegalArgumentException if the width is too small
   * @since 2.0
   */
  public static String abbreviate(String str, int maxWidth) {
    return abbreviate(str, 0, maxWidth);
  }

  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "...is the time for..."</p>
   *
   * <p>Works like <code>abbreviate(String, int)</code>, but allows you to specify
   * a "left edge" offset.  Note that this left edge is not necessarily going to
   * be the leftmost character in the result, or the first character following the
   * ellipses, but it will appear somewhere in the result.
   *
   * <p>In no case will it return a String of length greater than
   * <code>maxWidth</code>.</p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *, *)                = null
   * StringUtils.abbreviate("", 0, 4)                  = ""
   * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
   * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
   * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
   * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param offset  left edge of source String
   * @param maxWidth  maximum length of result String, must be at least 4
   * @return abbreviated String, <code>null</code> if null String input
   * @throws IllegalArgumentException if the width is too small
   * @since 2.0
   */
  public static String abbreviate(String str, int offset, int maxWidth) {
    if (str == null) {
      return null;
    }
    if (maxWidth < 4) {
      throw new IllegalArgumentException("Minimum abbreviation width is 4");
    }
    if (str.length() <= maxWidth) {
      return str;
    }
    if (offset > str.length()) {
      offset = str.length();
    }
    if ((str.length() - offset) < (maxWidth - 3)) {
      offset = str.length() - (maxWidth - 3);
    }
    if (offset <= 4) {
      return str.substring(0, maxWidth - 3) + "...";
    }
    if (maxWidth < 7) {
      throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
    }
    if ((offset + (maxWidth - 3)) < str.length()) {
      return "..." + abbreviate(str.substring(offset), maxWidth - 3);
    }
    return "..." + str.substring(str.length() - (maxWidth - 3));
  }

  // Splitting
  //-----------------------------------------------------------------------
  /**
   * <p>Splits the provided text into an array, using whitespace as the
   * separator.
   * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as one separator.
   * For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.split(null)       = null
   * StringUtils.split("")         = []
   * StringUtils.split("abc def")  = ["abc", "def"]
   * StringUtils.split("abc  def") = ["abc", "def"]
   * StringUtils.split(" abc ")    = ["abc"]
   * </pre>
   *
   * @param str  the String to parse, may be null
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  public static String[] split(String str) {
    return split(str, null, -1);
  }

  /**
   * <p>Splits the provided text into an array, separator specified.
   * This is an alternative to using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as one separator.
   * For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.split(null, *)         = null
   * StringUtils.split("", *)           = []
   * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
   * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
   * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
   * StringUtils.split("a\tb\nc", null) = ["a", "b", "c"]
   * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
   * </pre>
   *
   * @param str  the String to parse, may be null
   * @param separatorChar  the character used as the delimiter,
   *  <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.0
   */
  public static String[] split(String str, char separatorChar) {
    return splitWorker(str, separatorChar, false);
  }

  /**
   * <p>Splits the provided text into an array, separators specified.
   * This is an alternative to using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as one separator.
   * For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> separatorChars splits on whitespace.</p>
   *
   * <pre>
   * StringUtils.split(null, *)         = null
   * StringUtils.split("", *)           = []
   * StringUtils.split("abc def", null) = ["abc", "def"]
   * StringUtils.split("abc def", " ")  = ["abc", "def"]
   * StringUtils.split("abc  def", " ") = ["abc", "def"]
   * StringUtils.split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
   * </pre>
   *
   * @param str  the String to parse, may be null
   * @param separatorChars  the characters used as the delimiters,
   *  <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  public static String[] split(String str, String separatorChars) {
    return splitWorker(str, separatorChars, -1, false);
  }

  /**
   * <p>Splits the provided text into an array with a maximum length,
   * separators specified.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as one separator.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> separatorChars splits on whitespace.</p>
   *
   * <p>If more than <code>max</code> delimited substrings are found, the last
   * returned string includes all characters after the first <code>max - 1</code>
   * returned strings (including separator characters).</p>
   *
   * <pre>
   * StringUtils.split(null, *, *)            = null
   * StringUtils.split("", *, *)              = []
   * StringUtils.split("ab de fg", null, 0)   = ["ab", "cd", "ef"]
   * StringUtils.split("ab   de fg", null, 0) = ["ab", "cd", "ef"]
   * StringUtils.split("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
   * StringUtils.split("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
   * </pre>
   *
   * @param str  the String to parse, may be null
   * @param separatorChars  the characters used as the delimiters,
   *  <code>null</code> splits on whitespace
   * @param max  the maximum number of elements to include in the
   *  array. A zero or negative value implies no limit
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  public static String[] split(String str, String separatorChars, int max) {
    return splitWorker(str, separatorChars, max, false);
  }

  /**
   * <p>Splits the provided text into an array, separator string specified.</p>
   *
   * <p>The separator(s) will not be included in the returned String array.
   * Adjacent separators are treated as one separator.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> separator splits on whitespace.</p>
   *
   * <pre>
   * StringUtils.split(null, *)            = null
   * StringUtils.split("", *)              = []
   * StringUtils.split("ab de fg", null)   = ["ab", "de", "fg"]
   * StringUtils.split("ab   de fg", null) = ["ab", "de", "fg"]
   * StringUtils.split("ab:cd:ef", ":")    = ["ab", "cd", "ef"]
   * StringUtils.split("abstemiouslyaeiouyabstemiously", "aeiouy")  = ["bst", "m", "sl", "bst", "m", "sl"]
   * StringUtils.split("abstemiouslyaeiouyabstemiously", "aeiouy")  = ["abstemiously", "abstemiously"]
   * </pre>
   *
   * @param str  the String to parse, may be null
   * @param separator  String containing the String to be used as a delimiter,
   *  <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String was input
   */
  public static String[] splitByWholeSeparator(String str, String separator) {
    return splitByWholeSeparator(str, separator, -1);
  }

  /**
   * <p>Splits the provided text into an array, separator string specified.
   * Returns a maximum of <code>max</code> substrings.</p>
   *
   * <p>The separator(s) will not be included in the returned String array.
   * Adjacent separators are treated as one separator.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> separator splits on whitespace.</p>
   *
   * <pre>
   * StringUtils.splitByWholeSeparator(null, *, *)               = null
   * StringUtils.splitByWholeSeparator("", *, *)                 = []
   * StringUtils.splitByWholeSeparator("ab de fg", null, 0)      = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab   de fg", null, 0)    = ["ab", "de", "fg"]
   * StringUtils.splitByWholeSeparator("ab:cd:ef", ":", 2)       = ["ab", "cd"]
   * StringUtils.splitByWholeSeparator("abstemiouslyaeiouyabstemiously", "aeiouy", 2) = ["bst", "m"]
   * StringUtils.splitByWholeSeparator("abstemiouslyaeiouyabstemiously", "aeiouy", 2)  = ["abstemiously", "abstemiously"]
   * </pre>
   *
   * @param str  the String to parse, may be null
   * @param separator  String containing the String to be used as a delimiter,
   *  <code>null</code> splits on whitespace
   * @param max  the maximum number of elements to include in the returned
   *  array. A zero or negative value implies no limit.
   * @return an array of parsed Strings, <code>null</code> if null String was input
   */
  public static String[] splitByWholeSeparator(String str, String separator, int max) {
    if (str == null) {
      return null;
    }

    int len = str.length();

    if (len == 0) {
      return EMPTY_STRING_ARRAY;
    }

    if ((separator == null) || ("".equals(separator))) {
      // Split on whitespace.
      return split(str, null, max);
    }

    int separatorLength = separator.length();

    ArrayList substrings = new ArrayList();
    int numberOfSubstrings = 0;
    int beg = 0;
    int end = 0;
    while (end < len) {
      end = str.indexOf(separator, beg);

      if (end > -1) {
        if (end > beg) {
          numberOfSubstrings += 1;

          if (numberOfSubstrings == max) {
            end = len;
            substrings.add(str.substring(beg));
          } else {
            // The following is OK, because String.substring( beg, end ) excludes
            // the character at the position 'end'.
            substrings.add(str.substring(beg, end));

            // Set the starting point for the next search.
            // The following is equivalent to beg = end + (separatorLength - 1) + 1,
            // which is the right calculation:
            beg = end + separatorLength;
          }
        } else {
          // We found a consecutive occurrence of the separator, so skip it.
          beg = end + separatorLength;
        }
      } else {
        // String.substring( beg ) goes from 'beg' to the end of the String.
        substrings.add(str.substring(beg));
        end = len;
      }
    }

    return (String[]) substrings.toArray(new String[substrings.size()]);
  }

  //-----------------------------------------------------------------------
  /**
   * <p>Splits the provided text into an array, using whitespace as the
   * separator, preserving all tokens, including empty tokens created by 
   * adjacent separators. This is an alternative to using StringTokenizer.
   * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as separators for empty tokens.
   * For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.splitPreserveAllTokens(null)       = null
   * StringUtils.splitPreserveAllTokens("")         = []
   * StringUtils.splitPreserveAllTokens("abc def")  = ["abc", "def"]
   * StringUtils.splitPreserveAllTokens("abc  def") = ["abc", "", "def"]
   * StringUtils.splitPreserveAllTokens(" abc ")    = ["", "abc", ""]
   * </pre>
   *
   * @param str  the String to parse, may be <code>null</code>
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str) {
    return splitWorker(str, null, -1, true);
  }

  /**
   * <p>Splits the provided text into an array, separator specified,
   * preserving all tokens, including empty tokens created by adjacent
   * separators. This is an alternative to using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as separators for empty tokens.
   * For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.splitPreserveAllTokens(null, *)         = null
   * StringUtils.splitPreserveAllTokens("", *)           = []
   * StringUtils.splitPreserveAllTokens("a.b.c", '.')    = ["a", "b", "c"]
   * StringUtils.splitPreserveAllTokens("a..b.c", '.')   = ["a", "b", "c"]
   * StringUtils.splitPreserveAllTokens("a:b:c", '.')    = ["a:b:c"]
   * StringUtils.splitPreserveAllTokens("a\tb\nc", null) = ["a", "b", "c"]
   * StringUtils.splitPreserveAllTokens("a b c", ' ')    = ["a", "b", "c"]
   * StringUtils.splitPreserveAllTokens("a b c ", ' ')   = ["a", "b", "c", ""]
   * StringUtils.splitPreserveAllTokens("a b c ", ' ')   = ["a", "b", "c", "", ""]
   * StringUtils.splitPreserveAllTokens(" a b c", ' ')   = ["", a", "b", "c"]
   * StringUtils.splitPreserveAllTokens("  a b c", ' ')  = ["", "", a", "b", "c"]
   * StringUtils.splitPreserveAllTokens(" a b c ", ' ')  = ["", a", "b", "c", ""]
   * </pre>
   *
   * @param str  the String to parse, may be <code>null</code>
   * @param separatorChar  the character used as the delimiter,
   *  <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str, char separatorChar) {
    return splitWorker(str, separatorChar, true);
  }

  /**
   * Performs the logic for the <code>split</code> and 
   * <code>splitPreserveAllTokens</code> methods that do not return a
   * maximum array length.
   *
   * @param str  the String to parse, may be <code>null</code>
   * @param separatorChar the separate character
   * @param preserveAllTokens if <code>true</code>, adjacent separators are
   * treated as empty token separators; if <code>false</code>, adjacent
   * separators are treated as one separator.
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  private static String[] splitWorker(String str, char separatorChar,
      boolean preserveAllTokens) {
    // Performance tuned for 2.0 (JDK1.4)

    if (str == null) {
      return null;
    }
    int len = str.length();
    if (len == 0) {
      return EMPTY_STRING_ARRAY;
    }
    List list = new ArrayList();
    int i = 0, start = 0;
    boolean match = false;
    boolean lastMatch = false;
    while (i < len) {
      if (str.charAt(i) == separatorChar) {
        if (match || preserveAllTokens) {
          list.add(str.substring(start, i));
          match = false;
          lastMatch = true;
        }
        start = ++i;
        continue;
      }
      lastMatch = false;
      match = true;
      i++;
    }
    if (match || (preserveAllTokens && lastMatch)) {
      list.add(str.substring(start, i));
    }
    return (String[]) list.toArray(new String[list.size()]);
  }

  /**
   * <p>Splits the provided text into an array, separators specified, 
   * preserving all tokens, including empty tokens created by adjacent
   * separators. This is an alternative to using StringTokenizer.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as separators for empty tokens.
   * For more control over the split use the StrTokenizer class.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> separatorChars splits on whitespace.</p>
   *
   * <pre>
   * StringUtils.splitPreserveAllTokens(null, *)           = null
   * StringUtils.splitPreserveAllTokens("", *)             = []
   * StringUtils.splitPreserveAllTokens("abc def", null)   = ["abc", "def"]
   * StringUtils.splitPreserveAllTokens("abc def", " ")    = ["abc", "def"]
   * StringUtils.splitPreserveAllTokens("abc  def", " ")   = ["abc", "", def"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":")   = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef:", ":")  = ["ab", "cd", "ef", ""]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef::", ":") = ["ab", "cd", "ef", "", ""]
   * StringUtils.splitPreserveAllTokens("ab::cd:ef", ":")  = ["ab", "", cd", "ef"]
   * StringUtils.splitPreserveAllTokens(":cd:ef", ":")     = ["", cd", "ef"]
   * StringUtils.splitPreserveAllTokens("::cd:ef", ":")    = ["", "", cd", "ef"]
   * StringUtils.splitPreserveAllTokens(":cd:ef:", ":")    = ["", cd", "ef", ""]
   * </pre>
   *
   * @param str  the String to parse, may be <code>null</code>
   * @param separatorChars  the characters used as the delimiters,
   *  <code>null</code> splits on whitespace
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str, String separatorChars) {
    return splitWorker(str, separatorChars, -1, true);
  }

  /**
   * <p>Splits the provided text into an array with a maximum length,
   * separators specified, preserving all tokens, including empty tokens 
   * created by adjacent separators.</p>
   *
   * <p>The separator is not included in the returned String array.
   * Adjacent separators are treated as separators for empty tokens.
   * Adjacent separators are treated as one separator.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * A <code>null</code> separatorChars splits on whitespace.</p>
   *
   * <p>If more than <code>max</code> delimited substrings are found, the last
   * returned string includes all characters after the first <code>max - 1</code>
   * returned strings (including separator characters).</p>
   *
   * <pre>
   * StringUtils.splitPreserveAllTokens(null, *, *)            = null
   * StringUtils.splitPreserveAllTokens("", *, *)              = []
   * StringUtils.splitPreserveAllTokens("ab de fg", null, 0)   = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 0) = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
   * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 2) = ["ab", "  de fg"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 3) = ["ab", "", " de fg"]
   * StringUtils.splitPreserveAllTokens("ab   de fg", null, 4) = ["ab", "", "", "de fg"]
   * </pre>
   *
   * @param str  the String to parse, may be <code>null</code>
   * @param separatorChars  the characters used as the delimiters,
   *  <code>null</code> splits on whitespace
   * @param max  the maximum number of elements to include in the
   *  array. A zero or negative value implies no limit
   * @return an array of parsed Strings, <code>null</code> if null String input
   * @since 2.1
   */
  public static String[] splitPreserveAllTokens(String str, String separatorChars, int max) {
    return splitWorker(str, separatorChars, max, true);
  }

  /**
   * Performs the logic for the <code>split</code> and 
   * <code>splitPreserveAllTokens</code> methods that return a maximum array 
   * length.
   *
   * @param str  the String to parse, may be <code>null</code>
   * @param separatorChars the separate character
   * @param max  the maximum number of elements to include in the
   *  array. A zero or negative value implies no limit.
   * @param preserveAllTokens if <code>true</code>, adjacent separators are
   * treated as empty token separators; if <code>false</code>, adjacent
   * separators are treated as one separator.
   * @return an array of parsed Strings, <code>null</code> if null String input
   */
  private static String[] splitWorker(String str, String separatorChars, int max,
      boolean preserveAllTokens) {
    // Performance tuned for 2.0 (JDK1.4)
    // Direct code is quicker than StringTokenizer.
    // Also, StringTokenizer uses isSpace() not isWhitespace()

    if (str == null) {
      return null;
    }
    int len = str.length();
    if (len == 0) {
      return EMPTY_STRING_ARRAY;
    }
    List list = new ArrayList();
    int sizePlus1 = 1;
    int i = 0, start = 0;
    boolean match = false;
    boolean lastMatch = false;
    if (separatorChars == null) {
      // Null separator means use whitespace
      while (i < len) {
        if (Character.isWhitespace(str.charAt(i))) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    } else if (separatorChars.length() == 1) {
      // Optimise 1 character case
      char sep = separatorChars.charAt(0);
      while (i < len) {
        if (str.charAt(i) == sep) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    } else {
      // standard case
      while (i < len) {
        if (separatorChars.indexOf(str.charAt(i)) >= 0) {
          if (match || preserveAllTokens) {
            lastMatch = true;
            if (sizePlus1++ == max) {
              i = len;
              lastMatch = false;
            }
            list.add(str.substring(start, i));
            match = false;
          }
          start = ++i;
          continue;
        }
        lastMatch = false;
        match = true;
        i++;
      }
    }
    if (match || (preserveAllTokens && lastMatch)) {
      list.add(str.substring(start, i));
    }
    return (String[]) list.toArray(new String[list.size()]);
  }

  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No separator is added to the joined String.
   * Null objects or empty strings within the array are represented by
   * empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null)            = null
   * StringUtils.join([])              = ""
   * StringUtils.join([null])          = ""
   * StringUtils.join(["a", "b", "c"]) = "abc"
   * StringUtils.join([null, "", "a"]) = "a"
   * </pre>
   *
   * @param array  the array of values to join together, may be null
   * @return the joined String, <code>null</code> if null array input
   * @since 2.0
   */
  public static String join(Object[] array) {
    return join(array, null);
  }

  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * Null objects or empty strings within the array are represented by
   * empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null, *)               = null
   * StringUtils.join([], *)                 = ""
   * StringUtils.join([null], *)             = ""
   * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
   * StringUtils.join(["a", "b", "c"], null) = "abc"
   * StringUtils.join([null, "", "a"], ';')  = ";;a"
   * </pre>
   *
   * @param array  the array of values to join together, may be null
   * @param separator  the separator character to use
   * @return the joined String, <code>null</code> if null array input
   * @since 2.0
   */
  public static String join(Object[] array, char separator) {
    if (array == null) {
      return null;
    }
    int arraySize = array.length;
    int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].toString()
        .length()) + 1)
        * arraySize);
    StringBuffer buf = new StringBuffer(bufSize);

    for (int i = 0; i < arraySize; i++) {
      if (i > 0) {
        buf.append(separator);
      }
      if (array[i] != null) {
        buf.append(array[i]);
      }
    }
    return buf.toString();
  }

  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * A <code>null</code> separator is the same as an empty String ("").
   * Null objects or empty strings within the array are represented by
   * empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null, *)                = null
   * StringUtils.join([], *)                  = ""
   * StringUtils.join([null], *)              = ""
   * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
   * StringUtils.join(["a", "b", "c"], null)  = "abc"
   * StringUtils.join(["a", "b", "c"], "")    = "abc"
   * StringUtils.join([null, "", "a"], ',')   = ",,a"
   * </pre>
   *
   * @param array  the array of values to join together, may be null
   * @param separator  the separator character to use, null treated as ""
   * @return the joined String, <code>null</code> if null array input
   */
  public static String join(Object[] array, String separator) {
    if (array == null) {
      return null;
    }
    if (separator == null) {
      separator = "";
    }
    int arraySize = array.length;

    // ArraySize ==  0: Len = 0
    // ArraySize > 0:   Len = NofStrings *(len(firstString) + len(separator))
    //           (Assuming that all Strings are roughly equally long)
    int bufSize = ((arraySize == 0) ? 0 : arraySize
        * ((array[0] == null ? 16 : array[0].toString().length()) + separator.length()));

    StringBuffer buf = new StringBuffer(bufSize);

    for (int i = 0; i < arraySize; i++) {
      if (i > 0) {
        buf.append(separator);
      }
      if (array[i] != null) {
        buf.append(array[i]);
      }
    }
    return buf.toString();
  }

  /**
   * <p>Joins the elements of the provided <code>Iterator</code> into
   * a single String containing the provided elements.</p>
   *
   * <p>No delimiter is added before or after the list. Null objects or empty
   * strings within the iteration are represented by empty strings.</p>
   *
   * <p>See the examples here: {@link #join(Object[],char)}. </p>
   *
   * @param iterator  the <code>Iterator</code> of values to join together, may be null
   * @param separator  the separator character to use
   * @return the joined String, <code>null</code> if null iterator input
   * @since 2.0
   */
  public static String join(Iterator iterator, char separator) {
    if (iterator == null) {
      return null;
    }
    StringBuffer buf = new StringBuffer(256); // Java default is 16, probably too small
    while (iterator.hasNext()) {
      Object obj = iterator.next();
      if (obj != null) {
        buf.append(obj);
      }
      if (iterator.hasNext()) {
        buf.append(separator);
      }
    }
    return buf.toString();
  }

  /**
   * <p>Joins the elements of the provided <code>Iterator</code> into
   * a single String containing the provided elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * A <code>null</code> separator is the same as an empty String ("").</p>
   *
   * <p>See the examples here: {@link #join(Object[],String)}. </p>
   *
   * @param iterator  the <code>Iterator</code> of values to join together, may be null
   * @param separator  the separator character to use, null treated as ""
   * @return the joined String, <code>null</code> if null iterator input
   */
  public static String join(Iterator iterator, String separator) {
    if (iterator == null) {
      return null;
    }
    StringBuffer buf = new StringBuffer(256); // Java default is 16, probably too small
    while (iterator.hasNext()) {
      Object obj = iterator.next();
      if (obj != null) {
        buf.append(obj);
      }
      if ((separator != null) && iterator.hasNext()) {
        buf.append(separator);
      }
    }
    return buf.toString();
  }

  /**
   * <p>Returns either the passed in String,
   * or if the String is <code>null</code>, an empty String ("").</p>
   *
   * <pre>
   * StringUtils.defaultString(null)  = ""
   * StringUtils.defaultString("")    = ""
   * StringUtils.defaultString("bat") = "bat"
   * </pre>
   *
   * @see String#valueOf(Object)
   * @param str  the String to check, may be null
   * @return the passed in String, or the empty String if it
   *  was <code>null</code>
   */
  public static String defaultString(String str) {
    return str == null ? "" : str;
  }

  /**
   * <p>Returns either the passed in String, or if the String is
   * <code>null</code>, the value of <code>defaultStr</code>.</p>
   *
   * <pre>
   * StringUtils.defaultString(null, "NULL")  = "NULL"
   * StringUtils.defaultString("", "NULL")    = ""
   * StringUtils.defaultString("bat", "NULL") = "bat"
   * </pre>
   *
   * @see String#valueOf(Object)
   * @param str  the String to check, may be null
   * @param defaultStr  the default String to return
   *  if the input is <code>null</code>, may be null
   * @return the passed in String, or the default if it was <code>null</code>
   */
  public static String defaultString(String str, String defaultStr) {
    return str == null ? defaultStr : str;
  }

  /**
   * <p>Returns either the passed in String, or if the String is
   * empty or <code>null</code>, the value of <code>defaultStr</code>.</p>
   *
   * <pre>
   * StringUtils.defaultIfEmpty(null, "NULL")  = "NULL"
   * StringUtils.defaultIfEmpty("", "NULL")    = "NULL"
   * StringUtils.defaultIfEmpty("bat", "NULL") = "bat"
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param defaultStr  the default String to return
   *  if the input is empty ("") or <code>null</code>, may be null
   * @return the passed in String, or the default
   */
  public static String defaultIfEmpty(String str, String defaultStr) {
    return isEmpty(str) ? defaultStr : str;
  }

  /**
   * <p>Capitalizes a String changing the first letter to title case as
   * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
   *
   * A <code>null</code> input String returns <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.capitalize(null)  = null
   * StringUtils.capitalize("")    = ""
   * StringUtils.capitalize("cat") = "Cat"
   * StringUtils.capitalize("cAt") = "CAt"
   * </pre>
   *
   * @param str  the String to capitalize, may be null
   * @return the capitalized String, <code>null</code> if null String input
   * @since 2.0
   */
  public static String capitalize(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    return new StringBuffer(strLen).append(Character.toTitleCase(str.charAt(0))).append(
        str.substring(1)).toString();
  }

  /**
   * <p>Checks if String contains a search character, handling <code>null</code>.
   * This method uses {@link String#indexOf(int)}.</p>
   *
   * <p>A <code>null</code> or empty ("") String will return <code>false</code>.</p>
   *
   * <pre>
   * StringUtils.contains(null, *)    = false
   * StringUtils.contains("", *)      = false
   * StringUtils.contains("abc", 'a') = true
   * StringUtils.contains("abc", 'z') = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param searchChar  the character to find
   * @return true if the String contains the search character,
   *  false if not or <code>null</code> string input
   * @since 2.0
   */
  public static boolean contains(String str, char searchChar) {
    if (isEmpty(str)) {
      return false;
    }
    return str.indexOf(searchChar) >= 0;
  }

  /**
   * <p>Checks if String contains a search String, handling <code>null</code>.
   * This method uses {@link String#indexOf(int)}.</p>
   *
   * <p>A <code>null</code> String will return <code>false</code>.</p>
   *
   * <pre>
   * StringUtils.contains(null, *)     = false
   * StringUtils.contains(*, null)     = false
   * StringUtils.contains("", "")      = true
   * StringUtils.contains("abc", "")   = true
   * StringUtils.contains("abc", "a")  = true
   * StringUtils.contains("abc", "z")  = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param searchStr  the String to find, may be null
   * @return true if the String contains the search String,
   *  false if not or <code>null</code> string input
   * @since 2.0
   */
  public static boolean contains(String str, String searchStr) {
    if (str == null || searchStr == null) {
      return false;
    }
    return str.indexOf(searchStr) >= 0;
  }
  
  /**
   * An empty immutable <code>String</code> array.
   */
  public static final String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * <p>Compares two objects for equality, where either one or both
   * objects may be <code>null</code>.</p>
   *
   * <pre>
   * ObjectUtils.equals(null, null)                  = true
   * ObjectUtils.equals(null, "")                    = false
   * ObjectUtils.equals("", null)                    = false
   * ObjectUtils.equals("", "")                      = true
   * ObjectUtils.equals(Boolean.TRUE, null)          = false
   * ObjectUtils.equals(Boolean.TRUE, "true")        = false
   * ObjectUtils.equals(Boolean.TRUE, Boolean.TRUE)  = true
   * ObjectUtils.equals(Boolean.TRUE, Boolean.FALSE) = false
   * </pre>
   *
   * @param object1  the first object, may be <code>null</code>
   * @param object2  the second object, may be <code>null</code>
   * @return <code>true</code> if the values of both objects are the same
   */
  public static boolean equals(Object object1, Object object2) {
      if (object1 == object2) {
          return true;
      }
      if ((object1 == null) || (object2 == null)) {
          return false;
      }
      return object1.equals(object2);
  }

  /**
   * <p>A way to get the entire nested stack-trace of an throwable.</p>
   *
   * @param throwable  the <code>Throwable</code> to be examined
   * @return the nested stack trace, with the root cause first
   * @since 2.0
   */
  public static String getFullStackTrace(Throwable throwable) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      Throwable[] ts = getThrowables(throwable);
      for (int i = 0; i < ts.length; i++) {
          ts[i].printStackTrace(pw);
          if (isNestedThrowable(ts[i])) {
              break;
          }
      }
      return sw.getBuffer().toString();
  }
  
  /** true or false for if we know if this is a class or not */
  private static Map<String, Boolean> jexlKnowsIfClass = new HashMap<String, Boolean>();
  
  /** class object for this string */
  private static Map<String, Class<?>> jexlClass = new HashMap<String, Class<?>>();
  
  /** pattern to see if class or not */
  private static Pattern jexlClassPattern = Pattern.compile("^[a-zA-Z0-9_.]*\\.[A-Z][a-zA-Z0-9_]*$");

  /**
   * 
   */
  private static class GrouperMapContext extends MapContext {

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
     * @see org.apache.commons.jexl2.MapContext#get(java.lang.String)
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
     * @see org.apache.commons.jexl2.MapContext#has(java.lang.String)
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
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String substituteExpressionLanguage(String stringToParse, Map<String, Object> variableMap) {
    //by default dont allow static classes
    return substituteExpressionLanguage(stringToParse, variableMap, false, false);

  }

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @param allowStaticClasses if true allow static classes not registered with context
   * @return the string
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public static String substituteExpressionLanguage(String stringToParse, 
      Map<String, Object> variableMap, boolean allowStaticClasses) {
    return substituteExpressionLanguage(stringToParse, variableMap, allowStaticClasses, false);
  }

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @param allowStaticClasses if true allow static classes not registered with context
   * @param silent if silent mode, swallow exceptions (warn), and dont warn when variable not found
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String substituteExpressionLanguage(String stringToParse, 
      Map<String, Object> variableMap, boolean allowStaticClasses, boolean silent) {
    return substituteExpressionLanguage(stringToParse, variableMap, allowStaticClasses, silent, false);
  }

  /**
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @param allowStaticClasses if true allow static classes not registered with context
   * @param silent if silent mode, swallow exceptions (warn), and dont warn when variable not found
   * @param lenient false if undefined variables should throw an exception.  if lenient is true (default)
   * then undefined variables are null
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String substituteExpressionLanguage(String stringToParse, 
      Map<String, Object> variableMap, boolean allowStaticClasses, boolean silent, boolean lenient) {
    if (GrouperUtil.isBlank(stringToParse)) {
      return stringToParse;
    }
    String overallResult = null;
    Exception exception = null;
    try {
      JexlContext jc = allowStaticClasses ? new GrouperMapContext() : new MapContext();
        
      
      
      int index = 0;
      
      for (String key: variableMap.keySet()) {
        jc.set(key, variableMap.get(key));
      }
      
      //allow utility methods
      jc.set("grouperUtil", new GrouperUtilElSafe());
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
          if (!lenient && StringUtils.trimToEmpty(je.getMessage()).contains("undefined variable")) {
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
            throw new ExpressionLanguageMissingVariableException(message, je);
          }
          throw je;
        }
          
        if (o == null) {
          LOG.warn("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
              + GrouperUtil.toStringForLog(variableMap.keySet()));
        }
        
        if (o instanceof RuntimeException) {
          throw (RuntimeException)o;
        }
        
        result.append(o);
        
      }
      
      result.append(stringToParse.substring(index, stringToParse.length()));
      overallResult = result.toString();
      return overallResult;
      
    } catch (HookVeto hv) {
      exception = hv;
      throw hv;
    } catch (Exception e) {
      exception = e;
      if (e instanceof ExpressionLanguageMissingVariableException) {
        throw (ExpressionLanguageMissingVariableException)e;
      }
      throw new RuntimeException("Error substituting string: '" + stringToParse + "'", e);
    } finally {
      if (LOG.isDebugEnabled()) {
        Set<String> keysSet = GrouperUtil.nonNull(variableMap).keySet();
        keysSet.add("grouperUtil");
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
          if (exception instanceof HookVeto) {
            logMessage.append(", it was vetoed: " + exception);
          } else {
            logMessage.append(", and exception: " + exception + ", " + ExceptionUtils.getFullStackTrace(exception));
          }
        }
        LOG.debug(logMessage.toString());
      }
    }
  }

  /**
   * <p>Returns the list of <code>Throwable</code> objects in the
   * exception chain.</p>
   * 
   * <p>A throwable without cause will return an array containing
   * one element - the input throwable.
   * A throwable with one cause will return an array containing
   * two elements. - the input throwable and the cause throwable.
   * A <code>null</code> throwable will return an array size zero.</p>
   *
   * @param throwable  the throwable to inspect, may be null
   * @return the array of throwables, never null
   */
  public static Throwable[] getThrowables(Throwable throwable) {
      List list = new ArrayList();
      while (throwable != null) {
          list.add(throwable);
          throwable = getCause(throwable);
      }
      return (Throwable[]) list.toArray(new Throwable[list.size()]);
  }
  
  /**
   * <p>The names of methods commonly used to access a wrapped exception.</p>
   */
  private static String[] CAUSE_METHOD_NAMES = {
      "getCause",
      "getNextException",
      "getTargetException",
      "getException",
      "getSourceException",
      "getRootCause",
      "getCausedByException",
      "getNested",
      "getLinkedException",
      "getNestedException",
      "getLinkedCause",
      "getThrowable",
  };

  /**
   * <p>Checks whether this <code>Throwable</code> class can store a cause.</p>
   * 
   * <p>This method does <b>not</b> check whether it actually does store a cause.<p>
   *
   * @param throwable  the <code>Throwable</code> to examine, may be null
   * @return boolean <code>true</code> if nested otherwise <code>false</code>
   * @since 2.0
   */
  public static boolean isNestedThrowable(Throwable throwable) {
      if (throwable == null) {
          return false;
      }

      if (throwable instanceof Nestable) {
          return true;
      } else if (throwable instanceof SQLException) {
          return true;
      } else if (throwable instanceof InvocationTargetException) {
          return true;
      } else if (isThrowableNested()) {
          return true;
      }

      Class cls = throwable.getClass();
      for (int i = 0, isize = CAUSE_METHOD_NAMES.length; i < isize; i++) {
          try {
              Method method = cls.getMethod(CAUSE_METHOD_NAMES[i], (Class[])null);
              if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
                  return true;
              }
          } catch (NoSuchMethodException ignored) {
          } catch (SecurityException ignored) {
          }
      }

      try {
          Field field = cls.getField("detail");
          if (field != null) {
              return true;
          }
      } catch (NoSuchFieldException ignored) {
      } catch (SecurityException ignored) {
      }

      return false;
  }

  /**
   * <p>The Method object for JDK1.4 getCause.</p>
   */
  private static final Method THROWABLE_CAUSE_METHOD;
  static {
      Method getCauseMethod;
      try {
          getCauseMethod = Throwable.class.getMethod("getCause", (Class[])null);
      } catch (Exception e) {
          getCauseMethod = null;
      }
      THROWABLE_CAUSE_METHOD = getCauseMethod;
  }
  
  /**
   * <p>Checks if the Throwable class has a <code>getCause</code> method.</p>
   * 
   * <p>This is true for JDK 1.4 and above.</p>
   * 
   * @return true if Throwable is nestable
   * @since 2.0
   */
  public static boolean isThrowableNested() {
      return THROWABLE_CAUSE_METHOD != null;
  }

  /**
   * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
   * 
   * <p>The method searches for methods with specific names that return a 
   * <code>Throwable</code> object. This will pick up most wrapping exceptions,
   * including those from JDK 1.4, and
   * {@link org.apache.commons.lang.exception.NestableException NestableException}.</p>
   *
   * <p>The default list searched for are:</p>
   * <ul>
   *  <li><code>getCause()</code></li>
   *  <li><code>getNextException()</code></li>
   *  <li><code>getTargetException()</code></li>
   *  <li><code>getException()</code></li>
   *  <li><code>getSourceException()</code></li>
   *  <li><code>getRootCause()</code></li>
   *  <li><code>getCausedByException()</code></li>
   *  <li><code>getNested()</code></li>
   * </ul>
   * 
   * <p>In the absence of any such method, the object is inspected for a
   * <code>detail</code> field assignable to a <code>Throwable</code>.</p>
   * 
   * <p>If none of the above is found, returns <code>null</code>.</p>
   *
   * @param throwable  the throwable to introspect for a cause, may be null
   * @return the cause of the <code>Throwable</code>,
   *  <code>null</code> if none found or null throwable input
   * @since 1.0
   */
  public static Throwable getCause(Throwable throwable) {
      return getCause(throwable, CAUSE_METHOD_NAMES);
  }

  /**
   * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
   * 
   * <ol>
   * <li>Try known exception types.</li>
   * <li>Try the supplied array of method names.</li>
   * <li>Try the field 'detail'.</li>
   * </ol>
   * 
   * <p>A <code>null</code> set of method names means use the default set.
   * A <code>null</code> in the set of method names will be ignored.</p>
   *
   * @param throwable  the throwable to introspect for a cause, may be null
   * @param methodNames  the method names, null treated as default set
   * @return the cause of the <code>Throwable</code>,
   *  <code>null</code> if none found or null throwable input
   * @since 1.0
   */
  public static Throwable getCause(Throwable throwable, String[] methodNames) {
      if (throwable == null) {
          return null;
      }
      Throwable cause = getCauseUsingWellKnownTypes(throwable);
      if (cause == null) {
          if (methodNames == null) {
              methodNames = CAUSE_METHOD_NAMES;
          }
          for (int i = 0; i < methodNames.length; i++) {
              String methodName = methodNames[i];
              if (methodName != null) {
                  cause = getCauseUsingMethodName(throwable, methodName);
                  if (cause != null) {
                      break;
                  }
              }
          }

          if (cause == null) {
              cause = getCauseUsingFieldName(throwable, "detail");
          }
      }
      return cause;
  }

  /**
   * <p>Finds a <code>Throwable</code> by method name.</p>
   * 
   * @param throwable  the exception to examine
   * @param methodName  the name of the method to find and invoke
   * @return the wrapped exception, or <code>null</code> if not found
   */
  private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
      Method method = null;
      try {
          method = throwable.getClass().getMethod(methodName, (Class[])null);
      } catch (NoSuchMethodException ignored) {
      } catch (SecurityException ignored) {
      }

      if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
          try {
              return (Throwable) method.invoke(throwable, EMPTY_OBJECT_ARRAY);
          } catch (IllegalAccessException ignored) {
          } catch (IllegalArgumentException ignored) {
          } catch (InvocationTargetException ignored) {
          }
      }
      return null;
  }

  /**
   * <p>Finds a <code>Throwable</code> by field name.</p>
   * 
   * @param throwable  the exception to examine
   * @param fieldName  the name of the attribute to examine
   * @return the wrapped exception, or <code>null</code> if not found
   */
  private static Throwable getCauseUsingFieldName(Throwable throwable, String fieldName) {
      Field field = null;
      try {
          field = throwable.getClass().getField(fieldName);
      } catch (NoSuchFieldException ignored) {
      } catch (SecurityException ignored) {
      }

      if (field != null && Throwable.class.isAssignableFrom(field.getType())) {
          try {
              return (Throwable) field.get(throwable);
          } catch (IllegalAccessException ignored) {
          } catch (IllegalArgumentException ignored) {
          }
      }
      return null;
  }

  /**
   * <p>Finds a <code>Throwable</code> for known types.</p>
   * 
   * <p>Uses <code>instanceof</code> checks to examine the exception,
   * looking for well known types which could contain chained or
   * wrapped exceptions.</p>
   *
   * @param throwable  the exception to examine
   * @return the wrapped exception, or <code>null</code> if not found
   */
  private static Throwable getCauseUsingWellKnownTypes(Throwable throwable) {
      if (throwable instanceof Nestable) {
          return ((Nestable) throwable).getCause();
      } else if (throwable instanceof SQLException) {
          return ((SQLException) throwable).getNextException();
      } else if (throwable instanceof InvocationTargetException) {
          return ((InvocationTargetException) throwable).getTargetException();
      } else {
          return null;
      }
  }

  /**
   * An empty immutable <code>Object</code> array.
   */
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  
  /**
   * Copy bytes from an <code>InputStream</code> to chars on a
   * <code>Writer</code> using the default character encoding of the platform.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * <code>BufferedInputStream</code>.
   * <p>
   * This method uses {@link InputStreamReader}.
   *
   * @param input  the <code>InputStream</code> to read from
   * @param output  the <code>Writer</code> to write to
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static void copy(InputStream input, Writer output)
          throws IOException {
      InputStreamReader in = new InputStreamReader(input);
      copy(in, output);
  }

  /**
   * get a jar file from a sample class
   * @param sampleClass
   * @param printError if error should be printed when there is a problem
   * @return the jar file
   */
  public static File jarFile(Class sampleClass, boolean printError) {
    try {
      CodeSource codeSource = sampleClass.getProtectionDomain().getCodeSource();
      if (codeSource != null && codeSource.getLocation() != null) {
        return new File(codeSource.getLocation().getFile());
      }
      String resourcePath = sampleClass.getName();
      resourcePath = resourcePath.replace('.', '/') + ".class";
      URL url = computeUrl(resourcePath, true);
      String urlPath = url.toString();
      
      if (urlPath.startsWith("jar:")) {
        urlPath = urlPath.substring(4);
      }
      if (urlPath.startsWith("file:")) {
        urlPath = urlPath.substring(5);
      }
      urlPath = prefixOrSuffix(urlPath, "!", true); 
  
      File file = new File(urlPath);
      if (urlPath.endsWith(".jar") && file.exists() && file.isFile()) {
        return file;
      }
    } catch (Exception e) {
      if (printError) {
        e.printStackTrace();
        System.err.println("Cant find jar for class: " + sampleClass + ", " + e.getMessage());
      }
    }
    return null;
  }

  /**
   * strip the last slash (/ or \) from a string if it exists
   * 
   * @param input
   * 
   * @return input - the last / or \
   */
  public static String stripLastSlashIfExists(String input) {
    if ((input == null) || (input.length() == 0)) {
      return null;
    }

    char lastChar = input.charAt(input.length() - 1);

    if ((lastChar == '\\') || (lastChar == '/')) {
      return input.substring(0, input.length() - 1);
    }

    return input;
  }

  /**
   * retrieve a password from stdin
   * @param dontMask
   * @param prompt to print to user
   * @return the password
   */
  public static String retrievePasswordFromStdin(boolean dontMask, String prompt) {
    String passwordString = null;

    if (dontMask) {

      System.out.print(prompt);
      //  open up standard input 
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 

      //  read the username from the command-line; need to use try/catch with the 
      //  readLine() method 
      try { 
         passwordString = br.readLine(); 
      } catch (IOException ioe) { 
         System.out.println("IO error! " + getFullStackTrace(ioe));
         System.exit(1); 
      } 

    } else {
      char password[] = null;
      try {
        password = retrievePasswordFromStdin(System.in, prompt);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      passwordString = String.valueOf(password);
    } 
    return passwordString;
    
  }

  /**
   * @param in stream to be used (e.g. System.in)
   * @param prompt The prompt to display to the user.
   * @return The password as entered by the user.
   * @throws IOException 
   */
  public static final char[] retrievePasswordFromStdin(InputStream in, String prompt) throws IOException {
    MaskingThread maskingthread = new MaskingThread(prompt);

    Thread thread = new Thread(maskingthread);
    thread.start();

    char[] lineBuffer;
    char[] buf;

    buf = lineBuffer = new char[128];

    int room = buf.length;
    int offset = 0;
    int c;

    loop: while (true) {
      switch (c = in.read()) {
        case -1:
        case '\n':
          break loop;

        case '\r':
          int c2 = in.read();
          if ((c2 != '\n') && (c2 != -1)) {
            if (!(in instanceof PushbackInputStream)) {
              in = new PushbackInputStream(in);
            }
            ((PushbackInputStream) in).unread(c2);
          } else {
            break loop;
          }

        default:
          if (--room < 0) {
            buf = new char[offset + 128];
            room = buf.length - offset - 1;
            System.arraycopy(lineBuffer, 0, buf, 0, offset);
            Arrays.fill(lineBuffer, ' ');
            lineBuffer = buf;
          }
          buf[offset++] = (char) c;
          break;
      }
    }
    maskingthread.stopMasking();
    if (offset == 0) {
      return null;
    }
    char[] ret = new char[offset];
    System.arraycopy(buf, 0, ret, 0, offset);
    Arrays.fill(buf, ' ');
    return ret;
  }

  /**
   * thread to mask input
   */
  static class MaskingThread extends Thread {

    /** stop */
    private volatile boolean stop;

    /** echo char, this doesnt work correctly, so make a space so people dont notice...  
     * prints out too many */
    private char echochar = ' ';

    /**
     *@param prompt The prompt displayed to the user
     */
    public MaskingThread(String prompt) {
      System.out.print(prompt);
    }

    /**
     * Begin masking until asked to stop.
     */
    @Override
    public void run() {

      int priority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

      try {
        this.stop = true;
        while (this.stop) {
          System.out.print("\010" + this.echochar);
          try {
            // attempt masking at this rate
            Thread.sleep(1);
          } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      } finally { // restore the original priority
        Thread.currentThread().setPriority(priority);
      }
    }

    /**
     * Instruct the thread to stop masking.
     */
    public void stopMasking() {
      this.stop = false;
    }
  }

  /**
   * make sure a value exists in properties
   * @param resourceName
   * @param key
   * @return true if ok, false if not
   */
  public static boolean propertyValueRequired(String resourceName, String key) {
    Properties properties = propertiesFromResourceName(resourceName);
    String value = propertiesValue(properties, key);
    if (!StringUtils.isBlank(value)) {
      return true;
    }
    String error = "Cant find property " + key + " in resource: " + resourceName + ", it is required";
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }

  /**
   * make sure a value is boolean in properties
   * @param resourceName
   * @param key
   * @param required
   * @return true if ok, false if not
   */
  public static boolean propertyValueBoolean(String resourceName, String key, boolean required) {
    
    if (required && !propertyValueRequired(resourceName, key)) {
      return false;
    }
  
    Properties properties = propertiesFromResourceName(resourceName);
    String value = propertiesValue(properties, key);
    //maybe ok not there
    if (!required && StringUtils.isBlank(value)) {
      return true;
    }
    try {
      booleanValue(value);
      return true;
    } catch (Exception e) {
      
    }
    String error = "Expecting true or false property " + key + " in resource: " + resourceName + ", but is '" + value + "'";
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }

  /**
   * make sure a property is a class of a certain type
   * @param resourceName
   * @param key
   * @param classType
   * @param required 
   * @return true if ok
   */
  public static boolean propertyValueClass(String resourceName, 
      String key, Class<?> classType, boolean required) {
  
    if (required && !propertyValueRequired(resourceName, key)) {
      return false;
    }
    Properties properties = propertiesFromResourceName(resourceName);
    String value = propertiesValue(properties, key);
  
    //maybe ok not there
    if (!required && StringUtils.isBlank(value)) {
      return true;
    }
    
    String extraError = "";
    try {
      
      
      Class<?> theClass = forName(value);
      if (classType.isAssignableFrom(theClass)) {
        return true;
      }
      extraError = " does not derive from class: " + classType.getSimpleName();
      
    } catch (Exception e) {
      extraError = ", " + ExceptionUtils.getFullStackTrace(e);
    }
    String error = "Cant process property " + key + " in resource: " + resourceName + ", the current" +
        " value is '" + value + "', which should be of type: " 
        + classType.getName() + extraError;
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }
  
  /**
   * <p>Strips any of a set of characters from the start of a String.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * An empty string ("") input returns the empty string.</p>
   *
   * <p>If the stripChars String is <code>null</code>, whitespace is
   * stripped as defined by {@link Character#isWhitespace(char)}.</p>
   *
   * <pre>
   * StringUtils.stripStart(null, *)          = null
   * StringUtils.stripStart("", *)            = ""
   * StringUtils.stripStart("abc", "")        = "abc"
   * StringUtils.stripStart("abc", null)      = "abc"
   * StringUtils.stripStart("  abc", null)    = "abc"
   * StringUtils.stripStart("abc  ", null)    = "abc  "
   * StringUtils.stripStart(" abc ", null)    = "abc "
   * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
   * </pre>
   *
   * @param str  the String to remove characters from, may be null
   * @param stripChars  the characters to remove, null treated as whitespace
   * @return the stripped String, <code>null</code> if null String input
   */
  public static String stripStart(String str, String stripChars) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    int start = 0;
    if (stripChars == null) {
      while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
        start++;
      }
    } else if (stripChars.length() == 0) {
      return str;
    } else {
      while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
        start++;
      }
    }
    return str.substring(start);
  }

  /**
   * <p>Strips any of a set of characters from the end of a String.</p>
   *
   * <p>A <code>null</code> input String returns <code>null</code>.
   * An empty string ("") input returns the empty string.</p>
   *
   * <p>If the stripChars String is <code>null</code>, whitespace is
   * stripped as defined by {@link Character#isWhitespace(char)}.</p>
   *
   * <pre>
   * StringUtils.stripEnd(null, *)          = null
   * StringUtils.stripEnd("", *)            = ""
   * StringUtils.stripEnd("abc", "")        = "abc"
   * StringUtils.stripEnd("abc", null)      = "abc"
   * StringUtils.stripEnd("  abc", null)    = "  abc"
   * StringUtils.stripEnd("abc  ", null)    = "abc"
   * StringUtils.stripEnd(" abc ", null)    = " abc"
   * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
   * </pre>
   *
   * @param str  the String to remove characters from, may be null
   * @param stripChars  the characters to remove, null treated as whitespace
   * @return the stripped String, <code>null</code> if null String input
   */
  public static String stripEnd(String str, String stripChars) {
    int end;
    if (str == null || (end = str.length()) == 0) {
      return str;
    }

    if (stripChars == null) {
      while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
        end--;
      }
    } else if (stripChars.length() == 0) {
      return str;
    } else {
      while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
        end--;
      }
    }
    return str.substring(0, end);
  }

  /**
   * The empty String <code>""</code>.
   * @since 2.0
   */
  public static final String EMPTY = "";

  /**
   * Represents a failed index search.
   * @since 2.1
   */
  public static final int INDEX_NOT_FOUND = -1;

  /**
   * <p>The maximum size to which the padding constant(s) can expand.</p>
   */
  private static final int PAD_LIMIT = 8192;

  /**
   * <p>An array of <code>String</code>s used for padding.</p>
   *
   * <p>Used for efficient space padding. The length of each String expands as needed.</p>
   */
  private static final String[] PADDING = new String[Character.MAX_VALUE];

  static {
    // space padding is most common, start with 64 chars
    PADDING[32] = "                                                                ";
  }

  /**
   * <p>Repeat a String <code>repeat</code> times to form a
   * new String.</p>
   *
   * <pre>
   * StringUtils.repeat(null, 2) = null
   * StringUtils.repeat("", 0)   = ""
   * StringUtils.repeat("", 2)   = ""
   * StringUtils.repeat("a", 3)  = "aaa"
   * StringUtils.repeat("ab", 2) = "abab"
   * StringUtils.repeat("a", -2) = ""
   * </pre>
   *
   * @param str  the String to repeat, may be null
   * @param repeat  number of times to repeat str, negative treated as zero
   * @return a new String consisting of the original String repeated,
   *  <code>null</code> if null String input
   */
  public static String repeat(String str, int repeat) {
    // Performance tuned for 2.0 (JDK1.4)

    if (str == null) {
      return null;
    }
    if (repeat <= 0) {
      return EMPTY;
    }
    int inputLength = str.length();
    if (repeat == 1 || inputLength == 0) {
      return str;
    }
    if (inputLength == 1 && repeat <= PAD_LIMIT) {
      return padding(repeat, str.charAt(0));
    }

    int outputLength = inputLength * repeat;
    switch (inputLength) {
      case 1:
        char ch = str.charAt(0);
        char[] output1 = new char[outputLength];
        for (int i = repeat - 1; i >= 0; i--) {
          output1[i] = ch;
        }
        return new String(output1);
      case 2:
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        char[] output2 = new char[outputLength];
        for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
          output2[i] = ch0;
          output2[i + 1] = ch1;
        }
        return new String(output2);
      default:
        StringBuffer buf = new StringBuffer(outputLength);
        for (int i = 0; i < repeat; i++) {
          buf.append(str);
        }
        return buf.toString();
    }
  }

  /**
   * <p>Returns padding using the specified delimiter repeated
   * to a given length.</p>
   *
   * <pre>
   * StringUtils.padding(0, 'e')  = ""
   * StringUtils.padding(3, 'e')  = "eee"
   * StringUtils.padding(-2, 'e') = IndexOutOfBoundsException
   * </pre>
   *
   * @param repeat  number of times to repeat delim
   * @param padChar  character to repeat
   * @return String with repeated character
   * @throws IndexOutOfBoundsException if <code>repeat &lt; 0</code>
   */
  private static String padding(int repeat, char padChar) {
    // be careful of synchronization in this method
    // we are assuming that get and set from an array index is atomic
    String pad = PADDING[padChar];
    if (pad == null) {
      pad = String.valueOf(padChar);
    }
    while (pad.length() < repeat) {
      pad = pad.concat(pad);
    }
    PADDING[padChar] = pad;
    return pad.substring(0, repeat);
  }

  /**
   * <p>Right pad a String with spaces (' ').</p>
   *
   * <p>The String is padded to the size of <code>size</code>.</p>
   *
   * <pre>
   * StringUtils.rightPad(null, *)   = null
   * StringUtils.rightPad("", 3)     = "   "
   * StringUtils.rightPad("bat", 3)  = "bat"
   * StringUtils.rightPad("bat", 5)  = "bat  "
   * StringUtils.rightPad("bat", 1)  = "bat"
   * StringUtils.rightPad("bat", -1) = "bat"
   * </pre>
   *
   * @param str  the String to pad out, may be null
   * @param size  the size to pad to
   * @return right padded String or original String if no padding is necessary,
   *  <code>null</code> if null String input
   */
  public static String rightPad(String str, int size) {
    return rightPad(str, size, ' ');
  }

  /**
   * <p>Right pad a String with a specified character.</p>
   *
   * <p>The String is padded to the size of <code>size</code>.</p>
   *
   * <pre>
   * StringUtils.rightPad(null, *, *)     = null
   * StringUtils.rightPad("", 3, 'z')     = "zzz"
   * StringUtils.rightPad("bat", 3, 'z')  = "bat"
   * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
   * StringUtils.rightPad("bat", 1, 'z')  = "bat"
   * StringUtils.rightPad("bat", -1, 'z') = "bat"
   * </pre>
   *
   * @param str  the String to pad out, may be null
   * @param size  the size to pad to
   * @param padChar  the character to pad with
   * @return right padded String or original String if no padding is necessary,
   *  <code>null</code> if null String input
   * @since 2.0
   */
  public static String rightPad(String str, int size, char padChar) {
    if (str == null) {
      return null;
    }
    int pads = size - str.length();
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (pads > PAD_LIMIT) {
      return rightPad(str, size, String.valueOf(padChar));
    }
    return str.concat(padding(pads, padChar));
  }

  /**
   * <p>Right pad a String with a specified String.</p>
   *
   * <p>The String is padded to the size of <code>size</code>.</p>
   *
   * <pre>
   * StringUtils.rightPad(null, *, *)      = null
   * StringUtils.rightPad("", 3, "z")      = "zzz"
   * StringUtils.rightPad("bat", 3, "yz")  = "bat"
   * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
   * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
   * StringUtils.rightPad("bat", 1, "yz")  = "bat"
   * StringUtils.rightPad("bat", -1, "yz") = "bat"
   * StringUtils.rightPad("bat", 5, null)  = "bat  "
   * StringUtils.rightPad("bat", 5, "")    = "bat  "
   * </pre>
   *
   * @param str  the String to pad out, may be null
   * @param size  the size to pad to
   * @param padStr  the String to pad with, null or empty treated as single space
   * @return right padded String or original String if no padding is necessary,
   *  <code>null</code> if null String input
   */
  public static String rightPad(String str, int size, String padStr) {
    if (str == null) {
      return null;
    }
    if (isEmpty(padStr)) {
      padStr = " ";
    }
    int padLen = padStr.length();
    int strLen = str.length();
    int pads = size - strLen;
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (padLen == 1 && pads <= PAD_LIMIT) {
      return rightPad(str, size, padStr.charAt(0));
    }

    if (pads == padLen) {
      return str.concat(padStr);
    } else if (pads < padLen) {
      return str.concat(padStr.substring(0, pads));
    } else {
      char[] padding = new char[pads];
      char[] padChars = padStr.toCharArray();
      for (int i = 0; i < pads; i++) {
        padding[i] = padChars[i % padLen];
      }
      return str.concat(new String(padding));
    }
  }

  /**
   * <p>Left pad a String with spaces (' ').</p>
   *
   * <p>The String is padded to the size of <code>size<code>.</p>
   *
   * <pre>
   * StringUtils.leftPad(null, *)   = null
   * StringUtils.leftPad("", 3)     = "   "
   * StringUtils.leftPad("bat", 3)  = "bat"
   * StringUtils.leftPad("bat", 5)  = "  bat"
   * StringUtils.leftPad("bat", 1)  = "bat"
   * StringUtils.leftPad("bat", -1) = "bat"
   * </pre>
   *
   * @param str  the String to pad out, may be null
   * @param size  the size to pad to
   * @return left padded String or original String if no padding is necessary,
   *  <code>null</code> if null String input
   */
  public static String leftPad(String str, int size) {
    return leftPad(str, size, ' ');
  }

  /**
   * <p>Left pad a String with a specified character.</p>
   *
   * <p>Pad to a size of <code>size</code>.</p>
   *
   * <pre>
   * StringUtils.leftPad(null, *, *)     = null
   * StringUtils.leftPad("", 3, 'z')     = "zzz"
   * StringUtils.leftPad("bat", 3, 'z')  = "bat"
   * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
   * StringUtils.leftPad("bat", 1, 'z')  = "bat"
   * StringUtils.leftPad("bat", -1, 'z') = "bat"
   * </pre>
   *
   * @param str  the String to pad out, may be null
   * @param size  the size to pad to
   * @param padChar  the character to pad with
   * @return left padded String or original String if no padding is necessary,
   *  <code>null</code> if null String input
   * @since 2.0
   */
  public static String leftPad(String str, int size, char padChar) {
    if (str == null) {
      return null;
    }
    int pads = size - str.length();
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (pads > PAD_LIMIT) {
      return leftPad(str, size, String.valueOf(padChar));
    }
    return padding(pads, padChar).concat(str);
  }

  /**
   * <p>Left pad a String with a specified String.</p>
   *
   * <p>Pad to a size of <code>size</code>.</p>
   *
   * <pre>
   * StringUtils.leftPad(null, *, *)      = null
   * StringUtils.leftPad("", 3, "z")      = "zzz"
   * StringUtils.leftPad("bat", 3, "yz")  = "bat"
   * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
   * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
   * StringUtils.leftPad("bat", 1, "yz")  = "bat"
   * StringUtils.leftPad("bat", -1, "yz") = "bat"
   * StringUtils.leftPad("bat", 5, null)  = "  bat"
   * StringUtils.leftPad("bat", 5, "")    = "  bat"
   * </pre>
   *
   * @param str  the String to pad out, may be null
   * @param size  the size to pad to
   * @param padStr  the String to pad with, null or empty treated as single space
   * @return left padded String or original String if no padding is necessary,
   *  <code>null</code> if null String input
   */
  public static String leftPad(String str, int size, String padStr) {
    if (str == null) {
      return null;
    }
    if (isEmpty(padStr)) {
      padStr = " ";
    }
    int padLen = padStr.length();
    int strLen = str.length();
    int pads = size - strLen;
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (padLen == 1 && pads <= PAD_LIMIT) {
      return leftPad(str, size, padStr.charAt(0));
    }

    if (pads == padLen) {
      return padStr.concat(str);
    } else if (pads < padLen) {
      return padStr.substring(0, pads).concat(str);
    } else {
      char[] padding = new char[pads];
      char[] padChars = padStr.toCharArray();
      for (int i = 0; i < pads; i++) {
        padding[i] = padChars[i % padLen];
      }
      return new String(padding).concat(str);
    }
  }

  /**
   * <p>Gets the substring before the first occurrence of a separator.
   * The separator is not returned.</p>
   *
   * <p>A <code>null</code> string input will return <code>null</code>.
   * An empty ("") string input will return the empty string.
   * A <code>null</code> separator will return the input string.</p>
   *
   * <pre>
   * StringUtils.substringBefore(null, *)      = null
   * StringUtils.substringBefore("", *)        = ""
   * StringUtils.substringBefore("abc", "a")   = ""
   * StringUtils.substringBefore("abcba", "b") = "a"
   * StringUtils.substringBefore("abc", "c")   = "ab"
   * StringUtils.substringBefore("abc", "d")   = "abc"
   * StringUtils.substringBefore("abc", "")    = ""
   * StringUtils.substringBefore("abc", null)  = "abc"
   * </pre>
   *
   * @param str  the String to get a substring from, may be null
   * @param separator  the String to search for, may be null
   * @return the substring before the first occurrence of the separator,
   *  <code>null</code> if null String input
   * @since 2.0
   */
  public static String substringBefore(String str, String separator) {
    if (isEmpty(str) || separator == null) {
      return str;
    }
    if (separator.length() == 0) {
      return EMPTY;
    }
    int pos = str.indexOf(separator);
    if (pos == -1) {
      return str;
    }
    return str.substring(0, pos);
  }

  /**
   * <p>Gets the substring after the first occurrence of a separator.
   * The separator is not returned.</p>
   *
   * <p>A <code>null</code> string input will return <code>null</code>.
   * An empty ("") string input will return the empty string.
   * A <code>null</code> separator will return the empty string if the
   * input string is not <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.substringAfter(null, *)      = null
   * StringUtils.substringAfter("", *)        = ""
   * StringUtils.substringAfter(*, null)      = ""
   * StringUtils.substringAfter("abc", "a")   = "bc"
   * StringUtils.substringAfter("abcba", "b") = "cba"
   * StringUtils.substringAfter("abc", "c")   = ""
   * StringUtils.substringAfter("abc", "d")   = ""
   * StringUtils.substringAfter("abc", "")    = "abc"
   * </pre>
   *
   * @param str  the String to get a substring from, may be null
   * @param separator  the String to search for, may be null
   * @return the substring after the first occurrence of the separator,
   *  <code>null</code> if null String input
   * @since 2.0
   */
  public static String substringAfter(String str, String separator) {
    if (isEmpty(str)) {
      return str;
    }
    if (separator == null) {
      return EMPTY;
    }
    int pos = str.indexOf(separator);
    if (pos == -1) {
      return EMPTY;
    }
    return str.substring(pos + separator.length());
  }

  /**
   * <p>Gets the substring before the last occurrence of a separator.
   * The separator is not returned.</p>
   *
   * <p>A <code>null</code> string input will return <code>null</code>.
   * An empty ("") string input will return the empty string.
   * An empty or <code>null</code> separator will return the input string.</p>
   *
   * <pre>
   * StringUtils.substringBeforeLast(null, *)      = null
   * StringUtils.substringBeforeLast("", *)        = ""
   * StringUtils.substringBeforeLast("abcba", "b") = "abc"
   * StringUtils.substringBeforeLast("abc", "c")   = "ab"
   * StringUtils.substringBeforeLast("a", "a")     = ""
   * StringUtils.substringBeforeLast("a", "z")     = "a"
   * StringUtils.substringBeforeLast("a", null)    = "a"
   * StringUtils.substringBeforeLast("a", "")      = "a"
   * </pre>
   *
   * @param str  the String to get a substring from, may be null
   * @param separator  the String to search for, may be null
   * @return the substring before the last occurrence of the separator,
   *  <code>null</code> if null String input
   * @since 2.0
   */
  public static String substringBeforeLast(String str, String separator) {
    if (isEmpty(str) || isEmpty(separator)) {
      return str;
    }
    int pos = str.lastIndexOf(separator);
    if (pos == -1) {
      return str;
    }
    return str.substring(0, pos);
  }

  /**
   * <p>Gets the substring after the last occurrence of a separator.
   * The separator is not returned.</p>
   *
   * <p>A <code>null</code> string input will return <code>null</code>.
   * An empty ("") string input will return the empty string.
   * An empty or <code>null</code> separator will return the empty string if
   * the input string is not <code>null</code>.</p>
   *
   * <pre>
   * StringUtils.substringAfterLast(null, *)      = null
   * StringUtils.substringAfterLast("", *)        = ""
   * StringUtils.substringAfterLast(*, "")        = ""
   * StringUtils.substringAfterLast(*, null)      = ""
   * StringUtils.substringAfterLast("abc", "a")   = "bc"
   * StringUtils.substringAfterLast("abcba", "b") = "a"
   * StringUtils.substringAfterLast("abc", "c")   = ""
   * StringUtils.substringAfterLast("a", "a")     = ""
   * StringUtils.substringAfterLast("a", "z")     = ""
   * </pre>
   *
   * @param str  the String to get a substring from, may be null
   * @param separator  the String to search for, may be null
   * @return the substring after the last occurrence of the separator,
   *  <code>null</code> if null String input
   * @since 2.0
   */
  public static String substringAfterLast(String str, String separator) {
    if (isEmpty(str)) {
      return str;
    }
    if (isEmpty(separator)) {
      return EMPTY;
    }
    int pos = str.lastIndexOf(separator);
    if (pos == -1 || pos == (str.length() - separator.length())) {
      return EMPTY;
    }
    return str.substring(pos + separator.length());
  }
  
  /**
   * wait for input
   */
  public static void waitForInput() {
    System.out.print("Press enter to continue: ");
    BufferedReader stdin = null;

    try {
      stdin = new BufferedReader(new InputStreamReader(System.in));
      stdin.readLine();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * find an object (or objects) in a collection based on fields
   * @param <T>
   * @param collection
   * @param propertyNames
   * @param propertyValues
   * @return the object(s) or empty list if cant find
   */
  public static <T> T retrieveByProperties(Collection<T> collection, 
      List<String> propertyNames, List<Object> propertyValues) {
    List<T> list = retrieveListByProperties(collection, propertyNames, propertyValues);
    return listPopOne(list);
  }

  /**
   * find an object (or objects) in a collection based on fields
   * @param <T>
   * @param collection
   * @param propertyName
   * @param propertyValue
   * @return the object(s) or empty list if cant find
   */
  public static <T> T retrieveByProperty(Collection<T> collection, 
      String propertyName, Object propertyValue) {
    List<T> list = retrieveListByProperty(collection, propertyName, propertyValue);
    return listPopOne(list);
  }

  /**
   * Returns the first existing parent stem of a given name.
   * So if the following stems exist:
   *   i2
   *   i2:test
   *   
   * And you run getFirstParentStemOfName("i2:test:mystem:mygroup"),
   * you will get back the stem for i2:test.
   * 
   * If you run getFirstParentStemOfName("test1:test2"),
   * you will get back the root stem.
   * 
   * @param name
   * @return Stem
   */
  public static Stem getFirstParentStemOfName(String name) {
    String parent = GrouperUtil.parentStemNameFromName(name);

    if (parent == null || parent.equals(name)) {
      return StemFinder.findRootStem(GrouperSession.staticGrouperSession()
          .internal_getRootSession());
    }

    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession()
        .internal_getRootSession(), parent, false);
    if (stem != null) {
      return stem;
    }

    return getFirstParentStemOfName(parent);
  }

  /**
   * find an object (or objects) in a collection based on fields
   * @param <T>
   * @param collection
   * @param propertyNames
   * @param propertyValues
   * @return the object(s) or empty list if cant find
   */
  public static <T> List<T> retrieveListByProperties(Collection<T> collection, 
      List<String> propertyNames, List<Object> propertyValues) {
    
    int fieldNameLength = propertyNames.size();
    
    List<T> result = new ArrayList<T>();
    
    assertion(fieldNameLength == propertyValues.size(), "Problem: " + fieldNameLength + " != " + propertyValues.size());
  
    OUTER: for (T object : collection) {
      //loop through fields and values
      for (int i=0;i<fieldNameLength;i++) {
        Object propertyValue = propertyValue(object, propertyNames.get(i));
        if (!equals(propertyValue, propertyValues.get(i))) {
          continue OUTER;
        }
      }
      //if we got this far, then its a match
      result.add(object);
      
    }
    //if we havent found one, we done
    return result;
  }

  /**
   * find an object (or objects) in a collection based on fields
   * @param <T>
   * @param collection
   * @param propertyName 
   * @param propertyValue 
   * @return the object(s) or empty list if cant find
   */
  public static <T> List<T> retrieveListByProperty(Collection<T> collection, 
      String propertyName, Object propertyValue) {
    
    List<T> result = new ArrayList<T>();
    
    OUTER: for (T object : collection) {
      if (object == null) {
        continue;
      }
      Object currentPropertyValue = propertyValue(object, propertyName);
      if (!equals(currentPropertyValue, propertyValue)) {
        continue OUTER;
      }
      //if we got this far, then its a match
      result.add(object);
      
    }
    //if we havent found one, we done
    return result;
  }

  /**
   * Return the zero element of the list, if it exists, null if the list is empty.
   * If there is more than one element in the list, an exception is thrown.
   * @param <T>
   * @param list is the container of objects to get the first of.
   * @return the first object, null, or exception.
   */
  public static <T> T listPopOne(List<T> list) {
    int size = length(list);
    if (size == 1) {
      return list.get(0);
    } else if (size == 0) {
      return null;
    }
    throw new RuntimeException("More than one object of type " + className(list.get(0))
        + " was returned when only one was expected. (size:" + size +")" );
  }
  
  /**
   * Return the zero element of the set, if it exists, null if the list is empty.
   * If there is more than one element in the list, an exception is thrown.
   * @param <T>
   * @param set is the container of objects to get the first of.
   * @return the first object, null, or exception.
   */
  public static <T> T setPopOne(Set<T> set) {
    int size = length(set);
    if (size == 1) {
      return set.iterator().next();
    } else if (size == 0) {
      return null;
    }
    throw new RuntimeException("More than one object of type " + className(set.iterator().next())
        + " was returned when only one was expected. (size:" + size +")" );
  }
  
  /**
   * Return the zero element of the list, if it exists, null if the list is empty.
   * If there is more than one element in the list, an exception is thrown.
   * @param <T>
   * @param collection is the container of objects to get the first of.
   * @param exceptionIfMoreThanOne will throw exception if there is more than one item in list
   * @return the first object, null, or exception.
   */
  public static <T> T collectionPopOne(Collection<T> collection, boolean exceptionIfMoreThanOne) {
    int size = length(collection);
    if (size > 1 && exceptionIfMoreThanOne) {
      throw new RuntimeException("More than one object of type " + className(get(collection, 0))
          + " was returned when only one was expected. (size:" + size +")" );
    }
    if (size == 0) {
      return null;
    }
    return collection.iterator().next();
  }
  
  /** array for converting HTML to string */
  private static final String[] XML_SEARCH_NO_SINGLE = new String[]{"&","<",">","\""};

  /** array for converting HTML to string */
  private static final String[] XML_REPLACE_NO_SINGLE = new String[]{"&amp;","&lt;","&gt;","&quot;"};
  /**
   * Convert an XML string to HTML to display on the screen
   * 
   * @param input
   *          is the XML to convert
   * @param isEscape true to escape chars, false to unescape
   * 
   * @return the HTML converted string
   */
  public static String xmlEscape(String input, boolean isEscape) {
    if (isEscape) {
      return replace(input, XML_SEARCH_NO_SINGLE, XML_REPLACE_NO_SINGLE);
    }
    return replace(input, XML_REPLACE_NO_SINGLE, XML_SEARCH_NO_SINGLE);
  }

  /**
   * 
   * @param writer
   * @param attributeName
   * @param attributeValue
   */
  public static void xmlAttribute(Writer writer, String attributeName, String attributeValue) {
    try {
      writer.write(" ");
      writer.write(attributeName);
      writer.write("=\"");
      String escapedValue = xmlEscape(attributeValue, true);
      writer.write(escapedValue);
      writer.write("\"");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * see if the object has string fields with members in them (by memberUuid), and if so, export that member
   * @param object
   * @param stringSubstituteMap
   * @return if altered or not
   */
  public static boolean substituteStrings(Map<String, String> stringSubstituteMap, 
      Object object) {
  
    Set<String> fieldNames = GrouperUtil.stringFieldNames(object.getClass());
  
    boolean altered = false;
    
    //go through all the fields in the object
    for (String fieldName : fieldNames) {
      String value = (String)fieldValue(object, fieldName);
  
      //see if the value is a member id which has not been exported
      if (!StringUtils.isBlank(value) && stringSubstituteMap.containsKey(value)) {
        //assign the substitution
        assignField(object, fieldName, stringSubstituteMap.get(value));
        altered = true;
      }
    }
    return altered;
  }

  /** map of class to the list of fields which are string type */
  public static Map<Class<?>, Set<String>> stringFieldNames = new HashMap<Class<?>, Set<String>>();


  /**
   * get the list of string field names based on class, and cache this
   * @param theClass
   * @return the set of field names
   */
  public static Set<String> stringFieldNames(Class<?> theClass) {
    
    if (!stringFieldNames.containsKey(theClass)) {
      stringFieldNames.put(theClass, fieldNames(theClass, String.class, false));
    }
    return stringFieldNames.get(theClass);
  }
  
    /**
   * if theString is not blank, apppend it to the result, and if appending,
   * @param result to append to
   * add a prefix and suffix (if not null)
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @return true if something appended, false if not
   */
  public static boolean appendIfNotBlank(StringBuilder result, 
      Object theStringOrArrayOrList) {
    return appendIfNotBlank(result, null, theStringOrArrayOrList, null);
  }

  /**
   * <pre>
   * append a string to another string if both not blank, with separator.  trim to empty everything
   * </pre>
   * @param string
   * @param separator
   * @param suffix
   * @return the resulting string or blank if nothing
   */
  public static String appendIfNotBlankString(String string, String separator, String suffix) {
    
    string = StringUtils.trimToEmpty(string);
    suffix = StringUtils.trimToEmpty(suffix);
    
    boolean stringIsBlank = StringUtils.isBlank(string);
    boolean suffixIsBlank = StringUtils.isBlank(suffix);

    if (stringIsBlank && suffixIsBlank) {
      return "";
    }

    if (stringIsBlank) {
      return suffix;
    }
    
    if (suffixIsBlank) {
      return string;
    }

    return string + separator + suffix;
    
  }
  
  /**
   * if theString is not Blank, apppend it to the result, and if appending,
   * add a prefix (if not null)
   * @param result to append to
   * @param prefix
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @return true if something appended, false if not
   */
  public static boolean appendIfNotBlank(StringBuilder result, 
      String prefix, Object theStringOrArrayOrList) {
    return appendIfNotBlank(result, prefix, theStringOrArrayOrList, null);
  }

  /**
   * if theString is not Blank, apppend it to the result, and if appending,
   * add a prefix and suffix (if not null)
   * @param result to append to, assumed to be not null
   * @param prefix
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @param suffix
   * @return true if anything appended, false if not
   */
  public static boolean appendIfNotBlank(StringBuilder result, 
      String prefix, Object theStringOrArrayOrList, String suffix) {
    return appendIfNotBlank(result, prefix, null, theStringOrArrayOrList, suffix);
  }

  /**
   * if theString is not Blank, apppend it to the result, and if appending,
   * add a prefix and suffix (if not null)
   * @param result to append to, assumed to be not null
   * @param prefix prepend this prefix always (when a result not empty).  Will be after the other prefix
   * @param prefixIfNotBlank prepend this prefix if the result is not empty
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @param suffix
   * @return true if anything appended, false if not
   */
  public static boolean appendIfNotBlank(StringBuilder result, 
      String prefix, String prefixIfNotBlank, Object theStringOrArrayOrList, String suffix) {
    int length = length(theStringOrArrayOrList);
    Iterator iterator = iterator(theStringOrArrayOrList);
    boolean appendedAnything = false;
    
    //these could be appending spaces, so only check to see if they are empty
    boolean hasPrefix = !StringUtils.isEmpty(prefix);
    boolean hasPrefixIfNotBlank = !StringUtils.isEmpty(prefixIfNotBlank);
    boolean hasSuffix = !StringUtils.isEmpty(suffix);
    for (int i=0;i<length;i++) {
      String  current = (String) next(theStringOrArrayOrList, iterator, i);
      
      //only append if not empty
      if (!StringUtils.isBlank(current)) {
        
        //keeping track if anything changed
        appendedAnything = true;
        if (hasPrefix) {
          result.append(prefix);
        }
        if (hasPrefixIfNotBlank && result.length() > 0) {
          result.append(prefixIfNotBlank);
        }
        result.append(current);
        if (hasSuffix) {
          result.append(suffix);
        }
      }
    }
    return appendedAnything;
  }

  /**
     * if theString is not empty, apppend it to the result, and if appending,
     * @param result to append to
     * add a prefix and suffix (if not null)
     * @param theStringOrArrayOrList is a string, array, list, or set of strings
     * @return true if something appended, false if not
     */
    public static boolean appendIfNotEmpty(StringBuilder result, 
        Object theStringOrArrayOrList) {
      return appendIfNotEmpty(result, null, theStringOrArrayOrList, null);
  }

  /**
   * if theString is not empty, apppend it to the result, and if appending,
   * add a prefix (if not null)
   * @param result to append to
   * @param prefix
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @return true if something appended, false if not
   */
  public static boolean appendIfNotEmpty(StringBuilder result, 
      String prefix, Object theStringOrArrayOrList) {
    return appendIfNotEmpty(result, prefix, theStringOrArrayOrList, null);
  }

  /**
   * if theString is not empty, apppend it to the result, and if appending,
   * add a prefix and suffix (if not null)
   * @param result to append to, assumed to be not null
   * @param prefix
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @param suffix
   * @return true if anything appended, false if not
   */
  public static boolean appendIfNotEmpty(StringBuilder result, 
      String prefix, Object theStringOrArrayOrList, String suffix) {
    return appendIfNotEmpty(result, prefix, null, theStringOrArrayOrList, suffix);
  }

  /**
   * if theString is not empty, apppend it to the result, and if appending,
   * add a prefix and suffix (if not null)
   * @param result to append to, assumed to be not null
   * @param prefix prepend this prefix always (when a result not empty).  Will be after the other prefix
   * @param prefixIfNotEmpty prepend this prefix if the result is not empty
   * @param theStringOrArrayOrList is a string, array, list, or set of strings
   * @param suffix
   * @return true if anything appended, false if not
   */
  public static boolean appendIfNotEmpty(StringBuilder result, 
      String prefix, String prefixIfNotEmpty, Object theStringOrArrayOrList, String suffix) {
    int length = length(theStringOrArrayOrList);
    Iterator iterator = iterator(theStringOrArrayOrList);
    boolean appendedAnything = false;
    boolean hasPrefix = !StringUtils.isEmpty(prefix);
    boolean hasPrefixIfNotEmpty = !StringUtils.isEmpty(prefixIfNotEmpty);
    boolean hasSuffix = !StringUtils.isEmpty(suffix);
    for (int i=0;i<length;i++) {
      String  current = (String) next(theStringOrArrayOrList, iterator, i);
      
      //only append if not empty
      if (!StringUtils.isEmpty(current)) {
        
        //keeping track if anything changed
        appendedAnything = true;
        if (hasPrefix) {
          result.append(prefix);
        }
        if (hasPrefixIfNotEmpty && result.length() > 0) {
          result.append(prefixIfNotEmpty);
        }
        result.append(current);
        if (hasSuffix) {
          result.append(suffix);
        }
      }
    }
    return appendedAnything;
  }
 
  /**
   * <p>Find the index of the given object in the array.</p>
   *
   * <p>This method returns <code>-1</code> if <code>null</code> array input.</p>
   * 
   * @param array  the array to search through for the object, may be <code>null</code>
   * @param objectToFind  the object to find, may be <code>null</code>
   * @return the index of the object within the array, 
   *  <code>-1</code> if not found or <code>null</code> array input
   */
  public static int indexOf(Object[] array, Object objectToFind) {
    return indexOf(array, objectToFind, 0);
  }

  /**
   * <p>Checks if the object is in the given array.</p>
   *
   * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
   * 
   * @param array  the array to search through
   * @param objectToFind  the object to find
   * @return <code>true</code> if the array contains the object
   */
  public static boolean contains(Object[] array, Object objectToFind) {
    return indexOf(array, objectToFind) != -1;
  }

  /**
   * <p>Find the index of the given object in the array starting at the given index.</p>
   *
   * <p>This method returns <code>-1</code> if <code>null</code> array input.</p>
   *
   * <p>A negative startIndex is treated as zero. A startIndex larger than the array
   * length will return <code>-1</code>.</p>
   * 
   * @param array  the array to search through for the object, may be <code>null</code>
   * @param objectToFind  the object to find, may be <code>null</code>
   * @param startIndex  the index to start searching at
   * @return the index of the object within the array starting at the index,
   *  <code>-1</code> if not found or <code>null</code> array input
   */
  public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
    if (array == null) {
      return -1;
    }
    if (startIndex < 0) {
      startIndex = 0;
    }
    if (objectToFind == null) {
      for (int i = startIndex; i < array.length; i++) {
        if (array[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = startIndex; i < array.length; i++) {
        if (objectToFind.equals(array[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Return the zero element of the array, if it exists, null if the array is empty.
   * If there is more than one element in the list, an exception is thrown.
   * @param <T>
   * @param array is the container of objects to get the first of.
   * @return the first object, null, or exception.
   */
  public static <T> T arrayPopOne(T[] array) {
    int size = length(array);
    if (size == 1) {
      return array[0];
    } else if (size == 0) {
      return null;
    }
    throw new RuntimeException("More than one object of type " + className(array[0])
        + " was returned when only one was expected. (size:" + size +")" );
  }
  
  /**
   * Note, this is 
   * web service format string
   */
  private static final String WS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

  /**
   * Note, this is 
   * web service format string
   */
  private static final String WS_DATE_FORMAT2 = "yyyy/MM/dd_HH:mm:ss.SSS";

  /**
   * convert a date to a string using the standard web service pattern
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * 
   * @param date
   * @return the string, or null if the date is null
   */
  public static String dateToString(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
    return simpleDateFormat.format(date);
  }

  /**
   * convert a string to a date using the standard web service pattern Note
   * that HH is 0-23
   * 
   * @param dateString
   * @return the string, or null if the date was null
   */
  public static Date stringToDate(String dateString) {
    if (isBlank(dateString)) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
    try {
      return simpleDateFormat.parse(dateString);
    } catch (ParseException e) {
      SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(WS_DATE_FORMAT2);
      try {
        return simpleDateFormat2.parse(dateString);
      } catch (ParseException e2) {
        throw new RuntimeException("Cannot convert '" + dateString
            + "' to a date based on format: " + WS_DATE_FORMAT, e);
      }
    }
  }

  /**
   * @param values
   * @return the max long in the list of args
   */
  public static Long getMaxLongValue(Long... values) {
    if (values == null || values.length == 0) {
      return null;
    }
    
    Long maxValue = null;
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        if (maxValue == null || maxValue.compareTo(values[i]) < 0) {
          maxValue = new Long(values[i]);
        }
      }
    }
    
    return maxValue;
  }

  /**
   * @param values
   * @return the min long in the list of args
   */
  public static Long getMinLongValue(Long... values) {
    if (values == null || values.length == 0) {
      return null;
    }
    
    Long minValue = null;
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        if (minValue == null || minValue.compareTo(values[i]) > 0) {
          minValue = new Long(values[i]);
        }
      }
    }
    
    return minValue;
  }

  /**
   * override map for properties in thread local to be used in a web server or the like, based on property file name
   * @param propertiesFileName 
   * @return the override map
   */
  public static Map<String, String> propertiesThreadLocalOverrideMap(String propertiesFileName) {
    Map<String, Map<String, String>> overrideMap = propertiesThreadLocalOverrideMap.get();
    if (overrideMap == null) {
      overrideMap = new HashMap<String, Map<String, String>>();
      propertiesThreadLocalOverrideMap.set(overrideMap);
    }
    Map<String, String> propertiesOverrideMap = overrideMap.get(propertiesFileName);
    if (propertiesOverrideMap == null) {
      propertiesOverrideMap = new HashMap<String, String>();
      overrideMap.put(propertiesFileName, propertiesOverrideMap);
    }
    return propertiesOverrideMap;
  }

  /**
   * ^\s*\((.+)\)\s*([^\s]+)\s*$
   * start, optional space, open paren, stuff inside, close parent, optional space, not space, optional space
   */
  private static Pattern typeCastTypePattern = Pattern.compile("^\\s*\\((.+)\\)\\s*([^\\s]+)\\s*$");
  
  /**
   * process a string / string map and convert the values to a string/object map.
   * @param limitEnvVars if processing limits, pass in a map of limits.  The name is the
   * name of the variable, and the value is the value.  Note, you can typecast the
   * values by putting a valid type in parens in front of the param name.  e.g.
   * name: (integer)amount, value: 50         (will convert to long)
   * name: (decimal)amount, value: 50.3   (will convert to double)
   * name: (timestamp)amount, value: 2011/01/26 19:02:04   (will convert to date/timestamp)
   * @return the map of string to object
   */
  public static Map<String, Object> typeCastStringStringMap(Map<String, Object> limitEnvVars) {
    
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    
    if (GrouperUtil.length(limitEnvVars) == 0) {
      return result;
    }
    
    Matcher matcher = null;
    
    for (String key : limitEnvVars.keySet()) {
      
      Object value = limitEnvVars.get(key);
      matcher = typeCastTypePattern.matcher(key);
      if (value instanceof String && matcher.matches()) {
        String type = StringUtils.trimToEmpty(matcher.group(1));
        Object valueOriginal = value;
        key = StringUtils.trimToEmpty(matcher.group(2));
        try {
          if (StringUtils.equalsIgnoreCase(type, "int") || StringUtils.equalsIgnoreCase(type, "integer")
              || StringUtils.equalsIgnoreCase(type, "long")) {
            value = GrouperUtil.longValue(value);
          } else if (StringUtils.equalsIgnoreCase(type, "double") || StringUtils.equalsIgnoreCase(type, "float")
              || StringUtils.equalsIgnoreCase(type, "decimal")) {
            value = GrouperUtil.doubleValue(value);
          } else if (StringUtils.equalsIgnoreCase(type, "date") || StringUtils.equalsIgnoreCase(type, "timestamp")) {
            value = GrouperUtil.toTimestamp(value);
          } else if (StringUtils.equalsIgnoreCase(type, "text") || StringUtils.equalsIgnoreCase(type, "string")) {
            //nothing, the value is a string
          } else if (StringUtils.equalsIgnoreCase(type, "boolean")) {
            value = GrouperUtil.booleanValue(value);
          } else if (StringUtils.equalsIgnoreCase(type, "null")) {
            value = null;
          } else if (StringUtils.equalsIgnoreCase(type, "empty") || StringUtils.equalsIgnoreCase(type, "emptyString")) {
            value = "";
          } else {
            throw new RuntimeException("Not expecting type: " + type + ", " + valueOriginal);
          }
        } catch (RuntimeException re) {
          throw new RuntimeException("Cannot convert value to " + key + ", " + type + ", " + valueOriginal, re);
        }
      }
      
      result.put(key, value);
      
    }
    return result;
  }

  /**
   * see if an ip address is on a network
   * 
   * @param ipString
   *          is the ip address to check
   * @param networkIpString
   *          is the ip address of the network
   * @param mask
   *          is the length of the mask (0-32)
   * @return boolean
   */
  public static boolean ipOnNetwork(String ipString, String networkIpString, int mask) {

    //this allows all
    if (mask == 0) {
      return true;
    }
    int ip = ipInt(ipString);
    int networkIp = ipInt(networkIpString);
  
    ip = ipReadyForAnd(ip, mask);
    networkIp = ipReadyForAnd(networkIp, mask);
  
    return ip == networkIp;
  }

  /**
   * see if an ip address is on a network
   * 
   * @param ipString
   *          is the ip address to check
   * @param networkIpStrings
   *          are the ip addresses of the networks, e.g. 1.2.3.4/12, 2.3.4.5/24
   * @return boolean
   */
  public static boolean ipOnNetworks(String ipString, String networkIpStrings) {
    
    String[] networkIpStringsArray = splitTrim(networkIpStrings, ",");
    
    //check each one
    for (String networkIpString : networkIpStringsArray) {
      
      if (!contains(networkIpString, "/")) {
        throw new RuntimeException("String must contain slash and CIDR network bits, e.g. 1.2.3.4/14");
      }
      //get network part:
      String network = prefixOrSuffix(networkIpString, "/", true);
      network = trim(network);
      
      String mask = prefixOrSuffix(networkIpString, "/", false);
      mask = trim(mask);
      int maskInt = -1;
      
      maskInt = Integer.parseInt(mask);
      
      //if on the network, then all good
      if (ipOnNetwork(ipString, network, maskInt)) {
        return true;
      }
      
      
    }
    return false;
  }

  /**
   * get the ip address after putting 1's where the subnet mask is not
   * @param ip int
   * @param maskLength int
   * @return int
   */
  public static int ipReadyForAnd(int ip, int maskLength) {
    int mask = -1 + (int) Math.pow(2, 32 - maskLength);

    return ip | mask;
  }

  /**
   * get the ip addres integer from a string ip address
   * @param ip String
   * @return int
   */
  public static int ipInt(String ip) {
    int block1;
    int block2;
    int block3;
    int block4;
  
    try {
      int periodIndex = ip.indexOf('.');
      String blockString = ip.substring(0, periodIndex);
      block1 = Integer.parseInt(blockString);
  
      //split it up for 2^24 since it does the math wrong if you dont
      int mathPow = (int) Math.pow(2, 24);
      block1 *= mathPow;
  
      int oldPeriodIndex = periodIndex;
  
      periodIndex = ip.indexOf('.', periodIndex + 1);
      blockString = ip.substring(oldPeriodIndex + 1, periodIndex);
      block2 = Integer.parseInt(blockString);
      block2 *= Math.pow(2, 16);
      oldPeriodIndex = periodIndex;
  
      periodIndex = ip.indexOf('.', periodIndex + 1);
      blockString = ip.substring(oldPeriodIndex + 1, periodIndex);
      block3 = Integer.parseInt(blockString);
      block3 *= Math.pow(2, 8);
  
      blockString = ip.substring(periodIndex + 1, ip.length());
      block4 = Integer.parseInt(blockString);
    } catch (NumberFormatException nfe) {
      throw new RuntimeException("Could not parse the ipaddress: " + ip);
    }
  
    return block1 + block2 + block3 + block4;
  }

  /**
   * get the set of methods
   * @param theClass
   * @param methodName
   * @param superclassToStopAt 
   * @param includeSuperclassToStopAt 
   * @param includeStaticMethods
   * @param markerAnnotation 
   * @param includeAnnotation 
   * @param methodSet
   */
  public static void methodsByNameHelper(Class<?> theClass, String methodName, Class<?> superclassToStopAt, 
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
        if (StringUtils.equals(methodName, method.getName())) {
          // go for it
          methodSet.add(method);
        }
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
    methodsByNameHelper(superclass, methodName, superclassToStopAt,
        includeSuperclassToStopAt, includeStaticMethods,
        markerAnnotation, includeAnnotation, methodSet);
    
  }

  /**
   * call method with more params.  e.g. if you are only passing in the first 3 params, of a 6 param method...
   * @param instance
   * @param theType of object to call method on
   * @param methodName
   * @param params
   * @return the result of the method
   */
  public static Object callMethodWithMoreParams(Object instance, Class<?> theType, String methodName, Object[] params) {
    Method method = methodByName(theType, methodName, Object.class, false, instance == null ? true : false, true);
    
    int paramsSize = length(params);
    
    Class<?>[] methodParamTypes = method.getParameterTypes();
    
    int methodParamsSize = length(methodParamTypes);
    if (methodParamsSize < paramsSize) {
      throw new RuntimeException("Why is method params " + methodParamsSize + "less than args? " + paramsSize);
    }
    
    //lets see if the params match up, as many as there are
    for (int i=0;i<paramsSize;i++) {
      Class<?> methodParamClass = methodParamTypes[i];
      
      //make sure assignable
      if (methodParamTypes[i].isPrimitive() && params[i] == null) {
        throw new RuntimeException("Trying to call method: " + methodName + " on class: " + (instance == null ? null : instance.getClass())
            + " and arg index: " + i + " is null: but should be a primitive: " + methodParamTypes[i].getName());
      }
        
      if (params[i] != null && !(methodParamTypes[i].isAssignableFrom(params[i].getClass()))) {  
        throw new RuntimeException("Trying to call method: " + methodName + " on class: " + (instance == null ? null : instance.getClass())
            + " and arg index: " + i + " is of type: " + methodParamClass + ", but trying to pass: " + params[i].getClass());
      }
      
    }
    
    Object[] methodArgs = new Object[methodParamsSize];
    
    //copy the args, the rest are null
    if (paramsSize > 0) {
      System.arraycopy(params, 0, methodArgs, 0, params.length);
      
    }
    
    //we are all good, just pass null for the extra methods
    try {
      return method.invoke(instance, methodArgs);
    } catch (Exception e) {
      throw new RuntimeException("Trying to call method: " + methodName + " on class: " + (instance == null ? null : instance.getClass()), e);
    }
  }

  /**
   * convert an object from one version to another
   * @param input input object
   * @param newPackage is the package where the other version of things are
   * @return the object in the new version or null if input null or new version not found
   */
  public static Object changeToVersion(Object input, String newPackage) {
    return changeToVersionHelper(input, newPackage, 100);
  }

  /**
   * convert an object from one version to another
   * @param input input object
   * @param newPackage is the package where the other version of things are
   * @param timeToLive avoid circular references
   * @return the object in the new version or null if input null or new version not found
   */
  public static Object changeToVersionHelper(Object input, String newPackage, int timeToLive) {
    
    
    if (input == null) {
      return null;
    }
  
    //if we are a string, just return it
    if (input instanceof String) {
      return input;
    }
    
    //lets get the input class
    Class inputClass = input.getClass();
    
    int interestingLogFields = 0;
    
    //if we are an array of strings, clone and return it
    int inputArrayLength = inputClass.isArray() ? length(input) : -1;
    if (inputClass.isArray() && String.class.equals(inputClass.getComponentType())) {
      //lets clone
      String[] result = new String[inputArrayLength];
      System.arraycopy(input, 0, result, 0, result.length);
      return result;
    }
    
    StringBuilder logMessage = LOG.isDebugEnabled() ? new StringBuilder() : null;
    
    if (logMessage != null) {
      logMessage.append("class: ").append(inputClass.getSimpleName()).append(", ");
    }
    
    if (timeToLive-- < 0) {
      throw new RuntimeException("Circular reference!");
    }
    
    //new class
    try {
  
      Object result = null;
  
      Class<?> outputClass = null;
      String outputClassName = newPackage + "." + (inputClass.isArray() ? 
          inputClass.getComponentType().getSimpleName() : inputClass.getSimpleName());
      try {
        outputClass = forName(outputClassName);
      } catch (RuntimeException re) {
        if (re.getCause() instanceof ClassNotFoundException) {
          if (logMessage != null) {
            logMessage.append("output classNotFound: ").append(outputClassName);
            LOG.debug(logMessage.toString());
          }
          return null;
        }
        //let this be handled below
        throw re;
      }
  
  
  
      //if we are an array of objects, do that
      if (inputClass.isArray()) {
        Object array = Array.newInstance(outputClass, inputArrayLength);
        for (int i=0;i<inputArrayLength;i++) {
          
          Object inputElement = Array.get(input, i);
          Object outputElement = changeToVersionHelper(inputElement, newPackage, timeToLive);
          Array.set(array, i, outputElement);
          
        }
        return array;
      }
      
      
      //get instance
      result = newInstance(outputClass);
  
      //get all fields in the input
      Set<Field> inputFields = fields(inputClass, Object.class, null, false, false, false, null, false);
      Set<Field> outputFields = fields(outputClass, Object.class, null, false, false, false, null, false);
      
      Map<String, Field> inputFieldMap = new HashMap<String, Field>();
      
      for (Field field : nonNull(inputFields)) {
        inputFieldMap.put(field.getName(), field);
      }
      
      //see which ones match
      for (Field outputField : nonNull(outputFields)) {
        
        Field inputField = inputFieldMap.get(outputField.getName());
        
        if (inputField == null) {
          if (logMessage != null) {
            interestingLogFields++;
            logMessage.append("field not found input: ").append(outputField.getName()).append(", ");
          }
          continue;
        }
        //take it out of the map so we know which ones are left
        inputFieldMap.remove(inputField.getName());
        
        Object inputFieldObject = fieldValue(inputField, input);
        
        //lets convert that field
        Object outputFieldObject = changeToVersionHelper(inputFieldObject, newPackage, timeToLive);
        
        //this is ok
        if (outputFieldObject == null) {
          continue;
        }
        
        try {
          assignField(outputField, result, outputFieldObject, true, false);
        } catch (RuntimeException re) {
          if (logMessage != null) {
            logMessage.append("problem with field: ").append(inputField.getName()).append(", ").append(ExceptionUtils.getFullStackTrace(re));
            interestingLogFields++;
          }
        }
      }
      if (logMessage != null) {
        for (String inputFieldName : nonNull(inputFieldMap.keySet())) {
          logMessage.append("field not found output: ").append(inputFieldName).append(", ");
          interestingLogFields++;
        }
        
        if (interestingLogFields > 0) {
          LOG.debug(logMessage.toString());
        }
      }    
      
      return result;
    } catch (RuntimeException re) {
      if (logMessage != null) {
        logMessage.append("Problem with class: ").append(re.getClass()).append(", ").append(re.getMessage());
        LOG.debug(logMessage.toString(), re);
      }
      throw re;
    }
    
  }

  /**
   * 
   */
  public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
  
  /** original tmp dir */
  private static final String ORIGINAL_TMP_DIR = System.getProperty(JAVA_IO_TMPDIR);

  /** log it once */
  private static boolean loggedTempDir = false;
  
  /**
   * return the temp dir, either what is in the java env var, or something in the grouper conf
   * @return the temp dir
   */
  public static String tmpDir() {
    
    String tmpDir = null;

    tmpDir = GrouperConfig.getProperty("grouper.tmp.dir");
    if (isBlank(tmpDir)) {
      tmpDir = ORIGINAL_TMP_DIR;
      if (isBlank(tmpDir)) {
        //logger might not be set, lets SOP this too...
        System.out.println("Error: Cant find tmpDir.  You should set grouper.tmp.dir in the grouper.properties!");
        LOG.fatal("Error: Cant find tmpDir.  You should set grouper.tmp.dir in the grouper.properties!");
      }
    }
    if (!isBlank(tmpDir)) {
      if (!loggedTempDir) {
        loggedTempDir = true;
        LOG.info("Tmp dir is set to: '" + tmpDir + "'");
      }
    }
    return tmpDir;
  }
 
}
