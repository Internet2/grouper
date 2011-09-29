/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class LoaderLdapElUtils {

  /**
   * convert from uid=someapp,ou=people,dc=myschool,dc=edu
   * to someapp
   * @param dn
   * @return the most specific value
   */
  public static String convertDnToSpecificValue(String dn) {
    String result = dn;
    if (!StringUtils.isBlank(dn)) {
      String[] dnElements = GrouperUtil.splitTrim(dn, ",");
      if (GrouperUtil.length(dnElements) > 0) {
        String firstElement = dnElements[0];
        int equalsIndex = firstElement.indexOf('=');
        if (equalsIndex >= 0) {
          result = firstElement.substring(equalsIndex+1, firstElement.length());
        }
      }
    }
    return result;
  }
  
}
