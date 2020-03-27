/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * config of one query
 */
public class CustomUiUserQueryConfigBean {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //printRequiredAndOptionalFields();
    
    //customUiUserQueryConfigBean = new edu.internet2.middleware.grouper.ui.customUi.CustomUiUserQueryConfigBean();
    //customUiUserQueryConfigBean.setUserQueryType("grouper");
    //customUiUserQueryConfigBean.setVariableToAssign("cu_o365twoStepRequiredToEnroll");
    //customUiUserQueryConfigBean.setVariableType("boolean");
    //customUiUserQueryConfigBean.setGroupName("penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod_policy");
    //customUiUserQueryConfigBean.setFieldNames("members");
    //
    //System.out.println(GrouperUtil.jsonConvertTo(customUiUserQueryConfigBean, false));
  }

  /**
   * 
   */
  public static void printRequiredAndOptionalFields() {
    Set<String> fieldNames = new TreeSet<String>(GrouperUtil.fieldNames(CustomUiUserQueryConfigBean.class, null, false));
    for (String fieldName : fieldNames) {
      System.out.println(fieldName);
      StringBuilder result = new StringBuilder();
      for (CustomUiUserQueryType customUiUserQueryType : CustomUiUserQueryType.values()) {
        if (customUiUserQueryType.requiredFieldNames().contains(fieldName)) {
          if (result.length() > 0) {
            result.append(", ");
          } else {
            result.append("Required: ");
          }
          result.append(customUiUserQueryType.name());
        }
      }
      System.out.println(result);
      result = new StringBuilder();
      for (CustomUiUserQueryType customUiUserQueryType : CustomUiUserQueryType.values()) {
        if (customUiUserQueryType.optionalFieldNames().contains(fieldName)) {
          if (result.length() > 0) {
            result.append(", ");
          } else {
            result.append("Optional: ");
          }
          result.append(customUiUserQueryType.name());
        }
      }
      System.out.println(result);
      System.out.println("");
    }
  }
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";

  /** constant for field name for: azureGroupId */
  public static final String FIELD_AZURE_GROUP_ID = "azureGroupId";

  /** constant for field name for: bindVar0 */
  public static final String FIELD_BIND_VAR0 = "bindVar0";

  /** constant for field name for: bindVar0type */
  public static final String FIELD_BIND_VAR0TYPE = "bindVar0type";

  /** constant for field name for: bindVar1 */
  public static final String FIELD_BIND_VAR1 = "bindVar1";

  /** constant for field name for: bindVar1type */
  public static final String FIELD_BIND_VAR1TYPE = "bindVar1type";

  /** constant for field name for: bindVar2 */
  public static final String FIELD_BIND_VAR2 = "bindVar2";

  /** constant for field name for: bindVar2type */
  public static final String FIELD_BIND_VAR2TYPE = "bindVar2type";

  /** constant for field name for: configId */
  public static final String FIELD_CONFIG_ID = "configId";

  /** constant for field name for: enabled */
  public static final String FIELD_ENABLED = "enabled";

  /** constant for field name for: error label */
  public static final String FIELD_ERROR_LABEL = "errorLabel";

  /** constant for field name for: fieldNames */
  public static final String FIELD_FIELD_NAMES = "fieldNames";

  /** constant for field name for: groupId */
  public static final String FIELD_GROUP_ID = "groupId";

  /** constant for field name for: forLoggedInUser */
  public static final String FIELD_FOR_LOGGED_IN_USER = "forLoggedInUser";

  /** constant for field name for: groupName */
  public static final String FIELD_GROUP_NAME = "groupName";

  /** constant for field name for: label */
  public static final String FIELD_LABEL = "label";

  /** constant for field name for: ldapAttributeToRetrieve */
  public static final String FIELD_LDAP_ATTRIBUTE_TO_RETRIEVE = "ldapAttributeToRetrieve";

  /** constant for field name for: ldapFilter */
  public static final String FIELD_LDAP_FILTER = "ldapFilter";

  /** constant for field name for: ldapSearchDn */
  public static final String FIELD_LDAP_SEARCH_DN = "ldapSearchDn";

  /** constant for field name for: listName */
  public static final String FIELD_LIST_NAME = "listName";

  /** constant for field name for: nameOfAttributeDef */
  public static final String FIELD_NAME_OF_ATTRIBUTE_DEF = "nameOfAttributeDef";

  /** constant for field name for: order */
  public static final String FIELD_ORDER = "order";

  /** constant for field name for: query */
  public static final String FIELD_QUERY = "query";

  /** constant for field name for: script */
  public static final String FIELD_SCRIPT = "script";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";

  /** constant for field name for: stemName */
  public static final String FIELD_STEM_NAME = "stemName";

  /** constant for field name for: userQueryType */
  public static final String FIELD_USER_QUERY_TYPE = "userQueryType";

  /** constant for field name for: variableToAssign */
  public static final String FIELD_VARIABLE_TO_ASSIGN = "variableToAssign";

  /** constant for field name for: variableToAssignOnError */
  public static final String FIELD_VARIABLE_TO_ASSIGN_ON_ERROR = "variableToAssignOnError";

  /** constant for field name for: variableType */
  public static final String FIELD_VARIABLE_TYPE = "variableType";

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * order to display on screen
   */
  private Integer order;
  
  /**
   * order to display on screen
   * @return the order
   */
  public Integer getOrder() {
    return this.order;
  }
  
  /**
   * order to display on screen
   * @param order1 the order to set
   */
  public void setOrder(Integer order1) {
    this.order = order1;
  }

  /**
   * label on screen
   */
  private String label;
  
  /**
   * label on screen
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }
  
  /**
   * label on screen
   * @param label1 the label to set
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * label if error
   */
  private String errorLabel;
  
  /**
   * label if error
   * @return the errorLabel
   */
  public String getErrorLabel() {
    return this.errorLabel;
  }
  
  /**
   * label if error
   * @param errorLabel1 the errorLabel to set
   */
  public void setErrorLabel(String errorLabel1) {
    this.errorLabel = errorLabel1;
  }

  /**
   * e.g. sql query
   */
  private String query;

  
  
  
  /**
   * e.g. sql query
   * @return the query
   */
  public String getQuery() {
    return this.query;
  }


  
  /**
   * e.g. sql query
   * @param query the query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * bind var script 0
   */
  private String bindVar0;

  
  
  
  /**
   * bind var script 0
   * @return the bindVar0
   */
  public String getBindVar0() {
    return this.bindVar0;
  }

  
  /**
   * bind var script 0
   * @param bindVar0 the bindVar0 to set
   */
  public void setBindVar0(String bindVar0) {
    this.bindVar0 = bindVar0;
  }

  /**
   * CustomUiVariableType
   */
  private String bindVar0type;
  
  /**
   * CustomUiVariableType
   * @return the bindVar0type
   */
  public String getBindVar0type() {
    return this.bindVar0type;
  }

  
  /**
   * CustomUiVariableType
   * @param bindVar0type the bindVar0type to set
   */
  public void setBindVar0type(String bindVar0type) {
    this.bindVar0type = bindVar0type;
  }

  /**
   * bind var 1
   */
  private String bindVar1;
  
  
  /**
   * @return the bindVar1
   */
  public String getBindVar1() {
    return this.bindVar1;
  }

  
  /**
   * @param bindVar1 the bindVar1 to set
   */
  public void setBindVar1(String bindVar1) {
    this.bindVar1 = bindVar1;
  }

  /**
   * CustomUiVariableType
   */
  private String bindVar1type;
  
  /**
   * CustomUiVariableType
   * @return the bindVar1type
   */
  public String getBindVar1type() {
    return this.bindVar1type;
  }

  
  /**
   * CustomUiVariableType
   * @param bindVar1type the bindVar1type to set
   */
  public void setBindVar1type(String bindVar1type) {
    this.bindVar1type = bindVar1type;
  }

  /**
   * 
   */
  private String bindVar2;
  
  /**
   * @return the bindVar2
   */
  public String getBindVar2() {
    return this.bindVar2;
  }

  
  /**
   * @param bindVar2 the bindVar2 to set
   */
  public void setBindVar2(String bindVar2) {
    this.bindVar2 = bindVar2;
  }

  /**
   * CustomUiVariableType
   * type of bind var2
   */
  private String bindVar2type;
  
  /**
   * CustomUiVariableType
   * type of bind var2
   * @return the bindVar2type
   */
  public String getBindVar2type() {
    return this.bindVar2type;
  }

  
  /**
   * CustomUiVariableType
   * type of bind var2
   * @param bindVar2type1 the bindVar2type to set
   */
  public void setBindVar2type(String bindVar2type1) {
    this.bindVar2type = bindVar2type1;
  }


  
  /**
   * stem id
   */
  private String stemId;
  
  
  /**
   * stem id
   * @return the stemId
   */
  public String getStemId() {
    return this.stemId;
  }
  
  /**
   * stem id
   * @param stemId the stemId to set
   */
  public void setStemId(String stemId) {
    this.stemId = stemId;
  }

  /**
   * stem name
   */
  private String stemName;
  
  
  /**
   * stem name
   * @return the stemName
   */
  public String getStemName() {
    return this.stemName;
  }

  
  /**
   * stem name
   * @param stemName the stemName to set
   */
  public void setStemName(String stemName) {
    this.stemName = stemName;
  }

  /**
   * attribute def id
   */
  private String attributeDefId;
  
  
  /**
   * attribute def id
   * @return the attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  
  /**
   * attribute def id
   * @param attributeDefId the attributeDefId to set
   */
  public void setAttributeDefId(String attributeDefId) {
    this.attributeDefId = attributeDefId;
  }

  /**
   * name of attribute def
   */
  private String nameOfAttributeDef;
  
  
  /**
   * name of attribute def
   * @return the nameOfAttributeDef
   */
  public String getNameOfAttributeDef() {
    return this.nameOfAttributeDef;
  }

  
  /**
   * name of attribute def
   * @param nameOfAttributeDef the nameOfAttributeDef to set
   */
  public void setNameOfAttributeDef(String nameOfAttributeDef) {
    this.nameOfAttributeDef = nameOfAttributeDef;
  }

  /**
   * members, readers, updaters, something from grouper_fields.name database column, comma separated
   */
  private String fieldNames;
  
  
  /**
   * members, readers, updaters, something from grouper_fields.name database column, comma separated
   * @return the fieldName
   */
  public String getFieldNames() {
    return this.fieldNames;
  }

  
  /**
   * members, readers, updaters, something from grouper_fields.name database column, comma separated
   * @param fieldNames the fieldName to set
   */
  public void setFieldNames(String fieldNames) {
    this.fieldNames = fieldNames;
  }

  /**
   * if this uses a script like EL 
   */
  private String script;
  
  /**
   * if this uses a script like EL 
   * @return the script
   */
  public String getScript() {
    return this.script;
  }

  /**
   * if this uses a script like EL 
   * @param script1 the script to set
   */
  public void setScript(String script1) {
    this.script = script1;
  }

  /**
   * if this involves a group, this is the groupId
   */
  private String groupId;
  
  
  /**
   * @return the groupId
   */
  public String getGroupId() {
    return this.groupId;
  }



  
  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
  /**
   * if this involves a group, this is the group name
   */
  private String groupName;

  /**
   * if this involves a group, this is the group name
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  
  /**
   * if this involves a group, this is the group name
   * @param groupName the groupName to set
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }


  /**
   * if hard coding the azure group id put it here
   */
  private String azureGroupId;
  
  
  
  
  /**
   * if hard coding the azure group id put it here
   * @return the azureGroupId
   */
  public String getAzureGroupId() {
    return this.azureGroupId;
  }


  
  /**
   * if hard coding the azure group id put it here
   * @param azureGroupId the azureGroupId to set
   */
  public void setAzureGroupId(String azureGroupId) {
    this.azureGroupId = azureGroupId;
  }


  /**
   * 
   */
  public CustomUiUserQueryConfigBean() {
  }

  /**
   * if this config is enabled
   */
  private Boolean enabled;
  
  
  
  
  /**
   * if this config is enabled
   * @return the enabled
   */
  public Boolean getEnabled() {
    return this.enabled;
  }

  
  /**
   * if this config is enabled
   * @param enabled the enabled to set
   */
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * must be one of the enum CustomUiUserQueryType
   */
  private String userQueryType;
  
  /**
   * must be one of the enum CustomUiUserQueryType
   * @return the userQueryType
   */
  public String getUserQueryType() {
    return this.userQueryType;
  }

  
  /**
   * must be one of the enum CustomUiUserQueryType
   * @param userQueryType the userQueryType to set
   */
  public void setUserQueryType(String userQueryType) {
    this.userQueryType = userQueryType;
  }

  /**
   * if this is for the logged in user instead of the user being operated on
   */
  private Boolean forLoggedInUser;

  
  
  /**
   * if this is for the logged in user instead of the user being operated on
   * @return for logged in user
   */
  public Boolean getForLoggedInUser() {
    return this.forLoggedInUser;
  }

  /**
   * 
   * @param forLoggedInUser1
   */
  public void setForLoggedInUser(Boolean forLoggedInUser1) {
    this.forLoggedInUser = forLoggedInUser1;
  }

  /**
   * some variable name to be used in display logic
   */
  private String variableToAssign;
  

  
  /**
   * some variable name to be used in display logic
   * @return the variableToAssign
   */
  public String getVariableToAssign() {
    return this.variableToAssign;
  }

  
  /**
   * some variable name to be used in display logic
   * @param variableToAssign the variableToAssign to set
   */
  public void setVariableToAssign(String variableToAssign) {
    this.variableToAssign = variableToAssign;
  }

  /**
   * one of CustomUiVariableType
   */
  private String variableType;
  

  /**
   * one of CustomUiVariableType
   * @return the variableType
   */
  public String getVariableType() {
    return this.variableType;
  }

  
  /**
   * one of CustomUiVariableType
   * @param variableType the variableType to set
   */
  public void setVariableType(String variableType) {
    this.variableType = variableType;
  }

  /**
   * variable name to set if there is an error in the query
   */
  private String variableToAssignOnError;

  /**
   * variable name to set if there is an error in the query
   * @return the variableToAssignOnError
   */
  public String getVariableToAssignOnError() {
    return this.variableToAssignOnError;
  }

  
  /**
   * variable name to set if there is an error in the query
   * @param variableToAssignOnError the variableToAssignOnError to set
   */
  public void setVariableToAssignOnError(String variableToAssignOnError) {
    this.variableToAssignOnError = variableToAssignOnError;
  }

  /**
   * ldap filter to run
   */
  private String ldapFilter;
  

  /**
   * ldap filter to run
   * @return the ldapFilter
   */
  public String getLdapFilter() {
    return this.ldapFilter;
  }

  
  /**
   * ldap filter to run
   * @param ldapFilter the ldapFilter to set
   */
  public void setLdapFilter(String ldapFilter) {
    this.ldapFilter = ldapFilter;
  }


  /**
   * ldap base dn to run filter on
   */
  private String ldapSearchDn; 
  

  /**
   * ldap base dn to run filter on
   * @return the ldapBaseDn
   */
  public String getLdapSearchDn() {
    return this.ldapSearchDn;
  }

  
  /**
   * ldap base dn to run filter on
   * @param ldapBaseDn the ldapBaseDn to set
   */
  public void setLdapSearchDn(String ldapBaseDn) {
    this.ldapSearchDn = ldapBaseDn;
  }

  /**
   * attribute to retrieve
   */
  private String ldapAttributeToRetrieve;
  

  
  /**
   * attribute to retrieve
   * @return the ldapAttributeToRetrieve
   */
  public String getLdapAttributeToRetrieve() {
    return this.ldapAttributeToRetrieve;
  }

  
  /**
   * attribute to retrieve
   * @param ldapAttributeToRetrieve the ldapAttributeToRetrieve to set
   */
  public void setLdapAttributeToRetrieve(String ldapAttributeToRetrieve) {
    this.ldapAttributeToRetrieve = ldapAttributeToRetrieve;
  }

  /**
   * config id in config file
   */
  private String configId;
  
  
  /**
   * config id in config file
   * @return the configId
   */
  public String getConfigId() {
    return this.configId;
  }

  
  /**
   * config id in config file
   * @param configId the configId to set
   */
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
