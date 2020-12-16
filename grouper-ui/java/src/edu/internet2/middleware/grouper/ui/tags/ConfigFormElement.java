package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringUtils;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * shows the label, EL checkbox, and html form element for a config attribute
 */
public class ConfigFormElement extends SimpleTagSupport {
  
  /**
   * id of the config
   */
  private String configId;
  
  /**
   * value to display/save 
   */
  private String value;
  
  /**
   * helper text default value (eg: false, 5, 'abc')
   */
  private String helperTextDefaultValue;
  
  /**
   * is the field required
   */
  private Boolean required = false;
  
  /**
   * is the field read only
   */
  private Boolean readOnly = false;
  
  /**
   * helper text to display under the field
   */
  private String helperText;
  
  /**
   * label to display to the left of the field 
   */
  private String label;
  
  /**
   * form element type (eg: TEXT, TEXTAREA)
   */
  private String formElementType;
  
  /**
   * only applicable to dropdown
   */
  private List<MultiKey> valuesAndLabels;
  
  
  /**
   * only applicable to checkboxes
   */
  private List<MultiKey> checkboxAttributes;
  
  /**
   * ajaxCallback for onchange etc
   */
  private String ajaxCallback;
  
  /**
   * should the form element be rendered 
   */
  private Boolean shouldShow = true;
  
  /**
   * does the value have expression language
   */
  private Boolean hasExpressionLanguage = false;
  
  /**
   * only applicable to dropdown
   * @return
   */
  public List<MultiKey> getValuesAndLabels() {
    return valuesAndLabels;
  }

  /**
   * only applicable to dropdown
   * @param valuesAndLabels
   */
  public void setValuesAndLabels(List<MultiKey> valuesAndLabels) {
    this.valuesAndLabels = valuesAndLabels;
  }

  /**
   * only applicable to checkboxes
   * @return
   */
  public List<MultiKey> getCheckboxAttributes() {
    return checkboxAttributes;
  }

  /**
   * only applicable to checkboxes
   * @param checkboxAttributes
   */
  public void setCheckboxAttributes(List<MultiKey> checkboxAttributes) {
    this.checkboxAttributes = checkboxAttributes;
  }

  /**
   * does the value have expression language
   * @return
   */
  public Boolean getHasExpressionLanguage() {
    return hasExpressionLanguage;
  }

  /**
   * does the value have expression language
   * @param hasExpressionLanguage
   */
  public void setHasExpressionLanguage(Boolean hasExpressionLanguage) {
    this.hasExpressionLanguage = hasExpressionLanguage;
  }

  /**
   * should the form element be rendered
   * @return
   */
  public Boolean getShouldShow() {
    return shouldShow;
  }

  /**
   * should the form element be rendered
   * @param shouldShow
   */
  public void setShouldShow(Boolean shouldShow) {
    this.shouldShow = shouldShow;
  }

  /**
   * id of the config
   * @return
   */
  public String getConfigId() {
    return configId;
  }

  /**
   * id of the config
   * @param configId
   */
  public void setConfigId(String configId) {
    this.configId = configId;
  }
  
  /**
   * value to display/save  
   * @return
   */
  public String getValue() {
    return value;
  }

  /**
   * value to display/save 
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * helper text default value (eg: false, 5, 'abc')
   * @return
   */
  public String getHelperTextDefaultValue() {
    return helperTextDefaultValue;
  }

  /**
   * helper text default value (eg: false, 5, 'abc')
   * @param helperTextDefaultValue
   */
  public void setHelperTextDefaultValue(String helperTextDefaultValue) {
    this.helperTextDefaultValue = helperTextDefaultValue;
  }

  /**
   * is the field required
   * @return
   */
  public Boolean getRequired() {
    return required;
  }


  /**
   * is the field required
   * @param required
   */
  public void setRequired(Boolean required) {
    this.required = required;
  }
  
  
  /**
   * is the field read only
   * @return
   */
  public Boolean getReadOnly() {
    return readOnly;
  }

  /**
   * is the field read only
   * @param readOnly
   */
  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * helper text to display under the field
   * @return
   */
  public String getHelperText() {
    return helperText;
  }

  /**
   * helper text to display under the field
   * @param helperText
   */
  public void setHelperText(String helperText) {
    this.helperText = helperText;
  }

  /**
   * label to display to the left of the field
   * @return
   */
  public String getLabel() {
    return label;
  }

  /**
   * label to display to the left of the field
   * @param label
   */
  public void setLabel(String label) {
    this.label = label;
  }


  /**
   * form element type (eg: TEXT, TEXTAREA)
   * @return
   */
  public String getFormElementType() {
    return formElementType;
  }

  /**
   * form element type (eg: TEXT, TEXTAREA)
   * @param formElementType
   */
  public void setFormElementType(String formElementType) {
    this.formElementType = formElementType;
  }

  /**
   * ajaxCallback for onchange etc
   * @return
   */
  public String getAjaxCallback() {
    return ajaxCallback;
  }

  /**
   * ajaxCallback for onchange etc
   * @param ajaxCallback
   */
  public void setAjaxCallback(String ajaxCallback) {
    this.ajaxCallback = ajaxCallback;
  }

  /**
   * html to render
   */
  @Override
  public void doTag() throws JspException, IOException {
   
    StringBuilder field = new StringBuilder();
    
    field.append("<tr id='configRow_"+configId+"_id' " + (shouldShow ? "" : " style='display:none' ") + ">");
    field.append("<td style='vertical-align: top; white-space: nowrap;'>");
    field.append("<strong><label for='config_"+configId+"_id'>");
    field.append(label);
    field.append("</label></strong></td>");
    
    field.append("<td style='vertical-align: top; white-space: nowrap;' >");
    
    if (!readOnly) {
      field.append("<input style='vertical-align: top; min-height: 10px; margin-right: 2px;' type='checkbox' ");
      field.append("name='config_el_"+configId+"' ");
      
      if (hasExpressionLanguage) {
        field.append(" checked ");
      }
          
      field.append("onchange=\""+ajaxCallback+"\"");
      field.append("</input><span rel='tooltip' title='" + GrouperUtil.xmlEscape(GrouperTextContainer.textOrNull("grouperConfigIsElTooltip")) + "' style='border-bottom: 1px dotted #000;'>");
      field.append(GrouperTextContainer.textOrNull("grouperConfigIsElLabel"));
      field.append("</span>");
    }
    field.append("</td>");
    
    field.append("<td>");
    
    ConfigItemFormElement configItemFormElement = ConfigItemFormElement.valueOfIgnoreCase(formElementType, true);
    
    String displayClass = "";
    if (readOnly) {
      field.append(GrouperUtil.escapeHtml(value, true));
      displayClass = " display: none; ";
    }
    
    if (configItemFormElement == ConfigItemFormElement.TEXT) {
      
      field.append(
          "<input style='width:30em; "+ displayClass + "' type='text' id='config_"+configId+"_id' name='config_" + configId + "'");
      if (value != null) {
        field.append(" value = '"+GrouperUtil.escapeHtml(value, true)+"'");
      }
      field.append("></input>");
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.TEXTAREA) {
            
      field.append("<textarea style='width:30em; "+ displayClass + "' cols='20' rows='3' id='config_"+configId+"_id' name='config_"
          + configId + "'>");
      if (value != null) {
        field.append(GrouperUtil.escapeHtml(value, true));
      }
      field.append("</textarea>");
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.PASSWORD) {
      
      field.append(
          "<input style='width:30em; "+ displayClass + "' type='password' id='config_"+configId+"_id' name= 'config_" + configId + "'");
      if (value != null) {
        field.append(" value = '"+GrouperUtil.escapeHtml(value, true)+"'");
      }
      field.append("></input>");
    }
    
    if (configItemFormElement == ConfigItemFormElement.DROPDOWN) {
      
      field.append("<select style='width:30em; "+ displayClass + "' id='config_"+configId+"_id' name='config_"+configId+"' ");
      
      field.append("onchange=\""+ajaxCallback+"\"");
      field.append(">");
      
      for (MultiKey multiKey: valuesAndLabels) {
        
        String key = (String) multiKey.getKey(0);
        String optionValue = (String) multiKey.getKey(1);
        
        boolean selected = StringUtils.equals(key, value);
        
        field.append("<option value='"+key+"'" + (selected ? " selected='selected'" : "") + ">");
        field.append(GrouperUtil.escapeHtml(optionValue, true));
        field.append("</option>");
      }
      
      field.append("</select>");
    }
    
    if (configItemFormElement == ConfigItemFormElement.RADIOBUTTON) {
      boolean firstOption = true;
      for (MultiKey multiKey: valuesAndLabels) {
        
        String key = (String) multiKey.getKey(0);
        String radioButtonValue = (String) multiKey.getKey(1);
        boolean checked = StringUtils.equals(key, value);

        field.append("<input type='radio' style='margin-right:3px;margin-top:0px; "+ displayClass+"' id='config_"+configId+"_id' name='config_"+configId+"' value='"+key+"' ");
        field.append(checked ? " checked ": "");
        field.append("onchange=\""+ajaxCallback+"\"");
        field.append(">");
        field.append("</input>");
        
        if (firstOption) {
          firstOption = false;
          field.append("<span style='display: inline-block; width: 120px;'>"+radioButtonValue+"</span>");
        } else {
          field.append("<span style='margin-right: 10px;'>"+radioButtonValue+"</span>"); 
        }
      }
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.CHECKBOX) {
      
      String[] selectedValuesArray = value != null ? value.split(","): new String[] {};
      
      boolean isValueProvided = StringUtils.isNotBlank(value);
      
      List<String> selectedValues =  Arrays.asList(selectedValuesArray);
      
      for (MultiKey multiKey: checkboxAttributes) {
        
        String value = (String) multiKey.getKey(0);
        String label = (String) multiKey.getKey(1);
        boolean checked = (boolean) multiKey.getKey(2);
        
        field.append("<input type='checkbox' style='"+ displayClass + "' id='"+value+"_id' name='config_"+configId+"' ");
        if (value != null) {
          field.append(" value = '"+value+"'");
        }
        
        if (isValueProvided) {
          if (selectedValues.contains(value)) {
            field.append(" checked ");
          }
        } else if (checked) {
          field.append(" checked ");
        }
        
        field.append("></input>");
        field.append("&nbsp; &nbsp; <label for '"+value+"_id'>");
        field.append(label);
        field.append("</label>");
        field.append("<br>");
      }
      
    }
    
    if (!readOnly && required) {
      field.append("<span class='requiredField' rel='tooltip' data-html='true' data-delay-show='200' data-placement='right'>*");
      field.append("</span>");
    }
    
    field.append("<br>");
    field.append("<span class='description'>");
    if (StringUtils.isNotBlank(helperText)) {      
      field.append(helperText);
    }
    if (StringUtils.isNotBlank(helperTextDefaultValue)) {
      if (helperText.endsWith(".") == false) {
        field.append(".");
      }
      field.append(" ").append(GrouperTextContainer.textOrNull("grouperConfigDefaultValueHintPrefix"))
      .append(" '").append(helperTextDefaultValue).append("'.");
    }
    
    field.append("</span>");
    
    field.append("</td>");
    field.append("</tr>");
    
    this.getJspContext().getOut().print(field.toString());
  }

}
