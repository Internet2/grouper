/**
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouperClient.commandLine;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    STRING {
      
      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @param outputTemplate
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, 
          GcLdapSearchAttribute gcLdapSearchAttribute, String outputTemplate) {
        //get the first label
        String returningLdapAttribute = gcLdapSearchAttributeConfig.getReturningAttributes().iterator().next();
        String returningAttributeValue = GrouperClientUtils.defaultString(gcLdapSearchAttribute.retrieveResultAttributeString());

        Map<String, Object> substitutionVars = new LinkedHashMap<String, Object>();
        substitutionVars.put(returningLdapAttribute, returningAttributeValue);
        substitutionVars.put("grouperClientUtils", new GrouperClientUtils());

        addMatchingAttributesToSubstitutionVars(gcLdapSearchAttributeConfig,
            gcLdapSearchAttribute, substitutionVars);
        
        String result = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substitutionVars);
        
        return result + "\n";
      }
    },
    
    /** if it is one result, with one string, and it matches the input */
    BOOLEAN {
      
      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @param outputTemplate
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, 
          GcLdapSearchAttribute gcLdapSearchAttribute, String outputTemplate) {
        //get the first label
        String ldapReturningAttribute = gcLdapSearchAttributeConfig.getReturningAttributes().iterator().next();

        String inputValue = gcLdapSearchAttribute.getMatchingAttribute(ldapReturningAttribute);

        //see if the input attribute value is the same as the one found (if found)
        String returningAttributeValue = GrouperClientUtils.defaultString(gcLdapSearchAttribute.retrieveResultAttributeString());
        boolean foundAttribute = GrouperClientUtils.equals(inputValue, returningAttributeValue);

        Map<String, Object> substitutionVars = new LinkedHashMap<String, Object>();
        substitutionVars.put(ldapReturningAttribute, returningAttributeValue);
        substitutionVars.put("resultBoolean", foundAttribute);
        substitutionVars.put("grouperClientUtils", new GrouperClientUtils());

        addMatchingAttributesToSubstitutionVars(gcLdapSearchAttributeConfig,
            gcLdapSearchAttribute, substitutionVars);
        
        String result = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substitutionVars);
        
        return result + "\n";
      }
    },

    /** if there are multiple results, with one string in each */
    STRING_LIST {

      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @param outputTemplate
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, 
          GcLdapSearchAttribute gcLdapSearchAttribute, String outputTemplate) {

        List<String> returningAttributeValueList = GrouperClientUtils.nonNull(gcLdapSearchAttribute.retrieveResultAttributeStringList());
        StringBuilder result = new StringBuilder();
        
        Map<String, Object> substitutionVars = new LinkedHashMap<String, Object>();
        substitutionVars.put("grouperClientUtils", new GrouperClientUtils());

        addMatchingAttributesToSubstitutionVars(gcLdapSearchAttributeConfig,
            gcLdapSearchAttribute, substitutionVars);
        
        for (String returningAttributeValue : returningAttributeValueList) {
          returningAttributeValue = GrouperClientUtils.defaultString(returningAttributeValue);
          substitutionVars.put("resultString", returningAttributeValue);

          String row = GrouperClientUtils.substituteExpressionLanguage(outputTemplate, substitutionVars);
          result.append(row);
        }
        result.append("\n");
        return result.toString();
      }
    },
    
    /** if there are one or multiple results, and multiple strings in each */
    STRING_LIST_LIST {
      
      /**
       * process the output
       * @param gcLdapSearchAttributeConfig
       * @param gcLdapSearchAttribute
       * @param outputTemplate
       * @return the string of the output
       */
      @Override
      public String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, 
          GcLdapSearchAttribute gcLdapSearchAttribute, String outputTemplate) {
        throw new RuntimeException("Still need to implement");
      }
    };
    
    /**
     * 
     * @param string
     * @return the type
     */
    public static SearchAttributeResultType valueOfIgnoreCase(String string) {
      return GrouperClientUtils.enumValueOfIgnoreCase(SearchAttributeResultType.class, string, true);
    }
    
    /**
     * process the output
     * @param gcLdapSearchAttributeConfig
     * @param gcLdapSearchAttribute
     * @param outputTemplate
     * @return the string of the output and a newline on the last line
     */
    public abstract String processOutput(GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig, 
        GcLdapSearchAttribute gcLdapSearchAttribute, String outputTemplate);

    /**
     * @param gcLdapSearchAttributeConfig
     * @param gcLdapSearchAttribute
     * @param substitutionVars
     */
    void addMatchingAttributesToSubstitutionVars(
        GcLdapSearchAttributeConfig gcLdapSearchAttributeConfig,
        GcLdapSearchAttribute gcLdapSearchAttribute, Map<String, Object> substitutionVars) {
      //also add the input
      for (String matchingAttributeLabel: gcLdapSearchAttributeConfig.getMatchingAttributes().keySet()) {
        String ldapName = gcLdapSearchAttributeConfig.getMatchingAttributes().get(matchingAttributeLabel);
        String ldapValue = gcLdapSearchAttribute.getMatchingAttribute(ldapName);
        substitutionVars.put(ldapName, ldapValue);
      }

      //might as well add labels if there is no conflict
      for (String matchingAttributeLabel: gcLdapSearchAttributeConfig.getMatchingAttributes().keySet()) {
        String ldapName = gcLdapSearchAttributeConfig.getMatchingAttributes().get(matchingAttributeLabel);
        String ldapValue = gcLdapSearchAttribute.getMatchingAttribute(ldapName);
        if (!substitutionVars.containsKey(matchingAttributeLabel)) {
          substitutionVars.put(matchingAttributeLabel, ldapValue);
        }
      }
    }
    
  }

  /**
   * result type 
   */
  private SearchAttributeResultType searchAttributeResultTypeEnum;
  /** operation name in the config file */
  private String operationName = null;

  /** output template */
  private String outputTemplate = null;
  
  /**
   * output template
   * @return output template
   */
  public String getOutputTemplate() {
    return this.outputTemplate;
  }

  /**
   * output template
   * @param outputTemplate1
   */
  public void setOutputTemplate(String outputTemplate1) {
    this.outputTemplate = outputTemplate1;
  }

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
  @Override
  public String toString() {
   return "operationName: " + this.operationName 
     + ", resultType: " + this.searchAttributeResultTypeEnum;
  }

  /**
   * operation name
   * @param theOperationName
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
  private Set<String> returningAttributes = new LinkedHashSet<String>();
  
  /**
   * add a returning attribute
   * @param ldapAttribute
   */
  public void addReturningAttribute(String ldapAttribute) {
    this.returningAttributes.add(ldapAttribute);
  }

  /**
   * matching attribute (label to ldap name)
   * @return matching attributes
   */
  public Map<String, String> getMatchingAttributes() {
    return this.matchingAttributes;
  }

  /**
   * returning attribute (label to ldap)
   * @return the attributes
   */
  public Set<String> getReturningAttributes() {
    return this.returningAttributes;
  }
  
  /** ldap name */
  private String ldapName;

  /**
   * ldap name
   * @return ldap name
   */
  public String getLdapName() {
    return this.ldapName;
  }

  /**
   * ldap name
   * @param ldapName1
   */
  public void setLdapName(String ldapName1) {
    this.ldapName = ldapName1;
  }

  
}
