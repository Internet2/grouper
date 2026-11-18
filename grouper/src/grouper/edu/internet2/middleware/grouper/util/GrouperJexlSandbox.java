/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlInfo;
import org.apache.commons.jexl2.introspection.UberspectImpl;
import org.apache.commons.jexl3.JexlArithmetic;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlOperator;
import org.apache.commons.jexl3.internal.introspection.BooleanGetExecutor;
import org.apache.commons.jexl3.internal.introspection.DuckGetExecutor;
import org.apache.commons.jexl3.internal.introspection.DuckSetExecutor;
import org.apache.commons.jexl3.internal.introspection.FieldGetExecutor;
import org.apache.commons.jexl3.internal.introspection.FieldSetExecutor;
import org.apache.commons.jexl3.internal.introspection.ListGetExecutor;
import org.apache.commons.jexl3.internal.introspection.ListSetExecutor;
import org.apache.commons.jexl3.internal.introspection.MapGetExecutor;
import org.apache.commons.jexl3.internal.introspection.MapSetExecutor;
import org.apache.commons.jexl3.internal.introspection.MethodExecutor;
import org.apache.commons.jexl3.internal.introspection.PropertyGetExecutor;
import org.apache.commons.jexl3.internal.introspection.PropertySetExecutor;
import org.apache.commons.jexl3.internal.introspection.Uberspect;
import org.apache.commons.jexl3.introspection.JexlMethod;
import org.apache.commons.jexl3.introspection.JexlPropertyGet;
import org.apache.commons.jexl3.introspection.JexlPropertySet;
import org.apache.commons.jexl3.introspection.JexlUberspect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * <pre>
 * Sandboxed JEXL engines for the restricted expression path of GrouperUtil.
 *
 * GrouperUtil.substituteExpressionLanguage / substituteExpressionLanguageScript /
 * substituteExpressionLanguageTemplate put a full GrouperUtil instance in every context as
 * "grouperUtil", and the engines they used had no limits on what an expression can call.
 * GrouperUtil is not a safe expression helper (gshRunScript, file read/write/delete, forName,
 * newInstance, callMethod, url helpers ...), and any expression can reach reflection with
 * ''.class.forName(...).  Some of these expressions are written by non-wheel users, e.g. the
 * recipient / subject / body of a predefined send-email rule, which runs as GrouperSystem.
 *
 * When the caller passes allowStaticClasses=false (the default 2-arg call, rules, permission
 * limits, external subject descriptions, UI screen labels), GrouperUtil now evaluates on the
 * engines from this class, which:
 *   - allow only an allowlist of GrouperUtil methods (string, collection, conversion and
 *     formatting helpers; the method names of the old GrouperUtilElSafe facade plus a few
 *     escapers used in base config), however the GrouperUtil got into the context
 *   - deny every member of reflection, class loading, process, thread, file, network, scripting
 *     and Groovy / GSH classes, and every method that returns one of those (so getClass() and
 *     ''.class are denied)
 *   - deny every member of a java.lang.Class object (no static calls through a class object)
 *   - deny all constructors (new(...)); JEXL 3.0 has no namespace or import pragmas
 * Everything else (public methods of the variables a caller puts in the map) still works, which
 * is what GRP-6773 needed.
 *
 * allowStaticClasses=true keeps the unrestricted engines in GrouperUtil.  That is the privileged
 * evaluator for sysadmin-authored config only (loader, provisioning, config UI, GSH templates).
 *
 * This is the variant for releases that ship commons-jexl 2.1.1 and commons-jexl3 3.0 (2.6.x,
 * 4.x and 5.x).  JEXL 3.0 has no JexlPermissions / JexlFeatures, so the template engine uses a
 * delegating JexlUberspect with the same rules instead.  The deny list, the GrouperUtil
 * allowlist and the JEXL2 part are the same as in the 6.x / 7.x variant.  The GrouperUtilElSafe
 * object these releases inject as "grouperUtil" by default keeps its methods (it is the
 * restricted facade already); only the general deny rules apply to it.
 * </pre>
 */
public class GrouperJexlSandbox {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperJexlSandbox.class);

  // ------------------------------------------------------------------------------------------
  // allowlist and deny list
  // ------------------------------------------------------------------------------------------

  /**
   * GrouperUtil methods an expression on the restricted path may call.  This is the method name
   * list of GrouperUtilElSafe as of 5.22.5 (the facade the restricted path used before it was
   * removed), minus sleep / class name helpers, plus escapers and helpers used by base config
   * and documented examples.  Every grouperUtil.xxx call in *.base.properties, the text
   * properties and the built-in templates is in this list.
   */
  static final Set<String> GROUPER_UTIL_ALLOWED_METHODS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
      "MapToString", "abbreviate", "addIfNotThere", "append", "appendIfNotBlank", "appendIfNotBlankString",
      "appendIfNotEmpty", "appendPrefixIfStringNotBlank", "argAfter", "arrayPopOne", "assertion", "batchList",
      "batchNumberOfBatches", "bigDecimalObjectValue", "booleanObjectValue", "booleanValue", "byteCountToDisplaySize", "byteObjectValue",
      "byteValue", "capitalize", "capitalizeFully", "charObjectValue", "charValue", "characterMatchesRegex",
      "collectionContains", "collectionPopOne", "collectionToString", "compare", "concat", "contains",
      "containsNewline", "convertLongToChar", "convertLongToCharSmall", "convertLongToString", "convertLongToStringSmall", "convertMillisToFriendlyString",
      "dateLongValue", "dateStringValue", "dateToString", "dateValue", "defaultIfBlank", "defaultIfEmpty",
      "defaultIfNull", "defaultString", "doubleObjectValue", "doubleValue", "doubleValueNoError", "equals",
      "equalsIgnoreCase", "escapeHtml", "escapeUrlDecode", "escapeUrlEncode", "extensionFromName", "floatObjectValue",
      "floatValue", "floatValueNoError", "formatNumberWithCommas", "get", "getCause", "getFirstParentStemOfName",
      "getFriendlyTimezoneStringForInputDescription", "getFullStackTrace", "getMaxLongValue", "getMinLongValue", "getThrowables", "hostname",
      "incrementChar", "incrementStringInt", "indent", "indexOf", "intObjectValue", "intValue",
      "intValueNoError", "ipInt", "ipOnNetwork", "ipOnNetworks", "ipReadyForAnd", "isAscii",
      "isBlank", "isEmpty", "isNestedThrowable", "isNotBlank", "isScalar", "isThrowableNested",
      "iterator", "join", "ldapAdDateCurrent", "ldapAdDateFromMillis1970", "ldapFilterEscape", "leftPad",
      "length", "lengthAscii", "listPopOne", "longObjectValue", "longValue", "longValueNoError",
      "mapToString", "monthInt", "next", "nonNull", "normalize", "normalizeEmailAddresses",
      "nullOrBlank", "packInts", "parentStemNameFromName", "prefixOrSuffix", "remove", "repeat",
      "replace", "replaceWhitespaceWithSpace", "rightPad", "setPopOne", "setShorten", "setToString",
      "shortObjectValue", "shortValue", "split", "splitByWholeSeparator", "splitPreserveAllTokens", "splitTrim",
      "splitTrimCommas", "splitTrimCurlyColons", "splitTrimToList", "splitTrimToSet", "stringLength", "stringToDate",
      "stringToDate2", "stringToTimestamp", "stringValue", "stripEnd", "stripLastSlashIfExists", "stripStart",
      "stripSuffix", "subjectHasAttribute", "subjectToString", "substringAfter", "substringAfterLast", "substringBefore",
      "substringBeforeLast", "suffixAfterChar", "toArray", "toList", "toListObject", "toLowerCase",
      "toMap", "toSet", "toSetObject", "toString", "toStringForLog", "toStringObjectMap",
      "toStringSafe", "toTimestamp", "toUpperCase", "trim", "trimToEmpty", "truncateAscii",
      "uniqueId", "unpackInt", "validEmail", "xmlEscape"
      )));

  /**
   * classes whose members are denied (and methods returning them).  Matched by exact name on the
   * class and its superclasses.
   */
  private static final Set<String> DENIED_CLASSES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
      "java.lang.Class", "java.lang.ClassLoader", "java.lang.Runtime", "java.lang.ProcessBuilder",
      "java.lang.Process", "java.lang.ProcessHandle", "java.lang.System", "java.lang.Thread",
      "java.lang.ThreadGroup", "java.lang.SecurityManager", "java.lang.Module", "java.lang.ModuleLayer",
      "java.lang.StackWalker", "java.lang.InheritableThreadLocal", "java.lang.ThreadLocal",
      "java.util.ServiceLoader",
      // the engines themselves (an expression must not build a new, unrestricted engine)
      "org.apache.commons.jexl2.JexlEngine", "org.apache.commons.jexl2.UnifiedJEXL",
      "org.apache.commons.jexl3.JexlEngine", "org.apache.commons.jexl3.JexlBuilder",
      "org.apache.commons.jexl3.JxltEngine",
      // grouperClient copies of the dangerous GrouperUtil methods (file, forName, callMethod ...)
      "edu.internet2.middleware.grouperClient.util.GrouperClientUtils",
      "edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils",
      "edu.internet2.middleware.morphString.MorphStringUtils",
      "edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils"
      )));

  /**
   * package prefixes whose members are denied (and methods returning them).  Matched on the
   * class and its superclasses (not interfaces, so e.g. java.io.Serializable does not deny
   * String).
   */
  private static final String[] DENIED_PACKAGE_PREFIXES = new String[] {
      "java.io.", "java.nio.", "java.net.", "java.lang.reflect.", "java.lang.invoke.",
      "java.lang.instrument.", "java.lang.management.", "java.lang.ref.", "java.rmi.", "java.sql.",
      "javax.sql.", "javax.script.", "javax.naming.", "javax.management.", "javax.tools.",
      "sun.", "jdk.", "com.sun.",
      "groovy.", "org.codehaus.groovy.", "org.apache.groovy.",
      "org.apache.commons.io.", "org.apache.commons.exec.",
      "org.hibernate.", "edu.internet2.middleware.grouper.hibernate.",
      "edu.internet2.middleware.grouper.app.gsh."
  };

  /**
   * reflect.Array is how JEXL reads array elements (arr[0]), allow it even though
   * java.lang.reflect is denied
   */
  private static final String ALLOWED_REFLECT_ARRAY = "java.lang.reflect.Array";

  /** cache of class decisions, true if denied */
  private static final Map<Class<?>, Boolean> deniedClassCache = Collections.synchronizedMap(new HashMap<Class<?>, Boolean>());

  /**
   * if a class (or one of its superclasses) is denied to sandboxed expressions
   * @param theClass
   * @return true if denied
   */
  static boolean isDeniedClass(Class<?> theClass) {
    if (theClass == null) {
      return false;
    }
    // arrays: decide on the component type (e.g. File[] is denied, String[] is not)
    while (theClass.isArray()) {
      theClass = theClass.getComponentType();
    }
    if (theClass.isPrimitive()) {
      return false;
    }
    Boolean denied = deniedClassCache.get(theClass);
    if (denied != null) {
      return denied;
    }
    denied = false;
    for (Class<?> current = theClass; current != null; current = current.getSuperclass()) {
      if (isDeniedClassName(current.getName())) {
        denied = true;
        break;
      }
    }
    deniedClassCache.put(theClass, denied);
    return denied;
  }

  /**
   * @param className
   * @return true if the class name itself is on the deny list
   */
  private static boolean isDeniedClassName(String className) {
    if (DENIED_CLASSES.contains(className)) {
      return true;
    }
    if (ALLOWED_REFLECT_ARRAY.equals(className)) {
      return false;
    }
    for (String prefix : DENIED_PACKAGE_PREFIXES) {
      if (className.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * if a method may be called (or used as a property getter / setter) by a sandboxed expression
   * @param method
   * @return true if allowed
   */
  static boolean isAllowedMethod(Method method) {
    if (method == null) {
      return false;
    }
    Class<?> declaringClass = method.getDeclaringClass();
    // GrouperUtil: only the allowlist, no matter how the instance got into the context
    if (declaringClass == GrouperUtil.class) {
      return GROUPER_UTIL_ALLOWED_METHODS.contains(method.getName());
    }
    if (isDeniedClass(declaringClass)) {
      return false;
    }
    // e.g. getClass(), getClassLoader(), currentThread(), Enum.getDeclaringClass()
    if (isDeniedClass(method.getReturnType())) {
      return false;
    }
    return true;
  }

  /**
   * if a public field may be read or written by a sandboxed expression
   * @param field
   * @return true if allowed
   */
  static boolean isAllowedField(Field field) {
    if (field == null) {
      return false;
    }
    if (field.getDeclaringClass() == GrouperUtil.class) {
      // GrouperUtil fields are config / caches, not expression helpers
      return false;
    }
    return !isDeniedClass(field.getDeclaringClass()) && !isDeniedClass(field.getType());
  }

  /**
   * if an object may be the target of a member access by a sandboxed expression
   * @param obj
   * @return true if allowed
   */
  static boolean isAllowedTarget(Object obj) {
    if (obj == null) {
      return true;
    }
    // no static calls through a class object, and no Class methods (forName, getMethod ...)
    if (obj instanceof Class) {
      return false;
    }
    return !isDeniedClass(obj.getClass());
  }

  /**
   * log a denied access, so an admin can see why an expression stopped working
   * @param what
   * @param obj
   * @param name
   */
  private static void logDenied(String what, Object obj, Object name) {
    if (LOG.isWarnEnabled()) {
      String targetClass = obj == null ? "null" : (obj instanceof Class ? ("class " + ((Class<?>)obj).getName()) : obj.getClass().getName());
      LOG.warn("JEXL sandbox denied " + what + " '" + name + "' on " + targetClass
          + " (expression evaluated with allowStaticClasses=false)");
    }
  }

  // ------------------------------------------------------------------------------------------
  // JEXL2 (substituteExpressionLanguage and substituteExpressionLanguageScript)
  // ------------------------------------------------------------------------------------------

  /**
   * JEXL2 uberspect that applies the sandbox rules.  Methods and property executors are
   * resolved by the normal UberspectImpl, then checked against the underlying java method.
   */
  static class SandboxedJexl2Uberspect extends UberspectImpl {

    /**
     * @param runtimeLogger
     */
    public SandboxedJexl2Uberspect(Log runtimeLogger) {
      super(runtimeLogger);
    }

    /**
     * no constructors: new('java.lang.ProcessBuilder', ...) and the like
     */
    @Override
    public org.apache.commons.jexl2.introspection.JexlMethod getConstructorMethod(Object ctorHandle, Object[] args, JexlInfo info) {
      logDenied("constructor", null, ctorHandle);
      return null;
    }

    /**
     * no constructors (deprecated variant)
     */
    @Override
    @Deprecated
    public Constructor<?> getConstructor(Object ctorHandle, Object[] args, JexlInfo info) {
      logDenied("constructor", null, ctorHandle);
      return null;
    }

    /**
     * loops (for (x : obj)): the iterated object must pass the sandbox
     */
    @Override
    public java.util.Iterator<?> getIterator(Object obj, JexlInfo info) {
      if (!isAllowedTarget(obj)) {
        logDenied("iterator", obj, "");
        return null;
      }
      return super.getIterator(obj, info);
    }

    /**
     * method calls: target and resolved method must pass the sandbox
     */
    @Override
    public org.apache.commons.jexl2.introspection.JexlMethod getMethod(Object obj, String method, Object[] args, JexlInfo info) {
      if (!isAllowedTarget(obj)) {
        logDenied("method", obj, method);
        return null;
      }
      org.apache.commons.jexl2.introspection.JexlMethod jexlMethod = super.getMethod(obj, method, args, info);
      if (jexlMethod == null) {
        return null;
      }
      if (!(jexlMethod instanceof org.apache.commons.jexl2.internal.AbstractExecutor) || !isAllowedMethod(((org.apache.commons.jexl2.internal.AbstractExecutor)jexlMethod).getMethod())) {
        logDenied("method", obj, method);
        return null;
      }
      return jexlMethod;
    }

    /**
     * property reads (a.b, a['b']): getter method or public field must pass the sandbox
     */
    @Override
    public org.apache.commons.jexl2.introspection.JexlPropertyGet getPropertyGet(Object obj, Object identifier, JexlInfo info) {
      if (!isAllowedTarget(obj)) {
        logDenied("property", obj, identifier);
        return null;
      }
      org.apache.commons.jexl2.introspection.JexlPropertyGet get = super.getPropertyGet(obj, identifier, info);
      if (get == null) {
        return null;
      }
      if (get instanceof org.apache.commons.jexl2.internal.AbstractExecutor) {
        if (isAllowedMethod(((org.apache.commons.jexl2.internal.AbstractExecutor)get).getMethod())) {
          return get;
        }
      } else if (get instanceof FieldPropertyGet) {
        if (obj != null && identifier != null && isAllowedField(getField(obj, identifier.toString(), info))) {
          return get;
        }
      }
      // anything else (e.g. indexed getters) cannot be inspected, deny it
      logDenied("property", obj, identifier);
      return null;
    }

    /**
     * property writes (a.b = c): setter method or public field must pass the sandbox
     */
    @Override
    public org.apache.commons.jexl2.introspection.JexlPropertySet getPropertySet(Object obj, Object identifier, Object arg, JexlInfo info) {
      if (!isAllowedTarget(obj)) {
        logDenied("property set", obj, identifier);
        return null;
      }
      org.apache.commons.jexl2.introspection.JexlPropertySet set = super.getPropertySet(obj, identifier, arg, info);
      if (set == null) {
        return null;
      }
      if (set instanceof org.apache.commons.jexl2.internal.AbstractExecutor) {
        if (isAllowedMethod(((org.apache.commons.jexl2.internal.AbstractExecutor)set).getMethod())) {
          return set;
        }
      } else if (set instanceof FieldPropertySet) {
        if (obj != null && identifier != null && isAllowedField(getField(obj, identifier.toString(), info))) {
          return set;
        }
      }
      logDenied("property set", obj, identifier);
      return null;
    }
  }

  /** sandboxed JEXL2 engines by silent/lenient: index (silent ? 2 : 0) + (lenient ? 1 : 0) */
  private static final JexlEngine[] jexl2Engines = new JexlEngine[4];

  /** sandboxed JEXL3 engines by silent/lenient: index (silent ? 2 : 0) + (lenient ? 1 : 0) */
  private static final org.apache.commons.jexl3.JexlEngine[] jexl3Engines = new org.apache.commons.jexl3.JexlEngine[4];

  /**
   * @param silent
   * @param lenient
   * @return the array index
   */
  private static int engineIndex(boolean silent, boolean lenient) {
    return (silent ? 2 : 0) + (lenient ? 1 : 0);
  }

  /**
   * jexl.cacheSize like the unrestricted engines (same default)
   * @return the cache size
   */
  private static int cacheSize() {
    return GrouperConfig.retrieveConfig().propertyValueInt("jexl.cacheSize", 512);
  }

  /**
   * sandboxed JEXL2 engine for expressions and scripts with allowStaticClasses=false
   * @param silent
   * @param lenient
   * @return the engine
   */
  public static JexlEngine sandboxedJexl2Engine(boolean silent, boolean lenient) {
    int index = engineIndex(silent, lenient);
    JexlEngine engine = jexl2Engines[index];
    if (engine == null) {
      synchronized (GrouperJexlSandbox.class) {
        engine = jexl2Engines[index];
        if (engine == null) {
          Log jexlLog = LogFactory.getLog(JexlEngine.class);
          engine = new JexlEngine(new SandboxedJexl2Uberspect(jexlLog), null, null, jexlLog);
          engine.setSilent(silent);
          engine.setLenient(lenient);
          engine.setCache(cacheSize());
          jexl2Engines[index] = engine;
        }
      }
    }
    return engine;
  }

  // ------------------------------------------------------------------------------------------
  // JEXL3 (substituteExpressionLanguageTemplate), commons-jexl3 3.0
  // ------------------------------------------------------------------------------------------

  /**
   * the java method behind a JEXL 3.0 executor.  The common executor base class is not public
   * in 3.0, so check each public executor type.  Anything else returns null, which the sandbox
   * denies.
   * @param executor
   * @return the method or null
   */
  static Method jexl3ExecutorMethod(Object executor) {
    if (executor instanceof MethodExecutor) {
      return ((MethodExecutor)executor).getMethod();
    }
    if (executor instanceof PropertyGetExecutor) {
      return ((PropertyGetExecutor)executor).getMethod();
    }
    if (executor instanceof BooleanGetExecutor) {
      return ((BooleanGetExecutor)executor).getMethod();
    }
    // Map.get, and List.get or java.lang.reflect.Array.get (allowed) for arr[0]
    if (executor instanceof MapGetExecutor) {
      return ((MapGetExecutor)executor).getMethod();
    }
    if (executor instanceof ListGetExecutor) {
      return ((ListGetExecutor)executor).getMethod();
    }
    if (executor instanceof DuckGetExecutor) {
      return ((DuckGetExecutor)executor).getMethod();
    }
    if (executor instanceof PropertySetExecutor) {
      return ((PropertySetExecutor)executor).getMethod();
    }
    if (executor instanceof MapSetExecutor) {
      return ((MapSetExecutor)executor).getMethod();
    }
    if (executor instanceof ListSetExecutor) {
      return ((ListSetExecutor)executor).getMethod();
    }
    if (executor instanceof DuckSetExecutor) {
      return ((DuckSetExecutor)executor).getMethod();
    }
    return null;
  }

  /**
   * JEXL3 uberspect with the sandbox rules, for commons-jexl3 3.0 (no JexlPermissions or
   * JexlFeatures there).  It wraps the normal 3.0 Uberspect: members are resolved as usual,
   * then the target object and the underlying java method or field are checked.  Constructors
   * are always denied.  JEXL 3.0 has no namespace or import pragmas, and a MapContext is not a
   * namespace resolver, so a template cannot bind a namespace to a class.
   */
  static class SandboxedJexl3Uberspect implements JexlUberspect {

    /** the normal JEXL 3.0 uberspect that does the resolution */
    private final Uberspect base;

    /**
     * @param runtimeLogger
     */
    public SandboxedJexl3Uberspect(Log runtimeLogger) {
      this.base = new Uberspect(runtimeLogger, JexlUberspect.JEXL_STRATEGY);
    }

    @Override
    public List<PropertyResolver> getResolvers(JexlOperator op, Object obj) {
      return this.base.getResolvers(op, obj);
    }

    @Override
    public void setClassLoader(ClassLoader loader) {
      this.base.setClassLoader(loader);
    }

    @Override
    public int getVersion() {
      return this.base.getVersion();
    }

    /**
     * no constructors: new('java.lang.ProcessBuilder', ...) and the like
     */
    @Override
    public JexlMethod getConstructor(Object ctorHandle, Object... args) {
      logDenied("constructor", null, ctorHandle);
      return null;
    }

    /**
     * method calls: target and resolved method must pass the sandbox
     */
    @Override
    public JexlMethod getMethod(Object obj, String method, Object... args) {
      if (!isAllowedTarget(obj)) {
        logDenied("method", obj, method);
        return null;
      }
      JexlMethod jexlMethod = this.base.getMethod(obj, method, args);
      if (jexlMethod == null) {
        return null;
      }
      if (!isAllowedMethod(jexl3ExecutorMethod(jexlMethod))) {
        logDenied("method", obj, method);
        return null;
      }
      return jexlMethod;
    }

    @Override
    public JexlPropertyGet getPropertyGet(Object obj, Object identifier) {
      return getPropertyGet(null, obj, identifier);
    }

    /**
     * property reads (a.b, a['b'], arr[0]): getter method or public field must pass the sandbox
     */
    @Override
    public JexlPropertyGet getPropertyGet(List<PropertyResolver> resolvers, Object obj, Object identifier) {
      if (!isAllowedTarget(obj)) {
        logDenied("property", obj, identifier);
        return null;
      }
      JexlPropertyGet get = this.base.getPropertyGet(resolvers, obj, identifier);
      if (get == null) {
        return null;
      }
      if (get instanceof FieldGetExecutor) {
        if (obj != null && identifier != null && isAllowedField(this.base.getField(obj.getClass(), identifier.toString()))) {
          return get;
        }
      } else if (isAllowedMethod(jexl3ExecutorMethod(get))) {
        // getters, map get, list / array get (java.lang.reflect.Array is allowed), duck get
        return get;
      }
      // anything else (e.g. indexed containers) cannot be inspected, deny it
      logDenied("property", obj, identifier);
      return null;
    }

    @Override
    public JexlPropertySet getPropertySet(Object obj, Object identifier, Object arg) {
      return getPropertySet(null, obj, identifier, arg);
    }

    /**
     * property writes (a.b = c): setter method or public field must pass the sandbox
     */
    @Override
    public JexlPropertySet getPropertySet(List<PropertyResolver> resolvers, Object obj, Object identifier, Object arg) {
      if (!isAllowedTarget(obj)) {
        logDenied("property set", obj, identifier);
        return null;
      }
      JexlPropertySet set = this.base.getPropertySet(resolvers, obj, identifier, arg);
      if (set == null) {
        return null;
      }
      if (set instanceof FieldSetExecutor) {
        if (obj != null && identifier != null && isAllowedField(this.base.getField(obj.getClass(), identifier.toString()))) {
          return set;
        }
      } else if (isAllowedMethod(jexl3ExecutorMethod(set))) {
        return set;
      }
      logDenied("property set", obj, identifier);
      return null;
    }

    /**
     * loops (for (x : obj)): the iterated object must pass the sandbox
     */
    @Override
    public Iterator<?> getIterator(Object obj) {
      if (!isAllowedTarget(obj)) {
        logDenied("iterator", obj, "");
        return null;
      }
      return this.base.getIterator(obj);
    }

    /**
     * operator overloads of the (default) JexlArithmetic, unchanged
     */
    @Override
    public JexlArithmetic.Uberspect getArithmetic(JexlArithmetic arithmetic) {
      return this.base.getArithmetic(arithmetic);
    }
  }

  /**
   * sandboxed JEXL3 engine for templates with allowStaticClasses=false
   * @param silent
   * @param lenient
   * @return the engine
   */
  public static org.apache.commons.jexl3.JexlEngine sandboxedJexl3Engine(boolean silent, boolean lenient) {
    int index = engineIndex(silent, lenient);
    org.apache.commons.jexl3.JexlEngine engine = jexl3Engines[index];
    if (engine == null) {
      synchronized (GrouperJexlSandbox.class) {
        engine = jexl3Engines[index];
        if (engine == null) {
          Log jexlLog = LogFactory.getLog(org.apache.commons.jexl3.JexlEngine.class);
          engine = new JexlBuilder().uberspect(new SandboxedJexl3Uberspect(jexlLog)).logger(jexlLog)
              .silent(silent).strict(!lenient).cache(cacheSize()).create();
          jexl3Engines[index] = engine;
        }
      }
    }
    return engine;
  }

}
