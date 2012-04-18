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
/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.apache.directory.shared.ldap.entry.client.ClientModification;
import org.apache.directory.shared.ldap.entry.client.DefaultClientAttribute;
import org.apache.directory.shared.ldap.ldif.ChangeType;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifUtils;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.LdappcOptions.ProvisioningMode;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;

/**
 * This provides utility methods for interacting with a LDAP directory.
 */
public final class LdapUtil {

  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LdapUtil.class);

  /**
   * Object class attribute name.
   */
  public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

  /**
   * Empty name string for name parser.
   */
  public static final String EMPTY_NAME = "";

  /**
   * Delimiter for multivalued RDNs.
   */
  public static final String MULTIVALUED_RDN_DELIMITER = "+";

  /**
   * Space character (i.e., ' ').
   */
  static final char SPACE = ' ';

  /**
   * LB Sign (i.e., '#').
   */
  static final char LB_SIGN = '#';

  /**
   * Comma (i.e., ',').
   */
  static final char COMMA = ',';

  /**
   * Plus Sign (i.e, '+').
   */
  static final char PLUS_SIGN = '+';

  /**
   * Double qoute (i.e., '"').
   */
  static final char DOUBLE_QUOTE = '"';

  /**
   * Backward slash (i.e., '\').
   */
  static final char BACKWARD_SLASH = '\\';

/**
       * Less than (i.e., '<').
       */
  static final char LESS_THAN = '<';

  /**
   * Greater than (i.e., '>').
   */
  static final char GREATER_THAN = '>';

  /**
   * Semi-colon (i.e., ';').
   */
  static final char SEMI_COLON = ';';

  /**
   * Asterix (i.e., '*').
   */
  static final char ASTERIX = '*';

  /**
   * Left Parenthesis (i.e., '(').
   */
  static final char LEFT_PARENTHESIS = '(';

  /**
   * Right Parenthesis (i.e., ')').
   */
  static final char RIGHT_PARENTHESIS = ')';

  /**
   * Nul charcter.
   */
  static final char NUL = '\u0000';

  /**
   * Pattern matching the JNDI special forward slash character.
   */
  private static Pattern forwardSlashPattern = Pattern.compile("([^\\\\])/");

  /**
   * Pattern matching an escaped JNDI special forward slash character.
   */
  private static Pattern escapedforwardSlashPattern = Pattern.compile("\\\\/");

  /**
   * Prevent instantiation.
   */
  private LdapUtil() {
  }

  /**
   * This deletes the object identified by the given dn and any child objects.
   * 
   * @param ldappc
   * @param dn
   *          the DN to delete
   * @throws NamingException
   */
  public static void delete(Ldappc ldappc, Name dn) throws NamingException {
    //
    // Remove the subcontexts
    //
    List<String> childDNs = LdapUtil.getChildDNs(dn.toString(), ldappc.getContext());
    for (String childDN : childDNs) {

      if (ldappc.getOptions().getMode().equals(ProvisioningMode.DRYRUN)) {
        LdapUtil.writeLdif(ldappc.getWriter(), getLdifDelete(new LdapDN(childDN)));
      }

      if (ldappc.getOptions().getMode().equals(ProvisioningMode.PROVISION)) {
        String msg = "delete '" + dn + "'";
        if (ldappc.getOptions().getLogLdif()) {
          msg += "\n\n" + getLdifDelete(new LdapDN(childDN));
        }
        LOG.debug(msg);
        ldappc.getContext().delete(LdapUtil.escapeForwardSlash(childDN));
      }
    }

    //
    // Remove the object
    //
    if (ldappc.getOptions().getMode().equals(ProvisioningMode.DRYRUN)) {
      LdapUtil.writeLdif(ldappc.getWriter(), getLdifDelete(new LdapDN(dn)));
    }

    if (ldappc.getOptions().getMode().equals(ProvisioningMode.PROVISION)) {
      LOG.debug("delete '{}'", dn);
      ldappc.getContext().delete(LdapUtil.escapeForwardSlash(dn.toString()));
    }
  }

  /**
   * This method converts '*','(',')' and the "null" character (i.e., 0x00) to be a
   * forward slash (i.e., \) and the two hex character value as defined in RFC2254. For
   * example, the string "abc * efg" is converted to "abc \2a efg".
   * 
   * @param value
   *          String to make safe
   * @return Ldap filter value safe string
   */
  public static String makeLdapFilterValueSafe(String value) {
    StringBuffer safeBuf = new StringBuffer();
    if (value != null) {
      //
      // Get value as a char[]
      //
      char[] valueChars = value.toCharArray();

      for (int i = 0; i < valueChars.length; i++) {
        //
        // IMPORTANT
        // IMPORTANT: This switch takes advantage of "falling through"
        // IMPORTANT: behavior of switch.
        // IMPORTANT
        //
        switch (valueChars[i]) {
          case ASTERIX:
          case LEFT_PARENTHESIS:
          case RIGHT_PARENTHESIS:
          case BACKWARD_SLASH:
          case NUL:
            safeBuf.append(BACKWARD_SLASH);
            if (valueChars[i] == 0) {
              //
              // This makes sure '\u0000' is encoded as '\00' rather
              // than '\0'
              //
              safeBuf.append("0");
            }
            safeBuf.append(Integer.toHexString(valueChars[i]));
            break;
          default:
            safeBuf.append(valueChars[i]);
            break;
        }
      }
    }

    return value == null ? value : safeBuf.toString();
  }

  /**
   * This method escapes all LDAP special characters in the <code>value</code>. This way
   * it is safe to use as part of an LDAP DN.
   * 
   * @param value
   *          String to make safe
   * @return LDAP name safe string
   */
  public static String makeLdapNameSafe(String value) {
    //
    // Must escape the following characters with a "\"
    // 1. A space or "#" character occurring at the beginning of the string
    // 2. A space character occurring at the end of the string
    // 3. One of the characters ",", "+", """, "\", "<", ">" or ";"
    //

    StringBuffer safeBuf = new StringBuffer();
    if (value != null) {

      //
      // Get value as a char[]
      //
      char[] valueChars = value.toCharArray();

      //
      // Determine index of beginning of trailing spaces
      //
      int trailingSpacesStart = valueChars.length;
      while (trailingSpacesStart > 0 && valueChars[trailingSpacesStart - 1] == SPACE) {
        trailingSpacesStart--;
      }

      //
      // Escape leading spaces and #
      //
      int index = 0;
      for (index = 0; index < trailingSpacesStart; index++) {
        if (valueChars[index] == SPACE || valueChars[index] == LB_SIGN) {
          //
          // Escape the space or # character
          //
          safeBuf.append(BACKWARD_SLASH);
          safeBuf.append(valueChars[index]);
        } else {
          //
          // Hit non space or # character so break out of this loop
          //
          break;
        }
      }

      //
      // Escape ",", "+", """, "\", "<", ">" or ";"
      //
      for (; index < trailingSpacesStart; index++) {
        //
        // IMPORTANT
        // IMPORTANT: This switch takes advantage of "falling through"
        // IMPORTANT: behavior of switch.
        // IMPORTANT
        //
        switch (valueChars[index]) {
          case COMMA:
          case PLUS_SIGN:
          case DOUBLE_QUOTE:
          case BACKWARD_SLASH:
          case LESS_THAN:
          case GREATER_THAN:
          case SEMI_COLON:
            safeBuf.append(BACKWARD_SLASH);
          default:
            safeBuf.append(valueChars[index]);
            break;
        }
      }

      //
      // Escape trailing spaces
      //
      for (; index < valueChars.length; index++) {
        safeBuf.append(BACKWARD_SLASH);
        safeBuf.append(valueChars[index]);
      }
    }

    return value == null ? value : safeBuf.toString();
  }

  /**
   * Converts a "{i}" ldap query filter parameter to "*" where i >= 0.
   * 
   * @param filter
   *          Ldap query filter
   * @param parameterIndex
   *          Index of the parameter to alter
   * @return A new ldap query filter with "{i}" replaced with "*", or an empty string if
   *         the filter is null.
   */
  public static String convertParameterToAsterisk(String filter, int parameterIndex) {
    String newFilter = "";
    if (filter != null) {
      String regExpr = "\\{\\s*?" + parameterIndex + "\\s*?\\}";
      String asterisk = "*";
      newFilter = filter.replaceAll(regExpr, asterisk);
    }
    return newFilter;
  }

  /**
   * Return the Name of a SearchResult via getName().
   * 
   * @param parser
   *          the Context's NameParser
   * @param searchResult
   *          the SearchResult
   * @return the Name
   * @throws NamingException
   */
  public static Name getName(NameParser parser, SearchResult searchResult)
      throws NamingException {
    String entryName = searchResult.getName();

    // there should be a better way to handle
    // names that begin and end with quotes as returned by JNDI
    if (entryName.startsWith("\"") && entryName.endsWith("\"")) {
      entryName = entryName.replaceFirst("^\"", "");
      entryName = entryName.replaceFirst("\"$", "");
    }

    //
    // Build the entry's DN
    //
    return parser.parse(entryName);
  }

  /**
   * Create an LDIF representation.
   * 
   * @param attribute
   * @return the LDIF representation
   * @throws NamingException
   */
  public static String getLdif(Attribute attribute) throws NamingException {

    StringBuffer ldif = new StringBuffer();

    NamingEnumeration<?> values = attribute.getAll();
    while (values.hasMore()) {
      String value = values.next().toString();
      ldif.append(attribute.getID() + ": " + value + "\n");
    }

    return ldif.toString();
  }

  /**
   * Create an LDIF representation.
   * 
   * @param attributes
   * @return the LDIF representation
   * @throws NamingException
   */
  public static String getLdif(Attributes attributes) throws NamingException {

    StringBuffer ldif = new StringBuffer();

    NamingEnumeration<?> ae = attributes.getAll();
    while (ae.hasMore()) {
      Attribute attribute = (Attribute) ae.next();
      ldif.append(LdapUtil.getLdif(attribute));
    }

    return ldif.toString();
  }

  public static String getLdifModify(LdapDN dn, ModificationItem[] modificationItems)
      throws NamingException {

    LdifEntry ldifEntry = new LdifEntry();
    ldifEntry.setChangeType(ChangeType.Modify);
    ldifEntry.setDn(dn);

    for (ModificationItem modificationItem : modificationItems) {
      ClientModification clientModification = new ClientModification();
      clientModification.setOperation(modificationItem.getModificationOp());

      DefaultClientAttribute clientAttribute = new DefaultClientAttribute();
      clientAttribute.setId(modificationItem.getAttribute().getID());

      NamingEnumeration<?> values = modificationItem.getAttribute().getAll();
      while (values.hasMore()) {
        // only strings, not bytes
        clientAttribute.add(values.next().toString());
      }
      clientModification.setAttribute(clientAttribute);

      ldifEntry.addModificationItem(clientModification);
    }

    return LdifUtils.convertToLdif(ldifEntry);
  }

  public static String getLdifAdd(LdapDN dn, Attributes attributes)
      throws NamingException {

    LdifEntry ldifEntry = new LdifEntry();
    ldifEntry.setChangeType(ChangeType.Add);
    ldifEntry.setDn(dn);

    NamingEnumeration<?> a = attributes.getAll();
    while (a.hasMore()) {
      Attribute ax = (Attribute) a.next();

      DefaultClientAttribute clientAttribute = new DefaultClientAttribute();
      clientAttribute.setId(ax.getID());

      NamingEnumeration<?> values = ax.getAll();
      while (values.hasMore()) {
        // only strings, not bytes
        clientAttribute.add(values.next().toString());
      }

      ldifEntry.addAttribute(clientAttribute);
    }

    return LdifUtils.convertToLdif(ldifEntry);
  }

  public static String getLdifDelete(LdapDN dn) throws NamingException {
    LdifEntry ldifEntry = new LdifEntry();
    ldifEntry.setChangeType(ChangeType.Delete);
    ldifEntry.setDn(dn);

    return LdifUtils.convertToLdif(ldifEntry);
  }

  public static void writeLdif(BufferedWriter writer, String ldif) {
    try {
      writer.write(ldif);
    } catch (IOException e) {
      throw new LdappcException(e);
    }
  }

  /**
   * Open the file for writing.
   * 
   * @param file
   *          File to write to.
   * @return BufferedWriter for the file.
   * @throws LdappcException
   *           thrown if the file cannot be opened.
   */
  public static BufferedWriter openWriter(File file) throws LdappcException {
    try {
      return new BufferedWriter(new FileWriter(file));
    } catch (IOException e) {
      throw new LdappcException("Unable to open file: " + file, e);
    }
  }

  /**
   * Open file for reading.
   * 
   * @param file
   *          File to read from.
   * 
   * @return BufferedReader for the file.
   * 
   * @throws LdappcException
   *           thrown if the file cannot be opened.
   */
  public static BufferedReader openReader(File file) throws LdappcException {
    try {
      return new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      throw new LdappcException("Unable to open membership file", e);
    }
  }

  /**
   * Return a list of child DNs under the given DN, in (reverse) order suitable for
   * deletion. This method requires the use of the FqdnSearchResultHandler.
   * 
   * @param dn
   *          the dn to delete, as well as all children
   * @param ldap
   *          the ldap connection
   * @return
   * @throws NamingException
   */
  public static List<String> getChildDNs(String dn, Ldap ldap) throws NamingException {
    return getChildDNs(dn, ldap, true);
  }
  
  /**
   * Return a list of child DNs under the given DN either in ascending or descending order (suitable for deletion). This
   * method requires the use of the FqdnSearchResultHandler.
   * 
   * @param baseDn the base DN to include as well as child DNs
   * @param ldap the ldap connection
   * @param decendingOrder true to indicate descending order, false to indicate ascending order
   * @return
   * @throws NamingException
   */
  public static List<String> getChildDNs(String baseDn, Ldap ldap, boolean decendingOrder) throws NamingException {

    ArrayList<LdapName> ldapNames = new ArrayList<LdapName>();

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setReturningAttributes(new String[] {});

    Iterator<SearchResult> results = ldap.search(LdapUtil.escapeForwardSlash(baseDn),
        new SearchFilter("objectclass=*"), searchControls);

    while (results.hasNext()) {
      ldapNames.add(new LdapName(results.next().getName()));
    }

    ldapNames.remove(new LdapName(baseDn));

    Collections.sort(ldapNames);

    if (!decendingOrder) {
      Collections.reverse(ldapNames);
    }

    ArrayList<String> dns = new ArrayList<String>();
    for (LdapName ldapName : ldapNames) {
      dns.add(ldapName.toString());
    }

    return dns;
  }

  /**
   * Return a list of child DNs under the given DN, in (reverse) order suitable for
   * deletion.
   * 
   * @param dn
   *          the dn to delete, as well as all children
   * @param ldap
   *          the ldap connection
   * @return
   * @throws NamingException
   */
  public static List<String> getChildDNs(String dn, LdapContext ldap)
      throws NamingException {

    ArrayList<String> tree = new ArrayList<String>();

    SearchControls ctrls = new SearchControls();
    ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    ctrls.setReturningAttributes(new String[] {});

    NamingEnumeration<SearchResult> results = ldap.search(dn, "objectclass=*", ctrls);

    while (results.hasMore()) {
      SearchResult result = results.next();
      tree.addAll(getChildDNs(result.getNameInNamespace(), ldap));
      tree.add(result.getNameInNamespace());
    }

    return tree;
  }

  /**
   * Returns all attributes. See {@link #searchAttributes(Ldap, String, String[])}
   */
  public static Attributes searchAttributes(Ldap ldap, String dn) throws NamingException,
      LdappcException {
    return searchAttributes(ldap, dn, null);
  }

  /**
   * Returns the attributes of a given LDAP dn. The underlying LdapContext method used is
   * search(), rather than getAttributes(), so that SearchResultHandlers are used. The
   * filter is (objectclass=*), the base is the given dn, and the scope is OBJECT.
   * 
   * @param ldap
   *          the vt-ldap connection
   * @param dn
   *          the dn to search for
   * @param retAttrs
   *          the attr names to return
   * @return
   * @throws NamingException
   * @throws LdappcException
   *           if one and only one matching dn is not found
   */
  public static Attributes searchAttributes(Ldap ldap, String dn, String[] retAttrs)
      throws NamingException, LdappcException {

    Iterator<SearchResult> results = LdapUtil.searchEntryDn(ldap, dn, retAttrs);

    if (!results.hasNext()) {
      LOG.debug("No result found for '" + dn + "'");
      throw new LdappcException("No result found for '" + dn + "'");
    }

    SearchResult result = results.next();

    if (results.hasNext()) {
      LOG.error("More than one result returned for '" + dn + "'");
      throw new LdappcException("More than one result returned for " + dn + "'");
    }

    return result.getAttributes();
  }

  /**
   * Perform a search for a given LDAP entry. The underlying LdapContext method used is
   * search(), rather than getAttributes(), so that SearchResultHandlers are used. The
   * filter is (objectclass=*), the base is the given dn, and the scope is OBJECT.
   * 
   * @param ldap
   * @param dn
   * @param retAttrs
   * @return
   * @throws NamingException
   */
  public static Iterator<SearchResult> searchEntryDn(Ldap ldap, String dn, String[] retAttrs) throws NamingException {

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
    searchControls.setReturningAttributes(retAttrs);
    searchControls.setCountLimit(1);

    return ldap.search(dn, new SearchFilter("objectclass=*"), searchControls);
  }

  /**
   * Escape all forward slashes "/" with "\/".
   * 
   * @param dn
   * @return the resultant string with / replaced with \/
   */
  public static String escapeForwardSlash(String dn) {

    Matcher matcher = forwardSlashPattern.matcher(dn);

    if (matcher.find()) {
      dn = matcher.replaceAll("$1\\\\/");
    }

    return dn;
  }

  /**
   * Remove the escape character "\" from all escaped forward slashes "\/", returning "/".
   * 
   * @param dn
   * @return the resultant string
   */
  public static String unescapeForwardSlash(String dn) {

    Matcher matcher = escapedforwardSlashPattern.matcher(dn);

    if (matcher.find()) {
      dn = matcher.replaceAll("/");
    }

    return dn;
  }

  /**
   * Normalize LDAP DN using {@link org.apache.directory.shared.ldap.name.LdapDN}. This
   * will convert RDN attributeTypes to lowercase, which is of interest since Active
   * Directory usually (?) returns attributeTypes uppercased.
   * 
   * @param dn
   * @return the lowercased and normalized dn
   * @throws InvalidNameException
   */
  public static String canonicalizeDn(String dn) throws InvalidNameException {
    return new LdapDN(LdapUtil.unescapeForwardSlash(dn)).toString();
  }

  public static String getParentDn(String dn) throws InvalidNameException {
    LdapDN ldapDN = new LdapDN(dn);
    ldapDN.remove(ldapDN.getRdns().size() - 1);
    return ldapDN.toString();
  }
}
