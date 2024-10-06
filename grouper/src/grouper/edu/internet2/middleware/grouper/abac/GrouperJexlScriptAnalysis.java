package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;

public class GrouperJexlScriptAnalysis {

  public GrouperJexlScriptAnalysis() {
  }

  private List<GrouperJexlScriptPart> grouperJexlScriptParts = new ArrayList<GrouperJexlScriptPart>();

  
  public List<GrouperJexlScriptPart> getGrouperJexlScriptParts() {
    return grouperJexlScriptParts;
  }
  
  public void setGrouperJexlScriptParts(
      List<GrouperJexlScriptPart> grouperJexlScriptParts) {
    this.grouperJexlScriptParts = grouperJexlScriptParts;
  }
  
  /**
   * 
   */
  @Override
  public String toString() {

    StringBuilder result = new StringBuilder();

    if (!StringUtils.isBlank(this.getErrorMessage())) {
      result.append("Error: " + this.getErrorMessage() + "\n");
    }
    
    if (!StringUtils.isBlank(this.getWarningMessage())) {
      result.append("Warning: " + this.getWarningMessage() + "\n");
    }
    
    for (int i=0; i<grouperJexlScriptParts.size(); i++) {
      result.append(i).append(": ").append(grouperJexlScriptParts.get(i)).append("\n");
    }
    return result.toString();
  }
  
  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  //TODO should have multiple errors or warnings or should append?
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  
  private String warningMessage;

  public String getWarningMessage() {
    return warningMessage;
  }

  public void setWarningMessage(String warningMessage) {
    this.warningMessage = warningMessage;
  }

  private GrouperDataEngine grouperDataEngine;
  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }
  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }
  
}
