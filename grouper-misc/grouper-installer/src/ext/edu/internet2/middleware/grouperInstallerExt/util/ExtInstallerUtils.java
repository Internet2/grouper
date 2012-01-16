/**
 * 
 */
package edu.internet2.middleware.grouperInstallerExt.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;



/**
 * utility methods for grouper.
 * @author mchyzer
 *
 */
@SuppressWarnings({ "serial", "unchecked" })
public class ExtInstallerUtils  {

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
    return ExtInstallerUtils.class.getClassLoader();
  }  
}