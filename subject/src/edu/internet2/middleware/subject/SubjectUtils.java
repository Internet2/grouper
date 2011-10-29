/*
 * @author mchyzer
 * $Id: SubjectUtils.java,v 1.5 2009-03-22 02:49:27 mchyzer Exp $
 */
package edu.internet2.middleware.subject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 *
 */
public class SubjectUtils {

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
   * format on screen of config for milestone: yyyy/MM/dd HH:mm:ss.SSS
   */
  public static final String TIMESTAMP_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
  /**
   * start of error parsing messages
   */
  private static final String errorStart = "Invalid timestamp, please use any of the formats: "
      + DATE_FORMAT + ", " + TIMESTAMP_FORMAT + ", " + DATE_MINUTES_SECONDS_FORMAT + ": ";
  /**
   * format on screen of config for milestone: yyyyMMdd HH:mm:ss.SSS
   */
  public static final String TIMESTAMP_NO_SLASH_FORMAT = "yyyyMMdd HH:mm:ss.SSS";
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
   * special number when a number is not found 
   */
  public static final int NOT_FOUND = -999999999;
  /**
   * The name says it all.
   */
  public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  /** logger */
  private static Log log = LogFactory.getLog(SubjectUtils.class);
  /**
   * cache the properties read from resource 
   */
  private static Map<String, Properties> resourcePropertiesCache = new HashMap<String, Properties>();

  /**
   * If we can, inject this into the exception, else return false
   * @param t
   * @param message
   * @return true if success, false if not
   */
  public static boolean injectInException(Throwable t, String message) {
    
    String throwableFieldName = null;//GrouperConfig.getProperty("throwable.data.field.name");
    
    //if (StringUtils.isBlank(throwableFieldName)) {
      //this is the field for sun java 1.5
      throwableFieldName = "detailMessage";
    //}
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
      log.error(message);
      return false;
    }
    
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
      Class<?> fieldType = field.getType();
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
  @SuppressWarnings({ "unchecked", "cast" })
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

  /**
   * convert a date to the standard string yyyymmdd
   * @param date 
   * @return the string value
   */
  public static String stringValue(java.util.Date date) {
    synchronized (SubjectUtils.class) {
      if (date == null) {
        return null;
      }
  
      String theString = dateFormat().format(date);
  
      return theString;
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
      log.error(e);
    }
  
    return NOT_FOUND;
  }

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
   * get the timestamp format for this thread
   * if you call this make sure to synchronize on FastDateUtils.class
   * @return the timestamp format
   */
  synchronized static SimpleDateFormat dateFormat() {
    return dateFormat;
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
   * print out various types of objects
   * 
   * @param object
   * @param maxChars is where it should stop when figuring out object.  note, result might be longer than max...
   * need to abbreviate when back
   * @param result is where to append to
   */
  @SuppressWarnings("unchecked")
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
   * return the string or the other if the first is blank
   * @param string
   * @param defaultStringIfBlank
   * @return the string or the default one
   */
  public static String defaultIfBlank(String string, String defaultStringIfBlank) {
    return StringUtils.isBlank(string) ? defaultStringIfBlank : string;
  }

  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @return the properties
   */
  public synchronized static Properties propertiesFromResourceName(String resourceName) {
    Properties properties = resourcePropertiesCache.get(resourceName);
    if (properties == null) {
  
      properties = new Properties();
  
      URL url = computeUrl(resourceName, true);
      InputStream inputStream = null;
      try {
        inputStream = url.openStream();
        properties.load(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Problem with resource: '" + resourceName + "'", e);
      } finally {
        closeQuietly(inputStream);
      }
      resourcePropertiesCache.put(resourceName, properties);
    }
    return properties;
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
    return SubjectUtils.class.getClassLoader();
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
   * substitute an EL for objects
   * @param stringToParse
   * @param variableMap
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String substituteExpressionLanguage(String stringToParse, Map<String, Object> variableMap) {
    if (isBlank(stringToParse)) {
      return stringToParse;
    }
    try {
      JexlContext jc = new MapContext();

      int index = 0;
      
      for (String key: variableMap.keySet()) {
        jc.set(key, variableMap.get(key));
      }
      
      //allow utility methods
      jc.set("subjectUtils", new SubjectUtils());
      
      // matching ${ exp }   (non-greedy)
      Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
      Matcher matcher = pattern.matcher(stringToParse);
      
      StringBuilder result = new StringBuilder();
  
      JexlEngine jexlEngine = new JexlEngine();
      jexlEngine.setSilent(false);

      
      //loop through and find each script
      while(matcher.find()) {
        result.append(stringToParse.substring(index, matcher.start()));
        
        //here is the script inside the curlies
        String script = matcher.group(1);
        
        Expression e = jexlEngine.createExpression(script);
  
        //this is the result of the evaluation
        Object o = e.evaluate(jc);
  
        if (o == null) {
          log.warn("expression returned null: " + script + ", in pattern: '" + stringToParse + "', available variables are: "
              + toStringForLog(variableMap.keySet()));
        }
        
        result.append(o);
        
        index = matcher.end();
      }
      
      result.append(stringToParse.substring(index, stringToParse.length()));
      return result.toString();
      
    } catch (Exception e) {
      throw new RuntimeException("Error substituting string: '" + stringToParse + "'", e);
    }
  }

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


  
}
