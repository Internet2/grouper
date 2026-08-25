package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
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
   * should show EL checkbox
   */
  private Boolean shouldShowElCheckbox = true;
  
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
   * should show EL checkbox
   * @return
   */
  public Boolean getShouldShowElCheckbox() {
    return shouldShowElCheckbox;
  }

  /**
   * should show EL checkbox
   * @param shouldShowElCheckbox
   */
  public void setShouldShowElCheckbox(Boolean shouldShowElCheckbox) {
    this.shouldShowElCheckbox = shouldShowElCheckbox;
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
   * number of levels to indent
   */
  private Integer indent;
  
  /**
   * number of levels to indent
   * @return
   */
  public Integer getIndent() {
    return indent;
  }

  /**
   * number of levels to indent
   * @param indent
   */
  public void setIndent(Integer indent) {
    this.indent = indent;
  }

  /**
   * html to render
   */
  @Override
  public void doTag() throws JspException, IOException {
   
    StringBuilder field = new StringBuilder();
    if (!shouldShow) {
      this.getJspContext().getOut().print(field.toString());
      return;
    }
    
    field.append("<tr id='configRow_"+configId+"_id' " + (shouldShow ? "" : " style='display:none' ") + ">");
    field.append("<td style='vertical-align: top; white-space: nowrap;");
    if (this.indent != null && this.indent > 0) {
      field.append("padding-left: " + (2*this.indent) + "em;");
    }
    field.append("'>");
    field.append("<strong>");
    if (!readOnly) {
      field.append("<label for='config_"+configId+"_id'>");
    }
    field.append(label);
    if (!readOnly) {
      field.append("</label>");
    }
    field.append("</strong></td>");
      
    
    if (shouldShowElCheckbox) {
      field.append("<td style='vertical-align: top; white-space: nowrap;' >");
      
      if (!readOnly) {
        field.append("<input class='config-el-checkbox' type='checkbox' ");
        field.append("name='config_el_"+configId+"' ");
        field.append("aria-label='" + GrouperUtil.xmlEscape(GrouperTextContainer.textOrNull("grouperConfigIsElLabel")) + "' ");
        
        if (hasExpressionLanguage) {
          field.append(" checked ");
        }
            
        field.append("onchange=\""+ajaxCallback+"\">");
        field.append("</input><span rel='tooltip' class='config-el-label' title='" + GrouperUtil.xmlEscape(GrouperTextContainer.textOrNull("grouperConfigIsElTooltip")) + "'>");
        field.append(GrouperTextContainer.textOrNull("grouperConfigIsElLabel"));
        field.append("</span>");
      }
      field.append("</td>");
    }
    
    field.append("<td><span style=\"white-space: nowrap\" id=\"config_"+configId+"_spanid\">");
    
    ConfigItemFormElement configItemFormElement = ConfigItemFormElement.valueOfIgnoreCase(formElementType, true);
    
    String displayClass = "";
    if (readOnly) {
      
      if (configItemFormElement != ConfigItemFormElement.RADIOBUTTON && 
          configItemFormElement != ConfigItemFormElement.DROPDOWN) {
        field.append(GrouperUtil.escapeHtml(value, true));
      }
      
      displayClass = " display: none; ";
    }

    // when read-only the label above is not rendered, so give the (hidden) control its own accessible name
    String readOnlyAriaLabel = (readOnly && StringUtils.isNotBlank(label)) ? " aria-label='" + GrouperUtil.xmlEscape(label) + "' " : "";

    if (configItemFormElement == ConfigItemFormElement.TEXT) {
      
      field.append(
          "<input data-gr-input-type='text' style='width:30em; "+ displayClass + "' type='text' id='config_"+configId+"_id' name='config_" + configId + "'");
      field.append(readOnlyAriaLabel);
      if (value != null) {
        field.append(" value = '"+GrouperUtil.escapeHtml(value, true)+"'");
      }
      field.append("></input>");
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.GROUPCOMBOBOX) {

      if (!readOnly) {
        // config suffixes contain dots; the submitted name must keep them (server reads config_<suffix>Name),
        // but the DOM id must be dot-free so jQuery/querySelector (used by TomSelect) work
        String submitBase = "config_" + configId;
        String domIdBase = submitBase.replaceAll("[^A-Za-z0-9_]", "_");
        String filterOperation = "../app/UiV2Group.groupUpdateFilter";
        String valueEscaped = value == null ? "" : StringUtils.replace(value, "'", "\\'");
        String ariaLabel = StringUtils.isNotBlank(label) ? " aria-label='" + GrouperUtil.xmlEscape(label) + "' " : "";

        // Resolve a friendly display label for the stored value so it renders even when the
        // async combobox lookup returns nothing (e.g. an existing rule that stored a group name).
        // Fall back to the raw value, which is exactly what the old text field showed.
        String valueLabel = value;
        if (StringUtils.isNotBlank(value)) {
          try {
            GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
            Group theGroup = GroupFinder.findByName(grouperSession, value, false);
            if (theGroup == null) {
              theGroup = GroupFinder.findByUuid(grouperSession, value, false);
            }
            if (theGroup != null) {
              valueLabel = theGroup.getDisplayName();
            }
          } catch (Exception e) {
            // leave valueLabel as the raw value
          }
        }
        String valueLabelEscaped = StringUtils.replace(StringUtils.defaultString(valueLabel), "'", "\\'");

        boolean disableEnter = GrouperUiConfig.retrieveConfig()
            .propertyValueBoolean("grouperUi.disableEnterKeyOnCombobox", false);
        boolean useEnterForLookup = !disableEnter;

        field.append("<input type='text' id='" + domIdBase + "Id' name='" + submitBase + "Name' autocomplete='off' style='width:30em;'");
        field.append(ariaLabel);
        field.append(" />");
        field.append("<input id='" + domIdBase + "IdDisplay' name='" + submitBase + "NameDisplay' type='hidden' value='' />");
        field.append("<span id='" + domIdBase + "ErrorId'></span>");
        field.append("<script>");
        field.append("$(document).ready(function(){");
        field.append("grouperRegisterCombobox('#" + domIdBase + "Id', '" + filterOperation + "', null, "
            + (StringUtils.isBlank(value) ? "null" : "'" + valueEscaped + "'")
            + ", {searchDelay: null, useEnterForLookup: " + (useEnterForLookup ? "true" : "false")
            + (StringUtils.isBlank(value) ? "" : ", valueLabel: '" + valueLabelEscaped + "'")
            + "});");
        field.append("});");
        field.append("</script>");
      }

    }

    if (configItemFormElement == ConfigItemFormElement.STEMCOMBOBOX) {

      if (!readOnly) {
        // config suffixes contain dots; the submitted name must keep them (server reads config_<suffix>Name),
        // but the DOM id must be dot-free so jQuery/querySelector (used by TomSelect) work
        String submitBase = "config_" + configId;
        String domIdBase = submitBase.replaceAll("[^A-Za-z0-9_]", "_");
        String filterOperation = "../app/UiV2Stem.createStemParentFolderFilter";
        String valueEscaped = value == null ? "" : StringUtils.replace(value, "'", "\\'");
        String ariaLabel = StringUtils.isNotBlank(label) ? " aria-label='" + GrouperUtil.xmlEscape(label) + "' " : "";

        // Resolve a friendly display label for the stored value so it renders even when the
        // async combobox lookup returns nothing (e.g. an existing rule that stored a folder name).
        // Fall back to the raw value, which is exactly what the old text field showed.
        String valueLabel = value;
        if (StringUtils.isNotBlank(value)) {
          try {
            GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
            Stem theStem = StemFinder.findByName(grouperSession, value, false);
            if (theStem == null) {
              theStem = StemFinder.findByUuid(grouperSession, value, false);
            }
            if (theStem != null) {
              valueLabel = theStem.getDisplayName();
            }
          } catch (Exception e) {
            // leave valueLabel as the raw value
          }
        }
        String valueLabelEscaped = StringUtils.replace(StringUtils.defaultString(valueLabel), "'", "\\'");

        boolean disableEnter = GrouperUiConfig.retrieveConfig()
            .propertyValueBoolean("grouperUi.disableEnterKeyOnCombobox", false);
        boolean useEnterForLookup = !disableEnter;

        field.append("<input type='text' id='" + domIdBase + "Id' name='" + submitBase + "Name' autocomplete='off' style='width:30em;'");
        field.append(ariaLabel);
        field.append(" />");
        field.append("<input id='" + domIdBase + "IdDisplay' name='" + submitBase + "NameDisplay' type='hidden' value='' />");
        field.append("<span id='" + domIdBase + "ErrorId'></span>");
        field.append("<script>");
        field.append("$(document).ready(function(){");
        field.append("grouperRegisterCombobox('#" + domIdBase + "Id', '" + filterOperation + "', null, "
            + (StringUtils.isBlank(value) ? "null" : "'" + valueEscaped + "'")
            + ", {searchDelay: null, useEnterForLookup: " + (useEnterForLookup ? "true" : "false")
            + (StringUtils.isBlank(value) ? "" : ", valueLabel: '" + valueLabelEscaped + "'")
            + "});");
        field.append("});");
        field.append("</script>");
      }

    }

    if (configItemFormElement == ConfigItemFormElement.TEXTAREA) {
            
      field.append("<textarea data-gr-input-type='textarea' style='width:30em; "+ displayClass + "' cols='20' rows='3' id='config_"+configId+"_id' name='config_"
          + configId + "'" + readOnlyAriaLabel + ">");
      if (value != null) {
        field.append(GrouperUtil.escapeHtml(value, true));
      }
      field.append("</textarea>");
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.FILE) {
      
      field.append("<input type='file' data-gr-input-type='file' style='width:30em; "+ displayClass + "' cols='20' rows='3' id='config_"+configId+"_id' name='config_"
          + configId + "'" + readOnlyAriaLabel + ">");
      if (value != null) {
        field.append(GrouperUtil.escapeHtml(value, true));
      }
      field.append("</input>");
    }
    
    if (configItemFormElement == ConfigItemFormElement.PASSWORD) {
      
      field.append(
          "<input style='width:30em; "+ displayClass + "' data-gr-input-type='password' type='password' id='config_"+configId+"_id' name= 'config_" + configId + "'");
      field.append(readOnlyAriaLabel);
      if (value != null) {
        field.append(" value = '"+GrouperUtil.escapeHtml(value, true)+"'");
      }
      field.append("></input>");
    }
    
    if (configItemFormElement == ConfigItemFormElement.DROPDOWN) {
      
      if (readOnly) {
        for (MultiKey multiKey: valuesAndLabels) {

          if (multiKey.size() <= 2) {
            String key = GrouperUtil.stringValue(multiKey.getKey(0));
            String optionValue = GrouperUtil.stringValue(multiKey.getKey(1));

            boolean selected = StringUtils.equals(key, value);
            if (!selected) {
              continue;
            }
            field.append("<span style='margin-right: 10px;'>" + optionValue + "</span>");
          }
        }
      } else {
        field.append("<select data-gr-input-type='select' style='width:30em; "+ displayClass + "' id='config_"+configId+"_id' name='config_"+configId+"' ");
        
        field.append("onchange=\""+ajaxCallback+"\"");
        field.append(">");
        
        for (MultiKey multiKey: valuesAndLabels) {

          if (multiKey.size() > 2) {
            field.append(GrouperUtil.stringValue(multiKey.getKey(2)));
          } else {
            String key = GrouperUtil.stringValue(multiKey.getKey(0));
            String optionValue = GrouperUtil.stringValue(multiKey.getKey(1));

            boolean selected = StringUtils.equals(key, value);

            field.append("<option value='" + GrouperUtil.escapeHtml(key, true) + "'" + (selected ? " selected='selected'" : "") + ">");
            field.append(GrouperUtil.escapeHtml(optionValue, true));
            field.append("</option>");
          }
        }
        
        field.append("</select>");
      }
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.RADIOBUTTON) {
      boolean firstOption = true;
      
      if (readOnly) {
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = GrouperUtil.stringValue(multiKey.getKey(0));
          String radioButtonValue = GrouperUtil.stringValue(multiKey.getKey(1));
          boolean checked = StringUtils.equals(key, value);
          if (!checked) {
            continue;
          }
          field.append("<span style='margin-right: 10px;'>"+radioButtonValue+"</span>"); 
        }
      } else {
        int index = 0;
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = GrouperUtil.stringValue(multiKey.getKey(0));
          String radioButtonValue = GrouperUtil.stringValue(multiKey.getKey(1));
          boolean checked = StringUtils.equals(key, value);

          field.append("<input type='radio' class='config-radio-button' style='"+ displayClass+"' id='config_"+configId+(index==0?"":Integer.toString(index))+"_id' name='config_"+configId+"' value='"+key+"' ");
          field.append("aria-label='"+GrouperUtil.xmlEscape(radioButtonValue)+"' ");
          field.append(checked ? " checked ": "");
          field.append("onchange=\""+ajaxCallback+"\"");
          field.append(">");
          field.append("</input>");
          
          if (firstOption) {
            firstOption = false;
            field.append("<span class='config-first-radio-button-label'>"+radioButtonValue+"</span>");
          } else {
            field.append("<span class='config-radio-button-label'>"+radioButtonValue+"</span>"); 
          }
          
          index++;
        }
      }
    }
    
    if (configItemFormElement == ConfigItemFormElement.CHECKBOX) {
      
      String[] selectedValuesArray = value != null ? value.split(","): new String[] {};
      
      boolean isValueProvided = StringUtils.isNotBlank(value);
      
      List<String> selectedValues =  Arrays.asList(selectedValuesArray);
      
      for (MultiKey multiKey: checkboxAttributes) {
        
        String value = GrouperUtil.stringValue(multiKey.getKey(0));
        String label = GrouperUtil.stringValue(multiKey.getKey(1));
        boolean checked = (boolean) multiKey.getKey(2);
        
        // why is the "id" the value and not a generic thing?  maybe for javascript?  hmmm
        field.append("<input type='checkbox' style='"+ displayClass + "' id='"+GrouperUtil.escapeHtml(value, true)+"_id' name='config_"+configId+"' ");
        if (value != null) {
          field.append(" value = '"+GrouperUtil.escapeHtml(value, true)+"'");
        }
        
        if (isValueProvided) {
          if (selectedValues.contains(value)) {
            field.append(" checked ");
          }
        } else if (checked) {
          field.append(" checked ");
        }
        
        field.append("></input>");
        field.append("&nbsp; &nbsp; <label for='"+GrouperUtil.escapeHtml(value, true)+"_id'>");
        field.append(label);
        field.append("</label>");
        field.append("<br>");
      }
      
    }
    
    if (configItemFormElement == ConfigItemFormElement.BOOLEANCHECKBOX) {
      
      // a single boolean checkbox: checked = true, unchecked = false.
      String checkboxLabel = "";
      boolean defaultChecked = false;
      if (checkboxAttributes != null && checkboxAttributes.size() > 0) {
        MultiKey multiKey = checkboxAttributes.get(0);
        checkboxLabel = GrouperUtil.stringValue(multiKey.getKey(1));
        defaultChecked = GrouperUtil.booleanValue(multiKey.getKey(2), false);
      }
      
      boolean checked;
      if (StringUtils.isNotBlank(value)) {
        checked = GrouperUtil.booleanValue(value, false);
      } else {
        checked = defaultChecked;
      }
      
      if (readOnly) {
        field.append(checked ? "true" : "false");
      } else {
        // The visible checkbox intentionally has no "name" attribute so the ajax form serializer
        // does not transmit it as an array (checkbox values are serialized as arrays and arrive
        // server-side under "config_X[]", so a directly-named checkbox never round-trips on the
        // ajax reload). Instead its onchange writes the current state into a plain hidden field
        // named "config_X", which serializes as a simple string for both the ajax reload and the
        // final form submit, and is read server-side via request.getParameter("config_X").
        String hiddenId = "config_" + configId + "__hidden";
        field.append("<input type='checkbox' class='config-boolean-checkbox' style='" + displayClass + "' id='config_" + configId + "_id' ");
        field.append(checked ? " checked " : "");
        field.append("onchange=\"document.getElementById('" + hiddenId + "').value = this.checked ? 'true' : 'false'; " + ajaxCallback + "\"");
        field.append("></input>");
        field.append("<input type='hidden' id='" + hiddenId + "' name='config_" + configId + "' value='" + (checked ? "true" : "false") + "' />");
        if (StringUtils.isNotBlank(checkboxLabel)) {
          field.append("<span class='config-boolean-checkbox-label' style='margin-left: 8px;'>" + GrouperUtil.escapeHtml(checkboxLabel, true) + "</span>");
        }
      }
    }
    
    if (!readOnly && required) {
      field.append("<span class='requiredField' rel='tooltip' data-html='true' data-delay-show='200' data-placement='right'>*");
      field.append("</span>");
    }
    
    field.append("</span><br>");
    field.append("<span class='description'>");
    if (StringUtils.isNotBlank(helperText)) {      
      field.append(helperText);
    }
    helperText = helperText.trim();
    if (StringUtils.isNotBlank(helperTextDefaultValue)) {
      if (!helperText.endsWith(".") && !helperText.endsWith(",") && !helperText.endsWith("?") && !helperText.endsWith(">")) {
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
