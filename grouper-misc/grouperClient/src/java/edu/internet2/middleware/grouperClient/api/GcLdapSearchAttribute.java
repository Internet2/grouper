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

import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverLogic;
import edu.internet2.middleware.grouperClient.failover.FailoverLogicBean;
import edu.internet2.middleware.grouperClient.util.GrouperClientLdapUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * Generate an ldap call and get results.  Note, you can only call the result methods once
 * since the enumeration will be done.
 * 
 * @author mchyzer
 *
 */
public class GcLdapSearchAttribute {

  /** name to search, e.g. ou=pennnames */
  private String ldapName;
  
  /** attributes to return */
  private Set<String> returningAttributes = new LinkedHashSet<String>();
  
  /** attributes to match */
  private Map<String, String> matchingAttributes = new LinkedHashMap<String, String>();
  
  /** naming enumeration, is the result */
  private NamingEnumeration<?> namingEnumeration = null;

  /**
   * copy from the argument to this object
   * @param gcLdapSearchAttribute
   */
  public void copyFrom(GcLdapSearchAttribute gcLdapSearchAttribute) {
    this.ldapName = gcLdapSearchAttribute.ldapName;
    this.matchingAttributes = gcLdapSearchAttribute.matchingAttributes;
    this.namingEnumeration = gcLdapSearchAttribute.namingEnumeration;
    this.returningAttributes = gcLdapSearchAttribute.returningAttributes;
  }

  /**
   * 
   * @return string
   */
  public String retrieveResultAttributeString() {
    if (this.returningAttributes.size() != 1) {
      throw new RuntimeException("Should be looking for 1 attribute, but was looking for "
          + this.returningAttributes.size() + ", " + GrouperClientUtils.toStringForLog(this.returningAttributes));
    }
    try {
      String ldapAttribute = this.returningAttributes.iterator().next();
      if (LOG.isDebugEnabled()) {
        LOG.debug("method: GcLdapSearchAttribute.retrieveAttributeString, LDAP looking for attribute: '" + ldapAttribute + "'");
      }
      return GrouperClientLdapUtils.retrieveAttributeStringValue(this.namingEnumeration, ldapAttribute);
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

  /**
   * 
   */
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
  
  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcLdapSearchAttribute.class);

  /**
   * execute the call
   */
  public void execute() {
    //configure the failover client (every 30 seconds)
    GrouperClientLdapUtils.configureFailoverClient();
    
    //there could be multiple ldaps accepting connections, try each one if error or timeout
    GcLdapSearchAttribute gcLdapSearchAttribute = FailoverClient.failoverLogic(
        GrouperClientLdapUtils.LDAP_FAILOVER_CONFIG_NAME, new FailoverLogic<GcLdapSearchAttribute>() {

      /**
       * 
       */
      @Override
      public GcLdapSearchAttribute logic(FailoverLogicBean failoverLogicBean) {
        return executeHelper(GcLdapSearchAttribute.this, failoverLogicBean.getConnectionName());
      }
    });
    
    if (gcLdapSearchAttribute != null) {
      
      //copy from the instance back to this
      this.copyFrom(gcLdapSearchAttribute);
      
    }

  }

  /**
   * execute the call
   * @param input fields from the input
   * @param url 
   * @return the instance to copy from
   */
  public static GcLdapSearchAttribute executeHelper(GcLdapSearchAttribute input, String url) {

    GcLdapSearchAttribute gcLdapSearchAttribute = new GcLdapSearchAttribute();
    gcLdapSearchAttribute.copyFrom(input);
    gcLdapSearchAttribute.validate();
    
    DirContext context = GrouperClientLdapUtils.retrieveContext(url);
    
    try {
      LOG.debug("LDAP search name: '" + gcLdapSearchAttribute.ldapName + "'");
      
      Attributes searchAttributes = new BasicAttributes();
      for (String key : gcLdapSearchAttribute.matchingAttributes.keySet()) {
        String value = gcLdapSearchAttribute.matchingAttributes.get(key);
        searchAttributes.put(new BasicAttribute(key, value));
        LOG.debug("LDAP search attribute: '" + key + "' = '" + value + "'");
      }
  
      String[] returningAttributesArray = GrouperClientUtils.toArray(gcLdapSearchAttribute.returningAttributes, String.class);
  
      for (String returningAttribute : returningAttributesArray) {
        LOG.debug("LDAP search returning attribute: '" + returningAttribute + "'");
      }
      
      try {
  
        gcLdapSearchAttribute.namingEnumeration = context.search(gcLdapSearchAttribute.ldapName, 
            searchAttributes, returningAttributesArray);
      } catch (Exception e) {
        throw new RuntimeException("Error querying ldap for name: '" + gcLdapSearchAttribute.ldapName + "', searchAttributes: " + 
            GrouperClientUtils.toStringForLog(gcLdapSearchAttribute.matchingAttributes) + ", returning attributes: "
            + GrouperClientUtils.toStringForLog(gcLdapSearchAttribute.returningAttributes), e);
      }
    } finally {
      try {
        context.close();
      } catch (Exception e) {}
    }
    return gcLdapSearchAttribute;
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
