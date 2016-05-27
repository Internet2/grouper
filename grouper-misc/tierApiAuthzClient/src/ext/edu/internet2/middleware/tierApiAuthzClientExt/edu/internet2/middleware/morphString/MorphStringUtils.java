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
package edu.internet2.middleware.tierApiAuthzClientExt.edu.internet2.middleware.morphString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * ExceptionUtils from commons, and some other useful classes, used for websec in oracle
 * and other external plugin type java programs
 * @version $Id: MorphStringUtils.java,v 1.2 2009-11-17 06:25:05 mchyzer Exp $
 * @author mchyzer
 */
public class MorphStringUtils {

  /**
   * The empty String <code>""</code>.
   * @since 2.0
   */
  public static final String EMPTY = "";

  /**
   * The name says it all.
   */
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  /**
   * <p>Used when printing stack frames to denote the start of a
   * wrapped exception.</p>
   *
   * <p>Package private for accessibility by test suite.</p>
   */
  static final String WRAPPED_MARKER = " [wrapped] ";

  /**
   * <p>The names of methods commonly used to access a wrapped exception.</p>
   */
  private static String[] CAUSE_METHOD_NAMES = { "getCause", "getNextException",
      "getTargetException", "getException", "getSourceException", "getRootCause",
      "getCausedByException", "getNested", "getLinkedException", "getNestedException",
      "getLinkedCause", "getThrowable", };

  /** fast temp dir name (subdir from log dir) */
  public static final String TEMP_DIR_NAME = "fastTemp";

  /** fast config cache dir name (subdir from log dir) */
  public static final String CONFIG_CACHE_DIR_NAME = "fastConfigCache";

  /**
   * encode a param that will go in a url
   * @param string
   * @return the encoded param
   */
  public static String encodeForUrl(String string) {
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }

  /**
   * make sure parent dirs exist etc
   * @param file
   */
  public static void initFile(File file) {
    //make log file and parent dirs if not exists
    if (!file.exists()) {

      File parentFile = file.getParentFile();
      if (!parentFile.exists()) {
        if (!parentFile.mkdirs()) {
          throw new RuntimeException("Cannot create parent dirs: "
              + parentFile.getAbsolutePath());
        }
      }

      if (!parentFile.isDirectory()) {
        throw new RuntimeException("Parent file is not a directory! "
            + parentFile.getAbsolutePath());
      }

    }

  }

  /**
   * <p>The Method object for JDK1.4 getCause.</p>
   */
  private static final Method THROWABLE_CAUSE_METHOD;
  static {
    Method getCauseMethod;
    try {
      getCauseMethod = Throwable.class.getMethod("getCause", (Class[]) null);
    } catch (Exception e) {
      getCauseMethod = null;
    }
    THROWABLE_CAUSE_METHOD = getCauseMethod;
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

  //-----------------------------------------------------------------------
  /**
   * <p>Adds to the list of method names used in the search for <code>Throwable</code>
   * objects.</p>
   * 
   * @param methodName  the methodName to add to the list, <code>null</code>
   *  and empty strings are ignored
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  public static void addCauseMethodName(String methodName) {
    if (isNotEmpty(methodName) && !isCauseMethodName(methodName)) {
      List list = getCauseMethodNameList();
      if (list.add(methodName)) {
        CAUSE_METHOD_NAMES = toArray(list);
      }
    }
  }

  /**
   * <p>Removes from the list of method names used in the search for <code>Throwable</code>
   * objects.</p>
   * 
   * @param methodName  the methodName to remove from the list, <code>null</code>
   *  and empty strings are ignored
   * @since 2.1
   */
  public static void removeCauseMethodName(String methodName) {
    if (isNotEmpty(methodName)) {
      List list = getCauseMethodNameList();
      if (list.remove(methodName)) {
        CAUSE_METHOD_NAMES = toArray(list);
      }
    }
  }

  /**
   * Returns the given list as a <code>String[]</code>.
   * @param list a list to transform.
   * @return the given list as a <code>String[]</code>.
   */

  private static String[] toArray(List list) {
    return (String[]) list.toArray(new String[list.size()]);
  }

  /**
   * Returns CAUSE_METHOD_NAMES as a List.
   * @return CAUSE_METHOD_NAMES  as a List.
   */

  private static ArrayList getCauseMethodNameList() {
    return new ArrayList(Arrays.asList(CAUSE_METHOD_NAMES));
  }

  /**
   * <p>Tests if the list of method names used in the search for <code>Throwable</code>
   * objects include the given name.</p>
   * 
   * @param methodName  the methodName to search in the list.
   * @return if the list of method names used in the search for <code>Throwable</code>
   *  objects include the given name.
   * @since 2.1
   */
  public static boolean isCauseMethodName(String methodName) {
    return indexOf(CAUSE_METHOD_NAMES, methodName) >= 0;
  }

  /**
   * <p>Introspects the <code>Throwable</code> to obtain the cause.</p>
   * 
   * <p>The method searches for methods with specific names that return a 
   * <code>Throwable</code> object. This will pick up most wrapping exceptions,
   * including those from JDK 1.4, and
   * The method names can be added to using {@link #addCauseMethodName(String)}.</p>
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
   * null safe classname method
   * @param object
   * @return the classname
   */
  public static String className(Object object) {
    return object == null ? null : object.getClass().getName();
  }

  /**
   * call reflection on an object with no arg method
   * @param object
   * @param methodName
   * @return the return of the method
   */
  public static Object executeMethod(Object object, String methodName) {
    try {
      Class theClass = object.getClass();
      Method method = theClass.getDeclaredMethod(methodName, (Class[]) null);
      return method.invoke(object, (Object[]) null);
    } catch (Exception e) {
      throw new RuntimeException("Problem calling class method: " + className(object)
          + "." + methodName, e);
    }
  }

  /**
   * call reflection on an object with no arg method
   * @param object
   * @param methodName
   * @param argumentType type of the argument
   * @param argument to the method
   * @return the return of the method
   */
  public static Object executeMethod(Object object, String methodName,
      Class argumentType, Object argument) {
    try {
      Class theClass = object.getClass();
      Method method = theClass
          .getDeclaredMethod(methodName, new Class[] { argumentType });
      return method.invoke(object, new Object[] { argument });
    } catch (Exception e) {
      throw new RuntimeException("Problem calling class method: " + className(object)
          + "." + methodName, e);
    }
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
   * <p>Introspects the <code>Throwable</code> to obtain the root cause.</p>
   * 
   * <p>This method walks through the exception chain to the last element,
   * "root" of the tree, using {@link #getCause(Throwable)}, and
   * returns that exception.</p>
   *
   * @param throwable  the throwable to get the root cause for, may be null
   * @return the root cause of the <code>Throwable</code>,
   *  <code>null</code> if none found or null throwable input
   */
  public static Throwable getRootCause(Throwable throwable) {
    Throwable cause = getCause(throwable);
    if (cause != null) {
      throwable = cause;
      while ((throwable = getCause(throwable)) != null) {
        cause = throwable;
      }
    }
    return cause;
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
    if (throwable instanceof SQLException) {
      return ((SQLException) throwable).getNextException();
    } else if (throwable instanceof InvocationTargetException) {
      return ((InvocationTargetException) throwable).getTargetException();
    } else {
      return null;
    }
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
      method = throwable.getClass().getMethod(methodName, (Class[]) null);
    } catch (NoSuchMethodException ignored) {
    } catch (SecurityException ignored) {
    }

    if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
      try {
        return (Throwable) method.invoke(throwable, new Object[0]);
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

  //-----------------------------------------------------------------------
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

    if (throwable instanceof SQLException) {
      return true;
    } else if (throwable instanceof InvocationTargetException) {
      return true;
    } else if (isThrowableNested()) {
      return true;
    }

    Class cls = throwable.getClass();
    for (int i = 0, isize = CAUSE_METHOD_NAMES.length; i < isize; i++) {
      try {
        Method method = cls.getMethod(CAUSE_METHOD_NAMES[i], (Class[]) null);
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

  //-----------------------------------------------------------------------
  /**
   * <p>Counts the number of <code>Throwable</code> objects in the
   * exception chain.</p>
   * 
   * <p>A throwable without cause will return <code>1</code>.
   * A throwable with one cause will return <code>2</code> and so on.
   * A <code>null</code> throwable will return <code>0</code>.</p>
   * 
   * @param throwable  the throwable to inspect, may be null
   * @return the count of throwables, zero if null input
   */
  public static int getThrowableCount(Throwable throwable) {
    int count = 0;
    while (throwable != null) {
      count++;
      throwable = getCause(throwable);
    }
    return count;
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

  //-----------------------------------------------------------------------
  /**
   * <p>Returns the (zero based) index of the first <code>Throwable</code>
   * that matches the specified class (exactly) in the exception chain.
   * Subclasses of the specified class do not match - see
   * {@link #indexOfType(Throwable, Class)} for the opposite.</p>
   * 
   * <p>A <code>null</code> throwable returns <code>-1</code>.
   * A <code>null</code> type returns <code>-1</code>.
   * No match in the chain returns <code>-1</code>.</p>
   *
   * @param throwable  the throwable to inspect, may be null
   * @param clazz  the class to search for, subclasses do not match, null returns -1
   * @return the index into the throwable chain, -1 if no match or null input
   */
  public static int indexOfThrowable(Throwable throwable, Class clazz) {
    return indexOf(throwable, clazz, 0, false);
  }

  /**
   * <p>Returns the (zero based) index of the first <code>Throwable</code>
   * that matches the specified type in the exception chain from
   * a specified index.
   * Subclasses of the specified class do not match - see
   * {@link #indexOfType(Throwable, Class, int)} for the opposite.</p>
   * 
   * <p>A <code>null</code> throwable returns <code>-1</code>.
   * A <code>null</code> type returns <code>-1</code>.
   * No match in the chain returns <code>-1</code>.
   * A negative start index is treated as zero.
   * A start index greater than the number of throwables returns <code>-1</code>.</p>
   *
   * @param throwable  the throwable to inspect, may be null
   * @param clazz  the class to search for, subclasses do not match, null returns -1
   * @param fromIndex  the (zero based) index of the starting position,
   *  negative treated as zero, larger than chain size returns -1
   * @return the index into the throwable chain, -1 if no match or null input
   */
  public static int indexOfThrowable(Throwable throwable, Class clazz, int fromIndex) {
    return indexOf(throwable, clazz, fromIndex, false);
  }

  //-----------------------------------------------------------------------
  /**
   * <p>Returns the (zero based) index of the first <code>Throwable</code>
   * that matches the specified class or subclass in the exception chain.
   * Subclasses of the specified class do match - see
   * {@link #indexOfThrowable(Throwable, Class)} for the opposite.</p>
   * 
   * <p>A <code>null</code> throwable returns <code>-1</code>.
   * A <code>null</code> type returns <code>-1</code>.
   * No match in the chain returns <code>-1</code>.</p>
   *
   * @param throwable  the throwable to inspect, may be null
   * @param type  the type to search for, subclasses match, null returns -1
   * @return the index into the throwable chain, -1 if no match or null input
   * @since 2.1
   */
  public static int indexOfType(Throwable throwable, Class type) {
    return indexOf(throwable, type, 0, true);
  }

  /**
   * <p>Returns the (zero based) index of the first <code>Throwable</code>
   * that matches the specified type in the exception chain from
   * a specified index.
   * Subclasses of the specified class do match - see
   * {@link #indexOfThrowable(Throwable, Class)} for the opposite.</p>
   * 
   * <p>A <code>null</code> throwable returns <code>-1</code>.
   * A <code>null</code> type returns <code>-1</code>.
   * No match in the chain returns <code>-1</code>.
   * A negative start index is treated as zero.
   * A start index greater than the number of throwables returns <code>-1</code>.</p>
   *
   * @param throwable  the throwable to inspect, may be null
   * @param type  the type to search for, subclasses match, null returns -1
   * @param fromIndex  the (zero based) index of the starting position,
   *  negative treated as zero, larger than chain size returns -1
   * @return the index into the throwable chain, -1 if no match or null input
   * @since 2.1
   */
  public static int indexOfType(Throwable throwable, Class type, int fromIndex) {
    return indexOf(throwable, type, fromIndex, true);
  }

  /**
   * 
   * @param throwable
   * @param type
   * @param fromIndex
   * @param subclass
   * @return the index
   */

  private static int indexOf(Throwable throwable, Class type, int fromIndex,
      boolean subclass) {
    if (throwable == null || type == null) {
      return -1;
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    Throwable[] throwables = getThrowables(throwable);
    if (fromIndex >= throwables.length) {
      return -1;
    }
    if (subclass) {
      for (int i = fromIndex; i < throwables.length; i++) {
        if (type.isAssignableFrom(throwables[i].getClass())) {
          return i;
        }
      }
    } else {
      for (int i = fromIndex; i < throwables.length; i++) {
        if (type.equals(throwables[i].getClass())) {
          return i;
        }
      }
    }
    return -1;
  }

  //-----------------------------------------------------------------------
  /**
   * <p>Prints a compact stack trace for the root cause of a throwable
   * to <code>System.err</code>.</p>
   * 
   * <p>The compact stack trace starts with the root cause and prints
   * stack frames up to the place where it was caught and wrapped.
   * Then it prints the wrapped exception and continues with stack frames
   * until the wrapper exception is caught and wrapped again, etc.</p>
   *
   * <p>The method is equivalent to <code>printStackTrace</code> for throwables
   * that don't have nested causes.</p>
   * 
   * @param throwable  the throwable to output
   * @since 2.0
   */
  public static void printRootCauseStackTrace(Throwable throwable) {
    printRootCauseStackTrace(throwable, System.err);
  }

  /**
   * <p>Prints a compact stack trace for the root cause of a throwable.</p>
   *
   * <p>The compact stack trace starts with the root cause and prints
   * stack frames up to the place where it was caught and wrapped.
   * Then it prints the wrapped exception and continues with stack frames
   * until the wrapper exception is caught and wrapped again, etc.</p>
   *
   * <p>The method is equivalent to <code>printStackTrace</code> for throwables
   * that don't have nested causes.</p>
   * 
   * @param throwable  the throwable to output, may be null
   * @param stream  the stream to output to, may not be null
   * @throws IllegalArgumentException if the stream is <code>null</code>
   * @since 2.0
   */
  public static void printRootCauseStackTrace(Throwable throwable, PrintStream stream) {
    if (throwable == null) {
      return;
    }
    if (stream == null) {
      throw new IllegalArgumentException("The PrintStream must not be null");
    }
    String trace[] = getRootCauseStackTrace(throwable);
    for (int i = 0; i < trace.length; i++) {
      stream.println(trace[i]);
    }
    stream.flush();
  }

  /**
   * <p>Prints a compact stack trace for the root cause of a throwable.</p>
   *
   * <p>The compact stack trace starts with the root cause and prints
   * stack frames up to the place where it was caught and wrapped.
   * Then it prints the wrapped exception and continues with stack frames
   * until the wrapper exception is caught and wrapped again, etc.</p>
   *
   * <p>The method is equivalent to <code>printStackTrace</code> for throwables
   * that don't have nested causes.</p>
   * 
   * @param throwable  the throwable to output, may be null
   * @param writer  the writer to output to, may not be null
   * @throws IllegalArgumentException if the writer is <code>null</code>
   * @since 2.0
   */
  public static void printRootCauseStackTrace(Throwable throwable, PrintWriter writer) {
    if (throwable == null) {
      return;
    }
    if (writer == null) {
      throw new IllegalArgumentException("The PrintWriter must not be null");
    }
    String trace[] = getRootCauseStackTrace(throwable);
    for (int i = 0; i < trace.length; i++) {
      writer.println(trace[i]);
    }
    writer.flush();
  }

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
      return new String[0];
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
      return new String[0];
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
      return new String[0];
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
   * split a string based on a separator into an array, and trim each entry (see
   * the Commons Util StringUtils.trim for more details)
   * 
   * @param input
   *          is the delimited input to split and trim
   * @param separator
   *          is what to split on
   * 
   * @return the array of items after split and trimmed
   */
  public static String[] splitTrim(String input, String separator) {
    if (input == null) {
      return null;
    }

    //first split
    String[] items = split(input, separator);

    //then trim
    for (int i = 0; (items != null) && (i < items.length); i++) {
      items[i] = trim(items[i]);
    }

    //return the array
    return items;
  }

  //-----------------------------------------------------------------------
  /**
   * <p>Creates a compact stack trace for the root cause of the supplied
   * <code>Throwable</code>.</p>
   * 
   * @param throwable  the throwable to examine, may be null
   * @return an array of stack trace frames, never null
   * @since 2.0
   */

  public static String[] getRootCauseStackTrace(Throwable throwable) {
    if (throwable == null) {
      return new String[0];
    }
    Throwable throwables[] = getThrowables(throwable);
    int count = throwables.length;
    ArrayList frames = new ArrayList();
    List nextTrace = getStackFrameList(throwables[count - 1]);
    for (int i = count; --i >= 0;) {
      List trace = nextTrace;
      if (i != 0) {
        nextTrace = getStackFrameList(throwables[i - 1]);
        removeCommonFrames(trace, nextTrace);
      }
      if (i == count - 1) {
        frames.add(throwables[i].toString());
      } else {
        frames.add(WRAPPED_MARKER + throwables[i].toString());
      }
      for (int j = 0; j < trace.size(); j++) {
        frames.add(trace.get(j));
      }
    }
    return (String[]) frames.toArray(new String[0]);
  }

  /**
   * <p>Removes common frames from the cause trace given the two stack traces.</p>
   * 
   * @param causeFrames  stack trace of a cause throwable
   * @param wrapperFrames  stack trace of a wrapper throwable
   * @throws IllegalArgumentException if either argument is null
   * @since 2.0
   */
  public static void removeCommonFrames(List causeFrames, List wrapperFrames) {
    if (causeFrames == null || wrapperFrames == null) {
      throw new IllegalArgumentException("The List must not be null");
    }
    int causeFrameIndex = causeFrames.size() - 1;
    int wrapperFrameIndex = wrapperFrames.size() - 1;
    while (causeFrameIndex >= 0 && wrapperFrameIndex >= 0) {
      // Remove the frame from the cause trace if it is the same
      // as in the wrapper trace
      String causeFrame = (String) causeFrames.get(causeFrameIndex);
      String wrapperFrame = (String) wrapperFrames.get(wrapperFrameIndex);
      if (causeFrame.equals(wrapperFrame)) {
        causeFrames.remove(causeFrameIndex);
      }
      causeFrameIndex--;
      wrapperFrameIndex--;
    }
  }

  //-----------------------------------------------------------------------
  /**
   * <p>Gets the stack trace from a Throwable as a String.</p>
   *
   * @param throwable  the <code>Throwable</code> to be examined
   * @return the stack trace as generated by the exception's
   *  <code>printStackTrace(PrintWriter)</code> method
   */
  public static String getStackTrace(Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
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

  //-----------------------------------------------------------------------
  /**
   * <p>Captures the stack trace associated with the specified
   * <code>Throwable</code> object, decomposing it into a list of
   * stack frames.</p>
   *
   * @param throwable  the <code>Throwable</code> to examine, may be null
   * @return an array of strings describing each stack frame, never null
   */
  public static String[] getStackFrames(Throwable throwable) {
    if (throwable == null) {
      return new String[0];
    }
    return getStackFrames(getStackTrace(throwable));
  }

  /**
   * <p>Returns an array where each element is a line from the argument.</p>
   * <p>The end of line is determined by the value of separator.</p>
   *  
   * <p>Functionality shared between the
   * <code>getStackFrames(Throwable)</code> methods of this and the
   * classes.</p>
   * @param stackTrace A stack trace String.
   * @return an array where each element is a line from the argument.
   */

  public static String[] getStackFrames(String stackTrace) {
    String linebreak = getSystemProperty("line.separator");
    StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
    List list = new LinkedList();
    while (frames.hasMoreTokens()) {
      list.add(frames.nextToken());
    }
    return toArray(list);
  }

  /**
   * <p>Produces a <code>List</code> of stack frames - the message
   * is not included.</p>
   *
   * <p>This works in most cases - it will only fail if the exception
   * message contains a line that starts with:
   * <code>&quot;&nbsp;&nbsp;&nbsp;at&quot;.</code></p>
   * 
   * @param t is any throwable
   * @return List of stack frames
   */

  public static List getStackFrameList(Throwable t) {
    String stackTrace = getStackTrace(t);
    String linebreak = getSystemProperty("line.separator");
    StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
    List list = new LinkedList();
    boolean traceStarted = false;
    while (frames.hasMoreTokens()) {
      String token = frames.nextToken();
      // Determine if the line starts with <whitespace>at
      int at = token.indexOf("at");
      if (at != -1 && token.substring(0, at).trim().length() == 0) {
        traceStarted = true;
        list.add(token);
      } else if (traceStarted) {
        break;
      }
    }
    return list;
  }

  /**
   * <p>Gets a System property, defaulting to <code>null</code> if the property
   * cannot be read.</p>
   *
   * <p>If a <code>SecurityException</code> is caught, the return
   * value is <code>null</code> and a message is written to <code>System.err</code>.</p>
   * 
   * @param property the system property name
   * @return the system property value or <code>null</code> if a security problem occurs
   */
  private static String getSystemProperty(String property) {
    try {
      return System.getProperty(property);
    } catch (SecurityException ex) {
      // we are not allowed to look at this property
      System.err.println("Caught a SecurityException reading the system property '"
          + property + "'; the SystemUtils property value will default to null.");
      return null;
    }
  }

  /**
   * <p>Checks if a String is not empty ("") and not null.</p>
   *
   * <pre>
   * StringUtils.isNotEmpty(null)      = false
   * StringUtils.isNotEmpty("")        = false
   * StringUtils.isNotEmpty(" ")       = true
   * StringUtils.isNotEmpty("bob")     = true
   * StringUtils.isNotEmpty("  bob  ") = true
   * </pre>
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if the String is not empty and not null
   */
  public static boolean isNotEmpty(String str) {
    return str != null && str.length() > 0;
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
   * <p>Compares two Strings, returning <code>true</code> if they are equal.</p>
   *
   * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
   * references are considered to be equal. The comparison is case sensitive.</p>
   *
   * <pre>
   * StringUtils.equals(null, null)   = true
   * StringUtils.equals(null, "abc")  = false
   * StringUtils.equals("abc", null)  = false
   * StringUtils.equals("abc", "abc") = true
   * StringUtils.equals("abc", "ABC") = false
   * </pre>
   *
   * @see java.lang.String#equals(Object)
   * @param str1  the first String, may be null
   * @param str2  the second String, may be null
   * @return <code>true</code> if the Strings are equal, case sensitive, or
   *  both <code>null</code>
   */
  public static boolean equals(String str1, String str2) {
    return str1 == null ? str2 == null : str1.equals(str2);
  }

  /**
   * <p>Compares two Strings, returning <code>true</code> if they are equal ignoring
   * the case.</p>
   *
   * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
   * references are considered equal. Comparison is case insensitive.</p>
   *
   * <pre>
   * StringUtils.equalsIgnoreCase(null, null)   = true
   * StringUtils.equalsIgnoreCase(null, "abc")  = false
   * StringUtils.equalsIgnoreCase("abc", null)  = false
   * StringUtils.equalsIgnoreCase("abc", "abc") = true
   * StringUtils.equalsIgnoreCase("abc", "ABC") = true
   * </pre>
   *
   * @param str1  the first String, may be null
   * @param str2  the second String, may be null
   * @return <code>true</code> if the Strings are equal, case insensitive, or
   *  both <code>null</code>
   */
  public static boolean equalsIgnoreCase(String str1, String str2) {
    return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
  }

  /**
   * <p>Replaces all occurrences of a String within another String.</p>
   *
   * <p>A <code>null</code> reference passed to this method is a no-op.</p>
   *
   * <pre>
   * StringUtils.replace(null, *, *)        = null
   * StringUtils.replace("", *, *)          = ""
   * StringUtils.replace("any", null, *)    = "any"
   * StringUtils.replace("any", *, null)    = "any"
   * StringUtils.replace("any", "", *)      = "any"
   * StringUtils.replace("aba", "a", null)  = "aba"
   * StringUtils.replace("aba", "a", "")    = "b"
   * StringUtils.replace("aba", "a", "z")   = "zbz"
   * </pre>
   *
   * @see #replace(String text, String repl, String with, int max)
   * @param text  text to search and replace in, may be null
   * @param repl  the String to search for, may be null
   * @param with  the String to replace with, may be null
   * @return the text with any replacements processed,
   *  <code>null</code> if null String input
   */
  public static String replace(String text, String repl, String with) {
    return replace(text, repl, with, -1);
  }

  /**
   * <p>Replaces a String with another String inside a larger String,
   * for the first <code>max</code> values of the search String.</p>
   *
   * <p>A <code>null</code> reference passed to this method is a no-op.</p>
   *
   * <pre>
   * StringUtils.replace(null, *, *, *)         = null
   * StringUtils.replace("", *, *, *)           = ""
   * StringUtils.replace("any", null, *, *)     = "any"
   * StringUtils.replace("any", *, null, *)     = "any"
   * StringUtils.replace("any", "", *, *)       = "any"
   * StringUtils.replace("any", *, *, 0)        = "any"
   * StringUtils.replace("abaa", "a", null, -1) = "abaa"
   * StringUtils.replace("abaa", "a", "", -1)   = "b"
   * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
   * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
   * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
   * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
   * </pre>
   *
   * @param text  text to search and replace in, may be null
   * @param repl  the String to search for, may be null
   * @param with  the String to replace with, may be null
   * @param max  maximum number of values to replace, or <code>-1</code> if no maximum
   * @return the text with any replacements processed,
   *  <code>null</code> if null String input
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
   * <p>Checks if a String is empty ("") or null.</p>
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * <p>NOTE: This method changed in Lang version 2.0.
   * It no longer trims the String.
   * That functionality is available in isBlank().</p>
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if the String is empty or null
   */
  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
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
   * <p>Removes control characters (char &lt;= 32) from both
   * ends of this String, handling <code>null</code> by returning
   * <code>null</code>.</p>
   *
   * <p>The String is trimmed using {@link String#trim()}.
   * Trim removes start and end characters &lt;= 32.
   * To strip whitespace use strip(String)</p>
   *
   * <p>To trim your choice of characters, use the
   * strip(String, String) methods.</p>
   *
   * <pre>
   * StringUtils.trim(null)          = null
   * StringUtils.trim("")            = ""
   * StringUtils.trim("     ")       = ""
   * StringUtils.trim("abc")         = "abc"
   * StringUtils.trim("    abc    ") = "abc"
   * </pre>
   *
   * @param str  the String to be trimmed, may be null
   * @return the trimmed string, <code>null</code> if null String input
   */
  public static String trim(String str) {
    return str == null ? null : str.trim();
  }

  /**
   * <p>Removes control characters (char &lt;= 32) from both
   * ends of this String returning <code>null</code> if the String is
   * empty ("") after the trim or if it is <code>null</code>.
   *
   * <p>The String is trimmed using {@link String#trim()}.
   * Trim removes start and end characters &lt;= 32.
   * To strip whitespace use stripToNull.</p>
   *
   * <pre>
   * StringUtils.trimToNull(null)          = null
   * StringUtils.trimToNull("")            = null
   * StringUtils.trimToNull("     ")       = null
   * StringUtils.trimToNull("abc")         = "abc"
   * StringUtils.trimToNull("    abc    ") = "abc"
   * </pre>
   *
   * @param str  the String to be trimmed, may be null
   * @return the trimmed String,
   *  <code>null</code> if only chars &lt;= 32, empty or null String input
   * @since 2.0
   */
  public static String trimToNull(String str) {
    String ts = trim(str);
    return isEmpty(ts) ? null : ts;
  }

  /**
   * <p>Removes control characters (char &lt;= 32) from both
   * ends of this String returning an empty String ("") if the String
   * is empty ("") after the trim or if it is <code>null</code>.
   *
   * <p>The String is trimmed using trim().
   * Trim removes start and end characters &lt;= 32.
   * To strip whitespace use stripToEmpty(String).</p>
   *
   * <pre>
   * StringUtils.trimToEmpty(null)          = ""
   * StringUtils.trimToEmpty("")            = ""
   * StringUtils.trimToEmpty("     ")       = ""
   * StringUtils.trimToEmpty("abc")         = "abc"
   * StringUtils.trimToEmpty("    abc    ") = "abc"
   * </pre>
   *
   * @param str  the String to be trimmed, may be null
   * @return the trimmed String, or an empty String if <code>null</code> input
   * @since 2.0
   */
  public static String trimToEmpty(String str) {
    return str == null ? EMPTY : str.trim();
  }

  /**
   * <p>Checks if the String contains any whitespace.</p>
   *
   * <p><code>null</code> will return <code>false</code>.
   * An empty String ("") will return <code>false</code>.</p>
   *
   * <pre>
   * StringUtils.isWhitespace(null)   = false
   * StringUtils.isWhitespace("")     = false
   * StringUtils.isWhitespace("  ")   = true
   * StringUtils.isWhitespace("abc")  = false
   * StringUtils.isWhitespace("ab 2c") = true
   * StringUtils.isWhitespace("ab-c") = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if contains any whitespace, and is non-null
   */
  public static boolean hasWhitespace(String str) {
    if (str == null) {
      return false;
    }
    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      if ((Character.isWhitespace(str.charAt(i)) == true)) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>Checks if String contains a search String, handling <code>null</code>.
   * This method uses indexOf(int).</p>
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
   * If array, get the element based on index, if Collection, get it based on iterator.
   * @param arrayOrCollection
   * @param iterator
   * @param index
   * @return the object
   */
  public static Object next(Object arrayOrCollection, Iterator iterator, int index) {
    if (arrayOrCollection.getClass().isArray()) {
      return Array.get(arrayOrCollection, index);
    }
    if (arrayOrCollection instanceof ArrayList) {
      return ((ArrayList) arrayOrCollection).get(index);
    }
    if (arrayOrCollection instanceof Collection) {
      return iterator.next();
    }
    //simple object
    if (0 == index) {
      return arrayOrCollection;
    }
    throw new RuntimeException("Invalid class type: "
        + arrayOrCollection.getClass().getName());
  }

  /**
   * null safe iterator getter if the type if collection
   * @param collection
   * @return the iterator
   */
  public static Iterator iterator(Object collection) {
    if (collection == null) {
      return null;
    }
    //array list doesnt need an iterator
    if (collection instanceof Collection && !(collection instanceof ArrayList)) {
      return ((Collection) collection).iterator();
    }
    return null;
  }

  /**
   * Null safe array length or map
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
    //simple non array non collection object
    return 1;
  }

  /**
   * find the most recently modified resource
   * @param resourceNames resource name, or array or list
   * @return the most recently modified value, or 0 if cant find any or all
   */
  public static long lastModifiedResources(Object resourceNames) {

    //some place to start
    long lastModified = 0;

    if (resourceNames == null) {
      throw new NullPointerException();
    }

    //loop through the resource or resources
    Iterator resourceIterator = iterator(resourceNames);
    int resourceLength = length(resourceNames);
    String current = null;
    for (int i = 0; i < resourceLength; i++) {
      current = (String) next(resourceNames, resourceIterator, i);

      File configFile = fileFromResourceNameHelper(current, null, false, false);

      //  ignore this file if it is in a jar.
      if (configFile.getAbsolutePath().indexOf('!') != -1) {
        return 0;
      }

      //  make sure it exists and is a file
      if (!(configFile.exists() && configFile.isFile())) {
        String error = "Cannot find resource file: "
            + ((configFile == null) ? "null" : configFile.getPath());
        throw new RuntimeException(error);
      }

      lastModified = Math.max(lastModified, configFile.lastModified());
    }
    return lastModified;
  }

  /**
   * get a file name from a resource name
   * 
   * @param resourceName
   *          is the classpath location
   * @param log 
   * @param isDebug 
   * @param isInfo 
   * 
   * @return the file path on the system
   */
  public static File fileFromResourceNameHelper(String resourceName, StringBuffer log,
      boolean isDebug, boolean isInfo) {

    //consider log not null
    isDebug = isDebug && (log != null);
    isInfo = isInfo && (log != null);

    URL url = computeUrl(resourceName, true);

    if (url == null) {
      if (isInfo) {
        log.append(", Could not find the URL of resource when converting to file: "
            + resourceName);

      }
      return null;
    }

    if (isDebug) {
      log.append(", URL from FileUtils.url() is: " + url);
    }

    String fileName = null;
    File configFile = null;
    try {
      fileName = URLDecoder.decode(url.getFile(), "UTF-8");
  
      configFile = new File(fileName);
  
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }

    if (configFile == null) {
      if (isInfo) {
        log.append(", Could not find the File of URL when converting to file: "
            + resourceName + ", " + url);
      }
      return null;
    }

    return configFile;
  }

  /**
   * fast class loader
   * @return the class loader
   */
  public static ClassLoader fastClassLoader() {
    return MorphStringUtils.class.getClassLoader();
  }

  /**
   * compute a url of a resource
   * @param resourceName
   * @param canBeNull if cant be null, throw runtime
   * @return the URL
   */
  public static URL computeUrl(String resourceName, boolean canBeNull) {
    //get the url of the navigation file
    ClassLoader cl = fastClassLoader();

    URL url = null;

    try {
      url = cl.getResource(resourceName);
    } catch (NullPointerException npe) {
      String error = "getUrl() Could not find resource file: " + resourceName;
      throw new RuntimeException(error, npe);
    }

    if (!canBeNull && url == null) {
      throw new RuntimeException("Cant find resource: " + resourceName);
    }

    return url;
  }

  /**
   * Get a specific index of an array or collection (note for collections and iterating,
   * it is more efficient to get an iterator and iterate
   * @param arrayOrCollection
   * @param index
   * @return the object at that index
   */
  public static Object get(Object arrayOrCollection, int index) {

    if (arrayOrCollection == null) {
      if (index == 0) {
        return null;
      }
      throw new RuntimeException("Trying to access index " + index + " of null");
    }

    //no need to iterator an arraylist (NOT LINKED LIST)
    if (arrayOrCollection instanceof ArrayList) {
      return ((ArrayList) arrayOrCollection).get(index);
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

    throw new RuntimeException("Trying to access index " + index + " of and object: "
        + arrayOrCollection);
  }

  /**
   * Helper method to find the next match in an array of strings replace
   * @param searchForLength
   * @param searchFor
   * @param replaceWith
   * @param iteratorSearchFor
   * @param iteratorReplaceWith
   * @param noMoreMatchesForReplIndex
   * @param input
   * @param start is where to start looking
   * @return result packed into a long, inputIndex first, then replaceIndex
   */
  private static long findNextIndexHelper(int searchForLength, Object searchFor,
      Object replaceWith, Iterator iteratorSearchFor, Iterator iteratorReplaceWith,
      boolean[] noMoreMatchesForReplIndex, String input, int start) {

    int inputIndex = -1;
    int replaceIndex = -1;

    String currentSearchFor = null;
    String currentReplaceWith = null;
    int tempIndex = -1;
    for (int i = 0; i < searchForLength; i++) {
      currentSearchFor = (String) next(searchFor, iteratorSearchFor, i);
      currentReplaceWith = (String) next(replaceWith, iteratorReplaceWith, i);
      if (noMoreMatchesForReplIndex[i] || isEmpty(currentSearchFor)
          || currentReplaceWith == null) {
        continue;
      }
      tempIndex = input.indexOf(currentSearchFor, start);

      //see if we need to keep searching for this
      noMoreMatchesForReplIndex[i] = tempIndex == -1;

      if (tempIndex != -1 && (inputIndex == -1 || tempIndex < inputIndex)) {
        inputIndex = tempIndex;
        replaceIndex = i;
      }

    }
    //dont create an array, no more objects
    long resultPacked = packInts(inputIndex, replaceIndex);
    return resultPacked;
  }

  /**
   * pack two ints into a long.  Note: the first is held in the left bits, 
   * the second is held in the right bits
   * @param first is first int
   * @param second is second int
   * @return the long which has two ints in there
   */
  public static long packInts(int first, int second) {
    long result = first;
    result <<= 32;
    result |= second;
    return result;
  }

  /**
   * replace a string or strings from a string, and put the output in a string buffer
   * @param outBuffer stringbuffer to write to
   * @param text string to look in
   * @param searchFor string array to search for
   * @param replaceWith string array to replace with
   * @param recurse if true then do multiple replaces 
   * (on the replacements)
   */
  public static void replace(StringBuffer outBuffer, String text, Object searchFor,
      Object replaceWith, boolean recurse) {
    replace(outBuffer, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0);
  }

  /**
   * replace a string or strings from a string, and put the output in a string buffer.  This does not recurse
   * @param outBuffer stringbuffer to write to
   * @param text string to look in
   * @param searchFor string array to search for
   * @param replaceWith string array to replace with
   */
  public static void replace(StringBuffer outBuffer, String text, Object searchFor,
      Object replaceWith) {
    replace(outBuffer, null, text, searchFor, replaceWith, false, 0);
  }

  /**
   * replace a string or strings from a string, and put the output in a string buffer
   * @param outWriter writer to write to
   * @param text string to look in
   * @param searchFor string array to search for
   * @param replaceWith string array to replace with
   * @param recurse if true then do multiple replaces 
   * (on the replacements)
   */
  public static void replace(Writer outWriter, String text, Object searchFor,
      Object replaceWith, boolean recurse) {
    replace(null, outWriter, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0);
  }

  /**
   * replace a string or strings from a string, and put the output in a string buffer.  This does not recurse
   * @param outWriter writer to write to
   * @param text string to look in
   * @param searchFor string array to search for
   * @param replaceWith string array to replace with
   */
  public static void replace(Writer outWriter, String text, Object searchFor,
      Object replaceWith) {
    replace(null, outWriter, text, searchFor, replaceWith, false, 0);
  }

  /**
   * replace a string or strings from a string, and put the output in a string buffer
   * @param text string to look in
   * @param searchFor string array to search for
   * @param replaceWith string array to replace with
   * @param recurse if true then do multiple replaces 
   * (on the replacements)
   * @return the string
   */
  public static String replace(String text, Object searchFor, Object replaceWith,
      boolean recurse) {
    return replace(null, null, text, searchFor, replaceWith, recurse,
        recurse ? length(searchFor) : 0);
  }

  /**
   * replace a string or strings from a string, and put the output in a string buffer.  This does not recurse
   * @param text string to look in
   * @param searchFor string array to search for
   * @param replaceWith string array to replace with
   * @return the string
   */
  public static String replace(String text, Object searchFor, Object replaceWith) {
    return replace(null, null, text, searchFor, replaceWith, false, 0);
  }

  /**
   * replace a string with other strings, and either 
   * write to outWriter, or StringBuffer, and if StringBuffer
   * potentially return a string.  If outBuffer and outWriter
   * are null, then return the String
   * @param outBuffer stringbuffer to write to, or null to not
   * @param outWriter Writer to write to, or null to not.
   * @param text string to look in
   * @param searchFor string array to search for, or string, or list
   * @param replaceWith string array to replace with, or string, or list
   * @param recurse if true then do multiple replaces 
   * (on the replacements)
   * @param timeToLive if recursing, prevent endless loops
   * @return the String if outBuffer and outWriter are null
   * @throws IndexOutOfBoundsException
   *             if the lengths of the arrays are not the same (null is ok,
   *             and/or size 0)
   * @throws IllegalArgumentException
   *             if the search is recursive and there is an endless loop due
   *             to outputs of one being inputs to another
   */
  private static String replace(StringBuffer outBuffer, Writer outWriter, String text,
      Object searchFor, Object replaceWith, boolean recurse, int timeToLive) {

    //if recursing, we need to get the string, then print to buffer (since we need multiple passes)
    if (!recurse) {
      return replaceHelper(outBuffer, outWriter, text, searchFor, replaceWith, recurse,
          timeToLive);
    }
    //get the string
    String result = replaceHelper(null, null, text, searchFor, replaceWith, recurse,
        timeToLive);
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
   * take a long
   * @param theLong to unpack
   * @param isFirst true for first, false for second
   * @return one of the packed ints, first or second
   */
  public static int unpackInt(long theLong, boolean isFirst) {

    int result = 0;
    //put this in the position of the second one
    if (isFirst) {
      theLong >>= 32;
    }
    //only look at right part
    result = (int) (theLong & 0xffffffff);
    return result;
  }

  /**
   * replace a string with other strings, and either 
   * write to outWriter, or StringBuffer, and if StringBuffer
   * potentially return a string.  If outBuffer and outWriter
   * are null, then return the String
   * @param outBuffer stringbuffer to write to, or null to not
   * @param outWriter Writer to write to, or null to not.
   * @param text string to look in
   * @param searchFor string array to search for, or string, or list
   * @param replaceWith string array to replace with, or string, or list
   * @param recurse if true then do multiple replaces 
   * (on the replacements)
   * @param timeToLive if recursing, prevent endless loops
   * @return the String if outBuffer and outWriter are null
   * @throws IllegalArgumentException
   *             if the search is recursive and there is an endless loop due
   *             to outputs of one being inputs to another
   * @throws IndexOutOfBoundsException
   *             if the lengths of the arrays are not the same (null is ok,
   *             and/or size 0)
   */
  private static String replaceHelper(StringBuffer outBuffer, Writer outWriter,
      String text, Object searchFor, Object replaceWith, boolean recurse, int timeToLive) {

    try {
      //if recursing, this shouldnt be less than 0
      if (timeToLive < 0) {
        throw new IllegalArgumentException("TimeToLive under 0: " + timeToLive + ", "
            + text);
      }

      int searchForLength = length(searchFor);
      boolean done = false;
      //no need to do anything
      if (isEmpty(text)) {
        return text;
      }
      //need to write the input to output, later
      if (searchForLength == 0) {
        done = true;
      }

      boolean[] noMoreMatchesForReplIndex = null;
      int inputIndex = -1;
      int replaceIndex = -1;
      long resultPacked = -1;
      Iterator iteratorSearchFor = null;
      Iterator iteratorReplaceWith = null;

      if (!done) {
        //make sure lengths are ok, these need to be equal
        if (searchForLength != length(replaceWith)) {
          throw new IndexOutOfBoundsException("Lengths dont match: " + searchForLength
              + ", " + length(replaceWith));
        }

        //keep track of which still have matches
        noMoreMatchesForReplIndex = new boolean[searchForLength];

        //index of replace array that will replace the search string found
        iteratorSearchFor = iterator(searchFor);
        iteratorReplaceWith = iterator(replaceWith);

        resultPacked = findNextIndexHelper(searchForLength, searchFor, replaceWith,
            iteratorSearchFor, iteratorReplaceWith, noMoreMatchesForReplIndex, text, 0);

        inputIndex = unpackInt(resultPacked, true);
        replaceIndex = unpackInt(resultPacked, false);
      }

      //get a good guess on the size of the result buffer so it doesnt have to double if it
      //goes over a bit
      boolean writeToWriter = outWriter != null;

      //no search strings found, we are done
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

      //no buffer if writing to writer
      StringBuffer bufferToWriteTo = outBuffer != null ? outBuffer
          : (writeToWriter ? null : new StringBuffer(text.length()
              + replaceStringsBufferIncrease(text, searchFor, replaceWith)));

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
          appendSubstring(bufferToWriteTo, text, start, inputIndex).append(replaceString);
        }
        start = inputIndex + searchString.length();

        resultPacked = findNextIndexHelper(searchForLength, searchFor, replaceWith,
            iteratorSearchFor, iteratorReplaceWith, noMoreMatchesForReplIndex, text,
            start);
        inputIndex = unpackInt(resultPacked, true);
        replaceIndex = unpackInt(resultPacked, false);
      }
      if (writeToWriter) {
        outWriter.write(text, start, text.length() - start);

      } else {
        appendSubstring(bufferToWriteTo, text, start, text.length());
      }

      //no need to convert to string if incoming buffer or writer
      if (writeToWriter || outBuffer != null) {
        if (recurse) {
          throw new IllegalArgumentException(
              "Cannot recurse and write to existing buffer or writer!");
        }
        return null;
      }
      String resultString = bufferToWriteTo.toString();

      if (recurse) {
        return replaceHelper(outBuffer, outWriter, resultString, searchFor, replaceWith,
            recurse, timeToLive - 1);
      }
      //this might be null for writer
      return resultString;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * append a substring to a stringbuffer.  removes dependency on substring which 
   * creates objects
   * @param buf stringbuffer
   * @param string source string
   * @param start start index of source string
   * @param end end index of source string
   * @return the string buffer for chaining
   */
  private static StringBuffer appendSubstring(StringBuffer buf, String string, int start,
      int end) {
    for (int i = start; i < end; i++) {
      buf.append(string.charAt(i));
    }
    return buf;
  }

  /**
   * give a bext guess on buffer increase for String[] replace
   * get a good guess on the size of the result buffer so it doesnt have to double if it
   * goes over a bit
   * @param text
   * @param repl
   * @param with
   * @return the increase, with 20% cap
   */
  static int replaceStringsBufferIncrease(String text, Object repl, Object with) {
    //count the greaters
    int increase = 0;
    Iterator iteratorReplace = iterator(repl);
    Iterator iteratorWith = iterator(with);
    int replLength = length(repl);
    String currentRepl = null;
    String currentWith = null;
    for (int i = 0; i < replLength; i++) {
      currentRepl = (String) next(repl, iteratorReplace, i);
      currentWith = (String) next(with, iteratorWith, i);
      int greater = currentWith.length() - currentRepl.length();
      increase += greater > 0 ? 3 * greater : 0; //assume 3 matches
    }
    //have upper-bound at 20% increase, then let Java take over
    increase = Math.min(increase, text.length() / 5);
    return increase;
  }

  /**
   * make sure a directory exists, if not, create it
   * 
   * @param path
   *          is the local path to the directory
   */
  public static void assertDirExists(String path) {
    File theDir = new File(path);

    if (!theDir.exists()) {
      if (!theDir.mkdirs()) {
        throw new RuntimeException("Cannot create directory: " + theDir);
      }
    }
  }

  /**
   * Null safe arrayList length
   * @param arrayList is the array list to test
   * @return the length of the arrayList (0 for null)
   */
  public static int collectionLength(Collection arrayList) {
    if (arrayList == null) {
      return 0;
    }
    return arrayList.size();
  }

  /**
   * If you want to grow an array (or shrink it) to a certain size
   * 
   * @param currentArray
   *          is the array to grow. Can be null, will return null if so
   * @param newSize
   *          is the new size which can be greater than or less than or equal to
   *          the old size
   * 
   * @return the new array (typecast to the array type)
   */
  public static Object growArrayOrList(Object currentArray, int newSize) {
    if (currentArray == null) {
      return null;
    }

    //see if anything needs to be done
    int oldArraySize = length(currentArray);

    if (oldArraySize == newSize) {
      return currentArray;
    }

    if (!currentArray.getClass().isArray() && (!(currentArray instanceof List))) {
      throw new RuntimeException(
          "You can't grow a non-array or list as an array or list! "
              + currentArray.getClass());
    }

    Object newArray = null;

    //init all the objects
    if (currentArray.getClass().isArray()) {
      newArray = Array.newInstance(currentArray.getClass().getComponentType(), newSize);
    } else {
      newArray = new ArrayList();
      for (int i = 0; i < newSize; i++) {
        ((List) newArray).add(null);
      }
    }

    int numberToCopyOver = newSize > oldArraySize ? oldArraySize : newSize;

    //ocpy over old ones to new object
    if (currentArray.getClass().isArray()) {
      System.arraycopy(currentArray, 0, newArray, 0, numberToCopyOver);
    } else {
      List currentList = (List) currentArray;
      List newList = (List) newArray;
      for (int i = 0; i < numberToCopyOver; i++) {
        newList.set(i, currentList.get(i));
      }
    }

    return newArray;

  }

  /**
   * return true if newline or formfeed
   * @param input
   * @return true if newline
   */
  public static boolean isNewline(char input) {
    return input == '\n' || input == '\r';
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
   * safely assign an object to a list (add if not exist)
   * @param arrayOrList is array or set or list (if null, make an array
   * @param index
   * @param value
   * @return the arrayOrListOrSet
   */
  public static Object set(Object arrayOrList, int index, Object value) {

    //by default make an array
    if (arrayOrList == null) {
      arrayOrList = Array.newInstance(value == null ? Object.class : value.getClass(),
          index + 1);
    }

    if (arrayOrList instanceof Set) {
      ((Set) arrayOrList).add(value);
      return arrayOrList;
    }

    if (arrayOrList instanceof List) {
      List list = (List) arrayOrList;
      if (index >= list.size()) {
        while (index > list.size()) {
          list.add(null);
        }
        list.add(index, value);
      } else {
        list.set(index, value);
      }
      return list;
    }

    if (arrayOrList.getClass().isArray()) {
      if (index >= Array.getLength(arrayOrList)) {
        arrayOrList = growArrayOrList(arrayOrList, index + 1);
      }
      Array.set(arrayOrList, index, value);
      return arrayOrList;
    }
    //if just an object, return the new object
    if (index == 0) {
      return value;
    }
    throw new RuntimeException("Invalid arguments: " + arrayOrList + ", " + index + ", "
        + value);
  }

  /** special chars array to replace */
  private static final String[] SPECIAL_CHARS_FROM = new String[] { "%", "(", ")", ",",
      "\\", ";", "\"", "'", "_" };

  /** special chars array to replace with */
  private static final String[] SPECIAL_CHARS_TO = new String[] { "%25", "%28", "%29",
      "%2C", "%5C", "%3B", "%22", "%27", "%5F" };

  /**
   * Unsubstitute special chars from inputs (at least used for regex). If you
   * change this, change the equivalent Javascript method.
   * 
   * %28 -> ( %29 -> ) %2C -> , %3B -> ; %22 -> " %27 -> ' %25 -> % %5F -> _ %5C -> \
   * 
   * @param input
   *          is the input string
   * @param isToSpecialChars
   *          true if the output has special chars, false if not
   * @return the string subbed with special chars
   */
  public static String specialChars(String input, boolean isToSpecialChars) {
    if (isEmpty(input)) {
      return input;
    }

    if (isToSpecialChars) {

      input = replace(input, SPECIAL_CHARS_FROM, SPECIAL_CHARS_TO);

    } else {

      input = replace(input, SPECIAL_CHARS_TO, SPECIAL_CHARS_FROM);
    }
    return input;
  }

  /** 
   * list files with a certain extension 
   * @param dir
   * @param extension if this is the empty string it should list all
   * @return the array of files
   */
  public static File[] listFilesByExtension(File dir, String extension) {
    final String finalExtension = extension;

    FilenameFilter fileFilter = new FilenameFilter() {

      /*
       * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
       */
      public boolean accept(File theDir, String name) {
        if ((name != null) && name.endsWith(finalExtension)) {
          //doubt we would ever look for .., but in case
          if (contains(finalExtension, "..")) {
            return true;
          }
          //if the file is .., then its not what we are looking for
          if (contains(name, "..")) {
            return false;
          }
          return true;
        }

        return false;
      }
    };

    return dir.listFiles(fileFilter);
  }

  /** 
   * list files with a certain prefix 
   * @param dir
   * @param prefix if this is the empty string it should list all
   * @return the array of files
   */
  public static File[] listFilesByPrefix(File dir, final String prefix) {

    FilenameFilter fileFilter = new FilenameFilter() {

      /*
       * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
       */
      public boolean accept(File theDir, String name) {
        if ((name != null) && name.startsWith(prefix)) {
          //doubt we would ever look for .., but in case
          if (contains(prefix, "..")) {
            return true;
          }
          //if the file is .., then its not what we are looking for
          if (contains(name, "..")) {
            return false;
          }
          return true;
        }

        return false;
      }
    };

    return dir.listFiles(fileFilter);
  }

  /** 
   * list files with a certain extension.  Note, there cannot be more than 10000
   * files or exception will be throws
   * @param dir
   * @param extension if this is the empty string it should list all
   * @return the array of files
   */
  public static List listFilesByExtensionRecursive(File dir, String extension) {
    List theList = new ArrayList();
    listFilesByExtensionRecursiveHelper(dir, extension, theList);
    return theList;
  }

  /** 
   * list files with a certain extension 
   * @param dir
   * @param extension if this is the empty string it should list all
   * @param theList is the current list to append to
   */
  private static void listFilesByExtensionRecursiveHelper(File dir, String extension,
      List theList) {
    //see if its a directory
    if (!dir.exists()) {
      throw new RuntimeException("The directory: " + dir + " does not exist");
    }
    if (!dir.isDirectory()) {
      throw new RuntimeException("The directory: " + dir + " is not a directory");
    }

    //get the files into a list
    File[] allFiles = listFilesByExtension(dir, extension);

    //loop through the array
    for (int i = 0; i < allFiles.length; i++) {
      if (contains(allFiles[i].getName(), "..")) {
        continue; //dont go to the parent directory
      }

      if (allFiles[i].isFile()) {

        //make sure not too big
        if (theList.size() > 10000) {
          throw new RuntimeException("File list too large: " + dir.getAbsolutePath()
              + ", " + extension);
        }

        //add to list
        theList.add(allFiles[i]);
      } else {
        //ignore, we will do all dirs in good time
      }
    }

    //do all the subdirs
    File[] allSubdirs = listSubdirs(dir);
    int allSubdirsLength = allSubdirs == null ? 0 : allSubdirs.length;
    for (int i = 0; i < allSubdirsLength; i++) {
      listFilesByExtensionRecursiveHelper(allSubdirs[i], extension, theList);
    }

  }

  /**
   * get the subdirs of a dir (not ..)
   * @param dir
   * @return the dirs
   */
  public static File[] listSubdirs(File dir) {
    //see if its a directory
    if (!dir.exists()) {
      throw new RuntimeException("The directory: " + dir + " does not exist");
    }
    if (!dir.isDirectory()) {
      throw new RuntimeException("The directory: " + dir + " is not a directory");
    }

    File[] subdirs = dir.listFiles(new FileFilter() {

      public boolean accept(File pathname) {
        if (contains(pathname.getName(), "..")) {
          return false; //dont go to the parent directory
        }
        //allow dirs
        if (pathname.isDirectory()) {
          return true;
        }
        //must not be a dir
        return false;
      }

    });

    return subdirs;
  }

  /**
   * see if we are running on windows
   * @return true if windows
   */
  public static boolean isWindows() {
    String osname = System.getProperty("os.name");

    if (contains(osname, "Windows")) {
      return true;
    }
    return false;
  }

  /**
   * execute a command and return the output
   * @param command
   * @return the output (end errors), and trim the output
   */
  public static String execCommand(String command) {
    Runtime runtime = Runtime.getRuntime();
  
    Process process = null;
    BufferedReader reader = null;
  
    StringBuffer result = new StringBuffer();
  
    try {
      process = runtime.exec(command);
  
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
  
      String line = null;
  
      while ((line = reader.readLine()) != null) {
        
        result.append(line);
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  
    String resultString = result.toString();
    return resultString.trim();
  }

  /**
   * if the input is a file, read string from file.  if not, or if disabled from grouper.properties, return the input
   * @param in
   * @return the result
   */
  public static String readFromFileIfFile(String in) {
    return readFromFileIfFile(in, 
        MorphPropertyFileUtils.retrievePropertyBoolean("grouper.encrypt.disableExternalFileLookup", false));
  }

  /**
   * if the input is a file, read string from file.  
   * if not, or if disabled from grouper.properties, return the input
   * @param in
   * @param disableExternalFileLookup 
   * @return the result
   */
  public static String readFromFileIfFile(String in, boolean disableExternalFileLookup) {
    
    String newIn = in;
    
    //convert both slashes to file slashes
    if (File.separatorChar == '/') {
      newIn = replace(newIn, "\\", "/");
    } else {
      newIn = replace(newIn, "/", "\\");
    }
    
    //see if it is a file reference
    if (newIn.indexOf(File.separatorChar) != -1 && !disableExternalFileLookup) {
      //read the contents of the file into a string
      newIn = readFileIntoString(new File(newIn));
      return newIn;
    }
    //return original
    return in;
  }
  
  /**
   * <p>Checks if a String is whitespace, empty ("") or null.</p>
   *
   * <pre>
   * StringUtils.isBlank(null)      = true
   * StringUtils.isBlank("")        = true
   * StringUtils.isBlank(" ")       = true
   * StringUtils.isBlank("bob")     = false
   * StringUtils.isBlank("  bob  ") = false
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

  
}
