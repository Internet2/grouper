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
package edu.internet2.middleware.ldappc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.SearchResult;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.handler.CopySearchResultHandler;
import edu.vt.middleware.ldap.handler.ExtendedSearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchCriteria;

/**
 * The <code>RangeSearchResultHandler</code> rewrites attributes returned from Active
 * Directory to include all values by performing additional searches. This behavior is
 * based on the expired RFC "Incremental Retrieval of Multi-valued Properties"
 * http://www.ietf.org/proceedings/53/I-D/draft-kashi-incremental-00.txt.
 * 
 * For example, when the membership of a group exceeds 1500, requests for the member
 * attribute will likely return an attribute with name "member;Range=0-1499" and 1500
 * values. For a group with just over 3000 members, subsequent searches will request
 * "member;Range=1500-2999" and then "member;Range=3000-4499". When the returned attribute
 * is of the form "member;Range=3000-*", all values have been retrieved.
 */
public class RangeSearchResultHandler extends CopySearchResultHandler implements ExtendedSearchResultHandler {

  /** The character indicating that the end of the range has been reached. */
  public static final String END_OF_RANGE = "*";

  /** The format used to calculate attribute IDs for subsequent searches. */
  public static final String RANGE_FORMAT = "%1$s;Range=%2$s-%3$s";

  /** The expression matching the range attribute ID "<id>;range=<X>-<Y>". */
  public static final String RANGE_PATTERN_STRING = "^(.*?);Range=([\\d\\*]+)-([\\d\\*]+)";

  /** The pattern matching the range attribute ID. */
  public static final Pattern RANGE_PATTERN = Pattern.compile(RANGE_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

  /** Ldap object for searching. */
  private Ldap ldap;

  /**
   * Creates a new <code>RangeSearchResultHandler</code>.
   */
  public RangeSearchResultHandler() {
  }

  /**
   * Creates a new <code>RangeSearchResultHandler</code> with the supplied ldap.
   * 
   * @param ldap
   *          <code>Ldap</code>
   */
  public RangeSearchResultHandler(final Ldap ldap) {
    this.ldap = ldap;
  }

  /** {@inheritDoc} */
  public Ldap getSearchResultLdap() {
    return this.ldap;
  }

  /** {@inheritDoc} */
  public void setSearchResultLdap(Ldap l) {
    this.ldap = l;
  }

  /** {@inheritDoc} */
  protected Attributes processAttributes(final SearchCriteria sc, final SearchResult sr)
      throws NamingException {

    // get all attributes in the search result
    Attributes attrs = sr.getAttributes();

    // for every attribute in the search result
    NamingEnumeration<? extends Attribute> attrsEnumeration = attrs.getAll();
    while (attrsEnumeration.hasMore()) {
      Attribute attr = attrsEnumeration.next();

      // skip nulls
      if (attr == null) {
        continue;
      }

      // Match attribute ID against the pattern
      Matcher matcher = RANGE_PATTERN.matcher(attr.getID());

      // If the attribute ID matches the pattern
      if (matcher.find()) {

        String msg = "attribute '" + attr.getID() + "' result '" + sr.getName() + "'";

        // Determine the attribute name without the range syntax
        String attrTypeName = matcher.group(1);
        this.logger.debug("Found Range option " + msg);
        if (attrTypeName == null || attrTypeName.isEmpty()) {
          this.logger.error("Unable to determine the attribute type name for " + msg);
          throw new RuntimeException("Unable to determine the attribute type name for " + msg);
        }

        // Create or update the attribute whose ID has the range syntax removed
        Attribute newAttr = attrs.get(attrTypeName);
        if (newAttr == null) {
          newAttr = new BasicAttribute(attrTypeName, attr.isOrdered());
          attrs.put(newAttr);
        }

        // Copy values
        NamingEnumeration<?> attrValues = attr.getAll();
        while (attrValues.hasMore()) {
          newAttr.add(attrValues.next());
        }

        // Remove original attribute with range syntax from returned attributes
        sr.getAttributes().remove(attr.getID());

        // If the attribute ID ends with * we're done, otherwise increment
        if (!attr.getID().endsWith(END_OF_RANGE)) {

          // Determine next attribute ID
          String initialRange = matcher.group(2);
          if (initialRange == null || initialRange.isEmpty()) {
            this.logger.error("Unable to determine initial range for " + msg);
            throw new RuntimeException("Unable to determine initial range for " + msg);
          }
          String terminalRange = matcher.group(3);
          if (terminalRange == null || terminalRange.isEmpty()) {
            this.logger.error("Unable to determine terminal range for " + msg);
            throw new RuntimeException("Unable to determine terminal range for " + msg);
          }
          int start = 0;
          int end = 0;
          try {
            start = Integer.parseInt(initialRange);
            end = Integer.parseInt(terminalRange);
          } catch (NumberFormatException e) {
            this.logger.error("Unable to parse range for " + msg);
            throw new RuntimeException("Unable to parse range for " + msg);
          }
          int diff = end - start;
          String nextAttrID = String.format(RANGE_FORMAT, attrTypeName, end + 1, end + diff + 1);

          // Search for next increment of values
          this.logger.debug("Searching for '" + nextAttrID + "' to increment " + msg);
          Attributes nextAttrs = this.ldap.getAttributes(sr.getName(), new String[] { nextAttrID });

          // Add all attributes to the search result
          NamingEnumeration<? extends Attribute> nextAttrsEnum = nextAttrs.getAll();
          while (nextAttrsEnum.hasMore()) {
            Attribute nextAttr = nextAttrsEnum.next();
            if (nextAttr == null) {
              this.logger.error("Null attribute returned for '" + nextAttrID + "' when incrementing " + msg);
              throw new RuntimeException("Null attribute returned for '" + nextAttrID + "' when incrementing " + msg);
            }
            sr.getAttributes().put(nextAttr);
          }

          // Iterate
          attrs = processAttributes(sc, sr);
        }
      }
    }

    return attrs;
  }
}
