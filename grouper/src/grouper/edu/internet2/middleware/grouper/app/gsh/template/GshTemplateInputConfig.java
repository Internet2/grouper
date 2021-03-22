package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GshTemplateInputConfig {
  
  private GshTemplateConfig gshTemplateConfig;
  
  private String name;
  
  private GshTemplateInputType gshTemplateInputType;
  
  private GshTemplateInputValidationType gshTemplateInputValidationType;
  
  private String validationRegex;
  
  private String validationJexl;
  
  private ValidationBuiltinType validationBuiltinType;
  
  private boolean required;
  
  private boolean trimWhitespace = true;
  
  private String defaultValue;
  
  private String validationMessage;
  
  private String validationMessageExternalizedTextKey;
  
  private Integer maxLength;
  
  private ConfigItemFormElement configItemFormElement;
  
  private GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType;
  
  private String dropdownCsvValue;
  
  private String dropdownJsonValue;
  
  private String dropdownJavaClassValue;
  
  private String labelExternalizedTextKey;
  
  private String label;
  
  private String descriptionExternalizedTextKey;
  
  private String description;
  
  private String showEl;
  
  private int index;

  public String getDropdownValueBasedOnType() {
    
    if (StringUtils.isNotBlank(dropdownCsvValue)) {
      return dropdownCsvValue;
    } else if (StringUtils.isNotBlank(dropdownJsonValue)) {
      return dropdownJsonValue;
    } else {
      return dropdownJavaClassValue;
    }
    
  }
  
  private boolean useExternalizedText;
  
  
  public boolean isUseExternalizedText() {
    return useExternalizedText;
  }

  
  public void setUseExternalizedText(boolean useExternalizedText) {
    this.useExternalizedText = useExternalizedText;
  }

  public String getLabelForUi() {
    if (!this.useExternalizedText) {
      return this.label;
    } else {
      return GrouperTextContainer.textOrNull(labelExternalizedTextKey);
    }
  }
  
  public String getDescriptionForUi() {
    if (!this.useExternalizedText) {
      return this.description;
    } else {
      return GrouperTextContainer.textOrNull(descriptionExternalizedTextKey);
    }
  }
  
  
  public List<MultiKey> getDropdownKeysAndLabels() {
    List<MultiKey> dropdownKeysAndLabels = new ArrayList<MultiKey>();
    
    if (this.getConfigItemFormElement() == ConfigItemFormElement.DROPDOWN) {
      dropdownKeysAndLabels.add(new MultiKey("", ""));
      dropdownKeysAndLabels.addAll(this.getGshTemplateDropdownValueFormatType().retrieveKeysAndLabels(this.getDropdownValueBasedOnType()));
      return dropdownKeysAndLabels;
    } else if (this.getConfigItemFormElement() == ConfigItemFormElement.RADIOBUTTON) {
      
      String trueLabel = GrouperTextContainer.textOrNull("config.defaultTrueLabel");
      String falseLabel = GrouperTextContainer.textOrNull("config.defaultFalseLabel");
      
      if (StringUtils.isNotBlank(defaultValue)) {
        
        Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(defaultValue);
        if (booleanObjectValue != null) {
          String defaultValueStr = booleanObjectValue ? "("+trueLabel+")" : "("+falseLabel+")"; 
          dropdownKeysAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel")+" " + defaultValueStr ));
        }
      }
      
      
      dropdownKeysAndLabels.add(new MultiKey("true", trueLabel));
      dropdownKeysAndLabels.add(new MultiKey("false", falseLabel));
      return dropdownKeysAndLabels;
    }
    
    return dropdownKeysAndLabels;
  }
  
  public String getDescriptionExternalizedTextKey() {
    return descriptionExternalizedTextKey;
  }


  
  public void setDescriptionExternalizedTextKey(String descriptionExternalizedTextKey) {
    this.descriptionExternalizedTextKey = descriptionExternalizedTextKey;
  }


  
  public String getDescription() {
    return description;
  }


  
  public void setDescription(String description) {
    this.description = description;
  }


  public String getLabelExternalizedTextKey() {
    return labelExternalizedTextKey;
  }



  
  public void setLabelExternalizedTextKey(String labelExternalizedTextKey) {
    this.labelExternalizedTextKey = labelExternalizedTextKey;
  }



  
  public String getLabel() {
    return label;
  }



  
  public void setLabel(String label) {
    this.label = label;
  }



  public String getDropdownCsvValue() {
    return dropdownCsvValue;
  }



  
  public void setDropdownCsvValue(String dropdownCsvValue) {
    this.dropdownCsvValue = dropdownCsvValue;
  }



  
  public String getDropdownJsonValue() {
    return dropdownJsonValue;
  }



  
  public void setDropdownJsonValue(String dropdownJsonValue) {
    this.dropdownJsonValue = dropdownJsonValue;
  }



  
  public String getDropdownJavaClassValue() {
    return dropdownJavaClassValue;
  }



  
  public void setDropdownJavaClassValue(String dropdownJavaClassValue) {
    this.dropdownJavaClassValue = dropdownJavaClassValue;
  }

  public GshTemplateDropdownValueFormatType getGshTemplateDropdownValueFormatType() {
    return gshTemplateDropdownValueFormatType;
  }

  
  public ConfigItemFormElement getConfigItemFormElement() {
    return configItemFormElement;
  }


  
  public void setConfigItemFormElement(ConfigItemFormElement configItemFormElement) {
    this.configItemFormElement = configItemFormElement;
  }


  public void setGshTemplateDropdownValueFormatType(GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType) {
    this.gshTemplateDropdownValueFormatType = gshTemplateDropdownValueFormatType;
  }


  public GshTemplateConfig getGshTemplateConfig() {
    return gshTemplateConfig;
  }

  
  public void setGshTemplateConfig(GshTemplateConfig gshTemplateConfig) {
    this.gshTemplateConfig = gshTemplateConfig;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public GshTemplateInputType getGshTemplateInputType() {
    return gshTemplateInputType;
  }

  
  public void setGshTemplateInputType(GshTemplateInputType gshTemplateInputType) {
    this.gshTemplateInputType = gshTemplateInputType;
  }

  
  public GshTemplateInputValidationType getGshTemplateInputValidationType() {
    return gshTemplateInputValidationType;
  }

  
  public void setGshTemplateInputValidationType(
      GshTemplateInputValidationType gshTemplateInputValidationType) {
    this.gshTemplateInputValidationType = gshTemplateInputValidationType;
  }

  
  public String getValidationRegex() {
    return validationRegex;
  }

  
  public void setValidationRegex(String validationRegex) {
    this.validationRegex = validationRegex;
  }

  
  public String getValidationJexl() {
    return validationJexl;
  }

  
  public void setValidationJexl(String validationJexl) {
    this.validationJexl = validationJexl;
  }

  
  public boolean isRequired() {
    return required;
  }

  
  public void setRequired(boolean required) {
    this.required = required;
  }

  
  public String getDefaultValue() {
    return defaultValue;
  }

  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }


  
  public ValidationBuiltinType getValidationBuiltinType() {
    return validationBuiltinType;
  }


  
  public void setValidationBuiltinType(ValidationBuiltinType validationBuiltinType) {
    this.validationBuiltinType = validationBuiltinType;
  }


  
  public boolean isTrimWhitespace() {
    return trimWhitespace;
  }


  
  public void setTrimWhitespace(boolean trimWhitespace) {
    this.trimWhitespace = trimWhitespace;
  }


  
  public String getValidationMessage() {
    return validationMessage;
  }


  
  public void setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
  }


  
  public String getValidationMessageExternalizedTextKey() {
    return validationMessageExternalizedTextKey;
  }


  
  public void setValidationMessageExternalizedTextKey(
      String validationMessageExternalizedTextKey) {
    this.validationMessageExternalizedTextKey = validationMessageExternalizedTextKey;
  }


  
  public Integer getMaxLength() {
    return maxLength;
  }


  
  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }


  
  public String getShowEl() {
    return showEl;
  }
  
  public void setShowEl(String showEl) {
    this.showEl = showEl;
  }


  
  public int getIndex() {
    return index;
  }


  
  public void setIndex(int index) {
    this.index = index;
  }
  
  
}
