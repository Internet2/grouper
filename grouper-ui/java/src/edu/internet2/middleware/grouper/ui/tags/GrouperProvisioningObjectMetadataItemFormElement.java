package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.util.ArrayList;
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
  private String value;
  
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


  
  public String getValue() {
    return value;
  }


  
  public void setValue(String value) {
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
    if (readOnly) {
      field.append(GrouperUtil.escapeHtml(value, true));
      displayClass = " display: none; ";
    }
    
    GrouperProvisioningObjectMetadataItemFormElementType configItemFormElement = GrouperProvisioningObjectMetadataItemFormElementType.valueOfIgnoreCase(formElementType, true);
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.TEXT) {
      
      field.append(
          "<input style='width:30em; "+ displayClass + "' type='text' id='"+name+"_id' name='" + name + "'");
      if (value != null) {
        field.append(" value = '"+GrouperUtil.escapeHtml(value, true)+"'");
      }
      field.append("></input>");
      
    }
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.TEXTAREA) {
            
      field.append("<textarea style='width:30em; "+ displayClass + "' cols='20' rows='3' id='"+name+"_id' name='"
          + name + "'>");
      if (value != null) {
        field.append(GrouperUtil.escapeHtml(value, true));
      }
      field.append("</textarea>");
      
    }
    
    if (configItemFormElement == GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN) {
      
      field.append("<select style='width:30em; "+ displayClass + "' id='"+name+"_id' name='"+name+"' ");
      
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
    
    if (!readOnly && required) {
      field.append("<span class='requiredField' rel='tooltip' data-html='true' data-delay-show='200' data-placement='right'>*");
      field.append("</span>");
    }
    
    field.append("<br>");
    field.append("<span class='description'>");
    if (StringUtils.isNotBlank(descriptionKey)) {
      
      String description = GrouperTextContainer.textOrNull(descriptionKey);
      if (StringUtils.isBlank(description)) {
        description = descriptionKey;
      }
      
      field.append(description);
    }
    
    field.append("</span>");
    
    field.append("</td>");
    field.append("</tr>");
    
    this.getJspContext().getOut().print(field.toString());
  }

}
