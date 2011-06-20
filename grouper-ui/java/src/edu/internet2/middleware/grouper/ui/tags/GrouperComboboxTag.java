/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Pennsylvania
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * This will generate a combobox
 * </pre>
 * @author mchyzer
 *
 */
public class GrouperComboboxTag extends SimpleTagSupport {

  /** id and class of elements, and name of combobox.  Make this unique in app */
  private String id;
  
  /** width, int, means pixels */
  private int width = -1;

  /** the operation to call when filtering */
  private String filterOperation;

  /** the default text which should appear in the combo box when drawn */
  private String comboDefaultText;
  
  /** the default value (will be submitted) which should appear in the combo box when drawn */
  private String comboDefaultValue;
  
  /** send more form element names to the filter operation, comma separated */
  private String additionalFormElementNames;
  
  /**
   * send more form element names to the filter operation, comma separated
   * @param additionalFormElementNames1
   */
  public void setAdditionalFormElementNames(String additionalFormElementNames1) {
    this.additionalFormElementNames = additionalFormElementNames1;
  }

  /**
   * the default text which should appear in the combo box when drawn
   * @param comboDefaultText1
   */
  public void setComboDefaultText(String comboDefaultText1) {
    this.comboDefaultText = comboDefaultText1;
  }

  /**
   * the default value (will be submitted) which should appear in the combo box when drawn
   * @param comboDefaultValue1
   */
  public void setComboDefaultValue(String comboDefaultValue1) {
    this.comboDefaultValue = comboDefaultValue1;
  }

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {

    StringBuilder result = new StringBuilder();
    
    //<div id="simpleMembershipUpdatePickGroupDiv" style="width:400px;"></div>
    //
    //<script> 
    //    guiRegisterDhtmlxCombo('simpleMembershipUpdatePickGroupDiv', 
    //       'simpleMembershipUpdatePickGroup', 400, 
    //        true, "../app/SimpleMembershipUpdate.filterGroups", 'defaultText', 'defaultValue' );    
    //</script> 
    
    //if it was shorthand, prefix with the full path
    if (!StringUtils.contains(this.filterOperation, "/")) {
      this.filterOperation = "../app/" + this.filterOperation;
    }
    
    result.append("<div id=\"").append(this.id + "Div").append("\"");
    if (this.width != -1) {
      //TODO this width doesnt work since the width: part isnt there, or px
      result.append(" style=\"").append(this.width).append("\"");
    }
    result.append("></div>\n");
    result.append("<script type=\"text/javascript\"> \n");
    result.append("guiRegisterDhtmlxCombo('").append(this.id).append("Div', '")
       .append(this.id).append("', ")
      .append(this.width == -1 ? null : this.width).append(", true, \"");
    result.append(this.filterOperation).append("\", ");
    if (StringUtils.isBlank(this.comboDefaultText)) {
      result.append("null");
    } else {
      result.append("'").append(GrouperUiUtils.escapeJavascript(this.comboDefaultText, true)).append("'");
    }
    result.append(", ");
    if (StringUtils.isBlank(this.comboDefaultValue)) {
      result.append("null");
    } else {
      result.append("'").append(GrouperUiUtils.escapeJavascript(this.comboDefaultValue, true)).append("'");
    }
    result.append(", ");
    if (StringUtils.isBlank(this.additionalFormElementNames)) {
      result.append("null");
    } else {
      result.append("'").append(GrouperUiUtils.escapeJavascript(this.additionalFormElementNames, true)).append("'");
    }
    result.append(");\n");
    
    
    result.append("</script>\n");

    this.getJspContext().getOut().print(result.toString());
  }
  
  /**
   * id and class of elements, and name of combobox.  Make this unique in app
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * width, int, means pixels
   * @param width1 the width to set
   */
  public void setWidth(int width1) {
    this.width = width1;
  }
  
  /**
   * the operation to call when filtering
   * @param filterOperation1 the filterOperation to set
   */
  public void setFilterOperation(String filterOperation1) {
    this.filterOperation = filterOperation1;
  }

}
