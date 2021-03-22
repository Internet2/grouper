package edu.internet2.middleware.grouper.app.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.CheckboxValueDriver;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.GrouperUtilElSafe;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class GrouperConfigurationModuleBase {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperConfigurationModuleBase.class);
  
  /**
   * config id of the daemon
   */
  private String configId;
  
  /**
   * is the config enabled or not
   * @return
   */
  public boolean isEnabled() {
    return true;
  }
  
  /**
   * property suffix that will be used to identify the config eg class
   * @return
   */
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return null;
  }
  
  /**
   * property value that identifies the config. Suffix is required for this property to be useful. eg: edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync
   * @return
   */
  public String getPropertyValueThatIdentifiesThisConfig() {
    return null;
  }
  
  /**
   * config id that identified this config. either suffix and value or getConfigIdThatIdentifiesThisConfig is required, not both. eg: personLdap
   * @return
   */
  public String getConfigIdThatIdentifiesThisConfig() {
    return null;
  }
  
  /**
   * extra configs that dont match the regex or prefix
   */
  protected Set<String> extraConfigKeys = new LinkedHashSet<String>();

  public Set<String> retrieveExtraConfigKeys() {
    return extraConfigKeys;
  }
  
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<GrouperConfigurationModuleBase> retrieveAllConfigurationTypesHelper(Set<String> classNames) {
    
    List<GrouperConfigurationModuleBase> result = new ArrayList<GrouperConfigurationModuleBase>();
    
    for (String className: classNames) {

      try {
        Class<GrouperConfigurationModuleBase> configClass = (Class<GrouperConfigurationModuleBase>) GrouperUtil.forName(className);
        GrouperConfigurationModuleBase config = GrouperUtil.newInstance(configClass);
        result.add(config);
      } catch (Exception e) {
        //TODO ignore for now. for external systems we might not have all the classes on the classpath
      }
    }
    
    return result;
  }
  
  /**
   * get the index of the label
   */
  private static Pattern iPattern = Pattern.compile("^(.*)\\.([0-9]+).*$");

  
  /**
   * format indexes to be the number or a label (take special logic)
   * @param grouperConfigModule
   * @param realConfigSuffix
   * @param hasIconfigSuffix
   * @param label
   * @return the formatted label
   */
  public String formatIndexes(String realConfigSuffix, boolean hasIconfigSuffix,
      String label) {
    // lets see if we need to subsitute
    if (hasIconfigSuffix) {
      Matcher matcher = iPattern.matcher(realConfigSuffix);
      if (!matcher.matches()) {
        throw new RuntimeException("Cant find index of label! '" + realConfigSuffix + "'");
      }
      String preIndex = matcher.group(1);
      int index = GrouperUtil.intValue(matcher.group(2));
      
      String name = null;
      // GSH templates
      if (StringUtils.equals(preIndex, "input")) {
        name = (String)this.retrieveObjectValueSubstituteMap().get("input." + index + ".name");
      } else if (StringUtils.equals(preIndex, "targetGroupAttribute")) {
        // Group field name
        boolean isFieldElseAttribute = GrouperUtil.booleanValue(this.retrieveObjectValueSubstituteMap().get("targetGroupAttribute." + index + ".isFieldElseAttribute"), false);
        String theName = isFieldElseAttribute ? 
          (String)this.retrieveObjectValueSubstituteMap().get("targetGroupAttribute." + index + ".fieldName")
          : (String)this.retrieveObjectValueSubstituteMap().get("targetGroupAttribute." + index + ".name");
         
        if (StringUtils.isBlank(theName)) {
          theName = Integer.toString(index+1);
        }
        name = this.getCacheGroupAttributePrefix() + " " + (isFieldElseAttribute ? this.getCacheFieldPrefix()
            : this.getCacheAttributePrefix()) + " " + theName;
        
      } else if (StringUtils.equals(preIndex, "targetEntityAttribute")) {
        // Entity field name
        boolean isFieldElseAttribute = GrouperUtil.booleanValue(this.retrieveObjectValueSubstituteMap().get("targetEntityAttribute." + index + ".isFieldElseAttribute"), false);
        String theName = isFieldElseAttribute ? 
            (String)this.retrieveObjectValueSubstituteMap().get("targetEntityAttribute." + index + ".fieldName")
            : (String)this.retrieveObjectValueSubstituteMap().get("targetEntityAttribute." + index + ".name");

        if (StringUtils.isBlank(theName)) {
          theName = Integer.toString(index+1);
        }
          
        name = this.getCacheEntityAttributePrefix() + " " + (isFieldElseAttribute ? this.getCacheFieldPrefix()
             : this.getCacheAttributePrefix()) + " " + theName;

      } else if (StringUtils.equals(preIndex, "metadata")) {
        name = (String)this.retrieveObjectValueSubstituteMap().get("metadata." + index + ".name");
      } else if (StringUtils.equals(preIndex, "attribute")) {
        name = (String)this.retrieveObjectValueSubstituteMap().get("attribute." + index + ".name");
      }
            
      if (!StringUtils.isBlank(name)) {
        label = StringUtils.replace(label, "__i__", name);
        label = StringUtils.replace(label, "__i+1__", name);
      } else {
      
        label = StringUtils.replace(label, "__i__", Integer.toString(index));
        label = StringUtils.replace(label, "__i+1__", Integer.toString(index+1));
      }
    }
    return label;
  }


  
  /**
   * get all configurations configured for this type
   * @return
   */
  protected List<GrouperConfigurationModuleBase> listAllConfigurationsOfThisType() {
    
    List<GrouperConfigurationModuleBase> result = new ArrayList<GrouperConfigurationModuleBase>();
    
    for (String configId : this.retrieveConfigurationConfigIds()) {
      
      @SuppressWarnings("unchecked")
      Class<GrouperConfigurationModuleBase> theClass = (Class<GrouperConfigurationModuleBase>)this.getClass();
      GrouperConfigurationModuleBase config = GrouperUtil.newInstance(theClass);
      config.setConfigId(configId);
      result.add(config);
    }
    
    return result;
  }
  
  /**
   * list of configured systems
   * @return
   */
  public static List<GrouperConfigurationModuleBase> retrieveAllConfigurations(Set<String> classNames) {
    
    List<GrouperConfigurationModuleBase> result = new ArrayList<GrouperConfigurationModuleBase>();
    
    for (String className: classNames) {
      try {        
        Class<GrouperConfigurationModuleBase> configClass = (Class<GrouperConfigurationModuleBase>) GrouperUtil.forName(className);
        GrouperConfigurationModuleBase config = GrouperUtil.newInstance(configClass);
        result.addAll(config.listAllConfigurationsOfThisType());
      } catch(Exception e) {
        //TODO ignore for now. for external systems we might not have all the classes on the classpath
      }
    }
    
    return result;
  }
  
  /**
   * call retrieveAttributes() to get this
   */
  protected Map<String, GrouperConfigurationModuleAttribute> attributeCache = null;
  
  /**
   * config id
   * @return
   */
  public String getConfigId() {
    return configId;
  }
  
  /**
   * config id
   * @param configId
   */
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  /**
   * validations to run before saving values into db
   * @param isInsert
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    if (isInsert) {
      if (this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
        validationErrorsToDisplay.put("#configId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdUsed"));
      }
      
      if (!isMultiple()) {
        validationErrorsToDisplay.put("#configId", GrouperTextContainer.textOrNull("grouperConfigurationValidationNotMultiple"));
      }
    }
    
    if (isMultiple()) {
      Pattern configIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
      if (!configIdPattern.matcher(this.getConfigId()).matches()) {
        validationErrorsToDisplay.put("#configId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdInvalid"));
      }
    }
    
    
    // first check if checked the el checkbox then make sure there's a script there
    {
      boolean foundElRequiredError = false;
      for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
        
        if (grouperConfigModuleAttribute.isExpressionLanguage() && StringUtils.isBlank(grouperConfigModuleAttribute.getExpressionLanguageScript())) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperConfigModuleAttribute.getLabel());
          validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull("grouperConfigurationValidationElRequired"));
          GrouperTextContainer.resetThreadLocalVariableMap();
          foundElRequiredError = true;
        }
        
      }
      if (foundElRequiredError) {
        return;
      }
    }
    
    // types
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
      
      GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperConfigModuleAttribute.getLabel());
      try {
        
      ConfigItemMetadataType configItemMetadataType = grouperConfigModuleAttribute.getConfigItemMetadata().getValueType();
      
      String value = null;
      
      try {
        value = grouperConfigModuleAttribute.getEvaluatedValueForValidation();
      } catch (UnsupportedOperationException uoe) {
        // ignore, it will get validated in the post-save
        continue;
      }
      
      // required
      if (StringUtils.isBlank(value)) {
        if (grouperConfigModuleAttribute.getConfigItemMetadata().isRequired() && grouperConfigModuleAttribute.isShow()) {

          validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull("grouperConfigurationValidationRequired"));
        }
        
        continue;
      }
      String[] valuesToValidate = null;
     
      if (grouperConfigModuleAttribute.getConfigItemMetadata().isMultiple()) {
        valuesToValidate = GrouperUtil.splitTrim(value, ",");
      } else {
        valuesToValidate = new String[] {value};
      }

      for (String theValue : valuesToValidate) {
        
        // validate types
        String externalizedTextKey = configItemMetadataType.validate(theValue);
        if (StringUtils.isNotBlank(externalizedTextKey)) {
          
          validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull(externalizedTextKey));
          
        } else {
          String mustExtendClass = grouperConfigModuleAttribute.getConfigItemMetadata().getMustExtendClass();
          if (StringUtils.isNotBlank(mustExtendClass)) {
            
            Class mustExtendKlass = GrouperUtil.forName(mustExtendClass);
            Class childClass = GrouperUtil.forName(theValue);
            
            if (!mustExtendKlass.isAssignableFrom(childClass)) {
              
              String error = GrouperTextContainer.textOrNull("grouperConfigurationValidationDoesNotExtendClass");
              error = error.replace("$$mustExtendClass$$", mustExtendClass);
              
              validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), error);
            }
          }
          
          String mustImplementInterface = grouperConfigModuleAttribute.getConfigItemMetadata().getMustImplementInterface();
          if (StringUtils.isNotBlank(mustImplementInterface)) {
            
            Class mustImplementInterfaceClass = GrouperUtil.forName(mustImplementInterface);
            Class childClass = GrouperUtil.forName(theValue);
            
            if (!mustImplementInterfaceClass.isAssignableFrom(childClass)) {
              
              String error = GrouperTextContainer.textOrNull("grouperConfigurationValidationDoesNotImplementInterface");
              error = error.replace("$$mustImplementInterface$$", mustImplementInterface);
              
              validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), error);
            }
          }
          
          
        }
      }
    } finally {
      GrouperTextContainer.resetThreadLocalVariableMap();
    }
    }
  }
  
  private Map<String, Object> objectValueSubstituteMap = null;

  private String cacheAttributePrefix;

  private String cacheEntityAttributePrefix;

  private String cacheFieldPrefix;

  private String cacheGroupAttributePrefix;
  
  /**
   * expression language substitute map
   * @return
   */
  public Map<String, Object> retrieveObjectValueSubstituteMap() {
    
    if (this.objectValueSubstituteMap == null) {
      Map<String, Object> variableMap = new HashMap<String, Object>();

      variableMap.put("grouperUtil", new GrouperUtilElSafe());
      
      for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
        
        variableMap.put(grouperConfigModuleAttribute.getConfigSuffix(), grouperConfigModuleAttribute.getObjectValueAllowInvalid());
        
      }
      this.objectValueSubstituteMap = variableMap;
    }
    return this.objectValueSubstituteMap;
  }
  
  private static final Pattern otherSuffixPattern = Pattern.compile("^(.*)<otherSuffix_([^>]+)>(.*)$");
  
  public void populateConfigurationValuesFromUi(final HttpServletRequest request) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    for (GrouperConfigurationModuleAttribute attribute: attributes.values()) {
      
      if (attribute.isReadOnly()) {
        
//        if (StringUtils.isNotBlank(attribute.getValue())) {
//          String newValue = attribute.getValue().replace("<configId>", this.getConfigId());
//          attribute.setValue(newValue);
//          
//          if (attribute.getValue().contains("<otherSuffix_")) {
//            
//            Matcher otherSuffixMatcher = otherSuffixPattern.matcher(attribute.getValue());
//            if (otherSuffixMatcher.matches()) {
//              
//              String otherSuffixPrefix = otherSuffixMatcher.group(1);
//              String otherSuffix = otherSuffixMatcher.group(2);
//              String otherSuffixPost = otherSuffixMatcher.group(3);
//              
//              String key = attribute.getConfigSuffix();
//              int lastDotIndex = key.lastIndexOf('.');
//              key = key.substring(0, lastDotIndex);
//              key = key +  "."+otherSuffix;
//              GrouperConfigurationModuleAttribute nameAttribute = attributes.get(key);
//              if (nameAttribute != null) {
//                String nameValue = nameAttribute.getValueOrExpressionEvaluation();
//                if (StringUtils.isNotBlank(nameValue)) {
//                  newValue = otherSuffixPrefix + nameValue + otherSuffixPost;
//                  attribute.setValue(newValue);
//                }
//              }
//            }
//            
//            
//               
//          }
//        }
        
        continue;
      }
      
      String name = "config_"+attribute.getConfigSuffix();
      String elCheckboxName = "config_el_"+attribute.getConfigSuffix();
      
      String elValue = request.getParameter(elCheckboxName);
      
      String value = null;
      if (attribute.getConfigItemMetadata().getFormElement() == ConfigItemFormElement.CHECKBOX) {
        String[] values = request.getParameterValues(name+"[]");
        if (values != null && values.length > 0) {
          value = String.join(",", Arrays.asList(values));
        }
      } else {
        value = request.getParameter(name);
      }
      
      if (StringUtils.isNotBlank(elValue) && elValue.equalsIgnoreCase("on")) {
        attribute.setExpressionLanguage(true);
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setExpressionLanguageScript(value);
      } else {
        attribute.setExpressionLanguage(false);
        attribute.setValue(value);
      }
        
    }
    
    // if the dropdown is based on an option driver that uses configuration values
    // we need a second pass
    for (GrouperConfigurationModuleAttribute attribute: attributes.values()) {
      
     populateValuesLabelsFromOptionValueClass(attributes, attribute);
      
    }
    
    
  }

  /**
   * clear the attribute cache which also clears the expression language cache
   */
  public void clearAttributeCache() {
    this.attributeCache = null;
  }

  
  /**
   * retrieve attributes based on the instance.  Key is the config suffix
   * @return
   */
  public Map<String, GrouperConfigurationModuleAttribute> retrieveAttributes() {
    
    if (this.attributeCache != null) {
      return this.attributeCache;
    }
    
    // recalculate this
    this.objectValueSubstituteMap = null;
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperConfigurationModuleAttribute> tempResult = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();

    Pattern pattern = Pattern.compile(this.getConfigIdRegex());

    List<ConfigSectionMetadata> configSectionMetadataList = configFileName.configFileMetadata().getConfigSectionMetadataList();
    
    // get the attributes based on the configIdThatIdentifiesThisConfig
    String configIdThatIdentifiesThisConfig = null;
    
    if (this.isMultiple() && StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Cant have isMultiple and a blank configId! " + this.getClass().getName());
    }
    if (!this.isMultiple() && !StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Cant have not isMultiple and configId! " + this.getClass().getName());
    }

    if (this.getPropertySuffixThatIdentifiesThisConfig() != null) {
      
      if (StringUtils.isBlank(this.getPropertyValueThatIdentifiesThisConfig())) {
        throw new RuntimeException("getPropertyValueThatIdentifiesThisConfig is required for " + this.getClass().getName());
      }
      
      if (StringUtils.isNotBlank(this.getConfigIdThatIdentifiesThisConfig())) {
        throw new RuntimeException("can't specify ConfigIdThatIdentifiesThisConfig and PropertySuffixThatIdentifiesThisConfig for class "+this.getClass().getName());
      }
 
      if (!this.isMultiple()) {
        throw new RuntimeException("Cant have getPropertySuffixThatIdentifiesThisConfig and not be multiple! " + this.getClass().getName());
      }
      
      outer: for (ConfigSectionMetadata configSectionMetadata: configSectionMetadataList) {
        for (ConfigItemMetadata configItemMetadata: configSectionMetadata.getConfigItemMetadataList()) {
          
          Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
          if (!matcher.matches()) {
            continue;
          }
          
          String configId = matcher.group(2);
          String suffix = matcher.group(3);

          if (StringUtils.equals(suffix, this.getPropertySuffixThatIdentifiesThisConfig())) {
            
            if (StringUtils.equals(configItemMetadata.getValue(), this.getPropertyValueThatIdentifiesThisConfig())
                || StringUtils.equals(configItemMetadata.getSampleValue(), this.getPropertyValueThatIdentifiesThisConfig())) {
              configIdThatIdentifiesThisConfig = configId;
              break outer;
            }
            
          }
        }
      }
      
      if (StringUtils.isBlank(configIdThatIdentifiesThisConfig)) {
        throw new RuntimeException("can't find property in config file that identifies this daemon for " + this.getClass().getName());
      }
      
    } else if (this.getConfigIdThatIdentifiesThisConfig() != null ) {
      configIdThatIdentifiesThisConfig = this.getConfigIdThatIdentifiesThisConfig();
    }
    
    Set<String> subsectionsToIgnore = new HashSet<String>();
    Set<String> suffixesToIgnore = new HashSet<String>();
    Set<Pattern> regexPatternsToIgnore = new HashSet<Pattern>();
    
    if (!StringUtils.isBlank(configIdThatIdentifiesThisConfig)) {
      
      {
        String subsectionsToIgnoreString = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisionerPropertiesToIgnore."+this.getClass().getSimpleName()+".subsections", "");
        String[] ignoreSusections = subsectionsToIgnoreString.split(",");
        for (String singleSubsection: ignoreSusections) {
          subsectionsToIgnore.add(singleSubsection.trim());
        }
      }
      
      {
        String suffixesToIgnoreString = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisionerPropertiesToIgnore."+this.getClass().getSimpleName()+".keySuffixes", "");
        String[] ignoreSuffixes = suffixesToIgnoreString.split(",");
        for (String singleSuffix: ignoreSuffixes) {
          suffixesToIgnore.add(singleSuffix.trim());
        }
      }
      
      {
        String suffixesRegexToIgnoreString = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisionerPropertiesToIgnore."+this.getClass().getSimpleName()+".keySuffixRegexes", "");
        String[] ignoreSuffixesRegex = suffixesRegexToIgnoreString.split(",");
        for (String singleSuffixRegex: ignoreSuffixesRegex) {
          String regexStr = singleSuffixRegex.trim().replace("U+002C", ",");
          regexPatternsToIgnore.add(Pattern.compile(regexStr));
        }
      }
      
    }
    
    for (ConfigSectionMetadata configSectionMetadata: configSectionMetadataList) {
      lbl: for (ConfigItemMetadata configItemMetadata: configSectionMetadata.getConfigItemMetadataList()) {

        String propertyName = configItemMetadata.getKeyOrSampleKey();
        String suffix = propertyName;
        if (!this.retrieveExtraConfigKeys().contains(propertyName)) {
          
          Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
          if (!matcher.matches()) {
            continue;
          }
          
          String prefix = matcher.group(1);
          suffix = null;
                  
          if(this.isMultiple()) { // multiple means config id will not be blank on an edit

            if (StringUtils.isBlank(configIdThatIdentifiesThisConfig)) {
              throw new RuntimeException("Why is configIdThatIdentifiesThisConfig blank??? " + this.getClass().getName());
            }

            String currentConfigId = matcher.group(2);

            if (!StringUtils.equals(currentConfigId, configIdThatIdentifiesThisConfig) && 
                !StringUtils.equals(currentConfigId, this.getGenericConfigId())) {
              continue;
            }
            
            suffix = matcher.group(3);
            propertyName = prefix + "." + this.getConfigId() + "." + suffix;
            
          } else {
            
            if (!StringUtils.isBlank(this.getConfigId())) {
              throw new RuntimeException("Why is configId not blank??? " + this.getClass().getName());
            }
            suffix = matcher.group(2);
            propertyName = configItemMetadata.getKeyOrSampleKey();
            
          }

        }
        
        if (subsectionsToIgnore.contains(configItemMetadata.getSubSection())) {
          continue;
        }
        
        if (suffixesToIgnore.contains(suffix)) {
          continue;
        }
        
        for (Pattern patternToIgnore: regexPatternsToIgnore) {
          Matcher matcher = patternToIgnore.matcher(suffix);
          if (matcher.matches()) {
            continue lbl;
          }
        }
        
        GrouperConfigurationModuleAttribute grouperConfigModuleAttribute =
            buildConfigurationModuleAttribute(propertyName, suffix, true, configItemMetadata,
                configPropertiesCascadeBase, tempResult);
        tempResult.put(suffix, grouperConfigModuleAttribute);
     
      }
    }
    
    // entries belonging to the same repeat group; pull them out because they need to stay together
    // they don't follow the order based on order property from json
    
    // keys are the repeat groups and the values are the key value pairs of all the configs
    // in order for that repeat group
    Map<String, Map<String, GrouperConfigurationModuleAttribute>> repeatGroups = new LinkedHashMap<String, Map<String, GrouperConfigurationModuleAttribute>>();
    
    // order index of the first item in the repeat group
    // which is also in the list of items that don't include the repeat group
    //  so when we iterate through and we see an item with that index in this map,
    // we will substitute that repeat group
    Map<Integer, String> orderToRepeatGroup = new TreeMap<Integer, String>(); 
    
    // temp result is all the config items without the full repeat groups but including
    // the first item of the repeat group
    Iterator<Entry<String, GrouperConfigurationModuleAttribute>> iterator = tempResult.entrySet().iterator();
    
    while (iterator.hasNext()) {
      
      Entry<String, GrouperConfigurationModuleAttribute> entry = iterator.next();
      String repeatGroup = entry.getValue().getConfigItemMetadata().getRepeatGroup();
      
      if (StringUtils.isNotBlank(repeatGroup)) {
        // if we have seen the repeat group then add the item
        if (repeatGroups.containsKey(repeatGroup)) {
          repeatGroups.get(repeatGroup).put(entry.getKey(), entry.getValue());
          iterator.remove();
        } else {
          // create a repeat group if we haven't seen it before and add the item
          // don't move the item from the temp result because we need the pointer back to the repeat group
          Map<String, GrouperConfigurationModuleAttribute> map = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();
          map.put(entry.getKey(), entry.getValue());
          repeatGroups.put(repeatGroup, map);
          orderToRepeatGroup.put(entry.getValue().getConfigItemMetadata().getOrder(), repeatGroup);
        }
      }
    }
    
    // sort the temp result by the sort order index, only include the first repeat group item in the index
    List<Map.Entry<String, GrouperConfigurationModuleAttribute>> sorted = new ArrayList<>(tempResult.entrySet());
   
    Collections.sort(sorted, new Comparator<Map.Entry<String, GrouperConfigurationModuleAttribute>>() {

      @Override
      public int compare(Entry<String, GrouperConfigurationModuleAttribute> o1,
          Entry<String, GrouperConfigurationModuleAttribute> o2) {
        
        return o1.getValue().getConfigItemMetadata().getOrder() - o2.getValue().getConfigItemMetadata().getOrder();
      }
    });
    
    // all items including repeat group in order
    Map<String, GrouperConfigurationModuleAttribute> finalResult = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();
    
    // place repeat groups in the sorted based on the order
    for (Map.Entry<String, GrouperConfigurationModuleAttribute> entry: sorted) {
      
      int orderWithoutRepeatGroup= entry.getValue().getConfigItemMetadata().getOrder();
      
      // if this is the first item of the repeat group then put the repeat group in the index instead of the item
      // the repeat group contains this item so don't also add this item
      // Note: subsections are sorted by the first item that has the subsection and the order index in another method
      // repeat groups will create subsections based on the repeat group label and the repeat group iterator
      String repeatGroupName = orderToRepeatGroup.get(orderWithoutRepeatGroup);
      if (StringUtils.isNotBlank(repeatGroupName)) {
        Map<String, GrouperConfigurationModuleAttribute> map = repeatGroups.get(repeatGroupName);
        finalResult.putAll(map);
      } else {
        // this is not the first item of the repeat group so just add it
        finalResult.put(entry.getKey(), entry.getValue());
      }
      
    }
    
    Map<String, GrouperConfigurationModuleAttribute> extraAttributes = retrieveExtraAttributes(finalResult);
    finalResult.putAll(extraAttributes);
    
    this.attributeCache = finalResult;

    return finalResult;
    
  }
  
  /**
   * config file name to check for properties and metadata
   * @return
   */
  public abstract ConfigFileName getConfigFileName();
  
  /**
   * prefix for the properties eg: provisioner.
   * @return
   */
  public abstract String getConfigItemPrefix();
  
  /**
   * config id regeg eg: ^(provisioner)\\.([^.]+)\\.(.*)$
   * @return
   */
  public abstract String getConfigIdRegex();
  
  /**
   * retrieve suffix based on the property name
   * @param pattern
   * @param propertyName
   * @return
   */
  public String retrieveSuffix(Pattern pattern, String propertyName) {
    Matcher matcher = pattern.matcher(propertyName);
    
    if (!matcher.matches()) {
      return null;
    }
    
    String configId = this.getConfigId();
    if (StringUtils.isBlank(configId)) {
      throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
    }
    
    String configIdFromProperty = matcher.group(2);
    
    if (!StringUtils.equals(configId, configIdFromProperty)) {
      return null;
    }
    
    String suffix = matcher.group(3);
    return suffix;
  }
  
  
  /**
   * get subsections for the UI
   * @return
   */
  public List<GrouperConfigurationModuleSubSection> getSubSections() {
    
    List<GrouperConfigurationModuleSubSection> results = new ArrayList<GrouperConfigurationModuleSubSection>();
    
    Set<String> sectionLabelsUsed = new HashSet<String>();
    
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
      
      String sectionLabel = grouperConfigModuleAttribute.getConfigItemMetadata().getSubSection();
      if (StringUtils.isBlank(sectionLabel)) {
        sectionLabel = "NULL";
      }
      if (sectionLabelsUsed.contains(sectionLabel)) {
        continue;
      }
      sectionLabelsUsed.add(sectionLabel);
      
      
      GrouperConfigurationModuleSubSection configurationSubSection = new GrouperConfigurationModuleSubSection();
      configurationSubSection.setConfiguration(this);
      configurationSubSection.setLabel(grouperConfigModuleAttribute.getConfigItemMetadata().getSubSection());
      results.add(configurationSubSection);
    }
    
    return results;
  }
  
  private Map<String, GrouperConfigurationModuleAttribute> retrieveExtraAttributes(Map<String, GrouperConfigurationModuleAttribute> attributesFromBaseConfig) {
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperConfigurationModuleAttribute> result = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();
    
    Pattern pattern = null;
    try {
      pattern = Pattern.compile(this.getConfigIdRegex());
    } catch (Exception e) {
      // daemon might throw an error so ignore it.
    }
        
    for (String propertyName: configPropertiesCascadeBase.properties().stringPropertyNames()) {
      
      String suffix = retrieveSuffix(pattern, propertyName);
      
      if (StringUtils.isBlank(suffix)) {
        continue;
      }
      
      if (attributesFromBaseConfig.containsKey(suffix)) {
        GrouperConfigurationModuleAttribute attribute = attributesFromBaseConfig.get(suffix);
        if (GrouperConfigHibernate.isPasswordHelper(attribute.getConfigItemMetadata(), configPropertiesCascadeBase.propertyValueString(propertyName))) {
          attribute.setValue(GrouperConfigHibernate.ESCAPED_PASSWORD);
        } else {
          attribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
        }
        
      } else {
        
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setFormElement(ConfigItemFormElement.TEXT);
        configItemMetadata.setValueType(ConfigItemMetadataType.STRING);
        
        GrouperConfigurationModuleAttribute configModuleAttribute =
            buildConfigurationModuleAttribute(propertyName, suffix, false, configItemMetadata, 
                configPropertiesCascadeBase, result);
        
        result.put(suffix, configModuleAttribute);
      }
      
    }
    
    return result;
     
  }
  
  private GrouperConfigurationModuleAttribute buildConfigurationModuleAttribute(
      String propertyName, String suffix, boolean useConfigItemMetadataValue,
      ConfigItemMetadata configItemMetadata, ConfigPropertiesCascadeBase configPropertiesCascadeBase,
      Map<String, GrouperConfigurationModuleAttribute> attributesSoFar) {
    
    GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = new GrouperConfigurationModuleAttribute();
    grouperConfigModuleAttribute.setConfigItemMetadata(configItemMetadata);
    grouperConfigModuleAttribute.setRepeatGroupIndex(configItemMetadata.getRepeatGroupIndex());
    
    grouperConfigModuleAttribute.setFullPropertyName(propertyName);
    grouperConfigModuleAttribute.setGrouperConfigModule(this);
    
    {
      boolean hasExpressionLanguage = configPropertiesCascadeBase.hasExpressionLanguage(propertyName);
      grouperConfigModuleAttribute.setExpressionLanguage(hasExpressionLanguage);

      if (hasExpressionLanguage) {
        String rawExpressionLanguage = configPropertiesCascadeBase.rawExpressionLanguage(propertyName);
        grouperConfigModuleAttribute.setExpressionLanguageScript(rawExpressionLanguage);
      }
    }
    
    String value = configPropertiesCascadeBase.propertyValueString(propertyName);
    if (useConfigItemMetadataValue) {
      value = StringUtils.isBlank(value) ? configItemMetadata.getValue(): value;
      value = StringUtils.isBlank(value) ? configItemMetadata.getSampleValue(): value;
    }
    grouperConfigModuleAttribute.setValue(value);
    
    grouperConfigModuleAttribute.setConfigSuffix(suffix);
    
    {
      grouperConfigModuleAttribute.setReadOnly(configItemMetadata.isReadOnly());
      grouperConfigModuleAttribute.setType(configItemMetadata.getValueType());
      
      String defaultValue = configItemMetadata.getDefaultValue();
      String defaultValueEl = configItemMetadata.getDefaultValueEl();
      
      if (StringUtils.isBlank(defaultValue) && StringUtils.isNotBlank(defaultValueEl)) {
        defaultValue = GrouperUtil.substituteExpressionLanguage(defaultValueEl, new HashMap<String, Object>(), true, false, true);
      }
      
      grouperConfigModuleAttribute.setDefaultValue(defaultValue);
      grouperConfigModuleAttribute.setPassword(configItemMetadata.isSensitive());
    }
    
    if (configItemMetadata.getFormElement() == ConfigItemFormElement.DROPDOWN) {
      
      if (GrouperUtil.length(configItemMetadata.getOptionValues()) > 0) {
        List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
        valuesAndLabels.add(new MultiKey("", ""));
        for (String optionValue : configItemMetadata.getOptionValues()) {
          
          String label = GrouperTextContainer.textOrNull("config."
              + this.getClass().getSimpleName() + ".attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + "." + optionValue + ".label");
          
          if (StringUtils.isBlank(label)) {
            if (grouperConfigModuleAttribute.getConfigSuffix().matches(".*\\.[0-9]\\..*")) {
              String key = "config." + this.getClass().getSimpleName() + ".attribute.option." + (grouperConfigModuleAttribute.getConfigSuffix().replaceAll("\\.[0-9]+\\.", ".i.")) + "." + optionValue + ".label";
              label = GrouperTextContainer.textOrNull(key);
            } 
          }
          
          label = StringUtils.defaultIfBlank(label, optionValue);
          
          MultiKey valueAndLabel = new MultiKey(optionValue, label);
          valuesAndLabels.add(valueAndLabel);
        }
        grouperConfigModuleAttribute.setDropdownValuesAndLabels(valuesAndLabels);
      }
      
      if (StringUtils.isNotBlank(configItemMetadata.getOptionValuesFromClass())) {
        
        populateValuesLabelsFromOptionValueClass(attributesSoFar, grouperConfigModuleAttribute);
        
      }
    }
    
    if (configItemMetadata.getFormElement() == ConfigItemFormElement.CHECKBOX) {
      String checkboxValueFromClassString = configItemMetadata.getCheckboxValuesFromClass();
      Class<CheckboxValueDriver> klass = GrouperUtil.forName(checkboxValueFromClassString);
      CheckboxValueDriver driver = GrouperUtil.newInstance(klass);
      List<MultiKey> checkboxAttributes = driver.retrieveCheckboxAttributes();
      grouperConfigModuleAttribute.setCheckboxAttributes(checkboxAttributes);
    }
    
    if (grouperConfigModuleAttribute.isPassword()) {
      grouperConfigModuleAttribute.setPassword(true);
      grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.PASSWORD);
    } else {
      ConfigItemFormElement configItemFormElement = configItemMetadata.getFormElement();
      if (configItemFormElement != null) {
        grouperConfigModuleAttribute.setFormElement(configItemFormElement);
      } else {
        // boolean is a radio button
        if (configItemMetadata.getValueType() == ConfigItemMetadataType.BOOLEAN) {
          
          grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.RADIOBUTTON);
  
          if (GrouperUtil.length(grouperConfigModuleAttribute.getDropdownValuesAndLabels()) == 0) {
            
            String trueLabel = GrouperTextContainer.textOrNull("config." 
                + this.getClass().getSimpleName() + ".attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + ".trueLabel");
            
            if (StringUtils.isBlank(trueLabel)) {
              trueLabel = GrouperTextContainer.textOrNull(
                  "config.GenericConfiguration.attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + ".trueLabel");
            }
            
            if (StringUtils.isBlank(trueLabel)) {
              String configSuffix = grouperConfigModuleAttribute.getConfigSuffix();
              
              if (configSuffix.matches(".*\\.[0-9]\\..*")) {
                configSuffix = configSuffix.replaceAll("\\.[0-9]+\\.", ".i.");
                trueLabel = GrouperTextContainer.textOrNull("config." 
                    + this.getClass().getSimpleName() + ".attribute.option." + configSuffix + ".trueLabel");
                if (StringUtils.isBlank(trueLabel)) {
                  trueLabel = GrouperTextContainer.textOrNull(
                      "config.GenericConfiguration.attribute.option." + configSuffix + ".trueLabel");
                }
              }
            }
            
            if (StringUtils.isBlank(trueLabel)) {
              trueLabel = GrouperTextContainer.textOrNull("config.defaultTrueLabel");
            }
            
            String falseLabel = GrouperTextContainer.textOrNull("config." 
                + this.getClass().getSimpleName() + ".attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + ".falseLabel");

            if (StringUtils.isBlank(falseLabel)) {
              falseLabel = GrouperTextContainer.textOrNull(
                  "config.GenericConfiguration.attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + ".falseLabel");
            }

            if (StringUtils.isBlank(falseLabel)) {
              String configSuffix = grouperConfigModuleAttribute.getConfigSuffix();
              
              if (configSuffix.matches(".*\\.[0-9]\\..*")) {
                configSuffix = configSuffix.replaceAll("\\.[0-9]+\\.", ".i.");
                falseLabel = GrouperTextContainer.textOrNull("config." 
                    + this.getClass().getSimpleName() + ".attribute.option." + configSuffix + ".falseLabel");
                if (StringUtils.isBlank(falseLabel)) {
                  falseLabel = GrouperTextContainer.textOrNull(
                      "config.GenericConfiguration.attribute.option." + configSuffix + ".falseLabel");
                }
              }
            }

            if (StringUtils.isBlank(falseLabel)) {
              falseLabel = GrouperTextContainer.textOrNull("config.defaultFalseLabel");
            }
            
            String defaultValue = configItemMetadata.getDefaultValue();
            Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(defaultValue);
            
            List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
            
            String defaultValueStr = "";

            if (booleanObjectValue != null) {
              defaultValueStr = booleanObjectValue ? "("+trueLabel+")" : "("+falseLabel+")"; 
              valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel")+" " + defaultValueStr ));
            }
            
            
            valuesAndLabels.add(new MultiKey("true", trueLabel));
            valuesAndLabels.add(new MultiKey("false", falseLabel));
            grouperConfigModuleAttribute.setDropdownValuesAndLabels(valuesAndLabels);
          }
        } else if (GrouperUtil.length(grouperConfigModuleAttribute.getValue()) > 100) {
  
          grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.TEXTAREA);
  
        } else {
          grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.TEXT);
        }
      }
    }
    
    return grouperConfigModuleAttribute;
  }

  protected void populateValuesLabelsFromOptionValueClass(
      Map<String, GrouperConfigurationModuleAttribute> attributesSoFar,
      GrouperConfigurationModuleAttribute grouperConfigModuleAttribute) {
    
    String optionValueFromClassString = grouperConfigModuleAttribute.getConfigItemMetadata().getOptionValuesFromClass();
    
    if (StringUtils.isNotBlank(optionValueFromClassString)) {

      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      valuesAndLabels.add(new MultiKey("", ""));
    
      Class theClassRaw = GrouperUtil.forName(optionValueFromClassString);
      if (OptionValueDriver.class.isAssignableFrom(theClassRaw)) {
        Class<OptionValueDriver> klass = theClassRaw;
        OptionValueDriver driver = GrouperUtil.newInstance(klass);
        driver.setConfigSuffixToConfigModuleAttribute(attributesSoFar);
        valuesAndLabels.addAll(driver.retrieveKeysAndLabels());
      } else if (theClassRaw.isEnum()) {
        Set<String> values = new TreeSet<String>();
        for (Enum theEnum : (List<Enum>)EnumUtils.getEnumList(theClassRaw)) {
          String name = theEnum.name();
          // dont use all caps, assume parser doesnt care about case
          if (name.equals(name.toUpperCase())) {
            name = name.toLowerCase();
          }
          values.add(name);
        }
        for (String value: values) {
          valuesAndLabels.add(new MultiKey(value, value));
        }
      } else {
        throw new RuntimeException("Option value driver needs to be OptionValueDriver or enum: " + theClassRaw);
      }
      grouperConfigModuleAttribute.setDropdownValuesAndLabels(valuesAndLabels);
      
    }
    
  }
  
  /**
   * 
   * @param suffix
   * @return
   */
  public Boolean showAttributeOverride(String suffix) {
    return null;
  }
  
  /**
   * save the attribute in an insert.  Note, if theres a failure, you should see if any made it
   * @param attributesToSave are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void insertConfig(boolean fromUi, 
      StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    validatePreSave(true, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add all the possible ones
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    for (String suffix : attributes.keySet()) {
    
      GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = attributes.get(suffix);
      
      if (grouperConfigModuleAttribute.isHasValue() && grouperConfigModuleAttribute.getConfigItemMetadata().isSaveToDb()) {
        
        StringBuilder localMessage = new StringBuilder();
        
        DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(),
            grouperConfigModuleAttribute.getFullPropertyName(),
            grouperConfigModuleAttribute.isExpressionLanguage() ? "true" : "false",
            grouperConfigModuleAttribute.isExpressionLanguage() ? grouperConfigModuleAttribute.getExpressionLanguageScript() : grouperConfigModuleAttribute.getValue(),
            grouperConfigModuleAttribute.isPassword(), localMessage, new Boolean[] {false},
            new Boolean[] {false}, fromUi, "Added from config editor", errorsToDisplay, validationErrorsToDisplay, false);
        
        if (localMessage.length() > 0) {
          if(message.length() > 0) {
            
            if (fromUi && !endOfStringNewlinePattern.matcher(message).matches()) {
              message.append("<br />\n");
            } else if (!fromUi && message.charAt(message.length()-1) != '\n') {
              message.append("\n");
            }
            message.append(localMessage);
          }
        }
      }
    }

    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
  }
  
  /**
   * delete config
   * @param fromUi
   */
  public void deleteConfig(boolean fromUi) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    for (GrouperConfigurationModuleAttribute attribute: attributes.values()) {
      String fullPropertyName = attribute.getFullPropertyName();
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().name(), fullPropertyName, fromUi, true);
    }
    
    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
  }
  
  /**
   * save the attribute in an edit.  Note, if theres a failure, you should see if any made it
   * @param attributesFromUser are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one (delete)
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    validatePreSave(false, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    Set<String> propertyNamesToDelete = new HashSet<String>();

    // add all the possible ones
    for (String suffix : attributes.keySet()) {
    
      GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = attributes.get(suffix);

      propertyNamesToDelete.add(grouperConfigModuleAttribute.getFullPropertyName());
      
    }

    // and all the ones we detect
    if (!StringUtils.isBlank(this.getConfigId())) {
      
      Set<String> configKeys = this.retrieveConfigurationKeysByPrefix(this.getConfigItemPrefix());
      
      if (GrouperUtil.length(configKeys) > 0) {
        propertyNamesToDelete.addAll(configKeys);
      }
    }
    
    Map<String, GrouperConfigurationModuleAttribute> attributesToSave = new HashMap<String, GrouperConfigurationModuleAttribute>();
    
    // remove the edited ones
    for (String suffix : attributes.keySet()) {
    
      GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = attributes.get(suffix);
      
      if (grouperConfigModuleAttribute.isHasValue()) {
        propertyNamesToDelete.remove(grouperConfigModuleAttribute.getFullPropertyName());
        attributesToSave.put(suffix, grouperConfigModuleAttribute);
      }
    }
    // delete some
    for (String key : propertyNamesToDelete) {
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().getConfigFileName(), key , fromUi, false);
    }

    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add/edit all the possible ones
    for (String suffix : attributesToSave.keySet()) {
    
      GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = attributesToSave.get(suffix);
      
      if (!grouperConfigModuleAttribute.getConfigItemMetadata().isSaveToDb()) {
        continue;
      }
      
      StringBuilder localMessage = new StringBuilder();
      
      DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
          grouperConfigModuleAttribute.getFullPropertyName(), 
          grouperConfigModuleAttribute.isExpressionLanguage() ? "true" : "false", 
          grouperConfigModuleAttribute.isExpressionLanguage() ? grouperConfigModuleAttribute.getExpressionLanguageScript() : grouperConfigModuleAttribute.getValue(),
          grouperConfigModuleAttribute.isPassword(), localMessage, new Boolean[] {false},
          new Boolean[] {false}, fromUi, "Added from config editor", errorsToDisplay, validationErrorsToDisplay, false);
      
      if (localMessage.length() > 0) {
        if(message.length() > 0) {
          
          if (fromUi && !endOfStringNewlinePattern.matcher(message).matches()) {
            message.append("<br />\n");
          } else if (!fromUi && message.charAt(message.length()-1) != '\n') {
            message.append("\n");
          }
          message.append(localMessage);
        }
      }
    }

    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
  }
  
  /**
   * get configuration names configured by prefix 
   * @param prefix of config e.g. ldap.personLdap.
   * @return the list of configured keys
   */
  public Set<String> retrieveConfigurationKeysByPrefix(String prefix) {
    Set<String> result = new HashSet<String>();
    ConfigFileName configFileName = this.getConfigFileName();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Properties properties = configPropertiesCascadeBase.properties();

    for (Object propertyNameObject : properties.keySet()) {
      String propertyName = (String)propertyNameObject;
      if (propertyName.startsWith(prefix)) {

        if (result.contains(propertyName)) {
          LOG.error("Config key '" + propertyName + "' is defined in '" + configFileName.getConfigFileName() + "' more than once!");
        } else {
          result.add(propertyName);
        }
      }
    }
    return result;
  }
  
  /**
   * get title of the grouper daemon configuration
   * @return
   */
  public String getTitle() {
    String title = GrouperTextContainer.textOrNull("config." + this.getClass().getSimpleName() + ".title");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  /**
   * get description of the external system
   * @return
   */
  public String getDescription() {
    String title = GrouperTextContainer.textOrNull("config." + this.getClass().getSimpleName() + ".description");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  /**
   * get a set of config ids
   * @return
   */
  public Set<String> retrieveConfigurationConfigIds() {
    
    String regex = this.getConfigIdRegex();
    
    if (StringUtils.isBlank(regex)) {
      throw new RuntimeException("Regex is reqired for " + this.getClass().getName());
    }
    
    Set<String> result = new TreeSet<String>();
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Properties properties = configPropertiesCascadeBase.properties();

    Pattern pattern = Pattern.compile(regex);
    
    for (Object propertyNameObject : properties.keySet()) {
      String propertyName = (String)propertyNameObject;
      String value = (String) properties.get(propertyNameObject);
      
      Matcher matcher = pattern.matcher(propertyName);
      
      if (!matcher.matches()) {
        continue;
      }

      String configId = matcher.group(2);
      String suffix = matcher.group(3);
      
      if (StringUtils.isNotBlank(this.getConfigIdThatIdentifiesThisConfig()) && StringUtils.equals(configId, this.getConfigIdThatIdentifiesThisConfig())) {
        result.add(configId);
      }
      
      if (StringUtils.isNotBlank(this.getPropertySuffixThatIdentifiesThisConfig()) && 
          StringUtils.isNotBlank(this.getPropertyValueThatIdentifiesThisConfig()) &&
          StringUtils.equals(suffix, this.getPropertySuffixThatIdentifiesThisConfig()) 
          && StringUtils.equals(value, this.getPropertyValueThatIdentifiesThisConfig())) {
        result.add(configId);
      }
      
      if (StringUtils.isBlank(this.getPropertySuffixThatIdentifiesThisConfig()) && 
          StringUtils.isBlank(this.getPropertyValueThatIdentifiesThisConfig())) {
        result.add(configId);
      }
      
    }
    return result;
  }


  /**
   * for each type of configuration this is the prefix for eg in subsections. only ui concern in external text config.
   * @return
   */
  protected abstract String getConfigurationTypePrefix();
  
  protected String getGenericConfigId() {
    return null;
  }
  
  /**
   * can there be multiple instances of this config. for eg: LdapProvisionerConfig is true but for GrouperDaemonChangeLogRulesConfiguration is false
   * @return
   */
  public boolean isMultiple() {
    return true;
  }

  public String getCacheAttributePrefix() {
    if (this.cacheAttributePrefix == null) {
      this.cacheAttributePrefix = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.attributePrefix");
    }
    return cacheAttributePrefix;
  }

  public String getCacheEntityAttributePrefix() {
    if (this.cacheEntityAttributePrefix == null) {
      this.cacheEntityAttributePrefix = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetEntityAttribute.i.entityAttributePrefix");
    }
    return cacheEntityAttributePrefix;
  }

  public String getCacheFieldPrefix() {
    if (this.cacheFieldPrefix == null) {
      this.cacheFieldPrefix = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.fieldPrefix");
    }
    return cacheFieldPrefix;
  }

  public String getCacheGroupAttributePrefix() {
    
    if (this.cacheGroupAttributePrefix == null) {
      this.cacheGroupAttributePrefix = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.groupAttributePrefix");
    }
    
    return cacheGroupAttributePrefix;
  }
  
}
