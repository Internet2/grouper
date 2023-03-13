/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.remedyV2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 *
 */
public class GrouperRemedyUtils {

  /**
   * 
   */
  public GrouperRemedyUtils() {
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

}
