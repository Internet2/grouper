package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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

import java.util.*;

import com.unboundid.ldap.sdk.RDN;
import edu.internet2.middleware.grouper.util.GrouperUtilElSafe;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is included in the variable namespace created when JEXL 
 * expressions are evaluated. This class's purpose it to help simplify
 * some jexl expressions. 
 * 
 *  -- containedWithin(item, collections...) will search multiple
 *  java arrays or collections for 'item.' This class will transparently
 *  handle null collections.
 *  
 * @author bert
 *
 */
public class PspJexlUtils extends GrouperUtilElSafe {
  private static final Logger LOG = LoggerFactory.getLogger(LdapObject.class);

  /**
   * This is a null-safe and flexible method for seeing if an item is a member
   * of one or more arrays or collections.
   * 
   * @param item
   * @param arraysOrCollections varargs of Java arrays or collections
   * 
   * @return True iff item is a member of (or equal to) one of the arrays or collections.
   */
  public static boolean containedWithin(Object item, Object... arraysOrCollections) {
    for (Object arrayOrCollection : arraysOrCollections ) {
      if ( arrayOrCollection == null )
        continue;
      
      if ( arrayOrCollection instanceof Collection ) {
        if ( ((Collection) arrayOrCollection).contains(item) )
          return true;
      } else if ( arrayOrCollection instanceof Object[] ) {
        if ( Arrays.asList((Object[]) arrayOrCollection).contains(item) )
          return true;
      } else {
        if ( arrayOrCollection.equals(item) )
          return true;
      }
    }
    
    return false;
  }

  // This bushyDn function is typically called from Jexl templates and does the most common
  // escaping (rdn escaping), but does not escape filter characters
  public static String bushyDn(String groupName, String rdnAttributeName, String ouAttributeName) {
    return bushyDn(groupName, rdnAttributeName, ouAttributeName, true, false);
  }

  public static String bushyDn(String groupName, String rdnAttributeName, String ouAttributeName,
                               boolean performRdnEscaping, boolean performFilterEscaping) {
    StringBuilder result = new StringBuilder();
    
    List<String> namePieces=Arrays.asList(groupName.split(":"));
    Collections.reverse(namePieces);

    /// Work through the pieces backwards. The first is rdn=X and the others are ou=X
    for (int i=0; i<namePieces.size(); i++) {
      if ( result.length() != 0 )
        result.append(',');

      RDN rdn;
      String piece = namePieces.get(i);

      // Look for filter-relevant characters if this will be used in a filter
      if ( performFilterEscaping ) {
        piece = escapeLdapFilter(piece);
      }

      if (i==0)
        rdn = new RDN(rdnAttributeName, piece);
      else
        rdn = new RDN(ouAttributeName, piece);

      if ( performRdnEscaping ) {
        result.append(rdn.toMinimallyEncodedString());
      } else {
        result.append(rdn.toString());
      }
    }

    // document if any ldap escaping (filter or dn) has occurred
    if ( performRdnEscaping ) {
      LdapProvisioner.stringHasBeenDnEscaped(result.toString());
    }

    if ( performFilterEscaping ) {
      LdapProvisioner.stringHasBeenLdapFilterEscaped(result.toString());
    }

    return result.toString();
  }

  /**
   * This takes a string of attribute=value and makes sure that special, dn-relevant characters
   * are escaped, particularly commas, pluses, etc
   * @param rdnString An RDN: attribute=value
   * @return
   */
  public static String escapeLdapRdn(String rdnString) throws PspException  {
    String rdnAttribute = StringUtils.substringBefore(rdnString, "=");
    String rdnValue     = StringUtils.substringAfter(rdnString, "=");

    if ( StringUtils.isEmpty(rdnValue) || StringUtils.isEmpty(rdnValue) ) {
      LOG.error("RDN was not of the format attribute=value: {}", rdnString);
      throw new PspException("Unable to parse and escape rdn");
    }

    // This is wrapping the Value in quotes so the RDN class will consider
    // all the dn-relevant characters (eg: ,+;) as escaped
    RDN rdn = new RDN(rdnAttribute, rdnValue);
    return rdn.toMinimallyEncodedString();
  }

  /**
   * This takes a simple ldap filter 'attribute=value' or just 'value' and escapes the
   * filter-relevant characters: \, *, (, )
   *
   * Ref: https://stackoverflow.com/questions/31309673/parse-ldap-filter-to-escape-special-characters
   * @param filterString
   * @return
   */
  public static String escapeLdapFilter(String filterString) {
      if(filterString == null) return "";
      String result = filterString.replace("\\", "\\5C")
              .replace("*", "\\2A")
              .replace("(", "\\28")
              .replace(")", "\\29")
              .replace("\000", "\\00");

      LdapProvisioner.stringHasBeenLdapFilterEscaped(result);
      return result;
  }
}
