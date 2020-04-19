package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class GrouperExternalSystem {
  
  private String configId;
  
  public List<String> validate(boolean isAdd) {
    List<String> errors = new ArrayList<String>();
    if(StringUtils.isBlank(configId)) {
      errors.add(""); //TODO fill me
    }
    return errors;
  }
  
  public List<String> test() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  
  public String getHtml(String methodName) {
    
    StringBuilder html = new StringBuilder();
    
    List<GrouperExternalSystemSubSection> sections = this.getSubSections();
    
    for (GrouperExternalSystemSubSection section: sections) {
      String label = section.getLabel();
      if (StringUtils.isNotBlank(label)) {
        
        html.append("<tbody>");
        html.append("<tr>");
        html.append("<th colspan='3' >");
        html.append("<h4>");
        html.append(section.getTitle());
        html.append("</h4>");
        html.append("<p style='font-weight: normal;'>");
        html.append(section.getDescription());
        html.append("</p>");
        
        html.append("</th>");
        html.append("</tr>");
       
        for (GrouperExternalSystemAttribute attribute: section.getAttributes().values()) {
          html.append(buildHtmlFormElement(attribute, methodName));   
        }
        
        html.append("</tbody>");
      } else {
        for (GrouperExternalSystemAttribute attribute: section.getAttributes().values()) {
          html.append(buildHtmlFormElement(attribute, methodName));   
        }
      }
      
    }
    
    return html.toString();
  }
  
  private String buildHtmlFormElement(GrouperExternalSystemAttribute attribute, String methodName) {
    
    StringBuilder field = new StringBuilder();
    
    field.append("<tr id='configRow_"+attribute.getConfigSuffix()+"_id' " + (attribute.isShow() ? "" : " style='display:none' ") + ">");
    field.append("<td style='vertical-align: top; white-space: nowrap;'>");
    field.append("<strong><label for='config_"+attribute.getConfigSuffix()+"_id'>");
    field.append(attribute.getLabel());
    field.append("</label></strong></td>");
    
    field.append("<td style='vertical-align: top; white-space: nowrap;' >");
    field.append("<input style='vertical-align: top; min-height: 10px; margin-right: 2px;' type='checkbox' ");
    
    if (attribute.isExpressionLanguage()) {
      field.append(" checked ");
    }
        
    field.append("onchange=\"ajax('../app/UiV2ExternalSystem."+methodName+"?externalSystemConfigId="+this.getConfigId()+"&externalSystemType="+this.getClass().getName()+"', {formIds: 'externalSystemConfigDetails'}); return false;\""
        + " name='config_el_"+attribute.getConfigSuffix()+"'>");
    field.append("</input><span rel='tooltip' title='" + GrouperUtil.xmlEscape(GrouperTextContainer.textOrNull("grouperExternalSystemAttributesIsElTooltip")) + "' style='border-bottom: 1px dotted #000;'>");
    field.append(GrouperTextContainer.textOrNull("grouperExternalSystemAttributesIsElLabel"));
    field.append("</span></td>");
    
    field.append("<td>");
    
    String value = attribute.getValueOrExpressionEvaluation();

    if (attribute.getFormElement() == ConfigItemFormElement.TEXT) {
      
      
      field.append(
          "<input style='width:30em;' type='text' id='config_"+attribute.getConfigSuffix()+"_id' name='config_" + attribute.getConfigSuffix() + "'");
      if (value != null) {
        field.append(" value = '"+GrouperUtil.xmlEscape(value)+"'");
      }
      field.append("></input>");
      
    }
    
    if (attribute.getFormElement() == ConfigItemFormElement.TEXTAREA) {
            
      field.append("<textarea style='width:30em;' cols='20' rows='3' id='config_"+attribute.getConfigSuffix()+"_id' name='config_"
          + attribute.getConfigSuffix() + "'>");
      if (value != null) {
        field.append(GrouperUtil.xmlEscape(value));
      }
      field.append("</textarea>");
      
    }
    
    if (attribute.getFormElement() == ConfigItemFormElement.PASSWORD) {
      
      field.append(
          "<input style='width:30em;' type='password' id='config_"+attribute.getConfigSuffix()+"_id' name= 'config_" + attribute.getConfigSuffix() + "'");
      if (value != null) {
        field.append(" value = '"+GrouperUtil.xmlEscape(value)+"'");
      }
      field.append("></input>");
    }
    
    if (attribute.getFormElement() == ConfigItemFormElement.DROPDOWN) {
      
      field.append("<select style='width:30em;' id='config_"+attribute.getConfigSuffix()+"_id' name='config_"+attribute.getConfigSuffix()+"' ");
      
      field.append("onchange=\"ajax('../app/UiV2ExternalSystem."+methodName+"?externalSystemConfigId="+this.getConfigId()+"&externalSystemType="+this.getClass().getName()+"', {formIds: 'externalSystemConfigDetails'}); return false;\">");
      
      List<MultiKey> valuesAndLabels = attribute.getDropdownValuesAndLabels();
      for (MultiKey multiKey: valuesAndLabels) {
        
        String key = (String) multiKey.getKey(0);
        String optionValue = (String) multiKey.getKey(1);
        
        boolean selected = StringUtils.equals(key, value);
        
        field.append("<option value='"+key+"'" + (selected ? " selected='selected'" : "") + ">");
        field.append(GrouperUtil.xmlEscape(optionValue));
        field.append("</option>");
      }
      
      field.append("</select>");
    }
    
    if (attribute.isRequired()) {
      field.append("<span class='requiredField' rel='tooltip' data-html='true' data-delay-show='200' data-placement='right'>*");
      field.append("</span>");
    }
    
    field.append("<br>");
    field.append("<span class='description'>");
    field.append(attribute.getDescription());
    field.append("</span>");
    
    field.append("</td>");
    field.append("</tr>");
    
    return field.toString();
  }
  
  public Boolean showAttributeOverride(String suffix) {
    return null;
  }
  
  /**
   * 
   * @param isInsert
   * @param fromUi
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void validatePreSave(boolean isInsert, boolean fromUi, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    if (isInsert) {
      if (this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
        validationErrorsToDisplay.put("#externalSystemConfigId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdUsed"));
      }
    } else {
      if (!this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
        validationErrorsToDisplay.put("#externalSystemConfigId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdDoesntExist"));
      }
    }
    
    Pattern configIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
    if (!configIdPattern.matcher(this.getConfigId()).matches()) {
      validationErrorsToDisplay.put("#externalSystemConfigId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdInvalid"));
    }

    // first check if checked the el checkbox then make sure theres a script there
    {
      boolean foundElRequiredError = false;
      for (GrouperExternalSystemAttribute grouperExternalSystemAttribute : this.retrieveAttributes().values()) {
        
        if (grouperExternalSystemAttribute.isExpressionLanguage() && StringUtils.isBlank(grouperExternalSystemAttribute.getExpressionLanguageScript())) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperExternalSystemAttribute.getLabel());
          validationErrorsToDisplay.put(grouperExternalSystemAttribute.getHtmlForElementIdHandle(), 
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
    for (GrouperExternalSystemAttribute grouperExternalSystemAttribute : this.retrieveAttributes().values()) {
      
      ConfigItemMetadataType configItemMetadataType = grouperExternalSystemAttribute.getConfigItemMetadata().getValueType();
      
      String value = null;
      
      try {
        value = grouperExternalSystemAttribute.getEvaluatedValueForValidation();
      } catch (UnsupportedOperationException uoe) {
        // ignore, it will get validated in the post-save
        continue;
      }
      
      // required
      if (StringUtils.isBlank(value)) {
        if (grouperExternalSystemAttribute.getConfigItemMetadata().isRequired()) {

          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperExternalSystemAttribute.getLabel());
          validationErrorsToDisplay.put(grouperExternalSystemAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull("grouperConfigurationValidationRequired"));
          GrouperTextContainer.resetThreadLocalVariableMap();
          
        }
        
        continue;
      }
      String[] valuesToValidate = null;
      if (grouperExternalSystemAttribute.getConfigItemMetadata().isMultiple()) {
        valuesToValidate = GrouperUtil.splitTrim(value, ",");
      } else {
        valuesToValidate = new String[] {value};
      }

      for (String theValue : valuesToValidate) {
        
        // validate types
        String externalizedTextKey = configItemMetadataType.validate(theValue);
        if (!StringUtils.isBlank(externalizedTextKey)) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperExternalSystemAttribute.getLabel());
          validationErrorsToDisplay.put(grouperExternalSystemAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull(externalizedTextKey));
          GrouperTextContainer.resetThreadLocalVariableMap();
          
        }
      }
    }
        
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
    
    validatePreSave(true, fromUi, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add all the possible ones
    Map<String, GrouperExternalSystemAttribute> attributes = this.retrieveAttributes();
    for (String suffix : attributes.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributes.get(suffix);
      
      if (grouperExternalSystemAttribute.isHasValue()) {
        
        StringBuilder localMessage = new StringBuilder();
        
        DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
            grouperExternalSystemAttribute.getFullPropertyName(), 
            grouperExternalSystemAttribute.isExpressionLanguage() ? "true" : "false", 
            grouperExternalSystemAttribute.isExpressionLanguage() ? grouperExternalSystemAttribute.getExpressionLanguageScript() : grouperExternalSystemAttribute.getValue(),
            grouperExternalSystemAttribute.isPassword(), localMessage, new Boolean[] {false},
            new Boolean[] {false}, fromUi, "Added from external system editor", errorsToDisplay, validationErrorsToDisplay, false);
        
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
   * save the attribute in an insert.  Note, if theres a failure, you should see if any made it
   * @param attributesFromUser are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one (delete)
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    validatePreSave(false, fromUi, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }

    
    Map<String, GrouperExternalSystemAttribute> attributes = this.retrieveAttributes();
    
    Set<String> propertyNamesToDelete = new HashSet<String>();

    // add all the possible ones
    for (String suffix : attributes.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributes.get(suffix);

      propertyNamesToDelete.add(grouperExternalSystemAttribute.getFullPropertyName());
      
    }

    // and all the ones we detect
    if (!StringUtils.isBlank(this.getConfigId())) {
      
      Set<String> configKeys = this.retrieveConfigurationKeysByPrefix(this.getConfigItemPrefix());
      
      if (GrouperUtil.length(configKeys) > 0) {
        propertyNamesToDelete.addAll(configKeys);
      }
    }
    
    Map<String, GrouperExternalSystemAttribute> attributesToSave = new HashMap<String, GrouperExternalSystemAttribute>();
    
    // remove the edited ones
    for (String suffix : attributes.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributes.get(suffix);
      
      if (grouperExternalSystemAttribute.isHasValue()) {
        propertyNamesToDelete.remove(grouperExternalSystemAttribute.getFullPropertyName());
        attributesToSave.put(suffix, grouperExternalSystemAttribute);
      }
    }
    // delete some
    for (String key : propertyNamesToDelete) {
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().getConfigFileName(), key , fromUi, false);
    }

    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add/edit all the possible ones
    for (String suffix : attributesToSave.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributesToSave.get(suffix);
      
      StringBuilder localMessage = new StringBuilder();
      
      DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
          grouperExternalSystemAttribute.getFullPropertyName(), 
          grouperExternalSystemAttribute.isExpressionLanguage() ? "true" : "false", 
          grouperExternalSystemAttribute.isExpressionLanguage() ? grouperExternalSystemAttribute.getExpressionLanguageScript() : grouperExternalSystemAttribute.getValue(),
          grouperExternalSystemAttribute.isPassword(), localMessage, new Boolean[] {false},
          new Boolean[] {false}, fromUi, "Added from external system editor", errorsToDisplay, validationErrorsToDisplay, false);
      
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
  
  public String getTitle() {
    String title = GrouperTextContainer.textOrNull("externalSystem." + this.getClass().getSimpleName() + ".title");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  public String getDescription() {
    String title = GrouperTextContainer.textOrNull("externalSystem." + this.getClass().getSimpleName() + ".description");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  /**
   * 
   */
  public void deleteConfig(boolean fromUi) {
    
    if (!this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
      throw new RuntimeException("Why doesnt this config id already exist??? '" + this.getConfigId() + "'");
    }
    
    Map<String, GrouperExternalSystemAttribute> attributes = this.retrieveAttributes();
    
    Set<String> propertyNamesToDelete = new HashSet<String>();

    // add all the possible ones
    for (String suffix : attributes.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributes.get(suffix);

      propertyNamesToDelete.add(grouperExternalSystemAttribute.getFullPropertyName());
      
    }

    // and all the ones we detect
    if (!StringUtils.isBlank(this.getConfigId())) {
      
      Set<String> configKeys = this.retrieveConfigurationKeysByPrefix(this.getConfigItemPrefix());
      
      if (GrouperUtil.length(configKeys) > 0) {
        propertyNamesToDelete.addAll(configKeys);
      }
    }
    
    for (String key : propertyNamesToDelete) {
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().getConfigFileName(), key , fromUi, false);
    }
    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
  }
  
  /**
   * 
   * @return
   */
  public List<GrouperExternalSystem> listAllExternalSystemsOfThisType() {
    
    List<GrouperExternalSystem> result = new ArrayList<GrouperExternalSystem>();
    
    for (String configId : this.retrieveConfigurationConfigIds()) {
      
      @SuppressWarnings("unchecked")
      Class<GrouperExternalSystem> theClass = (Class<GrouperExternalSystem>)this.getClass();
      GrouperExternalSystem grouperExternalSystem = GrouperUtil.newInstance(theClass);
      grouperExternalSystem.setConfigId(configId);
      result.add(grouperExternalSystem);
    }
    
    return result;
  }
  
  /**
   * 
   * @return
   */
  public List<GrouperExternalSystemConsumer> retrieveAllUsedBy() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * call retrieveAttributes() to get this
   */
  private Map<String, GrouperExternalSystemAttribute> attributeCache = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperExternalSystem.class);

  public List<GrouperExternalSystemSubSection> getSubSections() {
    
    List<GrouperExternalSystemSubSection> results = new ArrayList<GrouperExternalSystemSubSection>();
    
    Set<String> sectionLabelsUsed = new HashSet<String>();
    
    for (GrouperExternalSystemAttribute grouperExternalSystemAttribute : this.retrieveAttributes().values()) {
      
      String sectionLabel = grouperExternalSystemAttribute.getConfigItemMetadata().getSubSection();
      if (StringUtils.isBlank(sectionLabel)) {
        sectionLabel = "NULL";
      }
      if (sectionLabelsUsed.contains(sectionLabel)) {
        continue;
      }
      sectionLabelsUsed.add(sectionLabel);
      
      GrouperExternalSystemSubSection grouperExternalSystemSection = new GrouperExternalSystemSubSection();
      grouperExternalSystemSection.setGrouperExternalSystem(this);
      grouperExternalSystemSection.setLabel(grouperExternalSystemAttribute.getConfigItemMetadata().getSubSection());
      
      results.add(grouperExternalSystemSection);
    }
    
    return results;
  }
  
  /**
   * 
   * @return get the attributes from config by suffix
   */
  public Map<String, GrouperExternalSystemAttribute> retrieveAttributes() {
    
    if (this.attributeCache != null) {
      return this.attributeCache;
    }
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperExternalSystemAttribute> result = new LinkedHashMap<String, GrouperExternalSystemAttribute>();

    Pattern pattern = Pattern.compile(this.getConfigIdRegex());
    
    for (ConfigSectionMetadata configSectionMetadata : configFileName.configFileMetadata().getConfigSectionMetadataList()) {
      for (ConfigItemMetadata configItemMetadata : configSectionMetadata.getConfigItemMetadataList()) {
        
        Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
        if (!matcher.matches()) {
          continue;
        }
        
        String prefix = matcher.group(1);
        String configId = this.getConfigId();
        if (StringUtils.isBlank(configId)) {
          throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
        }
        String suffix = matcher.group(3);
        String propertyName = prefix + "." + configId + "." + suffix;

        GrouperExternalSystemAttribute grouperExternalSystemAttribute = new GrouperExternalSystemAttribute();

        grouperExternalSystemAttribute.setFullPropertyName(propertyName);
        grouperExternalSystemAttribute.setGrouperExternalSystem(this);
        
        result.put(suffix, grouperExternalSystemAttribute);
        
        grouperExternalSystemAttribute.setConfigSuffix(suffix);

        grouperExternalSystemAttribute.setConfigItemMetadata(configItemMetadata);
        
        {
          boolean hasExpressionLanguage = configPropertiesCascadeBase.hasExpressionLanguage(propertyName);
          grouperExternalSystemAttribute.setExpressionLanguage(hasExpressionLanguage);

          if (hasExpressionLanguage) {
            String rawExpressionLanguage = configPropertiesCascadeBase.rawExpressionLanguage(propertyName);
            grouperExternalSystemAttribute.setExpressionLanguageScript(rawExpressionLanguage);
          }
        }
        if (DbConfigEngine.isPasswordHelper(configItemMetadata, configPropertiesCascadeBase.propertyValueString(propertyName))) {
          grouperExternalSystemAttribute.setValue(DbConfigEngine.ESCAPED_PASSWORD);
        } else {
          grouperExternalSystemAttribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
        }
        {
          // use the metadata
          grouperExternalSystemAttribute.setDefaultValue(configItemMetadata.getDefaultValue());
          grouperExternalSystemAttribute.setPassword(configItemMetadata.isSensitive());
          grouperExternalSystemAttribute.setRequired(configItemMetadata.isRequired());
          grouperExternalSystemAttribute.setType(configItemMetadata.getValueType());

          if (GrouperUtil.length(configItemMetadata.getOptionValues()) > 0) {
            List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
            for (String value : configItemMetadata.getOptionValues()) {
              
              String label = GrouperTextContainer.textOrNull("externalSystem." 
                  + this.getClass().getSimpleName() + ".attribute.option." + grouperExternalSystemAttribute.getConfigSuffix() + "." + value + ".label");
              label = StringUtils.defaultIfBlank(label, value);
              
              MultiKey valueAndLabel = new MultiKey(value, label);
              valuesAndLabels.add(valueAndLabel);
            }
            grouperExternalSystemAttribute.setDropdownValuesAndLabels(valuesAndLabels);
          }

          if (grouperExternalSystemAttribute.isPassword()) {
            grouperExternalSystemAttribute.setPassword(true);
            grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.PASSWORD);
          } else {
            
            ConfigItemFormElement configItemFormElement = configItemMetadata.getFormElement();
            if (configItemFormElement != null) {
              grouperExternalSystemAttribute.setFormElement(configItemFormElement);
            } else {
              
              // boolean is a drop down
              if (configItemMetadata.getValueType() == ConfigItemMetadataType.BOOLEAN) {
                
                grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.DROPDOWN);

                if (GrouperUtil.length(grouperExternalSystemAttribute.getDropdownValuesAndLabels()) == 0) {
                  
                  List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
                  valuesAndLabels.add(new MultiKey("", ""));
                  
                  
                  String trueLabel = GrouperTextContainer.textOrNull("externalSystem." 
                      + this.getClass().getSimpleName() + ".attribute.option." + grouperExternalSystemAttribute.getConfigSuffix() + ".trueLabel");
                  
                  trueLabel = GrouperUtil.defaultIfBlank(trueLabel, GrouperTextContainer.textOrNull("externalSystem.defaultTrueLabel"));

                  String falseLabel = GrouperTextContainer.textOrNull("externalSystem." 
                      + this.getClass().getSimpleName() + ".attribute.option." + grouperExternalSystemAttribute.getConfigSuffix() + ".falseLabel");
                  
                  falseLabel = GrouperUtil.defaultIfBlank(falseLabel, GrouperTextContainer.textOrNull("externalSystem.defaultFalseLabel"));
                  
                  valuesAndLabels.add(new MultiKey("true", trueLabel));
                  valuesAndLabels.add(new MultiKey("false", falseLabel));
                  grouperExternalSystemAttribute.setDropdownValuesAndLabels(valuesAndLabels);
                }
              } else if (GrouperUtil.length(grouperExternalSystemAttribute.getValue()) > 100) {

                grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.TEXTAREA);

              } else {
                
                grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.TEXT);

              }
            }
          }
        }
      }
    }
    this.attributeCache = result;
    return result;
  }
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public boolean isEnabled() {
   try {
     GrouperExternalSystemAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
     String enabledString = enabledAttribute.getValue();
     if (StringUtils.isBlank(enabledString)) {
       enabledString = enabledAttribute.getDefaultValue();
     }
     
     return GrouperUtil.booleanValue(enabledString, true);
   } catch (Exception e) {
     return false;
   }
    
  }
  
  
  public String propertiesApiProperyValue(String attributeName) {
    
    return this.getConfigFileName().getConfig().propertyValueString(this.getConfigItemPrefix()+attributeName);
  }

  
  /**
   * which config file this is in
   * @return the config file
   */
  public abstract ConfigFileName getConfigFileName();
  
  /**
   * if any config in the file has this prefix, then its related.  includes the config id and dot.
   * e.g. for ldap this is a property ldap.personLdap.url and this is the prefix: ldap.personLdap.
   * the prefix concatenated with the suffix the the config item key
   * @return the prefix
   */
  public abstract String getConfigItemPrefix();
  
  /**
   * get the config id regex. This is a regex that will return the configId, and will do that for all properties
   * for ldap.personLdap.url, the regex is ^(ldap)\.([^.]+)\.(.*)$
   * The first group in regex should be prefix excluding dot.  the second group is the config id.  The third group should be the suffix after that
   * @return the regex
   */
  public abstract String getConfigIdRegex();
  
  /**
   * regex like: 
   * @param regex
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
      
      Matcher matcher = pattern.matcher(propertyName);
      
      if (!matcher.matches()) {
        continue;
      }

      String configId = matcher.group(2);
      result.add(configId);
    }
    return result;
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
  
  
  public final static Set<String> externalTypeClassNames = new LinkedHashSet<String>();
  static {
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.azure.GrouperExternalSystemAzure");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.externalSystem.LdapGrouperExternalSystem");
  }
  
  public static List<GrouperExternalSystem> retrieveAllGrouperExternalSystemTypes() {
    
    List<GrouperExternalSystem> result = new ArrayList<GrouperExternalSystem>();
    
    for (String className: externalTypeClassNames) {       
      
      Class<GrouperExternalSystem> externalSystemClass = (Class<GrouperExternalSystem>) GrouperUtil.forName(className);
      GrouperExternalSystem externalSystem = GrouperUtil.newInstance(externalSystemClass);
      result.add(externalSystem);
    }
    
    return result;
  }

  
  public static List<GrouperExternalSystem> retrieveAllGrouperExternalSystems() {
    
    List<GrouperExternalSystem> result = new ArrayList<GrouperExternalSystem>();
    
    for (String className: externalTypeClassNames) {       
        Class<GrouperExternalSystem> externalSystemClass = (Class<GrouperExternalSystem>) GrouperUtil.forName(className);
        GrouperExternalSystem externalSystem = GrouperUtil.newInstance(externalSystemClass);
        result.addAll(externalSystem.listAllExternalSystemsOfThisType());
    }
    
    return result;
  }
}
