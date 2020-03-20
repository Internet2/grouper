/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import jline.internal.Log;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 *
 */
public class CustomUiEngine {
  
  /**
   * debug map for custom ui
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  /**
   * user query beans
   */
  private List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeansForUser = new ArrayList<CustomUiUserQueryConfigBean>();
  
  /**
   * user query beans
   */
  private List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeansForManager = new ArrayList<CustomUiUserQueryConfigBean>();
  
  /**
   * display beans
   */
  private Set<CustomUiUserQueryDisplayBean> customUiUserQueryDisplayBeans = new TreeSet<CustomUiUserQueryDisplayBean>();
  
  
  /**
   * display beans
   * @return the customUiUserQueryDisplayBeans
   */
  public Set<CustomUiUserQueryDisplayBean> getCustomUiUserQueryDisplayBeans() {
    return this.customUiUserQueryDisplayBeans;
  }

  /**
   * debug map for custom ui
   * @return the debugMap
   */
  public Map<String, Object> getDebugMap() {
    return this.debugMap;
  }
  
  /**
   * debug map for custom ui
   * @param debugMap the debugMap to set
   */
  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }
  
  /**
   * 
   */
  private CustomUiUserQueryConfigBean defaultConfigBean = null;

  /**
   * 
   * @param jsons
   */
  public void parseCustomUiUserQueryConfigBeanJsons(Collection<String> jsons) {
    
    for (String json : GrouperUtil.nonNull(jsons)) {
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean = GrouperUtil.jsonConvertFrom(json, CustomUiUserQueryConfigBean.class);
      
      //keeep track of enabled fields that arent default
      if (customUiUserQueryConfigBean.getEnabled() == null || customUiUserQueryConfigBean.getEnabled()) {
        if (StringUtils.equals("default", customUiUserQueryConfigBean.getVariableToAssign())) {
          defaultConfigBean = customUiUserQueryConfigBean;
        } else {
          if (customUiUserQueryConfigBean.getUserVariable() == null || customUiUserQueryConfigBean.getUserVariable()) {
            customUiUserQueryConfigBeansForUser.add(customUiUserQueryConfigBean);
          }
          if (customUiUserQueryConfigBean.getManagerVariable() != null && customUiUserQueryConfigBean.getManagerVariable()) {
            customUiUserQueryConfigBeansForManager.add(customUiUserQueryConfigBean);
          }
        }
      }
    }
    
  }
  
  /**
   * 
   */
  public void copyDefaultsForConfigBeans() {

    // copy default values
    if (this.defaultConfigBean != null) {
      for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForUser) {

        copyDefaultsForConfigBean(customUiUserQueryConfigBean);
      }
      for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForManager) {

        copyDefaultsForConfigBean(customUiUserQueryConfigBean);
      }
    }
  }

  /**
   * @param customUiUserQueryConfigBean
   */
  public void copyDefaultsForConfigBean(
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean) {
    String userQueryTypeString = customUiUserQueryConfigBean.getUserQueryType();
    CustomUiUserQueryType customUiUserQueryType = CustomUiUserQueryType.valueOfIgnoreCase(userQueryTypeString, true);
    
    for (String fieldName : GrouperUtil.fieldNames(CustomUiUserQueryConfigBean.class, null, false)) {
      
      if (StringUtils.equals(CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE, fieldName)) {
        continue;
      }
      
      if (customUiUserQueryType.optionalFieldNames().contains(fieldName) || customUiUserQueryType.requiredFieldNames().contains(fieldName)) {
        
        // see the value of the configured bean
        Object configuredValue = GrouperUtil.fieldValue(customUiUserQueryConfigBean, fieldName);
        if (configuredValue == null) {
          
          Object defaultValue = GrouperUtil.fieldValue(this.defaultConfigBean, fieldName);
          if (defaultValue != null) {
            GrouperUtil.assignField(customUiUserQueryConfigBean, fieldName, defaultValue);
          }
        }
      }
    }
  }
  
  private CustomUiGrouper customUiGrouperForCache = new CustomUiGrouper();
  
  /**
   * 
   */
  public void cacheGroupObjects() {
    
    List<MultiKey> groupIdAndNames = new ArrayList<MultiKey>();
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForUser) {
      cacheGroupObjects(groupIdAndNames, customUiUserQueryConfigBean);
    }
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForManager) {
      cacheGroupObjects(groupIdAndNames, customUiUserQueryConfigBean);
    }
    if (GrouperUtil.length(groupIdAndNames) > 0) {
      this.customUiGrouperForCache.setCustomUiEngine(this);
      this.customUiGrouperForCache.setDebugMapPrefix("groupCache_");
      this.customUiGrouperForCache.cacheGroups(groupIdAndNames);
    }
  }

  /**
   * @param groupIdAndNames
   * @param customUiUserQueryConfigBean
   */
  public void cacheGroupObjects(List<MultiKey> groupIdAndNames,
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean) {
    if (!StringUtils.isBlank(customUiUserQueryConfigBean.getGroupId()) || !StringUtils.isBlank(customUiUserQueryConfigBean.getGroupName())) {
      MultiKey groupIdAndName = new MultiKey(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName());
      groupIdAndNames.add(groupIdAndName);
    }
  }

  
  /**
   * 
   */
  public void cacheStemObjects() {
    
    List<MultiKey> stemIdAndNames = new ArrayList<MultiKey>();
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForManager) {
      cacheStemObjects(stemIdAndNames, customUiUserQueryConfigBean);
    }
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForUser) {
      cacheStemObjects(stemIdAndNames, customUiUserQueryConfigBean);
    }
    if (GrouperUtil.length(stemIdAndNames) > 0) {
      this.customUiGrouperForCache.setCustomUiEngine(this);
      this.customUiGrouperForCache.setDebugMapPrefix("stemCache_");
      this.customUiGrouperForCache.cacheStems(stemIdAndNames);
    }
  }

  /**
   * @param stemIdAndNames
   * @param customUiUserQueryConfigBean
   */
  public void cacheStemObjects(List<MultiKey> stemIdAndNames,
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean) {
    if (!StringUtils.isBlank(customUiUserQueryConfigBean.getStemId()) || !StringUtils.isBlank(customUiUserQueryConfigBean.getStemName())) {
      MultiKey stemIdAndName = new MultiKey(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName());
      stemIdAndNames.add(stemIdAndName);
    }
  }

  /**
   * 
   */
  public void cacheAttributeDefObjects() {
    
    List<MultiKey> attributeDefIdAndNames = new ArrayList<MultiKey>();
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForUser) {
      cacheAttributeDefObjects(attributeDefIdAndNames, customUiUserQueryConfigBean);
    }
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeansForManager) {
      cacheAttributeDefObjects(attributeDefIdAndNames, customUiUserQueryConfigBean);
    }
    if (GrouperUtil.length(attributeDefIdAndNames) > 0) {
      this.customUiGrouperForCache.setCustomUiEngine(this);
      this.customUiGrouperForCache.setDebugMapPrefix("attributeDefCache_");
      this.customUiGrouperForCache.cacheAttributeDefs(attributeDefIdAndNames);
    }
  }

  /**
   * @param attributeDefIdAndNames
   * @param customUiUserQueryConfigBean
   */
  public void cacheAttributeDefObjects(List<MultiKey> attributeDefIdAndNames,
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean) {
    if (!StringUtils.isBlank(customUiUserQueryConfigBean.getAttributeDefId()) || !StringUtils.isBlank(customUiUserQueryConfigBean.getNameOfAttributeDef())) {
      MultiKey attributeDefIdAndName = new MultiKey(customUiUserQueryConfigBean.getAttributeDefId(), customUiUserQueryConfigBean.getNameOfAttributeDef());
      attributeDefIdAndNames.add(attributeDefIdAndName);
    }
  }

  
  /**
   * @return the customUiGrouperForCache
   */
  public CustomUiGrouper getCustomUiGrouperForCache() {
    return this.customUiGrouperForCache;
  }

  /**
   * 
   */
  public void cacheMembershipObjects() {
    
    Map<MultiKey, Set<String>> sourceIdSubjectIdToGroupNames = new HashMap<MultiKey, Set<String>>();

    for (CustomUiUserType customUiUserType : CustomUiUserType.values()) {
      
      Subject subject = this.subject(customUiUserType);
      MultiKey sourceIdSubjectId = new MultiKey(subject.getSourceId(), subject.getId());
      
      for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans(customUiUserType)) {

        String userQueryTypeString = customUiUserQueryConfigBean.getUserQueryType();
        CustomUiUserQueryType customUiUserQueryType = CustomUiUserQueryType.valueOfIgnoreCase(userQueryTypeString, true);

        if (customUiUserQueryType == CustomUiUserQueryType.grouper) {
          if (!StringUtils.isBlank(customUiUserQueryConfigBean.getGroupId()) || !StringUtils.isBlank(customUiUserQueryConfigBean.getGroupName())) {
            if (StringUtils.isBlank(customUiUserQueryConfigBean.getFieldNames()) || customUiUserQueryConfigBean.getFieldNames().toLowerCase().contains("members")) {
              Group group = retrieveGroupFromCache(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName());
              Set<String> groupNames = sourceIdSubjectIdToGroupNames.get(sourceIdSubjectId);
              if (groupNames == null) {
                groupNames = new HashSet<String>();
                sourceIdSubjectIdToGroupNames.put(sourceIdSubjectId, groupNames);
              }
              groupNames.add(group.getName());
            }
          }
        }
      }
    }
    for (MultiKey sourceIdSubjectId : sourceIdSubjectIdToGroupNames.keySet()) {
      Set<String> groupNames = sourceIdSubjectIdToGroupNames.get(sourceIdSubjectId);
      if (GrouperUtil.length(groupNames) > 0) {
        Subject subject = this.subject(sourceIdSubjectId);
        this.customUiGrouperForCache.setCustomUiEngine(this);
        this.customUiGrouperForCache.setDebugMapPrefix("membershipCache_");
        this.customUiGrouperForCache.cacheMembershipsInGroups(groupNames, subject);
      }
    }
  }

  /**
   * 
   * @param customUiUserType
   * @return subject
   */
  public Subject subject(CustomUiUserType customUiUserType) {
    switch (customUiUserType) {
      case manager:
        return this.subjectManager;
      case user:
        return this.subjectUser;
      default:
        throw new RuntimeException("Not expecting " + customUiUserType);    
    }
  }
  
  /**
   * @param sourceIdSubjectId
   * @return subject
   */
  private Subject subject(MultiKey sourceIdSubjectId) {
    
    if (SubjectHelper.eq(sourceIdSubjectId, this.subjectManager)) {
      return subjectManager;
    }
    if (SubjectHelper.eq(sourceIdSubjectId, this.subjectUser)) {
      return subjectManager;
    }
    throw new RuntimeException("Cant find subject! " + sourceIdSubjectId.getKey(0) + ", " + sourceIdSubjectId.getKey(1) 
        + "," + SubjectHelper.getPretty(this.subjectManager) + ", " + SubjectHelper.getPretty(this.subjectUser));
  }
  
  /**
   * @param customUiUserType
   * @return the list of beans
   */
  private List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeans(CustomUiUserType customUiUserType) {
    switch (customUiUserType) {
      case manager:
        return this.customUiUserQueryConfigBeansForManager;
      case user:
        return this.customUiUserQueryConfigBeansForUser;
      default:
        throw new RuntimeException("Not expecting " + customUiUserType);    
    }
  }

  /**
   * 
   * @param groupId
   * @param groupName
   * @return the group
   */
  public Group retrieveGroupFromCache(String groupId, String groupName) {
    if (!StringUtils.isBlank(groupId)) {
      return this.customUiGrouperForCache.getGroupIdAndNameToGroup().get(groupId);
    }
    if (!StringUtils.isBlank(groupName)) {
      return this.customUiGrouperForCache.getGroupIdAndNameToGroup().get(groupName);
    }
    return null;
  }
  
  /**
   * 
   * @param stemId
   * @param stemName
   * @return the stem
   */
  public Stem retrieveStemFromCache(String stemId, String stemName) {
    if (!StringUtils.isBlank(stemId)) {
      return this.customUiGrouperForCache.getStemIdAndNameToStem().get(stemId);
    }
    if (!StringUtils.isBlank(stemName)) {
      return this.customUiGrouperForCache.getStemIdAndNameToStem().get(stemName);
    }
    return null;
  }
  
  /**
   * 
   * @param attributeDefId
   * @param nameOfAttributeDef
   * @return the attribute def
   */
  public AttributeDef retrieveAttributeDefFromCache(String attributeDefId, String nameOfAttributeDef) {
    if (!StringUtils.isBlank(attributeDefId)) {
      return this.customUiGrouperForCache.getAttributeDefIdAndNameToAttributeDef().get(attributeDefId);
    }
    if (!StringUtils.isBlank(nameOfAttributeDef)) {
      return this.customUiGrouperForCache.getAttributeDefIdAndNameToAttributeDef().get(nameOfAttributeDef);
    }
    return null;
  }
  
  /**
   * group
   */
  private Group group;
  
  /**
   * group
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }
  
  /**
   * group
   * @param group1 the group to set
   */
  public void setGroup(Group group1) {
    this.group = group1;
  }
  
  /**
   * subject
   */
  private Subject subjectManager;
  
  
  
  /**
   * subject
   * @return the subjectManager
   */
  public Subject getSubjectManager() {
    return this.subjectManager;
  }

  
  /**
   * subject
   * @param subjectManager1 the subjectManager to set
   */
  public void setSubjectManager(Subject subjectManager1) {
    this.subjectManager = subjectManager1;
  }

  /**
   * subject
   */
  private Subject subjectUser;
  
  /**
   * subject
   * @return the subject
   */
  public Subject getSubjectUser() {
    return this.subjectUser;
  }

  
  /**
   * subject
   * @param subject1 the subject to set
   */
  public void setSubjectUser(Subject subject1) {
    this.subjectUser = subject1;
  }

  /**
   * process a group for lite ui
   * @param subjectUser1
   * @param subjectManager1
   */
  public void processGroup(final Group group1, final Subject subjectUser1, final Subject subjectManager1) {
    long startedNanos = System.nanoTime();
    this.debugMap.put("group", group1.getName());
    this.debugMap.put("subjectUserId", subjectUser1.getId());
    if (!SubjectHelper.eq(subjectUser1, subjectManager1)) {
      this.debugMap.put("subjectManagerId", subjectUser1.getId());
    }
    
    this.group = group1;
    this.subjectUser = subjectUser1;
    this.subjectManager = subjectManager1;
    try {
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Map<String, Set<String>> attributeDefNameToValues = CustomUiAttributeNames.retrieveAttributeValuesForGroup(group1);
          
          Set<String> userQueryBeans = attributeDefNameToValues.get(CustomUiAttributeNames.retrieveAttributeDefNameUserQueryConfigBeans().getName());
          CustomUiEngine.this.runUserQueries(userQueryBeans);
          
          Set<String> textConfigBeans = attributeDefNameToValues.get(CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName());
          CustomUiEngine.this.parseCustomUiTextConfigBeanJsons(textConfigBeans);
          return null;
        }
      } );
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, group1.getName() + ", " + SubjectUtils.subjectToString(subjectUser1) + ", " + SubjectUtils.subjectToString(subjectManager1));
      throw re;
    } finally {
      this.debugMap.put("processGroupMillis", ((System.nanoTime() - startedNanos)/1000000));
    }
  }
  
  /**
   * 
   * @param jsons
   */
  public void parseCustomUiTextConfigBeanJsons(Collection<String> jsons) {
    
    for (String json : GrouperUtil.nonNull(jsons)) {
      CustomUiTextConfigBean customUiTextConfigBean = GrouperUtil.jsonConvertFrom(json, CustomUiTextConfigBean.class);
      
      //keeep track of enabled fields that arent default
      if (customUiTextConfigBean.getEnabled() == null || customUiTextConfigBean.getEnabled()) {
        
        CustomUiTextType customUiTextType = CustomUiTextType.valueOfIgnoreCase(customUiTextConfigBean.getCustomUiTextType(), true);

        if (customUiTextConfigBean.getDefaultText() != null && customUiTextConfigBean.getDefaultText()) {
          
          if (customUiTextTypeToDefaultCustomUiTextConfigBean.get(customUiTextType) != null) {
            throw new RuntimeException("Cant have multiple defaults for custom ui text: '" + customUiTextType + "'" );
          }
          customUiTextTypeToDefaultCustomUiTextConfigBean.put(customUiTextType, customUiTextConfigBean);
          
        } else {
          
          Set<CustomUiTextConfigBean> customUiTextConfigBeans = this.customUiTextTypeToCustomUiTextConfigBean.get(customUiTextType);
          if (customUiTextConfigBeans == null) {
            customUiTextConfigBeans = new TreeSet<CustomUiTextConfigBean>();
            this.customUiTextTypeToCustomUiTextConfigBean.put(customUiTextType, customUiTextConfigBeans);
          }
          customUiTextConfigBeans.add(customUiTextConfigBean);
        }
      }
    }
    
  }

  /**
   * custom ui text type to custom ui text config beans
   */
  private Map<CustomUiTextType, Set<CustomUiTextConfigBean>> customUiTextTypeToCustomUiTextConfigBean = 
      new HashMap<CustomUiTextType, Set<CustomUiTextConfigBean>>();
  
  /**
   * custom ui text type to custom ui text config beans
   */
  private Map<CustomUiTextType, CustomUiTextConfigBean> customUiTextTypeToDefaultCustomUiTextConfigBean = 
      new HashMap<CustomUiTextType, CustomUiTextConfigBean>();
  
  
  /**
   * 
   * @param jsonUserQueries
   * @param subject 
   */
  public void runUserQueries(Collection<String> jsonUserQueries) {
    
    this.parseCustomUiUserQueryConfigBeanJsons(jsonUserQueries);
    
    this.copyDefaultsForConfigBeans();
    
    this.cacheAttributeDefObjects();
    this.cacheGroupObjects();
    this.cacheMembershipObjects();
    this.cacheStemObjects();
    
    
    this.validateCustomUiUserQueryConfigBeanJsons();
    
    this.evaluateCustomUiUserQueryConfigBeanJsons();
  }

  /**
   * @param substituteMap 
   */
  public void generateUserQueryDisplayBeans(Map<String, Object> substituteMap) {
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : GrouperUtil.nonNull(this.customUiUserQueryConfigBeansForUser)) {

      Map<String, Object> variableMap = new HashMap<String, Object>(GrouperUtil.nonNull(substituteMap));
      
      CustomUiUserQueryDisplayBean customUiUserQueryDisplayBean = new CustomUiUserQueryDisplayBean();
      customUiUserQueryDisplayBean.setVariableName(customUiUserQueryConfigBean.getVariableToAssign());
      
      CustomUiVariableType customUiVariableType = CustomUiVariableType.valueOfIgnoreCase(customUiUserQueryConfigBean.getVariableType(), false);
      customUiVariableType = GrouperUtil.defaultIfNull(customUiVariableType, CustomUiVariableType.BOOLEAN);
      customUiUserQueryDisplayBean.setVariableType(customUiVariableType.name().toLowerCase());
      customUiUserQueryDisplayBean.setOrder(customUiUserQueryConfigBean.getOrder());

      String userQueryTypeString = customUiUserQueryConfigBean.getUserQueryType();
      CustomUiUserQueryType customUiUserQueryType = CustomUiUserQueryType.valueOfIgnoreCase(userQueryTypeString, true);
  
      Group group = this.retrieveGroupFromCache(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName());
      Stem stem = this.retrieveStemFromCache(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName());
      AttributeDef attributeDef = this.retrieveAttributeDefFromCache(customUiUserQueryConfigBean.getAttributeDefId(), 
          customUiUserQueryConfigBean.getNameOfAttributeDef());
      variableMap.put("customUiUserQueryConfigBean", customUiUserQueryConfigBean);

      Object value = this.theUserQueryVariables.get(customUiUserQueryConfigBean.getVariableToAssign());
      
      customUiUserQueryDisplayBean.setVariableValue(customUiVariableType.screenValue(value, variableMap));

      final String label = CustomUiUtil.substituteExpressionLanguage(customUiUserQueryConfigBean.getLabel(), 
          group, null, null, this.subjectUser, variableMap, true);
      customUiUserQueryDisplayBean.setLabel(label);

      customUiUserQueryDisplayBean.setUserQueryType(customUiUserQueryType.label(variableMap));
      String description = customUiUserQueryType.description(this, customUiUserQueryConfigBean, group, this.subjectUser, 
          stem, attributeDef, variableMap);
      customUiUserQueryDisplayBean.setDescription(description);
      
      this.customUiUserQueryDisplayBeans.add(customUiUserQueryDisplayBean);
  
      if (!StringUtils.isBlank(customUiUserQueryConfigBean.getVariableToAssignOnError())) {
        customUiUserQueryDisplayBean = new CustomUiUserQueryDisplayBean();
        
        final String errorLabel = CustomUiUtil.substituteExpressionLanguage(customUiUserQueryConfigBean.getErrorLabel(), 
            group, null, null, this.subjectUser, variableMap, true);
        customUiUserQueryDisplayBean.setLabel(errorLabel);
        
        // set to same order, the variable name will fix that
        customUiUserQueryDisplayBean.setOrder(customUiUserQueryConfigBean.getOrder());

        customUiUserQueryDisplayBean.setVariableValue(CustomUiVariableType.BOOLEAN.screenValue(
            this.theUserQueryVariables.get(customUiUserQueryConfigBean.getVariableToAssignOnError()), variableMap));

        customUiUserQueryDisplayBean.setUserQueryType(customUiUserQueryType.label(variableMap));
        customUiUserQueryDisplayBean.setVariableName(customUiUserQueryConfigBean.getVariableToAssignOnError());
        customUiUserQueryDisplayBean.setVariableType(CustomUiVariableType.BOOLEAN.name().toLowerCase());
        this.customUiUserQueryDisplayBeans.add(customUiUserQueryDisplayBean);
      }
  
    }
  }

  /**
   * variable name alphanumeric and underscore starts with char
   */
  private static Pattern variableNamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
  
  /**
   * 
   */
  public void validateCustomUiUserQueryConfigBeanJsons() {
    for (CustomUiUserType customUiUserType : CustomUiUserType.values()) {
      
      Subject subject  = this.subject(customUiUserType);
      for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans(customUiUserType)) {
        try {
          String userQueryTypeString = customUiUserQueryConfigBean.getUserQueryType();
          CustomUiUserQueryType customUiUserQueryType = CustomUiUserQueryType.valueOfIgnoreCase(userQueryTypeString, true);
          Group group = this.retrieveGroupFromCache(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName());
          Stem stem = this.retrieveStemFromCache(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName());
          AttributeDef attributeDef = this.retrieveAttributeDefFromCache(customUiUserQueryConfigBean.getAttributeDefId(), customUiUserQueryConfigBean.getNameOfAttributeDef());
    
          // check optional and require fields
          for (String fieldName : GrouperUtil.fieldNames(CustomUiUserQueryConfigBean.class, null, false)) {
            
            Object configuredValue = GrouperUtil.fieldValue(customUiUserQueryConfigBean, fieldName);
            if (configuredValue == null) {
              continue;
            }
    
            if (StringUtils.equals(CustomUiUserQueryConfigBean.FIELD_USER_QUERY_TYPE, fieldName) 
                || StringUtils.equals(CustomUiUserQueryConfigBean.FIELD_VARIABLE_TO_ASSIGN, fieldName)) {
              continue;
            }
            
            if (customUiUserQueryType.optionalFieldNames().contains(fieldName) || customUiUserQueryType.requiredFieldNames().contains(fieldName)) {
              continue;
            }
    
            throw new RuntimeException("Field '" + fieldName + "' is not valid for user query: " + customUiUserQueryConfigBean.toString());
          }
          
          {
            String variableToAssign = customUiUserQueryConfigBean.getVariableToAssign();
            if (!StringUtils.isBlank(variableToAssign)) {
              if (!variableToAssign.startsWith("cu_")) {
                throw new RuntimeException("variableToAssign must start with 'cu_', '" + variableToAssign + "'");
              }
              if (!variableNamePattern.matcher(variableToAssign).matches()) {
                throw new RuntimeException("variableToAssign must start with a char, then me alphanumeric with underscores only, '" + variableToAssign + "'");
              }
            }
          }
          
          {
            String variableToAssignOnError = customUiUserQueryConfigBean.getVariableToAssignOnError();
    
            if (!StringUtils.isBlank(variableToAssignOnError)) {
              if (!variableToAssignOnError.startsWith("cu_")) {
                throw new RuntimeException("variableToAssignOnError must start with 'cu_', '" + variableToAssignOnError + "'");
              }
              if (!variableNamePattern.matcher(variableToAssignOnError).matches()) {
                throw new RuntimeException("variableToAssignonError must start with a char, then me alphanumeric with underscores only, '" + variableToAssignOnError + "'");
              }
              if (StringUtils.isBlank(customUiUserQueryConfigBean.getErrorLabel())) {
                throw new RuntimeException("If you configure variableToAssignOnError then you must set an error label");
              }
            }
          }
    
          try {
            customUiUserQueryType.validate(customUiUserQueryConfigBean, group, subject, stem, attributeDef);
          } catch (RuntimeException re) {
            GrouperUtil.injectInException(re, customUiUserQueryConfigBean.toString());
            throw re;
          }
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, customUiUserQueryConfigBean.toString());
          throw re;
        }
      }
    }
  }
  
  /**
   * 
   * @param customUiUserType
   * @return the variable map
   */
  public Map<String, Object> userQueryVariables(CustomUiUserType customUiUserType) {
    switch (customUiUserType) {
      case manager:
        return this.theUserQueryVariables;
      case user:
        return this.managerQueryVariables;
      default:
        throw new RuntimeException("Not expecting " + customUiUserType);    
    }
  }
  
  /**
   * 
   */
  public void evaluateCustomUiUserQueryConfigBeanJsons() {
    for (CustomUiUserType customUiUserType : CustomUiUserType.values()) {
      Subject subject = this.subject(customUiUserType);
      for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans(customUiUserType)) {
        String userQueryTypeString = customUiUserQueryConfigBean.getUserQueryType();
        CustomUiUserQueryType customUiUserQueryType = CustomUiUserQueryType.valueOfIgnoreCase(userQueryTypeString, true);
        Group group = this.retrieveGroupFromCache(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName());
        Stem stem = this.retrieveStemFromCache(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName());
        AttributeDef attributeDef = this.retrieveAttributeDefFromCache(customUiUserQueryConfigBean.getAttributeDefId(), customUiUserQueryConfigBean.getNameOfAttributeDef());
  
        String variableToAssignOnError = customUiUserQueryConfigBean.getVariableToAssignOnError();
        
        if (!StringUtils.isBlank(variableToAssignOnError)) {
          this.userQueryVariables(customUiUserType).put(variableToAssignOnError, false);
        }
        
        // check optional and require fields
        try {
          Object result = customUiUserQueryType.evaluate(this, customUiUserQueryConfigBean, group, subject, stem, attributeDef);
          String variableToAssign = customUiUserQueryConfigBean.getVariableToAssign();
          this.userQueryVariables(customUiUserType).put(variableToAssign, result);
        } catch (RuntimeException re) {
          String error = "Error evaluating: " + customUiUserQueryConfigBean;
          Log.error(error, re);
          if (!StringUtils.isBlank(variableToAssignOnError)) {
            if (this.error != null) {
              this.error += "\n";
            }
            this.error += error + "\n" + GrouperUtil.getFullStackTrace(re);
            this.userQueryVariables(customUiUserType).put(variableToAssignOnError, true);
          } else {
            throw new RuntimeException(error, re);
          }
        }
      }
    }
  }

  /**
   * error
   */
  private String error;
  
  /**
   * error
   * @return the error
   */
  public String getError() {
    return this.error;
  }

  
  /**
   * error
   * @param error the error to set
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * user query names and values
   */
  private Map<String, Object> theUserQueryVariables = new HashMap<String, Object>();
  
  /**
   * user query names and values
   */
  private Map<String, Object> managerQueryVariables = new HashMap<String, Object>();
  
  /**
   * 
   */
  public CustomUiEngine() {
  }

  /**
   * results of text calls
   */
  private List<CustomUiTextResult> customUiTextResults = new ArrayList<CustomUiTextResult>();
  
  /**
   * results of text calls
   * @return the customUiTextResults
   */
  public List<CustomUiTextResult> getCustomUiTextResults() {
    return this.customUiTextResults;
  }
  
  /**
   * results of text calls
   * @param customUiTextResults1 the customUiTextResults to set
   */
  public void setCustomUiTextResults(List<CustomUiTextResult> customUiTextResults1) {
    this.customUiTextResults = customUiTextResults1;
  }

  /**
   * @param customUiUserType
   * @param customUiTextType
   * @param substituteMap 
   * @return the best text
   */
  public Object findBestText(CustomUiUserType customUiUserType, CustomUiTextType customUiTextType, Map<String, Object> substituteMap) {
    long startNanos = System.nanoTime();
    
    boolean assignedThreadLocal = false;
    if (threadLocalCustomUiEngine.get() == null) {
      threadLocalCustomUiEngine.set(this);
      assignedThreadLocal = true;
    }
    try {
      final Subject subject = this.subject(customUiUserType);
      final Map<String, Object> userQueryVariables = this.userQueryVariables(customUiUserType);

      Set<CustomUiTextConfigBean> customUiTextConfigBeans = this.customUiTextTypeToCustomUiTextConfigBean.get(customUiTextType);
      
      StringBuilder resultText = new StringBuilder();
  
      if (substituteMap == null) {
        substituteMap = new HashMap<String, Object>();
      }
      substituteMap.putAll(GrouperUtil.nonNull(userQueryVariables));
      
      boolean foundSomething = false;
      for (CustomUiTextConfigBean customUiTextConfigBean : GrouperUtil.nonNull(customUiTextConfigBeans)) {
        
        try {
          String script = customUiTextConfigBean.getScript();
          boolean shouldDisplay = true;
          
          if (!StringUtils.isBlank(script)) {
            
            String shouldDisplayString = CustomUiUtil.substituteExpressionLanguage(script, this.group, null, null, subject, userQueryVariables);
            
            shouldDisplay = GrouperUtil.booleanValue(shouldDisplayString);
          }
          if (shouldDisplay) {
            foundSomething = true;
            
            String theText = customUiTextConfigBean.getText();
            theText = CustomUiUtil.substituteExpressionLanguage(theText, this.group, null, null, subject, substituteMap);

            CustomUiTextResult customUiTextResult = new CustomUiTextResult();
            customUiTextResult.setCustomUiTextConfigBean(customUiTextConfigBean);
            customUiTextResult.setCustomUiTextType(customUiTextType);
            customUiTextResult.setTextResult(theText);
            this.customUiTextResults.add(customUiTextResult);
            
            resultText.append(theText);
            if (customUiTextConfigBean.getEndIfMatches() != null && customUiTextConfigBean.getEndIfMatches()) {
              return resultText.toString();
            }
          }

        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, customUiTextConfigBean.toString());
          throw re;
        }
      }
      
      if (foundSomething) {
        return resultText.toString();
      }
      
      CustomUiTextConfigBean customUiTextConfigBean = this.customUiTextTypeToDefaultCustomUiTextConfigBean.get(customUiTextType);
      
      if (customUiTextConfigBean != null) {
        String theText = customUiTextConfigBean.getText();
        theText = CustomUiUtil.substituteExpressionLanguage(theText, this.group, null, null, subject, substituteMap);
        return theText;
      }
      return "";
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "key: '" + customUiTextType.name() + "'");
      throw re;
    } finally {
      if (assignedThreadLocal) {
        threadLocalCustomUiEngine.remove();
      }
      this.debugMap.put("findText_" + customUiTextType.name() + "_millis", (System.nanoTime()-startNanos)/1000000);
    }
  }

  /**
   * thread local custom ui engine
   */
  private static ThreadLocal<CustomUiEngine> threadLocalCustomUiEngine = new InheritableThreadLocal<CustomUiEngine>();

  /**
   * thread local custom ui engine
   * @return custom ui engine
   */
  public static CustomUiEngine threadLocalCustomUiEngine() {
    return threadLocalCustomUiEngine.get();
  }
  
}
