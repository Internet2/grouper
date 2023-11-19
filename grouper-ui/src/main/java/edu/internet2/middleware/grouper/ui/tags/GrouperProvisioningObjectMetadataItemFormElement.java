package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperProvisioningObjectMetadataItemFormElement extends SimpleTagSupport {
  
  /**
   * camelCase alphaNumeric unique name per provisioner
   */
  private String name;
  
  /**
   * value to show
   */
  private Object value;
  
  /**
   * is the field required
   */
  private boolean required = false;
  
  /**
   * is the field read only
   */
  private boolean readOnly = false;
  
  /**
   * form element type (eg: TEXT, TEXTAREA)
   */
  private String formElementType;

  /**
   * label key to display to the left of the field 
   */
  private String labelKey;
  
  /**
   * helper text key to display under the field
   */
  private String descriptionKey;

  /**
   * only applicable to dropdown
   */
  private List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
  
  /**
   * ajaxCallback for onchange etc
   */
  private String ajaxCallback;


  
  public String getName() {
    return name;
  }


  
  public void setName(String name) {
    this.name = name;
  }


  
  public Object getValue() {
    return value;
  }


  
  public void setValue(Object value) {
    this.value = value;
  }


  
  public boolean isRequired() {
    return required;
  }


  
  public void setRequired(boolean required) {
    this.required = required;
  }


  
  public String getFormElementType() {
    return formElementType;
  }


  
  public void setFormElementType(String formElementType) {
    this.formElementType = formElementType;
  }
  
  public List<MultiKey> getValuesAndLabels() {
    return valuesAndLabels;
  }


  
  public void setValuesAndLabels(List<MultiKey> valuesAndLabels) {
    this.valuesAndLabels = valuesAndLabels;
  }



  
  public String getAjaxCallback() {
    return ajaxCallback;
  }

  
  public void setAjaxCallback(String ajaxCallback) {
    this.ajaxCallback = ajaxCallback;
  }
  
  
  public String getLabelKey() {
    return labelKey;
  }



  
  public void setLabelKey(String labelKey) {
    this.labelKey = labelKey;
  }



  
  public String getDescriptionKey() {
    return descriptionKey;
  }



  
  public void setDescriptionKey(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }
  

  
  public boolean isReadOnly() {
    return readOnly;
  }



  
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }



  /**
   * html to render
   */
  @Override
  public void doTag() throws JspException, IOException {
   
    StringBuilder field = new StringBuilder();
    
    field.append("<tr>");
    field.append("<td style='vertical-align: top; white-space: nowrap;'>");
    field.append("<label for='"+name+"_id'><strong>");
    
    String label = GrouperTextContainer.textOrNull(labelKey);
    if (StringUtils.isBlank(label)) {
      label = labelKey;
    }
    field.append(label);
    field.append("</strong></label></td>");
    
    field.append("<td style='vertical-align: top; white-space: nowrap;' >");
    
    String displayClass = "";
    GrouperProvisioningObjectMetadataItemFormElementType configItemFormElement = GrouperProvisioningObjectMetadataItemFormElementType.valueOfIgnoreCase(formElementType, true);

    String valueToDisplay = null;
    
    if (this.value != null) {
      if (this.value instanceof Collection) {
        Collection<Object> valueCollection = (Collection<Object>)this.value;
        valueToDisplay = GrouperUtil.collectionToString(valueCollection);
      } else {
        valueToDisplay = GrouperUtil.stringValue(this.value);
      }
    }
    
    if (this.readOnly) {
      
      if (configItemFormElement != GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON && 
          configItemFormElement != GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN && 
          configItemFormElement != GrouperProvisioningObjectMetadataItemFormElementType.CHECKBOX) {
        
        if (valueToDisplay != null) {
          field.append(GrouperUtil.escapeHtml(valueToDisplay, true) + " ");
        }
      }
      
      displayClass = " display: none; ";
    }
    
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.TEXT) {
      
      field.append(
          "<input style='width:30em; "+ displayClass + "' type='text' id='"+name+"_id' name='" + name + "'");
      if (valueToDisplay != null) {
        field.append(" value = '"+GrouperUtil.escapeHtml(valueToDisplay, true)+"'");
      }
      field.append("></input>");
      
    }
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.TEXTAREA) {
            
      field.append("<textarea style='width:30em; "+ displayClass + "' cols='20' rows='3' id='"+name+"_id' name='"
          + name + "'>");
      if (valueToDisplay != null) {
        field.append(GrouperUtil.escapeHtml(valueToDisplay, true));
      }
      field.append("</textarea>");
      
    }
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN) {
      
      if (readOnly) {
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = (String) multiKey.getKey(0);
          String optionValue = (String) multiKey.getKey(1);
          
         
          boolean selected =  GrouperUtil.equals(key, value);
          if (!selected) {
            continue;
          }
          field.append("<span style='margin-right: 10px;'>"+optionValue+"</span>"); 
        }
      } else {
        field.append("<select style='width:30em; "+ displayClass + "' id='"+name+"_id' name='"+name+"' ");
        
        field.append("onchange=\""+ajaxCallback+"\"");
        field.append(">");
        
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = (String) multiKey.getKey(0);
          String optionValue = (String) multiKey.getKey(1);
          
          boolean selected = GrouperUtil.equals(key, valueToDisplay);
          
          field.append("<option value='"+key+"'" + (selected ? " selected='selected'" : "") + ">");
          field.append(GrouperUtil.escapeHtml(optionValue, true));
          field.append("</option>");
        }
        
        field.append("</select>");
      }
      
    }
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON) {
      
      boolean firstOption = true;
      
      if (readOnly) {
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = (String) multiKey.getKey(0);
          String radioButtonValue = (String) multiKey.getKey(1);
          boolean checked = GrouperUtil.equals(key, valueToDisplay);
          
          // maybe the value is boolean
          try {
            Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(key);
            if (!checked) {
              checked = GrouperUtil.equals(booleanObjectValue, valueToDisplay);
            }
          } catch (Exception e) {}
          
          if (!checked) {
            continue;
          }
          field.append("<span style='margin-right: 10px;'>"+radioButtonValue+"</span>"); 
        }
      } else {
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = (String) multiKey.getKey(0);
          String radioButtonValue = (String) multiKey.getKey(1);
          boolean checked = GrouperUtil.equals(key, valueToDisplay);
          
          // maybe the value is boolean
          try {
            Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(key);
            if (!checked) {
              checked = GrouperUtil.equals(booleanObjectValue, valueToDisplay);
            }
          } catch (Exception e) {}

          field.append("<input type='radio' style='margin-right:3px;margin-top:0px; "+ displayClass+"' id='"+name+"_id' name='"+name+"' value='"+key+"' ");
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
      
    }
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.CHECKBOX) {
      
      boolean firstOption = true;
      
      if (readOnly) {
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = (String) multiKey.getKey(0);
          String radioButtonValue = (String) multiKey.getKey(1);
          
          if (this.value instanceof List) {
            
              List<Object> selectedValues = (List)value;
              boolean checked = selectedValues.contains(key);
              if (!checked) {
                continue;
              }
            
            field.append("<span style='margin-right: 10px;'>"+radioButtonValue+"</span>"); 
          }
        }
      } else {
        for (MultiKey multiKey: valuesAndLabels) {
          
          String key = (String) multiKey.getKey(0);
          String checkBoxValue = (String) multiKey.getKey(1);
          
          boolean checked = false;
          
          if (this.value instanceof List) {
            List<Object> selectedValues = (List)value;
            checked = selectedValues.contains(key);
          }
          
          field.append("<input type='checkbox' style='margin-right:3px;margin-top:0px; "+ displayClass+"' id='"+name+"_id' name='"+name+"' value='"+key+"' ");
          field.append(checked ? " checked ": "");
          field.append(">");
          field.append("</input>");
          field.append("<span style='margin-right: 20px;'>"+checkBoxValue+"</span>"); 
          
        }

      }
      
    }
    
    if (!readOnly && required) {
      field.append("<span class='requiredField' rel='tooltip' data-html='true' data-delay-show='200' data-placement='right'>*");
      field.append("</span>");
    }
    
    field.append("<br>");
    field.append("<span class='description'>");
    
    String description = "";
    
    if (StringUtils.isNotBlank(descriptionKey)) {
      
      description = GrouperTextContainer.textOrNull(descriptionKey);
      if (StringUtils.isBlank(description)) {
        description = descriptionKey;
      }
      
    }
    
    if (this.value != null && this.value instanceof Collection) {
      description = description + ". "+GrouperTextContainer.textOrNull("metadataValueCollectionTypeSuffix");
    }
    
    field.append(description);
    
    
    field.append("</span>");
    
    field.append("</td>");
    field.append("</tr>");
    
    this.getJspContext().getOut().print(field.toString());
  }

}
