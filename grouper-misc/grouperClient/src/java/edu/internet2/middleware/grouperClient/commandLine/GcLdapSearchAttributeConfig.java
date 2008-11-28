/**
 * 
 */
package edu.internet2.middleware.grouperClient.commandLine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.api.GcLdapSearchAttribute;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * represents the configuration of an ldap search attribute
 * @author mchyzer
 */
public class GcLdapSearchAttributeConfig {

  /**
   * type of result in the ldap search attribute
   *
   */
  public static enum SearchAttributeResultType {
    
    /** if the is one result, with one string */
    string {
      
      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, GcLdapSearchAttribute gcLdapSearchAttribute) {
        //get the first label
        String returningAttributeLabel = gcLdapSearchAttributeConfig.getReturningAttributes().keySet().iterator().next();
        String returningAttributeValue = GrouperClientUtils.defaultString(gcLdapSearchAttribute.retrieveResultAttributeString());
        return returningAttributeLabel + ": " + returningAttributeValue + "\n";
      }
    },
    
    /** if there are multiple results, with one string in each */
    stringList {
      
      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, GcLdapSearchAttribute gcLdapSearchAttribute) {
        //get the first label
        String returningAttributeLabel = gcLdapSearchAttributeConfig.getReturningAttributes().keySet().iterator().next();
        List<String> returningAttributeValueList = GrouperClientUtils.nonNull(gcLdapSearchAttribute.retrieveResultAttributeStringList());
        StringBuilder result = new StringBuilder();
        for (String returningAttributeValue : returningAttributeValueList) {
          returningAttributeValue = GrouperClientUtils.defaultString(returningAttributeValue);
          result.append(returningAttributeLabel + ": " + returningAttributeValue + "\n");
        }
        return result.toString();
      }
    },
    
    /** if there are one or multiple results, and multiple strings in each */
    stringListList {
      
      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, GcLdapSearchAttribute gcLdapSearchAttribute) {
        throw new RuntimeException("Still need to implement");
      }
    };
    
    public static SearchAttributeResultType valueOfIgnoreCase(String string) {
      return GrouperClientUtils.enumValueOfIgnoreCase(SearchAttributeResultType.class, string, true);
    }
    
    /**
     * process the output
     * @param gcLdapSearchAttributeConfig
     * @param gcLdapSearchAttribute
     * @return the string of the output and a newline on the last line
     */
    public abstract String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, GcLdapSearchAttribute gcLdapSearchAttribute);
    
  }

  /**
   * result type 
   */
  private SearchAttributeResultType searchAttributeResultTypeEnum;
  /** operation name in the config file */
  private String operationName = null;

  /**
   * the result type
   * @param string to convert to enum
   */
  public void setSearchAttributeResultType(String string) {
    this.searchAttributeResultTypeEnum = SearchAttributeResultType.valueOfIgnoreCase(string);
  }

  /**
   * get the search result
   * @return the search result
   */
  public SearchAttributeResultType getSearchAttributeResultTypeEnum() {
    return this.searchAttributeResultTypeEnum;
  }
  
  /**
   * @see Object#toString()
   */
  public String toString() {
   return "operationName: " + this.operationName 
     + ", resultType: " + this.searchAttributeResultTypeEnum;
  }

  /**
   * operation name
   * @param theOperationName
   * @return this for chaining
   */
  public void setOperationName(String theOperationName) {
    this.operationName = theOperationName;
  }

  /** map of matching attribute label to ldap name */
  private Map<String, String> matchingAttributes = new LinkedHashMap<String, String>();
  
  /**
   * add matching attribute label and ldap name
   * @param attributeLabel
   * @param ldapAttribute
   */
  public void addMatchingAttribute(String attributeLabel, String ldapAttribute) {
    this.matchingAttributes.put(attributeLabel, ldapAttribute);
  }

  /**
   * returning attribute label and ldap name
   */
  private Map<String, String> returningAttributes = new LinkedHashMap<String, String>();
  
  /**
   * add a returning attribute
   * @param attributeLabel
   * @param ldapAttribute
   */
  public void addReturningAttribute(String attributeLabel, String ldapAttribute) {
    this.returningAttributes.put(attributeLabel, ldapAttribute);
  }

  /**
   * matching attribute (label to ldap name)
   * @return matching attributes
   */
  public Map<String, String> getMatchingAttributes() {
    return matchingAttributes;
  }

  /**
   * returning attribute (label to ldap)
   * @return the attributes
   */
  public Map<String, String> getReturningAttributes() {
    return returningAttributes;
  }
  
  /** ldap name */
  private String ldapName;

  /**
   * ldap name
   * @return ldap name
   */
  public String getLdapName() {
    return ldapName;
  }

  /**
   * ldap name
   * @param ldapName1
   */
  public void setLdapName(String ldapName1) {
    this.ldapName = ldapName1;
  }

  
}
