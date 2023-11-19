/**
 * Copyright 2014 Internet2
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
 */
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
import javax.naming.directory.SearchControls;

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

  /** ldap search filter, e.g. (&(uid=test123)(cn=TestUser)) */
  private String searchFilter;
  
  /** scope of ldap search, e.g. sub */
  private String searchScope;
  
  /**
   * copy from the argument to this object
   * @param gcLdapSearchAttribute
   */
  public void copyFrom(GcLdapSearchAttribute gcLdapSearchAttribute) {
    this.ldapName = gcLdapSearchAttribute.ldapName;
    this.matchingAttributes = gcLdapSearchAttribute.matchingAttributes;
    this.namingEnumeration = gcLdapSearchAttribute.namingEnumeration;
    this.returningAttributes = gcLdapSearchAttribute.returningAttributes;
    this.searchFilter = gcLdapSearchAttribute.searchFilter;
    this.searchScope = gcLdapSearchAttribute.searchScope;
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
    // For backward compatibility. searchScope is set to ONE level if it's blank or not set
    if (GrouperClientUtils.isBlank(this.searchScope)) {
      this.searchScope = "one";
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

    /** search controls */
    SearchControls controls = new SearchControls();

    if (gcLdapSearchAttribute.searchScope.equalsIgnoreCase("one")) {
      controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    } else if (gcLdapSearchAttribute.searchScope.equalsIgnoreCase("sub")) {
      controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    } else if (gcLdapSearchAttribute.searchScope.equalsIgnoreCase("base")) {
      controls.setSearchScope(SearchControls.OBJECT_SCOPE);
    } else {
      throw new RuntimeException("Search scope is optional. If absent, it defaults to 'one'. If set, its value has to be either 'sub', 'base', or 'one'");
    }
    
    try {
      LOG.debug("LDAP search name: '" + gcLdapSearchAttribute.ldapName + "'");
      LOG.debug("LDAP search scope: '" + gcLdapSearchAttribute.searchScope + "'");
      
      gcLdapSearchAttribute.searchFilter = "(&";

      Attributes searchAttributes = new BasicAttributes();
      for (String key : gcLdapSearchAttribute.matchingAttributes.keySet()) {
        String value = gcLdapSearchAttribute.matchingAttributes.get(key);
        searchAttributes.put(new BasicAttribute(key, value));
	gcLdapSearchAttribute.searchFilter = gcLdapSearchAttribute.searchFilter + "(" + key + "=" + value + ")";
        LOG.debug("LDAP search attribute: '" + key + "' = '" + value + "'");
      }
      gcLdapSearchAttribute.searchFilter = gcLdapSearchAttribute.searchFilter + ")";
      LOG.debug("LDAP search filter: '" + gcLdapSearchAttribute.searchFilter + "'");
  
      String[] returningAttributesArray = GrouperClientUtils.toArray(gcLdapSearchAttribute.returningAttributes, String.class);
      controls.setReturningAttributes(returningAttributesArray);
  
      for (String returningAttribute : returningAttributesArray) {
        LOG.debug("LDAP search returning attribute: '" + returningAttribute + "'");
      }
      
      try {
  
        gcLdapSearchAttribute.namingEnumeration = context.search(gcLdapSearchAttribute.ldapName, 
            gcLdapSearchAttribute.searchFilter, controls);
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

  /**
   * assign the sarch scope
   * @param theScope
   * @return searchScope
   */
  public String assignSearchScope(String theScope) {
    this.searchScope = theScope;
    return this.searchScope;
  }

}
