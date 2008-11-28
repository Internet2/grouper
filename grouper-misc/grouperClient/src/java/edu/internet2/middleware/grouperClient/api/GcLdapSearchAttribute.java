/**
 * 
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;

import edu.internet2.middleware.grouperClient.util.GrouperClientLdapUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Generate an ldap call and get results.  Note, you can only call the result methods once
 * since the enumeration will be done.
 * 
 * @author mchyzer
 *
 */
public class GcLdapSearchAttribute {

  
  
  /**
  SearchControls searchControls = new SearchControls();
  searchControls.setReturningAttributes(new String[]{"pennname"});
  searchControls.setReturningObjFlag(false);
  searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
  NamingEnumeration<?> namingEnumeration = context.search("ou=pennnames", "pennid=" + pennid, searchControls);
  //printNamingEnumeration(namingEnumeration);
  return GrouperClientLdapUtils.retrieveAttributeStringValue(namingEnumeration, "pennname");
  
      Attributes searchAttributes = new BasicAttributes();
    searchAttributes.put(new BasicAttribute("cn", groupName));
    
    NamingEnumeration namingEnumeration = context.search(
        "ou=groups",
        searchAttributes, new String[]{"hasMember"});
    List<String> members = GrouperClientLdapUtils.retrieveAttributeStringListValue(namingEnumeration, "hasMember");

*/

  public String retrieveResultAttributeString() {
    if (this.returningAttributes.size() != 1) {
      throw new RuntimeException("Should be looking for 1 attribute, but was looking for "
          + this.returningAttributes.size() + ", " + GrouperClientUtils.toStringForLog(this.returningAttributes));
    }
    try {
      return GrouperClientLdapUtils.retrieveAttributeStringValue(this.namingEnumeration, this.returningAttributes.iterator().next());
    } catch (NamingException ne) {
      throw new RuntimeException("Problem returning one attribute string: " 
          + this.toString(), ne);
    }
  }

  /**
   * see if the attribute value matches a certain value
   * @param valueToMatch
   * @return true if matches this value
   */
  public boolean retrieveResultAttributeStringMatch(String valueToMatch) {
    String attributeValue = this.retrieveResultAttributeString();
    return GrouperClientUtils.equals(valueToMatch, attributeValue);
  }

  /**
   * retrieve attribute list
   * @return the attributes
   */
  public List<String> retrieveResultAttributeStringList() {
    if (this.returningAttributes.size() != 1) {
      throw new RuntimeException("Should be looking for 1 attribute, but was looking for "
          + this.returningAttributes.size() + ", " + GrouperClientUtils.toStringForLog(this.returningAttributes));
    }
    try {
      return GrouperClientLdapUtils.retrieveAttributeStringListValue(this.namingEnumeration, this.returningAttributes.iterator().next());
    } catch (NamingException ne) {
      throw new RuntimeException("Problem returning one attribute list: " 
          + this.toString(), ne);
    }
  }
  
  /** name to search, e.g. ou=pennnames */
  private String ldapName;
  
  /** attributes to return */
  private Set<String> returningAttributes = new LinkedHashSet<String>();
  
  /** attributes to match */
  private Map<String, String> matchingAttributes = new LinkedHashMap<String, String>();
  
  /**
   * get a matching attribute value
   * @param matchingAttributeName
   * @return the attribute
   */
  public String getMatchingAttribute(String matchingAttributeName) {
    return this.matchingAttributes.get(matchingAttributeName);
  }
  
  /**
   * add a matching attribute
   * @param attributeName
   * @param attributeValue
   * @return this for chaining
   */
  public GcLdapSearchAttribute addMatchingAttribute(String attributeName, String attributeValue) {
    this.matchingAttributes.put(attributeName, attributeValue);
    return this;
  }
  
  /**
   * add an attribute to return
   * @param returningAttribute
   * @return the ldap call for chaining
   */
  public GcLdapSearchAttribute addReturningAttribute(String returningAttribute) {
    this.returningAttributes.add(returningAttribute);
    return this;
  }

  public void validate() {
    if (this.returningAttributes.isEmpty()) {
      throw new RuntimeException("Returning attributes are empty!");
    }
    if (this.matchingAttributes.isEmpty()) {
      throw new RuntimeException("Matching attributes are empty!");
    }
    if (GrouperClientUtils.isBlank(this.ldapName)) {
      throw new RuntimeException("Name is blank!");
    }
  }
  
  public void execute() {

    this.validate();
    
    DirContext context = GrouperClientLdapUtils.retrieveContext();
    
    Attributes searchAttributes = new BasicAttributes();
    for (String key : this.matchingAttributes.keySet()) {
      searchAttributes.put(new BasicAttribute(key, this.matchingAttributes.get(key)));
    }

    String[] returningAttributesArray = GrouperClientUtils.toArray(this.returningAttributes, String.class);
    try {
      this.namingEnumeration = context.search(this.ldapName, searchAttributes, returningAttributesArray);
    } catch (Exception e) {
      throw new RuntimeException("Error querying ldap for name: '" + this.ldapName + "', searchAttributes: " + 
          GrouperClientUtils.toStringForLog(this.matchingAttributes) + ", returning attributes: "
          + GrouperClientUtils.toStringForLog(this.returningAttributes), e);
    }
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return this.ldapName + "', searchAttributes: " + 
        GrouperClientUtils.toStringForLog(this.matchingAttributes) + ", returning attributes: "
        + GrouperClientUtils.toStringForLog(this.returningAttributes);
  }
  
  /** naming enumeration, is the result */
  private NamingEnumeration<?> namingEnumeration = null;
  
  /**
   * assign the name
   * @param theName
   * @return this for chaining
   */
  public GcLdapSearchAttribute assignLdapName(String theName) {
    this.ldapName = theName;
    return this;
  }

}
