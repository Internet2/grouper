/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class CustomUiUserQueryDisplayBean implements Comparable<CustomUiUserQueryDisplayBean> {

  
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * 
   */
  public CustomUiUserQueryDisplayBean() {
  }

  /**
   * label
   */
  private String label;
  
  /**
   * label
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }
  
  /**
   * label
   * @param label1 the label to set
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  /**
   * order to display on screen
   */
  private Integer order;
  
  /**
   * order to display on screen
   * @return the order
   */
  public Integer getOrder() {
    return this.order;
  }
  
  /**
   * order to display on screen
   * @param order1 the order to set
   */
  public void setOrder(Integer order1) {
    this.order = order1;
  }


  /**
   * variable name
   */
  private String variableName;

  
  /**
   * variable name
   * @return the variableName
   */
  public String getVariableName() {
    return this.variableName;
  }

  
  /**
   * variable name
   * @param variableName the variableName to set
   */
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  
  /**
   * variable type
   */
  private String variableType;
  
  /**
   * user query type
   */
  private String userQueryType;

  /**
   * description
   */
  private String description;


  /**
   * variable value
   */
  private String variableValue;
  
  /**
   * variable value
   * @return the variableValue
   */
  public String getVariableValue() {
    return this.variableValue;
  }

  
  /**
   * variable value
   * @param variableValue the variableValue to set
   */
  public void setVariableValue(String variableValue) {
    this.variableValue = variableValue;
  }

  
  /**
   * @return the variableType
   */
  public String getVariableType() {
    return this.variableType;
  }

  
  /**
   * @param variableType the variableType to set
   */
  public void setVariableType(String variableType) {
    this.variableType = variableType;
  }

  
  /**
   * @return the userQueryType
   */
  public String getUserQueryType() {
    return this.userQueryType;
  }

  
  /**
   * @param userQueryType the userQueryType to set
   */
  public void setUserQueryType(String userQueryType) {
    this.userQueryType = userQueryType;
  }

  
  /**
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }


  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(CustomUiUserQueryDisplayBean other) {
    if (other == null) {
      return 1;
    }
    if (GrouperUtil.equals(this.order,other.order)) {
      
      if (this.variableName == other.variableName) {
        return 0;
      }
      if (other.variableName == null) {
        return 1;
      }
      if (this.variableName == null) {
        return -1;
      }
     return this.variableName.compareTo(other.variableName);
    }
    if (other.order == null) {
      return 1;
    }
    if (this.order == null) {
      return -1;
    }
    return this.order.compareTo(other.order);
  }

}
