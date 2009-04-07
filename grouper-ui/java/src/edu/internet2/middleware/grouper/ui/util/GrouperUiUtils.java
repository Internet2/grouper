/**
 * 
 */
package edu.internet2.middleware.grouper.ui.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;




/**
 * utility methods for grouper
 * 
 * @author mchyzer
 * 
 */
public class GrouperUiUtils {

  /**
   * compute a url of a resource
   * @param resourceName
   * @param canBeNull if cant be null, throw runtime
   * @return the URL
   */
  public static URL computeUrl(String resourceName, boolean canBeNull) {
    //get the url of the navigation file
    //TODO move this to grouperutil
    ClassLoader cl = classLoader();

    URL url = null;

    try {
      url = cl.getResource(resourceName);
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
    return GrouperUiUtils.class.getClassLoader();
  }
  
  /**
   * Field lastId.
   */
  private static char[] lastId = convertLongToStringSmall(new Date().getTime())
      .toCharArray();

  /** cache the properties read from resource */
  private static Map<String, Properties> resourcePropertiesCache = new HashMap<String, Properties>();
  
  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @return the properties
   */
  public synchronized static Properties propertiesFromResourceName(String resourceName) {
    Properties properties = resourcePropertiesCache.get(resourceName);
    if (properties == null) {

      properties = new Properties();
      //TODO move this to grouperutil
      URL url = computeUrl(resourceName, true);
      InputStream inputStream = null;
      try {
        inputStream = url.openStream();
        properties.load(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Problem with resource: '" + resourceName + "'");
      } finally {
        GrouperUtil.closeQuietly(inputStream);
      }

    }
    return properties;
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
    synchronized (GrouperUiUtils.class) {
      lastId = incrementStringInt(lastId);
    }

    return String.valueOf(lastId);
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
   * get a cookie based on name or null if not there
   * @param cookieName
   * @param cookies (from httprequest)
   * @return the cookie
   */
  public static Cookie retrieveCookie(String cookieName, Cookie[] cookies) {

    cookies = cookies == null ? new Cookie[0] : cookies;
    for (Cookie cookie : cookies) {
      if (StringUtils.equals(cookie.getName(), cookieName)) {
        return cookie;
      }
    }
    return null;
  }
  
  /**
   * get a cookie value (null if not there)
   * @param cookieName
   * @param cookies (from httprequest)
   * @return the cookie value
   */
  public static String cookieValue(String cookieName, Cookie[] cookies) {
    Cookie cookie = retrieveCookie(cookieName, cookies);
    return cookie == null ? null : cookie.getName();
  }
  
  /**
   * kill a cookie if it is there
   * @param cookieName
   * @param cookies (from httprequest)
   * @param httpServletResponse is response for adding cookies
   */
  public static void killCookie(String cookieName, Cookie[] cookies, HttpServletResponse httpServletResponse) {
    Cookie cookie = retrieveCookie(cookieName, cookies);
    if (cookie != null) {
      cookie.setMaxAge(0);
      cookie.setValue(null);
      httpServletResponse.addCookie(cookie);
    }
  }
  
}
