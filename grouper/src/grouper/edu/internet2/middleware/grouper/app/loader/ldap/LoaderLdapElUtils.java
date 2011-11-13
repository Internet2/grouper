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
  
  /**
   * convert from uid=someapp,ou=people,dc=myschool,dc=edu
   * baseDn is edu
   * searchDn is myschool
   * to people:someapp
   * @param dn
   * @param baseDn if there is one, take it off
   * @param searchDn if there is one after the baseDn is off, take it off
   * @return the subpath
   */
  public static String convertDnToSubPath(String dn, String baseDn, String searchDn) {
    
    if (!StringUtils.isBlank(baseDn)) {
      if (dn.endsWith(baseDn)) {
        dn = dn.substring(0, dn.length() - (baseDn.length()+1));
      }
    }
    if (!StringUtils.isBlank(searchDn)) {
      if (dn.endsWith(searchDn)) {
        dn = dn.substring(0, dn.length() - (searchDn.length()+1));
      }
    }
    StringBuilder path = new StringBuilder();
    if (!StringUtils.isBlank(dn)) {
      String[] dnElements = GrouperUtil.splitTrim(dn, ",");
      
      for (int i=dnElements.length-1; i>=0; i--) {
        
        String element = dnElements[i];
        int equalsIndex = element.indexOf('=');
        if (equalsIndex >= 0) {
          element = element.substring(equalsIndex+1, element.length());
        }
        path.append(element);
        if (i != 0) {
          path.append(":");
        }
      }
    }
    return path.toString();
  }
  
}
