/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public enum CustomUiUserQueryType {

  /**
   * check something in azure
   */
  azure {

    @Override
    public Set<String> requiredFieldNames() {
      return azureRequiredFieldNames;
    }

    @Override
    public Set<String> optionalFieldNames() {
      return azureOptionaldFieldNames;
    }

    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group, Subject subject, Stem stem, AttributeDef attributeDef) {

      CustomUiUtil.validateGroup(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName(), group);

      boolean hasGroup = group != null;
      boolean hasAzureGroupId = !StringUtils.isBlank(customUiUserQueryConfigBean.getAzureGroupId());
      boolean hasUserScript = !StringUtils.isBlank(customUiUserQueryConfigBean.getScript());
      
      int queryCount = 0;
      if (hasGroup) {
        queryCount++;
        if (!StringUtils.isBlank(customUiUserQueryConfigBean.getVariableType())) {
          throw new RuntimeException("If you arent doing a user query then you cant set variableType");
        }

      }
      if (hasAzureGroupId) {
        queryCount++;
        if (!StringUtils.isBlank(customUiUserQueryConfigBean.getVariableType())) {
          throw new RuntimeException("If you arent doing a user query then you cant set variableType");
        }
      }
      if (hasUserScript) {
        queryCount++;
      }
      
      // could be a user query with 
      if (queryCount == 0 ) {
        throw new RuntimeException("You need to pass a groupId, groupName, or azureGroupId!");
      }
      
      if (queryCount != 1) {
        throw new RuntimeException("Can only have 1 query type: " + (hasGroup ? "group, " : "") + (hasAzureGroupId ? "azureGroupId, " : "") + (hasUserScript ? "user, " : ""));
      }
      
      if (hasUserScript && StringUtils.isBlank(customUiUserQueryConfigBean.getScript())) {
        throw new RuntimeException("If you are querying an azure user, you need to pass a 'script'");
      }
      
      if (!hasUserScript && !StringUtils.isBlank(customUiUserQueryConfigBean.getScript())) {
        throw new RuntimeException("If you are not querying an azure user, you cannot pass a 'script'");
      }
    }

    @Override
    public Object evaluate(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
      
      CustomUiAzure customUiAzure = new CustomUiAzure();
      customUiAzure.setCustomUiEngine(customUiEngine);
      customUiAzure.setDebugMapPrefix(customUiUserQueryConfigBean.getVariableToAssign());
      
      if (group != null) {
        
        boolean result = customUiAzure.hasAzureMembershipByGroup(customUiUserQueryConfigBean.getConfigId(), group, subject);
        
        return result;
      }
      
      if (!StringUtils.isBlank(customUiUserQueryConfigBean.getAzureGroupId())) {
        boolean result = customUiAzure.hasAzureMembershipByAzureGroupId(customUiUserQueryConfigBean.getConfigId(), customUiUserQueryConfigBean.getAzureGroupId(), subject);
        
        return result;
      }
      
      // user
      Map<String, Object> azureUser = customUiAzure.retrieveAzureUserOrFromCache(customUiUserQueryConfigBean.getConfigId(), subject);
      
      // dont substitute the sql, for security reasons
      String result = CustomUiUtil.substituteExpressionLanguage(customUiUserQueryConfigBean.getScript(), group, stem, attributeDef, subject, azureUser);
      
      CustomUiVariableType customUiVariableType = CustomUiVariableType.valueOfIgnoreCase(customUiUserQueryConfigBean.getVariableType(), false);
      
      Object resultObject = customUiVariableType.convertTo(result);
      return resultObject;
    }

    @Override
    public String description(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean, 
        Group group, Subject subject, Stem stem, AttributeDef attributeDef, Map<String, Object> argumentMap) {
      
      if (group == null) {
      
        if (!StringUtils.isBlank(customUiUserQueryConfigBean.getAzureGroupId())) {
        
          return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionAzureGroupId']}", 
              group, null, null, subject, argumentMap, true);
        }
        
        return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionAzureExpression']}", 
            null, null, null, subject, argumentMap, true);
        
      } 
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionAzureGroup']}", 
          group, null, null, subject, argumentMap, true);
    }

    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }

  },
  
  /**
   * run an expression language
   */
  expressionLanguage {

    @Override
    public Set<String> requiredFieldNames() {
      return expressionLanguageRequiredFieldNames;
    }

    @Override
    public Set<String> optionalFieldNames() {
      return expressionLanguageOptionaldFieldNames;
    }

    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
      CustomUiUtil.validateGroup(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName(), group);
    }

    @Override
    public Object evaluate(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef) {
      CustomUiExpressionLanguage customUiExpressionLanguage = new CustomUiExpressionLanguage();
      customUiExpressionLanguage.setCustomUiEngine(customUiEngine);
      customUiExpressionLanguage.setDebugMapPrefix(customUiUserQueryConfigBean.getVariableToAssign());
      CustomUiVariableType customUiVariableType = CustomUiVariableType.valueOfIgnoreCase(customUiUserQueryConfigBean.getVariableType(), false);
      Object result = customUiExpressionLanguage.expression(customUiUserQueryConfigBean.getScript(), group, subject, customUiVariableType, stem, attributeDef);
      return result;
    }

    @Override
    public String description(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef,
        Map<String, Object> argumentMap) {
      
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionExpression']}", 
          group, stem, attributeDef, subject, argumentMap, true);
        
    }
    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }
  },
  
  
  /**
   * do a grouper check
   */
  grouper {

    @Override
    public Set<String> requiredFieldNames() {
      return grouperRequiredFieldNames;
    }

    @Override
    public Set<String> optionalFieldNames() {
      return grouperOptionaldFieldNames;
    }

    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {

      CustomUiUtil.validateGroup(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName(), group);
      
      CustomUiUtil.validateStem(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName(), stem);
      
      CustomUiUtil.validateAttributeDef(customUiUserQueryConfigBean.getAttributeDefId(), customUiUserQueryConfigBean.getNameOfAttributeDef(), attributeDef);
      
      String fieldNames = customUiUserQueryConfigBean.getFieldNames();
      if (!StringUtils.isBlank(fieldNames)) {
        for (String fieldName : GrouperUtil.splitTrim(fieldNames, ",")) {
          Field field = FieldFinder.find(fieldName, true);
          if (field.isGroupListField() || field.isGroupAccessField()) {
            if (group == null) {
              throw new RuntimeException("Must configure groupId or groupName");
            }
          }
          if (field.isStemListField()) {
            if (stem == null) {
              throw new RuntimeException("Must configure stemId or stemName");
            }
          }
          if (field.isAttributeDefListField()) {
            if (attributeDef == null) {
              throw new RuntimeException("Must configure attributeDefId or nameOfAttributeDef");
            }
          }
        }
      }
    }

    @Override
    public Object evaluate(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef) {
      
      CustomUiGrouper customUiGrouper = new CustomUiGrouper();
      customUiGrouper.setCustomUiEngine(customUiEngine);

      boolean result = true;

      String fieldNames = customUiUserQueryConfigBean.getFieldNames();
      if (StringUtils.isBlank(fieldNames)) {
        fieldNames = "members";
      }
      for (String fieldName : GrouperUtil.splitTrim(fieldNames, ",")) {
        
        Field field = FieldFinder.find(fieldName, true);
        
        customUiGrouper.setDebugMapPrefix(customUiUserQueryConfigBean.getVariableToAssign() + "_" + field.getName());

        if (field.isGroupListField() && !field.isGroupAccessField()) {
          MultiKey groupNameSourceIdSubjectId = new MultiKey(group.getName(), subject.getSourceId(), subject.getId());
          result = result && customUiEngine.getCustomUiGrouperForCache().getMembershipGroupNameSourceIdSubjectIdToGroupMap().containsKey(groupNameSourceIdSubjectId);
        } else if (field.isGroupAccessField()) {
          
          result = result && customUiGrouper.canHaveGroupPrivilege(group, subject, field.getName());
        } else if (field.isStemListField()) {

          result = result && customUiGrouper.canHaveStemPrivilege(stem, subject, field.getName());
        } else if (field.isAttributeDefListField()) {
          
          result = result && customUiGrouper.canHaveAttributeDefPrivilege(attributeDef, subject, field.getName());
        } else {
          throw new RuntimeException("Why here? " + field.getName());
        }
      }

      return result;
    }

    @Override
    public String description(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef,
        Map<String, Object> argumentMap) {
      
      
      String fieldNames = customUiUserQueryConfigBean.getFieldNames();
      if (StringUtils.isBlank(fieldNames)) {
        fieldNames = "members";
      }
      String members = null;
      StringBuilder access = new StringBuilder();
      StringBuilder naming = new StringBuilder();
      StringBuilder attributeDefPrivs = new StringBuilder();

      for (String fieldName : new TreeSet<String>(GrouperUtil.splitTrimToSet(fieldNames, ","))) {
        
        Field field = FieldFinder.find(fieldName, true);
        
        if (field.isGroupListField() && !field.isGroupAccessField()) {
          members = "members";
          continue;
        }
        
        Privilege privilege = Privilege.getInstance(field.getName());
        
        String privHtml = CustomUiUtil.substituteExpressionLanguage("${textContainer.text['priv." + privilege + "']}", 
            null, null, null, null, argumentMap);

        StringBuilder current = null;
        if (field.isGroupAccessField()) {
          current = access;
        } else if (field.isStemListField()) {
          current = naming;
        } else if (field.isAttributeDefListField()) {
          current = attributeDefPrivs;
        } else {
          throw new RuntimeException("Why here? " + field.getName());
        }
        if (current.length() > 0) {
          current.append(", ");
        }
        current.append(privHtml);

      }
      StringBuilder result = new StringBuilder();
      if (!StringUtils.isBlank(members)) {
        
        if (result.length() > 0) {
          result.append("<br />");
        }
        
        result.append(CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionGroupMember']}", 
            group, null, null, subject, argumentMap, true));
        
      }
      if (access.length() > 0) {
        
        if (result.length() > 0) {
          result.append("<br />");
        }
        argumentMap.put("thePrivileges", access.toString());
        
        result.append(CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionGroupAccess']}", 
            group, null, null, subject, argumentMap, true));
        
      }
      if (naming.length() > 0) {
        
        if (result.length() > 0) {
          result.append("<br />");
        }
        argumentMap.put("thePrivileges", naming.toString());
        
        result.append(CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionStemNaming']}", 
            null, stem, null, subject, argumentMap, true));
        
      }
      if (attributeDefPrivs.length() > 0) {
        
        if (result.length() > 0) {
          result.append("<br />");
        }
        argumentMap.put("thePrivileges", attributeDefPrivs.toString());
        
        result.append(CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionAttributeDefPrivileges']}", 
            null, null, attributeDef, subject, argumentMap, true));
        
      }
      return result.toString();
    }
    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }
  },
  
  /**
   * do an ldap filter
   */
  ldap {

    @Override
    public Set<String> requiredFieldNames() {
      return ldapRequiredFieldNames;
    }

    @Override
    public Set<String> optionalFieldNames() {
      return ldapOptionalFieldNames;
    }

    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {

    }

    @Override
    public Object evaluate(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef) {
      CustomUiLdap customUiLdap = new CustomUiLdap();
      customUiLdap.setCustomUiEngine(customUiEngine);
      customUiLdap.setDebugMapPrefix(customUiUserQueryConfigBean.getVariableToAssign());

      CustomUiVariableType customUiVariableType = CustomUiVariableType.valueOfIgnoreCase(customUiUserQueryConfigBean.getVariableType(), false);

      Object result = customUiLdap.ldapFilter(customUiUserQueryConfigBean.getConfigId(), customUiUserQueryConfigBean.getLdapSearchDn(), 
          customUiUserQueryConfigBean.getLdapFilter(), customUiUserQueryConfigBean.getLdapAttributeToRetrieve(), group, subject, customUiVariableType);

      return result;
    }

    @Override
    public String description(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef,
        Map<String, Object> argumentMap) {
      
      if (customUiUserQueryConfigBean.getLdapFilter() != null && customUiUserQueryConfigBean.getLdapFilter().contains("${group")) {
        
        return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionLdapGroup']}", 
            group, stem, attributeDef, subject, argumentMap, true);
        
      }
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionLdap']}", 
          group, stem, attributeDef, subject, argumentMap, true);

    }
    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }

  },
  
  /**
   * do a sql query
   */
  sql {

    @Override
    public Set<String> requiredFieldNames() {
      return sqlRequiredFieldNames;
    }

    @Override
    public Set<String> optionalFieldNames() {
      return sqlOptionaldFieldNames;
    }

    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
      
      CustomUiUtil.validateGroup(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName(), group);

      CustomUiUtil.validateStem(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName(), stem);
      
      CustomUiUtil.validateAttributeDef(customUiUserQueryConfigBean.getAttributeDefId(), customUiUserQueryConfigBean.getNameOfAttributeDef(), attributeDef);

    }

    @Override
    public Object evaluate(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef) {
      CustomUiSql customUiSql = new CustomUiSql();
      customUiSql.setCustomUiEngine(customUiEngine);
      customUiSql.setDebugMapPrefix(customUiUserQueryConfigBean.getVariableToAssign());

      CustomUiVariableType customUiVariableType = CustomUiVariableType.valueOfIgnoreCase(customUiUserQueryConfigBean.getVariableType(), false);

      Object result = customUiSql.sqlResult(customUiUserQueryConfigBean.getConfigId(), customUiUserQueryConfigBean.getQuery(), 
          group, stem, attributeDef, subject, customUiUserQueryConfigBean.getBindVar0(), customUiUserQueryConfigBean.getBindVar0type(), 
          customUiUserQueryConfigBean.getBindVar1(), customUiUserQueryConfigBean.getBindVar1type(), customUiUserQueryConfigBean.getBindVar2(), 
          customUiUserQueryConfigBean.getBindVar2type(), customUiVariableType);

      return result;
    }

    @Override
    public String description(CustomUiEngine customUiEngine,
        CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group,
        Subject subject, Stem stem, AttributeDef attributeDef,
        Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionSql']}", 
          group, stem, attributeDef, subject, argumentMap, true);
    }

    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }
  }, 
  /**
   * check provisioning in box
   */
  box {
  
    @Override
    public Set<String> requiredFieldNames() {
      return boxRequiredFieldNames;
    }
  
    @Override
    public Set<String> optionalFieldNames() {
      return boxOptionaldFieldNames;
    }
  
    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
  
    }
  
    @Override
    public Object evaluate(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
      
      boolean[] accountValid = new boolean[]{false};
      Map<String, Object> variableMap = new HashMap<String, Object>();

      Class<?> clazz = GrouperUtil.forName("edu.internet2.middleware.grouperBox.CustomUiBox");
      variableMap = (Map<String, Object>)GrouperUtil.callMethod(clazz, "customUiBoxUserAnalysis", 
          new Object[]{String.class, String.class}, 
          new Object[]{subject.getSourceId(), subject.getId()});
      
      //todo 
      return null;

    }
  
    @Override
    public String description(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean, 
        Group group, Subject subject, Stem stem, AttributeDef attributeDef, Map<String, Object> argumentMap) {
      
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionBox']}", 
          group, null, null, subject, argumentMap, true);
    }
  
    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }
  
  }, 
  
  /**
   * check provisioning in box
   */
  zoom {
  
    @Override
    public Set<String> requiredFieldNames() {
      return zoomRequiredFieldNames;
    }
  
    @Override
    public Set<String> optionalFieldNames() {
      return zoomOptionaldFieldNames;
    }
  
    @Override
    public void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
  
    }
  
    @Override
    public Object evaluate(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean,
        Group group, Subject subject, Stem stem, AttributeDef attributeDef) {
      
      Class<?> clazz = GrouperUtil.forName("edu.internet2.middleware.grouper.app.zoom.CustomUiZoom");
      Map<String, Object> variableMap = (Map<String, Object>)GrouperUtil.callMethod(clazz, "customUiZoomUserAnalysis", 
          new Class[]{String.class, String.class, String.class}, 
          new Object[]{customUiUserQueryConfigBean.getConfigId() ,subject.getSourceId(), subject.getId()});
      
      // dont substitute the sql, for security reasons
      String result = CustomUiUtil.substituteExpressionLanguage(customUiUserQueryConfigBean.getScript(), group, stem, attributeDef, subject, variableMap);
      
      CustomUiVariableType customUiVariableType = CustomUiVariableType.valueOfIgnoreCase(customUiUserQueryConfigBean.getVariableType(), false);
      
      Object resultObject = customUiVariableType.convertTo(result);
      return resultObject;

    }
  
    @Override
    public String description(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean, 
        Group group, Subject subject, Stem stem, AttributeDef attributeDef, Map<String, Object> argumentMap) {
      
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryDescriptionZoomExpression']}", 
          null, null, null, subject, argumentMap, true);
    }
  
    @Override
    public String label(Map<String, Object> argumentMap) {
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiUserQueryTypeLabel_" + this.name().toLowerCase() + "']}", 
          null, null, null, null, argumentMap);
    }
  
  };

  /**
   * azure required
   */
  private static Set<String> azureRequiredFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_CONFIG_ID,
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR
      );

  /**
   * azure optional
   */
  private static Set<String> azureOptionaldFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_AZURE_GROUP_ID, 
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_FOR_LOGGED_IN_USER,
      CustomUiUserQueryConfigBean.FIELD_SCRIPT, 
      CustomUiUserQueryConfigBean.FIELD_GROUP_ID, 
      CustomUiUserQueryConfigBean.FIELD_GROUP_NAME,
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TYPE
      );
    
  /**
   * box required
   */
  private static Set<String> boxRequiredFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_CONFIG_ID,
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR
      );

  /**
   * box optional
   */
  private static Set<String> boxOptionaldFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ACCOUNT_VALID
      );
    
  /**
   * grouper required
   */
  private static Set<String> grouperRequiredFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN
      );

  /**
   * grouper optional
   */
  private static Set<String> grouperOptionaldFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_ATTRIBUTE_DEF_ID, 
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_FIELD_NAMES, 
      CustomUiUserQueryConfigBean.FIELD_FOR_LOGGED_IN_USER,
      CustomUiUserQueryConfigBean.FIELD_GROUP_ID, 
      CustomUiUserQueryConfigBean.FIELD_GROUP_NAME,
      CustomUiUserQueryConfigBean.FIELD_NAME_OF_ATTRIBUTE_DEF,
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_STEM_ID, 
      CustomUiUserQueryConfigBean.FIELD_STEM_NAME,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TYPE
      );
    
  /**
   * el required
   */
  private static Set<String> expressionLanguageRequiredFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_SCRIPT,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN
      );

  /**
   * el optional
   */
  private static Set<String> expressionLanguageOptionaldFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_ATTRIBUTE_DEF_ID, 
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_FOR_LOGGED_IN_USER,
      CustomUiUserQueryConfigBean.FIELD_GROUP_ID, 
      CustomUiUserQueryConfigBean.FIELD_GROUP_NAME,
      CustomUiUserQueryConfigBean.FIELD_NAME_OF_ATTRIBUTE_DEF,
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_STEM_ID, 
      CustomUiUserQueryConfigBean.FIELD_STEM_NAME,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TYPE
      );
    
  /**
   * sql required
   */
  private static Set<String> sqlRequiredFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_QUERY,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN
      );

  /**
   * sql optional
   */
  private static Set<String> sqlOptionaldFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_ATTRIBUTE_DEF_ID, 
      CustomUiUserQueryConfigBean.FIELD_BIND_VAR0,
      CustomUiUserQueryConfigBean.FIELD_BIND_VAR0TYPE,
      CustomUiUserQueryConfigBean.FIELD_BIND_VAR1,
      CustomUiUserQueryConfigBean.FIELD_BIND_VAR1TYPE,
      CustomUiUserQueryConfigBean.FIELD_BIND_VAR2,
      CustomUiUserQueryConfigBean.FIELD_BIND_VAR2TYPE,
      CustomUiUserQueryConfigBean.FIELD_CONFIG_ID, 
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_FOR_LOGGED_IN_USER,
      CustomUiUserQueryConfigBean.FIELD_GROUP_ID, 
      CustomUiUserQueryConfigBean.FIELD_GROUP_NAME,
      CustomUiUserQueryConfigBean.FIELD_NAME_OF_ATTRIBUTE_DEF,
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_STEM_ID, 
      CustomUiUserQueryConfigBean.FIELD_STEM_NAME,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TYPE
      );
    
  /**
   * ldap required
   */
  private static Set<String> ldapRequiredFieldNames = GrouperUtil.toSet(
      
      CustomUiUserQueryConfigBean.FIELD_CONFIG_ID,
      CustomUiUserQueryConfigBean.FIELD_LDAP_ATTRIBUTE_TO_RETRIEVE,
      CustomUiUserQueryConfigBean.FIELD_LDAP_FILTER,
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN
      );

  /**
   * ldap optional
   */
  private static Set<String> ldapOptionalFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_FOR_LOGGED_IN_USER,
      CustomUiUserQueryConfigBean.FIELD_GROUP_ID, 
      CustomUiUserQueryConfigBean.FIELD_GROUP_NAME,
      CustomUiUserQueryConfigBean.FIELD_LDAP_SEARCH_DN,
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TYPE
      );
    
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static CustomUiUserQueryType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(CustomUiUserQueryType.class, 
        string, exceptionOnNull);
  
  }

  /**
   * box optional
   */
  private static Set<String> zoomOptionaldFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_ENABLED, 
      CustomUiUserQueryConfigBean.FIELD_ORDER,
      CustomUiUserQueryConfigBean.FIELD_SCRIPT, 
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TYPE

      );

  /**
   * box required
   */
  private static Set<String> zoomRequiredFieldNames = GrouperUtil.toSet(
      CustomUiUserQueryConfigBean.FIELD_CONFIG_ID,
      CustomUiUserQueryConfigBean.FIELD_ERROR_LABEL,
      CustomUiUserQueryConfigBean.FIELD_LABEL,
      CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN,
      CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN_ON_ERROR
    );

  /**
   * 
   * @param argumentMap
   * @return the label
   */
  public abstract String label(Map<String, Object> argumentMap);
  
  /**
   * @param argumentMap 
   * @return description
   */
  public abstract String description(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group, Subject subject, Stem stem, AttributeDef attributeDef, Map<String, Object> argumentMap);
  
  /**
   * see if everything is set ok
   * @return required field name
   */
  public abstract Set<String> requiredFieldNames();
  
  /**
   * see if everything is set ok
   * @return required field name
   */
  public abstract Set<String> optionalFieldNames();
  
  /**
   * 
   * @param customUiUserQueryConfigBean
   * @param group 
   * @param subject 
   * @param stem 
   * @param attributeDef 
   */
  public abstract void validate(CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group, Subject subject, Stem stem, AttributeDef attributeDef);
  
  /**
   * 
   * @param customUiEngine 
   * @param customUiUserQueryConfigBean
   * @param group 
   * @param subject 
   * @param stem 
   * @param attributeDef 
   * @return the value, note, doesnt have to be tpye cast yet
   */
  public abstract Object evaluate(CustomUiEngine customUiEngine, CustomUiUserQueryConfigBean customUiUserQueryConfigBean, Group group, Subject subject, Stem stem, AttributeDef attributeDef);
  
}
