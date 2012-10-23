/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.Expression;
import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.JexlContext;
import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.JexlEngine;
import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.JexlException;
import edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.MapContext;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 *
 */
public class GrouperActivemqUtils {

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
      if (GrouperClientUtils.isBlank(name)) {
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
     * @see edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.MapContext#get(java.lang.String)
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
     * @see edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.MapContext#has(java.lang.String)
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
   * @param allowStaticClasses if true allow static classes not registered with context
   * @param silent if silent mode, swallow exceptions (warn), and dont warn when variable not found
   * @param lenient false if undefined variables should throw an exception.  if lenient is true (default)
   * then undefined variables are null
   * @param logOnNull if null output of substitution should be logged
   * @return the string
   */
  public static String substituteExpressionLanguage(String stringToParse, 
      Map<String, Object> variableMap, boolean allowStaticClasses, boolean silent, boolean lenient, boolean logOnNull) {
    if (GrouperClientUtils.isBlank(stringToParse)) {
      return stringToParse;
    }
    String overallResult = null;
    Exception exception = null;
    try {
      JexlContext jc = allowStaticClasses ? new ElMapContext() : new MapContext();

      int index = 0;
      
      variableMap = GrouperClientUtils.nonNull(variableMap);
      
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
          if (!lenient && GrouperClientUtils.trimToEmpty(je.getMessage()).contains("undefined variable")) {
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
                + GrouperClientUtils.toStringForLog(variableMap.keySet()));
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
                  + GrouperClientUtils.toStringForLog(variableMap.keySet()));
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
        Set<String> keysSet = new LinkedHashSet<String>(GrouperClientUtils.nonNull(variableMap).keySet());
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
          logMessage.append(", and exception: " + exception + ", " + GrouperClientUtils.getFullStackTrace(exception));
        }
        LOG.debug(logMessage.toString());
      }
    }
  }

  /**
   * Construct a class.  TODO replace with GrouperClientUtils method
   * @param <T> template type
   * @param theClass
   * @param allowPrivateConstructor true if should allow private constructors
   * @return the instance
   */
  public static <T> T newInstance(Class<T> theClass, boolean allowPrivateConstructor) {
    if (!allowPrivateConstructor) {
      return GrouperClientUtils.newInstance(theClass);
    }
    try {
      Constructor<?>[] constructorArray = theClass.getDeclaredConstructors();
      for (Constructor<?> constructor : constructorArray) {
         if (constructor.getGenericParameterTypes().length == 0) {
           if (allowPrivateConstructor) {
             constructor.setAccessible(true);
           }
           return (T)constructor.newInstance();
         }
      }
      //why cant we find a constructor???
      throw new RuntimeException("Why cant we find a constructor for class: " + theClass);
    } catch (Throwable e) {
      if (theClass != null && Modifier.isAbstract(theClass.getModifiers())) {
        throw new RuntimeException("Problem with class: " + theClass + ", maybe because it is abstract!", e);        
      }
      throw new RuntimeException("Problem with class: " + theClass, e);
    }
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperActivemqUtils.class);
  /** class object for this string */
  private static Map<String, Class<?>> jexlClass = new HashMap<String, Class<?>>();
  /** pattern to see if class or not */
  private static Pattern jexlClassPattern = Pattern.compile("^[a-zA-Z0-9_.]*\\.[A-Z][a-zA-Z0-9_]*$");
  /** true or false for if we know if this is a class or not */
  private static Map<String, Boolean> jexlKnowsIfClass = new HashMap<String, Boolean>();
  
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

}
