/*
 * @author mchyzer
 * $Id: GuiUtils.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * utils for the gui
 */
public class GuiUtils {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(GuiUtils.class);

  /**
   * get a cookie value by name, null if not there
   * @param prefix
   */
  public static void removeCookiesByPrefix(String prefix) {
    HttpServletResponse httpServletResponse = GrouperUiJ2ee.retrieveHttpServletResponse();
    
    List<Cookie> cookies = findCookiesByPrefix(prefix);
    for (Cookie cookie : cookies) {
      cookie.setMaxAge(0);
      //note: this is needed for websec cookies... is it for all cookies?
      cookie.setPath("/");
      httpServletResponse.addCookie(cookie);
      
      if (httpServletResponse.isCommitted()) {
        LOG.error("Trying to kill cookie: " + cookie.getName() + ", but the response is committed!", new RuntimeException("stack"));
      }
      
    }
  }
  
  /**
   * find a cookie or empty list if cant find
   * @param name
   * @return the cookies or empty list if not found
   */
  public static List<Cookie> findCookiesByPrefix(String name) {
    
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    StringBuilder allCookies = null;
    boolean isDebug = LOG.isDebugEnabled();
    if (isDebug) {
      allCookies = new StringBuilder("Looking for cookie with prefix: '" + name + "'");
    }

    List<Cookie> cookieList = new ArrayList<Cookie>();
    Cookie[] cookies = httpServletRequest.getCookies();
    //go through all cookies and find the cookie by name
    int cookiesLength = GrouperUtil.length(cookies);
    for (int i=0;i<cookiesLength;i++) {
      if (StringUtils.indexOf(cookies[i].getName(), name) == 0) {
        cookieList.add(cookies[i]);
        if (isDebug) {
          allCookies.append(", Found cookie: " + cookies[i].getName());
        }
      } else {
        if (isDebug) {
          allCookies.append(", Didnt find cookie: " + cookies[i].getName());
        }
      }
      
    }
    if (isDebug) {
      LOG.debug(allCookies.toString());
    }
    return cookieList;
  }

  
  /**
   * get the text properties, might be cached
   * @return the properties
   */
  public static Properties propertiesUiTextGui() {
    Properties propertiesSettings = GrouperUtil.propertiesFromResourceName(
      "grouperUiSettings.properties");
    boolean cache = GrouperUtil.propertiesValueBoolean(propertiesSettings, 
      "grouperUi.cache.uiText", true);
  
    Properties properties = GrouperUtil.propertiesFromResourceName(
        "grouperUiText.properties", cache , true);
    return properties;
  }

  /** class file dir cached */
  private static File classFileDir = null;
  
  /**
   * get the class file dir
   * @return the class file dir
   */
  public static File classFileDir() {
    if (classFileDir == null) {
      classFileDir = GrouperUtil.fileFromResourceName("grouperUiText.properties").getParentFile();
    }
    return classFileDir;
  }
  
  /** 
   * list files with a certain extension.  Note, there cannot be more than 10000
   * files or exception will be throws
   * @param dir
   * @param extension if this is the empty string it should list all
   * @return the array of files
   */
  public static List<File> listFilesByExtensionRecursive(File dir, String extension) {
    List<File> theList = new ArrayList<File>();
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
      List<File> theList) {
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
      if (StringUtils.contains(allFiles[i].getName(), "..")) {
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
        if (StringUtils.contains(pathname.getName(), "..")) {
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
          if (StringUtils.contains(finalExtension, "..")) {
            return true;
          }
          //if the file is .., then its not what we are looking for
          if (StringUtils.contains(name, "..")) {
            return false;
          }
          return true;
        }
  
        return false;
      }
    };
  
    return dir.listFiles(fileFilter);
  }


}
