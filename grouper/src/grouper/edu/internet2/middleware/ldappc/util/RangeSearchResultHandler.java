package edu.internet2.middleware.ldappc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.handler.CopySearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchCriteria;

/**
 * The <code>RangeSearchResultHandler</code> rewrites attributes returned from Active
 * Directory to include all values.
 */
public class RangeSearchResultHandler extends CopySearchResultHandler {

  /** The string expression matching the attribute ID. */
  private static final String RANGE_PATTERN_STRING = "^(.*?);range=([\\d\\*]+)-([\\d\\*]+)";

  /** The attribute ID pattern. */
  private static final Pattern PATTERN = Pattern.compile(RANGE_PATTERN_STRING);

  /** Ldap object for searching. */
  private Ldap ldap;

  public RangeSearchResultHandler(final Ldap ldap) {
    this.ldap = ldap;
  }

  protected Attributes processAttributes(final SearchCriteria sc, final SearchResult sr)
      throws NamingException {
    Attributes attrs = sr.getAttributes();
    Attributes newAttrs = new BasicAttributes(attrs.isCaseIgnored());
    NamingEnumeration<? extends Attribute> en = attrs.getAll();
    while (en.hasMore()) {
      Attribute attr = en.next();

      // Match attribute ID against the pattern ?
      Matcher matcher = PATTERN.matcher(attr.getID());

      // If the attribute ID does not match the pattern, just copy the attribute
      if (!matcher.find()) {
        newAttrs.put(attr);
      } else {
        // Determine the attribute name without the range syntax
        final String attributeNameWithoutRange = matcher.group(1);
        this.logger.debug("Original attribute ID '" + attr.getID() + "' new '"
            + attributeNameWithoutRange + "'");

        // Create or update the attribute whose ID has the range syntax removed
        Attribute newAttr = attrs.get(attributeNameWithoutRange);
        if (newAttr == null) {
          newAttr = new BasicAttribute(attributeNameWithoutRange, attr.isOrdered());
          newAttrs.put(newAttr);
        }

        // Copy values
        NamingEnumeration<?> e = attr.getAll();
        while (e.hasMore()) {
          newAttr.add(e.next());
        }

        // If the attribute ID ends with * we're done, otherwise increment
        if (attr != null && !attr.getID().endsWith("*")) {
          // Determine next attribute ID
          int start = Integer.parseInt(matcher.group(2));
          int end = Integer.parseInt(matcher.group(3));
          int diff = end - start;
          String nextAttributeID = attributeNameWithoutRange + ";range="
              + Integer.toString(end + 1) + "-" + Integer.toString(end + diff + 1);

          this.logger.debug("Searching for next attribute '" + nextAttributeID + "'");
          Attributes attributes = this.ldap.getAttributes(sr.getName(),
              new String[] { nextAttributeID });
          NamingEnumeration<? extends Attribute> nextEn = attributes.getAll();
          while (nextEn.hasMore()) {
            Attribute nextAttribute = nextEn.next();
            if (nextAttribute != null) {
              this.logger.debug("Searching for next attribute found '"
                  + nextAttribute.getID() + "'");
              newAttrs.put(nextAttribute);
            }
          }

          // Include the next attribute in the search result
          sr.setAttributes(newAttrs);

          // Iterate
          newAttrs = processAttributes(sc, sr);
        }
      }
    }

    return newAttrs;
  }
}
