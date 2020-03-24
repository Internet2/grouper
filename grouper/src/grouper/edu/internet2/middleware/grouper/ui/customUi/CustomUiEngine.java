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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import jline.internal.Log;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
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
  private List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeans = new ArrayList<CustomUiUserQueryConfigBean>();
  
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
          customUiUserQueryConfigBeans.add(customUiUserQueryConfigBean);
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
      for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans) {

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
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans) {
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
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans) {
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
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans) {
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

    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans()) {

      Subject subject = subject(customUiUserQueryConfigBean);
      
      MultiKey sourceIdSubjectId = new MultiKey(subject.getSourceId(), subject.getId());
      
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
   */
  public Subject subject(CustomUiUserQueryConfigBean customUiUserQueryConfigBean) {

    if (customUiUserQueryConfigBean.getForLoggedInUser() != null 
        && customUiUserQueryConfigBean.getForLoggedInUser()) {
      return this.subjectLoggedIn;
    }
    return this.subjectOperatedOn;
  }
  
  /**
   * @param sourceIdSubjectId
   * @return subject
   */
  private Subject subject(MultiKey sourceIdSubjectId) {
    
    if (SubjectHelper.eq(sourceIdSubjectId, this.subjectLoggedIn)) {
      return subjectLoggedIn;
    }
    if (SubjectHelper.eq(sourceIdSubjectId, this.subjectOperatedOn)) {
      return subjectOperatedOn;
    }
    throw new RuntimeException("Cant find subject! " + sourceIdSubjectId.getKey(0) + ", " + sourceIdSubjectId.getKey(1) 
        + "," + SubjectHelper.getPretty(this.subjectLoggedIn) + ", " + SubjectHelper.getPretty(this.subjectOperatedOn));
  }
  
  /**
   * @param customUiUserType
   * @return the list of beans
   */
  private List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeans() {
    return this.customUiUserQueryConfigBeans;
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
  private Subject subjectLoggedIn;
  
  
  
  /**
   * subject
   * @return the subjectManager
   */
  public Subject getSubjectLoggedIn() {
    return this.subjectLoggedIn;
  }

  
  /**
   * subject
   * @param subjectManager1 the subjectManager to set
   */
  public void setSubjectLoggedIn(Subject subjectManager1) {
    this.subjectLoggedIn = subjectManager1;
  }

  /**
   * subject
   */
  private Subject subjectOperatedOn;
  
  /**
   * subject
   * @return the subjectgetSubjectOperatedOnc Subject getSubjectOperatedOn() {
    return this.subjectOperatedOn;
  }

  
  /**
   * subject
   * @param subject1 thesetSubjectOperatedOnt
   */
  public void setSubjectOperatedOn(Subject subject1) {
    this.subjectOperatedOn = subject1;
  }

  /**
   * process a group for lite ui
   * @param subjectOperatedOn1
   * @param subjectLoggedIn1
   */
  public void processGroup(final Group group1, final Subject subjectLoggedIn1, final Subject subjectOperatedOn1) {
    long startedNanos = System.nanoTime();
    this.debugMap.put("group", group1.getName());
    this.debugMap.put("subjectUserId", subjectOperatedOn1.getId());
    if (!SubjectHelper.eq(subjectOperatedOn1, subjectLoggedIn1)) {
      this.debugMap.put("subjectManagerId", subjectOperatedOn1.getId());
    }
    
    this.group = group1;
    this.subjectOperatedOn = subjectOperatedOn1;
    this.subjectLoggedIn = subjectLoggedIn1;
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
      GrouperUtil.injectInException(re, group1.getName() + ", " + SubjectUtils.subjectToString(subjectOperatedOn1) + ", " + SubjectUtils.subjectToString(subjectLoggedIn1));
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
            throw new RuntimeException("Cant have multiple defaults for custom ui text: '" + customUiTextType + "', " 
                + customUiTextTypeToDefaultCustomUiTextConfigBean.get(customUiTextType).getIndex() + ", " 
                + customUiTextTypeToDefaultCustomUiTextConfigBean.get(customUiTextType).getText() + ", " 
                + ", " + customUiTextConfigBean.getIndex()
                + ", " + customUiTextConfigBean.getText()
                );
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
      new TreeMap<CustomUiTextType, Set<CustomUiTextConfigBean>>();
  
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
    
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : GrouperUtil.nonNull(this.customUiUserQueryConfigBeans)) {

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

      customUiUserQueryDisplayBean.setForLoggedInUser(CustomUiVariableType.BOOLEAN.screenValue(
          GrouperUtil.booleanValue(customUiUserQueryConfigBean.getForLoggedInUser(), false), variableMap));
      customUiUserQueryDisplayBean.setForLoggedInUserBoolean(
          GrouperUtil.booleanValue(customUiUserQueryConfigBean.getForLoggedInUser(), false));

      final String label = CustomUiUtil.substituteExpressionLanguage(customUiUserQueryConfigBean.getLabel(), 
          group, null, null, this.subjectOperatedOn, variableMap, true);
      customUiUserQueryDisplayBean.setLabel(label);

      customUiUserQueryDisplayBean.setUserQueryType(customUiUserQueryType.label(variableMap));
      String description = customUiUserQueryType.description(this, customUiUserQueryConfigBean, group, this.subjectOperatedOn, 
          stem, attributeDef, variableMap);
      customUiUserQueryDisplayBean.setDescription(description);
      
      this.customUiUserQueryDisplayBeans.add(customUiUserQueryDisplayBean);
  
      if (!StringUtils.isBlank(customUiUserQueryConfigBean.getVariableToAssignOnError())) {
        customUiUserQueryDisplayBean = new CustomUiUserQueryDisplayBean();
        
        final String errorLabel = CustomUiUtil.substituteExpressionLanguage(customUiUserQueryConfigBean.getErrorLabel(), 
            group, null, null, this.subjectOperatedOn, variableMap, true);
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
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans()) {
      Subject subject  = this.subject(customUiUserQueryConfigBean);
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
  
  /**
   * 
   * @param customUiUserType
   * @return the variable map
   */
  public Map<String, Object> userQueryVariables() {
    return this.theUserQueryVariables;
  }
  
  /**
   * 
   */
  public void evaluateCustomUiUserQueryConfigBeanJsons() {
    for (CustomUiUserQueryConfigBean customUiUserQueryConfigBean : this.customUiUserQueryConfigBeans()) {
      Subject subject = this.subject(customUiUserQueryConfigBean);
      String userQueryTypeString = customUiUserQueryConfigBean.getUserQueryType();
      CustomUiUserQueryType customUiUserQueryType = CustomUiUserQueryType.valueOfIgnoreCase(userQueryTypeString, true);
      Group group = this.retrieveGroupFromCache(customUiUserQueryConfigBean.getGroupId(), customUiUserQueryConfigBean.getGroupName());
      Stem stem = this.retrieveStemFromCache(customUiUserQueryConfigBean.getStemId(), customUiUserQueryConfigBean.getStemName());
      AttributeDef attributeDef = this.retrieveAttributeDefFromCache(customUiUserQueryConfigBean.getAttributeDefId(), customUiUserQueryConfigBean.getNameOfAttributeDef());

      String variableToAssignOnError = customUiUserQueryConfigBean.getVariableToAssignOnError();
      
      if (!StringUtils.isBlank(variableToAssignOnError)) {
        this.userQueryVariables().put(variableToAssignOnError, false);
      }
      
      // check optional and require fields
      try {
        Object result = customUiUserQueryType.evaluate(this, customUiUserQueryConfigBean, group, subject, stem, attributeDef);
        String variableToAssign = customUiUserQueryConfigBean.getVariableToAssign();
        this.userQueryVariables().put(variableToAssign, result);
      } catch (RuntimeException re) {
        String error = "Error evaluating: " + customUiUserQueryConfigBean;
        Log.error(error, re);
        if (!StringUtils.isBlank(variableToAssignOnError)) {
          if (this.error != null) {
            this.error += "\n";
          }
          this.error += error + "\n" + GrouperUtil.getFullStackTrace(re);
          this.userQueryVariables().put(variableToAssignOnError, true);
        } else {
          throw new RuntimeException(error, re);
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
   * 
   */
  public CustomUiEngine() {
  }

  /**
   * results of text calls
   */
  private List<CustomUiTextResult> customUiTextResults = new ArrayList<CustomUiTextResult>();
  
  /**
   * all texts to show on screen for troubleshooting
   */
  private List<CustomUiTextResult> customUiTextResultsAll = null;
  
  
  /**
   * @return the customUiTextResultsAll
   */
  public List<CustomUiTextResult> getCustomUiTextResultsAll() {
    return this.customUiTextResultsAll;
  }

  /**
   * @param substituteMap 
   */
  public void generateCustomUiTextResultsAll(Map<String, Object> substituteMap) {
    if (this.customUiTextResultsAll == null) {

      long startNanos = System.nanoTime();
      
      boolean assignedThreadLocal = false;
      if (threadLocalCustomUiEngine.get() == null) {
        threadLocalCustomUiEngine.set(this);
        assignedThreadLocal = true;
      }

      try {
        this.customUiTextResultsAll = new ArrayList<CustomUiTextResult>();
        
        for (CustomUiTextType customUiTextType : customUiTextTypeToCustomUiTextConfigBean.keySet()) {
          Set<CustomUiTextConfigBean> customUiTextConfigBeans = new LinkedHashSet(customUiTextTypeToCustomUiTextConfigBean.get(customUiTextType));

          {
            CustomUiTextConfigBean customUiTextConfigBean = this.customUiTextTypeToDefaultCustomUiTextConfigBean.get(customUiTextType);
            if (customUiTextConfigBean != null) {
              customUiTextConfigBeans.add(customUiTextConfigBean);
            }
          }
          for (CustomUiTextConfigBean customUiTextConfigBean : customUiTextConfigBeans) {
            
            final Subject subject = this.subjectOperatedOn;
            
            final Map<String, Object> userQueryVariables = this.userQueryVariables();

            userQueryVariables.put("subjectLoggedIn", this.subjectLoggedIn);
            
            if (substituteMap == null) {
              substituteMap = new HashMap<String, Object>();
            }
            substituteMap.putAll(GrouperUtil.nonNull(userQueryVariables));

            try {
              String script = customUiTextConfigBean.getScript();
              boolean shouldDisplay = true;
              
              if (!StringUtils.isBlank(script)) {
                
                String shouldDisplayString = CustomUiUtil.substituteExpressionLanguage(script, this.group, null, null, subject, userQueryVariables);
                
                shouldDisplay = GrouperUtil.booleanValue(shouldDisplayString);
              }
                
              String theText = customUiTextConfigBean.getText();
              theText = CustomUiUtil.substituteExpressionLanguage(theText, this.group, null, null, subject, substituteMap);

              CustomUiTextResult customUiTextResult = new CustomUiTextResult();
              customUiTextResult.setCustomUiTextConfigBean(customUiTextConfigBean);
              customUiTextResult.setCustomUiTextType(customUiTextType);
              customUiTextResult.setTextResult(theText);
              if (customUiTextConfigBean.getDefaultText() != null && customUiTextConfigBean.getDefaultText()) {
                customUiTextResult.setTheDefault(CustomUiVariableType.BOOLEAN.screenValue(true, substituteMap));
              }
              if (customUiTextConfigBean.getEndIfMatches() != null && customUiTextConfigBean.getEndIfMatches()) {
                customUiTextResult.setEndIfMatches(CustomUiVariableType.BOOLEAN.screenValue(true, substituteMap));
              }
              customUiTextResult.setScriptResult(CustomUiVariableType.BOOLEAN.screenValue(shouldDisplay, substituteMap));
              this.customUiTextResultsAll.add(customUiTextResult);

            } catch (RuntimeException re) {
              GrouperUtil.injectInException(re, customUiTextConfigBean.toString());
              CustomUiTextResult customUiTextResult = new CustomUiTextResult();
              customUiTextResult.setCustomUiTextConfigBean(customUiTextConfigBean);
              customUiTextResult.setCustomUiTextType(customUiTextType);
              customUiTextResult.setTextResult(GrouperUtil.getFullStackTrace(re));
              this.customUiTextResultsAll.add(customUiTextResult);
            }
          }   
        }
      } finally {
        if (assignedThreadLocal) {
          threadLocalCustomUiEngine.remove();
        }
        this.debugMap.put("generateCustomUiTextResultsAll_millis", (System.nanoTime()-startNanos)/1000000);
      }
    }
  }

  
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
   * @param substituteMap 
   */
  public void sendEmail(Map<String, Object> substituteMap) {
    
    boolean emailToUser = GrouperUtil.booleanValue(findBestText(CustomUiTextType.emailToUser, substituteMap), false);
    
    this.debugMap.put("emailToUser", emailToUser);
    
    if (!emailToUser) {
      return;
    }
    
    String emailTo = GrouperEmailUtils.getEmail(this.subjectOperatedOn);

    final String bccGroup = findBestText(CustomUiTextType.emailBccGroupName, substituteMap);

    String bccAddresses = null;
    
    this.debugMap.put("emailAddress", emailTo);

    if (!StringUtils.isBlank(bccGroup)) {
      
      bccAddresses = GrouperEmailUtils.retrieveEmailAddressesOrFromCache(bccGroup);
    }

    if (StringUtils.isBlank(emailTo) && StringUtils.isBlank(bccAddresses)) {
      return;
    }

    if (StringUtils.isBlank(emailTo)) {
      emailTo = bccAddresses;
      bccAddresses = null;
    }
    
    final String emailSubject = findBestText(CustomUiTextType.emailSubject, substituteMap);
    final String emailBody = findBestText(CustomUiTextType.emailBody, substituteMap);

    new GrouperEmail().setBcc(bccAddresses).setBody(emailBody).setSubject(emailSubject).setTo(emailTo).send();

  }

  /**
   * @param customUiTextType
   * @param substituteMap 
   * @return the best text
   */
  public String findBestText(CustomUiTextType customUiTextType, Map<String, Object> substituteMap) {
    long startNanos = System.nanoTime();
    
    boolean assignedThreadLocal = false;
    if (threadLocalCustomUiEngine.get() == null) {
      threadLocalCustomUiEngine.set(this);
      assignedThreadLocal = true;
    }
    try {

      final Subject subject = this.subjectOperatedOn;
      
      final Map<String, Object> userQueryVariables = this.userQueryVariables();

      userQueryVariables.put("subjectLoggedIn", this.subjectLoggedIn);
      
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

        CustomUiTextResult customUiTextResult = new CustomUiTextResult();
        customUiTextResult.setCustomUiTextConfigBean(customUiTextConfigBean);
        customUiTextResult.setCustomUiTextType(customUiTextType);
        customUiTextResult.setTextResult(theText);
        this.customUiTextResults.add(customUiTextResult);

        return theText;
      }
      
      CustomUiTextResult customUiTextResult = new CustomUiTextResult();
      customUiTextResult.setCustomUiTextType(customUiTextType);
      customUiTextResult.setTextResult(null);
      this.customUiTextResults.add(customUiTextResult);

      return "";
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "key: '" + customUiTextType.name() + "'");
      throw re;
    } finally {
      if (assignedThreadLocal) {
        threadLocalCustomUiEngine.remove();
      }
      //this.debugMap.put("findText_" + customUiTextType.name() + "_millis", (System.nanoTime()-startNanos)/1000000);
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
